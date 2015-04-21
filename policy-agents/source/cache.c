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

/*
 * Session and Policy response attribute cache
 * ===============================================================
 * key: 'token value'
 * 
 * Policy ResourceResult name (only) cache
 * ===============================================================
 * key: 'url value'
 * 
 * PDP cache:
 * ===============================================================
 * key: 'uuid value'
 * 
 */

enum {
    AM_CACHE_SESSION = 1 << 0, /*cache entry type - session data*/
    AM_CACHE_PDP = 1 << 1, /*cache entry type - pdp data*/
    AM_CACHE_POLICY = 1 << 2, /*cache entry type - policy response*/

    AM_CACHE_POLICY_RESPONSE_A = 1 << 3, /*attribute identifiers in policy response data (list)*/
    AM_CACHE_POLICY_RESPONSE_D = 1 << 4,
    AM_CACHE_POLICY_ACTION = 1 << 5,
    AM_CACHE_POLICY_ADVICE = 1 << 6,
    AM_CACHE_POLICY_ALLOW = 1 << 7,
    AM_CACHE_POLICY_DENY = 1 << 8
};

struct am_cache_entry_data {
    unsigned int type;
    int index;
    int scope;
    char method;
    uint64_t ttl;
    size_t size[3];
    struct offset_list lh;
    char value[1]; /*format: value\0value\0value\0 */
};

struct am_cache_entry {
    char key[AM_SHARED_CACHE_KEY_SIZE];
    time_t ts; /*create timestamp*/
    int valid; /*entry is valid, in sec*/
    unsigned long instance_id;
    struct offset_list data;
    struct offset_list lh; /*collisions*/
};

struct am_cache {
    size_t count;
    struct offset_list table[AM_SHARED_CACHE_SIZE]; /* first,last */
};

static am_shm_t *cache = NULL;

int am_cache_init() {
    size_t i;
    if (cache != NULL) return AM_SUCCESS;

    cache = am_shm_create("am_shared_cache", sizeof (struct am_cache) +
            (sizeof (struct am_cache_entry) + sizeof (struct am_cache_entry_data)) * 2048);
    if (cache == NULL) {
        return AM_ERROR;
    }
    if (cache->error != AM_SUCCESS) {
        return cache->error;
    }

    if (cache->init) {
        struct am_cache *cache_data = (struct am_cache *) am_shm_alloc(cache, sizeof (struct am_cache));
        if (cache_data == NULL) {
            cache->user = NULL;
            return AM_ENOMEM;
        }
        am_shm_lock(cache);
        cache_data->count = 0;
        /* initialize head nodes */
        for (i = 0; i < AM_SHARED_CACHE_SIZE; i++) {
            cache_data->table[i].next = cache_data->table[i].prev = 0;
        }
        cache->user = cache_data;
        /*store table offset (for other processes)*/
        am_shm_set_user_offset(cache, AM_GET_OFFSET(cache->pool, cache_data));
        am_shm_unlock(cache);
    }

    return AM_SUCCESS;
}

int am_cache_shutdown() {
    am_shm_shutdown(cache);
    return AM_SUCCESS;
}

static unsigned int index_for(unsigned int tablelength, unsigned int hashvalue) {
    return (hashvalue % tablelength);
}

static unsigned int sdbm_hash(void *s) {
    unsigned long hash = 0;
    int c;
    unsigned char *str = (unsigned char *) s;
    while ((c = *str++)) {
        hash = c + (hash << 6) + (hash << 16) - hash;
    }
    return (unsigned int) hash;
}

static unsigned int hash(void *k) {
    unsigned int i = sdbm_hash(k);
    i += ~(i << 9);
    i ^= ((i >> 14) | (i << 18));
    i += (i << 4);
    i ^= ((i >> 10) | (i << 22));
    return i;
}

static struct am_cache_entry *get_cache_entry(const char *key, int *index) {
    struct am_cache_entry *e, *t, *h;
    unsigned int key_hash;
    int entry_index;
    struct am_cache *cache_data = cache != NULL ? (struct am_cache *) cache->user : NULL;

