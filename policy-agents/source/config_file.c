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

/*
 * Agent configuration file parser (local configuration)
 */

enum {
    NUMBER = 0, STRING, NUMBER_LIST, STRING_LIST, DEBUG_LEVEL, ATTR_MODE, AUDIT_LEVEL
};

struct val_string_list {
    int size;
    char **list;
};

struct val_number_list {
    int size;
    int *list;
};

static void *parse_value(const char *line, const char *name,
        int value_type, const char *mvsep) {
    char set = 0, map = 0, *token = NULL, *key = NULL, *key_val = NULL;
    void *value = NULL;
    size_t token_sz, name_sz = strlen(name);
    char *orig, *tn, *tmp = strdup(line);
    size_t line_sz;

    if (tmp == NULL) return NULL;
    orig = tmp;
    line_sz = strlen(tmp);

    tn = strchr(tmp, '=');
    if (tn != NULL && ((size_t) (tn - tmp)) < line_sz) {

        token = tn + 1; /*move past the '='*/
        *tn = 0; /*terminate token key and reset the pointer*/
        tn = tmp;

        trim(tn, ' ');
        trim(token, ' ');

        /*check if the key is what we're looking for*/
        if (strncmp(tn, name, name_sz) == 0) {
            map = 0;
            if ((token_sz = strlen(tn)) != name_sz) {
                /*get map value key*/
                key = strstr(tn, "[");
                if (key != NULL) {
                    key = key + 1;
                }
                if (key != NULL && *key != ']') {
                    tn[token_sz - 1] = 0;
                    key_val = malloc(strlen(key) + 1);
                    if (key_val != NULL) {
                        strcpy(key_val, key);
                        map = 1;
                    }
                }
            }
            set = 1;
        }

        if (set == 1 && ISVALID(token)) {
            switch (value_type) {
                case NUMBER:
                    value = (int *) malloc(sizeof (int));
                    if (strcasecmp(token, "on") == 0 || strcasecmp(token, "true") == 0 || strcasecmp(token, "local") == 0) {
                        *((int *) value) = 1;
                    } else if (strcasecmp(token, "off") == 0 || strcasecmp(token, "false") == 0 || strcasecmp(token, "centralized") == 0) {
                        *((int *) value) = 0;
                    } else {
                        *((int *) value) = strtol(token, NULL, 10);
                    }
                    break;
                case DEBUG_LEVEL:
                    value = (int *) malloc(sizeof (int));
                    if (strncasecmp(token, "all", 3) == 0) {
                        *((int *) value) = AM_LOG_DEBUG;
                    } else if (strcasecmp(token, "error") == 0) {
                        *((int *) value) = AM_LOG_ERROR;
                    } else if (strcasecmp(token, "info") == 0) {
                        *((int *) value) = AM_LOG_INFO;
                    } else if (strcasecmp(token, "message") == 0) {
                        *((int *) value) = AM_LOG_WARNING;
                    } else if (strcasecmp(token, "warning") == 0) {
                        *((int *) value) = AM_LOG_WARNING;
                    } else {
                        *((int *) value) = AM_LOG_NONE;
                    }
                    break;
                case ATTR_MODE:
                    value = (int *) malloc(sizeof (int));
                    if (strcasecmp(token, "HTTP_HEADER") == 0) {
                        *((int *) value) = SET_ATTRS_AS_HEADER;
                    } else if (strcasecmp(token, "HTTP_COOKIE") == 0) {
                        *((int *) value) = SET_ATTRS_AS_COOKIE;
                    } else {
                        *((int *) value) = SET_ATTRS_NONE;
                    }
                    break;
                case AUDIT_LEVEL:
                    value = (int *) malloc(sizeof (int));
                    if (strcasecmp(token, "LOG_ALLOW") == 0) {
                        *((int *) value) |= AM_LOG_AUDIT_ALLOW;
                    } else if (strcasecmp(token, "LOG_BOTH") == 0) {
                        *((int *) value) |= AM_LOG_AUDIT_ALLOW;
                        *((int *) value) |= AM_LOG_AUDIT_DENY;
                    } else if (strcasecmp(token, "LOG_DENY") == 0) {
                        *((int *) value) |= AM_LOG_AUDIT_DENY;
                    } else if (strcasecmp(token, "ALL") == 0) {
                        *((int *) value) |= AM_LOG_AUDIT;
                        *((int *) value) |= AM_LOG_AUDIT_REMOTE;
                    } else if (strcasecmp(token, "LOCAL") == 0) {
                        *((int *) value) |= AM_LOG_AUDIT;
                    } else if (strcasecmp(token, "REMOTE") == 0) {
                        *((int *) value) |= AM_LOG_AUDIT_REMOTE;
                    }
                    break;
                case STRING:
                {
                    if (map == 1) {
                        size_t l0 = strlen(token);
                        size_t l1 = strlen(key_val);
                        /*value is stored as:
                         * key\0value\0
                         */
                        value = malloc(l0 + l1 + 2);
                        if (value == NULL) {
                            break;
                        }
                        memcpy(value, key_val, l1);
                        ((char *) value)[l1] = 0;
                        memcpy((char *) value + l1 + 1, token, l0);
                        ((char *) value)[l0 + l1 + 1] = 0;
                        free(key_val);
                        key_val = NULL;
                    } else {
                        size_t l = strlen(token);
                        value = malloc(l + 1);
                        if (value == NULL) {
                            break;
                        }
                        memcpy(value, token, l);
                        ((char *) value)[l] = 0;
                    }
                }
                    break;
                case STRING_LIST:
                {
                    struct val_string_list *ret = NULL;
                    char *sl_token = NULL, *o, *sl_tmp = strdup(token);
                    char **vl = NULL;
                    int i = 0, vl_sz = 0;
                    size_t l;
                    if (sl_tmp == NULL) break;
                    o = sl_tmp;
                    while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                        trim(sl_token, ' ');
                        if (!sl_token || sl_token[0] == '\0') continue;
                        vl_sz++;
                    }
                    free(o);
                    if (vl_sz == 0) break;
                    sl_tmp = strdup(token);
                    if (sl_tmp == NULL) break;
                    o = sl_tmp;
                    vl = malloc(sizeof (char *) * vl_sz);
                    while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                        trim(sl_token, ' ');
                        if (!sl_token || sl_token[0] == '\0') continue;
                        l = strlen(sl_token);
                        vl[i] = malloc(l + 1);
                        if (vl[i] == NULL) break;
                        memcpy(vl[i], sl_token, l);
                        vl[i][l] = 0;
                        i++;
                    }
                    free(o);

                    ret = malloc(sizeof (struct val_string_list));
                    if (ret == NULL) {
                        break;
                    }
                    ret->size = vl_sz;
                    ret->list = vl;
                    value = ret;
                }
                    break;
                case NUMBER_LIST:
                {
                    struct val_number_list *ret = NULL;
                    char *sl_token = NULL, *o, *sl_tmp = strdup(token);
                    int *vl = NULL;
                    int i = 0, vl_sz = 0;
                    if (sl_tmp == NULL) break;
                    o = sl_tmp;
                    while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                        trim(sl_token, ' ');
                        if (!sl_token || sl_token[0] == '\0') continue;
                        vl_sz++;
                    }
                    free(o);
                    if (vl_sz == 0) break;
                    sl_tmp = strdup(token);
                    if (sl_tmp == NULL) break;
                    o = sl_tmp;
                    vl = malloc(sizeof (int) * vl_sz);
                    while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                        trim(sl_token, ' ');
                        if (!sl_token || sl_token[0] == '\0') continue;
                        vl[i] = strtol(sl_token, NULL, 10);
                        i++;
                    }
                    free(o);

                    ret = malloc(sizeof (struct val_number_list));
                    if (ret == NULL) {
                        break;
                    }
                    ret->size = vl_sz;
                    ret->list = vl;
                    value = ret;
                }
                    break;
            }
        }
        if (key_val != NULL) free(key_val);
    }

    if (orig != NULL) free(orig);
    return value;
}

