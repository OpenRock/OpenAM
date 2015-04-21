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

#ifndef NET_CLIENT_H
#define NET_CLIENT_H

#include "http_parser.h"
#include "thread.h"

typedef struct {
#ifdef _WIN32
    SOCKET sock;
    HANDLE pw;
    CRITICAL_SECTION lk;
    HANDLE tm;
    HANDLE tm_tick;
#else
    int sock;
    pthread_t pw; /*event loop*/
    pthread_mutex_t lk;

#ifdef __APPLE__    
    pthread_t tm_th;
    pthread_mutex_t tm_lk;
    pthread_cond_t tm_cv;
#else
    timer_t tm;
#endif

#endif
    unsigned long instance_id;
    int timeout; /*connect timeout*/

    unsigned int retry;
    unsigned int retry_wait; /*in seconds*/

    const char *url;
    struct url uv;

    struct ssl {
        char on;
        void *ssl_handle;
        void *ssl_context;
        void *read_bio;
        void *write_bio;
        int error;
        int sys_error;
        struct am_ssl_options info;
        char *request_data;
        size_t request_data_sz;
    } ssl;

    http_parser_settings *hs;
    http_parser *hp;
    unsigned int http_status;

    struct addrinfo *ra;
    am_event_t *ce; /*connected event*/
    am_exit_event_t *de; /*disconnect event*/

    void *data;
    void (*on_connected)(void *udata, int status);
    void (*on_data)(void *udata, const char *data, size_t data_sz, int status);
    void (*on_complete)(void *udata, int status); /*callback when all data for the current request is read*/
    void (*on_close)(void *udata, int status);
    void (*log)(const char *, ...);
    int error;
} am_net_t;

int am_net_connect(am_net_t *n);
int am_net_write(am_net_t *n, const char *data, size_t data_sz);

void am_net_diconnect(am_net_t *n); /*disconnect socket (client side)*/
int am_net_close(am_net_t *n);
void am_net_set_ssl_options(am_config_t *ac, struct am_ssl_options *info);

void am_net_init();
void am_net_shutdown();

#endif
