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
 * Copyright 2015 ForgeRock AS.
 */

#include "platform.h"
#include "am.h"
#include "utility.h"
#include "net_client.h"

#ifdef _WIN32
#define AM_SSL_LIB "ssleay32"
#define AM_CRYPTO_LIB "libeay32"
#else
#define AM_SSL_LIB "libssl"
#define AM_CRYPTO_LIB "libcrypto"
#endif

#ifdef _WIN32
static INIT_ONCE ssl_lib_initialized = INIT_ONCE_STATIC_INIT;
static CRITICAL_SECTION *ssl_mutexes = NULL;
#else
static pthread_once_t ssl_lib_initialized = PTHREAD_ONCE_INIT;
static pthread_mutex_t *ssl_mutexes = NULL;
#endif
static void *crypto_lib = NULL;
static void *ssl_lib = NULL;

struct ssl_func {
    const char *name;
    void (*ptr)(void);
};

static struct ssl_func ssl_sw[] = {
    {"SSL_library_init", NULL},
    {"SSL_CTX_new", NULL},
    {"SSLv23_client_method", NULL},
    {"SSL_CTX_ctrl", NULL},
    {"SSL_CTX_set_cipher_list", NULL},
    {"SSL_CTX_load_verify_locations", NULL},
    {"SSL_CTX_use_certificate_file", NULL},
    {"SSL_CTX_set_default_passwd_cb_userdata", NULL},
    {"SSL_CTX_set_default_passwd_cb", NULL},
    {"SSL_CTX_use_PrivateKey_file", NULL},
    {"SSL_CTX_check_private_key", NULL},
    {"SSL_CTX_set_verify", NULL},
    {"SSL_new", NULL},
    {"SSL_CTX_set_info_callback", NULL},
    {"SSL_CTX_set_msg_callback", NULL},
    {"SSL_set_connect_state", NULL},
    {"SSL_do_handshake", NULL},
    {"SSL_get_error", NULL},
    {"SSL_read", NULL},
    {"SSL_write", NULL},
    {"SSL_connect", NULL},
    {"SSL_shutdown", NULL},
    {"SSL_CTX_free", NULL},
    {"SSL_free", NULL},
    {"SSL_get_peer_certificate", NULL},
    {"SSL_alert_type_string", NULL},
    {"SSL_alert_desc_string", NULL},
    {"SSL_alert_desc_string_long", NULL},
    {"SSL_set_bio", NULL},
    {"SSL_get_verify_result", NULL},
    {"SSL_state_string", NULL},
    {"SSL_state_string_long", NULL},
    {"SSL_state", NULL},
#ifndef _WIN32
    {"BIO_s_mem", NULL},
    {"BIO_new", NULL},
    {"BIO_write", NULL},
    {"BIO_read", NULL},
#endif
    {NULL, NULL}
};

static struct ssl_func crypto_sw[] = {
    {"CRYPTO_num_locks", NULL},
    {"CRYPTO_set_locking_callback", NULL},
    {"CRYPTO_set_id_callback", NULL},
    {"CRYPTO_set_mem_functions", NULL},
    {"OPENSSL_add_all_algorithms_noconf", NULL},
    {"X509_get_subject_name", NULL},
    {"X509_get_issuer_name", NULL},
    {"X509_NAME_oneline", NULL},
    {"X509_free", NULL},
    {"ERR_get_error", NULL},
    {"ERR_error_string", NULL},
#ifdef _WIN32
    {"BIO_s_mem", NULL},
    {"BIO_new", NULL},
    {"BIO_write", NULL},
    {"BIO_read", NULL},
#endif
    {NULL, NULL}
};

#define SSL_ERROR_NONE 0
#define SSL_ERROR_WANT_READ 2
#define SSL_ERROR_WANT_WRITE 3
#define SSL_OP_NO_SSLv2 0x01000000L
#define SSL_OP_NO_SSLv3 0x02000000L
#define SSL_FILETYPE_PEM 1
#define SSL_VERIFY_NONE 0
#define SSL_VERIFY_PEER 0x01
#define SSL_CB_LOOP 0x01
#define SSL_CB_EXIT 0x02
#define SSL_CB_READ 0x04
#define SSL_CB_WRITE 0x08
#define SSL_CB_ALERT 0x4000
#define SSL_CB_HANDSHAKE_DONE 0x20
#define SSL_CTRL_OPTIONS 32
#define SSL_CTRL_SET_MSG_CALLBACK_ARG 16
#define SSL_ST_OK 0x03

