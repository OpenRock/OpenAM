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

/**
 * A writable map-like interface. It is useful for exposing a subset of {@link Map} behavior
 * and duck-typing map-like objects.
 *
 * @author Paul C. Bryan
 */
public interface WritableMap<K, V> {

    /** Used for duck typing writable maps. */
    public static final DuckType<WritableMap> DUCK = new DuckType<WritableMap>(WritableMap.class);  

    /**
     * Maps the specified value to the specified key. If there was a previous mapping for the
     * key, the old value is replaced with the new value.
     *
     * @param key the key with which the specified value is to be mapped.
     * @param value the value to be mapped to the specified key.
     * @return the previous value mapped to the key, or {@code null} if no such mapping.
     */
    V put(K key, V value);

    /**
     * Removes the mapping for the specified key if it is present.
     *
     * @param key the key whose mapping is to be removed.
     * @return the value previously mapped to the key, or {@code null} if no such mapping.
     */
    V remove(Object key);
}
