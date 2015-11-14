/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2015 ForgeRock AS.
 */

package org.forgerock.openam.oauth2;

import static com.sun.identity.shared.DateUtils.*;
import static org.forgerock.oauth2.core.OAuth2Constants.Params.*;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.authentication.AuthContext;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdUtils;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;
import java.security.AccessController;
import java.text.ParseException;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import org.forgerock.oauth2.core.OAuth2Constants;
import org.forgerock.oauth2.core.OAuth2Request;
import org.forgerock.oauth2.core.ResourceOwner;
import org.forgerock.oauth2.core.ResourceOwnerAuthenticator;
import org.forgerock.oauth2.core.exceptions.NotFoundException;
import org.forgerock.openam.utils.RealmNormaliser;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.resource.ResourceException;

/**
 * Authenticates a resource owner from the credentials provided on the request.
 *
 * @since 12.0.0
 */
@Singleton
public class OpenAMResourceOwnerAuthenticator implements ResourceOwnerAuthenticator {

    private final Debug logger = Debug.getInstance("amOpenAMResourceOwnerAuthenticator");
    private final RealmNormaliser realmNormaliser;

    /**
     * Constructs a new OpenAMResourceOwnerAuthenticator.
     *
     * @param realmNormaliser An instance of the RealmNormaliser.
     */
    @Inject
    public OpenAMResourceOwnerAuthenticator(RealmNormaliser realmNormaliser) {
        this.realmNormaliser = realmNormaliser;
    }

    /**
     * {@inheritDoc}
     */
    public ResourceOwner authenticate(OAuth2Request request, boolean useSession) throws NotFoundException {
        SSOToken token = null;
        try {
            SSOTokenManager mgr = SSOTokenManager.getInstance();
            token = mgr.createSSOToken(ServletUtils.getRequest(request.<Request>getRequest()));
        } catch (Exception e){
            logger.warning("No SSO Token in request", e);
        }
        if (token == null || !useSession) {
            final String username = request.getParameter(USERNAME);
            final char[] password = request.getParameter(PASSWORD) == null ? null :
                    request.<String>getParameter(PASSWORD).toCharArray();
            final String realm = realmNormaliser.normalise(request.<String>getParameter(OAuth2Constants.Custom.REALM));
            final String authChain = request.getParameter(AUTH_CHAIN);
            return authenticate(username, password, realm, authChain);
        } else {
            try {
                final AMIdentity id = IdUtils.getIdentity(
                        AccessController.doPrivileged(AdminTokenAction.getInstance()),
                        token.getProperty(Constants.UNIVERSAL_IDENTIFIER));

                long authTime = stringToDate(token.getProperty(ISAuthConstants.AUTH_INSTANT)).getTime();

                return new OpenAMResourceOwner(id.getName(), id, authTime);
            } catch (SSOException e) {
                logger.error("Unable to create ResourceOwner", e);
            } catch (ParseException e) {
                logger.error("Unable to create ResourceOwner", e);
            } catch (IdRepoException e) {
                logger.error("Unable to create ResourceOwner", e);
            }
        }
        return null;
    }

    private ResourceOwner authenticate(String username, char[] password, String realm, String service) {

        ResourceOwner ret = null;
        AuthContext lc = null;
        try {
            lc = new AuthContext(realm);
            if (service != null) {
                lc.login(AuthContext.IndexType.SERVICE, service, null, ServletUtils.getRequest(Request.getCurrent()),
                        ServletUtils.getResponse(Response.getCurrent()));
            } else {
                lc.login(ServletUtils.getRequest(Request.getCurrent()), ServletUtils.getResponse(Response.getCurrent()));
            }

            while (lc.hasMoreRequirements()) {
                Callback[] callbacks = lc.getRequirements();
                ArrayList missing = new ArrayList();
                // loop through the requires setting the needs..
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof NameCallback) {
                        NameCallback nc = (NameCallback) callbacks[i];
                        nc.setName(username);
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback pc = (PasswordCallback) callbacks[i];
                        pc.setPassword(password);
                    } else {
                        missing.add(callbacks[i]);
                    }
                }
                // there's missing requirements not filled by this
                if (missing.size() > 0) {
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Missing requirements");
                }
                lc.submitRequirements(callbacks);
            }

            // validate the password..
            if (lc.getStatus() == AuthContext.Status.SUCCESS) {
                try {
                    // package up the token for transport..
                    ret = createResourceOwner(lc);
                } catch (Exception e) {
                    logger.error("Unable to get SSOToken", e);
                    // we're going to throw a generic error
                    // because the system is likely down..
                    throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
                }
            }
        } catch (AuthLoginException le) {
            logger.error("AuthException", le);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, le);
        } finally {
            if (lc != null && AuthContext.Status.SUCCESS.equals(lc.getStatus())) {
                try {
                    lc.logout();
                    logger.message("Logged user out.");
                } catch (AuthLoginException e) {
                    logger.error("Exception caught logging out of AuthContext after successful login", e);
                }
            }
        }
        return ret;
    }

    private ResourceOwner createResourceOwner(AuthContext authContext) throws Exception {
        SSOToken token = authContext.getSSOToken();
        final AMIdentity id = IdUtils.getIdentity(
                AccessController.doPrivileged(AdminTokenAction.getInstance()),
                token.getProperty(Constants.UNIVERSAL_IDENTIFIER));
        return new OpenAMResourceOwner(id.getName(), id);
    }
}
