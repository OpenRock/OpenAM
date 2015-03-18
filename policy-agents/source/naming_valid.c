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
#include "thread.h"

#if defined(__sun) && defined(__SunOS_5_10)
#include <port.h>
static int port;
typedef void (timer_callback) (void *);
#else
typedef void (timer_callback) (union sigval);
#endif

typedef struct {
    unsigned long ping_interval;
    unsigned long ping_ok_count;
    unsigned long ping_fail_count;
    int instance_id;
    int default_set_size;
    int *default_set;
    int url_size;
    char **url_list;
    //void (*log)(const char *, ...);
    //void (*debug)(const char *, ...);
    int (*validate)(const char *, const char **, int *httpcode);
} naming_validator_t;

typedef struct {
    char *url;
    int ok;
    int fail;
    int run;
    unsigned long ping_ok_count;
    unsigned long ping_fail_count;
} naming_url_t;

typedef struct {
    int size;
    naming_url_t *list;
} naming_status_t;

typedef struct {
    char *url;
    int idx;
    //void (*log)(const char *, ...);
    //void (*debug)(const char *, ...);
    int (*validate)(const char *, const char **, int *httpcode);
} naming_validator_int_t;

#ifdef OFF1 //TODO: mac does not compile

static volatile int keep_going = 1;
static am_thread_t wthr;
static am_mutex_t mutex;
static naming_status_t *nlist = NULL;

static void store_index_value(int ix, int *map, int iid) {
    char idx[8];
    int i = map[ix];
    snprintf(idx, sizeof (idx), "%d", i);
    write_naming_value(AM_NAMING_LOCK, idx, iid);
}

static void *url_watchdog(void *arg) {
    int i, current_ok, default_ok, current_fail, next_ok, first_run = 1;
    naming_validator_t *v = (naming_validator_t *) arg;
    while (keep_going) {
        int current_index = 0;
        char *current_value = NULL;
        /* for initial run persisted index might not yet be available */
        if (!first_run) {
            sleep(v->ping_interval);
        }
        first_run = 0;
        /* fetch current index value */
        current_value = read_naming_value(AM_NAMING_LOCK, v->instance_id);
        if (current_value != NULL) {
            current_index = strtol(current_value, NULL, 10);
            if (current_index < 0 || current_index > nlist->size || errno == ERANGE) {
                v->log("naming_validator(): invalid current index value, defaulting to %s", v->url_list[0]);
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
            free(current_value);
        } else {
            v->log("naming_validator(): failed to read current index value, defaulting to %s", v->url_list[0]);
            store_index_value(0, v->default_set, v->instance_id);
        }
        /* check if current index value is valid; double check whether default has 
         * become valid again (ping.ok.count) and fail-back to it if so */
        MUTEX_LOCK(mutex);
        current_ok = nlist->list[current_index].ok;
        current_fail = nlist->list[current_index].fail;
        default_ok = nlist->list[0].ok;
        MUTEX_UNLOCK(mutex);
        if (current_ok > 0) {
            if (current_index > 0 && default_ok >= v->ping_ok_count) {
                store_index_value(0, v->default_set, v->instance_id);
                v->log("naming_validator(): fail-back to %s", v->url_list[0]);
            } else {
                v->log("naming_validator(): continue with %s", v->url_list[current_index]);
            }
            continue;
        }
        /* current index is not valid; check its ping.miss.count */
        if (current_ok == 0 && current_fail <= v->ping_fail_count) {
            v->log("naming_validator(): still staying with %s", v->url_list[current_index]);
            continue;
        }
        /* find next valid index value to fail-over to */
        next_ok = 0;
        MUTEX_LOCK(mutex);
        for (i = 0; i < nlist->size; i++) {
            if (nlist->list[i].ok > 0) {
                next_ok = nlist->list[i].ok;
                break;
            }
        }
        default_ok = nlist->list[0].ok;
        MUTEX_UNLOCK(mutex);
        if (next_ok == 0) {
            v->log("naming_validator(): none of the values are valid, defaulting to %s", v->url_list[0]);
            store_index_value(0, v->default_set, v->instance_id);
            continue;
        }
        if (current_index > 0 && default_ok >= v->ping_ok_count) {
            v->log("naming_validator(): fail-back to %s", v->url_list[0]);
            store_index_value(0, v->default_set, v->instance_id);
        } else {
            v->log("naming_validator(): fail-over to %s", v->url_list[i]);
            store_index_value(i, v->default_set, v->instance_id);
        }
    }
    return 0;
}

static void *url_validator(void *arg) {
    const char *status_message;
    int validate_status, httpcode = 0;
    naming_validator_int_t *v = (naming_validator_int_t *) arg;
    if (!keep_going) return 0;
    validate_status = v->validate(v->url, &status_message, &httpcode);
    MUTEX_LOCK(mutex);
    if (validate_status == 0) {
        v->log("url_validator(%d): %s validation succeeded", v->idx, v->url);
        if ((nlist->list[v->idx].ok)++ > nlist->list[v->idx].ping_ok_count)
            nlist->list[v->idx].ok = nlist->list[v->idx].ping_ok_count;
        nlist->list[v->idx].fail = 0;
    } else {
        v->log("url_validator(%d): %s validation failed with %s (%d), http status (%d)", v->idx,
                v->url, status_message, validate_status, httpcode);
        if ((nlist->list[v->idx].fail)++ > nlist->list[v->idx].ping_fail_count)
            nlist->list[v->idx].fail = nlist->list[v->idx].ping_fail_count;
        nlist->list[v->idx].ok = 0;
    }
    nlist->list[v->idx].run = 0;
    MUTEX_UNLOCK(mutex);
    free(v);
    return 0;
}

#ifdef _WIN32

static VOID CALLBACK callback(PVOID lpParam, BOOLEAN TimerOrWaitFired) {
    int i, j;
    naming_validator_t *v = (naming_validator_t *) lpParam;
#else 

#if defined(__sun) && defined(__SunOS_5_10)

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
        THREAD vthr;
        naming_validator_int_t *arg = NULL;
        if (!keep_going) return;
        MUTEX_LOCK(mutex);
        j = nlist->list[i].run;
        MUTEX_UNLOCK(mutex);
        if (j == 1) {
            v->log("naming_validator(): validate is already running for %s", v->url_list[i]);
            continue;
        }
        arg = (naming_validator_int_t *) malloc(sizeof (naming_validator_int_t));
        if (arg == NULL) {
            v->log("naming_validator(): timer callback memory allocation error");
            return;
        }
        arg->log = v->log;
        arg->debug = v->debug;
        arg->validate = v->validate;
        arg->url = v->url_list[i];
        arg->idx = i;
        if (!keep_going) return;
        THREAD_CREATE(vthr, url_validator, arg);
        MUTEX_LOCK(mutex);
        nlist->list[i].run = 1;
        MUTEX_UNLOCK(mutex);
    }
}

