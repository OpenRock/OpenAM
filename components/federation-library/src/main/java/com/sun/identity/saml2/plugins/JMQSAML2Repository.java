package com.sun.identity.saml2.plugins;

import com.iplanet.dpro.session.exceptions.StoreException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: allan
 * Date: 2/2/13
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */


import com.iplanet.dpro.session.exceptions.StoreException;

import java.util.List;


public interface JMQSAML2Repository {

    static final String SYS_PROPERTY_TOKEN_SAML2_REPOSITORY_ROOT_SUFFIX =
            "iplanet-am-token-saml2-root-suffix";

    /**
     * Retrives existing SAML2 object from persistent Repository.
     *
     * @param samlKey primary key
     * @return Object - SAML2 unMarshaled Object, if failed, return null.
     */
    public Object retrieveSAML2Token(String samlKey) throws StoreException;

    /**
     * Retrieves a list of existing SAML2 object from persistent Repository with the Secondary Key.
     *
     * @param secKey Secondary Key
     * @return List<Object> - List of SAML2 unMarshaled Objects, if failed, return null.
     */
    public List<Object> retrieveSAML2TokenWithSecondaryKey(String secKey) throws StoreException;

    /**
     * Deletes the SAML2 object by given primary key from the repository
     * @param samlKey primary key
     */
    public void deleteSAML2Token(String samlKey) throws StoreException;

    /**
     * Deletes expired SAML2 object from the repository
     */
    public void deleteExpiredSAML2Tokens() throws StoreException;

    /**
     * Saves SAML2 data into the SAML2 Repository
     * @param samlKey primary key
     * @param samlObj saml object such as Response, IDPSession
     * @param expirationTime expiration time
     * @param secKey Secondary Key
     */
    public void saveSAML2Token(String samlKey, Object samlObj, long expirationTime,
                               String secKey) throws StoreException;
}
