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
import java.util.LinkedList;

// ApexIdentity Core Library
import com.apexidentity.handler.GenericHandler;
import com.apexidentity.handler.Handler;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.ValueNode;

/**
 * A chain of exchange zero or more filters and one handler. The chain is responsible for
 * dispatching the exchange to each filter in the chain, and finally the handler.
 * <p>
 * When a chain dispatches an exchange to a filter, it creates a "subchain" (a subset of this
 * chain, which contains the remaining downstream filters and handler), and passes it as a
 * parameter to the filter. For this reason, a filter should make no assumptions or
 * correlations using the chain it is supplied with when invoked.
 * <p>
 * A filter may elect to terminate dispatching of the exchange to the rest of the chain by not
 * calling {@code chain.handle(exchange)} and generate its own response or dispatch to a
 * completely different handler.
 *
 * @author Paul C. Bryan
 * @see Filter
 */
public class Chain extends GenericHandler {

    /** A list of filters, in the order they are to be dispatched by the chain. */
    public final LinkedList<Filter> filters = new LinkedList<Filter>();

    /** The handler dispatch the exchange to; terminus of the chain. */
    public Handler handler;

    @Override
    public void handle(Exchange exchange) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        if (filters.size() > 0) {
            Chain chain = new Chain();
            chain.filters.addAll(filters);
            chain.handler = handler;
            chain.filters.pop().filter(exchange, chain);
        }
        else {
            handler.handle(exchange);
        }
        timer.stop();
    }

    /** Creates and initializes a filter chain in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            Chain chain = new Chain();
            for (ValueNode filter : config.get("filters").required().asListNode()) {
                chain.filters.add(HeapUtil.getRequiredObject(heap, filter, Filter.class));
            }
            chain.handler = HeapUtil.getRequiredObject(heap, config.get("handler"), Handler.class);
            return chain;
        }
    }
}