    if (cache_data != NULL) {
        key_hash = hash((void *) key);
        entry_index = index_for(AM_SHARED_CACHE_SIZE, key_hash);
        h = (struct am_cache_entry *) AM_GET_POINTER(cache->pool, cache_data->table[entry_index].prev);

        AM_OFFSET_LIST_FOR_EACH(cache->pool, h, e, t, struct am_cache_entry) {
            if (strcmp(key, e->key) == 0) {
                if (index) *index = entry_index;
                return e;
            }
        }
    }
    return NULL;
}

static int delete_cache_entry(int entry_index, struct am_cache_entry *e) {
    int rv = 0;
    struct am_cache_entry_data *i, *t, *h;
    struct am_cache *cache_data = (struct am_cache *) cache->user;

    if (e == NULL) return AM_EINVAL;
    /* cleanup cache entry data */
    h = (struct am_cache_entry_data *) AM_GET_POINTER(cache->pool, e->data.prev);

    AM_OFFSET_LIST_FOR_EACH(cache->pool, h, i, t, struct am_cache_entry_data) {
        am_shm_free(cache, i);
    }

    /* remove a node from a doubly linked list */
    if (e->lh.prev == 0) {
        cache_data->table[entry_index].prev = e->lh.next;
    } else {
        ((struct am_cache_entry *) AM_GET_POINTER(cache->pool, e->lh.prev))->lh.next = e->lh.next;
    }

    if (e->lh.next == 0) {
        cache_data->table[entry_index].next = e->lh.prev;
    } else {
        ((struct am_cache_entry *) AM_GET_POINTER(cache->pool, e->lh.next))->lh.prev = e->lh.prev;
    }
    return rv;
}

/* 
 * Find PDP cache entry (key: uuid value).
 */
int am_get_pdp_cache_entry(am_request_t *r, const char *key, char **data, size_t *data_sz, char **content_type) {
    static const char *thisfunc = "am_get_pdp_cache_entry():";
    int status = AM_NOT_FOUND;
    int entry_index = 0;
    struct am_cache_entry *c;
    struct am_cache_entry_data *e, *t, *h;

    struct am_cache *cache_data = cache != NULL ? (struct am_cache *) cache->user : NULL;

    if (cache_data == NULL) return AM_ENOMEM;
    if (!ISVALID(key)) return AM_EINVAL;
    am_shm_lock(cache);

    c = get_cache_entry(key, &entry_index);
    if (c == NULL) {
        AM_LOG_WARNING(r->instance_id, "%s failed to locate data for a key (%s)",
                thisfunc, key);
        am_shm_unlock(cache);
        return AM_NOT_FOUND;
    }

    if (r->conf->pdp_cache_valid > 0) {
        time_t ts = c->ts;
        ts += r->conf->pdp_cache_valid;
        if (difftime(time(NULL), ts) >= 0) {
            char tsc[32], tsu[32];
            struct tm created, until;
            localtime_r(&c->ts, &created);
            localtime_r(&ts, &until);
            strftime(tsc, sizeof (tsc), AM_CACHE_TIMEFORMAT, &created);
            strftime(tsu, sizeof (tsu), AM_CACHE_TIMEFORMAT, &until);
            AM_LOG_WARNING(r->instance_id, "%s data for a key (%s) is obsolete (created: %s, valid until: %s)",
                    thisfunc, key, tsc, tsu);

            h = (struct am_cache_entry_data *) AM_GET_POINTER(cache->pool, c->data.prev);

            AM_OFFSET_LIST_FOR_EACH(cache->pool, h, e, t, struct am_cache_entry_data) {
                if (e->type == AM_CACHE_PDP && e->size[1] != 0) {
                    char *file = e->value + e->size[0] + 1;
                    if (ISVALID(file)) {
                        unlink(file);
                        break;
                    }
                }
            }

            if (!delete_cache_entry(entry_index, c)) {
                am_shm_free(cache, c);
                cache_data->count--;
            }
            am_shm_unlock(cache);
            return AM_ETIMEDOUT;
        }
    }

    h = (struct am_cache_entry_data *) AM_GET_POINTER(cache->pool, c->data.prev);

    AM_OFFSET_LIST_FOR_EACH(cache->pool, h, e, t, struct am_cache_entry_data) {
        if (e->type == AM_CACHE_PDP && e->size[0] > 0 && e->size[1] > 0 && e->size[2] > 0) {
            *data = malloc(e->size[0] + e->size[1] + 2);
            if (*data != NULL) {
                memcpy(*data, e->value, e->size[0] + e->size[1] + 2);
                *data_sz = e->size[0]; /*report url size only*/
            }

            *content_type = malloc(e->size[2] + 1);
            if (*content_type != NULL) {
                memcpy(*content_type, e->value + e->size[0] + e->size[1] + 2, e->size[2]);
                (*content_type)[e->size[2]] = 0;
            }
            status = AM_SUCCESS;
            break;
        }
    }

    am_shm_unlock(cache);
    return status;
}

