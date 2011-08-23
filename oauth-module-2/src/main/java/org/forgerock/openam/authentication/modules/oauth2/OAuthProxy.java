/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 * Copyright © 2011 Cybernetica AS.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.Base64;
import com.sun.identity.shared.encode.CookieUtils;

import com.sun.identity.authentication.service.AuthD;
import com.sun.identity.authentication.service.AuthUtils;

import com.iplanet.dpro.session.SessionID;
import com.iplanet.dpro.session.share.SessionEncodeURL;

/*
 * OAuth module specific Get2Post gateway. 
 * In some conditions OpenAM would prefer POST method over GET.
 * OAuthProxy is more like workaround over some specific scenarios,
 * that did not work. OAuthProxy may not be needed with future
 * versions of OpenAM.
 */
public class OAuthProxy implements OAuthParam {

    private static Debug debug = Debug.getInstance(OAuthConf.MODULE_NAME);

    public static String toPostForm(HttpServletRequest req,
            HttpServletResponse res) {
        if (debug.messageEnabled()) {
            debug.message("toPostForm: started");
        }
 
        String action = req.getParameter(PARAM_LOGIN_URL);
        if (OAuthUtil.isEmpty(action)) {
            return getError(PARAM_LOGIN_URL + " parameter is empty!"); 
        }

        StringBuffer html = new StringBuffer();
        try {
            String gourl = req.getParameter(PARAM_GOTO);
            if (!OAuthUtil.isEmpty(gourl)) {
                action = action + "?" + PARAM_GOTO + "="
                    + URLEncoder.encode(gourl, "UTF-8");
            }
            
            String onLoad = "document.postform.submit()";

            html.append("<html>\n").append("<body onLoad=\"")
                .append(onLoad).append("\">\n");
            html.append("<form name=\"postform\" action=\"")
                .append(action).append("\" method=\"post\">\n");

            String response = req.getParameter(PARAM_EXPECT_RESPONSE);
            if (!OAuthUtil.isEmpty(response)) {
                html.append(input(PARAM_EXPECT_RESPONSE, response));
            }

            String realm = req.getParameter(PARAM_REALM);
            if (!OAuthUtil.isEmpty(realm)) {
                html.append(input(PARAM_REALM, realm));
            }

            String module = req.getParameter(PARAM_MODULE);
            if (!OAuthUtil.isEmpty(module)) {
                html.append(input(PARAM_MODULE, module));
            }

            String code = req.getParameter(PARAM_CODE);
            if (!OAuthUtil.isEmpty(code)) {
                html.append(input(PARAM_CODE, code));
            }

            Integer oauthModuleChoiceIndex = OAuthUtil.getModuleChoiceIndex(
                    req, realm, OAuthConf.MODULE_NAME);
            if (oauthModuleChoiceIndex != null) {
                // We will have oauthModuleChoiceIndex when we have AMAuthCookie.
                // Icon-initiated (or link-initiated) session upgrades make use
                // of AMAuthCookie, Choice-form initiated (or post-form initiated)
                // auth module upgrades destroy AMAuthCookie (because we somehow
                // must redirect to external IdP and currently we use
                // LoginFailureURL with LOGIN_IGNORE return code for this purpose).
                html.append(input("IDToken0",
                        oauthModuleChoiceIndex.toString()));
            }
        } catch (Exception e) {
            return getError(e.getMessage());
        }

        html.append("</form>\n").append("</body>\n").append("</html>\n");

        debug.message("form html:\n" + html);

        return html.toString();
    }
   
    private static StringBuffer input(String name, String value) {
        return new StringBuffer()
            .append("<input type=\"hidden\" name=\"")
            .append(name).append("\" value=\"")
            .append(value).append("\"/>\n");
    }
 
    private static String getError(String message) {
        StringBuffer html = new StringBuffer();
        html.append("<html>\n").append("<body>\n")
            .append("<h1>\n").append(message).append("</h1>\n")
            .append("</body>\n").append("</html>\n");
        return html.toString();
    }

    private static void clearAuthCookie(HttpServletResponse res) {
        Cookie cookie = AuthUtils.createCookie(
                AuthUtils.getAuthCookieName(), "LOGOUT", null);

        cookie.setMaxAge(0);
        res.addCookie(cookie);
    }

    private static void clearSessID(SessionID sessionID) {
        debug.message("clearing sessionID: " + sessionID.toString());
        AuthUtils.removeAuthContext(sessionID);
        AuthD.getAuth().destroySession(sessionID);
    }
}
