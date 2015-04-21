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
#include "version.h"
#include "utility.h"
#include "net_client.h"
#include "list.h"

#define AM_NET_CONNECT_TIMEOUT 8 /*in sec*/

struct request_data {
    char *rd;
    size_t sz;
    int error;
    am_event_t *rf;
};

static void on_agent_request_data_cb(void *udata, const char *data, size_t data_sz, int status) {
    struct request_data *ld = (struct request_data *) udata;
    if (ld->rd == NULL) {
        ld->rd = malloc(data_sz + 1);
        if (ld->rd == NULL) {
            ld->error = AM_ENOMEM;
            return;
        }
        memcpy(ld->rd, data, data_sz);
        ld->rd[data_sz] = 0;
        ld->sz = data_sz;
    } else {
        char *rd_tmp = realloc(ld->rd, ld->sz + data_sz + 1);
        if (rd_tmp == NULL) {
            am_free(ld->rd);
            ld->error = AM_ENOMEM;
            return;
        } else {
            ld->rd = rd_tmp;
        }
        memcpy(ld->rd + ld->sz, data, data_sz);
        ld->sz += data_sz;
        ld->rd[ld->sz] = 0;
    }
}

static void on_connected_cb(void *udata, int status) {
}

static void on_close_cb(void *udata, int status) {
    struct request_data *ld = (struct request_data *) udata;
    set_event(ld->rf);
}

static void on_complete_cb(void *udata, int status) {
    struct request_data *ld = (struct request_data *) udata;
    set_event(ld->rf);
}