/* 
 * Add PDP cache entry (key: uuid value).
 */
int am_add_pdp_cache_entry(am_request_t *request, const char *key, const char *url,
        const char *file, const char *content_type) {
    static const char *thisfunc = "am_add_pdp_cache_entry():";
    unsigned int key_hash;
    int entry_index = 0;
    size_t url_length, file_length, content_type_length;
    struct am_cache_entry *cache_entry;
    struct am_cache_entry_data *cache_entry_data;
    struct am_cache *cache_data;

    if (cache == NULL || cache->user == NULL) {
        return AM_ENOMEM;
    }
    if (!ISVALID(key) || !ISVALID(url) || !ISVALID(file) || !ISVALID(content_type)) {
        return AM_EINVAL;
    }
    if (strlen(key) >= AM_SHARED_CACHE_KEY_SIZE) {
        return AM_E2BIG;
    }

    cache_data = (struct am_cache *) cache->user;

    url_length = strlen(url);
    file_length = strlen(file);
    content_type_length = strlen(content_type);

    key_hash = hash((void *) key);
    entry_index = index_for(AM_SHARED_CACHE_SIZE, key_hash);

    am_shm_lock(cache);

    cache_entry = get_cache_entry(key, NULL);
    if (cache_entry != NULL) {
        if (!delete_cache_entry(entry_index, cache_entry)) {
            am_shm_free(cache, cache_entry);
            cache_data->count--;
            cache_entry = NULL;
        } else {
            AM_LOG_ERROR(request->instance_id, "%s failed to remove cache entry (%s)",
                    thisfunc, key);
            am_shm_unlock(cache);
            return AM_ERROR;
        }
    }

    cache_entry = am_shm_alloc(cache, sizeof (struct am_cache_entry));
    if (cache_entry == NULL) {
        AM_LOG_ERROR(request->instance_id, "%s failed to allocate %ld bytes",
                thisfunc, sizeof (struct am_cache_entry));
        am_shm_unlock(cache);
        return AM_ENOMEM;
    }

    cache_entry->ts = time(NULL);
    cache_entry->valid = request->conf->pdp_cache_valid;
    cache_entry->instance_id = request->instance_id;
    strncpy(cache_entry->key, key, sizeof (cache_entry->key) - 1);

    cache_entry->data.next = cache_entry->data.prev = 0;
    cache_entry->lh.next = cache_entry->lh.prev = 0;
    AM_OFFSET_LIST_INSERT(cache->pool, cache_entry, &(cache_data->table[entry_index]), struct am_cache_entry);

    cache_entry_data = am_shm_alloc(cache, sizeof (struct am_cache_entry_data) +url_length + file_length + content_type_length + 3);
    if (cache_entry_data == NULL) {
        AM_LOG_ERROR(request->instance_id, "%s failed to allocate %ld bytes",
                thisfunc, sizeof (struct am_cache_entry_data));
        am_shm_unlock(cache);
        return AM_ENOMEM;
    }

    cache_entry_data->type = AM_CACHE_PDP;
    cache_entry_data->method = AM_REQUEST_UNKNOWN;
    cache_entry_data->ttl = 0;
    cache_entry_data->size[0] = url_length;
    cache_entry_data->size[1] = file_length;
    cache_entry_data->size[2] = content_type_length;
    memcpy(cache_entry_data->value, url, cache_entry_data->size[0]);
    cache_entry_data->value[cache_entry_data->size[0]] = 0;
    memcpy(cache_entry_data->value + cache_entry_data->size[0] + 1, file, cache_entry_data->size[1]);
    cache_entry_data->value[cache_entry_data->size[0] + cache_entry_data->size[1] + 1] = 0;
    memcpy(cache_entry_data->value + cache_entry_data->size[0] + cache_entry_data->size[1] + 2, content_type,
            cache_entry_data->size[2]);
    cache_entry_data->value[cache_entry_data->size[0] + cache_entry_data->size[1] + cache_entry_data->size[2] + 2] = 0;
    cache_entry_data->lh.next = cache_entry_data->lh.prev = 0;

    AM_OFFSET_LIST_INSERT(cache->pool, cache_entry_data, &(cache_entry->data), struct am_cache_entry_data);

    cache_data->count += 1;

    am_shm_unlock(cache);
    return AM_SUCCESS;
}

