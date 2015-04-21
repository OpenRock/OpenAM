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
    int *value_int;

    if (tmp == NULL) {
        return NULL;
    }
    orig = tmp;
    line_sz = strlen(tmp);

    tn = strchr(tmp, '=');
    if (tn != NULL && ((size_t) (tn - tmp)) < line_sz) {

        token = tn + 1; /* move past the '=' */
        *tn = 0; /* terminate token key and reset the pointer */
        tn = tmp;

        trim(tn, ' ');
        trim(token, ' ');

        /* check if the key is what we're looking for */
        if (strncmp(tn, name, name_sz) == 0) {
            map = 0;
            if ((token_sz = strlen(tn)) != name_sz) {
                /*get map value key*/
                key = strstr(tn, "[");
                if (key != NULL) {
                    key++; /* move past the '[' */
                    if (*key != ']') {
                        tn[token_sz - 1] = 0;
                        key_val = strdup(key);
                        if (key_val != NULL) {
                            map = 1;
                        }
                    }
                }
            }
            set = 1;
        }

        if (set != 1 || !ISVALID(token)) {
            AM_FREE(key_val, orig);
            return NULL;
        }

        switch (value_type) {
            case CONF_NUMBER:
                value = malloc(sizeof (int));
                if (value == NULL) {
                    break;
                }
                value_int = (int *) value;
                if (strcasecmp(token, "on") == 0 || strcasecmp(token, "true") == 0 || strcasecmp(token, "local") == 0) {
                    *value_int = 1;
                    break;
                }
                if (strcasecmp(token, "off") == 0 || strcasecmp(token, "false") == 0 || strcasecmp(token, "centralized") == 0) {
                    *value_int = 0;
                    break;
                }
                *value_int = strtol(token, NULL, AM_BASE_TEN);
                break;
            case CONF_DEBUG_LEVEL:
                value = malloc(sizeof (int));
                if (value == NULL) {
                    break;
                }
                value_int = (int *) value;
                if (strncasecmp(token, "all", 3) == 0) {
                    *value_int = AM_LOG_LEVEL_DEBUG;
                    break;
                }
                if (strcasecmp(token, "error") == 0) {
                    *value_int = AM_LOG_LEVEL_ERROR;
                    break;
                }
                if (strcasecmp(token, "info") == 0) {
                    *value_int = AM_LOG_LEVEL_INFO;
                    break;
                }
                if (strcasecmp(token, "message") == 0) {
                    *value_int = AM_LOG_LEVEL_WARNING;
                    break;
                }
                if (strcasecmp(token, "warning") == 0) {
                    *value_int = AM_LOG_LEVEL_WARNING;
                    break;
                }
                *value_int = AM_LOG_LEVEL_NONE;
                break;
            case CONF_ATTR_MODE:
                value = malloc(sizeof (int));
                if (value == NULL) {
                    break;
                }
                value_int = (int *) value;
                if (strcasecmp(token, "HTTP_HEADER") == 0) {
                    *value_int = AM_SET_ATTRS_AS_HEADER;
                    break;
                }
                if (strcasecmp(token, "HTTP_COOKIE") == 0) {
                    *value_int = AM_SET_ATTRS_AS_COOKIE;
                    break;
                }
                *value_int = AM_SET_ATTRS_NONE;
                break;
            case CONF_AUDIT_LEVEL:
                value = calloc(1, sizeof (int));
                if (value == NULL) {
                    break;
                }
                value_int = (int *) value;
                if (strcasecmp(token, "LOG_ALLOW") == 0) {
                    *value_int |= AM_LOG_LEVEL_AUDIT_ALLOW;
                    break;
                }
                if (strcasecmp(token, "LOG_BOTH") == 0) {
                    *value_int |= AM_LOG_LEVEL_AUDIT_ALLOW;
                    *value_int |= AM_LOG_LEVEL_AUDIT_DENY;
                    break;
                }
                if (strcasecmp(token, "LOG_DENY") == 0) {
                    *value_int |= AM_LOG_LEVEL_AUDIT_DENY;
                    break;
                }
                if (strcasecmp(token, "ALL") == 0) {
                    *value_int |= AM_LOG_LEVEL_AUDIT;
                    *value_int |= AM_LOG_LEVEL_AUDIT_REMOTE;
                    break;
                }
                if (strcasecmp(token, "LOCAL") == 0) {
                    *value_int |= AM_LOG_LEVEL_AUDIT;
                    break;
                }
                if (strcasecmp(token, "REMOTE") == 0) {
                    *value_int |= AM_LOG_LEVEL_AUDIT_REMOTE;
                    break;
                }
                break;
            case CONF_STRING:
            {
                if (map == 1) {
                    size_t val_sz = strlen(token);
                    size_t key_sz = strlen(key_val);
                    /*value is stored as:
                     * key\0value\0
                     */
                    value = malloc(val_sz + key_sz + 2);
                    if (value == NULL) {
                        break;
                    }
                    memcpy(value, key_val, key_sz);
                    ((char *) value)[key_sz] = 0;
                    memcpy((char *) value + key_sz + 1, token, val_sz);
                    ((char *) value)[val_sz + key_sz + 1] = 0;
                    free(key_val);
                    key_val = NULL;
                    break;
                }
                value = strdup(token);
            }
                break;
            case CONF_STRING_LIST:
            {
                struct val_string_list *ret = NULL;
                char *sl_token = NULL, *o, *sl_tmp = strdup(token);
                char **vl = NULL;
                int i = 0, vl_sz = 0;
                if (sl_tmp == NULL) {
                    break;
                }
                o = sl_tmp;
                while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                    trim(sl_token, ' ');
                    if (!sl_token || sl_token[0] == '\0') {
                        continue;
                    }
                    vl_sz++;
                }
                free(o);
                if (vl_sz == 0) {
                    break;
                }
                sl_tmp = strdup(token);
                if (sl_tmp == NULL) {
                    break;
                }
                o = sl_tmp;
                vl = malloc(sizeof (char *) * vl_sz);
                if (vl == NULL) {
                    free(sl_tmp);
                    break;
                }
                while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                    trim(sl_token, ' ');
                    if (!sl_token || sl_token[0] == '\0') {
                        continue;
                    }
                    vl[i] = strdup(sl_token);
                    if (vl[i] == NULL) {
                        break;
                    }
                    i++;
                }
                free(o);

                ret = malloc(sizeof (struct val_string_list));
                if (ret == NULL) {
                    for (i = 0; i < vl_sz; i++) {
                        am_free(vl[i]);
                    }
                    free(vl);
                    break;
                }
                ret->size = vl_sz;
                ret->list = vl;
                value = ret;
            }
                break;
            case CONF_NUMBER_LIST:
            {
                struct val_number_list *ret = NULL;
                char *sl_token = NULL, *o, *sl_tmp = strdup(token);
                int *vl = NULL;
                int i = 0, vl_sz = 0;
                if (sl_tmp == NULL) {
                    break;
                }
                o = sl_tmp;
                while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                    trim(sl_token, ' ');
                    if (!sl_token || sl_token[0] == '\0') {
                        continue;
                    }
                    vl_sz++;
                }
                free(o);
                if (vl_sz == 0) {
                    break;
                }
                sl_tmp = strdup(token);
                if (sl_tmp == NULL) {
                    break;
                }
                o = sl_tmp;
                vl = malloc(sizeof (int) * vl_sz);
                if (vl == NULL) {
                    free(sl_tmp);
                    break;
                }
                while ((sl_token = am_strsep(&sl_tmp, mvsep)) != NULL) {
                    trim(sl_token, ' ');
                    if (!sl_token || sl_token[0] == '\0') {
                        continue;
                    }
                    vl[i] = strtol(sl_token, NULL, AM_BASE_TEN);
                    i++;
                }
                free(o);

                ret = malloc(sizeof (struct val_number_list));
                if (ret == NULL) {
                    free(vl);
                    break;
                }
                ret->size = vl_sz;
                ret->list = vl;
                value = ret;
            }
                break;
        }
        am_free(key_val);
    }
    am_free(orig);
    return value;
}

