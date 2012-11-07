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

// JSON Fluent
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.fluent.JsonValueException;

/**
 * Instantiates a script object, based-on the supplied configuration value. Each scripting
 * language implementation should implement this interface and add its class name to the
 * {@code META-INF/services/org.forgerock.openidm.script.ScriptFactory} file.
 *
 * @author Paul C. Bryan
 */
public interface ScriptFactory {

    /**
     * Returns a new script object for the provided script configuration value. If the
     * factory does not match the configuration (e.g. different {@code "type"} property,
     * then {@code null} is returned.
     * <p>
     * The configuration value must contain a {@code Map}, and have a {@code "type"} string
     * member, which contains the media type of the script (e.g. {@code "text/javascript"}).
     * Implementations of script factories are free to define any other properties in the
     * configuration value.
     *
     * @param name unique name of the script. Value MUST be in rfc2396
     * Uniform Resource Identifiers (URI) compliant format.
     * @param config the configuration value for the script; must contain a {@code Map}.
     * @return a new script instance, or {@code null} if the factory could not create it.
     * @throws JsonValueException if the configuration object or script is malformed.
     */
    Script newInstance(String name, JsonValue config) throws JsonValueException;
}
