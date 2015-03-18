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

#if defined(__sun)
#if defined(__sparc) || defined(__sparcv9)
#define BYTEORDER 4321
#else
#define BYTEORDER 1234
#endif
#define HAVE_BCOPY 1
#define HAVE_DLFCN_H 1
#define HAVE_FCNTL_H 1
#define HAVE_GETPAGESIZE 1
#define HAVE_INTTYPES_H 1
#define HAVE_MEMMOVE 1
#define HAVE_MEMORY_H 1
#define HAVE_MMAP 1
#define HAVE_STDINT_H 1
#define HAVE_STDLIB_H 1
#define HAVE_STRINGS_H 1
#define HAVE_STRING_H 1
#define HAVE_SYS_PARAM_H 1
#define HAVE_SYS_STAT_H 1
#define HAVE_SYS_TYPES_H 1
#define HAVE_UNISTD_H 1
#define STDC_HEADERS 1
#define XML_CONTEXT_BYTES 1024
#define XML_DTD 1
#define XML_NS 1
#elif defined(__linux__) || defined(__MACH__)
#define BYTEORDER 1234
#define HAVE_BCOPY 1
#define HAVE_DLFCN_H 1
#define HAVE_FCNTL_H 1
#define HAVE_GETPAGESIZE 1
#define HAVE_INTTYPES_H 1
#define HAVE_MEMMOVE 1
#define HAVE_MEMORY_H 1
#define HAVE_MMAP 1
#define HAVE_STDINT_H 1
#define HAVE_STDLIB_H 1
#define HAVE_STRINGS_H 1
#define HAVE_STRING_H 1
#define HAVE_SYS_PARAM_H 1
#define HAVE_SYS_STAT_H 1
#define HAVE_SYS_TYPES_H 1
#define HAVE_UNISTD_H 1
#define STDC_HEADERS 1
#define XML_CONTEXT_BYTES 1024
#define XML_DTD 1
#define XML_NS 1
#elif defined(_AIX)
#define BYTEORDER 4321
#define HAVE_BCOPY 1
#define HAVE_DLFCN_H 1
#define HAVE_FCNTL_H 1
#define HAVE_GETPAGESIZE 1
#define HAVE_INTTYPES_H 1
#define HAVE_MEMMOVE 1
#define HAVE_MEMORY_H 1
#define HAVE_MMAP 1
#define HAVE_STDINT_H 1
#define HAVE_STDLIB_H 1
#define HAVE_STRINGS_H 1
#define HAVE_STRING_H 1
#define HAVE_SYS_PARAM_H 1
#define HAVE_SYS_STAT_H 1
#define HAVE_SYS_TYPES_H 1
#define HAVE_UNISTD_H 1
#define STDC_HEADERS 1
#define XML_CONTEXT_BYTES 1024
#define XML_DTD 1
#define XML_NS 1
#elif defined(_WIN32)
#include "winconfig.h"
#endif
