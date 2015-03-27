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

#ifndef CONFIG_H
#define CONFIG_H

#include <time.h>

struct am_session_info {
    char *si;
    char *sk;
    char *s1;
    int error;
};

typedef struct am_config_map {
    char *name;
    char *value;
} am_config_map_t;

#define AM_CONF_MAP_FREE(sz,el) \
    do {\
        int i;\
        for (i = 0; (el) && i < sz; i++) {\
            am_config_map_t v = (el)[i];\
                if (v.name != NULL) free(v.name);\
                /*if (v.value != NULL) free(v.value); should not be freed as name-value is allocated in one chunk*/\
        }\
        if ((el) != NULL) free(el);\
        (el) = NULL;\
    } while (0)

typedef struct {
    time_t ts;
    unsigned long instance_id;
    char *token;
    char *config;
    struct am_session_info si;

    /* bootstrap options */

    int local; /* local or remote configuration */
    char *pdp_dir; /* directory to store pdp data files */
    int naming_url_sz;
    char **naming_url; /* OpenAM deployment URLs only */

    char *realm; /* agent profile info */
    char *user;
    char *pass;
    char *key;

    /* debug and audit logging */
    int debug; /*0 do not rotate, x rotate at x bytes, -1 rotate once a day*/
    int debug_level;
    char *debug_file;
    int audit; /*0 do not rotate, x rotate at x bytes, -1 rotate once a day*/
    int audit_level;
    char *audit_file;

    char *cert_key_file;
    char *cert_key_pass;
    char *cert_file;
    char *cert_ca_file;
    char *ciphers;
    char *tls_opts;
    int cert_trust;

    int net_timeout;

    int valid_level;
    int valid_ping;
    int valid_ping_miss;
    int valid_ping_ok;
    int valid_default_url_sz;
    int *valid_default_url;

    int hostmap_sz;
    char **hostmap;

    int retry_max;
    int retry_wait;

    /* other options */

    char *agenturi;

    char *cookie_name;

    int login_url_sz;
    am_config_map_t *login_url;

    int cookie_secure;

    int notif_enable;
    char *notif_url;

    int url_eval_case_ignore;
    int policy_cache_valid; /*seconds*/
    int token_cache_valid;

    char *userid_param;
    char *userid_param_type;

    int profile_attr_fetch; /* SET_ATTRS_NONE - 0, SET_ATTRS_AS_HEADER, SET_ATTRS_AS_COOKIE */
    int profile_attr_map_sz;
    am_config_map_t *profile_attr_map;

    int session_attr_fetch;
    int session_attr_map_sz;
    am_config_map_t *session_attr_map;

    int response_attr_fetch;
    int response_attr_map_sz;
    am_config_map_t *response_attr_map;

    int lb_enable;
    int sso_only;

    char *access_denied_url;

    int fqdn_check_enable;
    char *fqdn_default;
    int fqdn_map_sz;
    am_config_map_t *fqdn_map;

    int cookie_reset_enable;
    int cookie_reset_map_sz;
    am_config_map_t *cookie_reset_map;

    /* not enforced handling */
    int not_enforced_invert;
    int not_enforced_fetch_attr;
    int not_enforced_map_sz;
    /* key: [GET,]0  value: regular expression 
     * key format: [method,]index
     * where method name may be omitted ("all methods") */
    am_config_map_t *not_enforced_map;
    int not_enforced_ip_map_sz;
    /* key: [GET,]0  value: cidr notation */
    am_config_map_t *not_enforced_ip_map;

    int pdp_enable;
    char *pdp_lb_cookie;
    int pdp_cache_valid;
    int pdp_js_repost;

    int client_ip_validate;

    char *cookie_prefix; /* HTTP_ */
    int cookie_maxage;

    int cdsso_enable;
    int cdsso_login_map_sz;
    am_config_map_t *cdsso_login_map;
    int cdsso_cookie_domain_map_sz;
    am_config_map_t *cdsso_cookie_domain_map;

    int logout_cookie_reset_map_sz;
    am_config_map_t *logout_cookie_reset_map;
    char *logout_redirect_url;
    int logout_map_sz;
    am_config_map_t *logout_map; /*application logout url list (regular expressions only)*/
    int openam_logout_map_sz;
    am_config_map_t *openam_logout_map; /*OpenAM logout url list*/

    int policy_scope_subtree; /*0 - self, 1 - subtree*/
    int resolve_client_host;
    int policy_eval_encode_chars;
    int cookie_encode_chars;

    int override_protocol;
    int override_host;
    int override_port;
    int override_notif_url;

    int config_valid; /* agent configuration valid (sec) */

    char *password_replay_key; /**/

    int policy_clock_skew;

    char *url_redirect_param;

    int cache_control_enable;
    int use_redirect_for_advice;

    char *client_ip_header;
    char *client_hostname_header;

    char *url_check_regex;

    /* key: pattern,0  value: url 
     * key format: pattern,index
     **/
    int cond_login_url_sz;
    am_config_map_t *cond_login_url;

    int cookie_http_only;
    char *multi_attr_separator;

    int logon_user_enable;
    int password_header_enable;

} am_config_t;

