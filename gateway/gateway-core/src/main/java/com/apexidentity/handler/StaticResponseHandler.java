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

package com.apexidentity.handler;

// Java Standard Edition
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.el.Expression;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.HTTPUtil;
import com.apexidentity.http.Response;
import com.apexidentity.io.BranchingStreamWrapper;
import com.apexidentity.io.TemporaryStorage;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.NodeException;
import com.apexidentity.model.ValueNode;
import com.apexidentity.util.CaseInsensitiveMap;
import com.apexidentity.util.Loader;
import com.apexidentity.util.MultiValueMap;

/**
 * Creates a static response in an HTTP exchange.
 *
 * @author Paul C. Bryan
 */
public class StaticResponseHandler extends GenericHandler {

    /** The response status code (e.g.&nbsp200). */
    public Integer status;

    /** The response status reason (e.g.&nbsp"OK"). */
    public String reason;

    /** Protocol version (e.g.&nbsp{@code "HTTP/1.1"}. */
    public String version = null;

    /** Message header fields whose values are expressions that are evaluated. */
    public final MultiValueMap<String, Expression> headers =
     new MultiValueMap<String, Expression>(new CaseInsensitiveMap<List<Expression>>());

    /** The message entity. */
    public String entity = null;

    /**
     * Handles an HTTP the exchange by creating a static response. 
     */
    @Override
    public void handle(Exchange exchange) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        Response response = new Response();
        response.status = this.status;
        response.reason = this.reason;
        if (response.reason == null) { // not explicit, derive from status
            response.reason = HTTPUtil.getReason(response.status);
        }
        if (response.reason == null) { // couldn't derive from status; say something
            response.reason = "Uncertain";
        }
        if (this.version != null) { // default in Message class
            response.version = this.version;
        }
        for (String key : this.headers.keySet()) {
            for (Expression expression : this.headers.get(key)) {
                String eval = expression.eval(exchange, String.class);
                if (eval != null) {
                    response.headers.add(key, eval);
                }
            }
        }
        if (this.entity != null) {
            HTTPUtil.toEntity(response, entity, null); // use content-type charset (or default)
        }
        exchange.response = response; // finally replace response in the exchange
        timer.stop();
    }

    /** Creates and initializes a static response handler in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            StaticResponseHandler handler = new StaticResponseHandler();
            handler.status = config.get("status").required().asInteger(); // required
            handler.reason = config.get("reason").asString(); // optional
            handler.version = config.get("version").asString(); // optional
            MapNode headers = config.get("headers").asMapNode(); // optional
            if (headers != null) {
                for (String key : headers.keySet()) {
                    for (ValueNode value : headers.get(key).required().asListNode()) {
                        handler.headers.add(key, value.required().asExpression());
                    }
                }
            }
            handler.entity = config.get("entity").asString(); // optional
            return handler;
        }
    }
}