int am_agent_login(unsigned long instance_id, const char *openam, const char *notifyurl,
        const char *user, const char *pass, const char *realm, int is_local,
        struct am_ssl_options *info,
        char **agent_token, char **pxml, size_t *pxsz, struct am_namevalue **session_list,
        void(*log)(const char *, ...)) {
    static const char *thisfunc = "am_agent_login():";
    char *post = NULL, *post_data = NULL;
    am_net_t n;
    size_t post_sz;
    int status = AM_ERROR;

    struct request_data ld;

    char *token_enc = NULL;
    char *realm_enc = url_encode(realm);
    char *user_enc = url_encode(user);

    if (!ISVALID(realm_enc) || !ISVALID(user_enc) ||
            !ISVALID(pass) || !ISVALID(openam)) return AM_EINVAL;

    memset(&ld, 0, sizeof (struct request_data));

    memset(&n, 0, sizeof (am_net_t));
    n.log = log;
    n.instance_id = instance_id;
    n.timeout = AM_NET_CONNECT_TIMEOUT;
    n.url = openam;
    if (info != NULL) {
        memcpy(&n.ssl.info, info, sizeof (struct am_ssl_options));
    }

    ld.rf = create_event();
    if (ld.rf == NULL) return AM_ENOMEM;

    n.data = &ld;
    n.on_connected = on_connected_cb;
    n.on_close = on_close_cb;
    n.on_data = on_agent_request_data_cb;
    n.on_complete = on_complete_cb;

    if (am_net_connect(&n) == 0) {
        size_t post_data_sz;
        char *pass_encoded = url_encode(pass);

        post_data_sz = am_asprintf(&post_data,
                "username=%s&password=%s&uri=realm%%3D%s%%26module%%3DApplication",
                user_enc, NOTNULL(pass_encoded), realm_enc);
        am_free(pass_encoded);

        if (post_data != NULL) {
            post_sz = am_asprintf(&post, "POST %s/identity/authenticate HTTP/1.1\r\n"
                    "Host: %s:%d\r\n"
                    "User-Agent: "MODINFO"\r\n"
                    "Content-Language: UTF-8\r\n"
                    "Connection: Keep-Alive\r\n"
                    "Content-Type: application/x-www-form-urlencoded\r\n"
                    "Content-Length: %d\r\n\r\n"
                    "%s", n.uv.path, n.uv.host, n.uv.port, post_data_sz, post_data);
            if (post != NULL) {
                AM_LOG_DEBUG(instance_id, "%s sending %d bytes", thisfunc, post_sz);
                if (log != NULL) {
                    log("%s sending %d bytes", thisfunc, post_sz);
#ifdef DEBUG
                    log("%s\n%s", thisfunc, post);
#endif
                }
                status = am_net_write(&n, post, post_sz);
                free(post);
                post = NULL;
            }
            free(post_data);
            post_data = NULL;
        }

        if (status == AM_SUCCESS)
            wait_for_event(ld.rf, 0);

        AM_LOG_DEBUG(instance_id, "%s authenticate response status code: %d",
                thisfunc, n.http_status);
        if (log != NULL) {
            log("%s authenticate response status code: %d\n%s", thisfunc,
                    n.http_status, LOGEMPTY(ld.rd));
        }

        if (status == AM_SUCCESS && n.http_status == 200 && ISVALID(ld.rd)) {
            char *identity_get = NULL;
            char *sep = strchr(ld.rd, '=');
            status = AM_ERROR;
            trim(ld.rd, '\n');
            token_enc = sep != NULL && *(sep + 1) != '\0' ?
                    url_encode(sep + 1) : NULL;
            if (token_enc != NULL) {

                if (agent_token != NULL) *agent_token = strdup(sep + 1);

                free(ld.rd);
                ld.rd = NULL;

                if (!is_local) {
                    post_sz = am_asprintf(&identity_get, "GET %s/identity/xml/read?"
                            "name=%s&attributes_names=realm&attributes_values_realm=%s&attributes_names=objecttype"
                            "&attributes_values_objecttype=Agent&admin=%s HTTP/1.1\r\n"
                            "Host: %s:%d\r\n"
                            "User-Agent: "MODINFO"\r\n"
                            "Accept: text/xml\r\n"
                            "Connection: Keep-Alive\r\n\r\n",
                            n.uv.path,
                            user_enc, realm_enc, token_enc,
                            n.uv.host, n.uv.port);
                    if (identity_get != NULL) {
                        AM_LOG_DEBUG(instance_id, "%s sending request:\n%s", thisfunc, identity_get);
                        if (log != NULL) {
                            log("%s sending request:\n%s", thisfunc, identity_get);
                        }
                        status = am_net_write(&n, identity_get, post_sz);
                        free(identity_get);
                    }
                } else {
                    status = AM_SUCCESS;
                }
            }
        } else {
            status = AM_ERROR;
        }

        if (status == AM_SUCCESS && !is_local) {
            wait_for_event(ld.rf, 0);
        }

        if (!is_local) {
            AM_LOG_DEBUG(instance_id, "%s profile response status code: %d", thisfunc,
                    n.http_status);
            if (log != NULL) {
                log("%s profile response status code: %d", thisfunc,
                        n.http_status);
            }
        }

        if (status == AM_SUCCESS && n.http_status == 200 && agent_token != NULL) {
            char *token_in = NULL;
            char *session_post_data = NULL;
            size_t token_sz = am_asprintf(&token_in, "token:%s", NOTNULL(*agent_token));
            char *token_b64 = base64_encode(token_in, &token_sz);

            if (pxml != NULL && !is_local && ISVALID(ld.rd)) {
                /*no interest in a remote profile in case of local-only configuration*/
                *pxml = malloc(ld.sz + 1);
                if (*pxml != NULL) {
                    memcpy(*pxml, ld.rd, ld.sz);
                    (*pxml)[ld.sz] = 0;
                }
            }
            if (pxsz != NULL && !is_local) *pxsz = ld.sz;

            am_free(ld.rd);
            ld.rd = NULL;

            status = AM_ERROR;

            post_data_sz = am_asprintf(&session_post_data,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    "<RequestSet vers=\"1.0\" svcid=\"Session\" reqid=\"0\">"
                    "<Request><![CDATA["
                    "<SessionRequest vers=\"1.0\" reqid=\"1\" requester=\"%s\">"
                    "<GetSession reset=\"true\">"
                    "<SessionID>%s</SessionID>"
                    "</GetSession>"
                    "</SessionRequest>]]>"
                    "</Request>"
                    "<Request><![CDATA["
                    "<SessionRequest vers=\"1.0\" reqid=\"2\" requester=\"%s\">"
                    "<AddSessionListener>"
                    "<URL>%s</URL>"
                    "<SessionID>%s</SessionID>"
                    "</AddSessionListener>"
                    "</SessionRequest>]]>"
                    "</Request>"
                    "</RequestSet>",
                    NOTNULL(token_b64), *agent_token, NOTNULL(token_b64), notifyurl, *agent_token);

            AM_FREE(token_in, token_b64);

            if (session_post_data != NULL) {
                char *session_post = NULL;
                post_sz = am_asprintf(&session_post, "POST %s/sessionservice HTTP/1.1\r\n"
                        "Host: %s:%d\r\n"
                        "User-Agent: "MODINFO"\r\n"
                        "Accept: text/xml\r\n"
                        "Content-Language: UTF-8\r\n"
                        "Connection: Close\r\n"
                        "Content-Type: text/xml; charset=UTF-8\r\n"
                        "Content-Length: %d\r\n\r\n"
                        "%s", n.uv.path, n.uv.host, n.uv.port, post_data_sz, session_post_data);
                if (session_post != NULL) {
                    AM_LOG_DEBUG(instance_id, "%s sending request:\n%s", thisfunc, session_post);
                    if (log != NULL) {
                        log("%s sending request:\n%s", thisfunc, session_post);
                    }
                    status = am_net_write(&n, session_post, post_sz);
                    free(session_post);
                    session_post = NULL;
                }
                free(session_post_data);
                session_post_data = NULL;
            }
        } else {
            status = AM_ERROR;
        }
    }

    if (status == AM_SUCCESS) {
        wait_for_event(ld.rf, 0);
    } else {
        AM_LOG_DEBUG(instance_id, "%s disconnecting", thisfunc);
        if (log != NULL) {
            log("%s disconnecting", thisfunc);
        }
        am_net_diconnect(&n);
    }

    AM_LOG_DEBUG(instance_id, "%s sessionservice status code: %d", thisfunc, n.http_status);
    if (log != NULL) {
        log("%s sessionservice status code: %d", thisfunc, n.http_status);
    }

    if (status == AM_SUCCESS && n.http_status == 200 && ISVALID(ld.rd)) {
        AM_LOG_DEBUG(instance_id, "%s response:\n%s", thisfunc, ld.rd);
        if (log != NULL) {
            log("%s response:\n%s", thisfunc, ld.rd);
        }
        if (session_list != NULL)
            *session_list = am_parse_session_xml(instance_id, ld.rd, ld.sz);
    }

    am_net_close(&n);
    close_event(ld.rf);

    AM_FREE(ld.rd, user_enc, realm_enc, token_enc);
    return status;
}

