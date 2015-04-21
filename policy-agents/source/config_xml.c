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
        return;
    }
    if (strcmp(name, "realm") == 0) {
        for (i = 0; atts[i]; i += 2) {
            if (strcmp(atts[i], "value") == 0) {
                size_t nl = strlen(atts[i + 1]);
                ctx->conf->realm = malloc(nl + 1);
                memcpy(ctx->conf->realm, atts[i + 1], nl);
                ctx->conf->realm[nl] = 0;
                break;
            }
        }
        return;
    }
    if (strcmp(name, "attribute") == 0) {
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
        return;
    }
    if (strcmp(name, "value") == 0) {
        ctx->setting_value = 1;
        return;
    }
    memset(&ctx->current_name[0], 0, sizeof (ctx->current_name));
}

static void end_element(void * userData, const char * name) {
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    ctx->setting_value = 0;
}

static void parse_config_value(am_xml_parser_ctx_t *x, const char *prm, int type,
        int *itm_sz, void *itm, const char *val, int len) {

    if (itm == NULL || val == NULL || len <= 0 || strcmp(x->current_name, prm) != 0) {
        return;
    }

    switch (type) {
        case CONF_STRING:
        {
            char **value = (char **) itm;
            *value = strndup(val, len);
            if (x->log_enable) {
                if (strstr(prm, "password") != NULL) {
                    AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to '********'",
                            prm);
                    break;
                }
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to '%s'",
                        prm, LOGEMPTY(*value));
            }
        }
            break;
        case CONF_NUMBER:
        {
            int *value = (int *) itm;
            if (strncasecmp(val, "on", len) == 0 || strncasecmp(val, "true", len) == 0 ||
                    strncasecmp(val, "local", len) == 0) {
                *value = 1;
            } else if (strncasecmp(val, "off", len) == 0 || strncasecmp(val, "false", len) == 0 ||
                    strncasecmp(val, "centralized", len) == 0) {
                *value = 0;
            } else {
                char *t = strndup(val, len);
                if (t != NULL) {
                    *value = strtol(t, NULL, AM_BASE_TEN);
                    free(t);
                }
            }
            if (x->log_enable) {
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to '%d'", prm, *value);
            }
        }
            break;
        case CONF_DEBUG_LEVEL:
        {
            int *value = (int *) itm;
            *value = AM_LOG_LEVEL_NONE;
            if (strncasecmp(val, "all", 3) == 0) {
                *value = AM_LOG_LEVEL_DEBUG;
            } else if (strncasecmp(val, "error", len) == 0) {
                *value = AM_LOG_LEVEL_ERROR;
            } else if (strncasecmp(val, "info", len) == 0) {
                *value = AM_LOG_LEVEL_INFO;
            } else if (strncasecmp(val, "message", len) == 0) {
                *value = AM_LOG_LEVEL_WARNING;
            } else if (strncasecmp(val, "warning", len) == 0) {
                *value = AM_LOG_LEVEL_WARNING;
            }
            if (x->log_enable) {
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to '%d'",
                        prm, *value);
            }
        }
            break;
        case CONF_ATTR_MODE:
        {
            int *value = (int *) itm;
            *value = AM_SET_ATTRS_NONE;
            if (strncasecmp(val, "HTTP_HEADER", len) == 0) {
                *value = AM_SET_ATTRS_AS_HEADER;
            } else if (strncasecmp(val, "HTTP_COOKIE", len) == 0) {
                *value = AM_SET_ATTRS_AS_COOKIE;
            }
            if (x->log_enable) {
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to '%d'",
                        prm, *value);
            }
        }
            break;
        case CONF_AUDIT_LEVEL:
        {
            int *value = (int *) itm;
            *value = 0;
            if (strncasecmp(val, "LOG_ALLOW", len) == 0) {
                *value |= AM_LOG_LEVEL_AUDIT_ALLOW;
            } else if (strncasecmp(val, "LOG_BOTH", len) == 0) {
                *value |= AM_LOG_LEVEL_AUDIT_ALLOW;
                *value |= AM_LOG_LEVEL_AUDIT_DENY;
            } else if (strncasecmp(val, "LOG_DENY", len) == 0) {
                *value |= AM_LOG_LEVEL_AUDIT_DENY;
            } else if (strncasecmp(val, "ALL", len) == 0) {
                *value |= AM_LOG_LEVEL_AUDIT;
                *value |= AM_LOG_LEVEL_AUDIT_REMOTE;
            } else if (strncasecmp(val, "LOCAL", len) == 0) {
                *value |= AM_LOG_LEVEL_AUDIT;
            } else if (strncasecmp(val, "REMOTE", len) == 0) {
                *value |= AM_LOG_LEVEL_AUDIT_REMOTE;
            }
            if (x->log_enable) {
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to '%d'",
                        prm, *value);
            }
        }
            break;
        case CONF_STRING_LIST:
        {
            int old_sz = *itm_sz;
            char ***value = (char ***) itm;
            char **value_tmp;
            char *v = strndup(val, len);
            if (v == NULL) break;
            value_tmp = (char **) realloc(*value, sizeof (char *) *(++(*itm_sz)));
            if (value_tmp == NULL) {//TODO: realloc failure?
                (*itm_sz)--;
                if (*itm_sz < 0) *itm_sz = 0;
                free(v);
                break;
            }
            *value = value_tmp;
            (*value)[old_sz] = v;
            if (x->log_enable) {
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to %d value(s)",
                        prm, *itm_sz);
            }
        }
            break;
        case CONF_NUMBER_LIST:
        {
            int old_sz = *itm_sz;
            int **value = (int **) itm;
            int *value_tmp;
            char *v = strndup(val, len);
            if (v == NULL) break;
            value_tmp = (int *) realloc(*value, sizeof (int) *(++(*itm_sz)));
            if (value_tmp == NULL) {
                (*itm_sz)--;
                if (*itm_sz < 0) *itm_sz = 0;
                free(v);
                break;
            }
            *value = value_tmp;
            (*value)[old_sz] = strtol(v, NULL, AM_BASE_TEN);
            free(v);
            if (x->log_enable) {
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to %d value(s)",
                        prm, *itm_sz);
            }
        }
            break;
        case CONF_STRING_MAP:
        {
            int old_sz = *itm_sz;
            am_config_map_t **value = (am_config_map_t **) itm;
            am_config_map_t *value_tmp;
            size_t slen = (size_t) len;
            char *v, *mv;

            if (val[0] != '[') break;
            v = strndup(val, len);
            if (v == NULL) break;

            mv = match_group(x->rgx, 2 /*groups in [key]=value*/, v, &slen);
            free(v);
            if (mv == NULL) break;

            value_tmp = (am_config_map_t *) realloc(*value, sizeof (am_config_map_t) *(++(*itm_sz)));
            if (value_tmp == NULL) {
                (*itm_sz)--;
                if (*itm_sz < 0) *itm_sz = 0;
                free(mv);
                break;
            }
            *value = value_tmp;
            (&(*value)[old_sz])->name = mv;
            (&(*value)[old_sz])->value = mv + strlen(mv) + 1;
            if (x->log_enable) {
                AM_LOG_DEBUG(x->conf->instance_id, "am_parse_config_xml() %s is set to %d value(s)",
                        prm, *itm_sz);
            }
        }
            break;
        default:
            AM_LOG_WARNING(x->conf->instance_id, "am_parse_config_xml() unknown type value %d setting %s", type, prm);
            break;
    }
}

