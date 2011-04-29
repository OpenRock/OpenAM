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

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import java.io.File;
import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.Log;

/**
 *
 * @author steve
 */
public class EmbeddedOServer {
    private static OServer server = null;
    
    public static void startEmbedded() {
        Log.logger.log(Level.INFO, "Start embedded DB server");
        
        try {
            server = OServerMain.create();
            server.startup(new File("../config/dbconfig.xml"));
        } catch (Exception ex) {
            Log.logger.log(Level.SEVERE, "Unable to start embedded db", ex);
        }
    }
    
    public static void stopEmbedded() {
        if (server != null) {
            server.shutdown();
        }
        
        Log.logger.log(Level.INFO, "Stop embedded DB server");
    }
}
