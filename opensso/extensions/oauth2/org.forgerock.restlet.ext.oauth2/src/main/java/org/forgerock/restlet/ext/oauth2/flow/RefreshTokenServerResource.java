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

import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.forgerock.restlet.ext.oauth2.OAuth2Utils;
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.forgerock.restlet.ext.oauth2.model.AccessToken;
import org.forgerock.restlet.ext.oauth2.model.RefreshToken;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class RefreshTokenServerResource extends AbstractFlow {


    @Post("form:json")
    public Representation represent(Representation entity) {
        /*
  o  require client authentication for confidential clients or for any
     client that was issued client credentials (or with other
     authentication requirements),
  o  authenticate the client if client authentication is included and
     ensure the refresh token was issued to the authenticated client,
     and
  o  validate the refresh token.
        */

        client = getAuthenticatedClient();
        String refresh_token = OAuth2Utils.getRequestParameter(getRequest(), OAuth2.Params.REFRESH_TOKEN, String.class);
        //Find Token
        RefreshToken refreshToken = null;


        if (null == refreshToken) {
            throw OAuthProblemException.OAuthError.INVALID_REQUEST.handle(getRequest(), "RefreshToken does not exist");
        } else if (!refreshToken.getClient().getClientId().equals(client.getClient().getClientId())) {
            //TODO throw Exception
            throw OAuthProblemException.OAuthError.INVALID_REQUEST.handle(getRequest(), "Token was issued to a different client");
        } else {
            //TODO validate the refresh token.
            if (refreshToken.getExpireTime() - System.currentTimeMillis() < 0) {
                //Throw expired refreshToken
            }

            //Get the requested scope
            String scope_before = OAuth2Utils.getRequestParameter(getRequest(), OAuth2.Params.SCOPE, String.class);

            //Get the granted scope
            Set<String> granted_after = refreshToken.getScope();
            //Validate the granted scope
            Set<String> checkedScope = null;//getCheckedScope(scope_after, toke.getScope(), client.getClient().defaultGrantScopes());

            //Generate Token
            AccessToken token = null;
            Map<String, Object> response = token.convertToMap();

        }

        Map<String, Object> response = new HashMap<String, Object>();
        response.put(OAuth2.Params.ACCESS_TOKEN, UUID.randomUUID().toString());
        response.put(OAuth2.Params.REFRESH_TOKEN, UUID.randomUUID().toString());
        response.put(OAuth2.Params.TOKEN_TYPE, OAuth2.Bearer.BEARER.toLowerCase());
        response.put(OAuth2.Params.EXPIRES_IN, 3600);
        return new JacksonRepresentation<Map>(response);
    }

    @Override
    protected String[] getRequiredParameters() {
        return new String[]{OAuth2.Params.GRANT_TYPE, OAuth2.Params.REFRESH_TOKEN};
    }


}