#define PARSE_STRING(line,prm,itm) \
    do {\
        if (compare_property(line,prm) != 0) break;\
        itm = (char *) parse_value(line, prm, \
            STRING, NULL);\
        if (strstr(prm, "password") != NULL) \
            am_log_debug(instance_id, "am_get_config_file() %s is set to '%s'",\
                prm, itm == NULL ? "NULL" : "********");\
        else \
            am_log_debug(instance_id, "am_get_config_file() %s is set to '%s'",\
                prm, itm == NULL ? "NULL" : itm);\
    } while(0)

#define PARSE_NUMBER(line,prm,itm) \
    do {\
        int *value = NULL;\
        if (compare_property(line,prm) != 0) break;\
        value = (int *) parse_value(line, prm, \
            NUMBER, NULL);\
        itm = value == NULL ? 0 : *value;\
        if (value != NULL) free(value);\
        am_log_debug(instance_id, "am_get_config_file() %s is set to '%d'",\
            prm, itm);\
    } while (0)

#define PARSE_DEBUG_LOG_LEVEL(line,prm,itm) \
    do {\
        int *value = NULL;\
        if (compare_property(line,prm) != 0) break;\
        value = (int *) parse_value(line, prm, \
            DEBUG_LEVEL, NULL);\
        itm = value == NULL ? 0 : *value;\
        if (value != NULL) free(value);\
        am_log_debug(instance_id, "am_get_config_file() %s is set to '%d'",\
            prm, itm);\
    } while (0)

