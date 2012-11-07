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
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.openam.script.javascript;

// Java Standard Edition
import java.util.List;

// Mozilla Rhino
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

// OpenIDM
import org.forgerock.openam.script.Function;

/**
 * Provides a {@code Scriptable} wrapper for a {@code List} object.
 *
 * @author Paul C. Bryan
 */
class ScriptableList implements Scriptable, Wrapper {

    /** The list being wrapped. */
    private final List<Object> list;

    /** The parent scope of the object. */
    private Scriptable parent;

    /** The prototype of the object. */
    private Scriptable prototype;

    /**
     * Constructs a new scriptable wrapper around the specified list.
     *
     * @param list the list to be wrapped.
     * @throws NullPointerException if the specified list is {@code null}.
     */
    public ScriptableList(List<Object> list) {
        if (list == null) {
            throw new NullPointerException();
        }
        this.list = list;
    }

    /**
     * Attempts to resize the list to the specified size. If growing the list, "sparse"
     * elements with {@code null} value are added.
     *
     * @param size the required size of the list.
     * @throws org.mozilla.javascript.EvaluatorException if the list could not be resized.
     */
    private void resize(int size) {
        while (list.size() < size) {
            try {
                list.add(null);
            } catch (Exception e) {
                throw Context.reportRuntimeError("list prohibits addition of null elements");
            }
        }
        while (list.size() > size) {
            try {
                list.remove(size);
            } catch (Exception e) {
                throw Context.reportRuntimeError("list prohibits element removal");
            }
        }
    }

    @Override
    public String getClassName() {
        return "ScriptableList";
    }

    @Override
    public Object get(String name, Scriptable start) {
        if ("length".equals(name) || "size".equals(name)) {
            return Integer.valueOf(list.size());
        } else {
            return NOT_FOUND;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object get(int index, Scriptable start) {
        if (index >= 0 && index < list.size()) {
            return ScriptableWrapper.wrap(list.get(index));
        } else {
            return NOT_FOUND;
        }
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return "length".equals(name); // length is only supported property
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return (index >=0 && index < list.size());
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        if ("length".equals(name)) {
            if (!(value instanceof Number)) {
                throw Context.reportRuntimeError("invalid array length");
            }
            int length = ((Number)value).intValue();
            if (length < 0) {
                throw Context.reportRuntimeError("invalid array length");
            }
            resize(length);
        }
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        if (index <= 0) {
            throw Context.reportRuntimeError("index out of bounds");
        }
        if (list.size() < index + 1) {
            resize(index + 1); // "sparsely" allocate null elements if index exceeds size
        }
        value = Converter.convert(value);
        try {
            if (index < list.size()) {
                list.set(index, value);
            } else {
                list.add(value);
            }
        } catch (Exception e) {
            throw Context.reportRuntimeError("list prohibits modification");
        }
    }

    @Override
    public void delete(String name) {
        // attempt to delete any property is silently ignored
    }

    @Override
    public void delete(int index) {
        if (index >= 0 && index < list.size()) {
            try {
                list.set(index, null); // "sparse" allocation; does not remove elements
            } catch (Exception e) {
                throw Context.reportRuntimeError("list prohibits modification");
            }
        }
    }

    @Override
    public Scriptable getPrototype() {
/*        if (prototype == null) { // default if not explicitly set
            return ScriptableObject.getClassPrototype(getParentScope(), "Array");
        } FIXME */
        return prototype;
    }

    @Override
    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    @Override
    public Scriptable getParentScope() {
        return parent;
    }

    @Override
    public void setParentScope(Scriptable parent) {
        this.parent = parent;
    }

    @Override
    public Object[] getIds() {
        Object[] result = new Object[list.size()];
        for (int n = 0; n < result.length; n++) {
            result[n] = Integer.valueOf(n);
        }
        return result;
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        if (hint == null || hint == String.class) {
            return "[object ScriptableList]";
        } else if (hint == Number.class) {
            return Double.NaN;
        } else if (hint == Boolean.class) {
            return Boolean.TRUE;
        } else {
            return this;
        }
    }

    @Override
    public boolean hasInstance(Scriptable instance) {
        return false; // no support for javascript instanceof
    }

    @Override
    public Object unwrap() {
        return list;
    }

    public String toString() {
        return list == null ? "null" : list.toString();
    }
}
