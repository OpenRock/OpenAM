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

package com.apexidentity.resolver;

// Java Standard Edition
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;

/**
 * Resolves native arrays of objects.
 *
 * @author Paul C. Bryan
 */
public class ArrayResolver implements Resolver {

    /**
     * Returns {@code null}, as arrays are not resolved through type discovery.
     */
    @Override
    public Class getKey() {
        return null; // special resolver, doesn't need index
    }

    @Override
    public Object get(Object object, Object element) {
        if (element instanceof Number) {
            int index = ((Number)element).intValue();
            try {
                if (object instanceof Object[]) { // for array of objects
                    return ((Object[])object)[index];
                }
                else if (object.getClass().isArray()) { // for array of primitives
                    return Array.get(object, index);
                }
            }
            catch (IndexOutOfBoundsException ioobe) {
                // cannot resolve index
            }
        }
        return Resolver.UNRESOLVED;
    }

    @Override
    public Object put(Object object, Object element, Object value) {
        Object original = get(object, element); // get original value first
        if (original != Resolver.UNRESOLVED && element instanceof Number) {
            int index = ((Number)element).intValue();
            try {
                if (object instanceof Object[]) { // for array of objects
                    ((Object[])object)[index] = value;
                }
                else if (object.getClass().isArray()) { // for array of primitives
                    Array.set(object, index, value);
                }
                return original;
            }
            catch (ArrayStoreException ase) {
                // cannot resolve index
            }
            catch (IllegalArgumentException iae) {
                // cannot resolve index
            }
            catch (IndexOutOfBoundsException ioobe) {
                // cannot resolve index
            }
        }
        return Resolver.UNRESOLVED;
    }

    @Override
    public Object remove(Object object, Object element) {
        return Resolver.UNRESOLVED; // we are not at liberty to change size of array
    }

    @Override
    public boolean containsKey(Object object, Object element) {
        if (element instanceof Number) {
            int index = ((Number)element).intValue();
            if (index < 0) {
                return false;
            }
            if (object instanceof Object[]) { // for array of objects
                return ((Object[])object).length > index;
            }
            else if (object.getClass().isArray()) { // for array of primitives
                return Array.getLength(object) > index;
            }
        }
        return false;
    }

    @Override
    public Set<?> keySet(Object object) {
        return (Collections.emptySet()); // arrays are not enumerable
    }
}
