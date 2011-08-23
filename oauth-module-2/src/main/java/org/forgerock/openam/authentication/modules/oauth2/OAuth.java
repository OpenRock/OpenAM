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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;

import com.sun.identity.authentication.service.LoginState;
import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.spi.RedirectCallback;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;

import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


public class OAuth extends AMLoginModule implements OAuthParam {

    private static final SecureRandom random = new SecureRandom();

    private static Debug debug = Debug.getInstance(OAuthConf.MODULE_NAME);
 
    private String authenticatedUser;

    private Map sharedState;

    private OAuthConf config;
    
    String authServiceUrl = "";

    public OAuth() {
        debug.message("OAuth()");
    }

    public void init(Subject subject, Map sharedState, Map config) {
        this.sharedState = sharedState;
        this.config = new OAuthConf(config);

        amCache.getResBundle(OAuthConf.BUNDLE_NAME, getLoginLocale());
 
        String authLevel = CollectionHelper.getMapAttr(config, OAuthConf.KEY_AUTH_LEVEL);
        if (authLevel != null) {
            try {
                setAuthLevel(Integer.parseInt(authLevel));
            } catch (Exception e) {
                debug.error("Unable to set auth level " + authLevel, e);
            }
        } 
    }

    // There are two scenarios...

    // The first scenario, is when we have a clickable Facebook icon (or link) at Login.jsp,
    // which leads a user directly to Facebook's authentication service. In this scenario process()
    // method is invoked only once, when Facebook redirects a user back, with newly
    // created Facebook session.

    // The second scenario, is when we use a standard OpenAM submit form, choosing OAuth module
    // from the choice list. In this case process() method is invoked twice: in the first step the module
    // will redirect a user to Facebook authentication service and the second step is when 
    // Facebook redirects a user back, with newly created Facebook session (just like described above).

