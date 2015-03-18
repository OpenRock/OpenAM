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
#include "error.h"
#include "pcre.h"
#include "zlib.h"  
#include "list.h" 
#include "thread.h"

#define AM_STRERROR_GEN(name, msg) case AM_ ## name: return msg;

const char *am_strerror(int err) {
    switch (err) {
            AM_ERRNO_MAP(AM_STRERROR_GEN)
        default:
            return "unknown system error";
    }
}
#undef AM_STRERROR_GEN

#define REQUEST_METHOD_GET                      "GET"
#define REQUEST_METHOD_POST                     "POST"
#define REQUEST_METHOD_HEAD                     "HEAD"
#define REQUEST_METHOD_PUT                      "PUT"
#define REQUEST_METHOD_DELETE                   "DELETE"
#define REQUEST_METHOD_TRACE                    "TRACE"
#define REQUEST_METHOD_OPTIONS                  "OPTIONS"
#define REQUEST_METHOD_CONNECT                  "CONNECT"
#define REQUEST_METHOD_COPY                     "COPY"
#define REQUEST_METHOD_INVALID                  "INVALID"
#define REQUEST_METHOD_LOCK                     "LOCK"
#define REQUEST_METHOD_UNLOCK                   "UNLOCK"
#define REQUEST_METHOD_MKCOL                    "MKCOL"
#define REQUEST_METHOD_MOVE                     "MOVE"
#define REQUEST_METHOD_PATCH                    "PATCH"
#define REQUEST_METHOD_PROPFIND                 "PROPFIND"
#define REQUEST_METHOD_PROPPATCH                "PROPPATCH"
#define REQUEST_METHOD_VERSION_CONTROL          "VERSION_CONTROL"
#define REQUEST_METHOD_CHECKOUT                 "CHECKOUT"
#define REQUEST_METHOD_UNCHECKOUT               "UNCHECKOUT"
#define REQUEST_METHOD_CHECKIN                  "CHECKIN"
#define REQUEST_METHOD_UPDATE                   "UPDATE"
#define REQUEST_METHOD_LABEL                    "LABEL"
#define REQUEST_METHOD_REPORT                   "REPORT"
#define REQUEST_METHOD_MKWORKSPACE              "MKWORKSPACE"
#define REQUEST_METHOD_MKACTIVITY               "MKACTIVITY"
#define REQUEST_METHOD_BASELINE_CONTROL         "BASELINE_CONTROL"
#define REQUEST_METHOD_MERGE                    "MERGE"
#define REQUEST_METHOD_CONFIG                   "CONFIG"
#define REQUEST_METHOD_ENABLE_APP               "ENABLE-APP"
#define REQUEST_METHOD_DISABLE_APP              "DISABLE-APP"
#define REQUEST_METHOD_STOP_APP                 "STOP-APP"
#define REQUEST_METHOD_STOP_APP_RSP             "STOP-APP-RSP"
#define REQUEST_METHOD_REMOVE_APP               "REMOVE-APP"
#define REQUEST_METHOD_STATUS                   "STATUS"
#define REQUEST_METHOD_STATUS_RSP               "STATUS-RSP"
#define REQUEST_METHOD_INFO                     "INFO"
#define REQUEST_METHOD_INFO_RSP                 "INFO-RSP"
#define REQUEST_METHOD_DUMP                     "DUMP"
#define REQUEST_METHOD_DUMP_RSP                 "DUMP-RSP"
#define REQUEST_METHOD_PING                     "PING"
#define REQUEST_METHOD_PING_RSP                 "PING-RSP"
#define REQUEST_METHOD_UNKNOWN                  "UNKNOWN"

#define URI_HTTP "%5[HTPShtps]"
#define URI_HOST "%255[-_.abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789]"
#define URI_PORT "%6d"
#define URI_PATH "%4095[-_.!~*'();/?:@&=+$,%#abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789]"
#define HD1 URI_HTTP "://" URI_HOST ":" URI_PORT "/" URI_PATH
#define HD2 URI_HTTP "://" URI_HOST "/" URI_PATH
#define HD3 URI_HTTP "://" URI_HOST ":" URI_PORT
#define HD4 URI_HTTP "://" URI_HOST

enum {
    AM_TIMER_INACTIVE = 0,
    AM_TIMER_ACTIVE = 1 << 0,
    AM_TIMER_PAUSED = 1 << 1
};

#define AM_TIMER_USEC_PER_SEC 1000000

#define RC5_EXPANDED_KEYSIZE_LONG 64 
#define RC5_EXPANDED_KEYSIZE (RC5_EXPANDED_KEYSIZE_LONG * sizeof(uint32_t))
#define P32_MAGIC       0xB7E15163UL   
#define Q32_MAGIC       0x9E3779B9UL   
#define ROTL(x,s)       (((x) << (s)) | ((x) >> (32 - (s))))   
#define ROTR(x,s)       (((x) >> (s)) | ((x) << (32 - (s))))   
#define MASK32(x)       ((x) & 0xFFFFFFFFUL)
#define DECRYPT_ROUND(A,B,key) \
    B = int32_sub(B, *--key);\
    B = ROTR( MASK32(B), A & 0x1F) ^ A;\
    A = int32_sub(A, *--key);\
    A = ROTR( MASK32(A), B & 0x1F) ^ B  
#define ENCRYPT_ROUND(A,B,key)\
    A = MASK32(ROTL(A ^ B, B & 0x1F) + *key++);\
    B = MASK32(ROTL(B ^ A, A & 0x1F) + *key++)   

#ifdef _WIN32 

#define DT_DIR 1

struct dirent {
    long d_ino;
    off_t d_off;
    unsigned short d_reclen;
    unsigned char d_type;
    char d_name[AM_URI_SIZE + 1];
};

typedef struct {
    long handle;
    short offset;
    short finished;
    struct _finddata_t fileinfo;
    char *dir;
    struct dirent dent;
} DIR;

#endif

typedef struct {
    uint32_t S[RC5_EXPANDED_KEYSIZE_LONG];
    int r;
} am_rc5_key_t;

static am_timer_t am_timer_s = {0, 0, 0, 0};

static const char *hex_chars = "0123456789ABCDEF";

static const unsigned char base64_table[64] =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static int am_tolower(int c) {
    if (c >= 'A' && c <= 'Z') {
        c ^= 0x20;
    }
    return c;
}

size_t page_size(size_t size) {
    size_t p_size = 0;
#ifdef _WIN32
    SYSTEM_INFO si;
    GetSystemInfo(&si);
    p_size = si.dwPageSize;
#else
    p_size = sysconf(_SC_PAGE_SIZE);
#endif
    return p_size * ((size + p_size - 1) / p_size);
}

char is_big_endian() {

    union {
        uint32_t i;
        char c[4];
    } b = {0x01020304};

    return b.c[0] == 1;
}

int match(unsigned long instance_id, const char *subject, const char *pattern) {
    pcre *x = NULL;
    const char *error;
    int erroroffset, rc = -1;
    int offsets[3];
    if (subject == NULL || pattern == NULL) return 0;
    x = pcre_compile(pattern, 0, &error, &erroroffset, NULL);
    if (x != NULL) {
        rc = pcre_exec(x, NULL, subject, (int) strlen(subject),
                0, 0, offsets, 3);
        if (rc < 0) {
            am_log_debug(instance_id, "match(): '%s' does not match '%s'",
                    subject, pattern);
        }
        pcre_free(x);
    } else {
        if (error != NULL)
            am_log_debug(instance_id, "match(): error: %s", error);
    }
    return rc < 0 ? 1 : 0;
}

char *match_group(pcre *x, int cg, const char *subject, size_t *len) {
    int max_cg = (cg + 1)*3;
    size_t k = 0, slen = *len;
    int i, l, rc, ret_len = 0;
    unsigned int offset = 0;
    char *ret = NULL;
    int *ovector = malloc(max_cg * sizeof (int)); /* (max_capturing_groups+1)*3 */
    if (x == NULL || subject == NULL || ovector == NULL) {
        if (ovector != NULL) free(ovector);
        return NULL;
    }
    while (offset < slen && (rc = pcre_exec(x, 0, subject, (int) slen, offset, 0, ovector, max_cg)) >= 0) {
        for (i = 1/*skip the first pair: "identify the portion of the subject string matched by the entire pattern" */;
                i < rc; ++i) {
            char *rslt;
            if ((l = pcre_get_substring(subject, ovector, rc, i, (const char **) &rslt)) > 0) {
                ret = realloc(ret, ret_len + l + 1);
                if (ret == NULL) {
                    pcre_free_substring(rslt);
                    free(ovector);
                    return NULL;
                }
                /*return value is stored as:
                 * key\0value\0...
                 */
                memcpy(ret + ret_len, rslt, l);
                ret[ret_len + l] = 0;
                ret_len += l + 1;
                k++;
            }
            pcre_free_substring(rslt);
        }
        offset = ovector[1];
    }
    *len = k;
    free(ovector);
    return ret;
}

static void uri_normalize(struct url *url, char *path) {
    char *s, *o, *p = path != NULL ? strdup(path) : NULL;
    int i, m = 0, ls = 0;
    char **l = NULL, **lo = NULL;
    char u[AM_URI_SIZE];

    if (p == NULL) {
        if (url != NULL)
            url->error = path != NULL ? AM_ENOMEM : AM_EINVAL;
        return;
    }
    o = p; /*preserve original pointer*/

    /* split path into segments */
    while ((s = am_strsep(&p, "/")) != NULL) {
        if (strcmp(s, ".") == 0) continue; /* remove (ignore) single dot segments */
        l = (char **) realloc(l, sizeof (char *) * (++ls));
        l[ls - 1] = s;
    }
    if (ls == 0) {
        /* nothing to do here */
        free(o);
        if (url != NULL) url->error = AM_SUCCESS;
        return;
    }

    /* create a list for normalized segment storage */
    lo = (char **) calloc(ls, sizeof (char *));
    if (lo == NULL) {
        free(o);
        if (url != NULL) url->error = AM_ENOMEM;
        return;
    }

    for (i = 0; i < ls; i++) {
        if (strcmp(l[i], "..") == 0) {
            /* remove double dot segments */
            if (m-- <= 1) {
                m = 1;
                continue;
            }
            lo[m] = NULL;
        } else {
            lo[m++] = l[i];
        }
    }

    memset(&u[0], 0, sizeof (u));
    /* join normalized segments */
    for (i = 0; i < ls; i++) {
        if (lo[i] == NULL) break;
        if (i == 0) {
            strncpy(u, lo[i], sizeof (u) - 1);
            if ((i + 1) < ls && lo[i + 1] != NULL) {
                strncat(u, "/", sizeof (u) - 1);
            }
        } else {
            strncat(u, lo[i], sizeof (u) - 1);
            if ((i + 1) < ls && lo[i + 1] != NULL) {
                strncat(u, "/", sizeof (u) - 1);
            }
        }
    }
    memcpy(path, u, sizeof (u));

    free(lo);
    free(l);
    free(o);
    if (url != NULL) url->error = AM_SUCCESS;
}

