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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Contains another map, which is uses as its basic source of data, possibly transforming the
 * data along the way. This class itself simply overrides all methods of {@link Map} with
 * versions that pass all requests to the contained map. Subclasses may further override
 * some of these methods and may also provide additional methods and fields.
 *
 * @author Paul C. Bryan
 */
public class MapDecorator<K, V> implements Map<K, V>, FullMap<K, V> {

    /** The map wrapped by this decorator. */
    protected final Map<K, V> map;

    /**
     * Constructs a new map decorator, wrapping the specified map.
     *
     * @param map the map to wrap with the decorator.
     */
    public MapDecorator(Map<K, V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        this.map = map;
    }

    /**
     * Returns the number of key-value mappings in the map.
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns {@code true} if the map contains no key-value mappings.
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns {@code true} if the map contains a mapping for the specified key.
     *
     * @throws ClassCastException if the key is of an inappropriate type for the map (optional).
     * @throws NullPointerException if the specified key is null and the map does not permit null keys (optional).
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Returns {@code true} if the map maps one or more keys to the specified value.
     *
     * @param value value whose presence in the map is to be tested.
     * @return {@code true} if the map maps one or more keys to the specified value.
     * @throws ClassCastException if the value is of an inappropriate type for the map (optional).
     * @throws NullPointerException if the specified value is null and the map does not permit null values (optional).
     */
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if the map
     * contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value to which the specified key is mapped, or {@code null} if the map contains no mapping for the key.
     * @throws ClassCastException if the key is of an inappropriate type for the map (optional).
     * @throws NullPointerException if the specified key is null and the map does not permit null keys (optional).
     */
    @Override
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * Associates the specified value with the specified key in the map (optional operation).
     * 
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return the previous value associated with key, or {@code null} if there was no mapping for key.
     * @throws ClassCastException if the class of the specified key or value prevents it from being stored in the map.
     * @throws IllegalArgumentException if some property of the specified key or value prevents it from being stored in the map.
     * @throws NullPointerException if the specified key or value is {@code null} and the map does not permit null keys or values.
     * @throws UnsupportedOperationException if the {@code put} operation is not supported by the map.
     */
    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * Removes the mapping for a key from the map if it is present (optional operation).
     *
     * @param key key whose mapping is to be removed from the map.
     * @return the previous value associated with key, or {@code null} if there was no mapping for key.
     * @throws ClassCastException if the key is of an inappropriate type for the map (optional).
     * @throws NullPointerException if the specified key is {@code null} and the map does not permit null keys (optional).
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by the map.
     */
    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    /**
     * Copies all of the mappings from the specified map to the map (optional operation).
     * 
     * @param m mappings to be stored in the map.
     * @throws ClassCastException if the class of a key or value in the specified map prevents it from being stored in the map.
     * @throws IllegalArgumentException if some property of a key or value in the specified map prevents it from being stored in the map.
     * @throws NullPointerException if the specified map is null, or if the map does not permit null keys or values, and the specified map contains null keys or values.
     * @throws UnsupportedOperationException if the {@code putAll} operation is not supported by the map.
     */
    @Override
    public void putAll(Map<? extends K,? extends V> m) {
        map.putAll(m);
    }

    /**
     * Removes all of the mappings from the map (optional operation).
     *
     * @throws UnsupportedOperationException if the {@code clear} operation is not supported by the map.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns a {@link Set} view of the keys contained in the map.
     */
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * Returns a {@link Collection} view of the values contained in the map.
     */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /**
     * Returns a {@link Set} view of the mappings contained in the map.
     */
    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        return map.entrySet();
    }

    /**
     * Returns the hash code value for the map.
     */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * Compares the specified object with the map for equality.
     *
     * @param o object to be compared for equality with the map.
     * @return true if the specified object is equal to the map.
     */
    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }
}
