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

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <ws2tcpip.h>
#include <windows.h>
#include <process.h>
#include <io.h>
#else
#include <unistd.h>
#endif
#include <ctype.h>
#include <string.h>
#include <httpd.h>
#include <http_core.h>
#include <http_protocol.h>
#include <http_request.h>
#include <http_log.h>
#include <http_main.h>
#include <ap_mpm.h>
#include <apr_strings.h>
#include <apr_buckets.h>

#include "version.h"
#include "am.h"

static const char amagent_post_filter_name[] = "AmModuleFilterIn";
module AP_MODULE_DECLARE_DATA amagent_module;

APLOG_USE_MODULE(amagent);

/* APLOG_INFO APLOG_ERR APLOG_WARNING APLOG_DEBUG */
#define LOG_R(l,r,...) \
	ap_log_rerror(APLOG_MARK,l|APLOG_NOERRNO,0,r, "%s", apr_psprintf((r)->pool, __VA_ARGS__))
#define LOG_S(l,s,...) \
	ap_log_error(APLOG_MARK,l|APLOG_NOERRNO,0,s, "%s", apr_psprintf((s)->process->pool, __VA_ARGS__))

typedef struct {
    char enabled;
    char *config;
    char *debug_file;
    char *audit_file;
    int debug_level;
    int audit_level;
    int error;
    unsigned long config_id;
} amagent_config_t; /*per server config*/

static const char *am_set_opt(cmd_parms *c, void *cfg, const char *arg) {
    amagent_config_t *conf = (amagent_config_t *)
            ap_get_module_config(c->server->module_config, &amagent_module);
    const char *name = c->cmd->name;
    if (!conf || !name) {
        return NULL;
    }
    if (strcmp(name, "AmAgentConf") == 0) {
        am_config_t *ac = NULL;
        conf->config = apr_psprintf(c->pool, "%s", arg);
        conf->config_id = am_instance_id(conf->config);
        /* read and parse agent bootstrap configuration */
        ac = am_get_config_file(conf->config_id, conf->config);
        if (ac != NULL) {
            conf->debug_file = ac->debug_file != NULL ? apr_pstrdup(c->pool, ac->debug_file) : NULL;
            conf->audit_file = ac->audit_file != NULL ? apr_pstrdup(c->pool, ac->audit_file) : NULL;
            conf->debug_level = ac->debug_level;
            conf->audit_level = ac->audit_level;
            conf->error = AM_SUCCESS;
            am_config_free(&ac);
        } else {
            conf->error = AM_FILE_ERROR;
        }
    } else if (strcmp(name, "AmAgent") == 0) {
        conf->enabled = !strcasecmp(arg, "on");
    }
    return NULL;
}

/*Context: either top level or inside VirtualHost*/
static const command_rec amagent_cmds[] = {
    AP_INIT_TAKE1("AmAgent", am_set_opt, NULL, RSRC_CONF, "Module enabled/disabled"),
    AP_INIT_TAKE1("AmAgentConf", am_set_opt, NULL, RSRC_CONF, "Module configuration file"), {
        NULL
    }
};

static apr_status_t amagent_cleanup(void *arg) {
    /* main process cleanup */
    server_rec *s = (server_rec *) arg;
    LOG_S(APLOG_DEBUG, s, "amagent_cleanup() %d", getpid());
#ifndef _WIN32
    am_shutdown();
#endif
    return APR_SUCCESS;
}

