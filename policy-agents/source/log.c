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

#define AM_CONFIG_INIT_NAME "am_instance_config_init"
#if defined(_WIN32)
static HANDLE ic_sem = NULL;
#elif defined(__APPLE__)
static semaphore_t ic_sem;
#else
static sem_t *ic_sem = NULL;
#endif   

struct am_shared_log_s {
    void *am_shared;
    size_t am_shared_sz;
    char am_shared_fln[AM_PATH_SIZE];
#ifdef _WIN32
    HANDLE am_shared_id;
    int am_shared_reader_pid;
    HANDLE am_shared_reader_t;
#else
    int am_shared_id;
    pid_t am_shared_reader_pid;
    pthread_t am_shared_reader_t;
#endif
};

static struct am_shared_log_s *am_log_p = NULL;

#ifdef _WIN32

struct am_shared_log_lock_s {
    HANDLE exit;
    HANDLE lock;
    HANDLE queue_empty;
    HANDLE queue_overflow;
};

static struct am_shared_log_lock_s am_log_lck = {NULL, NULL, NULL, NULL};

#endif

/*log header*/
struct am_log_s {
    size_t capacity; /*total number of bytes in a log data buffer*/

    struct log_queue {
        size_t size;
        size_t offset;
        size_t size_wrap;
        size_t offset_wrap;
        unsigned long instance_id;
        char audit;
    } queue[AM_LOG_QUEUE_DEPTH];

    struct log_files {
        char used;
        unsigned long instance_id;
        char name_debug[AM_PATH_SIZE];
        char name_audit[AM_PATH_SIZE];
        int owner;
        int fd_debug;
        int fd_audit;
        size_t max_size_debug;
        size_t max_size_audit;
        time_t created_debug;
        time_t created_audit;
#ifndef _WIN32
        ino_t node_debug;
        ino_t node_audit;
#endif
    } files[AM_MAX_INSTANCES];

    size_t in;
    size_t out;
    volatile size_t waiting;
    volatile size_t queue_size;
    volatile char wrapped;

#ifndef _WIN32
    pthread_mutex_t exit;
    pthread_mutex_t lock;
    pthread_cond_t queue_empty;
    pthread_cond_t queue_overflow;
#endif

    int owner; /*current log reader process id*/

    struct log_level {
        unsigned long instance_id;
        int log;
        int audit;
    } level[AM_MAX_INSTANCES];

    struct valid_url {
        unsigned long instance_id;
        int url_index;
    } valid[AM_MAX_INSTANCES];

    struct instance_init {
        unsigned long instance_id;
        int in_progress;
    } init[AM_MAX_INSTANCES];
};

#ifndef _WIN32

static int need_quit(pthread_mutex_t *mtx) {
    switch (pthread_mutex_trylock(mtx)) {
        case 0:
            pthread_mutex_unlock(mtx);
            return 1;
        case EBUSY:
            return 0;
    }
    return 1;
}
#endif

static char should_rotate_time(time_t ct) {
    time_t ts = ct;
    ts += 86400; /*once in 24 hours*/
    if (difftime(time(NULL), ts) >= 0) {
        return AM_TRUE;
    }
    return AM_FALSE;
}

