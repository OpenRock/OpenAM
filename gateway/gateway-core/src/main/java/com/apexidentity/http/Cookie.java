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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.util.FieldMap;

/**
 * An HTTP cookie. For more information, see the original
 * <a href="http://web.archive.org/web/20070805052634/http://wp.netscape.com/newsref/std/cookie_spec.html">Netscape specification<a>,
 * <a href="http://www.ietf.org/rfc/rfc2109.txt">RFC 2109</a> and 
 * <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a>.
 *
 * @author Paul C. Bryan
 */
public class Cookie extends FieldMap {

    /** The name of the cookie. */
    public String name;

    /** The value of the cookie. */
    public String value;

    /** The intended use of a cookie. */
    public String comment;

    /** URL identifying the intended use of a cookie. */
    public String commentURL;

    /** Directs the user agent to discard the cookie unconditionally when it terminates. */
    public Boolean discard;

    /** The domain for which the cookie is valid. */
    public String domain;

    /** The lifetime of the cookie, expressed as the date and time of expiration. */
    public Date expires;

    /** The lifetime of the cookie, expressed in seconds. */
    public Integer maxAge;

    /** The subset of URLs on the origin server to which this cookie applies. */
    public String path;

    /** Restricts the port(s) to which a cookie may be returned. */
    public final List<Integer> port = new ArrayList<Integer>();

    /** Directs the user agent to use only secure means to send back this cookie. */
    public Boolean secure;

    /** The version of the state management mechanism to which this cookie conforms. */
    public Integer version;

    /** Directs the user agent to make the cookie inaccessible to client side script. */
    public Boolean httpOnly;
}