static int amagent_init(apr_pool_t *pconf, apr_pool_t *plog, apr_pool_t *ptemp,
        server_rec *s) {
    /* main process init */
    int status;
    apr_status_t rv = APR_SUCCESS;
    void *data;

#define AMAGENT_INIT_ONCE "AMAGENT_INIT_ONCE"
    apr_pool_userdata_get(&data, AMAGENT_INIT_ONCE, s->process->pool);
    if (!data) {
        /* module has already been initialized */
        apr_pool_userdata_set((const void *) 1, AMAGENT_INIT_ONCE,
                apr_pool_cleanup_null, s->process->pool);
        return rv;
    }
    apr_pool_cleanup_register(pconf, s, amagent_cleanup, apr_pool_cleanup_null);
    ap_add_version_component(pconf, MODINFO);
    LOG_S(APLOG_DEBUG, s, "amagent_init() %d", getpid());

#ifndef _WIN32
    status = am_init();
    if (status != AM_SUCCESS) {
        rv = APR_EINIT;
        LOG_S(APLOG_ERR, s, "amagent_init() status: %s", am_strerror(status));
    }
#endif
    return rv;
}

static apr_status_t amagent_worker_cleanup(void *arg) {
    /* worker process cleanup */
    server_rec *s = (server_rec *) arg;
    LOG_S(APLOG_DEBUG, s, "amagent_worker_cleanup() %d", getpid());
    am_shutdown_worker();
#ifdef _WIN32
    am_shutdown();
#endif
    return APR_SUCCESS;
}

static void amagent_worker_init(apr_pool_t *p, server_rec *s) {
    /* worker process init */
    LOG_S(APLOG_DEBUG, s, "amagent_worker_init() %d", getpid());
    am_init_worker();
    apr_pool_cleanup_register(p, s, amagent_worker_cleanup, apr_pool_cleanup_null);
}

static void *amagent_srv_config(apr_pool_t *p, server_rec *srv) {
    amagent_config_t *c = apr_pcalloc(p, sizeof (amagent_config_t));
    if (c != NULL) {
        c->config = NULL;
        c->config_id = 0;
        c->enabled = 0;
        c->debug_file = NULL;
        c->audit_file = NULL;
        c->debug_level = 0;
        c->audit_level = 0;
        c->error = 0;
    }
    return (void *) c;
}

static int am_status_value(am_status_t v) {
    switch (v) {
        case AM_SUCCESS:
            return OK;
        case AM_PDP_DONE:
        case AM_DONE:
            return DONE;
        case AM_NOT_HANDLING:
            return DECLINED;
        case AM_NOT_FOUND:
            return HTTP_NOT_FOUND;
        case AM_REDIRECT:
            return HTTP_MOVED_TEMPORARILY;
        case AM_FORBIDDEN:
            return HTTP_FORBIDDEN;
        case AM_BAD_REQUEST:
            return HTTP_BAD_REQUEST;
        case AM_ERROR:
            return HTTP_INTERNAL_SERVER_ERROR;
        case AM_NOT_IMPLEMENTED:
            return HTTP_NOT_IMPLEMENTED;
        default:
            return HTTP_INTERNAL_SERVER_ERROR;
    }
}

static am_status_t get_request_url(am_request_t *rq) {
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL) return AM_EINVAL;
    rq->orig_url = ap_construct_url(r->pool, r->unparsed_uri, r);
    if (rq->orig_url == NULL) return AM_EINVAL;
    return AM_SUCCESS;
}

static am_status_t set_user(am_request_t *rq, const char *user) {
    static const char *thisfunc = "set_user():";
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL) return AM_EINVAL;
    r->user = apr_pstrdup(r->pool, user == NULL ? "" : user);
    r->ap_auth_type = apr_pstrdup(r->pool, "OpenAM");
    AM_LOG_DEBUG(rq->instance_id, "%s %s", thisfunc, LOGEMPTY(user));
    return AM_SUCCESS;
}

static am_status_t set_header_in_request(am_request_t *rq, const char *key, const char *value) {
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL || !ISVALID(key)) return AM_EINVAL;
    /* remove all instances of the header first */
    apr_table_unset(r->headers_in, key);
    if (ISVALID(value)) {
        apr_table_set(r->headers_in, key, value);
    }
    return AM_SUCCESS;
}

