/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the specific
 * language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright 2009 Sun Microsystems Inc. All rights reserved.
 * Portions Copyrighted © 2010–2011 ApexIdentity Inc.
 */

// TODO: distinguish between basic and other schemes that use 401 (Digest, OAuth, ...)

package com.apexidentity.filter;

// Java Standard Edition
import java.io.IOException;
import java.util.Arrays;

// Apache Commons Codec
import org.apache.commons.codec.binary.Base64;

// ApexIdentity Core Library
import com.apexidentity.el.Expression;
import com.apexidentity.handler.Handler;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.Request;
import com.apexidentity.http.Response;
import com.apexidentity.io.BranchingInputStream;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ModelException;
import com.apexidentity.util.CaseInsensitiveSet;

/**
 * Performs authentication through the HTTP Basic authentication scheme. For more information,
 * see <a href="http://www.ietf.org/rfc/rfc2617.txt">RFC 2617</a>.
 * <p>
 * If challenged for authentication via a {@code 401 Unauthorized} status code by the server,
 * this filter will retry the request with credentials attached. Therefore, the request entity
 * will be branched and stored for the duration of the exchange.
 * <p>
 * Once an HTTP authentication challenge (status code 401) is issued from the remote server,
 * all subsequent requests to that remote server that pass through the filter will include the
 * user credentials.
 * <p>
 * If authentication fails (including the case of no credentials yielded from the
 * {@code username} or {@code password} expressions, then the exchange is diverted to the
 * authentication failure handler.
 *
 * @author Paul C. Bryan
 */
public class HTTPBasicAuthFilter extends GenericFilter {

    /** Headers that are suppressed from incoming request. */
    private static final CaseInsensitiveSet SUPPRESS_REQUEST_HEADERS =
     new CaseInsensitiveSet(Arrays.asList("Authorization"));

    /** Headers that are suppressed for outgoing response. */
    private static final CaseInsensitiveSet SUPPRESS_RESPONSE_HEADERS =
     new CaseInsensitiveSet(Arrays.asList("WWW-Authenticate"));

    /** Expression that yields the username to supply during authentication. */
    public Expression username;

    /** Expression that yields the password to supply during authentication. */
    public Expression password;

    /** Handler dispatch to if authentication fails. */
    public Handler failureHandler;

    /**
     * Resolves a session attribute name for the remote server specified in the specified
     * request.
     *
     * @param name the name of the attribute to resolve.
     * @return the session attribute name, fully qualified the request remote server.
     */
    private String attributeName(Request request) {
        return this.getClass().getName() + ':' + request.uri.getScheme() + ':' + 
         request.uri.getHost() + ':' + request.uri.getPort() + ':' + "userpass";
    }

    /**
     * Handles the message exchange by authenticating via HTTP basic scheme. Credentials are
     * cached in the session to allow subsequent requests to automatically include
     * authentication credentials.
     */
    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        exchange.request.headers.remove(SUPPRESS_REQUEST_HEADERS);
        BranchingInputStream trunk = null;
        if (exchange.request.entity != null) {
            trunk = exchange.request.entity;
        }
        // loop to retry for intitially retrieved (or refreshed) credentials
        for (int n = 0; n < 2; n++) {
            // put a branch of the trunk in the entity to allow retries
            exchange.request.entity = trunk.branch();
            // because credentials are sent in every request, this class caches them in the session
            String userpass = (String)exchange.session.get(attributeName(exchange.request));
            if (userpass != null) {
                exchange.request.headers.add("Authorization", "Basic " + userpass);
            }
            chain.handle(exchange);
            // successful exchange from this filter's standpoint
            if (exchange.response.status != 401) {
                exchange.response.headers.remove(SUPPRESS_RESPONSE_HEADERS);
                timer.stop();
                return;
            }
            // credentials might be stale, so fetch them
            String user = username.eval(exchange, String.class);
            String pass = password.eval(exchange, String.class);
            // no credentials is equivalent to invalid credentials
            if (user == null || pass == null) {
                break;
            }
            // ensure conformance with specification
            if (user.indexOf(':') > 0) {
                throw new HandlerException("username must not contain a colon ':' character");
            }
            // set in session for fetch in next iteration of this loop
            exchange.session.put(attributeName(exchange.request),
             new Base64(0).encodeToString((user + ":" + pass).getBytes()));
        }
        // close the incoming response because it's about to be dereferenced
        if (exchange.response.entity != null) {
            exchange.response.entity.close(); // important!
        }
        // credentials were missing or invalid; let invalid credentials handler deal with it
        exchange.response = new Response();
        failureHandler.handle(exchange);
        timer.stop();
    }

    /** Creates and initializes an HTTP basic authentication filter in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            HTTPBasicAuthFilter filter = new HTTPBasicAuthFilter();
            filter.username = config.get("username").required().asExpression();
            filter.password = config.get("password").required().asExpression();
            filter.failureHandler = HeapUtil.getObject(heap, config.get("failureHandler"), Handler.class); // optional
            return filter;
        }
    }
}