static void *am_log_worker(void *arg) {
    struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;
    char *log_d = ((char *) am_log_p->am_shared) + sizeof (struct am_log_s);

    do {
        size_t i;
        char *d, *dp, audit;
        unsigned long instance_id;
        struct stat st;
#ifdef _WIN32

        WaitForSingleObject(am_log_lck.lock, INFINITE);
        while (log->waiting == 0) {
            ReleaseMutex(am_log_lck.lock);
            if (WaitForSingleObject(am_log_lck.queue_empty, 500) == WAIT_TIMEOUT) {
                if (WaitForSingleObject(am_log_lck.exit, 0) == WAIT_OBJECT_0) {
                    return NULL;
                }
            }
            WaitForSingleObject(am_log_lck.lock, INFINITE);
        }
#else
        pthread_mutex_lock(&log->lock);
        while (log->waiting == 0) {
            struct timeval now = {0, 0};
            struct timespec ts = {0, 0};
            gettimeofday(&now, NULL);
            ts.tv_sec = now.tv_sec + 1;
            ts.tv_nsec = now.tv_usec * 1000;
            if (pthread_cond_timedwait(&log->queue_empty, &log->lock, &ts) == ETIMEDOUT) {
                if (need_quit(&log->exit)) {
                    pthread_mutex_unlock(&log->lock);
                    return NULL;
                }
            }
        }
#endif

        audit = log->queue[log->out].audit;
        instance_id = log->queue[log->out].instance_id;
        d = calloc(1, log->queue[log->out].size + log->queue[log->out].size_wrap + 1);
        if (d != NULL) {
            dp = d;
            if (log->queue[log->out].size_wrap > 0 && log->queue[log->out].offset_wrap > 0 && log->wrapped == 1) {
                memcpy(d, log_d + log->queue[log->out].offset_wrap, log->queue[log->out].size_wrap);
                dp = d + log->queue[log->out].size_wrap;
                memcpy(dp, log_d, log->queue[log->out].size);
                log->wrapped = 0;
            } else {
                memcpy(dp, log_d + log->queue[log->out].offset, log->queue[log->out].size);
            }
            d[log->queue[log->out].size + log->queue[log->out].size_wrap] = 0;

            if (instance_id > 0) {
                for (i = 0; i < AM_MAX_INSTANCES; i++) {
                    struct log_files *f = &log->files[i];

                    if (f->used == 1 && f->instance_id == instance_id) {
                        if (f->fd_audit == -1 && f->fd_debug == -1) {
#ifdef _WIN32
                            f->fd_debug = _open(f->name_debug, _O_CREAT | _O_WRONLY | _O_APPEND | _O_BINARY,
                                    _S_IREAD | _S_IWRITE);
                            f->fd_audit = _open(f->name_audit, _O_CREAT | _O_WRONLY | _O_APPEND | _O_BINARY,
                                    _S_IREAD | _S_IWRITE);
                            if (f->fd_debug != -1 && stat(f->name_debug, &st) == 0) {
                                f->created_debug = time(NULL);
                                f->owner = getpid();
                            }
                            if (f->fd_audit != -1 && stat(f->name_audit, &st) == 0) {
                                f->created_audit = time(NULL);
                                f->owner = getpid();
                            }
#else
                            f->fd_debug = open(f->name_debug, O_CREAT | O_WRONLY | O_APPEND, S_IWUSR | S_IRUSR);
                            f->fd_audit = open(f->name_audit, O_CREAT | O_WRONLY | O_APPEND, S_IWUSR | S_IRUSR);
                            if (f->fd_debug != -1 && stat(f->name_debug, &st) == 0) {
                                f->node_debug = st.st_ino;
                                f->created_debug = time(NULL);
                                f->owner = getpid();
                            }
                            if (f->fd_audit != -1 && stat(f->name_audit, &st) == 0) {
                                f->node_audit = st.st_ino;
                                f->created_audit = time(NULL);
                                f->owner = getpid();
                            }
#endif
                        }
                        if (f->fd_debug == -1 || f->fd_audit == -1) {
                            fprintf(stderr, "am_log_worker() log file open failed with error: %d", errno);
                            f->fd_debug = f->fd_audit = -1;
                        } else {
                            int fdw = audit == 1 ? f->fd_audit : f->fd_debug;
                            char *fnm = audit == 1 ? f->name_audit : f->name_debug;
                            size_t max_size = audit == 1 ? f->max_size_audit : f->max_size_debug;
#ifndef _WIN32
                            ino_t fdi = audit == 1 ? f->node_audit : f->node_debug;
#endif
                            write(fdw, d, (unsigned int) (log->queue[log->out].size + log->queue[log->out].size_wrap));
#ifdef _WIN32
                            write(fdw, "\r\n", 2);
                            _commit(fdw);

                            /*check file size; rotate if set so*/
                            if (max_size > 0) {
                                BY_HANDLE_FILE_INFORMATION info;
                                uint64_t fsz = 0;
                                HANDLE fh = (HANDLE) _get_osfhandle(fdw);
                                if (GetFileInformationByHandle(fh, &info)) {
                                    fsz = ((DWORDLONG) (((DWORD) (info.nFileSizeLow)) |
                                            (((DWORDLONG) ((DWORD) (info.nFileSizeHigh))) << 32)));
                                }
                                if ((fsz + 1024) > max_size) {
                                    unsigned int idx = 1;
                                    char tmp[4096];
                                    do {
                                        memset(&tmp[0], 0, sizeof (tmp));
                                        snprintf(tmp, sizeof (tmp), "%s.%d", fnm, idx);
                                        idx++;
                                    } while (_access(tmp, 0) == 0);
                                    if (CopyFileA(fnm, tmp, FALSE)) {
                                        SetFilePointer(fh, 0, NULL, FILE_BEGIN);
                                        SetEndOfFile(fh);
                                        if (audit) {
                                            f->created_audit = time(NULL);
                                        } else {
                                            f->created_debug = time(NULL);
                                        }
                                    } else {
                                        fprintf(stderr, "could not rotate log file %s (error: %d)\n",
                                                fnm, GetLastError());
                                    }
                                }
                            }
#else
                            write(fdw, "\n", 1);
                            fsync(fdw);

                            //TODO: optional rotate by date

                            /*check file size; rotate if set so*/
                            if (max_size > 0 && stat(fnm, &st) == 0 && (st.st_size + 1024) > max_size) {
                                unsigned int idx = 1;
                                char tmp[4096];
                                do {
                                    memset(&tmp[0], 0, sizeof (tmp));
                                    snprintf(tmp, sizeof (tmp), "%s.%d", fnm, idx);
                                    idx++;
                                } while (access(tmp, F_OK) == 0);
                                if (rename(fnm, tmp) != 0) {
                                    fprintf(stderr, "could not rotate log file %s (error: %d)\n",
                                            fnm, errno);
                                }
                            }
                            if (stat(fnm, &st) != 0 || st.st_ino != fdi) {
                                close(fdw);
                                if (audit == 1) {
                                    f->fd_audit = open(f->name_audit, O_CREAT | O_WRONLY | O_APPEND, S_IWUSR | S_IRUSR);
                                    f->node_audit = st.st_ino;
                                    f->created_audit = time(NULL);
                                    f->owner = getpid();
                                } else {
                                    f->fd_debug = open(f->name_debug, O_CREAT | O_WRONLY | O_APPEND, S_IWUSR | S_IRUSR);
                                    f->node_debug = st.st_ino;
                                    f->created_debug = time(NULL);
                                    f->owner = getpid();
                                }
                                if (f->fd_debug == -1 || f->fd_audit == -1) {
                                    fprintf(stderr, "am_log_worker() log file re-open failed with error: %d", errno);
                                    f->fd_debug = f->fd_audit = -1;
                                }
                            }
#endif                            
                        }
                        break;
                    }
                }
            }

            free(d);
        }

        --(log->waiting);
        ++(log->out);
        log->out %= log->queue_size;

#ifdef _WIN32
        SetEvent(am_log_lck.queue_overflow);
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_cond_broadcast(&log->queue_overflow);
        pthread_mutex_unlock(&log->lock);
#endif

    } while (
#ifdef _WIN32
            WaitForSingleObject(am_log_lck.exit, 0) == WAIT_TIMEOUT
#else
            !need_quit(&log->exit)
#endif
            );

    return NULL;
}

