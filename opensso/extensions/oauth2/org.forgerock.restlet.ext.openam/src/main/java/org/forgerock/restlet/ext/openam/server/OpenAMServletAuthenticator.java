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
package org.forgerock.restlet.ext.openam.server;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import org.forgerock.restlet.ext.openam.server.AbstractOpenAMAuthenticator;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.security.Enroler;

import javax.servlet.http.HttpServletRequest;


/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class OpenAMServletAuthenticator extends AbstractOpenAMAuthenticator {

    public OpenAMServletAuthenticator(Context context, Reference openamServer) {
        super(context, openamServer);
    }

    public OpenAMServletAuthenticator(Context context, Reference openamServer, boolean optional) {
        super(context, openamServer, optional);
    }

    public OpenAMServletAuthenticator(Context context, Reference openamServer, boolean multiAuthenticating, boolean optional, Enroler enroler) {
        super(context, openamServer, multiAuthenticating, optional, enroler);
    }

    public OpenAMServletAuthenticator(Context context, Reference openamServer, boolean optional, Enroler enroler) {
        super(context, openamServer, optional, enroler);
    }


    @Override
    protected SSOToken getToken(Request request, Response response) throws SSOException {
        SSOToken token = null;
        HttpServletRequest servletRequest = ServletUtils.getRequest(request);
        if (null != servletRequest) {
            SSOTokenManager manager = SSOTokenManager.getInstance();
            token = manager.createSSOToken(servletRequest);
        }
        return token;
    }
}
