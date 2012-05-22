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
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.restlet.ext.oauth2.flow;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.*;

import java.util.Map;

import org.fest.assertions.Condition;
import org.fest.assertions.MapAssert;
import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.forgerock.restlet.ext.oauth2.consumer.BearerOAuth2Proxy;
import org.forgerock.restlet.ext.oauth2.consumer.BearerToken;
import org.forgerock.restlet.ext.oauth2.consumer.RequestFactory.AuthorizationCodeRequest;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.testng.annotations.Test;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class AuthorizationCodeServerResourceTest extends AbstractFlowTest {
    @Test
    public void testValidRequest() throws Exception {

        BearerOAuth2Proxy auth2Proxy = BearerOAuth2Proxy.popOAuth2Proxy(component.getContext());
        assertNotNull(auth2Proxy);

        AuthorizationCodeRequest factory =
                auth2Proxy.getAuthorizationCodeRequest().setClientId("cid").setRedirectUri(
                        auth2Proxy.getRedirectionEndpoint().toString()).setState("random");
        factory.getScope().add("read");
        factory.getScope().add("write");

        Request request = factory.buildRequest();
        ChallengeResponse resource_owner =
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        request.setChallengeResponse(resource_owner);
        Response response = new Response(request);

        // handle
        getClient().handle(request, response);
        assertTrue(response.getStatus().isSuccess());
        assertTrue(response.getEntity() instanceof TemplateRepresentation);
        assertTrue(MediaType.TEXT_HTML.equals(response.getEntity().getMediaType()));

        request = new Request(Method.POST, auth2Proxy.getAuthorizationEndpoint());
        request.setChallengeResponse(resource_owner);
        response = new Response(request);

        Form parameters = new Form();
        parameters.add(OAuth2.Params.RESPONSE_TYPE, OAuth2.AuthorizationEndpoint.CODE);
        parameters.add(OAuth2.Params.CLIENT_ID, auth2Proxy.getClientId());
        parameters.add(OAuth2.Params.REDIRECT_URI, auth2Proxy.getRedirectionEndpoint().toString());
        parameters.add(OAuth2.Params.SCOPE, "read write");
        parameters.add(OAuth2.Params.STATE, "random");
        parameters.add(OAuth2.Custom.DECISION, OAuth2.Custom.ALLOW);
        request.setEntity(parameters.getWebRepresentation());

        // handle
        getClient().handle(request, response);
        assertEquals(response.getStatus(), Status.REDIRECTION_FOUND);
        Form fragment = response.getLocationRef().getQueryAsForm();

        // assert
        assertThat(fragment.getValuesMap())
                .includes(MapAssert.entry(OAuth2.Params.STATE, "random")).is(
                        new Condition<Map<?, ?>>() {
                            @Override
                            public boolean matches(Map<?, ?> value) {
                                return value.containsKey(OAuth2.Params.CODE);
                            }
                        });

        BearerToken token =
                auth2Proxy.flowAuthorizationToken(fragment.getFirstValue(OAuth2.Params.CODE));
        assertNotNull(token);

    }

    @Test
    public void testToken() throws Exception {

    }
}
