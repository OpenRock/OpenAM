package org.forgerock.openam.authentication.modules.passphrase.security.login.module;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import javax.servlet.http.HttpServletRequest;

import org.forgerock.openam.authentication.modules.passphrase.common.utility.CommonUtilities;
import org.forgerock.openam.authentication.modules.passphrase.common.utility.PassphraseConstants;
import org.forgerock.openam.authentication.modules.passphrase.security.login.principal.PassPhrasePrincipal;
import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.shared.debug.Debug;

/**
 * This login module is used to capture the Passphrase for the first time
 * logging in user.
 */
@SuppressWarnings("unchecked")
public class PassphraseEntryModule extends AMLoginModule {
	
	private static Debug debug = Debug.getInstance("PassphraseEntryModule");

	private Map sharedState;
	private Principal userPrincipal;
	private String userTokenId;
	private String userName;
	private String passphraseEntered;
	
	public void init(Subject subject, Map sharedState, Map options) {
		this.sharedState = sharedState;
	}

	public int process(Callback callbacks[], int state) throws AuthLoginException {
		if (callbacks.length < 1)
			throw new AuthLoginException( "Fatal configuration error, wrong number of callbacks");

		userName = (String) sharedState.get("javax.security.auth.login.name");
		if (userName == null) {
			debug.error("The sharedState userId is null, hence getting user from request.");
			HttpServletRequest request = getHttpServletRequest();
			debug.message("Session token id is :" + getSSOSession().getTokenID().toString());

			debug.message("User Realm is :" + request.getParameter("realm"));
			userName = CommonUtilities.getUserName(request);
			debug.message("userName got from request is " + userName);
		}

		try {
			if (state == 1) {
				passphraseEntered = new String(((PasswordCallback) callbacks[0]).getPassword());

				Map<String, Set> mapEcriture = new HashMap<String, Set>();
				Set<String> passphraseStore = new HashSet<String>();
				passphraseStore.add(passphraseEntered);
				mapEcriture.put(CommonUtilities.getProperty(PassphraseConstants.USER_PASSPHRASE_ATTRIBUTE),passphraseStore);

				AMIdentity user = CommonUtilities.getUser(userName, getHttpServletRequest());
				user.setAttributes(mapEcriture);
				user.store();

				userTokenId = userName;
			}
		} catch (Exception ex) {
			debug.error("Error occure while storing Passphrase for first time login",ex);
			throw new AuthLoginException(ex);
		}
		return -1;
	}

	public Principal getPrincipal() {
		Principal thePrincipal = null;
		if (userPrincipal != null)
			thePrincipal = userPrincipal;
		else if (userTokenId != null) {
			userPrincipal = new PassPhrasePrincipal(userName);
			thePrincipal = userPrincipal;
			debug.message("Principal created");
		}
		return thePrincipal;
	}
}