/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */

#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <stdio.h>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <stdarg.h>
#include <unistd.h>

#include <vrt.h>
#include <bin/varnishd/cache.h>
#include <vct.h>
#include <vcc_if.h>

#include <am_web.h>

#define	MAGIC_STR		"sunpostpreserve"
#define	POST_PRESERVE_URI	"/dummypost/"MAGIC_STR
#define LOG_E(...)              fprintf(stderr, __VA_ARGS__);

static pthread_mutex_t init_mutex = PTHREAD_MUTEX_INITIALIZER;

struct var {
    char *name;
    char *value;
    VTAILQ_ENTRY(var) list;
};

struct var_head {
    unsigned int magic;
#define VMOD_AM_MAGIC 0x53F43E2F
    VTAILQ_HEAD(, var) vars;
};

typedef enum {
    OK = 0,
    DONE = 1,
} ret_status;

typedef struct {
    struct sess *s;
    int status;
    struct var_head *headers_out;
    const char *body;
    const char *notes;
} request_data_t;

static const char * am_vmod_printf(struct sess *sp, const char * format, ...) {
    char *p;
    unsigned int u, v;
    va_list args;
    u = WS_Reserve(sp->wrk->ws, 0);
    p = sp->wrk->ws->f;
    va_start(args, format);
    v = vsnprintf(p, u, format, args);
    va_end(args);
    v++;
    if (v > u) {
        WS_Release(sp->wrk->ws, 0);
        return NULL;
    }
    WS_Release(sp->wrk->ws, v);
    return p;
}

static void am_vmod_set_header(struct sess *sp, enum gethdr_e where, int unset, const char *hdr,
        const char *p, ...) {
    struct http *hp;
    va_list ap;
    char *b;
    switch (where) {
        case HDR_REQ:
            hp = sp->http;
            break;
        case HDR_RESP:
            hp = sp->wrk->resp;
            break;
        case HDR_OBJ:
            hp = sp->obj->http;
            break;
        case HDR_BEREQ:
            hp = sp->wrk->bereq;
            break;
        case HDR_BERESP:
            hp = sp->wrk->beresp;
            break;
        default:
            return;
    }
    if (p == NULL) {
        http_Unset(hp, hdr);
    } else {
        va_start(ap, p);
        b = VRT_String(hp->ws, hdr + 1, p, ap);
        if (b == NULL) {
            LOG_E("am_vmod_set_header failed. error allocating memory for %s header\n", hdr + 1);
        } else {
            if (unset) http_Unset(hp, hdr);
            http_SetHeader(sp->wrk, sp->fd, hp, b);
        }
        va_end(ap);
    }
}

static struct var * am_get_var(struct var_head *vh, const char *name) {
    struct var *v;
    if (!name)
        return NULL;

    VTAILQ_FOREACH(v, &vh->vars, list) {
        if (v->name && strcmp(v->name, name) == 0)
            return v;
    }
    return NULL;
}

static struct var * am_get_var_alloc(struct var_head *vh, const char *name, struct sess *sp) {
    struct var *v;
    v = am_get_var(vh, name);
    if (!v) {
        v = (struct var*) WS_Alloc(sp->ws, sizeof (struct var));
        AN(v);
        v->name = WS_Dup(sp->ws, name);
        AN(v->name);
        VTAILQ_INSERT_HEAD(&vh->vars, v, list);
    }
    return v;
}

static void am_add_header(request_data_t *sd, const char *name, const char *value) {
    struct var *v;
    if (sd != NULL) {
        v = am_get_var_alloc(sd->headers_out, name, sd->s);
        if (value != NULL) {
            v->value = WS_Dup(sd->s->ws, value);
        } else {
            v->value = NULL;
        }
    } else {
        LOG_E("am_add_header failed. vmod private data is NULL\n");
    }
}