#define PARSE_ATTR_FETCH_MODE(line,prm,itm) \
    do {\
        int *value = NULL;\
        if (compare_property(line,prm) != 0) break;\
        value = (int *) parse_value(line, prm, \
            ATTR_MODE, NULL);\
        itm = value == NULL ? 0 : *value;\
        if (value != NULL) free(value);\
        am_log_debug(instance_id, "am_get_config_file() %s is set to '%d'",\
            prm, itm);\
    } while (0)

#define PARSE_AUDIT_LOG_LEVEL(line,prm,itm) \
    do {\
        int *value = NULL;\
        if (compare_property(line,prm) != 0) break;\
        value = (int *) parse_value(line, prm, \
            AUDIT_LEVEL, NULL);\
        itm = value == NULL ? 0 : *value;\
        if (value != NULL) free(value);\
        am_log_debug(instance_id, "am_get_config_file() %s is set to '%d'",\
            prm, itm);\
    } while (0)

#define PARSE_STRING_LIST(line,prm,sep,itmsz,itm) \
    do {\
        struct val_string_list *value = NULL;\
        if (compare_property(line,prm) != 0 || sep == NULL) break;\
        value = (struct val_string_list *) parse_value(line, prm, \
            STRING_LIST, sep);\
        itmsz = value == NULL ? 0 : value->size;\
        itm = value == NULL ? NULL : value->list;\
        if (value != NULL) free(value);\
        am_log_debug(instance_id, "am_get_config_file() %s is set to %d value(s)",\
            prm, itmsz);\
    } while (0)

#define PARSE_NUMBER_LIST(line,prm,sep,itmsz,itm) \
    do {\
        struct val_number_list *value = NULL;\
        if (compare_property(line,prm) != 0 || sep == NULL) break;\
        value = (struct val_number_list *) parse_value(line, prm, \
            NUMBER_LIST, sep);\
        itmsz = value == NULL ? 0 : value->size;\
        itm = value == NULL ? NULL : value->list;\
        if (value != NULL) free(value);\
        am_log_debug(instance_id, "am_get_config_file() %s is set to %d value(s)",\
            prm, itmsz);\
    } while (0)