void am_log_re_init(int status) {
    struct am_log_s *log = am_log_p != NULL ? (struct am_log_s *) am_log_p->am_shared : NULL;
    if (log != NULL && status == AM_RETRY_ERROR) {
#ifdef _WIN32
        WaitForSingleObject(am_log_lck.lock, INFINITE);
#else
        pthread_mutex_lock(&log->lock);
#endif
        log->owner = getpid();
#ifdef _WIN32
        am_log_p->am_shared_reader_t = CreateThread(NULL, 0,
                (LPTHREAD_START_ROUTINE) am_log_worker, NULL, 0, NULL);
#else
        pthread_create(&am_log_p->am_shared_reader_t, NULL, am_log_worker, NULL);
#endif
#ifdef _WIN32
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_mutex_unlock(&log->lock);
#endif
    }
}

void am_log_init(int status) {
    int i;
    const size_t log_data_sz = 1024 * 1024 * 10; /*size of a shared log buffer*/
    char opened = 0;

    am_agent_instance_init_init();

    if (am_log_p == NULL) {
        am_log_p = (struct am_shared_log_s *) malloc(sizeof (struct am_shared_log_s));
        if (am_log_p == NULL) return;
    } else if (am_log_p->am_shared_reader_pid == getpid()) {
        return;
    }

    am_log_p->am_shared_reader_pid = getpid();
    snprintf(am_log_p->am_shared_fln,
            sizeof (am_log_p->am_shared_fln), AM_GLOBAL_PREFIX"am_log_%d", 0);
    am_log_p->am_shared_sz = page_size(log_data_sz + sizeof (struct am_log_s));

#ifdef _WIN32
    am_log_p->am_shared_id = CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE,
            0, (DWORD) am_log_p->am_shared_sz, am_log_p->am_shared_fln);

    if (am_log_p->am_shared_id == NULL) return;
    if (NULL != am_log_p->am_shared_id && GetLastError() == ERROR_ALREADY_EXISTS) {
        opened = 1;
    }

    if (am_log_p->am_shared_id != NULL) {
        am_log_p->am_shared = MapViewOfFile(am_log_p->am_shared_id, FILE_MAP_ALL_ACCESS,
                0, 0, am_log_p->am_shared_sz);
    }
    if (am_log_p->am_shared != NULL) {
        if (status == AM_SUCCESS || status == AM_EAGAIN) {
            struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;

            memset(log, 0, am_log_p->am_shared_sz);
            log->capacity = log_data_sz;
            log->waiting = log->in = log->out = 0;
            log->wrapped = 0;
            log->queue_size = sizeof (log->queue) / sizeof (log->queue[0]);

            for (i = 0; i < AM_MAX_INSTANCES; i++) {
                struct log_files *f = &log->files[i];
                f->fd_audit = f->fd_debug = -1;
                f->used = 0;
                f->instance_id = 0;
                f->max_size_debug = f->max_size_audit = 0;
            }

            am_log_lck.exit = CreateEvent(NULL, FALSE, FALSE, "Global\\am_log_exit");
            if (am_log_lck.exit == NULL && GetLastError() == ERROR_ACCESS_DENIED) {
                am_log_lck.exit = OpenEventA(SYNCHRONIZE, TRUE, "Global\\am_log_exit");
            }
            am_log_lck.lock = CreateMutex(NULL, FALSE, "Global\\am_log_lock");
            if (am_log_lck.lock == NULL && GetLastError() == ERROR_ACCESS_DENIED) {
                am_log_lck.lock = OpenMutexA(SYNCHRONIZE, TRUE, "Global\\am_log_lock");
            }
            am_log_lck.queue_empty = CreateEvent(NULL, FALSE, FALSE, "Global\\am_log_queue_empty");
            if (am_log_lck.queue_empty == NULL && GetLastError() == ERROR_ACCESS_DENIED) {
                am_log_lck.queue_empty = OpenEventA(SYNCHRONIZE, TRUE, "Global\\am_log_queue_empty");
            }
            am_log_lck.queue_overflow = CreateEvent(NULL, FALSE, FALSE, "Global\\am_log_queue_overflow");
            if (am_log_lck.queue_overflow == NULL && GetLastError() == ERROR_ACCESS_DENIED) {
                am_log_lck.queue_overflow = OpenEventA(SYNCHRONIZE, TRUE, "Global\\am_log_queue_overflow");
            }

            log->owner = getpid();
            am_log_p->am_shared_reader_t = CreateThread(NULL, 0,
                    (LPTHREAD_START_ROUTINE) am_log_worker, NULL, 0, NULL);
        }
    }