static am_status_t set_cookie(am_request_t *rq, const char *header) {
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    const char *c;
    if (r == NULL || !ISVALID(header)) return AM_EINVAL;
    apr_table_add(r->err_headers_out, "Set-Cookie", header);
    c = apr_table_get(r->headers_in, "Cookie");
    if (c == NULL) {
        apr_table_add(r->headers_in, "Cookie", header);
    } else {
        apr_table_set(r->headers_in, "Cookie", apr_pstrcat(r->pool, header, ";", c, NULL));
    }
    return AM_SUCCESS;
}

static am_status_t add_header_in_response(am_request_t *rq, const char *key, const char *value) {
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL || !ISVALID(key)) return AM_EINVAL;
    if (!ISVALID(value)) {
        /*value is empty, sdk is setting a cookie in response*/
        return set_cookie(rq, key);
    }
    /* Apache HTTPD keeps two separate server response header tables in the request 
     * record — one for normal response headers and one for error headers. 
     * The difference between them is the error headers are sent to 
     * the client even (not only) on an error response (REDIRECT is one of them)
     */
    apr_table_add(r->err_headers_out, key, value);
    return AM_SUCCESS;
}

static am_status_t set_custom_response(am_request_t *rq, const char *text, const char *cont_type) {
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    am_status_t status = AM_ERROR;

    if (r == NULL || !ISVALID(text)) {
        return AM_EINVAL;
    }

    status = rq->status;
    switch (status) {
        case AM_JSON_RESPONSE:
        {
            //TODO:
            rq->status = AM_DONE;
        }
            break;
        case AM_INTERNAL_REDIRECT:
        {
            ap_internal_redirect(text, r);
            rq->status = AM_DONE;
        }
            break;
        case AM_REDIRECT:
        {
            apr_table_add(r->headers_out, "Location", text);
            ap_custom_response(r, HTTP_MOVED_TEMPORARILY, text);
        }
            break;
        case AM_PDP_DONE:
        {
            request_rec *sr = ap_sub_req_method_uri(am_method_num_to_str(rq->method),
                    rq->post_data_url, r, NULL);

            sr->headers_in = r->headers_in;
            sr->notes = r->notes;
            sr->clength = rq->post_data_sz;
            sr->content_type = cont_type;

            AM_LOG_DEBUG(rq->instance_id, "set_custom_response(): issuing sub-request %s to %s (%s)",
                    sr->method, rq->post_data_url, LOGEMPTY(cont_type));

            ap_run_sub_req(sr);

            r->status_line = apr_pstrdup(r->pool, sr->status_line);
            r->status = sr->status;
            r->uri = sr->uri;

            ap_rflush(sr);

            ap_destroy_sub_req(sr);
            rq->status = AM_SUCCESS;
        }
            break;
        default:
        {
            size_t tl = strlen(text);
            if (ISVALID(cont_type)) {
                ap_set_content_type(r, cont_type);
            }
            ap_set_content_length(r, tl);
            ap_rwrite(text, (int) tl, r);
            ap_custom_response(r,
                    am_status_value(rq->status == AM_SUCCESS ||
                    rq->status == AM_DONE ? AM_SUCCESS : rq->status), text);
            ap_rflush(r);
        }
            break;
    }
    AM_LOG_INFO(rq->instance_id, "set_custom_response(): status: %s (exit: %s)",
            am_strerror(status), am_strerror(rq->status));
    return AM_SUCCESS;
}