static void am_vmod_free(void *d) {
    request_data_t *sd = (request_data_t *) d;
    if (sd != NULL) {
        struct var *v, *v2;
        struct var_head *vh = sd->headers_out;

        VTAILQ_INIT(&vh->vars);

        VTAILQ_FOREACH_SAFE(v, &vh->vars, list, v2) {
            VTAILQ_REMOVE(&vh->vars, v, list);
        }
        vh->magic = 0;

        free(sd->headers_out);
        sd->headers_out = NULL;
        sd->body = NULL;
        sd->notes = NULL;
        free(sd);
    }
    sd = NULL;
}

int init_am(struct vmod_priv *priv, const struct VCL_conf *conf) {
    return 0;
}

void vmod_init(struct sess *sp, struct vmod_priv *priv, const char *agent_bootstrap_file, const char *agent_config_file) {
    request_data_t *sd;
    am_status_t status = AM_FAILURE;
    boolean_t init = B_FALSE;
    AZ(pthread_mutex_lock(&init_mutex));
    if (priv->priv == NULL) {
        if (agent_bootstrap_file == NULL || agent_config_file == NULL ||
                access(agent_bootstrap_file, R_OK) != 0 || access(agent_config_file, R_OK) != 0 ||
                am_web_init(agent_bootstrap_file, agent_config_file) != AM_SUCCESS) {
            LOG_E("am_web_init failed. can't access bootstrap|configuration file.\n");
        } else {
            if ((status = am_agent_init(&init)) != AM_SUCCESS) {
                const char *sts = am_status_to_string(status);
                LOG_E("am_agent_init failed: %s (%d)\n", sts != NULL ? sts : "N/A", status);
            } else {
                sd = (request_data_t *) malloc(sizeof (request_data_t));
                AN(sd);
                sd->headers_out = (struct var_head *) malloc(sizeof (struct var_head));
                AN(sd->headers_out);
                sd->headers_out->magic = VMOD_AM_MAGIC;
                VTAILQ_INIT(&sd->headers_out->vars);
                sd->s = sp;
                priv->priv = (void *) sd;
                priv->free = am_vmod_free;
            }
        }
    }
    AZ(pthread_mutex_unlock(&init_mutex));
}

void vmod_request_cleanup(struct sess *sp, struct vmod_priv *priv) {
    struct var *v, *v2;
    request_data_t *sd = (request_data_t *) priv->priv;
    if (sd != NULL) {
        struct var_head *vh = sd->headers_out;

        VTAILQ_FOREACH_SAFE(v, &vh->vars, list, v2) {
            VTAILQ_REMOVE(&vh->vars, v, list);
        }
        sd->body = NULL;
        sd->notes = NULL;
    } else {
        LOG_E("am_vmod_request_cleanup failed. vmod private data is NULL\n");
    }
}

void vmod_cleanup(struct sess *sp, struct vmod_priv *priv) {
    am_web_cleanup();
}

void vmod_done(struct sess *sp, struct vmod_priv *priv) {
    int status;
    const char* ct = "\015Content-Type:";
    request_data_t *sd = (request_data_t *) priv->priv;
    if (sd != NULL) {
        struct var *v;
        struct var_head *vh = sd->headers_out;

        VTAILQ_FOREACH(v, &vh->vars, list) {
            if (v->value != NULL && v->value[0] != '\0')
                am_vmod_set_header(sp, HDR_OBJ, 0,
                    am_vmod_printf(sp, "%c%s:", (int) strlen(v->name) + 1, v->name),
                    v->value, vrt_magic_string_end);
        }

        if ((status = sd->status) != 0) {
            if (status < 100 || status > 999) {
                status = 503;
            }
            http_PutStatus(sp->obj->http, status);
            http_PutResponse(sp->wrk, sp->fd,
                    sp->obj->http, http_StatusMessage(status));
        }
        if (sd->body != NULL && sd->body[0] != '\0') {
            VRT_synth_page(sp, 0, sd->body, vrt_magic_string_end);
        }
    } else {
        LOG_E("am_vmod_done failed. vmod private data is NULL\n");
        http_PutStatus(sp->obj->http, 403);
        http_PutResponse(sp->wrk, sp->fd, sp->obj->http, http_StatusMessage(403));
        VRT_synth_page(sp, 0, "403 Forbidden", vrt_magic_string_end);
        VRT_SetHdr(sp, HDR_OBJ, ct, "text/plain", vrt_magic_string_end);
    }
    vmod_request_cleanup(sp, priv);
}