static void parse_config_value(unsigned long instance_id, const char *line, const char *prm, int type,
        int *itm_sz, void *itm, const char *sep) {

    if (itm == NULL || compare_property(line, prm) != AM_SUCCESS) {
        return;
    }

    switch (type) {
        case CONF_STRING:
        {
            char **value = (char **) itm;
            *value = (char *) parse_value(line, prm, type, NULL);
            if (strstr(prm, "password") != NULL) {
                AM_LOG_DEBUG(instance_id, "am_get_config_file() %s is set to '%s'",
                        prm, *value == NULL ? "NULL" : "********");
                break;
            }
            AM_LOG_DEBUG(instance_id, "am_get_config_file() %s is set to '%s'",
                    prm, *value == NULL ? "NULL" : *value);
        }
            break;
        case CONF_NUMBER:
        case CONF_DEBUG_LEVEL:
        case CONF_ATTR_MODE:
        case CONF_AUDIT_LEVEL:
        {
            int *value = (int *) itm;
            int *value_tmp = (int *) parse_value(line, prm, type, NULL);
            if (value_tmp != NULL) {
                *value = *value_tmp;
                free(value_tmp);
            }
            AM_LOG_DEBUG(instance_id, "am_get_config_file() %s is set to '%d'", prm, *value);
        }
            break;
        case CONF_STRING_LIST:
        {
            char ***value = (char ***) itm;
            struct val_string_list *value_tmp = (struct val_string_list *) parse_value(line, prm, type, sep);
            if (value_tmp != NULL) {
                *value = value_tmp->list;
                *itm_sz = value_tmp->size;
                free(value_tmp);
            }
            AM_LOG_DEBUG(instance_id, "am_get_config_file() %s is set to %d value(s)", prm, *itm_sz);
        }
            break;
        case CONF_NUMBER_LIST:
        {
            int **value = (int **) itm;
            struct val_number_list *value_tmp = (struct val_number_list *) parse_value(line, prm, type, sep);
            if (value_tmp != NULL) {
                *value = value_tmp->list;
                *itm_sz = value_tmp->size;
                free(value_tmp);
            }
            AM_LOG_DEBUG(instance_id, "am_get_config_file() %s is set to %d value(s)", prm, *itm_sz);
        }
            break;
        case CONF_STRING_MAP:
        {
            int old_sz = *itm_sz;
            am_config_map_t **value = (am_config_map_t **) itm;
            char *value_tmp = (char *) parse_value(line, prm, CONF_STRING, NULL);
            if (value_tmp == NULL) {
                break;
            }
            *value = (am_config_map_t *) realloc(*value, sizeof (am_config_map_t) *(++(*itm_sz)));
            if (*value == NULL) {
                if (--(*itm_sz) < 0) {
                    *itm_sz = 0;
                }
                free(value_tmp);
                break;
            }
            (&(*value)[old_sz])->name = value_tmp;
            (&(*value)[old_sz])->value = value_tmp + strlen(value_tmp) + 1;
            AM_LOG_DEBUG(instance_id, "am_get_config_file() %s is set to %d value(s)", prm, *itm_sz);
        }
            break;
        default:
            AM_LOG_WARNING(instance_id, "am_get_config_file() unknown type value %d setting %s", type, prm);
            break;
    }
}

