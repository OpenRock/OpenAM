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

package com.apexidentity.resolver;

// Java Standard Edition
import java.util.Set;

// ApexIdentity Core Library
import com.apexidentity.util.Indexed;

/**
 * Exposes an object's elements for access through dynamic expressions and scripts.
 *
 * @author Paul C. Bryan
 */
public interface Resolver extends Indexed<Class> {

    /** Singleton that is returned to indicate an element is not resolved by a resolver. */
    public static final Object UNRESOLVED = new Object() {
        @Override public String toString() {
            return null;
        }
    };

    /**
     * Returns the type of object that the resolver supports. This does not necessarily
     * guarantee that the resolver will provide resolution; rather this is how a resolver
     * specifies what type of object it may resolve. Resolvers for more specific classes
     * and interfaces are called earlier than those of more general classes and interfaces.
     */ 
    @Override
    Class getKey();

    /**
     * Attempts to resolve an element of an object. The {@code object} argument references an
     * object for which a named or indexed element is being requested. The {@code element}
     * argument specifies the element that is being requested from the referenced object.
     * <p>
     * The {@code element} argument can be either a {@link String} or an {@link Integer}
     * object. A string represents a named element of an associative array; an integer
     * represents the index of an ordered array.
     * <p>
     * If the resolver cannot resolve the requested element, then {@link #UNRESOLVED} should
     * be returned. This allows other resovlers of more generic classes or interfaces to
     * potentially resolve the requested element. 
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element to resolve within the specified object.
     * @return the value of the resolved element, or {@link #UNRESOLVED} if it cannot be resolved.
     */
    Object get(Object object, Object element);

    /**
     * Attempts to set the value of an element of an object. The {@code object} argument
     * references an object for which a named or indexed element is to be set. The
     * {@code element} argument specifies which element value is to be set. The {@code value}
     * argument specifies the value to be set.
     * <p>
     * The {@code element} argument can be either a {@link String} or an {@link Integer}
     * object. A string represents a named element of an associative array; an integer
     * represents the index of an ordered array.
     * <p>
     * If the resolver cannot resolve the requested element or set its value, then
     * {@link #UNRESOLVED} should be returned. This allows other resovlers of more generic
     * classes or interfaces to potentially resolve the requested element. 
     *  
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element within the specified object whose value is to be set.
     * @param value the value to set the element to.
     * @return the previous value of the element, {@code null} if no previous value, or {@link #UNRESOLVED} if it cannot be resolved.
     */
    Object put(Object object, Object element, Object value);

    /**
     * Attempts to remove an element of an object, or clear its value if elements cannot be
     * removed. The {@code object} argument references an object for which a named or indexed
     * element is to be removed. The {@code element} argument specifies which element 
     * should be removed.
     * <p>
     * The {@code element} argument can be either a {@link String} or an {@link Integer}
     * object. A string represents a named element of an associative array; an integer
     * represents the index of an ordered array.
     * <p>
     * If the resolver cannot resolve the requested element or remove/clear its value, then
     * {@link #UNRESOLVED} should be returned. This allows other resovlers of more generic
     * classes or interfaces to potentially resolve the requested element.
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element within the specified object whose value is to be removed or cleared.
     * @return the previous value of the element, {@code null} if no previous value, or {@link #UNRESOLVED} if it cannot be resolved.
     */
    Object remove(Object object, Object element);

    /**
     * Returns {@code true} if the object contains the specified element. The {@code object}
     * argument references an object for which a named or indexed element is being sought.
     * The {@code element} argument specifies which element is being sought.
     * <p>
     * The {@code element} argument can be either a {@link String} or an {@link Integer}
     * object. A string represents a named element of an associative array; an integer
     * represents the index of an ordered array.
     * <p>
     * If the resolver cannot resolve the requested element, then {@code false} should be
     * returned. This allows other resovlers of more generic classes or interfaces to
     * potentially resolve the requested element.
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element within the specified object being sought.
     * @return the {@code true} if the object contains the specified element.
     */
    boolean containsKey(Object object, Object element);

    /**
     * Returns a set of elements that the object contains.
     *
     * @param object the object to return the elements for.
     * @return the elements that the object contains, or an empty set if it cannot be resolved.
     */
    Set<?> keySet(Object object);
}
