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
* Copyright 2015 ForgeRock AS.
*/
package org.forgerock.openam.authentication.modules.saml2;

import static com.sun.identity.shared.Constants.*;
import static org.forgerock.openam.authentication.modules.saml2.Constants.*;

import com.sun.identity.authentication.AuthContext;
import com.sun.identity.authentication.client.AuthClientUtils;
import com.sun.identity.authentication.service.AuthUtils;
import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.spi.AuthenticationException;
import com.sun.identity.authentication.spi.PagePropertiesCallback;
import com.sun.identity.authentication.spi.RedirectCallback;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.sun.identity.common.DNUtils;
import com.sun.identity.saml2.assertion.Assertion;
import com.sun.identity.saml2.assertion.EncryptedID;
import com.sun.identity.saml2.assertion.NameID;
import com.sun.identity.saml2.assertion.Subject;
import com.sun.identity.saml2.common.AccountUtils;
import com.sun.identity.saml2.common.NameIDInfo;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2FailoverUtils;
import com.sun.identity.saml2.common.SAML2Utils;
import com.sun.identity.saml2.jaxb.entityconfig.SPSSOConfigElement;
import com.sun.identity.saml2.jaxb.metadata.IDPSSODescriptorElement;
import com.sun.identity.saml2.jaxb.metadata.SPSSODescriptorElement;
import com.sun.identity.saml2.key.KeyUtil;
import com.sun.identity.saml2.meta.SAML2MetaException;
import com.sun.identity.saml2.meta.SAML2MetaManager;
import com.sun.identity.saml2.plugins.DefaultLibrarySPAccountMapper;
import com.sun.identity.saml2.plugins.SAML2PluginsUtils;
import com.sun.identity.saml2.profile.AuthnRequestInfo;
import com.sun.identity.saml2.profile.AuthnRequestInfoCopy;
import com.sun.identity.saml2.profile.SPACSUtils;
import com.sun.identity.saml2.profile.SPCache;
import com.sun.identity.saml2.profile.SPSSOFederate;
import com.sun.identity.saml2.protocol.AuthnRequest;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.CookieUtils;
import com.sun.identity.shared.locale.L10NMessageImpl;
import com.sun.identity.sm.DNMapper;
import java.security.Principal;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.forgerock.openam.federation.saml2.SAML2TokenRepositoryException;
import org.forgerock.openam.saml2.SAML2Store;
import org.forgerock.openam.utils.JsonValueBuilder;
import org.forgerock.openam.utils.StringUtils;

/**
 * SAML2 Authentication Module, acting from the SP's POV. Will redirect to a SAML2 IdP for authentication, then
 * return and complete.  Supports HTTP-Redirect and HTTP-POST bindings for sending the AuthnRequests, and HTTP-POST,
 * HTTP-Artifact binding for processing SAML responses.
 */
public class SAML2 extends AMLoginModule {

    private static final Debug DEBUG = Debug.getInstance(AM_AUTH_SAML2);
    private static final String BUNDLE_NAME = "amAuthSAML2";

    //From config
    private String entityName;
    private String metaAlias;
    private String reqBinding;
    private String localChain;
    private String sloRelayState;
    private boolean singleLogoutEnabled;
    private String nameIDFormat;

    //Internal state
    private Subject assertionSubject;
    private Map<String, List<String>> params = new HashMap<>();
    private Principal principal;
    private AuthContext authenticationContext;
    private String realm;
    private int previousLength = 0;
    private ResourceBundle bundle = null;
    private String sessionIndex;

    private SAML2MetaManager metaManager;

