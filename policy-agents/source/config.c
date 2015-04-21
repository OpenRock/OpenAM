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
#include "list.h"
#include "net_client.h"

#define MAKE_TYPE(t,s) (s << 16 | t)
#define GET_TYPE(r)    (r & 0xFFFF)
#define GET_SIZE(r)    (r >> 16)

enum {
    AM_CONF_ALL = 0,
    AM_CONF_BOOT,
    AM_CONF_REMOTE
};

enum {
    AM_CONF_LOCAL = 1,
    AM_CONF_PDP_DIR,
    AM_CONF_NAMING_URL,
    AM_CONF_REALM,
    AM_CONF_USER,
    AM_CONF_PASSWORD,
    AM_CONF_KEY,
    AM_CONF_DEBUG,
    AM_CONF_DEBUG_LEVEL,
    AM_CONF_DEBUG_FILE,
    AM_CONF_AUDIT,
    AM_CONF_AUDIT_LEVEL,
    AM_CONF_AUDIT_FILE,
    AM_CONF_CERT_KEY_FILE,
    AM_CONF_CERT_KEY_PASS,
    AM_CONF_CERT_FILE,
    AM_CONF_CERT_CA_FILE,
    AM_CONF_CIPHERS,
    AM_CONF_TLS_OPTIONS,
    AM_CONF_CERT_TRUST,
    AM_CONF_NET_TIMEOUT,
    AM_CONF_VALID_LEVEL,
    AM_CONF_VALID_PING,
    AM_CONF_VALID_PING_MISS,
    AM_CONF_VALID_PING_OK,
    AM_CONF_VALID_DEFAULT_IDX,
    AM_CONF_HOST_MAP,
    AM_CONF_RETRY_MAX,
    AM_CONF_RETRY_WAIT,
    AM_CONF_AGENT_URI,
    AM_CONF_COOKIE,
    AM_CONF_LOGIN_URL,
    AM_CONF_COOKIE_SECURE,
    AM_CONF_NOTIF_ENABLE,
    AM_CONF_NOTIF_URL,
    AM_CONF_EVAL_CASE,
    AM_CONF_POLICY_CACHE_VALID,
    AM_CONF_TOKEN_CACHE_VALID,
    AM_CONF_UID_PARAM,
    AM_CONF_UID_PARAM_TYPE,
    AM_CONF_PROF_ATTR,
    AM_CONF_PROF_ATTR_MAP,
    AM_CONF_SESS_ATTR,
    AM_CONF_SESS_ATTR_MAP,
    AM_CONF_RESP_ATTR,
    AM_CONF_RESP_ATTR_MAP,
    AM_CONF_LB_ENABLE,
    AM_CONF_SSO_ONLY,
    AM_CONF_ACC_DENIED,
    AM_CONF_FQDN_CHECK,
    AM_CONF_FQDN_DEFAULT,
    AM_CONF_FQDN_MAP,
    AM_CONF_COOKIE_RESET,
    AM_CONF_COOKIE_RESET_MAP,
    AM_CONF_NEF_INVERT,
    AM_CONF_NEF_ATTR,
    AM_CONF_NEF_MAP,
    AM_CONF_NEF_IP_MAP,
    AM_CONF_NEF_EXT_MAP,
    AM_CONF_PDP,
    AM_CONF_PDP_LBCOOKIE,
    AM_CONF_PDP_CACHE,
    AM_CONF_PDP_JS,
    AM_CONF_IP_VALIDATE,
    AM_CONF_COOKIE_PREFIX,
    AM_CONF_COOKIE_MAXAGE,
    AM_CONF_CDSSO,
    AM_CONF_CDSSO_LOGIN_MAP,
    AM_CONF_CDSSO_COOKIE_MAP,
    AM_CONF_LOGOUT_COOKIE_MAP,
    AM_CONF_LOGOUT_REDIRECT,
    AM_CONF_LOGOUT_MAP,
    AM_CONF_AMLOGOUT_MAP,
    AM_CONF_SCOPE,
    AM_CONF_CRESOLVE,
    AM_CONF_PE_ENC_CHARS,
    AM_CONF_CK_ENC_CHARS,
    AM_CONF_OV_PROTO,
    AM_CONF_OV_HOST,
    AM_CONF_OV_PORT,
    AM_CONF_OV_NURL,
    AM_CONF_VALID,
    AM_CONF_PASS_REPLY_KEY,
    AM_CONF_POL_CSKEW,
    AM_CONF_URL_REDIR_PARAM,
    AM_CONF_CACHE_HEADERS,
    AM_CONF_REDIRECT_ADVICE,
    AM_CONF_CLIENT_IP_HEADER,
    AM_CONF_CLIENT_HOST_HEADER,
    AM_CONF_URL_CHECK,
    AM_CONF_CONDLOGIN_MAP,
    AM_CONF_HTTP_ONLY_COOKIE,
    AM_CONF_ATTR_SEP,
    AM_CONF_WIN_LOGON,
    AM_CONF_WIN_PASS_HEADER
};

struct am_instance {
    struct offset_list list; /* list of instance configurations */
};

struct am_instance_entry {
    time_t ts;
    unsigned long instance_id;
    char token[AM_SHARED_CACHE_KEY_SIZE];
    char name[AM_SHARED_CACHE_KEY_SIZE]; /*agent id*/
    char config[AM_PATH_SIZE]; /*config file name*/
    struct offset_list data; /*agent configuration data*/
    struct offset_list lh;
};

struct am_instance_entry_data {
    long type; /*low byte: type; high byte: optional map/list size*/
    int num_value;
    size_t size[2];
    struct offset_list lh;
    char value[1]; /*format: key\0value\0*/
};

static am_shm_t *conf = NULL;

