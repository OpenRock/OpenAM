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

#ifndef ERROR_H
#define ERROR_H

#define AM__UNKNOWN                 (-28)
#define AM__JSON_RESPONSE           (-27)
#define AM__ENOSSL                  (-26)
#define AM__NOTIFICATION_DONE       (-25)
#define AM__INVALID_AGENT_SESSION   (-24)
#define AM__INTERNAL_REDIRECT       (-23)
#define AM__EOPNOTSUPP              (-22)
#define AM__ECONNREFUSED            (-21)
#define AM__FILE_ERROR              (-20)
#define AM__RETRY_ERROR             (-19)
#define AM__XML_ERROR               (-18)
#define AM__PDP_DONE                (-17)
#define AM__INVALID_FQDN_ACCESS     (-16)
#define AM__EAGAIN                  (-15)
#define AM__EHOSTUNREACH            (-14)
#define AM__ETIMEDOUT               (-13)
#define AM__EOF                     (-12)
#define AM__E2BIG                   (-11)
#define AM__EACCES                  (-10)
#define AM__EINVAL                  (-9)
#define AM__ENOMEM                  (-8)
#define AM__EPROTO                  (-7)
#define AM__EPERM                   (-6)
#define AM__EFAULT                  (-5)
#define AM__INVALID_SESSION         (-4)
#define AM__ACCESS_DENIED           (-3)
#define AM__NOT_HANDLING            (-2)
#define AM__DONE                    (-1)
#define AM__SUCCESS                 (0)
#define AM__REDIRECT                (1)
#define AM__BAD_REQUEST             (2)
#define AM__FORBIDDEN               (3)
#define AM__NOT_FOUND               (4)
#define AM__ERROR                   (5)
#define AM__NOT_IMPLEMENTED         (6)

#define AM_ERRNO_MAP(AE) \
  AE(EINVAL, "invalid argument") \
  AE(JSON_RESPONSE, "json response") \
  AE(ENOSSL, "no ssl/library support") \
  AE(INTERNAL_REDIRECT, "internal redirect") \
  AE(EOPNOTSUPP, "operation not supported") \
  AE(ECONNREFUSED, "connection refused") \
  AE(RETRY_ERROR, "max number of retries exhausted") \
  AE(E2BIG, "argument list too long") \
  AE(XML_ERROR, "xml parser error") \
  AE(FILE_ERROR, "file parser error") \
  AE(EOF, "end of file") \
  AE(PDP_DONE, "post data preservation success") \
  AE(NOTIFICATION_DONE, "notification success") \
  AE(SUCCESS, "success") \
  AE(EAGAIN, "try again") \
  AE(ENOMEM, "not enough memory") \
  AE(EPROTO, "protocol error") \
  AE(UNKNOWN, "unknown system error") \
  AE(EPERM, "operation not permitted") \
  AE(EFAULT, "bad address") \
  AE(INVALID_SESSION, "invalid session") \
  AE(INVALID_AGENT_SESSION, "invalid agent session") \
  AE(INVALID_FQDN_ACCESS, "invalid fqdn access") \
  AE(ACCESS_DENIED, "access denied") \
  AE(REDIRECT, "redirect") \
  AE(BAD_REQUEST, "bad request") \
  AE(FORBIDDEN, "forbidden") \
  AE(NOT_FOUND, "not found") \
  AE(ERROR, "error") \
  AE(ETIMEDOUT, "operation timed out") \
  AE(EHOSTUNREACH, "no route to host") \
  AE(NOT_IMPLEMENTED, "not implemented") \
  AE(DONE, "done") \
  AE(NOT_HANDLING, "not handling") \
  AE(EACCES, "permission denied")                                             

typedef enum {
#define AE(code, _) AM_ ## code = AM__ ## code,
    AM_ERRNO_MAP(AE)
#undef AE
} am_status_t;

/**
 * Get description for the error/status code
 * 
 * @return description
 */
const char *am_strerror(int status);

#endif
