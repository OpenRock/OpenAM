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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Contains another list, which is uses as its basic source of data, possibly transforming the
 * data along the way. This class itself simply overrides all methods of {@link List} with
 * versions that pass all requests to the contained list. Subclasses may further override
 * some of these methods and may also provide additional methods and fields.
 *
 * @author Paul C. Bryan
 */
public class ListDecorator<E> implements List<E>, FullList<E> {

    /** The list wrapped by this decorator. */
    protected final List<E> list;

    /**
     * Constructs a new list decorator, wrapping the specified list.
     *
     * @param list the list to wrap with the decorator.
     */
    public ListDecorator(List<E> list) {
        if (list == null) {
            throw new NullPointerException();
        }
        this.list = list;
    }

    /**
     * Returns the number of elements in the list.
     */
    @Override
    public int size() {
        return list.size();
    }

    /**
     * Returns {@code true} if the list contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns {@code true} if the list contains the specified element.
     *
     * @param o element whose presence in the list is to be tested.
     * @return {@code true} if the list contains the specified element.
     * @throws ClassCastException if the type of the specified element is incompatible with the list (optional).
     * @throws NullPointerException if the specified element is {@code null} and the list does not permit {@code null} elements (optional).
     */
    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    /**
     * Returns an iterator over the elements in the list in proper sequence.
     */
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    /**
     * Returns an array containing all of the elements in the list in proper sequence (from
     * first to last element).
     */
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    /**
     * Returns an array containing all of the elements in the list in proper sequence
     * (from first to last element); the runtime type of the returned array is that of the
     * specified array.
     *
     * @param a the array into which the elements of the list are to be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every element in the list.
     * @throws NullPointerException if the specified array is {@code null}.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    /**
     * Appends the specified element to the end of the list (optional operation).
     *
     * @param e element to be appended to the list.
     * @return {@code true}.
     * @throws UnsupportedOperationException if the {@code add} operation is not supported by the list.
     * @throws ClassCastException if the class of the specified element prevents it from being added to the list.
     * @throws IllegalArgumentException if some property of the element prevents it from being added to the list.
     * @throws NullPointerException if the specified element is {@code null} and the list does not permit {@code null} elements.
     */
    @Override
    public boolean add(E e) {
        return list.add(e);
    }

    /**
     * Removes the first occurrence of the specified element from the list, if it is present
     * (optional operation).
     *
     * @param o element to be removed from the list, if present.
     * @return {@code true} if the list contained the specified element.
     * @throws ClassCastException if the type of the specified element is incompatible with the list (optional).
     * @throws NullPointerException if the specified element is {@code null} and the list does not permit {@code null} elements (optional).
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by the list.
     */
    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    /**
     * Returns {@code true} if the list contains all of the elements of the specified collection.
     *
     * @param c collection to be checked for containment in the list.
     * @return {@code true} if the list contains all of the elements of the specified collection.
     * @throws ClassCastException if the types of one or more elements in the specified collection are incompatible with the list (optional).
     * @throws NullPointerException if the specified collection contains one or more {@code null} elements and the list does not permit {@code null} elements (optional), or if the specified collection is {@code null}.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    /**
     * Appends all of the elements in the specified collection to the end of the list, in the
     * order that they are returned by the specified collection's iterator (optional
     * operation).
     *
     * @param c collection containing elements to be added to the list.
     * @return {@code true} if the list changed as a result of the call.
     * @throws ClassCastException if the class of an element of the specified collection prevents it from being added to the list.
     * @throws IllegalArgumentException if some property of an element of the specified collection prevents it from being added to the list.
     * @throws NullPointerException if the specified collection contains one or more {@code null} elements and the list does not permit {@code null} elements, or if the specified collection is {@code null}.
     * @throws UnsupportedOperationException if the {@code addAll} operation is not supported by the list.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        return list.addAll(c);
    }

    /**
     * Inserts all of the elements in the specified collection into the list at the
     * specified position (optional operation).
     *
     * @param index index at which to insert the first element from the specified collection.
     * @param c collection containing elements to be added to the list.
     * @return {@code true} if the list changed as a result of the call.
     * @throws ClassCastException if the class of an element of the specified collection prevents it from being added to the list.
     * @throws IllegalArgumentException if some property of an element of the specified collection prevents it from being added to the list.
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @throws NullPointerException if the specified collection contains one or more {@code null} elements and the list does not permit {@code null} elements, or if the specified collection is {@code null}.
     * @throws UnsupportedOperationException if the {@code addAll} operation is not supported by the list.
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    /**
     * Removes from the list all of its elements that are contained in the specified
     * collection (optional operation).
     *
     * @param c collection containing elements to be removed from the list.
     * @return {@code true} if the list changed as a result of the call.
     * @throws UnsupportedOperationException if the {@code removeAll} operation is not supported by the list.
     * @throws ClassCastException if the class of an element of the list is incompatible with the specified collection (optional).
     * @throws NullPointerException if the list contains a {@code null} element and the specified collection does not permit {@code null} elements (optional), or if the specified collection is {@code null}.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    /**
     * Retains only the elements in the list that are contained in the specified collection
     * (optional operation).
     *
     * @param c collection containing elements to be retained in the list.
     * @return {@code true} if the list changed as a result of the call.
     * @throws UnsupportedOperationException if the {@code retainAll} operation is not supported by the list.
     * @throws ClassCastException if the class of an element of the list is incompatible with the specified collection (optional).
     * @throws NullPointerException if the list contains a {@code null} element and the specified collection does not permit {@code null} elements (optional), or if the specified collection is {@code null}.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    /**
     * Removes all of the elements from the list (optional operation).
     *
     * @throws UnsupportedOperationException if the {@code clear} operation is not supported by the list.
     */
    @Override
    public void clear() {
        list.clear();
    }