static void parse_other_options(am_xml_parser_ctx_t *ctx, const char *val, int len) {
    parse_config_value(ctx, AM_AGENTS_CONFIG_AGENT_URI, CONF_STRING, NULL, &ctx->conf->agenturi, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_COOKIE_NAME, CONF_STRING, NULL, &ctx->conf->cookie_name, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_LOGIN_URL_MAP, CONF_STRING_MAP, &ctx->conf->login_url_sz, &ctx->conf->login_url, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_COOKIE_SECURE, CONF_NUMBER, NULL, &ctx->conf->cookie_secure, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_NOTIF_ENABLE, CONF_NUMBER, NULL, &ctx->conf->notif_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_NOTIF_URL, CONF_STRING, NULL, &ctx->conf->notif_url, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CMP_CASE_IGNORE, CONF_NUMBER, NULL, &ctx->conf->url_eval_case_ignore, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_POLICY_CACHE_VALID, CONF_NUMBER, NULL, &ctx->conf->policy_cache_valid, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_TOKEN_CACHE_VALID, CONF_NUMBER, NULL, &ctx->conf->token_cache_valid, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_UID_PARAM, CONF_STRING, NULL, &ctx->conf->userid_param, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_UID_PARAM_TYPE, CONF_STRING, NULL, &ctx->conf->userid_param_type, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_PROFILE_MODE, CONF_ATTR_MODE, NULL, &ctx->conf->profile_attr_fetch, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_PROFILE_MAP, CONF_STRING_MAP, &ctx->conf->profile_attr_map_sz, &ctx->conf->profile_attr_map, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_SESSION_MODE, CONF_ATTR_MODE, NULL, &ctx->conf->session_attr_fetch, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_SESSION_MAP, CONF_STRING_MAP, &ctx->conf->session_attr_map_sz, &ctx->conf->session_attr_map, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_RESPONSE_MODE, CONF_ATTR_MODE, NULL, &ctx->conf->response_attr_fetch, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_RESPONSE_MAP, CONF_STRING_MAP, &ctx->conf->response_attr_map_sz, &ctx->conf->response_attr_map, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_LB_ENABLE, CONF_NUMBER, NULL, &ctx->conf->lb_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_SSO_ONLY, CONF_NUMBER, NULL, &ctx->conf->sso_only, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ACCESS_DENIED_URL, CONF_STRING, NULL, &ctx->conf->access_denied_url, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_FQDN_CHECK_ENABLE, CONF_NUMBER, NULL, &ctx->conf->fqdn_check_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_FQDN_DEFAULT, CONF_STRING, NULL, &ctx->conf->fqdn_default, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_FQDN_MAP, CONF_STRING_MAP, &ctx->conf->fqdn_map_sz, &ctx->conf->fqdn_map, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_COOKIE_RESET_ENABLE, CONF_NUMBER, NULL, &ctx->conf->cookie_reset_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_COOKIE_RESET_MAP, CONF_STRING_MAP, &ctx->conf->cookie_reset_map_sz, &ctx->conf->cookie_reset_map, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_URL, CONF_STRING_MAP, &ctx->conf->not_enforced_map_sz, &ctx->conf->not_enforced_map, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_INVERT, CONF_NUMBER, NULL, &ctx->conf->not_enforced_invert, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_ATTR, CONF_NUMBER, NULL, &ctx->conf->not_enforced_fetch_attr, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_NOT_ENFORCED_IP, CONF_STRING_MAP, &ctx->conf->not_enforced_ip_map_sz, &ctx->conf->not_enforced_ip_map, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_EXT_NOT_ENFORCED_URL, CONF_STRING_MAP, &ctx->conf->not_enforced_ext_map_sz, &ctx->conf->not_enforced_ext_map, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_PDP_ENABLE, CONF_NUMBER, NULL, &ctx->conf->pdp_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_PDP_VALID, CONF_NUMBER, NULL, &ctx->conf->pdp_cache_valid, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_PDP_COOKIE, CONF_STRING, NULL, &ctx->conf->pdp_lb_cookie, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_CLIENT_IP_VALIDATE, CONF_NUMBER, NULL, &ctx->conf->client_ip_validate, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_COOKIE_PREFIX, CONF_STRING, NULL, &ctx->conf->cookie_prefix, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_ATTR_COOKIE_MAX_AGE, CONF_NUMBER, NULL, &ctx->conf->cookie_maxage, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_CDSSO_ENABLE, CONF_NUMBER, NULL, &ctx->conf->cdsso_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CDSSO_LOGIN, CONF_STRING_MAP, &ctx->conf->cdsso_login_map_sz, &ctx->conf->cdsso_login_map, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CDSSO_DOMAIN, CONF_STRING_MAP, &ctx->conf->cdsso_cookie_domain_map_sz, &ctx->conf->cdsso_cookie_domain_map, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_LOGOUT_URL, CONF_STRING_MAP, &ctx->conf->openam_logout_map_sz, &ctx->conf->openam_logout_map, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_APP_LOGOUT_URL, CONF_STRING_MAP, &ctx->conf->logout_map_sz, &ctx->conf->logout_map, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_LOGOUT_REDIRECT_URL, CONF_STRING, NULL, &ctx->conf->logout_redirect_url, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_LOGOUT_COOKIE_RESET, CONF_STRING_MAP, &ctx->conf->logout_cookie_reset_map_sz, &ctx->conf->logout_cookie_reset_map, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_POLICY_SCOPE, CONF_NUMBER, NULL, &ctx->conf->policy_scope_subtree, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_RESOLVE_CLIENT_HOST, CONF_NUMBER, NULL, &ctx->conf->resolve_client_host, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_POLICY_ENCODE_SPECIAL_CHAR, CONF_NUMBER, NULL, &ctx->conf->policy_eval_encode_chars, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_COOKIE_ENCODE_SPECIAL_CHAR, CONF_NUMBER, NULL, &ctx->conf->cookie_encode_chars, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_OVERRIDE_PROTO, CONF_NUMBER, NULL, &ctx->conf->override_protocol, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_OVERRIDE_HOST, CONF_NUMBER, NULL, &ctx->conf->override_host, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_OVERRIDE_PORT, CONF_NUMBER, NULL, &ctx->conf->override_port, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_OVERRIDE_NOTIFICATION_URL, CONF_NUMBER, NULL, &ctx->conf->override_notif_url, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_VALID, CONF_NUMBER, NULL, &ctx->conf->config_valid, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_PASSWORD_REPLAY_KEY, CONF_STRING, NULL, &ctx->conf->password_replay_key, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_POLICY_CLOCK_SKEW, CONF_NUMBER, NULL, &ctx->conf->policy_clock_skew, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_GOTO_PARAM_NAME, CONF_STRING, NULL, &ctx->conf->url_redirect_param, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_CACHE_CONTROL_ENABLE, CONF_NUMBER, NULL, &ctx->conf->cache_control_enable, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_USE_REDIRECT_ADVICE, CONF_NUMBER, NULL, &ctx->conf->use_redirect_for_advice, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_CLIENT_IP_HEADER, CONF_STRING, NULL, &ctx->conf->client_ip_header, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CLIENT_HOSTNAME_HEADER, CONF_STRING, NULL, &ctx->conf->client_hostname_header, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_INVALID_URL, CONF_STRING, NULL, &ctx->conf->url_check_regex, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_CONDITIONAL_LOGIN_URL, CONF_STRING_MAP, &ctx->conf->cond_login_url_sz, &ctx->conf->cond_login_url, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_COOKIE_HTTP_ONLY, CONF_NUMBER, NULL, &ctx->conf->cookie_http_only, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_MULTI_VALUE_SEPARATOR, CONF_STRING, NULL, &ctx->conf->multi_attr_separator, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_IIS_LOGON_USER, CONF_NUMBER, NULL, &ctx->conf->logon_user_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_IIS_PASSWORD_HEADER, CONF_NUMBER, NULL, &ctx->conf->password_header_enable, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_PDP_JS_REPOST, CONF_NUMBER, NULL, &ctx->conf->pdp_js_repost, val, len);
}

