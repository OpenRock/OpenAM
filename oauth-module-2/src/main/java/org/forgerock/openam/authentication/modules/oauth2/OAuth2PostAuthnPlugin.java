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
 * The <code>FMPostAuthPostProcess</code> implements
 * AMPostAuthProcessInterface interface for authentication
 * post processing. This class can only be used for the Federation Manager
 * because it uses the FM FlatFile authentication  implementation.
 * However, with changes this class can be extended to support also LDAP
 * Authentication for both AM and FM.
 * The post processing class can be assigned per ORGANIZATION or SERVICE or ROLE
 */
public class OAuth2PostAuthnPlugin implements AMPostAuthProcessInterface {

    private static Debug debug = Debug.getInstance("postAuthNPlugin");

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
            String authType = ssoToken.getAuthType();

            debug.message("OAuth2PostAuthnPlugin: Auth Type is: " + authType);

            if (authType.contains("google") || authType.contains("facebook") ) {
                debug.message("OAuth2PostAuthnPlugin: in the if");
                String orig_url = OAuthUtil.findCookie(request, "ORIG_URL");
                ssoToken.setProperty(ISAuthConstants.FULL_LOGIN_URL,orig_url );
                debug.message("OAuth2PostAuthnPlugin: Proprety ISAuthConstants.FULL_LOGIN_URL "
                        + "set to " + orig_url);
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
        debug.message("OAuth2PostAuthnPlugin:onLogout called");
        try {
            String authType = ssoToken.getAuthType();

            debug.message("OAuth2PostAuthnPlugin: Auth Type is: " + authType);

            if (authType.equalsIgnoreCase("google")) {
                debug.message("OAuth2PostAuthnPlugin: in the google if");
            }
            
            if (authType.equalsIgnoreCase("facebook")) {
                debug.message("OAuth2PostAuthnPlugin: facebook");
                String origUrl = URLEncoder.encode(
                        ssoToken.getProperty(ISAuthConstants.FULL_LOGIN_URL), "UTF-8");
                String accessToken = ssoToken.getProperty("OAuthToken");
                debug.message("OAuth2PostAuthnPlugin: acessToken=" + accessToken);
                
                String [] tokenParts = accessToken.split("\\|");
                String api_key = tokenParts[0];
                String session_key = tokenParts[1];
                
                String serviceUrl = "https://www.facebook.com/logout.php";
                String query = "api_key=" +
                        api_key + "&session_key=" + session_key +"&next=" + origUrl;
                
                debug.message("OAuth2PostAuthnPlugin: redirecting to: " + 
                        serviceUrl + "?" + query);
                
                request.setAttribute( AMPostAuthProcessInterface.POST_PROCESS_LOGOUT_URL,
                        serviceUrl + "?" + query);
            }
        } catch (Exception ex) {
            debug.message("OAuth2PostAuthnPlugin: onLoginSuccess exception while setting properties :", ex);
        }

    }

}
