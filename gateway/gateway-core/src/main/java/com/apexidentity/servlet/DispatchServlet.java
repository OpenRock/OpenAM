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

// Java Standard Edition
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// java Enterprise Edition
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

// ApexIdentity Core Library
import com.apexidentity.heap.GenericHeaplet;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.log.Logger;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.NodeException;
import com.apexidentity.model.ValueNode;

/**
 * Dispatches requests to mapped filters and servlets based on request extra path information.
 * The extra path information is the path that follows the path of the dispatch servlet itself,
 * but precedes the query string. It is guaranteed to be a value that always begins with a
 * {@code "/"} character.
 * <p>
 * All filters that match the pattern will be invoked in the order they are expressed in the
 * bindings list until a matching servlet is encountered. The first matching servlet object in
 * the bindings list will be invoked, and terminates any further processing of the request. If
 * no matching servlet is found, a {@link ServletException} is thrown. To avoid this, a final
 * "catch-all" servlet binding with a pattern of {@code ".*"} is recommended.
 *
 * @author Paul C. Bryan
 */
public class DispatchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Regular expression patterns to match against request path information, bound to filters and/or servlets to dispatch to. */
    public final List<Binding> bindings = new ArrayList<Binding>();

    /** Provides methods for logging activities. */
    public Logger logger;

    /**
     * Handles client requests by dispatching requests to mapped filters and servlets.
     *
     * @param request object that contains the request the client made of the servlet.
     * @param response object that contains the response the servlet returns to the client.
     * @throws IOException if an I/O exception occurs.
     * @throws ServletException if the HTTP request cannot be handled.
     */ 
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LogTimer timer = logger.getTimer().start();
        String path = request.getPathInfo();
        if (path == null) {
            path = "/"; // for our purposes, absense of path is equivalent to root
        }
        Wrapper wrapper = null;
        DispatchChain chain = new DispatchChain();
        for (Binding binding : bindings) {
            Matcher matcher = binding.pattern.matcher(path);
            if (matcher.find()) {
                if (binding.object instanceof HttpServlet) {
                    String servletPath;
                    String pathInfo;
                    if (matcher.groupCount() > 0) { // explicit capture group denoting servlet path
                        servletPath = matcher.group(1);
                        pathInfo = path.substring(matcher.end(1));
                    }
                    else { // implicit servlet path pattern
                        servletPath = path.substring(0, matcher.end());
                        pathInfo = path.substring(matcher.end());
                    }
                    if (servletPath.length() > 0 && servletPath.charAt(servletPath.length() - 1) == '/') {
                        servletPath = servletPath.substring(0, servletPath.length() - 1);
                        pathInfo = '/' + pathInfo; // move trailing slash from servletPath to pathInfo
                    }
                    if (pathInfo.length() > 0 && pathInfo.charAt(0) != '/') {
                        continue; // not a real match
                    }
                    if (pathInfo.length() == 0) {
                        pathInfo = null; // spec calls for null if no pathInfo
                    }
                    wrapper = new Wrapper(request);
                    wrapper.servletPath = servletPath;
                    wrapper.pathInfo = pathInfo;
                    chain.objects.add(binding.object);
                    break; // first matching servlet is chain's terminus
                }
            }
        }
        if (wrapper == null) {
            throw new ServletException("path " + path + " has no servlet to dispatch to");
        }
        chain.doFilter(wrapper, response);
        timer.stop();
    }

    private class DispatchChain implements FilterChain {
        private ArrayList<Object> objects = new ArrayList<Object>();
        private int cursor = 0;
        public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {
            if (cursor >= objects.size()) {
                throw new ServletException("no more objects in chain to dispatch to");
            }
            Object object = objects.get(cursor++);
            if (object instanceof Filter) {
                Filter filter = (Filter)object;
                LogTimer timer = logger.getTimer(filter.getClass().getName() + ".service").start();
                filter.doFilter(request, response, this);
                timer.stop();       
            }
            else if (object instanceof HttpServlet) {
                HttpServlet servlet = (HttpServlet)object;
                LogTimer timer = logger.getTimer(servlet.getServletConfig().getServletName() + ".service").start();
                servlet.service(request, response);
                timer.stop();
            }
            else {
                throw new ServletException("object in chain is not a " + Filter.class.getName() + " or " + HttpServlet.class.getName());
            }
        }
    }

    private class Wrapper extends HttpServletRequestWrapper {
        private String servletPath;
        private String pathInfo;
        private Wrapper(HttpServletRequest request) {
            super(request);
        }
        @Override public String getServletPath() {
            return servletPath;
        }
        @Override public String getPathInfo() {
            return pathInfo;
        }
    }

    /** Binds a regular expression pattern to an servlet or filter to dispatch to. */
    public static class Binding {
        /** The regular expression pattern to match against the incoming request extra path information. */
        public Pattern pattern;
        /** The servlet or filter to dispatch to if the regular expression pattern matches. */
        public Object object;
    }

    /** Creates and initializes a dispatch servlet in a heap environment. */
    public static class Heaplet extends GenericServletHeaplet {
        @Override public HttpServlet createServlet() throws HeapException, ModelException {
            DispatchServlet servlet = new DispatchServlet();
            for (Iterator<ValueNode> i = config.get("bindings").asListNode().iterator(); i.hasNext();) {
                Binding binding = new Binding();
                MapNode node = i.next().required().asMapNode();
                binding.pattern = node.get("pattern").required().asPattern();
                binding.object = HeapUtil.getRequiredObject(heap, node.get("object"), Object.class);
                if (!(binding.object instanceof HttpServlet) && !(binding.object instanceof Filter)) {
                    throw new NodeException(node.get("object"), "must be " + Filter.class.getName() + " or " + HttpServlet.class.getName());
                }
                servlet.bindings.add(binding);
            }
            servlet.logger = this.logger;
            return servlet;
        }
    }
}
