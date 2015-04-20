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
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */
package org.forgerock.openam.entitlement;

import com.sun.identity.shared.debug.Debug;

import javax.security.auth.Subject;

/**
 *  Simple class to be a holder for shared policy constants.
 *
 * @since 13.0.0
 */
public final class PolicyConstants {

    public static final Debug DEBUG = Debug.getInstance("Entitlement");
    public static final Subject SUPER_ADMIN_SUBJECT = new Subject();

    private PolicyConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

}
