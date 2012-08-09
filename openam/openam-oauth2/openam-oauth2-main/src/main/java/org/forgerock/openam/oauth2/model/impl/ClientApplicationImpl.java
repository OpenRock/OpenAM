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

package org.forgerock.openam.oauth2.model.impl;

import com.sun.identity.idm.AMIdentity;
import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.forgerock.restlet.ext.oauth2.model.ClientApplication;
import org.restlet.data.Status;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;


public class ClientApplicationImpl implements ClientApplication{
    private static final String CLIENT_TYPE = "com.forgerock.openam.oauth2provider.clientType";
    private static final String REDIRECTION_URIS = "com.forgerock.openam.oauth2provider.redirectionURIs";
    private static final String SCOPES = "com.forgerock.openam.oauth2provider.scopes";
    private static final String NAME = "com.forgerock.openam.oauth2provider.name";
    private static final String DESCRIPTION = "com.forgerock.openam.oauth2provider.description";

    AMIdentity id = null;

    public ClientApplicationImpl(AMIdentity id) {
        this.id = id;
    }

    @Override
    public String getClientId(){
        return id.getName();
    }

    @Override
    public ClientType getClientType(){
        ClientType clientType = null;
        try {
            Set<String> clientTypeSet = id.getAttribute(CLIENT_TYPE);
            if (clientTypeSet.iterator().next().equalsIgnoreCase("CONFIDENTIAL")){
                clientType = ClientType.CONFIDENTIAL;
            } else {
                clientType = ClientType.PUBLIC;
            }
        } catch (Exception e){
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
        return clientType;
    }

    @Override
    public Set<URI> getRedirectionURIs(){
        Set<URI> redirectionURIs = null;
        try {
            Set<String> redirectionURIsSet = id.getAttribute(REDIRECTION_URIS);
            redirectionURIs = new HashSet<URI>();
            for (String uri : redirectionURIsSet){
                redirectionURIs.add(URI.create(uri));
            }
        } catch (Exception e){
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
        return redirectionURIs;
    }

    @Override
    public String getAccessTokenType(){
        return OAuth2.Bearer.BEARER;
    }

    @Override
    public String getClientAuthenticationSchema(){
        return null;
    }

    @Override
    public Set<String> allowedGrantScopes(){
        Set<String> scopes = null;
        try {
            scopes = id.getAttribute(SCOPES);
        } catch (Exception e){
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
        return scopes;
    }

    @Override
    public Set<String> defaultGrantScopes(){
        return null;
    }

    @Override
    public boolean isAutoGrant(){
        return false;
    }

    @Override
    public Set<String> getDisplayName(){
        Set<String> displayName = null;
        try {
            displayName = id.getAttribute(NAME);
        } catch (Exception e){
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
        return displayName;
    }

    @Override
    public Set<String> getDisplayDescription(){
        Set<String> displayDescription = null;
        try {
            displayDescription = id.getAttribute(DESCRIPTION);
        } catch (Exception e){
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
        return displayDescription;
    }


}