static void character_data(void *userData, const char *val, int len) {
    char *v, *t, k[AM_URI_SIZE];
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    if (!ISVALID(ctx->current_name) || ctx->setting_value == 0 || len <= 0 ||
            strncmp(val, "[]=", len) == 0 || strncmp(val, "[0]=", len) == 0) return;

    /* bootstrap options */

    parse_config_value(ctx, AM_AGENTS_CONFIG_LOCAL, CONF_NUMBER, NULL, &ctx->conf->local, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_POSTDATA_PRESERVE_DIR, CONF_STRING, NULL, &ctx->conf->pdp_dir, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_NAMING_URL, CONF_STRING_LIST, &ctx->conf->naming_url_sz, &ctx->conf->naming_url, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_REALM, CONF_STRING, NULL, &ctx->conf->realm, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_USER, CONF_STRING, NULL, &ctx->conf->user, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_PASSWORD, CONF_STRING, NULL, &ctx->conf->pass, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_KEY, CONF_STRING, NULL, &ctx->conf->key, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_DEBUG_FILE, CONF_STRING, NULL, &ctx->conf->debug_file, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_DEBUG_LEVEL, CONF_DEBUG_LEVEL, NULL, &ctx->conf->debug_level, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_DEBUG_OPT, CONF_NUMBER, NULL, &ctx->conf->debug, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_AUDIT_FILE, CONF_STRING, NULL, &ctx->conf->audit_file, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_AUDIT_LEVEL, CONF_AUDIT_LEVEL, NULL, &ctx->conf->audit_level, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_AUDIT_OPT, CONF_NUMBER, NULL, &ctx->conf->audit, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_CERT_KEY_FILE, CONF_STRING, NULL, &ctx->conf->cert_key_file, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CERT_KEY_PASSWORD, CONF_STRING, NULL, &ctx->conf->cert_key_pass, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CERT_FILE, CONF_STRING, NULL, &ctx->conf->cert_file, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CA_FILE, CONF_STRING, NULL, &ctx->conf->cert_ca_file, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_CIPHERS, CONF_STRING, NULL, &ctx->conf->ciphers, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_TRUST_CERT, CONF_NUMBER, NULL, &ctx->conf->cert_trust, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_TLS_OPT, CONF_STRING, NULL, &ctx->conf->tls_opts, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_NET_TIMEOUT, CONF_NUMBER, NULL, &ctx->conf->net_timeout, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_LEVEL, CONF_NUMBER, NULL, &ctx->conf->valid_level, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_PING_INTERVAL, CONF_NUMBER, NULL, &ctx->conf->valid_ping, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_PING_MISS, CONF_NUMBER, NULL, &ctx->conf->valid_ping_miss, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_PING_OK, CONF_NUMBER, NULL, &ctx->conf->valid_ping_ok, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_URL_VALIDATE_DEFAULT_SET, CONF_NUMBER_LIST, &ctx->conf->valid_default_url_sz, &ctx->conf->valid_default_url, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_HOST_MAP, CONF_STRING_LIST, &ctx->conf->hostmap_sz, &ctx->conf->hostmap, val, len);

    parse_config_value(ctx, AM_AGENTS_CONFIG_RETRY_MAX, CONF_NUMBER, NULL, &ctx->conf->retry_max, val, len);
    parse_config_value(ctx, AM_AGENTS_CONFIG_RETRY_WAIT, CONF_NUMBER, NULL, &ctx->conf->retry_wait, val, len);

    /* other options */

    if (strcmp(ctx->current_name, "com.sun.identity.agents.config.freeformproperties") != 0) {
        parse_other_options(ctx, val, len);
        return;
    }

    /*handler for old freeformproperties*/
    v = strndup(val, len);
    if (v == NULL) return;
    /* make up parser's current_name to handle freeformproperties:
     * instead of a property key being supplied as an <attribute name="...">,
     * it is sent as a part of <value> element, for example:
     * <value>com.forgerock.agents.conditional.login.url[0]=signin.example.com|http...</value>
     */
    memset(&k[0], 0, sizeof (k));
    if ((t = strchr(v, '[')) != NULL) {
        memcpy(k, v, t - v);
    } else if ((t = strchr(v, '=')) != NULL) {
        memcpy(k, v, t - v);
        trim(k, ' ');
        t++; /*move past the '='*/
    }
    if (ISVALID(k) && t != NULL) {
        am_xml_parser_ctx_t f;
        size_t l = strlen(t);
        f.conf = ctx->conf;
        f.setting_value = ctx->setting_value;
        f.rgx = ctx->rgx;
        f.parser = ctx->parser;
        f.log_enable = ctx->log_enable;
        memcpy(f.current_name, k, sizeof (k));
        parse_other_options(&f, t, (int) l);
    }
    free(v);
}

