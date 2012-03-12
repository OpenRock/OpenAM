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
package org.forgerock.restlet.ext.oauth2.consumer;

import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.engine.header.Header;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.util.Series;

import java.util.StringTokenizer;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class BearerAuthenticatorHelper extends AuthenticatorHelper {

    public final static ChallengeScheme HTTP_OAUTH_BEARER = new ChallengeScheme("HTTP_OAUTH_BEARER", OAuth2.Bearer.BEARER,
            "OAuth 2.0 Authorization Protocol: Bearer Tokens");

    /**
     * Constructor.
     */
    public BearerAuthenticatorHelper() {
        super(BearerAuthenticatorHelper.HTTP_OAUTH_BEARER, true, true);
    }

    /**
     * Constructor.
     *
     * @param clientSide Indicates if client side authentication is supported.
     * @param serverSide Indicates if server side authentication is supported.
     */
    public BearerAuthenticatorHelper(boolean clientSide, boolean serverSide) {
        super(BearerAuthenticatorHelper.HTTP_OAUTH_BEARER, clientSide, serverSide);
    }


    /**
     * Parses an authenticate header into a challenge request. The header is
     * {@link org.restlet.engine.header.HeaderConstants#HEADER_WWW_AUTHENTICATE}.
     * <p/>
     * Values for the "scope" attribute MUST NOT include characters outside the set %x21 / %x23-5B / %x5D-7E for
     * representing scope values and %x20 for delimiters between scope values.  Values for the "error" and
     * "error_description" attributes MUST NOT include characters outside the set %x20-21 / %x23-5B / %x5D-7E.
     * Values for the "error_uri" attribute MUST conform to the URI-Reference syntax, and thus MUST NOT include
     * characters outside the set %x21 / %x23-5B / %x5D-7E.
     *
     * @param challenge   The challenge request to update.
     * @param response    The parent response.
     * @param httpHeaders The current response HTTP headers.
     */
    @Override
    public void parseRequest(ChallengeRequest challenge, Response response, Series<Header> httpHeaders) {
        /*
            All challenges defined by this specification MUST use the auth-scheme
            value "Bearer".  This scheme MUST be followed by one or more auth-
            param values.

A "realm" attribute MAY be included to indicate the scope of protection
The "scope" attribute is a space-delimited list of scope values indicating the required scope of the access token for accessing the requested resource.

            HTTP/1.1 401 Unauthorized
            WWW-Authenticate: Bearer realm="example"
            WWW-Authenticate: Bearer realm="apps", type=1, title="Login to \"apps\"", Basic realm="simple"

            And in response to a protected resource request with an
            authentication attempt using an expired access token:

            HTTP/1.1 401 Unauthorized
            WWW-Authenticate: Bearer realm="example",error="invalid_token",error_description="The access token expired"
         */
        super.parseRequest(challenge, response, httpHeaders);
    }

    @Override
    public void parseResponse(ChallengeResponse challenge, Request request, Series<Header> httpHeaders) {
        /*
        bearer
        oauth header

        token is b64token

        Authorization: Bearer realm="example" vF9dft4qmT
        Authorization: Bearer realm=example vF9dft4qmT
        Authorization: Bearer vF9dft4qmT

         */
        //super.parseResponse(challenge, request, httpHeaders);


        String raw = challenge.getRawValue();

        if (raw != null && raw.length() > 0) {
            StringTokenizer st = new StringTokenizer(raw, ",");
            String realm = st.nextToken();

            if (realm != null && realm.length() > 0) {
                int eq = realm.indexOf('=');

                if (eq > 0) {
                    String value = realm.substring(eq + 1).trim();
                    // Remove the quotes, first and last after trim...
                    challenge.setRealm(value.substring(1, value.length() - 1));
                }
            }

            Series<Parameter> params = new Form();

            while (st.hasMoreTokens()) {
                String param = st.nextToken();

                if (param != null && param.length() > 0) {
                    int eq = param.indexOf('=');

                    if (eq > 0) {
                        String name = param.substring(0, eq).trim();
                        String value = param.substring(eq + 1).trim();
                        // Remove the quotes, first and last after trim...
                        params.add(name, value.substring(1, value.length() - 1));
                    }
                }
            }

            challenge.setParameters(params);
        }

    }


}
