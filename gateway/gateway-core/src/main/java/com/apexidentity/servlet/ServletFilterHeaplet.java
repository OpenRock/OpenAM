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

// ApexIdentity Library Core
import com.apexidentity.heap.HeapException;
import com.apexidentity.model.ModelException;

/**
 * Creates and initializes a stock servlet filter in a heap environment.
 *
 * @author Paul C. Bryan
 */
public class ServletFilterHeaplet extends GenericFilterHeaplet {
    @Override public Class getKey() {
        return Filter.class;
    }
    @Override public Filter createFilter() throws HeapException, ModelException {
        return config.get("class").required().asNewInstance(Filter.class);
    }
}