/*
 * Delete a shared cache entry (key: any)
 */
int am_remove_cache_entry(unsigned long instance_id, const char *key) {
    static const char *thisfunc = "am_remove_cache_entry():";
    int entry_index = 0;
    int rv;
    struct am_cache_entry *cache_entry;
    struct am_cache *cache_data;

    if (cache == NULL || cache->user == NULL) {
        return AM_ENOMEM;
    }
    if (!ISVALID(key)) {
        return AM_EINVAL;
    }
    am_shm_lock(cache);

    cache_data = (struct am_cache *) cache->user;
    cache_entry = get_cache_entry(key, &entry_index);
    if (cache_entry == NULL) {
        AM_LOG_WARNING(instance_id, "%s cache data is not available (%s)", thisfunc, key);
        am_shm_unlock(cache);
        return AM_NOT_FOUND;
    }

    rv = delete_cache_entry(entry_index, cache_entry);
    if (rv != 0) {
        AM_LOG_ERROR(instance_id, "%s failed to remove cache entry (%s)", thisfunc, key);
    } else {
        am_shm_free(cache, cache_entry);
        cache_data->count--;
        AM_LOG_DEBUG(instance_id, "%s cache entry removed (%s)", thisfunc, key);
    }
    am_shm_unlock(cache);
    return rv;
}

/* 
 * Find session/policy response cache entry (key: session token).
 */
