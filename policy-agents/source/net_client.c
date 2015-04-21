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

#ifdef _GNU_SOURCE
#undef _GNU_SOURCE /*prefer strerror_r*/
#endif
#include "platform.h"
#include "am.h"
#include "utility.h"
#include "net_client.h"

#ifndef INVALID_SOCKET
#define INVALID_SOCKET -1
#endif

#ifdef _WIN32
static short connect_ev = POLLWRNORM;
static short connected_ev = POLLWRNORM;
static short read_ev = POLLRDNORM;
static short read_avail_ev = POLLRDNORM | POLLHUP;
#else
static short connect_ev = POLLOUT | POLLNVAL | POLLERR | POLLHUP;
static short connected_ev = POLLOUT;
static short read_ev = POLLIN | POLLNVAL | POLLERR | POLLHUP;
static short read_avail_ev = POLLIN | POLLHUP;
#endif

#ifdef _WIN32
#define net_log_error(i,e) \
do {LPSTR es = NULL; \
if (FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, 0, e, 0, (LPSTR) & es, 0, 0) == 0) { \
        AM_LOG_ERROR(i, "net_error(%s:%d) unknown error code (%X)", __FILE__, __LINE__, e); } else { \
        char *p = strchr(es, '\r'); \
        if (p != NULL) *p = '\0'; \
        AM_LOG_ERROR(i, "net_error(%s:%d): %s (%X)", __FILE__, __LINE__, es, e); \
        LocalFree(es);}} while(0)
#else
#define net_log_error(i,e) \
   do {size_t size = 1024; char *tmp, *es = malloc(size + 1); \
   if (es != NULL) { \
        while (strerror_r(e, es, size) == -1 && errno == ERANGE) {\
        size *= 2; tmp = realloc(es, size + 1); if (tmp == NULL) {am_free(es); break;} }\
        es = tmp;\
        if (es != NULL) {\
        AM_LOG_ERROR(i, "net_error(%s:%d): %s (%d)", __FILE__, __LINE__, es, e);\
        free(es);}}} while(0)
#endif

void net_init_ssl();
void net_shutdown_ssl();
void net_connect_ssl(am_net_t *n);
void net_close_ssl(am_net_t *n);
int net_read_ssl(am_net_t *n, const char *buf, int sz);

void am_net_init() {
#ifdef _WIN32
    WSADATA w;
    WSAStartup(MAKEWORD(2, 2), &w);
#endif
    net_init_ssl();
}

void am_net_shutdown() {
#ifdef _WIN32
    WSACleanup();
#endif
    net_shutdown_ssl();
}

static int net_error() {
    int e = 0;
#ifdef _WIN32
    e = WSAGetLastError();
#else
    e = errno;
#endif
    return e;
}

static int net_in_progress(int e) {
#ifdef _WIN32
    return (e == WSAEWOULDBLOCK || e == WSAEINPROGRESS);
#else
    return (e == EWOULDBLOCK || e == EINPROGRESS || e == EAGAIN);
#endif
}

static int net_close_socket(
#ifdef _WIN32
        SOCKET
#else
        int
#endif
        s) {
    if (s != INVALID_SOCKET)
#ifdef _WIN32
        shutdown(s, SD_BOTH);
    return closesocket(s);
#else
        shutdown(s, SHUT_RDWR);
    return close(s);
#endif
}

static int set_nonblocking(am_net_t *n, int cmd) {
#ifdef _WIN32
    u_long nonblock = cmd == 1 ? 1 : 0;
    if (ioctlsocket(n->sock, FIONBIO, &nonblock) == SOCKET_ERROR) {
        return -1;
    }
#else
    int ofdflags, fdflags;
    if ((fdflags = ofdflags = fcntl(n->sock, F_GETFL, 0)) == -1) {
        return -1;
    }
    if (cmd == 1) {
        fdflags |= O_NONBLOCK;
    } else {
        fdflags &= ~O_NONBLOCK;
    }
    if (fdflags != ofdflags) {
        if (fcntl(n->sock, F_SETFL, fdflags) == -1) {
            return -1;
        }
    }
#endif
    return 0;
}


#ifdef _WIN32

