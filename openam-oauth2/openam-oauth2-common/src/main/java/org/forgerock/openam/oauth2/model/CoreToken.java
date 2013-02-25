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
/*
 * "Portions copyright 2012-2013 ForgeRock Inc".
 */
package org.forgerock.openam.oauth2.model;


import com.sun.identity.shared.OAuth2Constants;
import org.forgerock.json.fluent.JsonValue;
import java.util.*;

public class CoreToken extends JsonValue implements Token {

    private String id;
    private static ResourceBundle rb = ResourceBundle.getBundle("OAuth2CoreToken");

    public CoreToken(){
        super(new HashMap<String, Object>());
    }

    public CoreToken(String id, JsonValue value){
        super(value);
        this.id = id;

    }

    public CoreToken(String id, String userName, String realm, long expireTime, String tokenType, String tokenName){
        super(new HashMap<String, Object>());
        setTokenID(id);
        setUserName(userName);
        setRealm(realm);
        setExpireTime(expireTime);
        setTokenType(tokenType);
        setTokenName(tokenName);
    }

    /**
     * @{inheritDoc}
     */
    public Map<String, Object> convertToMap(){
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.TOKEN_TYPE), getParameter(OAuth2Constants.CoreTokenParams.TOKEN_TYPE));
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.EXPIRE_TIME), (System.currentTimeMillis() - getExpireTime())/1000);
        return tokenMap;
    }

    /**
     * Gets information about the token for the tokeninfo end point
     * @return
     */
    public Map<String, Object> getTokenInfo() {
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.TOKEN_TYPE), getTokenType());
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.EXPIRE_TIME), (System.currentTimeMillis() - getExpireTime())/1000);
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.REALM), getRealm());
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.SCOPE), getScope());
        return tokenMap;
    }

    protected void setTokenID(String id){
        this.id = id;
        this.put(OAuth2Constants.CoreTokenParams.ID, id);
    }

    protected void setUserName(String userName) {
        this.put(OAuth2Constants.CoreTokenParams.USERNAME, userName);
    }

    protected void setRealm(String realm) {
        if (realm == null || realm.isEmpty()){
            this.put(OAuth2Constants.CoreTokenParams.REALM, "/");
        } else {
            this.put(OAuth2Constants.CoreTokenParams.REALM, realm);
        }
    }

    protected void setExpireTime(long expireTime) {
        this.put(OAuth2Constants.CoreTokenParams.EXPIRE_TIME, String.valueOf((expireTime * 1000) + System.currentTimeMillis()));
    }

    protected void setTokenType(String tokenType) {
        this.put(OAuth2Constants.CoreTokenParams.TOKEN_TYPE, tokenType);
    }

    protected void setTokenName(String tokenName) {
        this.put(OAuth2Constants.CoreTokenParams.TOKEN_NAME, tokenName);
    }

    /**
     * Get tokens id
     *
     * @return
     *          ID of token
     */
    public String getTokenID(){
        if (id != null){
            return id;
        } else {
            JsonValue val = this.get(OAuth2Constants.CoreTokenParams.ID);
            if (val != null){
                id = val.asString();
                return val.asString();
            }
        }
        return null;
    }

    /**
     * Gets the parent token
     * @return the id of the parent token
     */
    public String getParent(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.PARENT);
    }

    /**
     * Gets the issued state for code type
     * @return true or false if issued or not
     */
    public boolean isIssued(){
        if (this.getParameter(OAuth2Constants.CoreTokenParams.ISSUED) != null){
            return Boolean.parseBoolean(this.getParameter(OAuth2Constants.CoreTokenParams.ISSUED));
        } else {
            return false;
        }
    }

    /**
     * Gets the refresh token id
     * @return id of refresh token
     */
    public String getRefreshToken(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.REFRESH_TOKEN);
    }

    /**
     * Get tokens UserID
     *
     * @return
     *          ID of user
     */
    public String getUserID() {
        return this.getParameter(OAuth2Constants.CoreTokenParams.USERNAME);
    }

    /**
     * Get Tokens Realm
     *
     * @return
     *          the realm
     */
    public String getRealm() {
        return this.getParameter(OAuth2Constants.CoreTokenParams.REALM);
    }

    /**
     * Returns the seconds until token expire.
     *
     * @return time of expiry expressed as milliseconds since the epoch.
     */
    public long getExpireTime() {
        return Long.parseLong(this.getParameter(OAuth2Constants.CoreTokenParams.EXPIRE_TIME));
    }

    /**
     * Get tokens client
     *
     * @return
     *          the {@link SessionClient} for the token
     */
    public SessionClient getClient(){

        return new SessionClientImpl(getClientID(), getRedirectURI());
    }

    /**
     * Gets the tokens scope
     *
     * @return
     *          Set of strings that are the tokens scope
     */
    public Set<String> getScope(){
        if (this.get(OAuth2Constants.CoreTokenParams.SCOPE) != null){
            return (Set<String>) this.get(OAuth2Constants.CoreTokenParams.SCOPE).getObject();
        } else {
            return null;
        }
    }

    /**
     * Checks if token is expired
     *
     * @return
     *          true if expired
     *          false if not expired
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() > getExpireTime());
    }

    /**
     * Returns the token type
     *
     * @return The type of token. For example {@link BearerToken}
     */
    public String getTokenType(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.TOKEN_TYPE);
    }

    /**
     * Returns the name of the token
     *
     * @return The name of token. Will be either access_token, code, refresh_token
     */
    public String getTokenName(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.TOKEN_NAME);
    }

    /**
     * Returns the client_id associated token
     *
     * @return The client_id associated with token
     */
    public String getClientID(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.CLIENT_ID);
    }

    /**
     * Returns the redirect_uri associated token
     *
     * @return The  redirect_uri associated with token
     */
    public String getRedirectURI(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.REDIRECT_URI);
    }
    /**
     * Gets any parameter stored in the token
     * @return
     */
    public String getParameter(String paramName){
        JsonValue param = get(paramName);
        if (param != null && param.isString()){
            return param.asString();
        }
        return null;
    }

}
