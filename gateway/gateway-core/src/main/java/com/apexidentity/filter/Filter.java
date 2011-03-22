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

// ApexIdentity Core Library
import com.apexidentity.handler.HandlerException;
import com.apexidentity.http.Exchange;

/**
 * Filters the request and/or response of an HTTP exchange.
 *
 * @author Paul C. Bryan
 * @see Chain
 */
public interface Filter {

    /**
     * Filters the request and/or response of an exchange. Initially, {@code exchange.request}
     * contains the request to be filtered. To pass the request to the next filter or handler
     * in the chain, the filter calls {@code chain.handle(exchange)}. After this call,
     * {@code exchange.response} contains the response that can be filtered.
     * <p>
     * This method may elect not to pass the request to the next filter or handler, and instead
     * handle the request itself. It can achieve this by merely avoiding a call to
     * {@code chain.handle(exchange)} and creating its own response object the exchange. The
     * filter is also at liberty to replace a response with another of its own after the call
     * to {@code chain.handle(exchange)}.
     * <p>
     * A filter can exist in more than one chain, therefore should make no assumptions or
     * correlations using the chain it is supplied. The only valid use of a chain by a filter
     * is to call {@code chain.handle(exchange)} if it elects to dispatch the exchange to
     * the rest of the chain.
     * <p>
     * <strong>Important note:</strong> If an existing response exists in the exchange object
     * and the filter intends to replace it with its own, it must first check to see if the
     * existing response has an entity, and if it does, must call its {@code close} method in
     * order to signal that the processing of the response from a remote server is complete.
     *
     * @param exchange the exchange containing the request and response to filter.
     * @param chain the chain which the filter is a part of, and which handles the exchange.
     * @throws HandlerException if an exception occurred handling the exchange.
     * @throws IOException if an I/O exception occurred.
     */
    void filter(Exchange exchange, Chain chain) throws HandlerException, IOException;
}