#else
    am_log_p->am_shared_id = shm_open(am_log_p->am_shared_fln,
            O_CREAT | O_EXCL | O_RDWR, 0666);
    if (am_log_p->am_shared_id == -1 && EEXIST != errno) return;
    if (am_log_p->am_shared_id == -1) {
        /* already there, open without O_EXCL and go; if
         * something goes wrong, close and cleanup */
        am_log_p->am_shared_id = shm_open(am_log_p->am_shared_fln,
                O_RDWR, 0666);
        if (am_log_p->am_shared_id == -1) {
            fprintf(stderr, "am_log_init() shm_open failed (%d)\n", errno);
            free(am_log_p);
            am_log_p = NULL;
            return;
        } else {
            opened = 1;
        }
    } else {
        /* we just created the shm area, must setup; if
         * something goes wrong, delete the shm area and
         * cleanup
         */
        if (ftruncate(am_log_p->am_shared_id, am_log_p->am_shared_sz) == -1) {
            fprintf(stderr, "am_log_init() ftruncate failed\n");
            return;
        }
    }
    if (am_log_p->am_shared_id != -1) {
        am_log_p->am_shared = mmap(NULL, am_log_p->am_shared_sz,
                PROT_READ | PROT_WRITE, MAP_SHARED, am_log_p->am_shared_id, 0);
        if (am_log_p->am_shared == MAP_FAILED) {
            fprintf(stderr, "am_log_init() mmap failed (%d)\n", errno);
            free(am_log_p);
            am_log_p = NULL;
        } else {
            pthread_mutexattr_t exit_attr, lock_attr;
            pthread_condattr_t empty_attr, overflow_attr;

            struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;

            pthread_mutexattr_init(&exit_attr);
            pthread_mutexattr_init(&lock_attr);
            pthread_condattr_init(&empty_attr);
            pthread_condattr_init(&overflow_attr);
            pthread_mutexattr_setpshared(&exit_attr, PTHREAD_PROCESS_SHARED);
            pthread_mutexattr_setpshared(&lock_attr, PTHREAD_PROCESS_SHARED);
            pthread_condattr_setpshared(&empty_attr, PTHREAD_PROCESS_SHARED);
            pthread_condattr_setpshared(&overflow_attr, PTHREAD_PROCESS_SHARED);

            if (status == AM_SUCCESS || status == AM_EAGAIN) {
                memset(log, 0, am_log_p->am_shared_sz);
                log->capacity = log_data_sz;
                log->waiting = log->in = log->out = 0;
                log->wrapped = 0;
                log->queue_size = sizeof (log->queue) / sizeof (log->queue[0]);

                for (i = 0; i < AM_MAX_INSTANCES; i++) {
                    struct log_files *f = &log->files[i];
                    f->fd_audit = f->fd_debug = -1;
                    f->used = 0;
                    f->instance_id = 0;
                    f->max_size_debug = f->max_size_audit = 0;
                }

                pthread_mutex_init(&log->exit, &exit_attr);
                pthread_mutex_init(&log->lock, &lock_attr);
                pthread_cond_init(&log->queue_empty, &empty_attr);
                pthread_cond_init(&log->queue_overflow, &overflow_attr);

                pthread_mutex_lock(&log->exit);
                pthread_create(&am_log_p->am_shared_reader_t, NULL, am_log_worker, NULL);
                log->owner = getpid();
            }

            pthread_mutexattr_destroy(&exit_attr);
            pthread_mutexattr_destroy(&lock_attr);
            pthread_condattr_destroy(&empty_attr);
            pthread_condattr_destroy(&overflow_attr);
        }
    }

