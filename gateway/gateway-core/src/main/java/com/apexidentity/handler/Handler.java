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

package com.apexidentity.handler;

// Java Standard Edition
import java.io.IOException;

// ApexIdentity Core Library
import com.apexidentity.http.Exchange;

/**
 * Handles an HTTP exchange request by producing an associated response.
 *
 * @author Paul C. Bryan
 */
public interface Handler {

    /**
     * Called to request the handler respond to the request.
     * <p>
     * A handler that doesn't hand-off an exchange to another handler downstream is
     * responsible for creating the response in the exchange object.
     * <p>
     * <strong>Important note:</strong> If an existing response exists in the exchange object
     * and the handler intends to replace it with its own, it must first check to see if the
     * existing response has an entity, and if it does, must call its {@code close} method in
     * order to signal that the processing of the response from a remote server is complete.
     *
     * @param exchange the exchange containing the request to handle.
     * @throws HandlerException if an exception occurs that prevents handling of the request.
     * @throws IOException if an I/O exception occurs.
     */
    void handle(Exchange exchange) throws HandlerException, IOException;
}
