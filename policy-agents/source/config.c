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

struct am_instance {
    struct offset_list list; /* list of instance configurations */
};

struct am_instance_entry {
    time_t ts;
    unsigned long instance_id;
    char token[AM_SHARED_CACHE_KEY_SIZE];
    char name[AM_SHARED_CACHE_KEY_SIZE]; /*agent id*/
    char config[AM_PATH_SIZE]; /*config file name*/
    size_t xml_sz;
    struct offset_list lh;
    char xml[1];
};

static am_shm_t *conf = NULL;

int am_configuration_init() {
    int status = AM_SUCCESS;
    if (conf != NULL)
        return status;

    conf = am_shm_create("am_shared_conf", sizeof (struct am_instance) * 2048 * AM_MAX_INSTANCES);
    if (conf == NULL) {
        return AM_ERROR;
    }
    if (conf->error != AM_SUCCESS) {
        return conf->error;
    }

    am_shm_lock(conf);
    if (conf->init == 1) {
        /* allocate the table itself */
        struct am_instance *instance_data = (struct am_instance *) am_shm_alloc(conf, sizeof (struct am_instance));
        if (instance_data != NULL) {
            /* initialize head node */
            instance_data->list.next = instance_data->list.prev = 0;
            conf->user = instance_data;
            /*store instance_data offset (for other processes)*/
            am_shm_set_user_offset(conf, am_get_offset(conf->pool, instance_data));
        } else {
            conf->user = NULL;
            status = AM_ENOMEM;
        }
    }
    am_shm_unlock(conf);
    return status;
}

int am_configuration_shutdown() {
    am_shm_shutdown(conf);
    return 0;
}

static struct am_instance_entry *get_instance_entry(unsigned long instance_id) {
    struct am_instance_entry *e, *t, *h;
    struct am_instance *instance_data = conf != NULL ? (struct am_instance *) conf->user : NULL;

    if (instance_data != NULL) {
        h = (struct am_instance_entry *) am_get_pointer(conf->pool, instance_data->list.prev);

        am_offset_list_for_each(conf->pool, h, e, t, struct am_instance_entry) {
            if (instance_id == e->instance_id) {
                return e;
            }
        }
    }
    return NULL;
}

static int delete_instance_entry(struct am_instance_entry *e) {
    int rv = 0;
    struct am_instance *instance_data = (struct am_instance *) conf->user;

    if (e == NULL) return AM_EINVAL;
    /* remove a node from a doubly linked list */
    if (e->lh.prev == 0) {
        instance_data->list.prev = e->lh.next;
    } else {
        ((struct am_instance_entry *) am_get_pointer(conf->pool, e->lh.prev))->lh.next = e->lh.next;
    }

    if (e->lh.next == 0) {
        instance_data->list.next = e->lh.prev;
    } else {
        ((struct am_instance_entry *) am_get_pointer(conf->pool, e->lh.next))->lh.prev = e->lh.prev;
    }
    return rv;
}

void remove_agent_instance_byname(const char *name) {
    struct am_instance_entry *e, *t, *h;
    struct am_instance *instance_data = conf != NULL ? (struct am_instance *) conf->user : NULL;

    if (instance_data == NULL) return;
    h = (struct am_instance_entry *) am_get_pointer(conf->pool, instance_data->list.prev);

    am_shm_lock(conf);

    am_offset_list_for_each(conf->pool, h, e, t, struct am_instance_entry) {
        if (strcmp(e->name, name) == 0) {
            am_remove_cache_entry(e->instance_id, e->token); /*delete cached agent session data*/
            am_agent_init_set_value(e->instance_id, AM_TRUE, AM_FALSE); /*set this instance 'unconfigured'*/
            delete_instance_entry(e); /*remove cached configuration data*/
            break;
        }
    }
    am_shm_unlock(conf);
}

