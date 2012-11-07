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

package org.forgerock.openam.script.javascript;

// Java Standard Edition
import java.util.List;
import java.util.Map;

// OpenIDM
import org.forgerock.openam.script.Function;

/**
 * TODO: Description.
 *
 * @author Paul C. Bryan
 */
public class ScriptableWrapper {

    /**
     * TODO: Description.
     *
     * @param value TODO.
     * @return TODO.
     */
    public static final Object wrap(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Map) {
            return new ScriptableMap((Map)value);
        } else if (value instanceof List) {
            return new ScriptableList((List)value);
        } else if (value instanceof Function) {
            return new ScriptableFunction((Function)value);
        } else {
            return value;
        }
    }
}
