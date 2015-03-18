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
#include "expat.h"
#include "pcre.h"

/*
 * XML parser for 'identitydetails' element (agent profile)
 */

typedef struct {
    int depth;
    char current_name[AM_URI_SIZE];
    char setting_value;
    am_config_t *conf;
    pcre *rgx;
    void *parser;
    char log_enable;
} am_xml_parser_ctx_t;

static void start_element(void *userData, const char *name, const char **atts) {
    int i;
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;

    ctx->setting_value = 0;
    if (strcmp(name, "name") == 0) {
        for (i = 0; atts[i]; i += 2) {
            if (strcmp(atts[i], "value") == 0) {
                size_t nl = strlen(atts[i + 1]);
                ctx->conf->user = malloc(nl + 1);
                memcpy(ctx->conf->user, atts[i + 1], nl);
                ctx->conf->user[nl] = 0;
                break;
            }
        }
    } else if (strcmp(name, "realm") == 0) {
        for (i = 0; atts[i]; i += 2) {
            if (strcmp(atts[i], "value") == 0) {
                size_t nl = strlen(atts[i + 1]);
                ctx->conf->realm = malloc(nl + 1);
                memcpy(ctx->conf->realm, atts[i + 1], nl);
                ctx->conf->realm[nl] = 0;
                break;
            }
        }
    } else if (strcmp(name, "attribute") == 0) {
        for (i = 0; atts[i]; i += 2) {
            if (strcmp(atts[i], "name") == 0) {
                size_t nl = strlen(atts[i + 1]);
                if (nl < sizeof (ctx->current_name)) {
                    memcpy(ctx->current_name, atts[i + 1], nl);
                    ctx->current_name[nl] = 0;
                    break;
                }
            }
        }
    } else if (strcmp(name, "value") == 0) {
        ctx->setting_value = 1;
    } else {
        memset(&ctx->current_name[0], 0, sizeof (ctx->current_name));
    }

    ctx->depth++;
}

static void end_element(void * userData, const char * name) {
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    ctx->depth--;
    ctx->setting_value = 0;
}

#define PARSE_NUMBER(x,prm,itm,val,vl) \
    do {\
        if (strcmp(x->current_name,prm) != 0) break;\
        if (strncasecmp(val, "on", len) == 0 || strncasecmp(val, "true", len) == 0) {\
            itm = 1;\
        } else if (strncasecmp(val, "off", len) == 0 || strncasecmp(val, "false", len) == 0) {\
            itm = 0;\
        } else {\
            char *t = malloc( vl + 1);\
            if (t == NULL) break;\
            memcpy(t, val, vl);\
            t[vl] = 0;\
            itm = strtol(t, NULL, 10);free(t);\
        }\
        if (x->log_enable) \
        am_log_debug(x->conf->instance_id, "am_parse_config_xml() %s is set to '%d'",\
            prm, itm);\
    } while (0);

#define PARSE_STRING(x,prm,itm,val,vl) \
    do {\
        if (strcmp(x->current_name,prm) != 0) break;\
            itm = (char *)malloc(vl + 1);\
            if (itm == NULL) break;\
            memcpy(itm, val, vl);\
            (itm)[vl] = 0;\
            if (x->log_enable) { \
            if (strstr(prm, "password") != NULL) \
            am_log_debug(x->conf->instance_id, "am_parse_config_xml() %s is set to '********'",\
                prm);\
            else \
            am_log_debug(x->conf->instance_id, "am_parse_config_xml() %s is set to '%s'",\
                prm, itm); } \
    } while (0);

#define PARSE_STRING_LIST(x,prm,itmsz,itm,val,vl) \
    do {\
        char *value = NULL; int old_sz = itmsz;\
        if (strcmp(x->current_name,prm) != 0) break;\
        value = (char *)malloc(vl + 1);\
        if (value == NULL) break;\
        memcpy(value, val, vl);\
        value[vl] = 0;\
        itm = (char **) realloc(itm,\
            sizeof(char *) *(++(itmsz)));\
        if (itm == NULL) {itmsz--; if (itmsz<0) itmsz = 0; free(value); break;}\
        itm[old_sz] = value;\
        if (x->log_enable) \
        am_log_debug(x->conf->instance_id, "am_parse_config_xml() %s is set to %d value(s)",\
            prm, itmsz);\
    } while (0);

