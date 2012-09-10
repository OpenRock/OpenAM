/*
 * Copyright (c) 2012 ForgeRock AS. All rights reserved.
 *
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
 * information: "Portions Copyrighted [2012] [ForgeRock Inc]".
 *
 */
package org.forgerock.openam.oauth2.rest;

import static org.forgerock.json.resource.Context.newRootContext;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import javax.servlet.ServletException;


import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.Connection;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.Connections;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.Requests;
import org.forgerock.json.resource.exception.ResourceException;
import org.forgerock.json.resource.provider.RequestHandler;
import org.forgerock.json.resource.provider.Router;
import org.forgerock.json.resource.provider.UriTemplateRoutingStrategy;


public class RestTokenDispathcer {

    public RestTokenDispathcer(){

    }

    public static ConnectionFactory getConnectionFactory() throws ServletException {
        try {
            final UriTemplateRoutingStrategy routes = new UriTemplateRoutingStrategy();
            routes.register("/tokens", new TokensResource());
            routes.register("/token", new TokenResource());
            final RequestHandler handler = new Router(routes);
            final ConnectionFactory factory = Connections.newInternalConnectionFactory(handler);
            return factory;
        } catch (final Exception e) {
            throw new ServletException(e);
        }
    }
}
