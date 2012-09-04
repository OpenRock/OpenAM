/*
 * Copyright (c) 2012 ForgeRock AS. All rights reserved.
 *
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
 * information: "Portions Copyrighted [2012] [ForgeRock Inc]".
 *
 */
package org.forgerock.openam.ext.cts.repo;

import com.sun.identity.common.GeneralTaskRunnable;
import com.sun.identity.common.SystemTimer;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.fluent.JsonValueException;
import org.forgerock.json.resource.JsonResource;
import org.forgerock.json.resource.JsonResourceException;
import org.forgerock.json.resource.SimpleJsonResource;
import org.forgerock.openam.ext.cts.model.TokenDataEntry;
import org.forgerock.openam.oauth2.OAuth2;
import org.forgerock.openam.session.ha.amsessionstore.db.opendj.EmbeddedSearchResultIterator;
import org.opends.server.core.AddOperation;
import org.opends.server.core.DeleteOperation;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.protocols.internal.InternalSearchOperation;
import org.opends.server.types.*;

import java.util.*;

public class OpenDJTokenRepo extends GeneralTaskRunnable implements JsonResource {


    final static Debug debug = Debug.getInstance("CTS");

    private static boolean isDatabaseUp = true;

    // Time period between two successive runs of repository cleanup thread which checks and removes expired records
    private static long CLEANUPPERIOD = 5 * 60 * 1000; // 5 min in milliseconds
    private static long CLEANUPVALUE = 0;

    // TODO rename
    public static final String CLEANUP_RUN_PERIOD = "org.forgerock.ext.cts.repo.cleanupRunPeriod";

    // Time period between two successive runs of DBHealthChecker thread which checks for Database availability
    private static long HEALTHCHECKPERIOD = 1 * 60 * 1000;
    // TODO rename
    public static final String HEALTH_CHECK_RUN_PERIOD = "org.forgerock.ext.cts.repo.healthCheckRunPeriod";

    // This period is actual one that is used by the thread, smallest value of cleanUpPeriod and healthCheckPeriod
    private static long RUNPERIOD = 1 * 60 * 1000; // 1 min in milliseconds

    private String CTS = "CTS";

    /**
     * Internal LDAP Connection.
     */
    private static InternalClientConnection icConn;
    private Thread storeThread;

    //TODO make configurable
    private static final String BASE_DN = "ou=oauth2tokens";
    private static final String ROOT_DN = "dc=internal,dc=opensso,dc=java,dc=net";

    //TODO Store this somewhere
    static final String TOKEN_FILTER = "(objectclass=*)";
    static final String EXPDATE_FILTER_PRE = "(expirytime<=";
    static final String EXPDATE_FILTER_POST = ")";

    private static LinkedHashSet<String> returnAttrs;

    static {

        RUNPERIOD = (CLEANUPPERIOD <= HEALTHCHECKPERIOD) ? CLEANUPPERIOD : HEALTHCHECKPERIOD;
        CLEANUPVALUE = CLEANUPPERIOD;
    }

    /**
     * Create the JMQ connection based on settings in system properties, and start the timer for cleanup operations.
     *
     * @throws Exception
     */
    public OpenDJTokenRepo() throws Exception {

        try {
            icConn = InternalClientConnection.getRootConnection();
            // TODO -- Fix Make ths Dynamic.
            InternalSearchOperation results =
                    icConn.processSearch("dc=internal,dc=openam,dc=java,dc=net", SearchScope.BASE_OBJECT, "*");
            debug.message("Search for base container yielded Result Code: " + results.getResultCode().toString() + "]");
            isDatabaseUp = true;
        } catch (DirectoryException directoryException) {
            debug.warning("Unable to obtain the Internal Root Container for Token Persistence!",
                    directoryException);
            // TODO -- Abort further setup.
        }


        // Start our AM Repository Store Thread.
        //storeThread = new Thread(this);
        //storeThread.setName(CTS);
        //storeThread.start();

        SystemTimer.getTimer().schedule(this, new Date((System.currentTimeMillis() / 1000) * 1000));

    }