#endif
}

void am_log_init_worker(int status) {
#ifdef _WIN32
    am_log_init(status);
#endif
}

void am_log(unsigned long instance_id, int level, const char *format, ...) {
    size_t off, off_out;
    int sz, sr, i, log_lvl = AM_LOG_LEVEL_NONE, aud_lvl = AM_LOG_LEVEL_NONE;
    char *data, quit = AM_FALSE;
    struct am_log_s *log = am_log_p != NULL ?
            (struct am_log_s *) am_log_p->am_shared : NULL;
    char *log_d = log != NULL ?
            ((char *) am_log_p->am_shared) + sizeof (struct am_log_s) : NULL;
    va_list args;

    if (log_d == NULL) return;

    va_start(args, format);
    sz = vsnprintf(NULL, 0, format, args);
    va_end(args);

    data = (char *) calloc(1, sz + 1);
    if (data == NULL) return;

    va_start(args, format);
    sr = vsnprintf(data, sz + 1, format, args);
    va_end(args);

    if (sr <= 0) {
        free(data);
        return;
    }

#ifdef _WIN32
    WaitForSingleObject(am_log_lck.lock, INFINITE);
    while (log->waiting == log->queue_size || log->wrapped == 1) {
        ReleaseMutex(am_log_lck.lock);
        WaitForSingleObject(am_log_lck.queue_overflow, INFINITE);
        WaitForSingleObject(am_log_lck.lock, INFINITE);
    }
#else
    pthread_mutex_lock(&log->lock);
    while (log->waiting == log->queue_size || log->wrapped == 1) {
        pthread_cond_wait(&log->queue_overflow, &log->lock);
    }
#endif

    for (i = 0; i < AM_MAX_INSTANCES; i++) {
        if (log->level[i].instance_id == instance_id) {
            log_lvl = log->level[i].log;
            aud_lvl = log->level[i].audit;
            break;
        }
    }

    /*check if we have any logging configuration at all*/
    if (level == AM_LOG_LEVEL_NONE ||
            (log_lvl == AM_LOG_LEVEL_NONE && ((level & AM_LOG_LEVEL_AUDIT) != AM_LOG_LEVEL_AUDIT)) ||
            (aud_lvl == AM_LOG_LEVEL_NONE && (level & AM_LOG_LEVEL_AUDIT) == AM_LOG_LEVEL_AUDIT)) {
        quit = AM_TRUE;
    }

    /*check configured logging level (LOG_ALLWAYS*/
    if ((level & AM_LOG_LEVEL_AUDIT) != AM_LOG_LEVEL_AUDIT && (level & AM_LOG_LEVEL_ALWAYS) != AM_LOG_LEVEL_ALWAYS) {
        /* DEBUG > INFO > WARNING > ERROR */
        if (level > log_lvl) {
            quit = AM_TRUE;
        }
    }

    /* either this particular instance has no logging configuration or requested log 
     * level does not correspond to one set in instance configuration
     */
    if (quit) {
        free(data);
#ifdef _WIN32
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_mutex_unlock(&log->lock);
#endif
        return;
    }

    off = log->queue[log->in].offset;

    if (sr / 2 > log->capacity) {
        fprintf(stderr, "am_log() shared log buffer is too small (%ld)", log->capacity);
        free(data);
#ifdef _WIN32
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_mutex_unlock(&log->lock);
#endif
        return;
    }

    if (sr + off > log->capacity) {
        size_t count2 = sr + off - log->capacity;
        size_t count1 = sr - count2;
        log->queue[log->in].offset_wrap = log->queue[log->in].size_wrap = 0;
        /* copy first part of a message to the end of ring buffer */
        if (count1 > 0) {
            memcpy(log_d + off, data, count1);
            log->queue[log->in].offset_wrap = off;
            log->queue[log->in].size_wrap = count1;
        }
        /* remaining portion of a message (if any) wraps to start of buffer */
        if (count2 > 0) {
            memcpy(log_d, data + count1, count2);
        }
        off_out = count2;
        log->queue[log->in].size = count2;
        log->wrapped = 1;
    } else {
        memcpy(log_d + off, data, sr);
        log->queue[log->in].size = sr;
        log->queue[log->in].offset_wrap = log->queue[log->in].size_wrap = 0;
        off_out = off + sr;
    }

    log->queue[log->in].instance_id = instance_id;
    log->queue[log->in].audit = (level & AM_LOG_LEVEL_AUDIT) != 0 ? 1 : 0;

    ++(log->waiting);
    ++(log->in);
    log->in %= log->queue_size;
    log->queue[log->in].offset = off_out;

#ifdef _WIN32
    SetEvent(am_log_lck.queue_empty);
    ReleaseMutex(am_log_lck.lock);
#else
    pthread_cond_signal(&log->queue_empty);
    pthread_mutex_unlock(&log->lock);
#endif
    free(data);
}

