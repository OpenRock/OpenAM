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
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.http.Cookie;
import com.apexidentity.http.Message;

/**
 * Processes the <strong>{@code Cookie}</strong> request message header. For more information, see the original
 * <a href="http://web.archive.org/web/20070805052634/http://wp.netscape.com/newsref/std/cookie_spec.html">Netscape specification<a>,
 * <a href="http://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a> and 
 * <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a>.
 * <p>
 * Note: This implementation is designed to be forgiving when parsing malformed cookies.
 *
 * @author Paul C. Bryan
 */
public class CookieHeader implements Header {

    /** The name of the header that this object represents. */
    public static final String NAME = "Cookie";

    /** Request message cookies. */
    public final ArrayList<Cookie> cookies = new ArrayList<Cookie>();

    /**
     * Constructs a new empty header.
     */
    public CookieHeader() {
    }

    /**
     * Constructs a new header, initialized from the specified message.
     *
     * @param message the message to initialize the header from.
     */
    public CookieHeader(Message message) {
        fromMessage(message);
    }

    /**
     * Constructs a new header, initialized from the specified string value.
     *
     * @param string the value to initialize the header from. 
     */
    public CookieHeader(String string) {
        fromString(string);
    }

    @Override
    public void clear() {
        cookies.clear();
    }

    @Override
    public String getKey() {
        return NAME;
    }

    @Override
    public void fromMessage(Message message) {
        if (message != null && message.headers != null) {
            fromString(HeaderUtil.join(message.headers.get(getKey()), ','));
        }
    }

    @Override
    public void fromString(String string) {
        clear();
        if (string != null) {
            Integer version = null;
            Cookie cookie = new Cookie();
            for (String s1 : HeaderUtil.split(string, ',')) {
                for (String s2 : HeaderUtil.split(s1, ';')) {
                    String[] nvp = HeaderUtil.parseParameter(s2);
                    if (nvp[0].length() > 0 && nvp[0].charAt(0) != '$') {
                        if (cookie.name != null) { // existing cookie was being parsed
                            cookies.add(cookie);
                        }
                        cookie = new Cookie();
                        cookie.version = version; // inherit previous parsed version
                        cookie.name = nvp[0];
                        cookie.value = nvp[1];
                    }
                    else if (nvp[0].equalsIgnoreCase("$Version")) {
                        cookie.version = version = parseInteger(nvp[1]);
                    }
                    else if (nvp[0].equalsIgnoreCase("$Path")) {
                        cookie.path = nvp[1];
                    }
                    else if (nvp[0].equalsIgnoreCase("$Domain")) {
                        cookie.domain = nvp[1];
                    }
                    else if (nvp[0].equalsIgnoreCase("$Port")) {
                        cookie.port.clear();
                        parsePorts(cookie.port, nvp[1]);
                    }
                }
            }
            if (cookie.name != null) { // last cookie being parsed
                cookies.add(cookie);
            }
        }
    }

    @Override
    public void toMessage(Message message) {
        String value = toString();
        if (value != null) {
            message.headers.put(getKey(), value);
        }
    }

    @Override
    public String toString() {
        boolean quoted = false;
        Integer version = null;
        for (Cookie cookie : cookies) {
            if (cookie.version != null && (version == null || cookie.version > version)) {
                version = cookie.version;
            }
            else if (version == null && (cookie.path != null || cookie.domain != null)) {
                version = 1; // presence of extended fields makes it version 1 at minimum
            }
        }
        StringBuilder sb = new StringBuilder();
        if (version != null) {
            sb.append("$Version=").append(version.toString());
            quoted = true;
        }
        for (Cookie cookie : cookies) {
            if (cookie.name != null) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(cookie.name).append('=');
                sb.append(quoted ? HeaderUtil.quote(cookie.value) : cookie.value);
                if (cookie.path != null) {
                    sb.append("; $Path=").append(HeaderUtil.quote(cookie.path));
                }
                if (cookie.domain != null) {
                    sb.append("; $Domain=").append(HeaderUtil.quote(cookie.domain));
                }
                if (cookie.port.size() > 0) {
                    sb.append("; $Port=").append(HeaderUtil.quote(portList(cookie.port)));
                }
            }
        }
        return (sb.length() > 0 ? sb.toString() : null); // return null if empty
    }

    @Override
    public boolean equals(Object o) {
        return (o == this || (o != null && o instanceof CookieHeader &&
         this.cookies.equals(((CookieHeader)o).cookies)));
    }

    @Override
    public int hashCode() {
        return cookies.hashCode();
    }

    private void parsePorts(List<Integer> list, String s) {
        for (String port : s.split(",")) {
            Integer p = parseInteger(port);
            if (p != null) {
                list.add(p);
            }
        }
    }

    private Integer parseInteger(String s) {
        try {
            return Integer.valueOf(s);
        }
        catch (NumberFormatException nfe) {
            return null;
        }
    }

    private String portList(List<Integer> ports) {
        StringBuilder sb = new StringBuilder();
        for (Integer port : ports) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(port.toString());
        }
        return sb.toString();
    }
}
