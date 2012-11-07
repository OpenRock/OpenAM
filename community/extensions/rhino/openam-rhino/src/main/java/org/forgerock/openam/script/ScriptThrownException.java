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
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.openam.script;

/**
 * An exception that is thrown to indicate that an executed script encountered an exception.
 *
 * @author Paul C. Bryan
 */
public class ScriptThrownException extends ScriptException {

    /** Serializable class a version number. */
    static final long serialVersionUID = 1L;

    /** Value that was thrown by the script. */
    private Object value;

    /**
     * Constructs a new exception with the specified value and {@code null} as its detail
     * message.
     */
    public ScriptThrownException(Object value) {
        this.value = value;
    }

    /**
     * Constructs a new exception with the specified value and detail message.
     */
    public ScriptThrownException(Object value, String message) {
        super(message);
        this.value = value;
    }
    
    /**
     * Constructs a new exception with the specified value and cause.
     */
    public ScriptThrownException(Object value, Throwable cause) {
        super(cause);
        this.value = value;
    }

    /**
     * Constructs a new exception with the specified value, detail message and cause.
     */
    public ScriptThrownException(Object value, String message, Throwable cause) {
        super(message, cause);
        this.value = value;
    }

    /**
     * Returns the value that was thrown from the script.
     */
    public Object getValue() {
        return value;
    }
}