int am_configuration_init() {
    if (conf != NULL) return AM_SUCCESS;

    conf = am_shm_create("am_shared_conf", sizeof (struct am_instance) * 2048 * AM_MAX_INSTANCES);
    if (conf == NULL) {
        return AM_ERROR;
    }
    if (conf->error != AM_SUCCESS) {
        return conf->error;
    }

    if (conf->init) {
        /* allocate the table itself */
        struct am_instance *instance_data = (struct am_instance *) am_shm_alloc(conf, sizeof (struct am_instance));
        if (instance_data == NULL) {
            conf->user = NULL;
            return AM_ENOMEM;
        }
        am_shm_lock(conf);
        /* initialize head node */
        instance_data->list.next = instance_data->list.prev = 0;
        conf->user = instance_data;
        /*store instance_data offset (for other processes)*/
        am_shm_set_user_offset(conf, AM_GET_OFFSET(conf->pool, instance_data));
        am_shm_unlock(conf);
    }

    return AM_SUCCESS;
}

int am_configuration_shutdown() {
    am_shm_shutdown(conf);
    return AM_SUCCESS;
}

static struct am_instance_entry *get_instance_entry(unsigned long instance_id) {
    struct am_instance_entry *e, *t, *h;
    struct am_instance *instance_data = conf != NULL ? (struct am_instance *) conf->user : NULL;

    if (instance_data != NULL) {
        h = (struct am_instance_entry *) AM_GET_POINTER(conf->pool, instance_data->list.prev);

        AM_OFFSET_LIST_FOR_EACH(conf->pool, h, e, t, struct am_instance_entry) {
            if (instance_id == e->instance_id) {
                return e;
            }
        }
    }
    return NULL;
}

static int delete_instance_entry(struct am_instance_entry *e) {
    int rv = 0;
    struct am_instance_entry_data *i, *t, *h;
    struct am_instance *instance_data = (struct am_instance *) conf->user;

    if (e == NULL) return AM_EINVAL;

    /* cleanup instance entry data */
    h = (struct am_instance_entry_data *) AM_GET_POINTER(conf->pool, e->data.prev);

    AM_OFFSET_LIST_FOR_EACH(conf->pool, h, i, t, struct am_instance_entry_data) {
        am_shm_free(conf, i);
    }

    /* remove a node from a doubly linked list */
    if (e->lh.prev == 0) {
        instance_data->list.prev = e->lh.next;
    } else {
        ((struct am_instance_entry *) AM_GET_POINTER(conf->pool, e->lh.prev))->lh.next = e->lh.next;
    }

    if (e->lh.next == 0) {
        instance_data->list.next = e->lh.prev;
    } else {
        ((struct am_instance_entry *) AM_GET_POINTER(conf->pool, e->lh.next))->lh.prev = e->lh.prev;
    }
    return rv;
}

void remove_agent_instance_byname(const char *name) {
    struct am_instance_entry *e, *t, *h;
    struct am_instance *instance_data = conf != NULL ? (struct am_instance *) conf->user : NULL;

    if (instance_data == NULL) return;
    h = (struct am_instance_entry *) AM_GET_POINTER(conf->pool, instance_data->list.prev);

    am_shm_lock(conf);

    AM_OFFSET_LIST_FOR_EACH(conf->pool, h, e, t, struct am_instance_entry) {
        if (strcmp(e->name, name) == 0) {
            am_remove_cache_entry(e->instance_id, e->token); /*delete cached agent session data*/
            am_agent_init_set_value(e->instance_id, AM_TRUE, AM_FALSE); /*set this instance to 'unconfigured'*/
            if (delete_instance_entry(e) == AM_SUCCESS) { /*remove cached configuration data*/
                am_shm_free(conf, e);
            }
            break;
        }
    }
    am_shm_unlock(conf);
}

#define SAVE_NUM_VALUE(cf,h,t,v) \
    do {\
        struct am_instance_entry_data *x = NULL;\
        x = am_shm_alloc(cf, sizeof (struct am_instance_entry_data));\
        if (x == NULL) return AM_ENOMEM;\
        x->type = t;\
        x->num_value = v;\
        x->size[0] = x->size[1] = 0;\
        x->lh.next = x->lh.prev = 0;\
        AM_OFFSET_LIST_INSERT(cf->pool, x, h, struct am_instance_entry_data);\
    } while(0)

#define SAVE_CHAR_VALUE(cf,h,t,v) \
    do {\
        size_t sz = strlen(v);\
        struct am_instance_entry_data *x = NULL;\
        x = am_shm_alloc(cf, sizeof (struct am_instance_entry_data) + sz + 1);\
        if (x == NULL) return AM_ENOMEM;\
        x->type = t;\
        x->num_value = 0;\
        x->size[0] = sz; x->size[1] = 0;\
        memcpy(x->value, v, x->size[0]);\
        x->value[x->size[0]] = 0;\
        x->lh.next = x->lh.prev = 0;\
        AM_OFFSET_LIST_INSERT(cf->pool, x, h, struct am_instance_entry_data);\
    } while(0)

#define SAVE_CHAR2_VALUE(cf,h,t,v,k) \
    do {\
        size_t sz = strlen(v);\
        size_t kz = strlen(k);\
        struct am_instance_entry_data *x = NULL;\
        x = am_shm_alloc(cf, sizeof (struct am_instance_entry_data) + sz + kz + 2);\
        if (x == NULL) return AM_ENOMEM;\
        x->type = t;\
        x->num_value = 0;\
        x->size[0] = sz; x->size[1] = kz;\
        memcpy(x->value, v, x->size[0]);\
        x->value[x->size[0]] = 0;\
        memcpy(x->value + x->size[0] + 1, k, x->size[1]);\
        x->value[x->size[0] + x->size[1] + 1] = 0;\
        x->lh.next = x->lh.prev = 0;\
        AM_OFFSET_LIST_INSERT(cf->pool, x, h, struct am_instance_entry_data);\
    } while(0)

