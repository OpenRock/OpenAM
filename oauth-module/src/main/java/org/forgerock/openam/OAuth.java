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
 * Copyright 2011 Cybernetica AS.
 */

package org.forgerock.openam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.dpro.session.service.InternalSession;

import com.sun.identity.authentication.service.AuthD;
import com.sun.identity.authentication.service.LoginState;
import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.idm.IdSearchControl;
import com.sun.identity.idm.IdSearchOpModifier;
import com.sun.identity.idm.IdSearchResults;
import com.sun.identity.idm.IdType;
import com.sun.identity.idm.IdUtils;

import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.Base64;

public class OAuth extends AMLoginModule implements OAuthParam {

    // standard internal user repo attributes
    private static final String USER_PASSWORD = "userpassword";
    private static final String USER_STATUS = "inetuserstatus";
    private static final String USER_MAIL = "mail";
    private static final String USER_GIVENNAME = "givenname";
    private static final String USER_LAST_NAME = "sn";
    private static final String USER_FULL_NAME = "cn";

    // User repo LDAP attributes for storing information from Facebook profile
    // (consider giving them better names, if these are not good enough).
    private static final String USER_FACEBOOK_ID = "facebook-id";
    private static final String USER_FACEBOOK_FNAME = "facebook-fname";
    private static final String USER_FACEBOOK_LNAME = "facebook-lname";
    private static final String USER_FACEBOOK_EMAIL = "facebook-email";

    private static final SecureRandom random = new SecureRandom();

    private static Debug debug = Debug.getInstance(OAuthConf.MODULE_NAME);
 
    private String authenticatedUser;

    private Map sharedState;

    private OAuthConf config;

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

