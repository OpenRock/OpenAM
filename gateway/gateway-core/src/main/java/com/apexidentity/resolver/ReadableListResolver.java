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
import java.util.Collections;
import java.util.Set;

// ApexIdentity Core Library
import com.apexidentity.util.ReadableList;

/**
 * Resolves {@link ReadableList} objects. This implementation uses duck-typing to attempt
 * access to the object. It will successfully resolve the object if it exposes the methods
 * defined in the {@code ReadableList} interface.
 *
 * @author Paul C. Bryan
 */
public class ReadableListResolver implements Resolver {

    @Override
    public Class getKey() {
        return ReadableList.class;
    }

    @Override
    public Object get(Object object, Object element) {
        if (element instanceof Number) {
            int index = ((Number)element).intValue();
            try {
                return ReadableList.DUCK.cast(object).get(index);
            }
            catch (ClassCastException cce) {
                // drop down
            }
            catch (IndexOutOfBoundsException ioobe) {
                // drop down
            }
        }
        return Resolver.UNRESOLVED; // unresolved
    }
    
    @Override
    public Object put(Object object, Object element, Object value) {
        return Resolver.UNRESOLVED; // read-only
    }

    @Override
    public Object remove(Object object, Object element) {
        return Resolver.UNRESOLVED; // read-only
    }

    @Override
    public boolean containsKey(Object object, Object element) {
        if (element instanceof Number) {
            int index = ((Number)element).intValue();
            try {
                return (index >= 0 && ReadableList.DUCK.cast(object).size() > index);
            }
            catch (ClassCastException cce) {
                // cannot be duck-typed into a readable list
            }
        }
        return false;
    }

    @Override
    public Set<?> keySet(Object object) {
        return (Collections.emptySet()); // lists aren't enumerable
    }
}
