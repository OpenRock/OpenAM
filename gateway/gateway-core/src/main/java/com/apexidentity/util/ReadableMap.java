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
 * A readable map-like interface. It is useful for exposing a subset of {@link Map} behavior
 * and duck-typing map-like objects.
 *
 * @author Paul C. Bryan
 */
public interface ReadableMap<K, V> {

    /** Used for duck typing readable maps. */
    public static final DuckType<ReadableMap> DUCK = new DuckType<ReadableMap>(ReadableMap.class);  

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key whose mapped value is to be returned.
     * @return the value to which the specified key is mapped, or {@code null} if no such mapping.
     */
    V get(Object key);
}
