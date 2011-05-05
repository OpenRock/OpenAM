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

package org.forgerock.openam.amsessionstore.db.orientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.index.OIndexException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 * The OrientDB implementation of the PersistentStore interface.
 * 
 * @author steve
 */
public class OrientDBPersistentStore implements PersistentStore, Runnable {
    private boolean shutdown = false;
    private Thread storeThread;
    private int sleepInterval = 60 * 1000;
    private final static String ID = "OrientDBPersistentStore";
    private ODatabaseDocumentPool pool;
    private PredefinedQueries predefinedQueries = new PredefinedQueries();

    private static String dbURL; 
    private static String user;
    private static String password;
    private static int poolMinSize; 
    private static int poolMaxSize;
    
    private final static String DEFAULT_DB_URL = "local:../db/amsessiondb"; 
    private final static String DEFAULT_USER = "admin";
    private final static String DEFAULT_PASSWORD = "admin";
    private final static String DEFAULT_POOL_MIN_SIZE = "5"; 
    private final static String DEFAULT_POOL_MAX_SIZE = "20";
    
    static {
        initialize();
    }
    
    private static void initialize() {
        dbURL = SystemProperties.get(Constants.DB_URL, DEFAULT_DB_URL);
        user = SystemProperties.get(Constants.DB_ADMIN, DEFAULT_USER);
        password = SystemProperties.get(Constants.DB_PASSWORD, DEFAULT_PASSWORD);
        String minSize = SystemProperties.get(Constants.DB_POOL_MIN, 
                DEFAULT_POOL_MIN_SIZE);
        poolMinSize = Integer.parseInt(minSize);
        String maxSize = SystemProperties.get(Constants.DB_POOL_MAX, 
                DEFAULT_POOL_MAX_SIZE);
        poolMaxSize = Integer.parseInt(maxSize);
    }
    
