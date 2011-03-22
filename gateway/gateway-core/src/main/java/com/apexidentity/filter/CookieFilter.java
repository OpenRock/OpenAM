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

package com.apexidentity.filter;

// Java Standard Edition
import java.io.IOException;
import java.net.CookiePolicy;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

// ApexIdentity Core Library
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.Request;
import com.apexidentity.http.Response;
import com.apexidentity.http.Session;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ModelException;
import com.apexidentity.util.CaseInsensitiveSet;
import com.apexidentity.util.StringUtil;

/**
 * Suppresses, relays and manages cookies. The names of filtered cookies are stored in one of
 * three action set variables: {@code suppressed}, {@code relayed} and {@code managed}. If a
 * cookie is not found in any of the action sets, then a default action is selected.
 * <p>
 * The default action is controlled by setting the {@code defaultAction} field. The default
 * action at initialization is to manage all cookies. In the event a cookie appears in more
 * than one action set, then it will be selected in order of precedence: managed, suppressed,
 * relayed.
 * <p>
 * Managed cookies are intercepted by the cookie filter itself and stored in the request
 * {@link Session} object. The default {@code policy} is to accept all incoming cookies, but
 * can be changed to others as appropriate.
 *
 * @author Paul C. Bryan
 */
public class CookieFilter extends GenericFilter {

    /** Action to be performed for a cookie. */
    public enum Action {
        /** Intercept and manage the cookie within the proxy. */ MANAGE,
        /** Remove the cookie from request and response. */ SUPPRESS,
        /** Relay the cookie between remote client and remote host. */ RELAY
    }

// TODO: Use the com.apexidentity.header framework now for parsing, not regexes anymore.

    /** Splits string using comma delimiter, outside of quotes. */
    private static final Pattern DELIM_COMMA = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");
    
    /** Splits string using equals sign delimiter, outside of quotes. */
    private static final Pattern DELIM_EQUALS = Pattern.compile("=(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");

    /** Splits string using semicolon delimiter, outside of quotes. */
    private static final Pattern DELIM_SEMICOLON = Pattern.compile(";(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");

    /** Splits string using colon delimiter. */
    private static final Pattern DELIM_COLON = Pattern.compile(":");

    /** Response headers to parse. */
    private static final String[] RESPONSE_HEADERS = { "Set-Cookie", "Set-Cookie2" };

    /** Action to perform for cookies that do not match an action set. Default: manage. */
    public Action defaultAction = Action.MANAGE;

    /** The policy for managed cookies. Default: accept all cookies. */
    public CookiePolicy policy = CookiePolicy.ACCEPT_ALL;

    /** Action set for cookies to be suppressed. */
    public final CaseInsensitiveSet suppressed = new CaseInsensitiveSet();

    /** Action set for cookies to be relayed. */
    public final CaseInsensitiveSet relayed = new CaseInsensitiveSet();
    
    /** Action set for cookies that filter should intercept and manage. */
    public final CaseInsensitiveSet managed = new CaseInsensitiveSet();

