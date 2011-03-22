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
 * Copyright 2009 Sun Microsystems Inc. All rights reserved.
 * Portions Copyrighted © 2010–2011 ApexIdentity Inc.
 */

package com.apexidentity.http;

// Java Standard Edition
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.io.ByteArrayBranchingStream;
import com.apexidentity.io.Streamer;
import com.apexidentity.util.CaseInsensitiveMap;
import com.apexidentity.util.MultiValueMap;
import com.apexidentity.util.URIUtil;

/**
 * Form fields, a case-sensitive multi-string-valued map. The form can be read from and
 * written to request objects as query parameters (GET) and request entities (POST).
 *
 * @author Paul C. Bryan
 */
public class Form extends MultiValueMap<String, String> {

    /**
     * Constructs a new, empty form object.
     */
    public Form() {
        super(new LinkedHashMap<String, List<String>>());
    }

    /**
     * Parses a URL-encoded string containing form parameters and stores them in this object.
     * Malformed name-value pairs (missing the "=" delimiter) are simply ignored.
     *
     * @param s the URL-encoded string to parse.
     */
    public Form fromString(String s) {
        for (String param : s.split("&")) {
            String[] nv = param.split("=", 2);
            if (nv.length == 2) {
                try {
                    add(URLDecoder.decode(nv[0], "UTF-8"), URLDecoder.decode(nv[1], "UTF-8"));
                }
                catch (UnsupportedEncodingException uee) {
                    throw new IllegalStateException(uee);
                }
            }
        }
        return this;
    }

    /**
     * Returns this form in a URL-encoded format string.
     *
     * @return the URL-encoded form.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : keySet()) {
            for (String value: get(name)) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                try {
                    sb.append(URLEncoder.encode(name, "UTF-8")).append('=').append(URLEncoder.encode(value, "UTF-8"));
                }
                catch (UnsupportedEncodingException uee) {
                    throw new IllegalStateException(uee);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parses the query parameters of a request and stores them in this object. The object
     * is not cleared beforehand, so this adds to any fields already in place.
     *
     * @param request the request to be parsed.
     * @return this form object.
     */
    public Form fromRequestQuery(Request request) {
        String query = request.uri.getRawQuery();
        if (query != null) {
            fromString(query);
        }
        return this;
    }

    /**
     * Sets a request URI with query parameters. This overwrites any query parameters that
     * may already exist in the request URI.
     *
     * @param request the request to set query parameters to.
     */
    public void toRequestQuery(Request request) {
        try {
            request.uri = URIUtil.create(request.uri.getScheme(), request.uri.getRawUserInfo(),
             request.uri.getHost(), request.uri.getPort(), request.uri.getRawPath(),
             toString(), request.uri.getRawFragment());
        }
        catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }

    /**
     * Appends the form as additional query parameters on an existing request URI. This leaves
     * any existing query parameters intact.
     *
     * @param request the request to append query parameters to.
     */
    public void appendRequestQuery(Request request) {
        StringBuilder sb = new StringBuilder();
        String uriQuery = request.uri.getRawQuery();
        if (uriQuery != null && uriQuery.length() > 0) {
            sb.append(uriQuery);
        }
        String toAppend = toString();
        if (toAppend != null && toAppend.length() > 0) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(toAppend);
        }
        String newQuery = sb.toString();
        if (newQuery.length() == 0) {
            newQuery = null;
        }
        try {
            request.uri = URIUtil.create(request.uri.getScheme(), request.uri.getRawUserInfo(),
             request.uri.getHost(), request.uri.getPort(), request.uri.getRawPath(),
             newQuery, request.uri.getRawFragment());
        }
        catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }

    /**
     * Parses the URL-encoded form entity of a request and stores them in this object. The
     * object is not cleared beforehand, so this adds to any fields already in place.
     *
     * @param request the request to be parsed.
     * @return this form object.
     * @throws IOException if an I/O exception occurs.
     */
    public Form fromRequestEntity(Request request) throws IOException {
        if (request != null & request.entity != null && request.headers != null
        && request.headers.getFirst("Content-Type").equalsIgnoreCase("application/x-www-form-urlencoded")) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Streamer.stream(request.entity, bytes);
            fromString(bytes.toString());
        }
        return this;
    }

    /**
     * Populates a request with the necessary headers and entity for the form
     * to be submitted as a POST with application/x-www-form-urlencoded content
     * type. This overwrites any entity that may already be in the request.
     *
     * @param request the request to add the form entity to.
     */
    public void toRequestEntity(Request request) {
        String form = toString();
        request.method = "POST";
        request.headers.put("Content-Type", "application/x-www-form-urlencoded");
        request.headers.put("Content-Length", Integer.toString(form.length()));
        request.entity = new ByteArrayBranchingStream(form.getBytes());
    }
}