static char get_method_num(request_rec *r, unsigned long instance_id) {
    static const char *thisfunc = "get_method_num():";
    char method_num = AM_REQUEST_UNKNOWN;
    const char *mthd = ap_method_name_of(r->pool, r->method_number);
    AM_LOG_DEBUG(instance_id, "%s method %s (%s, %d)", thisfunc, LOGEMPTY(r->method),
            LOGEMPTY(mthd), r->method_number);
    if (r->method_number == M_GET && r->header_only > 0) {
        method_num = AM_REQUEST_HEAD;
    } else {
        method_num = am_method_str_to_num(mthd);
    }
    AM_LOG_DEBUG(instance_id, "%s number corresponds to %s method",
            thisfunc, am_method_num_to_str(method_num));
    /* check if method number and method string correspond */
    if (method_num == AM_REQUEST_UNKNOWN) {
        /* if method string is not null, set the correct method number */
        if (r->method != NULL && *(r->method) != '\0') {
            method_num = am_method_str_to_num(r->method);
            r->method_number = ap_method_number_of(r->method);
            AM_LOG_DEBUG(instance_id, "%s set method number to correspond to %s method (%d)",
                    thisfunc, r->method, r->method_number);
        }
    } else if (ISVALID(r->method) && strcasecmp(r->method, am_method_num_to_str(method_num))
            && (method_num != AM_REQUEST_INVALID)) {
        /* in case the method number and the method string do not match,
         * correct the method string. But if the method number is invalid
         * the method string needs to be preserved in case Apache is
         * used as a proxy (in front of Exchange Server for instance)
         */
        r->method = am_method_num_to_str(method_num);
        AM_LOG_DEBUG(instance_id, "%s set method to %s", thisfunc, LOGEMPTY(r->method));
    }
    return method_num;
}

static am_status_t set_method(am_request_t *rq) {
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    if (r == NULL) return AM_EINVAL;
    r->method = am_method_num_to_str(rq->method);
    r->method_number = ap_method_number_of(r->method);
    return AM_SUCCESS;
}

static am_status_t set_request_body(am_request_t *rq) {
    static const char *thisfunc = "set_request_body():";
    request_rec *r = (request_rec *) (rq != NULL ? rq->ctx : NULL);
    am_status_t status = AM_EINVAL;

    if (r == NULL) {
        return status;
    }

    apr_table_unset(r->notes, amagent_post_filter_name);

    if (ISVALID(rq->post_data) && rq->post_data_sz > 0) {
        size_t data_sz = rq->post_data_sz;
        char *encoded = base64_encode(rq->post_data, &data_sz);
        if (encoded != NULL) {
            apr_table_set(r->notes, amagent_post_filter_name, encoded);
            AM_LOG_DEBUG(rq->instance_id, "%s preserved %d bytes", thisfunc,
                    rq->post_data_sz);
            /* restore the content length so that we have a
             * match with a re-played data in the agent filter 
             */
            r->clength = rq->post_data_sz;
            apr_table_set(r->headers_in, "Content-Length",
                    apr_psprintf(r->pool, "%ld", rq->post_data_sz));
            free(encoded);
        }
    }

    return AM_SUCCESS;
}

static am_status_t get_request_body(am_request_t *rq) {
    static const char *thisfunc = "get_request_body():";
    request_rec *r;
    apr_bucket_brigade *bb;
    int eos_found = 0, read_bytes = 0;
    apr_status_t read_status = 0;
    am_status_t status = AM_ERROR;
    char *out = NULL, *out_tmp = NULL;

    if (rq == NULL || rq->ctx == NULL) {
        return AM_EINVAL;
    }

    r = (request_rec *) rq->ctx;
    bb = apr_brigade_create(r->pool, r->connection->bucket_alloc);

    do {
        apr_bucket *ob;
        read_status = ap_get_brigade(r->input_filters, bb, AP_MODE_READBYTES,
                APR_BLOCK_READ, HUGE_STRING_LEN);
        if (read_status != APR_SUCCESS) {
            am_free(out);
            return AM_ERROR;
        }

        ob = APR_BRIGADE_FIRST(bb);
        while (ob != APR_BRIGADE_SENTINEL(bb)) {
            const char *data;
            apr_size_t data_size;

            if (APR_BUCKET_IS_EOS(ob)) {
                eos_found = 1;
                break;
            }

            if (APR_BUCKET_IS_FLUSH(ob)) {
                continue;
            }

            /* read data */
            apr_bucket_read(ob, &data, &data_size, APR_BLOCK_READ);
            /* process data */
            out_tmp = realloc(out, read_bytes + data_size + 1);
            if (out_tmp == NULL) {
                am_free(out);
                status = AM_ENOMEM;
                eos_found = 1;
                break;
            } else {
                out = out_tmp;
            }

            memcpy(out + read_bytes, data, data_size);
            read_bytes += (int) data_size;
            out[read_bytes] = 0;

            ob = APR_BUCKET_NEXT(ob);
            status = AM_SUCCESS;
        }
        apr_brigade_destroy(bb);

    } while (eos_found == 0);

    apr_brigade_destroy(bb);

    rq->post_data = out;
    rq->post_data_sz = read_bytes;

    if (status == AM_SUCCESS) {
        AM_LOG_DEBUG(rq->instance_id, "%s read %d bytes \n%s", thisfunc,
                read_bytes, LOGEMPTY(out));
        /* remove the content length since the body has been read */
        r->clength = 0;
        apr_table_unset(r->headers_in, "Content-Length");
    }
    return status;
}