typedef struct ssl_st SSL;
typedef struct ssl_ctx_st SSL_CTX;
typedef struct ssl_method_st SSL_METHOD;
typedef struct x509_store_ctx_st X509_STORE_CTX;
typedef struct x509_st X509;
#ifdef _WIN32
#undef X509_NAME
#endif
typedef struct X509_name_st X509_NAME;
typedef struct bio_st BIO;
typedef struct bio_method_st BIO_METHOD;

#define SSL_library_init (* (int (*)(void)) ssl_sw[0].ptr)
#define SSL_CTX_new (* (SSL_CTX * (*)(SSL_METHOD *)) ssl_sw[1].ptr)
#define SSLv23_client_method (* (SSL_METHOD * (*)(void)) ssl_sw[2].ptr)
#define SSL_CTX_ctrl (* (long (*)(SSL_CTX *, int, long, void *)) ssl_sw[3].ptr)
#define SSL_CTX_set_cipher_list (* (int (*)(SSL_CTX *,const char *)) ssl_sw[4].ptr)
#define SSL_CTX_load_verify_locations (* (int (*)(SSL_CTX *, const char *, const char *)) ssl_sw[5].ptr)
#define SSL_CTX_use_certificate_file (* (int (*)(SSL_CTX *, const char *, int)) ssl_sw[6].ptr)
#define SSL_CTX_set_default_passwd_cb_userdata (* (void (*)(SSL_CTX *, void *)) ssl_sw[7].ptr)
#define SSL_CTX_set_default_passwd_cb (* (void (*)(SSL_CTX *, int (*callback)(char *, int, int, void *))) ssl_sw[8].ptr)
#define SSL_CTX_use_PrivateKey_file (* (int (*)(SSL_CTX *, const char *, int)) ssl_sw[9].ptr)
#define SSL_CTX_check_private_key (* (int (*)(const SSL_CTX *)) ssl_sw[10].ptr)
#define SSL_CTX_set_verify (* (void (*)(SSL_CTX *, int, int (*verify_callback)(int, X509_STORE_CTX *))) ssl_sw[11].ptr)
#define SSL_new (* (SSL * (*)(SSL_CTX *)) ssl_sw[12].ptr)
#define SSL_CTX_set_info_callback (* (void (*)(SSL_CTX *, void (*callback)())) ssl_sw[13].ptr)
#define SSL_CTX_set_msg_callback (* (void (*)(SSL_CTX *, void (*callback)(int, int, int, const void *, size_t, SSL *, void *))) ssl_sw[14].ptr)
#define SSL_set_connect_state (* (void (*)(SSL *)) ssl_sw[15].ptr)
#define SSL_do_handshake (* (int (*)(SSL *)) ssl_sw[16].ptr)
#define SSL_get_error (* (int (*)(SSL *, int)) ssl_sw[17].ptr)
#define SSL_read (* (int (*)(SSL *, void *, int)) ssl_sw[18].ptr)
#define SSL_write (* (int (*)(SSL *, const void *,int)) ssl_sw[19].ptr)
#define SSL_connect (* (int (*)(SSL *)) ssl_sw[20].ptr)
#define SSL_shutdown (* (void (*)(SSL *)) ssl_sw[21].ptr)
#define SSL_CTX_free (* (void (*)(SSL_CTX *)) ssl_sw[22].ptr)
#define SSL_free (* (void (*)(SSL *)) ssl_sw[23].ptr)
#define SSL_get_peer_certificate (* (X509 * (*)(const SSL *)) ssl_sw[24].ptr)
#define SSL_alert_type_string (* (const char * (*)(int)) ssl_sw[25].ptr)
#define SSL_alert_desc_string (* (const char * (*)(int)) ssl_sw[26].ptr)
#define SSL_alert_desc_string_long (* (const char * (*)(int)) ssl_sw[27].ptr)
#define SSL_set_bio (* (void (*)(SSL *, BIO *, BIO *)) ssl_sw[28].ptr)
#define SSL_get_verify_result (* (int (*)(const SSL *)) ssl_sw[29].ptr)
#define SSL_state_string (* (const char * (*)(const SSL *)) ssl_sw[30].ptr)
#define SSL_state_string_long (* (const char * (*)(const SSL *)) ssl_sw[31].ptr)
#define SSL_state (* (int (*)(const SSL *)) ssl_sw[32].ptr)
#ifndef _WIN32
#define BIO_s_mem (* (BIO_METHOD * (*)(void)) ssl_sw[33].ptr)
#define BIO_new (* (BIO * (*)(BIO_METHOD *)) ssl_sw[34].ptr)
#define BIO_write (* (int (*)(BIO *, const void *, int)) ssl_sw[35].ptr)
#define BIO_read (* (int (*)(BIO *, void *, int)) ssl_sw[36].ptr)
#endif

