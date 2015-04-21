/**
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2012 - 2015 ForgeRock AS.
 */

#include "platform.h"
#include "am.h"
#include "thread.h"
#include "utility.h"
#include "log.h"

#if defined(__sun) && defined(__SunOS_5_10)
#include <port.h>
static int port;
typedef void (timer_callback) (void *);
#else
typedef void (timer_callback) (union sigval);
#endif

typedef struct {
    unsigned long instance_id;
    int ping_interval;
    int ping_ok_count;
    int ping_fail_count;
    int default_set_size;
    int url_size;
    int *default_set;
    char **url_list;
    void *data;
} naming_validator_t;

typedef struct {
    unsigned long instance_id;
    int ok;
    int fail;
    int run;
    int ping_ok_count;
    int ping_fail_count;
    char *url;
} naming_url_t;

typedef struct {
    int size;
    naming_url_t *list;
} naming_status_t;

typedef struct {
    unsigned long instance_id;
    int idx;
    char *url;
} naming_validator_int_t;

#ifdef _WIN32
static CRITICAL_SECTION mutex;
static HANDLE wthr;
#else
static pthread_mutex_t mutex;
static pthread_t wthr;
#endif
static volatile int keep_going = 1;
static naming_status_t *nlist = NULL;

static void store_index_value(int ix, int *map, unsigned long instance_id) {
    int i = map[ix];
    set_valid_url_index(instance_id, i);
}

static void *url_watchdog(void *arg) {
    int i, current_ok, default_ok, current_fail, next_ok, first_run = 1;
    naming_validator_t *v = (naming_validator_t *) arg;
    while (keep_going) {
        int current_index = 0;
        /* for the initial run persisted index might not yet be available */
        if (!first_run) {
            sleep(v->ping_interval);
        }
        first_run = 0;
        /* fetch current index value */
        current_index = get_valid_url_index(v->instance_id);
        if (current_index < 0 || current_index > nlist->size) {
            AM_LOG_WARNING(v->instance_id, "naming_validator(): invalid current index value, defaulting to %s", v->url_list[0]);
            store_index_value(0, v->default_set, v->instance_id);
            current_index = 0;
        } else {
            /* map stored index to our ordered list index */
            for (i = 0; i < v->default_set_size; i++) {
                if (current_index == v->default_set[i]) {
                    current_index = i;
                    break;
                }
            }
        }
        /* check if current index value is valid; double check whether default has 
         * become valid again (ping.ok.count) and fail-back to it if so */
#ifdef _WIN32
        EnterCriticalSection(&mutex);
#else
        pthread_mutex_lock(&mutex);
#endif
        current_ok = nlist->list[current_index].ok;
        current_fail = nlist->list[current_index].fail;
        default_ok = nlist->list[0].ok;
#ifdef _WIN32
        LeaveCriticalSection(&mutex);
#else
        pthread_mutex_unlock(&mutex);
#endif
        if (current_ok > 0) {
            if (current_index > 0 && default_ok >= v->ping_ok_count) {
                store_index_value(0, v->default_set, v->instance_id);
                AM_LOG_DEBUG(v->instance_id, "naming_validator(): fail-back to %s", v->url_list[0]);
            } else {
                AM_LOG_DEBUG(v->instance_id, "naming_validator(): continue with %s", v->url_list[current_index]);
            }
            continue;
        }
        /* current index is not valid; check its ping.miss.count */
        if (current_ok == 0 && current_fail <= v->ping_fail_count) {
            AM_LOG_DEBUG(v->instance_id, "naming_validator(): still staying with %s", v->url_list[current_index]);
            continue;
        }
        /* find next valid index value to fail-over to */
        next_ok = 0;
#ifdef _WIN32
        EnterCriticalSection(&mutex);
#else
        pthread_mutex_lock(&mutex);
#endif
        for (i = 0; i < nlist->size; i++) {
            if (nlist->list[i].ok > 0) {
                next_ok = nlist->list[i].ok;
                break;
            }
        }
        default_ok = nlist->list[0].ok;
#ifdef _WIN32
        LeaveCriticalSection(&mutex);
#else
        pthread_mutex_unlock(&mutex);
#endif
        if (next_ok == 0) {
            AM_LOG_WARNING(v->instance_id, "naming_validator(): none of the values are valid, defaulting to %s", v->url_list[0]);
            store_index_value(0, v->default_set, v->instance_id);
            continue;
        }
        if (current_index > 0 && default_ok >= v->ping_ok_count) {
            AM_LOG_DEBUG(v->instance_id, "naming_validator(): fail-back to %s", v->url_list[0]);
            store_index_value(0, v->default_set, v->instance_id);
        } else {
            AM_LOG_DEBUG(v->instance_id, "naming_validator(): fail-over to %s", v->url_list[i]);
            store_index_value(i, v->default_set, v->instance_id);
        }
    }
    return 0;
}