#define PARSE_NUMBER_LIST(x,prm,itmsz,itm,val,vl) \
    do {\
        char *value = NULL; int old_sz = itmsz;\
        if (strcmp(x->current_name,prm) != 0) break;\
        value = (char *)malloc(vl + 1);\
        if (value == NULL) break;\
        memcpy(value, val, vl);\
        value[vl] = 0;\
        itm = (int *) realloc(itm, \
            sizeof(int) *(++(itmsz)));\
        if (itm == NULL) {itmsz--; if (itmsz<0) itmsz = 0; free(value); break;}\
        itm[old_sz] = strtol(value, NULL, 10);free(value);\
        if (x->log_enable) \
        am_log_debug(x->conf->instance_id, "am_parse_config_xml() %s is set to %d value(s)",\
            prm, itmsz);\
    } while (0);

#define PARSE_STRING_MAP(x,prm,itmsz,itm,val,vl) \
    do {\
        char *value_tmp, *value = NULL; int old_sz = itmsz;\
        size_t slen = vl;\
        value_tmp = (char *)malloc(vl + 1);\
        if (value_tmp == NULL) break;\
        memcpy(value_tmp, val, vl);\
        value_tmp[vl] = 0;\
        if (strcmp(x->current_name,prm) != 0 || val[0] != '[') break;\
        value = match_group(x->rgx, 2 /*groups in [key]=value*/, value_tmp, &slen);\
        free(value_tmp);\
        if (value == NULL) break;\
        itm = (am_config_map_t *) realloc(itm, \
            sizeof(am_config_map_t) *(++(itmsz)));\
        if (itm == NULL) {itmsz--; if (itmsz<0) itmsz = 0; free(value); break;}\
        (&itm[old_sz])->name = value;\
        (&itm[old_sz])->value = value + strlen(value) + 1;\
        if (x->log_enable) \
        am_log_debug(x->conf->instance_id, "am_parse_config_xml() %s is set to %d value(s)",\
            prm, itmsz);\
    } while (0);