static int am_create_instance_entry_data(am_shm_t *cf, struct offset_list *h, am_config_t *c, char all) {
    int i;

    if (all == AM_CONF_ALL || all == AM_CONF_BOOT) {

        if (c->local > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_LOCAL, 0), c->local);
        }
        if (ISVALID(c->pdp_dir)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_PDP_DIR, 0), c->pdp_dir);
        }
        if (c->naming_url_sz > 0 && c->naming_url != NULL) {
            for (i = 0; i < c->naming_url_sz; i++) {
                SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_NAMING_URL, c->naming_url_sz), c->naming_url[i]);
            }
        }
        if (ISVALID(c->realm)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_REALM, 0), c->realm);
        }
        if (ISVALID(c->user)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_USER, 0), c->user);
        }
        if (ISVALID(c->pass)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_PASSWORD, 0), c->pass);
        }
        if (ISVALID(c->key)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_KEY, 0), c->key);
        }
        if (c->debug > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_DEBUG, 0), c->debug);
        }
        if (c->debug_level > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_DEBUG_LEVEL, 0), c->debug_level);
        }
        if (ISVALID(c->debug_file)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_DEBUG_FILE, 0), c->debug_file);
        }
        if (c->audit > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_AUDIT, 0), c->audit);
        }
        if (c->audit_level > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_AUDIT_LEVEL, 0), c->audit_level);
        }
        if (ISVALID(c->audit_file)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_AUDIT_FILE, 0), c->audit_file);
        }
        if (ISVALID(c->cert_key_file)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_CERT_KEY_FILE, 0), c->cert_key_file);
        }
        if (ISVALID(c->cert_key_pass)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_CERT_KEY_PASS, 0), c->cert_key_pass);
        }
        if (ISVALID(c->cert_file)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_CERT_FILE, 0), c->cert_file);
        }
        if (ISVALID(c->cert_ca_file)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_CERT_CA_FILE, 0), c->cert_ca_file);
        }
        if (ISVALID(c->ciphers)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_CIPHERS, 0), c->ciphers);
        }
        if (ISVALID(c->tls_opts)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_TLS_OPTIONS, 0), c->tls_opts);
        }
        if (c->cert_trust > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_CERT_TRUST, 0), c->cert_trust);
        }
        if (c->net_timeout > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_NET_TIMEOUT, 0), c->net_timeout);
        }
        if (c->valid_level > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_VALID_LEVEL, 0), c->valid_level);
        }
        if (c->valid_ping > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_VALID_PING, 0), c->valid_ping);
        }
        if (c->valid_ping_miss > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_VALID_PING_MISS, 0), c->valid_ping_miss);
        }
        if (c->valid_ping_ok > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_VALID_PING_OK, 0), c->valid_ping_ok);
        }
        if (c->valid_default_url_sz > 0 && c->valid_default_url != NULL) {
            for (i = 0; i < c->valid_default_url_sz; i++) {
                SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_VALID_DEFAULT_IDX, c->valid_default_url_sz), c->valid_default_url[i]);
            }
        }
        if (c->hostmap_sz > 0 && c->hostmap != NULL) {
            for (i = 0; i < c->hostmap_sz; i++) {
                SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_HOST_MAP, c->hostmap_sz), c->hostmap[i]);
            }
        }
        if (c->retry_max > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_RETRY_MAX, 0), c->retry_max);
        }
        if (c->retry_wait > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_RETRY_WAIT, 0), c->retry_wait);
        }
    }

    if (all == AM_CONF_ALL || all == AM_CONF_REMOTE) {

        if (ISVALID(c->agenturi)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_AGENT_URI, 0), c->agenturi);
        }
        if (ISVALID(c->cookie_name)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_COOKIE, 0), c->cookie_name);
        }
        if (c->login_url_sz > 0 && c->login_url != NULL) {
            for (i = 0; i < c->login_url_sz; i++) {
                am_config_map_t *v = &c->login_url[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_LOGIN_URL, c->login_url_sz), v->name, v->value);
                }
            }
        }
        if (c->cookie_secure > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_COOKIE_SECURE, 0), c->cookie_secure);
        }
        if (c->notif_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_NOTIF_ENABLE, 0), c->notif_enable);
        }
        if (ISVALID(c->notif_url)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_NOTIF_URL, 0), c->notif_url);
        }
        if (c->url_eval_case_ignore > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_EVAL_CASE, 0), c->url_eval_case_ignore);
        }
        if (c->policy_cache_valid > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_POLICY_CACHE_VALID, 0), c->policy_cache_valid);
        }
        if (c->token_cache_valid > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_TOKEN_CACHE_VALID, 0), c->token_cache_valid);
        }
        if (ISVALID(c->userid_param)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_UID_PARAM, 0), c->userid_param);
        }
        if (ISVALID(c->userid_param_type)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_UID_PARAM_TYPE, 0), c->userid_param_type);
        }
        if (c->profile_attr_fetch > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_PROF_ATTR, 0), c->profile_attr_fetch);
        }
        if (c->profile_attr_map_sz > 0 && c->profile_attr_map != NULL) {
            for (i = 0; i < c->profile_attr_map_sz; i++) {
                am_config_map_t *v = &c->profile_attr_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_PROF_ATTR_MAP, c->profile_attr_map_sz), v->name, v->value);
                }
            }
        }
        if (c->session_attr_fetch > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_SESS_ATTR, 0), c->session_attr_fetch);
        }
        if (c->session_attr_map_sz > 0 && c->session_attr_map != NULL) {
            for (i = 0; i < c->session_attr_map_sz; i++) {
                am_config_map_t *v = &c->session_attr_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_SESS_ATTR_MAP, c->session_attr_map_sz), v->name, v->value);
                }
            }
        }
        if (c->response_attr_fetch > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_RESP_ATTR, 0), c->response_attr_fetch);
        }
        if (c->response_attr_map_sz > 0 && c->response_attr_map != NULL) {
            for (i = 0; i < c->response_attr_map_sz; i++) {
                am_config_map_t *v = &c->response_attr_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_RESP_ATTR_MAP, c->response_attr_map_sz), v->name, v->value);
                }
            }
        }
        if (c->lb_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_LB_ENABLE, 0), c->lb_enable);
        }
        if (c->sso_only > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_SSO_ONLY, 0), c->sso_only);
        }
        if (ISVALID(c->access_denied_url)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_ACC_DENIED, 0), c->access_denied_url);
        }
        if (c->fqdn_check_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_FQDN_CHECK, 0), c->fqdn_check_enable);
        }
        if (ISVALID(c->fqdn_default)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_FQDN_DEFAULT, 0), c->fqdn_default);
        }
        if (c->fqdn_map_sz > 0 && c->fqdn_map != NULL) {
            for (i = 0; i < c->fqdn_map_sz; i++) {
                am_config_map_t *v = &c->fqdn_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_FQDN_MAP, c->fqdn_map_sz), v->name, v->value);
                }
            }
        }
        if (c->cookie_reset_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_COOKIE_RESET, 0), c->cookie_reset_enable);
        }
        if (c->cookie_reset_map_sz > 0 && c->cookie_reset_map != NULL) {
            for (i = 0; i < c->cookie_reset_map_sz; i++) {
                am_config_map_t *v = &c->cookie_reset_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_COOKIE_RESET_MAP, c->cookie_reset_map_sz), v->name, v->value);
                }
            }
        }
        if (c->not_enforced_invert > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_NEF_INVERT, 0), c->not_enforced_invert);
        }
        if (c->not_enforced_fetch_attr > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_NEF_ATTR, 0), c->not_enforced_fetch_attr);
        }
        if (c->not_enforced_map_sz > 0 && c->not_enforced_map != NULL) {
            for (i = 0; i < c->not_enforced_map_sz; i++) {
                am_config_map_t *v = &c->not_enforced_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_NEF_MAP, c->not_enforced_map_sz), v->name, v->value);
                }
            }
        }
        if (c->not_enforced_ext_map_sz > 0 && c->not_enforced_ext_map != NULL) {
            for (i = 0; i < c->not_enforced_ext_map_sz; i++) {
                am_config_map_t *v = &c->not_enforced_ext_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_NEF_EXT_MAP, c->not_enforced_ext_map_sz), v->name, v->value);
                }
            }
        }
        if (c->not_enforced_ip_map_sz > 0 && c->not_enforced_ip_map != NULL) {
            for (i = 0; i < c->not_enforced_ip_map_sz; i++) {
                am_config_map_t *v = &c->not_enforced_ip_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_NEF_IP_MAP, c->not_enforced_ip_map_sz), v->name, v->value);
                }
            }
        }
        if (c->pdp_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_PDP, 0), c->pdp_enable);
        }
        if (ISVALID(c->pdp_lb_cookie)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_PDP_LBCOOKIE, 0), c->pdp_lb_cookie);
        }
        if (c->pdp_cache_valid > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_PDP_CACHE, 0), c->pdp_cache_valid);
        }
        if (c->pdp_js_repost > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_PDP_JS, 0), c->pdp_js_repost);
        }
        if (c->client_ip_validate > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_IP_VALIDATE, 0), c->client_ip_validate);
        }
        if (ISVALID(c->cookie_prefix)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_COOKIE_PREFIX, 0), c->cookie_prefix);
        }
        if (c->cookie_maxage > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_COOKIE_MAXAGE, 0), c->cookie_maxage);
        }
        if (c->cdsso_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_CDSSO, 0), c->cdsso_enable);
        }
        if (c->cdsso_login_map_sz > 0 && c->cdsso_login_map != NULL) {
            for (i = 0; i < c->cdsso_login_map_sz; i++) {
                am_config_map_t *v = &c->cdsso_login_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_CDSSO_LOGIN_MAP, c->cdsso_login_map_sz), v->name, v->value);
                }
            }
        }
        if (c->cdsso_cookie_domain_map_sz > 0 && c->cdsso_cookie_domain_map != NULL) {
            for (i = 0; i < c->cdsso_cookie_domain_map_sz; i++) {
                am_config_map_t *v = &c->cdsso_cookie_domain_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_CDSSO_COOKIE_MAP, c->cdsso_cookie_domain_map_sz), v->name, v->value);
                }
            }
        }
        if (c->logout_cookie_reset_map_sz > 0 && c->logout_cookie_reset_map != NULL) {
            for (i = 0; i < c->logout_cookie_reset_map_sz; i++) {
                am_config_map_t *v = &c->logout_cookie_reset_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_LOGOUT_COOKIE_MAP, c->logout_cookie_reset_map_sz), v->name, v->value);
                }
            }
        }
        if (ISVALID(c->logout_redirect_url)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_LOGOUT_REDIRECT, 0), c->logout_redirect_url);
        }
        if (c->logout_map_sz > 0 && c->logout_map != NULL) {
            for (i = 0; i < c->logout_map_sz; i++) {
                am_config_map_t *v = &c->logout_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_LOGOUT_MAP, c->logout_map_sz), v->name, v->value);
                }
            }
        }
        if (c->openam_logout_map_sz > 0 && c->openam_logout_map != NULL) {
            for (i = 0; i < c->openam_logout_map_sz; i++) {
                am_config_map_t *v = &c->openam_logout_map[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_AMLOGOUT_MAP, c->openam_logout_map_sz), v->name, v->value);
                }
            }
        }
        if (c->policy_scope_subtree > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_SCOPE, 0), c->policy_scope_subtree);
        }
        if (c->resolve_client_host > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_CRESOLVE, 0), c->resolve_client_host);
        }
        if (c->policy_eval_encode_chars > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_PE_ENC_CHARS, 0), c->policy_eval_encode_chars);
        }
        if (c->cookie_encode_chars > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_CK_ENC_CHARS, 0), c->cookie_encode_chars);
        }
        if (c->override_protocol > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_OV_PROTO, 0), c->override_protocol);
        }
        if (c->override_host > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_OV_HOST, 0), c->override_host);
        }
        if (c->override_port > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_OV_PORT, 0), c->override_port);
        }
        if (c->override_notif_url > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_OV_NURL, 0), c->override_notif_url);
        }
        if (c->config_valid > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_VALID, 0), c->config_valid);
        }
        if (ISVALID(c->password_replay_key)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_PASS_REPLY_KEY, 0), c->password_replay_key);
        }
        if (c->policy_clock_skew > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_POL_CSKEW, 0), c->policy_clock_skew);
        }
        if (ISVALID(c->url_redirect_param)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_URL_REDIR_PARAM, 0), c->url_redirect_param);
        }
        if (c->cache_control_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_CACHE_HEADERS, 0), c->cache_control_enable);
        }
        if (c->use_redirect_for_advice > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_REDIRECT_ADVICE, 0), c->use_redirect_for_advice);
        }
        if (ISVALID(c->client_ip_header)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_CLIENT_IP_HEADER, 0), c->client_ip_header);
        }
        if (ISVALID(c->client_hostname_header)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_CLIENT_HOST_HEADER, 0), c->client_hostname_header);
        }
        if (ISVALID(c->url_check_regex)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_URL_CHECK, 0), c->url_check_regex);
        }
        if (c->cond_login_url_sz > 0 && c->cond_login_url != NULL) {
            for (i = 0; i < c->cond_login_url_sz; i++) {
                am_config_map_t *v = &c->cond_login_url[i];
                if (ISVALID(v->name) && ISVALID(v->value)) {
                    SAVE_CHAR2_VALUE(cf, h, MAKE_TYPE(AM_CONF_CONDLOGIN_MAP, c->cond_login_url_sz), v->name, v->value);
                }
            }
        }
        if (c->cookie_http_only > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_HTTP_ONLY_COOKIE, 0), c->cookie_http_only);
        }
        if (ISVALID(c->multi_attr_separator)) {
            SAVE_CHAR_VALUE(cf, h, MAKE_TYPE(AM_CONF_ATTR_SEP, 0), c->multi_attr_separator);
        }
        if (c->logon_user_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_WIN_LOGON, 0), c->logon_user_enable);
        }
        if (c->password_header_enable > 0) {
            SAVE_NUM_VALUE(cf, h, MAKE_TYPE(AM_CONF_WIN_PASS_HEADER, 0), c->password_header_enable);
        }
    }
    return AM_SUCCESS;
}

