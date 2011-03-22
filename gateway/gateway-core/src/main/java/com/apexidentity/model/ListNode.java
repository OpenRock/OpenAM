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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

// ApexIdentity Core Library
import com.apexidentity.util.ListDecorator;
import com.apexidentity.util.ReadableList;

/**
 * Represents a list in an object model structure.
 * <p>
 * While this class intentionally resembles the Java {@code List} class, there is a notable
 * difference: the {@code get} method unconditionally returns a {@link ValueNode} object, even
 * if the requested index is out of bounds. If there is no actual value in the underlying list,
 * then the returned value will contain {@code null}.
 * <p>
 * A list-like object is one that exposes all of the methods defined in the
 * {@link ReadableList} interface, which is a subset of the {@link List} interface.
 *
 * @author Paul C. Bryan
 */
public class ListNode extends Node implements ReadableList<ValueNode> {

    /** The underlying list, duck-typed as a readable list. */
    private ReadableList<Object> readable;

    /**
     * Wraps a list-like object in a an object model structure.
     *
     * @param list the underlying list whose elements will be wrapped.
     * @param path the path of this value in an object tree structure.
     * @throws NodeException if the supplied object is not a list-like object.
     */
    @SuppressWarnings("unchecked")
    public ListNode(Object list, String path) throws NodeException {
        super(list, path);
        if (list instanceof List) { // avoid unnecessary proxying via duck typing
            readable = new ListDecorator((List)list);
        }
        else {
            try {
                readable = ReadableList.DUCK.cast(list);
            }
            catch (ClassCastException cce) {
                throw new NodeException(this, "expecting ReadableList-like object");
            }
        }
    }

    /**
     * Returns the number of elements in the list.
     */
    @Override
    public int size() {
        return readable.size();
    }

    /**
     * Returns the element at the specified index in the list, wrapped in a {@link ValueNode}.
     * If the index is out of bounds, then the returned value will contain {@code null}.
     *
     * @param index the index of the element to return.
     * @return a value containing the element at the specified index.
     * @throws IndexOutOfBoundsException if the index is nonsensically a negative value.
     */
    public ValueNode get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return new ValueNode((index < size() ? readable.get(index) : null), path + '[' + index + ']');
    }

    /**
     * Returns an iterator over the elements of the listing.
     */
    @Override
    public Iterator<ValueNode> iterator() {
        return new Iterator<ValueNode>() {
            int cursor = 0;
            public boolean hasNext() {
                return cursor < size();
            }
            public ValueNode next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return get(cursor++);
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
