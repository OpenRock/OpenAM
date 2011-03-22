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

package com.apexidentity.el;

// Java Standard Edition
import java.beans.FeatureDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;

// Java Enterprise Edition
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

// JUEL
import de.odysseus.el.ExpressionFactoryImpl;

// ApexIdentity Core Library
import com.apexidentity.resolver.Resolver;
import com.apexidentity.resolver.Resolvers;

/**
 * An Unified Expression Language expression. Creating an expression is the equivalent to
 * compiling it. Once created, an expression can be evaluated within a supplied scope. An
 * expression can safely be evaluated concurrently in multiple threads.
 *
 * @author Paul C. Bryan
 */
public class Expression {

    /** The underlying EL expression that this object represents. */
    private final ValueExpression valueExpression;

    /**
     * Constructs an expression for later evaluation.
     *
     * @param expression the expression to parse.
     * @throws ExpressionException if the expression was not syntactically correct.
     */
    public Expression(String expression) throws ExpressionException {
        try {
            valueExpression = new ExpressionFactoryImpl().createValueExpression(new XLContext(null), expression, Object.class);
        }
        catch (ELException ele) {
            throw new ExpressionException(ele);
        }
    }

    /**
     * Evaluates the expression within the specified scope and returns the resulting object, or
     * {@code null} if it does not resolve a value.
     *
     * @param scope the scope to evaluate the expression within.
     * @return the result of the expression evaluation, or {@code null} if does not resolve a value.
     */
    public Object eval(Object scope) {
        try {
            return valueExpression.getValue(new XLContext(scope));
        }
        catch (ELException ele) {
            return null; // unresolved element yields null value
        }
    }

    /**
     * Evaluates the expression within the specified scope and returns the resulting object
     * if it matches the specified type, or {@code null} if it does not resolve or match.
     *
     * @param scope the scope to evaluate the expression within.
     * @param type the type of object the evaluation is expected to yield.
     * @return the result of the expression evaliation, or {@code null} if it does not resolve or match the type.
     */
    @SuppressWarnings("unchecked")
    public <T> T eval(Object scope, Class<T> type) {
        Object value = eval(scope);
        return (value != null && type.isInstance(value) ? (T)value : null);
    }

    /**
     * Sets the result of an evaluated expression to a specified value. The expression is
     * treated as an <em>lvalue</em>, the expression resolves to an object whose value will be
     * set. If the expression does not resolve to an object or cannot otherwise be written to
     * (e.g. read-only), then this method will have no effect.
     *
     * @param scope the scope to evaluate the expression within.
     * @param value the value to set in the result of the expression evaluation.
     */
    public void set(Object scope, Object value) {
        try {
            valueExpression.setValue(new XLContext(scope), value);
        }
        catch (ELException ele) {
            // unresolved elements are simply ignored
        }
    }

    private static class XLContext extends ELContext {
        private final ELResolver elResolver;
        private final FunctionMapper fnMapper = new Functions();
        public XLContext(Object scope) {
            elResolver = new XLResolver(scope);
        }
        @Override public ELResolver getELResolver() {
            return elResolver;
        }
        @Override public FunctionMapper getFunctionMapper() {
            return fnMapper;
        }
        @Override public VariableMapper getVariableMapper() {
            return null;
        }
    }

    private static class XLResolver extends ELResolver {
        private final Object scope;
        public XLResolver(Object scope) {
            this.scope = scope;
        }
        @Override public Object getValue(ELContext context, Object base, Object property) {
            context.setPropertyResolved(true);
            Object value = Resolvers.get((base == null ? scope : base), property);
            return (value != Resolver.UNRESOLVED ? value : null);
        }
        @Override public Class<?> getType(ELContext context, Object base, Object property) {
            context.setPropertyResolved(true);
            return Object.class;
        }
        @Override public void setValue(ELContext context, Object base, Object property, Object value) {
            context.setPropertyResolved(true);
            Resolvers.put((base == null ? scope : base), property, value);
        }
        @Override public boolean isReadOnly(ELContext context, Object base, Object property) {
            context.setPropertyResolved(true);
            return false; // attempts to write to read-only values are merely ignored
        }
        @Override public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
            return null;
        }
        @Override public Class<?> getCommonPropertyType(ELContext context, Object base) {
            return (base == null ? String.class : Object.class);
        }
    }
}
