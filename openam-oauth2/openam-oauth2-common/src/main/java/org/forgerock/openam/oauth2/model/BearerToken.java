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

package org.forgerock.openam.oauth2.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.sun.identity.shared.OAuth2Constants;
import org.forgerock.json.fluent.JsonValue;

/**
 * Implementation of AccessToken
 */
public class BearerToken extends CoreToken {

    private static ResourceBundle rb = ResourceBundle.getBundle("OAuth2CoreToken");

    /**
     * Constructor. Creates an Bearer Access Token
     * 
     * @param id
     *            Id of the access token
     * @param parent
     *            Id of the parent token
     * @param userID
     *            UserID of the user creating the token
     * @param client
     *            The sessionClient of the client creating the token
     * @param realm
     *            The realm this token is created in
     * @param scope
     *            The scope of this token
     * @param expireTime
     *            The amount of time in seconds this token will expire in
     */
    public BearerToken(String id, String parent, String userID, SessionClient client,
                       String realm, Set<String> scope, long expireTime) {
        super(id, userID, realm, String.valueOf(expireTime), OAuth2Constants.Token.OAUTH_ACCESS_TOKEN,
                OAuth2Constants.Bearer.BEARER);
        super.put("scope", scope);
        super.put("clientID", client.getClientId());
        super.put("redirectURI", client.getClientId());
        super.put("parent", parent);
    }

    /**
     * Constructor. Creates an Bearer Access Code Token
     *
     * @param id
     *            Id of the access token
     * @param parent
     *            Id of the parent token
     * @param userID
     *            UserID of the user creating the token
     * @param client
     *            The sessionClient of the client creating the token
     * @param realm
     *            The realm this token is created in
     * @param scope
     *            The scope of this token
     * @param expireTime
     *            The amount of time in seconds this token will expire in
     */
    public BearerToken(String id, String userID, SessionClient client,
                       String realm, Set<String> scope, long expireTime, String issued) {
        super(id, userID, realm, String.valueOf(expireTime), OAuth2Constants.Token.OAUTH_CODE_TYPE,
                OAuth2Constants.Bearer.BEARER);
        super.put("scope", scope);
        super.put("clientID", client.getClientId());
        super.put("redirectURI", client.getClientId());
        super.put("issued", issued);
    }

    /**
     * Creates a Bearer Token
     * @param id
     *          id of the token
     * @param parent
     *          id of the parent token
     * @param userID
     *          UserID of the user creating the token
     * @param client
     *          The sessionClient of the client creating the token
     * @param realm
     *          The realm this token is created in
     * @param scope
     *          The scope of this token
     * @param expireTime
     *          The amount of time in seconds this token will expire in
     * @param tokenType
     *          The type of this token. Refresh, Access, Code
     */
    public BearerToken(String id, String parent, String userID, SessionClient client,
                       String realm, Set<String> scope, long expireTime, String tokenType) {
        super(id, userID, realm, String.valueOf(expireTime), tokenType,
                OAuth2Constants.Bearer.BEARER);
        super.put("scope", scope);
        super.put("clientID", client.getClientId());
        super.put("redirectURI", client.getRedirectUri());
        super.put("parent", parent);
    }

    /**
     * Creates a Bearer Token
     * @param id
     *          id of the token
     * @param parent
     *          id of the parent token
     * @param userID
     *          UserID of the user creating the token
     * @param client
     *          The sessionClient of the client creating the token
     * @param realm
     *          The realm this token is created in
     * @param scope
     *          The scope of this token
     * @param expireTime
     *          The amount of time in seconds this token will expire in
     * @param tokenType
     *          The type of this token. Refresh, Access, Code
     * @param refreshToken
     *          The id of the refresh token
     */
    public BearerToken(String id, String parent, String userID, SessionClient client,
                       String realm, Set<String> scope, long expireTime, String refreshToken, String tokenType) {
        super(id, userID, realm, String.valueOf(expireTime), tokenType,
                OAuth2Constants.Bearer.BEARER);
        super.put("scope", scope);
        super.put("clientID", client.getClientId());
        super.put("redirectURI", client.getRedirectUri());
        super.put("parent", parent);
        super.put("refreshToken", refreshToken);
    }

    /**
     * Creates an Bearer Access Token
     * 
     * @param id
     *            Id of the access Token
     * @param value
     *            A JsonValue map to populate this token with.
     */
    public BearerToken(String id, JsonValue value) {
        super(id, value);
    }

    @Override
    /**
     * @{inheritDoc}
     */
    public Map<String, Object> convertToMap(){
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put(rb.getString(OAuth2Constants.Params.ACCESS_TOKEN), getTokenID());
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.TOKEN_TYPE), getParameter(OAuth2Constants.CoreTokenParams.TOKEN_TYPE));
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.EXPIRE_TIME), getExpireTime());
        return tokenMap;
    }

    @Override
    /**
     * @{inheritDoc}
     */
    public Map<String, Object> getTokenInfo() {
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put(OAuth2Constants.Params.ACCESS_TOKEN, getTokenID());
        tokenMap.put(OAuth2Constants.Params.TOKEN_TYPE, OAuth2Constants.Bearer.BEARER);
        tokenMap.put(OAuth2Constants.Params.EXPIRES_IN, getExpireTime());
        tokenMap.put(OAuth2Constants.Params.REALM, getRealm());
        tokenMap.put(OAuth2Constants.Params.SCOPE, this.getParameter(OAuth2Constants.CoreTokenParams.SCOPE));
        return tokenMap;
    }

}
