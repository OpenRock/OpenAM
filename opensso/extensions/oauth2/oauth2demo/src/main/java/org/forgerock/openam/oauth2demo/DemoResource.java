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

import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.forgerock.restlet.ext.oauth2.OAuth2Utils;
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.forgerock.restlet.ext.oauth2.consumer.BearerOAuth2Proxy;
import org.forgerock.restlet.ext.oauth2.consumer.BearerToken;
import org.forgerock.restlet.ext.oauth2.consumer.OAuth2Proxy;
import org.forgerock.restlet.ext.oauth2.consumer.RequestCallbackHandler;
import org.forgerock.restlet.ext.oauth2.provider.OAuth2TokenStore;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Redirector;
import org.restlet.util.Series;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A NAME does ...
 *
 * @author Laszlo Hordos
 */
public class DemoResource extends ServerResource implements RequestCallbackHandler<BearerToken> {

    private HttpServletRequest servletRequest = null;
    private BearerOAuth2Proxy proxy = null;
    private OAuth2Utils.ParameterLocation tokenLocation = OAuth2Utils.ParameterLocation.HTTP_HEADER;
    private Collection<String> scope = null;
    private Series<Parameter> customParameters = null;
    private boolean dynamicEndpoint = false;


    private volatile boolean redirected = false;

    @Get("json")
    public Representation getStatusInfo() {
        Form parameters = getQuery();
        Map<String, Object> response = new HashMap<String, Object>();

        //Set Authentication Mode
        String mode = parameters.getFirstValue("mode");
        if (mode != null) {
            tokenLocation = Enum.valueOf(OAuth2Utils.ParameterLocation.class, mode.toUpperCase());
            if (tokenLocation == null) tokenLocation = OAuth2Utils.ParameterLocation.HTTP_HEADER;
        }

        String action = parameters.getFirstValue("action");
        if ("flush_token".equalsIgnoreCase(action)) {
            HttpSession session = getHttpServletRequest().getSession(false);
            if (null != session) {
                session.removeAttribute(BearerToken.class.getName());
            }
        }

        String scopes = parameters.getFirstValue("scope");
        if (scopes instanceof String) {
            scope = OAuth2Utils.split(scopes, " ");
        }

        dynamicEndpoint = null != parameters.getFirst("dynamicEndpoint");


        for (Parameter parameter : parameters) {
            if (parameter.getName().startsWith("oa_")) {
                if (null == customParameters) {
                    customParameters = new Form();
                }
                customParameters.add(parameter.getName().substring(3), parameter.getValue());
            }
        }


        String flow = parameters.getFirstValue("flow");
        BearerOAuth2Proxy.Flow flowType = null;
        if (null != flow) {
            flowType = Enum.valueOf(BearerOAuth2Proxy.Flow.class, flow.toUpperCase());
        }
        if (null == flowType) {
            flowType = OAuth2Proxy.Flow.AUTHORIZATION_CODE;
        }

        try {
            Representation representation = getClientResource(getTarget(), flowType).get();
            if (redirected) {
                return getResponseEntity();
            }
            response.put("protected", new JacksonRepresentation<Map>(representation, Map.class).getObject());
        } catch (OAuthProblemException e) {
            response = e.getErrorMessage();
        } catch (ResourceException e) {
            response.put(OAuth2.Error.ERROR, e.getMessage());
            response.put(OAuth2.Error.ERROR_DESCRIPTION, e.getStatus().getDescription());
            response.put("status", e.getStatus().getCode());
        }
        return new JacksonRepresentation<Map>(response);
    }

    private Reference getTarget() {
        URI root = (URI) getContext().getAttributes().get("org.forgerock.openam.oauth2demo");
        if (OAuth2Utils.ParameterLocation.HTTP_HEADER.equals(tokenLocation)) {
            return new Reference(root.resolve("./protected/mode1"));
        }
        return new Reference(root.resolve("./protected/mode2"));
    }

    protected HttpServletRequest getHttpServletRequest() {
        if (null == servletRequest) {
            servletRequest = ServletUtils.getRequest(getRequest());
        }
        return servletRequest;
    }

    protected ClientResource getClientResource(Reference reference, OAuth2Proxy.Flow flow) {
        ClientResource clientResource = new ClientResource(getContext(), reference);
        BearerOAuth2Proxy auth2Proxy = new BearerOAuth2Proxy(getContext(), proxy, flow, this);
        auth2Proxy.setNext(clientResource.getNext());
        clientResource.setNext(auth2Proxy);
        return clientResource;
    }

    /**
     * Set-up method that can be overridden in order to initialize the state of
     * the resource. By default it does nothing.
     *
     * @see #init(org.restlet.Context, org.restlet.Request, org.restlet.Response)
     */
    protected void doInit() throws ResourceException {
        proxy = BearerOAuth2Proxy.popOAuth2Proxy(getContext());
        if (null == proxy) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Missing required context attribute: " +
                    OAuth2TokenStore.class.getName());
        }
    }


    @Override
    public BearerToken popAccessToken(Request request) {
        BearerToken token = null;
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        if (null != httpServletRequest) {
            HttpSession session = httpServletRequest.getSession(false);
            if (null != session) {
                Object o = session.getAttribute(BearerToken.class.getName());
                if (o instanceof BearerToken) {
                    token = (BearerToken) o;
                }
            }
        }
        return token;
    }

    @Override
    public void pushAccessToken(Request request, BearerToken token) {
        if (null != token) {
            HttpServletRequest httpServletRequest = getHttpServletRequest();
            if (null != httpServletRequest) {
                HttpSession session = httpServletRequest.getSession(true);
                session.setAttribute(BearerToken.class.getName(), token);
            }
        }
    }

    @Override
    public String getRedirectionEndpoint(Request request, Reference reference) {
        if (dynamicEndpoint) {
            reference.addQueryParameter("goto", getStateResourceRef().toString());
        }
        return reference.toString();
    }

    @Override
    public String getState(Request request) {
        Reference redirectRef = getStateResourceRef();
        return Base64.encode(redirectRef.normalize().toString().toCharArray(), "ISO-8859-1", false);
    }

    private Reference getStateResourceRef() {
        Reference redirectRef = new Reference(getRequest().getResourceRef());
        Form parameters = redirectRef.getQueryAsForm();
        if ("flush_token".equals(parameters.getFirstValue("action"))) {
            parameters.removeAll("action");
        }
        redirectRef.setQuery(parameters.getQueryString());
        return redirectRef;
    }

    @Override
    public OAuth2Utils.ParameterLocation getTokenLocation(Request request) {
        if (!OAuth2Utils.ParameterLocation.HTTP_HEADER.equals(tokenLocation)) {
            return OAuth2Utils.ParameterLocation.HTTP_QUERY;
        }
        return tokenLocation;
    }

    @Override
    public Collection<String> getScope(Request request, Set<String> scope) {
        return this.scope != null ? this.scope : scope;
    }

    @Override
    public Series<Parameter> decorateParameters(Series<Parameter> parameters) {
        if (null != customParameters) {
            parameters.addAll(customParameters);
        }
        return parameters;
    }

    @Override
    public OAuth2Proxy.AuthenticationStatus authorizationRedirect(Redirector redirector) {
        redirector.handle(getRequest(), getResponse());
        redirected = true;
        return OAuth2Proxy.AuthenticationStatus.REDIRECTED;
    }
}
