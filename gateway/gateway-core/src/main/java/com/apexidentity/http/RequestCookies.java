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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// ApexIdentity Core Library
import com.apexidentity.header.CookieHeader;
import com.apexidentity.util.CaseInsensitiveSet;
import com.apexidentity.util.EnumerableMap;

/**
 * Exposes incoming request cookies.
 *
 * @author Paul C. Bryan
 */
public class RequestCookies implements EnumerableMap<String, List<Cookie>> {

    /** The request to read cookies from. */
    private Request request;

    /**
     * Constructs a new request cookies object that reads cookies from the specified request.
     *
     * @param request the request to read cookies from.
     */
    public RequestCookies(Request request) {
        this.request = request;
    }

    @Override
    public List<Cookie> get(Object key) {
// TODO: maybe some intelligent caching so each call to get doesn't re-parse the cookies
        ArrayList<Cookie> list = new ArrayList<Cookie>();
        if (key instanceof String) {
            String s = (String)key;
            for (Cookie cookie : new CookieHeader(request).cookies) {
                if (s.equalsIgnoreCase(cookie.name)) {
                    list.add(cookie);
                }
            }
        }
        return (list.size() > 0 ? list : null);
    }

    @Override
    public boolean containsKey(Object key) {
        return (keySet().contains(key));
    }

    @Override
    public int size() {
        return (keySet().size());
    }

    @Override
    public Set<String> keySet() {
// TODO: maybe some intelligent caching so each call to get doesn't re-parse the cookies
        CaseInsensitiveSet keys = new CaseInsensitiveSet();
        for (Cookie cookie : new CookieHeader(request).cookies) {
            keys.add(cookie.name);
        }
        return keys;
    }
}