int am_agent_logout(unsigned long instance_id, const char *openam,
        const char *token, struct am_ssl_options *info, void(*log)(const char *, ...)) {
    static const char *thisfunc = "am_agent_logout():";
    char *get = NULL;
    am_net_t n;
    size_t get_sz;
    int status = AM_ERROR;

    struct request_data ld;

    char *token_enc = url_encode(token);

    if (!ISVALID(token_enc) || !ISVALID(openam)) return AM_EINVAL;

    memset(&ld, 0, sizeof (struct request_data));

    memset(&n, 0, sizeof (am_net_t));
    n.log = log;
    n.instance_id = instance_id;
    n.timeout = AM_NET_CONNECT_TIMEOUT;
    n.url = openam;
    if (info != NULL) {
        memcpy(&n.ssl.info, info, sizeof (struct am_ssl_options));
    }

    ld.rf = create_event();
    if (ld.rf == NULL) return AM_ENOMEM;

    n.data = &ld;
    n.on_connected = on_connected_cb;
    n.on_close = on_close_cb;
    n.on_data = on_agent_request_data_cb;
    n.on_complete = on_complete_cb;

    if (am_net_connect(&n) == 0) {
        get_sz = am_asprintf(&get, "GET %s/identity/logout?subjectid=%s HTTP/1.1\r\n"
                "Host: %s:%d\r\n"
                "User-Agent: "MODINFO"\r\n"
                "Accept: text/plain\r\n"
                "Connection: close\r\n\r\n",
                n.uv.path, token_enc,
                n.uv.host, n.uv.port);
        if (get != NULL) {
            AM_LOG_DEBUG(instance_id, "%s sending request:\n%s", thisfunc, get);
            if (log != NULL) {
                log("%s sending request:\n%s", thisfunc, get);
            }
            status = am_net_write(&n, get, get_sz);
            free(get);
        }
    }

    if (status == AM_SUCCESS) {
        wait_for_event(ld.rf, 0);
    } else {
        AM_LOG_DEBUG(instance_id, "%s disconnecting", thisfunc);
        if (log != NULL) {
            log("%s disconnecting", thisfunc);
        }
        am_net_diconnect(&n);
    }

    AM_LOG_DEBUG(instance_id, "%s response status code: %d", thisfunc, n.http_status);
    if (log != NULL) {
        log("%s response status code: %d", thisfunc, n.http_status);
    }

    am_net_close(&n);
    close_event(ld.rf);

    AM_FREE(ld.rd, token_enc);
    return status;
}

