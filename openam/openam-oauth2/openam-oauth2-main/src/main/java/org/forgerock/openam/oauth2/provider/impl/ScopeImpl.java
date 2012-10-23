/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [2012] [Forgerock Inc]"
 */

package org.forgerock.openam.oauth2.provider.impl;

import org.forgerock.openam.oauth2.model.AccessToken;
import org.forgerock.openam.oauth2.provider.Scope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScopeImpl implements Scope {

    @Override
    public Set<String> scopeToPresentOnAuthorizationPage(Set<String> requestedScope, Set<String> availableScopes, Set<String> defaultScopes){

        if (requestedScope == null){
            return defaultScopes;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    @Override
    public Set<String> scopeRequestedForAccessToken(Set<String> requestedScope, Set<String> availableScopes, Set<String> defaultScopes){

        if (requestedScope == null){
            return defaultScopes;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    @Override
    public Set<String> scopeRequestedForRefreshToken(Set<String> requestedScope,
                                                     Set<String> availableScopes,
                                                     Set<String> allScopes,
                                                     Set<String> defaultScopes){

        if (requestedScope == null){
            return defaultScopes;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    @Override
    public Map<String, Object> retrieveTokenInfoEndPoint(AccessToken token){
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> s = token.getScope();

        if (s != null){
            map.put("scope", s.toString());
        }

        return map;
    }

}
