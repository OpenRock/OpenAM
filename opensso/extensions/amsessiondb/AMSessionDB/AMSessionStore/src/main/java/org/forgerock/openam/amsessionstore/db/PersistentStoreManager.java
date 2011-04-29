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

package org.forgerock.openam.amsessionstore.db;

import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.common.SystemProperties;

/**
 *
 * @author steve
 */
public class PersistentStoreManager {
    private static PersistentStore persistentStore = null; 
    private static PersistentStoreManager instance = null;
    private static String persistentStoreImpl = null; 
    private static final String DEFAULT_PERSISTER_VALUE = 
        "org.forgerock.openam.amsessionstore.db.memory.MemoryPersistentStore";
    

    static {
        try {
            initialize();
        } catch (Exception e) {
            persistentStoreImpl = DEFAULT_PERSISTER_VALUE;
        }         
    }
    
    private static void initialize() 
    throws Exception {
        persistentStoreImpl = SystemProperties.get(Constants.PERSISTER_KEY,
                DEFAULT_PERSISTER_VALUE);
        Log.logger.log(Level.FINE, "Initialised Persistent Store: {0}", persistentStoreImpl);
    }

    private PersistentStoreManager() throws Exception {
        persistentStore = (PersistentStore) Class.forName(
            persistentStoreImpl).newInstance();        
    } 
    
    public synchronized static PersistentStoreManager getInstance() 
    throws Exception {
        if (instance == null) {
            try {
                instance = new PersistentStoreManager();
                Log.logger.log(Level.FINE, "Created PersistentStoreManager instance");
            } catch (Exception ex) {
                Log.logger.log(Level.SEVERE, "Unable to create PersistentStoreManager", ex);
                throw ex;
            }
        }
        
        return instance; 
    }
   
    public static PersistentStore getPersistentStore() {
        return persistentStore; 
    }

}