static void *url_validator(void *arg) {
    int validate_status, httpcode = 0;
    naming_validator_int_t *v = (naming_validator_int_t *) arg;
    if (!keep_going) return 0;
    validate_status = am_url_validate(v->instance_id, v->url, NULL, &httpcode);
#ifdef _WIN32
    EnterCriticalSection(&mutex);
#else
    pthread_mutex_lock(&mutex);
#endif
    if (validate_status == AM_SUCCESS) {
        AM_LOG_DEBUG(v->instance_id, "url_validator(%d): %s validation succeeded", v->idx, v->url);
        if ((nlist->list[v->idx].ok)++ > nlist->list[v->idx].ping_ok_count)
            nlist->list[v->idx].ok = nlist->list[v->idx].ping_ok_count;
        nlist->list[v->idx].fail = 0;
    } else {
        AM_LOG_WARNING(v->instance_id, "url_validator(%d): %s validation failed with %s, http status %d", v->idx,
                v->url, am_strerror(validate_status), httpcode);
        if ((nlist->list[v->idx].fail)++ > nlist->list[v->idx].ping_fail_count)
            nlist->list[v->idx].fail = nlist->list[v->idx].ping_fail_count;
        nlist->list[v->idx].ok = 0;
    }
    nlist->list[v->idx].run = 0;
#ifdef _WIN32
    LeaveCriticalSection(&mutex);
#else
    pthread_mutex_unlock(&mutex);
#endif
    free(v);
    return 0;
}

#ifdef _WIN32

static VOID CALLBACK callback(PVOID lpParam, BOOLEAN TimerOrWaitFired) {
    int i, j;
    naming_validator_t *v = (naming_validator_t *) lpParam;
#else 

#if (defined(__sun) && defined(__SunOS_5_10)) || defined(__APPLE__)

static void callback(void *ta) {
    int i, j;
    naming_validator_t *v = (naming_validator_t *) ta;
#else

static void callback(union sigval si) {
    int i, j;
    naming_validator_t *v = (naming_validator_t *) si.sival_ptr;
#endif

#endif
    for (i = 0; i < v->url_size; i++) {
#ifdef _WIN32
        HANDLE vthr;
#else
        pthread_t vthr;
#endif
        naming_validator_int_t *arg = NULL;
        if (!keep_going) return;
#ifdef _WIN32
        EnterCriticalSection(&mutex);
#else
        pthread_mutex_lock(&mutex);
#endif
        j = nlist->list[i].run;
#ifdef _WIN32
        LeaveCriticalSection(&mutex);
#else
        pthread_mutex_unlock(&mutex);
#endif
        if (j == 1) {
            AM_LOG_DEBUG(v->instance_id, "naming_validator(): validate is already running for %s", v->url_list[i]);
            continue;
        }
        arg = (naming_validator_int_t *) malloc(sizeof (naming_validator_int_t));
        if (arg == NULL) {
            AM_LOG_ERROR(v->instance_id, "naming_validator(): timer callback memory allocation error");
            return;
        }
        arg->instance_id = v->instance_id;
        arg->url = v->url_list[i];
        arg->idx = i;
        if (!keep_going) return;
#ifdef _WIN32
        vthr = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE) url_validator, arg, 0, NULL);
        EnterCriticalSection(&mutex);
#else
        pthread_create(&vthr, NULL, url_validator, arg);
        pthread_mutex_lock(&mutex);
#endif
        nlist->list[i].run = 1;
#ifdef _WIN32
        LeaveCriticalSection(&mutex);
#else
        pthread_mutex_unlock(&mutex);
#endif
    }
}

#if !defined(_WIN32) && !defined(__APPLE__)

#if defined(__sun) && defined(__SunOS_5_10)

static void *evtimer_listener(void *arg) {
    port_event_t ev;
    while (keep_going) {
        if (port_get(port, &ev, NULL) < 0) {
            break;
        }
        if (ev.portev_source == PORT_SOURCE_TIMER) {
            naming_validator_t *ptr = (naming_validator_t *) ev.portev_user;
            callback(ptr);
        } else
            break;
    }
    pthread_exit(NULL);
}

#endif

