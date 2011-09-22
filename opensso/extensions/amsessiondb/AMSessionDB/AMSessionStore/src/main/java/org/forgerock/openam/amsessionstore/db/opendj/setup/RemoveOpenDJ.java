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

package org.forgerock.openam.amsessionstore.db.opendj.setup;

import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.db.opendj.EmbeddedOpenDJ;
import org.forgerock.openam.amsessionstore.db.opendj.OpenDJConfig;

/**
 *
 * @author steve
 */
public class RemoveOpenDJ {
    public static void main(String[] argv) {
        if (EmbeddedOpenDJ.isInstalled()) {
            System.out.println("amsessiondb is configured on this node\n");
            System.out.println("removing this node from the amsessiondb\n");
            
            if (!EmbeddedOpenDJ.isStarted()) {
                try {
                    EmbeddedOpenDJ.startServer(OpenDJConfig.getOdjRoot());
                } catch (Exception ex) {
                    System.err.println("Unable to start embedded OpenDJ server: " + ex.getMessage());
                    System.exit(Constants.EXIT_REMOVE_FAILED);
                }
            }
            
            try {
                EmbeddedOpenDJ.unregisterServer(OpenDJConfig.getHostUrl());
                EmbeddedOpenDJ.replicationDisable(OpenDJConfig.getOpenDJSetupMap());
                EmbeddedOpenDJ.shutdownServer();
            } catch (Exception ex) {
                System.err.println("Unable to setup amsessiondb: " + ex.getMessage());
                System.exit(Constants.EXIT_REMOVE_FAILED);
            }
        } else {
            System.out.println("amsessiondb is not configured on this host \n");
            System.out.println("removal of this node is not possible.\n");
        }
    }    
}
