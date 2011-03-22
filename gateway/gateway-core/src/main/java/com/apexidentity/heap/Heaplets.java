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

package com.apexidentity.heap;

// Java Standard Edition
import java.util.Collections;
import java.util.Map;

// ApexIdentity Core Library
import com.apexidentity.util.Loader;

/**
 * Loads {@link Heaplet} classes based on the class of object they create. Three methods of
 * locating the heaplet class are attempted, in the following order:
 * <ol>
 * <li>The {@link Loader} class attempts to locate a {@code com.apexidentity.heap.Heaplet}
 * interface implementation, where the index key exported by {@link Heaplet#getKey() getKey()}
 * is the class being created.</li>
 * <li>A nested {@code Heaplet} class is searched for. Example: creating
 * {@code com.example.Foo} would search for a heaplet class named
 * {@code com.example.Foo$Heaplet}.</li>
 * <li>A standalone class with the name {@code Heaplet} appended. Example: creating
 * {@code com.example.Foo} would search for a heaplet class named
 * {@code com.example.FooHeaplet}. </li>
 * </ol>
 *
 * @author Paul C. Bryan
 */
public class Heaplets {

    /** Services mapped from class created to heaplet implementation. */
    private static final Map<Class, Heaplet> SERVICES =
     Collections.unmodifiableMap(Loader.loadMap(Class.class, Heaplet.class));

    /** Static methods only. */
    private Heaplets() {
    }

    /**
     * Returns the heaplet that creates an instance of the specified class, or {@code null}
     * if no such heaplet could be found.
     *
     * @param c the class that the heaplet is responsible for creating.
     * @return the heaplet that creates the specified class, or {@code null} if not found.
     */
    public static Heaplet getHeaplet(Class c) {
        Heaplet heaplet = SERVICES.get(c); // try service loader
        if (heaplet == null) {
            heaplet = Loader.newInstance(c.getName() + "$Heaplet", Heaplet.class); // try nested class
        }
        if (heaplet == null) {
            heaplet = Loader.newInstance(c.getName() + "Heaplet", Heaplet.class); // try standalone class
        }
        return heaplet;
    }
}