int am_agent_naming_request(unsigned long instance_id, const char *openam, const char *token) {
    char *post = NULL, *post_data = NULL;
    am_net_t n;
    size_t post_sz;
    int status = AM_ERROR;

    struct request_data ld;

    if (!ISVALID(token) || !ISVALID(openam)) return AM_EINVAL;

    memset(&ld, 0, sizeof (struct request_data));

    memset(&n, 0, sizeof (am_net_t));
    n.instance_id = instance_id;
    n.timeout = AM_NET_CONNECT_TIMEOUT;
    n.url = openam;

    ld.rf = create_event();
    if (ld.rf == NULL) return AM_ENOMEM;

    n.data = &ld;
    n.on_connected = on_connected_cb;
    n.on_close = on_close_cb;
    n.on_data = on_agent_request_data_cb;
    n.on_complete = on_complete_cb;

    if (am_net_connect(&n) == 0) {
        size_t post_data_sz = am_asprintf(&post_data,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                "<RequestSet vers=\"1.0\" svcid=\"com.iplanet.am.naming\" reqid=\"0\">"
                "<Request><![CDATA["
                "<NamingRequest vers=\"3.0\" reqid=\"1\" sessid=\"%s\">"
                "<GetNamingProfile>"
                "</GetNamingProfile>"
                "</NamingRequest>]]>"
                "</Request>"
                "</RequestSet>",
                token);
        if (post_data != NULL) {
            post_sz = am_asprintf(&post, "POST %s/namingservice HTTP/1.1\r\n"
                    "Host: %s:%d\r\n"
                    "User-Agent: "MODINFO"\r\n"
                    "Accept: text/xml\r\n"
                    "Content-Language: UTF-8\r\n"
                    "Connection: close\r\n"
                    "Content-Type: text/xml; charset=UTF-8\r\n"
                    "Content-Length: %d\r\n\r\n"
                    "%s", n.uv.path, n.uv.host, n.uv.port, post_data_sz, post_data);
            if (post != NULL) {
                status = am_net_write(&n, post, post_sz);
                free(post);
                post = NULL;
            }
            free(post_data);
            post_data = NULL;
        }
    }

    if (status == AM_SUCCESS) {
        wait_for_event(ld.rf, 0);
    } else {
        am_net_diconnect(&n);
    }

    am_net_close(&n);
    close_event(ld.rf);

    am_free(ld.rd);
    return status;
}