int am_get_session_policy_cache_entry(am_request_t *r, const char *key,
        struct am_policy_result **policy, struct am_namevalue **session, time_t *ets) {
    static const char *thisfunc = "am_get_session_policy_cache_entry():";
    int i = -1, entry_index, status = AM_NOT_FOUND;
    struct am_cache_entry *c;
    struct am_cache_entry_data *a, *t, *h;

    struct am_cache *cache_data = cache != NULL ? (struct am_cache *) cache->user : NULL;
    struct am_namevalue *sesion_attrs = NULL;
    struct am_policy_result *pol_attrs = NULL, *pol_curr = NULL;

    if (cache_data == NULL) return AM_ENOMEM;
    if (!ISVALID(key)) return AM_EINVAL;
    am_shm_lock(cache);

    c = get_cache_entry(key, &entry_index);
    if (c == NULL) {
        AM_LOG_WARNING(r->instance_id, "%s failed to locate data for a key (%s)",
                thisfunc, key);
        am_shm_unlock(cache);
        return AM_NOT_FOUND;
    }

    if (c->valid > 0) {
        time_t ts = c->ts;
        ts += c->valid;
        if (difftime(time(NULL), ts) >= 0) {
            char tsc[32], tsu[32];
            struct tm created, until;
            localtime_r(&c->ts, &created);
            localtime_r(&ts, &until);
            strftime(tsc, sizeof (tsc), AM_CACHE_TIMEFORMAT, &created);
            strftime(tsu, sizeof (tsu), AM_CACHE_TIMEFORMAT, &until);
            AM_LOG_WARNING(r->instance_id, "%s data for a key (%s) is obsolete (created: %s, valid until: %s)",
                    thisfunc, key, tsc, tsu);

            /*if (!delete_cache_entry(entry_index, c)) {
                am_shm_free(cache, c);
                cache_data->count--;
            }
            am_shm_unlock(cache);
            return AM_ETIMEDOUT;*/

            *ets = c->ts;
        }
    }

    h = (struct am_cache_entry_data *) AM_GET_POINTER(cache->pool, c->data.prev);

    AM_OFFSET_LIST_FOR_EACH(cache->pool, h, a, t, struct am_cache_entry_data) {

        if (a->type == AM_CACHE_SESSION && a->size[0] > 0 && a->size[1] > 0) {
            struct am_namevalue *el = NULL;
            if (create_am_namevalue_node(a->value, a->size[0], a->value + a->size[0] + 1, a->size[1], &el) == 0) {
                AM_LIST_INSERT(sesion_attrs, el);
            }
        } else if ((a->type & AM_CACHE_POLICY) == AM_CACHE_POLICY) {

            if (/*a->type == AM_CACHE_POLICY && a->index != -1 && a->scope != -1 &&*/ i != a->index) {
                struct am_policy_result *el = NULL;
                if (create_am_policy_result_node(a->value, a->size[0], &el) == 0) {
                    AM_LIST_INSERT(pol_attrs, el);
                    el->index = i = a->index;
                    el->scope = a->scope;
                    pol_curr = el;
                }
            }

            if (pol_curr == NULL) continue;

            if (a->type == AM_CACHE_POLICY && i == a->index && pol_curr != NULL) {
                am_free(pol_curr->resource);
                pol_curr->resource = strndup(a->value, a->size[0]);
                pol_curr->scope = a->scope;
            }

            if ((a->type & AM_CACHE_POLICY_RESPONSE_A) == AM_CACHE_POLICY_RESPONSE_A && a->size[0] > 0 && a->size[1] > 0) {
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node(a->value, a->size[0], a->value + a->size[0] + 1, a->size[1], &el) == 0) {
                    AM_LIST_INSERT(pol_curr->response_attributes, el);
                }
            }
            if ((a->type & AM_CACHE_POLICY_RESPONSE_D) == AM_CACHE_POLICY_RESPONSE_D && a->size[0] > 0 && a->size[1] > 0) {
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node(a->value, a->size[0], a->value + a->size[0] + 1, a->size[1], &el) == 0) {
                    AM_LIST_INSERT(pol_curr->response_decisions, el);
                }
            }
            if ((a->type & AM_CACHE_POLICY_ACTION) == AM_CACHE_POLICY_ACTION) {
                char act = AM_FALSE;
                struct am_action_decision *el = NULL;
                if ((a->type & AM_CACHE_POLICY_ALLOW) == AM_CACHE_POLICY_ALLOW) act = AM_TRUE;
                if ((a->type & AM_CACHE_POLICY_DENY) == AM_CACHE_POLICY_DENY) act = AM_FALSE;
                if (create_am_action_decision_node(act, a->method, a->ttl, &el) == 0) {
                    AM_LIST_INSERT(pol_curr->action_decisions, el);
                }
            }
            if ((a->type & AM_CACHE_POLICY_ADVICE) == AM_CACHE_POLICY_ADVICE && a->size[0] > 0 && a->size[1] > 0) {
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node(a->value, a->size[0], a->value + a->size[0] + 1, a->size[1], &el) == 0) {
                    AM_LIST_INSERT(pol_curr->action_decisions->advices, el);
                }
            }
        }
    }

    if (session != NULL) *session = sesion_attrs;
    if (policy != NULL) *policy = pol_attrs;
    if (sesion_attrs != NULL || pol_attrs != NULL) {
        status = AM_SUCCESS;
    }

    am_shm_unlock(cache);
    return status;
}

/* 
 * Add session/policy response cache entry (key: session token).
 */
