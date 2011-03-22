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
import java.util.Iterator;
import java.util.Set;

/**
 * Contains another set, which is uses as its basic source of data, possibly transforming the
 * data along the way. This class itself simply overrides all methods of {@link Set} with
 * versions that pass all requests to the contained set. Subclasses may further override
 * some of these methods and may also provide additional methods and fields.
 *
 * @author Paul C. Bryan
 */
public class SetDecorator<E> implements Set<E>, Clearable {

    /** The set wrapped by this decorator. */
    protected final Set<E> set;

    /**
     * Constructs a new set decorator, wrapping the specified set.
     *
     * @param set the set to wrap with the decorator.
     */
    public SetDecorator(Set<E> set) {
        if (set == null) {
            throw new NullPointerException();
        }
        this.set = set;
    }

    /**
     * Returns the number of elements in the set (its cardinality).
     */
    @Override
    public int size() {
        return set.size();
    }

    /**
     * Returns {@code true} if the set contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * Returns {@code true} if the set contains the specified element.
     *
     * @param o element whose presence in the set is to be tested.
     * @return {@code true} if the set contains the specified element.
     * @throws ClassCastException if the type of the specified element is incompatible with the set (optional).
     * @throws NullPointerException if the specified element is {@code null} and the set does not permit null elements (optional).
     */
    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    /**
     * Returns an iterator over the elements in the set.
     */
    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    /**
     * Returns an array containing all of the elements in the set.
     */
    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    /**
     * Returns an array containing all of the elements in the set; the runtime type of the
     * returned array is that of the specified array.
     *
     * @param a the array into which the elements of the set are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
     * @return an array containing all the elements in the set.
     * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every element in the set.
     * @throws NullPointerException if the specified array is {@code null}.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    /**
     * Adds the specified element to the set if it is not already present (optional
     * operation).
     *
     * @param e element to be added to the set.
     * @return {@code true} if the set did not already contain the specified element.
     * @throws UnsupportedOperationException if the {@code add} operation is not supported by the set.
     * @throws ClassCastException if the class of the specified element prevents it from being added to the set.
     * @throws NullPointerException if the specified element is {@code null} and the set does not permit null elements.
     * @throws IllegalArgumentException if some property of the specified element prevents it from being added to the set.
     */
    @Override
    public boolean add(E e) {
        return set.add(e);
    }

    /**
     * Removes the specified element from the set if it is present (optional operation).
     *
     * @param o object to be removed from the set, if present.
     * @return {@code true} if the set contained the specified element.
     * @throws ClassCastException if the type of the specified element is incompatible with the set (optional).
     * @throws NullPointerException if the specified element is {@code null} and the set does not permit null elements (optional).
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by the set.
     */
    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    /**
     * Returns {@code true} if the set contains all of the elements of the specified
     * collection.
     *
     * @param c collection to be checked for containment in the set.
     * @return {@code true} if the set contains all of the elements of the specified collection.
     * @throws ClassCastException if the types of one or more elements in the specified collection are incompatible with the set (optional).
     * @throws NullPointerException if the specified collection contains one or more {@code null} elements and the set does not permit null elements (optional), or if the specified collection is {@code null}.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to the set if they're not already
     * present (optional operation).
     *
     * @param c collection containing elements to be added to the set.
     * @return {@code true} if the set changed as a result of the call.
     * @throws UnsupportedOperationException if the {@code addAll} operation is not supported by the set.
     * @throws ClassCastException if the class of an element of the specified collection prevents it from being added to the set.
     * @throws NullPointerException if the specified collection contains one or more {@code null} elements and the set does not permit null elements, or if the specified collection is {@code null}.
     * @throws IllegalArgumentException if some property of an element of the specified collection prevents it from being added to the set.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return set.addAll(c);
    }

    /**
     * Retains only the elements in the set that are contained in the specified collection
     * (optional operation).
     *
     * @param c collection containing elements to be retained in the set.
     * @return {@code true} if the set changed as a result of the call.
     * @throws UnsupportedOperationException if the retainAll operation is not supported by the set.
     * @throws ClassCastException if the class of an element of the set is incompatible with the specified collection (optional).
     * @throws NullPointerException if the set contains a {@code null} element and the specified collection does not permit null elements (optional), or if the specified collection is {@code null}.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    /**
     * Removes from the set all of its elements that are contained in the specified collection
     * (optional operation).
     *
     * @param c collection containing elements to be removed from the set.
     * @return {@code true} if the set changed as a result of the call.
     * @throws UnsupportedOperationException if the {@code removeAll} operation is not supported by the set.
     * @throws ClassCastException if the class of an element of the set is incompatible with the specified collection (optional).
     * @throws NullPointerException if the set contains a {@code null} element and the specified collection does not permit null elements (optional), or if the specified collection is {@code null}.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    /**
     * Removes all of the elements from the set (optional operation).
     *
     * @throws UnsupportedOperationException if the {@code clear} method is not supported by the set.
     */
    @Override
    public void clear() {
        set.clear();
    }

    /**
     * Compares the specified object with the set for equality.
     *
     * @param o object to be compared for equality with the set.
     * @return {@code true} if the specified object is equal to the set.
     */
    @Override
    public boolean equals(Object o) {
        return set.equals(o);
    }

    /**
     * Returns the hash code value for the set.
     */
    @Override
    public int hashCode() {
        return set.hashCode();
    }
}