/* bootstrap options */

#define AM_AGENTS_CONFIG_LOCAL "com.sun.identity.agents.config.repository.location"
#define AM_AGENTS_CONFIG_POSTDATA_PRESERVE_DIR "org.forgerock.agents.config.postdata.preserve.dir"
#define AM_AGENTS_CONFIG_NAMING_URL "com.sun.identity.agents.config.naming.url"
#define AM_AGENTS_CONFIG_REALM "com.sun.identity.agents.config.organization.name"
#define AM_AGENTS_CONFIG_USER "com.sun.identity.agents.config.username"
#define AM_AGENTS_CONFIG_PASSWORD "com.sun.identity.agents.config.password"
#define AM_AGENTS_CONFIG_KEY "com.sun.identity.agents.config.key"

#define AM_AGENTS_CONFIG_DEBUG_OPT "com.sun.identity.agents.config.debug.file.size"
#define AM_AGENTS_CONFIG_DEBUG_FILE "com.sun.identity.agents.config.local.logfile"
#define AM_AGENTS_CONFIG_DEBUG_LEVEL "com.sun.identity.agents.config.debug.level"
#define AM_AGENTS_CONFIG_AUDIT_FILE "com.sun.identity.agents.config.local.audit.logfile"
#define AM_AGENTS_CONFIG_AUDIT_LEVEL "com.sun.identity.agents.config.audit.accesstype"
#define AM_AGENTS_CONFIG_AUDIT_OPT "com.sun.identity.agents.config.local.log.size"

#define AM_AGENTS_CONFIG_CERT_KEY_FILE "com.forgerock.agents.config.cert.key"
#define AM_AGENTS_CONFIG_CERT_KEY_PASSWORD "com.forgerock.agents.config.cert.key.password"
#define AM_AGENTS_CONFIG_CERT_FILE "com.forgerock.agents.config.cert.file"        
#define AM_AGENTS_CONFIG_CA_FILE "com.forgerock.agents.config.cert.ca.file"
#define AM_AGENTS_CONFIG_CIPHERS "com.forgerock.agents.config.ciphers"
#define AM_AGENTS_CONFIG_TRUST_CERT "com.sun.identity.agents.config.trust.server.certs"
#define AM_AGENTS_CONFIG_TLS_OPT "org.forgerock.agents.config.tls"

#define AM_AGENTS_CONFIG_NET_TIMEOUT "com.sun.identity.agents.config.connect.timeout"

#define AM_AGENTS_CONFIG_URL_VALIDATE_LEVEL "com.forgerock.agents.ext.url.validation.level"        
#define AM_AGENTS_CONFIG_URL_VALIDATE_PING_INTERVAL "com.forgerock.agents.ext.url.validation.ping.interval"
#define AM_AGENTS_CONFIG_URL_VALIDATE_PING_MISS "com.forgerock.agents.ext.url.validation.ping.miss.count"
#define AM_AGENTS_CONFIG_URL_VALIDATE_PING_OK "com.forgerock.agents.ext.url.validation.ping.ok.count"        
#define AM_AGENTS_CONFIG_URL_VALIDATE_DEFAULT_SET "com.forgerock.agents.ext.url.validation.default.url.set"        

#define AM_AGENTS_CONFIG_HOST_MAP "com.forgerock.agents.config.hostmap"

#define AM_AGENTS_CONFIG_RETRY_MAX "com.forgerock.agents.init.retry.max"
#define AM_AGENTS_CONFIG_RETRY_WAIT "com.forgerock.agents.init.retry.wait"

/* other options */

