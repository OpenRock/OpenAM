/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
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
 *
 */

package org.forgerock.openam.authentication.modules.oauth2;

import com.iplanet.sso.SSOToken;
import com.sun.identity.shared.debug.Debug;
import java.util.*;
import com.sun.identity.authentication.spi.AMPostAuthProcessInterface;
import com.sun.identity.authentication.spi.AuthenticationException;
import com.sun.identity.authentication.util.ISAuthConstants;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>OAuth2PostAuthnPlugin</code> implements
 * AMPostAuthProcessInterface interface for authentication
 * post processing. This class can only be used for the OAuth2 authentication
 * module.
 * 
 * The post processing class can be assigned per ORGANIZATION or SERVICE
 */
public class OAuth2PostAuthnPlugin implements AMPostAuthProcessInterface {

    private static Debug debug = Debug.getInstance("amAuth");

    /** Post processing on successful authentication.
     * @param requestParamsMap - map contains HttpServletRequest parameters
     * @param request HttpServletRequest object
     * @param response HttpServletResponse object
     * @param ssoToken  authenticated user's ssoToken
     * @exception Authentication Exception when there is an error
     */
    public void onLoginSuccess(Map requestParamsMap,
            HttpServletRequest request,
            HttpServletResponse response,
            SSOToken ssoToken)
            throws AuthenticationException {

        debug.message("OAuth2PostAuthnPlugin:onLoginSuccess called");
        try {
            
            String oauth2 = ssoToken.getProperty("OAUTH2");

            debug.message("OAuth2PostAuthnPlugin: OAUTH2 is: " + oauth2);

            if (oauth2.equalsIgnoreCase("1")) {
                String orig_url = OAuthUtil.findCookie(request, OAuth.COOKIE_ORIG_URL);
                ssoToken.setProperty(ISAuthConstants.FULL_LOGIN_URL,orig_url );
                debug.message("OAuth2PostAuthnPlugin: Proprety ISAuthConstants.FULL_LOGIN_URL "
                        + "set to " + orig_url);
                OAuthUtil.deleteCookie(OAuth.COOKIE_ORIG_URL, 
                        request.getServerName(), "/");
            }
        } catch (Exception ex) {
            debug.message("OAuth2PostAuthnPlugin: onLoginSuccess exception while setting properties :", ex);
        }
    }

    /** Post processing on failed authentication.
     * @param requestParamsMap - map contains HttpServletRequest parameters
     * @param request HttpServletRequest object
     * @param response HttpServletResponse object
     * @exception AuthenticationException when there is an error
     */
    public void onLoginFailure(Map requestParamsMap,
            HttpServletRequest req,
            HttpServletResponse res)
            throws AuthenticationException {
        debug.message("OAuth2PostAuthnPlugin:onLoginFailure called");

    }

    /** Post processing on Logout.
     * @param requestParamsMap - map contains HttpServletRequest parameters
     * @param request HttpServletRequest object
     * @param response HttpServletResponse object
     * @param ssoToken - user's session
     */
    public void onLogout(HttpServletRequest request,
            HttpServletResponse response,
            SSOToken ssoToken)
            throws AuthenticationException {
        
        debug.message("OAuth2PostAuthnPlugin:onLogout called " + request.getRequestURL());
        String gotoParam = request.getParameter("goto");

        try {
//            String oauth2 = ssoToken.getProperty(OAuth.SESSION_OAUTH2);
//            debug.message("OAuth2PostAuthnPlugin: OAUTH2 is: " + oauth2);
            String loginURL = ssoToken.getProperty(
                    ISAuthConstants.FULL_LOGIN_URL);
            String accessToken = ssoToken.getProperty(OAuth.SESSION_OAUTH_TOKEN);

            debug.message("OAuth2PostAuthnPlugin: OAUTH2 Token is: " + accessToken);
            String logoutBehaviour = ssoToken.getProperty(OAuth.SESSION_LOGOUT_BEHAVIOUR);
            if (logoutBehaviour.equalsIgnoreCase("donotlogout")) {
                return;
            }
            
            if (accessToken != null && !accessToken.isEmpty()) {
                debug.message("OAuth2PostAuthnPlugin: OAuth2 logout");

                String logoutURL =
                        OAuthUtil.findCookie(request, OAuth.COOKIE_LOGOUT_URL);

                if (logoutURL.toLowerCase().contains("facebook")) {
                    debug.message("OAuth2PostAuthnPlugin: facebook");
                    String origUrl = OAuthUtil.encodeUriToRedirect(loginURL);
                    String query = "";
                    // Non encrypted token ?
                    if (accessToken.contains("\\|")) {
                        String[] tokenParts = accessToken.split("\\|");
                        String api_key = tokenParts[0];
                        String session_key = tokenParts[1];
                        query = "api_key=" + api_key
                                + "&session_key=" + session_key + "&next=" + origUrl;
                    } else { // Encrypted token
                        query = "next=" + origUrl + "&access_token=" + accessToken;
                    }
                    logoutURL = URLEncoder.encode(logoutURL + "?" + query, "UTF-8");
                }

                logoutURL = "/openam/logoutOAuth.jsp?logoutURL=" + logoutURL;
                
                if (logoutBehaviour.equalsIgnoreCase("logout")) {
                    logoutURL += "&loggedout=logmeout";
                }
                
                if (gotoParam != null && !gotoParam.isEmpty()) {
                    logoutURL = logoutURL + "&goto=" + gotoParam;
                } 
                
                debug.message("OAuth2PostAuthnPlugin: redirecting to: "
                        + logoutURL);

                request.setAttribute(AMPostAuthProcessInterface.POST_PROCESS_LOGOUT_URL,
                        logoutURL);
            }
        } catch (Exception ex) {
            debug.message("OAuth2PostAuthnPlugin: onLogout exception "
                    + "while setting properties :", ex);
        }

    }
}
