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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of a set whose values are case-insensitive strings. All operations match
 * values in a case-insensitive manner. The original cases of values are retained, so the
 * {@link #iterator() iterator()} method for example returns the originally values.
 * <p>
 * <strong>Note:</strong> The behavior of this class is undefined when wrapping a set that
 * has keys that would result in duplicate case-insensitive values.
 *
 * @author Paul C. Bryan
 */
public class CaseInsensitiveSet extends SetDecorator<String> {

    /** Maps lowercase elements to the real string elements. */
    private final HashMap<String, String> lc;

    /**
     * Constructs a new empty case-insensitive set. The backing set is a new {@link HashSet}
     * with default initial capacity and load factor.
     */
    public CaseInsensitiveSet() {
        super(new HashSet<String>());
        lc = new HashMap<String, String>();
    }

    /**
     * Constructs a new case-insensitive set containing the elements in the specified
     * collection. The {@code HashSet} is created with default load factor and an initial
     * capacity sufficient to contain the elements in the specified collection.
     *
     * @param c the collection whose elements are to be placed into this set.
     * @throws NullPointerException if the specified collection is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public CaseInsensitiveSet(Collection<String> c) {
        super(c instanceof Set ? (Set)c : new HashSet<String>(c)); 
        lc = new HashMap<String, String>(c.size());
        for (String e : c) {
            lc.put(e.toLowerCase(), e);
        }
    }

    /**
     * Constructs a new, empty case-insensitive set; the backing {@code HashSet} instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the hash set.
     * @param loadFactor the load factor of the hash set.
     * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load factor is nonpositive.
     */
    public CaseInsensitiveSet(int initialCapacity, float loadFactor) {
        super(new HashSet<String>(initialCapacity, loadFactor));
        lc = new HashMap<String, String>(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty case-insensitive set; the backing {@code HashSet} instance has
     * the specified initial capacity and default load factor.
     *
     * @param initialCapacity the initial capacity of the hash set.
     * @throws IllegalArgumentException if the initial capacity is less than zero.
     */
    public CaseInsensitiveSet(int initialCapacity) {
        super(new HashSet<String>(initialCapacity));
        lc = new HashMap<String, String>(initialCapacity);
    }

    private Object translate(Object element) {
        if (element != null && element instanceof String) {
            String e = lc.get(((String)element).toLowerCase());
            if (e != null) { // found a mapped-equivalent
                element = e;
            }
        }
        return element;
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(translate(o));
    }

    @Override
    public boolean add(String e) {
        if (contains(e)) {
            return false;
        }
        lc.put(e.toLowerCase(), e);
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = super.remove(translate(o));
        if (o != null && o instanceof String) {
            lc.remove(((String)o).toLowerCase());
        }
        return removed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean changed = false;
        for (String e : c) {
            if (add(e)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        for (String e : this) {
            if (!c.contains(e)) {
                remove(e);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (String e : this) {
            if (c.contains(e)) {
                remove(e);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
        lc.clear();
        super.clear();
    }
}
