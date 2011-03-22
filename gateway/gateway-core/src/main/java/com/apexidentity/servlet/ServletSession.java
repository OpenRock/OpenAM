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

package com.apexidentity.servlet;

// Java Enterprise Edition
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

// ApexIdentity Core Library
import com.apexidentity.http.Session;

/**
 * Exposes the session managed by the servlet container as an exchange session. This
 * implementation will get a servlet session if already allocated, otherwise will not create
 * one until an attempt is made to put an attribute in it.
 *
 * @author Paul C. Bryan
 */
public class ServletSession implements Session {

    /** The servlet request from which to get a servlet session object. */
    private HttpServletRequest request;

    /** The servlet session object, if available. */
    private HttpSession httpSession;

    /**
     * Creates a new session object which manages sessions through the provided servlet
     * request object.
     *
     * @param request the servlet request object through which servlet sessions are managed.
     */
    public ServletSession(HttpServletRequest request) {
        this.request = request;
        this.httpSession = request.getSession(false); // get session if already allocated
    }

    @Override
    public Object get(Object key) {
        Object value = null;
        if (key instanceof String && httpSession != null) {
            value = httpSession.getAttribute((String)key);
        }
        return value;
    }

    @Override
    public synchronized Object put(String key, Object value) {
        Object old = get(key);
        if (httpSession == null) {
            httpSession = request.getSession(true); // create session just-in-time
        }
        httpSession.setAttribute(key, value);
        return old;
    }

    @Override
    public Object remove(Object key) {
        Object old = get(key);
        if (key instanceof String && httpSession != null) {
            httpSession.removeAttribute((String)key);
        }
        return old;
    }
}