static am_config_t *am_get_stored_agent_config(struct am_instance_entry *c) {
    am_config_t *r = NULL;
    struct am_instance_entry_data *i, *t, *h;

    if (c == NULL) return NULL;
    r = calloc(1, sizeof (am_config_t));
    if (r == NULL) return NULL;
    h = (struct am_instance_entry_data *) AM_GET_POINTER(conf->pool, c->data.prev);

    AM_OFFSET_LIST_FOR_EACH(conf->pool, h, i, t, struct am_instance_entry_data) {
        int ty = GET_TYPE(i->type);
        int sz = GET_SIZE(i->type);
        switch (ty) {
            case AM_CONF_LOCAL:
                r->local = i->num_value;
                break;
            case AM_CONF_PDP_DIR:
                r->pdp_dir = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_NAMING_URL:
                if (r->naming_url_sz == 0) {
                    r->naming_url = malloc(sz * sizeof (char *));
                }
                if (r->naming_url != NULL && r->naming_url_sz < sz) {
                    r->naming_url[r->naming_url_sz++] = strndup(i->value, i->size[0]);
                }
                break;
            case AM_CONF_REALM:
                r->realm = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_USER:
                r->user = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_PASSWORD:
                r->pass = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_KEY:
                r->key = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_DEBUG:
                r->debug = i->num_value;
                break;
            case AM_CONF_DEBUG_LEVEL:
                r->debug_level = i->num_value;
                break;
            case AM_CONF_DEBUG_FILE:
                r->debug_file = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_AUDIT:
                r->audit = i->num_value;
                break;
            case AM_CONF_AUDIT_LEVEL:
                r->audit_level = i->num_value;
                break;
            case AM_CONF_AUDIT_FILE:
                r->audit_file = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CERT_KEY_FILE:
                r->cert_key_file = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CERT_KEY_PASS:
                r->cert_key_pass = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CERT_FILE:
                r->cert_file = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CERT_CA_FILE:
                r->cert_ca_file = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CIPHERS:
                r->ciphers = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_TLS_OPTIONS:
                r->tls_opts = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CERT_TRUST:
                r->cert_trust = i->num_value;
                break;
            case AM_CONF_NET_TIMEOUT:
                r->net_timeout = i->num_value;
                break;
            case AM_CONF_VALID_LEVEL:
                r->valid_level = i->num_value;
                break;
            case AM_CONF_VALID_PING:
                r->valid_ping = i->num_value;
                break;
            case AM_CONF_VALID_PING_MISS:
                r->valid_ping_miss = i->num_value;
                break;
            case AM_CONF_VALID_PING_OK:
                r->valid_ping_ok = i->num_value;
                break;
            case AM_CONF_VALID_DEFAULT_IDX:
                if (r->valid_default_url_sz == 0) {
                    r->valid_default_url = malloc(sz * sizeof (int));
                }
                if (r->valid_default_url != NULL && r->valid_default_url_sz < sz) {
                    r->valid_default_url[r->valid_default_url_sz++] = i->num_value;
                }
                break;
            case AM_CONF_HOST_MAP:
                if (r->hostmap_sz == 0) {
                    r->hostmap = malloc(sz * sizeof (char *));
                }
                if (r->hostmap != NULL && r->hostmap_sz < sz) {
                    r->hostmap[r->hostmap_sz++] = strndup(i->value, i->size[0]);
                }
                break;
            case AM_CONF_RETRY_MAX:
                r->retry_max = i->num_value;
                break;
            case AM_CONF_RETRY_WAIT:
                r->retry_wait = i->num_value;
                break;
            case AM_CONF_AGENT_URI:
                r->agenturi = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_COOKIE:
                r->cookie_name = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_LOGIN_URL:
                if (r->login_url_sz == 0) {
                    r->login_url = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->login_url != NULL && r->login_url_sz < sz) {
                    am_config_map_t *m = &r->login_url[r->login_url_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_COOKIE_SECURE:
                r->cookie_secure = i->num_value;
                break;
            case AM_CONF_NOTIF_ENABLE:
                r->notif_enable = i->num_value;
                break;
            case AM_CONF_NOTIF_URL:
                r->notif_url = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_EVAL_CASE:
                r->url_eval_case_ignore = i->num_value;
                break;
            case AM_CONF_POLICY_CACHE_VALID:
                r->policy_cache_valid = i->num_value;
                break;
            case AM_CONF_TOKEN_CACHE_VALID:
                r->token_cache_valid = i->num_value;
                break;
            case AM_CONF_UID_PARAM:
                r->userid_param = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_UID_PARAM_TYPE:
                r->userid_param_type = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_PROF_ATTR:
                r->profile_attr_fetch = i->num_value;
                break;
            case AM_CONF_PROF_ATTR_MAP:
                if (r->profile_attr_map_sz == 0) {
                    r->profile_attr_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->profile_attr_map != NULL && r->profile_attr_map_sz < sz) {
                    am_config_map_t *m = &r->profile_attr_map[r->profile_attr_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_SESS_ATTR:
                r->session_attr_fetch = i->num_value;
                break;
            case AM_CONF_SESS_ATTR_MAP:
                if (r->session_attr_map_sz == 0) {
                    r->session_attr_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->session_attr_map != NULL && r->session_attr_map_sz < sz) {
                    am_config_map_t *m = &r->session_attr_map[r->session_attr_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_RESP_ATTR:
                r->response_attr_fetch = i->num_value;
                break;
            case AM_CONF_RESP_ATTR_MAP:
                if (r->response_attr_map_sz == 0) {
                    r->response_attr_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->response_attr_map != NULL && r->response_attr_map_sz < sz) {
                    am_config_map_t *m = &r->response_attr_map[r->response_attr_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_LB_ENABLE:
                r->lb_enable = i->num_value;
                break;
            case AM_CONF_SSO_ONLY:
                r->sso_only = i->num_value;
                break;
            case AM_CONF_ACC_DENIED:
                r->access_denied_url = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_FQDN_CHECK:
                r->fqdn_check_enable = i->num_value;
                break;
            case AM_CONF_FQDN_DEFAULT:
                r->fqdn_default = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_FQDN_MAP:
                if (r->fqdn_map_sz == 0) {
                    r->fqdn_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->fqdn_map != NULL && r->fqdn_map_sz < sz) {
                    am_config_map_t *m = &r->fqdn_map[r->fqdn_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_COOKIE_RESET:
                r->cookie_reset_enable = i->num_value;
                break;
            case AM_CONF_COOKIE_RESET_MAP:
                if (r->cookie_reset_map_sz == 0) {
                    r->cookie_reset_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->cookie_reset_map != NULL && r->cookie_reset_map_sz < sz) {
                    am_config_map_t *m = &r->cookie_reset_map[r->cookie_reset_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_NEF_INVERT:
                r->not_enforced_invert = i->num_value;
                break;
            case AM_CONF_NEF_ATTR:
                r->not_enforced_fetch_attr = i->num_value;
                break;
            case AM_CONF_NEF_MAP:
                if (r->not_enforced_map_sz == 0) {
                    r->not_enforced_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->not_enforced_map != NULL && r->not_enforced_map_sz < sz) {
                    am_config_map_t *m = &r->not_enforced_map[r->not_enforced_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_NEF_IP_MAP:
                if (r->not_enforced_ip_map_sz == 0) {
                    r->not_enforced_ip_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->not_enforced_ip_map != NULL && r->not_enforced_ip_map_sz < sz) {
                    am_config_map_t *m = &r->not_enforced_ip_map[r->not_enforced_ip_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_NEF_EXT_MAP:
                if (r->not_enforced_ext_map_sz == 0) {
                    r->not_enforced_ext_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->not_enforced_ext_map != NULL && r->not_enforced_ext_map_sz < sz) {
                    am_config_map_t *m = &r->not_enforced_ext_map[r->not_enforced_ext_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_PDP:
                r->pdp_enable = i->num_value;
                break;
            case AM_CONF_PDP_LBCOOKIE:
                r->pdp_lb_cookie = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_PDP_CACHE:
                r->pdp_cache_valid = i->num_value;
                break;
            case AM_CONF_PDP_JS:
                r->pdp_js_repost = i->num_value;
                break;
            case AM_CONF_IP_VALIDATE:
                r->client_ip_validate = i->num_value;
                break;
            case AM_CONF_COOKIE_PREFIX:
                r->cookie_prefix = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_COOKIE_MAXAGE:
                r->cookie_maxage = i->num_value;
                break;
            case AM_CONF_CDSSO:
                r->cdsso_enable = i->num_value;
                break;
            case AM_CONF_CDSSO_LOGIN_MAP:
                if (r->cdsso_login_map_sz == 0) {
                    r->cdsso_login_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->cdsso_login_map != NULL && r->cdsso_login_map_sz < sz) {
                    am_config_map_t *m = &r->cdsso_login_map[r->cdsso_login_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_CDSSO_COOKIE_MAP:
                if (r->cdsso_cookie_domain_map_sz == 0) {
                    r->cdsso_cookie_domain_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->cdsso_cookie_domain_map != NULL && r->cdsso_cookie_domain_map_sz < sz) {
                    am_config_map_t *m = &r->cdsso_cookie_domain_map[r->cdsso_cookie_domain_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_LOGOUT_COOKIE_MAP:
                if (r->logout_cookie_reset_map_sz == 0) {
                    r->logout_cookie_reset_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->logout_cookie_reset_map != NULL && r->logout_cookie_reset_map_sz < sz) {
                    am_config_map_t *m = &r->logout_cookie_reset_map[r->logout_cookie_reset_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_LOGOUT_REDIRECT:
                r->logout_redirect_url = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_LOGOUT_MAP:
                if (r->logout_map_sz == 0) {
                    r->logout_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->logout_map != NULL && r->logout_map_sz < sz) {
                    am_config_map_t *m = &r->logout_map[r->logout_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_AMLOGOUT_MAP:
                if (r->openam_logout_map_sz == 0) {
                    r->openam_logout_map = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->openam_logout_map != NULL && r->openam_logout_map_sz < sz) {
                    am_config_map_t *m = &r->openam_logout_map[r->openam_logout_map_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_SCOPE:
                r->policy_scope_subtree = i->num_value;
                break;
            case AM_CONF_CRESOLVE:
                r->resolve_client_host = i->num_value;
                break;
            case AM_CONF_PE_ENC_CHARS:
                r->policy_eval_encode_chars = i->num_value;
                break;
            case AM_CONF_CK_ENC_CHARS:
                r->cookie_encode_chars = i->num_value;
                break;
            case AM_CONF_OV_PROTO:
                r->override_protocol = i->num_value;
                break;
            case AM_CONF_OV_HOST:
                r->override_host = i->num_value;
                break;
            case AM_CONF_OV_PORT:
                r->override_port = i->num_value;
                break;
            case AM_CONF_OV_NURL:
                r->override_notif_url = i->num_value;
                break;
            case AM_CONF_VALID:
                r->config_valid = i->num_value;
                break;
            case AM_CONF_PASS_REPLY_KEY:
                r->password_replay_key = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_POL_CSKEW:
                r->policy_clock_skew = i->num_value;
                break;
            case AM_CONF_URL_REDIR_PARAM:
                r->url_redirect_param = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CACHE_HEADERS:
                r->cache_control_enable = i->num_value;
                break;
            case AM_CONF_REDIRECT_ADVICE:
                r->use_redirect_for_advice = i->num_value;
                break;
            case AM_CONF_CLIENT_IP_HEADER:
                r->client_ip_header = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CLIENT_HOST_HEADER:
                r->client_hostname_header = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_URL_CHECK:
                r->url_check_regex = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_CONDLOGIN_MAP:
                if (r->cond_login_url_sz == 0) {
                    r->cond_login_url = malloc(sz * sizeof (am_config_map_t));
                }
                if (r->cond_login_url != NULL && r->cond_login_url_sz < sz) {
                    am_config_map_t *m = &r->cond_login_url[r->cond_login_url_sz++];
                    m->name = malloc(i->size[0] + i->size[1] + 2);
                    memcpy(m->name, i->value, i->size[0] + i->size[1] + 2);
                    m->value = m->name + i->size[0] + 1;
                }
                break;
            case AM_CONF_HTTP_ONLY_COOKIE:
                r->cookie_http_only = i->num_value;
                break;
            case AM_CONF_ATTR_SEP:
                r->multi_attr_separator = strndup(i->value, i->size[0]);
                break;
            case AM_CONF_WIN_LOGON:
                r->logon_user_enable = i->num_value;
                break;
            case AM_CONF_WIN_PASS_HEADER:
                r->password_header_enable = i->num_value;
                break;
        }
    }

    return r;
}

static int am_set_agent_config(unsigned long instance_id, const char *xml,
        size_t xsz, const char *token, const char *config_file, const char *name,
        am_config_t *bc, struct am_instance_entry **ie) {
    static const char *thisfunc = "am_set_agent_config():";
    struct am_instance_entry *c;
    int ret;
    struct am_instance *instance_data = conf != NULL ? (struct am_instance *) conf->user : NULL;

    if (bc == NULL) return AM_EINVAL;
    if (instance_data == NULL) return AM_ENOMEM;

    am_shm_lock(conf);

    c = get_instance_entry(instance_id);
    if (c != NULL) {
        ret = delete_instance_entry(c);
        if (ret == AM_SUCCESS) {
            am_shm_free(conf, c);
        }
        c = NULL;
        if (xml == NULL) {
            am_shm_unlock(conf);
            return ret;
        }
    }

    c = am_shm_alloc(conf, sizeof (struct am_instance_entry));
    if (c == NULL) {
        AM_LOG_ERROR(instance_id, "%s failed to allocate %ld bytes",
                thisfunc, sizeof (struct am_instance_entry));
        am_shm_unlock(conf);
        return AM_ENOMEM;
    }

    c->instance_id = instance_id;
    c->ts = time(NULL);
    memset(c->token, 0, sizeof (c->token));
    if (ISVALID(token)) {
        strncpy(c->token, token, sizeof (c->token) - 1);
    }
    memset(c->config, 0, sizeof (c->config));
    if (ISVALID(config_file)) {
        strncpy(c->config, config_file, sizeof (c->config) - 1);
    }
    memset(c->name, 0, sizeof (c->name));
    if (ISVALID(name)) {
        strncpy(c->name, name, sizeof (c->name) - 1);
    }

    c->data.next = c->data.prev = 0;
    c->lh.next = c->lh.prev = 0;
    AM_OFFSET_LIST_INSERT(conf->pool, c, &(instance_data->list), struct am_instance_entry);

    if (bc->local) {
        ret = am_create_instance_entry_data(conf, &(c->data), bc, AM_CONF_ALL);
    } else {
        if (xml == NULL || xsz == 0) {
            ret = AM_EINVAL;
        } else {
            am_config_t *cf = am_parse_config_xml(instance_id, xml, xsz, AM_TRUE);
            if (cf == NULL) {
                AM_LOG_ERROR(instance_id, "%s failed to parse agent profile xml",
                        thisfunc);
                ret = AM_XML_ERROR;
            } else {
                ret = am_create_instance_entry_data(conf, &(c->data), bc, AM_CONF_BOOT); /*store bootstrap properties*/
                ret = am_create_instance_entry_data(conf, &(c->data), cf, AM_CONF_REMOTE);
                am_config_free(&cf);
            }
        }
    }

    if (ie != NULL) *ie = c;
    am_shm_unlock(conf);
    return ret;
}

int am_get_agent_config(unsigned long instance_id, const char *config_file, am_config_t **cnf) {
    static const char *thisfunc = "am_get_agent_config():";
    struct am_instance_entry *c;
    int rv = 1, in_progress = AM_FALSE;
    char *profile_xml = NULL;
    size_t profile_xml_sz = 0;
    int max_retry = 3;
    unsigned int retry = 3, retry_wait = 2; //TODO: conf values

    if (conf == NULL) {
        AM_LOG_ERROR(instance_id, "%s unable to fetch agent configuration (shared memory error)",
                thisfunc);
        return AM_ENOMEM;
    }

    max_retry++;
    do {

        am_shm_lock(conf);
        c = get_instance_entry(instance_id);
        if (c == NULL) {
            am_request_t r;
            int login_status, should_retry = AM_FALSE, store_status;
            char *agent_token = NULL;
            struct am_namevalue *agent_session = NULL;
            am_config_t *ac = NULL;
            struct am_ssl_options info;

            am_shm_unlock(conf);
            am_agent_instance_init_lock();

            in_progress = am_agent_init_get_value(instance_id, AM_FALSE);

            AM_LOG_DEBUG(instance_id, "%s agent configuration fetch in progress: %d",
                    thisfunc, in_progress);

            if (in_progress) {
                am_agent_instance_init_unlock();
                AM_LOG_WARNING(instance_id, "%s retry %d",
                        thisfunc, (retry - max_retry) + 1);
                sleep(retry_wait);
                continue;
            }

            am_agent_init_set_value(instance_id, AM_FALSE, AM_TRUE); /*configuration fetch in progress*/

            ac = am_get_config_file(instance_id, config_file);
            if (ac == NULL) {
                am_agent_init_set_value(instance_id, AM_FALSE, AM_FALSE);
                am_agent_instance_init_unlock();
                AM_LOG_ERROR(instance_id, "%s failed to load instance bootstrap %ld data",
                        thisfunc, instance_id);
                return AM_FILE_ERROR; /*fatal*/
            }

            am_net_set_ssl_options(ac, &info);

            memset(&r, 0, sizeof (am_request_t));
            r.conf = ac;
            r.instance_id = instance_id;
            login_status = am_agent_login(instance_id, get_valid_openam_url(&r), NOTNULL(ac->notif_url),
                    ac->user, ac->pass, ac->realm, ac->local, &info,
                    &agent_token, &profile_xml, &profile_xml_sz, &agent_session, NULL);
            if (login_status == AM_SUCCESS && ISVALID(agent_token) && agent_session != NULL) {

                AM_LOG_DEBUG(instance_id, "%s agent login%s succeeded", thisfunc,
                        ISVALID(profile_xml) ? " and profile fetch" : "");

                if ((store_status = am_set_agent_config(instance_id, profile_xml, profile_xml_sz,
                        agent_token, config_file, ac->user, ac, &c)) == AM_SUCCESS) {
                    am_add_session_policy_cache_entry(&r, agent_token,
                            NULL, agent_session);
                    AM_LOG_DEBUG(instance_id, "%s agent configuration stored in a cache",
                            thisfunc);
                } else {
                    AM_LOG_WARNING(instance_id, "%s retry %d (%s)",
                            thisfunc, (retry - max_retry) + 1, am_strerror(store_status));

                    //TODO async logout if failed (session_logout_worker)?

                    if (c != NULL && delete_instance_entry(c) == AM_SUCCESS) {
                        am_shm_free(conf, c);
                    }
                    should_retry = AM_TRUE;
                }
            } else {
                AM_LOG_WARNING(instance_id, "%s retry %d (login failure)",
                        thisfunc, (retry - max_retry) + 1);
                should_retry = AM_TRUE;
            }

            am_config_free(&ac);
            delete_am_namevalue_list(&agent_session);
            AM_FREE(agent_token, profile_xml);
            profile_xml = agent_token = NULL;
            agent_session = NULL;

            if (should_retry) {
                am_agent_init_set_value(instance_id, AM_FALSE, AM_FALSE);
                am_agent_instance_init_unlock();
                sleep(retry_wait);
                continue;
            }

            am_agent_instance_init_unlock();
        }

        if (c != NULL && cnf != NULL) {
            *cnf = am_get_stored_agent_config(c);
            if (*cnf != NULL) {
                (*cnf)->instance_id = instance_id;
                (*cnf)->ts = c->ts;
                (*cnf)->token = strdup(c->token);
                (*cnf)->config = strdup(c->config);
                rv = AM_SUCCESS;
                AM_LOG_DEBUG(instance_id, "%s agent configuration read from a cache",
                        thisfunc);
                am_shm_unlock(conf);
                break;
            }
        }

        am_shm_unlock(conf);
    } while (--max_retry > 0);

    if (max_retry == 0) {
        AM_LOG_ERROR(instance_id,
                "%s failed to locate instance configuration %ld data (max %d retries exhausted)",
                thisfunc, instance_id, retry);
        return AM_RETRY_ERROR; /*fatal*/
    }

    return rv;
}
