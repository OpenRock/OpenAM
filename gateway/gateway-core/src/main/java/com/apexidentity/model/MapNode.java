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

package com.apexidentity.model;

// Java Standard Edition
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;

// ApexIdentity Core Library
import com.apexidentity.util.EnumerableMap;
import com.apexidentity.util.MapDecorator;
import com.apexidentity.util.ReadableMap;
import com.apexidentity.util.TypedHashSet;

/**
 * Represents a map in an object model structure.
 * <p>
 * While this class intentionally resembles the Java {@code Map} class, there is a notable
 * difference: the {@code get} method unconditionally returns a {@link ValueNode} object. If
 * there is no actual value in the underlying map, then the returned value will contain
 * {@code null}.
 * <p>
 * A map-like object is one that exposes the method defined in the {@link ReadableMap}
 * interface, which is a subset of the {@link Map} interface. In addition, the
 * {@link EnumerableMap} interface is optionally supported for enumerating entries in the
 * map.
 *
 * @author Paul C. Bryan
 */
public class MapNode extends Node implements ReadableMap<String, ValueNode>, EnumerableMap<String, ValueNode> {

    /** The underlying map, duck-typed as a readable map. */
    private ReadableMap<String, Object> readable;

    /** The underlying map, duck-typed as an enumerable map, or {@code null} if not enumerable . */
    private EnumerableMap<String, Object> enumerable;

    /**
     * Wraps a map-like object in an object model structure.
     *
     * @param map the underlying map whose values will be wrapped.
     * @param path the path of this value in an object model structure.
     * @throws NodeException if the supplied object is not a map-like object.
     */
    @SuppressWarnings("unchecked")
    public MapNode(Object map, String path) throws NodeException {
        super(map, path);
        if (map instanceof Map) { // avoid unnecessary proxying via duck typing
            MapDecorator decorator = new MapDecorator((Map)map);
            readable = decorator;
            enumerable = decorator;
        }
        else {
            try {
                readable = ReadableMap.DUCK.cast(map);
            }
            catch (ClassCastException cce) {
                throw new NodeException(this, "expecting ReadableMap-like object");
            }
            if (EnumerableMap.DUCK.isInstance(map)) {
                enumerable = EnumerableMap.DUCK.cast(map);
            }
        }
    }

    private String subpath(String path, String key) {
        StringBuilder sb = new StringBuilder(path);
        if (SourceVersion.isIdentifier(key)) { // dot notation for identifier
            sb.append('.');
            sb.append(key);
        }
        else {
            int length = key.length();
            sb.append("['");
            for (int n = 0, cp; n < length; n += Character.charCount(cp)) {
                cp = key.codePointAt(n);
                if (cp == '\'' || cp == '\\') {
                    sb.append('\\');
                }
                sb.appendCodePoint(cp);
            }
            sb.append("']");
        }
        return sb.toString();
    }

    /**
     * Returns the value to which the specified key is associated, wrapped in a
     * {@code ValueNode}. If no such key exists, then the returned value will contain
     * {@code null}.
     *
     * @param key the key whose associated value is to be returned.
     * @return a value to which the specified key is associated.
     * @throws ClassCastException if the key is of an inappropriate type for this object.
     */
    @Override
    public ValueNode get(Object key) {
        ValueNode value = null;
        if (key instanceof CharSequence) {
            String k = ((CharSequence)key).toString();
            value = new ValueNode(readable.get(k), subpath(path, k));
        }
        return value;
    }

    /**
     * Returns {@code true} if this map contains a value for the specified key. If the
     * underlying map is not enumerable, then this method returns {@code false}.
     *
     * @param key the key whose presence in this object is to be tested.
     * @return {@code true} if this object contains a value for the specified key.
     */
    @Override
    public boolean containsKey(Object key) {
        return (enumerable != null && enumerable.containsKey(key));
    }

    /**
     * Returns the number of values in this map. If the underlying map is not enumerable,
     * then this method returns {@code 0}.
     */
    @Override
    public int size() {
        return (enumerable != null ? enumerable.size() : 0);
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. If the underlying map is
     * not enumerable, then this method returns an empty set.
     */
    @Override
    public Set<String> keySet() {
        if (enumerable != null) {
            return new TypedHashSet<String>(enumerable.keySet(), String.class);
        }
        return Collections.emptySet();
    }
}