#ifndef _WIN32

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

void stop_naming_validator() {
    keep_going = 0;
}

void *naming_validator(void *arg) {
    int i, status;
#ifdef _WIN32
    HANDLE tick_q = NULL;
    HANDLE tick = NULL;
#else
    timer_t tick;
#endif
    naming_validator_t *v = (naming_validator_t *) arg;
    if (v->validate == NULL) return 0;
    nlist = (naming_status_t *) malloc(sizeof (naming_status_t));
    if (nlist != NULL) {
        nlist->list = (naming_url_t *) calloc(v->url_size, sizeof (nlist->list[0]));
        if (nlist->list != NULL) {
            MUTEX_CREATE(mutex);
            nlist->size = v->url_size;
            for (i = 0; i < v->url_size; i++) {
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
                    (WAITORTIMERCALLBACK) callback, arg, 1000, (v->ping_interval * 1000), WT_EXECUTELONGFUNCTION);
#else
#if defined(__sun) && defined(__SunOS_5_10) 
            if ((port = port_create()) == -1) {
                v->log("naming_validator(): port_create failed");
            }
#endif
            status = set_timer(&tick, 1, v->ping_interval, callback, arg);
#endif
            THREAD_CREATE(wthr, url_watchdog, arg);
            while (keep_going) {
                sleep(1);
            }
            THREAD_WAIT(wthr);
#ifdef _WIN32
            DeleteTimerQueue(tick_q);
#else
            timer_delete(tick);
#if defined(__sun) && defined(__SunOS_5_10)
            close(port);
#endif
#endif
            free(nlist->list);
            MUTEX_DELETE(mutex);
        }
        free(nlist);
    } else {
        v->log("naming_validator(): memory allocation error");
    }
    return 0;
}

#endif
