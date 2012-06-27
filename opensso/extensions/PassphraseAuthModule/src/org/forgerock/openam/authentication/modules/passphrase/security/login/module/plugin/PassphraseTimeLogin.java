package org.forgerock.openam.authentication.modules.passphrase.security.login.module.plugin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.iplanet.sso.SSOToken;
import com.sun.identity.authentication.spi.AMPostAuthProcessInterface;
import com.sun.identity.authentication.spi.AuthenticationException;
import com.sun.identity.shared.debug.Debug;

@SuppressWarnings("unchecked")
public class PassphraseTimeLogin implements AMPostAuthProcessInterface {

	private static Debug debug = Debug.getInstance("FirstTimeLogin");

	/**
	 * Post processing on successful authentication.
	 * 
	 * @param requestParamsMap contains HttpServletRequest parameters
	 * @param request HttpServlet request
	 * @param response HttpServlet response
	 * @param ssoToken user's session
	 * @throws AuthenticationException  if there is an error while setting the session paswword property
	 */
	public void onLoginSuccess(Map requestParamsMap, HttpServletRequest request, HttpServletResponse response, SSOToken ssoToken)
			throws AuthenticationException {
		if (debug.messageEnabled()) {
			debug.message("FirstTimeLogin.onLoginSuccess called: Req:" + request.getRequestURL());
		}

		request.setAttribute(AMPostAuthProcessInterface.POST_PROCESS_LOGIN_SUCCESS_URL, "/opensso/console");
		System.out.println("Redirecting to POST Success URL /opensso/console");

		if (debug.messageEnabled()) {
			debug.message("FirstTimeLogin.onLoginSuccess: FirstTimeLogin concluded successfully");
		}
	}

	/**
	 * Post processing on failed authentication.
	 * 
	 * @param requestParamsMap contains HttpServletRequest parameters
	 * @param req HttpServlet request
	 * @param res HttpServlet response
	 * @throws AuthenticationException if there is an error
	 */
	public void onLoginFailure(Map requestParamsMap, HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
		debug.message("FirstTimeLogin.onLoginFailure: called");
	}

	/**
	 * Post processing on Logout.
	 * 
	 * @param req HttpServlet request
	 * @param res HttpServlet response
	 * @param ssoToken user's session
	 * @throws AuthenticationException if there is an error
	 */
	public void onLogout(HttpServletRequest req, HttpServletResponse res, SSOToken ssoToken) throws AuthenticationException {
		debug.message("FirstTimeLogin.onLogout called");
	}
}