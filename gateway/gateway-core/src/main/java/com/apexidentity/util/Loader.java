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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Provides methods for dynamically loading classes.
 *
 * @author Paul C. Bryan
 */
public class Loader {

    /** Static methods only. */
    private Loader() {
    }

    /**
     * Returns the class loader that should be used consistently throughout the application.
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Returns the {@code Class} object associated with the class or interface with the given
     * name, or {@code null} if the class could not be returned for any reason.
     *
     * @param name the fully qualified name of the desired class.
     * @return the Class object for the class with the specified name.
     */
    public static Class getClass(String name) {
        try {
            return Class.forName(name, true, getClassLoader());
        }
        catch (Throwable t) {
            return null;
        }
    }

    /**
     * Creates a new instance of a named class. The class is instantiated as if by a
     * {@code new} expression with an empty argument list. If the class cannot be instantiated
     * for any reason, {@code null} is returned.
     *
     * @param name the fully qualified name of the class to instantiate.
     * @return the newly instantiated object, or {@code null} if it could not be instantiated.
     */
    public static Object newInstance(String name) {
        try {
            return getClass(name).newInstance();
        }
        catch (Throwable t) {
            return null;
        }
    }

    /**
     * Creates a new instance of a named class. The class is instantiated as if by a
     * {@code new} expression with an empty argument list. If the class cannot be instantiated
     * for any reason, {@code null} is returned.
     *
     * @param name the fully qualified name of the class to instantiate.
     * @param type the class of the type of object to instantiate.
     * @return the newly instantiated object, or {@code null} if it could not be instantiated.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String name, Class<T> type) {
        Object object = newInstance(name);
        if (object != null && !type.isInstance(object)) {
            object = null;
        }
        return (T)object;
    }

    /**
     * Loads services of a particular type into a map. Such services implement the
     * {@link Indexed} interface to provide a key to index the service by in the map.
     *
     * @param keyType the class type of the key to be indexed in the map.
     * @param serviceType the class type of services to load.
     * @return a map containing the loaded services, indexed by the services' keys.
     */
    public static <K, V extends Indexed<K>> Map<K, V> loadMap(Class<K> keyType, Class<V> serviceType) {
        HashMap<K, V> map = new HashMap<K, V>();
        for (V v : ServiceLoader.load(serviceType, getClassLoader())) {
            map.put(v.getKey(), v);
        }
        return map;
    }

    /**
     * Loads services of a particualr type into a list.
     *
     * @param serviceType the class type of services to load.
     * @return a list containing the loaded services.
     */
    public static <E> List<E> loadList(Class<E> serviceType) {
        ArrayList<E> list = new ArrayList<E>();
        for (E e : ServiceLoader.load(serviceType, getClassLoader())) {
            list.add(e);
        }
        return list;
    }

    /**
     * Finds the resource with the given name.
     *
     * @param name the resource name.
     * @return A {@code URL} object for reading the resource, or {@code null} if the resource could not be found.
     * @see ClassLoader#getResource(java.lang.String)
     */
    public static URL getResource(String name) {
        return getClassLoader().getResource(name);
    }
}