#define PARSE_STRING_MAP(line,prm,itmsz,itm) \
    do {\
        char *value = NULL; int old_sz = itmsz;\
        if (compare_property(line,prm) != 0) break;\
        value = (char *) parse_value(line, prm, \
            STRING, NULL);\
        if (value == NULL) break;\
        itm = (am_config_map_t *) realloc(itm, \
            sizeof(am_config_map_t) *(++(itmsz)));\
        if (itm == NULL) {itmsz--; if (itmsz<0) itmsz = 0; free(value); break;}\
        (&itm[old_sz])->name = value;\
        (&itm[old_sz])->value = value + strlen(value) + 1;\
        am_log_debug(instance_id, "am_get_config_file() %s is set to %d value(s)",\
            prm, itmsz);\
    } while (0)

am_config_t *am_get_config_file(unsigned long instance_id, const char *filename) {
    const char *thisfunc = "am_get_config_file():";
    am_config_t *r = NULL;
    FILE *file = NULL;
    char *line = NULL;
    size_t len = 0;
    ssize_t read;

    r = calloc(1, sizeof (am_config_t));
    if (r == NULL) {
        am_log_error(instance_id, "%s memory allocation error", thisfunc);
        return NULL;
    }
    r->instance_id = instance_id;

    file = fopen(filename, "r");
    if (file == NULL) {
        am_log_error(instance_id,
                "%s can't open file %s (error: %d)", thisfunc,
                filename, errno);
        if (r != NULL) free(r);
        return NULL;
    }

    while ((read = get_line(&line, &len, file)) != -1) {
        if (!line || line[0] == '#') continue;
        trim(line, '\n');
        trim(line, '\r');
        trim(line, ' ');
        if (!line || line[0] == '\0' || line[0] == '#') continue;

        /* bootstrap options */

        PARSE_NUMBER(line, AM_AGENTS_CONFIG_LOCAL, r->local); /*must be the 1st option*/
        PARSE_STRING(line, AM_AGENTS_CONFIG_POSTDATA_PRESERVE_DIR, r->pdp_dir);
        PARSE_STRING_LIST(line, AM_AGENTS_CONFIG_NAMING_URL, " ", r->naming_url_sz, r->naming_url);
        PARSE_STRING(line, AM_AGENTS_CONFIG_REALM, r->realm);
        PARSE_STRING(line, AM_AGENTS_CONFIG_USER, r->user);
        PARSE_STRING(line, AM_AGENTS_CONFIG_PASSWORD, r->pass);
        PARSE_STRING(line, AM_AGENTS_CONFIG_KEY, r->key);
        PARSE_NUMBER(line, AM_AGENTS_CONFIG_DEBUG_OPT, r->debug);
        PARSE_STRING(line, AM_AGENTS_CONFIG_DEBUG_FILE, r->debug_file);
        PARSE_DEBUG_LOG_LEVEL(line, AM_AGENTS_CONFIG_DEBUG_LEVEL, r->debug_level);
        PARSE_STRING(line, AM_AGENTS_CONFIG_AUDIT_FILE, r->audit_file);
        PARSE_AUDIT_LOG_LEVEL(line, AM_AGENTS_CONFIG_AUDIT_LEVEL, r->audit_level);
        PARSE_NUMBER(line, AM_AGENTS_CONFIG_AUDIT_OPT, r->audit);

        PARSE_STRING(line, AM_AGENTS_CONFIG_CERT_KEY_FILE, r->cert_key_file);
        PARSE_STRING(line, AM_AGENTS_CONFIG_CERT_KEY_PASSWORD, r->cert_key_pass); //TODO: decrypt
        PARSE_STRING(line, AM_AGENTS_CONFIG_CERT_FILE, r->cert_file);
        PARSE_STRING(line, AM_AGENTS_CONFIG_CA_FILE, r->cert_ca_file);
        PARSE_STRING(line, AM_AGENTS_CONFIG_CIPHERS, r->ciphers);
        PARSE_NUMBER(line, AM_AGENTS_CONFIG_TRUST_CERT, r->cert_trust);
        PARSE_STRING(line, AM_AGENTS_CONFIG_TLS_OPT, r->tls_opts);

        PARSE_NUMBER(line, AM_AGENTS_CONFIG_NET_TIMEOUT, r->net_timeout);

        PARSE_NUMBER(line, AM_AGENTS_CONFIG_URL_VALIDATE_LEVEL, r->valid_level);
        PARSE_NUMBER(line, AM_AGENTS_CONFIG_URL_VALIDATE_PING_INTERVAL, r->valid_ping);
        PARSE_NUMBER(line, AM_AGENTS_CONFIG_URL_VALIDATE_PING_MISS, r->valid_ping_miss);
        PARSE_NUMBER(line, AM_AGENTS_CONFIG_URL_VALIDATE_PING_OK, r->valid_ping_ok);
        PARSE_NUMBER_LIST(line, AM_AGENTS_CONFIG_URL_VALIDATE_DEFAULT_SET, " ", r->valid_default_url_sz, r->valid_default_url);

        /*
         * com.forgerock.agents.config.hostmap format:
         *  server1.domain.name|192.168.1.1,server2.domain.name|192.168.1.2
         */
        PARSE_STRING_LIST(line, AM_AGENTS_CONFIG_HOST_MAP, ",", r->hostmap_sz, r->hostmap);

        PARSE_NUMBER(line, AM_AGENTS_CONFIG_RETRY_MAX, r->retry_max);
        PARSE_NUMBER(line, AM_AGENTS_CONFIG_RETRY_WAIT, r->retry_wait);

        if (r->local == AM_TRUE) { /*do read other options in case configuration is local*/

            /* other options */

            PARSE_STRING(line, AM_AGENTS_CONFIG_AGENT_URI, r->agenturi);
            PARSE_STRING(line, AM_AGENTS_CONFIG_COOKIE_NAME, r->cookie_name);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_LOGIN_URL_MAP, r->login_url_sz, r->login_url);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_COOKIE_SECURE, r->cookie_secure);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_NOTIF_ENABLE, r->notif_enable);
            PARSE_STRING(line, AM_AGENTS_CONFIG_NOTIF_URL, r->notif_url);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_CMP_CASE_IGNORE, r->url_eval_case_ignore);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_POLICY_CACHE_VALID, r->policy_cache_valid);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_TOKEN_CACHE_VALID, r->token_cache_valid);
            PARSE_STRING(line, AM_AGENTS_CONFIG_UID_PARAM, r->userid_param);
            PARSE_STRING(line, AM_AGENTS_CONFIG_UID_PARAM_TYPE, r->userid_param_type);

            PARSE_ATTR_FETCH_MODE(line, AM_AGENTS_CONFIG_ATTR_PROFILE_MODE, r->profile_attr_fetch);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_ATTR_PROFILE_MAP, r->profile_attr_map_sz, r->profile_attr_map);
            PARSE_ATTR_FETCH_MODE(line, AM_AGENTS_CONFIG_ATTR_SESSION_MODE, r->session_attr_fetch);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_ATTR_SESSION_MAP, r->session_attr_map_sz, r->session_attr_map);
            PARSE_ATTR_FETCH_MODE(line, AM_AGENTS_CONFIG_ATTR_RESPONSE_MODE, r->response_attr_fetch);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_ATTR_RESPONSE_MAP, r->response_attr_map_sz, r->response_attr_map);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_LB_ENABLE, r->lb_enable);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_SSO_ONLY, r->sso_only);
            PARSE_STRING(line, AM_AGENTS_CONFIG_ACCESS_DENIED_URL, r->access_denied_url);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_FQDN_CHECK_ENABLE, r->fqdn_check_enable);
            PARSE_STRING(line, AM_AGENTS_CONFIG_FQDN_DEFAULT, r->fqdn_default);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_FQDN_MAP, r->fqdn_map_sz, r->fqdn_map);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_COOKIE_RESET_ENABLE, r->cookie_reset_enable);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_COOKIE_RESET_MAP, r->cookie_reset_map_sz, r->cookie_reset_map);

            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_NOT_ENFORCED_URL, r->not_enforced_map_sz, r->not_enforced_map);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_NOT_ENFORCED_INVERT, r->not_enforced_invert);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_NOT_ENFORCED_ATTR, r->not_enforced_fetch_attr);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_NOT_ENFORCED_IP, r->not_enforced_ip_map_sz, r->not_enforced_ip_map);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_PDP_ENABLE, r->pdp_enable);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_PDP_VALID, r->pdp_cache_valid);
            PARSE_STRING(line, AM_AGENTS_CONFIG_PDP_COOKIE, r->pdp_lb_cookie);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_CLIENT_IP_VALIDATE, r->client_ip_validate);
            PARSE_STRING(line, AM_AGENTS_CONFIG_ATTR_COOKIE_PREFIX, r->cookie_prefix);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_ATTR_COOKIE_MAX_AGE, r->cookie_maxage);


            PARSE_NUMBER(line, AM_AGENTS_CONFIG_CDSSO_ENABLE, r->cdsso_enable);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_CDSSO_LOGIN, r->cdsso_login_map_sz, r->cdsso_login_map);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_CDSSO_DOMAIN, r->cdsso_cookie_domain_map_sz, r->cdsso_cookie_domain_map);

            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_LOGOUT_URL, r->openam_logout_map_sz, r->openam_logout_map);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_APP_LOGOUT_URL, r->logout_map_sz, r->logout_map);
            PARSE_STRING(line, AM_AGENTS_CONFIG_LOGOUT_REDIRECT_URL, r->logout_redirect_url);
            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_LOGOUT_COOKIE_RESET, r->logout_cookie_reset_map_sz, r->logout_cookie_reset_map);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_POLICY_SCOPE, r->policy_scope_subtree);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_RESOLVE_CLIENT_HOST, r->resolve_client_host);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_POLICY_ENCODE_SPECIAL_CHAR, r->policy_eval_encode_chars);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_COOKIE_ENCODE_SPECIAL_CHAR, r->cookie_encode_chars);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_OVERRIDE_PROTO, r->override_protocol);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_OVERRIDE_HOST, r->override_host);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_OVERRIDE_PORT, r->override_port);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_OVERRIDE_NOTIFICATION_URL, r->override_notif_url);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_VALID, r->config_valid);

            PARSE_STRING(line, AM_AGENTS_CONFIG_PASSWORD_REPLAY_KEY, r->password_replay_key);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_POLICY_CLOCK_SKEW, r->policy_clock_skew);

            PARSE_STRING(line, AM_AGENTS_CONFIG_GOTO_PARAM_NAME, r->url_redirect_param);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_CACHE_CONTROL_ENABLE, r->cache_control_enable);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_USE_REDIRECT_ADVICE, r->use_redirect_for_advice);

            PARSE_STRING(line, AM_AGENTS_CONFIG_CLIENT_IP_HEADER, r->client_ip_header);
            PARSE_STRING(line, AM_AGENTS_CONFIG_CLIENT_HOSTNAME_HEADER, r->client_hostname_header);

            PARSE_STRING(line, AM_AGENTS_CONFIG_INVALID_URL, r->url_check_regex);

            PARSE_STRING_MAP(line, AM_AGENTS_CONFIG_CONDITIONAL_LOGIN_URL, r->cond_login_url_sz, r->cond_login_url);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_COOKIE_HTTP_ONLY, r->cookie_http_only);
            PARSE_STRING(line, AM_AGENTS_CONFIG_MULTI_VALUE_SEPARATOR, r->multi_attr_separator);

            PARSE_NUMBER(line, AM_AGENTS_CONFIG_IIS_LOGON_USER, r->logon_user_enable);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_IIS_PASSWORD_HEADER, r->password_header_enable);
            PARSE_NUMBER(line, AM_AGENTS_CONFIG_PDP_JS_REPOST, r->pdp_js_repost);
        }
    }

    r->ts = time(NULL);

    fclose(file);
    if (line != NULL) free(line);
    return r;
}

