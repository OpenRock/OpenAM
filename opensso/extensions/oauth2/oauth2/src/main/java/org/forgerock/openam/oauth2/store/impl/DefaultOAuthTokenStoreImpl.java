/*
 * Copyright (c) 2012 ForgeRock AS. All rights reserved.
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
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 */

package org.forgerock.openam.oauth2.store.impl;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.JsonResourceAccessor;
import org.forgerock.json.resource.JsonResourceContext;
import org.forgerock.json.resource.JsonResourceException;
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.forgerock.restlet.ext.oauth2.model.AccessToken;
import org.forgerock.restlet.ext.oauth2.model.AuthorizationCode;
import org.forgerock.restlet.ext.oauth2.model.RefreshToken;
import org.forgerock.restlet.ext.oauth2.model.SessionClient;
import org.forgerock.restlet.ext.oauth2.provider.OAuth2TokenStore;
import org.restlet.data.Status;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of the OAuthTokenStore interface that uses the CoreTokenService for storing the tokens as JSON
 * objects.
 *
 * @author Jonathan Scudder
 */
public class DefaultOAuthTokenStoreImpl implements OAuth2TokenStore {

    private static final long AUTHZ_CODE_LIFETIME = 1000 * 60 * 10; // 10 minutes TODO: make configurable
    private static final long REFRESH_TOKEN_LIFETIME = 1000 * 60 * 60 * 8; // 8 hours TODO: make configurable
    private static final long ACCESS_TOKEN_LIFETIME = 1000 * 60 * 10; // 10 minutes TODO: make configurable

    // Removed: long requestTime
    // Removed: String clientID
    // Removed: String redirectURI

    @Override
    public AuthorizationCode createAuthorizationCode(Set<String> scopes, String realm, String uuid, SessionClient client) throws OAuthProblemException {

        String id = UUID.randomUUID().toString();
        long expires = System.currentTimeMillis() + AUTHZ_CODE_LIFETIME;

        // Create an authorization code JSON object
        JsonValue code = new JsonValue(new HashMap<String, Object>());
        code.add("id", id);
        code.add("type", "authorization_code");
        code.add("uuid", uuid);
        if (client != null) {
            code.add("client", client.getClientId());
        }
        code.add("realm", realm);
        code.add("scope", scopes);
        code.add("valid", true);
        code.add("expires", expires);

        JsonValue response = null;

        // Store in CTS
        JsonResourceAccessor accessor = new JsonResourceAccessor(null, JsonResourceContext.newRootContext());
        // TODO: call above with CTS resource
        try {
            response = accessor.create(id, code);
        } catch (JsonResourceException e) {
            // TODO: logging
            throw new OAuthProblemException(Status.SERVER_ERROR_INTERNAL.getCode(), "Internal error", "Could not create token in CTS", null);
        }

        if (response == null) {
            throw new OAuthProblemException(Status.SERVER_ERROR_INTERNAL.getCode(), "Internal error", "Could not create token in CTS", null);
        }

        // Construct an AuthorizationCode object and return it
        AuthorizationCode ac = constructAuthorizationCode(true, response.get("id").asString(), uuid, client, realm, expires);
        return ac;
    }

    @Override
    public AuthorizationCode readAuthorizationCode(String id) throws OAuthProblemException {

        JsonValue response = null;

        // Read from CTS
        JsonResourceAccessor accessor = new JsonResourceAccessor(null, JsonResourceContext.newRootContext());
        // TODO: call above with CTS resource
        try {
            response = accessor.read(id);
        } catch (JsonResourceException e) {
            // TODO: logging
            throw new OAuthProblemException(Status.SERVER_ERROR_INTERNAL.getCode(), "Internal error", "Could not read token from CTS: " + e.getMessage(), null);
        }

        if (response == null) {
            throw new OAuthProblemException(Status.CLIENT_ERROR_NOT_FOUND.getCode(), "Not found", "Could not find token from CTS", null);
        }

        // Construct an AuthorizationCode object and return it
        AuthorizationCode ac = constructAuthorizationCode(!response.get("valid").asBoolean(), response.get("id").asString(), response.get("uuid").asString(), (SessionClient) response.get("client"), response.get("realm").asString(), response.get("expires").asLong());
        return ac;
        // Construct a request
        //JsonValue request = new JsonValue(new HashMap<String, Object>());


        // Call the CTS

        // Wrap the returned object // TODO:

        // Return the AuthorizationCode
        //return ac;
    }

    /**
     * Internal helper class to construct an anonymous authorization code object.
     */
    private AuthorizationCode constructAuthorizationCode(final boolean valid, final String id, final String uuid, final SessionClient client, final String realm, final long expires) {
        return new AuthorizationCode() {
            public boolean isTokenIssued() {
                return valid;
            }

            public String getToken() {
                return id;
            }

            public String getUserID() {
                return uuid;
            }

            public String getRealm() {
                return realm;
            }

            public SessionClient getClient() {
                return client;
            }

            public Set<String> getScope() {
                return Collections.emptySet();
            }

            public long getExpireTime() {
                return 0;
            }

            public boolean isExpired() {
                return false;
            }

            public long getExpires() {
                return expires;
            }
        };
    }


    @Override
    public void deleteAuthorizationCode(String id) throws OAuthProblemException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessToken createAccessToken(String accessTokenType, Set<String> scopes, AuthorizationCode code) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessToken createAccessToken(String accessTokenType, Set<String> scopes, RefreshToken refreshToken) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessToken createAccessToken(String accessTokenType, Set<String> scopes, String realm, String uuid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessToken createAccessToken(String accessTokenType, Set<String> scopes, String realm, String uuid, SessionClient client) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessToken createAccessToken(String accessTokenType, Set<String> scopes, String realm, String uuid, String clientId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AccessToken readAccessToken(String id) throws OAuthProblemException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteAccessToken(String id) throws OAuthProblemException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RefreshToken createRefreshToken(Set<String> scopes, String realm, String uuid, String clientId) throws OAuthProblemException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RefreshToken readRefreshToken(String id) throws OAuthProblemException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteRefreshToken(String id) throws OAuthProblemException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
