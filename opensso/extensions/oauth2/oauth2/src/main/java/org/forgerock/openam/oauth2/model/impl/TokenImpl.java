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

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.restlet.ext.oauth2.model.SessionClient;
import org.forgerock.restlet.ext.oauth2.model.Token;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jonathan
 * Date: 26/3/12
 * Time: 10:43 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TokenImpl implements Token {

    private String id;
    private String userID = null;
    private String realm = "/";
    private SessionClient client = null;
    private Set<String> scope = Collections.emptySet();
    private long expireTime = 0;

    // TODO javadoc
    public TokenImpl(String id, String userID, SessionClient client, String realm, Set<String> scope, long expireTime) {
        this.id = id;
        this.userID = userID;
        this.client = client;
        this.realm = realm;
        this.scope = scope;
        this.expireTime = expireTime;
    }

    // TODO javadoc
    public TokenImpl(JsonValue value) {
        this.id = value.get("id").asString();
        this.userID = value.get("uuid").asString();
        this.client = (SessionClient) value.get("client");
        this.realm = value.get("realm").asString();
        this.scope = convertScope(value.get("scope").asList());
        this.expireTime = value.get("expire_time").asLong();
    }

    // TODO javadoc
    public JsonValue asJson() {
        JsonValue value = new JsonValue(new HashMap<String, Object>());
        value.add("id", id);
        value.add("uuid", userID);
        if (client != null) {
            value.add("client", client.getClientId());
        }
        value.add("realm", realm);
        value.add("scope", scope);
        value.add("valid", true);
        value.add("expire_time", expireTime);
        return value;
    }
    
    private Set<String> convertScope(List<Object> scopeList) {
        Set<String> scopeSet = new HashSet<String>();
        for (Object o : scopeList) {
            scopeSet.add(o.toString());
        }
        return scopeSet;
    }

    public void setToken(String id) {
        this.id = id;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public void setClient(SessionClient client) {
        this.client = client;
    }

    public void setScope(Set<String> scope) {
        this.scope = scope;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String getToken() {
        return id;
    }

    @Override
    public String getUserID() {
        return userID;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public SessionClient getClient() {
        return client;
    }

    @Override
    public Set<String> getScope() {
        return scope;
    }

    @Override
    public long getExpireTime() {
        return expireTime;
    }

    @Override
    public boolean isExpired() {
        return (System.currentTimeMillis() > expireTime);
    }

}