int parse_url(const char *u, struct url *url) {
    int i = 0, port = 0;
    char last = 0;
    char *d, *p, uri[AM_URI_SIZE];

    if (url == NULL || u == NULL) {
        if (url != NULL) url->error = AM_EINVAL;
        return 1;
    }

    if (strlen(u) > (5 + 255 + 6 + AM_URI_SIZE/*max size of all sscanf format limits*/)) {
        url->error = AM_E2BIG;
        return 1;
    }

    url->error = url->ssl = url->port = 0;
    memset(&uri[0], 0, sizeof (uri));
    memset(&url->proto[0], 0, sizeof (url->proto));
    memset(&url->host[0], 0, sizeof (url->host));
    memset(&url->uri[0], 0, sizeof (url->uri));
    memset(&url->query[0], 0, sizeof (url->query));

    while (u) {
        if (sscanf(u, HD1, url->proto, url->host, &port, url->uri) == 4) {
            break;
        } else if (sscanf(u, HD2, url->proto, url->host, url->uri) == 3) {
            break;
        } else if (sscanf(u, HD3, url->proto, url->host, &port) == 3) {
            break;
        } else if (sscanf(u, HD4, url->proto, url->host) == 2) {
            break;
        } else {
            url->error = AM_EOF;
            return 1;
        }
    }
    url->port = port < 0 ? -(port) : port;
    if (strcasecmp(url->proto, "https") == 0) {
        url->ssl = 1;
    } else {
        url->ssl = 0;
    }
    if (strcasecmp(url->proto, "https") == 0 && url->port == 0) {
        url->port = 443;
    } else if (strcasecmp(url->proto, "http") == 0 && url->port == 0) {
        url->port = 80;
    }
    if (!ISVALID(url->uri)) {
        strcpy(url->uri, "/");
    } else if (url->uri[0] != '/') {
        size_t ul = strlen(url->uri);
        if (ul < sizeof (url->uri)) {
            memmove(url->uri + 1, url->uri, ul);
        }
        url->uri[0] = '/';
    }

    /* split out a query string, if any (not modifying it later) */
    p = strchr(url->uri, '?');
    if (p != NULL) {
        strncpy(url->query, p, sizeof (url->query) - 1);
        *p = 0;
    }

    /* decode uri */
    d = url_decode(url->uri);
    if (d == NULL) {
        url->error = AM_ENOMEM;
        return 1;
    }

    p = d;
    /* replace all consecutive '/' with a single '/' */
    while (*p != '\0') {
        if (*p != '/' || (*p == '/' && last != '/')) {
            uri[i++] = *p;
        }
        last = *p;
        p++;
    }
    free(d);

    /* normalize uri path segments, RFC-2396, section-5.2 */
    uri_normalize(url, uri);

    strncpy(url->uri, uri, sizeof (url->uri) - 1);
    return 0;
}

char *url_encode(const char *str) {
    if (str != NULL) {
        unsigned char *pstr = (unsigned char *) str;
        char *buf = (char *) calloc(1, strlen(str) * 3 + 1), *pbuf = buf;
        if (buf == NULL) return NULL;
        while (*pstr) {
            if (isalnum(*pstr) || *pstr == '-' || *pstr == '_' || *pstr == '.' || *pstr == '~') {
                *pbuf++ = *pstr;
            } else if (*pstr == ' ') {
                *pbuf++ = '%';
                *pbuf++ = '2';
                *pbuf++ = '0';
            } else {
                *pbuf++ = '%', *pbuf++ = hex_chars[((*pstr) >> 4) & 0xF], *pbuf++ = hex_chars[(*pstr) & 0xF];
            }
            pstr++;
        }
        *pbuf = '\0';
        return buf;
    }
    return NULL;
}

char *url_decode(const char *str) {
    size_t s = 0, url_len = 0;
    int d = 0;
    char c;
    char *dest = NULL;
    if (str != NULL) {
        url_len = strlen(str) + 1;
        dest = calloc(1, url_len);
        if (!dest)
            return NULL;
        while (s < url_len) {
            c = str[s++];
            if (c == '%' && s + 2 < url_len) {
                char c2 = str[s++];
                char c3 = str[s++];
                if (isxdigit(c2) && isxdigit(c3)) {
                    c2 = am_tolower(c2);
                    c3 = am_tolower(c3);
                    if (c2 <= '9') {
                        c2 = c2 - '0';
                    } else {
                        c2 = c2 - 'a' + 10;
                    }
                    if (c3 <= '9') {
                        c3 = c3 - '0';
                    } else {
                        c3 = c3 - 'a' + 10;
                    }
                    dest[d++] = 16 * c2 + c3;
                } else {
                    dest[d++] = c;
                    dest[d++] = c2;
                    dest[d++] = c3;
                }
            } else if (c == '+') {
                dest[d++] = ' ';
            } else {
                dest[d++] = c;
            }
        }
    }
    return dest;
}

int am_vasprintf(char **buffer, const char *fmt, va_list arg) {
    int size;
    va_list ap;
    *buffer = NULL;
    va_copy(ap, arg);
    size = vsnprintf(NULL, 0, fmt, ap);
    if (size >= 0) {
        if ((*buffer = malloc(++size)) != NULL) {
            va_end(ap);
            va_copy(ap, arg);
            if ((size = vsnprintf(*buffer, size, fmt, ap)) < 0) {
                free(*buffer);
                *buffer = NULL;
            }
        }
    }
    va_end(ap);
    return size;
}

int am_asprintf(char **buffer, const char *fmt, ...) {
    int size;
    char *tmp = NULL;
    va_list ap;
    va_start(ap, fmt);
    tmp = *buffer;
    size = am_vasprintf(buffer, fmt, ap);
    free(tmp);
    va_end(ap);
    return size;
}

int gzip_inflate(const char *compressed, size_t *compressed_sz, char **uncompressed) {
    size_t full_length, half_length, uncompLength;
    char *uncomp = NULL;
    z_stream strm;
    int done = 1;

    if (compressed == NULL || compressed_sz == NULL || *compressed_sz == 0) {
        return 1;
    }

    full_length = *compressed_sz;
    half_length = *compressed_sz / 2;
    uncompLength = full_length;

    uncomp = (char *) calloc(sizeof (char), uncompLength);
    if (uncomp == NULL) return 1;

    strm.next_in = (Bytef *) compressed;
    strm.avail_in = (uInt) * compressed_sz;
    strm.total_out = 0;
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;

    if (inflateInit2(&strm, (16 + MAX_WBITS)) != Z_OK) {
        free(uncomp);
        return 1;
    }

    while (done) {
        int err;

        if (strm.total_out >= uncompLength) {
            char *uncomp2 = (char *) calloc(sizeof (char), uncompLength + half_length);
            memcpy(uncomp2, uncomp, uncompLength);
            uncompLength += half_length;
            free(uncomp);
            uncomp = uncomp2;
        }

        strm.next_out = (Bytef *) (uncomp + strm.total_out);
        strm.avail_out = (uInt) uncompLength - strm.total_out;

        err = inflate(&strm, Z_SYNC_FLUSH);
        if (err == Z_STREAM_END) done = 0;
        else if (err != Z_OK) {
            break;
        }
    }

    if (inflateEnd(&strm) != Z_OK) {
        free(uncomp);
        return 1;
    }

    *uncompressed = uncomp;
    *compressed_sz = strm.total_out;
    return 0;
}

int gzip_deflate(const char *uncompressed, size_t *uncompressed_sz, char **compressed) {
    uLong comp_length, ucomp_length;
    char *comp = NULL;
    z_stream strm;
    int deflate_status;

    if (uncompressed == NULL || uncompressed_sz == NULL || *uncompressed_sz == 0) {
        return 1;
    }

    ucomp_length = (uLong) * uncompressed_sz;
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;
    strm.opaque = Z_NULL;
    strm.total_out = 0;
    strm.next_in = (Bytef *) uncompressed;
    strm.avail_in = ucomp_length;

    if (deflateInit2(&strm, Z_DEFAULT_COMPRESSION, Z_DEFLATED, (15 + 16),
            8, Z_DEFAULT_STRATEGY) != Z_OK) {
        free(comp);
        return 1;
    }

    comp_length = deflateBound(&strm, ucomp_length);
    comp = (char *) calloc(sizeof (char), comp_length);
    if (comp == NULL) {
        deflateEnd(&strm);
        return 1;
    }

    do {
        strm.next_out = (Bytef *) (comp + strm.total_out);
        strm.avail_out = comp_length - strm.total_out;
        deflate_status = deflate(&strm, Z_FINISH);
    } while (deflate_status == Z_OK);

    if (deflate_status != Z_STREAM_END) {
        free(comp);
        deflateEnd(&strm);
        return 1;
    }

    if (deflateEnd(&strm) != Z_OK) {
        free(comp);
        return 1;
    }

    *compressed = comp;
    *uncompressed_sz = strm.total_out;
    return 0;
}

unsigned long am_instance_id(const char *instance_id) {
    uLong crc = crc32(0L, Z_NULL, 0);
    if (instance_id == NULL) return 0;
    crc = crc32(crc, (const Bytef *) instance_id, (uInt) strlen(instance_id));
    return crc;
}

void trim(char *a, char w) {
    char *b = a;
    if (w == 0) {
        while (isspace(*b)) ++b;
    } else {
        while (*b == w) ++b;
    }
    while (*b) *a++ = *b++;
    *a = '\0';
    if (w == 0) {
        while (isspace(*--a)) *a = '\0';
    } else {
        while ((*--a) == w) *a = '\0';
    }
}