#define AM_AGENTS_CONFIG_AGENT_URI "com.sun.identity.agents.config.agenturi.prefix"
#define AM_AGENTS_CONFIG_COOKIE_NAME "com.sun.identity.agents.config.cookie.name"
#define AM_AGENTS_CONFIG_LOGIN_URL_MAP "com.sun.identity.agents.config.login.url"        
#define AM_AGENTS_CONFIG_COOKIE_SECURE "com.sun.identity.agents.config.cookie.secure"        
#define AM_AGENTS_CONFIG_NOTIF_ENABLE "com.sun.identity.agents.config.notification.enable"        
#define AM_AGENTS_CONFIG_NOTIF_URL "com.sun.identity.client.notification.url"        
#define AM_AGENTS_CONFIG_CMP_CASE_IGNORE "com.sun.identity.agents.config.url.comparison.case.ignore"

#define AM_AGENTS_CONFIG_POLICY_CACHE_VALID "com.sun.identity.agents.config.policy.cache.polling.interval"        
#define AM_AGENTS_CONFIG_TOKEN_CACHE_VALID "com.sun.identity.agents.config.sso.cache.polling.interval"       

#define AM_AGENTS_CONFIG_UID_PARAM "com.sun.identity.agents.config.userid.param"        
#define AM_AGENTS_CONFIG_UID_PARAM_TYPE "com.sun.identity.agents.config.userid.param.type"

#define AM_AGENTS_CONFIG_ATTR_PROFILE_MODE "com.sun.identity.agents.config.profile.attribute.fetch.mode"
#define AM_AGENTS_CONFIG_ATTR_PROFILE_MAP "com.sun.identity.agents.config.profile.attribute.mapping"        
#define AM_AGENTS_CONFIG_ATTR_SESSION_MODE "com.sun.identity.agents.config.session.attribute.fetch.mode"
#define AM_AGENTS_CONFIG_ATTR_SESSION_MAP "com.sun.identity.agents.config.session.attribute.mapping"
#define AM_AGENTS_CONFIG_ATTR_RESPONSE_MODE "com.sun.identity.agents.config.response.attribute.fetch.mode"
#define AM_AGENTS_CONFIG_ATTR_RESPONSE_MAP "com.sun.identity.agents.config.response.attribute.mapping"

#define AM_AGENTS_CONFIG_LB_ENABLE "com.sun.identity.agents.config.load.balancer.enable"
#define AM_AGENTS_CONFIG_SSO_ONLY "com.sun.identity.agents.config.sso.only"
#define AM_AGENTS_CONFIG_ACCESS_DENIED_URL "com.sun.identity.agents.config.access.denied.url"
#define AM_AGENTS_CONFIG_FQDN_CHECK_ENABLE "com.sun.identity.agents.config.fqdn.check.enable"    
#define AM_AGENTS_CONFIG_FQDN_DEFAULT "com.sun.identity.agents.config.fqdn.default"       
#define AM_AGENTS_CONFIG_FQDN_MAP "com.sun.identity.agents.config.fqdn.mapping"      

#define AM_AGENTS_CONFIG_COOKIE_RESET_ENABLE "com.sun.identity.agents.config.cookie.reset.enable"
#define AM_AGENTS_CONFIG_COOKIE_RESET_MAP "com.sun.identity.agents.config.cookie.reset"

#define AM_AGENTS_CONFIG_NOT_ENFORCED_URL "com.sun.identity.agents.config.notenforced.url"
#define AM_AGENTS_CONFIG_NOT_ENFORCED_INVERT "com.sun.identity.agents.config.notenforced.url.invert"
#define AM_AGENTS_CONFIG_NOT_ENFORCED_ATTR "com.sun.identity.agents.config.notenforced.url.attributes.enable"   
#define AM_AGENTS_CONFIG_NOT_ENFORCED_IP "com.sun.identity.agents.config.notenforced.ip"

#define AM_AGENTS_CONFIG_PDP_ENABLE "com.sun.identity.agents.config.postdata.preserve.enable"
#define AM_AGENTS_CONFIG_PDP_VALID "com.sun.identity.agents.config.postcache.entry.lifetime"
#define AM_AGENTS_CONFIG_PDP_COOKIE "com.sun.identity.agents.config.postdata.preserve.lbcookie"

#define AM_AGENTS_CONFIG_CLIENT_IP_VALIDATE "com.sun.identity.agents.config.client.ip.validation.enable"
#define AM_AGENTS_CONFIG_ATTR_COOKIE_PREFIX "com.sun.identity.agents.config.profile.attribute.cookie.prefix"
#define AM_AGENTS_CONFIG_ATTR_COOKIE_MAX_AGE "com.sun.identity.agents.config.profile.attribute.cookie.maxage"

