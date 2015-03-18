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
 * Copyright 2014 - 2015 ForgeRock AS.
 */

#include "platform.h"
#include "am.h"
#include "utility.h"
#include "version.h"
#include "thread.h"

#ifndef AM_MAX_THREADS_POOL
#define AM_MAX_THREADS_POOL 4
#endif

#ifdef _WIN32
static INIT_ONCE worker_pool_initialized = INIT_ONCE_STATIC_INIT;
static TP_CALLBACK_ENVIRON worker_env;
static PTP_POOL worker_pool = NULL;
static PTP_CLEANUP_GROUP worker_pool_cleanup = NULL;
#else
static pthread_once_t worker_pool_initialized = PTHREAD_ONCE_INIT;

struct am_threadpool_work {
    void (*routine) (void *, void *);
    void *arg;
    struct am_threadpool_work *next;
};

struct am_threadpool {
    int num_threads;
    int size;
    pthread_t *threads;
    struct am_threadpool_work *head;
    struct am_threadpool_work *tail;
    pthread_mutex_t lock;
    pthread_cond_t not_empty;
    pthread_cond_t empty;
    char shutdown;
    char dont_accept;
};

static struct am_threadpool *worker_pool = NULL;

static void *do_work(void *arg) {
    struct am_threadpool *pool = (struct am_threadpool *) arg;
    struct am_threadpool_work *cur;

    while (1) {
        pool->size = pool->size;
        pthread_mutex_lock(&(pool->lock));
        while (pool->size == 0) {
            struct timeval now = {0, 0};
            struct timespec ts = {0, 0};
            gettimeofday(&now, NULL);
            ts.tv_sec = now.tv_sec + 1;
            ts.tv_nsec = now.tv_usec * 1000;
            if (pthread_cond_timedwait(&pool->not_empty, &pool->lock, &ts) == ETIMEDOUT) {
                if (pool->shutdown) {
                    pthread_mutex_unlock(&(pool->lock));
                    return NULL;
                }
            }
        }

        cur = pool->head;
        pool->size--;
        if (pool->size == 0) {
            pool->head = NULL;
            pool->tail = NULL;
        } else {
            pool->head = cur->next;
        }
        if (pool->size == 0 && !pool->shutdown) {
            pthread_cond_broadcast(&(pool->empty));
        }
        pthread_mutex_unlock(&(pool->lock));

        (cur->routine) (NULL /*used with _WIN32 only*/, cur->arg); /* do the actual work */

        free(cur);
    }

    return NULL;
}

#endif

static
#ifdef _WIN32
BOOL CALLBACK
#else
void
#endif
create_threadpool(
#ifdef _WIN32
        PINIT_ONCE io, PVOID p, PVOID *c
#endif
        ) {
#ifdef _WIN32

    worker_pool = CreateThreadpool(NULL);
    if (worker_pool == NULL) {
        return FALSE;
    }

    SetThreadpoolThreadMaximum(worker_pool, AM_MAX_THREADS_POOL);
    SetThreadpoolThreadMinimum(worker_pool, 2);

    InitializeThreadpoolEnvironment(&worker_env);
    SetThreadpoolCallbackPool(&worker_env, worker_pool);
    worker_pool_cleanup = CreateThreadpoolCleanupGroup();
    if (worker_pool_cleanup == NULL) {
        DestroyThreadpoolEnvironment(&worker_env);
        CloseThreadpool(worker_pool);
        worker_pool = NULL;
        return FALSE;
    } else {
        SetThreadpoolCallbackCleanupGroup(&worker_env, worker_pool_cleanup, NULL);
    }
    return TRUE;

#else
    int i;

    worker_pool = (struct am_threadpool *) malloc(sizeof (struct am_threadpool));
    if (worker_pool == NULL) {
        return;
    }

    memset(worker_pool, 0, sizeof (struct am_threadpool));

    worker_pool->threads = (pthread_t *) malloc(sizeof (pthread_t) * AM_MAX_THREADS_POOL);
    if (worker_pool->threads == NULL) {
        free(worker_pool);
        worker_pool = NULL;
        return;
    }

    worker_pool->num_threads = AM_MAX_THREADS_POOL;
    worker_pool->size = 0;
    worker_pool->head = NULL;
    worker_pool->tail = NULL;
    worker_pool->shutdown = 0;
    worker_pool->dont_accept = 0;

    if (pthread_mutex_init(&worker_pool->lock, NULL)) {
        free(worker_pool->threads);
        free(worker_pool);
        worker_pool = NULL;
        return;
    }
    if (pthread_cond_init(&(worker_pool->empty), NULL)) {
        free(worker_pool->threads);
        free(worker_pool);
        worker_pool = NULL;
        return;
    }
    if (pthread_cond_init(&(worker_pool->not_empty), NULL)) {
        free(worker_pool->threads);
        free(worker_pool);
        worker_pool = NULL;
        return;
    }

    for (i = 0; i < AM_MAX_THREADS_POOL; i++) {
        if (pthread_create(&(worker_pool->threads[i]), NULL, do_work, worker_pool)) {
            free(worker_pool->threads);
            free(worker_pool);
            worker_pool = NULL;
            return;
        }
    }
#endif
}

