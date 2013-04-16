/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 ForgeRock Inc. All rights reserved.
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
 * "Portions Copyrighted [year] [name of company]"
 */

package org.forgerock.openam.oauth2.provider.impl;

import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.*;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.shared.OAuth2Constants;
import org.forgerock.openam.ext.cts.repo.DefaultOAuthTokenStoreImpl;
import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.forgerock.openam.oauth2.model.CoreToken;
import org.forgerock.openam.oauth2.model.JWTToken;
import org.forgerock.openam.oauth2.provider.Scope;
import org.forgerock.openam.oauth2.utils.OAuth2Utils;
import org.restlet.Request;

import java.security.*;
import java.util.*;

/**
 * This is the default scope implementation class. This class by default
 * follows the OAuth2 specs rules regarding how scope should be assigned.
 * The only exceptions is in the retrieveTokenInfoEndPoint method end point
 * the scopes are assumed to be OpenAM user attributes, which will be returned
 * upon the completion of the retrieveTokenInfoEndPoint method
 */
public class ScopeImpl implements Scope {

    // TODO remove this temporary keypair generation and use the client keypair
    static KeyPair keyPair;
    static{
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            keyPair = keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException e){

        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> scopeToPresentOnAuthorizationPage(Set<String> requestedScope, Set<String> availableScopes, Set<String> defaultScopes){

        if (requestedScope == null){
            return defaultScopes;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> scopeRequestedForAccessToken(Set<String> requestedScope, Set<String> availableScopes, Set<String> defaultScopes){

        if (requestedScope == null){
            return defaultScopes;
        }

        Set<String> scopes = new HashSet<String>(availableScopes);
        scopes.retainAll(requestedScope);
        return scopes;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> evaluateScope(CoreToken token){
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> scopes = token.getScope();
        String resourceOwner = token.getUserID();

        if (resourceOwner != null){
            AMIdentity id = null;
            try {
            id = getIdentity(resourceOwner, token.getRealm());
            } catch (Exception e){
                OAuth2Utils.DEBUG.error("Unable to get user identity", e);
            }
            if (id != null && scopes != null){
                for (String scope : scopes){
                    try {
                        Set<String> mail = id.getAttribute(scope);
                        if (mail != null || !mail.isEmpty()){
                            map.put(scope, mail.iterator().next());
                        }
                    } catch (Exception e){
                        OAuth2Utils.DEBUG.error("Unable to get attribute", e);
                    }
                }
            }
        }

        return map;
    }

    private AMIdentity getIdentity(String uName, String realm) throws OAuthProblemException {
        SSOToken token = (SSOToken) AccessController.doPrivileged(AdminTokenAction.getInstance());
        AMIdentity theID = null;

        try {
            AMIdentityRepository amIdRepo = new AMIdentityRepository(token, realm);

            IdSearchControl idsc = new IdSearchControl();
            idsc.setRecursive(true);
            idsc.setAllReturnAttributes(true);
            // search for the identity
            Set<AMIdentity> results = Collections.EMPTY_SET;
            idsc.setMaxResults(0);
            IdSearchResults searchResults =
                    amIdRepo.searchIdentities(IdType.USER, uName, idsc);
            if (searchResults != null) {
                results = searchResults.getSearchResults();
            }

            if (results == null || results.size() != 1) {
                throw OAuthProblemException.OAuthError.UNAUTHORIZED_CLIENT.handle(null,
                        "Not able to get client from OpenAM");

            }

            theID = results.iterator().next();

            //if the client is deactivated return null
            if (theID.isActive()){
                return theID;
            } else {
                return null;
            }
        } catch (Exception e){
            OAuth2Utils.DEBUG.error("ClientVerifierImpl::Unable to get client AMIdentity: ", e);
            throw OAuthProblemException.OAuthError.UNAUTHORIZED_CLIENT.handle(null, "Not able to get client from OpenAM");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> extraDataToReturnForTokenEndpoint(Set<String> parameters, CoreToken token){
        Map<String, Object> map = new HashMap<String, Object>();
        Set<String> scope = token.getScope();

        //OpenID Connect
        // if an openid scope return the id_token
        if (scope.contains("openid")){
            DefaultOAuthTokenStoreImpl store = new DefaultOAuthTokenStoreImpl();
            String jwtToken = store.createSignedJWT(token.getRealm(),
                    token.getUserID(),
                    token.getClientID(),
                    OAuth2Utils.getDeploymentURL(Request.getCurrent()),
                    token.getClientID(),
                    keyPair.getPrivate());
            map.put("id_token", jwtToken);
        }
        //END OpenID Connect
        return map;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> extraDataToReturnForAuthorizeEndpoint(Set<String> parameters, Map<String, CoreToken> tokens){
        Map<String, String> map = new HashMap<String, String>();

        // OpenID Connect
        boolean fragment = false;
        if (tokens != null && !tokens.isEmpty()){
            for(Map.Entry<String, CoreToken> token : tokens.entrySet() ){
                Set<String> scope = token.getValue().getScope();
                if (scope.contains("openid") && !token.getKey().equalsIgnoreCase(OAuth2Constants.AuthorizationEndpoint.CODE)){
                    DefaultOAuthTokenStoreImpl store = new DefaultOAuthTokenStoreImpl();
                    String jwtToken = store.createSignedJWT(token.getValue().getRealm(),
                                                token.getValue().getUserID(),
                                                token.getValue().getClientID(),
                                                OAuth2Utils.getDeploymentURL(Request.getCurrent()),
                                                token.getValue().getClientID(),
                                                keyPair.getPrivate());
                    map.put("id_token", jwtToken);
                    fragment = true;
                    break;
                }
            }
        }
        if (fragment){
            map.put("returnType", "FRAGMENT");
        }
        //end OpenID Connect

        return map;
    }

}
