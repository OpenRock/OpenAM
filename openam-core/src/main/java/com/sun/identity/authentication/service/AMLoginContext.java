/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005 Sun Microsystems Inc. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: AMLoginContext.java,v 1.24 2009/12/23 20:03:04 mrudul_uchil Exp $
 *
 * Portions Copyrighted 2011-2015 ForgeRock AS.
 * Portions Copyrighted 2014 Nomura Research Institute, Ltd
 */
package com.sun.identity.authentication.service;

import static org.forgerock.openam.audit.AuditConstants.AuthenticationFailureReason.*;

import com.iplanet.am.util.SystemProperties;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.authentication.AuthContext.IndexType;
import com.sun.identity.authentication.audit.AuthenticationProcessEventAuditor;
import com.sun.identity.authentication.config.AMAuthConfigUtils;
import com.sun.identity.authentication.config.AMAuthLevelManager;
import com.sun.identity.authentication.config.AMAuthenticationInstance;
import com.sun.identity.authentication.config.AMAuthenticationManager;
import com.sun.identity.authentication.config.AMConfiguration;
import com.sun.identity.authentication.config.AMConfigurationException;
import com.sun.identity.authentication.server.AuthContextLocal;
import com.sun.identity.authentication.service.DSAMECallbackHandler.DSAMECallbackHandlerError;
import com.sun.identity.authentication.spi.AuthErrorCodeException;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.spi.InvalidPasswordException;
import com.sun.identity.authentication.spi.MessageLoginException;
import com.sun.identity.authentication.spi.RedirectCallback;
import com.sun.identity.authentication.util.AMAuthUtils;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.common.DNUtils;
import com.sun.identity.monitoring.Agent;
import com.sun.identity.monitoring.MonitoringUtil;
import com.sun.identity.monitoring.SsoServerAuthSvcImpl;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.URLEncDec;
import com.sun.identity.shared.locale.AMResourceBundleCache;
import com.sun.identity.sm.DNMapper;
import org.forgerock.guice.core.InjectorHolder;
import org.forgerock.openam.audit.AuditConstants.AuthenticationFailureReason;
import org.forgerock.openam.utils.StringUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.security.AccessController;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * <code>AMLoginContext</code> class is the core layer in the authentication
 * middle tier which connects user clients to the JAAS <code>LoginModule</code>.
 * The <code>AMLoginContext</code> executes pre and post authentication process
 * based on authentication status.
 * <p>
 * <code>AMLoginContext</code> provides a synchronous layer on top of the JAAS
 * framework for appropriate user interaction and communication between clients
 * and authentication module via callbacks requirements
 * <p>
 * <code>AMLoginContext</code> sets and retrieves the authentication
 * configuration entry
 * <p>
 * This class actually starts the JAAS login process by instantiating the
 * <code>LoginContext</code> object with the JAAS configuration name and the
 * <code>CallbackHandler</code> followed by calling the
 * <code>LoginContext::login()</code> method.
 *
 */
public class AMLoginContext {

    private static final String LIST_DELIMITER = "|";
    /**
     * AuthThreadManager associated with this AMLoginContext.
     */
    public static AuthThreadManager authThread  = null;
    private String exceedRetryLimit = null;
    private static final String BUNDLE_NAME = "amAuth";

    private String configName; // jaas configuration name.
    private String orgDN = null;
    private javax.security.auth.login.LoginContext loginContext = null;
    private com.sun.identity.authentication.jaas.LoginContext jaasLoginContext = null;
    private LoginStatus loginStatus;
    private LoginState loginState;
    private AuthContextLocal authContext;
    private Subject subject;
    private IndexType indexType;
    private String indexName;
    private String clientType;
    private String lockoutMsg = null;
    private Set<String> moduleSet = null;
    private String sid = null;
    private boolean accountLocked = false;
    private boolean isFailed = false;
    private boolean internalAuthError = false;
    private boolean processDone = false;
    private int jaasCheck;
    private Thread jaasThread = null;
    private AppConfigurationEntry[] entries = null;
    private Callback[] recdCallback;
    private final AuthenticationProcessEventAuditor auditor;

    private static SsoServerAuthSvcImpl authImpl;
    private static Configuration defaultConfig = null;
    private static AuthD ad;
    private static Debug debug;

    //OPENAM-3959
    private static boolean excludeRequiredOrRequisite = false;

    /**
     * Bundle to be used for localized error message. users can be in different
     * locales. Since we create an AMLoginContext for each user, we can cache
     * the bundle reference in the class
     */
    private ResourceBundle bundle;

    static {
        // set the auth configuration programmatically.
        // this getConfiguration() call throws null exception
        // when no default config is available, which looks like
        // a bug of JDK.
        try {
            defaultConfig = Configuration.getConfiguration();
        } catch (java.lang.SecurityException e) {
            //Continue
        }
        AMConfiguration ISConfig = new AMConfiguration(defaultConfig);
        try {
            Configuration.setConfiguration(ISConfig);
        } catch (java.lang.SecurityException e) {
            System.err.println("AMLoginContext:Set AM config error:" + e.getMessage());
        }

        if (MonitoringUtil.isRunning()) {
            authImpl = Agent.getAuthSvcMBean();
        }

        excludeRequiredOrRequisite =
                SystemProperties.getAsBoolean(Constants.AUTH_LEVEL_EXCLUDE_REQUIRED_REQUISITE, false);
    }

    /**
     * Sets the JAAS configuration to the default container's configuration.
     */
    public static void resetJAASConfig() {
        try {
            Configuration.setConfiguration(defaultConfig);
        } catch (java.lang.SecurityException e) {
            System.err.println("AMLoginContext:resetJAASConfig to default:" + e.getMessage());
        }
    }

    /**
     * Sets the configuration entries.
     * @param entries configuration entries
     */
    public void setConfigEntries(AppConfigurationEntry[] entries) {
        this.entries = entries;
    }

    /**
     * Creates <code>AMLoginContext</code> object.
     * @param authContext <code>AuthContextLocal</code> object
     */
    public AMLoginContext(AuthContextLocal authContext) {
        ad = AuthD.getAuth();
        debug = AuthD.debug;
        if (debug.messageEnabled()) {
            debug.message("AMLoginContext:initialThread name is... :" + Thread.currentThread().getName());
        }
        this.authContext = authContext;
        loginStatus = new LoginStatus();
        loginStatus.setStatus(LoginStatus.AUTH_IN_PROGRESS);
        auditor = InjectorHolder.getInstance(AuthenticationProcessEventAuditor.class);
        bundle = ad.bundle; //default value for bundle until we find out
        //user login locale from LoginState object
    }

    /**
     * Starts login process, the map passed to this method is the parameters
     * required to start the login process. These parameters are
     * <code>indexType</code>, <code>indexName</code> , <code>principal</code>,
     * <code>subject</code>, <code>password</code>,
     * <code>organization name</code>. Based on these parameters Module
     * Configuration name is retrieved using Configuration component. Creates
     * a new LoginContext and starts login process and returns. On error
     * LoginException is thrown.
     *
     * @param loginParamsMap login parameters HashMap
     * @throws AuthLoginException if execute login fails
     */
    public void executeLogin(Map<String, Object> loginParamsMap) throws AuthLoginException {
        boolean errorState = false;
        internalAuthError = false;
        processDone = false;
        isFailed = false;
        setLoginHash();

        /*
         * Ensure loginState created and loginParamsMap provided
         */
        if (loginState == null || loginParamsMap == null) {
            debug.error("Error: loginState or loginParams is null");
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            if (loginState != null) {
                loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
            }
            setErrorMsgAndTemplate();
            internalAuthError = true;
            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_ERROR, null);
        }

        /*
         * Lookup resource bundle and locale specific settings based on locale associated with LoginState
         */
        java.util.Locale loginLocale = com.sun.identity.shared.locale.Locale.getLocale(loginState.getLocale());
        bundle = AMResourceBundleCache.getInstance().getResBundle(BUNDLE_NAME, loginLocale);
        exceedRetryLimit = AMResourceBundleCache.getInstance()
                .getResBundle("amAuthLDAP", loginLocale).getString(ISAuthConstants.EXCEED_RETRY_LIMIT);
        if (debug.messageEnabled()) {
            debug.message("LoginState : " + loginState);
        }