    @Override
    public void init(javax.security.auth.Subject subject, Map sharedState, Map options) {

        for (Object key : options.keySet()) {
            String keyStr = (String) key;
            if (OPTIONS_MAP.containsKey(keyStr) && CollectionHelper.getMapAttr(options, keyStr) != null) {
                if (((String) key).equalsIgnoreCase(BINDING)) {
                    //binding needs to be just the suffix, e.g. HTTP-POST
                    String bindingTmp = CollectionHelper.getMapAttr(options, keyStr);
                    params.put(OPTIONS_MAP.get(keyStr),
                            Collections.singletonList(bindingTmp.substring(bindingTmp.lastIndexOf(":") + 1)));
                } else {
                    params.put(OPTIONS_MAP.get(keyStr),
                            Collections.singletonList(CollectionHelper.getMapAttr(options, keyStr)));
                }
            }
        }

        nameIDFormat = CollectionHelper.getMapAttr(options, NAME_ID_FORMAT);
        entityName = CollectionHelper.getMapAttr(options, ENTITY_NAME);
        metaAlias = CollectionHelper.getMapAttr(options, META_ALIAS);
        reqBinding = CollectionHelper.getMapAttr(options, REQ_BINDING);
        localChain = CollectionHelper.getMapAttr(options, LOCAL_CHAIN);
        singleLogoutEnabled = CollectionHelper.getBooleanMapAttr(options, SLO_ENABLED, false);
        sloRelayState = CollectionHelper.getMapAttr(options, SLO_RELAY_STATE);
        metaManager = SAML2Utils.getSAML2MetaManager();
        realm = DNMapper.orgNameToRealmName(getRequestOrg());

        bundle = amCache.getResBundle(BUNDLE_NAME, getLoginLocale());
        String authLevel = CollectionHelper.getMapAttr(options, AUTHLEVEL);

        if (authLevel != null) {
            try {
                setAuthLevel(Integer.parseInt(authLevel));
            } catch (Exception e) {
                DEBUG.error("SAML2 :: init() : Unable to set auth level {}", authLevel, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int process(final Callback[] realCallbacks, int state) throws LoginException {

        final HttpServletRequest request = getHttpServletRequest();
        final HttpServletResponse response = getHttpServletResponse();

        if (null == request) {
            return processError(bundle.getString("samlNullRequest"),
                    "SAML2 :: process() : Http Request is null - programmatic login is not supported.");
        }

        try {
            final String spName = metaManager.getEntityByMetaAlias(metaAlias);
            if (authenticationContext != null) {
                state = LOGIN_STEP;
            }

            switch (state) {
            case START:
                return initiateSAMLLoginAtIDP(response, request);
            case REDIRECT:
                return handleReturnFromRedirect(state, request, spName, response);
            case LOGIN_STEP:
                return stepLogin(realCallbacks, state);
            default:
                return processError(bundle.getString("invalidLoginState"), "Unrecognised login state: {}", state);
            }

        } catch (SAML2Exception e) {
            return processError(e, null, "SAML2 :: process() : Authentication Error");
        }
    }

    /**
     * Performs similar to SPSSOFederate.initiateAuthnRequest by returning to the next auth stage
     * with a redirect (either GET or POST depending on the config) which triggers remote IdP authentication.
     */
    private int initiateSAMLLoginAtIDP(final HttpServletResponse response, final HttpServletRequest request)
            throws SAML2Exception, AuthLoginException {

        if (reqBinding == null) {
            reqBinding = SAML2Constants.HTTP_REDIRECT;
        }

        final String spEntityID = SPSSOFederate.getSPEntityId(metaAlias);
        final IDPSSODescriptorElement idpsso = SPSSOFederate.getIDPSSOForAuthnReq(realm, entityName);
        final SPSSODescriptorElement spsso = SPSSOFederate.getSPSSOForAuthnReq(realm, spEntityID);

        if (idpsso == null || spsso == null) {
            return processError(bundle.getString("samlLocalConfigFailed"), "SAML2 :: initiateSAMLLoginAtIDP() : {}",
                    bundle.getString("samlLocalConfigFailed"));
        }

        final String ssoURL = SPSSOFederate.getSSOURL(idpsso.getSingleSignOnService(), reqBinding);
        final List extensionsList = SPSSOFederate.getExtensionsList(spEntityID, realm);

        final Map<String, Collection<String>> spConfigAttrsMap
                = SPSSOFederate.getAttrsMapForAuthnReq(realm, spEntityID);
        final AuthnRequest authnRequest = SPSSOFederate.createAuthnRequest(realm, spEntityID, params,
                spConfigAttrsMap, extensionsList, spsso, idpsso, ssoURL, false);
        final AuthnRequestInfo reqInfo = new AuthnRequestInfo(request, response, realm, spEntityID, null,
                authnRequest, null, params);

        synchronized (SPCache.requestHash) {
            SPCache.requestHash.put(authnRequest.getID(), reqInfo);
        }

        saveAuthnRequestIfFailoverEnabled(authnRequest, reqInfo);

        final Callback[] nextCallbacks = getCallback(REDIRECT);
        final RedirectCallback redirectCallback = (RedirectCallback) nextCallbacks[0];

        setCookiesForRedirects(request, response);

        //we only handle Redirect and POST
        if (SAML2Constants.HTTP_POST.equals(reqBinding)) {
            final String postMsg = SPSSOFederate.getPostBindingMsg(idpsso, spsso, spConfigAttrsMap, authnRequest);
            configurePostRedirectCallback(postMsg, ssoURL, redirectCallback);
        } else {
            final String authReqXMLString = authnRequest.toXMLString(true, true);
            final String redirectUrl = SPSSOFederate.getRedirect(authReqXMLString, null, ssoURL, idpsso,
                    spsso, spConfigAttrsMap);
            configureGetRedirectCallback(redirectUrl, redirectCallback);
        }
        return REDIRECT;
    }

    private void saveAuthnRequestIfFailoverEnabled(final AuthnRequest authnRequest, final AuthnRequestInfo reqInfo)
            throws SAML2Exception {

        final long sessionExpireTimeInSeconds
                = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + SPCache.interval;
        final String key = authnRequest.getID();

        if (SAML2FailoverUtils.isSAML2FailoverEnabled()) {
            try {
                SAML2FailoverUtils.saveSAML2TokenWithoutSecondaryKey(key, new AuthnRequestInfoCopy(reqInfo),
                        sessionExpireTimeInSeconds);
                DEBUG.message("SAML2.saveAuthnRequestIfFailoverEnabled : "
                        + "SAVE AuthnRequestInfoCopy for requestID {}", key);
            } catch (SAML2TokenRepositoryException e) {
                DEBUG.error("SAML2.saveAuthnRequestIfFailoverEnabled : There was a problem saving the "
                       + "AuthnRequestInfoCopy in the SAML2 Token Repository for requestID {}", key, e);
                throw new SAML2Exception(BUNDLE_NAME, "samlFailover", null);
            }
        } else {
            SAML2Store.saveTokenWithKey(key, new AuthnRequestInfoCopy(reqInfo));

            DEBUG.message("SAML2.saveAuthnRequestIfFailoverDisabled : "
                    + "SAVE AuthnRequestInfoCopy for requestID {}", key);
        }
    }

    /**
     * Once we're back from the ACS, we need to validate that we have not errored during the proxying process.
     * Then we detect if we need to perform a local linking authentication chain, or if the user is already
     * locally linked, we need to look up the already-linked username.
     */
    private int handleReturnFromRedirect(final int state, final HttpServletRequest request, final String spName,
                                         final HttpServletResponse response)  throws AuthLoginException {

        //first make sure to delete the cookie
        removeCookiesForRedirects(response);

        if (Boolean.parseBoolean(request.getParameter(SAML2Proxy.ERROR_PARAM_KEY))) {
            return handleRedirectError(request);
        }

        final String key;
        if (request.getParameter("jsonContent") != null) {
            key = JsonValueBuilder.toJsonValue(request.getParameter("jsonContent")).get("responsekey").asString();
        } else {
            key = request.getParameter(SAML2Proxy.RESPONSE_KEY);
        }

        final String username;
        final Assertion authnAssertion;
        final SAML2ResponseData data;

        if (SAML2FailoverUtils.isSAML2FailoverEnabled() && !StringUtils.isBlank(key)) {
            try {
                data = (SAML2ResponseData) SAML2FailoverUtils.retrieveSAML2Token(key);
            } catch (SAML2TokenRepositoryException e) {
                return processError(bundle.getString("samlFailoverError"),
                        "SAML2.handleReturnFromRedirect : Error reading from failover map.", e);
            }
        } else if (!StringUtils.isBlank(key)) {
            data = (SAML2ResponseData) SAML2Store.getTokenFromStore(key);
        } else {
            return processError(bundle.getString("localLinkError"), "SAML2 :: handleReturnFromRedirect() : "
                    + "Unable to perform local linking - response data key not found");
        }

        assertionSubject = data.getSubject();
        authnAssertion = data.getAssertion();
        sessionIndex = data.getSessionIndex();

        try { //you're already linked or we auto looked up user
            username = SPACSUtils.getPrincipalWithoutLogin(assertionSubject, authnAssertion,
                    realm, spName, metaManager, entityName);
            if (username != null) {
                principal = new SAML2Principal(username);
                return success(getNameId());
            }
        } catch (SAML2Exception e) {
            return processError(e, null, "SAML2.handleReturnFromRedirect : Unable to perform user lookup.");
        }

        if (StringUtils.isBlank(localChain)) {
            return processError(bundle.getString("localLinkError"), "SAML2 :: handleReturnFromRedirect() : "
                    + "Unable to perform local linking - local auth chain not found.");
        }

        //generate a sub-login context, owned by this module, and start login sequence to it
        authenticationContext = new AuthContext(realm);
        authenticationContext.login(AuthContext.IndexType.SERVICE, localChain, null, null, null, null);

        return injectCallbacks(null, state);
    }

    /**
     * Grab error code/message and display to user via processError.
     */
    private int handleRedirectError(HttpServletRequest request) throws AuthLoginException {
        final String errorCode = request.getParameter(SAML2Proxy.ERROR_CODE_PARAM_KEY);
        final String errorMessage = request.getParameter(SAML2Proxy.ERROR_MESSAGE_PARAM_KEY);

        if (StringUtils.isNotEmpty(errorMessage)) {
            return processError(errorMessage, "SAML2 :: handleReturnFromRedirect() : "
                    + "error forwarded from saml2AuthAssertionConsumer.jsp.  Error code - {}. "
                    + "Error message - {}", String.valueOf(errorCode), String.valueOf(errorMessage));
        } else if (StringUtils.isNotEmpty(errorCode)) {
            return processError(bundle.getString(errorCode), "SAML2 :: handleReturnFromRedirect() : "
                    + "error forwarded from saml2AuthAssertionConsumer.jsp.  Error code - {}. "
                    + "Error message - {}", errorCode, errorMessage);
        } else {
            return processError(bundle.getString("samlVerify"), "SAML2 :: handleReturnFromRedirect() : "
                    + "error forwarded from saml2AuthAssertionConsumer.jsp.  Error code - {}. "
                    + "Error message - {}", errorMessage);
        }
    }

    /**
     * Generates the redirect from SAML2 auth module to IDP as POST.
     */
    private void configurePostRedirectCallback(final String postMsg, final String ssoURL,
                                               final RedirectCallback redirectCallback) throws AuthLoginException {
        final Map<String, String> postData = new HashMap<>();
        postData.put(SAML2Constants.SAML_REQUEST, postMsg);

        final RedirectCallback rcNew = new RedirectCallback(ssoURL, postData, "POST",
                redirectCallback.getStatusParameter(), redirectCallback.getRedirectBackUrlCookieName());
        replaceCallback(REDIRECT, REDIRECT_CALLBACK, rcNew);
    }

    /**
     * Generates the redirect from SAML2 auth module to IDP as GET.
     */
    private void configureGetRedirectCallback(final String redirectUrl, RedirectCallback redirectCallback)
            throws AuthLoginException {
        final RedirectCallback rcNew = new RedirectCallback(redirectUrl, null, "GET",
                redirectCallback.getStatusParameter(), redirectCallback.getRedirectBackUrlCookieName());

        Map<String, String> redirectData = rcNew.getRedirectData();
        if (null == redirectData) {
            redirectData = new HashMap<>();
        }

        rcNew.setRedirectData(redirectData);
        replaceCallback(REDIRECT, REDIRECT_CALLBACK, rcNew);
    }

    /**
     * "Inspired" by the OAuth2 module. We use this cookie to remind us exactly where we are when
     * returning from a remote server as we currently cannot trust the RedirectCallback's authentication
     * framework equiv.
     */
    private void setCookiesForRedirects(final HttpServletRequest request, final HttpServletResponse response) {
        final Set<String> domains = AuthClientUtils.getCookieDomains();
        final String authCookieName = AuthUtils.getAuthCookieName();

        String requestedQuery = request.getQueryString();
        String requestedURI = request.getRequestURI();

        if (requestedQuery != null) {
            if (requestedQuery.endsWith(authCookieName + "=")) {
                requestedQuery = requestedQuery.substring(0, requestedQuery.length() - authCookieName.length() - 1);
            }
            requestedURI += "?" + requestedQuery;
        }

        // forces us back to XUI, not directly to endpoint
        if (requestedURI.contains("/json/authenticate")) {
            requestedURI = requestedURI.replace("/json/authenticate", "");
        }

        // Set the return URL Cookie
        for (String domain : domains) {
            CookieUtils.addCookieToResponse(response,
                    CookieUtils.newCookie(Constants.AM_LOCATION_COOKIE, requestedURI, "/", domain));
        }
    }

    /**
     * Clears out the cookie from the user agent so we don't leave detritus.
     */
    private void removeCookiesForRedirects(final HttpServletResponse response) {
        final Set<String> domains = AuthClientUtils.getCookieDomains();

        // Set the return URL Cookie
        for (String domain : domains) {
            CookieUtils.addCookieToResponse(response,
                    CookieUtils.newCookie(Constants.AM_LOCATION_COOKIE, "", 0, "/", domain));
        }
    }

    /**
     * In conjuncture with injectCallbacks, steps through an internal auth chain (stored in authenticationContext) until
     * it's completed by repeatedly injecting the callbacks from the internal chain's modules and submitting
     * them until the status has confirmed failed or succeeded.
     */
    private int stepLogin(final Callback[] realCallbacks, final int state) throws AuthLoginException {

        if (authenticationContext == null || authenticationContext.getStatus().equals(AuthContext.Status.FAILED)) {
            return processError(bundle.getString("samlLocalAuthFailed"),
                    "SAML2 :: process() : failed to perform local authentication - {} ",
                    bundle.getString("samlLocalAuthFailed"));
        } else if (authenticationContext.getStatus().equals(AuthContext.Status.IN_PROGRESS)) {
            return injectCallbacks(realCallbacks, state);
        } else if (authenticationContext.getStatus().equals(AuthContext.Status.SUCCESS)) {
            try {
                final NameID nameId = getNameId();
                linkAccount(authenticationContext.getSSOToken().getProperty(UNIVERSAL_IDENTIFIER), nameId);
                return success(nameId);
            } catch (L10NMessageImpl l10NMessage) {
                return processError(l10NMessage, null,
                        "SAML2 :: process() : failed to perform local authentication - {} ",
                        l10NMessage.getL10NMessage(getLoginLocale()));
            } finally {
                authenticationContext.logout();
            }
        }

        return processError(bundle.getString("invalidLoginState"), "SAML2 :: stepLogin() : unexpected login state");
    }

    /**
     * Sets the auth module's logged-in username via storeUsernamePasswd, triggers call
     * to add information necessary for SLO (if configured) and returns success.
     */
    private int success(NameID nameId) throws AuthLoginException, SAML2Exception {
        setSessionPropertiesForSLO(nameId);
        DEBUG.message("SAML2 :: User Authenticated via SAML2 - {}", getPrincipal().getName());
        storeUsernamePasswd(DNUtils.DNtoName(getPrincipal().getName()), null);
        return ISAuthConstants.LOGIN_SUCCEED;
    }

    /**
     * Submits completed callbacks (from the just-completed step - the first time this is called realCallbacks should
     * be null as there is no just-completed step in the internal auth module), and injects the next lot if there
     * are any.
     */
    private int injectCallbacks(final Callback[] realCallbacks, final int state) throws AuthLoginException {

        if (authenticationContext.hasMoreRequirements()) {
            //replace existing callbacks
            if (realCallbacks != null) {
                authenticationContext.submitRequirements(realCallbacks);
            }

            if (authenticationContext.hasMoreRequirements()) {
                return injectAndReturn();
            } else { //completed auth, status should be failure or success, allow stepLogin to return
                if (authenticationContext.getStatus().equals(AuthContext.Status.IN_PROGRESS)) {
                    return processError(bundle.getString("invalidLoginState"),
                            "SAML2 :: injectCallbacks() : Authentication Module - invalid login state");
                }
                return stepLogin(null, state);
            }
        }

        return processError(bundle.getString("invalidLoginState"),
                "SAML2 :: injectCallbacks() : Authentication Module - invalid login state");
    }

    /**
     * Draws the next set of callbacks on to the current (externally-facing) auth module's step.
     */
    private int injectAndReturn() throws AuthLoginException {

        replaceHeader(LOGIN_STEP,
                ((PagePropertiesCallback)
                        authenticationContext.getAuthContextLocal().getLoginState().getReceivedInfo()[0]).getHeader());
        final Callback[] injectedCallbacks = authenticationContext.getRequirements();
        if (injectedCallbacks.length > MAX_CALLBACKS_INJECTED) {
            return processError(bundle.getString("samlLocalAuthFailed"),
                    "SAML2 :: injectAndReturn() : Local authentication failed");
        }

        if (previousLength > 0) { //reset
            for (int i = 0; i < previousLength; i++) {
                replaceCallback(LOGIN_STEP, i, DEFAULT_CALLBACK);
            }
        }

        for (int i = 0; i < injectedCallbacks.length; i++) {
            replaceCallback(LOGIN_STEP, i, injectedCallbacks[i]);
        }

        previousLength = injectedCallbacks.length;

        return LOGIN_STEP;
    }

    /**
     * Reads the authenticating user's SAML2 NameId from the stored map. Decrypts if necessary.
     */
    private NameID getNameId() throws SAML2Exception, AuthLoginException {
        final String spName = metaManager.getEntityByMetaAlias(metaAlias);
        final EncryptedID encId = assertionSubject.getEncryptedID();
        final SPSSOConfigElement spssoconfig = metaManager.getSPSSOConfig(realm, spName);
        final Set<PrivateKey> decryptionKeys = KeyUtil.getDecryptionKeys(spssoconfig);

        NameID nameId = assertionSubject.getNameID();

        if (encId != null) {
            nameId = encId.decrypt(decryptionKeys);
        }
        return nameId;
    }

    /**
     * Adds information necessary for the SLO to be able to be run into the user's session.
     */
    private void setSessionPropertiesForSLO(NameID nameId) throws AuthLoginException, SAML2Exception {
        setUserSessionProperty(SAML2Constants.SINGLE_LOGOUT, String.valueOf(singleLogoutEnabled));
        if (singleLogoutEnabled) {
            setUserSessionProperty(SAML2Constants.SESSION_INDEX, sessionIndex);
            setUserSessionProperty(SAML2Constants.IDPENTITYID, entityName);
            setUserSessionProperty(SAML2Constants.RELAY_STATE, sloRelayState);
            setUserSessionProperty(SAML2Constants.SPENTITYID, SPSSOFederate.getSPEntityId(metaAlias));
            setUserSessionProperty(SAML2Constants.METAALIAS, metaAlias);
            setUserSessionProperty(SAML2Constants.REQ_BINDING, reqBinding);
            setUserSessionProperty(SAML2Constants.NAMEID, nameId.toXMLString(true, true));
        }
    }

    /**
     * Links SAML2 accounts once all local auth steps have completed and we have a local principalId,
     * sets the local principal to a new SAML2Pricipal with that ID.
     */
    private void linkAccount(final String principalId, final NameID nameId)
            throws SAML2MetaException, AuthenticationException {

        final String spName = metaManager.getEntityByMetaAlias(metaAlias);

        try {
            NameIDInfo info = new NameIDInfo(spName, entityName, nameId, SAML2Constants.SP_ROLE, false);
            DEBUG.message("SAML2 :: Local User {} Linked to Federation Account - {}", principalId, nameId.getValue());

            final DefaultLibrarySPAccountMapper spAccountMapper = new DefaultLibrarySPAccountMapper();
            final String spEntityID = SPSSOFederate.getSPEntityId(metaAlias);
            final IDPSSODescriptorElement idpsso = SPSSOFederate.getIDPSSOForAuthnReq(realm, entityName);
            final SPSSODescriptorElement spsso = SPSSOFederate.getSPSSOForAuthnReq(realm, spEntityID);

            nameIDFormat = SAML2Utils.verifyNameIDFormat(nameIDFormat, spsso, idpsso);
            boolean isTransient = SAML2Constants.NAMEID_TRANSIENT_FORMAT.equals(nameIDFormat);
            boolean isPersistent = SAML2Constants.PERSISTENT.equals(nameIDFormat);
            boolean ignoreProfile = SAML2PluginsUtils.isIgnoredProfile(realm);

            boolean shouldPersistNameID = isPersistent || (!isTransient && !ignoreProfile
                    && spAccountMapper.shouldPersistNameIDFormat(realm, spName, entityName, nameIDFormat));

            if (shouldPersistNameID) {
                AccountUtils.setAccountFederation(info, principalId);
            }

            principal = new SAML2Principal(principalId);
        } catch (SAML2Exception e) {
            // exception logged later
            throw new AuthenticationException(BUNDLE_NAME, "localLinkError", new Object[0]);
        }
    }

    /**
     * Writes out an error debug (if a throwable and debug message are provided) and returns a user-facing
     * error page.
     */
    private int processError(String headerMessage, String debugMessage,
                             Object... messageParameters) throws AuthLoginException {
        if (null != debugMessage) {
            DEBUG.error(debugMessage, messageParameters);
        }
        substituteHeader(STATE_ERROR, headerMessage);
        return STATE_ERROR;
    }

    /**
     * Writes out an error debug (if a throwable and debug message are provided) and returns a user-facing
     * error page.
     */
    private int processError(L10NMessageImpl e, String headerMessageCode,
                             String debugMessage, Object... messageParameters) throws AuthLoginException {

        if (null == e) {
            return processError(headerMessageCode, debugMessage, messageParameters);
        }
        String headerMessage;
        if (null == headerMessageCode) {
            headerMessage = e.getL10NMessage(getLoginLocale());
        } else {
            headerMessage = bundle.getString(headerMessageCode);
        }
        if (debugMessage != null) {
            DEBUG.error(debugMessage, messageParameters, e);
        }
        substituteHeader(STATE_ERROR, headerMessage);
        return STATE_ERROR;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }
}