#define CRYPTO_num_locks (* (int (*)(void)) crypto_sw[0].ptr)
#define CRYPTO_set_locking_callback (* (void (*)(void (*)(int, int, const char *, int))) crypto_sw[1].ptr)
#define CRYPTO_set_id_callback (* (void (*)(unsigned long (*)(void))) crypto_sw[2].ptr)
#define CRYPTO_set_mem_functions (* (int (*)(void *(*m)(size_t),void *(*r)(void *,size_t), void (*f)(void *))) crypto_sw[3].ptr)
#define OPENSSL_add_all_algorithms_noconf (* (void (*)(void)) crypto_sw[4].ptr)
#define X509_get_subject_name (* (X509_NAME * (*)(X509 *)) crypto_sw[5].ptr)
#define X509_get_issuer_name (* (X509_NAME * (*)(X509 *)) crypto_sw[6].ptr)
#define X509_NAME_oneline (* (char * (*)(X509_NAME *, char *, int)) crypto_sw[7].ptr)
#define X509_free (* (void (*)(X509 *)) crypto_sw[8].ptr)
#define ERR_get_error (* (unsigned long (*)(void)) crypto_sw[9].ptr)
#define ERR_error_string (* (char * (*)(unsigned long, char *)) crypto_sw[10].ptr)
#ifdef _WIN32
#define BIO_s_mem (* (BIO_METHOD * (*)(void)) crypto_sw[11].ptr)
#define BIO_new (* (BIO * (*)(BIO_METHOD *)) crypto_sw[12].ptr)
#define BIO_write (* (int (*)(BIO *, const void *, int)) crypto_sw[13].ptr)
#define BIO_read (* (int (*)(BIO *, void *, int)) crypto_sw[14].ptr)
#endif

static void *get_function(void *lib, const char *name) {
#ifdef _WIN32
    return (void *) GetProcAddress((HINSTANCE) lib, name);
#else
    return dlsym(lib, name);
#endif
}

static void close_library(void *lib) {
#ifdef _WIN32
    FreeLibrary((HINSTANCE) lib);
#else
    dlclose(lib);
#endif
}

static void *load_library(const char *lib, struct ssl_func *sw) {
    char name[AM_PATH_SIZE];
#if !defined(_WIN32) && !defined(__APPLE__)
    char name1[AM_PATH_SIZE], name2[AM_PATH_SIZE];
#endif
    void *lib_handle = NULL;
    struct ssl_func *fp, *fpd;

    union {
        void *p;
        void (*fp)(void);
    } u;

    snprintf(name, sizeof (name),
#if defined(_WIN32)
            "%s.dll"
#elif defined(__APPLE__)
            "%s.dylib"
#else
            "%s.so"
#endif   
            , NOTNULL(lib));

#ifdef _WIN32
    if ((lib_handle = (void *) LoadLibrary(name)) == NULL) {
        return NULL;
    }
#else
#ifdef __APPLE__
    lib_handle = dlopen(name, RTLD_LAZY | RTLD_GLOBAL);
#else
    snprintf(name1, sizeof (name1), "%s.so.1.0.0", NOTNULL(lib));
    snprintf(name2, sizeof (name2), "%s.so.0.9.8", NOTNULL(lib));
    lib_handle = dlopen(name1, RTLD_LAZY | RTLD_GLOBAL);
    if (lib_handle == NULL) {
        lib_handle = dlopen(name2, RTLD_LAZY | RTLD_GLOBAL);
        if (lib_handle == NULL) {
            lib_handle = dlopen(name, RTLD_LAZY | RTLD_GLOBAL);
        }
    }
#endif
    if (lib_handle == NULL) {
        return NULL;
    }
#endif

    fpd = sw;
    for (fp = sw; fp->name != NULL; fp++) {
        u.p = get_function(lib_handle, fp->name);
        if (u.fp == NULL) {
            for (; fpd->name != NULL; fpd++) {
                fpd->ptr = NULL;
            }
            close_library(lib_handle);
            return NULL;
        } else {
            fp->ptr = u.fp;
        }
    }
    return lib_handle;
}

