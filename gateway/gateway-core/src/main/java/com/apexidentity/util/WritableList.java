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
import java.util.List; // Javadoc

/**
 * A writable list-like interface. It is useful for exposing a subset of {@link List} behavior
 * and duck-typing list-like objects.
 *
 * @author Paul C. Bryan
 */
public interface WritableList<E> {

    /** Used for duck typing writable lists. */
    public static final DuckType<WritableList> DUCK = new DuckType<WritableList>(WritableList.class);  

    /**
     * Appends the specified element to the end of the list.
     *
     * @return {@code true}.
     */
    boolean add(E e);

    /**
     * Removes all of the elements from the list.
     */ 
    void clear();

    /**
     * Replaces the element at the specified position in the list with the specified element.
     *
     * @param index index of the element to replace.
     * @param e the element to be stored at the specified position.
     * @return the element previously at the specified position.
     */
    E set(int index, E e);

    /**
     * Removes the element at the specified position in the list.
     *
     * @param index the index of the element to be removed.
     * @return the element previously at the specified position.
     */
    E remove(int index);
}