void vmod_ok(struct sess *sp, struct vmod_priv *priv) {
    request_data_t *sd = (request_data_t *) priv->priv;
    if (sd != NULL) {
        struct var *v;
        struct var_head *vh = sd->headers_out;

        VTAILQ_FOREACH(v, &vh->vars, list) {
            if (v->value != NULL && v->value[0] != '\0')
                am_vmod_set_header(sp, HDR_RESP, 0,
                    am_vmod_printf(sp, "%c%s:", (int) strlen(v->name) + 1, v->name),
                    v->value, vrt_magic_string_end);
        }
    } else {
        LOG_E("am_vmod_ok failed. vmod private data is NULL\n");
    }
    vmod_request_cleanup(sp, priv);
}

static am_status_t content_read(void **args, char **body) {
    request_data_t *r;
    const char thisfunc[] = "content_read()";
    am_status_t status = AM_FAILURE;
    int re, buf_size, rsize;
    char *cl_ptr, buf[2048];
    unsigned long cl, ocl;
    if (args == NULL || (r = args[0]) == NULL) {
        am_web_log_error("%s: invalid arguments", thisfunc);
        return AM_INVALID_ARGUMENT;
    } else {
        CHECK_OBJ_NOTNULL(r->s, SESS_MAGIC);
        *body = NULL;
        cl_ptr = VRT_GetHdr(r->s, HDR_REQ, "\017Content-Length:");
        ocl = cl = cl_ptr ? strtoul(cl_ptr, NULL, 10) : 0;
        if (cl <= 0 || errno == ERANGE) {
            am_web_log_warning("%s: post data is empty", thisfunc);
            return AM_NOT_FOUND;
        } else {
            status = AM_SUCCESS;
            if (r->s->htc->pipeline.b != NULL && Tlen(r->s->htc->pipeline) == cl) {
                *body = r->s->htc->pipeline.b;
            } else {
                int rxbuf_size = Tlen(r->s->htc->rxbuf);

                /*do ws memory allocation*/
                int u = WS_Reserve(r->s->wrk->ws, 0);
                if (u < cl + rxbuf_size + 1) {
                    *body = NULL;
                    WS_Release(r->s->wrk->ws, 0);
                    am_web_log_error("%s: memory allocation failure", thisfunc);
                    return AM_FAILURE;
                }
                *body = (char*) r->s->wrk->ws->f;
                memcpy(*body, r->s->htc->rxbuf.b, rxbuf_size);
                r->s->htc->rxbuf.b = *body;
                *body += rxbuf_size;
                *body[0] = 0;
                r->s->htc->rxbuf.e = *body;
                WS_Release(r->s->wrk->ws, cl + rxbuf_size + 1);

                /*read post data*/
                re = 0;
                while (cl) {
                    if (cl > sizeof (buf)) {
                        buf_size = sizeof (buf) - 1;
                    } else {
                        buf_size = cl;
                    }
#ifdef VARNISH302
                    rsize = HTC_Read(r->s->htc, buf, buf_size);
#else
                    rsize = HTC_Read(r->s->wrk, r->s->htc, buf, buf_size);
#endif
                    if (rsize <= 0) {
                        *body = NULL;
                        am_web_log_error("%s: memory failure", thisfunc);
                        return AM_FAILURE;
                    }
                    cl -= rsize;
                    memcpy(*body + re, buf, buf_size);
                    re += rsize;
                }
                r->s->htc->pipeline.b = *body;
                r->s->htc->pipeline.e = *body + ocl;
            }
        }
    }

    if (status == AM_SUCCESS) {
        (*body)[ocl] = 0;
        am_web_log_max_debug("%s:\n%s\n", thisfunc, *body);
    }

    am_web_log_debug("%s: %d bytes", thisfunc, ocl);
    return status;
}