#define AM_CONF_FREE(sz,el) \
    do {\
        int i;\
        if (el != NULL) {\
            for (i = 0; i < sz; i++) {\
               void *v = (el)[i];\
               if (v) free(v);\
               v = NULL;\
            }\
            free(el);\
            el = NULL;\
        }\
    } while (0)

void am_config_free(am_config_t **cp) {
    if (cp != NULL && *cp != NULL) {
        am_config_t *c = *cp;

        if (c->token) free(c->token);
        if (c->config) free(c->config);

        /* bootstrap options */

        if (c->pdp_dir) free(c->pdp_dir);
        AM_CONF_FREE(c->naming_url_sz, c->naming_url);
        if (c->realm) free(c->realm);
        if (c->user) free(c->user);
        if (c->pass) free(c->pass);
        if (c->key) free(c->key);

        if (c->debug_file) free(c->debug_file);
        if (c->audit_file) free(c->audit_file);

        if (c->cert_key_file) free(c->cert_key_file);
        if (c->cert_key_pass) free(c->cert_key_pass);
        if (c->cert_file) free(c->cert_file);
        if (c->cert_ca_file) free(c->cert_ca_file);
        if (c->ciphers) free(c->ciphers);
        if (c->tls_opts) free(c->tls_opts);

        AM_CONF_FREE(c->hostmap_sz, c->hostmap);
        if (c->valid_default_url) free(c->valid_default_url);

        /* other options */

        if (c->agenturi) free(c->agenturi);
        if (c->cookie_name) free(c->cookie_name);
        AM_CONF_MAP_FREE(c->login_url_sz, c->login_url);

        if (c->notif_url) free(c->notif_url);

        if (c->userid_param) free(c->userid_param);
        if (c->userid_param_type) free(c->userid_param_type);

        AM_CONF_MAP_FREE(c->profile_attr_map_sz, c->profile_attr_map);
        AM_CONF_MAP_FREE(c->session_attr_map_sz, c->session_attr_map);
        AM_CONF_MAP_FREE(c->response_attr_map_sz, c->response_attr_map);

        if (c->access_denied_url) free(c->access_denied_url);
        if (c->fqdn_default) free(c->fqdn_default);

        AM_CONF_MAP_FREE(c->fqdn_map_sz, c->fqdn_map);
        AM_CONF_MAP_FREE(c->cookie_reset_map_sz, c->cookie_reset_map);
        AM_CONF_MAP_FREE(c->not_enforced_map_sz, c->not_enforced_map);
        AM_CONF_MAP_FREE(c->not_enforced_ip_map_sz, c->not_enforced_ip_map);

        if (c->pdp_lb_cookie) free(c->pdp_lb_cookie);
        if (c->cookie_prefix) free(c->cookie_prefix);

        AM_CONF_MAP_FREE(c->cdsso_login_map_sz, c->cdsso_login_map);
        AM_CONF_MAP_FREE(c->cdsso_cookie_domain_map_sz, c->cdsso_cookie_domain_map);
        AM_CONF_MAP_FREE(c->logout_cookie_reset_map_sz, c->logout_cookie_reset_map);
        if (c->logout_redirect_url) free(c->logout_redirect_url);

        AM_CONF_MAP_FREE(c->logout_map_sz, c->logout_map);
        AM_CONF_MAP_FREE(c->openam_logout_map_sz, c->openam_logout_map);

        if (c->password_replay_key) free(c->password_replay_key);
        if (c->url_redirect_param) free(c->url_redirect_param);
        if (c->client_ip_header) free(c->client_ip_header);
        if (c->client_hostname_header) free(c->client_hostname_header);
        if (c->url_check_regex) free(c->url_check_regex);

        AM_CONF_MAP_FREE(c->cond_login_url_sz, c->cond_login_url);
        if (c->multi_attr_separator) free(c->multi_attr_separator);

        free(c);
        c = NULL;
    }
}
