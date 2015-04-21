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

#ifndef LOG_H
#define LOG_H

#include <stdint.h>

typedef long am_usec_t;

#ifdef _WIN32
#define AM_LOG_ALWAYS(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tze[6];\
    int minutes;\
    TIME_ZONE_INFORMATION tz;\
    SYSTEMTIME st;\
    size_t fmt_sz = strlen(f);\
    GetLocalTime(&st);\
    GetTimeZoneInformation(&tz);\
    GetTimeFormatA(LOCALE_USER_DEFAULT, TIME_NOTIMEMARKER | TIME_FORCE24HOURFORMAT, &st,\
        "HH':'mm':'ss", time_string, sizeof (time_string));\
    minutes = -(tz.Bias);\
    sprintf_s(tze, sizeof (tze), "%03d%02d", minutes / 60, abs(minutes % 60));\
    if (*tze == '0') *tze = '+';\
    sprintf_s(header, sizeof (header), "%04d-%02d-%02d %s.%03d %s   INFO [%d:%d]  ",\
        st.wYear, st.wMonth, st.wDay, time_string, st.wMilliseconds, tze, \
        GetCurrentThreadId(), _getpid());\
    mod_fmt = (char *) malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
    strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
    am_log(i, (AM_LOG_LEVEL_INFO|AM_LOG_LEVEL_ALWAYS), mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#else
#define AM_LOG_ALWAYS(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tz[8];\
    struct tm now;\
    struct timeval tv;\
    size_t fmt_sz = strlen(f);\
    gettimeofday(&tv, NULL);\
    localtime_r(&tv.tv_sec, &now);\
    strftime(time_string, sizeof (time_string) - 1, "%Y-%m-%d %H:%M:%S", &now);\
    strftime(tz, sizeof (tz) - 1, "%z", &now);\
    snprintf(header, sizeof(header), "%s.%03ld %s   INFO [%p:%d]  ", \
        time_string, (am_usec_t)tv.tv_usec / 1000, tz, (void *)(uintptr_t)pthread_self(), \
        getpid());\
    mod_fmt = malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
        strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
        am_log(i, (AM_LOG_LEVEL_INFO|AM_LOG_LEVEL_ALWAYS), mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#endif

#ifdef _WIN32
#define AM_LOG_INFO(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tze[6];\
    int minutes;\
    TIME_ZONE_INFORMATION tz;\
    SYSTEMTIME st;\
    size_t fmt_sz = strlen(f);\
    GetLocalTime(&st);\
    GetTimeZoneInformation(&tz);\
    GetTimeFormatA(LOCALE_USER_DEFAULT, TIME_NOTIMEMARKER | TIME_FORCE24HOURFORMAT, &st,\
        "HH':'mm':'ss", time_string, sizeof (time_string));\
    minutes = -(tz.Bias);\
    sprintf_s(tze, sizeof (tze), "%03d%02d", minutes / 60, abs(minutes % 60));\
    if (*tze == '0') *tze = '+';\
    sprintf_s(header, sizeof (header), "%04d-%02d-%02d %s.%03d %s   INFO [%d:%d]  ",\
        st.wYear, st.wMonth, st.wDay, time_string, st.wMilliseconds, tze, \
        GetCurrentThreadId(), _getpid());\
    mod_fmt = (char *) malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
    strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
    am_log(i, AM_LOG_LEVEL_INFO, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#else
#define AM_LOG_INFO(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tz[8];\
    struct tm now;\
    struct timeval tv;\
    size_t fmt_sz = strlen(f);\
    gettimeofday(&tv, NULL);\
    localtime_r(&tv.tv_sec, &now);\
    strftime(time_string, sizeof (time_string) - 1, "%Y-%m-%d %H:%M:%S", &now);\
    strftime(tz, sizeof (tz) - 1, "%z", &now);\
    snprintf(header, sizeof(header), "%s.%03ld %s   INFO [%p:%d]  ", \
        time_string, (am_usec_t)tv.tv_usec / 1000, tz, (void *)(uintptr_t)pthread_self(), \
        getpid());\
    mod_fmt = malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
        strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
        am_log(i, AM_LOG_LEVEL_INFO, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#endif

#ifdef _WIN32
#define AM_LOG_WARNING(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tze[6];\
    int minutes;\
    TIME_ZONE_INFORMATION tz;\
    SYSTEMTIME st;\
    size_t fmt_sz = strlen(f);\
    GetLocalTime(&st);\
    GetTimeZoneInformation(&tz);\
    GetTimeFormatA(LOCALE_USER_DEFAULT, TIME_NOTIMEMARKER | TIME_FORCE24HOURFORMAT, &st,\
        "HH':'mm':'ss", time_string, sizeof (time_string));\
    minutes = -(tz.Bias);\
    sprintf_s(tze, sizeof (tze), "%03d%02d", minutes / 60, abs(minutes % 60));\
    if (*tze == '0') *tze = '+';\
    sprintf_s(header, sizeof (header), "%04d-%02d-%02d %s.%03d %s   WARNING [%d:%d]  ",\
        st.wYear, st.wMonth, st.wDay, time_string, st.wMilliseconds, tze, \
        GetCurrentThreadId(), _getpid());\
    mod_fmt = (char *) malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
    strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
    am_log(i, AM_LOG_LEVEL_WARNING, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#else
#define AM_LOG_WARNING(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tz[8];\
    struct tm now;\
    struct timeval tv;\
    size_t fmt_sz = strlen(f);\
    gettimeofday(&tv, NULL);\
    localtime_r(&tv.tv_sec, &now);\
    strftime(time_string, sizeof (time_string) - 1, "%Y-%m-%d %H:%M:%S", &now);\
    strftime(tz, sizeof (tz) - 1, "%z", &now);\
    snprintf(header, sizeof(header), "%s.%03ld %s   WARNING [%p:%d]  ", \
        time_string, (am_usec_t)tv.tv_usec / 1000, tz, (void *)(uintptr_t)pthread_self(), \
        getpid());\
    mod_fmt = malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
        strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
        am_log(i, AM_LOG_LEVEL_WARNING, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#endif

#ifdef _WIN32
#define AM_LOG_ERROR(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tze[6];\
    int minutes;\
    TIME_ZONE_INFORMATION tz;\
    SYSTEMTIME st;\
    size_t fmt_sz = strlen(f);\
    GetLocalTime(&st);\
    GetTimeZoneInformation(&tz);\
    GetTimeFormatA(LOCALE_USER_DEFAULT, TIME_NOTIMEMARKER | TIME_FORCE24HOURFORMAT, &st,\
        "HH':'mm':'ss", time_string, sizeof (time_string));\
    minutes = -(tz.Bias);\
    sprintf_s(tze, sizeof (tze), "%03d%02d", minutes / 60, abs(minutes % 60));\
    if (*tze == '0') *tze = '+';\
    sprintf_s(header, sizeof (header), "%04d-%02d-%02d %s.%03d %s   ERROR [%d:%d]  ",\
        st.wYear, st.wMonth, st.wDay, time_string, st.wMilliseconds, tze, \
        GetCurrentThreadId(), _getpid());\
    mod_fmt = (char *) malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
    strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
    am_log(i, AM_LOG_LEVEL_ERROR, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#else
#define AM_LOG_ERROR(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tz[8];\
    struct tm now;\
    struct timeval tv;\
    size_t fmt_sz = strlen(f);\
    gettimeofday(&tv, NULL);\
    localtime_r(&tv.tv_sec, &now);\
    strftime(time_string, sizeof (time_string) - 1, "%Y-%m-%d %H:%M:%S", &now);\
    strftime(tz, sizeof (tz) - 1, "%z", &now);\
    snprintf(header, sizeof(header), "%s.%03ld %s   ERROR [%p:%d]  ", \
        time_string, (am_usec_t)tv.tv_usec / 1000, tz, (void *)(uintptr_t)pthread_self(), \
        getpid());\
    mod_fmt = malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
        strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
        am_log(i, AM_LOG_LEVEL_ERROR, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#endif

#ifdef _WIN32
#define AM_LOG_DEBUG(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tze[6];\
    int minutes;\
    TIME_ZONE_INFORMATION tz;\
    SYSTEMTIME st;\
    size_t fmt_sz = strlen(f);\
    GetLocalTime(&st);\
    GetTimeZoneInformation(&tz);\
    GetTimeFormatA(LOCALE_USER_DEFAULT, TIME_NOTIMEMARKER | TIME_FORCE24HOURFORMAT, &st,\
        "HH':'mm':'ss", time_string, sizeof (time_string));\
    minutes = -(tz.Bias);\
    sprintf_s(tze, sizeof (tze), "%03d%02d", minutes / 60, abs(minutes % 60));\
    if (*tze == '0') *tze = '+';\
    sprintf_s(header, sizeof (header), "%04d-%02d-%02d %s.%03d %s   DEBUG [%d:%d][%s:%d]  ",\
        st.wYear, st.wMonth, st.wDay, time_string, st.wMilliseconds, tze, \
        GetCurrentThreadId(), _getpid(), __FILE__, __LINE__);\
    mod_fmt = (char *) malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
    strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
    am_log(i, AM_LOG_LEVEL_DEBUG, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#else
#define AM_LOG_DEBUG(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tz[8];\
    struct tm now;\
    struct timeval tv;\
    size_t fmt_sz = strlen(f);\
    gettimeofday(&tv, NULL);\
    localtime_r(&tv.tv_sec, &now);\
    strftime(time_string, sizeof (time_string) - 1, "%Y-%m-%d %H:%M:%S", &now);\
    strftime(tz, sizeof (tz) - 1, "%z", &now);\
    snprintf(header, sizeof(header), "%s.%03ld %s   DEBUG [%p:%d][%s:%d]  ", \
        time_string, (am_usec_t)tv.tv_usec / 1000, tz, (void *)(uintptr_t)pthread_self(), \
        getpid(), __FILE__, __LINE__);\
    mod_fmt = malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
        strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
        am_log(i, AM_LOG_LEVEL_DEBUG, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#endif

#ifdef _WIN32
#define AM_LOG_AUDIT(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tze[6];\
    int minutes;\
    TIME_ZONE_INFORMATION tz;\
    SYSTEMTIME st;\
    size_t fmt_sz = strlen(f);\
    GetLocalTime(&st);\
    GetTimeZoneInformation(&tz);\
    GetTimeFormatA(LOCALE_USER_DEFAULT, TIME_NOTIMEMARKER | TIME_FORCE24HOURFORMAT, &st,\
        "HH':'mm':'ss", time_string, sizeof (time_string));\
    minutes = -(tz.Bias);\
    sprintf_s(tze, sizeof (tze), "%03d%02d", minutes / 60, abs(minutes % 60));\
    if (*tze == '0') *tze = '+';\
    sprintf_s(header, sizeof (header), "%04d-%02d-%02d %s.%03d %s   AUDIT [%d:%d]  ",\
        st.wYear, st.wMonth, st.wDay, time_string, st.wMilliseconds, tze, \
        GetCurrentThreadId(), _getpid());\
    mod_fmt = (char *) malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
    strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
    am_log(i, AM_LOG_LEVEL_AUDIT, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#else
#define AM_LOG_AUDIT(i,f,...) do { if (i > 0 && f != NULL) {\
    char *mod_fmt, header[256];\
    char time_string[25];\
    char tz[8];\
    struct tm now;\
    struct timeval tv;\
    size_t fmt_sz = strlen(f);\
    gettimeofday(&tv, NULL);\
    localtime_r(&tv.tv_sec, &now);\
    strftime(time_string, sizeof (time_string) - 1, "%Y-%m-%d %H:%M:%S", &now);\
    strftime(tz, sizeof (tz) - 1, "%z", &now);\
    snprintf(header, sizeof(header), "%s.%03ld %s   AUDIT [%p:%d]  ", \
        time_string, (am_usec_t)tv.tv_usec / 1000, tz, (void *)(uintptr_t)pthread_self(), \
        getpid());\
    mod_fmt = malloc(fmt_sz + 3);\
    if (mod_fmt != NULL) {\
        strcpy(mod_fmt,"%s");strcat(mod_fmt,f);\
        am_log(i, AM_LOG_LEVEL_AUDIT, mod_fmt, header, ##__VA_ARGS__);\
    free(mod_fmt);}}}while (0)
#endif

#endif