int am_agent_session_request(unsigned long instance_id, const char *openam,
        const char *token, const char *user_token, const char *notif_url) {
    char *post = NULL, *post_data = NULL;
    am_net_t n;
    size_t post_sz;
    int status = AM_ERROR;

    struct request_data ld;

    if (!ISVALID(token) || !ISVALID(user_token) ||
            !ISVALID(openam) || !ISVALID(notif_url)) return AM_EINVAL;

    memset(&ld, 0, sizeof (struct request_data));

    memset(&n, 0, sizeof (am_net_t));
    n.instance_id = instance_id;
    n.timeout = AM_NET_CONNECT_TIMEOUT;
    n.url = openam;

    ld.rf = create_event();
    if (ld.rf == NULL) return AM_ENOMEM;

    n.data = &ld;
    n.on_connected = on_connected_cb;
    n.on_close = on_close_cb;
    n.on_data = on_agent_request_data_cb;
    n.on_complete = on_complete_cb;

    if (am_net_connect(&n) == 0) {
        char *token_in = NULL;
        size_t token_sz = am_asprintf(&token_in, "token:%s", token);
        char *token_b64 = base64_encode(token_in, &token_sz);

        size_t post_data_sz = am_asprintf(&post_data,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                "<RequestSet vers=\"1.0\" svcid=\"Session\" reqid=\"0\">"
                "<Request><![CDATA["
                "<SessionRequest vers=\"1.0\" reqid=\"1\" requester=\"%s\">"
                "<GetSession reset=\"true\">"
                "<SessionID>%s</SessionID>"
                "</GetSession>"
                "</SessionRequest>]]>"
                "</Request>"
                "<Request><![CDATA["
                "<SessionRequest vers=\"1.0\" reqid=\"2\" requester=\"%s\">"
                "<AddSessionListener>"
                "<URL>%s</URL>"
                "<SessionID>%s</SessionID>"
                "</AddSessionListener>"
                "</SessionRequest>]]>"
                "</Request>"
                "</RequestSet>",
                NOTNULL(token_b64), user_token, NOTNULL(token_b64), notif_url, user_token);

        AM_FREE(token_in, token_b64);

        if (post_data != NULL) {
            post_sz = am_asprintf(&post, "POST %s/sessionservice HTTP/1.1\r\n"
                    "Host: %s:%d\r\n"
                    "User-Agent: "MODINFO"\r\n"
                    "Accept: text/xml\r\n"
                    "Content-Language: UTF-8\r\n"
                    "Connection: close\r\n"
                    "Content-Type: text/xml; charset=UTF-8\r\n"
                    "Content-Length: %d\r\n\r\n"
                    "%s", n.uv.path, n.uv.host, n.uv.port, post_data_sz, post_data);
            if (post != NULL) {
                status = am_net_write(&n, post, post_sz);
                free(post);
                post = NULL;
            }
            free(post_data);
            post_data = NULL;
        }

    }

    if (status == AM_SUCCESS) {
        wait_for_event(ld.rf, 0);
    } else {
        am_net_diconnect(&n);
    }

    am_net_close(&n);
    close_event(ld.rf);

    am_free(ld.rd);
    return status;
}