static void character_data(void *userData, const char *val, int len) {
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    if (!ISVALID(ctx->current_name) || ctx->setting_value == 0 || len <= 0 ||
            strncmp(val, "[]=", len) == 0 || strncmp(val, "[0]=", len) == 0) return;

    /* bootstrap options */

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_LOCAL, ctx->conf->local, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_POSTDATA_PRESERVE_DIR, ctx->conf->pdp_dir, val, len);
    PARSE_STRING_LIST(ctx, AM_AGENTS_CONFIG_NAMING_URL, ctx->conf->naming_url_sz, ctx->conf->naming_url, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_REALM, ctx->conf->realm, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_USER, ctx->conf->user, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_PASSWORD, ctx->conf->pass, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_KEY, ctx->conf->key, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_DEBUG_FILE, ctx->conf->debug_file, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_DEBUG_LEVEL, ctx->conf->debug_level, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_DEBUG_OPT, ctx->conf->debug, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_AUDIT_FILE, ctx->conf->audit_file, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_AUDIT_LEVEL, ctx->conf->audit_level, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_AUDIT_OPT, ctx->conf->audit, val, len);

    PARSE_STRING(ctx, AM_AGENTS_CONFIG_CERT_KEY_FILE, ctx->conf->cert_key_file, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_CERT_KEY_PASSWORD, ctx->conf->cert_key_pass, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_CERT_FILE, ctx->conf->cert_file, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_CA_FILE, ctx->conf->cert_ca_file, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_CIPHERS, ctx->conf->ciphers, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_TRUST_CERT, ctx->conf->cert_trust, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_TLS_OPT, ctx->conf->tls_opts, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_NET_TIMEOUT, ctx->conf->net_timeout, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_LEVEL, ctx->conf->valid_level, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_PING_INTERVAL, ctx->conf->valid_ping, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_PING_MISS, ctx->conf->valid_ping_miss, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_PING_OK, ctx->conf->valid_ping_ok, val, len);
    PARSE_NUMBER_LIST(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_DEFAULT_SET, ctx->conf->valid_default_url_sz, ctx->conf->valid_default_url, val, len);

    PARSE_STRING_LIST(ctx, AM_AGENTS_CONFIG_HOST_MAP, ctx->conf->hostmap_sz, ctx->conf->hostmap, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_RETRY_MAX, ctx->conf->retry_max, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_RETRY_WAIT, ctx->conf->retry_wait, val, len);

    /* other options */

    PARSE_STRING(ctx, AM_AGENTS_CONFIG_AGENT_URI, ctx->conf->agenturi, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_COOKIE_NAME, ctx->conf->cookie_name, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_LOGIN_URL_MAP, ctx->conf->login_url_sz, ctx->conf->login_url, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_COOKIE_SECURE, ctx->conf->cookie_secure, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_NOTIF_ENABLE, ctx->conf->notif_enable, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_NOTIF_URL, ctx->conf->notif_url, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_CMP_CASE_IGNORE, ctx->conf->url_eval_case_ignore, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_POLICY_CACHE_VALID, ctx->conf->policy_cache_valid, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_TOKEN_CACHE_VALID, ctx->conf->token_cache_valid, val, len);

    PARSE_STRING(ctx, AM_AGENTS_CONFIG_UID_PARAM, ctx->conf->userid_param, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_UID_PARAM_TYPE, ctx->conf->userid_param_type, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_ATTR_PROFILE_MODE, ctx->conf->profile_attr_fetch, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_ATTR_PROFILE_MAP, ctx->conf->profile_attr_map_sz, ctx->conf->profile_attr_map, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_ATTR_SESSION_MODE, ctx->conf->session_attr_fetch, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_ATTR_SESSION_MAP, ctx->conf->session_attr_map_sz, ctx->conf->session_attr_map, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_ATTR_RESPONSE_MODE, ctx->conf->response_attr_fetch, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_ATTR_RESPONSE_MAP, ctx->conf->response_attr_map_sz, ctx->conf->response_attr_map, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_LB_ENABLE, ctx->conf->lb_enable, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_SSO_ONLY, ctx->conf->sso_only, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_ACCESS_DENIED_URL, ctx->conf->access_denied_url, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_FQDN_CHECK_ENABLE, ctx->conf->fqdn_check_enable, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_FQDN_DEFAULT, ctx->conf->fqdn_default, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_FQDN_MAP, ctx->conf->fqdn_map_sz, ctx->conf->fqdn_map, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_COOKIE_RESET_ENABLE, ctx->conf->cookie_reset_enable, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_COOKIE_RESET_MAP, ctx->conf->cookie_reset_map_sz, ctx->conf->cookie_reset_map, val, len);

    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_URL, ctx->conf->not_enforced_map_sz, ctx->conf->not_enforced_map, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_INVERT, ctx->conf->not_enforced_invert, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_ATTR, ctx->conf->not_enforced_fetch_attr, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_IP, ctx->conf->not_enforced_ip_map_sz, ctx->conf->not_enforced_ip_map, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_PDP_ENABLE, ctx->conf->pdp_enable, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_PDP_VALID, ctx->conf->pdp_cache_valid, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_PDP_COOKIE, ctx->conf->pdp_lb_cookie, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_CLIENT_IP_VALIDATE, ctx->conf->client_ip_validate, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_ATTR_COOKIE_PREFIX, ctx->conf->cookie_prefix, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_ATTR_COOKIE_MAX_AGE, ctx->conf->cookie_maxage, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_CDSSO_ENABLE, ctx->conf->cdsso_enable, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_CDSSO_LOGIN, ctx->conf->cdsso_login_map_sz, ctx->conf->cdsso_login_map, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_CDSSO_DOMAIN, ctx->conf->cdsso_cookie_domain_map_sz, ctx->conf->cdsso_cookie_domain_map, val, len);

    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_LOGOUT_URL, ctx->conf->openam_logout_map_sz, ctx->conf->openam_logout_map, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_APP_LOGOUT_URL, ctx->conf->logout_map_sz, ctx->conf->logout_map, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_LOGOUT_REDIRECT_URL, ctx->conf->logout_redirect_url, val, len);
    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_LOGOUT_COOKIE_RESET, ctx->conf->logout_cookie_reset_map_sz, ctx->conf->logout_cookie_reset_map, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_POLICY_SCOPE, ctx->conf->policy_scope_subtree, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_RESOLVE_CLIENT_HOST, ctx->conf->resolve_client_host, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_POLICY_ENCODE_SPECIAL_CHAR, ctx->conf->policy_eval_encode_chars, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_COOKIE_ENCODE_SPECIAL_CHAR, ctx->conf->cookie_encode_chars, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_OVERRIDE_PROTO, ctx->conf->override_protocol, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_OVERRIDE_HOST, ctx->conf->override_host, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_OVERRIDE_PORT, ctx->conf->override_port, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_OVERRIDE_NOTIFICATION_URL, ctx->conf->override_notif_url, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_VALID, ctx->conf->config_valid, val, len);

    PARSE_STRING(ctx, AM_AGENTS_CONFIG_PASSWORD_REPLAY_KEY, ctx->conf->password_replay_key, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_POLICY_CLOCK_SKEW, ctx->conf->policy_clock_skew, val, len);

    PARSE_STRING(ctx, AM_AGENTS_CONFIG_GOTO_PARAM_NAME, ctx->conf->url_redirect_param, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_CACHE_CONTROL_ENABLE, ctx->conf->cache_control_enable, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_USE_REDIRECT_ADVICE, ctx->conf->use_redirect_for_advice, val, len);

    PARSE_STRING(ctx, AM_AGENTS_CONFIG_CLIENT_IP_HEADER, ctx->conf->client_ip_header, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_CLIENT_HOSTNAME_HEADER, ctx->conf->client_hostname_header, val, len);

    PARSE_STRING(ctx, AM_AGENTS_CONFIG_INVALID_URL, ctx->conf->url_check_regex, val, len);

    PARSE_STRING_MAP(ctx, AM_AGENTS_CONFIG_CONDITIONAL_LOGIN_URL, ctx->conf->cond_login_url_sz, ctx->conf->cond_login_url, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_COOKIE_HTTP_ONLY, ctx->conf->cookie_http_only, val, len);
    PARSE_STRING(ctx, AM_AGENTS_CONFIG_MULTI_VALUE_SEPARATOR, ctx->conf->multi_attr_separator, val, len);

    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_IIS_LOGON_USER, ctx->conf->logon_user_enable, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_IIS_PASSWORD_HEADER, ctx->conf->password_header_enable, val, len);
    PARSE_NUMBER(ctx, AM_AGENTS_CONFIG_PDP_JS_REPOST, ctx->conf->pdp_js_repost, val, len);
}