char *am_strsep(char **str, const char *delim) {
    char *s, *t;
    const char *sp;
    int c, sc;
    if ((s = *str) == NULL) return NULL;
    for (t = s;;) {
        c = *s++;
        sp = delim;
        do {
            if ((sc = *sp++) == c) {
                if (c == 0) s = NULL;
                else s[-1] = 0;
                *str = s;
                return t;
            }
        } while (sc != 0);
    }
}

int compare_property(const char *line, const char *property) {
    if (ISVALID(line) && ISVALID(property)) {
        size_t l = strlen(property);
        if (strncmp(line, property, l) == 0 && line[l] != '\0' &&
                (line[l] == ' ' || line[l] == '=' || line[l] == '[')) {
            return AM_SUCCESS;
        }
    }
    return AM_NOT_FOUND;
}

int get_line(char **line, size_t *size, FILE *file) {
    int c;
    unsigned int i = 0;

#define DEFAULT_LINE_LEN 256
    if (*line == NULL || *size == 0) {
        *line = malloc(DEFAULT_LINE_LEN);
        if (!(*line)) {
            return -1;
        }
        *size = DEFAULT_LINE_LEN;
    }

    while (1) {
        c = getc(file);
        if (c < 0) {
            return -1; /* EOF */
        }

        (*line)[i++] = (char) c;
        /* time to expand the buffer? */
        if (i >= *size) {
            char *new = realloc(*line, (*size) * 2);
            if (!new) {
                return -1;
            }
            *line = new;
            (*size) *= 2;
        }
        if (c == '\n' || c == '\r') {
            break;
        }
    };

    (*line)[i] = 0;
    return i;
}

const char *am_method_num_to_str(char method) {
    const char *mn;
    switch (method) {
        case AM_REQUEST_GET:
            mn = REQUEST_METHOD_GET;
            break;
        case AM_REQUEST_POST:
            mn = REQUEST_METHOD_POST;
            break;
        case AM_REQUEST_HEAD:
            mn = REQUEST_METHOD_HEAD;
            break;
        case AM_REQUEST_PUT:
            mn = REQUEST_METHOD_PUT;
            break;
        case AM_REQUEST_DELETE:
            mn = REQUEST_METHOD_DELETE;
            break;
        case AM_REQUEST_TRACE:
            mn = REQUEST_METHOD_TRACE;
            break;
        case AM_REQUEST_OPTIONS:
            mn = REQUEST_METHOD_OPTIONS;
            break;
        case AM_REQUEST_CONNECT:
            mn = REQUEST_METHOD_CONNECT;
            break;
        case AM_REQUEST_COPY:
            mn = REQUEST_METHOD_COPY;
            break;
        case AM_REQUEST_INVALID:
            mn = REQUEST_METHOD_INVALID;
            break;
        case AM_REQUEST_LOCK:
            mn = REQUEST_METHOD_LOCK;
            break;
        case AM_REQUEST_UNLOCK:
            mn = REQUEST_METHOD_UNLOCK;
            break;
        case AM_REQUEST_MKCOL:
            mn = REQUEST_METHOD_MKCOL;
            break;
        case AM_REQUEST_MOVE:
            mn = REQUEST_METHOD_MOVE;
            break;
        case AM_REQUEST_PATCH:
            mn = REQUEST_METHOD_PATCH;
            break;
        case AM_REQUEST_PROPFIND:
            mn = REQUEST_METHOD_PROPFIND;
            break;
        case AM_REQUEST_PROPPATCH:
            mn = REQUEST_METHOD_PROPPATCH;
            break;
        case AM_REQUEST_VERSION_CONTROL:
            mn = REQUEST_METHOD_VERSION_CONTROL;
            break;
        case AM_REQUEST_CHECKOUT:
            mn = REQUEST_METHOD_CHECKOUT;
            break;
        case AM_REQUEST_UNCHECKOUT:
            mn = REQUEST_METHOD_UNCHECKOUT;
            break;
        case AM_REQUEST_CHECKIN:
            mn = REQUEST_METHOD_CHECKIN;
            break;
        case AM_REQUEST_UPDATE:
            mn = REQUEST_METHOD_UPDATE;
            break;
        case AM_REQUEST_LABEL:
            mn = REQUEST_METHOD_LABEL;
            break;
        case AM_REQUEST_REPORT:
            mn = REQUEST_METHOD_REPORT;
            break;
        case AM_REQUEST_MKWORKSPACE:
            mn = REQUEST_METHOD_MKWORKSPACE;
            break;
        case AM_REQUEST_MKACTIVITY:
            mn = REQUEST_METHOD_MKACTIVITY;
            break;
        case AM_REQUEST_BASELINE_CONTROL:
            mn = REQUEST_METHOD_BASELINE_CONTROL;
            break;
        case AM_REQUEST_MERGE:
            mn = REQUEST_METHOD_MERGE;
            break;
        case AM_REQUEST_CONFIG:
            mn = REQUEST_METHOD_CONFIG;
            break;
        case AM_REQUEST_ENABLE_APP:
            mn = REQUEST_METHOD_ENABLE_APP;
            break;
        case AM_REQUEST_DISABLE_APP:
            mn = REQUEST_METHOD_DISABLE_APP;
            break;
        case AM_REQUEST_STOP_APP:
            mn = REQUEST_METHOD_STOP_APP;
            break;
        case AM_REQUEST_STOP_APP_RSP:
            mn = REQUEST_METHOD_STOP_APP_RSP;
            break;
        case AM_REQUEST_REMOVE_APP:
            mn = REQUEST_METHOD_REMOVE_APP;
            break;
        case AM_REQUEST_STATUS:
            mn = REQUEST_METHOD_STATUS;
            break;
        case AM_REQUEST_STATUS_RSP:
            mn = REQUEST_METHOD_STATUS_RSP;
            break;
        case AM_REQUEST_INFO:
            mn = REQUEST_METHOD_INFO;
            break;
        case AM_REQUEST_INFO_RSP:
            mn = REQUEST_METHOD_INFO_RSP;
            break;
        case AM_REQUEST_DUMP:
            mn = REQUEST_METHOD_DUMP;
            break;
        case AM_REQUEST_DUMP_RSP:
            mn = REQUEST_METHOD_DUMP_RSP;
            break;
        case AM_REQUEST_PING:
            mn = REQUEST_METHOD_PING;
            break;
        case AM_REQUEST_PING_RSP:
            mn = REQUEST_METHOD_PING_RSP;
            break;
        case AM_REQUEST_UNKNOWN:
        default:
            mn = REQUEST_METHOD_UNKNOWN;
            break;
    }
    return mn;
}

char am_method_str_to_num(const char *method_str) {
    char method = AM_REQUEST_UNKNOWN;
    if (method_str != NULL) {
        if (!strcasecmp(method_str, REQUEST_METHOD_GET))
            method = AM_REQUEST_GET;
        else if (!strcasecmp(method_str, REQUEST_METHOD_POST))
            method = AM_REQUEST_POST;
        else if (!strcasecmp(method_str, REQUEST_METHOD_HEAD))
            method = AM_REQUEST_HEAD;
        else if (!strcasecmp(method_str, REQUEST_METHOD_PUT))
            method = AM_REQUEST_PUT;
        else if (!strcasecmp(method_str, REQUEST_METHOD_DELETE))
            method = AM_REQUEST_DELETE;
        else if (!strcasecmp(method_str, REQUEST_METHOD_TRACE))
            method = AM_REQUEST_TRACE;
        else if (!strcasecmp(method_str, REQUEST_METHOD_OPTIONS))
            method = AM_REQUEST_OPTIONS;
        else if (!strcasecmp(method_str, REQUEST_METHOD_CONNECT))
            method = AM_REQUEST_CONNECT;
        else if (!strcasecmp(method_str, REQUEST_METHOD_COPY))
            method = AM_REQUEST_COPY;
        else if (!strcasecmp(method_str, REQUEST_METHOD_INVALID))
            method = AM_REQUEST_INVALID;
        else if (!strcasecmp(method_str, REQUEST_METHOD_LOCK))
            method = AM_REQUEST_LOCK;
        else if (!strcasecmp(method_str, REQUEST_METHOD_UNLOCK))
            method = AM_REQUEST_UNLOCK;
        else if (!strcasecmp(method_str, REQUEST_METHOD_MKCOL))
            method = AM_REQUEST_MKCOL;
        else if (!strcasecmp(method_str, REQUEST_METHOD_MOVE))
            method = AM_REQUEST_MOVE;
        else if (!strcasecmp(method_str, REQUEST_METHOD_PATCH))
            method = AM_REQUEST_PATCH;
        else if (!strcasecmp(method_str, REQUEST_METHOD_PROPFIND))
            method = AM_REQUEST_PROPFIND;
        else if (!strcasecmp(method_str, REQUEST_METHOD_PROPPATCH))
            method = AM_REQUEST_PROPPATCH;
        else if (!strcasecmp(method_str, REQUEST_METHOD_VERSION_CONTROL))
            method = AM_REQUEST_VERSION_CONTROL;
        else if (!strcasecmp(method_str, REQUEST_METHOD_CHECKOUT))
            method = AM_REQUEST_CHECKOUT;
        else if (!strcasecmp(method_str, REQUEST_METHOD_UNCHECKOUT))
            method = AM_REQUEST_UNCHECKOUT;
        else if (!strcasecmp(method_str, REQUEST_METHOD_CHECKIN))
            method = AM_REQUEST_CHECKIN;
        else if (!strcasecmp(method_str, REQUEST_METHOD_UPDATE))
            method = AM_REQUEST_UPDATE;
        else if (!strcasecmp(method_str, REQUEST_METHOD_LABEL))
            method = AM_REQUEST_LABEL;
        else if (!strcasecmp(method_str, REQUEST_METHOD_REPORT))
            method = AM_REQUEST_REPORT;
        else if (!strcasecmp(method_str, REQUEST_METHOD_MKWORKSPACE))
            method = AM_REQUEST_MKWORKSPACE;
        else if (!strcasecmp(method_str, REQUEST_METHOD_MKACTIVITY))
            method = AM_REQUEST_MKACTIVITY;
        else if (!strcasecmp(method_str, REQUEST_METHOD_BASELINE_CONTROL))
            method = AM_REQUEST_BASELINE_CONTROL;
        else if (!strcasecmp(method_str, REQUEST_METHOD_MERGE))
            method = AM_REQUEST_MERGE;
        else if (!strcasecmp(method_str, REQUEST_METHOD_CONFIG))
            method = AM_REQUEST_CONFIG;
        else if (!strcasecmp(method_str, REQUEST_METHOD_ENABLE_APP))
            method = AM_REQUEST_ENABLE_APP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_DISABLE_APP))
            method = AM_REQUEST_DISABLE_APP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_STOP_APP))
            method = AM_REQUEST_STOP_APP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_STOP_APP_RSP))
            method = AM_REQUEST_STOP_APP_RSP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_REMOVE_APP))
            method = AM_REQUEST_REMOVE_APP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_STATUS))
            method = AM_REQUEST_STATUS;
        else if (!strcasecmp(method_str, REQUEST_METHOD_STATUS_RSP))
            method = AM_REQUEST_STATUS_RSP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_INFO))
            method = AM_REQUEST_INFO;
        else if (!strcasecmp(method_str, REQUEST_METHOD_INFO_RSP))
            method = AM_REQUEST_INFO_RSP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_DUMP))
            method = AM_REQUEST_DUMP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_DUMP_RSP))
            method = AM_REQUEST_DUMP_RSP;
        else if (!strcasecmp(method_str, REQUEST_METHOD_PING))
            method = AM_REQUEST_PING;
        else if (!strcasecmp(method_str, REQUEST_METHOD_PING_RSP))
            method = AM_REQUEST_PING_RSP;
        else
            method = AM_REQUEST_UNKNOWN;
    }
    return method;
}