int am_agent_policy_request(unsigned long instance_id, const char *openam,
        const char *token, const char *user_token, const char *req_url,
        const char *notif_url, const char *scope, const char *cip, const char *pattr,
        struct am_ssl_options *info,
        struct am_namevalue **session_list,
        struct am_policy_result **policy_list) {
    static const char *thisfunc = "am_agent_policy_request():";
    char *post = NULL, *post_data = NULL;
    am_net_t n;
    size_t post_sz;
    int status = AM_ERROR;
    int session_status = AM_SUCCESS;

    struct request_data ld;

    if (!ISVALID(token) || !ISVALID(user_token) || !ISVALID(notif_url) || !ISVALID(scope) ||
            !ISVALID(req_url) || !ISVALID(openam) || !ISVALID(cip)) return AM_EINVAL;

    memset(&ld, 0, sizeof (struct request_data));

    memset(&n, 0, sizeof (am_net_t));
    n.instance_id = instance_id;
    n.timeout = AM_NET_CONNECT_TIMEOUT;
    n.url = openam;
    if (info != NULL) {
        memcpy(&n.ssl.info, info, sizeof (struct am_ssl_options));
    }

    ld.rf = create_event();
    if (ld.rf == NULL) return AM_ENOMEM;

    n.data = &ld;
    n.on_connected = on_connected_cb;
    n.on_close = on_close_cb;
    n.on_data = on_agent_request_data_cb;
    n.on_complete = on_complete_cb;

    if (am_net_connect(&n) == 0) {
        char *token_in = NULL;
        size_t token_sz = am_asprintf(&token_in, "token:%s", token);
        char *token_b64 = base64_encode(token_in, &token_sz);

        size_t post_data_sz = am_asprintf(&post_data,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                "<RequestSet vers=\"1.0\" svcid=\"Session\" reqid=\"0\">"
                "<Request><![CDATA["
                "<SessionRequest vers=\"1.0\" reqid=\"1\" requester=\"%s\">"
                "<GetSession reset=\"true\">" /*reset the idle timeout*/
                "<SessionID>%s</SessionID>"
                "</GetSession>"
                "</SessionRequest>]]>"
                "</Request>"
                "<Request><![CDATA["
                "<SessionRequest vers=\"1.0\" reqid=\"2\" requester=\"%s\">"
                "<AddSessionListener>"
                "<URL>%s</URL>"
                "<SessionID>%s</SessionID>"
                "</AddSessionListener>"
                "</SessionRequest>]]>"
                "</Request>"
                "</RequestSet>",
                NOTNULL(token_b64), user_token, NOTNULL(token_b64), notif_url, user_token);

        AM_FREE(token_in, token_b64);

        if (post_data != NULL) {
            post_sz = am_asprintf(&post, "POST %s/sessionservice HTTP/1.1\r\n"
                    "Host: %s:%d\r\n"
                    "User-Agent: "MODINFO"\r\n"
                    "Accept: text/xml\r\n"
                    "Content-Language: UTF-8\r\n"
                    "Connection: Keep-Alive\r\n"
                    "Content-Type: text/xml; charset=UTF-8\r\n"
                    "Content-Length: %d\r\n\r\n"
                    "%s", n.uv.path, n.uv.host, n.uv.port, post_data_sz, post_data);
            if (post != NULL) {
                AM_LOG_DEBUG(instance_id, "%s sending request:\n%s", thisfunc, post);
                status = am_net_write(&n, post, post_sz);
                free(post);
                post = NULL;
            }
            free(post_data);
            post_data = NULL;
        }

        if (status == AM_SUCCESS)
            wait_for_event(ld.rf, 0);

        AM_LOG_DEBUG(instance_id, "%s response status code: %d", thisfunc, n.http_status);

        if (status == AM_SUCCESS && n.http_status == 200 && ISVALID(ld.rd)) {
            size_t req_url_sz = strlen(req_url);
            char *req_url_escaped = malloc(req_url_sz * 6 + 1); /*worst case*/
            if (req_url_escaped != NULL) {
                memcpy(req_url_escaped, req_url, req_url_sz);
                xml_entity_escape(req_url_escaped, req_url_sz);
            }

            AM_LOG_DEBUG(instance_id, "%s response:\n%s", thisfunc, ld.rd);

            if (strstr(ld.rd, "<Exception>") != NULL && strstr(ld.rd, "Invalid session ID") != NULL) {
                session_status = AM_INVALID_SESSION;
            }
            if (strstr(ld.rd, "<Exception>") != NULL && strstr(ld.rd, "Application token passed in") != NULL) {
                session_status = AM_INVALID_AGENT_SESSION;
            }

            if (session_status == AM_SUCCESS && session_list != NULL)
                *session_list = am_parse_session_xml(instance_id, ld.rd, ld.sz);

            ld.sz = 0;
            free(ld.rd);
            ld.rd = NULL;

            /* TODO:
             * <AttributeValuePair><Attribute name=\"requestDnsName\"/><Value>%s</Value></AttributeValuePair>
             */
            post_data_sz = am_asprintf(&post_data,
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    "<RequestSet vers=\"1.0\" svcid=\"Policy\" reqid=\"3\">"
                    "<Request><![CDATA[<PolicyService version=\"1.0\">"
                    "<PolicyRequest requestId=\"4\" appSSOToken=\"%s\">"
                    "<GetResourceResults userSSOToken=\"%s\" serviceName=\"iPlanetAMWebAgentService\" resourceName=\"%s\" resourceScope=\"%s\">"
                    "<EnvParameters><AttributeValuePair><Attribute name=\"requestIp\"/><Value>%s</Value></AttributeValuePair></EnvParameters>"
                    "<GetResponseDecisions>"
                    "%s"
                    "</GetResponseDecisions>"
                    "</GetResourceResults>"
                    "</PolicyRequest>"
                    "</PolicyService>]]>"
                    "</Request>"
                    "</RequestSet>",
                    token, user_token, NOTNULL(req_url_escaped), scope,
                    cip, NOTNULL(pattr));

            am_free(req_url_escaped);

            post_sz = am_asprintf(&post, "POST %s/policyservice HTTP/1.1\r\n"
                    "Host: %s:%d\r\n"
                    "User-Agent: "MODINFO"\r\n"
                    "Accept: text/xml\r\n"
                    "Content-Language: UTF-8\r\n"
                    "Content-Type: text/xml; charset=UTF-8\r\n"
                    "Content-Length: %d\r\n"
                    "Connection: close\r\n\r\n"
                    "%s", n.uv.path, n.uv.host, n.uv.port,
                    post_data_sz, post_data);

            if (post != NULL) {
                AM_LOG_DEBUG(instance_id, "%s sending request:\n%s", thisfunc, post);
                status = am_net_write(&n, post, post_sz);
                free(post);
            }
        } else {
            status = n.error != AM_SUCCESS ? n.error : AM_ERROR;
        }
    }

    if (status == AM_SUCCESS) {
        wait_for_event(ld.rf, 0);
    } else {
        AM_LOG_DEBUG(instance_id, "%s disconnecting", thisfunc);
        am_net_diconnect(&n);
    }

    AM_LOG_DEBUG(instance_id, "%s response status code: %d", thisfunc, n.http_status);

    if (status == AM_SUCCESS && n.http_status == 200 && ISVALID(ld.rd)) {
        AM_LOG_DEBUG(instance_id, "%s response:\n%s", thisfunc, ld.rd);

        if (strstr(ld.rd, "<Exception>") != NULL && strstr(ld.rd, "SSO token is invalid") != NULL) {
            session_status = AM_INVALID_SESSION;
        }
        if (strstr(ld.rd, "<Exception>") != NULL && strstr(ld.rd, "Application sso token is invalid") != NULL) {
            session_status = AM_INVALID_AGENT_SESSION;
        }

        if (session_status == AM_SUCCESS && policy_list != NULL)
            *policy_list = am_parse_policy_xml(instance_id, ld.rd, ld.sz,
                am_scope_to_num(scope));
    }

    am_net_close(&n);
    close_event(ld.rf);

    am_free(ld.rd);
    return session_status != AM_SUCCESS ? session_status : status;
}

