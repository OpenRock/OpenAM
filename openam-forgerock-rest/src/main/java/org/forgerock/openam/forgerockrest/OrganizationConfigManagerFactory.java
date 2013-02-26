package org.forgerock.openam.forgerockrest;

/**
 * Copyright 2013 ForgeRock, Inc.
 * <p/>
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 * <p/>
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 * <p/>
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 */

import com.iplanet.sso.SSOToken;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SMSException;

/**
 * Alin: The intention here is that we are hiding the construction of the OCM behind a factory
 * to ensure that it then represents a single point of entry for the mocking.
 */
public class OrganizationConfigManagerFactory {
    public OrganizationConfigManager getManager(SSOToken token, String path) {
        try {
            return new OrganizationConfigManager(token, path);
        } catch (SMSException e) {
            throw new IllegalStateException(e);
        }
    }
}