static void show_server_cert(am_net_t *n) {
    X509 *cert;
    char *line;
    cert = SSL_get_peer_certificate(n->ssl.ssl_handle);
    if (cert != NULL) {
        line = X509_NAME_oneline(X509_get_subject_name(cert), 0, 0);
        am_log_debug(n->instance_id,
                "show_server_cert(): server certificate subject: %s", line);
        free(line);
        line = X509_NAME_oneline(X509_get_issuer_name(cert), 0, 0);
        am_log_debug(n->instance_id,
                "show_server_cert(): server certificate issuer: %s", line);
        free(line);
        X509_free(cert);
    }
}

static int password_callback(char *buf, int size, int rwflag, void *passwd) {
    strncpy(buf, (char *) passwd, size);
    buf[size - 1] = '\0';
    return (int) (strlen(buf));
}

static const char *read_ssl_error() {
    unsigned long err = ERR_get_error();
    return err == 0 ? "" : LOGEMPTY(ERR_error_string(err, NULL));
}

static char ssl_is_fatal_error(int ssl_error) {
    switch (ssl_error) {
        case SSL_ERROR_NONE:
        case SSL_ERROR_WANT_READ:
        case SSL_ERROR_WANT_WRITE:
            return 0;
    }
    return 1;
}

static void ssl_locking_callback(int mode, int mutex_num, const char *file, int line) {
    if (mode & 1) {
#ifdef _WIN32
        EnterCriticalSection(&ssl_mutexes[mutex_num]);
#else
        pthread_mutex_lock(&ssl_mutexes[mutex_num]);
#endif
    } else {
#ifdef _WIN32
        LeaveCriticalSection(&ssl_mutexes[mutex_num]);
#else
        pthread_mutex_unlock(&ssl_mutexes[mutex_num]);
#endif
    }
}

static unsigned long ssl_id_callback(void) {
#ifdef _WIN32
    return (unsigned long) GetCurrentThreadId();
#else
    return (unsigned long) pthread_self();
#endif
}

static
#ifdef _WIN32
BOOL CALLBACK
#else
void
#endif
init_ssl(
#ifdef _WIN32
        PINIT_ONCE io, PVOID p, PVOID *c
#endif
        ) {
    int i, size;
    ssl_lib = load_library(AM_SSL_LIB, ssl_sw);
    crypto_lib = load_library(AM_CRYPTO_LIB, crypto_sw);
    if (ssl_lib != NULL && crypto_lib != NULL &&
            CRYPTO_set_mem_functions && SSL_library_init && CRYPTO_num_locks &&
            CRYPTO_set_id_callback && CRYPTO_set_locking_callback && OPENSSL_add_all_algorithms_noconf) {
        CRYPTO_set_mem_functions(malloc, realloc, free);
        SSL_library_init();
#ifdef _WIN32
        size = sizeof (CRITICAL_SECTION) * CRYPTO_num_locks();
        ssl_mutexes = (CRITICAL_SECTION *) malloc(size);
        for (i = 0; i < CRYPTO_num_locks(); i++) {
            InitializeCriticalSection(&ssl_mutexes[i]);
        }
#else
        size = sizeof (pthread_mutex_t) * CRYPTO_num_locks();
        ssl_mutexes = (pthread_mutex_t *) malloc(size);
        for (i = 0; i < CRYPTO_num_locks(); i++) {
            pthread_mutex_init(&ssl_mutexes[i], NULL);
        }
#endif
        CRYPTO_set_id_callback(ssl_id_callback);
        CRYPTO_set_locking_callback(ssl_locking_callback);
        OPENSSL_add_all_algorithms_noconf();
    } else {
        if (ssl_lib != NULL) close_library(ssl_lib);
        if (crypto_lib != NULL) close_library(crypto_lib);
        ssl_lib = NULL;
        crypto_lib = NULL;
    }
#ifdef _WIN32
    return TRUE;
#endif
}

