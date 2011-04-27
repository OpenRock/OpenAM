/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: apache_agent.c,v 1.1 2011/04/26 15:13:00 dknab Exp $
 */

/*
 * Portions Copyrighted 2011 TOOLS.LV SIA
 */

#include <limits.h>
#include <signal.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>

#if defined(LINUX)
#include <dlfcn.h>
#endif
#include <httpd.h>
#include <http_config.h>
#include <http_core.h>
#include <http_protocol.h>
#include <http_request.h>
#include <http_main.h>
#include <http_log.h>
#include <apr.h>
#include <apr_strings.h>
#include <apr_general.h>
#include <apr_shm.h>
#include <apr_rmm.h>
#include <apr_global_mutex.h>
#include <apr_tables.h>
#ifdef _MSC_VER
#include <process.h>
#include <windows.h>
#include <winbase.h>
#else
#include <unistd.h>
#endif

#include "am_web.h"

#define DSAME                   "DSAME"
#define OpenSSO                 "OpenSSO"
#define	MAGIC_STR		"sunpostpreserve"
#define	POST_PRESERVE_URI	"/dummypost/"MAGIC_STR

module AP_MODULE_DECLARE_DATA dsame_module;

static apr_status_t pre_cleanup_dsame(void *data);
static apr_status_t cleanup_dsame(void *data);

typedef void (*sighandler_t)(int);
boolean_t agentInitialized = B_FALSE;
/* This is used to hold SIGTERM while agent is cleaning up
 * and released when done.
 */
static int sigterm_delivered = 0;
/* Notification listener thread sleep interval, in sec.
 * Values equal to 0 will shut down listener thread.
 */
static int am_watchdog_interval = 0;

/* Mutex variable */
static apr_thread_mutex_t *init_mutex = NULL;
/* Notification listener thread handle */
static apr_thread_t *notification_listener_t = NULL;

typedef struct am_notification_list_item* am_notification_list_ptr;

typedef struct am_notification_list_item {
    pid_t pid;
    int read; /*0 - value is unread by pid, 1 - read*/
    char *value;
    am_notification_list_ptr next;
} am_notification_list_item_t;

typedef struct am_post_data_list_item* am_post_data_item_ptr;

typedef struct am_post_data_list_item {
    char *key; //post data key in a cache
    char *action_url; //orig. action url
    char *value; //orig. post data value
    am_post_data_item_ptr next;
} am_post_data_list_item_t;

typedef struct {
    char *properties_file;
    char *bootstrap_file;
    char *notification_lockfile;
    char *postdata_lockfile;
    int notification_shm_size;
    int postdata_shm_size;
    int max_pid_count;
} agent_server_config;

static struct notification_hash_table {
    am_notification_list_item_t **table;
    unsigned int tbl_len;
    unsigned int num_entries;
} *notification_list;

static struct post_data_hash_table {
    am_post_data_list_item_t **table;
    unsigned int tbl_len;
    unsigned int num_entries;
} *post_data_list;

static apr_shm_t *notification_shm = NULL; /* the APR shared segment object */
static apr_rmm_t *notification_rmm = NULL; /* the APR relocatable memory management handler */
static apr_global_mutex_t *notification_lock = NULL; /* the cross-thread/cross-process mutex */
static apr_shm_t *postdata_shm = NULL;
static apr_rmm_t *postdata_rmm = NULL;
static apr_global_mutex_t *postdata_lock = NULL;

static void register_process(int pid) {
    int bucket;
    am_notification_list_ptr entry;
    bucket = pid % notification_list->tbl_len;
    apr_global_mutex_lock(notification_lock);
    entry = apr_rmm_addr_get(notification_rmm, apr_rmm_malloc(notification_rmm, sizeof (am_notification_list_item_t)));
    entry->pid = pid;
    entry->read = 1; /*new pid is just registered, nothing to see here for a listener thread*/
    entry->value = NULL;
    entry->next = notification_list->table[bucket];
    notification_list->table[bucket] = entry;
    notification_list->num_entries++;
    apr_global_mutex_unlock(notification_lock);
}

static void post_notification(char *value) {
    am_notification_list_ptr entry, prev;
    unsigned int i;
    prev = NULL;
    apr_global_mutex_lock(notification_lock);
    for (i = 0; i < notification_list->tbl_len; i++) {
        entry = notification_list->table[i];
        while (entry) {
            prev = entry;
            if (entry->value) {
                /*if value exists, clear it first here*/
                apr_rmm_free(notification_rmm, apr_rmm_offset_get(notification_rmm, entry->value));
            }
            entry->read = 0;
            entry->value = apr_rmm_addr_get(notification_rmm, apr_rmm_calloc(notification_rmm, strlen(value) + 1));
            memcpy(entry->value, value, strlen(value));
            entry = entry->next;
        }
    }
    apr_global_mutex_unlock(notification_lock);
}

static void listall_post_data() {
    unsigned int i;
    am_post_data_item_ptr entry = NULL;
    if (am_web_is_max_debug_on()) {
        am_web_log_max_debug("PDP=========START===========");
        apr_global_mutex_lock(postdata_lock);
        for (i = 0; i < post_data_list->tbl_len; i++) {
            entry = post_data_list->table[i];
            while (entry) {
                am_web_log_max_debug("PDP=========[ %d ]===========", i);
                am_web_log_max_debug("PDP-KEY: %s", entry->key);
                am_web_log_max_debug("PDP-ACTIONURL: %s", entry->action_url);
                am_web_log_max_debug("PDP-VALUE: %s", entry->value);
                am_web_log_max_debug("PDP========================");
                entry = entry->next;
            }
        }
        apr_global_mutex_unlock(postdata_lock);
        am_web_log_max_debug("PDP=========END============");
    }
}

static am_status_t find_post_data(char *id, am_web_postcache_data_t *pd) {
    unsigned int i;
    am_post_data_item_ptr entry = NULL;
    am_status_t status = AM_FAILURE;
    am_bool_t entry_found = AM_FALSE;
    apr_global_mutex_lock(postdata_lock);
    for (i = 0; i < post_data_list->tbl_len; i++) {
        entry = post_data_list->table[i];
        while (entry) {
            if (id != NULL && entry->key != NULL && strcmp(entry->key, id) == 0) {
                /*found it, copy values out and release the memory*/
                pd->url = strdup(entry->action_url);
                pd->value = strdup(entry->value);
                if (entry->key)
                    apr_rmm_free(postdata_rmm, apr_rmm_offset_get(postdata_rmm, entry->key));
                if (entry->value)
                    apr_rmm_free(postdata_rmm, apr_rmm_offset_get(postdata_rmm, entry->value));
                if (entry->action_url)
                    apr_rmm_free(postdata_rmm, apr_rmm_offset_get(postdata_rmm, entry->action_url));
                entry_found = AM_TRUE;
                break;
            }
            entry = entry->next;
        }
        if (entry_found == AM_TRUE) {
            /*remove this row from post-data table*/
            apr_rmm_free(postdata_rmm, apr_rmm_offset_get(postdata_rmm, post_data_list->table[i]));
            post_data_list->table[i] = NULL;
            status = AM_SUCCESS;
            break;
        }
    }
    apr_global_mutex_unlock(postdata_lock);
    return status;
}

