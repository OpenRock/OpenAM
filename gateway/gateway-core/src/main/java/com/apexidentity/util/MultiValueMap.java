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
 * Copyright 2009 Sun Microsystems Inc. All rights reserved.
 * Portions Copyrighted © 2010–2011 ApexIdentity Inc.
 */

package com.apexidentity.util;

// Java Standard Edition
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wraps a map for which the values are lists, providing a set of convenience methods for
 * handling list values.
 *
 * @author Paul C. Bryan
 */
public class MultiValueMap<K, V> extends MapDecorator<K, List<V>> {

    /**
     * Creates a new multi-value map, wrapping an existing map with list values.
     *
     * @param map the map to wrap with a new multi-value map.
     */
    public MultiValueMap(Map<K, List<V>> map) {
        super(map);
    }

    /**
     * Adds the specified value to the list for the specified key. If no list for the key yet
     * exists in the map, a new list is created and added.
     *
     * @param key the key of the list to add the value to.
     * @param value the value to be added to the list.
     */
    public void add(K key, V value) {
        List<V> list = get(key);
        if (list == null) {
            list = new ArrayList<V>();
            put(key, list);
        }
        list.add(value);
    }

    /**
     * Adds the specified values to the list for the specified key. If no list for the key
     * yet exists in the map, a new list is created and added.
     *
     * @param key the key of the list to add the values to.
     * @param values the values to be added to the list.
     */
    public void addAll(K key, Collection<? extends V> values) {
        List<V> list = get(key);
        if (list == null) {
            list = new ArrayList<V>();
            put(key, list);
        }
        list.addAll(values);
    }

    /**
     * Adds the specified keys and values from the specified map into this map.
     *
     * @param map the map whose keys and values are to be added.
     */
    public void addAll(MultiValueMap<K, V> map) {
        for (K key : map.keySet()) {
            addAll(key, map.get(key));
        }
    }

    /**
     * Adds the specified keys and values from the specified map into this map.
     *
     * @param map the map whose keys and values are to be added.
     */
    public void addAll(Map<? extends K, Collection<? extends V>> map) {
        for (K key : map.keySet()) {
            addAll(key, map.get(key));
        }
    }

    /**
     * Returns the first value in the list of values for the matching key, or {@code null}
     * if no such value exists.
     *
     * @param key the key whose associated first item is to be returned.
     * @return the first value in the key's value list, or null if non-existent.
     */
    public V getFirst(K key) {
        List<V> list = get(key);
        if (list == null || list.size() == 0) {
            return null;
        }
        else {
            return list.get(0);
        }
    }

    /**
     * Maps a single value to the specified key, replacing any value(s) that are already
     * mapped to that key.
     *
     * @param key key with which the specified value is to be mapped.
     * @param value the single value to be mapped to the specified key.
     */
    public void put(K key, V value) {
        remove(key);
        add(key, value);
    }
}
