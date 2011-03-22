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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link FieldMap} that can be extended with arbitrary keys. If the key maps to a key
 * exposed by the field map, the field map is used, otherwise the key is handled in this
 * implementation. The backing map is a {@link HashMap} with default initial capacity and
 * load factor. 
 *
 * @author Paul C. Bryan
 */
public class ExtensibleFieldMap extends FieldMap {

    /** Map to store extended keys. */
    private final HashMap<String, Object> extension = new HashMap<String, Object>();

    /**
     * Constructs a new extensible field map, using this object's field members as keys. This
     * is only useful in the case where a class subclasses {@code ExtensibleFieldMap}.
     */
    public ExtensibleFieldMap() {
        super();
    }

    /**
     * Constructs a new extensible field map, using the specified object's field members as
     * keys.
     *
     * @param object the object whose field members are to be exposed in the map.
     */
    public ExtensibleFieldMap(Object object) {
        super(object);
    }

    @Override
    public Object get(Object key) {
        return (super.containsKey(key) ? super.get(key) : extension.get(key));
    }

    @Override
    public boolean containsKey(Object key) {
        return (super.containsKey(key) || extension.containsKey(key));
    }

    @Override
    public int size() {
        return super.size() + extension.size();
    }

    @Override
    public Set<String> keySet() {
        HashSet<String> keys = new HashSet<String>(size());
        keys.addAll(super.keySet());
        keys.addAll(extension.keySet());
        return keys;
    }

    @Override
    public Object put(String key, Object value) {
        return (super.containsKey(key) ? super.put(key, value) : extension.put(key, value));
    }

    @Override
    public Object remove(Object key) {
        return (super.containsKey(key) ? super.remove(key) : extension.remove(key));
    }

    @Override
    public void clear() {
        super.clear();
        extension.clear();
    }
}
