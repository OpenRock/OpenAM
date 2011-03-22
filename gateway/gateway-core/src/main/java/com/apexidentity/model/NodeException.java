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

package com.apexidentity.model;

/**
 * An exception that is thrown during object model node operations.
 *
 * @author Paul C. Bryan
 */
public class NodeException extends ModelException {

    private static final long serialVersionUID = 1L;

    /** The object model node for which the exception was thrown. */
    private final Node node;

    /**
     * Constructs a new exception with the specified object model node and {@code null} as its
     * detail message.
     */
    public NodeException(Node node) {
        this.node = node;
    }

    /**
     * Constructs a new exception with the specified object model node and detail message.
     */
    public NodeException(Node node, String message) {
        super(message);
        this.node = node;
    }
    
    /**
     * Constructs a new exception with the specified object model node and cause.
     */
    public NodeException(Node node, Throwable cause) {
        super(cause);
        this.node = node;
    }

    /**
     * Constructs a new exception with the specified object model node, detail message and
     * cause.
     */
    public NodeException(Node node, String message, Throwable cause) {
        super(message, cause);
        this.node = node;
    }

    /**
     * Returns the detail message string of this exception.
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        String message = super.getMessage();
        if (node != null) {
            sb.append(node.path.toString());
        }
        if (node != null && message != null) {
            sb.append(": ");
        }
        if (message != null) {
            sb.append(message);
        }
        return sb.toString();
    }

    /**
     * Returns the object model node for which the exception was thrown.
     */
    public Node getNode() {
        return node;
    }
}