static const char* get_req_header(request_data_t* r, const char* key) {
    if (r == NULL || r->s == NULL) {
        am_web_log_error("get_req_header(): invalid arguments");
        return NULL;
    }
    return VRT_GetHdr(r->s, HDR_REQ, am_vmod_printf(r->s, "%c%s:", (int) strlen(key) + 1, key));
}

static am_status_t set_header_in_request(void **args, const char *key, const char *value) {
    am_status_t sts = AM_SUCCESS;
    request_data_t * r = (request_data_t *) args[0];
    if (r == NULL || key == NULL) {
        am_web_log_error("set_header_in_request(): invalid arguments");
        sts = AM_INVALID_ARGUMENT;
    } else {
        am_vmod_set_header(r->s, HDR_REQ, 1,
                am_vmod_printf(r->s, "%c%s:", (int) strlen(key) + 1, key),
                value != NULL && *value != '\0' ? value : "",
                vrt_magic_string_end);
    }
    return sts;
}

static am_status_t set_cookie(const char *header, void **args) {
    am_status_t ret = AM_INVALID_ARGUMENT;
    char *currentCookies;
    if (header != NULL && args != NULL) {
        request_data_t *r = (request_data_t *) args[0];
        if (r == NULL) {
            am_web_log_error("set_cookie(): invalid arguments");
        } else {
            am_add_header(r, "Set-Cookie", header);
            if ((currentCookies = (char *) get_req_header(r, "Cookie")) == NULL) {
                set_header_in_request(args, "Cookie", header);
            } else {
                set_header_in_request(args, "Cookie",
                        am_vmod_printf(r->s, "%s;%s", header, currentCookies));
            }
            ret = AM_SUCCESS;
        }
    }
    return ret;
}

static am_status_t add_header_in_response(void **args, const char *key, const char *values) {
    request_data_t* r = NULL;
    am_status_t sts = AM_SUCCESS;
    if (args == NULL || (r = (request_data_t *) args[0]) == NULL || key == NULL) {
        am_web_log_error("add_header_in_response(): invalid arguments");
        sts = AM_INVALID_ARGUMENT;
    } else {
        if (values == NULL) {
            sts = set_cookie(key, args);
        } else {
            am_add_header(r, key, values);
            sts = AM_SUCCESS;
        }
    }
    return sts;
}

static am_status_t set_user(void **args, const char *user) {
    /*not implemented/supported on Varnish*/
    return AM_SUCCESS;
}

static void am_custom_response(request_data_t *rec, int status, char *data) {
    rec->status = status;
    rec->body = data != NULL ? am_vmod_printf(rec->s, "%s", data) : NULL;
}

static am_status_t set_method(void **args, am_web_req_method_t method) {
    request_data_t *rec = NULL;
    struct sess* sp = NULL;
    am_status_t sts = AM_SUCCESS;
    if (args == NULL || (rec = (request_data_t *) args[0]) == NULL ||
            (sp = rec->s) == NULL || sp->http == NULL) {
        am_web_log_error("set_method(): invalid arguments");
        sts = AM_INVALID_ARGUMENT;
    } else {
        http_SetH(sp->http, HTTP_HDR_REQ, am_vmod_printf(rec->s, "%s", am_web_method_num_to_str(method)));
        sts = AM_SUCCESS;
    }
    return sts;
}

