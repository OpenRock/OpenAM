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

package org.forgerock.openam.amsessionrepository.client;

import com.iplanet.dpro.session.service.SessionService;
import com.sun.identity.ha.FAMRecord;
import com.sun.identity.ha.FAMRecordPersister;
import com.sun.identity.ha.FAMRecordUtils;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.encode.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.forgerock.openam.amsessionstore.common.AMRecord;
import org.forgerock.openam.amsessionstore.resources.GetRecordCountResource;
import org.forgerock.openam.amsessionstore.resources.ReadResource;
import org.forgerock.openam.amsessionstore.resources.ReadWithSecKeyResource;
import org.restlet.resource.ClientResource;

/**
 * This is an implementation of the FAMRecordPersister interface. It uses REST
 * calls to send/receive messages to the AMSessionStore server. 
 *
 */
public class AMSessionDBRecordPersister implements FAMRecordPersister {
    private String resourceURL = null;
    private String userName = null;
    private String password = null;
    private int readTimeOut = 5000;
    
    private final static String BLOBS = "blobs";
    private final static Debug debug = FAMRecordUtils.debug;
    
    private ExecutorService threadPool = null;
            
    
    public AMSessionDBRecordPersister() {
        resourceURL = SessionService.getJdbcURL();
        userName =   SessionService.getSessionStoreUserName();
        password =   SessionService.getSessionStorePassword();
        readTimeOut = SessionService.getConnectionMaxWaitTime();
        
        threadPool = Executors.newCachedThreadPool();
        
        if (debug.messageEnabled()) {
            debug.message("AMSessionDBRecordPersister created: URL: " +
                    resourceURL + " : username: " + userName + " : password: " +
                    password + " readTimeOut: " + readTimeOut);
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }
    
    /**
     * 
     * @param famRecord The record to persist
     * @return Some operations return their results in a FAMRecord
     * @throws Exception If something goes wrong
     */
    public FAMRecord send(FAMRecord famRecord)
    throws Exception {
        String op =  famRecord.getOperation();

        // Process the operation
        if (op.equals(FAMRecord.DELETE)) {
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: " + FAMRecord.DELETE);
            }
            
            String recordToDelete = famRecord.getPrimaryKey();
            
            if (recordToDelete == null) {
                debug.error("Unable to delete without a primary key");
                throw new Exception("Unable to delete without a primary key");
            }
            
            Runnable deleteTask = new DeleteTask(resourceURL, recordToDelete);
            threadPool.execute(deleteTask);
            
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: DeleteTasks queued");
            }
        } else if (op.equals(FAMRecord.DELETEBYDATE)) {
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: " + FAMRecord.DELETEBYDATE);
            }

            long expTime = famRecord.getExpDate(); 

            if (expTime < 0) {
                throw new IllegalArgumentException("Invalid expiration time" + expTime);
            }