    public OrientDBPersistentStore() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                internalShutdown();
            }
        });
        
        storeThread = new Thread(this);        
        storeThread.setName(ID);
        storeThread.start();
        initializeDB();
        
        Log.logger.log(Level.FINE, "OrientDBPersistentStore created successfully.");
    }
    
    private void initializeDB() {
        EmbeddedOServer.startEmbedded();
        
        try {
            pool = DBHelper.initPool(dbURL, user, password, poolMinSize, poolMaxSize);
        } catch (RuntimeException ex) {
            throw ex;
        }
    }
    
    @SuppressWarnings("SleepWhileInLoop")
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
        
    public void write(AMRecord record) 
    throws StoreException {
        String pKey = record.getPrimaryKey();
 
        if (pKey == null) {
            throw new StoreException("Cannot write without a primary key " + pKey);
        }
        
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        ODocument existingDoc = null;
        
        try {
            existingDoc = predefinedQueries.getByID(pKey, db);

            if (existingDoc == null) {
                Log.logger.log(Level.FINE, "Record does not exist in db: {0}", pKey);
            }   
            
            if (existingDoc == null) {
                store(record);
            } else {
                update(existingDoc, record);
            }
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        Log.logger.log(Level.FINE, "Write record id: {0}", pKey);
    }
     
    protected void store(AMRecord record)
    throws StoreException {
        String pKey = record.getPrimaryKey();
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        ODocument recordToWrite = null;
        
        try {
            recordToWrite = DocumentUtil.toDocument(record, db);
            recordToWrite.save();
        } catch (OIndexException oix) {
            // duplicate inserts will fail            
            Log.logger.log(Level.WARNING, "Create rejected as Object with same ID already exists. {0}", oix.getMessage());
            throw new StoreException("Create rejected as Object with same ID already exists.");
        } catch (ODatabaseException odx) {
            // duplicate inserts will fail. 
            // OrientDB may wrap the IndexException root cause.
            if (isCauseIndexException(odx, 10)) {
                Log.logger.log(Level.WARNING, "Create rejected as Object with same ID already exists. {0}", odx.getMessage());
                throw new StoreException("Create rejected as Object with same ID already exists.");                   
            } else {
                throw new StoreException("Unable to write record", odx);
            }
        } catch (RuntimeException re){
            throw re;
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        Log.logger.log(Level.FINE, "Stored record id: {0}", pKey);
    }
    
    protected void update(ODocument existingRecord, AMRecord record) 
    throws StoreException {
        String pKey = record.getPrimaryKey();
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        
        try {
            existingRecord.field(DocumentUtil.ORIENTDB_DATA, record.getData());
            existingRecord.save();
        } catch (OIndexException oix) {
            // duplicate inserts will fail            
            Log.logger.log(Level.WARNING, "Update rejected as Object with same ID already exists. {0}", oix.getMessage());
            throw new StoreException("Update rejected as Object with same ID already exists.");
        } catch (ODatabaseException odx) {
            // duplicate inserts will fail. 
            // OrientDB may wrap the IndexException root cause.
            if (isCauseIndexException(odx, 10)) {
                Log.logger.log(Level.WARNING, "Update rejected as Object with same ID already exists. {0}", odx.getMessage());
                throw new StoreException("Update rejected as Object with same ID already exists.");                   
            } else {
                throw new StoreException("Unable to update record", odx);
            }
        } catch (RuntimeException re){
            throw re;
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        Log.logger.log(Level.FINE, "Update record id: {0}", pKey);        
    }
    
    public AMRecord read(String id) 
    throws NotFoundException, StoreException {        
        if (id == null) {
            throw new StoreException("Cannot write without a primary key: " + id);
        }
        
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        AMRecord result = null;
        
        try {
            ODocument doc = predefinedQueries.getByID(id, db);

            if (doc == null) {
                throw new NotFoundException("Object " + id + " not found");
            }    
            
            result = DocumentUtil.toAMRecord(doc);
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        Log.logger.log(Level.FINE, "Read record id: {0}", id);

        return result;
    }
    
    public Set<String> readWithSecKey(String id) 
    throws StoreException, NotFoundException {
        if (id == null) {
            throw new NotFoundException("Cannot write without a primary key: " + id);
        }
        
        Set<String> records = new HashSet<String>();
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        List<ODocument> docs = null;
        
        try {
            docs = predefinedQueries.getBySecID(id, db);     
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        for (ODocument doc : docs) {
            AMRecord record = DocumentUtil.toAMRecord(doc);
            String data = record.getData();
                
            if (data != null) {
                records.add(data);
            }
        }
        
        Log.logger.log(Level.FINE, "Read sec key: size: {0}", records.size());
        
        return records;
    }
    
    public void delete(String id)
    throws StoreException, NotFoundException {
        if (id == null) {
            throw new NotFoundException("Cannot delete without a primary key: " + id);
        }
                
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);

        try {
            ODocument existingDoc = predefinedQueries.getByID(id, db);

            if (existingDoc == null) {
                throw new NotFoundException("Object does not exist for delete on: " + id);
            }
            
            db.delete(existingDoc); 
        } catch (OConcurrentModificationException oex) {  
            throw new StoreException("Delete rejected as the object has changed since retrieval.", oex);
        } catch (RuntimeException re){
            throw re;
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        Log.logger.log(Level.FINE, "Delete by id: {0}", id);
    }
    
    public void deleteExpired(long expDate)
    throws StoreException {
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        List<ODocument> docs = null;
        
        try {
            docs = predefinedQueries.getAll(db);     
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        int deletedCount = 0;
        
        for (ODocument doc : docs) {
            AMRecord record = DocumentUtil.toAMRecord(doc);
            
            if (record.getExpDate() <= expDate) {
                try {
                    deletedCount++;
                    delete(record.getPrimaryKey());
                } catch (NotFoundException nfe) {
                    Log.logger.log(Level.WARNING, nfe.getMessage());
                }
            }
        }  

        if (Log.logger.isLoggable(Level.FINE)) {
            String[] objs = {new Date(expDate * 1000).toString(), Integer.toString(deletedCount)};
            Log.logger.log(Level.FINE, "Delete By Exp Date: {0} deleted {1} records", objs);
        }
    }
    
    public void shutdown() {
        internalShutdown();
        Log.logger.log(Level.FINE, "shutdown called");
    }
    
    public Map<String, Long> getRecordCount(String id) 
    throws StoreException {
        if (id == null) {
            throw new StoreException("Cannot write without a primary key: " + id);
        }
        
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        List<ODocument> docs = null;
        
        try {
            docs = predefinedQueries.getBySecID(id, db);     
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        Map<String, Long> sessions = new HashMap<String, Long>();
        
        for (ODocument doc : docs) {
            AMRecord record = DocumentUtil.toAMRecord(doc);
            String data = record.getData();
            sessions.put(record.getAuxdata(), Long.valueOf(record.getExpDate()));
        }
        
        Log.logger.log(Level.FINE, "Get Record Count: {0}", sessions.size());
        
        return sessions;
    }
    
    public DBStatistics getDBStatistics() {
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        List<ODocument> docs = null;
        
        try {
            docs = predefinedQueries.getAll(db);     
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        DBStatistics stats = DBStatistics.getInstance();
        stats.setNumRecords(docs.size());
        return stats;
    }
    
    public Set<String> getDBOverview() {
        ODatabaseDocumentTx db = pool.acquire(dbURL, user, password);
        List<ODocument> docs = null;
        
        try {
            docs = predefinedQueries.getAll(db);     
        } finally {
            if (db != null) {
                db.close();
                pool.release(db);
            }
        }
        
        Set<String> results = new HashSet<String>();
        
        for (ODocument doc : docs) {
            AMRecord record = DocumentUtil.toAMRecord(doc);
            StringBuilder buffer = new StringBuilder();
            buffer.append(record.getPrimaryKey()).append(" : ").append(record.getAuxdata());
            results.add(buffer.toString());
        } 

        return results;
    }
    
    protected void internalShutdown() {
        shutdown = true;
        //Thread.dumpStack();
        
        /*if (pool != null) {
            try {
                pool.close();
            } catch (Exception ex) {
                Log.logger.log(Level.WARNING, "Unable to close db during shutdown", ex);
            }
        }*/
        
        Log.logger.log(Level.FINE, "Internal Shutdown called");
        EmbeddedOServer.stopEmbedded();
    }
    
    /**
     * Detect if the root cause of the exception is an index constraint violation
     * This is necessary as the database may wrap this root cause in further exceptions,
     * masking the underlying cause
     * 
     * @param ex The throwable to check
     * @param maxLevels the maximum level of causes to check, avoiding the cost
     * of checking recursiveness
     */
    private boolean isCauseIndexException(Throwable ex, int maxLevels) {
        if (maxLevels > 0) {
            Throwable cause = ex.getCause();
            
            if (cause != null) { 
                if (cause instanceof OIndexException) {
                    return true;
                } else {
                    return isCauseIndexException(cause, maxLevels - 1);
                }
            }
        }    
        return false;
    }
}