static void entity_declaration(void *userData, const XML_Char *entityName,
        int is_parameter_entity, const XML_Char *value, int value_length, const XML_Char *base,
        const XML_Char *systemId, const XML_Char *publicId, const XML_Char *notationName) {
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    XML_StopParser(ctx->parser, XML_FALSE);
}

am_config_t *am_parse_config_xml(unsigned long instance_id, const char *xml, size_t xml_sz, char log_enable) {
    static const char *thisfunc = "am_parse_config_xml():";
    am_config_t *r = NULL;
    char *begin, *stream = NULL;
    size_t data_sz;
    pcre *x = NULL;
    const char *error = NULL;
    int erroroffset;

    am_xml_parser_ctx_t xctx = {.setting_value = 0,
        .conf = NULL, .rgx = NULL, .parser = NULL, .log_enable = log_enable};

    if (xml == NULL || xml_sz == 0) {
        AM_LOG_ERROR(instance_id, "%s memory allocation error", thisfunc);
        return NULL;
    }

    /*match [key]=value returned within <value>[key]=value_of_a_key</value> element*/
    x = pcre_compile("(?<=\\[)(.+?)(?=\\])\\]\\s*\\=\\s*(.+)", 0, &error, &erroroffset, NULL);
    if (x == NULL) {
        AM_LOG_ERROR(instance_id, "%s pcre error %s", thisfunc, error == NULL ? "" : error);
    }

    r = calloc(1, sizeof (am_config_t));
    if (r == NULL) {
        AM_LOG_ERROR(instance_id, "%s memory allocation error", thisfunc);
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
            AM_LOG_ERROR(instance_id, "%s xml parser error (%d:%d) %s", thisfunc,
                    line, col, message);
            am_config_free(&r);
            r = NULL;
        } else {
            r->ts = time(NULL);
        }
        XML_ParserFree(parser);
    }

    pcre_free(x);

    decrypt_agent_passwords(r);
    return r;
}