void am_log_shutdown() {
    static const char *thisfunc = "am_log_shutdown():";
    int i;
    int pid = getpid();
    if (am_log_p != NULL) {
        struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;

        /* notify the logger exit */
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            struct log_files *f = &log->files[i];
            if (f->instance_id > 0 && f->owner == pid) {
                AM_LOG_ALWAYS(f->instance_id, "%s exiting", thisfunc);
            }
        }

#ifdef _WIN32
        SetEvent(am_log_lck.exit);
        WaitForSingleObject(am_log_p->am_shared_reader_t, INFINITE);
        CloseHandle(am_log_lck.exit);
        CloseHandle(am_log_lck.queue_empty);
        CloseHandle(am_log_lck.queue_overflow);

        WaitForSingleObject(am_log_lck.lock, INFINITE);
        /*close log file(s)*/
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            struct log_files *f = &log->files[i];
            if (f->owner == pid) {
                if (f->fd_debug != -1) {
                    _close(f->fd_debug);
                    f->fd_debug = -1;
                }
                if (f->fd_audit != -1) {
                    _close(f->fd_audit);
                    f->fd_audit = -1;
                }
                f->used = 0;
                f->instance_id = 0;
                f->max_size_debug = f->max_size_audit = 0;
            }
        }
        ReleaseMutex(am_log_lck.lock);
        CloseHandle(am_log_lck.lock);
        UnmapViewOfFile(am_log_p->am_shared);
        CloseHandle(am_log_p->am_shared_id);