static am_status_t update_post_data_for_request(void **args, const char *key, const char *acturl, const char *value) {
    const char *thisfunc = "update_post_data_for_request()";
    am_web_log_info("%s: updating post data cache for key: %s", thisfunc, key);
    request_rec *r = NULL;
    am_status_t sts = AM_SUCCESS;
    unsigned int idx;
    int bucket;
    am_post_data_item_ptr entry = NULL;
    if (args == NULL || (r = (request_rec *) args[0]) == NULL ||
            key == NULL || acturl == NULL) {
        am_web_log_error("%s: invalid argument passed.", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else {
        apr_global_mutex_lock(postdata_lock);
        bucket = post_data_list->num_entries;
        /* check if there are empty rows (deleted by find_post_data)
         * if so - reuse first one found
         */
        for (idx = 0; idx < post_data_list->tbl_len; idx++) {
            if (post_data_list->table[idx] == NULL) {
                bucket = idx;
                break;
            }
        }
        entry = apr_rmm_addr_get(postdata_rmm, apr_rmm_malloc(postdata_rmm, sizeof (am_post_data_list_item_t)));
        if (key) {
            entry->key = apr_rmm_addr_get(postdata_rmm, apr_rmm_calloc(postdata_rmm, strlen(key) + 1));
            memcpy(entry->key, key, strlen(key));
        }
        if (acturl) {
            entry->action_url = apr_rmm_addr_get(postdata_rmm, apr_rmm_calloc(postdata_rmm, strlen(acturl) + 1));
            memcpy(entry->action_url, acturl, strlen(acturl));
        }
        if (value) {
            entry->value = apr_rmm_addr_get(postdata_rmm, apr_rmm_calloc(postdata_rmm, strlen(value) + 1));
            memcpy(entry->value, value, strlen(value));
        }
        entry->next = post_data_list->table[bucket];
        post_data_list->table[bucket] = entry;
        if (bucket == post_data_list->num_entries) {
            /* increase entry count only when adding to the table,
             * not when replacing empty rows
             */
            post_data_list->num_entries++;
        }
        apr_global_mutex_unlock(postdata_lock);
        sts = AM_SUCCESS;
    }
    if (am_web_is_max_debug_on()) {
        listall_post_data();
    }
    return sts;
}

static const char *am_set_string_slot(cmd_parms *cmd, void *dummy, const char *arg) {
    char *error_str = NULL;
    int offset = (int) (long) cmd->info;
    agent_server_config *fsc = ap_get_module_config(cmd->server->module_config, &dsame_module);
    *(const char **) ((char *) fsc + offset) = arg;
    if (*arg == '\0') {
        error_str = apr_psprintf(cmd->pool,
                "Invalid value for directive %s, expected string",
                cmd->directive->directive);
    }
    return error_str;
}

static const char *am_set_int_slot(cmd_parms *cmd, void *dummy, const char *arg) {
    char *endptr;
    char *error_str = NULL;
    int offset = (int) (long) cmd->info;
    agent_server_config *fsc = ap_get_module_config(cmd->server->module_config, &dsame_module);
    *(int *) ((char *) fsc + offset) = strtol(arg, &endptr, 10);
    if ((*arg == '\0') || (*endptr != '\0')) {
        error_str = apr_psprintf(cmd->pool,
                "Invalid value for directive %s, expected integer",
                cmd->directive->directive);
    }
    return error_str;
}

static const command_rec dsame_auth_cmds[] = {
    AP_INIT_TAKE1("Agent_Config_File", am_set_string_slot, (void *) APR_OFFSETOF(agent_server_config, properties_file), RSRC_CONF,
    "Full path of the Agent configuration file"),
    AP_INIT_TAKE1("Agent_Bootstrap_File", am_set_string_slot, (void *) APR_OFFSETOF(agent_server_config, bootstrap_file), RSRC_CONF,
    "Full path of the Agent bootstrap file"),
    AP_INIT_TAKE1("Agent_Notification_Memory_Size", am_set_int_slot, (void *) APR_OFFSETOF(agent_server_config, notification_shm_size), RSRC_CONF,
    "Agent notification module shared memory segment size in bytes"),
    AP_INIT_TAKE1("Agent_PostData_Memory_Size", am_set_int_slot, (void *) APR_OFFSETOF(agent_server_config, postdata_shm_size), RSRC_CONF,
    "Agent pdp module shared memory segment size in bytes"), {
        NULL
    }
};

static void * APR_THREAD_FUNC notification_listener(apr_thread_t *t, void *data) {
    server_rec *s = (server_rec *) data;
    pid_t pid = 0;
    apr_status_t ms;
    unsigned int i;
#ifdef _MSC_VER
    pid = _getpid();
#else
    pid = getpid();
#endif
    am_web_log_info("Starting agent notification listener thread for pid: %d", pid);
    for (;;) {
        if (am_watchdog_interval <= 0)
            break;
        if (agentInitialized == B_TRUE && pid > 0) {
            am_notification_list_ptr entry, prev = NULL;
            ms = apr_global_mutex_trylock(notification_lock);
            if (!APR_STATUS_IS_EBUSY(ms)) {
                am_web_log_max_debug("Notification listener pid: %d, got lock", pid);
                for (i = 0; i < notification_list->tbl_len; i++) {
                    entry = notification_list->table[i];
                    while (entry && pid == entry->pid && entry->read == 0) {
                        prev = entry;
                        am_web_log_max_debug("Notification listener pid: %d, read: %d, value: %s", pid, entry->read, (entry->value ? entry->value : "NULL"));
                        if (entry->value) {
                            apr_thread_mutex_lock(init_mutex);
                            am_web_handle_notification(entry->value, strlen(entry->value), am_web_get_agent_configuration());
                            apr_thread_mutex_unlock(init_mutex);
                        }
                        entry->read = 1;
                        entry = entry->next;
                    }
                }
                apr_global_mutex_unlock(notification_lock);
                am_web_log_max_debug("Notification listener pid: %d, lock released", pid);
            } else {
                am_web_log_max_debug("Notification listener pid: %d, lock busy", pid);
            }
        }
#ifdef _MSC_VER
        Sleep(am_watchdog_interval * 1000);
#else
        sleep(am_watchdog_interval);
#endif
    }
    am_web_log_info("Shutting down policy web agent notification listener thread for pid: %d", pid);
    return NULL;
}

static void *dsame_create_server_config(apr_pool_t *p, server_rec *s) {
    char *tmpdir = NULL;
    agent_server_config *cfg = apr_pcalloc(p, sizeof (agent_server_config));
    if (apr_temp_dir_get((const char**) &tmpdir, p) != APR_SUCCESS) {
        ap_log_error(__FILE__, __LINE__, APLOG_ALERT, 0, s,
                "Policy web agent failed to locate temporary storage directory");
    }
    /*default values*/
    ((agent_server_config *) cfg)->notification_lockfile = apr_pstrcat(p, tmpdir, "/AMNotifLock", NULL);
    ((agent_server_config *) cfg)->postdata_lockfile = apr_pstrcat(p, tmpdir, "/AMPostLock", NULL);
    ((agent_server_config *) cfg)->max_pid_count = 256;
    ((agent_server_config *) cfg)->notification_shm_size = 268435456; //256MB
    ((agent_server_config *) cfg)->postdata_shm_size = 67108864; //64MB
    return (void *) cfg;
}

/*
 * This routine is called by the Apache server when the module is first
 * loaded.  It handles loading all of the shared libraries that are needed
 * to instantiate the DSAME Policy Agent.  If all of the libraries can
 * successfully be loaded, then the routine looks up the two entry points
 * in the actual policy agent: am_web_init and dsame_check_access.  The
 * first routine is invoked directly and an error is logged if it returns
 * an error.  The second routine is inserted into the module interface
 * table for use by Apache during request processing.
 */
static int init_dsame(apr_pool_t *pconf, apr_pool_t *plog, apr_pool_t *ptemp, server_rec *server_ptr) {
    void *lib_handle;
    int ret = OK;
    am_status_t status = AM_SUCCESS;
    apr_status_t rv;
    apr_size_t shm_size, pd_shm_size;
    int idx;
    void *data; /* These two help ensure that we only init once. */
    const char *data_key = "init_dsame";
    agent_server_config *scfg;
    int mp;

    /*
     * The following checks if this routine has been called before.
     * This is necessary because the parent process gets initialized
     * a couple of times as the server starts up, and we don't want
     * to create any more mutexes and shared memory segments than
     * we're actually going to use.
     */
    apr_pool_userdata_get(&data, data_key, server_ptr->process->pool);
    if (!data) {
        apr_pool_userdata_set((const void *) 1, data_key, apr_pool_cleanup_null, server_ptr->process->pool);
        return OK;
    }

#if defined(WINNT)
    LoadLibrary("libnspr4.dll");
    LoadLibrary("libamapc22.dll");
#endif

#if defined(LINUX) 					
    lib_handle = dlopen("libamapc22.so", RTLD_LAZY);
    if (!lib_handle) {
        fprintf(stderr, "Error during dlopen(): %s\n", dlerror());
        exit(1);
    }
#endif

    scfg = ap_get_module_config(server_ptr->module_config, &dsame_module);

    /* If the shared memory/lock file already exists then delete it.  Otherwise we are
     * going to run into problems creating the shared memory.
     */
    if (scfg->notification_lockfile)
        apr_file_remove(scfg->notification_lockfile, pconf);
    if (scfg->postdata_lockfile)
        apr_file_remove(scfg->postdata_lockfile, pconf);

    status = am_web_init(scfg->bootstrap_file, scfg->properties_file);

    if (status == AM_SUCCESS) {
        am_web_log_debug("Process initialization result:%s", am_status_to_string(status));

        ap_add_version_component(pconf, "DSAME/3.0");

        if ((apr_thread_mutex_create(&init_mutex, APR_THREAD_MUTEX_UNNESTED,
                pconf)) != APR_SUCCESS) {
            ap_log_error(__FILE__, __LINE__, APLOG_ALERT, 0, server_ptr,
                    "Policy web agent configuration failed: %s",
                    am_status_to_string(status));
            ret = HTTP_BAD_REQUEST;
        }

        rv = apr_global_mutex_create(&notification_lock, scfg->notification_lockfile, APR_LOCK_DEFAULT, pconf);
        if (rv != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to create "
                    "policy web agent notification global mutex file '%s'",
                    scfg->notification_lockfile);
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        rv = apr_global_mutex_create(&postdata_lock, scfg->postdata_lockfile, APR_LOCK_DEFAULT, pconf);
        if (rv != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to create "
                    "policy web agent postdata global mutex file '%s'",
                    scfg->postdata_lockfile);
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        shm_size = APR_ALIGN_DEFAULT(scfg->notification_shm_size);
        pd_shm_size = APR_ALIGN_DEFAULT(scfg->postdata_shm_size);

        rv = apr_shm_create(&notification_shm, shm_size, NULL, pconf);
        if (rv != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to create "
                    "policy web agent notification anonymous shared segment");
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        rv = apr_shm_create(&postdata_shm, pd_shm_size, NULL, pconf);
        if (rv != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to create "
                    "policy web agent postdata anonymous shared segment");
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        rv = apr_rmm_init(&notification_rmm, NULL, apr_shm_baseaddr_get(notification_shm), shm_size, pconf);
        if (rv != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to create "
                    "policy web agent notification relocatable memory management handler");
            return HTTP_INTERNAL_SERVER_ERROR;
        }

        rv = apr_rmm_init(&postdata_rmm, NULL, apr_shm_baseaddr_get(postdata_shm), pd_shm_size, pconf);
        if (rv != APR_SUCCESS) {
            ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to create "
                    "policy web agent postdata relocatable memory management handler");
            return HTTP_INTERNAL_SERVER_ERROR;
        }
        mp = scfg->max_pid_count;

        notification_list = apr_rmm_addr_get(notification_rmm, apr_rmm_malloc(notification_rmm, sizeof (*notification_list) +
                sizeof (am_notification_list_item_t*) * mp));

        notification_list->table = (am_notification_list_item_t**) (notification_list + 1);
        for (idx = 0; idx < mp; idx++) {
            notification_list->table[idx] = NULL;
        }
        notification_list->tbl_len = mp;
        notification_list->num_entries = 0;

        post_data_list = apr_rmm_addr_get(postdata_rmm, apr_rmm_malloc(postdata_rmm, sizeof (*post_data_list) +
                sizeof (am_post_data_list_item_t*) * mp));

        post_data_list->table = (am_post_data_list_item_t**) (post_data_list + 1);
        for (idx = 0; idx < mp; idx++) {
            post_data_list->table[idx] = NULL;
        }
        post_data_list->tbl_len = mp;
        post_data_list->num_entries = 0;

        ap_log_error(__FILE__, __LINE__, APLOG_NOTICE, 0, server_ptr,
                "Policy web agent shared memory configuration: notif_shm_size[%d], pdp_shm_size[%d], max_pid_count[%d]",
                shm_size, pd_shm_size, mp);
    }
    return ret;
}