void net_init_ssl() {
#ifdef _WIN32
    InitOnceExecuteOnce(&ssl_lib_initialized, init_ssl, NULL, NULL);
#else
    pthread_once(&ssl_lib_initialized, init_ssl);
#endif
}

void net_shutdown_ssl() {
    int i;
    if (CRYPTO_set_locking_callback && CRYPTO_set_id_callback && CRYPTO_num_locks) {
        CRYPTO_set_locking_callback(NULL);
        CRYPTO_set_id_callback(NULL);
        if (ssl_mutexes != NULL) {
            for (i = 0; i < CRYPTO_num_locks(); i++) {
#ifdef _WIN32
                DeleteCriticalSection(&ssl_mutexes[i]);
#else
                pthread_mutex_destroy(&ssl_mutexes[i]);
#endif
            }
            free(ssl_mutexes);
        }
    }
    ssl_mutexes = NULL;
    if (ssl_lib != NULL) close_library(ssl_lib);
    if (crypto_lib != NULL) close_library(crypto_lib);
    ssl_lib = NULL;
    crypto_lib = NULL;
}

static void write_bio_to_socket(am_net_t *n) {
    char buf[1024], *p;
    int len, remaining,
            hasread = BIO_read ? BIO_read(n->ssl.write_bio, buf, sizeof (buf)) : -1;
    if (hasread > 0) {
        p = buf;
        remaining = hasread;
        while (remaining) {
            len = send(n->sock, p, remaining, 0);
            if (len <= 0) {
#ifdef _WIN32
                n->ssl.sys_error = WSAGetLastError();
#else
                n->ssl.sys_error = errno;
#endif
                return;
            }
            remaining -= len;
            p += len;
        }
    }
}

void net_close_ssl(am_net_t *n) {
    if (n->ssl.ssl_handle != NULL) {
        SSL_shutdown(n->ssl.ssl_handle);
        SSL_free(n->ssl.ssl_handle);
    }
    if (n->ssl.ssl_context != NULL) {
        SSL_CTX_free(n->ssl.ssl_context);
    }
    if (n->ssl.request_data != NULL) {
        free(n->ssl.request_data);
    }
    n->ssl.request_data = NULL;
    n->ssl.ssl_handle = NULL;
    n->ssl.ssl_context = NULL;
    n->ssl.on = AM_FALSE;
}

