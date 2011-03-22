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

package com.apexidentity.heap;

// ApexIdentity Core Library
import com.apexidentity.model.ModelException;

/**
 * Manages a collection of associated objects created and initialized by {@link Heaplet}
 * objects. A heap object may be lazily initialized, meaning that it or its dependencies
 * may not be created until first requested from the heap.
 *
 * @author Paul C. Bryan
 */
public interface Heap {

    /**
     * Returns an object from the heap with a specified name, or {@code null} if no such
     * object exists.
     *
     * @param name the name of the object in the heap to be retrieved.
     * @return the requested object from the heap, or {@code null} if no such object exists.
     * @throws HeapException if an exception occurred during creation of the heap object or any of its dependencies.
     * @throws ModelException if a heaplet (or one of its dependencies) has a malformed configuration object.
     */
    Object get(String name) throws HeapException, ModelException;
}
