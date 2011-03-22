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
import java.util.Collections;
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.Headers;
import com.apexidentity.http.Message;
import com.apexidentity.http.MessageType;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.util.CaseInsensitiveSet;

/**
 * Removes headers from and adds headers to a message.
 *
 * @author Paul C. Bryan
 */
public class HeaderFilter extends GenericFilter {

    /** Indicates the type of message in the exchange to filter headers for. */
    MessageType messageType;

    /** The names of header fields to remove from the message. */
    public final CaseInsensitiveSet remove = new CaseInsensitiveSet();

    /** Header fields to add to the message. */
    public final Headers add = new Headers();

    /**
     * Removes all specified headers, then adds all specified headers.
     *
     * @param message the message to remove headers from and add headers to.
     */
    private void process(Message message) {
        for (String s : this.remove) {
            message.headers.remove(s);
        }
        message.headers.addAll(this.add);
    }

    /**
     * Filters the request and/or response of an exchange by removing headers from and adding
     * headers to a message.
     */
    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        if (messageType == MessageType.REQUEST) {
            process(exchange.request);
        }
        chain.handle(exchange);
        if (messageType == MessageType.RESPONSE) {
            process(exchange.response);
        }
        timer.stop();
    }

    /** Creates and initializes a header filter in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            HeaderFilter filter = new HeaderFilter();
            filter.messageType = config.get("messageType").required().asEnum(MessageType.class);
            filter.remove.addAll(config.get("remove").defaultTo(Collections.emptyList()).asList(String.class));
            MapNode add = config.get("add").defaultTo(Collections.emptyMap()).asMapNode();
            for (String key : add.keySet()) {
                List<String> values = add.get(key).required().asList(String.class);
                filter.add.addAll(key, values);
            }
            return filter;
        }
    }
}
