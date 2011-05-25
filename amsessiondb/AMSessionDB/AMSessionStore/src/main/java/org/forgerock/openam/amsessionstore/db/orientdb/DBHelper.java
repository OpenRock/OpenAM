/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright Â© 2011 ForgeRock AS. All rights reserved.
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
 */
package org.forgerock.openam.amsessionstore.db.orientdb;

import java.util.ArrayList;
import java.util.List;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OProperty.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.storage.OStorage;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.handler.distributed.ODistributedServerManager;
import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.Log;

/**
 * A Helper to interact with the OrientDB
 * 
 * @author aegloff
 * @auther steve
 */
public class DBHelper {
    /**
     * Initialize the DB pool.
     * @param dbURL the orientdb URL
     * @param user the orientdb user to connect
     * @param password the orientdb password to connect
     * @param minSize the orientdb pool minimum size
     * @param maxSize the orientdb pool maximum size
     * @return the initialized pool
     */
    public static ODatabaseDocumentPool initPool(String dbURL, String user, String password, int minSize, int maxSize) {        
        createSchema(dbURL, user, password);
        
        try {
            //shareDatabase(dbURL, user, password);
        } catch (Exception ex) {
            
        }
        
        ODatabaseDocumentPool pool = new ODatabaseDocumentPool(); //ODatabaseDocumentPool.global(); // Moving from 0.9.25 to 1.0 RC had to change this, is it safe?
        pool.setup(minSize, maxSize);
        warmUpPool(pool, dbURL, user, password, minSize);
        
        Log.logger.log(Level.FINE, "DB Pool initied");
        
        return pool;
    }
    
    /**
     * Ensure the min size pool entries are initialized.
     * Cuts down on some (small) initial latency with lazy init
     * Do not call with a min past the real pool max, it will block.
     */
    private static void warmUpPool(ODatabaseDocumentPool pool, String dbURL, String user, String password, int minSize) {
        List<ODatabaseDocumentTx> list = new ArrayList<ODatabaseDocumentTx>();
        
        for (int count=0; count < minSize; count++) {
            list.add(pool.acquire(dbURL, user, password));
        }
        
        for (ODatabaseDocumentTx entry : list) {
            pool.release(entry);
        }
    }
    

    // TODO: This is temporary until we have the mechanisms in place to laod default schema and test data
    private static void createSchema(String dbURL, String user, String password) {
        ODatabaseDocumentTx db = null;
        
        try {
            db = new ODatabaseDocumentTx(dbURL).open(user, password);
            OSchema schema = db.getMetadata().getSchema();
        
            if (!schema.existsClass(DocumentUtil.CLASS_NAME)) {
                OClass session = schema.createClass(DocumentUtil.CLASS_NAME, OStorage.CLUSTER_TYPE.PHYSICAL); 
                OProperty prop = session.createProperty(DocumentUtil.ORIENTDB_PRIMARY_KEY, OType.STRING);
                prop.createIndex(INDEX_TYPE.UNIQUE);
                prop = session.createProperty(DocumentUtil.ORIENTDB_SECONDARY_KEY, OType.STRING);
                prop.createIndex(INDEX_TYPE.NOTUNIQUE);
                schema.save();      

                Log.logger.log(Level.FINE, "Created class in the schema");
            } 
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE, "Unable to load schema into database", ex);
        } finally {
            if (db != null) {
                db.close();
            }
        }

    }
    
    private static void shareDatabase(String dbURL, String user, String password) 
    throws Exception {
        ODistributedServerManager manager = OServerMain.server().getHandler(ODistributedServerManager.class);
        
        if (manager == null) {
            throw new Exception("Can't find a ODistributedServerDiscoveryManager");
        }
        
        //if (!manager.isCurrentNodeTheClusterOwner("amsessiondb", "default")) {
            ODocument servers = manager.getServersForCluster("amsessiondb", "default");
            manager.getServersForCluster("amsessiondb", "default");

            if (servers == null) {
                throw new Exception("Node is not distributed");   
            }

            String leaderId = servers.field("owner");
        //} 
        
        
    }
}