static am_status_t render_result(void **args, am_web_result_t http_result, char *data) {
    const char *thisfunc = "render_result()";
    request_data_t* rec = NULL;
    am_status_t sts = AM_SUCCESS;
    int *ret = NULL;
    int len = 0;

    if (args == NULL || (rec = (request_data_t *) args[0]) == NULL || (ret = (int *) args[1]) == NULL ||
            ((http_result == AM_WEB_RESULT_OK_DONE || http_result == AM_WEB_RESULT_REDIRECT) &&
            (data == NULL || *data == '\0'))) {
        am_web_log_error("%s: invalid arguments", thisfunc);
        sts = AM_INVALID_ARGUMENT;
    } else {
        switch (http_result) {
            case AM_WEB_RESULT_OK:
                *ret = OK;
                break;
            case AM_WEB_RESULT_OK_DONE:
                if (data && ((len = strlen(data)) > 0)) {
                    rec->status = 200;
                    am_add_header(rec, "Content-Type", "text/html");
                    am_add_header(rec, "Content-Length", am_vmod_printf(rec->s, "%d", len));
                    am_custom_response(rec, 200, data);
                    *ret = DONE;
                } else {
                    *ret = OK;
                }
                break;
            case AM_WEB_RESULT_REDIRECT:
                am_add_header(rec, "Location", data);
                am_add_header(rec, "Content-Type", "text/html");
                am_custom_response(rec, 302, (char *) am_vmod_printf(rec->s, "<head><title>Document Moved</title></head>\n"
                        "<body><h1>Object Moved</h1>This document may be found "
                        "<a href=\"%s\">here</a></body>", data));
                *ret = DONE;
                break;
            case AM_WEB_RESULT_FORBIDDEN:
                rec->status = 403;
                am_add_header(rec, "Content-Type", "text/plain");
                am_custom_response(rec, 403, "403 Forbidden");
                *ret = DONE;
                break;
            case AM_WEB_RESULT_ERROR:
                rec->status = 500;
                am_add_header(rec, "Content-Type", "text/plain");
                am_custom_response(rec, 500, "500 Internal Server Error");
                *ret = DONE;
                break;
            default:
                am_web_log_error("%s: Unrecognized process result %d", thisfunc, http_result);
                rec->status = 500;
                am_add_header(rec, "Content-Type", "text/plain");
                am_custom_response(rec, 500, "500 Internal Server Error");
                *ret = DONE;
                break;
        }
        sts = AM_SUCCESS;
    }
    return sts;
}

static am_web_req_method_t get_method_num(request_data_t *sp) {
    const char *thisfunc = "get_method_num()";
    am_web_req_method_t method_num = AM_WEB_REQUEST_UNKNOWN;
    if (sp == NULL || sp->s == NULL || sp->s->http == NULL) {
        am_web_log_error("%s: invalid arguments", thisfunc);
    } else {
        method_num = am_web_method_str_to_num(http_GetReq(sp->s->http));
        am_web_log_debug("%s: Method string is %s", thisfunc, http_GetReq(sp->s->http));
        am_web_log_debug("%s: Varnish method number corresponds to %s method",
                thisfunc, am_web_method_num_to_str(method_num));
    }
    return method_num;
}

static void send_deny(request_data_t * rec) {
    if (NULL == rec) return;
    am_add_header(rec, "Content-Type", "text/plain");
    am_custom_response(rec, 403, "403 Forbidden");
}

static void send_ok(request_data_t * rec) {
    if (NULL == rec) return;
    am_add_header(rec, "Content-Type", "text/plain");
    am_custom_response(rec, 200, "OK");
}

static void send_error(request_data_t * rec) {
    if (NULL == rec) return;
    am_add_header(rec, "Content-Type", "text/plain");
    am_custom_response(rec, 500, "500 Internal Server Error");
}

static am_status_t update_post_data_for_request(void **args, const char *key, const char *acturl, const char *value, const unsigned long postcacheentry_life) {
    const char *thisfunc = "update_post_data_for_request()";
    am_web_postcache_data_t post_data;
    void *agent_config = NULL;
    am_status_t status = AM_SUCCESS;
    agent_config = am_web_get_agent_configuration();
    if (agent_config == NULL || key == NULL || acturl == NULL) {
        am_web_log_error("%s: invalid arguments", thisfunc);
        return AM_INVALID_ARGUMENT;
    } else {
        post_data.value = (char *) value;
        post_data.url = (char *) acturl;
        am_web_log_debug("%s: Register POST data key :%s", thisfunc, key);
        if (am_web_postcache_insert(key, &post_data, agent_config) == B_FALSE) {
            am_web_log_error("Register POST data insert into hash table failed: %s", key);
            status = AM_FAILURE;
        }
    }
    am_web_delete_agent_configuration(agent_config);
    return status;
}