static int amagent_auth_handler(request_rec *r) {
    static const char *thisfunc = "amagent_auth_handler():";
    int rv;
    am_request_t d;
    am_config_t *boot = NULL;

    amagent_config_t *c = ap_get_module_config(r->server->module_config, &amagent_module);

    if (c == NULL || !c->enabled) {
        /* amagent module is not enabled for this 
         * server/virtualhost - we are not handling this request
         **/
        return DECLINED;
    }

    if (c->error != AM_SUCCESS) {
        LOG_R(APLOG_ERR, r, "%s is not configured to handle the request "
                "to %s (unable to load bootstrap configuration from %s, error: %s)",
                DESCRIPTION, r->uri, c->config, am_strerror(c->error));
        return HTTP_FORBIDDEN;
    }

    LOG_R(APLOG_DEBUG, r, "amagent_auth_handler(): [%s] [%ld]", c->config, c->config_id);

    /* register and update instance logger configuration (for already registered
     * instances - update logging level only 
     */
    am_log_register_instance(c->config_id, c->debug_file, c->debug_level,
            c->audit_file, c->audit_level);

    AM_LOG_DEBUG(c->config_id, "%s begin", thisfunc);

    /* fetch agent configuration instance (from cache if available) */
    rv = am_get_agent_config(c->config_id, c->config, &boot);
    if (boot == NULL || rv != AM_SUCCESS) {
        LOG_R(APLOG_ERR, r, "%s is not configured to handle the request "
                "to %s (unable to get agent configuration instance, configuration: %s, error: %s)",
                DESCRIPTION, r->uri, c->config, am_strerror(rv));
        AM_LOG_ERROR(c->config_id, "amagent_auth_handler(): failed to get agent configuration instance, error: %s",
                am_strerror(rv));
        return HTTP_FORBIDDEN;
    }

    /* set up request processor data structure */
    memset(&d, 0, sizeof (am_request_t));
    d.conf = boot;
    d.status = AM_ERROR;
    d.instance_id = c->config_id;
    d.ctx = r;
    d.method = get_method_num(r, c->config_id);
    d.content_type = apr_table_get(r->headers_in, "Content-Type");
    d.cookies = apr_table_get(r->headers_in, "Cookie");

    if (ISVALID(d.conf->client_ip_header)) {
        d.client_ip = (char *) apr_table_get(r->headers_in, d.conf->client_ip_header);
    }
    if (!ISVALID(d.client_ip)) {
#ifdef APACHE24
        d.client_ip = (char *) r->connection->client_ip;
#else
        d.client_ip = (char *) r->connection->remote_ip;
#endif
    }

    if (ISVALID(d.conf->client_hostname_header)) {
        d.client_host = (char *) apr_table_get(r->headers_in, d.conf->client_hostname_header);
    }

    d.am_get_request_url_f = get_request_url;
    d.am_get_post_data_f = get_request_body;
    d.am_set_post_data_f = set_request_body;
    d.am_set_user_f = set_user;
    d.am_set_header_in_request_f = set_header_in_request;
    d.am_add_header_in_response_f = add_header_in_response;
    d.am_set_cookie_f = set_cookie;
    d.am_set_custom_response_f = set_custom_response;
    d.am_set_method_f = set_method;

    am_process_request(&d);

    rv = am_status_value(d.status);

    AM_LOG_DEBUG(c->config_id, "amagent_auth_handler(): exit status: %s (%d)",
            am_strerror(d.status), d.status);

    am_config_free(&d.conf);
    am_request_free(&d);

    LOG_R(APLOG_DEBUG, r, "amagent_auth_handler(): return status: %d", rv);
    return rv;
}