            Runnable deleteByDateTask = new DeleteByDateTask(resourceURL, expTime);
            threadPool.execute(deleteByDateTask);
            
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: DeleteByDateTasks queued");
            }
        } else if (op.equals(FAMRecord.WRITE)) {
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: " + FAMRecord.WRITE);
            }
            
            AMRecord record = new AMRecord();

            record.setOperation(op);
            record.setService(famRecord.getService());

            // Write Primary key   
            String pKey = famRecord.getPrimaryKey(); 

            if (pKey == null || pKey.length() <= 0 || pKey.length() > 256) {
                debug.error("Primary key length is not valid: " + pKey);
                return null;
            }

            if (pKey != null && !pKey.isEmpty()) {
                record.setPrimaryKey(pKey);
            }

            //Write expiration date 
            long expirationTime = famRecord.getExpDate(); 
            if (expirationTime > 0) {
                record.setExpDate(expirationTime);
            }

            // Write Secondary Key such as UUID
            String sKey = famRecord.getSecondarykey(); 

            if (sKey != null && !sKey.isEmpty()) {
                record.setSecondaryKey(sKey);
            } 

            // Write AuxData such as Master ID 
            String auxData = famRecord.getAuxData();
            if (auxData != null && !auxData.isEmpty()) {
                record.setAuxdata(auxData);
            }

            int state = famRecord.getState(); 
            if (state > 0) {
                record.setState(state);
            }

            byte[] blob = famRecord.getBlob(); 
            if (blob != null) {
                String data = Base64.encode(blob);
                record.setData(data);
            }
               
            // Write extra bytes 
            Map<String, byte[]> extraByteAttrs = famRecord.getExtraByteAttributes();

            if (extraByteAttrs != null) {
               for (Map.Entry<String, byte[]> entry : extraByteAttrs.entrySet()) {
                   String data = Base64.encode(entry.getValue());
                   record.setExtraByteAttrs(entry.getKey(), data);
               }
            }

            // Write extra String 
            record.setExtraStringAttrs(famRecord.getExtraStringAttributes());
            
            Runnable writeTask = new WriteTask(resourceURL, record);
            threadPool.execute(writeTask);
            
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: WriteTask queued");
            }
        } else if (op.equals(FAMRecord.SHUTDOWN)) {
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: " + FAMRecord.SHUTDOWN);
            }
            
            Runnable shutdownTask = new ShutdownTask(resourceURL);
            threadPool.execute(shutdownTask);
            
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: ShutdownTask queued");
            }
        } else if (op.equals(FAMRecord.READ)) {
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: " + FAMRecord.READ);
            }
            
            String recordToRead = famRecord.getPrimaryKey();
            
            if (recordToRead == null) {
                debug.error("Unable to delete without a primary key");
                throw new IllegalArgumentException("Unable to delete without a primary key");
            }
            
            ClientResource resource = new ClientResource(resourceURL + ReadResource.URI);
            ReadResource readResource = resource.wrap(ReadResource.class);

            AMRecord record = readResource.read(recordToRead);
            record.setOperation(FAMRecord.READ);
            
            if (debug.messageEnabled()) {
                debug.message("Message read: " + record);
            }
            
            return toFAMRecord(record);
        } else if (op.equals(FAMRecord.GET_RECORD_COUNT)) {
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: " + FAMRecord.GET_RECORD_COUNT);
            }
            
            String pKey = famRecord.getPrimaryKey();
            String sKey = famRecord.getSecondarykey();
            ClientResource resource = new ClientResource(resourceURL + GetRecordCountResource.URI);
            GetRecordCountResource getRecordCountResource = resource.wrap(GetRecordCountResource.class);

            Map<String, Long> sessions = getRecordCountResource.getRecordCount(sKey);
            AMRecord record = new AMRecord();
            record.setOperation(FAMRecord.GET_RECORD_COUNT);
            record.setPrimaryKey(pKey);
            
            Map<String, String> newMap = new HashMap<String, String>();
            
            for (Map.Entry<String, Long> entry : sessions.entrySet()) {
                newMap.put(entry.getKey(), entry.getValue().toString());
            }
            
            record.setExtraStringAttrs(newMap);
            
            if (debug.messageEnabled()) {
                if (sessions != null) {
                    debug.message("Get Record Count for " + sKey + " size " + sessions.size());
                } else {
                    debug.message("Get Record Count for " + sKey + " no results");
                }
            }
            
            return toFAMRecord(record);
        } else if (op.equals(FAMRecord.READ_WITH_SEC_KEY)) {
            if (debug.messageEnabled()) {
                debug.message("AMSessionDBRecordPersister: " + FAMRecord.READ_WITH_SEC_KEY);
            }
            
            String pKey = famRecord.getPrimaryKey();
            String sKey = famRecord.getSecondarykey();
            ClientResource resource = new ClientResource(resourceURL + ReadWithSecKeyResource.URI);
            ReadWithSecKeyResource secReadResource = resource.wrap(ReadWithSecKeyResource.class);

            Set<String> records = secReadResource.readWithSecKey(sKey);
            AMRecord record = new AMRecord();
            record.setOperation(FAMRecord.READ_WITH_SEC_KEY);
            record.setPrimaryKey(pKey);

            @SuppressWarnings("UseOfObsoleteCollectionType")
            Vector<String> blobs = new Vector<String>();
            
            for (String session : records) {
                blobs.add(session);
            }
            
            if (debug.messageEnabled()) {
                debug.message("read with sec key: " + sKey + " found: " + blobs);
            }
            
            return toFAMRecord(record, blobs);
        } 
        
        return null;
    }

    /**
     * No implementation required with this implementation of FAMRecordPersister
     * 
     * @throws Exception 
     */
    public void close()
    throws Exception {
        // no implementation required
    }
    
    /**
     * Called by the JVM shutdown hook, shuts down the thread pool
     */
    protected void shutdown() {
        threadPool.shutdown();
    }
    
    //Cumbersome code, read TODO on AMRecord
    /**
     * For a set of operations turns an AMRecord into a FAMRecord
     * 
     * @param record The AMRecord to convert
     * @param blobs The Vector of sessions
     * @return The FAMRecord object
     * @throws Exception If the incoming record is invalid
     */
    protected FAMRecord toFAMRecord(AMRecord record)
    throws Exception {
        return toFAMRecord(record, null);
    }
    
    /**
     * For a set of operations turns an AMRecord into a FAMRecord
     * 
     * @param record The AMRecord to convert
     * @param blobs The Vector of sessions
     * @return The FAMRecord object
     * @throws Exception If the incoming record is invalid
     */
    @SuppressWarnings("UseOfObsoleteCollectionType")
    protected FAMRecord toFAMRecord(AMRecord record, Vector sessions)
    throws Exception {
        FAMRecord result = null;
        
        String service = record.getService();
        
        if (service == null || service.isEmpty()) {
            throw new Exception("Service cannot be null");
        }
        
        String operation = record.getOperation();
        
        if (operation == null || operation.isEmpty()) {
            throw new Exception("Operation cannot be null");
        }
        
        String pKey = record.getPrimaryKey();

        if (pKey == null || pKey.isEmpty()) {
            throw new Exception("Primary key cannot be null");
        }
       
        if (operation.equals(FAMRecord.READ)) {
            String data = record.getData();
            byte[] blob = null;
            
            if (data != null) {
                blob = Base64.decode(data);
            } else {
                debug.error("Data is null during READ");
            }
            
            result = new FAMRecord(service, operation, pKey, 0, null, 0, null, blob);
        } else if (operation.equals(FAMRecord.GET_RECORD_COUNT)) {
            result = new FAMRecord(service, operation, pKey, 0, null, 0, null, null);
            result.setStringAttrs(new HashMap(record.getExtraStringAttributes()));
        } else if (operation.equals(FAMRecord.READ_WITH_SEC_KEY)) {
            if (sessions == null) {
                throw new Exception("blobs cannot be null");
            }
            result = new FAMRecord(service, operation, pKey, 0, null, 0, null, null);
            HashMap<String, Vector<String>> blobs = new HashMap<String, Vector<String>>();
            blobs.put(BLOBS, sessions);
            result.setStringAttrs(blobs);
        } else {
            throw new Exception("Unsupported operation " + record.getOperation());
        }
        
        return result;
    }
}