int am_add_session_policy_cache_entry(am_request_t *r, const char *key,
        struct am_policy_result *policy, struct am_namevalue *session) {
    static const char *thisfunc = "am_add_session_policy_cache_entry():";
    unsigned int key_hash;
    int entry_index = 0, max_caching, time_left;

    struct am_cache_entry *c;
    struct am_cache *cache_data = cache != NULL ? (struct am_cache *) cache->user : NULL;

    if (cache_data == NULL) return AM_ENOMEM;
    if (!ISVALID(key) || (policy == NULL && session == NULL)) return AM_EINVAL;
    if (strlen(key) >= AM_SHARED_CACHE_KEY_SIZE) return AM_E2BIG;

    key_hash = hash((void *) key);
    entry_index = index_for(AM_SHARED_CACHE_SIZE, key_hash);

    am_shm_lock(cache);

    c = get_cache_entry(key, NULL);
    if (c != NULL) {
        if (!delete_cache_entry(entry_index, c)) {
            am_shm_free(cache, c);
            cache_data->count--;
            c = NULL;
        } else {
            AM_LOG_ERROR(r->instance_id, "%s failed to remove cache entry (%s)",
                    thisfunc, key);
            am_shm_unlock(cache);
            return AM_ERROR;
        }
    }

    c = am_shm_alloc(cache, sizeof (struct am_cache_entry));
    if (c == NULL) {
        AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                thisfunc, sizeof (struct am_cache_entry));
        am_shm_unlock(cache);
        return AM_ENOMEM;
    }

    /* maxcaching value is in minutes, timeleft in seconds */
    max_caching = get_ttl_value(session, "maxcaching", r->conf->token_cache_valid, AM_TRUE);
    time_left = get_ttl_value(session, "timeleft", r->conf->token_cache_valid, AM_FALSE);

    c->ts = time(NULL);
    c->valid = r->conf->token_cache_valid <= max_caching ?
            r->conf->token_cache_valid : (max_caching < time_left ? max_caching : time_left);
    c->instance_id = r->instance_id;
    strncpy(c->key, key, sizeof (c->key) - 1);

    c->data.next = c->data.prev = 0;
    c->lh.next = c->lh.prev = 0;

    AM_OFFSET_LIST_INSERT(cache->pool, c, &(cache_data->table[entry_index]), struct am_cache_entry);
    cache_data->count += 1;

    if (session != NULL) {
        struct am_namevalue *e, *t;

        AM_LIST_FOR_EACH(session, e, t) {
            struct am_cache_entry_data *x = am_shm_alloc(cache, sizeof (struct am_cache_entry_data) +e->ns + e->vs + 2);
            if (x == NULL) {
                AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                        thisfunc, sizeof (struct am_cache_entry_data));
                am_shm_unlock(cache);
                return AM_ENOMEM;
            }
            x->type = AM_CACHE_SESSION;
            x->method = AM_REQUEST_UNKNOWN;
            x->scope = x->index = -1; /*not used in this context*/
            x->ttl = c->ts + c->valid;
            x->size[0] = e->ns;
            x->size[1] = e->vs;
            x->size[2] = 0;
            memcpy(x->value, e->n, x->size[0]);
            x->value[x->size[0]] = 0;
            memcpy(x->value + x->size[0] + 1, e->v, x->size[1]);
            x->value[x->size[0] + x->size[1] + 1] = 0;
            x->lh.next = x->lh.prev = 0;

            AM_OFFSET_LIST_INSERT(cache->pool, x, &(c->data), struct am_cache_entry_data);
        }
    }

    if (policy != NULL) {
        struct am_policy_result *e, *t;

        struct am_namevalue *rae, *rat;
        struct am_namevalue *rde, *rdt;
        struct am_action_decision *ae, *at;
        struct am_namevalue *aee, *att;

        AM_LIST_FOR_EACH(policy, e, t) {

            {
                /*add policy entry (per resource)*/
                size_t rs = strlen(e->resource);
                struct am_cache_entry_data *x = am_shm_alloc(cache, sizeof (struct am_cache_entry_data) +rs + 1);
                if (x == NULL) {
                    AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                            thisfunc, sizeof (struct am_cache_entry_data));
                    am_shm_unlock(cache);
                    return AM_ENOMEM;
                }

                x->type = AM_CACHE_POLICY;
                x->method = AM_REQUEST_UNKNOWN;
                x->scope = e->scope;
                x->index = e->index;
                x->ttl = c->ts + c->valid;
                x->size[0] = rs;
                x->size[1] = 0;
                x->size[2] = 0;
                memcpy(x->value, e->resource, x->size[0]);
                x->value[x->size[0]] = 0;
                x->lh.next = x->lh.prev = 0;

                AM_OFFSET_LIST_INSERT(cache->pool, x, &(c->data), struct am_cache_entry_data);
            }

            AM_LIST_FOR_EACH(e->response_attributes, rae, rat) {
                /*add response attributes*/
                struct am_cache_entry_data *x = am_shm_alloc(cache, sizeof (struct am_cache_entry_data) +rae->ns + rae->vs + 2);
                if (x == NULL) {
                    AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                            thisfunc, sizeof (struct am_cache_entry_data));
                    am_shm_unlock(cache);
                    return AM_ENOMEM;
                }

                x->type = AM_CACHE_POLICY | AM_CACHE_POLICY_RESPONSE_A;
                x->method = AM_REQUEST_UNKNOWN;
                x->scope = -1;
                x->index = e->index;
                x->ttl = c->ts + c->valid;
                x->size[0] = rae->ns;
                x->size[1] = rae->vs;
                x->size[2] = 0;
                memcpy(x->value, rae->n, x->size[0]);
                x->value[x->size[0]] = 0;
                memcpy(x->value + x->size[0] + 1, rae->v, x->size[1]);
                x->value[x->size[0] + x->size[1] + 1] = 0;
                x->lh.next = x->lh.prev = 0;

                AM_OFFSET_LIST_INSERT(cache->pool, x, &(c->data), struct am_cache_entry_data);
            }

            AM_LIST_FOR_EACH(e->action_decisions, ae, at) {

                {
                    /*add action decision*/
                    struct am_cache_entry_data *x = am_shm_alloc(cache, sizeof (struct am_cache_entry_data));
                    if (x == NULL) {
                        AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                                thisfunc, sizeof (struct am_cache_entry_data));
                        am_shm_unlock(cache);
                        return AM_ENOMEM;
                    }

                    x->type = AM_CACHE_POLICY | AM_CACHE_POLICY_ACTION;
                    if (ae->action) x->type |= AM_CACHE_POLICY_ALLOW;
                    else x->type |= AM_CACHE_POLICY_DENY;
                    x->method = ae->method;
                    x->ttl = ae->ttl;
                    x->scope = -1;
                    x->index = e->index;
                    x->size[0] = 0;
                    x->size[1] = 0;
                    x->size[2] = 0;
                    x->lh.next = x->lh.prev = 0;

                    AM_OFFSET_LIST_INSERT(cache->pool, x, &(c->data), struct am_cache_entry_data);
                }

                AM_LIST_FOR_EACH(ae->advices, aee, att) {
                    /*add advices*/
                    struct am_cache_entry_data *x = am_shm_alloc(cache, sizeof (struct am_cache_entry_data) +aee->ns + aee->vs + 2);
                    if (x == NULL) {
                        AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                                thisfunc, sizeof (struct am_cache_entry_data));
                        am_shm_unlock(cache);
                        return AM_ENOMEM;
                    }

                    x->type = AM_CACHE_POLICY | AM_CACHE_POLICY_ADVICE;
                    x->method = AM_REQUEST_UNKNOWN;
                    x->scope = -1;
                    x->index = e->index;
                    x->ttl = c->ts + c->valid;
                    x->size[0] = aee->ns;
                    x->size[1] = aee->vs;
                    x->size[2] = 0;
                    memcpy(x->value, aee->n, x->size[0]);
                    x->value[x->size[0]] = 0;
                    memcpy(x->value + x->size[0] + 1, aee->v, x->size[1]);
                    x->value[x->size[0] + x->size[1] + 1] = 0;
                    x->lh.next = x->lh.prev = 0;

                    AM_OFFSET_LIST_INSERT(cache->pool, x, &(c->data), struct am_cache_entry_data);
                }
            }

            AM_LIST_FOR_EACH(e->response_decisions, rde, rdt) {
                /*add response decisions (profile attributes)*/
                struct am_cache_entry_data *x = am_shm_alloc(cache, sizeof (struct am_cache_entry_data) +rde->ns + rde->vs + 2);
                if (x == NULL) {
                    AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                            thisfunc, sizeof (struct am_cache_entry_data));
                    am_shm_unlock(cache);
                    return AM_ENOMEM;
                }

                x->type = AM_CACHE_POLICY | AM_CACHE_POLICY_RESPONSE_D;
                x->method = AM_REQUEST_UNKNOWN;
                x->scope = -1;
                x->index = e->index;
                x->ttl = c->ts + c->valid;
                x->size[0] = rde->ns;
                x->size[1] = rde->vs;
                x->size[2] = 0;
                memcpy(x->value, rde->n, x->size[0]);
                x->value[x->size[0]] = 0;
                memcpy(x->value + x->size[0] + 1, rde->v, x->size[1]);
                x->value[x->size[0] + x->size[1] + 1] = 0;
                x->lh.next = x->lh.prev = 0;

                AM_OFFSET_LIST_INSERT(cache->pool, x, &(c->data), struct am_cache_entry_data);
            }
        }
    }

    am_shm_unlock(cache);
    return AM_SUCCESS;
}

