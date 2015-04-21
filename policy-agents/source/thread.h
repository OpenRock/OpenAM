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

#ifndef THREAD_H
#define THREAD_H

#ifdef _WIN32
typedef CRITICAL_SECTION am_mutex_t;
typedef HANDLE am_thread_t;
#define AM_MUTEX_LOCK           EnterCriticalSection
#define AM_MUTEX_UNLOCK         LeaveCriticalSection
#define AM_MUTEX_DESTROY        DeleteCriticalSection
#define AM_THREAD_CREATE(t,f,a) do { t = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE) f, a, 0, NULL); } while(0)
#define AM_THREAD_JOIN(t)       WaitForSingleObject(t, INFINITE)
#else
typedef pthread_mutex_t am_mutex_t;
typedef pthread_t am_thread_t;
#define AM_MUTEX_LOCK           pthread_mutex_lock
#define AM_MUTEX_UNLOCK         pthread_mutex_unlock
#define AM_MUTEX_DESTROY        pthread_mutex_destroy
#define AM_THREAD_CREATE(t,f,a) pthread_create(&(t), NULL, f, a)
#define AM_THREAD_JOIN(t)       pthread_join(t, NULL)
#endif

typedef struct {
#ifdef _WIN32
    HANDLE e;
#else
    volatile char e;
    pthread_mutex_t m;
    pthread_cond_t c;
#endif
} am_event_t;

typedef struct {
#ifdef _WIN32
    HANDLE e;
#else
    pthread_mutex_t m;
#endif
} am_exit_event_t;

am_event_t *create_event();
am_exit_event_t *create_exit_event();

int wait_for_event(am_event_t *e, int timeout);
int wait_for_exit_event(am_exit_event_t *e);

void set_event(am_event_t *e);
void set_exit_event(am_exit_event_t *e);

void close_event(am_event_t *e);
void close_exit_event(am_exit_event_t *e);

void am_worker_pool_shutdown();
void am_worker_pool_init();

int am_worker_dispatch(void (*worker_f)(void *, void *), void *arg);

void notification_worker(
#ifdef _WIN32
        PTP_CALLBACK_INSTANCE
#else
        void *
#endif
        inst, void *arg);

void session_logout_worker(
#ifdef _WIN32
        PTP_CALLBACK_INSTANCE
#else
        void *
#endif
        inst, void *arg);

#endif
