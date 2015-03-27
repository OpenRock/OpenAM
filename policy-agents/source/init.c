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
#include "net_client.h"

#ifdef _WIN32

struct am_main_init {
    HANDLE id;
    int error;
};

struct am_main_init init = {
    NULL,
    AM_ERROR
};

static void am_main_create() {
    init.id = CreateMutex(NULL, FALSE, "Global\\am_main_init_lock");
    if (init.id == NULL && GetLastError() == ERROR_ACCESS_DENIED) {
        init.id = OpenMutexA(SYNCHRONIZE, TRUE, "Global\\am_main_init_lock");
    }
}

static void am_main_destroy() {
    CloseHandle(init.id);
    init.error = AM_ERROR;
}

static void am_main_init_timed_lock() {
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
}

static void am_main_init_unlock() {
    ReleaseMutex(init.id);
    init.error = AM_SUCCESS;
}

#endif

int am_init() {
    int rv = AM_SUCCESS;
#ifndef _WIN32
    am_net_init();
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
#ifdef _WIN32
    am_main_destroy();
#else
    am_net_shutdown();
#endif
    return 0;
}

int am_init_worker() {
#ifdef _WIN32
    am_net_init();
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
#ifdef _WIN32
    am_main_init_timed_lock();
    if (init.error == AM_SUCCESS || init.error == AM_EAGAIN) {
        am_log_re_init(AM_RETRY_ERROR);
    }
#endif
    return 0;
}

int am_shutdown_worker() {
#ifdef _WIN32
    am_main_init_unlock();
#endif
    am_worker_pool_shutdown();
#ifdef _WIN32
    am_net_shutdown();
#endif
    return 0;
}
