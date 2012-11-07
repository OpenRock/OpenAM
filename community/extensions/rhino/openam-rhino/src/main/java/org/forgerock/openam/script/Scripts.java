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

// Java Standard Edition
import java.util.ServiceLoader;

// JSON-Fluent
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.fluent.JsonValueException;

// TODO: migrate to OSGi whiteboard pattern
import org.forgerock.openam.script.javascript.JavaScriptFactory;

/**
 * Instantiates script objects using registered script factory implementations.
 *
 * @author Paul C. Bryan
 * @see ScriptFactory
 */
public class Scripts {

    /** TEMPORARY. */
    private static final ScriptFactory JS_FACTORY = new JavaScriptFactory();

    /**
     * Returns a new script object for the provided script configuration object.
     *
     * @param config configuration object for script.
     * @return a new script instance, or {@code null} if {@code config} is {@code null}.
     * @throws JsonValueException if the script configuration object or source is malformed.
     */
    public static Script newInstance(String name, JsonValue config) throws JsonValueException {
        if (config == null || config.isNull()) {
            return null;
        }
        Script script = JS_FACTORY.newInstance(name + config.getPointer().toString(), config); // until OSGi support provided
        if (script != null) {
            return script;
        }
        JsonValue type = config.get("type");
        throw new JsonValueException(type, "script type " + type.asString() + " unsupported"); // no matching factory
    }
}
