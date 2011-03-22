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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

// Java Enterprise Edition
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

// ApexIdentity Common Library
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;

/**
 * Heaplet with methods common to servlets and filters.
 *
 * @author Paul C. Bryan
 */
abstract class CommonHeaplet extends NestedHeaplet { 

    /** Initialization parameters to supply to servlet or filter. */
    protected final Map<String, String> initParams = new HashMap<String, String>();

    /** Context to supply in configuration during initialization. */
    protected ServletContext servletContext;

    /**
     * Returns the servlet context in which the caller is executing.
     */ 
    public ServletContext getServletContext() { // FilterConfig, ServletConfig
        return servletContext;
    }

    /**
     * Returns the value of the named initialization parameter.
     *
     * @param name the name of the initialization parameter to get.
     * @return the value of the the initialization parameter, or {@code null} if the parameter does not exist.
     */
    public String getInitParameter(String name) { // FilterConfig, ServletConfig
        return initParams.get(name);
    }

    /**
     * Returns the names of the initialization parameters, or an empty enumeration if there
     * are no initialization parameters.
     */
    public Enumeration getInitParameterNames() { // FilterConfig, ServletConfig
        return Collections.enumeration(initParams.keySet());
    }

    /**
     * Configures the servlet context and initialization parameters.
     *
     * @throws HeapException if an exception occurred during creation of the heap object or any of its dependencies.
     * @throws ModelException if the heaplet (or one of its dependencies) has a malformed object model configuration.
     */
    protected void configure() throws HeapException, ModelException {
        servletContext = HeapUtil.getRequiredObject(heap, config.get("servletContext").defaultTo("ServletContext"), ServletContext.class);
        MapNode node = config.get("initParams").asMapNode();
        if (node != null) {
            for (String key : node.keySet()) {
                initParams.put(key, node.get(key).asString());
            }
        }
    }
}
