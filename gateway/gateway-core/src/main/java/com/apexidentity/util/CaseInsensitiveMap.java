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
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of a map whose keys are case-insensitive strings. All operations match
 * keys in a case-insensitive manner. The original cases of keys are retained, so the
 * {@link #keySet() keySet()} method for example returns the original keys.
 * <p>
 * <strong>Note:</strong> The behavior of this class is undefined when wrapping a map that
 * has keys that would result in duplicate case-insensitive keys.
 *
 * @author Paul C. Bryan
 */
public class CaseInsensitiveMap<V> extends MapDecorator<String, V> {

    /** Maps lowercase keys to the real string keys. */
    private final HashMap<String, String> lc = new HashMap<String, String>();

    /**
     * Constructs a new empty case-insensitive map. The backing map is a new {@link HashMap}
     * with default initial capacity and load factor.
     */
    public CaseInsensitiveMap() {
        super(new HashMap<String, V>());
    }

    /**
     * Wraps an existing map with a new case insensitive map.
     *
     * @param map the map to wrap with a new case insensitive map.
     */
    public CaseInsensitiveMap(Map<String, V> map) {
        super(map);
        sync();
    }

    /**
     * Returns a case-insensitve key translated into its mapped equivalent. If its equivalent
     * cannot be found, then the key is returned untouched.
     */
    private Object translate(Object key) {
        if (key != null && key instanceof String) {
            String k = lc.get(((String)key).toLowerCase());
            if (k != null) { // found a mapped-equivalent
                key = k;
            }
        }
        return key;
    }

    /**
     * Synchronizes the keys of this case insensitive map and those of the map it is wrapping.
     * This is necessary if the underlying map is modified directly and this map needs to be
     * resynchronized.
     */
    public void sync() {
        lc.clear();
        for (String key : map.keySet()) {
            lc.put(key.toLowerCase(), key);
        }
    }

    @Override
    public void clear() {
        lc.clear();
        super.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(translate(key));
    }

    @Override
    public V put(String key, V value) {
        V removed = remove(key); // remove potentially differently-cased key
        lc.put(key.toLowerCase(), key);
        super.put(key, value);
        return removed;
    }

    @Override 
    public void putAll(Map<? extends String, ? extends V> m) {
        for (String key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public V get(Object key) {
        return super.get(translate(key));
    }

    @Override
    public V remove(Object key) {
        V removed = super.remove(translate(key));
        if (key != null && key instanceof String) {
            lc.remove(((String)key).toLowerCase());
        }
        return removed;
    }
}