am_status_t get_cookie_value(am_request_t *rq, const char *separator, const char *cookie_name,
        const char *cookie_header_val, char **value) {
    size_t value_len = 0, ec = 0;
    am_status_t found = AM_NOT_FOUND;
    char *a, *b, *header_val = NULL, *c = NULL;
    if (!ISVALID(cookie_name)) {
        return AM_EINVAL;
    } else if (!ISVALID(cookie_header_val)) {
        return AM_NOT_FOUND;
    } else *value = NULL;
    header_val = strdup(cookie_header_val);
    if (header_val) {
        am_log_debug(rq->instance_id, "get_cookie_value(%s): parsing cookie header: %s", separator, cookie_header_val);
        for ((a = strtok_r(header_val, separator, &b)); a; (a = strtok_r(NULL, separator, &b))) {
            if (strcmp(separator, "=") == 0 || strcmp(separator, "~") == 0) {
                /* trim any leading/trailing whitespace */
                trim(a, 0);
                if (found != AM_SUCCESS && strcmp(a, cookie_name) == 0) found = AM_SUCCESS;
                else if (found == AM_SUCCESS && a[0] != '\0') {
                    value_len = strlen(a);
                    if ((*value = strdup(a)) == NULL) {
                        found = AM_NOT_FOUND;
                    } else {
                        (*value)[value_len] = '\0';
                        /* trim any leading/trailing double-quotes */
                        trim(*value, '"');
                    }
                }
            } else {
                if (strstr(a, cookie_name) == NULL) continue;
                for (ec = 0, c = a; *c != '\0'; ++c) {
                    if (*c == '=') ++ec;
                }
                if (ec > 1) {
                    c = strchr(a, '=');
                    *c = '~';
                    if ((found = get_cookie_value(rq, "~", cookie_name, a, value)) == AM_SUCCESS) break;
                } else {
                    if ((found = get_cookie_value(rq, "=", cookie_name, a, value)) == AM_SUCCESS) break;
                }
            }
        }
        free(header_val);
    } else
        return AM_ENOMEM;
    return found;
}

am_status_t get_token_from_url(am_request_t *rq) {
    char *token, *tmp = ISVALID(rq->url.query) ?
            strdup(rq->url.query + 1) : NULL;
    char *query = NULL;
    int ql = 0;
    char *o = tmp;

    if (tmp == NULL) return AM_ENOMEM;

    while ((token = am_strsep(&tmp, "&")) != NULL) {
        if (!ISVALID(rq->token) && strncmp(token,
                rq->conf->cookie_name, strlen(rq->conf->cookie_name)) == 0) {
            /* session token as a query parameter (cookie-less mode) */
            char *v = strstr(token, "=");
            if (v != NULL && *(v + 1) != '\n') {
                rq->token = strdup(v + 1);
            }
        } else if (!ISVALID(rq->token) && strncmp(token, "LARES=", 6) == 0) {
            /* session token (LARES/SAML encoded) as a query parameter */
            size_t clear_sz = strlen(token) - 6;
            char *clear = clear_sz > 0 ? base64_decode(token + 6, &clear_sz) : NULL;
            if (clear != NULL) {
                struct am_namevalue *e, *t, *session_list = NULL;
                session_list = am_parse_session_saml(rq->instance_id, clear, clear_sz);
                if (session_list != NULL) {

                    am_list_for_each(session_list, e, t) {
                        if (strcmp(e->n, "sid") == 0 && ISVALID(e->v)) {
                            rq->token = strdup(e->v);
                            break;
                        }
                    }
                    delete_am_namevalue_list(&session_list);
                }
                free(clear);
            }
        } else {
            /* reconstruct query parameters w/o a session token(s) */
            if (query == NULL) {
                ql += am_asprintf(&query, "?%s&", token);
            } else {
                ql += am_asprintf(&query, "%s%s&", query, token);
            }
        }
    }

    if (ql > 0 && query[ql - 1] == '&') {
        query[ql - 1] = 0;
        strncpy(rq->url.query, query, sizeof (rq->url.query) - 1);
    } else if (ql == 0 && ISVALID(rq->token)) {
        /*token is the only query parameter - clear it*/
        memset(rq->url.query, 0, sizeof (rq->url.query));
        /* TODO: should a question mark be left there even when token is the only parameter? */
    }
    if (query != NULL) free(query);
    if (o != NULL) free(o);

    return ISVALID(rq->token) ? AM_SUCCESS : AM_NOT_FOUND;
}

int remove_cookie(am_request_t *rq, const char *cookie_name, char **cookie_hdr) {
    char *tmp, *tok, *last = NULL;
    size_t cookie_name_len;

    if (rq == NULL || rq->ctx == NULL || !ISVALID(cookie_name)) {
        return AM_EINVAL;
    }

    if (!ISVALID(rq->cookies)) {
        return AM_SUCCESS;
    }

    if (strstr(rq->cookies, cookie_name) == NULL) {
        return AM_NOT_FOUND;
    }

    tmp = strdup(rq->cookies);
    if (tmp == NULL) return AM_ENOMEM;

    cookie_name_len = strlen(cookie_name);

    tok = strtok_r(tmp, ";", &last);
    while (tok != NULL) {
        char match = AM_FALSE;
        char *equal_sign = strchr(tok, '=');
        /* trim space before the cookie name in the cookie header */
        while (isspace(*tok)) tok++;
        if (equal_sign != NULL && equal_sign != tok) {
            /* trim white space after the cookie name in the cookie header */
            while ((--equal_sign) >= tok && isspace(*equal_sign))
                ;
            equal_sign++;
            /* now compare the cookie names */
            if (equal_sign != tok && (equal_sign - tok) == cookie_name_len &&
                    !strncmp(tok, cookie_name, cookie_name_len)) {
                match = AM_TRUE;
            }
        }
        /* put cookie in a header only if it didn't match cookie name */
        if (!match) {
            am_asprintf(cookie_hdr, "%s%s%s",
                    *cookie_hdr == NULL ? "" : *cookie_hdr,
                    *cookie_hdr != NULL ? ";" : "",
                    tok);
        }
        tok = strtok_r(NULL, ";", &last);
    }

    free(tmp);
    return AM_SUCCESS;
}

char *load_file(const char *filepath, size_t *data_sz) {
    char *text = NULL;
    int fd;
    struct stat st;
    if (stat(filepath, &st) == -1) {
        return NULL;
    }
#ifdef _WIN32
    fd = _open(filepath, _O_BINARY | _O_RDONLY);
#else
    fd = open(filepath, O_RDONLY);
#endif
    if (fd == -1) {
        return NULL;
    }
    text = malloc(st.st_size + 1);
    if (text != NULL) {
        if (st.st_size != read(fd, text, st.st_size)) {
            close(fd);
            free(text);
            return NULL;
        }
        text[st.st_size] = '\0';
        if (data_sz) *data_sz = st.st_size;
    }
    close(fd);
    return text;
}

ssize_t write_file(const char *filepath, const void *data, size_t data_sz) {
    int fd;
    ssize_t wr = 0;
    if (data == NULL || data_sz == 0) return AM_EINVAL;
#ifdef _WIN32
    fd = _open(filepath, _O_CREAT | _O_WRONLY | _O_TRUNC | _O_BINARY, _S_IWRITE);
#else
    fd = open(filepath, O_CREAT | O_WRONLY | O_TRUNC, S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP);
#endif
    if (fd == -1) {
        return AM_EPERM;
    }
    if (data_sz != (size_t) (wr = write(fd, data,
#ifdef _WIN32
            (unsigned int)
#endif
            data_sz))) {
        close(fd);
        return AM_EOF;
    } else {
#ifdef _WIN32
        _commit(fd);
#else
        fsync(fd);
#endif
    }
    close(fd);
    return wr;
}

char file_exists(const char *fn) {
#ifdef _WIN32
    if (_access(fn, 6) == 0) {
        return AM_TRUE;
    }
#else
    struct stat sb;
    if (stat(fn, &sb) == 0) {
        if (S_ISREG(sb.st_mode) || S_ISDIR(sb.st_mode) || S_ISLNK(sb.st_mode)) {
            if (S_ISDIR(sb.st_mode)) {
                int mask = 0200 | 020;
                /*we need owner/group write permission on a directory*/
                if (!(sb.st_mode & mask)) {
                    return AM_FALSE;
                }
            }
            return AM_TRUE;
        }
    }
#endif
    return AM_FALSE;
}

#ifndef _AIX

size_t strnlen(const char *string, size_t maxlen) {
    const char *end = memchr(string, '\0', maxlen);
    return end ? end - string : maxlen;
}

