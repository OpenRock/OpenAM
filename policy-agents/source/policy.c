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

static const char *policy_fetch_scope_str[] = {
    "self",
    "subtree",
    "response-attributes-only"
};

const char *am_policy_strerror(char status) {
    switch (status) {
        case AM_EXACT_MATCH: return "exact match";
        case AM_EXACT_PATTERN_MATCH: return "exact pattern match";
        default:
            return "no match";
    }
}

static char compare_resource(am_request_t *r, const char *pattern, const char *resource) {
    char status = AM_FALSE;
    char case_sensitive = (r != NULL && r->conf != NULL) ?
            (r->conf->url_eval_case_ignore == AM_TRUE ? AM_FALSE : AM_TRUE) : AM_FALSE;
    if (case_sensitive == AM_FALSE) {
        if (strcasecmp(pattern, resource) == 0)
            status = AM_TRUE;
    } else {
        if (strcmp(pattern, resource) == 0)
            status = AM_TRUE;
    }
    return status;
}

static char compare_pattern_resource(am_request_t *r, const char *ptn, const char *rsc) {
    const char *thisfunc = "compare_pattern_resource():";
    char t, w, status = AM_TRUE, in_one_level = AM_FALSE;
    char *resource, *p_resource, *pattern, *p_pattern,
            *after_last_wild = NULL, *after_last_resource = NULL;
    unsigned int one_level_sep_count = 0;
    char case_sensitive = (r != NULL && r->conf != NULL) ?
            (r->conf->url_eval_case_ignore == AM_TRUE ? AM_FALSE : AM_TRUE) : AM_FALSE;
    unsigned long instance_id = r != NULL ? r->instance_id : 0;

    p_resource = rsc == NULL ? NULL : strdup(rsc);
    p_pattern = ptn == NULL ? NULL : strdup(ptn);
    if (p_resource == NULL && p_pattern == NULL) return AM_FALSE;
    resource = p_resource;
    pattern = p_pattern;

    /* walk the resource and the pattern strings one character at a time */
    while (1) {
        t = *resource;
        w = *pattern;

        if (one_level_sep_count > 1) {
            am_log_debug(instance_id, "%s '%s' and '%s' did not match (one level wildcard match failure)",
                    thisfunc, rsc, ptn);
            status = AM_FALSE;
            break;
        }

        /* is the resource string still valid? */
        if (!t || t == '\0') {
            /* if the pattern string has additional characters by the time 
             * processing reaches the end of the resource string, and if at least 
             * one of those additional characters in the pattern string is not 
             * a multi/single-level wildcard 'items', then the strings don't match
             **/
            if (!w || w == '\0') {
                break; /* x matches x */
            } else if (w == '-' && *(pattern + 1) == '*' && *(pattern + 2) == '-') {
                in_one_level = AM_TRUE;
                one_level_sep_count = 0;
                pattern += 3;
                continue;
            } else if (w == '*') {
                in_one_level = AM_FALSE;
                one_level_sep_count = 0;
                pattern++;
                continue; /* x* matches x or xy */
            } else if (after_last_resource) {
                /* look through the remaining resource string, from which we 
                 * started after last wildcard
                 */
                if (!(*after_last_resource) || *after_last_resource == '\0') {
                    status = AM_FALSE;
                    break;
                }
                resource = after_last_resource++;
                pattern = after_last_wild;
                if (in_one_level && *resource == '/') one_level_sep_count++;
                continue;
            }
            status = AM_FALSE;
            break; /* x doesn't match xy */

        } else {

            /* algorithm entry */

            if (!case_sensitive) {
                /* lowercase the characters to be compared */
                if (t >= 'A' && t <= 'Z') {
                    t += ('a' - 'A');
                }
                if (w >= 'A' && w <= 'Z') {
                    w += ('a' - 'A');
                }
            }

            if (t != w) {
                /* characters do not match */
                if (w == '-' && *(pattern + 1) == '*' && *(pattern + 2) == '-') {
                    /* one-level wildcard. save the pointers to the next 
                     * character after the wildcard (both in the resource and in 
                     * the pattern strings)
                     */
                    in_one_level = AM_TRUE;
                    pattern += 3;
                    one_level_sep_count = 0;
                    after_last_wild = pattern;
                    after_last_resource = resource;
                    w = *pattern;
                    if (!w || w == '\0') {
                        int lc = 0, sc = char_count(after_last_resource, '/', &lc);
                        if (!(sc == 0 || (sc == 1 && lc == '/'))) {
                            status = AM_FALSE;
                            /* special case where one-level wildcard is the last
                             * item in a pattern:
                             *  /a/b-*- should match /a/b, /a/ba or /a/bc/
                             *   and should not match /a/b/c or /a/c
                             *  /a/-*- will match /a/
                             */
                            am_log_debug(instance_id, "%s '%s' and '%s' did not match (one level wildcard match failure)",
                                    thisfunc, rsc, ptn);
                            break;
                        }
                        break; /* * matches x */
                    }
                    continue; /* *y matches xy */
                } else if (w == '*') {
                    /* multi-level wildcard. the same rule as above. */
                    in_one_level = AM_FALSE;
                    one_level_sep_count = 0;
                    after_last_wild = ++pattern;
                    after_last_resource = resource;
                    w = *pattern;
                    if (!w || w == '\0') {
                        break; /* * matches x */
                    }
                    continue; /* *y matches xy */
                } else if (after_last_wild) {
                    /* found a valid (earlier) saved pointer - we've encountered 
                     * a wildcard character already. */
                    if (after_last_wild != pattern) {
                        pattern = after_last_wild;
                        w = *pattern;
                        if (!case_sensitive && w >= 'A' && w <= 'Z') {
                            w += ('a' - 'A');
                        }
                        if (t == w) {
                            /* the current char in the resource and the pattern strings match.
                             * move to the next pattern (and the resource) character
                             **/
                            pattern++;
                        } /* else: they don't match - move only to the next char 
                           * in the resource string. restart the loop from the beginning
                           **/
                    }
                    resource++;
                    /* keep track of the one-level-wildcard separator count, 
                     *  when in one-level wildcard processing state. 
                     * if there are more than one '/' - break out of the loop 
                     * with no-match
                     */
                    if (in_one_level && *resource == '/') one_level_sep_count++;
                    continue; /* *ue* matches queue */
                } else {
                    status = AM_FALSE;
                    break; /* x doesn't match y */
                }
            }
        }
        /* advance to the next character */
        resource++;
        pattern++;
    }

    free(p_resource);
    free(p_pattern);
    return status;
}

