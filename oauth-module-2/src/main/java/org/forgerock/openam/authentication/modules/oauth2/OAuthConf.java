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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import java.util.HashMap;
import java.util.Set;


/* 
 * The purpose of OAuthConf is to encapsulate module's configuration
 * and based on this configuration provide a common interface for getting
 * essential URLs, like: 
 * - authentication service URL;
 * - token service URL;
 * - profile service URL.
 *
 * This is first of all necessary for customized Login.jsp, which needs
 * to have the correct link that will lead a user to authentication service.
 * Otherwise, the links should be hardcoded (and hardcoded correctly), what
 * would lead to extra code and complexity of Login.jsp.
 */
public class OAuthConf implements OAuthParam {
    
    static final String CLIENT = "genericHTML";

    private static Debug debug = Debug.getInstance("amAuth");

    private String clientId = null;
    private String clientSecret = null;

    private String scope = null;

    private String authServiceUrl = null;
    private String tokenServiceUrl = null;
    private String profileServiceUrl = null;
    // private String ssoLoginUrl;
    private String ssoProxyUrl = null;
    private String accountMapper = null;
    private String attributeMapper = null;
    private String createAccountFlag = null;
    private String promptPasswordFlag = null;
    private String useAnonymousUserFlag = null;
    private String anonymousUser = null;
    private Set accountMapperConfig = null;
    private Set attributeMapperConfig = null;
    private String saveAttributesToSessionFlag = null;
    private String mailAttribute = null;
    private String logoutServiceUrl = null;
    private String logoutBehaviour = null;
    private String gatewayEmailImplClass = null;
    private String smtpHostName = null;
    private String smtpPort = null;
    private String smtpUserName = null;
    private String smtpUserPassword = null;
    private String smtpSSLEnabled = "false";
    

    OAuthConf() {
    }

    OAuthConf(Map config) {
        clientId = CollectionHelper.getMapAttr(config, KEY_CLIENT_ID);
        clientSecret = CollectionHelper.getMapAttr(config, KEY_CLIENT_SECRET);
        scope = CollectionHelper.getMapAttr(config, KEY_SCOPE);
        authServiceUrl = CollectionHelper.getMapAttr(config, KEY_AUTH_SERVICE);
        tokenServiceUrl = CollectionHelper.getMapAttr(config, KEY_TOKEN_SERVICE);
        profileServiceUrl = CollectionHelper.getMapAttr(config, KEY_PROFILE_SERVICE);
        // ssoLoginUrl = CollectionHelper.getMapAttr(config, KEY_SSO_LOGIN_URL);
        ssoProxyUrl = CollectionHelper.getMapAttr(config, KEY_SSO_PROXY_URL);
        accountMapper = CollectionHelper.getMapAttr(config, KEY_ACCOUNT_MAPPER);
        accountMapperConfig = (Set) config.get(KEY_ACCOUNT_MAPPER_CONFIG);
        attributeMapper = CollectionHelper.getMapAttr(config, KEY_ATTRIBUTE_MAPPER);
        attributeMapperConfig = (Set) config.get(KEY_ATTRIBUTE_MAPPER_CONFIG);
        saveAttributesToSessionFlag = CollectionHelper.getMapAttr(config, 
                KEY_SAVE_ATTRIBUTES_TO_SESSION);
        mailAttribute = CollectionHelper.getMapAttr(config, KEY_MAIL_ATTRIBUTE);
        createAccountFlag = CollectionHelper.getMapAttr(config, KEY_CREATE_ACCOUNT);
        promptPasswordFlag = CollectionHelper.getMapAttr(config,KEY_PROMPT_PASSWORD);
        useAnonymousUserFlag = CollectionHelper.getMapAttr(config, 
                KEY_MAP_TO_ANONYMOUS_USER_FLAG);
        anonymousUser = CollectionHelper.getMapAttr(config, KEY_ANONYMOUS_USER);
        logoutServiceUrl = CollectionHelper.getMapAttr(config, KEY_LOGOUT_SERVICE_URL);
        logoutBehaviour = CollectionHelper.getMapAttr(config, KEY_LOGOUT_BEHAVIOUR);
        // Email parameters
        gatewayEmailImplClass =  CollectionHelper.getMapAttr(config, KEY_EMAIL_GWY_IMPL);
        smtpHostName =  CollectionHelper.getMapAttr(config, KEY_SMTP_HOSTNAME);
        smtpPort = CollectionHelper.getMapAttr(config, KEY_SMTP_PORT);
        smtpUserName = CollectionHelper.getMapAttr(config, KEY_SMTP_USERNAME);
        smtpUserPassword = CollectionHelper.getMapAttr(config, KEY_SMTP_PASSWORD);
        smtpSSLEnabled = CollectionHelper.getMapAttr(config, KEY_SMTP_SSL_ENABLED);    
    }
    