    public int process(Callback[] callbacks, int state) throws LoginException {
    	if (debug.messageEnabled()) {
            debug.message("process: state = " + state);
    	}

        int result = ISAuthConstants.LOGIN_IGNORE;
        
        HttpServletRequest req = getHttpServletRequest();
        HttpServletResponse response = getHttpServletResponse();
        if (state == ISAuthConstants.LOGIN_START || state == 2) {
            
            LoginState lstate = getLoginState(OAuthConf.MODULE_NAME);
            boolean isSessionUpgrade = lstate.isSessionUpgrade();
            
            if (isSessionUpgrade) {
                if (debug.messageEnabled()) {
                    debug.message("SSO token found - doing session upgrade");
                }
                authenticatedUser = lstate.getOldSession().getProperty("UserToken");
            }

            String code = req.getParameter(PARAM_CODE);

            if (code == null) {
                String orig_requested_URL= req.getRequestURL().toString();
                String orig_requested_query = req.getQueryString();
                if (orig_requested_query != null) {
                    orig_requested_URL += "?" + orig_requested_query;
                }
                debug.message("ORIG: " + orig_requested_URL);
                String serverName = req.getServerName();
                
                Cookie returnURLCookie = new Cookie("ORIG_URL", orig_requested_URL );
                returnURLCookie.setDomain(serverName);
                returnURLCookie.setPath("/");
                response.addCookie(returnURLCookie);
                String authServiceUrl = config.getAuthServiceUrl(
                        req, isSessionUpgrade);
                
                Callback[] callbacks1 = getCallback(2);
                RedirectCallback rc = (RedirectCallback) callbacks1[0];

                if (debug.messageEnabled()) {
                    debug.message("OAuth.process(): New RedirectURL="
                            + authServiceUrl);
                }

                RedirectCallback rcNew = new RedirectCallback(authServiceUrl,
                        null,
                        rc.getMethod(),
                        rc.getStatusParameter(),
                        rc.getRedirectBackUrlCookieName());
                replaceCallback(2, 0, rcNew);
                return 2;
            }
            
            try {
                // check, if it is a redirect back from OAuth identity provider (e.g. Facebook)
                if (code != null && !code.isEmpty()) {
                    if (debug.messageEnabled()) {
                        debug.message("code parameter: " + code);
                    }
                    
                    String orig_url = OAuthUtil.findCookie(req, "ORIG_URL");
                    try {
                        orig_url =  URLDecoder.decode(orig_url,"UTF-8");
                        setUserSessionProperty(ISAuthConstants.FULL_LOGIN_URL, orig_url);
                        debug.message("ORIG_URL set as FULL_LOGIN_URL:" + orig_url);
                    } catch (UnsupportedEncodingException ex) {
                        debug.error("Could not decode the cookie ORIG_URL", ex);
                    }                         
                    
                    try {
                        authServiceUrl = URLEncoder.encode(orig_url,"UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        debug.error("Could not encode cookie ORIG_URL", ex);
                    }
                    
                    String token = getContent(config.getTokenServiceUrl(
                            req, isSessionUpgrade, authServiceUrl));
                    
                    if (debug.messageEnabled()) {
                        debug.message("token: " + token);
                    }

                    try {
                        JSONObject jsonToken = new JSONObject(token);
                        if (jsonToken != null && !jsonToken.isNull("access_token")) {
                            token = "access_token=" + jsonToken.getString("access_token");
                            debug.message("access_token: " + token);
                        }
                    } catch (JSONException je) {
                        debug.message("Not in JSON format" + je);
                    }

                    setUserSessionProperty("OAuthToken", token.substring(13));
                    
                    String jsonSrc = getContent(config.getProfileServiceUrl(token));
                    if (debug.messageEnabled()) {
                        debug.message("json response: " + jsonSrc);
                    }

                    // String realm = req.getParameter(PARAM_REALM);
                    String realm = getRequestOrg();

                    if (realm == null) {
                        realm = "/";
                    }

                    if (debug.messageEnabled()) {
                        debug.message("Oauth.process: realm=" + realm);
                    }
                    
                    JSONObject json = new JSONObject(jsonSrc);

                    AMIdentity user = getUser(realm, json);

                    if (lstate.isSessionUpgrade()) {
                        // In this case we already have autenticatedUser.
                        // Btw, some autofed logics might go here...
                    } else {
                        authenticatedUser = user.getName();
                    }

                    if (debug.messageEnabled()) {
                        debug.message("LOGIN_SUCCEED with user " + authenticatedUser);
                    }
                    result = ISAuthConstants.LOGIN_SUCCEED;
                    // getSSOSession().setProperty("FullLoginURL", authServiceUrl);
                } else {
                    if (debug.messageEnabled()) {
                        debug.message("LOGIN_IGNORE");
                    }
                    result = ISAuthConstants.LOGIN_IGNORE;
                }
            } catch (JSONException je) {
                debug.error("process: JSONException: " + je.getMessage());
                throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                        je.getMessage(), null);
            } catch (SSOException ssoe) {
                debug.error("process: SSOException: " + ssoe.getMessage());
                throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                        ssoe.getMessage(), null);
            } catch (IdRepoException ire) {
                debug.error("process: IdRepoException: " + ire.getMessage());
                throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                        ire.getMessage(), null);
            }
        }
        
   
        if (state != 1 && state != 2) {
            debug.error("WindowsDesktopSSOv2.process(): Illegal State");
            result = ISAuthConstants.LOGIN_IGNORE;
        }
        
        return result;
    }

    private AMIdentity getUser(String realm, JSONObject json)
            throws AuthLoginException, JSONException, SSOException, IdRepoException {
        
        AMIdentity amIdentity = null;
        Map attributes = new HashMap();

        try {
            debug.message("OAuth.getUser: About to get account mapper " + config.getAttributeMapper());
            AttributeMapper attributeMapper = (AttributeMapper)
                    Class.forName(config.getAttributeMapper()).
                    newInstance();
            attributes = attributeMapper.getAttributes(config.getAttributeMapperConfig(), json);
        } catch (ClassNotFoundException ex) {
            debug.error("OAuth.getUser: Attribute Mapper Class not Found", ex);
        } catch (InstantiationException ex) {
            debug.error("OAuth.getUser: Attribute Mapper Class could not be"
                    + " instantiated", ex);
        } catch (IllegalAccessException ex) {
            debug.error("OAuth.getUser: Illegal access when trying to get the "
                    + "Attribute Mapper Class ", ex);
        }


        if (debug.messageEnabled()) {
            debug.message("getUser: creating new user; attributes = " + attributes);
        }

        try {
            debug.message("OAuth.getUser: About to get account mapper " + config.getAccountMapper());
            AccountMapper accountMapper = (AccountMapper)
                    Class.forName(config.getAccountMapper()).
                    newInstance();
            amIdentity = accountMapper.getAccount(config.getAccountMapperConfig(),
                json, getAMIdentityRepository(realm),
                config.getCreateAccountFlag(), attributes);
        } catch (ClassNotFoundException ex) {
            debug.error("OAuth.getUser: Account Mapper Class not Found", ex);
        } catch (InstantiationException ex) {
            debug.error("OAuth.getUser: Account Mapper Class could not be"
                    + " instantiated", ex);
        } catch (IllegalAccessException ex) {
            debug.error("OAuth.getUser: Illegal access when trying to get the "
                    + "Account Mapper Class ", ex);
        }
        return amIdentity;
    }


    private String getContent(String serviceUrl) throws LoginException {
        
    	BufferedReader in = new BufferedReader(new InputStreamReader(
    			getContentStream(serviceUrl)));
    	StringBuffer buf = new StringBuffer();

    	try {
            for (String str; (str = in.readLine()) != null; ) {
	        buf.append(str);
	    }
    	} catch (IOException ioe) {
            debug.error("getContent: IOException: " + ioe.getMessage());
            throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                    ioe.getMessage(), null);
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
                throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                        ioe.getMessage(), null);
            }
        }
    	return buf.toString();
    }

    private InputStream getContentStream(String serviceUrl)
    		throws LoginException {
        if (debug.messageEnabled()) {
            debug.message("service url: " + serviceUrl);
        }
        try {
            InputStream is = null;
            URL urlC = new URL(serviceUrl);
            int init = urlC.getQuery().indexOf("access_token=");
            String queryC = "";
            if (init != -1) {
              queryC = urlC.getQuery().substring(init + 13);
              debug.message("QueryC: " + queryC);
            }


            
            HttpURLConnection connection = (HttpURLConnection) urlC.openConnection();
            connection.setDoOutput(true);
            if (!queryC.isEmpty()) {
               connection.setRequestProperty("Authorization", "OAuth " + queryC);
            }
            
            connection.setRequestMethod("GET");

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                debug.message("getContentStream: IT was OK");
                is = connection.getInputStream();
                // OK
            } else {
                debug.message("getContentStream: IT was NOT-OK: " + connection.getResponseCode());
                throw new IOException();
                // Server returned HTTP error code.
            }
            return is;
        } catch (MalformedURLException mfe) {
            debug.error("getContentStream: MalformedURLException: " + mfe.getMessage());
            throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                    mfe.getMessage(), null);
        } catch (IOException ioe) {
            debug.error("getContentStream: IOException: " + ioe.getMessage());

            try {
                debug.message("POSTING URL: " + serviceUrl); 
                
                URL url = new URL(serviceUrl);

                String query = url.getQuery();
                debug.message("POSTING query url: " + query); 
                
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(query);
                writer.close();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    debug.message("getContentStream: IT was OK");
                    return connection.getInputStream();
                    // OK
                } else {
                    debug.message("getContentStream: IT was NOT-OK: " +  connection.getResponseCode());
                    // Server returned HTTP error code.
                }
            } catch (MalformedURLException e) {
                
                // ...
            } catch (IOException e) {
                // ...
            }

            throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                    ioe.getMessage(), null);
        }
    } 

    public Principal getPrincipal() {
        if (authenticatedUser != null) {
            return new OAuthPrincipal(authenticatedUser);
        }
        return null;
    }

    public void destroyModuleState() {
        authenticatedUser = null;
    }

    public void nullifyUsedVars() {
        config = null;
        sharedState = null;
    }

}
