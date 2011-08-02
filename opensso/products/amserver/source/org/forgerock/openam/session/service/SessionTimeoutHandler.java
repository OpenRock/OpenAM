package org.forgerock.openam.session.service;

import com.iplanet.sso.SSOToken;

/**
 * Implementation of this class gets executed every time when an SSO Session
 * times out (either idle or max timeout). A new instance of the timeout handler
 * is created upon session timeout. The listed methods are called just before
 * the session gets removed, so it is safe to use the passed in {@link SSOToken}
 * instances. Because of this behavior it is encouraged that implementations
 * don't run lengthy operations.
 *
 * @author Peter Major
 */
public interface SessionTimeoutHandler {

    /**
     * Executed on idle timeout
     *
     * @param token The {@link SSOToken} instance for the timed out session
     */
    public void onIdleTimeout(SSOToken token);

    /**
     * Executed on max timeout
     *
     * @param token The {@link SSOToken} instance for the timed out session
     */
    public void onMaxTimeout(SSOToken token);
}