int am_url_validate(unsigned long instance_id, const char *url, struct am_ssl_options *info, int *httpcode) {
    static const char *thisfunc = "am_url_validate():";
    char *get = NULL;
    am_net_t n;
    size_t get_sz;
    int status = AM_ERROR;
    struct request_data ld;

    if (!ISVALID(url)) return AM_EINVAL;

    memset(&ld, 0, sizeof (struct request_data));
    memset(&n, 0, sizeof (am_net_t));
    n.log = NULL;
    n.instance_id = instance_id;
    n.timeout = AM_NET_CONNECT_TIMEOUT;
    n.url = url;
    if (info != NULL) {
        memcpy(&n.ssl.info, info, sizeof (struct am_ssl_options));
    }

    ld.rf = create_event();
    if (ld.rf == NULL) return AM_ENOMEM;

    n.data = &ld;
    n.on_connected = on_connected_cb;
    n.on_close = on_close_cb;
    n.on_data = on_agent_request_data_cb;
    n.on_complete = on_complete_cb;

    if (am_net_connect(&n) == 0) {
        get_sz = am_asprintf(&get, "HEAD %s HTTP/1.1\r\n"
                "Host: %s:%d\r\n"
                "User-Agent: "MODINFO"\r\n"
                "Accept: text/plain\r\n"
                "Connection: close\r\n\r\n",
                n.uv.path, n.uv.host, n.uv.port);
        if (get != NULL) {
            AM_LOG_DEBUG(instance_id, "%s sending request:\n%s", thisfunc, get);
            status = am_net_write(&n, get, get_sz);
            free(get);
        }
    }

    if (status == AM_SUCCESS) {
        wait_for_event(ld.rf, 0);
    } else {
        AM_LOG_DEBUG(instance_id, "%s disconnecting", thisfunc);
        am_net_diconnect(&n);
    }

    AM_LOG_DEBUG(instance_id, "%s response status code: %d", thisfunc, n.http_status);
    if (httpcode) *httpcode = n.http_status;

    am_net_close(&n);
    close_event(ld.rf);

    am_free(ld.rd);
    return status;
}
