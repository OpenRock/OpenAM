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

package org.forgerock.openam.amsessionstore.db.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.AMRecord;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.db.DBStatistics;
import org.forgerock.openam.amsessionstore.db.NotFoundException;
import org.forgerock.openam.amsessionstore.db.PersistentStore;
import org.forgerock.openam.amsessionstore.db.StoreException;

/**
 * Demonstration implementation of the PersistentStore interface
 * 
 * @author steve
 */
public class MemoryPersistentStore implements PersistentStore, Runnable {
    private Map<String, AMRecord> store = null;
    private volatile boolean shutdown = false;
    private Thread storeThread;
    private int sleepInterval = 60 * 1000;
    private final static String ID = "MemoryPersistentStore";
    
    public MemoryPersistentStore() {
        store = new HashMap<String, AMRecord>();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                internalShutdown();
            }
        });
        
        storeThread = new Thread(this);        
        storeThread.setName(ID);
        storeThread.start();
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        while (!shutdown) {
            try {
                Thread.sleep(sleepInterval);
                long curTime = System.currentTimeMillis() / 1000;
                deleteExpired(curTime);
            } catch (InterruptedException ie) {
                Log.logger.log(Level.WARNING, "Thread interrupted", ie);
            } catch (StoreException se) {
                Log.logger.log(Level.WARNING, "Store Exception", se);
            } 
            
        }
    }
    
    public void write(AMRecord record) 
    throws StoreException {
        store.put(record.getPrimaryKey(), record);
    }

    public AMRecord read(String id) 
    throws StoreException, NotFoundException {
        return store.get(id);
    }

    public Set<String> readWithSecKey(String id) 
    throws StoreException, NotFoundException {
        Set<String> records = new HashSet<String>();
        
        for (Map.Entry<String, AMRecord> entry : store.entrySet()) {
            if (entry.getValue().getSecondaryKey().equals(id)) {
                String data = entry.getValue().getData();
                
                if (data != null) {
                    records.add(data);
                }
            }
        }
        
        return records;
    }
    
    /*public Set<AMRecord> readWithSecKey(String id) {
        Set<AMRecord> records = new HashSet<AMRecord>();
        
        for (Map.Entry<String, AMRecord> entry : store.entrySet()) {
            if (entry.getValue().getSecondaryKey().equals(id)) {
                records.add(entry.getValue());
            }
        }
        
        return records;
    }*/

    public void delete(String id)
    throws StoreException, NotFoundException {
        Object removed = store.remove(id);
        
        if (removed == null) {
            throw new NotFoundException("Session was not found: " + id);
        }
    }

    public void deleteExpired(long expDate) 
    throws StoreException {
        for (Map.Entry<String, AMRecord> entry : store.entrySet()) {
            if (entry.getValue().getExpDate() <= expDate) {
                store.remove(entry.getKey());
            }
        }
    }

    public void shutdown() {
        internalShutdown();
    }

    public Map<String, Long> getRecordCount(String id) 
    throws StoreException {
        Map<String, Long> sessions = new HashMap<String, Long>();
        
        for (Map.Entry<String, AMRecord> entry : store.entrySet()) {
            if (entry.getValue().getSecondaryKey().equals(id)) {
                sessions.put(entry.getValue().getAuxdata(), 
                        Long.valueOf(entry.getValue().getExpDate()));
            }
        }
        
        return sessions;
    }
    
    public DBStatistics getDBStatistics() {
        DBStatistics stats = DBStatistics.getInstance();
        stats.setNumRecords(store.size());
        
        return stats;
    }
    
    protected void internalShutdown() {
        shutdown = true;
    }

}
