/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: AuthorizationFactory.java,v 1.1 2009/11/20 19:31:57 huacui Exp $
 *
 */

package com.sun.identity.oauth.service;

import com.sun.identity.oauth.service.models.RequestToken;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Hua Cui <hua.cui@Sun.COM>
 */

@Path(PathDefs.CREATE_AUTHORIZATION_PATH)
public class AuthorizationFactory implements OAuthServiceConstants {
    @Context
    private UriInfo context;


    /** Creates a new instance of AuthorizationFactory */
    public AuthorizationFactory() {
    }

    /**
     * GET method for obtaining user's consent
     * @param token OAuth token
     * @param cbk OAuth Callback URI
     * @param uid OAuth User Id
     * @return an HTTP form with content of the updated or created resource.
     */
    @GET
    @Consumes("application/xml")
    public Response createAuthorization(
            @QueryParam(OAUTH_TOKEN) String token,
            @QueryParam(OAUTH_CALLBACK) String cbk,
            @QueryParam(OAUTH_ID) String uid) {
        if (token == null)
            throw new WebApplicationException(new Throwable("No OAuth token."));
        if (cbk == null)
            throw new WebApplicationException(new Throwable("No callback URI."));
        if (uid == null)
            throw new WebApplicationException(new Throwable("No User iD."));

        // From here, we're good to go.
        OAuthResourceManager oauthResMgr = OAuthResourceManager.getInstance();
        try {
            Map<String, String> searchMap = new HashMap<String, String>();
            searchMap.put(REQUEST_TOKEN_URI, token);
            List<RequestToken> reqTokens= oauthResMgr.searchRequestTokens(searchMap);
            RequestToken rt = null;
            if ((reqTokens != null) && (!reqTokens.isEmpty())) {
                rt = reqTokens.get(0);
            }
            if (rt == null)
                throw new WebApplicationException(new Throwable("Request token invalid."));
            rt.setReqtPpalid(uid);

            oauthResMgr.updateRequestToken(rt);

            // Preparing the response.
            String resp = cbk;
            if (cbk.contains("?")) {
                resp += "&" + OAUTH_TOKEN + "=" + token;
            } else {
                resp += "?" + OAUTH_TOKEN + "=" + token;
            }
            URI respURI = new URI(resp);
            return Response.seeOther(respURI).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(AuthorizationFactory.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        } catch (Exception e) {
            Logger.getLogger(AuthorizationFactory.class.getName()).log(Level.SEVERE, null, e);
            throw new WebApplicationException(e);
        }
    }

}
