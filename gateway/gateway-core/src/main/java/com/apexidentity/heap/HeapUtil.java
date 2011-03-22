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
import com.apexidentity.model.NodeException;
import com.apexidentity.model.ValueNode;

/**
 * Utility methods for managing objects in the heap.
 *
 * @author Paul C. Bryan
 */
public class HeapUtil {

    /** Static methods only. */
    private HeapUtil() {
    }

    /**
     * Retreives an object from a heap with the specified name and type.
     *
     * @param heap the heap to retrieve the object from.
     * @param name an object model string value containing the name of the heap object to retrieve.
     * @param type the expected type of the heap object.
     * @return the specified heap object.
     * @throws HeapException if there was an exception creating the heap object or any of its dependencies.
     * @throws ModelException if the name is {@code null}, is not an object model string, or the specified heap object could not be retrieved.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObject(Heap heap, ValueNode name, Class<T> type) throws HeapException, ModelException {
        Object o = heap.get(name.required().asString());
        if (o != null && !(type.isInstance(o))) {
            throw new NodeException(name, "expecting heap object of type " + type.getName());
        }
        return (T)o;
    }

    /**
     * Retreives an object from a heap with the specified name and type. If the object does not
     * exist, a {@link NodeException} is thrown.
     *
     * @param heap the heap to retrieve the object from.
     * @param name an object model string value containing the name of the heap object to retrieve.
     * @param type the expected type of the heap object.
     * @return the specified heap object.
     * @throws HeapException if there was an exception creating the heap object or any of its dependencies.
     * @throws ModelException if the name is {@code null}, is not an object model string, or the specified heap object could not be retrieved.
     */
    public static <T> T getRequiredObject(Heap heap, ValueNode name, Class<T> type) throws HeapException, ModelException {
        T t = getObject(heap, name, type);
        if (t == null) {
            throw new NodeException(name, "object not found in heap");
        }
        return t;
    }
}
