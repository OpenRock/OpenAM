/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 ForgeRock AS. All Rights Reserved
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
 * 
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.extensions.crowd;

import com.atlassian.confluence.user.ConfluenceAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import static com.atlassian.seraph.auth.DefaultAuthenticator.LOGGED_IN_KEY;
import static com.atlassian.seraph.auth.DefaultAuthenticator.LOGGED_OUT_KEY;
import com.atlassian.seraph.config.SecurityConfigFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

/**
 *
 * @author steve
 */
public class OpenAMConfluenceAuthenticator extends ConfluenceAuthenticator {
    private static final Category log = Category.getInstance(OpenAMConfluenceAuthenticator.class);
    private static final String OPENAM_URL = "https://idp.forgerock.org:443/openam/identity/";
    private static final String TOKEN_VALID = "isTokenValid?tokenid=";
    private static final String GET_ATTRS = "attributes?subjectid=";
    private static final String COOKIE_NAME = "iPlanetDirectoryPro";

    @Override
    public Principal getUser(HttpServletRequest request, HttpServletResponse response) {
        Principal user = null;

        try {
            request.getSession(true);
            
            Object loop = request.getAttribute("loop");
            
            if (loop != null && loop.equals("true")) {
                log.debug("token invalid; looping");
                return null;
            } 
            
            if (loop != null && loop.equals("nocookie")) {
                log.debug("no cookie; looping");
                return null;
            } 
            
            String token = getToken(request);
            log.info("token=" + token);
            boolean tokenValid;
            
            if (token != null) {
                tokenValid = isTokenValid(token);
                
                log.info("valid=" + tokenValid);
            
                if (!tokenValid) {
                    request.setAttribute("loop", "true");
                }
            } else {
                request.setAttribute("loop", "nocookie");
            }
            
            log.info("Trying seamless Single Sign-on...");
            String username = obtainUsername(request);
            log.info("Got username = " + username);
            if (username != null) {
                if (request.getSession() != null && request.getSession().getAttribute(LOGGED_IN_KEY) != null) {
                    log.info("Session found; user already logged in");
                    user = (Principal) request.getSession().getAttribute(LOGGED_IN_KEY);
                } else {
                    user = getUser(username);
                    log.info("Logged in via SSO, with User " + user);
                    request.getSession().setAttribute(LOGGED_IN_KEY, user);
                    request.getSession().setAttribute(LOGGED_OUT_KEY, null);
                }
            } else {
                log.info("Username is null");
                
                return null;
            }
        } catch (Exception ex) {
            log.warn("Exception: " + ex, ex);
        }
        
        log.info("returning user is " + user);
        return user;

    }
    
    private String getToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        Cookie[] cookies = request.getCookies();
        
        if (cookies == null) {
            return null;
        }
        
        for (int c = 0; c < cookies.length; c++) {
            if (cookies[c].getName().equalsIgnoreCase(COOKIE_NAME)) {
                return cookies[c].getValue();
            }
        }
         
        return null;
    }
    
    private boolean isTokenValid(String token) {
        boolean result = false;
        
        try {
            URL url = new URL(OPENAM_URL + TOKEN_VALID + token);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                result = true;
            }

            conn.disconnect();
        } catch (Exception ex) {
            log.debug("Error validating token", ex);
        }
        
        log.debug("is token valid: " + result);
        return result;
    }
    
    private String getProperty(String token, String name) {
        String result = null;
        
        try {
            URL url = new URL(OPENAM_URL + GET_ATTRS + token + "&attributenames=" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            
            String input = null;
            while ((input = br.readLine()) != null) {
                if (input.contains("userdetails.attribute.value")) {
                    result = input.substring(input.indexOf("=") + 1);
                    log.debug("found property " + name + " value " + result);
                }
            }

            conn.disconnect();
        } catch (Exception ex) {
            log.error("Error validating token", ex);
        }
        
        return result;        
    }

    private String obtainUsername(HttpServletRequest request) {
        String result = null;
        String token = getToken(request);
        log.debug("found token from request " + token);
        
        if (token != null && isTokenValid(token)) {
            try {
                result = getProperty(token, "uid");
            } catch (Exception ex) {
                log.error("Error getting UserId from token", ex);
            }
        }
        
        return result;
    }

    @Override
    public boolean logout(HttpServletRequest request, HttpServletResponse response) 
    throws AuthenticatorException {
        boolean result = false;
        
        try {
            result = doLogout(request, response);
        } catch (Exception ex) {
            log.warn("Exception: " + ex, ex);
        }
        
        return result;
    }

    private boolean doLogout(HttpServletRequest request, HttpServletResponse response)
    throws AuthenticatorException, IOException {
        if (super.logout(request, response)) {
            logoutOfOpenSSO(response);
            return true;
        }
        
        return false;
    }

    private void logoutOfOpenSSO(HttpServletResponse response)
    throws IOException {
        String logoutURL = SecurityConfigFactory.getInstance().getLogoutURL();
        
        if (logoutURL != null) {
            response.sendRedirect(logoutURL);
        }
    }    
}
