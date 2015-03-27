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

#ifndef PLATFORM_H
#define PLATFORM_H

#ifdef _WIN32

#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <ws2tcpip.h>
#include <ws2ipdef.h>
#include <wincrypt.h>
#include <process.h>
#include <io.h>
#include <direct.h>
#include <fcntl.h>
#include <math.h>
#include <malloc.h>
#include <time.h>
#include <shlwapi.h>
#include <shellapi.h>
#if !defined(snprintf)
#define snprintf            sprintf_s
#endif
#define mkdir(a,b)          _mkdir(a)
#define getpid              _getpid
#define strcasecmp          _stricmp 
#define strncasecmp         _strnicmp
#define unlink              _unlink 
#define sleep(x)            SleepEx(x * 1000, FALSE)
#define localtime_r(a,b)    localtime_s(b,a)
#define strtok_r            strtok_s
#define sockpoll            WSAPoll
#define SOCKLEN_T           int
#define pid_t               int
typedef SSIZE_T             ssize_t;
#if (_MSC_VER < 1800)
#define va_copy(dst, src)   ((void)((dst) = (src)))
#endif
#ifndef S_ISDIR
#define S_ISDIR(mode)       (((mode) & S_IFMT) == S_IFDIR)
#endif
#ifndef S_ISREG
#define S_ISREG(mode)       (((mode) & S_IFMT) == S_IFREG)
#endif
#define FILE_PATH_SEP       "\\"
#define AM_GLOBAL_PREFIX    "Global\\"

#else

#include <pthread.h>
#include <unistd.h>
#if defined(__sun) && !defined(_POSIX_C_SOURCE)
#define _POSIX_C_SOURCE 200112L 
#include <sys/mman.h> 
#undef _POSIX_C_SOURCE 
#else
#include <sys/mman.h> 
#endif
#include <sys/time.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/signal.h>
#include <signal.h>
#include <netdb.h>
#include <fcntl.h>
#include <semaphore.h>
#include <netinet/tcp.h>
#include <sys/ioctl.h>
#include <sys/poll.h>
#include <sys/socket.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <ftw.h>
#include <dirent.h>
#include <dlfcn.h>
#ifdef __APPLE__
#include <mach-o/dyld.h>
#include <mach/clock.h>
#include <mach/mach.h>
#include <mach/semaphore.h>
#include <mach/task.h>
#include <sys/uio.h>
#include <copyfile.h>
#else
#include <sys/sendfile.h>
#endif
#define sockpoll            poll
#define SOCKLEN_T           socklen_t
#define FILE_PATH_SEP       "/"
#define AM_GLOBAL_PREFIX    ""

#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <stdarg.h>
#include <ctype.h>
#include <stdint.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <stddef.h>

#endif