char *strndup(const char *s, size_t n) {
    size_t len = (s == NULL) ? 0 : strnlen(s, n);
    char *new = (len == 0) ? NULL : malloc(len + 1);
    if (new == NULL) {
        return NULL;
    }
    new[len] = '\0';
    return memcpy(new, s, len);
}

#endif

char *stristr(char *str1, char *str2) {
    char *a, *b, *t, *ret = NULL;
    if (!str1 || !str2) return NULL;
    a = strdup(str1);
    b = strdup(str2);
    if (a && b) {
        t = a;
        while (*t) {
            *t = (char) tolower(*t);
            t++;
        }
        t = b;
        while (*t) {
            *t = (char) tolower(*t);
            t++;
        }
        t = strstr(a, b);
        if (t) {
            ret = str1 + (t - a);
        }
    }
    if (a) free(a);
    if (b) free(b);
    return ret;
}

char *base64_decode(const char *src, size_t *sz) {
#ifdef _WIN32
    DWORD ulBlobSz = 0, ulSkipped = 0, ulFmt = 0;
    BYTE *out = NULL;
    if (src == NULL || sz == NULL) return NULL;
    if (CryptStringToBinaryA(src, (DWORD) * sz, CRYPT_STRING_BASE64, NULL,
            &ulBlobSz, &ulSkipped, &ulFmt) == TRUE) {
        if ((out = malloc(ulBlobSz + 1)) != NULL) {
            memset(out, 0, ulBlobSz + 1);
            if (CryptStringToBinaryA(src, (DWORD) * sz, CRYPT_STRING_BASE64,
                    out, &ulBlobSz, &ulSkipped, &ulFmt) == TRUE) {
                out[ulBlobSz] = 0;
                *sz = ulBlobSz;
            }
        }
    }
#else
    unsigned char table[256], *out, *pos, *in;
    size_t i, count;

    if (src == NULL || sz == NULL) return NULL;

    memset(table, 64, 256);
    for (i = 0; i < sizeof (base64_table); i++) {
        table[base64_table[i]] = i;
    }

    in = (unsigned char *) src;
    while (table[*(in++)] <= 63);

    i = (in - (unsigned char *) src) - 1;
    count = (((i + 3) / 4) * 3) + 1;

    pos = out = malloc(count);
    if (out == NULL) return NULL;

    in = (unsigned char *) src;

    while (i > 4) {
        *(pos++) = (unsigned char) (table[*in] << 2 | table[in[1]] >> 4);
        *(pos++) = (unsigned char) (table[in[1]] << 4 | table[in[2]] >> 2);
        *(pos++) = (unsigned char) (table[in[2]] << 6 | table[in[3]]);
        in += 4;
        i -= 4;
    }

    if (i > 1) {
        *(pos++) = (unsigned char) (table[*in] << 2 | table[in[1]] >> 4);
    }
    if (i > 2) {
        *(pos++) = (unsigned char) (table[in[1]] << 4 | table[in[2]] >> 2);
    }
    if (i > 3) {
        *(pos++) = (unsigned char) (table[in[2]] << 6 | table[in[3]]);
    }

    *(pos++) = '\0';
    count -= (4 - i) & 3;
    *sz = count - 1;
#endif
    return (char *) out;
}

char *base64_encode(const void *in, size_t *sz) {
    int i;
    char *p, *out;
    const uint8_t *src = in;
    if (src == NULL || sz == NULL) return NULL;

    p = out = malloc(((*sz + 2) / 3 * 4) + 1);
    if (out == NULL) return NULL;

    for (i = 0; i < *sz - 2; i += 3) {
        *p++ = base64_table[(src[i] >> 2) & 0x3F];
        *p++ = base64_table[((src[i] & 0x3) << 4) | ((int) (src[i + 1] & 0xF0) >> 4)];
        *p++ = base64_table[((src[i + 1] & 0xF) << 2) | ((int) (src[i + 2] & 0xC0) >> 6)];
        *p++ = base64_table[src[i + 2] & 0x3F];
    }

    if (i < *sz) {
        *p++ = base64_table[(src[i] >> 2) & 0x3F];
        if (i == (*sz - 1)) {
            *p++ = base64_table[((src[i] & 0x3) << 4)];
            *p++ = '=';
        } else {
            *p++ = base64_table[((src[i] & 0x3) << 4) | ((int) (src[i + 1] & 0xF0) >> 4)];
            *p++ = base64_table[((src[i + 1] & 0xF) << 2)];
        }
        *p++ = '=';
    }

    *p++ = '\0';
    *sz = p - out - 1;
    return out;
}

void delete_am_cookie_list(struct am_cookie **list) {
    struct am_cookie *t = *list;
    if (t != NULL) {
        delete_am_cookie_list(&t->next);
        if (t->name != NULL) free(t->name);
        if (t->value != NULL) free(t->value);
        if (t->domain != NULL) free(t->domain);
        if (t->max_age != NULL) free(t->max_age);
        if (t->path != NULL) free(t->path);
        free(t);
        t = NULL;
    }
}

int char_count(const char *string, int c, int *last) {
    int j, count;
    for (j = 0, count = 0; string[j]; j++)
        count += (string[j] == c);
    if (last) *last = j > 0 ? string[j - 1] : 0;
    return count;
}

int concat(char **str, size_t *str_sz, const char *s2, size_t s2sz) {
    size_t len = 0;
    if (str == NULL || s2 == NULL) {
        return AM_EINVAL;
    }
    if (*str != NULL) {
        len = str_sz == NULL ? strlen(*str) : *str_sz;
    }
    len += s2sz;
    *str = realloc(*str, len + 1);
    if (*str == NULL) {
        return AM_ENOMEM;
    }
    (*str)[len - s2sz] = 0;
    strncat(*str, s2, s2sz);
    if (str_sz != NULL) *str_sz = len;
    return AM_SUCCESS;
}

void uuid(char *buf, size_t buflen) {

    union {

        struct {
            uint32_t time_low;
            uint16_t time_mid;
            uint16_t time_hi_and_version;
            uint8_t clk_seq_hi_res;
            uint8_t clk_seq_low;
            uint8_t node[6];
            uint16_t node_low;
            uint32_t node_hi;
        } u;
        unsigned char __rnd[16];
    } uuid_data;

#ifdef _WIN32
    HCRYPTPROV hcp;
    if (CryptAcquireContextA(&hcp, NULL, NULL, PROV_RSA_FULL,
            CRYPT_VERIFYCONTEXT | CRYPT_SILENT)) {
        CryptGenRandom(hcp, sizeof (uuid_data), uuid_data.__rnd);
        CryptReleaseContext(hcp, 0);
    }
#else
    FILE *fp = fopen("/dev/urandom", "r");
    if (fp != NULL) {
        fread(uuid_data.__rnd, 1, sizeof (uuid_data), fp);
        fclose(fp);
    }
#endif

    uuid_data.u.clk_seq_hi_res = (uuid_data.u.clk_seq_hi_res & ~0xC0) | 0x80;
    uuid_data.u.time_hi_and_version = htons((uuid_data.u.time_hi_and_version & ~0xF000) | 0x4000);

    snprintf(buf, buflen, "%08x-%04x-%04x-%02x%02x-%02x%02x%02x%02x%02x%02x",
            uuid_data.u.time_low, uuid_data.u.time_mid, uuid_data.u.time_hi_and_version,
            uuid_data.u.clk_seq_hi_res, uuid_data.u.clk_seq_low,
            uuid_data.u.node[0], uuid_data.u.node[1], uuid_data.u.node[2],
            uuid_data.u.node[3], uuid_data.u.node[4], uuid_data.u.node[5]);
}

int am_session_decode(am_request_t *r) {
    size_t tl;
    int i, nv = 0;
    char *begin, *end;

    enum {
        AM_NA, AM_SI, AM_SK, AM_S1
    } ty = AM_NA;

    char *token = (r != NULL && ISVALID(r->token)) ?
            strdup(r->token) : NULL;

    if (token == NULL) return AM_EINVAL;

    memset(&r->si, 0, sizeof (struct am_session_info));
    tl = strlen(token);

    if (strchr(token, '*') != NULL) {
        /* c66 decode */
        char first_star = AM_TRUE;
        for (i = 0; i < tl; i++) {
            if (token[i] == '-') {
                token[i] = '+';
            } else if (token[i] == '_') {
                token[i] = '/';
            } else if (token[i] == '.') {
                token[i] = '=';
            } else if (token[i] == '*') {
                if (first_star) {
                    first_star = AM_FALSE;
                    token[i] = '@';
                } else {
                    token[i] = '#';
                }
            }
        }
    }

    begin = strstr(token, "@");
    if (begin != NULL) {
        end = strstr(begin + 1, "#");
        if (end != NULL) {
            size_t ssz = end - begin - 1;
            unsigned char *c = ssz > 0 ?
                    (unsigned char *) base64_decode(begin + 1, &ssz) : NULL;
            if (c != NULL) {
                unsigned char *raw = c;
                size_t l = ssz;

                while (l > 0) {
                    uint16_t sz;
                    uint8_t len[2]; /*network byte order*/

                    memcpy(len, raw, sizeof (len));
                    if (is_big_endian()) {
                        sz = (*((uint16_t *) len));
                    } else {
                        sz = len[1] | len[0] << 8;
                    }

                    l -= sizeof (len);
                    raw += sizeof (len);

                    if (nv % 2 == 0) {
                        if (memcmp(raw, "SI", 2) == 0) {
                            ty = AM_SI;
                        } else if (memcmp(raw, "SK", 2) == 0) {
                            ty = AM_SK;
                        } else if (memcmp(raw, "S1", 2) == 0) {
                            ty = AM_S1;
                        } else {
                            break;
                        }
                    } else {
                        if (ty == AM_SI) {
                            r->si.si = malloc(sz + 1);
                            if (r->si.si == NULL) {
                                r->si.error = AM_ENOMEM;
                                break;
                            }
                            memcpy(r->si.si, raw, sz);
                            r->si.si[sz] = 0;
                        } else if (ty == AM_SK) {
                            r->si.sk = malloc(sz + 1);
                            if (r->si.sk == NULL) {
                                r->si.error = AM_ENOMEM;
                                break;
                            }
                            memcpy(r->si.sk, raw, sz);
                            r->si.sk[sz] = 0;
                        } else if (ty == AM_S1) {
                            r->si.s1 = malloc(sz + 1);
                            if (r->si.s1 == NULL) {
                                r->si.error = AM_ENOMEM;
                                break;
                            }
                            memcpy(r->si.s1, raw, sz);
                            r->si.s1[sz] = 0;
                        }
                    }
                    l -= sz;
                    raw += sz;
                    nv += 1;
                }
                free(c);
            }
        }
    }

    free(token);
    return AM_SUCCESS;
}

