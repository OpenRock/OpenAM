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

package org.forgerock.openam.oauth2;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.forgerock.openam.oauth2.internal.UserIdentityVerifier;
import org.forgerock.openam.oauth2.provider.impl.ClientVerifierImpl;
import org.forgerock.openam.oauth2.store.impl.DefaultOAuthTokenStoreImpl;
import org.forgerock.openam.oauth2.utils.OAuth2Utils;
import org.forgerock.openam.oauth2.model.ClientApplication;
import org.forgerock.restlet.ext.oauth2.provider.*;
import org.forgerock.restlet.ext.openam.OpenAMParameters;
import org.forgerock.restlet.ext.openam.internal.OpenAMServerAuthorizer;
import org.forgerock.restlet.ext.openam.server.OpenAMServletAuthenticator;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.Verifier;

/**
 * A NAME does ...
 * <p/>
 * 
 * <pre>
 * &lt;!-- Servlet to Restlet adapter declaration (Mandatory) --&gt;
 * &lt;servlet&gt;
 * &lt;servlet-name&gt;RestletAdapter&lt;/servlet-name&gt;
 * &lt;servlet-class&gt;org.restlet.ext.servlet.ServerServlet&lt;/servlet-class&gt;
 * 
 * &lt;!-- Your application class name (Optional - For mode 3) --&gt;
 * &lt;init-param&gt;
 * &lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 * &lt;param-value&gt;org.forgerock.openam.oauth2.OAuth2Application&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * 
 * &lt;!-- List of supported client protocols (Optional - Only in mode 3) --&gt;
 * &lt;init-param&gt;
 * &lt;param-name&gt;org.restlet.clients&lt;/param-name&gt;
 * &lt;param-value&gt;RIAP CLAP FILE&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * &lt;/servlet&gt;
 * 
 * &lt;!-- servlet declaration --&gt;
 * 
 * &lt;servlet-mapping&gt;
 * &lt;servlet-name&gt;RestletAdapter&lt;/servlet-name&gt;
 * &lt;url-pattern&gt;/oauth2/*&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * @author Laszlo Hordos
 */
public class OAuth2Application extends Application {

    private URI redirectURI = null;

    @Override
    public Restlet createInboundRoot() {
        Router root = new Router(getContext());

        //default route goes to the flows
        root.attachDefault(activate());

        // Add TokenInfo Resource
        OAuth2Utils.setTokenStore(getTokenStore(), getContext());

        //go to token info endpoint
        root.attach(OAuth2Utils.getTokenInfoPath(getContext()), ValidationServerResource.class);

        //go to register client endpoint
        root.attach("/register_client", RegisterClient.class);

        return root;
    }

    /**
     * Setups OAuth2 paths and handlers
     * 
     * @return TODO Description
     */
    public Restlet activate() {
        Context childContext = getContext().createChildContext();
        Router root = new Router(childContext);
        
        OpenAMParameters parameters = new OpenAMParameters();
        OpenAMServletAuthenticator authenticator =
                new OpenAMServletAuthenticator(childContext, parameters);
        // This endpoint protected by OpenAM Filter
        root.attach(OAuth2Utils.getAuthorizePath(childContext), authenticator);

        OpenAMServerAuthorizer authorizer = new OpenAMServerAuthorizer();
        authenticator.setNext(authorizer);

        // Define Authorization Endpoint
        OAuth2FlowFinder finder =
                new OAuth2FlowFinder(childContext, OAuth2.EndpointType.AUTHORIZATION_ENDPOINT)
                        .supportAuthorizationCode().supportClientCredentials().supportImplicit()
                        .supportPassword();
        authorizer.setNext(finder);

        ClientAuthenticationFilter filter = new ClientAuthenticationFilter(childContext);
        // Try to authenticate the client The verifier MUST set
        filter.setVerifier(getClientVerifier());
        root.attach(OAuth2Utils.getAccessTokenPath(childContext), filter);

        // Define Token Endpoint
        finder =
                new OAuth2FlowFinder(childContext, OAuth2.EndpointType.TOKEN_ENDPOINT)
                        .supportAuthorizationCode().supportClientCredentials().supportImplicit()
                        .supportPassword();
        filter.setNext(finder);

        // Configure context
        childContext.setDefaultVerifier(getUserVerifier());
        OAuth2Utils.setClientVerifier(getClientVerifier(), childContext);
        OAuth2Utils.setTokenStore(getTokenStore(), childContext);
        OAuth2Utils.setContextRealm("/", childContext);

        return root;
    }

    /**
     * Creates a new client verifier
     * 
     * @return ClientVerifierImpl
     *              A client verifier
     */
    public org.forgerock.openam.oauth2.provider.ClientVerifier getClientVerifier() {
        return new ClientVerifierImpl();
    }

    /**
     * Creates a new user verifier
     * 
     * @return UserIdentityVerifier
     *              A new UserVerifier
     */
    public Verifier getUserVerifier() {
        return new UserIdentityVerifier(new OpenAMParameters());
    }

    /**
     * Gets the current token store or creates a new one if it doesn't exist
     * 
     * @return OAuthTokenStore
     *              A new token store.
     */
    public org.forgerock.openam.oauth2.provider.OAuth2TokenStore getTokenStore() {
        return new DefaultOAuthTokenStoreImpl();
    }

    /**
     * A TestClientVerifier is only for testing.
     */
    private class TestClientVerifier implements org.forgerock.openam.oauth2.provider.ClientVerifier {

        @Override
        public ClientApplication verify(Request request, Response response ) {
            return new TestClientApplication();
        }

        @Override
        public Collection<ChallengeScheme> getRequiredAuthenticationScheme(String clientId) {
            return null;
        }
    }

    /**
     * A TestClientApplication is only for testing.
     */
    private class TestClientApplication implements ClientApplication {

        @Override
        public String getClientId() {
            return "cid";
        }

        @Override
        public ClientType getClientType() {
            return ClientType.CONFIDENTIAL;
        }

        @Override
        public Set<URI> getRedirectionURIs() {
            Set<URI> cfg = new HashSet<URI>(1);
            cfg.add(URI.create("http://local.identitas.no:9085/openam/oauth2test/code-token.html"));
            cfg.add(redirectURI);
            return Collections.unmodifiableSet(cfg);
        }

        @Override
        public String getAccessTokenType() {
            return OAuth2.Bearer.BEARER;
        }

        @Override
        public String getClientAuthenticationSchema() {
            return null;
        }

        @Override
        public Set<String> allowedGrantScopes() {
            Set<String> scope = new HashSet<String>();
            scope.add("read");
            scope.add("write");
            return Collections.unmodifiableSet(scope);
        }

        @Override
        public Set<String> defaultGrantScopes() {
            Set<String> scope = new HashSet<String>();
            scope.add("read");
            return Collections.unmodifiableSet(scope);
        }

        @Override
        public boolean isAutoGrant() {
            return false;
        }

        @Override
        public Set<String> getDisplayName(){
            return null;
        }

        @Override
        public Set<String> getDisplayDescription(){
            return null;
        }
    }

}
