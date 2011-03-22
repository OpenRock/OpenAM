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
import java.util.ArrayList;

// ApexIdentity Core Library
import com.apexidentity.http.Message;

/**
 * Processes the <strong>{@code Connection}</strong> message header. For more information, see
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a> §14.10.
 *
 * @author Paul C. Bryan
 */
public class ConnectionHeader implements Header {

    /** The name of the header that this object represents. */
    public static final String NAME = "Connection";

    /** A list of connection-tokens. */
    public final ArrayList<String> tokens = new ArrayList<String>();

    /**
     * Constructs a new empty header.
     */
    public ConnectionHeader() {
    }

    /**
     * Constructs a new header, initialized from the specified message.
     *
     * @param message the message to initialize the header from.
     */
    public ConnectionHeader(Message message) {
        fromMessage(message);
    }

    /**
     * Constructs a new header, initialized from the specified string value.
     *
     * @param string the value to initialize the header from. 
     */
    public ConnectionHeader(String string) {
        fromString(string);
    }

    @Override
    public void clear() {
        tokens.clear();
    }

    @Override
    public String getKey() {
        return NAME;
    }

    @Override
    public void fromMessage(Message message) {
        if (message != null && message.headers != null) {
            fromString(HeaderUtil.join(message.headers.get(NAME), ','));
        }
    }

    @Override
    public void fromString(String string) {
        clear();
        if (string != null) {
            tokens.addAll(HeaderUtil.split(string, ','));
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
        return HeaderUtil.join(tokens, ','); // will return null if empty
    }

    @Override
    public boolean equals(Object o) {
        return (o == this || (o != null && o instanceof ConnectionHeader &&
         this.tokens.equals(((ConnectionHeader)o).tokens)));
    }

    @Override
    public int hashCode() {
        return tokens.hashCode();
    }
}
