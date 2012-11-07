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
import java.util.List;
import java.util.Map;

/**
 * Exposes a function that can be provided to a script to invoke.
 *
 * @author Paul C. Bryan
 */
public interface Function {

    /**
     * Invokes the function.
     * <p>
     * Note: The {@code scope} and {@code _this} parameters are not currently enabled.
     * TODO: Add support for these in a future version (it's expensive to copy entire
     * "scope" and "this" object for every function call—they should be wrapped.
     *
     * @param scope scope that is made available to the function ({@code null}).
     * @param _this the object of which the function is a member ({@code null}).
     * @param params the parameters being passed to the function.
     * @return the value that the function returns upon successful invocation.
     * @throws Throwable if an exception occurred during execution of the function.
     */
    Object call(Map<String, Object> scope, Map<String, Object> _this, List<Object> params) throws Throwable;
}