void am_worker_pool_init() {
#ifdef _WIN32
    InitOnceExecuteOnce(&worker_pool_initialized, create_threadpool, NULL, NULL);
#else
    pthread_once(&worker_pool_initialized, create_threadpool);
#endif
}

int am_worker_dispatch(void (*worker_f)(void *, void *), void *arg) {
#ifdef _WIN32
    BOOL status = TrySubmitThreadpoolCallback(worker_f, arg, &worker_env);
    return status == FALSE ? AM_ENOMEM : AM_SUCCESS;
#else
    struct am_threadpool_work *cur;
    int k;

    if (worker_pool == NULL) return AM_EFAULT;
    k = worker_pool->size;

    cur = (struct am_threadpool_work *) malloc(sizeof (struct am_threadpool_work));
    if (cur == NULL) {
        return AM_ENOMEM;
    }

    cur->routine = worker_f;
    cur->arg = arg;
    cur->next = NULL;

    pthread_mutex_lock(&(worker_pool->lock));

    if (worker_pool->dont_accept) {
        free(cur);
        return AM_EPERM;
    }
    if (worker_pool->size == 0) {
        worker_pool->head = cur;
        worker_pool->tail = cur;
        pthread_cond_broadcast(&(worker_pool->not_empty));
    } else {
        worker_pool->tail->next = cur;
        worker_pool->tail = cur;
    }
    worker_pool->size++;
    pthread_mutex_unlock(&(worker_pool->lock));
    return AM_SUCCESS;
#endif
}

void am_worker_pool_shutdown() {
#ifdef _WIN32
    CloseThreadpoolCleanupGroupMembers(worker_pool_cleanup, TRUE, NULL);
    CloseThreadpoolCleanupGroup(worker_pool_cleanup);
    DestroyThreadpoolEnvironment(&worker_env);
    CloseThreadpool(worker_pool);
#else
    int i;

    if (worker_pool == NULL) return;

    pthread_mutex_lock(&(worker_pool->lock));
    worker_pool->dont_accept = 1;
    while (worker_pool->size != 0) {
        pthread_cond_wait(&(worker_pool->empty), &(worker_pool->lock));
    }
    worker_pool->shutdown = 1;
    pthread_cond_broadcast(&(worker_pool->not_empty));
    pthread_mutex_unlock(&(worker_pool->lock));

    for (i = 0; i < worker_pool->num_threads; i++) {
        pthread_cond_broadcast(&(worker_pool->not_empty));
        pthread_join(worker_pool->threads[i], NULL);
    }

    free(worker_pool->threads);
    pthread_mutex_destroy(&(worker_pool->lock));
    pthread_cond_destroy(&(worker_pool->empty));
    pthread_cond_destroy(&(worker_pool->not_empty));
    free(worker_pool);
#endif
}

