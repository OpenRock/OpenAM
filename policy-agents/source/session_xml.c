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
#include "list.h"

/*
 * XML parser for 'SessionResponse', 'SessionNotification', 'PolicyNotification' and
 * 'AgentConfigChangeNotification' elements
 */

void delete_am_namevalue_list(struct am_namevalue **list) {
    struct am_namevalue *t = *list;
    if (t != NULL) {
        delete_am_namevalue_list(&t->next);
        if (t->n != NULL) free(t->n);
        if (t->v != NULL) free(t->v);
        free(t);
        t = NULL;
    }
}

typedef struct {
    int depth;
    unsigned long instance_id;
    struct am_namevalue *list;
    void *parser;
} am_xml_parser_ctx_t;

static void start_element(void *userData, const char *name, const char **atts) {
    int i;
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;

    if (strcmp(name, "Session") == 0) {
        for (i = 0; atts[i]; i += 2) {
            if (strcmp(atts[i], "sid") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("sid", 3, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "cid") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("cid", 3, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "cdomain") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("cdomain", 7, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "maxtime") == 0) { /* in sec, just an interval ? */
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("maxtime", 7, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "maxidle") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("maxidle", 7, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "maxcaching") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("maxcaching", 10, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "timeidle") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("timeidle", 8, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "timeleft") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("timeleft", 8, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            } else if (strcmp(atts[i], "state") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("state", 5, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            }
        }
    } else if (strcmp(name, "Property") == 0) {
        const char **p = atts;

        /*count the number of attributes*/
        while (*p) {
            ++p;
        }

        if (((p - atts) >> 1) == 2) {
            for (i = 0; atts[i]; i += 4) {
                if (strcmp(atts[i + 1], "HostName") == 0) {
                    size_t nl = strlen(atts[i + 3]);
                    struct am_namevalue *el = NULL;
                    if (create_am_namevalue_node("HostName", 8, atts[i + 3], nl, &el) == 0) {
                        am_list_insert(ctx->list, el);
                    }
                } else if (strcmp(atts[i + 1], "Host") == 0) {
                    size_t nl = strlen(atts[i + 3]);
                    struct am_namevalue *el = NULL;
                    if (create_am_namevalue_node("Host", 4, atts[i + 3], nl, &el) == 0) {
                        am_list_insert(ctx->list, el);
                    }
                } else if (strcmp(atts[i + 1], "UserToken") == 0) {
                    size_t nl = strlen(atts[i + 3]);
                    struct am_namevalue *el = NULL;
                    if (create_am_namevalue_node("UserToken", 9, atts[i + 3], nl, &el) == 0) {
                        am_list_insert(ctx->list, el);
                    }
                } else if (strcmp(atts[i + 1], "AuthLevel") == 0) {
                    size_t nl = strlen(atts[i + 3]);
                    struct am_namevalue *el = NULL;
                    if (create_am_namevalue_node("AuthLevel", 9, atts[i + 3], nl, &el) == 0) {
                        am_list_insert(ctx->list, el);
                    }
                } else if (strcmp(atts[i + 1], "AuthType") == 0) {
                    size_t nl = strlen(atts[i + 3]);
                    struct am_namevalue *el = NULL;
                    if (create_am_namevalue_node("AuthType", 8, atts[i + 3], nl, &el) == 0) {
                        am_list_insert(ctx->list, el);
                    }
                } else if (strcmp(atts[i + 1], "Service") == 0) {
                    size_t nl = strlen(atts[i + 3]);
                    struct am_namevalue *el = NULL;
                    if (create_am_namevalue_node("Service", 7, atts[i + 3], nl, &el) == 0) {
                        am_list_insert(ctx->list, el);
                    }
                } else if (strcmp(atts[i + 1], "sunIdentityUserPassword") == 0) {
                    size_t nl = strlen(atts[i + 3]);
                    struct am_namevalue *el = NULL;
                    if (create_am_namevalue_node("sunIdentityUserPassword", 23, atts[i + 3], nl, &el) == 0) {
                        am_list_insert(ctx->list, el);
                    }
                }
            }
        }
    } else if (strcmp(name, "AgentConfigChangeNotification") == 0) {
        for (i = 0; atts[i]; i += 2) {
            if (strcmp(atts[i], "agentName") == 0) {
                size_t nl = strlen(atts[i + 1]);
                struct am_namevalue *el = NULL;
                if (create_am_namevalue_node("agentName", 9, atts[i + 1], nl, &el) == 0) {
                    am_list_insert(ctx->list, el);
                }
            }
        }
    } else if (strcmp(name, "PolicyChangeNotification") == 0) {
        //TODO: implementme
    }
}

static void end_element(void * userData, const char * name) {
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    ctx->depth--;
}

static void character_data(void *userData, const char *val, int len) {
}

static void entity_declaration(void *userData, const XML_Char *entityName,
        int is_parameter_entity, const XML_Char *value, int value_length, const XML_Char *base,
        const XML_Char *systemId, const XML_Char *publicId, const XML_Char *notationName) {
    am_xml_parser_ctx_t *ctx = (am_xml_parser_ctx_t *) userData;
    XML_StopParser(ctx->parser, XML_FALSE);
}

void *am_parse_session_xml(unsigned long instance_id, const char *xml, size_t xml_sz) {
    const char *thisfunc = "am_parse_session_xml():";
    char *begin, *stream = NULL;
    size_t data_sz;
    struct am_namevalue *r = NULL;

    am_xml_parser_ctx_t xctx = {.depth = 0, .instance_id = instance_id,
        .list = NULL, .parser = NULL};

    if (xml == NULL || xml_sz == 0) {
        am_log_error(instance_id, "%s memory allocation error", thisfunc);
        return NULL;
    }

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

            delete_am_namevalue_list(&xctx.list);
        } else {
            r = xctx.list;
        }
        XML_ParserFree(parser);
    }

    return (void *) r;
}