#else
        pthread_mutex_unlock(&log->exit);
        pthread_join(am_log_p->am_shared_reader_t, NULL);
        pthread_mutex_destroy(&log->exit);
        pthread_mutex_destroy(&log->lock);
        pthread_cond_destroy(&log->queue_overflow);
        pthread_cond_destroy(&log->queue_empty);
        /*close log file(s)*/
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            struct log_files *f = &log->files[i];
            if (f->fd_debug != -1) {
                close(f->fd_debug);
                f->fd_debug = -1;
            }
            if (f->fd_audit != -1) {
                close(f->fd_audit);
                f->fd_audit = -1;
            }
            f->used = 0;
            f->instance_id = 0;
            f->max_size_debug = f->max_size_audit = 0;
        }
        if (munmap((char *) am_log_p->am_shared, am_log_p->am_shared_sz) == -1) {
            fprintf(stderr, "am_log_shutdown() munmap failed (%d)\n", errno);
        }
        close(am_log_p->am_shared_id);
        if (shm_unlink(am_log_p->am_shared_fln) == -1) {
            fprintf(stderr, "am_log_shutdown() shm_unlink failed (%d)\n", errno);
        }
#endif

        am_agent_instance_init_release(AM_TRUE);

        free(am_log_p);
    }
    am_log_p = NULL;
}

int am_log_get_current_owner() {
    int rv = 0;
    struct am_log_s *log = am_log_p != NULL ? (struct am_log_s *) am_log_p->am_shared : NULL;
    if (log != NULL) {
#ifdef _WIN32
        WaitForSingleObject(am_log_lck.lock, INFINITE);
#else
        pthread_mutex_lock(&log->lock);
#endif
        rv = log->owner;
#ifdef _WIN32
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_mutex_unlock(&log->lock);
#endif
    }
    return rv;
}

void am_log_register_instance(unsigned long instance_id, const char *debug_log, int log_level,
        const char *audit_log, int audit_level) {
    int i, exist = 0;
    struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;
    if (instance_id > 0 && debug_log != NULL && audit_log != NULL) {
#ifdef _WIN32
        WaitForSingleObject(am_log_lck.lock, INFINITE);
#else
        pthread_mutex_lock(&log->lock);
#endif
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            struct log_files *f = &log->files[i];
            if (f->instance_id == instance_id) {
                exist = 1;
                break;
            }
        }
        if (exist == 0) {
            for (i = 0; i < AM_MAX_INSTANCES; i++) {
                struct log_files *f = &log->files[i];
                if (f->used == 0) {
                    f->instance_id = instance_id;
                    snprintf(f->name_debug, sizeof (f->name_debug), "%s", debug_log);
                    snprintf(f->name_audit, sizeof (f->name_audit), "%s", audit_log);
                    f->used = 1;
#define MAX_LOG_SIZE (1024 * 1024 * 5) /*5MB*/
                    f->max_size_debug = f->max_size_audit = MAX_LOG_SIZE;
                    f->created_debug = f->created_audit = 0;
                    f->owner = 0;
                    exist = 2;
                    break;
                }
            }
            /*create instance logging level configuration*/
            if (exist == 2)
                for (i = 0; i < AM_MAX_INSTANCES; i++) {
                    if (log->level[i].instance_id == 0) {
                        log->level[i].instance_id = instance_id;
                        log->level[i].log = log_level;
                        log->level[i].audit = audit_level;
                        break;
                    }
                }
        } else {
            /*update instance logging level configuration*/
            for (i = 0; i < AM_MAX_INSTANCES; i++) {
                if (log->level[i].instance_id == instance_id) {
                    log->level[i].log = log_level;
                    log->level[i].audit = audit_level;
                    break;
                }
            }
        }
#ifdef _WIN32
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_mutex_unlock(&log->lock);
#endif
        if (exist == 2) {
#define AM_LOG_HEADER "\r\n\r\n\t######################################################\r\n\t# %-51s#\r\n\t# Version: %-42s#\r\n\t# %-51s#\r\n\t# Build date: %s %-27s#\r\n\t######################################################\r\n"

            AM_LOG_ALWAYS(instance_id, AM_LOG_HEADER, DESCRIPTION, VERSION,
                    VERSION_VCS, __DATE__, __TIME__);

            am_agent_init_set_value(instance_id, AM_TRUE, AM_UNKNOWN);
        }
    }
}