        /*
         * Handle redirection if applicable
         */
        String redirectUrl = (String) loginParamsMap.get(AuthContextLocal.REDIRECT_URL);
        if (redirectUrl != null) {
            // Resource/IP/Env based auth case with Redirection Advice
            Callback[] redirectCallback = new Callback[1];
            redirectCallback[0] = new RedirectCallback(redirectUrl, null, "GET");
            if (isPureJAAS()) {
                loginState.setReceivedCallback_NoThread(redirectCallback);
            } else {
                loginState.setReceivedCallback(redirectCallback, this);
            }
            return;
        }

        /*
         * Initialize instance fields from loginParamsMap
         */
        parseLoginParams(loginParamsMap);

        /*
         * Copy orgDN and clientType values from LoginState
         */
        if (authContext.getOrgDN() != null && !authContext.getOrgDN().isEmpty()) {
            orgDN = authContext.getOrgDN();
            loginState.setQualifiedOrgDN(orgDN);
        } else {
            orgDN = loginState.getOrgDN();
        }
        clientType = loginState.getClientType();
        if (debug.messageEnabled()) {
            debug.message("orgDN : " + orgDN);
            debug.message("clientType : " + clientType);
        }

        /*
         * Throw an exception if module-based authentication is disabled and an authentication module other
         * than APPLICATION_MODULE or FEDERATION_MODULE is explicitly requested.
         */
        if (indexType == IndexType.MODULE_INSTANCE
                && !loginState.getEnableModuleBasedAuth()
                && !indexName.equals(ISAuthConstants.APPLICATION_MODULE)) {
            String moduleClassName = null;
            try {
                AMAuthenticationManager authManager = new AMAuthenticationManager(
                        AccessController.doPrivileged(AdminTokenAction.getInstance()), orgDN);
                AMAuthenticationInstance authInstance = authManager.getAuthenticationInstance(indexName);
                moduleClassName = authInstance.getType();
            } catch (AMConfigurationException amce) {
                debug.error("AMLoginContext.executeLogin(): Unable to get authentication config", amce);
            }
            if (moduleClassName != null && !moduleClassName.equalsIgnoreCase(ISAuthConstants.FEDERATION_MODULE)) {
                throwExceptionIfModuleBasedAuthenticationDisabled();
            }
        }

        /*
         * Update LoginState indexType and indexName
         * (after storing current loginState indexType if required for HTTP callback processing)
         */
        IndexType prevIndexType = loginState.getIndexType();
        if (prevIndexType == IndexType.LEVEL || prevIndexType == IndexType.COMPOSITE_ADVICE) {
            loginState.setPreviousIndexType(prevIndexType);
        }
        loginState.setIndexType(indexType);
        loginState.setIndexName(indexName);

        /*
         * Delegate actual processing of requested authentication type to the dispatch method 'processIndexType'
         */
        try {
            if (processIndexType(indexType, indexName, orgDN)) {
                return;
            }
        } catch (AuthLoginException le) {
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            debug.message("Error  : ", le);
            throw le;
        } catch (Exception e) {
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            debug.message("Error : ", e);
            throw new AuthLoginException(e);
        }

