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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// ApexIdentity Core Library
import com.apexidentity.util.Loader;

/**
 * Performs object resolution by object type. A given object may have more than one resolver,
 * depending on what class it extends and/or interfaces it implements, or what its
 * superclasses and interfaces are.
 *
 * @author Paul C. Bryan
 */
public class Resolvers {

    /** Resolver that handles native arrays (not handled like the service-based resolvers). */
    private static final List<Resolver> ARRAY_RESOLVER = Collections.unmodifiableList(Arrays.asList((Resolver)new ArrayResolver()));

    /** Mapping of supported classes to associated resolvers. */
    public static final Map<Class, Resolver> SERVICES = Collections.unmodifiableMap(Loader.loadMap(Class.class, Resolver.class));

    /** Static methods only. */
    private Resolvers() {
    }

    /**
     * Provides an iterable object over the resolvers that are appropriate for a particular
     * object. Resolvers are provided ordered from most specific to class/interface to
     * least. Resolvers are provided through an iterator interface to avoid the overhead of
     * determining all resolvers in advance.
     *
     * @param object the object for which a set of resolvers is being sought.
     * @return an object that returns an iterator over the set of resolvers for the object.
     */ 
    public static Iterable<Resolver> resolvers(final Object object) {
        return new Iterable<Resolver>() {
            public Iterator<Resolver> iterator() {
                return (object.getClass().isArray() ? ARRAY_RESOLVER.iterator() : new Iterator<Resolver>() {
                    Class class1 = object.getClass();
                    Class class2 = class1;
                    Iterator<Class> interfaces = null;
                    int n = 0;
                    public boolean hasNext() {
                        return (class2 != null); // interface hierarchy not yet exhausted
                    }
                    public Resolver next() {
                        while (class1 != null && class1 != Object.class) { // class hierarchy
                            Resolver resolver = SERVICES.get(class1);
                            class1 = class1.getSuperclass();
                            if (resolver != null) {
                                return resolver;
                            }
                        }
                        class1 = null; // exhausted class hierarchy
                        while (class2 != null && class2 != Object.class) { // interface hierarchy
                            if (interfaces != null && interfaces.hasNext()) {
                                Resolver resolver = SERVICES.get(interfaces.next());
                                if (resolver != null) {
                                    return resolver;
                                }
                            }
                            else {
                                List<Class> list = getInterfaces(class2, n++);
                                if (list.size() > 0) {
                                    interfaces = list.iterator();
                                }
                                else {
                                    class2 = class2.getSuperclass();
                                    n = 0;
                                } 
                            }
                        }
                        class2 = null; // exhausted interface hierarchy
                        return new Unresolver();
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                });
            }
        };
    }

    /**
     * Attempts to resolve an element of an object.
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element to resolve within the specified object.
     * @return the value of the resolved element, or {@link Resolver#UNRESOLVED UNRESOLVED} if it cannot be resolved.
     * @see Resolver#get(Object, Object)
     */
    public static Object get(Object object, Object element) {
        for (Resolver resolver : resolvers(object)) {
            Object value = resolver.get(object, element);
            if (value != Resolver.UNRESOLVED) {
                return value; // first hit wins
            }
        }
        return Resolver.UNRESOLVED;
    }

    /**
     * Attempts to set the value of an element of an object.
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element within the specified object whose value is to be set.
     * @param value the value to set the element to.
     * @return the previous value of the element, {@code null} if no previous value, or {@link Resolver#UNRESOLVED UNRESOLVED} if it cannot be resolved.
     * @see Resolver#put(Object, Object, Object)
     */
    public static Object put(Object object, Object element, Object value) {
        for (Resolver resolver : resolvers(object)) {
            Object resolved = resolver.put(object, element, value);
            if (resolved != Resolver.UNRESOLVED) {
                return resolved; // first hit wins
            }
        }
        return Resolver.UNRESOLVED;
    }

    /**
     * Attempts to remove an element of an object, or clear its value if elements cannot be
     * removed.
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element within the specified object whose value is to be removed or cleared.
     * @return the previous value of the element, {@code null} if no previous value, or {@link Resolver#UNRESOLVED UNRESOLVED} if it cannot be resolved.
     * @see Resolver#remove(Object, Object)
     */
    public static Object remove(Object object, Object element) {
        for (Resolver resolver : resolvers(object)) {
            Object resolved = resolver.remove(object, element);
            if (resolved != Resolver.UNRESOLVED) {
                return resolved; // first hit wins
            }
        }
        return Resolver.UNRESOLVED;
    }

    /**
     * Returns {@code true} if the object contains the specified element.
     *
     * @param object the object in which to resolve the specified element.
     * @param element the element within the specified object being sought.
     * @return the {@code true} if the object contains the specified element.
     * @see Resolver#containsKey(Object, Object)
     */
    public static boolean containsKey(Object object, Object element) {
        for (Resolver resolver : resolvers(object)) {
            if (resolver.containsKey(object, element)) {
                return true; // first hit wins
            }
        }
        return false; // unresolved
    }

    /**
     * Returns a set of elements that the object contains.
     *
     * @param object the object to return the elements for.
     * @return the elements that the object contains, or an empty set if it cannot be resolved.
     * @see Resolver#keySet(Object)
     */
    public static Set<Object> keySet(Object object) {
        HashSet<Object> set = new HashSet<Object>();
        for (Resolver resolver : resolvers(object)) {
            Set<? extends Object> s = resolver.keySet(object);
            if (set != null) { // combine all keys reported by resolvers
                set.addAll(s);
            }
        }
        return set;
    }

    private static List<Class> getInterfaces(Class c, int level) {
        List<Class> interfaces;
        if (level == 0) {
            interfaces = Arrays.asList(c.getInterfaces());
        }
        else {
            interfaces = new ArrayList<Class>();
            for (Class iface : c.getInterfaces()) {
                interfaces.addAll(getInterfaces(iface, level - 1)); // recursion
            }
        }
        return interfaces;
    }
}
