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
import com.apexidentity.handler.Handler;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.log.LogLevel; // Javadoc
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ModelException;

/**
 * Catches any exceptions thrown during handing of a request. This allows friendlier error
 * pages to be displayed than would otherwise be displayed by the container. Caught exceptions
 * are logged with a log level of {@link LogLevel#WARNING} and the exchange is diverted to
 * the specified exception handler. 
 * <p>
 * Note: While the response object will be retained in the exchange object, this class will
 * close any open entity within the response object prior to dispatching the exchange to the
 * exception handler.
 *
 * @author Paul C. Bryan
 */
public class ExceptionFilter extends GenericFilter {

    /** Handler to dispatch to in the event of caught exceptions. */
    public Handler handler;

    /**
     * Filters the request and/or response of an exchange by catching any exceptions thrown
     * during handing of a request. 
     */
    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        try {
            chain.handle(exchange);
        }
        catch (Throwable t) {
            logger.warning(t); // user-impacting
            if (exchange.response != null && exchange.response.entity != null) {
                try {
                    exchange.response.entity.close(); // important!
                }
                catch (IOException ioe) {
                    logger.debug(ioe);
                }
            }
            handler.handle(exchange);
        }
        timer.stop();
    }

    /** Creates and initializes an exception filter in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            ExceptionFilter filter = new ExceptionFilter();
            filter.handler = HeapUtil.getRequiredObject(heap, config.get("handler"), Handler.class);
            return filter;
        }
    }
}
