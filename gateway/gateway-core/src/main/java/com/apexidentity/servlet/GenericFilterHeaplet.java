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
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

// ApexIdentity Core Library
import com.apexidentity.heap.HeapException;
import com.apexidentity.model.ModelException;

/**
 * Generic heaplet base class for Java servlets and filters. Implements the
 * {@link FilterConfig} interface for initialization of the filter.
 *
 * @author Paul C. Bryan
 */
public abstract class GenericFilterHeaplet extends CommonHeaplet implements FilterConfig {

    /** The filter being managed by this heaplet. */
    private Filter filter;

    /**
     * Initializes the heaplet and creates the filter. The filter is created through a call
     * to the abstract {@link #createFilter()} method. Once created, the filter is
     * initialized through a call to its {@link Filter#init(FilterConfig)} method.
     */
    @Override // GenericHeaplet
    public Object create() throws HeapException, ModelException {
        configure();
        filter = createFilter();
        try {
            filter.init(this);
        }
        catch (ServletException se) {
            throw new HeapException(se);
        }
        return filter;
    }

    /**
     * Calls the filter's {@code destroy()} method to notify it that it is being taken out of
     * service. Once destroyed, the filter is dereferenced by the heaplet.
     */
    @Override // GenericHeaplet
    public void destroy() {
        filter.destroy();
        this.filter = null;
        super.destroy();
    }

    /**
     * Returns the name of the filter.
     */
    @Override // FilterConfig
    public String getFilterName() {
        return super.name;
    }

    /**
     * Called to request the heaplet create a filter object. Called by {@link #create()}.
     *
     * @throws HeapException if an exception occurred during creation of the heap object or any of its dependencies.
     * @throws ModelException if the heaplet (or one of its dependencies) has a malformed object model configuration.
     */
    public abstract Filter createFilter() throws HeapException, ModelException;
}