static int am_set_agent_config(unsigned long instance_id, const char *xml,
        size_t xsz, const char *token, const char *config_file, const char *name) {
    const char *thisfunc = "am_set_agent_config():";
    struct am_instance_entry *c;
    int ret;
    char *cxml = NULL;
    size_t xs = xsz;
    struct am_instance *instance_data = conf != NULL ? (struct am_instance *) conf->user : NULL;

    if (instance_data == NULL) return AM_ENOMEM;

    am_shm_lock(conf);

    c = get_instance_entry(instance_id);
    if (xml == NULL && c != NULL) {
        ret = delete_instance_entry(c);
        if (ret == AM_SUCCESS) {
            am_shm_free(conf, c);
        }
        am_shm_unlock(conf);
        return ret;
    }
    am_log_debug(instance_id, "%s data size %ld", thisfunc, xs);

    if (!gzip_deflate(xml, &xs, &cxml)) {
        am_log_debug(instance_id, "%s compressed data size %ld", thisfunc, xs);
    } else {
        am_log_error(instance_id, "%s data compression failed", thisfunc);
        if (cxml != NULL) free(cxml);
        if (c != NULL) {
            am_shm_free(conf, c);
        }
        am_shm_unlock(conf);
        return AM_ERROR;
    }

    if (c != NULL) {
        c = am_shm_realloc(conf, c, sizeof (struct am_instance_entry) +xs);
    } else {
        c = am_shm_alloc(conf, sizeof (struct am_instance_entry) +xs);
    }

    if (c == NULL) {
        am_log_error(instance_id, "%s failed to allocate %ld bytes",
                thisfunc, sizeof (struct am_instance_entry) +xs);
        if (cxml != NULL) free(cxml);
        am_shm_unlock(conf);
        return AM_ENOMEM;
    }

    c->ts = time(NULL);
    c->instance_id = instance_id;
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

    c->xml_sz = xs;
    memcpy(c->xml, cxml, c->xml_sz);
    if (cxml != NULL) free(cxml);

    c->lh.next = c->lh.prev = 0;
    am_offset_list_insert(conf->pool, c, &(instance_data->list), struct am_instance_entry);

    am_shm_unlock(conf);
    return AM_SUCCESS;
}

