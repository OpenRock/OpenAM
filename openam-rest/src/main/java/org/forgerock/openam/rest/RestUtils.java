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
 * Copyright 2012-2015 ForgeRock AS.
 */

package org.forgerock.openam.rest;

import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.iplanet.am.util.SystemProperties;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.delegation.DelegationEvaluator;
import com.sun.identity.delegation.DelegationEvaluatorImpl;
import com.sun.identity.delegation.DelegationException;
import com.sun.identity.delegation.DelegationPermission;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdType;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.session.util.SessionUtils;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.sm.SMSException;
import com.sun.identity.sm.ServiceManager;
import org.forgerock.services.context.Context;
import org.forgerock.json.resource.ForbiddenException;
import org.forgerock.json.resource.NotSupportedException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.http.HttpContext;
import org.forgerock.openam.utils.CollectionUtils;
import org.forgerock.util.promise.Promise;

/**
 * A collection of ForgeRock-REST based utility functions.
 */
final public class RestUtils {

    private static final Debug debug = Debug.getInstance("frRest");

    /**
     * Lazy initialization holder idiom.
     */
    private static final class AdminUserIdHolder {
        static final AMIdentity adminUserId;
        static {
            final SSOToken adminToken = getToken();
            final String adminUser = SystemProperties.get(Constants.AUTHENTICATION_SUPER_USER);
            if (adminUser != null) {
                adminUserId = new AMIdentity(adminToken, adminUser, IdType.USER, "/", null);
            } else {
                adminUserId = null;
                debug.error("SystemProperties AUTHENTICATION_SUPER_USER not set");
            }
        }
    }

    /**
     * Returns TokenID from headers
     *
     * @param context Context which contains the headers.
     * @return String with TokenID
     */
    static public String getCookieFromServerContext(Context context) {
        return ServerContextHelper.getCookieFromServerContext(context);
    }

    public static boolean isAdmin(Context context) {
        boolean isAdmin = false;
        try {
            String realm = context.asContext(RealmContext.class).getResolvedRealm();
            SSOToken userSSOToken = SSOTokenManager.getInstance().createSSOToken(getCookieFromServerContext(context));

            // Simple check to see if user is super user and if so dont need to perform delegation check
            if (SessionUtils.isAdmin(AccessController.doPrivileged(AdminTokenAction.getInstance()), userSSOToken)) {
                return true;
            }

            DelegationEvaluator delegationEvaluator = new DelegationEvaluatorImpl();
            DelegationPermission delegationPermission = new DelegationPermission();
            delegationPermission.setVersion("*");
            delegationPermission.setSubConfigName("default");
            delegationPermission.setOrganizationName(realm);
            delegationPermission.setActions(CollectionUtils.asSet("READ"));

            for (Iterator i = getServiceNames().iterator(); i.hasNext() && !isAdmin; ) {
                String name = (String) i.next();
                delegationPermission.setServiceName(name);
                isAdmin = delegationEvaluator.isAllowed(userSSOToken, delegationPermission,
                        Collections.<String, Set<String>>emptyMap());
            }
        } catch (DelegationException | SSOException | SMSException e) {
            debug.error("RestUtils::Failed to determine if user is an admin", e);
        }

        return isAdmin;
    }

    private static Set<String> getServiceNames() throws SMSException, SSOException {
        SSOToken adminSSOToken = AccessController.doPrivileged(AdminTokenAction.getInstance());
        ServiceManager sm = new ServiceManager(adminSSOToken);
        return sm.getServiceNames();
    }

    static public void hasPermission(final Context context) throws SSOException, IdRepoException, ForbiddenException {
        SSOTokenManager mgr = SSOTokenManager.getInstance();
        SSOToken ssotok = mgr.createSSOToken(getCookieFromServerContext(context));
        mgr.validateToken(ssotok);
        mgr.refreshSession(ssotok);
        AMIdentity amIdentity = new AMIdentity(ssotok);

        if (!(amIdentity.equals(AdminUserIdHolder.adminUserId))) {
            debug.error("Unauthorized user.");
            throw new ForbiddenException("Access Denied");
        }
    }

    /**
     * Signals to the handler that the current operation is unsupported.
     */
    public static <T> Promise<T, ResourceException> generateUnsupportedOperation() {
        return new NotSupportedException("Operation is not supported.").asPromise();
    }

    /**
     * Parses out deployment url
     * @param deploymentURL
     */
    public static StringBuilder getFullDeploymentURI(final String deploymentURL) throws URISyntaxException{

        // get URI
        String deploymentURI = null;
        URI uriHold = new URI(deploymentURL);
        String uri = uriHold.getPath();
        //Parse out the deployment URI
        int firstSlashIndex = uri.indexOf("/");
        int secondSlashIndex = uri.indexOf("/", firstSlashIndex + 1);
        if (secondSlashIndex != -1) {
            deploymentURI = uri.substring(0, secondSlashIndex);
        }
        //Build string that consist of protocol,host,port, and deployment uri
        StringBuilder fullDepURL = new StringBuilder(100);
        fullDepURL.append(uriHold.getScheme()).append("://")
                .append(uriHold.getHost()).append(":")
                .append(uriHold.getPort())
                .append(deploymentURI);
        return fullDepURL;
    }

    /**
     * Gets an SSOToken for Administrator
     * @return
     */
    public static SSOToken getToken() {
        return AccessController.doPrivileged(AdminTokenAction.getInstance());
    }

    /**
     * Returns the value of the named header field from the request, decoding it if it is mime-encoded. If the header
     * is not mime-encoded then it is returned as-is. If no such header is present, then {@code null} is returned. If
     * there are multiple values for the header, then the first value is returned.
     *
     * @param serverContext the context of the request. Must contain a {@link HttpContext}.
     * @param headerName the name of the header to get.
     * @return the decoded header value, or {@code null} if no such header exists in the request.
     *
     * @see <a href="https://tools.ietf.org/html/rfc2047">RFC 2047: MIME Part 3: Message Header Extensions for Non-ASCII
     * Text</a>
     */
    public static String getMimeHeaderValue(final Context serverContext, final String headerName) {
        final HttpContext httpContext = serverContext.asContext(HttpContext.class);
        final String headerValue = httpContext.getHeaderAsString(headerName);
        try {
            return headerValue == null ? null : MimeUtility.decodeText(headerValue);
        } catch (UnsupportedEncodingException ex) {
            if (debug.warningEnabled()) {
                debug.warning("Unable to decode mime header: " + ex);
            }
            return headerValue;
        }
    }
}