am_config_t *am_get_config_file(unsigned long instance_id, const char *filename) {
    static const char *thisfunc = "am_get_config_file():";
    am_config_t *r = NULL;
    FILE *file = NULL;
    char *line = NULL;
    size_t len = 0;
    ssize_t read;

    r = calloc(1, sizeof (am_config_t));
    if (r == NULL) {
        AM_LOG_ERROR(instance_id, "%s memory allocation error", thisfunc);
        return NULL;
    }
    r->instance_id = instance_id;

    file = fopen(filename, "r");
    if (file == NULL) {
        AM_LOG_ERROR(instance_id,
                "%s can't open file %s (error: %d)", thisfunc,
                filename, errno);
        free(r);
        return NULL;
    }

    while ((read = get_line(&line, &len, file)) != -1) {
        trim(line, '\n');
        trim(line, '\r');
        trim(line, ' ');
        if (line == NULL || line[0] == '\0' || line[0] == '#') continue;

        /* bootstrap options */

        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_LOCAL, CONF_NUMBER, NULL, &r->local, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_POSTDATA_PRESERVE_DIR, CONF_STRING, NULL, &r->pdp_dir, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NAMING_URL, CONF_STRING_LIST, &r->naming_url_sz, &r->naming_url, AM_SPACE_CHAR);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_REALM, CONF_STRING, NULL, &r->realm, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_USER, CONF_STRING, NULL, &r->user, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_PASSWORD, CONF_STRING, NULL, &r->pass, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_KEY, CONF_STRING, NULL, &r->key, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_DEBUG_OPT, CONF_NUMBER, NULL, &r->debug, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_DEBUG_FILE, CONF_STRING, NULL, &r->debug_file, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_DEBUG_LEVEL, CONF_DEBUG_LEVEL, NULL, &r->debug_level, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_AUDIT_FILE, CONF_STRING, NULL, &r->audit_file, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_AUDIT_LEVEL, CONF_AUDIT_LEVEL, NULL, &r->audit_level, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_AUDIT_OPT, CONF_NUMBER, NULL, &r->audit, NULL);

        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CERT_KEY_FILE, CONF_STRING, NULL, &r->cert_key_file, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CERT_KEY_PASSWORD, CONF_STRING, NULL, &r->cert_key_pass, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CERT_FILE, CONF_STRING, NULL, &r->cert_file, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CA_FILE, CONF_STRING, NULL, &r->cert_ca_file, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CIPHERS, CONF_STRING, NULL, &r->ciphers, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_TRUST_CERT, CONF_STRING, NULL, &r->cert_trust, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_TLS_OPT, CONF_STRING, NULL, &r->tls_opts, NULL);

        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NET_TIMEOUT, CONF_NUMBER, NULL, &r->net_timeout, NULL);

        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_URL_VALIDATE_LEVEL, CONF_NUMBER, NULL, &r->valid_level, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_URL_VALIDATE_PING_INTERVAL, CONF_NUMBER, NULL, &r->valid_ping, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_URL_VALIDATE_PING_MISS, CONF_NUMBER, NULL, &r->valid_ping_miss, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_URL_VALIDATE_PING_OK, CONF_NUMBER, NULL, &r->valid_ping_ok, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_URL_VALIDATE_DEFAULT_SET, CONF_NUMBER_LIST, &r->valid_default_url_sz, &r->valid_default_url, AM_SPACE_CHAR);

        /*
         * com.forgerock.agents.config.hostmap format:
         *  server1.domain.name|192.168.1.1,server2.domain.name|192.168.1.2
         */
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_HOST_MAP, CONF_STRING_LIST, &r->hostmap_sz, &r->hostmap, AM_COMMA_CHAR);

        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_RETRY_MAX, CONF_NUMBER, NULL, &r->retry_max, NULL);
        parse_config_value(instance_id, line, AM_AGENTS_CONFIG_RETRY_WAIT, CONF_NUMBER, NULL, &r->retry_wait, NULL);

        if (r->local) { /*do read other options in case configuration is local*/

            /* other options */

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_AGENT_URI, CONF_STRING, NULL, &r->agenturi, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_COOKIE_NAME, CONF_STRING, NULL, &r->cookie_name, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_LOGIN_URL_MAP, CONF_STRING_MAP, &r->login_url_sz, &r->login_url, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_COOKIE_SECURE, CONF_NUMBER, NULL, &r->cookie_secure, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NOTIF_ENABLE, CONF_NUMBER, NULL, &r->notif_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NOTIF_URL, CONF_STRING, NULL, &r->notif_url, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CMP_CASE_IGNORE, CONF_NUMBER, NULL, &r->url_eval_case_ignore, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_POLICY_CACHE_VALID, CONF_NUMBER, NULL, &r->policy_cache_valid, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_TOKEN_CACHE_VALID, CONF_NUMBER, NULL, &r->token_cache_valid, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_UID_PARAM, CONF_STRING, NULL, &r->userid_param, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_UID_PARAM_TYPE, CONF_STRING, NULL, &r->userid_param_type, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_PROFILE_MODE, CONF_ATTR_MODE, NULL, &r->profile_attr_fetch, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_PROFILE_MAP, CONF_STRING_MAP, &r->profile_attr_map_sz, &r->profile_attr_map, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_SESSION_MODE, CONF_ATTR_MODE, NULL, &r->session_attr_fetch, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_SESSION_MAP, CONF_STRING_MAP, &r->session_attr_map_sz, &r->session_attr_map, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_RESPONSE_MODE, CONF_ATTR_MODE, NULL, &r->response_attr_fetch, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_RESPONSE_MAP, CONF_STRING_MAP, &r->response_attr_map_sz, &r->response_attr_map, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_LB_ENABLE, CONF_NUMBER, NULL, &r->lb_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_SSO_ONLY, CONF_NUMBER, NULL, &r->sso_only, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ACCESS_DENIED_URL, CONF_STRING, NULL, &r->access_denied_url, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_FQDN_CHECK_ENABLE, CONF_NUMBER, NULL, &r->fqdn_check_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_FQDN_DEFAULT, CONF_STRING, NULL, &r->fqdn_default, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_FQDN_MAP, CONF_STRING_MAP, &r->fqdn_map_sz, &r->fqdn_map, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_COOKIE_RESET_ENABLE, CONF_NUMBER, NULL, &r->cookie_reset_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_COOKIE_RESET_MAP, CONF_STRING_MAP, &r->cookie_reset_map_sz, &r->cookie_reset_map, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NOT_ENFORCED_URL, CONF_STRING_MAP, &r->not_enforced_map_sz, &r->not_enforced_map, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NOT_ENFORCED_INVERT, CONF_NUMBER, NULL, &r->not_enforced_invert, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NOT_ENFORCED_ATTR, CONF_NUMBER, NULL, &r->not_enforced_fetch_attr, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_NOT_ENFORCED_IP, CONF_STRING_MAP, &r->not_enforced_ip_map_sz, &r->not_enforced_ip_map, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_EXT_NOT_ENFORCED_URL, CONF_STRING_MAP, &r->not_enforced_ext_map_sz, &r->not_enforced_ext_map, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_PDP_ENABLE, CONF_NUMBER, NULL, &r->pdp_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_PDP_VALID, CONF_NUMBER, NULL, &r->pdp_cache_valid, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_PDP_COOKIE, CONF_STRING, NULL, &r->pdp_lb_cookie, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CLIENT_IP_VALIDATE, CONF_NUMBER, NULL, &r->client_ip_validate, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_COOKIE_PREFIX, CONF_STRING, NULL, &r->cookie_prefix, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_ATTR_COOKIE_MAX_AGE, CONF_NUMBER, NULL, &r->cookie_maxage, NULL);


            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CDSSO_ENABLE, CONF_NUMBER, NULL, &r->cdsso_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CDSSO_LOGIN, CONF_STRING_MAP, &r->cdsso_login_map_sz, &r->cdsso_login_map, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CDSSO_DOMAIN, CONF_STRING_MAP, &r->cdsso_cookie_domain_map_sz, &r->cdsso_cookie_domain_map, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_LOGOUT_URL, CONF_STRING_MAP, &r->openam_logout_map_sz, &r->openam_logout_map, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_APP_LOGOUT_URL, CONF_STRING_MAP, &r->logout_map_sz, &r->logout_map, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_LOGOUT_REDIRECT_URL, CONF_STRING, NULL, &r->logout_redirect_url, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_LOGOUT_COOKIE_RESET, CONF_STRING_MAP, &r->logout_cookie_reset_map_sz, &r->logout_cookie_reset_map, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_POLICY_SCOPE, CONF_NUMBER, NULL, &r->policy_scope_subtree, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_RESOLVE_CLIENT_HOST, CONF_NUMBER, NULL, &r->resolve_client_host, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_POLICY_ENCODE_SPECIAL_CHAR, CONF_NUMBER, NULL, &r->policy_eval_encode_chars, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_COOKIE_ENCODE_SPECIAL_CHAR, CONF_NUMBER, NULL, &r->cookie_encode_chars, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_OVERRIDE_PROTO, CONF_NUMBER, NULL, &r->override_protocol, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_OVERRIDE_HOST, CONF_NUMBER, NULL, &r->override_host, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_OVERRIDE_PORT, CONF_NUMBER, NULL, &r->override_port, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_OVERRIDE_NOTIFICATION_URL, CONF_NUMBER, NULL, &r->override_notif_url, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_VALID, CONF_NUMBER, NULL, &r->config_valid, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_PASSWORD_REPLAY_KEY, CONF_STRING, NULL, &r->password_replay_key, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_POLICY_CLOCK_SKEW, CONF_NUMBER, NULL, &r->policy_clock_skew, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_GOTO_PARAM_NAME, CONF_STRING, NULL, &r->url_redirect_param, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CACHE_CONTROL_ENABLE, CONF_NUMBER, NULL, &r->cache_control_enable, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_USE_REDIRECT_ADVICE, CONF_NUMBER, NULL, &r->use_redirect_for_advice, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CLIENT_IP_HEADER, CONF_STRING, NULL, &r->client_ip_header, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CLIENT_HOSTNAME_HEADER, CONF_STRING, NULL, &r->client_hostname_header, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_INVALID_URL, CONF_STRING, NULL, &r->url_check_regex, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_CONDITIONAL_LOGIN_URL, CONF_STRING_MAP, &r->cond_login_url_sz, &r->cond_login_url, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_COOKIE_HTTP_ONLY, CONF_NUMBER, NULL, &r->cookie_http_only, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_MULTI_VALUE_SEPARATOR, CONF_STRING, NULL, &r->multi_attr_separator, NULL);

            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_IIS_LOGON_USER, CONF_NUMBER, NULL, &r->logon_user_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_IIS_PASSWORD_HEADER, CONF_NUMBER, NULL, &r->password_header_enable, NULL);
            parse_config_value(instance_id, line, AM_AGENTS_CONFIG_PDP_JS_REPOST, CONF_NUMBER, NULL, &r->pdp_js_repost, NULL);
        }
    }

    r->ts = time(NULL);

    fclose(file);
    am_free(line);

    decrypt_agent_passwords(r);
    return r;
}