static void policy_get_proto_host_port(const char *string, int *c) {
    int j;
    for (j = 0; string[j]; j++) {
        if (string[j] == ':') {
            if (c[0] == 0) c[0] = j;
            else c[1] = j;
        } else if (string[j] == '/') {
            c[2] += 1;
        }
        if (c[2] == 3) {
            c[2] = j;
            break;
        }
    }
}

char policy_compare_url(am_request_t *r, const char *pattern, const char *resource) {
    const char *thisfunc = "policy_compare_url():";
    char has_wildcard = AM_FALSE;
    unsigned long instance_id = r != NULL ? r->instance_id : 0;

    if (pattern == NULL || resource == NULL)
        return AM_NO_MATCH;

    /* validate pattern */
    if (strchr(pattern, '*') != NULL) {
        if (strlen(pattern) == 1 || strstr(pattern, " *") != NULL || strstr(pattern, "* ") != NULL) {
            /*
             * pattern matching algorithm forbids:
             * - wildcard only (i.e. "all allowed")
             * - white-space before/after a wildcard, though this is unlikely to be matched, because url list 
             *   is passed down to an agent as a space separated value object
             */
            am_log_warning(instance_id, "%s invalid pattern '%s'",
                    thisfunc, pattern);
            return AM_NO_MATCH;
        }
        has_wildcard = AM_TRUE;
    }

    /* validate resource */
    if (strchr(resource, '*') != NULL) {
        /*
         * pattern matching algorithm forbids:
         * - wildcard in a resource
         */
        am_log_warning(instance_id, "%s invalid resource '%s'",
                thisfunc, resource);
        return AM_NO_MATCH;
    }

    if (has_wildcard == AM_TRUE) {
        int pi[3] = {0, 0, 0};
        int ri[3] = {0, 0, 0};
        char *a, *b, match;

        /*wildcard in a proto/host/port is not the same as in uri match*/
        policy_get_proto_host_port(pattern, pi);
        policy_get_proto_host_port(resource, ri);

        /*quick sanity check for field indicators*/
        if (pi[0] == 0 || pi[2] < 3 || pi[1] >= pi[2] ||
                ri[0] == 0 || ri[2] < 3 || ri[1] >= pi[2]) return AM_NO_MATCH;

        a = strndup(pattern, pi[0]);
        b = strndup(resource, ri[0]);
        if (a == NULL || b == NULL) return AM_NO_MATCH;
        /*compare protocol*/
        match = compare_pattern_resource(r, a, b);
        free(a);
        free(b);
        if (match == AM_FALSE) return AM_NO_MATCH;
        if (pi[1] == 0 && ri[1] == 0) {
            /*port is not set, compare host*/
            a = strndup(pattern + pi[0] + 3, pi[2] - pi[0] - 3);
            b = strndup(resource + ri[0] + 3, ri[2] - ri[0] - 3);
            if (a == NULL || b == NULL) return AM_NO_MATCH;
            match = compare_pattern_resource(r, a, b);
            free(a);
            free(b);
            if (match == AM_FALSE) return AM_NO_MATCH;
        } else if (pi[1] > 0 && ri[1] > 0) {
            /*port is set, compare host first*/
            a = strndup(pattern + pi[0] + 3, pi[1] - pi[0] - 3);
            b = strndup(resource + ri[0] + 3, ri[1] - ri[0] - 3);
            if (a == NULL || b == NULL) return AM_NO_MATCH;
            match = compare_pattern_resource(r, a, b);
            free(a);
            free(b);
            if (match == AM_FALSE) return AM_NO_MATCH;
            /*compare port*/
            a = strndup(pattern + pi[1] + 1, pi[2] - pi[1] - 1);
            b = strndup(resource + ri[1] + 1, ri[2] - ri[1] - 1);
            if (a == NULL || b == NULL) return AM_NO_MATCH;
            match = compare_pattern_resource(r, a, b);
            free(a);
            free(b);
            if (match == AM_FALSE) return AM_NO_MATCH;
        }

        if (compare_pattern_resource(r, pattern + pi[2], resource + ri[2]) == AM_TRUE) {
            /*am_log_debug(instance_id, "%s '%s' and '%s' matched (%s)",
                    thisfunc, resource, pattern, am_policy_strerror(AM_EXACT_PATTERN_MATCH));*/
            return AM_EXACT_PATTERN_MATCH;
        }
    } else {
        /* no wildcard */
        if (compare_resource(r, pattern, resource) == AM_TRUE) {
            /*am_log_debug(instance_id, "%s '%s' and '%s' matched (%s)",
                    thisfunc, resource, pattern, am_policy_strerror(AM_EXACT_MATCH));*/
            return AM_EXACT_MATCH;
        }
    }
    /*am_log_debug(instance_id, "%s '%s' and '%s' did no match",
            thisfunc, resource, pattern);*/
    return AM_NO_MATCH;
}

int am_scope_to_num(const char *scope) {
    int i;
    for (i = 0; (scope != NULL), i < ARRAY_SIZE(policy_fetch_scope_str); i++) {
        if (strcasecmp(scope, policy_fetch_scope_str[0]) == 0) return i;
    }
    return 0;
}

const char *am_scope_to_str(int scope) {
    if (scope >= ARRAY_SIZE(policy_fetch_scope_str))
        return policy_fetch_scope_str[0];
    return policy_fetch_scope_str[scope];
}