    /**
     * Compares the specified object with the list for equality.
     *
     * @param o the object to be compared for equality with the list.
     * @return {@code true} if the specified object is equal to the list.
     */
    @Override
    public boolean equals(Object o) {
        return list.equals(o);
    }

    /**
     * Returns the hash code value for the list.
     */
    @Override
    public int hashCode() {
        return list.hashCode();
    }

    /**
     * Returns the element at the specified position in the list.
     *
     * @param index index of the element to return.
     * @return the element at the specified position in the list.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public E get(int index) {
        return list.get(index);
    }

    /**
     * Replaces the element at the specified position in the list with the specified element
     * (optional operation).
     *
     * @param index index of the element to replace.
     * @param element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws ClassCastException if the class of the specified element prevents it from being added to the list.
     * @throws IllegalArgumentException if some property of the specified element prevents it from being added to the list.
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @throws NullPointerException if the specified element is {@code null} and the list does not permit {@code null} elements.
     * @throws UnsupportedOperationException if the {@code set} operation is not supported by the list.
     */
    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    /**
     * Inserts the specified element at the specified position in the list (optional
     * operation).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws ClassCastException if the class of the specified element prevents it from being added to the list.
     * @throws IllegalArgumentException if some property of the specified element prevents it from being added to the list.
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @throws NullPointerException if the specified element is {@code null} and the list does not permit {@code null} elements.
     * @throws UnsupportedOperationException if the {@code add} operation is not supported by the list.
     */
    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }

    /**
     * Removes the element at the specified position in the list (optional operation).
     *
     * @param index the index of the element to be removed.
     * @return the element previously at the specified position.
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by the list.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public E remove(int index) {
        return list.remove(index);
    }

    /**
     * Returns the index of the first occurrence of the specified element in the list, or
     * {@code -1} if the list does not contain the element.
     *
     * @param o element to search for.
     * @return the index of the first occurrence of the specified element in the list, or {@code -1} if the list does not contain the element.
     * @throws ClassCastException if the type of the specified element is incompatible with the list (optional).
     * @throws NullPointerException if the specified element is {@code null} and the list does not permit {@code null} elements (optional).
     */
    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    /**
     * Returns the index of the last occurrence of the specified element in the list, or
     * {@code -1} if the list does not contain the element.
     *
     * @param o element to search for.
     * @return the index of the last occurrence of the specified element in the list, or {@code -1} if the list does not contain the element.
     * @throws ClassCastException if the type of the specified element is incompatible with the list (optional).
     * @throws NullPointerException if the specified element is {@code null} and the list does not permit {@code null} elements (optional).
     */
    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    /**
     * Returns a list iterator over the elements in the list (in proper sequence).
     */
    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    /**
     * Returns a list iterator over the elements in the list (in proper sequence), starting
     * at the specified position in the list.
     *
     * @param index index of the first element to be returned from the list iterator.
     * @return a list iterator over the elements in the list (in proper sequence), starting at the specified position in the list.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }


    /**
     * Returns a view of the portion of the list between the specified {@code fromIndex},
     * inclusive, and {@code toIndex}, exclusive. (If {@code fromIndex} and {@code toIndex}
     * are equal, the returned list is empty.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a view of the specified range within the list.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value.
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }
}