/*
 * Called in the child_init hook, this function is needed to
 * register the pre_cleanup_dsame and cleanup_dsame routines to be called upon the child's exit
 * as well as initialize global mutexes, attach to shared memory segment and register
 * process id with a notification listener
 */
static void child_init_dsame(apr_pool_t *pool_ptr, server_rec *server_ptr) {
    apr_status_t rv;
    agent_server_config *scfg = ap_get_module_config(server_ptr->module_config, &dsame_module);

    /*register callback - shut down notification listener thread before apr pool cleanup*/
    apr_pool_pre_cleanup_register(pool_ptr, NULL, pre_cleanup_dsame);
    /*register callback - clean up apr pool and release shared memory, shut down amsdk backend*/
    apr_pool_cleanup_register(pool_ptr, server_ptr, cleanup_dsame, apr_pool_cleanup_null);

    rv = apr_rmm_attach(&notification_rmm, NULL, apr_shm_baseaddr_get(notification_shm), pool_ptr);
    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to attach to "
                "policy web agent notification shared memory management object");
        return;
    }

    rv = apr_global_mutex_child_init(&notification_lock, scfg->notification_lockfile, pool_ptr);
    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to attach to "
                "policy web agent notification global mutex file '%s'",
                scfg->notification_lockfile);
        return;
    }

    rv = apr_rmm_attach(&postdata_rmm, NULL, apr_shm_baseaddr_get(postdata_shm), pool_ptr);
    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to attach to "
                "policy web agent postdata shared memory management object");
        return;
    }

    rv = apr_global_mutex_child_init(&postdata_lock, scfg->postdata_lockfile, pool_ptr);
    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to attach to "
                "policy web agent postdata global mutex file '%s'",
                scfg->postdata_lockfile);
        return;
    }

#ifdef _MSC_VER
    register_process(_getpid());
#else
    register_process(getpid());
#endif

    /*all is set, setup notification thread watchdog interval and start listener thread*/
    am_watchdog_interval = 1; //1 sec
    rv = apr_thread_create(&notification_listener_t, NULL, notification_listener, (void *) server_ptr, pool_ptr);
    if (rv != APR_SUCCESS) {
        ap_log_error(APLOG_MARK, APLOG_CRIT, rv, server_ptr, "Failed to create "
                "policy web agent notification changes listener");
        return;
    }
}

