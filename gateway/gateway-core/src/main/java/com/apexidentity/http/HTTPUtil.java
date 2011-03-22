/*
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
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2010–2011 ApexIdentity Inc. All rights reserved.
 */

package com.apexidentity.http;

// Java Standard Edition
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

// ApexIdentity Core Library
import com.apexidentity.header.ContentEncodingHeader;
import com.apexidentity.header.ContentTypeHeader;
import com.apexidentity.http.Message;
import com.apexidentity.io.ByteArrayBranchingStream;
import com.apexidentity.io.NullInputStream;

/**
 * Utility class for processing HTTP messages.
 *
 * @author Paul C. Bryan
 */
public class HTTPUtil {

    /** Default character set to use if not specified, per RFC 2616. */
    private static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /** Static methods only. */
    private HTTPUtil() {
    }

    /**
     * Sets the entity in the message to contain the content of the string with the specified
     * character set. Also sets the {@code Content-Length} header, overwriting any existing
     * 
     * <p>
     * If {@code charset} is not {@code null} then it will be used to encode the entity,
     * else the character set specified in the message's {@code Content-Type} header (if
     * present) will be used, otherwise the default {@code ISO-8859-1} character set.
     * <p>
     * Note: This method replaces the entity without closing any previous one. The caller is
     * responsible for first closing any existing entity. This method also does not attempt to
     * encode the entity based-on any codings specified in the {@code Content-Encoding} header. 
     *
     * @param message the message whose entity is to be set with the string value.
     * @param string the string whose value is to be set as the message entity.
     * @param charset the character set to encode the string in, or ISO-8859-1 if {@code null}.
     */
    public static void toEntity(Message message, String string, Charset charset) {
        byte[] data = string.getBytes(cs(message, charset));
        message.entity = new ByteArrayBranchingStream(data);
        message.headers.put("Content-Length", Integer.toString(data.length));
    }

    /**
     * Returns a new reader that decodes the entity of a message.
     * <p>
     * The entity will be decoded and/or decompressed based-on any codings that are specified
     * in the {@code Content-Encoding} header.
     * <p>
     * If {@code charset} is not {@code null} then it will be used to decode the entity,
     * else the character set specified in the message's {@code Content-Type} header (if
     * present) will be used, otherwise the default {@code ISO-8859-1} character set.
     * <p>
     * Note: The caller is responsible for calling the reader's {@code close} method when it
     * is finished reading the entity.
     *
     * @param message the message whose entity is to be decoded.
     * @param branch if the entity should be branched before reading.
     * @param charset the character set to decode with, or message-specified or default if {@code null}.
     * @return a buffered reader for reading the decoded entity.
     * @throws IOException if an I/O exception occurs.
     * @throws UnsupportedEncodingException if content encoding or charset are not supported.
     */
    public static BufferedReader entityReader(Message message, boolean branch, Charset charset)
    throws IOException, UnsupportedEncodingException {
        InputStream in;
        if (message == null || message.entity == null) {
            in = new NullInputStream();
        }
        else if (branch) {
            in = message.entity.branch();
        }
        else {
            in = message.entity;
        }
        // wrap entity with decoders for codings in Content-Encoding header
        in = new ContentEncodingHeader(message).decode(in);
        return new BufferedReader(new InputStreamReader(in, cs(message, charset)));
    }

    /**
     * Returns the example reason phrase for the corresponding status code, per
     * RFC 2616 §6.1.1. If the status code is unrecognized, then {@code null} is returned.
     *
     * @param status the status code from which to derive the reason phrase.
     * @return the reason phrase corresponding to the specified status code.
     */
    public static String getReason(int status) {
        switch (status) {
            case 100: return "Continue";
            case 101: return "Switching Protocols";
            case 200: return "OK";
            case 201: return "Created";
            case 202: return "Accepted";
            case 203: return "Non-Authoritative Information";
            case 204: return "No Content";
            case 205: return "Reset Content";
            case 206: return "Partial Content";
            case 300: return "Multiple Choices";
            case 301: return "Moved Permanently";
            case 302: return "Found";
            case 303: return "See Other";
            case 304: return "Not Modified";
            case 305: return "Use Proxy";
            case 307: return "Temporary Redirect";
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 402: return "Payment Required";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 406: return "Not Acceptable";
            case 407: return "Proxy Authentication Required";
            case 408: return "Request Time-out";
            case 409: return "Conflict";
            case 410: return "Gone";
            case 411: return "Length Required";
            case 412: return "Precondition Failed";
            case 413: return "Request Entity Too Large";
            case 414: return "Request-URI Too Large";
            case 415: return "Unsupported Media Type";
            case 416: return "Requested range not satisfiable";
            case 417: return "Expectation Failed";
            case 500: return "Internal Server Error";
            case 501: return "Not Implemented";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Time-out";
            case 505: return "HTTP Version not supported";
        }
        return null; // not specified per RFC 2616
    }

    private static Charset cs(Message message, Charset charset) {
        if (charset == null) { // use Content-Type charset if not explicitly specified
            charset = new ContentTypeHeader(message).getCharset();
        }
        if (charset == null) { // use default per RFC 2616 if not resolved
            charset = ISO_8859_1;
        }
        return charset;
    }
}
