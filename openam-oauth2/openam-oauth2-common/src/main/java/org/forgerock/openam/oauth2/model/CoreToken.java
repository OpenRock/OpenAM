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
package org.forgerock.openam.oauth2.model;


import com.sun.identity.shared.OAuth2Constants;
import com.sun.identity.shared.locale.AMResourceBundleCache;
import com.sun.jersey.api.uri.UriComponent;
import org.forgerock.json.fluent.JsonValue;

import javax.print.DocFlavor;
import java.util.*;

public class CoreToken extends JsonValue {

    private String id;
    private static ResourceBundle rb = ResourceBundle.getBundle("OAuth2CoreToken");

    public CoreToken(){
        super(new HashMap<String, String>());
    }

    public CoreToken(String id, JsonValue value){
        super(value);
        this.id = id;

    }

    public CoreToken(String id, String userName, String realm, String expireTime, String tokenType, String tokenName){
        super(new HashMap<String, String>());
        this.put(OAuth2Constants.CoreTokenParams.ID, id);
        this.put(OAuth2Constants.CoreTokenParams.USERNAME, userName);
        this.put(OAuth2Constants.CoreTokenParams.REALM, realm);
        this.put(OAuth2Constants.CoreTokenParams.EXPIRE_TIME, expireTime);
        this.put(OAuth2Constants.CoreTokenParams.TOKEN_TYPE, tokenType);
        this.put(OAuth2Constants.CoreTokenParams.TOKEN_NAME, tokenName);
    }

    /**
     * @{inheritDoc}
     */
    public Map<String, Object> convertToMap(){
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.TOKEN_TYPE), getParameter(OAuth2Constants.CoreTokenParams.TOKEN_TYPE));
        tokenMap.put(rb.getString(OAuth2Constants.CoreTokenParams.EXPIRE_TIME), getExpireTime());
        return tokenMap;
    }

    /**
     * Gets information about the token for the tokeninfo end point
     * @return
     */
    public Map<String, Object> getTokenInfo() {
        Map<String, Object> tokenMap = new HashMap<String, Object>();
        tokenMap.put(rb.getString(OAuth2Constants.Params.TOKEN_TYPE), OAuth2Constants.Bearer.BEARER);
        tokenMap.put(rb.getString(OAuth2Constants.Params.EXPIRES_IN), getExpireTime());
        tokenMap.put(rb.getString(OAuth2Constants.Params.REALM), getRealm());
        tokenMap.put(rb.getString(OAuth2Constants.Params.SCOPE), this.getParameter(OAuth2Constants.CoreTokenParams.SCOPE));
        return tokenMap;
    }

    public void setTokenID(String id){
        this.id = id;
    }

    public void setUserName(String userName) {
        this.put(OAuth2Constants.CoreTokenParams.USERNAME, userName);
    }

    /**
     * {@inheritDoc}
     */
    public void setRealm(String realm) {
        if (realm == null || realm.isEmpty()){
            this.put(OAuth2Constants.CoreTokenParams.REALM, "/");
        } else {
            this.put(OAuth2Constants.CoreTokenParams.REALM, realm);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setExpireTime(long expireTime) {
        this.put(OAuth2Constants.CoreTokenParams.EXPIRE_TIME,String.valueOf(expireTime));
    }

    /**
     * @{inheritDoc}
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
     * {@inheritDoc}
     */
    public String getUserID() {
        return this.getParameter(OAuth2Constants.CoreTokenParams.USERNAME);
    }

    /**
     * {@inheritDoc}
     */
    public String getRealm() {
        return this.getParameter(OAuth2Constants.CoreTokenParams.REALM);
    }

    /**
     * Returns the expiry time as stored.
     *
     * @return time of expiry expressed as milliseconds since the epoch.
     */
    public long getExpireTime() {
        return Long.parseLong(this.getParameter(OAuth2Constants.CoreTokenParams.EXPIRE_TIME));
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() > getExpireTime());
    }

    /**
     * {@inheritDoc}
     */
    public String tokenType(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.TOKEN_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    public String tokenName(){
        return this.getParameter(OAuth2Constants.CoreTokenParams.TOKEN_NAME);
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