int am_get_policy_cache_entry(am_request_t *r, const char *key) {
    static const char *thisfunc = "am_get_policy_cache_entry():";
    int entry_index = 0;
    struct am_cache_entry *c;
    struct am_cache *cache_data = cache != NULL ? (struct am_cache *) cache->user : NULL;

    if (cache_data == NULL) return AM_ENOMEM;
    if (!ISVALID(key)) return AM_EINVAL;
    am_shm_lock(cache);

    c = get_cache_entry(key, &entry_index);
    if (c == NULL) {
        AM_LOG_WARNING(r->instance_id, "%s failed to locate data for a key (%s)",
                thisfunc, key);
        am_shm_unlock(cache);
        return AM_NOT_FOUND;
    }

    if (c->valid > 0) {
        time_t ts = c->ts;
        ts += c->valid;
        if (difftime(time(NULL), ts) >= 0) {
            char tsc[32], tsu[32];
            struct tm created, until;
            localtime_r(&c->ts, &created);
            localtime_r(&ts, &until);
            strftime(tsc, sizeof (tsc), AM_CACHE_TIMEFORMAT, &created);
            strftime(tsu, sizeof (tsu), AM_CACHE_TIMEFORMAT, &until);
            AM_LOG_WARNING(r->instance_id, "%s data for a key (%s) is obsolete (created: %s, valid until: %s)",
                    thisfunc, key, tsc, tsu);
            if (!delete_cache_entry(entry_index, c)) {
                am_shm_free(cache, c);
                cache_data->count--;
            }
            am_shm_unlock(cache);
            return AM_ETIMEDOUT;
        }
    }

    am_shm_unlock(cache);
    return AM_SUCCESS;
}

