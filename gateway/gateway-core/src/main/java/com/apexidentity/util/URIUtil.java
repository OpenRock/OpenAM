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

package com.apexidentity.util;

// Java Standard Edition
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility class for performing operations on universal resource identifiers.
 *
 * @author Paul C. Bryan
 */
public class URIUtil {

    /** Static methods only. */
    private URIUtil() {
    }

    /**
     * Returns a hierarchical URI constructed from the given components. Differs from the URI
     * constructor by accepting raw versions of userInfo, path, query and fragment components.
     *
     * @param scheme the scheme component of the URI or {@code null} if none.
     * @param rawUserInfo the raw user-information componenent of the URI or {@code null} if none.
     * @param host the host component of the URI or {@code null} if none.
     * @param port the port number of the URI or {@code -1} if none.
     * @param rawPath the raw path component of the URI or {@code null} if none.
     * @param rawQuery the raw query component of the URI or {@code null} if none.
     * @param rawFragment the raw fragment component of the URI or {@code null} if none.
     * @return the URI constructed from the given components.
     * @throws URISyntaxException if the resulting URI would be malformed per RFC 2396.
     */
    public static URI create(String scheme, String rawUserInfo, String host, int port,
    String rawPath, String rawQuery, String rawFragment) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        if (scheme != null) {
            sb.append(scheme).append(':');
        }
        if (host != null) {
            sb.append("//");
        }
        if (rawUserInfo != null) {
            sb.append(rawUserInfo).append('@');
        }
        if (host != null) {
            sb.append(host);
            if (port != -1) {
                sb.append(':').append(Integer.toString(port));
            } 
        }
        if (rawPath != null) {
            sb.append(rawPath);
        }
        if (rawQuery != null) {
            sb.append('?').append(rawQuery);
        }
        if (rawFragment != null) {
            sb.append("#").append(rawFragment);
        }
        return new URI(sb.toString());
    }

    /**
     * Changes the base scheme, host and port of a request to that specified in a base URI,
     * or leaves them unchanged if the base URI is {@code null}. This implementation only
     * uses scheme, host and port. The remaining components of the URI remain intact.
     *
     * @param uri the URI whose base is to be changed.
     * @param base the URI to base the other URI on.
     * @return the the URI with the new established base.
     * @throws URISyntaxException if the resulting URI would be malformed per RFC 2396.
     */
    public static URI rebase(URI uri, URI base) throws URISyntaxException {
        if (base == null) {
            return uri;
        }
        String scheme = base.getScheme();
        String host = base.getHost();
        int port = base.getPort();
        if (scheme == null || host == null) {
            return uri;
        }
        return create(scheme, uri.getRawUserInfo(), host, port, uri.getRawPath(),
         uri.getRawQuery(), uri.getRawFragment());
    }
}
