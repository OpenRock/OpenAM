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
import java.util.Map;

/**
 * Interface for all executable scripts.
 *
 * @author Paul C. Bryan
 */
public interface Script {

    /**
     * Executes the script with the given scope.
     * <p>
     * Existing properties in the given scope can be modified by the script. Any new
     * properties added by the script are transient and are not retained beyond script
     * execution.
     *
     * @param scope the scope to supply to the script.
     * @return the value yielded from the script.
     * @throws ScriptException if an exception occurred during execution of the script.
     */
    Object exec(Map<String, Object> scope) throws ScriptException;
}
