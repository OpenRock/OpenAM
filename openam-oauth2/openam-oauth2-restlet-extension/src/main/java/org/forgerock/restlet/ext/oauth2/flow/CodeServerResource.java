/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock US Inc. All Rights Reserved
 *
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
 * information:
 *
 * "Portions copyright [year] [name of copyright owner]".
 *
 */

package org.forgerock.restlet.ext.oauth2.flow;

import org.forgerock.openam.ext.cts.repo.DefaultOAuthTokenStoreImpl;
import org.forgerock.openam.oauth2.model.CoreToken;
import org.forgerock.openam.oauth2.model.SessionClientImpl;
import org.forgerock.openam.oauth2.provider.ResponseType;
import org.forgerock.openam.oauth2.utils.OAuth2Utils;

import java.util.Map;

public class CodeServerResource implements ResponseType {

    public CoreToken createToken(Map<String, String> data){
        DefaultOAuthTokenStoreImpl store = new DefaultOAuthTokenStoreImpl();
        return store.createAuthorizationCode(OAuth2Utils.stringToSet(data.get("scope")),
                data.get("realm"),
                data.get("userName"),
                new SessionClientImpl(data.get("clientID"), data.get("redirectURI")));

    }

    public String getReturnLocation(){
        return "QUERY";
    }

    public String URIParamValue(){
        return "code";
    }
}