    /**
     * Creates a token in the OpenDJ instance
     * @param request a JsonValue created from a Token
     * @return returns the created token
     * @throws JsonResourceException
     */
    protected JsonValue create(JsonValue request) throws JsonResourceException {
        if (!isDatabaseUp) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE);
        }

        TokenDataEntry token = new TokenDataEntry(request);
        List<RawAttribute> attrList = token.getAttrList();
        attrList.addAll(token.getObjectClasses());

        StringBuilder dn = new StringBuilder();
        dn.append(OAuth2.Params.ID).append(Constants.EQUALS).append(token.getDN());
        dn.append(Constants.COMMA).append(BASE_DN);
        dn.append(Constants.COMMA).append(ROOT_DN);
        AddOperation ao = icConn.processAdd(dn.toString(), attrList);
        ResultCode resultCode = ao.getResultCode();

        if (resultCode == ResultCode.SUCCESS) {
            //TODO LOG created
            request.get("value").put(OAuth2.Params.ID, token.getDN());
            return new JsonValue(request.get("values"));
        } else if (resultCode == ResultCode.ENTRY_ALREADY_EXISTS) {
            throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
        } else {
            throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
        }
    }

    /**
     * Read a token from the OpenDJ store given a request that contains an id of the token.
     * @param request a JsonValue containing an id value to retrieve
     * @return A JsonValue containing the returned token Map<String, Set<String>>
     * @throws JsonResourceException
     */
    protected JsonValue read(JsonValue request) throws JsonResourceException {
        if (!isDatabaseUp) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE);
        }
        String dn = request.get("id").required().asString();

        StringBuilder baseDN = new StringBuilder();
        try {
            baseDN.append(OAuth2.Params.ID).append(Constants.EQUALS);
            baseDN.append(dn).append(Constants.COMMA).append(BASE_DN);
            baseDN.append(Constants.COMMA).append(ROOT_DN);
            InternalSearchOperation iso = icConn.processSearch(baseDN.toString(),
                    SearchScope.BASE_OBJECT, DereferencePolicy.NEVER_DEREF_ALIASES,
                    0, 0, false, TOKEN_FILTER, returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList searchResult = iso.getSearchEntries();

                if (!searchResult.isEmpty()) {
                    SearchResultEntry entry =
                            (SearchResultEntry) searchResult.get(0);
                    List<Attribute> attributes = entry.getAttributes();

                    Map<String, Set<String>> results =
                            EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(attributes);

                    addUnderScoresToParams(results);

                    return new JsonValue(results);
                } else {
                    throw new JsonResourceException(JsonResourceException.NOT_FOUND, "Object not found with id: " + dn);
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
            } else {
                throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
            }
        } catch (DirectoryException dex) {
            throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR, dex);
        }
    }

    /**
     *  Calls create with the request.
     * @param request a request containing a token to create
     * @return returns the created token
     * @throws JsonResourceException
     */
    protected JsonValue update(JsonValue request) throws JsonResourceException {
        // Update is run in the same way as a create
        return create(request);
    }

    /**
     * Deletes a token
     * @param request a JsonValue containing an id value of a token to delete
     * @return returns a null JsonValue
     * @throws JsonResourceException
     */
    protected JsonValue delete(JsonValue request) throws JsonResourceException {
        if (!isDatabaseUp) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE);
        }
        String id = request.get("id").required().asString();
        StringBuilder dn = new StringBuilder();
        dn.append(OAuth2.Params.ID).append(Constants.EQUALS).append(id);
        dn.append(Constants.COMMA).append(BASE_DN);
        dn.append(Constants.COMMA).append(ROOT_DN);
        DeleteOperation dop = icConn.processDelete(dn.toString());
        ResultCode resultCode = dop.getResultCode();

        if (resultCode != ResultCode.SUCCESS) {
            //TODO LOG FAILED
            throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
        }

        return new JsonValue(null);

    }

    protected JsonValue patch(JsonValue request) throws JsonResourceException {
        if (!isDatabaseUp) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE);
        }
        //TODO
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }


    protected JsonValue action(JsonValue request) throws JsonResourceException {
        if (!isDatabaseUp) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE);
        }
        //TODO
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Queries the OpenDJ store given a set of filters
     * @param request request contains a filter value which has a valid LDAP filter command
     * @return returns a Set of tokens that match the filter.
     * @throws JsonResourceException
     */
    protected JsonValue query(JsonValue request) throws JsonResourceException {
        if (!isDatabaseUp) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE);
        }

        String filter = request.get("filter").required().asString();
        Set<Map<String, Set<String>>> tokens = new HashSet<Map<String, Set<String>>>();
        try {
            StringBuilder baseDN = new StringBuilder();
            baseDN.append(BASE_DN).append(Constants.COMMA).append(ROOT_DN);
            InternalSearchOperation iso = icConn.processSearch(baseDN.toString(),
                    SearchScope.SINGLE_LEVEL, DereferencePolicy.NEVER_DEREF_ALIASES,
                    0, 0, false, filter, returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();

                if (!searchResult.isEmpty()) {
                    for (SearchResultEntry entry : searchResult) {
                        List<Attribute> attributes = entry.getAttributes();

                        Map<String, Set<String>> results =
                                EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(attributes);
                        addUnderScoresToParams(results);
                        tokens.add(results);

                    }
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
            } else {
                throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
            }
        } catch (DirectoryException dex) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE, dex);
        } catch (Exception ex) {
            throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR, ex);
        }

        return new JsonValue(tokens);
    }

    /**
     * Triggers deletion of all expired resources in the store.
     *
     * @throws JsonResourceException
     */

    protected void deleteExpired() throws JsonResourceException {
        if (!isDatabaseUp) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE);
        }
        try {
            StringBuilder baseDN = new StringBuilder();
            StringBuilder filter = new StringBuilder();
            filter.append(EXPDATE_FILTER_PRE).append(System.currentTimeMillis()).append(EXPDATE_FILTER_POST);
            baseDN.append(BASE_DN).append(Constants.COMMA).append(ROOT_DN);
            InternalSearchOperation iso = icConn.processSearch(baseDN.toString(),
                    SearchScope.SINGLE_LEVEL, DereferencePolicy.NEVER_DEREF_ALIASES,
                    0, 0, false, filter.toString(), returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();

                if (!searchResult.isEmpty()) {
                    for (SearchResultEntry entry : searchResult) {
                        List<Attribute> attributes = entry.getAttributes();

                        Map<String, Set<String>> results =
                                EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(attributes);

                        Set<String> value = results.get(OAuth2.Params.ID);

                        if (value != null && !value.isEmpty()) {
                            for (String v : value) {
                                    delete(v);
                            }
                        }
                    }
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
            } else {
                throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
            }
        } catch (DirectoryException dex) {
            throw new JsonResourceException(JsonResourceException.UNAVAILABLE, dex);
        } catch (Exception ex) {
            throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR, ex);
        }
    }

    private void delete(String id) throws JsonResourceException {
        StringBuilder dn = new StringBuilder();
        dn.append(OAuth2.Params.ID).append(Constants.EQUALS).append(id);
        dn.append(Constants.COMMA).append(BASE_DN);
        dn.append(Constants.COMMA).append(ROOT_DN);
        DeleteOperation dop = icConn.processDelete(dn.toString());
        ResultCode resultCode = dop.getResultCode();

        if (resultCode != ResultCode.SUCCESS) {
            throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR);
        }
    }

    /**
     * Handles a JSON resource request by dispatching to the method corresponding with the
     * method member of the request. If the request method is not one of the standard JSON
     * resource request methods, a {@code JsonResourceException} is thrown.
     * <p/>
     * This method catches any thrown {@code JsonValueException}, and rethrows it as a
     * {@link JsonResourceException#BAD_REQUEST}. This allows the use of JsonValue methods
     * to validate the content of the request.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    @Override
    public JsonValue handle(JsonValue request) throws JsonResourceException {
        try {
            try {
                switch (request.get("method").required().asEnum(SimpleJsonResource.Method.class)) {
                    case create:
                        return create(request);
                    case read:
                        return read(request);
                    case update:
                        return update(request);
                    case delete:
                        return delete(request);
                    case patch:
                        return patch(request);
                    case query:
                        return query(request);
                    case action:
                        return action(request);
                    default:
                        throw new JsonResourceException(JsonResourceException.BAD_REQUEST);
                }
            } catch (JsonValueException jve) {
                throw new JsonResourceException(JsonResourceException.BAD_REQUEST, jve);
            }
        } catch (Exception e1) {
            try {
                //onException(e1); // give handler opportunity to throw its own exception
                throw e1;
            } catch (Exception e2) {
                if (e2 instanceof JsonResourceException) { // no rethrowing necessary
                    throw (JsonResourceException) e2;
                } else { // need to rethrow as resource exception
                    throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR, e2);
                }
            }
        }
    }


    // Overrides from GeneralTaskRunnable
    @Override
    public boolean addElement(Object o) {
        throw new IllegalStateException("Not supported");
    }

    @Override
    public boolean removeElement(Object o) {
        throw new IllegalStateException("Not supported");
    }

    @Override
    public boolean isEmpty() {
        throw new IllegalStateException("Not supported");
    }

    @Override
    public long getRunPeriod() {
        return RUNPERIOD;
    }

    @Override
    public void run() {
        String classMethod = "OpenDJTokenRepo.run: ";
        try {

            if (debug.messageEnabled()) {
                debug.message(classMethod + "Cleaning expired tokens");
            }

            // TODO synchronize??
            /*
             * Clean up is done based on the cleanUpPeriod even though the
             * thread runs based on the runPeriod.
             */
            if (CLEANUPVALUE <= 0) {
                deleteExpired();
                CLEANUPVALUE = CLEANUPPERIOD;
            }
            CLEANUPVALUE = CLEANUPVALUE - RUNPERIOD;

            /*
             * HealthChecking is done based on the runPeriod but only when
             * the Database is down.
             */
            if (!isDatabaseUp) {
                //TODO fail
                //logDBStatus();
            }
        } catch (Exception e) {
            debug.error("OpenDJTokenRepo: Exception during run.", e);
        }
    }

    private void addUnderScoresToParams(Map<String, Set<String>> results){
        //need to add the _ back into expiry_time, client_id, redirect_uri
        //and remove objectClass(by product of LDAP)
        for (String key : results.keySet()){
            if (key.equalsIgnoreCase("expirytime")){
                results.put(OAuth2.StoredToken.EXPIRY_TIME, results.remove(key));
            } else if (key.equalsIgnoreCase("clientid")){
                results.put(OAuth2.Params.CLIENT_ID, results.remove(key));
            } else if (key.equalsIgnoreCase("redirecturi")){
                results.put(OAuth2.Params.REDIRECT_URI, results.remove(key));
            } else if (key.equalsIgnoreCase("objectClass")){
                results.remove(key);
            }
        }
    }
}