static void amagent_auth_post_insert_filter(request_rec *r) {
    ap_add_input_filter(amagent_post_filter_name, NULL, r, r->connection);
}

static apr_status_t amagent_post_filter(ap_filter_t *f, apr_bucket_brigade *bucket_out,
        ap_input_mode_t emode, apr_read_type_e eblock, apr_off_t nbytes) {
    request_rec *r = f->r;
    conn_rec *c = r->connection;
    apr_bucket *bucket;
    apr_size_t sz;
    char *clean;
    const char *data = apr_table_get(r->notes, amagent_post_filter_name);

    do {
        if (data == NULL) break;

        sz = strlen(data);
        clean = base64_decode(data, &sz);
        if (clean == NULL) break;

        apr_table_unset(r->notes, amagent_post_filter_name);

        LOG_R(APLOG_DEBUG, r, "amagent_post_filter(): reposting %ld bytes", sz);

        bucket = apr_bucket_heap_create((const char *) clean, sz, NULL, c->bucket_alloc);
        if (bucket == NULL) {
            free(clean);
            return APR_EGENERAL;
        }
        APR_BRIGADE_INSERT_TAIL(bucket_out, bucket);
        free(clean);

        bucket = apr_bucket_eos_create(c->bucket_alloc);
        if (bucket == NULL) {
            return APR_EGENERAL;
        }
        APR_BRIGADE_INSERT_TAIL(bucket_out, bucket);
        ap_remove_input_filter(f);
        return APR_SUCCESS;

    } while (0);

    apr_table_unset(r->notes, amagent_post_filter_name);
    ap_remove_input_filter(f);
    return ap_get_brigade(f->next, bucket_out, emode, eblock, nbytes);
}

static void amagent_register_hooks(apr_pool_t *p) {
#if AP_SERVER_MAJORVERSION_NUMBER == 2 && AP_SERVER_MINORVERSION_NUMBER < 3
    ap_hook_access_checker(amagent_auth_handler, NULL, NULL, APR_HOOK_FIRST);
#else
    ap_hook_check_access_ex(amagent_auth_handler, NULL, NULL, APR_HOOK_FIRST, AP_AUTH_INTERNAL_PER_CONF);
#endif
    ap_hook_post_config(amagent_init, NULL, NULL, APR_HOOK_MIDDLE);
    ap_hook_child_init(amagent_worker_init, NULL, NULL, APR_HOOK_MIDDLE);

    ap_hook_insert_filter(amagent_auth_post_insert_filter, NULL, NULL, APR_HOOK_FIRST);
    ap_register_input_filter(amagent_post_filter_name, amagent_post_filter, NULL, AP_FTYPE_RESOURCE);
}

module AP_MODULE_DECLARE_DATA amagent_module = {
    STANDARD20_MODULE_STUFF,
    NULL,
    NULL,
    amagent_srv_config,
    NULL,
    amagent_cmds,
    amagent_register_hooks
};
