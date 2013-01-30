package org.forgerock.openam.oauth2.model;


import com.sun.identity.shared.OAuth2Constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface CoreToken {

    /**
     * Get the string representation of the identifier of this token
     * <p/>
     *
     *
     * @return unique identifier of the represented token
     */
    public String getTokenID();

    /**
     * Get tokens UserID
     *
     * @return
     *          ID of user
     */
    public String getUserID();

    /**
     * Get Tokens Realm
     *
     * @return
     *          the realm
     */
    public String getRealm();

    /**
     * Get the exact expiration time in POSIX format.
     *
     * @return long representation of the maximum valid date.
     */
    public long getExpireTime();

    /**
     * Checks if token is expired
     *
     * @return
     *          true if expired
     *          false if not expired
     */
    public boolean isExpired();

    /**
     * Converts the token to Map
     *
     * @return new Map representation of this AccessToken
     */
    public Map<String, Object> convertToMap();

    /**
     * Method to create a token given its data
     *
     * @return
     */
    public void create(Map<String, Set<String>> tokenData);
}
