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

// ApexIdentity Core Library
import com.apexidentity.el.Expression;
import com.apexidentity.handler.Handler;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ListNode;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.ValueNode;

/**
 * Conditionally diverts the exchange to another handler. Before and after the exchange is
 * handled, associated conditions are evaluated. If a condition evalutes to {@code true}, then
 * the exchange flow is diverted to the associated handler. If no condition evaluates to
 * {@code true}, then the exchange flows normally through the filter. 
 *
 * @author Paul C. Bryan
 */
public class SwitchFilter extends GenericFilter {

    /** Associates a condition with a handler to divert to if the condition yields {@code true}. */
    public static class Case {
        /** Condition to evaluate if exchange should be diverted to handler. */
        public Expression condition;
        /** Handler to divert to if condition yields {@code true}. */
        public Handler handler;
    }

    /** Switch cases to test before the exchange is handled. */
    public final List<Case> onRequest = new ArrayList<Case>(); 

    /** Switch cases to test after the exchange is handled. */
    public final List<Case> onResponse = new ArrayList<Case>(); 

    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        if (!doSwitch(exchange, onRequest)) { // not intercepted
            chain.handle(exchange);
            doSwitch(exchange, onResponse);
        }
        timer.stop();
    }

    private boolean doSwitch(Exchange exchange, List<Case> cases) throws HandlerException, IOException {
        for (Case c : cases) {
            Object o = (c.condition != null ? c.condition.eval(exchange) : Boolean.TRUE);
            if (o instanceof Boolean && ((Boolean)o)) {
                c.handler.handle(exchange);
                return true; // switched flow
            }
        }
        return false; // no interception
    }

    /** Creates and initializes an expect filter in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            SwitchFilter filter = new SwitchFilter();
            filter.onRequest.addAll(asCases("onRequest"));
            filter.onResponse.addAll(asCases("onResponse"));
            return filter;
        }
        private List<Case> asCases(String name) throws HeapException, ModelException {
            ArrayList<Case> cases = new ArrayList<Case>();
            ListNode node = config.get(name).asListNode(); // optional
            if (node != null) {
                for (Iterator<ValueNode> i = node.iterator(); i.hasNext(); ) {
                    cases.add(asCase(i.next().asMapNode()));
                }
            }
            return cases;
        }
        private Case asCase(MapNode node) throws HeapException, ModelException {
            Case c = new Case();
            c.condition = node.get("condition").asExpression(); // optional
            c.handler = HeapUtil.getRequiredObject(heap, node.get("handler"), Handler.class);
            return c;
        }
    }
}
