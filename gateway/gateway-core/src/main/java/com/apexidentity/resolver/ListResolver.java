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
import java.util.List;
import java.util.Set;

/**
 * Resolves {@link List} objects.
 *
 * @author Paul C. Bryan
 */
public class ListResolver implements Resolver {

    @Override
    public Class getKey() {
        return List.class;
    }

    @Override
    public Object get(Object object, Object element) {
        if (object instanceof List && element instanceof Number) {
            try {
                return ((List)object).get(((Number)element).intValue());
            }
            catch (IndexOutOfBoundsException ioobe) {
                // cannot resolve index
            }
        }
        return Resolver.UNRESOLVED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object put(Object object, Object element, Object value) {
        if (object instanceof List && element instanceof Number) {
            List list = (List)object;
            int index = ((Number)element).intValue();
            try {
                if (list.size() > index) { // within existing list 
                    list.set(index, value);
                }
                else if (list.size() == index) { // appending to end of list
                    list.add(element);
                }
                // otherwise, ignore out-of-range index
            }
            catch (UnsupportedOperationException uoe) {
                // ignore failed attempts to write to read-only list
            }
        }
        return Resolver.UNRESOLVED;
    }

    @Override
    public Object remove(Object object, Object element) {
        if (object instanceof List && element instanceof Number) {
            try {
                List list = (List)object;
                int index = ((Number)element).intValue();
                if (index > 0 && index == list.size() - 1) { // removing end of list
                    return list.remove(index);
                }
            }
            catch (UnsupportedOperationException uoe) {
                // ignore failed attempts to write to read-only list
            }
        }
        return Resolver.UNRESOLVED;
    }

    @Override
    public boolean containsKey(Object object, Object element) {
        if (object instanceof List && element instanceof Number) {
            int index = ((Number)element).intValue();
            return (index >= 0 && ((List)object).size() > index);
        }
        return false;
    }

    @Override
    public Set<?> keySet(Object object) {
        return (Collections.emptySet()); // lists aren't enumerable
    }
}