#define AM_CONF_FREE(sz,el) \
    do {\
        int i;\
        if (el != NULL) {\
            for (i = 0; i < sz; i++) {\
               void *v = (el)[i];\
               am_free(v);\
               v = NULL;\
            }\
            free(el);\
            el = NULL;\
        }\
    } while (0)

void am_config_free(am_config_t **cp) {
    if (cp != NULL && *cp != NULL) {
        am_config_t *c = *cp;

        if (ISVALID(c->pass) && c->pass_sz > 0) {
            am_secure_zero_memory(c->pass, c->pass_sz);
        }
        if (ISVALID(c->cert_key_pass) && c->cert_key_pass_sz > 0) {
            am_secure_zero_memory(c->cert_key_pass, c->cert_key_pass_sz);
        }

        AM_FREE(c->token, c->config, c->pdp_dir, c->realm, c->user, c->pass,
                c->key, c->debug_file, c->audit_file, c->cert_key_file,
                c->cert_key_pass, c->cert_file, c->cert_ca_file, c->ciphers,
                c->tls_opts, c->valid_default_url, c->agenturi, c->cookie_name,
                c->notif_url, c->userid_param, c->userid_param_type, c->access_denied_url,
                c->fqdn_default, c->pdp_lb_cookie, c->cookie_prefix, c->logout_redirect_url,
                c->password_replay_key, c->url_redirect_param, c->client_ip_header,
                c->client_hostname_header, c->url_check_regex, c->multi_attr_separator);

        AM_CONF_FREE(c->naming_url_sz, c->naming_url);
        AM_CONF_FREE(c->hostmap_sz, c->hostmap);
        AM_CONF_MAP_FREE(c->login_url_sz, c->login_url);
        AM_CONF_MAP_FREE(c->profile_attr_map_sz, c->profile_attr_map);
        AM_CONF_MAP_FREE(c->session_attr_map_sz, c->session_attr_map);
        AM_CONF_MAP_FREE(c->response_attr_map_sz, c->response_attr_map);
        AM_CONF_MAP_FREE(c->fqdn_map_sz, c->fqdn_map);
        AM_CONF_MAP_FREE(c->cookie_reset_map_sz, c->cookie_reset_map);
        AM_CONF_MAP_FREE(c->not_enforced_map_sz, c->not_enforced_map);
        AM_CONF_MAP_FREE(c->not_enforced_ext_map_sz, c->not_enforced_ext_map);
        AM_CONF_MAP_FREE(c->not_enforced_ip_map_sz, c->not_enforced_ip_map);
        AM_CONF_MAP_FREE(c->cdsso_login_map_sz, c->cdsso_login_map);
        AM_CONF_MAP_FREE(c->cdsso_cookie_domain_map_sz, c->cdsso_cookie_domain_map);
        AM_CONF_MAP_FREE(c->logout_cookie_reset_map_sz, c->logout_cookie_reset_map);
        AM_CONF_MAP_FREE(c->logout_map_sz, c->logout_map);
        AM_CONF_MAP_FREE(c->openam_logout_map_sz, c->openam_logout_map);
        AM_CONF_MAP_FREE(c->cond_login_url_sz, c->cond_login_url);

        free(c);
        c = NULL;
    }
}