const char *get_valid_openam_url(am_request_t *r) {
    const char *val = NULL;
    int valid_idx = get_valid_url_index(r->instance_id);
    /*find active OpenAM service URL*/
    if (r->conf->naming_url_sz > 0) {
        val = valid_idx >= r->conf->naming_url_sz ?
                r->conf->naming_url[0] : r->conf->naming_url[valid_idx];
        am_log_debug(r->instance_id,
                "get_valid_openam_url(): active OpenAM service url: %s (%d)",
                val, valid_idx);
    }
    return val;
}

void xml_entity_escape(char *temp_str, size_t str_len) {
    int nshifts = 0;
    const char ec[6] = {'&', '\'', '\"', '>', '<', '\0'};
    const char * const est[] = {
        "&amp;", "&apos;", "&quot;", "&gt;", "&lt;"
    };
    size_t i, j, k, nref = 0, ecl = strlen(ec);

    for (i = 0; i < str_len; i++) {
        for (nref = 0; nref < ecl; nref++) {
            if (temp_str[i] == ec[nref]) {
                if ((nshifts = (int) strlen(est[nref]) - 1) > 0) {
                    memmove(temp_str + i + nshifts, temp_str + i, str_len - i + nshifts);
                    for (j = i, k = 0; j <= i + nshifts, k <= nshifts; j++, k++) {
                        temp_str[j] = est[nref][k];
                    }
                    str_len += nshifts;
                }
            }
        }
    }
    temp_str[str_len] = '\0';
}

static void am_timer(uint64_t *t) {
#ifdef _WIN32
    QueryPerformanceCounter((LARGE_INTEGER *) t);
#else
    struct timeval tv;
    gettimeofday(&tv, NULL);
    *t = ((uint64_t) tv.tv_sec * AM_TIMER_USEC_PER_SEC) + tv.tv_usec;
#endif
}

void am_timer_start(am_timer_t *t) {
    t = t ? t : &am_timer_s;
    t->state = AM_TIMER_ACTIVE;
#ifdef _WIN32
    t->freq = 0;
#else
    t->freq = AM_TIMER_USEC_PER_SEC;
#endif
    am_timer(&t->start);
}

void am_timer_stop(am_timer_t *t) {
    t = t ? t : &am_timer_s;
    am_timer(&t->stop);
    t->state = AM_TIMER_INACTIVE;
}

void am_timer_pause(am_timer_t *t) {
    t = t ? t : &am_timer_s;
    am_timer(&t->stop);
    t->state |= AM_TIMER_PAUSED;
}

void am_timer_resume(am_timer_t *t) {
    uint64_t now, d;
    t = t ? t : &am_timer_s;
    t->state &= ~AM_TIMER_PAUSED;
    am_timer(&now);
    d = now - t->stop;
    t->start += d;
#ifdef _WIN32
    t->freq = 0;
#else
    t->freq = AM_TIMER_USEC_PER_SEC;
#endif
}

double am_timer_elapsed(am_timer_t *t) {
    uint64_t d, s;
    t = t ? t : &am_timer_s;
#ifdef _WIN32
    QueryPerformanceFrequency((LARGE_INTEGER *) & t->freq);
#endif
    if (t->state != AM_TIMER_ACTIVE) {
        s = t->stop;
    } else {
        am_timer(&s);
    }
    d = s - t->start;
    return (double) d / (double) t->freq;
}

void am_timer_report(unsigned long instance_id, am_timer_t *t, const char *op) {
    am_log_debug(instance_id, "am_timer(): %s took %.0f seconds",
            NOTNULL(op), am_timer_elapsed(t));
}

static uint32_t int32_sub(uint32_t A, uint32_t B) {
    if (A >= B) return A - B;
    B = 0xffffffff - B + 1;
    return A + B;
}

static void rc5_key_init(am_rc5_key_t *k, unsigned char *key, size_t size) {
    uint32_t L[RC5_EXPANDED_KEYSIZE_LONG], A = 0, B = 0;
    unsigned char temp[RC5_EXPANDED_KEYSIZE], *tp = temp;
    int i, j, c = ((int) size + 3) / 4, t = 2 * (k->r + 1);
    int iterations = (c > t) ? 3 * c : 3 * t;
    char be = is_big_endian();

    /* copy the user key into the L array */
    memset(temp, 0, RC5_EXPANDED_KEYSIZE);
    memcpy(temp, key, size);
    for (i = 0; i < RC5_EXPANDED_KEYSIZE_LONG; i++) {
        if (be) {
            L[i] = (((uint32_t) tp[0] << 24) | ((uint32_t) tp[1] << 16) |
                    ((uint32_t) tp[2] << 8) | ((uint32_t) tp[3]));
        } else {
            L[i] = (((uint32_t) tp[0]) | ((uint32_t) tp[1] << 8) |
                    ((uint32_t) tp[2] << 16) | ((uint32_t) tp[3] << 24));
        }
        tp += 4;
    }

    /* initialize the key array with the LCRNG */
    k->S[0] = P32_MAGIC;
    for (i = 1; i < RC5_EXPANDED_KEYSIZE_LONG; i++)
        k->S[i] = k->S[i - 1] + Q32_MAGIC; /* overflow ignored */

    /* mix the user key into the key array */
    i = j = 0;
    while (iterations--) {
        A = k->S[i] = ROTL(MASK32(k->S[i] + A + B), 3);
        B = L[j] = ROTL(MASK32(L[j] + A + B), (A + B) & 0x1F);
        i = (i + 1) % t;
        j = (j + 1) % c;
    }

    /* clean up */
    memset(L, 0, RC5_EXPANDED_KEYSIZE_LONG * sizeof (uint32_t));
    memset(temp, 0, RC5_EXPANDED_KEYSIZE);
}

int decrypt_password(const char *key, char **password) {
    char *key_clear, *pass_clear;
    size_t key_sz, pass_sz;
    unsigned char *dp, *bp;
    uint32_t *kp, A, B;
    am_rc5_key_t rk;
    int i, j, blocks;
    char be = is_big_endian();

    if (!ISVALID(key)) return AM_SUCCESS;
    if (key == NULL || password == NULL || !ISVALID(*password)) return AM_EINVAL;

    key_sz = strlen(key);
    pass_sz = strlen(*password);
    key_clear = base64_decode(key, &key_sz);
    pass_clear = base64_decode(*password, &pass_sz);
    free(*password);
    *password = NULL;
    if (key_clear == NULL || pass_clear == NULL) return AM_ENOMEM;

    rk.r = 12;
    rc5_key_init(&rk, (unsigned char *) key_clear, MIN(key_sz, RC5_EXPANDED_KEYSIZE));
    free(key_clear);
    blocks = (int) pass_sz / 8;

    bp = (unsigned char *) pass_clear;
    for (j = 0; j < blocks; j++) {

        dp = bp;
        kp = rk.S;

        /* copy the data buffer to the local variables */
        if (be) {
            A = (((uint32_t) dp[0] << 24) | ((uint32_t) dp[1] << 16) |
                    ((uint32_t) dp[2] << 8) | ((uint32_t) dp[3]));
        } else {
            A = (((uint32_t) dp[0]) | ((uint32_t) dp[1] << 8) |
                    ((uint32_t) dp[2] << 16) | ((uint32_t) dp[3] << 24));
        }
        dp += 4;
        if (be) {
            B = (((uint32_t) dp[0] << 24) | ((uint32_t) dp[1] << 16) |
                    ((uint32_t) dp[2] << 8) | ((uint32_t) dp[3]));
        } else {
            B = (((uint32_t) dp[0]) | ((uint32_t) dp[1] << 8) |
                    ((uint32_t) dp[2] << 16) | ((uint32_t) dp[3] << 24));
        }
        dp += 4;

        /* point to the end of the keying material (no.rounds + the initial  
           addition, two longwords each time) */
        kp += 2 * (rk.r + 1);

        /* perform the 12 rounds of decryption */
        for (i = 0; i < rk.r; i++) {
            DECRYPT_ROUND(A, B, kp);
        }
        B = int32_sub(B, *--kp);
        A = int32_sub(A, *--kp);

        /* copy the local variables back to the data buffer */
        dp = bp;
        if (be) {
            dp[ 0 ] = ((A) >> 24) & 0xFF;
            dp[ 1 ] = ((A) >> 16) & 0xFF;
            dp[ 2 ] = ((A) >> 8) & 0xFF;
            dp[ 3 ] = (A) & 0xFF;
        } else {
            dp[ 0 ] = (A) & 0xFF;
            dp[ 1 ] = ((A) >> 8) & 0xFF;
            dp[ 2 ] = ((A) >> 16) & 0xFF;
            dp[ 3 ] = ((A) >> 24) & 0xFF;
        }
        dp += 4;
        if (be) {
            dp[ 0 ] = ((B) >> 24) & 0xFF;
            dp[ 1 ] = ((B) >> 16) & 0xFF;
            dp[ 2 ] = ((B) >> 8) & 0xFF;
            dp[ 3 ] = (B) & 0xFF;
        } else {
            dp[ 0 ] = (B) & 0xFF;
            dp[ 1 ] = ((B) >> 8) & 0xFF;
            dp[ 2 ] = ((B) >> 16) & 0xFF;
            dp[ 3 ] = ((B) >> 24) & 0xFF;
        }
        dp += 4;
        bp += 8;
    }

    *password = pass_clear;
    return (int) (pass_sz - pass_clear[pass_sz - 1]);
}