static am_status_t check_for_post_data(void **args, const char *requestURL, char **page, const unsigned long postcacheentry_life) {
    const char *thisfunc = "check_for_post_data()";
    const char *post_data_query = NULL;
    am_web_postcache_data_t get_data = {NULL, NULL};
    const char *actionurl = NULL;
    const char *postdata_cache = NULL;
    am_status_t status = AM_SUCCESS;
    am_status_t status_tmp = AM_SUCCESS;
    void *agent_config = NULL;
    char *buffer_page = NULL;
    char *stickySessionValue = NULL;
    char *stickySessionPos = NULL;
    char *temp_uri = NULL;
    *page = NULL;

    agent_config = am_web_get_agent_configuration();

    if (agent_config == NULL) {
        am_web_log_error("%s: unable to get agent configuration", thisfunc);
        return AM_FAILURE;
    }

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
            status_tmp = am_web_get_postdata_preserve_URL_parameter(&stickySessionValue, agent_config);
            if (status_tmp == AM_SUCCESS) {
                stickySessionPos = strstr((char *) post_data_query, stickySessionValue);
                if (stickySessionPos != NULL) {
                    size_t len = strlen(post_data_query) - strlen(stickySessionPos) - 1;
                    temp_uri = (char *) malloc(len + 1);
                    memset(temp_uri, 0, len + 1);
                    strncpy(temp_uri, post_data_query, len);
                    post_data_query = temp_uri;
                }
            }
        }
    }
    // If magic uri present search for corresponding value in hashtable
    if ((status == AM_SUCCESS) && (post_data_query != NULL) && (strlen(post_data_query) > 0)) {
        am_web_log_debug("%s: POST Magic Query Value: %s", thisfunc, post_data_query);
        if (am_web_postcache_lookup(post_data_query, &get_data,
                agent_config) == B_TRUE) {
            postdata_cache = get_data.value;
            actionurl = get_data.url;
            am_web_log_debug("%s: POST hashtable actionurl: %s", thisfunc, actionurl);
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
            am_web_log_error("%s: Found magic URI (%s) but entry is not in POST hash table", thisfunc, post_data_query);
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
    am_web_delete_agent_configuration(agent_config);
    return status;
}

static am_status_t set_notes_in_request(void **args, const char *key, const char *values) {
    request_data_t *r = NULL;
    am_status_t sts = AM_SUCCESS;
    if (args == NULL || (r = (request_data_t *) args[0]) == NULL || key == NULL) {
        am_web_log_error("set_notes_in_request(): invalid arguments");
        sts = AM_INVALID_ARGUMENT;
    } else {
        r->notes = NULL;
        if (values != NULL && *values != '\0') {
            r->notes = am_vmod_printf(r->s, "%s", values);
        }
        sts = AM_SUCCESS;
    }
    return sts;
}

static const char *get_query_string(request_data_t *r, const char *url) {
    char *ptr = NULL;
    if (r != NULL && url != NULL) {
        ptr = strstr(url, "?");
        if (ptr != NULL) {
            return am_vmod_printf(r->s, "%s", ptr + 1);
        }
    }
    return "";
}

unsigned vmod_authenticate(struct sess *sp, struct vmod_priv *priv, const char *req_method, const char *proto, const char *host, int port, const char *uri, struct sockaddr_storage * cip) {
    const char thisfunc[] = "vmod_authenticate()";
    void *agent_config = NULL;
    am_status_t status = AM_FAILURE;
    const char *url = NULL;
    request_data_t* r = NULL;
    int ret = OK;
    void *args[] = {NULL, (void*) &ret};
    char client_ip[INET6_ADDRSTRLEN];
    am_web_req_method_t method;
    am_web_request_params_t req_params;
    am_web_request_func_t req_func;
    const char *clientIP_hdr_name = NULL;
    char *clientIP_hdr = NULL;
    char *clientIP = NULL;
    const char *clientHostname_hdr_name = NULL;
    char *clientHostname_hdr = NULL;
    char *clientHostname = NULL;

    memset((void *) & req_params, 0, sizeof (req_params));
    memset((void *) & req_func, 0, sizeof (req_func));

    CHECK_OBJ_NOTNULL(sp, SESS_MAGIC);

    r = (request_data_t *) priv->priv;
    if (r == NULL) {
        send_deny(r);
        return 0;
    }

    r->s = sp;
    args[0] = r;
    agent_config = am_web_get_agent_configuration();

    if (agent_config == NULL) {
        send_deny(r);
        return 0;
    }

    am_web_log_debug("Begin process %s request, proto: %s, host: %s, port: %d, uri: %s", req_method, proto, host, port, uri);

    if (proto == NULL) {
        url = am_vmod_printf(r->s, "http://%s%s", host, uri);
    } else {
        url = am_vmod_printf(r->s, "%s://%s%s", proto, host, uri);
    }

    if (url == NULL) {
        am_web_log_error("%s: memory allocation error", thisfunc);
        status = AM_FAILURE;
    } else {
        char *tmp = (char *) url;
        am_web_log_debug("%s: request url before normalization: %s", thisfunc, url);
        /*find the end of url string*/
        while (tmp && *tmp) ++tmp;
        for (--tmp; url < tmp; --tmp) {
            if (*tmp == '/') {
                /*erase (all) trailing slashes*/
                *tmp = 0;
            } else break;
        }
        am_web_log_debug("%s: request url: %s", thisfunc, url);
    }

    method = get_method_num(r);
    if (method == AM_WEB_REQUEST_UNKNOWN) {
        am_web_log_error("%s: Request method is unknown.", thisfunc);
        status = AM_FAILURE;
    } else {
        status = AM_SUCCESS;
    }

    if (status == AM_SUCCESS) {
        if (B_TRUE == am_web_is_notification(url, agent_config)) {
            char* data = NULL;
            status = content_read(args, &data);
            if (status == AM_SUCCESS && data != NULL && strlen(data) > 0) {
                am_web_handle_notification(data, strlen(data), agent_config);
                am_web_delete_agent_configuration(agent_config);
                am_web_log_debug("%s: received notification message, sending HTTP-200 response", thisfunc);
                send_ok(r);
                return 0;
            } else {
                am_web_log_error("%s: content_read for notification failed, %s", thisfunc, am_status_to_string(status));
            }
        }
    }

    if (status == AM_SUCCESS) {
        int vs = am_web_validate_url(agent_config, url);
        if (vs != -1) {
            if (vs == 1) {
                am_web_log_debug("%s: Request URL validation succeeded", thisfunc);
                status = AM_SUCCESS;
            } else {
                am_web_log_error("%s: Request URL validation failed. Returning Access Denied error (HTTP403)", thisfunc);
                status = AM_FAILURE;
                am_web_delete_agent_configuration(agent_config);
                send_deny(r);
                return 0;
            }
        }
    }

    if (status == AM_SUCCESS) {
        /* get the client IP address header set by the proxy, if there is one */
        clientIP_hdr_name = am_web_get_client_ip_header_name(agent_config);
        if (clientIP_hdr_name != NULL) {
            clientIP_hdr = (char *) get_req_header(r, clientIP_hdr_name);
        }
        /* get the client host name header set by the proxy, if there is one */
        clientHostname_hdr_name =
                am_web_get_client_hostname_header_name(agent_config);
        if (clientHostname_hdr_name != NULL) {
            clientHostname_hdr = (char *) get_req_header(r, clientHostname_hdr_name);
        }
        /* if the client IP and host name headers contain more than one
         * value, take the first value */
        if ((clientIP_hdr != NULL && strlen(clientIP_hdr) > 0) ||
                (clientHostname_hdr != NULL && strlen(clientHostname_hdr) > 0)) {
            status = am_web_get_client_ip_host(clientIP_hdr, clientHostname_hdr,
                    &clientIP, &clientHostname);
        }
    }

    if (status == AM_SUCCESS) {
        if (clientIP == NULL) {
            if (cip != NULL && cip->ss_family == AF_INET) {
                struct sockaddr_in *sai = (struct sockaddr_in *) cip;
                if (inet_ntop(AF_INET, &sai->sin_addr, client_ip, sizeof (client_ip)) == NULL) {
                    am_web_log_error("%s: Could not get the remote host IPv4 (error: %d)", thisfunc, errno);
                    status = AM_FAILURE;
                } else {
                    am_web_log_debug("%s: client host IPv4: %s", thisfunc, client_ip);
                    req_params.client_ip = client_ip;
                }
            } else if (cip != NULL && cip->ss_family == AF_INET6) {
                struct sockaddr_in6 *sai = (struct sockaddr_in6 *) cip;
                if (inet_ntop(AF_INET6, &sai->sin6_addr, client_ip, sizeof (client_ip)) == NULL) {
                    am_web_log_error("%s: Could not get the remote host IPv6 (error: %d)", thisfunc, errno);
                    status = AM_FAILURE;
                } else {
                    am_web_log_debug("%s: client host IPv6: %s", thisfunc, client_ip);
                    req_params.client_ip = client_ip;
                }
            } else {
                am_web_log_error("%s: Could not get the remote host IP (invalid address family)", thisfunc);
                status = AM_FAILURE;
            }
        } else {
            req_params.client_ip = clientIP;
        }
        if ((req_params.client_ip == NULL) || (strlen(req_params.client_ip) == 0)) {
            am_web_log_error("%s: Could not get the remote host IP", thisfunc);
            status = AM_FAILURE;
        }
    }

    if (status == AM_SUCCESS) {
        req_params.client_hostname = clientHostname;
        req_params.url = (char *) url;
        req_params.query = (char *) get_query_string(r, url);
        req_params.method = method;
        req_params.path_info = ""; // N/A in Varnish
        req_params.cookie_header_val = (char *) get_req_header(r, "Cookie");
        req_func.get_post_data.func = content_read;
        req_func.get_post_data.args = args;
        req_func.free_post_data.func = NULL;
        req_func.free_post_data.args = NULL;
        req_func.set_user.func = set_user;
        req_func.set_user.args = args;
        req_func.set_method.func = set_method;
        req_func.set_method.args = args;
        req_func.set_header_in_request.func = set_header_in_request;
        req_func.set_header_in_request.args = args;
        req_func.add_header_in_response.func = add_header_in_response;
        req_func.add_header_in_response.args = args;
#ifdef CDSSO_REPOST_URL
        /*not yet supported on Varnish*/
        req_func.set_notes_in_request.func = set_notes_in_request;
        req_func.set_notes_in_request.args = args;
#endif
        req_func.render_result.func = render_result;
        req_func.render_result.args = args;
        req_func.reg_postdata.func = update_post_data_for_request;
        req_func.reg_postdata.args = args;
        req_func.check_postdata.func = check_for_post_data;
        req_func.check_postdata.args = args;

        am_web_process_request(&req_params, &req_func, &status, agent_config);

        if (status != AM_SUCCESS) {
            am_web_log_error("%s: error from am_web_process_request: %s", thisfunc, am_status_to_string(status));
        }
    }

    if (clientIP != NULL) {
        am_web_free_memory(clientIP);
    }
    if (clientHostname != NULL) {
        am_web_free_memory(clientHostname);
    }

    am_web_delete_agent_configuration(agent_config);

    if (status != AM_SUCCESS) {
        am_web_log_error("%s: error encountered rendering result: %s", thisfunc, am_status_to_string(status));
        send_error(r);
        return 0;
    }
    return (0 == ret);
}
