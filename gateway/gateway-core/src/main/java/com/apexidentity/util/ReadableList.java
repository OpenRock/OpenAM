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
import java.util.Iterator;
import java.util.List; // Javadoc

/**
 * A readable list-like interface. It is useful for exposing a subset of {@link List} behavior
 * and duck-typing list-like objects.
 *
 * @author Paul C. Bryan
 */
public interface ReadableList<E> extends Iterable<E> {

    /** Used for duck typing readable lists. */
    public static final DuckType<ReadableList> DUCK = new DuckType<ReadableList>(ReadableList.class);  

    /**
     * Returns the number of elements in the list.
     */
    int size();

    /**
     * Returns an iterator over the elements in the list in proper sequence.
     */
    @Override
    Iterator<E> iterator();

    /**
     * Returns the element at the specified position in the list.
     */
    E get(int index);
}
