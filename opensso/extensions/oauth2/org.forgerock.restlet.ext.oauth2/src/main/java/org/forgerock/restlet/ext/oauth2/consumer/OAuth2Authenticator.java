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
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.security.Authenticator;

import java.util.Map;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public abstract class OAuth2Authenticator<V extends TokenVerifier> extends Authenticator {

    /**
     * The OAuth2 Token verifier.
     */
    private volatile V verifier;

    public OAuth2Authenticator(Context context) {
        super(context);
    }

    public OAuth2Authenticator(Context context, boolean optional) {
        super(context, optional);
    }

    public OAuth2Authenticator(Context context, boolean multiAuthenticating, boolean optional, Realm<V> realm) {
        super(context, multiAuthenticating, optional, realm != null ? realm.getEnroler() : null);
        verifier = realm != null ? realm.getVerifier() : null;
    }

    public OAuth2Authenticator(Context context, boolean optional, Realm<V> realm) {
        super(context, optional, realm != null ? realm.getEnroler() : null);
        verifier = realm != null ? realm.getVerifier() : null;
    }

    public V getVerifier() {
        return verifier;
    }

    /**
     * Attempts to authenticate the subject sending the request.
     *
     * @param request  The request sent.
     * @param response The response to update.
     * @return True if the authentication succeeded.
     */
    protected boolean authenticate(Request request, Response response) {
        try {
            Form parameters = getAuthenticationParameters(request);
            if (null != parameters && null != getVerifier()) {
                OAuth2User user = getVerifier().verify(normalizeParameters(parameters, request, response));
                //TODO Check NPE
                request.getClientInfo().setUser(user);
                return true;
            }
        } catch (OAuthProblemException e) {
            doError(request, response, e);
            //TODO handle exception. Callback {query,fragment} Response JSON object
        }
        return false;
    }

    protected abstract Map<String, Object> normalizeParameters(Form parameters, Request request, Response response) throws OAuthProblemException;

    protected abstract void doError(Request request, Response response, OAuthProblemException exception);


    /**
     * Returns the parameters to use for authentication.
     * <p/>
     * From Header - Authorization: Bearer vF9dft4qmT
     * From Header - Authorization: MAC id="h480djs93hd8",ts="1336363200",nonce="dj83hs9s",mac="bhCQXTVyfj5cmA9uKkPFx1zeOXM="
     * From Query  - ?access_token=vF9dft4qmT
     * From APPLICATION_WWW_FORM - access_token=vF9dft4qmT
     *
     * @param request The request.
     * @return The access token taken from a given request.
     */
    protected Form getAuthenticationParameters(Request request) throws OAuthProblemException {
        Form result = null;
        // Use the parameters which was populated with the AuthenticatorHelper
        if (request.getChallengeResponse() != null) {
            result = new Form(request.getChallengeResponse().getParameters());
            getLogger().fine("Found Authorization header" + result.getFirst(OAuth2.Params.ACCESS_TOKEN));
        }
        if ((result == null)) {
            getLogger().fine("No Authorization header - checking query");
            result = request.getOriginalRef().getQueryAsForm();
            getLogger().fine("Found Token in query" + result.getFirst(OAuth2.Params.ACCESS_TOKEN));

            // check body if all else fail:
            if (result == null) {
                if ((request.getMethod() == Method.POST)
                        || (request.getMethod() == Method.PUT)
                        || (request.getMethod() == Method.DELETE)) {
                    Representation r = request.getEntity();
                    if ((r != null) && MediaType.APPLICATION_WWW_FORM.equals(r.getMediaType())) {
                        // Search for an OAuth Token
                        result = new Form(r);
                        // restore the entity body
                        request.setEntity(result.getWebRepresentation());

                    }
                }
            }
        }
        return result;
    }
}