static void CALLBACK net_async_poll_timeout(PVOID arg, BOOLEAN timer_or_wait_fired) {
    am_net_t *n = (am_net_t *) arg;
    static const char *thisfunc = "net_async_poll_timeout()";

#elif defined(__APPLE__)

static void *net_async_poll_timeout(void *arg) {
    am_net_t *n = (am_net_t *) arg;
    static const char *thisfunc = "net_async_poll_timeout()";
    int rv;
    clock_serv_t host_clock;
    struct timespec abstime;
    mach_timespec_t now;

    host_get_clock_service(mach_host_self(), CALENDAR_CLOCK, &host_clock);
    abstime.tv_sec = AM_NET_POOL_TIMEOUT;
    abstime.tv_nsec = 0;

    pthread_mutex_lock(&n->tm_lk);
    clock_get_time(host_clock, &now);
    ADD_MACH_TIMESPEC(&abstime, &now);

    rv = pthread_cond_timedwait(&n->tm_cv, &n->tm_lk, &abstime);
    if (rv != ETIMEDOUT) {
        /*cv was signaled prior timeout*/
        pthread_mutex_unlock(&n->tm_lk);
        return NULL;
    }
#else

static void net_async_poll_timeout(union sigval si) {
    am_net_t *n = (am_net_t *) si.sival_ptr;
    static const char *thisfunc = "net_async_poll_timeout()";
#endif

    AM_LOG_WARNING(n->instance_id, "%s timeout waiting for a response from a server", thisfunc);
    n->error = AM_ETIMEDOUT;
    am_net_diconnect(n);

#ifdef __APPLE__
    pthread_mutex_unlock(&n->tm_lk);
    return NULL;
#endif
}

static void net_async_poll(am_net_t *n) {
    int ev = 0;
    char first_run = 1;
#ifdef _WIN32
    WSAPOLLFD fds[1];
#else
    struct pollfd fds[1];
#ifndef __APPLE__
    struct itimerspec ts;
    struct sigevent se;
#endif
#endif

#ifdef _WIN32
    n->tm = CreateTimerQueue();
    if (n->tm == NULL) return;
    if (CreateTimerQueueTimer(&n->tm_tick, n->tm,
            (WAITORTIMERCALLBACK) net_async_poll_timeout, n,
            AM_NET_POOL_TIMEOUT * 1000, 0, WT_EXECUTELONGFUNCTION) == 0) {
        n->tm_tick = NULL;
        return;
    }
#else

#ifdef __APPLE__
    pthread_attr_t tm_ta;
    pthread_mutex_init(&n->tm_lk, NULL);
    pthread_cond_init(&n->tm_cv, NULL);
    pthread_attr_init(&tm_ta);
    pthread_attr_setdetachstate(&tm_ta, PTHREAD_CREATE_DETACHED);
    pthread_create(&n->tm_th, &tm_ta, net_async_poll_timeout, n);
    pthread_attr_destroy(&tm_ta);
#else
    se.sigev_notify = SIGEV_THREAD; //TODO: sol10 ?
    se.sigev_value.sival_ptr = n;
    se.sigev_notify_function = net_async_poll_timeout;
    se.sigev_notify_attributes = NULL;
    if (timer_create(CLOCK_REALTIME, &se, &n->tm) == -1) {
        return;
    }
    ts.it_value.tv_sec = AM_NET_POOL_TIMEOUT;
    ts.it_value.tv_nsec = 0;
    ts.it_interval.tv_sec = 0;
    ts.it_interval.tv_nsec = 0;
    if (timer_settime(n->tm, 0, &ts, 0) == -1) {
        return;
    }
#endif

#endif

    memset(fds, 0, sizeof (fds));
    while (ev != -1) {

        fds[0].fd = n->sock;
        fds[0].events = read_ev;
        fds[0].revents = 0;

        if (first_run) {
            set_event(n->ce);
            if (n->on_connected) n->on_connected(n->data, 0);
            first_run = 0;
        }

        if (wait_for_exit_event(n->de) != 0) {
            break;
        }

        ev = sockpoll(fds, 1, 100);
        if (ev < 0) {
            net_log_error(n->instance_id, net_error());
            break;
        }
        if (ev == 1 && fds[0].revents & (POLLNVAL | POLLERR)) {
            if (n->on_close) n->on_close(n->data, 0);
            break;
        }
        if (ev == 1 && fds[0].revents & read_avail_ev) {
            /* read an output from a remote side */
            int er = 0, error = 0;
            char tmp[1024];
            int got = 0;
            SOCKLEN_T errlen = sizeof (error);
            er = getsockopt(n->sock, SOL_SOCKET, SO_ERROR, (void *) &error, &errlen);
            memset(&tmp[0], 0, sizeof (tmp));
            if (error != 0) break;

            got = recv(n->sock, tmp, sizeof (tmp), 0);
            if (n->ssl.on) {
                error = net_read_ssl(n, tmp, got);
                if (error != AM_SUCCESS) {
                    if (n->on_close) n->on_close(n->data, 0);
                    break;
                }
            } else {
                if (got < 0) {
                    if (!net_in_progress(errno)) {
                        if (n->on_close) n->on_close(n->data, 0);
                        break;
                    }
                } else if (got == 0) {
                    if (n->on_close) n->on_close(n->data, 0);
                    break;
                } else {
                    http_parser_execute(n->hp, n->hs, tmp, got);
                }
            }
        }
    }
}

int am_net_write(am_net_t *n, const char *data, size_t data_sz) {
    int status = 0, sent = 0, flags = 0;
    int er = 0, error = 0;
    SOCKLEN_T errlen = sizeof (error);
    if (n != NULL && data != NULL && data_sz > 0) {

        if (wait_for_event(n->ce, 0) == 0) {
            size_t len = data_sz;
            const char *buf = data;
            if (n->error != 0) {
                set_event(n->ce);
                return n->error;
            }
            if (n->ssl.on) {
                n->ssl.request_data_sz = 0;
                am_free(n->ssl.request_data);
                n->ssl.request_data = malloc(data_sz);
                if (n->ssl.request_data != NULL) {
                    memcpy(n->ssl.request_data, data, data_sz);
                    n->ssl.request_data_sz = data_sz;
                } else {
                    set_event(n->ce);
                    return AM_ENOMEM;
                }
            } else {
#ifdef MSG_NOSIGNAL
                flags |= MSG_NOSIGNAL;
#endif 
                er = getsockopt(n->sock, SOL_SOCKET, SO_ERROR, (void *) &error, &errlen);
                while (sent < len) {
                    int rv = send(n->sock, buf + sent, (int) len - sent, flags);
                    if (rv < 0) {
                        if (net_in_progress(
#ifdef _WIN32                 
                                WSAGetLastError()
#else
                                errno
#endif
                                )) {
#ifdef _WIN32
                            WSAPOLLFD fds[1];
#else
                            struct pollfd fds[1];
#endif
                            memset(fds, 0, sizeof (fds));
                            fds[0].fd = n->sock;
                            fds[0].events = connect_ev;
                            fds[0].revents = 0;
                            if (sockpoll(fds, 1, -1) == -1) {
                                break;
                            }
                            continue;
                        }
                        break;
                    }
                    if (rv == 0) {
                        break;
                    }
                    sent += rv;
                }
            }
            set_event(n->ce);
        }
    }
    return status;
}

static void *net_async_connect(void *arg) {
    am_net_t *n = (am_net_t *) arg;
    static const char *thisfunc = "net_async_connect()";
    struct in6_addr serveraddr;
    struct addrinfo *rp, hints;
    int err = 0, on = 1;
    char port[7];
    am_timer_t tmr;

    memset(&hints, 0, sizeof (struct addrinfo));
    hints.ai_flags = AI_NUMERICSERV;
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    err = INETPTON(AF_INET, n->uv.host, &serveraddr);
    if (err == 1) {
        hints.ai_family = AF_INET;
        hints.ai_flags |= AI_NUMERICHOST;
    } else {
        err = INETPTON(AF_INET6, n->uv.host, &serveraddr);
        if (err == 1) {
            hints.ai_family = AF_INET6;
            hints.ai_flags |= AI_NUMERICHOST;
        }
    }

    snprintf(port, sizeof (port), "%d", n->uv.port);

    am_timer_start(&tmr);
    if ((err = getaddrinfo(n->uv.host, port, &hints, &n->ra)) != 0) {
        n->error = AM_EHOSTUNREACH;
        am_timer_stop(&tmr);
        am_timer_report(n->instance_id, &tmr, "getaddrinfo");
        set_event(n->ce);
        return NULL;
    }

    am_timer_stop(&tmr);
    am_timer_report(n->instance_id, &tmr, "getaddrinfo");

    n->error = 0;

    for (rp = n->ra; rp != NULL; rp = rp->ai_next) {

        if (rp->ai_family != AF_INET && rp->ai_family != AF_INET6 &&
                rp->ai_socktype != SOCK_STREAM && rp->ai_protocol != IPPROTO_TCP) continue;

        if ((n->sock = socket(rp->ai_family, rp->ai_socktype, rp->ai_protocol)) == INVALID_SOCKET) {
            AM_LOG_ERROR(n->instance_id,
                    "%s: cannot create socket while connecting to %s:%d",
                    thisfunc, n->uv.host, n->uv.port);
            net_log_error(n->instance_id, net_error());
            continue;
        }

        if (setsockopt(n->sock, IPPROTO_TCP, TCP_NODELAY, (void *) &on, sizeof (on)) < 0) {
            net_log_error(n->instance_id, net_error());
        }
        if (setsockopt(n->sock, SOL_SOCKET, SO_REUSEADDR, (void *) &on, sizeof (on)) < 0) {
            net_log_error(n->instance_id, net_error());
        }
#ifdef SO_NOSIGPIPE
        if (setsockopt(n->sock, SOL_SOCKET, SO_NOSIGPIPE, (void *) &on, sizeof (on)) < 0) {
            net_log_error(n->instance_id, net_error());
        }
#endif
        if (set_nonblocking(n, 1) != 0) {
            n->error = AM_EPERM;
            continue;
        }

        err = connect(n->sock, rp->ai_addr, (SOCKLEN_T) rp->ai_addrlen);
        if (err == 0) {
            AM_LOG_DEBUG(n->instance_id, "%s: connected to %s:%d (%s)",
                    thisfunc, n->uv.host, n->uv.port,
                    rp->ai_family == AF_INET ? "IPv4" : "IPv6");
            n->error = 0;
            if (n->uv.ssl) {
                net_connect_ssl(n);
            }
            net_async_poll(n);
            break;
        }

        if (err == INVALID_SOCKET && net_in_progress(net_error())) {
#ifdef _WIN32
            WSAPOLLFD fds[1];
#else
            struct pollfd fds[1];
#endif
            memset(fds, 0, sizeof (fds));
            fds[0].fd = n->sock;
            fds[0].events = connect_ev;
            fds[0].revents = 0;

            err = sockpoll(fds, 1, n->timeout > 0 ? n->timeout * 1000 : -1);
            if (err > 0 && fds[0].revents & connected_ev) {
                int pe = 0;
                SOCKLEN_T pe_sz = sizeof (pe);
                err = getsockopt(n->sock, SOL_SOCKET, SO_ERROR, (char *) &pe, &pe_sz);
                if (err == 0 && pe == 0) {
                    AM_LOG_DEBUG(n->instance_id, "%s: connected to %s:%d (%s)",
                            thisfunc, n->uv.host, n->uv.port,
                            rp->ai_family == AF_INET ? "IPv4" : "IPv6");

                    n->error = 0;
                    if (n->uv.ssl) {
                        net_connect_ssl(n);
                    }
                    net_async_poll(n);
                    break;
                }
                net_log_error(n->instance_id, pe);
                n->error = AM_ECONNREFUSED;
            } else if (err == 0) {
                AM_LOG_WARNING(n->instance_id,
                        "%s: timeout connecting to to %s:%d (%s)",
                        thisfunc, n->uv.host, n->uv.port,
                        rp->ai_family == AF_INET ? "IPv4" : "IPv6");
                n->error = AM_ETIMEDOUT;
            } else {
                int pe = 0;
                SOCKLEN_T pe_sz = sizeof (pe);
                err = getsockopt(n->sock, SOL_SOCKET, SO_ERROR, (char *) &pe, &pe_sz);
                n->error = AM_ETIMEDOUT;
                break;
            }
        }

        net_close_socket(n->sock);
        n->sock = INVALID_SOCKET;
    }

    if (n->error != 0) {
        set_event(n->ce);
    }
    return NULL;
}

static int on_body_cb(http_parser *parser, const char *at, size_t length) {
    am_net_t *n = (am_net_t *) parser->data;
    if (n->on_data) n->on_data(n->data, at, length, 0);
    return 0;
}

static int on_headers_complete_cb(http_parser *parser) {
    am_net_t *n = (am_net_t *) parser->data;
    n->http_status = parser->status_code;
    return 0;
}

static int on_message_complete_cb(http_parser *parser) {
    am_net_t *n = (am_net_t *) parser->data;
    if (n->on_complete) n->on_complete(n->data, 0);
    return 0;
}

int am_net_connect(am_net_t *n) {
    int status = 0;
    if (n == NULL) return AM_EINVAL;
    n->sock = INVALID_SOCKET;

    if (n->url == NULL) return AM_EINVAL;

    n->ce = create_event();
    if (n->ce == NULL) return AM_ENOMEM;
    n->de = create_exit_event();
    if (n->de == NULL) return AM_ENOMEM;

    if (parse_url(n->url, &n->uv) != 0) {
        return n->uv.error;
    }

    n->hs = calloc(1, sizeof (http_parser_settings));
    if (n->hs == NULL) return AM_ENOMEM;
    n->hp = calloc(1, sizeof (http_parser));
    if (n->hp == NULL) return AM_ENOMEM;

    n->hs->on_headers_complete = on_headers_complete_cb;
    n->hs->on_body = on_body_cb;
    n->hs->on_message_complete = on_message_complete_cb;
    http_parser_init(n->hp, HTTP_RESPONSE);

#ifdef _WIN32
    InitializeCriticalSection(&n->lk);
    n->pw = CreateThread(NULL, 0,
            (LPTHREAD_START_ROUTINE) net_async_connect, n, 0, NULL);
    if (n->pw == NULL) {
        status = AM_EAGAIN;
    }
#else
    pthread_mutex_init(&n->lk, NULL);
    if (pthread_create(&n->pw, NULL, net_async_connect, n) != 0) {
        status = AM_EAGAIN;
    }
#endif
    n->hp->data = n;
    return status;
}

void am_net_diconnect(am_net_t *n) {
    if (n != NULL) {
        set_exit_event(n->de);
        net_close_ssl(n);
        net_close_socket(n->sock);
        n->sock = INVALID_SOCKET;
    }
}

int am_net_close(am_net_t *n) {
    if (n == NULL) return AM_EINVAL;
#ifdef _WIN32   
    WaitForSingleObject(n->pw, INFINITE);
    CloseHandle(n->pw);
    DeleteCriticalSection(&n->lk);
#else
    pthread_join(n->pw, NULL);
    pthread_mutex_destroy(&n->lk);
#endif
    am_net_diconnect(n);
    close_event(n->ce);
    close_exit_event(n->de);

#ifdef __APPLE__        
    pthread_cond_broadcast(&n->tm_cv);
    pthread_cond_destroy(&n->tm_cv);
    pthread_mutex_destroy(&n->tm_lk);
#endif

#ifdef _WIN32 
    if (n->tm != NULL && n->tm_tick != NULL)
        DeleteTimerQueueTimer(n->tm, n->tm_tick, NULL);
    n->tm_tick = NULL;
    if (n->tm != NULL) DeleteTimerQueue(n->tm);
    n->tm = NULL;
#else
#if !defined(__APPLE__)
    if (n->tm != NULL) timer_delete(n->tm);
    n->tm = NULL;
#endif
#endif
    if (n->ra != NULL) freeaddrinfo(n->ra);
    n->ra = NULL;
    AM_FREE(n->hs, n->hp);
    n->hs = NULL;
    n->hp = NULL;
    return AM_SUCCESS;
}