void net_connect_ssl(am_net_t *n) {
    const char *thisfunc = "net_connect_ssl():";
    int status = -1, err = 0;
    if (n != NULL) {
        n->ssl.on = AM_FALSE;

        /*check whether we have ssl library loaded and symbols are available*/
        if (SSL_CTX_new == NULL || SSLv23_client_method == NULL ||
                SSL_CTX_ctrl == NULL || BIO_new == NULL || BIO_s_mem == NULL ||
                SSL_set_bio == NULL || SSL_set_connect_state == NULL ||
                SSL_do_handshake == NULL || SSL_new == NULL || SSL_get_error == NULL) {
            n->ssl.error = AM_ENOSSL;
            return;
        }

        n->ssl.ssl_context = SSL_CTX_new(SSLv23_client_method());

        SSL_CTX_ctrl(n->ssl.ssl_context, SSL_CTRL_OPTIONS, SSL_OP_NO_SSLv2, NULL);
        SSL_CTX_ctrl(n->ssl.ssl_context, SSL_CTRL_OPTIONS, SSL_OP_NO_SSLv3, NULL);
        if (ISVALID(n->ssl.info.name[5])) {
            //TODO: parse tls options
        }

        if (ISVALID(n->ssl.info.name[0])) {
            if (!SSL_CTX_set_cipher_list(n->ssl.ssl_context, n->ssl.info.name[0])) {
                am_log_warning(n->instance_id,
                        "%s failed to set cipher list \"%s\"",
                        thisfunc, n->ssl.info.name[0]);
            }
        }
        if (ISVALID(n->ssl.info.name[1])) {
            if (!SSL_CTX_load_verify_locations(n->ssl.ssl_context, n->ssl.info.name[1], NULL)) {
                am_log_warning(n->instance_id,
                        "%s failed to load trusted CA certificates file \"%s\"",
                        thisfunc, n->ssl.info.name[1]);
            }
        }
        if (ISVALID(n->ssl.info.name[2])) {
            if (!SSL_CTX_use_certificate_file(n->ssl.ssl_context, n->ssl.info.name[2], SSL_FILETYPE_PEM)) {
                am_log_warning(n->instance_id,
                        "%s failed to load client certificate file \"%s\"",
                        thisfunc, n->ssl.info.name[2]);
            }
        }

        if (ISVALID(n->ssl.info.name[3])) {
            if (ISVALID(n->ssl.info.name[4])) {
                SSL_CTX_set_default_passwd_cb_userdata(n->ssl.ssl_context, (void *) n->ssl.info.name[4]);
                SSL_CTX_set_default_passwd_cb(n->ssl.ssl_context, password_callback);
            }
            if (!SSL_CTX_use_PrivateKey_file(n->ssl.ssl_context, n->ssl.info.name[3], SSL_FILETYPE_PEM)) {
                am_log_warning(n->instance_id,
                        "%s failed to load private key file \"%s\"",
                        thisfunc, n->ssl.info.name[3]);
            }
            if (!SSL_CTX_check_private_key(n->ssl.ssl_context)) {
                am_log_warning(n->instance_id,
                        "%s private key does not match the public certificate",
                        thisfunc);
            }
        }

        if (n->ssl.info.verifypeer == 0) {
            SSL_CTX_set_verify(n->ssl.ssl_context, SSL_VERIFY_NONE, NULL);
        } else {
            SSL_CTX_set_verify(n->ssl.ssl_context, SSL_VERIFY_PEER, NULL);
        }

        n->ssl.ssl_handle = SSL_new(n->ssl.ssl_context);
        if (n->ssl.ssl_handle != NULL) {
            n->ssl.read_bio = BIO_new(BIO_s_mem());
            n->ssl.write_bio = BIO_new(BIO_s_mem());
            if (n->ssl.read_bio != NULL && n->ssl.write_bio != NULL) {
                SSL_set_bio(n->ssl.ssl_handle, n->ssl.read_bio, n->ssl.write_bio);
                SSL_set_connect_state(n->ssl.ssl_handle);
                /*begin the handshake*/
                status = SSL_do_handshake(n->ssl.ssl_handle);
                write_bio_to_socket(n);
                if (status != 1) {
                    err = SSL_get_error(n->ssl.ssl_handle, status);
                    if (!ssl_is_fatal_error(err)) {
                        write_bio_to_socket(n);
                    }
                }
                n->ssl.on = AM_TRUE;
            }
        }
    }
}

static void read_data_after_handshake(am_net_t *n) {
    char buf[1024];
    int ret = 0;
    do {
        memset(&buf[0], 0, sizeof (buf));
        ret = SSL_read(n->ssl.ssl_handle, buf, sizeof (buf) - 1);
        if (ret < 0) {
            int err = SSL_get_error(n->ssl.ssl_handle, ret);
            if (!ssl_is_fatal_error(err)) {
                write_bio_to_socket(n);
            }
        } else {
            http_parser_execute(n->hp, n->hs, buf, ret);
        }
    } while (ret > 0);
}

static void send_data_after_handshake(am_net_t *n) {
    int ret = SSL_write(n->ssl.ssl_handle, n->ssl.request_data, (int) n->ssl.request_data_sz);
    if (ret > 0) {
        write_bio_to_socket(n);
    } else if (ret == 0) {
        /* connection closed */
    } else {
        int err = SSL_get_error(n->ssl.ssl_handle, ret);
        if (!ssl_is_fatal_error(err)) {
            write_bio_to_socket(n);
        }
    }
}

int net_read_ssl(am_net_t *n, const char *buf, int sz) {
    if (sz == 0) {
        read_data_after_handshake(n);
        return AM_EOF;
    } else {
        BIO_write(n->ssl.read_bio, buf, sz);
        if (SSL_state(n->ssl.ssl_handle) != SSL_ST_OK) {
            int ret = SSL_connect(n->ssl.ssl_handle);
            write_bio_to_socket(n);
            if (ret != 1) {
                int err = SSL_get_error(n->ssl.ssl_handle, ret);
                if (!ssl_is_fatal_error(err)) {
                    write_bio_to_socket(n);
                }
            } else {
                send_data_after_handshake(n);
            }
        } else {
            read_data_after_handshake(n);
        }
    }
    return AM_SUCCESS;
}