    /**
     * Resolves the request URI based on the request URI variable and optional
     * Host header. This allows the request URI to contain a raw IP address,
     * while the Host header resolves the hostname and port that the remote
     * client used to access it.
     * <p>
     * Note: This method returns a normalized URI, as though returned by the
     * {@link URI#normalize} method.
     *
     * @return the resolved URI value.
     */
// TODO: Rewrite and put in URIutil.
    private URI resolveHostURI(Request request) {
        URI uri = request.uri;
        String header = (request.headers != null ? request.headers.getFirst("Host") : null);
        if (uri != null && header != null) {
            String[] hostport = DELIM_COLON.split(header, 2);
            int port;
            try {
                port = (hostport.length == 2 ? Integer.parseInt(hostport[1]) : -1);
            }
            catch (NumberFormatException nfe) {
                port = -1;
            }
            try {
                uri = new URI(uri.getScheme(), null, hostport[0], port, "/", null, null).resolve(
                new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), null, null, null).relativize(uri));
            }
            catch (URISyntaxException use) {
            }
        }
        return uri;
    }

    /**
     * Sets all request cookies (existing in request plus those to add from cookie jar) in
     * a single "Cookie" header in the request.
     */
    private void addRequestCookies(CookieManager manager, URI resolved, Request request) throws IOException {
        List<String> cookies = request.headers.get("Cookie");
        if (cookies == null) {
            cookies = new ArrayList<String>();
        }
        List<String> managed = manager.get(resolved, request.headers).get("Cookie");
        if (managed != null) {
            cookies.addAll(managed);
        }
        StringBuilder sb = new StringBuilder();
        for (String cookie : cookies) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(cookie);
        }
        if (sb.length() > 0) {
            request.headers.put("Cookie", sb.toString()); // replace any existing header(s)
        }
    }

    /**
     * Filters the request and/or response of an exchange by suppressing, relayign and
     * managing cookies.
     */
    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        URI resolved = resolveHostURI(exchange.request); // resolve to client-supplied host header
        CookieManager manager = getManager(exchange.session); // session cookie jar
        suppress(exchange.request); // remove cookies that are suppressed or managed
        addRequestCookies(manager, resolved, exchange.request); // add any request cookies to header
        chain.handle(exchange); // pass exchange to next handler in chain
        manager.put(resolved, exchange.response.headers); // manage cookie headers in response
        suppress(exchange.response); // remove cookies that are suppressed or managed
        timer.stop();
    }

    /**
     * Computes what action to perform for the specified cookie name.
     *
     * @param name the name of the cookie to compute action for.
     * @return the computed action to perform for the given cookie.
     */
    private Action action(String name) {
        if (managed.contains(name)) {
            return Action.MANAGE;
        }
        else if (suppressed.contains(name)) {
            return Action.SUPPRESS;
        }
        else if (relayed.contains(name)) {
            return Action.RELAY;
        }
        else {
            return defaultAction;
        }
    }

    /**
     * Returns the cookie manager for the session, creating one if it does not already exist.
     *
     * @param session the session that contains the cookie manager.
     * @return the retrieved (or created) cookie manager.
     */
    private CookieManager getManager(Session session) {
        CookieManager manager = null;
        synchronized(session) { // prevent a race for the cookie manager
            manager = (CookieManager)session.get(CookieManager.class.getName());
            if (manager == null) {
                manager = new CookieManager(null, new CookiePolicy() {
                    public boolean shouldAccept(URI uri, HttpCookie cookie) {
                        return (action(cookie.getName()) == Action.MANAGE && policy.shouldAccept(uri, cookie));
                    }
                });
                session.put(CookieManager.class.getName(), manager);
            }
        }
        return manager;
    }

    /**
     * Removes the cookies from the request that are suppressed or managed.
     *
     * @param request the request to suppress the cookies in.
     */
    private void suppress(Request request) {
        List<String> headers = request.headers.get("Cookie");
        if (headers != null) {
            for (ListIterator<String> hi = headers.listIterator(); hi.hasNext();) {
                String header = hi.next();
                ArrayList<String> parts = new ArrayList<String>(Arrays.asList(DELIM_SEMICOLON.split(header, 0)));
                int originalSize = parts.size();
                boolean remove = false;
                int intact = 0;
                for (ListIterator<String> pi = parts.listIterator(); pi.hasNext();) {
                    String part = pi.next().trim();
                    if (part.length() != 0 && part.charAt(0) == '$') {
                        if (remove) {
                            pi.remove();
                        }
                    }
                    else {
                        Action action = action((DELIM_EQUALS.split(part, 2))[0].trim());
                        if (action == Action.SUPPRESS || action == Action.MANAGE) {
                            pi.remove();
                            remove = true;
                        }
                        else {
                            intact++;
                            remove = false;
                        }
                    }
                }
                if (intact == 0) {
                    hi.remove();
                }
                else if (parts.size() != originalSize) {
                    hi.set(StringUtil.join(";", parts));
                }
            }
        }
    }

    /**
     * Removes the cookies from the response that are suppressed or managed.
     *
     * @param response the response to suppress the cookies in.
     */
    private void suppress(Response response) {
        for (String name : RESPONSE_HEADERS) {
            List<String> headers = response.headers.get(name);
            if (headers != null) {
                for (ListIterator<String> hi = headers.listIterator(); hi.hasNext();) {
                    String header = hi.next();
                    ArrayList<String> parts;
                    if (name.equals("Set-Cookie2")) {  // RFC 2965 cookie
                        parts = new ArrayList<String>(Arrays.asList(DELIM_COMMA.split(header, 0)));
                    }
                    else { // Netscape cookie
                        parts = new ArrayList<String>();
                        parts.add(header);
                    }
                    int originalSize = parts.size();
                    for (ListIterator<String> pi = parts.listIterator(); pi.hasNext();) {
                        String part = pi.next();
                        Action action = action((DELIM_EQUALS.split(part, 2))[0].trim());
                        if (action == Action.SUPPRESS || action == Action.MANAGE) {
                            pi.remove();
                        }
                    }
                    if (parts.size() == 0) {
                        hi.remove();
                    }
                    else if (parts.size() != originalSize) {
                        hi.set(StringUtil.join(",", parts));
                    }
                }
            }
        }
    }

    /** Creates and initializes a cookie filter in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            CookieFilter filter = new CookieFilter();
            filter.suppressed.addAll(config.get("suppressed").defaultTo(Collections.emptyList()).asList(String.class));
            filter.relayed.addAll(config.get("relayed").defaultTo(Collections.emptyList()).asList(String.class));
            filter.managed.addAll(config.get("managed").defaultTo(Collections.emptyList()).asList(String.class));
            filter.defaultAction = config.get("defaultAction").defaultTo(filter.defaultAction.toString()).asEnum(Action.class);
            return filter;
        }
    }
}
