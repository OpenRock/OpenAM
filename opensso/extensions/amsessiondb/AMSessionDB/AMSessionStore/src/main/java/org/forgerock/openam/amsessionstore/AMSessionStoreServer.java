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
package org.forgerock.openam.amsessionstore;

import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.common.SystemProperties;
import org.forgerock.openam.amsessionstore.db.PersistentStoreManager;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 *
 * @author steve
 */

public class AMSessionStoreServer {
    private final static String DEFAULT_PORT = "8182";
    private final static String DEFAULT_URI = "/amsessiondb";
    
    public static void main( String[] args ) {
        // Create a new Component.  
        Component component = new Component();  
  
        String listenPort = SystemProperties.get(Constants.PORT, DEFAULT_PORT);
        int port = 8182;
        
        if (listenPort != null) {
            port = Integer.parseInt(listenPort);
        }
        
        String uri = SystemProperties.get(Constants.URI, DEFAULT_URI);
        
        // Add a new HTTP server listening.  
        component.getServers().add(Protocol.HTTP, port);  
  
        // Attach the sample application.  
        component.getDefaultHost().attach(uri, new AmSessionDbApplication());  
  
        // Start the component.  
        try {
            component.start(); 
            PersistentStoreManager.getInstance().getPersistentStore();
        } catch (Exception ex) {
            Log.logger.log(Level.WARNING, "Unable to start amsessiondb", ex);
        }
        
        Log.logger.log(Level.FINE, "amsessiondb started on port {0}", listenPort);
    }
}
