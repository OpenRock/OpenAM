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
#include "thread.h"

struct am_main_init {
#ifdef _WIN32
    HANDLE id;
#else
    int id;
#endif
    int error;
};

struct am_main_init init = {
#ifdef _WIN32
    NULL,
#else
    -1,
#endif
    AM_ERROR
};

static void am_main_create() {
#ifdef _WIN32
    init.id = CreateMutex(NULL, FALSE, "Global\\am_main_init_lock");
    if (init.id == NULL && GetLastError() == ERROR_ACCESS_DENIED) {
        init.id = OpenMutexA(SYNCHRONIZE, TRUE, "Global\\am_main_init_lock");
    }
#else
    struct sembuf op;
    if ((init.id = semget(IPC_PRIVATE, 1, 0600 | IPC_CREAT)) < 0) {
        init.error = AM_ENOMEM;
        return;
    }
    op.sem_num = 0;
    op.sem_op = +1;
    op.sem_flg = 0;
    if (semop(init.id, &op, 1) < 0) {
        init.error = AM_EINVAL;
    } else {
        init.error = AM_SUCCESS;
    }
#endif
}

static void am_main_destroy() {
#ifdef _WIN32
    CloseHandle(init.id);
    init.error = AM_ERROR;
#else
    if (semctl(init.id, 0, IPC_RMID) < 0) {
        init.error = AM_ERROR;
    } else {
        init.error = AM_SUCCESS;
    }
#endif
}

static void am_main_init_lock() {
#ifdef _WIN32
    DWORD status = WaitForSingleObject(init.id, INFINITE);
    if (status == WAIT_OBJECT_0) {
        init.error = AM_SUCCESS;
    } else if (status == WAIT_ABANDONED) {
        init.error = AM_EAGAIN;
    } else {
        init.error = AM_ERROR;
    }
#else
    struct sembuf op;
    op.sem_num = 0;
    op.sem_op = -1;
    op.sem_flg = 0;
    if (semop(init.id, &op, 1) < 0) {
        init.error = AM_ERROR;
    } else {
        init.error = AM_SUCCESS;
    }
#endif
}

static void am_main_init_timed_lock() {
#ifdef _WIN32
    DWORD status = WaitForSingleObject(init.id, 1000);
    if (status == WAIT_OBJECT_0) {
        init.error = AM_SUCCESS;
    } else if (status == WAIT_ABANDONED) {
        init.error = AM_EAGAIN;
    } else if (status == WAIT_TIMEOUT) {
        init.error = AM_ETIMEDOUT;
    } else {
        init.error = AM_ERROR;
    }
#else
    int status;
    struct sembuf op;
    struct timeval now = {0, 0};
    struct timespec ts = {0, 0};
    gettimeofday(&now, NULL);
    ts.tv_sec = now.tv_sec + 1;
    ts.tv_nsec = now.tv_usec * 1000;
    op.sem_num = 0;
    op.sem_op = -1;
    op.sem_flg = 0;
#ifdef __APPLE__
    status = -1;
#else
    status = semtimedop(init.id, &op, 1, &ts);
#endif
    if (status == -1) {
        init.error = errno == EAGAIN ? AM_ETIMEDOUT : AM_ERROR;
    } else {
        init.error = AM_SUCCESS;
    }
#endif
}

static void am_main_init_unlock() {
#ifdef _WIN32
    ReleaseMutex(init.id);
    init.error = AM_SUCCESS;
#else
    struct sembuf op;
    op.sem_num = 0;
    op.sem_op = +1;
    op.sem_flg = 0;
    if (semop(init.id, &op, 1) < 0) {
        init.error = AM_ERROR;
    } else {
        init.error = AM_SUCCESS;
    }
#endif
}

int am_init() {
    int rv = AM_SUCCESS;
#ifndef _WIN32
    am_main_create();
    am_log_init(AM_SUCCESS);
    am_configuration_init();
    rv = am_cache_init();
#endif
    return rv;
}

int am_shutdown() {
    am_cache_shutdown();
    am_configuration_shutdown();
    am_log_shutdown();
    am_main_destroy();
    return 0;
}

int am_init_worker() {
#ifdef _WIN32
    am_main_create();
    am_main_init_timed_lock();
    am_log_init_worker(init.error);
    am_configuration_init();
    am_cache_init();
#endif
    am_worker_pool_init();
    return 0;
}

int am_re_init_worker() {
    am_main_init_timed_lock();
    if (init.error == AM_SUCCESS || init.error == AM_EAGAIN) {
        am_log_re_init(AM_RETRY_ERROR);
    }
    return 0;
}

int am_shutdown_worker() {
    am_main_init_unlock();
    am_worker_pool_shutdown();
    return 0;
}
