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


import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;

import com.iplanet.dpro.session.SessionID;
import com.iplanet.dpro.session.share.SessionEncodeURL;

import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.Base64;
import com.sun.identity.shared.encode.CookieUtils;
import com.sun.identity.shared.locale.AMResourceBundleCache;
import com.sun.identity.shared.locale.Locale;

import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.authentication.util.AMAuthUtils;
import com.sun.identity.authentication.server.AuthContextLocal;
import com.sun.identity.authentication.service.AuthD;
import com.sun.identity.authentication.service.AuthUtils;
import com.sun.identity.authentication.service.AuthException;
import javax.servlet.http.Cookie;

public class OAuthUtil implements OAuthParam {

    private static Debug debug = Debug.getInstance(OAuthConf.MODULE_NAME);
    private static AMResourceBundleCache amCache =
        AMResourceBundleCache.getInstance();
    
    private static java.util.Locale locale = java.util.Locale.US;

    static boolean isEmpty(String value) {
        return value == null || "".equals(value);
    }

    static String getUrl(String url, boolean encodeBase64)
            throws UnsupportedEncodingException {
        if (url == null || "".equals(url)) {
            return null;
        }

        String decoded = null, encoded = null;

        try {
            decoded = new URL(url).toString();
            encoded = Base64.encode(decoded.getBytes("UTF-8"));
        } catch (MalformedURLException mue) {
            decoded = new String(Base64.decode(url), "UTF-8");
            encoded = url;
        }

        if (debug.messageEnabled()) {
            debug.message("base64 encoded url: " + encoded);
            debug.message("base64 decoded url: " + decoded);
        }

        if (encodeBase64) {
            return encoded;
        }
        return decoded;
    }

    static Integer getModuleChoiceIndex(HttpServletRequest req,
            String realm, String module) throws AuthException {
        String realmQualifiedModuleName =
            getRealmQualifiedModuleName(
                    realm == null ? "/" : realm, module);

        AuthContextLocal ac = getAC(req);
        if (ac == null) {
            debug.error("getModuleChoiceIndex: AuthContextLocal not found");
            return null;
        }

        Callback[] cb = AuthUtils.getRecdCallback(ac);

        ChoiceCallback cc = getChoiceCallback(cb);
        if (cc == null) {
           debug.error("getModuleChoiceIndex: ChoiceCallback "
                   + "not found for module " + module);
           return null;
        }

        return getChoiceIndex(cc.getChoices(),
                realmQualifiedModuleName);
    }

    private static String getRealmQualifiedModuleName(
            String realm, String moduleName) {
        java.util.ResourceBundle rb =
                amCache.getResBundle(OAuthConf.BUNDLE_NAME, locale);
        // it is more correct to retrieve localized name
        String locModuleName = Locale.getString(rb, moduleName, debug);

        return AMAuthUtils.toRealmQualifiedAuthnData(realm, locModuleName);
    }

    private static ChoiceCallback getChoiceCallback(Callback[] cb) {
        for (int i = 0; i < cb.length; i++) {
            if (cb[i] instanceof ChoiceCallback) {
                return ((ChoiceCallback) cb[i]);
            }
        }
        return null;
    }

    private static Integer getChoiceIndex(String[] choices,
            String realmQualifiedModuleName) {
        for (int i = 0; i < choices.length; i++) {
            if (realmQualifiedModuleName.equals(choices[i])) {
                return new Integer(i);
            }
        }
        if (debug.messageEnabled()) {
            debug.message("getChoiceIndex: " 
                + realmQualifiedModuleName + " not found");
        }
        return null;
    }

    private static AuthContextLocal getAC(HttpServletRequest req)
            throws AuthException {
        SessionID sid = getAuthCookieSid(req);
        com.iplanet.dpro.session.service.InternalSession is =
            AuthD.getSession(sid);

        if (is == null) {
            debug.error("InternalSession not found for sid " + sid);
            return null;    
        }
        return (AuthContextLocal) is.getObject(
                ISAuthConstants.AUTH_CONTEXT_OBJ);
    }

    // XXX: almost copy-paste from private method AuthClientUtils.getSidFromCookie()
    static SessionID getAuthCookieSid(HttpServletRequest req) {
        SessionID sessionID = null;
        String name = AuthUtils.getAuthCookieName();
        String sid =
            CookieUtils.getCookieValueFromReq(req, name);
        if (sid == null) {
            sid = SessionEncodeURL.getSidFromURL(req, name);
        }
        if (sid != null) {
            sessionID = new SessionID(sid);
            debug.message("getAuthCookieSid: found sid = " + sid);
        }
        return sessionID;
    }
    
    public static String findCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        Cookie returnCookie = null;
        String value = "";
        if (cookies != null) {
            for (int k = 0; k < cookies.length; k++) {
                if (cookieName.equalsIgnoreCase(cookies[k].getName())) {
                    returnCookie = cookies[k];
                    value = returnCookie.getValue();
                    if (debug.messageEnabled()) {
                        debug.message("OAuth.findCookie()" + "Cookie "
                                + cookieName
                                + " found. "
                                + "Content is: " + value);
                    }
                    break;
                }
            }
        }
        return value;
    }
}
