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
 * Copyright © 2012 ForgeRock. All rights reserved.
 */

package org.forgerock.openam.oauth2.model.impl;

import org.forgerock.restlet.ext.oauth2.model.SessionClient;

/**
 * Created by IntelliJ IDEA.
 * User: jonathan
 * Date: 26/3/12
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class SessionClientImpl implements SessionClient {

    // TODO clean up cases in interface
    private String clientId;
    private String redirectUri;
    
    public SessionClientImpl(String clientId, String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }
    
    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }
}
