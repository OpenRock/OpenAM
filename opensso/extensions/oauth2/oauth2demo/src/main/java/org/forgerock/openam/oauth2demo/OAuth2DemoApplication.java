/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2012 ForgeRock AS. All rights reserved.
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
 * "Portions Copyrighted [year] [name of copyright owner]"
 * $Id$
 */

package org.forgerock.openam.oauth2demo;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.forgerock.restlet.ext.oauth2.OAuth2Utils;
import org.forgerock.restlet.ext.oauth2.consumer.AccessTokenValidator;
import org.forgerock.restlet.ext.oauth2.consumer.BearerOAuth2Proxy;
import org.forgerock.restlet.ext.oauth2.consumer.BearerToken;
import org.forgerock.restlet.ext.oauth2.consumer.BearerTokenVerifier;
import org.forgerock.restlet.ext.oauth2.consumer.OAuth2Authenticator;
import org.forgerock.restlet.ext.oauth2.internal.DefaultScopeEnroler;
import org.forgerock.restlet.ext.oauth2.provider.ValidationServerResource;
import org.forgerock.restlet.ext.openam.OpenAMParameters;
import org.forgerock.restlet.ext.openam.server.OpenAMServletAuthenticator;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.resource.Directory;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * A NAME does ...
 *
 * @author Laszlo Hordos
 */
public class OAuth2DemoApplication extends Application {

    /**
     * The Freemarker's configuration.
     */
    private Configuration configuration;

    @Override
    public Restlet createInboundRoot() {
        Router root = new Router(getContext());

        String client_id = "demo";
        String client_secret = "Passw0rd";
        String username = client_id;
        String password = client_secret;
        String scope = null;
        String template_dir = "clap:///templates";

        for (Parameter parameter : getContext().getParameters()) {
            if ("oauth2.client_id".equals(parameter.getName())) {
                client_id = parameter.getValue();
            } else if ("oauth2.client_secret".equals(parameter.getName())) {
                client_secret = parameter.getValue();
            } else if ("oauth2.username".equals(parameter.getName())) {
                username = parameter.getValue();
            } else if ("oauth2.password".equals(parameter.getName())) {
                password = parameter.getValue();
            } else if ("oauth2.scope".equals(parameter.getName())) {
                scope = parameter.getValue();
            }
        }

        configuration = new Configuration();
        try {
            configuration.setSetting(Configuration.CACHE_STORAGE_KEY, "strong:20, soft:250");
        } catch (TemplateException e) {

        }
        configuration.setTemplateLoader(new ContextTemplateLoader(getContext(), template_dir));

        //Directory dir = new Directory(getContext(), new Reference("war:///WEB-INF/oauth2demo"));
        Directory dir = new Directory(getContext(), new Reference("clap:///org/forgerock/openam/oauth2demo"));
        root.attach("/static", dir);

        URI current = getCurrentURI();
        getContext().getAttributes().put("org.forgerock.openam.oauth2demo", current);

        BearerOAuth2Proxy auth2Proxy = new BearerOAuth2Proxy(getContext(), null);
        auth2Proxy.pushOAuth2Proxy(getContext());
        auth2Proxy.setAuthorizationEndpoint(new Reference(current.resolve("../oauth2/authorize")));
        auth2Proxy.setTokenEndpoint(new Reference(current.resolve("../oauth2/access_token")));
        auth2Proxy.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_BASIC, client_id, client_secret.toCharArray()));
        auth2Proxy.setClientCredentials(client_id, client_secret);
        auth2Proxy.setRedirectionEndpoint(new Reference(current.resolve("./redirect")));
        auth2Proxy.setResourceOwnerCredentials(username, password);
        auth2Proxy.setScope(OAuth2Utils.split(scope, " "));

        RedirectResource redirectResource = new RedirectResource(getContext(), current.resolve("./static/index.html").toString(), Redirector.MODE_CLIENT_FOUND);
        root.attach("/redirect", redirectResource);


        // Validation Resource
        Reference validationServerRef = new Reference(current.resolve("../oauth2/tokeninfo"));
        AccessTokenValidator<BearerToken> validator = new ValidationServerResource(getContext(), validationServerRef);

        // Use CHALLENGERESPONSE
        BearerTokenVerifier tokenVerifier = new BearerTokenVerifier(validator);
        OAuth2Authenticator authenticator = new OAuth2Authenticator(getContext(), null, OAuth2Utils.ParameterLocation.HTTP_HEADER, tokenVerifier);
        authenticator.setEnroler(new DefaultScopeEnroler());
        RoleAuthorizer authorizer = new RoleAuthorizer("RoleAuthorizer1");
        authorizer.setAuthorizedRoles(getAuthorizedRoles());
        authenticator.setNext(authorizer);
        authorizer.setNext(OAuth2TokenResource.class);
        root.attach("/protected/mode1", authenticator);


        // Use PARAMETER
        authenticator = new OAuth2Authenticator(getContext(), null, OAuth2Utils.ParameterLocation.HTTP_QUERY, tokenVerifier);
        authenticator.setEnroler(new DefaultScopeEnroler());
        authorizer = new RoleAuthorizer("RoleAuthorizer2");
        authorizer.setAuthorizedRoles(getAuthorizedRoles());
        authenticator.setNext(authorizer);
        authorizer.setNext(OAuth2TokenResource.class);
        root.attach("/protected/mode2", authenticator);


        OpenAMParameters parameters = new OpenAMParameters();
        OpenAMServletAuthenticator amauthenticator = new OpenAMServletAuthenticator(getContext(), parameters);
        amauthenticator.setNext(DemoResource.class);
        root.attach("/demo", amauthenticator);
        root.attach("/opendemo", DemoResource.class);


        return root;
    }

    protected URI getCurrentURI() {
        Object o = getContext().getAttributes().get(OAuth2DemoApplication.class.getName());
        URI root = null;

        if (o instanceof String) {
            String PATH = (String) o;
            root = URI.create(PATH.endsWith("/") ? PATH : PATH + "/");
        } else {
            Request request = Request.getCurrent();
            if (null != request) {
                HttpServletRequest servletRequest = ServletUtils.getRequest(request);
                String scheme = servletRequest.getScheme();             // http
                String serverName = servletRequest.getServerName();     // localhost
                int serverPort = servletRequest.getServerPort();        // 8080
                String contextPath = servletRequest.getContextPath();   // /openam
                String servletPath = servletRequest.getServletPath();   // /oauth2demo
                //String pathInfo = servletRequest.getPathInfo();         // /static/index.html
                //String queryString = servletRequest.getQueryString();          // d=789

                try {
                    root = new URI(scheme, null, serverName, serverPort, contextPath + servletPath + "/", null, null);

                    //TODO Find a proper solution
                    Client client = new Client(scheme);
                    if (client.isAvailable()) {
                        getConnectorService().getClientProtocols().addAll(client.getProtocols());
                    } else {
                        throw new RuntimeException("Client connector is not available");
                    }

                } catch (URISyntaxException e) {
                    // Should not happen
                }
            }
        }
        if (null == root) {
            throw new RuntimeException("OAuth2DemoApplication can not detect current context");
        }
        return root;
    }

    /**
     * Returns the modifiable list of authorized roles.
     *
     * @return The modifiable list of authorized roles.
     */
    private List<Role> getAuthorizedRoles() {
        List<Role> authorizedRoles = new ArrayList<Role>(1);
        authorizedRoles.add(new Role("read", ""));
        return authorizedRoles;
    }

    /**
     * Returns the Freemarker's configuration.
     *
     * @return The Freemarker's configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }
}