static am_status_t render_result(void **args, am_web_result_t http_result, char *data) {
    request_rec *r = NULL;
    const char *thisfunc = "render_result()";
    int *apache_ret = NULL;
    am_status_t sts = AM_SUCCESS;
    int len = 0;
    if (args == NULL || (r = (request_rec *) args[0]) == NULL,
            (apache_ret = (int *) args[1]) == NULL ||
            ((http_result == AM_WEB_RESULT_OK_DONE ||
            http_result == AM_WEB_RESULT_REDIRECT) &&
            (data == NULL || *data == '\0'))) {
        am_web_log_error("%s: invalid arguments received.", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else {
        // only redirect and OK-DONE need special handling.
        // ok, forbidden and internal error can just set in the result.
        switch (http_result) {
            case AM_WEB_RESULT_OK:
                *apache_ret = OK;
                break;
            case AM_WEB_RESULT_OK_DONE:
                if (data && ((len = strlen(data)) > 0)) {
                    ap_set_content_type(r, "text/html");
                    ap_set_content_length(r, len);
                    ap_rwrite(data, len, r);
                    ap_rflush(r);
                    *apache_ret = DONE;
                } else {
                    *apache_ret = OK;
                }
                break;
            case AM_WEB_RESULT_REDIRECT:
                ap_custom_response(r, HTTP_MOVED_TEMPORARILY, data);
                *apache_ret = HTTP_MOVED_TEMPORARILY;
                break;
            case AM_WEB_RESULT_FORBIDDEN:
                *apache_ret = HTTP_FORBIDDEN;
                break;
            case AM_WEB_RESULT_ERROR:
                *apache_ret = HTTP_INTERNAL_SERVER_ERROR;
                break;
            default:
                am_web_log_error("%s: Unrecognized process result %d.", thisfunc, http_result);
                *apache_ret = HTTP_INTERNAL_SERVER_ERROR;
                break;
        }
        sts = AM_SUCCESS;
    }
    return sts;
}

/**
 * gets request URL
 */
static am_status_t get_request_url(request_rec *r, char **requestURL) {
    const char *thisfunc = "get_request_url()";
    am_status_t status = AM_SUCCESS;
    const char *args = r->args;
    char *server_name = NULL;
    unsigned int port_num = 0;
    const char *host = NULL;
    char *http_method = NULL;
    char port_num_str[40];
    char *args_sep_str = NULL;

    // Get the host name
    if (host != NULL) {
        size_t server_name_len = 0;
        char *colon_ptr = strchr(host, ':');
        am_web_log_max_debug("%s: Host: %s", thisfunc, host);
        if (colon_ptr != NULL) {
            sscanf(colon_ptr + 1, "%u", &port_num);
            server_name_len = colon_ptr - host;
        } else {
            server_name_len = strlen(host);
        }
        server_name = apr_pcalloc(r->pool, server_name_len + 1);
        memcpy(server_name, host, server_name_len);
        server_name[server_name_len] = '\0';
    } else {
        server_name = (char *) r->hostname;
    }

    // In case of virtual servers with only a
    // IP address, use hostname defined in server_req
    // for the request hostname value
    if (server_name == NULL) {
        server_name = (char *) r->server->server_hostname;
        am_web_log_debug("%s: Host set to server hostname %s.",
                thisfunc, server_name);
    }
    if (server_name == NULL || strlen(server_name) == 0) {
        am_web_log_error("%s: Could not get the hostname.", thisfunc);
        status = AM_FAILURE;
    } else {
        am_web_log_debug("%s: hostname = %s", thisfunc, server_name);
    }
    if (status == AM_SUCCESS) {
        // Get the port
        if (port_num == 0) {
            port_num = r->server->port;
        }
        // Virtual servers set the port to 0 when listening on the default port.
        // This creates problems, so set it back to default port
        if (port_num == 0) {
            port_num = ap_default_port(r);
            am_web_log_debug("%s: Port is 0. Set to default port %u.",
                    thisfunc, ap_default_port(r));
        }
    }
    am_web_log_debug("%s: port = %u", thisfunc, port_num);
    sprintf(port_num_str, ":%u", port_num);
    // Get the protocol
    http_method = (char *) ap_http_scheme(r);
    // Get the query
    if (NULL == args || '\0' == args[0]) {
        args_sep_str = "";
        args = "";
    } else {
        args_sep_str = "?";
    }
    am_web_log_debug("%s: query = %s", thisfunc, args);

    // Construct the url
    // <method>:<host><:port or nothing><uri><? or nothing><args or nothing>
    *requestURL = apr_psprintf(r->pool, "%s://%s%s%s%s%s",
            http_method,
            server_name,
            port_num_str,
            r->uri,
            args_sep_str,
            args);
    am_web_log_debug("%s: Returning request URL = %s.", thisfunc, *requestURL);
    return status;
}

/**
 * gets content if this is notification.
 */
static am_status_t content_read(void **args, char **rbuf) {
    const char *thisfunc = "content_read()";
    request_rec *r = NULL;
    int rc = 0;
    int rsize = 0, len_read = 0, rpos = 0;
    int sts = AM_FAILURE;
    const char *new_clen_val = NULL;

    if (args == NULL || (r = (request_rec *) args[0]) == NULL || rbuf == NULL) {
        am_web_log_error("%s: invalid arguments passed.", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else if ((rc = ap_setup_client_block(r, REQUEST_CHUNKED_ERROR)) != OK) {
        am_web_log_error("%s: error setup client block: %d", thisfunc, rc);
        sts = AM_FAILURE;
    } else if (ap_should_client_block(r)) {
        char argsbuffer[HUGE_STRING_LEN];
        long length = r->remaining;
        *rbuf = apr_pcalloc(r->pool, length + 1);
        while ((len_read = ap_get_client_block(r, argsbuffer, sizeof (argsbuffer))) > 0) {
            if ((rpos + len_read) > length) {
                rsize = length - rpos;
            } else {
                rsize = len_read;
            }
            memcpy((char*) * rbuf + rpos, argsbuffer, rsize);
            rpos = rpos + rsize;
        }
        am_web_log_debug("%s: Read %d bytes", thisfunc, rpos);
        sts = AM_SUCCESS;
    }

    // Remove the content length since the body has been read.
    // If the content length is not reset, servlet containers think
    // the request is a POST.
    if (sts == AM_SUCCESS) {
        r->clength = 0;
        apr_table_unset(r->headers_in, "Content-Length");
        new_clen_val = apr_table_get(r->headers_in, "Content-Length");
        am_web_log_max_debug("content_read(): New value "
                "of content length after reset: %s",
                new_clen_val ? "(NULL)" : new_clen_val);
    }
    return sts;
}

static am_status_t set_header_in_request(void **args, const char *key, const char *values) {
    const char *thisfunc = "set_header_in_request()";
    request_rec *r = NULL;
    am_status_t sts = AM_SUCCESS;
    if (args == NULL || (r = (request_rec *) args[0]) == NULL ||
            key == NULL) {
        am_web_log_error("%s: invalid argument passed.", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else {
        // remove all instances of the header first.
        apr_table_unset(r->headers_in, key);
        if (values != NULL && *values != '\0') {
            apr_table_set(r->headers_in, key, values);
        }
        sts = AM_SUCCESS;
    }
    return sts;
}

static am_status_t add_header_in_response(void **args, const char *key, const char *values) {
    const char *thisfunc = "add_header_in_response()";
    request_rec *r = NULL;
    am_status_t sts = AM_SUCCESS;
    if (args == NULL || (r = (request_rec *) args[0]) == NULL ||
            key == NULL) {
        am_web_log_error("%s: invalid argument passed.", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else {
        /* Apache keeps two separate server response header tables in the request 
         * record—one for normal response headers and one for error headers. 
         * The difference between them is that the error headers are sent to 
         * the client even (not only) on an error response (REDIRECT is one of them)
         */
        apr_table_add(r->err_headers_out, key, values);
        sts = AM_SUCCESS;
    }
    return sts;
}

static am_status_t set_method(void **args, am_web_req_method_t method) {
    const char *thisfunc = "set_method()";
    request_rec *r = NULL;
    am_status_t sts = AM_SUCCESS;

    if (args == NULL || (r = (request_rec *) args[0]) == NULL) {
        am_web_log_error("%s: invalid argument passed.", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else {
        switch (method) {
            case AM_WEB_REQUEST_GET:
                r->method_number = M_GET;
                r->method = REQUEST_METHOD_GET;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_POST:
                r->method_number = M_POST;
                r->method = REQUEST_METHOD_POST;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_PUT:
                r->method_number = M_PUT;
                r->method = REQUEST_METHOD_PUT;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_DELETE:
                r->method_number = M_DELETE;
                r->method = REQUEST_METHOD_DELETE;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_OPTIONS:
                r->method_number = M_OPTIONS;
                r->method = REQUEST_METHOD_OPTIONS;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_CONNECT:
                r->method_number = M_CONNECT;
                r->method = REQUEST_METHOD_CONNECT;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_COPY:
                r->method_number = M_COPY;
                r->method = REQUEST_METHOD_COPY;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_INVALID:
                r->method_number = M_INVALID;
                r->method = REQUEST_METHOD_INVALID;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_LOCK:
                r->method_number = M_LOCK;
                r->method = REQUEST_METHOD_LOCK;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_UNLOCK:
                r->method_number = M_UNLOCK;
                r->method = REQUEST_METHOD_UNLOCK;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_MOVE:
                r->method_number = M_MOVE;
                r->method = REQUEST_METHOD_MOVE;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_PATCH:
                r->method_number = M_PATCH;
                r->method = REQUEST_METHOD_PATCH;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_PROPFIND:
                r->method_number = M_PROPFIND;
                r->method = REQUEST_METHOD_PROPFIND;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_PROPPATCH:
                r->method_number = M_PROPPATCH;
                r->method = REQUEST_METHOD_PROPPATCH;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_VERSION_CONTROL:
                r->method_number = M_VERSION_CONTROL;
                r->method = REQUEST_METHOD_VERSION_CONTROL;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_CHECKOUT:
                r->method_number = M_CHECKOUT;
                r->method = REQUEST_METHOD_CHECKOUT;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_UNCHECKOUT:
                r->method_number = M_UNCHECKOUT;
                r->method = REQUEST_METHOD_UNCHECKOUT;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_CHECKIN:
                r->method_number = M_CHECKIN;
                r->method = REQUEST_METHOD_CHECKIN;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_UPDATE:
                r->method_number = M_UPDATE;
                r->method = REQUEST_METHOD_UPDATE;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_LABEL:
                r->method_number = M_LABEL;
                r->method = REQUEST_METHOD_LABEL;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_REPORT:
                r->method_number = M_REPORT;
                r->method = REQUEST_METHOD_REPORT;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_MKWORKSPACE:
                r->method_number = M_MKWORKSPACE;
                r->method = REQUEST_METHOD_MKWORKSPACE;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_MKACTIVITY:
                r->method_number = M_MKACTIVITY;
                r->method = REQUEST_METHOD_MKACTIVITY;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_BASELINE_CONTROL:
                r->method_number = M_BASELINE_CONTROL;
                r->method = REQUEST_METHOD_BASELINE_CONTROL;
                sts = AM_SUCCESS;
                break;
            case AM_WEB_REQUEST_MERGE:
                r->method_number = M_MERGE;
                r->method = REQUEST_METHOD_MERGE;
                sts = AM_SUCCESS;
                break;
            default:
                sts = AM_INVALID_ARGUMENT;
                am_web_log_error("%s: invalid method [%s] passed.",
                        thisfunc, am_web_method_num_to_str(method));
                break;
        }
    }
    return sts;
}

static am_status_t set_user(void **args, const char *user) {
    const char *thisfunc = "set_user()";
    request_rec *r = NULL;
    am_status_t sts = AM_SUCCESS;

    if (args == NULL || (r = (request_rec *) args[0]) == NULL) {
        am_web_log_error("%s: invalid argument passed.", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else {
        if (user == NULL) {
            user = "";
        }
        r->user = apr_pstrdup(r->pool, user);
        r->ap_auth_type = apr_pstrdup(r->pool, OpenSSO);
        am_web_log_debug("%s: user set to %s", thisfunc, user);
        sts = AM_SUCCESS;
    }
    return sts;
}

static int get_apache_method_num(am_web_req_method_t am_num) {
    int apache_num = -1;
    switch (am_num) {
        case AM_WEB_REQUEST_GET:
            apache_num = M_GET;
            break;
        case AM_WEB_REQUEST_POST:
            apache_num = M_POST;
            break;
        case AM_WEB_REQUEST_PUT:
            apache_num = M_PUT;
            break;
        case AM_WEB_REQUEST_DELETE:
            apache_num = M_DELETE;
            break;
        case AM_WEB_REQUEST_TRACE:
            apache_num = M_TRACE;
            break;
        case AM_WEB_REQUEST_OPTIONS:
            apache_num = M_OPTIONS;
            break;
        case AM_WEB_REQUEST_CONNECT:
            apache_num = M_CONNECT;
            break;
        case AM_WEB_REQUEST_COPY:
            apache_num = M_COPY;
            break;
        case AM_WEB_REQUEST_INVALID:
            apache_num = M_INVALID;
            break;
        case AM_WEB_REQUEST_LOCK:
            apache_num = M_LOCK;
            break;
        case AM_WEB_REQUEST_UNLOCK:
            apache_num = M_UNLOCK;
            break;
        case AM_WEB_REQUEST_MOVE:
            apache_num = M_MOVE;
            break;
        case AM_WEB_REQUEST_MKCOL:
            apache_num = M_MKCOL;
            break;
        case AM_WEB_REQUEST_PATCH:
            apache_num = M_PATCH;
            break;
        case AM_WEB_REQUEST_PROPFIND:
            apache_num = M_PROPFIND;
            break;
        case AM_WEB_REQUEST_PROPPATCH:
            apache_num = M_PROPPATCH;
            break;
        case AM_WEB_REQUEST_VERSION_CONTROL:
            apache_num = M_VERSION_CONTROL;
            break;
        case AM_WEB_REQUEST_CHECKOUT:
            apache_num = M_CHECKOUT;
            break;
        case AM_WEB_REQUEST_UNCHECKOUT:
            apache_num = M_UNCHECKOUT;
            break;
        case AM_WEB_REQUEST_CHECKIN:
            apache_num = M_CHECKIN;
            break;
        case AM_WEB_REQUEST_UPDATE:
            apache_num = M_UPDATE;
            break;
        case AM_WEB_REQUEST_LABEL:
            apache_num = M_LABEL;
            break;
        case AM_WEB_REQUEST_REPORT:
            apache_num = M_REPORT;
            break;
        case AM_WEB_REQUEST_MKWORKSPACE:
            apache_num = M_MKWORKSPACE;
            break;
        case AM_WEB_REQUEST_MKACTIVITY:
            apache_num = M_MKACTIVITY;
            break;
        case AM_WEB_REQUEST_BASELINE_CONTROL:
            apache_num = M_BASELINE_CONTROL;
            break;
        case AM_WEB_REQUEST_MERGE:
            apache_num = M_MERGE;
            break;
    }
    return apache_num;
}

static am_web_req_method_t get_method_num(request_rec *r) {
    const char *thisfunc = "get_method_num()";
    am_web_req_method_t method_num = AM_WEB_REQUEST_UNKNOWN;
    // get request method from method number first cuz it's
    // faster. if not a recognized method, get it from the method string.
    switch (r->method_number) {
        case M_GET:
            method_num = AM_WEB_REQUEST_GET;
            break;
        case M_POST:
            method_num = AM_WEB_REQUEST_POST;
            break;
        case M_PUT:
            method_num = AM_WEB_REQUEST_PUT;
            break;
        case M_DELETE:
            method_num = AM_WEB_REQUEST_DELETE;
            break;
        case M_TRACE:
            method_num = AM_WEB_REQUEST_TRACE;
            break;
        case M_OPTIONS:
            method_num = AM_WEB_REQUEST_OPTIONS;
            break;
        case M_CONNECT:
            method_num = AM_WEB_REQUEST_CONNECT;
            break;
        case M_COPY:
            method_num = AM_WEB_REQUEST_COPY;
            break;
        case M_INVALID:
            method_num = AM_WEB_REQUEST_INVALID;
            break;
        case M_LOCK:
            method_num = AM_WEB_REQUEST_LOCK;
            break;
        case M_UNLOCK:
            method_num = AM_WEB_REQUEST_UNLOCK;
            break;
        case M_MOVE:
            method_num = AM_WEB_REQUEST_MOVE;
            break;
        case M_MKCOL:
            method_num = AM_WEB_REQUEST_MKCOL;
            break;
        case M_PATCH:
            method_num = AM_WEB_REQUEST_PATCH;
            break;
        case M_PROPFIND:
            method_num = AM_WEB_REQUEST_PROPFIND;
            break;
        case M_PROPPATCH:
            method_num = AM_WEB_REQUEST_PROPPATCH;
            break;
        case M_VERSION_CONTROL:
            method_num = AM_WEB_REQUEST_VERSION_CONTROL;
            break;
        case M_CHECKOUT:
            method_num = AM_WEB_REQUEST_CHECKOUT;
            break;
        case M_UNCHECKOUT:
            method_num = AM_WEB_REQUEST_UNCHECKOUT;
            break;
        case M_CHECKIN:
            method_num = AM_WEB_REQUEST_CHECKIN;
            break;
        case M_UPDATE:
            method_num = AM_WEB_REQUEST_UPDATE;
            break;
        case M_LABEL:
            method_num = AM_WEB_REQUEST_LABEL;
            break;
        case M_REPORT:
            method_num = AM_WEB_REQUEST_REPORT;
            break;
        case M_MKWORKSPACE:
            method_num = AM_WEB_REQUEST_MKWORKSPACE;
            break;
        case M_MKACTIVITY:
            method_num = AM_WEB_REQUEST_MKACTIVITY;
            break;
        case M_BASELINE_CONTROL:
            method_num = AM_WEB_REQUEST_BASELINE_CONTROL;
            break;
        case M_MERGE:
            method_num = AM_WEB_REQUEST_MERGE;
            break;
        default:
            method_num = AM_WEB_REQUEST_UNKNOWN;
            break;
    }
    am_web_log_debug("%s: Method string is %s", thisfunc, r->method);
    am_web_log_debug("%s: Apache method number corresponds to %s method",
            thisfunc, am_web_method_num_to_str(method_num));

    // Check if method number and method string correspond
    if (method_num == AM_WEB_REQUEST_UNKNOWN) {
        // If method string is not null, set the correct method number
        if (r->method != NULL && *(r->method) != '\0') {
            method_num = am_web_method_str_to_num(r->method);
            r->method_number = get_apache_method_num(method_num);
            am_web_log_debug("%s: Set method number to correspond to %s method",
                    thisfunc, r->method);
        }
    } else if (strcasecmp(r->method, am_web_method_num_to_str(method_num))
            && (method_num != AM_WEB_REQUEST_INVALID)) {
        // If the method number and the method string do not match,
        // correct the method string. But if the method number is invalid
        // the method string needs to be preserved in case Apache is
        // used as a proxy (in front of Exchange Server for instance)
        r->method = am_web_method_num_to_str(method_num);
        am_web_log_debug("%s: Set method string to %s", thisfunc, r->method);
    }
    return method_num;
}

/**
 * Deny the access in case the agent is found uninitialized
 */
static int do_deny(request_rec *r, am_status_t status) {
    int retVal = HTTP_FORBIDDEN;
    /* Set the return code 403 Forbidden */
    r->content_type = "text/plain";
    ap_custom_response(r, HTTP_FORBIDDEN,
            "Access denied as Agent profile not"
            " found in Access Manager.");
    am_web_log_info("do_deny() Status code= %s.",
            am_status_to_string(status));
    return retVal;
}

/**
 * Send HTTP_INTERNAL_SERVER_ERROR in case of an error
 */
static int send_error(request_rec *r) {
    int retVal = HTTP_INTERNAL_SERVER_ERROR;
    r->content_type = "text/plain";
    ap_custom_response(r, HTTP_INTERNAL_SERVER_ERROR,
            "Server encountered an error while processing"
            " request to/from OpenAM.");
    am_web_log_info("send_error() HTTP_INTERNAL_SERVER_ERROR");
    return retVal;
}

static char *get_cookie(request_rec *r, const char *cookie_name) {
    char *raw_cookie_start = NULL, *raw_cookie_end, *cookie;
    char *raw_cookie = (char*) apr_table_get(r->headers_in, "Cookie");
    if (!(raw_cookie)) return 0;
    do {
        if (!(raw_cookie = strstr(raw_cookie, cookie_name))) return NULL;
        raw_cookie_start = raw_cookie;
        if (!(raw_cookie = strchr(raw_cookie, '='))) return NULL;
    } while (strncmp(cookie_name, raw_cookie_start, raw_cookie - raw_cookie_start) != 0);
    raw_cookie++; //skip '='
    if (!((raw_cookie_end = strchr(raw_cookie, ';')) || (raw_cookie_end = strchr(raw_cookie, '\0')))) return NULL;
    if (!(cookie = apr_pstrndup(r->pool, raw_cookie, raw_cookie_end - raw_cookie))) return NULL;
    if (!(ap_unescape_url(cookie) == 0)) return NULL;
    return cookie;
}

static am_status_t set_cookie(const char *header, void **args) {
    am_status_t ret = AM_INVALID_ARGUMENT;
    char *currentCookies;
    if (header != NULL && args != NULL) {
        request_rec *rq = (request_rec *) args[0];
        if (rq == NULL) {
            am_web_log_error("in set_cookie: Invalid Request structure");
        } else {
            apr_table_add(rq->err_headers_out, "Set-Cookie", header);
            if ((currentCookies = (char *) apr_table_get(rq->headers_in, "Cookie")) == NULL)
                apr_table_add(rq->headers_in, "Cookie", header);
            else
                apr_table_set(rq->headers_in, "Cookie", (apr_pstrcat(rq->pool, header, ";", currentCookies, NULL)));
            ret = AM_SUCCESS;
        }
    }
    return ret;
}

/**
 * This function is invoked to initialize the agent
 * during the first request.
 */
void init_at_request() {
    am_status_t status;
    status = am_agent_init(&agentInitialized);
    if (status != AM_SUCCESS) {
        am_web_log_debug("Initialization of the agent failed: "
                "status = %s (%d)", am_status_to_string(status), status);
    }
}

static am_status_t check_for_post_data(char *requestURL, char **page, void *agent_config) {
    const char *thisfunc = "check_for_post_data()";
    const char *post_data_query = NULL;
    am_web_postcache_data_t get_data = {NULL, NULL};
    const char *actionurl = NULL;
    const char *postdata_cache = NULL;
    am_status_t status = AM_SUCCESS;
    am_status_t status_tmp = AM_SUCCESS;
    char* buffer_page = NULL;
    char *stickySessionValue = NULL;
    char *stickySessionPos = NULL;
    char *temp_uri = NULL;
    *page = NULL;

    if (requestURL == NULL) {
        status = AM_INVALID_ARGUMENT;
    }
    // Check if magic URI is present in the URL
    if (status == AM_SUCCESS) {
        post_data_query = strstr(requestURL, POST_PRESERVE_URI);
        if (post_data_query != NULL) {
            post_data_query += strlen(POST_PRESERVE_URI);
            // Check if a query parameter for the  sticky session has been
            // added to the dummy URL. Remove it if it is the case.
            status_tmp = am_web_get_postdata_preserve_URL_parameter
                    (&stickySessionValue, agent_config);
            if (status_tmp == AM_SUCCESS) {
                stickySessionPos = strstr(post_data_query, stickySessionValue);
                if (stickySessionPos != NULL) {
                    size_t len = strlen(post_data_query) -
                            strlen(stickySessionPos) - 1;
                    temp_uri = malloc(len + 1);
                    memset(temp_uri, 0, len + 1);
                    strncpy(temp_uri, post_data_query, len);
                    post_data_query = temp_uri;
                }
            }
        }
    }
    // If magic uri present search for corresponding value in shared cache
    if ((status == AM_SUCCESS) && (post_data_query != NULL) &&
            (strlen(post_data_query) > 0)) {
        am_web_log_debug("%s: POST Magic Query Value: %s", thisfunc, post_data_query);
        if (am_web_is_max_debug_on()) {
            listall_post_data();
        }
        if ((find_post_data((char*) post_data_query, &get_data)) == AM_SUCCESS) {
            postdata_cache = get_data.value;
            actionurl = get_data.url;
            am_web_log_debug("%s: POST cache actionurl: %s",
                    thisfunc, actionurl);
            // Create the post page
            buffer_page = am_web_create_post_page(post_data_query,
                    postdata_cache, actionurl, agent_config);
            *page = strdup(buffer_page);
            if (*page == NULL) {
                am_web_log_error("%s: Not enough memory to allocate page");
                status = AM_NO_MEMORY;
            }
            am_web_postcache_data_cleanup(&get_data);
            if (buffer_page != NULL) {
                am_web_free_memory(buffer_page);
            }
        } else {
            am_web_log_error("%s: Found magic URI (%s) but entry is not in POST"
                    " hash table", thisfunc, post_data_query);
            status = AM_FAILURE;
        }
    }
    if (temp_uri != NULL) {
        free(temp_uri);
        temp_uri = NULL;
    }
    if (stickySessionValue != NULL) {
        am_web_free_memory(stickySessionValue);
        stickySessionValue = NULL;
    }
    return status;
}

/**
 * Grant access depending on policy and session evaluation
 */
int dsame_check_access(request_rec *r) {
    const char *thisfunc = "dsame_check_access()";
    am_status_t status = AM_SUCCESS;
    am_status_t pdp_status = AM_SUCCESS;
    am_status_t status_tmp = AM_SUCCESS;
    int ret = OK;
    char *url = NULL;
    void *args[] = {(void *) r, (void *) & ret};
    const char *clientIP_hdr_name = NULL;
    char *clientIP_hdr = NULL;
    char *clientIP = NULL;
    const char *clientHostname_hdr_name = NULL;
    char *clientHostname_hdr = NULL;
    char *clientHostname = NULL;
    am_web_req_method_t method;
    am_web_request_params_t req_params;
    am_web_request_func_t req_func;
    void* agent_config = NULL;
    char *post_page = NULL;
    char *dpro_cookie = NULL;
    char *response = NULL;

    memset((void *) & req_params, 0, sizeof (req_params));
    memset((void *) & req_func, 0, sizeof (req_func));

    /**
     * Initialize the agent during first request
     * Should not be repeated during subsequent requests.
     */
    if (agentInitialized != B_TRUE) {
        apr_thread_mutex_lock(init_mutex);
        am_web_log_info("%s: Locked initialization section.", thisfunc);
        if (agentInitialized != B_TRUE) {
            (void) init_at_request();
            if (agentInitialized != B_TRUE) {
                ret = do_deny(r, status);
                status = AM_FAILURE;
            }
        }
        apr_thread_mutex_unlock(init_mutex);
        am_web_log_info("%s: Unlocked initialization section.", thisfunc);
    }
    if (status == AM_SUCCESS) {
        // Get the agent config
        agent_config = am_web_get_agent_configuration();
        // Check request
        if (r == NULL) {
            am_web_log_error("%s: Request to http server is NULL!", thisfunc);
            status = AM_FAILURE;
        }
    }
    am_web_log_info("%s: starting...", thisfunc);
    // Check arguments
    if (r == NULL) {
        am_web_log_error("%s: Request to http server is NULL.", thisfunc);
        status = AM_FAILURE;
    }
    if (status == AM_SUCCESS) {
        if (r->connection == NULL) {
            am_web_log_error("%s: Request connection is NULL.", thisfunc);
            status = AM_FAILURE;
        }
    }
    // Get the request URL
    if (status == AM_SUCCESS) {
        status = get_request_url(r, &url);
    }

    // Get the request method
    if (status == AM_SUCCESS) {
        method = get_method_num(r);
        if (method == AM_WEB_REQUEST_UNKNOWN) {
            am_web_log_error("%s: Request method is unknown.", thisfunc);
            status = AM_FAILURE;
        }
    }

    // Check notification URL
    if (status == AM_SUCCESS) {
        if (B_TRUE == am_web_is_notification(url, agent_config)) {
            char* data = NULL;
            status = content_read((void*) &r, &data);
            if (status == AM_SUCCESS) {
                post_notification(data);
                /*notification is received, respond with HTTP200 and OK in response body*/
                ap_set_content_type(r, "text/html");
                ap_set_content_length(r, 2);
                ap_rwrite("OK", 2, r);
                ap_rflush(r);
                /*data is allocated on apr pool, will be released together with a pool*/
                am_web_delete_agent_configuration(agent_config);
                return DONE;
            }
        }
    }

    // If post preserve data is enabled, check if there is post data
    // in the post data cache
    if (status == AM_SUCCESS) {
        if (B_TRUE == am_web_is_postpreserve_enabled(agent_config)) {
            pdp_status = check_for_post_data(url, &post_page, agent_config);
        }
    }

    //Check if the SSO token is in the HTTP_COOKIE header
    if (status == AM_SUCCESS) {
        const char *cookieName = am_web_get_cookie_name(agent_config);
        if (cookieName != NULL) {
            dpro_cookie = get_cookie(r, cookieName);
        }
    }

    // If there is a proxy in front of the agent, the user can set in the
    // properties file the name of the headers that the proxy uses to set
    // the real client IP and host name. In that case the agent needs
    // to use the value of these headers to process the request
    if (status == AM_SUCCESS) {
        // Get the client IP address header set by the proxy, if there is one
        clientIP_hdr_name = am_web_get_client_ip_header_name(agent_config);
        if (clientIP_hdr_name != NULL) {
            clientIP_hdr = (char *) apr_table_get(r->headers_in,
                    clientIP_hdr_name);
        }
        // Get the client host name header set by the proxy, if there is one
        clientHostname_hdr_name =
                am_web_get_client_hostname_header_name(agent_config);
        if (clientHostname_hdr_name != NULL) {
            clientHostname_hdr = (char *) apr_table_get(r->headers_in,
                    clientHostname_hdr_name);
        }
        // If the client IP and host name headers contain more than one
        // value, take the first value.
        if ((clientIP_hdr != NULL && strlen(clientIP_hdr) > 0) ||
                (clientHostname_hdr != NULL && strlen(clientHostname_hdr) > 0)) {
            status = am_web_get_client_ip_host(clientIP_hdr,
                    clientHostname_hdr,
                    &clientIP, &clientHostname);
        }
    }

    if (am_web_is_max_debug_on()) {
        am_web_log_max_debug("%s: post page: [%s], dpro cookie: [%s]", thisfunc, post_page, dpro_cookie);
    }

    //In CDSSO mode, check if the sso token is in the post data
    if (status == AM_SUCCESS) {
        if ((am_web_is_cdsso_enabled(agent_config) == B_TRUE) && method == AM_WEB_REQUEST_POST) {
            if ((dpro_cookie == NULL || (strlen(dpro_cookie) == 0))
                    && post_page != NULL) {
                char *recv_token = NULL;
                status = content_read((void*) &r, &response);
                if (status == AM_SUCCESS) {
                    status = am_web_get_token_from_assertion(response, &recv_token, agent_config);
                    if (status != AM_SUCCESS) {
                        am_web_log_error("%s: am_web_get_token_from_assertion() "
                                "failed with error code: %s",
                                thisfunc, am_status_to_string(status));
                    } else {
                        am_web_log_debug("%s: recv_token : %s", thisfunc, recv_token);
                        // Set cookie in browser for the foreign domain.
                        am_web_do_cookie_domain_set(set_cookie, args, recv_token, agent_config);
                    }
                }
                if (recv_token) {
                    free(recv_token);
                }
            }
        }
    }

    // Set the client ip in the request parameters structure
    if (status == AM_SUCCESS) {
        if (clientIP == NULL) {
            req_params.client_ip = (char *) r->connection->remote_ip;
        } else {
            req_params.client_ip = clientIP;
        }
        if ((req_params.client_ip == NULL) ||
                (strlen(req_params.client_ip) == 0)) {
            am_web_log_error("%s: Could not get the remote IP.", thisfunc);
            status = AM_FAILURE;
        }
    }

    // Process the request
    if (status == AM_SUCCESS) {
        req_params.client_hostname = clientHostname;
        req_params.url = url;
        req_params.query = r->args;
        req_params.method = method;
        req_params.path_info = r->path_info;
        req_params.cookie_header_val =
                (char *) apr_table_get(r->headers_in, "Cookie");
        req_func.get_post_data.func = content_read;
        req_func.get_post_data.args = args;
        // no free_post_data
        req_func.set_user.func = set_user;
        req_func.set_user.args = args;
        req_func.set_method.func = set_method;
        req_func.set_method.args = args;
        req_func.set_header_in_request.func = set_header_in_request;
        req_func.set_header_in_request.args = args;
        req_func.add_header_in_response.func = add_header_in_response;
        req_func.add_header_in_response.args = args;
        req_func.render_result.func = render_result;
        req_func.render_result.args = args;
        // post data preservation (create shared cache table entry)
        req_func.reg_postdata.func = update_post_data_for_request;
        req_func.reg_postdata.args = args;

        (void) am_web_process_request(&req_params, &req_func,
                &status, agent_config);

        if (status == AM_SUCCESS) {
            if (post_page != NULL && strlen(post_page) > 0) {
                /* If post_page is not null it means that the request 
                 * contains the "/dummypost/sunpostpreserve" string and
                 * that the post data of the original request need to be
                 * posted.
                 * If using a LB cookie, it needs to be set to NULL.
                 * If am_web_get_postdata_preserve_lbcookie() returns
                 * AM_INVALID_ARGUMENT, it means that the sticky session
                 * feature is disabled (ie no LB) or that the sticky
                 * session mode is URL
                 **/
                char *lbCookieHeader = NULL;
                status_tmp = am_web_get_postdata_preserve_lbcookie(&lbCookieHeader, B_TRUE, agent_config);
                if (status_tmp != AM_NO_MEMORY) {
                    if (status_tmp == AM_SUCCESS) {
                        am_web_log_debug("%s: Setting LB cookie for post data preservation to null.", thisfunc);
                        set_cookie(lbCookieHeader, args);
                    }
                    int len = strlen(post_page);
                    ap_set_content_type(r, "text/html");
                    ap_set_content_length(r, len);
                    ap_rwrite(post_page, len, r);
                    ap_rflush(r);
                    ret = DONE;
                } else {
                    am_web_log_debug("%s: get LB cookie error %d", thisfunc, status_tmp);
                }
                if (lbCookieHeader != NULL) {
                    am_web_free_memory(lbCookieHeader);
                    lbCookieHeader = NULL;
                }
            }
        } else {
            am_web_log_error("%s: Error encountered rendering result %d.",
                    thisfunc, ret);
        }
    }
    // Cleaning
    if (clientIP != NULL) {
        am_web_free_memory(clientIP);
    }
    if (clientHostname != NULL) {
        am_web_free_memory(clientHostname);
    }
    if (response != NULL) {
        free(response);
        response = NULL;
    }
    if (post_page != NULL) {
        free(post_page);
        post_page = NULL;
    }
    am_web_delete_agent_configuration(agent_config);
    // Failure handling
    if (status == AM_FAILURE) {
        if (ret == OK) {
            ret = HTTP_INTERNAL_SERVER_ERROR;
        }
    }
    return ret;
}

static void sigterm_handler(int sig) {
    // remember that a SIGTERM was delivered so we can raise it later.
    sigterm_delivered = 1;
}

static apr_status_t pre_cleanup_dsame(void *data) {
    apr_status_t ret = APR_SUCCESS;
    am_watchdog_interval = 0;
    if (notification_listener_t) {
        apr_thread_join(&ret, notification_listener_t);
    }
    return APR_SUCCESS;
}

static apr_status_t cleanup_dsame(void *data) {
    /*
     * Apache calls the cleanup func then sends a SIGTERM before
     * the routine finishes. so hold SIGTERM to let destroy agent session
     * complete and release it after.
     * The signal() interface (ANSI C) is chosen to hold the signal instead of
     * sigaction() or other signal handling interfaces since it seems
     * to work best across platforms.
     */
    sighandler_t prev_handler = signal(SIGTERM, sigterm_handler);

    am_web_log_info("Cleaning up web agent..");
    if (init_mutex) {
        apr_thread_mutex_destroy(init_mutex);
        am_web_log_info("Destroyed mutex...");
        init_mutex = NULL;
    }

    if (notification_rmm) {
        apr_rmm_destroy(notification_rmm);
        am_web_log_info("Destroyed shared memory manager...");
        notification_rmm = NULL;
    }

    if (notification_shm) {
        apr_shm_destroy(notification_shm);
        am_web_log_info("Destroyed shared memory...");
        notification_shm = NULL;
    }

    if (notification_lock) {
        apr_global_mutex_destroy(notification_lock);
        am_web_log_info("Destroyed shared memory global mutex...");
        notification_lock = NULL;
    }

    if (postdata_rmm) {
        apr_rmm_destroy(postdata_rmm);
        am_web_log_info("Destroyed shared memory manager...");
        postdata_rmm = NULL;
    }

    if (postdata_shm) {
        apr_shm_destroy(postdata_shm);
        am_web_log_info("Destroyed shared memory...");
        postdata_shm = NULL;
    }

    if (postdata_lock) {
        apr_global_mutex_destroy(postdata_lock);
        am_web_log_info("Destroyed shared memory global mutex...");
        postdata_lock = NULL;
    }

    (void) am_web_cleanup();

    // release SIGTERM
    (void) signal(SIGTERM, prev_handler);
    if (sigterm_delivered) {

        raise(SIGTERM);
    }
    return APR_SUCCESS;
}

static apr_status_t shutdownNSS(void *data) {
    am_status_t status = am_shutdown_nss();
    if (status != AM_SUCCESS) {
        am_web_log_error("shutdownNSS(): Failed to shutdown NSS.");
    }
    return OK;
}

static void dsame_register_hooks(apr_pool_t *p) {
    ap_hook_access_checker(dsame_check_access, NULL, NULL, APR_HOOK_MIDDLE);
    /*main agent init, called once per server lifecycle*/
    ap_hook_post_config(init_dsame, NULL, NULL, APR_HOOK_LAST);
    /*NSS shutdown hook*/
    apr_pool_cleanup_register(p, NULL, shutdownNSS, apr_pool_cleanup_null);
    /*agent child init, called upon new server child process creation*/
    ap_hook_child_init(child_init_dsame, NULL, NULL, APR_HOOK_LAST);
}

/*
 * Interface table used by Apache 2.x to interact with this module.
 */
module AP_MODULE_DECLARE_DATA dsame_module = {
    STANDARD20_MODULE_STUFF,
    NULL, /* per-directory config creator */
    NULL, /* dir config merger */
    dsame_create_server_config, /* server config creator */
    NULL, /* server config merger */
    dsame_auth_cmds, /* command table */
    dsame_register_hooks /* set up other request processing hooks */
};
