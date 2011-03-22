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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.IdentityHashMap;

/**
 * Provides a bridge between Java static typing and dynamic typing of objects. The interface
 * supplied is the "duck", for which objects can be compared to determine if they contain the
 * same methods as the duck interface. If so, these objects can be cast (by way of Java
 * reflection proxy) to the interface, and accessed using standard Java static type
 * conventions.
 *
 * @author Paul C. Bryan
 */
public final class DuckType<T> {

    /** Caches successful duck typing matches. */
    private static final IdentityHashMap<Class, IdentityHashMap<Class, HashMap<Method, Method>>>
     duckBirdMethodMap = new IdentityHashMap<Class, IdentityHashMap<Class, HashMap<Method, Method>>>();

    /** Caches unsuccessful duck typing matches. */
    private static final IdentityHashMap<Class, IdentityHashMap<Class, ClassCastException>>
     duckBirdExceptionMap = new IdentityHashMap<Class, IdentityHashMap<Class, ClassCastException>>();

    /** The class representing the interface of the duck to be duck typed. */
    private Class<T> duck;

    /**
     * Constructs a new duck typing object for which to test and cast instantiated objects.
     *
     * @param duck the class representing the interface of the duck typed.
     * @throws IllegalArgumentException if the specified class is not an interface.  
     */
    public DuckType(Class<T> duck) {
        if (!duck.isInterface()) {
            throw new IllegalArgumentException("duck type must be an interface");
        }
        this.duck = duck;
    }

    /**
     * Returns a mapping of methods in the duck type to methods in the object. All positive
     * and negative duck typing is cached such that new mappings need not be attempted for
     * previously attempted duck typing. No synchronization is used, which may result in
     * harmless redundant initialization, but results in higher performance overall.
     *
     * @param duck the interface to duck type the object to.
     * @param object the object to duck type to the interface.
     */ 
    private static HashMap<Method, Method> map(Class duck, Object object) throws ClassCastException {
        Class<? extends Object> bird = object.getClass();
        IdentityHashMap<Class, HashMap<Method, Method>> birdMethodMap = duckBirdMethodMap.get(duck);
        if (birdMethodMap == null) { // safely unsynchronized
            duckBirdMethodMap.put(duck, birdMethodMap = new IdentityHashMap<Class, HashMap<Method, Method>>());
        }
        HashMap<Method, Method> methodMap = birdMethodMap.get(bird);
        if (methodMap == null) { // safely unsynchronized
            IdentityHashMap<Class, ClassCastException> birdExceptionMap = duckBirdExceptionMap.get(duck);
            if (birdExceptionMap == null) { // safely unsynchronized
                duckBirdExceptionMap.put(duck, birdExceptionMap = new IdentityHashMap<Class, ClassCastException>());
            }
            if (birdExceptionMap.containsKey(bird)) {
                throw birdExceptionMap.get(bird);
            }
            methodMap = new HashMap<Method, Method>();
            for (Method duckMethod : duck.getMethods()) {
                try {
                    Method birdMethod = bird.getMethod(duckMethod.getName(), duckMethod.getParameterTypes());
                    if (!birdMethod.getReturnType().isAssignableFrom(duckMethod.getReturnType())) {
                        throw new NoSuchMethodException();
                    }
                    methodMap.put(duckMethod, birdMethod);
                }
                catch (NoSuchMethodException nsme) {
                    ClassCastException cce = new ClassCastException(bird.getName() + " cannot be duck typed as " + duck.getName());
                    birdExceptionMap.put(bird, cce); // cache mismatch for future calls
                    throw cce;
                }
            }
            birdMethodMap.put(bird, methodMap); // cache match for future calls
        }
        return methodMap;
    }

    /**
     * Determines if the specified object can be cast as the interface represented by this
     * class.
     *
     * @param object the object to check.
     * @return {@code true} if object can be duck typed as the interface represented by this class.
     */
    public boolean isInstance(Object object) {
        if (object == null) {
            return false;
        }
        if (duck.isInstance(object)) {
            return true; // literally a duck!
        }
        try {
            map(duck, object);
            return true; // has all of the behavior of a duck
        }
        catch (ClassCastException cce) {
            return false; // does not quack like a duck
        }
    }

    /**
     * Casts the object to the interface represented by this class, proxying as necessary.
     *
     * @param object the object to be duck typed.
     * @return an object that has the specified duck type.
     * @throws ClassCastException if the object cannot be duck typed to the specified interface.
     */
    @SuppressWarnings("unchecked")
    public T cast(final Object object) throws ClassCastException {
        if (object == null) {
            return null;
        }
        if (duck.isInstance(object)) { // already a duck! no proxy necessary
            return (T)object;
        }
        final HashMap<Method, Method> map = map(duck, object);
        return (T)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
         new Class[] { duck }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return map.get(method).invoke(object, args);
            }
        });
    }
}
