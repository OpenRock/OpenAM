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
import java.util.HashSet;

/**
 * A hash set that silently ignores operations on any elements other than those with a
 * specific type.
 *
 * @author Paul C. Bryan
 */
public class TypedHashSet<E> extends HashSet<E> {

    private static final long serialVersionUID = 1L;

    /** The type of objects that should be stored in the set. */
    private Class<E> type;

    /**
     * Constructs a new, empty set. The backing {@link HashSet} instance has default initial
     * capacity and load factor.
     *
     * @param type the type of objects that should be stored in the set.
     */
    public TypedHashSet(Class<E> type) {
        this(0, type);
    }

    /**
     * Constructs a new set containing the elements in the specified collection. The backing
     * {@link HashSet} is created with default load factor and an initial capacity sufficient
     * to contain the elements in the specified collection.
     *
     * @param c the collection whose elements are to be placed into this set.
     * @param type the type of objects that should be stored in the set.
     * @throws NullPointerException if the specified collection is {@code null}.
     */
    @SuppressWarnings("unchecked")
    public TypedHashSet(Collection c, Class<E> type) {
        this(c.size(), type);
        addAll(c);
    }

    /**
     * Constructs a new, empty set; the backing {@link HashSet} instance has the specified
     * initial capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the hash set.
     * @param loadFactor the load factor of the hash set.
     * @param type the type of objects that should be stored in the set.
     * @throws IllegalArgumentException if the initial capacity is less than zero, or if the load factor is nonpositive.
     */
    public TypedHashSet(int initialCapacity, float loadFactor, Class<E> type) {
        super(new HashSet<E>(initialCapacity, loadFactor));
        this.type = type;
    }

    /**
     * Constructs a new, empty set; the backing {@link HashSet} instance has the specified
     * initial capacity and default load factor.
     *
     * @param initialCapacity the initial capacity of the hash set.
     * @throws IllegalArgumentException if the initial capacity is less than zero.
     */
    public TypedHashSet(int initialCapacity, Class<E> type) {
        super(new HashSet<E>(initialCapacity));
        this.type = type;
    }

    /**
     * Adds the specified element to the set if its type is appropriate and is not already
     * present in the set.
     *
     * @param e element to be added to the set.
     * @return {@code true} if type is appropriate and the set did not already contain the specified element.
     */
    @Override
    public boolean add(E e) {
        return (type.isInstance(e) ? super.add(e) : false);
    }
}