int encrypt_password(const char *key, char **password) {
    char *key_clear, *b64p;
    size_t key_sz, pass_sz;
    unsigned char *dp, *bp;
    uint32_t *kp, A, B;
    int i, j, blocks, pad;
    am_rc5_key_t rk;
    char be = is_big_endian();

    if (!ISVALID(key)) return AM_SUCCESS;
    if (key == NULL || password == NULL || !ISVALID(*password)) return AM_EINVAL;

    key_sz = strlen(key);
    pass_sz = strlen(*password);
    key_clear = base64_decode(key, &key_sz);
    if (key_clear == NULL) return AM_ENOMEM;

    rk.r = 12;
    rc5_key_init(&rk, (unsigned char *) key_clear, MIN(key_sz, RC5_EXPANDED_KEYSIZE));
    free(key_clear);

    /* pad the data buffer */
    pad = 8 - (pass_sz % 8);
    *password = realloc(*password, pass_sz + pad);
    if (*password == NULL) return AM_ENOMEM;
    for (j = 0; j < pad; j++) {
        (*password)[pass_sz + j] = 0; /* padding */
    }
    pass_sz += pad;
    (*password)[pass_sz - 1] = pad; /* store pad at the end for decrypt use */
    blocks = (int) pass_sz / 8;

    bp = (unsigned char *) *password;
    for (j = 0; j < blocks; j++) {

        dp = bp;
        kp = rk.S;

        /* copy the data buffer to the local variables */
        if (be) {
            A = (((uint32_t) dp[0] << 24) | ((uint32_t) dp[1] << 16) |
                    ((uint32_t) dp[2] << 8) | ((uint32_t) dp[3]));
        } else {
            A = (((uint32_t) dp[0]) | ((uint32_t) dp[1] << 8) |
                    ((uint32_t) dp[2] << 16) | ((uint32_t) dp[3] << 24));
        }
        dp += 4;
        if (be) {
            B = (((uint32_t) dp[0] << 24) | ((uint32_t) dp[1] << 16) |
                    ((uint32_t) dp[2] << 8) | ((uint32_t) dp[3]));
        } else {
            B = (((uint32_t) dp[0]) | ((uint32_t) dp[1] << 8) |
                    ((uint32_t) dp[2] << 16) | ((uint32_t) dp[3] << 24));
        }
        dp += 4;

        A = MASK32(A + *kp++);
        B = MASK32(B + *kp++);

        /* perform the 12 rounds of encryption */
        for (i = 0; i < rk.r; i++) {
            ENCRYPT_ROUND(A, B, kp);
        }

        /* copy the local variables back to the data buffer */
        dp = bp;
        if (be) {
            dp[ 0 ] = ((A) >> 24) & 0xFF;
            dp[ 1 ] = ((A) >> 16) & 0xFF;
            dp[ 2 ] = ((A) >> 8) & 0xFF;
            dp[ 3 ] = (A) & 0xFF;
        } else {
            dp[ 0 ] = (A) & 0xFF;
            dp[ 1 ] = ((A) >> 8) & 0xFF;
            dp[ 2 ] = ((A) >> 16) & 0xFF;
            dp[ 3 ] = ((A) >> 24) & 0xFF;
        }
        dp += 4;
        if (be) {
            dp[ 0 ] = ((B) >> 24) & 0xFF;
            dp[ 1 ] = ((B) >> 16) & 0xFF;
            dp[ 2 ] = ((B) >> 8) & 0xFF;
            dp[ 3 ] = (B) & 0xFF;
        } else {
            dp[ 0 ] = (B) & 0xFF;
            dp[ 1 ] = ((B) >> 8) & 0xFF;
            dp[ 2 ] = ((B) >> 16) & 0xFF;
            dp[ 3 ] = ((B) >> 24) & 0xFF;
        }
        dp += 4;
        bp += 8;
    }

    b64p = base64_encode(*password, &pass_sz);
    if (b64p == NULL) return AM_ENOMEM;
    free(*password);
    *password = b64p;
    return (int) pass_sz;
}

void am_request_free(am_request_t *r) {
    if (r != NULL) {
        if (r->normalized_url) free(r->normalized_url);
        if (r->overridden_url) free(r->overridden_url);
        if (r->token) free(r->token);
        if (r->client_ip) free(r->client_ip);
        if (r->client_host) free(r->client_host);
        if (r->post_data) free(r->post_data);
        if (r->pattr) delete_am_policy_result_list(&r->pattr);
        if (r->sattr) delete_am_namevalue_list(&r->sattr);
    }
}

int am_bin_path(char *buffer, size_t len) {
#ifdef _WIN32
    if (GetModuleFileNameA(NULL, buffer, (DWORD) len) != 0) {
        PathRemoveFileSpecA(buffer);
        strcat(buffer, FILE_PATH_SEP);
        return (int) strlen(buffer);
    }
    return AM_ERROR;
#else
    char *path_end;
#ifdef __APPLE__
    uint32_t size = len;
    if (_NSGetExecutablePath(buffer, &size) != 0) {
        return AM_ENOMEM;
    }
#else
    char path[64];
    snprintf(path, sizeof (path),
#if defined(__sun)
            "/proc/%d/path/a.out"
#elif defined(LINUX)
            "/proc/%d/exe"
#elif defined(AIX)
            "/proc/%d/cwd"
#endif
            , getpid());
    int r = readlink(path, buffer, len);
    if (r <= 0) {
        fprintf(stderr, "readlink error %d\n", errno);
        return AM_ERROR;
    }
#endif
    path_end = strrchr(buffer, '/');
    if (path_end == NULL) {
        return AM_EINVAL;
    }
    ++path_end;
    *path_end = '\0';
    return (path_end - buffer);
#endif
}



#ifdef _WIN32 

int am_delete_directory(const char *path) {
    SHFILEOPSTRUCT file_op;
    int ret, len = (int) strlen(path) + 2; /*required by SHFileOperation*/
    char *tempdir = (char *) calloc(1, len);
    if (tempdir == NULL) return AM_ENOMEM;
    strcpy(tempdir, path);

    file_op.hwnd = NULL;
    file_op.wFunc = FO_DELETE;
    file_op.pFrom = tempdir;
    file_op.pTo = NULL;
    file_op.fFlags = FOF_NOCONFIRMATION | FOF_NOERRORUI | FOF_SILENT;
    file_op.fAnyOperationsAborted = FALSE;
    file_op.hNameMappings = NULL;
    file_op.lpszProgressTitle = "";

    ret = SHFileOperation(&file_op);
    free(tempdir);
    return ret;
}
#else

static int delete_directory(const char *path, const struct stat *s, int flag, struct FTW *f) {
    int status;
    int (*rm_func)(const char *);
    switch (flag) {
        default: rm_func = unlink;
            break;
        case FTW_DP: rm_func = rmdir;
    }
    status = rm_func(path);
    return status;
}

int am_delete_directory(const char *path) {
    if (nftw(path, delete_directory, 32, FTW_DEPTH)) {
        return -1;
    }
    return 0;
}
#endif

int am_delete_file(const char *fn) {
    struct stat sb;
    if (stat(fn, &sb) == 0) {
#ifdef _WIN32 
        return am_delete_directory(fn);
#else
        if (S_ISREG(sb.st_mode) || S_ISLNK(sb.st_mode)) {
            return unlink(fn);
        } else if (S_ISDIR(sb.st_mode)) {
            return am_delete_directory(fn);
        }
#endif
    }
    return -1;
}

int am_make_path(const char *path) {
#ifdef _WIN32 
    int s = '\\';
    int nmode = _S_IREAD | _S_IWRITE;
#else
    int s = '/';
    int nmode = 0770;
#endif
    char *p = NULL;
    size_t len;
    char *tmp = strdup(path);
    if (tmp != NULL) {
        len = strlen(tmp);
        if (tmp[len - 1] == '/' || tmp[len - 1] == '\\') {
            tmp[len - 1] = 0;
        }
        for (p = tmp + 1; *p; p++) {
            if (*p == '/' || *p == '\\') {
                *p = 0;
                mkdir(tmp, nmode);
                *p = s;
            }
        }
        mkdir(tmp, nmode);
        free(tmp);
    }
    return 0;
}

#ifdef _WIN32 

static DIR *opendir(const char *dir) {
    DIR *dp;
    char *filespec;
    long handle;
    int index;

    if (dir == NULL) return NULL;
    filespec = malloc(strlen(dir) + 2 + 1);
    if (filespec == NULL) return NULL;

    strcpy(filespec, dir);
    index = (int) strlen(filespec) - 1;
    if (index >= 0 && (filespec[index] == '/' || filespec[index] == '\\')) {
        filespec[index] = '\0';
    }
    strcat(filespec, "/*");

    dp = (DIR *) malloc(sizeof (DIR));
    if (dp == NULL) {
        free(filespec);
        return NULL;
    }

    dp->offset = 0;
    dp->finished = 0;
    dp->dir = strdup(dir);
    if (dp->dir == NULL) {
        free(dp);
        free(filespec);
        return NULL;
    }

    if ((handle = _findfirst(filespec, &(dp->fileinfo))) < 0) {
        if (errno == ENOENT) {
            dp->finished = 1;
        } else {
            free(dp->dir);
            free(dp);
            free(filespec);
            return NULL;
        }
    }
    dp->handle = handle;
    free(filespec);
    return dp;
}

static int closedir(DIR *dp) {
    if (dp != NULL) {
        _findclose(dp->handle);
        if (dp->dir) {
            free(dp->dir);
        }
        free(dp);
    }
    return 0;
}

struct dirent *readdir(DIR *dp) {
    if (!dp || dp->finished)
        return NULL;

    if (dp->offset != 0) {
        if (_findnext(dp->handle, &(dp->fileinfo)) < 0) {
            dp->finished = 1;
            return NULL;
        }
    }
    dp->offset++;
    strncpy(dp->dent.d_name, dp->fileinfo.name, AM_URI_SIZE);
    dp->dent.d_type = 0;
    if (dp->fileinfo.attrib & _A_SUBDIR) {
        dp->dent.d_type = 1;
    }
    dp->dent.d_ino = 1;
    dp->dent.d_reclen = (unsigned short) strlen(dp->dent.d_name);
    dp->dent.d_off = dp->offset;
    return &(dp->dent);
}