int am_get_agent_config(unsigned long instance_id, const char *config_file, am_config_t **cnf) {
    const char *thisfunc = "am_get_agent_config():";
    struct am_instance_entry *c;
    int rv = 1, in_progress = AM_FALSE;

    char *profile_xml = NULL;
    size_t profile_xml_sz = 0;

    unsigned int max_retry = 0;
    unsigned int retry = 0, retry_wait = 2; //TODO: conf values
    max_retry = retry = 3;

    if (conf == NULL) {
        am_log_error(instance_id, "%s unable to fetch agent configuration (shared memory error)",
                thisfunc);
        return AM_ENOMEM;
    }

    do {

        am_shm_lock(conf);
        c = get_instance_entry(instance_id);
        if (c == NULL) {
            am_request_t r;
            int login_status;
            char *agent_token = NULL;
            struct am_namevalue *agent_session = NULL;
            am_config_t *ac = NULL;

            am_shm_unlock(conf);
            am_agent_instance_init_lock();

            in_progress = am_agent_init_get_value(instance_id, AM_FALSE);

            am_log_debug(instance_id, "%s agent configuration fetch in progress: %d",
                    thisfunc, in_progress);

            if (in_progress == AM_TRUE) {
                am_agent_instance_init_unlock();
                am_log_warning(instance_id, "%s retry %d",
                        thisfunc, (retry - max_retry) + 1);
                sleep(retry_wait);
                continue;
            }

            am_agent_init_set_value(instance_id, AM_FALSE, AM_TRUE); /*configuration fetch in progress*/

            ac = am_get_config_file(instance_id, config_file);
            if (ac == NULL) {
                am_agent_init_set_value(instance_id, AM_FALSE, AM_FALSE);
                am_agent_instance_init_unlock();
                am_log_error(instance_id, "%s failed to load instance bootstrap %ld data",
                        thisfunc, instance_id);
                return AM_FILE_ERROR; /*fatal*/
            }

            memset(&r, 0, sizeof (am_request_t));
            r.conf = ac;
            r.instance_id = instance_id;
            login_status = am_agent_login(instance_id, get_valid_openam_url(&r), NOTNULL(ac->notif_url),
                    ac->user, ac->pass, ac->key, ac->realm, ac->local,
                    &agent_token, &profile_xml, &profile_xml_sz, &agent_session, NULL);
            if (login_status == AM_SUCCESS && ISVALID(agent_token) && agent_session != NULL) {

                am_log_debug(instance_id, "%s agent login%s succeeded", thisfunc,
                        ISVALID(profile_xml) ? " and profile fetch" : "");

                /* merge with bootstrap configuration */
                if (am_config_update_xml(ac, /* for local configuration - merge all */
                        !ISVALID(profile_xml) ? AM_TRUE : AM_FALSE,
                        &profile_xml, &profile_xml_sz) == 0) {

                    if (am_set_agent_config(instance_id, profile_xml, profile_xml_sz,
                            agent_token, config_file, ac->user) == AM_SUCCESS) {

                        am_add_session_policy_cache_entry(&r, agent_token,
                                NULL, agent_session);

                        if (cnf != NULL) {
                            *cnf = am_parse_config_xml(instance_id, profile_xml, profile_xml_sz, AM_TRUE);
                            if (*cnf == NULL) {
                                am_config_free(&ac);
                                if (agent_session != NULL) delete_am_namevalue_list(&agent_session);
                                if (agent_token != NULL) free(agent_token);
                                if (profile_xml != NULL) free(profile_xml);
                                am_agent_init_set_value(instance_id, AM_FALSE, AM_FALSE);
                                am_agent_instance_init_unlock();
                                am_log_error(instance_id, "%s failed to parse agent profile xml",
                                        thisfunc);
                                return AM_XML_ERROR;
                            }

                            (*cnf)->ts = time(NULL);
                            (*cnf)->token = strdup(agent_token);
                            (*cnf)->config = strdup(config_file);
                            am_log_debug(instance_id, "%s agent configuration read from a server",
                                    thisfunc);
                        }
                    } else {
                        am_log_warning(instance_id, "%s failed to store agent configuration",
                                thisfunc); //TODO: retry
                    }
                } else {
                    am_log_warning(instance_id, "%s failed to merge agent configuration",
                            thisfunc); //TODO: retry
                }

                am_config_free(&ac);
                delete_am_namevalue_list(&agent_session);
                agent_session = NULL;
            } else {
                am_config_free(&ac);
                if (agent_session != NULL) delete_am_namevalue_list(&agent_session);
                if (agent_token != NULL) free(agent_token);
                if (profile_xml != NULL) free(profile_xml);
                profile_xml = agent_token = NULL;
                agent_session = NULL;
                am_log_warning(instance_id, "%s retry %d (login failure)",
                        thisfunc, (retry - max_retry) + 1);
                am_agent_init_set_value(instance_id, AM_FALSE, AM_FALSE);
                am_agent_instance_init_unlock();
                sleep(retry_wait);
                continue;
            }

            if (agent_session != NULL) delete_am_namevalue_list(&agent_session);
            if (agent_token != NULL) free(agent_token);
            if (profile_xml != NULL) free(profile_xml);

            am_agent_instance_init_unlock();
            return AM_SUCCESS;
        }

        profile_xml_sz = c->xml_sz;
        if (!gzip_inflate(c->xml, &profile_xml_sz, &profile_xml)) {
            if (cnf != NULL) {
                *cnf = am_parse_config_xml(instance_id, profile_xml, profile_xml_sz, AM_FALSE);
                if (*cnf == NULL) {
                    am_log_error(instance_id, "%s failed to parse agent profile xml",
                            thisfunc);
                    rv = AM_XML_ERROR;
                    am_shm_unlock(conf);
                    break;
                }
                (*cnf)->ts = c->ts;
                (*cnf)->token = strdup(c->token);
                (*cnf)->config = strdup(c->config);
                am_log_debug(instance_id, "%s agent configuration read from a cache",
                        thisfunc);
            }
            rv = AM_SUCCESS;
        }
        am_shm_unlock(conf);
        break;

    } while (--max_retry != 0);

    if (profile_xml != NULL) free(profile_xml);

    if (max_retry == 0) {
        am_log_error(instance_id,
                "%s failed to locate instance configuration %ld data (max %d retries exhausted)",
                thisfunc, instance_id, retry);
        return AM_RETRY_ERROR; /*fatal*/
    }

    return rv;
}
