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
package org.forgerock.restlet.ext.oauth2.flow;

import org.fest.assertions.Condition;
import org.fest.assertions.MapAssert;
import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.testng.annotations.Test;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class AuthorizationCodeServerResourceTest extends AbstractFlowTest {
    @Test
    public void testValidRequest() throws Exception {
        Reference reference = new Reference("riap://component/test/oauth2/authorize");
        ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");

        Request request = new Request(Method.GET, reference);
        request.setChallengeResponse(cr);
        Response response = new Response(request);
        reference.addQueryParameter(OAuth2.Params.RESPONSE_TYPE, OAuth2.AuthorizationEndpoint.CODE);
        reference.addQueryParameter(OAuth2.Params.CLIENT_ID, "cid");
        reference.addQueryParameter(OAuth2.Params.REDIRECT_URI, "");
        reference.addQueryParameter(OAuth2.Params.SCOPE, "read write");
        reference.addQueryParameter(OAuth2.Params.STATE, "random");


        //handle
        getClient().handle(request, response);
        assertEquals(response.getStatus(), Status.SUCCESS_OK);
        assertTrue(response.getEntity() instanceof TemplateRepresentation);
        assertTrue(MediaType.TEXT_HTML.equals(response.getEntity().getMediaType()));

        reference = new Reference("riap://component/test/oauth2/authorize");
        request = new Request(Method.POST, reference);
        request.setChallengeResponse(cr);
        response = new Response(request);

        Form parameters = new Form();
        parameters.add(OAuth2.Params.RESPONSE_TYPE, OAuth2.AuthorizationEndpoint.CODE);
        parameters.add(OAuth2.Params.CLIENT_ID, "cid");
        parameters.add(OAuth2.Params.REDIRECT_URI, "http://localhost:8080/oauth2/cb");
        parameters.add(OAuth2.Params.SCOPE, "read write");
        parameters.add(OAuth2.Params.STATE, "random");
        parameters.add(OAuth2.Custom.DECISION, OAuth2.Custom.ALLOW);
        request.setEntity(parameters.getWebRepresentation());

        //handle
        getClient().handle(request, response);
        assertEquals(response.getStatus(), Status.REDIRECTION_FOUND);
        Form fragment = response.getLocationRef().getQueryAsForm();

        //assert
        assertThat(fragment.getValuesMap()).includes(
                MapAssert.entry(OAuth2.Params.STATE, "random")).is(new Condition<Map<?, ?>>() {
            @Override
            public boolean matches(Map<?, ?> value) {
                return value.containsKey(OAuth2.Params.CODE);
            }
        });

        reference = new Reference("riap://component/test/oauth2/access_token");
        request = new Request(Method.POST, reference);
        response = new Response(request);

        parameters = new Form();
        parameters.add(OAuth2.Params.GRANT_TYPE, OAuth2.TokeEndpoint.AUTHORIZATION_CODE);
        parameters.add(OAuth2.Params.CODE, fragment.getFirstValue(OAuth2.Params.CODE));
        parameters.add(OAuth2.Params.REDIRECT_URI, "http://localhost:8080/oauth2/cb");
        request.setEntity(parameters.getWebRepresentation());


        //handle
        getClient().handle(request, response);
        assertTrue(MediaType.APPLICATION_JSON.equals(response.getEntity().getMediaType()));
        JacksonRepresentation<Map> representation = new JacksonRepresentation<Map>(response.getEntity(), Map.class);

        //assert
        assertThat(representation.getObject()).includes(
                MapAssert.entry(OAuth2.Params.TOKEN_TYPE, OAuth2.Bearer.BEARER),
                MapAssert.entry(OAuth2.Params.EXPIRES_IN, 3600)).is(new Condition<Map<?, ?>>() {
            @Override
            public boolean matches(Map<?, ?> value) {
                return value.containsKey(OAuth2.Params.ACCESS_TOKEN);
            }
        });

    }

    @Test
    public void testToken() throws Exception {

    }
}