    	if (state != ISAuthConstants.LOGIN_START) {
            debug.error("process: state != ISAuthConstants.LOGIN_START");
            throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                    "state != ISAuthConstants.LOGIN_START", null);
    	}

        HttpServletRequest req = getHttpServletRequest();
        try {
            LoginState lstate = getLoginState(OAuthConf.MODULE_NAME);
            boolean isSessionUpgrade = lstate.isSessionUpgrade();

            if (isSessionUpgrade) {
                if (debug.messageEnabled()) {
                    debug.message("SSO token found - doing session upgrade");
                }
                authenticatedUser = lstate.getOldSession().getProperty("UserToken");
            }

            if (req.getParameter(PARAM_EXPECT_RESPONSE) == null) {
                String authServiceUrl = config.getAuthServiceUrl(
                        req, isSessionUpgrade);
                if (debug.messageEnabled()) {
                    debug.message("LoginFailureURL: " + authServiceUrl);
                }

                setLoginFailureURL(authServiceUrl);
                // LOGIN_IGNORE removes AMAuthCookie
                // XXX: Could not find a suitable constant, for making redirections
                // from within a module correctly. LOGIN_IGNORE seemed to work
                // (though with some side effects), but a specific and cleaner 
                // approach (e.g. LOGIN_REDIRECT) might be invented in future.
                return ISAuthConstants.LOGIN_IGNORE;
            }

            // check, if it is a redirect back from OAuth identity provider (e.g. Facebook)
            String module = req.getParameter(PARAM_MODULE);
            if (module != null) {
                if (debug.messageEnabled()) {
                    debug.message("module parameter: " + module);
                }

                String token = getContent(config.getTokenServiceUrl(
                        req, isSessionUpgrade));
                if (debug.messageEnabled()) {
                    debug.message("token: " + token);
    	        }

                String jsonSrc = getContent(config.getProfileServiceUrl(token));
                if (debug.messageEnabled()) {
                    debug.message("json response: " + jsonSrc);
    	        }

                String realm = req.getParameter(PARAM_REALM);
                if (realm == null) {
                    realm = "/";
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
                return ISAuthConstants.LOGIN_SUCCEED;
            } else {
                if (debug.messageEnabled()) {
                    debug.message("LOGIN_IGNORE");
                }
                return ISAuthConstants.LOGIN_IGNORE;
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

    private AMIdentity getUser(String realm, JSONObject json)
            throws AuthLoginException, JSONException, SSOException, IdRepoException {
        Map avMap = new HashMap();

        avMap.put(USER_FACEBOOK_ID, addToSet(new HashSet(), json.getString("id")));

        IdSearchControl ctrl = getSearchControl(IdSearchOpModifier.OR, avMap);

        AMIdentityRepository repo = getAMIdentityRepository(realm);
    	IdSearchResults results = repo.searchIdentities(
    			IdType.USER, "*", ctrl);

    	Iterator iter = results.getSearchResults().iterator();
    	if (iter.hasNext()) {
            if (debug.messageEnabled()) {
                debug.message("getUser: user found");
            }
            return (AMIdentity) iter.next();
    	}
        Map attributes = populateAttributeMap(new HashMap(), json);
        if (debug.messageEnabled()) {
            debug.message("getUser: creating new user; attributes = " + attributes);
        }
        String userId = getNewUserID(attributes);
        return repo.createIdentity(IdType.USER, userId, attributes);
    }

    private String getNewUserID(Map attributes) throws AuthLoginException {
        Set userIDs = getNewUserIDs(attributes, 1);
        if (userIDs != null && !userIDs.isEmpty()) {
            if (debug.messageEnabled()) {
                debug.message("getNewUserID: generator class used: userIDs = " + userIDs);
            } 
            return (String) userIDs.iterator().next();
        } 
        if (debug.messageEnabled()) {
            debug.message("getNewUserID: could not get ID from denerator class "
                    + "- using internal generator method");
        }
        // A UUID is used as a global uid, which is independent of any identity provider.
        // This is just technical means to identify and aggregate information of different
        // external identity providers under a single user profile.
        return UUID.randomUUID().toString();
    }

    private Map populateAttributeMap(Map attr, JSONObject json) throws JSONException {
        // if no such facebook id exists, create 
        // the user based on facebook information
        attr.put(USER_GIVENNAME,
                addToSet(new HashSet(), json.getString("first_name")));
        attr.put(USER_LAST_NAME,
		addToSet(new HashSet(), json.getString("last_name")));
        attr.put(USER_FULL_NAME,
		addToSet(new HashSet(), json.getString("name")));
        attr.put(USER_MAIL,
		addToSet(new HashSet(), json.getString("email")));
        // populate facebook attributes
        attr.put(USER_FACEBOOK_ID,
                addToSet(new HashSet(), json.getString("id")));
        attr.put(USER_FACEBOOK_FNAME,
                addToSet(new HashSet(), json.getString("first_name")));
        attr.put(USER_FACEBOOK_LNAME,
                addToSet(new HashSet(), json.getString("last_name")));
        attr.put(USER_FACEBOOK_EMAIL,
                addToSet(new HashSet(), json.getString("email")));
        // set other technical attributes
        attr.put(USER_STATUS,
                addToSet(new HashSet(), "Active"));
        attr.put(USER_PASSWORD,
                addToSet(new HashSet(), getRandomPassword()));
	return attr;
    }

    private Set addToSet(Set set, String attribute) {
	set.add(attribute);
	return set;
    }

    private String getRandomPassword() {
	byte[] pass = new byte[20];
	random.nextBytes(pass);
        return Base64.encode(pass);
    }

    private IdSearchControl getSearchControl(
            IdSearchOpModifier modifier, Map avMap) {
    	IdSearchControl control = new IdSearchControl();

    	control.setMaxResults(1);
        control.setSearchModifiers(modifier, avMap);

    	return control;
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
            return new URL(serviceUrl).openStream();
        } catch (MalformedURLException mfe) {
            debug.error("getContentStream: MalformedURLException: " + mfe.getMessage());
            throw new AuthLoginException(OAuthConf.BUNDLE_NAME,
                    mfe.getMessage(), null);
        } catch (IOException ioe) {
            debug.error("getContentStream: IOException: " + ioe.getMessage());
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