/* 
 * Add policy cache entry (key: ResourceResult name).
 */
int am_add_policy_cache_entry(am_request_t *r, const char *key, int valid) {
    static const char *thisfunc = "am_add_policy_cache_entry():";
    unsigned int key_hash;
    int entry_index = 0;
    struct am_cache_entry *c;
    struct am_cache *cache_data = cache != NULL ? (struct am_cache *) cache->user : NULL;

    if (cache_data == NULL) return AM_ENOMEM;
    if (!ISVALID(key)) return AM_EINVAL;
    if (strlen(key) >= AM_SHARED_CACHE_KEY_SIZE) return AM_E2BIG;

    key_hash = hash((void *) key);
    entry_index = index_for(AM_SHARED_CACHE_SIZE, key_hash);

    am_shm_lock(cache);

    c = get_cache_entry(key, NULL);
    if (c != NULL) {
        if (!delete_cache_entry(entry_index, c)) {
            am_shm_free(cache, c);
            cache_data->count--;
            c = NULL;
        } else {
            AM_LOG_ERROR(r->instance_id, "%s failed to remove cache entry (%s)",
                    thisfunc, key);
            am_shm_unlock(cache);
            return 1;
        }
    }

    c = am_shm_alloc(cache, sizeof (struct am_cache_entry));
    if (c == NULL) {
        AM_LOG_ERROR(r->instance_id, "%s failed to allocate %ld bytes",
                thisfunc, sizeof (struct am_cache_entry));
        am_shm_unlock(cache);
        return AM_ENOMEM;
    }

    c->ts = time(NULL);
    c->valid = r->conf->policy_cache_valid > 0 &&
            r->conf->policy_cache_valid < valid ? r->conf->policy_cache_valid : valid;
    c->instance_id = r->instance_id;
    strncpy(c->key, key, sizeof (c->key) - 1);

    c->data.next = c->data.prev = 0;
    c->lh.next = c->lh.prev = 0;

    AM_OFFSET_LIST_INSERT(cache->pool, c, &(cache_data->table[entry_index]), struct am_cache_entry);
    cache_data->count += 1;

    am_shm_unlock(cache);
    return AM_SUCCESS;
}
