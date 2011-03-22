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

package com.apexidentity.header;

// Java Standard Edition
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.http.Message;

/**
 * Processes the <strong>{@code Content-Type}</strong> message header. For more information,
 * see <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a> §14.17.
 *
 * @author Paul C. Bryan
 */
public class ContentTypeHeader implements Header {

    /** The name of the header that this object represents. */
    public static final String NAME = "Content-Type"; 

    /** The type/subtype of the message. */
    public String type = null;

    /** The character set used in encoding the message. */
    public String charset = null;

    /**
     * Constructs a new empty header.
     */
    public ContentTypeHeader() {
    }

    /**
     * Constructs a new header, initialized from the specified message.
     *
     * @param message the message to initialize the header from.
     */
    public ContentTypeHeader(Message message) {
        fromMessage(message);
    }

    /**
     * Constructs a new header, initialized from the specified string value.
     *
     * @param string the value to initialize the header from. 
     */
    public ContentTypeHeader(String string) {
        fromString(string);
    }

    /**
     * Returns the character set encoding used to encode the message, or {@code null} if no
     * character set was specified.
     *
     * @throws IllegalCharsetNameException if the given charset name is illegal.
     * @throws UnsupportedCharsetException if no support for the named charset is available.
     */
    public Charset getCharset() throws IllegalCharsetNameException, UnsupportedCharsetException {
        return (charset != null ? Charset.forName(charset) : null);
    }

    @Override
    public void clear() {
        type = null;
        charset = null;
    }

    @Override
    public String getKey() {
        return NAME;
    }

    @Override
    public void fromMessage(Message message) throws IllegalCharsetNameException, UnsupportedCharsetException {
        if (message != null && message.headers != null) {
            fromString(message.headers.getFirst(NAME));
        }
    }

    @Override
    public void fromString(String string) {
        clear();
        List<String> parts = HeaderUtil.split(string, ';');
        if (parts.size() > 0) {
            this.type = parts.get(0);
            this.charset = HeaderUtil.parseParameters(parts).get("charset");
        }
    }

    @Override
    public void toMessage(Message message) {
        String value = toString();
        if (value != null) {
            message.headers.put(NAME, value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type != null) {
            sb.append(type);
            if (charset != null) {
                sb.append("; charset=").append(charset);
            }
        }
        return (sb.length() > 0 ? sb.toString() : null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ContentTypeHeader)) {
            return false;
        }
        ContentTypeHeader ct = (ContentTypeHeader)o;
        return (((this.type == null && ct.type == null) || (this.type != null && this.type.equals(ct.type))) &&
         ((this.charset == null && ct.charset == null) || (this.charset != null && this.charset.equals(ct.charset))));
    }

    @Override
    public int hashCode() {
        return ((type == null ? 0 : type.hashCode()) ^
         (charset == null ? 0 : charset.hashCode()));
    }
}
