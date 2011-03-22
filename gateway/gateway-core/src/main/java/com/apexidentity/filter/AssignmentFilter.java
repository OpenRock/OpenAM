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

package com.apexidentity.filter;

// Java Standard Edition
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Apexidentity Core Library
import com.apexidentity.el.Expression;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ListNode;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.ValueNode;

/**
 * Conditionally assigns values to expressions before and after the exchange is handled.
 *
 * @author Paul C. Bryan
 */
public class AssignmentFilter extends GenericFilter {

    /** Defines assignment condition, target and value expressions. */
    public static class Binding {
        /** Condition to evaluate to determine if assignment should occur, or {@code null} if assignment is unconditional. */
        public Expression condition;
        /** Expression that yields the target object whose value is to be set. */
        public Expression target;
        /** Expression that yields the value to be set in the target. */
        public Expression value;
    }

    /** Assignment bindings to apply before the request is handled. */
    public final List<Binding> onRequest = new ArrayList<Binding>();

    /** Assignment bindings to apply after the request is handled. */
    public final List<Binding> onResponse = new ArrayList<Binding>();

    /**
     * Filters the request and/or response of an exchange by conditionally assigning values
     * to expressions before and after the exchange is handled.
     */
    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        for (Binding binding : onRequest) {
            eval(binding, exchange);
        }
        chain.handle(exchange);
        for (Binding binding : onResponse) {
            eval(binding, exchange);
        }
        timer.stop();
    }

    private void eval(Binding binding, Exchange exchange) {
        if (binding.condition == null || Boolean.TRUE.equals(binding.condition.eval(exchange))) {
            binding.target.set(exchange, binding.value != null ? binding.value.eval(exchange) : null);
        }
    }

    /** Creates and initializes an assignment filter in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            AssignmentFilter filter = new AssignmentFilter();
            filter.onRequest.addAll(asBindings("onRequest"));
            filter.onResponse.addAll(asBindings("onResponse"));
            return filter;
        }
        private ArrayList<Binding> asBindings(String name) throws ModelException {
            ArrayList<Binding> bindings = new ArrayList<Binding>();
            ListNode node = config.get(name).asListNode(); // optional
            if (node != null) {
                for (Iterator<ValueNode> i = node.iterator(); i.hasNext(); ) {
                    bindings.add(asBinding(i.next().asMapNode()));
                }
            }
            return bindings;
        }
        private Binding asBinding(MapNode node) throws ModelException {
            Binding binding = new Binding();
            binding.condition = node.get("condition").asExpression(); // optional
            binding.target = node.get("target").required().asExpression(); // required
            binding.value = node.get("value").asExpression(); // optional
            return binding;
        }
    }
}
