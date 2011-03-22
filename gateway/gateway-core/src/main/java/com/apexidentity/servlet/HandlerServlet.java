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

package com.apexidentity.servlet;

// Java Standard Edition
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

// Java Enterprise Edition
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// ApexIdentity Core Library
import com.apexidentity.handler.Handler;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.GenericHeaplet;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.Request;
import com.apexidentity.http.Session;
import com.apexidentity.io.BranchingInputStream;
import com.apexidentity.io.BranchingStreamWrapper;
import com.apexidentity.io.Streamer;
import com.apexidentity.io.TemporaryStorage;
import com.apexidentity.log.Logger;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ModelException;
import com.apexidentity.util.CaseInsensitiveSet;
import com.apexidentity.util.URIUtil;

/**
 * Translates between the Servlet API and the exchange object model. 
 *
 * @author Paul C. Bryan
 */
public class HandlerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Overrides request URLs constructed by container; making requests relative to a new base URI. */
    protected URI baseURI;

    /** The handler to dispatch exchanges to. */
    protected Handler handler;

    /** Allocates temporary buffers for caching streamed content during request processing. */
    protected TemporaryStorage storage;

    /** Provides methods for various logging activities. */
    protected Logger logger;

    /** Methods that should not include an entity body. */
    private static final CaseInsensitiveSet NON_ENTITY_METHODS =
     new CaseInsensitiveSet(Arrays.asList("GET", "HEAD", "TRACE", "DELETE"));

    /**
     * Handles a servlet request by dispatching it to a handler. It receives a servlet request,
     * translates it into an exchange object, dispatches the exchange to a handler, then
     * translates the exchange response into an servlet response.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LogTimer timer = logger.getTimer().start();
        Exchange exchange = new Exchange();
        // populate request
        exchange.request = new Request();
        exchange.request.method = request.getMethod();
        try {
            exchange.request.uri = URIUtil.create(request.getScheme(), null, request.getServerName(),
             request.getServerPort(), request.getRequestURI(), request.getQueryString(), null);
            if (baseURI != null) {
                exchange.request.uri = URIUtil.rebase(exchange.request.uri, baseURI);
            }
        }
        catch (URISyntaxException use) {
            throw new ServletException(use);
        }
        // request headers
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            exchange.request.headers.addAll(name, Collections.list(request.getHeaders(name)));
        }
        // include request entity if appears to be provided with request
        if ((request.getContentLength() > 0 || request.getHeader("Transfer-Encoding") != null)
        && !NON_ENTITY_METHODS.contains(exchange.request.method)) {
            exchange.request.entity = new BranchingStreamWrapper(request.getInputStream(), storage);
        }
        // remember request entity so that it (and its children) can be properly closed
        BranchingInputStream requestEntityTrunk = exchange.request.entity;
        exchange.session = new ServletSession(request);
        exchange.principal = request.getUserPrincipal();
        // handy servlet-specific attributes, sure to be abused by downstream filters
        exchange.put("javax.servlet.http.HttpServletRequest", request);
        exchange.put("javax.servlet.http.HttpServletResponse", response);
        try {
            // handle request
            try {
                handler.handle(exchange);
            }
            catch (HandlerException he) {
                throw new ServletException(he);
            }
            // response status-code (reason-phrase deprecated in Servlet API)
            response.setStatus(exchange.response.status);
            // response headers
            for (String name : exchange.response.headers.keySet()) {
                for (String value : exchange.response.headers.get(name)) {
                    if (value != null && value.length() > 0) {
                        response.addHeader(name, value);
                    }
                }
            }
            // response entity (if applicable)
            if (exchange.response.entity != null) {
                OutputStream out = response.getOutputStream();
                Streamer.stream(exchange.response.entity, out);
                out.flush();
            }
        }
        // final cleanup
        finally {
            if (requestEntityTrunk != null) {
                try {
                    requestEntityTrunk.close();
                }
                catch (IOException ioe) {
                    // ignore exception closing a stream
                }
            }
            if (exchange.response != null && exchange.response.entity != null) {
                try {
                    exchange.response.entity.close(); // important!
                }
                catch (IOException ioe) {
                    // ignore exception closing a stream
                }
            }
        }        
        timer.stop();
    }

    /** Creates and initializes a handler servlet in a heap environment. */
    public static class Heaplet extends GenericServletHeaplet {
        @Override public HttpServlet createServlet() throws HeapException, ModelException {
            HandlerServlet servlet = new HandlerServlet();
            servlet.handler = HeapUtil.getRequiredObject(heap, config.get("handler").required(), Handler.class);
            servlet.baseURI = config.get("baseURI").asURI(); // optional
            servlet.storage = this.storage;
            servlet.logger = this.logger;
            return servlet;
        }
    }
}