static int readdir_r(DIR *dp, struct dirent *entry, struct dirent **result) {
    if (!dp || dp->finished) {
        *result = NULL;
        return 0;
    }

    if (dp->offset != 0) {
        if (_findnext(dp->handle, &(dp->fileinfo)) < 0) {
            dp->finished = 1;
            *result = NULL;
            return 0;
        }
    }
    dp->offset++;
    strncpy(dp->dent.d_name, dp->fileinfo.name, AM_URI_SIZE);
    dp->dent.d_type = 0;
    if (dp->fileinfo.attrib & _A_SUBDIR) {
        dp->dent.d_type = 1;
    }
    dp->dent.d_ino = 1;
    dp->dent.d_reclen = (unsigned short) strlen(dp->dent.d_name);
    dp->dent.d_off = dp->offset;
    memcpy(entry, &dp->dent, sizeof (*entry));
    *result = &dp->dent;
    return 0;
}

#endif

static int am_alphasort(const struct dirent **_a, const struct dirent **_b) {
    struct dirent **a = (struct dirent **) _a;
    struct dirent **b = (struct dirent **) _b;
    return strcoll((*a)->d_name, (*b)->d_name);
}

static int am_file_filter(const struct dirent *_a) {
    return (strncasecmp(_a->d_name, "agent_", 6) == 0);
}

static int am_scandir(const char *dirname, struct dirent ***ret_namelist,
        int (*select)(const struct dirent *),
        int (*compar)(const struct dirent **, const struct dirent **)) {
    int len, used, allocated;
    DIR *dir;
    struct dirent *ent, *ent2, *dirbuf;
    struct dirent **namelist = NULL;
    if ((dir = opendir(dirname)) == NULL) {
        return AM_EINVAL;
    }
    used = 0;
    allocated = 2;
    namelist = malloc(allocated * sizeof (struct dirent *));
    if (namelist == NULL) return AM_ENOMEM;
    dirbuf = malloc(sizeof (struct dirent) + 255 + 1);
    if (dirbuf == NULL) return AM_ENOMEM;
    while (readdir_r(dir, dirbuf, &ent) == 0 && ent) {
        if (strcmp(ent->d_name, ".") == 0 || strcmp(ent->d_name, "..") == 0)
            continue;
        if (select != NULL && !select(ent))
            continue;
        len = offsetof(struct dirent, d_name) + (int) strlen(ent->d_name) + 1;
        if ((ent2 = malloc(len)) == NULL)
            return -1;
        if (used >= allocated) {
            allocated *= 2;
            namelist = realloc(namelist, allocated * sizeof (struct dirent *));
            if (namelist == NULL) return AM_ENOMEM;
        }
        memcpy(ent2, ent, len);
        namelist[used++] = ent2;
    }
    free(dirbuf);
    closedir(dir);
    if (compar) {
        qsort(namelist, used, sizeof (struct dirent *),
                (int (*)(const void *, const void *)) compar);
    }
    *ret_namelist = namelist;
    return used;
}

int am_create_agent_dir(const char *sep, const char *path,
        char **created_name, char **created_name_simple) {
    struct dirent **instlist = NULL;
    int i, n, r = AM_ERROR, idx = 0;
    char *p0 = NULL;
    if ((n = am_scandir(path, &instlist, am_file_filter, am_alphasort)) <= 0) {

        /*report back an agent instance path and a configuration name*/
        if (created_name) am_asprintf(created_name, "%s%sagent_1", path, sep);
        if (created_name != NULL && *created_name == NULL) return AM_ENOMEM;
        if (created_name_simple) am_asprintf(created_name_simple, "agent_1");
        if (created_name_simple != NULL && *created_name_simple == NULL) return AM_ENOMEM;

        /*create directory structure*/
        if (created_name) r = am_make_path(*created_name);
        am_asprintf(&p0, "%s%sagent_1%sconfig", path, sep, sep);
        if (p0 == NULL) return AM_ENOMEM;
        r = am_make_path(p0);
        free(p0);
        p0 = NULL;
        am_asprintf(&p0, "%s%sagent_1%slogs%sdebug", path, sep, sep, sep);
        if (p0 == NULL) return AM_ENOMEM;
        r = am_make_path(p0);
        free(p0);
        p0 = NULL;
        am_asprintf(&p0, "%s%sagent_1%slogs%saudit", path, sep, sep, sep);
        if (p0 == NULL) return AM_ENOMEM;
        r = am_make_path(p0);
        free(p0);
        if (instlist != NULL) free(instlist);
        return r;

    } else {
        /*the same as above, but there is an agent_x directory already*/
        for (i = 0; i < n; i++) {
            if (i == n - 1) {
                char *id = strstr(instlist[i]->d_name, "_");
                if (id != NULL && (idx = atoi(id + 1)) > 0) {
                    if (created_name) am_asprintf(created_name, "%s%sagent_%d", path, sep, idx + 1);
                    if (created_name != NULL && *created_name == NULL) return AM_ENOMEM;
                    if (created_name_simple) am_asprintf(created_name_simple, "agent_%d", idx + 1);
                    if (created_name_simple != NULL && *created_name_simple == NULL) return AM_ENOMEM;
                    if (created_name) r = am_make_path(*created_name);
                    am_asprintf(&p0, "%s%sagent_%d%sconfig", path, sep, idx + 1, sep);
                    if (p0 == NULL) return AM_ENOMEM;
                    r = am_make_path(p0);
                    free(p0);
                    p0 = NULL;
                    am_asprintf(&p0, "%s%sagent_%d%slogs%sdebug", path, sep, idx + 1, sep, sep);
                    if (p0 == NULL) return AM_ENOMEM;
                    r = am_make_path(p0);
                    free(p0);
                    p0 = NULL;
                    am_asprintf(&p0, "%s%sagent_%d%slogs%saudit", path, sep, idx + 1, sep, sep);
                    if (p0 == NULL) return AM_ENOMEM;
                    r = am_make_path(p0);
                    free(p0);
                }
            }
            free(instlist[i]);
        }
        free(instlist);
    }
    return r;
}

int string_replace(char **original, const char *pattern, const char *replace, size_t *sz) {
    size_t pcnt = 0;
    int rv = AM_EINVAL;
    const char *optr;
    const char *ploc;
    if (original != NULL && *original != NULL && pattern != NULL &&
            replace != NULL && sz != NULL) {
        size_t rlen = strlen(replace);
        size_t plen = strlen(pattern);
        char *op = *original;
        /*count the number of patterns*/
        for (optr = op; (ploc = strstr(optr, pattern)); optr = ploc + plen) pcnt++;
        if (pcnt > 0) {
            size_t retlen = (*sz) + pcnt * (rlen + plen); /*worst case*/
            char *newop = (char *) realloc(op, retlen + 1);
            retlen = *sz;
            if (newop != NULL) {
                char *ret = newop;
                /* go through each of thw patterns, make a space (by moving) 
                 * for a replacement string, copy replacement in, 
                 * repeat until all patterns are found
                 **/
                for (optr = ret; (ploc = strstr(optr, pattern)); optr = ploc + plen) {
                    size_t move_sz = retlen - (ploc + plen - ret);
                    memmove((void *) (ploc + rlen), ploc + plen, move_sz);
                    memcpy((void *) ploc, replace, rlen);
                    retlen = (ploc - ret) + rlen + move_sz;
                }
                newop[retlen] = 0;
                *original = newop;
                *sz = retlen;
                rv = AM_SUCCESS;
            } else {
                rv = AM_ENOMEM;
            }
        } else {
            rv = AM_NOT_FOUND;
        }
    }
    return rv;
}

int copy_file(const char *from, const char *to) {
    int rv = AM_FILE_ERROR, source, dest;
    char *to_tmp = NULL;

    if (!ISVALID(from)) return AM_EINVAL;
    if (!ISVALID(to)) {
        /*'to' is not provided - do a copy of 'from' with a timestamped name*/
        char tm[64];
        struct tm now;
        time_t tv = time(NULL);
        localtime_r(&tv, &now);
        strftime(tm, sizeof (tm) - 1, "%Y%m%d%H%M%S", &now);
        am_asprintf(&to_tmp, "%s_amagent_%s", from, tm);
        if (to_tmp == NULL) return AM_ENOMEM;
    } else {
        to_tmp = (char *) to;
    }
#ifdef _WIN32
    if (CopyFileA(from, to_tmp, FALSE) != 0) {
        rv = AM_SUCCESS;
    }
#else
    struct stat st;
    source = open(from, O_RDONLY);
    if (source == -1) {
        return rv;
    }

    dest = open(to_tmp, O_CREAT | O_WRONLY | O_TRUNC, 0644);
    if (dest == -1) {
        close(source);
        return rv;
    }

    if (fstat(source, &st) == 0) {
#ifdef __APPLE__
        if (fcopyfile(source, dest, NULL, COPYFILE_ALL) != -1) {
            rv = AM_SUCCESS;
        }
#else
        off_t offset = 0;
        if (sendfile(dest, source, &offset, st.st_size) != -1) {
            rv = AM_SUCCESS;
        }
#endif
    }
    close(source);
    close(dest);
#endif
    return rv;
}

void read_directory(const char *path, struct am_namevalue **list) {
    DIR *d;
    if ((d = opendir(path)) != NULL) {
        while (1) {
            struct dirent *e;
            const char *d_name;
            e = readdir(d);
            if (!e) break;
            d_name = e->d_name;
            if (strcmp(d_name, "..") != 0 &&
                    strcmp(d_name, ".") != 0) {
                struct am_namevalue *el = calloc(1, sizeof (struct am_namevalue));
                if (el != NULL) {
                    el->ns = (e->d_type & DT_DIR) ? 1 : 0; /*record its a directory or not*/
                    am_asprintf(&el->n,
                            el->ns == 1 ? "%s/%s/" : "%s/%s",
                            path, d_name);

                    el->v = NULL;
                    el->next = NULL;
                    am_list_insert(*list, el);
                } else break;
            }
            if (e->d_type & DT_DIR) {
                if (strcmp(d_name, "..") != 0 &&
                        strcmp(d_name, ".") != 0) {
                    char npath[AM_URI_SIZE];
                    snprintf(npath, AM_URI_SIZE,
                            "%s/%s", path, d_name);
                    read_directory(npath, list);
                }
            }
        }
        closedir(d);
    } else {
        if (errno == ENOTDIR) {
            /*not a directory - add to the list as a file*/
            struct am_namevalue *el = calloc(1, sizeof (struct am_namevalue));
            if (el != NULL) {
                el->ns = 0;
                el->n = strdup(path);
                el->v = NULL;
                el->next = NULL;
                am_list_insert(*list, el);
            }
        }
    }
}
