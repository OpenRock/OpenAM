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

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.*;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.sm.ServiceConfig;
import com.sun.identity.sm.ServiceConfigManager;
import org.forgerock.openam.ext.cts.CoreTokenService;
import org.forgerock.openam.ext.cts.repo.JMQTokenRepo;
import org.forgerock.openam.oauth2.model.impl.ClientApplicationImpl;
import org.forgerock.openam.oauth2.utils.OAuth2Constants;
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.forgerock.restlet.ext.oauth2.model.ClientApplication;
import org.forgerock.restlet.ext.oauth2.provider.ClientVerifier;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;

import java.security.AccessController;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.sun.identity.shared.encode.Hash;

public class ClientVerifierImpl implements ClientVerifier{

    private static final String CLIENT_PASSWORD = "userpassword";

    @Override
    public ClientApplication verify(ChallengeResponse challengeResponse)
            throws OAuthProblemException{
        String client_id = challengeResponse.getIdentifier();
        String client_secret = String.valueOf(challengeResponse.getSecret());
        client_secret = Hash.hash(client_secret);
        //String client_secret = String.valueOf(challengeResponse.getSecret());
        return verify(client_id, client_secret);
    }

    @Override
    public ClientApplication verify(String client_id, String client_secret)
            throws OAuthProblemException{
        ClientApplication user = null;
        try {
            AMIdentity id = getIdentity(client_id);
            Set<String> clientPassword = id.getAttribute(CLIENT_PASSWORD);
            //password is returned as {SHA-1}password
            //remove {SHA-1}
            String cleanpass = clientPassword.iterator().next().replaceAll("\\{SHA-1\\}", "");
            if (!cleanpass.equalsIgnoreCase(client_secret)){
                 //wrong client secret
                throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                        "Service unavailable", "Could not create underlying storage", null);
            }
            user = new ClientApplicationImpl(id);
        } catch (Exception e){
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
        return user;
    }

    @Override
    public Collection<ChallengeScheme> getRequiredAuthenticationScheme(String client_id){

        return null;
    }

    @Override
    public ClientApplication findClient(String client_id){

        ClientApplication user = null;
        try {
            AMIdentity id = getIdentity(client_id);
            Set<String> clientPassword = id.getAttribute(CLIENT_PASSWORD);
            user = new ClientApplicationImpl(id);
        } catch (Exception e){
            return user;
        }
        return user;
    }

    private AMIdentity getIdentity(String uName) throws OAuthProblemException {
        SSOToken token = (SSOToken) AccessController.doPrivileged(AdminTokenAction.getInstance());
        AMIdentity theID = null;

        try {
            AMIdentityRepository amIdRepo = new AMIdentityRepository(token, null);

            IdSearchControl idsc = new IdSearchControl();
            idsc.setRecursive(true);
            idsc.setAllReturnAttributes(true);
            // search for the identity
            Set<AMIdentity> results = Collections.EMPTY_SET;
            idsc.setMaxResults(0);
            IdSearchResults searchResults =
                    amIdRepo.searchIdentities(IdType.AGENT, uName, idsc);
            if (searchResults != null) {
                results = searchResults.getSearchResults();
            }

            if (results == null || results.size() != 1) {
                throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                        "Service unavailable", "Could not create underlying storage", null);

            }

            theID = results.iterator().next();
        } catch (Exception e){
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
        return theID;
    }
}
