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

#ifndef LIST_H
#define LIST_H

struct offset_list {
    unsigned int prev, next;
};

#ifdef _WIN32

#define am_list_insert(head,el) \
    do { \
        if ((head) != NULL) { \
            (el)->next = (head); \
            while ((el)->next->next) { (el)->next = (el)->next->next; } \
            (el)->next->next = (el); \
        } else (head) = (el); \
        (el)->next = NULL; \
    } while(0)

#else

#define am_list_insert(head,el) \
    do { \
        __typeof__(head) t; \
        (el)->next = NULL; \
        if ((head) != NULL) { \
            t = (head); \
            while (t->next) { t = t->next; } \
            t->next = (el); \
        } else (head) = (el); \
    } while(0)

#endif

#define am_list_for_each(head,el,tmp) \
     for ((el) = (head); (el) && (tmp = (el)->next, 1); (el) = tmp)

#define am_get_offset(base, ptr) \
    ((unsigned int) ((char *) ptr - (char *) base))

#define am_get_pointer(base, off) \
    ((void *) ((char *) (base) + off))

#define am_is_valid_pointer(base, ptr) \
    ((char *) (base) < (char *) (ptr))

#define am_offset_list_insert(base,el,head,typ) \
    do {\
        struct offset_list *dl = (head);\
        unsigned int eo = am_get_offset(base, (el));\
        if (dl->next == 0 && dl->next == dl->prev) {\
            dl->next = dl->prev = eo;\
        } else {\
            ((typ *) am_get_pointer(base, dl->next))->lh.next = eo;\
            (el)->lh.prev = dl->next;\
            dl->next = eo;\
        }\
    } while(0)

#define am_offset_list_for_each(base,head,el,tmp,typ) \
     for ((el) = (head); am_is_valid_pointer(base, el) && \
        (tmp = (typ *) am_get_pointer(base, el->lh.next), 1);(el) = tmp)

struct am_namevalue {
    char *n;
    size_t ns;
    char *v;
    size_t vs;
    struct am_namevalue *next;
};

void delete_am_namevalue_list(struct am_namevalue **list);

#endif
