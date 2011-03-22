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
 * Copyright 2009 Sun Microsystems Inc. All rights reserved.
 * Portions Copyrighted © 2010–2011 ApexIdentity Inc.
 */

package com.apexidentity.http;

// Java Standard Edition
import java.security.Principal;

// ApexIdentity Core Library
import com.apexidentity.handler.Handler; // Javadoc
import com.apexidentity.util.ExtensibleFieldMap;

/**
 * An HTTP exchange of request and response, and the root object for the exchange object model.
 * The exchange object model parallels the document object model, exposing elements of the
 * exchange. It supports this by exposing its fixed attributes and allowing arbitrary
 * attributes via its {@code ExtensibleFieldMap} superclass.
 * <p>
 * The contract of an exchange is such that it is the responsibility of the caller of a
 * {@link Handler} object to create and populate the request object, and responsibility of the
 * handler to create and populate the response object.
 * <p>
 * If an existing response object exists in the exchange and the handler intends to replace
 * it with another response object, it must first check to see if the existing response
 * object has an entity, and if it does, must call its {@code close} method in order to signal
 * that the processing of the response from a remote server is complete.
 *
 * @author Paul C. Bryan
 */
public class Exchange extends ExtensibleFieldMap {

    /** Self-referential value to make this the root object in the exchange object model. */
    public Exchange exchange = this;

    /** The request portion of the HTTP exchange. */
    public Request request;

    /** The response portion of the HTTP exchange. */
    public Response response;

    /** The principal associated with the request, or {@code null} if unknown. */
    public Principal principal;

    /** Session context associated with the remote client. */
    public Session session;
}