int get_valid_url_index(unsigned long instance_id) {
    int i, value = 0;
    if (am_log_p != NULL) {
        struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;
#ifdef _WIN32
        WaitForSingleObject(am_log_lck.lock, INFINITE);
#else
        pthread_mutex_lock(&log->lock);
#endif
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            if (log->valid[i].instance_id == instance_id) {
                value = log->valid[i].url_index;

                break;
            }
        }
#ifdef _WIN32
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_mutex_unlock(&log->lock);
#endif
    }
    return value;
}

void set_valid_url_index(unsigned long instance_id, int value) {
    int i, set = AM_FALSE;
    if (am_log_p != NULL) {
        struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;
#ifdef _WIN32
        WaitForSingleObject(am_log_lck.lock, INFINITE);
#else
        pthread_mutex_lock(&log->lock);
#endif
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            if (log->valid[i].instance_id == instance_id) {
                log->valid[i].url_index = value;
                set = AM_TRUE;
                break;
            }
        }
        if (!set) {
            for (i = 0; i < AM_MAX_INSTANCES; i++) {
                /*find first empty slot*/
                if (log->valid[i].instance_id == 0) {
                    log->valid[i].url_index = value;
                    log->valid[i].instance_id = instance_id;
                    break;
                }
            }
        }
#ifdef _WIN32
        ReleaseMutex(am_log_lck.lock);
#else
        pthread_mutex_unlock(&log->lock);
#endif
    }
}

int am_agent_instance_init_init() {
    int status = AM_ERROR;
#if defined(_WIN32)
    ic_sem = CreateSemaphoreA(NULL, 1, 1, "Global\\"AM_CONFIG_INIT_NAME);
    if (ic_sem != NULL)
        status = AM_SUCCESS;
#elif defined(__APPLE__)
    kern_return_t rv = semaphore_create(mach_task_self(), &ic_sem, SYNC_POLICY_FIFO, 1);
    if (rv == KERN_SUCCESS)
        status = AM_SUCCESS;
#else
    ic_sem = sem_open(AM_CONFIG_INIT_NAME, O_CREAT, 0600, 1);
    if (ic_sem != SEM_FAILED)
        status = AM_SUCCESS;
#endif
    return status;
}

void am_agent_instance_init_lock() {
#if defined(_WIN32)
    WaitForSingleObject(ic_sem, INFINITE);
#elif defined(__APPLE__)
    semaphore_wait(ic_sem);
#else
    sem_wait(ic_sem);
#endif 
}

void am_agent_instance_init_unlock() {
#if defined(_WIN32)
    ReleaseSemaphore(ic_sem, 1, NULL);
#elif defined(__APPLE__)
    semaphore_signal_all(ic_sem);
#else
    sem_post(ic_sem);
#endif 
}

void am_agent_instance_init_release(char unlink) {
#if defined(_WIN32)
    CloseHandle(ic_sem);
#elif defined(__APPLE__)
    semaphore_destroy(mach_task_self(), ic_sem);
#else
    sem_close(ic_sem);
    if (unlink)
        sem_unlink(AM_CONFIG_INIT_NAME);
#endif 
}

void am_agent_init_set_value(unsigned long instance_id, char lock, int val) {
    int i;
    if (am_log_p != NULL) {
        struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;
        if (lock)
            am_agent_instance_init_lock();
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            if (val == AM_UNKNOWN) {
                /*find first empty slot*/
                if (log->init[i].instance_id == 0) {
                    log->init[i].in_progress = 0;
                    log->init[i].instance_id = instance_id;
                    break;
                }
            } else {
                /*set/reset status value*/
                if (log->init[i].instance_id == instance_id) {
                    log->init[i].in_progress = val;
                    break;
                }
            }
        }
        if (lock)
            am_agent_instance_init_unlock();
    }
}

int am_agent_init_get_value(unsigned long instance_id, char lock) {
    int i, status = AM_FALSE;
    if (am_log_p != NULL) {
        struct am_log_s *log = (struct am_log_s *) am_log_p->am_shared;
        if (lock)
            am_agent_instance_init_lock();
        for (i = 0; i < AM_MAX_INSTANCES; i++) {
            /*get status value*/
            if (log->init[i].instance_id == instance_id) {
                status = log->init[i].in_progress;
                break;
            }
        }
        if (lock)
            am_agent_instance_init_unlock();
    }
    return status;
}