static int set_timer(timer_t * timer_id, float delay, float interval, timer_callback *func, void *data) {
    int status = 0;
    struct itimerspec ts;
    struct sigevent se;
#if defined(__sun) && defined(__SunOS_5_10)
    pthread_t pthr;
    port_notify_t pnotif;
    pnotif.portnfy_port = port;
    pnotif.portnfy_user = data;
    se.sigev_notify = SIGEV_PORT;
    se.sigev_value.sival_ptr = &pnotif;
#else
    se.sigev_notify = SIGEV_THREAD;
    se.sigev_value.sival_ptr = data;
    se.sigev_notify_function = func;
    se.sigev_notify_attributes = NULL;
#endif
    status = timer_create(CLOCK_REALTIME, &se, timer_id);
    ts.it_value.tv_sec = abs(delay);
    ts.it_value.tv_nsec = (delay - abs(delay)) * 1e09;
    ts.it_interval.tv_sec = abs(interval);
    ts.it_interval.tv_nsec = (interval - abs(interval)) * 1e09;
    status = timer_settime(*timer_id, 0, &ts, 0);
#if defined(__sun) && defined(__SunOS_5_10)    
    pthread_create(&pthr, NULL, evtimer_listener, NULL);
#endif
    return status;
}

#endif

#ifdef __APPLE__

static void *evtimer_listener(void *arg) {
    int n, kq = -1;
    struct kevent ch, ev;
    naming_validator_t *v = (naming_validator_t *) arg;
    if (v == NULL || v->data == NULL) return NULL;

    kq = *((int *) v->data);
    EV_SET(&ch, 1, EVFILT_TIMER, EV_ADD | EV_ENABLE, NOTE_SECONDS, v->ping_interval, NULL);
    while (keep_going) {
        n = kevent(kq, &ch, 1, &ev, 1, NULL);
        if (n <= 0 || ev.flags & EV_ERROR) break;
        callback(v);
    }
    pthread_exit(NULL);
}

#endif

void stop_naming_validator() {
    keep_going = 0;
}

int naming_validator(void *arg) {
    int i;
#ifdef _WIN32
    HANDLE tick_q = NULL;
    HANDLE tick = NULL;
#elif defined(__APPLE__)    
    int tick_q = -1;
    pthread_t tick_thr;
#else
    timer_t tick;
#endif
    naming_validator_t *v = (naming_validator_t *) arg;
    if (v->ping_interval == 0) return 0;

    nlist = (naming_status_t *) malloc(sizeof (naming_status_t));
    if (nlist == NULL) {
        AM_LOG_ERROR(v->instance_id, "naming_validator(): memory allocation error");
        return AM_ENOMEM;
    }

    nlist->list = (naming_url_t *) calloc(v->url_size, sizeof (nlist->list[0]));
    if (nlist->list == NULL) {
        free(nlist);
        AM_LOG_ERROR(v->instance_id, "naming_validator(): memory allocation error");
        return AM_ENOMEM;
    }

#ifdef _WIN32
    InitializeCriticalSection(&mutex);
#else
    pthread_mutex_init(&mutex, NULL);
#endif
    nlist->size = v->url_size;
    for (i = 0; i < v->url_size; i++) {
        nlist->list[i].instance_id = v->instance_id;
        nlist->list[i].ok = 0;
        nlist->list[i].fail = 0;
        nlist->list[i].ping_ok_count = v->ping_ok_count;
        nlist->list[i].ping_fail_count = v->ping_fail_count;
        nlist->list[i].run = 0;
        nlist->list[i].url = v->url_list[i];
    }
#ifdef _WIN32
    tick_q = CreateTimerQueue();
    CreateTimerQueueTimer(&tick, tick_q,
            (WAITORTIMERCALLBACK) callback, arg, 1000,
            (v->ping_interval * 1000), WT_EXECUTELONGFUNCTION);
    wthr = CreateThread(NULL, 0,
            (LPTHREAD_START_ROUTINE) url_watchdog, arg, 0, NULL);
#else
#if defined(__sun) && defined(__SunOS_5_10) 
    if ((port = port_create()) == -1) {
        AM_LOG_ERROR(v->instance_id, "naming_validator(): port_create failed");
    }
#endif
#ifdef __APPLE__    
    if ((tick_q = kqueue()) == -1) {
        AM_LOG_ERROR(v->instance_id, "naming_validator(): kqueue failed");
    }
    v->data = &tick_q;
    pthread_create(&tick_thr, NULL, evtimer_listener, arg);
#else
    set_timer(&tick, 1, v->ping_interval, callback, arg);
#endif
    pthread_create(&wthr, NULL, url_watchdog, arg);
#endif

    while (keep_going) {
        sleep(1);
    }

#ifdef _WIN32
    WaitForSingleObject(wthr, INFINITE);
    DeleteTimerQueue(tick_q);
    DeleteCriticalSection(&mutex);
#else
    pthread_join(wthr, NULL);
    pthread_mutex_destroy(&mutex);
#ifdef __APPLE__
    pthread_join(tick_thr, NULL);
    close(tick_q);
#else
    timer_delete(tick);
#endif
#if defined(__sun) && defined(__SunOS_5_10)
    close(port);
#endif
#endif
    free(nlist->list);
    free(nlist);
    return AM_SUCCESS;
}