#define AM_AGENTS_CONFIG_CDSSO_ENABLE "com.sun.identity.agents.config.cdsso.enable"
#define AM_AGENTS_CONFIG_CDSSO_LOGIN "com.sun.identity.agents.config.cdsso.cdcservlet.url"
#define AM_AGENTS_CONFIG_CDSSO_DOMAIN "com.sun.identity.agents.config.cdsso.cookie.domain"

#define AM_AGENTS_CONFIG_LOGOUT_URL "com.sun.identity.agents.config.logout.url"
#define AM_AGENTS_CONFIG_APP_LOGOUT_URL "com.sun.identity.agents.config.agent.logout.url"
#define AM_AGENTS_CONFIG_LOGOUT_REDIRECT_URL "com.sun.identity.agents.config.logout.redirect.url"
#define AM_AGENTS_CONFIG_LOGOUT_COOKIE_RESET "com.sun.identity.agents.config.logout.cookie.reset"

#define AM_AGENTS_CONFIG_POLICY_SCOPE "com.sun.identity.agents.config.fetch.from.root.resource"

#define AM_AGENTS_CONFIG_RESOLVE_CLIENT_HOST "com.sun.identity.agents.config.get.client.host.name"

#define AM_AGENTS_CONFIG_POLICY_ENCODE_SPECIAL_CHAR "com.sun.identity.agents.config.encode.url.special.chars.enable"
#define AM_AGENTS_CONFIG_COOKIE_ENCODE_SPECIAL_CHAR "com.sun.identity.agents.config.encode.cookie.special.chars.enable "

#define AM_AGENTS_CONFIG_OVERRIDE_PROTO "com.sun.identity.agents.config.override.protocol"
#define AM_AGENTS_CONFIG_OVERRIDE_HOST "com.sun.identity.agents.config.override.host"
#define AM_AGENTS_CONFIG_OVERRIDE_PORT "com.sun.identity.agents.config.override.port"        
#define AM_AGENTS_CONFIG_OVERRIDE_NOTIFICATION_URL "com.sun.identity.agents.config.override.notification.url"       

#define AM_AGENTS_CONFIG_VALID "com.sun.identity.agents.config.polling.interval"

#define AM_AGENTS_CONFIG_PASSWORD_REPLAY_KEY "com.sun.identity.agents.config.replaypasswd.key"

#define AM_AGENTS_CONFIG_POLICY_CLOCK_SKEW "com.sun.identity.agents.config.policy.clock.skew"

#define AM_AGENTS_CONFIG_GOTO_PARAM_NAME "com.sun.identity.agents.config.redirect.param"

#define AM_AGENTS_CONFIG_CACHE_CONTROL_ENABLE "com.forgerock.agents.cache_control_header.enable"

#define AM_AGENTS_CONFIG_USE_REDIRECT_ADVICE "com.sun.am.use_redirect_for_advice"

#define AM_AGENTS_CONFIG_CLIENT_IP_HEADER "com.sun.identity.agents.config.client.ip.header"
#define AM_AGENTS_CONFIG_CLIENT_HOSTNAME_HEADER "com.sun.identity.agents.config.client.hostname.header"

#define AM_AGENTS_CONFIG_INVALID_URL "com.forgerock.agents.agent.invalid.url.regex"

#define AM_AGENTS_CONFIG_CONDITIONAL_LOGIN_URL "com.forgerock.agents.conditional.login.url"

#define AM_AGENTS_CONFIG_COOKIE_HTTP_ONLY "com.sun.identity.cookie.httponly"
#define AM_AGENTS_CONFIG_COOKIE_SECURE "com.sun.identity.agents.config.cookie.secure"
#define AM_AGENTS_CONFIG_MULTI_VALUE_SEPARATOR "com.sun.identity.agents.config.attribute.multi.value.separator"

#define AM_AGENTS_CONFIG_IIS_LOGON_USER "com.sun.identity.agents.config.iis.logonuser"
#define AM_AGENTS_CONFIG_IIS_PASSWORD_HEADER "com.sun.identity.agents.config.iis.password.header"

#define AM_AGENTS_CONFIG_PDP_JS_REPOST "org.forgerock.agents.pdp.javascript.repost"

#endif
