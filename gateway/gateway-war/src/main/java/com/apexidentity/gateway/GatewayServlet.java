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

package com.apexidentity.gateway;

// Java Standard Edition
import java.io.IOException;

// Java Enterprise Edition
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// ApexIdentity Core Library
import com.apexidentity.config.Config;
import com.apexidentity.config.ConfigResource;
import com.apexidentity.heap.HeapImpl;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.io.TemporaryStorage;
import com.apexidentity.log.ConsoleLogSink;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.resource.ResourceException;

/**
 * TODO: Description.
 *
 * @author Paul C. Bryan
 */
public class GatewayServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** TODO: Description. */
    private HttpServlet servlet;

    @Override
    public void init() throws ServletException {
        try {
            ServletContext context = getServletConfig().getServletContext();
            MapNode config = new Config(new ConfigResource("ApexIdentity", "Gateway", context)).read();
            HeapImpl heap = new HeapImpl();
            heap.put("ServletContext", context); // can be overridden in config
            heap.put("TemporaryStorage", new TemporaryStorage()); // can be overridden in config
            heap.put("LogSink", new ConsoleLogSink()); // can be overridden in config
            heap.init(config.get("heap").required().asMapNode());
            servlet = HeapUtil.getRequiredObject(heap, config.get("servletObject").required(), HttpServlet.class);
        }
        catch (HeapException he) {
            throw new ServletException(he);
        }
        catch (ModelException me) {
            throw new ServletException(me);
        }
        catch (ResourceException re) {
            throw new ServletException(re); 
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        servlet.service(request, response);
    }
}
