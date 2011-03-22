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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

/**
 * Exposes public, non-primitive, non-static field members of an object as a map. As fields
 * cannot be removed from the underlying object, the {@code remove} and {@code clear} methods
 * merely set field values to {@code null}.
 *
 * @author Paul C. Bryan
 */
public class FieldMap implements FullMap<String, Object> {

    /** Cache of field mappings to avoid overhead of repeated mapping via reflection. */
    private static final HashMap<Class, HashMap<String, Field>> MAPPINGS = new HashMap<Class, HashMap<String, Field>>();

    /** The object whose field members are being exposed through the map. */
    private final Object object;

    /** Mapping between the map's keys and the object's fields. */
    private final HashMap<String, Field> fields;

    /**
     * Constructs a new extensible field map, using this object's field members as keys. This
     * is only useful in the case where a class subclasses {@code FieldMap}.
     */
    public FieldMap() {
        this.object = this;
        this.fields = getFields(this);
    }

    /**
     * Constructs a new field map, using the specified object's field members as keys.
     *
     * @param object the object whose field members are to be exposed in the map.
     */
    public FieldMap(Object object) {
        this.object = object;
        this.fields = getFields(object);
    }

    /**
     * Returns the value for the specified field name key.
     */
    @Override
    public Object get(Object key) {
        try {
            return fields.get(key).get(object);
        }
        catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae); // unexpected
        }
        catch (NullPointerException npe) {
            // no such field, yield null result
        }
        return null;
    }

    /**
     * Returns {@code true} if the object contains the specified field name key.
     */
    @Override
    public boolean containsKey(Object key) {
        return fields.containsKey(key);
    }

    /**
     * Returns the number of fields.
     */
    @Override
    public int size() {
        return fields.size();
    }

    /**
     * Returns a {@link Set} of the field name keys.
     */
    @Override
    public Set<String> keySet() {
        return fields.keySet();
    }

    /**
     * Stores the specified value in the field with the specified field name key.
     *
     * @throws UnsupportedOperationException if the specified field name key does not exist.
     */
 
    @Override
    public Object put(String key, Object value) {
        Object old = get(key);
        try {
            fields.get(key).set(object, value);
        }
        catch (Exception e) { // invalid field, invalid type or illegal access
            throw new UnsupportedOperationException(e);
        }
        return old;
    }

    /**
     * Sets the value of the field with the specified field name key to {@code null}.
     *
     * @throws UnsupportedOperationException if the specified field name key does not exist.
     */
    @Override
    public Object remove(Object key) {
        return (key instanceof String ? put((String)key, null) : null);
    }

    /**
     * Sets the values of all fields to {@code null}.
     */
    @Override
    public void clear() {
        for (String key : keySet()) {
            remove(key);
        }
    }

    private static HashMap<String, Field> getFields(Object o) {
        Class c = o.getClass();
        HashMap<String, Field> fields = MAPPINGS.get(c);
        if (fields == null) { // lazy initialization
            fields = new HashMap<String, Field>();
            for (Field f : c.getFields()) {
                int modifiers = f.getModifiers();
                if (!f.isSynthetic() && !Modifier.isStatic(modifiers) && !f.isEnumConstant() && !f.getType().isPrimitive()) {
                    fields.put(f.getName(), f);
                }
            }
            MAPPINGS.put(c, fields);
        }
        return fields;
    }
}
