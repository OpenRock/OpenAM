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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Mozilla Rhino
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.WrappedException;

// OpenIDM
import org.forgerock.openam.script.Function;

/**
 * Provides a Rhino {@code Function} wrapper for an OpenIDM {@code Function} object.
 *
 * @author Paul C. Bryan
 */
class ScriptableFunction implements org.mozilla.javascript.Function, Wrapper {

    /** The parent scope of the object. */
    private Scriptable parent;

    /** The prototype of the object. */
    private Scriptable prototype;

    /** TODO: Description. */
    private Function function;

    /**
     * TODO: Description.
     *
     * @param function TODO.
     */
    public ScriptableFunction(Function function) {
        this.function = function;
    }

    /**
     * TODO: Description.
     *
     * @param args TODO.
     * @return TODO.
     */
    private List<Object> convert(Object[] args) {
        ArrayList<Object> list = new ArrayList<Object>();
        for (Object object : args) {
            list.add(Converter.convert(object));
        }
        return list;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        try {
// TODO: Replace Converter w. something that dynamically exposes scope and thisObj values as MLNBN primitives, and avoids copying these entire structures.
            return ScriptableWrapper.wrap(function.call(null, null, convert(args)));
        } catch (Throwable throwable) {
            throw new WrappedException(throwable);
        }
    }

    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] args) {
        throw Context.reportRuntimeError("functions may not be used as constructors");
    }

    @Override
    public String getClassName() {
        return "ScriptableFunction";
    }

    @Override
    public Object get(String name, Scriptable start) {
        return NOT_FOUND;
    }

    @Override
    public Object get(int index, Scriptable start) {
        return NOT_FOUND;
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return false;
    }

    @Override
    public boolean has(int index, Scriptable start) {
        return false;
    }
    
    @Override
    public void put(String name, Scriptable start, Object value) {
        throw Context.reportRuntimeError("function prohibits modification");
    }

    @Override
    public void put(int index, Scriptable start, Object value) {
        throw Context.reportRuntimeError("function prohibits modification");
    }

    @Override
    public void delete(String name) {
        throw Context.reportRuntimeError("function prohibits modification");
    }

    @Override
    public void delete(int index) {
        throw Context.reportRuntimeError("function prohibits modification");
    }

    @Override
    public Scriptable getPrototype() {
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
        return new Object[0];
    }

    @Override
    public Object getDefaultValue(Class<?> hint) {
        return this;
    }

    @Override
    public boolean hasInstance(Scriptable instance) {
        return false;
    }

    @Override
    public Object unwrap() {
        return function;
    }
}
