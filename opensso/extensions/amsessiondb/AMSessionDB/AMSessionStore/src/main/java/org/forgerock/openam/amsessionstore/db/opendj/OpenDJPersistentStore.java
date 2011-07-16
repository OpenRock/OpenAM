/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 ForgeRock AS. All Rights Reserved
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

package org.forgerock.openam.amsessionstore.db.opendj;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.AMRecord;
import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.common.SystemProperties;
import org.forgerock.openam.amsessionstore.db.DBStatistics;
import org.forgerock.openam.amsessionstore.db.NotFoundException;
import org.forgerock.openam.amsessionstore.db.PersistentStore;
import org.forgerock.openam.amsessionstore.db.StoreException;
import org.opends.server.core.AddOperation;
import org.opends.server.core.DeleteOperation;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.protocols.internal.InternalSearchOperation;
import org.opends.server.types.DereferencePolicy;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;
import org.opends.server.types.SearchResultEntry;
import org.opends.server.types.SearchScope;

/**
 *
 * @author steve
 */
public class OpenDJPersistentStore implements PersistentStore, Runnable {
    private boolean shutdown = false;
    private Thread storeThread;
    private int sleepInterval = 60 * 1000;
    private final static String ID = "OpenDJPersistentStore"; 
    private static String sessionDBSuffix;
    private static String odjRoot;
    private static URL existingServerUrl;
    private static InternalClientConnection icConn;
    private static Map<String, String> openDJSetupMap; 
    private final static String PKEY_FILTER_PRE = "(pKey=";
    private final static String PKEY_FILTER_POST = ")";
    private final static String SKEY_FILTER_PRE = "(sKey=";
    private final static String SKEY_FILTER_POST = ")";
    private final static String EXPDATE_FILTER_PRE = "(expirationDate<";
    private final static String EXPDATE_FILTER_POST = ")";
    private static LinkedHashSet<String> returnAttrs;
    
    private final static String DEFAULT_OPENDJ_ROOT = "../opendj"; 
    private final static String DEFAULT_SUFFIX = "dc=amsessiondb,dc=com";
    private final static String DEFAULT_OPENDJ_ADMIN_PORT = "4444";
    private final static String DEFAULT_OPENDJ_LDAP_PORT =" 60389";
    private final static String DEFAULT_OPENDJ_JMX_PORT = "2689";
    private final static String DEFAULT_OPENDJ_DS_MGR_DN = "cn=Directory Manager";
    private final static String DEFAULT_OPENDJ_DS_MGR_PASSWD = "password";
    
    static {
        initialize();
    }
    