static void entity_declaration(void *userData, const XML_Char *entityName,
        int is_parameter_entity, const XML_Char *value, int value_length, const XML_Char *base,
        const XML_Char *systemId, const XML_Char *publicId, const XML_Char *notationName) {
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    XML_StopParser(ctx->parser, XML_FALSE);
}

am_config_t *am_parse_config_xml(unsigned long instance_id, const char *xml, size_t xml_sz, char log_enable) {
    const char *thisfunc = "am_parse_config_xml():";
    am_config_t *r = NULL;
    char *begin, *stream = NULL;
    size_t data_sz;
    pcre *x = NULL;
    const char *error = NULL;
    int erroroffset;

    am_xml_parser_ctx_t xctx = {.depth = 0, .setting_value = 0,
        .conf = NULL, .rgx = NULL, .parser = NULL, .log_enable = log_enable};

    if (xml == NULL || xml_sz == 0) {
        am_log_error(instance_id, "%s memory allocation error", thisfunc);
        return NULL;
    }

    /*match [key]=value returned within <value>[key]=value_of_a_key</value> element*/
    x = pcre_compile("(?<=\\[)(.+?)(?=\\])\\]\\s*\\=\\s*(.+)", 0, &error, &erroroffset, NULL);
    if (x == NULL) {
        am_log_error(instance_id, "%s pcre error %s", thisfunc, error == NULL ? "" : error);
    }

    r = calloc(1, sizeof (am_config_t));
    if (r == NULL) {
        am_log_error(instance_id, "%s memory allocation error", thisfunc);
        pcre_free(x);
        return NULL;
    }
    r->instance_id = instance_id;

    begin = strstr(xml, "![CDATA[");
    if (begin != NULL) {
        char *end = strstr(begin + 8, "]]>");
        if (end != NULL) {
            stream = begin + 8;
            data_sz = end - (begin + 8);
        }
    } else {
        /*no CDATA*/
        stream = (char *) xml;
        data_sz = xml_sz;
    }

    if (stream != NULL && data_sz > 0) {
        XML_Parser parser = XML_ParserCreate("UTF-8");
        xctx.parser = &parser;
        xctx.conf = r;
        xctx.rgx = x;
        XML_SetUserData(parser, &xctx);
        XML_SetElementHandler(parser, start_element, end_element);
        XML_SetCharacterDataHandler(parser, character_data);
        XML_SetEntityDeclHandler(parser, entity_declaration);
        if (XML_Parse(parser, stream, (int) data_sz, XML_TRUE) == XML_STATUS_ERROR) {
            const char *message = XML_ErrorString(XML_GetErrorCode(parser));
            int line = XML_GetCurrentLineNumber(parser);
            int col = XML_GetCurrentColumnNumber(parser);
            am_log_error(instance_id, "%s xml parser error (%d:%d) %s", thisfunc,
                    line, col, message);
            am_config_free(&r);
            r = NULL;
        } else {
            r->ts = time(NULL);
        }
        XML_ParserFree(parser);
    }

    pcre_free(x);
    return r;
}