am_event_t *create_event() {
    am_event_t *e = malloc(sizeof (am_event_t));
    if (e != NULL) {
#ifdef _WIN32
        e->e = CreateEvent(NULL, FALSE, FALSE, NULL);
#else
        pthread_mutexattr_t a;
        pthread_mutexattr_init(&a);
        pthread_mutexattr_settype(&a, PTHREAD_MUTEX_RECURSIVE);
        pthread_mutex_init(&e->m, &a);
        pthread_cond_init(&e->c, NULL);
        e->e = 0;
        pthread_mutexattr_destroy(&a);
#endif
    }
    return e;
}

am_exit_event_t *create_exit_event() {
    am_exit_event_t *e = malloc(sizeof (am_exit_event_t));
    if (e != NULL) {
#ifdef _WIN32
        e->e = CreateEvent(NULL, FALSE, FALSE, NULL);
#else
        pthread_mutexattr_t a;
        pthread_mutexattr_init(&a);
        pthread_mutexattr_settype(&a, PTHREAD_MUTEX_RECURSIVE);
        pthread_mutex_init(&e->m, &a);
        pthread_mutexattr_destroy(&a);
        pthread_mutex_lock(&e->m);
#endif
    }
    return e;
}

void set_event(am_event_t *e) {
    if (e != NULL) {
#ifdef _WIN32
        SetEvent(e->e);
#else
        pthread_mutex_lock(&e->m);
        e->e = 1;
        pthread_cond_broadcast(&e->c);
        pthread_mutex_unlock(&e->m);
#endif
    }
}

void reset_event(am_event_t *e) {/*optional*/
    if (e != NULL) {
#ifdef _WIN32
        ResetEvent(e->e);
#else
        pthread_mutex_lock(&e->m);
        e->e = 0;
        pthread_cond_broadcast(&e->c);
        pthread_mutex_unlock(&e->m);
#endif
    }
}

void set_exit_event(am_exit_event_t *e) {
    if (e != NULL) {
#ifdef _WIN32
        SetEvent(e->e);
#else
        pthread_mutex_unlock(&e->m);
#endif
    }
}

int wait_for_event(am_event_t *e, int timeout) {
    int r = 0;
    if (e != NULL) {
#ifdef _WIN32
        DWORD rv = WaitForSingleObject(e->e, timeout > 0 ? timeout * 1000 : INFINITE);
        if (rv != WAIT_OBJECT_0) {
            r = AM_ETIMEDOUT;
        }
#else
        pthread_mutex_lock(&e->m);
        while (!e->e) {
            if (timeout <= 0) {
                pthread_cond_wait(&e->c, &e->m);
            } else {
                struct timeval now = {0, 0};
                struct timespec ts = {0, 0};
                gettimeofday(&now, NULL);
                ts.tv_sec = now.tv_sec + timeout;
                ts.tv_nsec = now.tv_usec * 1000;
                if (pthread_cond_timedwait(&e->c, &e->m, &ts) == ETIMEDOUT) {
                    r = AM_ETIMEDOUT;
                    break;
                }
            }
        }
        if (r == 0) e->e = 0; /*resets the event state to nonsignaled after a single waiting thread has been released*/
        pthread_mutex_unlock(&e->m);
#endif
    }
    return r;
}

int wait_for_exit_event(am_exit_event_t *e) {
    if (e == NULL) return 1;
#ifdef _WIN32
    return WaitForSingleObject(e->e, 0) == WAIT_TIMEOUT ? 0 : 1;
#else
    switch (pthread_mutex_trylock(&e->m)) {
        case 0:
            pthread_mutex_unlock(&e->m);
            return 1;
        case EBUSY:
            return 0;
    }
    return 1;
#endif
}

void close_event(am_event_t *e) {
    if (e != NULL) {
        set_event(e);
#ifdef _WIN32
        CloseHandle(e->e);
#else
        pthread_mutex_destroy(&e->m);
        pthread_cond_destroy(&e->c);
#endif
        free(e);
        e = NULL;
    }
}

void close_exit_event(am_exit_event_t *e) {
    if (e != NULL) {
#ifdef _WIN32
        CloseHandle(e->e);
#else
        pthread_mutex_destroy(&e->m);
#endif
        free(e);
        e = NULL;
    }
}
