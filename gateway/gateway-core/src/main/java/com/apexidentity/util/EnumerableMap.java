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

package com.apexidentity.util;

// Java Standard Edition
import java.util.Map; // Javadoc
import java.util.Set;

/**
 * A readable map-like interface whose keys and size can also be determined. It is
 * useful for exposing a subset of {@link Map} behavior and duck-typing map-like objects.
 *
 * @author Paul C. Bryan
 */
public interface EnumerableMap<K, V> extends ReadableMap<K, V> {

    /** Used for duck typing enumerable maps. */
    public static final DuckType<EnumerableMap> DUCK = new DuckType<EnumerableMap>(EnumerableMap.class);  

    /**
     * Returns {@code true} if there is a mapping for the specified key.
     */
    boolean containsKey(Object key);

    /**
     * Returns the number of keys mapped to values.
     */
    int size();

    /**
     * Returns a {@link Set} of the keys that are mapped to values.
     */
    Set<K> keySet();
}