    private static void initialize() {
        odjRoot = SystemProperties.get(Constants.OPENDJ_ROOT, DEFAULT_OPENDJ_ROOT);
        sessionDBSuffix = SystemProperties.get(Constants.OPENDJ_SUFFIX, DEFAULT_SUFFIX);
        
        openDJSetupMap = new HashMap<String, String>();
        openDJSetupMap.put(Constants.OPENDJ_ADMIN_PORT, 
                SystemProperties.get(Constants.OPENDJ_ADMIN_PORT, DEFAULT_OPENDJ_ADMIN_PORT));
        openDJSetupMap.put(Constants.OPENDJ_LDAP_PORT, 
                SystemProperties.get(Constants.OPENDJ_LDAP_PORT, DEFAULT_OPENDJ_LDAP_PORT));
        openDJSetupMap.put(Constants.OPENDJ_JMX_PORT, 
                SystemProperties.get(Constants.OPENDJ_JMX_PORT, DEFAULT_OPENDJ_JMX_PORT));
        openDJSetupMap.put(Constants.OPENDJ_DS_MGR_DN, 
                SystemProperties.get(Constants.OPENDJ_DS_MGR_DN, DEFAULT_OPENDJ_DS_MGR_DN));
        openDJSetupMap.put(Constants.OPENDJ_DS_MGR_PASSWD, 
                SystemProperties.get(Constants.OPENDJ_DS_MGR_PASSWD, DEFAULT_OPENDJ_DS_MGR_PASSWD));
        
        String url = SystemProperties.get(Constants.EXISTING_SERVER_URL);
        
        if (url != null && url.length() > 0) {
            try {
                existingServerUrl = new URL(SystemProperties.get(Constants.EXISTING_SERVER_URL));
            } catch (MalformedURLException mue) {
                System.err.println(Constants.EXISTING_SERVER_URL + " URL in amsessiondb.properties " +
                        SystemProperties.get(Constants.EXISTING_SERVER_URL) + " is invalid, exiting.");
                System.exit(Constants.EXIT_INVALID_URL);
            }
        }
        
        returnAttrs = new LinkedHashSet<String>();
        returnAttrs.add("dn");
        returnAttrs.add(AMRecordDataEntry.PRI_KEY);
        returnAttrs.add(AMRecordDataEntry.SEC_KEY);
        returnAttrs.add(AMRecordDataEntry.AUX_DATA);
        returnAttrs.add(AMRecordDataEntry.DATA);
        returnAttrs.add(AMRecordDataEntry.EXP_DATE);
        returnAttrs.add(AMRecordDataEntry.EXTRA_BYTE_ATTR);
        returnAttrs.add(AMRecordDataEntry.EXTRA_STRING_ATTR);
        returnAttrs.add(AMRecordDataEntry.OPERATION);
        returnAttrs.add(AMRecordDataEntry.SERVICE);
        returnAttrs.add(AMRecordDataEntry.STATE);
    }
    
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public OpenDJPersistentStore() 
    throws StoreException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                internalShutdown();
            }
        });
        
        storeThread = new Thread(this);        
        storeThread.setName(ID);
        storeThread.start();
        initializeOpenDJ();
        
        icConn = InternalClientConnection.getRootConnection();
        Log.logger.log(Level.FINE, "OpenDJPersistentStore created successfully.");
    }
    
    public static Map<String, String> getOpenDJSetupMap() {
        return openDJSetupMap;
    }
    
    public static String getOdjRoot() {
        return odjRoot;
    }
    
    public static String getSessionDBSuffix() {
        return sessionDBSuffix;
    }
    
    public static URL getExistingServerUrl() {
        return existingServerUrl;
    }
    
    private void initializeOpenDJ() 
    throws StoreException {
        try {
            EmbeddedOpenDJ.startServer(odjRoot);
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE, "Unable to start embedded OpenDJ server.", ex);
            throw new StoreException("Unable to start embedded OpenDJ server.", ex);
        }
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public void run() {
        while (!shutdown) {
            try {
                Thread.sleep(sleepInterval);
                long curTime = System.currentTimeMillis() / 1000;
                deleteExpired(curTime);
            } catch (InterruptedException ie) {
                Log.logger.log(Level.WARNING, "Thread interupted", ie);
            } catch (StoreException se) {
                Log.logger.log(Level.WARNING, "Store Exception", se);
            }   
        }
    }
    
    @Override
    public void write(AMRecord record) 
    throws StoreException {
        AMRecordDataEntry entry = new AMRecordDataEntry(record);
        List attrList = entry.getAttrList();
        StringBuilder dn = new StringBuilder();
        dn.append(AMRecordDataEntry.PRI_KEY).append(Constants.EQUALS).append(record.getPrimaryKey());
        dn.append(Constants.COMMA).append(Constants.BASE_DN);
        dn.append(Constants.COMMA).append(OpenDJPersistentStore.getSessionDBSuffix());
        attrList.addAll(AMRecordDataEntry.getObjectClasses());
        AddOperation ao = icConn.processAdd(dn.toString(), attrList);
        ResultCode resultCode = ao.getResultCode();
        
        if (resultCode == ResultCode.SUCCESS) {
            Log.logger.log(Level.FINE, "Successfully created" +" entry: " + dn);
        } else if (resultCode == ResultCode.ENTRY_ALREADY_EXISTS) {
            Log.logger.log(Level.WARNING, " unable to create: Entry " +
                        "Already Exists Error for DN" + dn);
        } else {
            Log.logger.log(Level.WARNING, "Error creating entry: "+
                dn + ", error code = " + resultCode);
            throw new StoreException("Unable to create entry: " + dn);
        }
    }
    
    protected void store(AMRecord record)
    throws StoreException {
            
    }
    
    @Override
    public AMRecord read(String id) 
    throws NotFoundException, StoreException {        
        try {
            StringBuilder baseDN = new StringBuilder();
            StringBuilder filter = new StringBuilder();
            filter.append(PKEY_FILTER_PRE).append(id).append(PKEY_FILTER_POST);
            baseDN.append(Constants.BASE_DN).append(Constants.COMMA).append(sessionDBSuffix);
            InternalSearchOperation iso = icConn.processSearch(baseDN.toString(),
                SearchScope.SINGLE_LEVEL, DereferencePolicy.NEVER_DEREF_ALIASES,
                0, 0, false, filter.toString() , returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList searchResult = iso.getSearchEntries();
                
                if (!searchResult.isEmpty()) {
                    SearchResultEntry entry =
                        (SearchResultEntry) searchResult.get(0);
                    List attributes = entry.getAttributes();

                    Map<String, Set<String>> results = 
                            EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(attributes);
                    AMRecordDataEntry dataEntry = new AMRecordDataEntry("pkey=" + id + "," + baseDN, results);
                    return dataEntry.getAMRecord();
                } else {
                    return null;
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                Log.logger.log(Level.FINE,"Entry not present:" + sessionDBSuffix);
                
                return null;
            } else {
                Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix + 
                       ", error code = " + resultCode);
                throw new StoreException("Unable to access entry DN" + sessionDBSuffix);
            }
        } catch (DirectoryException dex) {
            Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix, dex);
            throw new StoreException("Unable to read record from store", dex);
        }
    }
    
    @Override
    public Set<String> readWithSecKey(String id) 
    throws StoreException, NotFoundException {
        try {
            StringBuilder baseDN = new StringBuilder();
            StringBuilder filter = new StringBuilder();
            filter.append(SKEY_FILTER_PRE).append(id).append(SKEY_FILTER_POST);
            baseDN.append(Constants.BASE_DN).append(Constants.COMMA).append(sessionDBSuffix);
            InternalSearchOperation iso = icConn.processSearch(baseDN.toString(),
                SearchScope.SINGLE_LEVEL, DereferencePolicy.NEVER_DEREF_ALIASES,
                0, 0, false, filter.toString() , returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();
                
                if (!searchResult.isEmpty()) {
                    Set<String> result = new HashSet<String>();
                    
                    for (SearchResultEntry entry : searchResult) {
                        List attributes = entry.getAttributes();
                        Map<String, Set<String>> results = 
                            EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(attributes);
                        
                        Set<String> value = results.get(AMRecordDataEntry.DATA);
                        
                        if (value != null && !value.isEmpty()) {
                            for (String v : value) {
                                result.add(v);
                            }
                        }   
                    }
                    
                    return result;
                } else {
                    return null;
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                Log.logger.log(Level.FINE,"Entry not present:" + sessionDBSuffix);
                
                return null;
            } else {
                Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix + 
                       ", error code = " + resultCode);
                throw new StoreException("Unable to access entry DN" + sessionDBSuffix);
            }
        } catch (DirectoryException dex) {
            Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix, dex);
            throw new StoreException("Unable to read record from store", dex);
        }
    }
    
    @Override
    public void delete(String id)
    throws StoreException, NotFoundException {
        StringBuilder dn = new StringBuilder();
        dn.append(AMRecordDataEntry.PRI_KEY).append(Constants.EQUALS).append(id);
        dn.append(Constants.COMMA).append(Constants.BASE_DN);
        dn.append(Constants.COMMA).append(OpenDJPersistentStore.getSessionDBSuffix());
        DeleteOperation dop = icConn.processDelete(dn.toString());
        ResultCode resultCode = dop.getResultCode();
        
        if (resultCode != ResultCode.SUCCESS) {
            Log.logger.log(Level.WARNING, "Unable to delete entry:" + dn);
        }
    }
    
    @Override
    public void deleteExpired(long expDate)
    throws StoreException {
        try {
            StringBuilder baseDN = new StringBuilder();
            StringBuilder filter = new StringBuilder();
            filter.append(EXPDATE_FILTER_PRE).append(expDate).append(EXPDATE_FILTER_POST);
            baseDN.append(Constants.BASE_DN).append(Constants.COMMA).append(sessionDBSuffix);
            InternalSearchOperation iso = icConn.processSearch(baseDN.toString(),
                SearchScope.SINGLE_LEVEL, DereferencePolicy.NEVER_DEREF_ALIASES,
                0, 0, false, filter.toString() , returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();
                
                if (!searchResult.isEmpty()) {
                    for (SearchResultEntry entry : searchResult) {
                         List attributes = entry.getAttributes();

                        Map<String, Set<String>> results = 
                            EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(attributes);
                    
                        Set<String> value = results.get(AMRecordDataEntry.PRI_KEY);
                    
                        if (value != null && !value.isEmpty()) {
                            for (String v : value) {
                                try {
                                    delete(v);
                                } catch (NotFoundException nfe) {
                                    Log.logger.log(Level.WARNING, "Unable to delete " + v + " not found");
                                }
                            }
                        }
                    }
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                Log.logger.log(Level.FINE,"Entry not present:" + sessionDBSuffix);
            } else {
                Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix + 
                       ", error code = " + resultCode);
                throw new StoreException("Unable to access entry DN" + sessionDBSuffix);
            }
        } catch (DirectoryException dex) {
            Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix, dex);
            throw new StoreException("Unable to read record from store", dex);
        }        
    }
    
    @Override
    public Map<String, Long> getRecordCount(String id) 
    throws StoreException {
        try {
            StringBuilder baseDN = new StringBuilder();
            StringBuilder filter = new StringBuilder();
            filter.append(SKEY_FILTER_PRE).append(id).append(SKEY_FILTER_POST);
            baseDN.append(Constants.BASE_DN).append(Constants.COMMA).append(sessionDBSuffix);
            InternalSearchOperation iso = icConn.processSearch(baseDN.toString(),
                SearchScope.SINGLE_LEVEL, DereferencePolicy.NEVER_DEREF_ALIASES,
                0, 0, false, filter.toString() , returnAttrs);
            ResultCode resultCode = iso.getResultCode();

            if (resultCode == ResultCode.SUCCESS) {
                LinkedList<SearchResultEntry> searchResult = iso.getSearchEntries();
                
                if (!searchResult.isEmpty()) {
                    Map<String, Long> result = new HashMap<String, Long>();
                    
                    for (SearchResultEntry entry : searchResult) {
                        List attributes = entry.getAttributes();
                        Map<String, Set<String>> results = 
                            EmbeddedSearchResultIterator.convertLDAPAttributeSetToMap(attributes);
                        
                        String key = "";
                        Long expDate = new Long(0);
                        
                        Set<String> value = results.get(AMRecordDataEntry.AUX_DATA);
                        
                        if (value != null && !value.isEmpty()) {
                            for (String v : value) {
                                key = v;
                            }
                        } 
                        
                        value = results.get(AMRecordDataEntry.EXP_DATE);
                        
                        if (value != null && !value.isEmpty()) {
                            for (String v : value) {
                                expDate = Long.parseLong(v);
                            }
                        }  
                        
                        result.put(key, expDate);
                    }
                    
                    return result;
                } else {
                    return null;
                }
            } else if (resultCode == ResultCode.NO_SUCH_OBJECT) {
                Log.logger.log(Level.FINE,"Entry not present:" + sessionDBSuffix);
                
                return null;
            } else {
                Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix + 
                       ", error code = " + resultCode);
                throw new StoreException("Unable to access entry DN" + sessionDBSuffix);
            }
        } catch (DirectoryException dex) {
            Log.logger.log(Level.WARNING, "Error in accessing entry DN: " + sessionDBSuffix, dex);
            throw new StoreException("Unable to read record from store", dex);
        }
    }
    
    @Override
    public void shutdown() {
        internalShutdown();
        Log.logger.log(Level.FINE, "shutdown called");
    }
    
    @Override
    public DBStatistics getDBStatistics() {
        return null;
    }
    
    protected void internalShutdown() {
        shutdown = true;    
        Log.logger.log(Level.FINE, "Internal Shutdown called");
        
        try {
            EmbeddedOpenDJ.shutdownServer(); 
        } catch (Exception ex) {
            Log.logger.log(Level.WARNING, "OpenDJ shutdown failure", ex);
        }
    }
}