    public String  getGatewayImplClass()
            throws AuthLoginException {

        return gatewayEmailImplClass;
    }
    
    
    public Map getSMTPConfig() {
        HashMap config = new HashMap();
        config.put(KEY_EMAIL_GWY_IMPL, gatewayEmailImplClass);
        config.put(KEY_SMTP_HOSTNAME, smtpHostName);
        config.put(KEY_SMTP_PORT, smtpPort);
        config.put(KEY_SMTP_USERNAME, smtpUserName);
        config.put(KEY_SMTP_PASSWORD, smtpUserPassword);
        config.put(KEY_SMTP_SSL_ENABLED, smtpSSLEnabled);
        return config;
        
    }
    
    
    public String  getSMTPHostName()
            throws AuthLoginException {

        return smtpHostName;
    }
    
    public String  getSMTPPort()
            throws AuthLoginException {

        return smtpPort;
    }
    
    public String  getSMTPUserName()
            throws AuthLoginException {

        return smtpUserName;
    }
    
    public String  getSMTPUserPassword()
            throws AuthLoginException {

        return smtpUserPassword;
    }
    
    public boolean  getSMTPSSLEnabled()
            throws AuthLoginException {

        return smtpSSLEnabled.equalsIgnoreCase("true");
    }
    
    public String  getLogoutServiceUrl()
            throws AuthLoginException {

        return logoutServiceUrl;
    }

    public String  getLogoutBhaviour()
            throws AuthLoginException {

        return logoutBehaviour;
    }
    
    public String  getAccountMapper()
            throws AuthLoginException {

        return accountMapper;
    }

    
    public String  getAttributeMapper()
            throws AuthLoginException {

        return attributeMapper;
    }

    public Set  getAccountMapperConfig()
            throws AuthLoginException {

        return accountMapperConfig;
    }

        
    public Set  getAttributeMapperConfig()
            throws AuthLoginException {

        return attributeMapperConfig;
    }
 
    public boolean  getSaveAttributesToSessionFlag()
            throws AuthLoginException {

        return saveAttributesToSessionFlag.equalsIgnoreCase("true");
    }
    
    public String  getMailAttribute()
            throws AuthLoginException {

        return mailAttribute;
    }
    
    public boolean  getCreateAccountFlag()
            throws AuthLoginException {

        return createAccountFlag.equalsIgnoreCase("true");
    }
    
    public boolean  getPromptPasswordFlag()
            throws AuthLoginException {

        return promptPasswordFlag.equalsIgnoreCase("true");
    }
    
    public boolean  getUseAnonymousUserFlag()
            throws AuthLoginException {

        return useAnonymousUserFlag.equalsIgnoreCase("true");
    }
    
    public String  getAnonymousUser()
            throws AuthLoginException {

        return anonymousUser;
    }
    
    public String  getProxyURL()
            throws AuthLoginException {

        return ssoProxyUrl;
    }
    
    
    public String getAuthServiceUrl(String originalUrl) throws AuthLoginException {

        if (authServiceUrl.indexOf("?") == -1) {
            authServiceUrl = authServiceUrl + "?"
                    + PARAM_CLIENT_ID + "=" + clientId;
        } else {
            authServiceUrl = authServiceUrl + "&"
                    + PARAM_CLIENT_ID + "=" + clientId;
        }

        return authServiceUrl
                + param(PARAM_SCOPE, scope)
                + param(PARAM_REDIRECT_URI,
                OAuthUtil.encodeUriToRedirect(originalUrl));
    }


    String getTokenServiceUrl(String code,
            String authServiceURL) throws AuthLoginException {

        if (code == null) {
            debug.error("process: code == null");
            throw new AuthLoginException(BUNDLE_NAME,
                    "authCode == null", null);
        }

        if (debug.messageEnabled()) {
            debug.message("authentication code: " + code);
        }

        if (tokenServiceUrl.indexOf("?") == -1) {
            tokenServiceUrl = tokenServiceUrl + "?"
                    + PARAM_CLIENT_ID + "=" + clientId;
        } else {
            tokenServiceUrl = tokenServiceUrl + "&"
                    + PARAM_CLIENT_ID + "=" + clientId;
        }

        return tokenServiceUrl
                + param(PARAM_REDIRECT_URI,
                OAuthUtil.encodeUriToRedirect(authServiceURL))
                + param(PARAM_SCOPE, scope)
                + param(PARAM_CLIENT_SECRET, clientSecret)
                + param(PARAM_CODE, code);

    }
  
    String getProfileServiceUrl(String token) {
        
        if (profileServiceUrl.indexOf("?") == -1) {
                return profileServiceUrl + "?" + OAuth.PARAM_ACCESS_TOKEN +
                        "=" + token;
            } else {
                return profileServiceUrl + "&" + OAuth.PARAM_ACCESS_TOKEN +
                        "=" + token;
        }
        
    }

    private String param(String key, String value) {
        return "&" + key + "=" + value;
    }

}
