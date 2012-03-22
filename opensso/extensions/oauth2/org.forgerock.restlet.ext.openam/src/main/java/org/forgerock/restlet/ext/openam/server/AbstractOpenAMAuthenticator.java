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
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdUtils;
import org.forgerock.restlet.ext.openam.OpenAMUser;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Redirector;
import org.restlet.security.Authenticator;
import org.restlet.security.Enroler;

/**
 * A NAME does ...
 *
 * @author Laszlo Hordos
 */
public abstract class AbstractOpenAMAuthenticator extends Authenticator {

    private final Reference openamServer;

    public AbstractOpenAMAuthenticator(Context context, Reference openamServer) {
        super(context);
        this.openamServer = openamServer;
    }

    public AbstractOpenAMAuthenticator(Context context, Reference openamServer, boolean optional) {
        super(context, optional);
        this.openamServer = openamServer;
    }

    public AbstractOpenAMAuthenticator(Context context, Reference openamServer, boolean multiAuthenticating, boolean optional, Enroler enroler) {
        super(context, multiAuthenticating, optional, enroler);
        this.openamServer = openamServer;
    }

    public AbstractOpenAMAuthenticator(Context context, Reference openamServer, boolean optional, Enroler enroler) {
        super(context, optional, enroler);
        this.openamServer = openamServer;
    }


    protected abstract SSOToken getToken(Request request, Response response) throws SSOException;


    @Override
    protected boolean authenticate(Request request, Response response) {
        try {
            SSOToken token = getToken(request, response);
            if (null != token) {
                AMIdentity identity = IdUtils.getIdentity(token);

                OpenAMUser user = new OpenAMUser(identity.getName(), identity.getRealm(), identity.getUniversalId(),
                        token);
                request.getClientInfo().setUser(user);
                return identity.isActive();
            }
        } catch (SSOException e) {
            redirect(request, response);
        } catch (IdRepoException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
        }
        return false;
    }

    protected void redirect(Request request, Response response) {
        Form parameters = request.getResourceRef().getQueryAsForm();
        String realm = parameters.getFirstValue("realm");

        Reference amserver = new Reference(openamServer);
        if (null != realm && realm.trim().length() > 0) {
            amserver.addQueryParameter("realm", realm);
        }
        amserver.addQueryParameter("goto", request.getResourceRef().toString());

        Redirector redirector = new Redirector(getContext(), amserver.toString(), Redirector.MODE_CLIENT_FOUND);
        redirector.handle(request, response);
    }
}