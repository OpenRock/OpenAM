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
import com.apexidentity.util.EnumerableMap;

/**
 * Resolves {@link EnumerableMap} objects. This implementation uses duck-typing to attempt
 * access to the object. It will successfully resolve the object if it exposes the methods
 * defined in the {@code EnumerableMap} interface.
 *
 * @author Paul C. Bryan
 */
public class EnumerableMapResolver extends ReadableMapResolver {

    @Override
    public Class getKey() {
        return EnumerableMap.class;
    }

    @Override
    public boolean containsKey(Object object, Object element) {
        try {
            return EnumerableMap.DUCK.cast(object).containsKey(element);
        }
        catch (ClassCastException cce) {
            return false; // cannot be duck-typed into a readable map
        }
    }

    @Override
    public Set<?> keySet(Object object) {
        try {
            return EnumerableMap.DUCK.cast(object).keySet();
        }
        catch (ClassCastException cce) {
            return Collections.emptySet(); // cannot be duck-typed into a readable map
        }
    }
}