        /*
         * Establish configName based on indexType, indexName, orgDN and clientType
         *
         * If configName can't be established, throw an exception
         */
        configName = getConfigName(indexType, indexName, orgDN, clientType);
        if (configName == null) {
            loginState.setErrorCode(AMAuthErrorCode.AUTH_CONFIG_NOT_FOUND);
            debug.message("Config not found");
            setErrorMsgAndTemplate();
            internalAuthError = true;
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            loginState.logFailed(bundle.getString("noConfig"), "NOCONFIG");
            auditor.auditLoginFailure(loginState, NO_CONFIG);

            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_CONFIG_NOT_FOUND, null);
        }

        /*
         * Create the LoginContext object that actually handles login/logout
         */
        if (debug.messageEnabled()) {
            debug.message("Creating login context object\n"
                    + "\n orgDN : " + orgDN
                    + "\n configName : " + configName);
        }
        try {
            jaasCheck = AuthUtils.isPureJAASModulePresent(configName, this);

            if (isPureJAAS()) {
                debug.message("Using pure jaas mode.");
                if (authThread == null) {
                    authThread = new AuthThreadManager();
                    authThread.start();
                }
            }

            DSAMECallbackHandler dsameCallbackHandler = new DSAMECallbackHandler(this);

            if (isPureJAAS()) {
                if (subject != null)  {
                    loginContext = new javax.security.auth.login.LoginContext(configName, subject, dsameCallbackHandler);
                } else {
                    loginContext = new javax.security.auth.login.LoginContext(configName, dsameCallbackHandler);
                }
            } else {
                debug.message("Using non pure jaas mode.");
                if (subject != null)  {
                    jaasLoginContext = new com.sun.identity.authentication.jaas.LoginContext(
                            entries, subject, dsameCallbackHandler);
                } else {
                    jaasLoginContext = new com.sun.identity.authentication.jaas.LoginContext(
                            entries, dsameCallbackHandler);
                }
            }
        } catch (AuthLoginException ae) {
            debug.error("JAAS module for config: " + configName + ", " + ae.getMessage());
            if (debug.messageEnabled()) {
                debug.message("AuthLoginException", ae);
            }
            /* The user based authentication errors should not be different
             * for users who exist and who don't, which can lead to
             * possiblity of enumerating existing users.
             * The AMAuthErrorCode.AUTH_LOGIN_FAILED error code is used for
             * all user based authentication errors.
             * Refer issue3278
             */
            if (indexType == IndexType.USER && AMAuthErrorCode.AUTH_CONFIG_NOT_FOUND.equals(ae.getErrorCode())) {
                loginState.setErrorCode(AMAuthErrorCode.AUTH_LOGIN_FAILED);
            } else {
                loginState.setErrorCode(ae.getErrorCode());
            }
            setErrorMsgAndTemplate();
            loginState.logFailed(bundle.getString("loginContextCreateFailed"));
            auditor.auditLoginFailure(loginState);
            internalAuthError = true;
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            throw ae;
        } catch (LoginException le) {
            debug.error("in creating LoginContext.");
            if (debug.messageEnabled()) {
                debug.message("Exception ", le);
            }
            loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
            loginState.logFailed(bundle.getString("loginContextCreateFailed"));
            auditor.auditLoginFailure(loginState);
            setErrorMsgAndTemplate();
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            internalAuthError = true;
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_ERROR, null, le);
        } catch (SecurityException se) {
            debug.error("security in creating LoginContext.");
            if (debug.messageEnabled()) {
                debug.message("Exception " , se);
            }
            loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
            setErrorMsgAndTemplate();
            loginState.logFailed(bundle.getString("loginContextCreateFailed"));
            auditor.auditLoginFailure(loginState);
            internalAuthError = true;
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_ERROR, null);
        } catch (Exception e) {
            debug.error("Creating DSAMECallbackHandler: " + e.getMessage());
            loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
            setErrorMsgAndTemplate();
            loginState.logFailed(bundle.getString("loginContextCreateFailed"));
            auditor.auditLoginFailure(loginState);
            internalAuthError = true;
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_ERROR, null, e);
        }

        /*
         * Perform the login using the objects this method has setup
         */
        try {
            if (isPureJAAS()) {
                if (jaasThread != null) {
                    jaasThread.interrupt();
                    jaasThread = null;
                    errorState = true;
                } else {
                    jaasThread = new JAASLoginThread(this);
                    jaasThread.start();
                }
            } else {
                runLogin();
            }
        } catch (IllegalThreadStateException ite) {
            errorState = true;
        } catch (Exception e) {
            errorState = true;
        }
        if (errorState) {
            loginStatus.setStatus(LoginStatus.AUTH_RESET);
            loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
            setErrorMsgAndTemplate();
            internalAuthError = true;
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_ERROR, null);

        }
        debug.message("AMLoginContext:Thread started... returning.");
    }

    /**
     * Starts the login process ,calls JAAS Login Context
     */
    public void runLogin() {
        Thread thread = Thread.currentThread();
        String logFailedMessage = bundle.getString("loginFailed");
        String logFailedError = null;
        AuthenticationFailureReason failureReason = null;
        AMAccountLockout amAccountLockout;
        boolean loginSuccess = false;
        try {
            if (isPureJAAS()) {
                loginContext.login();
                subject = loginContext.getSubject();
            } else {
                jaasLoginContext.login();
                subject = jaasLoginContext.getSubject();
            }

            loginState.setSubject(subject);

            if (!loginState.isAuthValidForInternalUser()) {
                if (debug.warningEnabled()) {
                    debug.warning("AMLoginContext.runLogin():auth failed, "
                            +  "using invalid realm name for internal user");
                }
                logFailedMessage = AuthUtils.getErrorVal(AMAuthErrorCode.AUTH_MODULE_DENIED, AuthUtils.ERROR_MESSAGE);
                logFailedError = "MODULEDENIED";
                failureReason = MODULE_DENIED;
                throw new AuthException(AMAuthErrorCode.AUTH_MODULE_DENIED, null);
            }

            debug.message("user authentication successful");

            // retrieve authenticated user's profile or create
            // a user profile if dynamic profile creation is
            // is true

            debug.message("searchUserProfile for Subject :");
            boolean profileState = loginState.searchUserProfile(subject, indexType, indexName);
            loginState.saveSubjectState();
            loginSuccess = true;
            if (!profileState) {
                debug.error("Profile not found ");
                logFailedMessage = bundle.getString("noUserProfile");
                logFailedError = "NOUSERPROFILE";
                failureReason = NO_USER_PROFILE;
                loginState.setErrorCode(AMAuthErrorCode.AUTH_PROFILE_ERROR);
                isFailed = true;
            } else {
                //update loginstate with authlevel , moduleName , role etc.
                amAccountLockout = new AMAccountLockout(loginState);
                if (amAccountLockout.isLockedOut()) {
                    debug.message("User locked out!!");
                    logFailedMessage = bundle.getString("lockOut");
                    logFailedError = "LOCKEDOUT";
                    failureReason = LOCKED_OUT;
                    loginState.setErrorCode(AMAuthErrorCode.AUTH_USER_LOCKED);
                    isFailed = true;
                } else {
                    boolean accountExpired = false;
                    if (!loginState.ignoreProfile()) {
                        accountExpired = amAccountLockout.isAccountExpired();
                    }
                    if (accountExpired) {
                        debug.message("Account expired!!");
                        logFailedMessage = bundle.getString("accountExpired");
                        logFailedError = "ACCOUNTEXPIRED";
                        failureReason = ACCOUNT_EXPIRED;
                        loginState.setErrorCode(AMAuthErrorCode.AUTH_ACCOUNT_EXPIRED);
                        isFailed = true;
                    } else {
                        // came here successful auth.
                        if (debug.messageEnabled()) {
                            debug.message("authContext is : " + authContext);
                            debug.message("loginSTate is : " + loginState);
                        }

                        updateLoginState(indexType, indexName, configName, orgDN);
                        //activate session
                        Object lcInSession;
                        if (isPureJAAS()) {
                            lcInSession = loginContext;
                        } else {
                            lcInSession = jaasLoginContext;
                        }
                        boolean sessionActivated = loginState.activateSession(subject, authContext, lcInSession);
                        if (sessionActivated) {
                            loginState.logSuccess();
                            auditor.auditLoginSuccess(loginState);
                            if (amAccountLockout.isLockoutEnabled()) {
                                amAccountLockout.resetPasswdLockout(loginState.getUserDN(), true);
                            }
                            loginStatus.setStatus(LoginStatus.AUTH_SUCCESS);
                            loginState.updateSessionForFailover();
                            debug.message("login success");
                        } else {
                            logFailedMessage = AuthUtils.getErrorVal(AMAuthErrorCode.AUTH_MAX_SESSION_REACHED,
                                    AuthUtils.ERROR_MESSAGE);
                            logFailedError = "MAXSESSIONREACHED";
                            failureReason = MAX_SESSION_REACHED;
                            throw new AuthException(AMAuthErrorCode.AUTH_MAX_SESSION_REACHED, null);
                        }
                    }
                }
            }
        } catch (InvalidPasswordException ipe) {
            debug.message("Invalid Password : ");
            if (debug.messageEnabled()) {
                debug.message("Exception ", ipe);
            }

            String failedUserId = ipe.getTokenId();

            if (debug.messageEnabled()) {
                debug.message("Invalid Password Exception " + failedUserId);
            }

            if (failedUserId != null) {
                amAccountLockout = new AMAccountLockout(loginState);
                accountLocked = amAccountLockout.isLockedOut(failedUserId);
                if ((!accountLocked) && (amAccountLockout.isLockoutEnabled())) {
                    amAccountLockout.invalidPasswd(failedUserId);
                    checkWarningCount(amAccountLockout);
                    accountLocked = amAccountLockout.isAccountLocked(failedUserId);
                }
            }

            logFailedMessage = bundle.getString("invalidPasswd");
            logFailedError = "INVALIDPASSWORD";
            failureReason = INVALID_PASSWORD;
            if (accountLocked) {
                if (failedUserId != null) {
                    loginState.logFailed(failedUserId, "LOCKEDOUT");
                } else {
                    loginState.logFailed("LOCKEDOUT");
                }
                auditor.auditLoginFailure(loginState, LOCKED_OUT);
            }

            loginState.setErrorCode(AMAuthErrorCode.AUTH_LOGIN_FAILED);

            isFailed = true;
            authContext.setLoginException(ipe);
        } catch (AuthErrorCodeException e) {
            if (debug.messageEnabled()) {
                debug.message(e.getMessage());
            }
            isFailed = true;
            java.util.Locale locale = com.sun.identity.shared.locale.Locale.getLocale(loginState.getLocale());
            loginState.setModuleErrorMessage(e.getL10NMessage(locale));
            loginState.setErrorCode(e.getAuthErrorCode());
            authContext.setLoginException(e);
        } catch (MessageLoginException me) {
            if (debug.messageEnabled()) {
                debug.message("LOGINFAILED MessageAuthLoginException....");
                debug.message("Exception " , me);
            }

            java.util.Locale locale = com.sun.identity.shared.locale.Locale.getLocale(loginState.getLocale());
            loginState.setModuleErrorMessage(me.getL10NMessage(locale));
            loginState.setErrorMessage(me.getL10NMessage(locale));
            isFailed = true;
            authContext.setLoginException(me);
        } catch (AuthLoginException le) {
            loginState.setErrorCode(AMAuthErrorCode.AUTH_LOGIN_FAILED);
            if (AMAuthErrorCode.AUTH_MODULE_DENIED.equals(le.getMessage())) {
                if (debug.warningEnabled()) {
                    debug.warning(
                            "AMLoginContext.runLogin():auth failed, using invalid auth module name for internal user");
                }
                logFailedMessage = AuthUtils.getErrorVal(AMAuthErrorCode.AUTH_MODULE_DENIED, AuthUtils.ERROR_MESSAGE);
                logFailedError = "MODULEDENIED";
                failureReason = MODULE_DENIED;
                loginState.setErrorCode(AMAuthErrorCode.AUTH_MODULE_DENIED);
            } else if (AMAuthErrorCode.AUTH_TIMEOUT.equals(le.getMessage())) {
                debug.message("LOGINFAILED Error Timed Out....");
            } else if (ISAuthConstants.EXCEED_RETRY_LIMIT.equals(le.getErrorCode())) {
                debug.message("LOGINFAILED ExceedRetryLimit");
            } else {
                debug.message("LOGINFAILED Error....");
            }
            if (debug.messageEnabled()) {
                debug.message("Exception : ", le);
            }
            isFailed = true;
            if (loginState.isTimedOut()) {
                logFailedMessage = bundle.getString("loginTimeout");
                logFailedError = "LOGINTIMEOUT";
                failureReason = LOGIN_TIMEOUT;
                loginState.setErrorCode(AMAuthErrorCode.AUTH_TIMEOUT);
            } else if (ISAuthConstants.EXCEED_RETRY_LIMIT.equals(le.getErrorCode())) {
                loginState.setErrorMessage(exceedRetryLimit);
                loginState.setErrorCode(AMAuthErrorCode.AUTH_USER_LOCKED_IN_DS);
            } else if (ISAuthConstants.SERVER_UNWILLING.equals(le.getErrorCode())) {
                loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
            }
            authContext.setLoginException(le);
        } catch (AuthException e) {
            if (debug.messageEnabled()) {
                debug.message("Exception : " + e.getMessage());
            }
            isFailed = true;
            loginState.setErrorCode(e.getErrorCode());
            loginState.logFailed(bundle.getString("loginFailed"));
            logFailedError = null;
            authContext.setLoginException(new AuthLoginException(BUNDLE_NAME, "loginFailed", null, e));
        } catch (Exception e) {
            debug.message("Error during login.. ");
            if (debug.messageEnabled()) {
                debug.message("Exception ", e);
            }
            isFailed = true;
            loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
            loginState.logFailed(bundle.getString("loginFailed"));
            logFailedError = null;
            authContext.setLoginException(new AuthLoginException(BUNDLE_NAME, "loginFailed", null, e));
        } catch (DSAMECallbackHandlerError error) {
            debug.message("Caught error returned from DSAMEHandler");
            return;
        }
        debug.message("Came to before if Failed loop");

        if (isFailed) {
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
                if (authImpl != null) {
                    authImpl.incSsoServerAuthenticationFailureCount();
                }
            }
            if (loginSuccess) {
                // this is the case where authentication to modules
                // succeeded but framework failed to validate the
                // user, in this case populate with all module user
                // successfully authenticated as.
                loginState.setFailureModuleList(getSuccessModuleString(orgDN));

            } else {
                loginState.setFailureModuleList(getFailureModuleList(orgDN));
            }
            loginState.logFailed(logFailedMessage, logFailedError);
            auditor.auditLoginFailure(loginState, failureReason);
            setErrorMsgAndTemplate();
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            if (indexType == IndexType.USER) {
                if (debug.messageEnabled()) {
                    debug.message("Set failureId in user based auth " + indexName);
                }
                loginState.setFailedUserId(indexName);
            }
        } else {
            if (debug.messageEnabled()) {
                debug.message("AMLoginContext.runLogin: calling incSsoServerAuthenticationSuccessCount");
            }
            if (MonitoringUtil.isRunning()) {
                if (authImpl == null) {
                    authImpl = Agent.getAuthSvcMBean();
                }
            }
            if (authImpl != null && !loginState.isNoSession()) {
                authImpl.incSsoServerAuthenticationSuccessCount();
            }
        }

        if (debug.messageEnabled()) {
            debug.message("finished...login notify all threads\n"
                    + "AMLoginContext:LoginStatus: " + loginStatus.getStatus());
        }
        if (isPureJAAS()) {
            authThread.removeFromHash(thread, "timeoutHash");

            // notify possible waiting thread
            loginState.setReceivedCallback(null, this);
        }

        isFailed = false;
        nullifyUsedVars();
    }

    /**
     * Logs out.
     *
     * @throws AuthLoginException when fails to logout
     */
    public void logout() throws AuthLoginException {
        debug.message("in logout:");
        try {
            if (isPureJAAS()) {
                if (loginContext != null) {
                    loginContext.logout();
                }
            } else {
                if (jaasLoginContext != null) {
                    jaasLoginContext.logout();
                }
            }
            loginState.logLogout();
            auditor.auditLogout(getSSOToken());
            loginState.postProcess(indexType, indexName, LoginState.PostProcessEvent.LOGOUT);
            destroySession();
            loginStatus.setStatus(LoginStatus.AUTH_COMPLETED);
        } catch (AuthLoginException le) {
            debug.message("Error during logout : ");
            if (debug.messageEnabled()) {
                debug.message("Exception " , le);
            }
            //logout - ignore this error since logout will be done
            throw new AuthLoginException(BUNDLE_NAME, "failedLogout", null, le);
        } catch (Exception e) {
            debug.message("Error during logout : ");
            if (debug.messageEnabled()) {
                debug.message("Exception " , e);
            }
        }
    }

    /* destroy Session on a logout OR abort */
    void destroySession() {
        if (debug.messageEnabled()) {
            debug.message("AMLoginContext:destroySession: " + loginState);
        }
        loginState.destroySession();
    }

    /**
     * Returns array of received callbacks from module.
     *
     * @return array of received callbacks from module.
     */
    public Callback[] getRequiredInfo() {
        if (loginStatus.getStatus() != LoginStatus.AUTH_IN_PROGRESS) {
            return null;
        }
        if (indexType == IndexType.LEVEL || indexType == IndexType.COMPOSITE_ADVICE) {
            debug.message("IndexType level/composite_advice, send choice callback");
            // reset indexType since UI will start module based auth
            indexType = null;
        } else {
            if (isPureJAAS()) {
                recdCallback = getRequiredInfoCallback();
            } else {
                recdCallback = getRequiredInfoCallback_NoThread();
            }
        }

        if (recdCallback != null) {
            if (debug.messageEnabled()) {
                for (Callback callback : recdCallback) {
                    debug.message("Recd Callback in amlc.getRequiredInfo : " + callback);
                }
            }
        } else {
            debug.message("Recd Callback in amlc.getRequiredInfo is NULL");
        }

        return recdCallback;
    }

    /**
     * Returns array of  required callback information non-JAAS thread mode
     * @return callbacks required <code>Callbacks</code> array to be submitted
     */
    public Callback[] getRequiredInfoCallback_NoThread() {
        return loginState.getReceivedInfo();
    }


    /**
     * Returns the array of required Callbacks from <code>CallbackHandler</code>
     * waits till <code>loginState::getReceivedInfo()</code> OR
     * authentication status is not <code>AUTH_IN_PROGRESS</code> OR
     * if thread receives a notify .
     *
     * @return array of Required Callbacks from <code>CallbackHandler</code>.
     */
    public synchronized Callback[] getRequiredInfoCallback() {
        if (debug.messageEnabled()) {
            debug.message("getRequiredInfo.. " + loginStatus.getStatus());
        }
        if (isFailed || (loginStatus.getStatus() != LoginStatus.AUTH_IN_PROGRESS)) {
            debug.message("no more requirements returning null");
            return null;
        }
        Thread thread = Thread.currentThread();
        long lastCallbackSent = loginState.getLastCallbackSent();
        long pageTimeOut = loginState.getPageTimeOut();
        if (debug.messageEnabled()) {
            debug.message("getRequiredInfo. ThreadName is.. :" + thread);
            debug.message("lastCallbackSent : " + lastCallbackSent);
            debug.message("pageTimeOut : " + pageTimeOut);
        }
        authThread.setHash(thread, pageTimeOut, lastCallbackSent);

        while ((!isFailed) && (loginState.getReceivedInfo() == null)
                && (loginStatus.getStatus() == LoginStatus.AUTH_IN_PROGRESS)) {
            try {
                if (debug.messageEnabled()) {
                    debug.message(Thread.currentThread() + "Waiting.." + loginStatus.getStatus());
                }
                if (loginStatus.getStatus() != LoginStatus.AUTH_IN_PROGRESS) {
                    return null;
                }
                if (!isFailed
                        && loginStatus.getStatus() == LoginStatus.AUTH_IN_PROGRESS
                        && loginState.getReceivedInfo() == null) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                debug.message("getRecdinfo INTERRUPTED");
                break;
            }
        }
        if (debug.messageEnabled()) {
            debug.message("Thread woke up... " + loginState.getReceivedInfo());
        }
        Callback[] getRequiredInfo = loginState.getReceivedInfo();
        if (debug.messageEnabled()) {
            debug.message("Returning getRequiredInfo... :" + getRequiredInfo);
        }
        authThread.removeFromHash(thread, "timeoutHash");
        return getRequiredInfo;
    }

    /**
     * Sets the submitted requirements, called by
     * <code>AuthContext.submitRequirements</code>
     * <code>loginState.setSubmittedCallback</code> is update.
     *
     * @param callback submit the required <code>Callbacks</code>
     */
    public void submitRequiredInfo(Callback[] callback) {
        if (debug.messageEnabled() && callback != null && callback.length > 0) {
            debug.message("submit required info... :" + callback[0]);
        }
        if (isPureJAAS()) {
            loginState.setSubmittedCallback(callback, this);
        } else {
            loginState.setSubmittedCallback_NoThread(callback);
        }
        if (debug.messageEnabled()) {
            debug.message("Returning from submitRequiredInfo");
        }
    }


    /**
     * <code>CallbackHandler</code> calls this to retrieve the submitted
     * credentials/callbacks waits till
     * <code>loginState.setSubmittedCallback</code> is set OR
     * <code>LoginStatus</code> is not <code>AUTH_IN_PROGRESS</code>.
     *
     * @return submitted credentials/callbacks.
     */
    public synchronized Callback[] submitCallbackInfo() {
        if (debug.messageEnabled()) {
            debug.message("submitRequiredInfo. ThreadName is.. :" + Thread.currentThread().getName());
        }

        if (loginStatus.getStatus() != LoginStatus.AUTH_IN_PROGRESS || isFailed) {
            debug.message("submitReq no more requirements returning null");
            return null;
        }

        Thread thread = Thread.currentThread();
        long lastCallbackSent = loginState.getLastCallbackSent();
        long pageTimeOut = loginState.getPageTimeOut();
        if (debug.messageEnabled()) {
            debug.message("submitRequiredInfo. ThreadName is.. :" + thread);
            debug.message("lastCallbackSent : " + lastCallbackSent);
            debug.message("pageTimeOut : " + pageTimeOut);
        }
        authThread.setHash(thread,pageTimeOut, lastCallbackSent);
        while (loginState.getSubmittedInfo() == null && loginStatus.getStatus() == LoginStatus.AUTH_IN_PROGRESS) {
            try {
                if (debug.messageEnabled()) {
                    debug.message(Thread.currentThread() + " Waiting...." + loginStatus.getStatus());
                }
                if (loginStatus.getStatus() != LoginStatus.AUTH_IN_PROGRESS) {
                    return null;
                }
                if ((loginState.getSubmittedInfo() == null)) {
                    wait();
                }
            } catch (InterruptedException e) {
                debug.message("submitRequired info INTERRUPTED");
                break;
            }
        }
        debug.message("Threadwaking up go submit info...");
        authThread.removeFromHash(thread, "timeoutHash");
        Callback[] setSubmittedInfo = loginState.getSubmittedInfo();
        debug.message("Returning submitted info: ");
        return setSubmittedInfo;
    }

    /**
     * Returns the authentication status.
     *
     * @return the authentication status.
     */
    public int getStatus() {
        int status  = loginStatus.getStatus();

        if (isFailed || status == LoginStatus.AUTH_FAILED) {
            postProcessOnFail();
        } else if (status == LoginStatus.AUTH_SUCCESS) {
            postProcessOnSuccess();
        }
        if (debug.messageEnabled()) {
            debug.message("getStatus : status is... : " + status);
        }
        return status;

    }

    /**
     * Returns login state for the authentication context.
     *
     * @return login state for the authentication context.
     */
    public LoginState getLoginState() {
        return AuthUtils.getLoginState(authContext);
    }


    /**
     * Terminates an ongoing login process.
     *
     * @throws AuthLoginException when fails to abort
     */
    public void abort() throws AuthLoginException {

        debug.message("in abort");
        try {
            logout();
        } catch (Exception ae) {
            if (debug.messageEnabled()) {
                debug.message("Error logging out.. :");
                debug.message("Exception ", ae);
            }
            try {
                destroySession();
                loginStatus.setStatus(LoginStatus.AUTH_COMPLETED);
            } catch (Exception e) {
                debug.message("Error aborting");
                if (debug.messageEnabled()) {
                    debug.message("Exception ", e);
                }

                // abort this error - since abort will be done
                throw new AuthLoginException(BUNDLE_NAME, "abortFailed", null);
            }
        }
    }


    /**
     * Returns authentication modules configured for a given organization.
     *
     * @return authentication modules configured for a given organization.
     */
    public Set<String> getModuleInstanceNames() {
        try {
            LoginState loginState = AuthUtils.getLoginState(authContext);

            if (loginState != null) {
                moduleSet = loginState.getModuleInstances();
            }

            if (debug.messageEnabled()) {
                debug.message("moduleSet is : " + moduleSet);
            }
        } catch (Exception e) {
            debug.message("Error : " , e);
        }

        return moduleSet;
    }

    /**
     * Returns organization/suborganization for a request.
     *
     * @return organization/suborganization for a request.
     */
    public String getOrganizationName() {
        return loginState.getQueryOrg();
    }

    /**
     * Returns Single Sign On Token for authenticated user, returns null if
     * session is inactive.
     *
     * @return Single Sign On Token for authenticated user.
     */
    public SSOToken getSSOToken() {
        try {
            return loginState.getSSOToken();
        } catch (SSOException e) {
            if (debug.messageEnabled()) {
                debug.message("error getting ssoToken : " );
                debug.message("Exception " , e);
            }
            return null;
        }
    }

    /**
     * Returns Login Success URL for authenticated user.
     *
     * @return Login Success URL for authenticated user.
     */
    public String getSuccessURL() {
        try {
            return loginState.getSuccessLoginURL();
        } catch (Exception e) {
            if (debug.messageEnabled()) {
                debug.message("error getting successURL : " + e.toString());
            }
            return null;
        }
    }

    /**
     * Returns Login Failure URL for authenticated user.
     *
     * @return Login Failure URL for authenticated user.
     */
    public String getFailureURL() {
        try {
            return loginState.getFailureLoginURL();
        } catch (Exception e) {
            if (debug.messageEnabled()) {
                debug.message("error getting failureURL : " + e.toString());
            }
            return null;
        }
    }

    /**
     * Returns the current <code>authIdentifier</code> of the authentication
     * process as String Session ID.
     *
     * @return <code>authIdentifier</code> of the authentication process.
     */
    public String getAuthIdentifier() {
        String sidString = null;
        try {
            sidString = loginState.getSid().toString();
        } catch (Exception e) {
            if (debug.messageEnabled()) {
                debug.message("Error retrieving sid from LoginState : " + e.getMessage());
            }
        }
        return sidString;
    }

    /**
     * Returns the subject of authenticated user.
     *
     * @return the subject of authenticated user.
     */
    public Subject getSubject() {

        try {
            return loginState.getSubject();
        } catch (Exception e) {
            if (debug.messageEnabled()) {
                debug.message("error getting Subject :");
                debug.message("Exception " , e);
            }

            return null;
        }
    }

    /* retrieve login parameters */
    private void parseLoginParams(Map<String, Object> loginParamsMap) {

        if (debug.messageEnabled()) {
            debug.message("loginParamsMap is.. :" + loginParamsMap);
        }

        try {
            indexType = (IndexType) loginParamsMap.get("indexType");
            indexName = (String) loginParamsMap.get("indexName");
            if (debug.messageEnabled()) {
                debug.message("indexType = " + indexType + "\nindexName = " + indexName);
            }
            //principal = (Principal) loginParamsMap.get("principal");
            //password = (char[]) loginParamsMap.get("password");
            subject = (Subject) loginParamsMap.get("subject");

            String locale = (String) loginParamsMap.get("locale");
            if (StringUtils.isNotEmpty(locale)) {
                loginState.setLocale(locale);
            }
        } catch (Exception e) {
            if (debug.messageEnabled()) {
                debug.message("Error parsing login Params");
                debug.message("Exception " , e);
            }
        }
    }

    /* retrieve config name from config component based on the
     * indexType , indexName , orgDN and clientType
     * if indexType , indexName are null then indexType is assumed
     * to be org
     */
    String getConfigName(IndexType indexType, String indexName, String orgDN, String clientType) {
        String configName = null;
        String universalID;

        // if index type is null assume org based authentication
        if (indexType == null) {
            configName = AMAuthConfigUtils.getAuthConfigName(orgDN, "html");
        } else {
            if (indexType == IndexType.USER) {
                universalID = loginState.getUserUniversalId(indexName);
            } else if (indexType == IndexType.ROLE) {
                universalID = loginState.getRoleUniversalId(indexName);
            } else {
                // means the index type is not ROLE or USER
                // for SERVICE , MODULE pass the indexName as is
                universalID = indexName;
            }
            try {
                if (universalID != null ) {
                    configName = AMAuthConfigUtils.getAuthConfigName(indexType, universalID, orgDN, clientType);
                }
            } catch (Exception e) {
                if (debug.messageEnabled()) {
                    debug.message("Error retrieving configName ");
                    debug.message("Exception : " , e);
                }
            }
        }
        return configName;
    }

    /* for indexType level retreive the module names .
     * if the more then 1 modules has the same level
     * then generate choice callback , else if module
     * is 1 then start module based authentication.
     * throws Exception if no modules are found
     */
    boolean processLevel(IndexType indexType, String indexName, String orgDN, String clientType)
            throws AuthException, AuthLoginException {

        throwExceptionIfModuleBasedAuthenticationDisabled();

        indexType= IndexType.LEVEL;

        java.util.Locale loc = com.sun.identity.shared.locale.Locale.getLocale(loginState.getLocale());
        AuthLevel authLevel = new AuthLevel(indexType, indexName, orgDN, clientType, loc);
        int numberOfModules = authLevel.getNumberOfAuthModules();
        if (debug.messageEnabled()) {
            debug.message("number of Modules : " + numberOfModules);
        }

        if (numberOfModules <= 0) {
            loginState.logFailed(bundle.getString("noConfig"), "NOCONFIG");
            auditor.auditLoginFailure(loginState, NO_CONFIG);
            throw new AuthException(AMAuthErrorCode.AUTH_CONFIG_NOT_FOUND, null);
        } else if (numberOfModules == 1) {
            this.indexType = IndexType.MODULE_INSTANCE;
            loginState.setIndexType(this.indexType);
            this.indexName = authLevel.getModuleName();
            return false;
        } else {
            try {
                recdCallback = authLevel.createChoiceCallback();
                loginState.setPrevCallback(recdCallback);
                loginState.setModuleMap(authLevel.getModuleMap());
                return true;
            } catch (AuthException ae) {
                if (debug.messageEnabled()) {
                    debug.message("Error creating choiceCallback");
                    debug.message("Exception " , ae);
                }
                return false;
            }
        }
    }

    /* for indexType composite_advice retrieves the module names .
     * if there is more then one modules required in composite advice
     * then generate choice callback , else if module
     * is 1 then start module based authentication.
     * throws Exception if no modules are found
     */
    boolean processCompositeAdvice(IndexType indexType, String indexName, String orgDN, String clientType)
            throws AuthException, AuthLoginException {

        java.util.Locale loc = com.sun.identity.shared.locale.Locale.getLocale(loginState.getLocale());
        CompositeAdvices compositeAdvice = new CompositeAdvices(indexName, orgDN, clientType, loc);

        if (compositeAdvice.getType() == AuthUtils.MODULE) {
            throwExceptionIfModuleBasedAuthenticationDisabled();
        }

        int numberOfModules = compositeAdvice.getNumberOfAuthModules();
        if (debug.messageEnabled()) {
            debug.message("processCompositeAdvice:number of Modules/Services : " + numberOfModules);
        }
        loginState.setCompositeAdviceType(compositeAdvice.getType());

        if (numberOfModules <= 0) {

            loginState.logFailed(bundle.getString("noConfig"));
            auditor.auditLoginFailure(loginState, NO_CONFIG);
            throw new AuthException(AMAuthErrorCode.AUTH_CONFIG_NOT_FOUND, null);

        } else if (numberOfModules == 1) {

            this.indexName = AMAuthUtils.getDataFromRealmQualifiedData(compositeAdvice.getModuleName());
            String qualifiedRealm = AMAuthUtils.getRealmFromRealmQualifiedData(compositeAdvice.getModuleName());
            if (StringUtils.isNotEmpty(qualifiedRealm)) {
                this.orgDN = DNMapper.orgNameToDN(qualifiedRealm);
                loginState.setQualifiedOrgDN(this.orgDN);
            }
            if (compositeAdvice.getType() == AuthUtils.MODULE) {
                this.indexType = IndexType.MODULE_INSTANCE;
            } else if (compositeAdvice.getType() == AuthUtils.SERVICE) {
                this.indexType = IndexType.SERVICE;
            } else if (compositeAdvice.getType() == AuthUtils.REALM) {
                this.orgDN = DNMapper.orgNameToDN(compositeAdvice.getModuleName());
                loginState.setQualifiedOrgDN(this.orgDN);
                this.indexName = AuthUtils.getOrgConfiguredAuthenticationChain(this.orgDN);
                this.indexType = IndexType.SERVICE;
            }
            loginState.setIndexType(this.indexType);
            loginState.setIndexName(this.indexName);
            if (debug.messageEnabled()) {
                debug.message("processCompositeAdvice:indexType : " + this.indexType);
                debug.message("processCompositeAdvice:indexName : " + this.indexName);
            }
            return false;

        } else {

            try {
                recdCallback = compositeAdvice.createChoiceCallback();
                loginState.setPrevCallback(recdCallback);
                loginState.setModuleMap(compositeAdvice.getModuleMap());
                return true;
            } catch (AuthException ae) {
                if (debug.messageEnabled()) {
                    debug.message("Error creating choiceCallback");
                    debug.message("Exception " , ae);
                }
                return false;
            }

        }
    }

    /*
     * Throw an exception as module-based authentication is disabled.
     */
    private void throwExceptionIfModuleBasedAuthenticationDisabled() throws AuthLoginException {
        if (!loginState.getEnableModuleBasedAuth()) {
            debug.error("Error: Module Based Auth is not allowed");
            loginStatus.setStatus(LoginStatus.AUTH_FAILED);
            loginState.setErrorCode(AMAuthErrorCode.MODULE_BASED_AUTH_NOT_ALLOWED);
            setErrorMsgAndTemplate();
            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.MODULE_BASED_AUTH_NOT_ALLOWED, null);
        }
    }

    /* update login state with indexType,indexName */
    void updateLoginState(IndexType indexType, String indexName, String configName, String orgDN) {
        // set authLevel in LoginState

        String authLevel;
        if (indexType == IndexType.LEVEL) {
            authLevel = indexName;
        } else {
            // retrieve from config component check with Qingwen
            // config component will return the max level in case
            // of multiple authentication.
            //authLevel=AMAuthConfigUtils.getAuthLevel(configName);
            authLevel = getAuthLevel(orgDN);
        }

        loginState.setAuthLevel(authLevel);

        // set the module name
        String moduleName;

        if (indexType == IndexType.MODULE_INSTANCE) {
            moduleName = indexName;
        } else {
            moduleName = getSuccessModuleString(orgDN);
        }

        if (debug.messageEnabled()) {
            debug.message("moduleName : " + moduleName);
        }

        loginState.setAuthModuleName(moduleName);
    }

    /* check if user exists and is enabled if not return
     * false - login process should not continue
     */
    boolean validateUser(String userName) {
        try {
            boolean userProfileExists = loginState.getUserProfile(userName, true);
            return ((userProfileExists) && (loginState.isUserEnabled()));
        } catch (Exception e) {
            if (debug.messageEnabled()) {
                debug.message("Error retrieving profile for : " + userName);
            }
            return false;
        }
    }

    /**
     * Checks the warning count to determine the lockout message
     * to be displayed to the user.
     *
     * @param amAccountLockout the account lockout object.
     */
    void checkWarningCount(AMAccountLockout amAccountLockout) {
        try {
            int warningCount = amAccountLockout.getWarnUserCount();
            if (warningCount == 0) {
                lockoutMsg = ISAuthConstants.EMPTY_STRING;
            } else {
                if (warningCount < 0) {
                    accountLocked=true;
                } else  {
                    String lockoutMsgFmt = bundle.getString("lockOutWarning");
                    Object [] params = new Object[1];
                    params[0] = new Integer(warningCount);
                    lockoutMsg = MessageFormat.format(lockoutMsgFmt, params);
                    loginState.setLockoutMsg(lockoutMsg);
                    accountLocked = false;
                }
            }

            if (debug.messageEnabled()) {
                debug.message("WARNING COUNT : " + warningCount);
                debug.message("WARNING COUNT MESSAGE: " + lockoutMsg);
            }
        } catch (Exception e) {
            debug.message("Error : ", e);
        }
    }

    /**
     * Sets the error message and template
     */
    void setErrorMsgAndTemplate() {
        if (loginState == null) {
            return;
        }
        String errorCode = loginState.getErrorCode();

        if (errorCode != null) {
            String resProperty = bundle.getString(errorCode);
            if (debug.messageEnabled()) {
                debug.message("resProperty is.. :" + resProperty);
            }
            String errorMsg = AuthUtils.getErrorVal(errorCode, AuthUtils.ERROR_MESSAGE);
            String templateName = AuthUtils.getErrorVal(errorCode, AuthUtils.ERROR_TEMPLATE);

            if (debug.messageEnabled()) {
                debug.message("Error Message : " + errorMsg);
                debug.message("Error Template: " + templateName);
            }

            loginState.setErrorMessage(errorMsg);
            loginState.setErrorTemplate(templateName);
        }
    }

    /* for error handling - methods to return error code , module error
     * template , framework error template , error message
     */
    String getTimedOutTemplate() {
        loginState.setErrorCode(AMAuthErrorCode.AUTH_TIMEOUT);
        loginState.logFailed(bundle.getString("loginTimeout"), "LOGINTIMEOUT");
        auditor.auditLoginFailure(loginState, LOGIN_TIMEOUT);
        loginState.setErrorMessage(AuthUtils.getErrorVal(AMAuthErrorCode.AUTH_TIMEOUT, AuthUtils.ERROR_MESSAGE));
        return AuthUtils.getErrorVal(AMAuthErrorCode.AUTH_TIMEOUT, AuthUtils.ERROR_TEMPLATE);
    }

    /**
     * Returns error template.
     *
     * @return error template.
     */
    public String getErrorTemplate() {

        String errorTemplate;
        if (loginState == null) {
            errorTemplate = AuthUtils.getErrorVal(AMAuthErrorCode.AUTH_ERROR, AuthUtils.ERROR_TEMPLATE);
            return errorTemplate;
        }
        if (loginState.isTimedOut()) {
            errorTemplate = getTimedOutTemplate();
        } else {
            errorTemplate = loginState.getModuleErrorTemplate();
            if (errorTemplate == null || errorTemplate.equals(ISAuthConstants.EMPTY_STRING)) {
                errorTemplate = loginState.getErrorTemplate();
            }
        }
        if (debug.messageEnabled()) {
            debug.message("Error Template is : " + errorTemplate);
        }
        loginState.setErrorTemplate(errorTemplate);
        return errorTemplate;
    }

    /**
     * Returns error message.
     *
     * @return error message.
     */
    public String getErrorMessage() {

        if (loginState == null) {
            return AuthUtils.getErrorVal(AMAuthErrorCode.AUTH_ERROR, AuthUtils.ERROR_MESSAGE);
        }

        String errorMsg = loginState.getModuleErrorMessage();
        if (errorMsg == null) {
            errorMsg = loginState.getErrorMessage();
        }

        if (debug.messageEnabled()) {
            debug.message("Error message is : " + errorMsg);
        }

        return errorMsg;
    }

    /**
     * Returns error code.
     *
     * @return Authentication error code.
     */
    public String getErrorCode() {

        if (loginState == null) {
            return AMAuthErrorCode.AUTH_ERROR;
        }
        String errorCode = loginState.getErrorCode();

        if (debug.messageEnabled()) {
            debug.message("Error Code is.. : " + errorCode);
        }

        return errorCode;
    }

    /**
     * Gets the account lockout message
     * @return account lockout message
     */
    public String getLockoutMsg() {
        if (debug.messageEnabled()) {
            debug.message("lockout Msg returned  : " + lockoutMsg);
        }
        return lockoutMsg;
    }

    /**
     * Checks if the account is locked
     * @return <code>true</code> if account is locked
     */
    public boolean isLockedOut() {
        return accountLocked;
    }

    /* get the authlevel
     * gets the module list for a given config for all
     * modules having option REQUIRED, REQUISITE
     * gets the level for each module in the list
     * the highest level will be set.
     */
    String getAuthLevel(String orgDN) {

        AMAuthLevelManager levelManager = AMAuthLevelManager.getInstance();
        int maxLevel = Integer.MIN_VALUE;

        if (moduleSet == null || moduleSet.isEmpty()) {
            moduleSet = getSuccessModuleSet(orgDN);
        }

        for (String moduleName : moduleSet) {
            int authLevel = levelManager.getLevelForModule(moduleName, orgDN, loginState.getDefaultAuthLevel());
            if (authLevel > maxLevel)  {
                maxLevel = authLevel;
            }

            if (debug.messageEnabled()) {
                debug.message("AuthLevel is : " + authLevel);
                debug.message("New AuthLevel is : " + maxLevel);
            }
        }

        if (debug.messageEnabled()) {
            debug.message("Returning AuthLevel is : " + maxLevel);
        }

        return (new Integer(maxLevel)).toString();
    }

    /* return the module list
     * this methods gets the configuration list for a given configName
     * retreives all module names which have option REQUIRED , REQUISITE
     * if org.forgerock.openam.authLevel.excludeRequiredOrRequisite is false
     */
    Set<String> getSuccessModuleSet(String orgDN) {

        try {
            Set<String> successModuleSet = loginState.getSuccessModuleSet();
            if (excludeRequiredOrRequisite) {
                if (debug.messageEnabled()) {
                    debug.message("get success modules excluding REQUIRED or REQUISITE in chain.");
                }
                moduleSet = successModuleSet;
            } else {
                if (debug.messageEnabled()) {
                    debug.message("retrieve all modules names with option REQUIRED or REQUISITE.");
                }
                moduleSet = getModuleFromAuthConfiguration(successModuleSet, orgDN);
            }

            if (debug.messageEnabled()) {
                debug.message("ModuleSet is : " + moduleSet);
            }
        } catch (Exception e) {
            debug.message("Exception : getSuccessModuleList " , e);
        }
        return moduleSet;
    }

    /* constructs a module list string where each module is
     * separated by a "|" e.g module1 | module2 | module3
     */
    String getModuleString(Set<String> moduleSet) {

        final String moduleList = moduleSet == null || moduleSet.isEmpty() ?
                ISAuthConstants.EMPTY_STRING :
                org.apache.commons.lang.StringUtils.join(moduleSet, LIST_DELIMITER);

        if (debug.messageEnabled()) {
            debug.message("ModuleList is : " + moduleList);
        }

        return moduleList;
    }


    /* do the required process for different indextypes
     * return true if needs to return back
     * false if needs to continue
     * Exception if error
     */
    boolean processIndexType(IndexType indexType, String indexName, String orgDN) throws AuthLoginException {
        boolean ignoreProfile = false;
        IndexType previousType = loginState.getPreviousIndexType();

        /*
         * Throw an exception if org specified in query does not match org specified in authContext/loginState
         *
         * (unless previous index type was LEVEL or COMPOSITE_ADVICE, or current index type is MODULE_INSTANCE)
         */
        String normOrgDN = DNUtils.normalizeDN(orgDN);
        if ((previousType != IndexType.LEVEL && previousType != IndexType.COMPOSITE_ADVICE)
                || indexType != IndexType.MODULE_INSTANCE) {
            // proceed only when the org in the auth context matches
            // that in the query. otherwise it means a call with a new org.
            HttpServletRequest hreq = loginState.getHttpServletRequest();
            boolean isTokenValid = false;
            final boolean isFederation = indexType == IndexType.MODULE_INSTANCE
                    && ISAuthConstants.FEDERATION_MODULE.equals(indexName);
            if (hreq != null && !isFederation) {
                try {
                    SSOTokenManager manager = SSOTokenManager.getInstance();
                    SSOToken ssoToken = manager.createSSOToken(hreq);
                    if (manager.isValidToken(ssoToken)) {
                        debug.message("Existing Valid session");
                        isTokenValid = true;
                    }
                } catch (Exception e) {
                    debug.message("ERROR processIndexType/SSOToken validation - " + e.toString());
                }

                if (!isTokenValid) {
                    debug.message("No existing valid session");
                    Hashtable requestHash = loginState.getRequestParamHash();
                    String newOrgDN = AuthUtils.getDomainNameByRequest(hreq, requestHash);
                    if (debug.messageEnabled()) {
                        debug.message("orgDN from existing auth context: " + orgDN +
                                ", orgDN from query string: " + newOrgDN);
                    }
                    if (normOrgDN != null) {
                        if (!normOrgDN.equals(newOrgDN)) {
                            loginStatus.setStatus(LoginStatus.AUTH_RESET);
                            loginState.setErrorCode(AMAuthErrorCode.AUTH_ERROR);
                            setErrorMsgAndTemplate();
                            internalAuthError = true;
                            throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_ERROR, null);
                        }
                    }
                }

            }
        }

        if (indexType == IndexType.COMPOSITE_ADVICE)  {
            /*
             * Configure login following COMPOSITE_ADVICE
             */

            debug.message("IndexType is COMPOSITE_ADVICE");
            // Set the Composite Advice in Login State after decoding
            String compositeAdvice = URLEncDec.decode(indexName);
            loginState.setCompositeAdvice(compositeAdvice);
            // if multiple modules are found then return
            // else continue with login process
            try {
                if (processCompositeAdvice(indexType, indexName, orgDN, clientType)) {
                    debug.message("multiple modules found");
                    return true;
                } else {
                    return false;
                }
            } catch (AuthException ae) {
                // no modules configured
                loginState.setErrorCode(ae.getErrorCode());
                loginState.logFailed(ae.getMessage());
                auditor.auditLoginFailure(loginState);
                setErrorMsgAndTemplate();
                loginStatus.setStatus(LoginStatus.AUTH_FAILED);
                throw new AuthLoginException(ae);
            }

        } else if (indexType == IndexType.LEVEL)  {
            /*
             * Configure login so that successful authentication achieve specified authentication LEVEL
             */

            debug.message("IndexType is level");
            // if multiple modules are found then return
            // else continue with login process
            try {
                if (processLevel(indexType, indexName, orgDN, clientType)) {
                    debug.message("multiple modules found");
                    return true;
                } else {
                    return false;
                }
            } catch (AuthException ae) {
                // no modules configured
                loginState.setErrorCode(ae.getErrorCode());
                loginState.logFailed(ae.getMessage());
                auditor.auditLoginFailure(loginState);
                setErrorMsgAndTemplate();
                loginStatus.setStatus(LoginStatus.AUTH_FAILED);
                throw new AuthLoginException(ae);
            }

        } else if (indexType == IndexType.USER) {
            /*
             * Configure login for specified user
             */

            debug.message("IndexType is user");
            // if user is not active throw exception
            // else continue with login
            boolean userValid = false;
            if (!loginState.ignoreProfile()) {
                userValid = validateUser(indexName);
            } else {
                ignoreProfile = true;
            }
            if ((!userValid) && (!ignoreProfile)) {
                debug.message("User is not active");
                loginState.logFailed(bundle.getString("userInactive"), "USERINACTIVE");
                auditor.auditLoginFailure(loginState, USER_INACTIVE);
                /* The user based authentication errors should not be different
                 * for users who exist and who don't, which can lead to
                 * possibility of enumerating existing users.
                 * The AMAuthErrorCode.AUTH_LOGIN_FAILED error code is used for
                 * all user based authentication errors.
                 * Refer issue3278
                 */
                loginState.setErrorCode(AMAuthErrorCode.AUTH_LOGIN_FAILED);
                setErrorMsgAndTemplate();
                //destroySession();
                loginStatus.setStatus(LoginStatus.AUTH_FAILED);
                throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_USER_INACTIVE, null);
            } else if (ignoreProfile) {
                setAuthError(AMAuthErrorCode.AUTH_PROFILE_ERROR, "loginDenied");
                throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_PROFILE_ERROR, null);
            } else {
                return false;
            }

        } else if (indexType == IndexType.MODULE_INSTANCE) {
            /*
             * Configure login for specified authentication module
             */

            // check if module exists in the allowed modules list
            debug.message("indexType is module");
            boolean instanceExists = loginState.getDomainAuthenticators().contains(indexName);
            if (!indexName.equals(ISAuthConstants.APPLICATION_MODULE) && !instanceExists) {
                debug.message("Module denied!!");
                loginState.setErrorCode(AMAuthErrorCode.AUTH_MODULE_DENIED);
                loginState.logFailed(bundle.getString("moduleDenied"), "MODULEDENIED");
                auditor.auditLoginFailure(loginState, MODULE_DENIED);
                setErrorMsgAndTemplate();
                loginStatus.setStatus(LoginStatus.AUTH_FAILED);
                throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_MODULE_DENIED, null);
            } else {
                return false;
            }

        } else if (indexType == IndexType.ROLE) {
            /*
             * Configure login for specified role - No longer supported, throw an exception
             */

            debug.message("indexType is Role");
            if (loginState.ignoreProfile()) {
                setAuthError(AMAuthErrorCode.AUTH_TYPE_DENIED, "loginDenied");
                throw new AuthLoginException(BUNDLE_NAME, AMAuthErrorCode.AUTH_TYPE_DENIED, null);
            }
        }

        /*
         * IndexType not processed by this method
         */
        return false;
    }

    /* set sid and loginState */
    void setLoginHash() {
        try {
            this.sid = AuthUtils.getSidString(authContext);
            this.loginState = AuthUtils.getLoginState(authContext);
            if (debug.messageEnabled()) {
                debug.message("sid .. "  + sid);
                debug.message("login state is .. : " + loginState);
            }
        } catch (Exception e) {
            debug.message("executeLogin exception : ", e);
        }
    }

    void setAuthError(String errorCode, String resString) {
        loginState.setErrorCode(errorCode);
        setErrorMsgAndTemplate();
        loginState.logFailed(bundle.getString(resString));
        auditor.auditLoginFailure(loginState);
        loginStatus.setStatus(LoginStatus.AUTH_FAILED);
    }

    /**
     * Sets the failure URL and execute the post process login SPI.
     * for <code>internalAutherror</code> and if already executed
     * just skip this,
     */
    public void postProcessOnFail() {
        if (!internalAuthError && !processDone) {
            if (debug.messageEnabled()) {
                debug.message("postProcessOnFail ");
            }
            //setErrorMsgAndTemplate();
            loginState.postProcess(indexType, indexName, LoginState.PostProcessEvent.FAILURE);
            loginState.setFailureLoginURL(indexType, indexName);
            processDone = true;
        }
    }

    /**
     * Sets the success URL and execute the post process login
     * SPI. for <code>internalAutherror</code> and if already executed
     * just skip this.
     */
    public void postProcessOnSuccess() {
        if (!processDone) {
            if (debug.messageEnabled()) {
                debug.message("postProcessOnSuccess ");
            }
            loginState.postProcess(indexType, indexName, LoginState.PostProcessEvent.SUCCESS);
            processDone = true;
        }
    }

    /** This method returns a Set with is the list of
     * modules for a Authentication Configuration.
     * Only modules with control flag REQUIRED and
     * REQUISITE are returned.
     * @param moduleListSet list of configured auth module
     * @return set of configured auth module with control flag REQUIRED and
     *         REQUISITE are returned
     */
    private Set<String> getModuleFromAuthConfiguration(Set<String> moduleListSet, String orgDN) {
        Configuration config = Configuration.getConfiguration();
        if (configName == null) {
            configName = getConfigName(indexType, indexName, orgDN, loginState.getClientType());
        }
        AppConfigurationEntry[] moduleList = config.getAppConfigurationEntry(configName);
        if (debug.messageEnabled()) {
            debug.message("configName is : " + configName);
        }
        String moduleName;
        if (moduleList != null && moduleList.length > 0) {
            if (moduleList.length == 1) {
                moduleName = (String) moduleList[0].getOptions().get(ISAuthConstants.MODULE_INSTANCE_NAME);
                moduleListSet.add(moduleName);
            } else {
                for (AppConfigurationEntry moduleListEntry : moduleList) {
                    LoginModuleControlFlag controlFlag = moduleListEntry.getControlFlag();
                    moduleName = (String) moduleListEntry.getOptions().get(ISAuthConstants.MODULE_INSTANCE_NAME);
                    if (isControlFlagMatchFound(controlFlag)) {
                        moduleListSet.add(moduleName);
                    }
                }
            }
        }
        if (debug.messageEnabled()) {
            debug.message("ModuleSet is : " + moduleListSet);
        }

        return moduleListSet;
    }

    /* return the failure module list */
    String getFailureModuleList(String orgDN) {

        String moduleList = ISAuthConstants.EMPTY_STRING;
        try {
            Set<String> failureModuleSet = loginState.getFailureModuleSet();
            Set<String> moduleSet = getModuleFromAuthConfiguration(failureModuleSet, orgDN);

            if (debug.messageEnabled()) {
                debug.message("ModuleSet is : " + moduleSet);
            }
            moduleList = getModuleString(moduleSet);
        } catch (Exception e) {
            debug.message("Exception : getFailureModuleList " , e);
        }
        if (debug.messageEnabled()) {
            debug.message("moduleList is :" + moduleList);
        }
        return moduleList;
    }


    /* Checks if the control flag matches the JAAS flags,
     * REQUIRED and REQUISITE flags
     */
    boolean isControlFlagMatchFound(LoginModuleControlFlag flag) {
        return flag == LoginModuleControlFlag.REQUIRED || flag == LoginModuleControlFlag.REQUISITE;
    }

    /* Returns the successful list of modules names */
    String getSuccessModuleString(String orgDN) {
        if (moduleSet == null || moduleSet.isEmpty()) {
            moduleSet = getSuccessModuleSet(orgDN);
        }
        return getModuleString(moduleSet);
    }

    /**
     * Checks if is pure JAAS mode
     * @return <code>true</code> if pure JAAS
     */
    public boolean isPureJAAS() {
        return jaasCheck == 1;
    }

    private void nullifyUsedVars() {
        configName = null; // jaas configuration name.
        subject = null;
        clientType = null;
        moduleSet = null;
        entries = null;
        recdCallback = null;
    }
}
