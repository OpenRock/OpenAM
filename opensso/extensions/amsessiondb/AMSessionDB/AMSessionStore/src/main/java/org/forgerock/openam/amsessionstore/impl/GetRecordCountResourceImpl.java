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

package org.forgerock.openam.amsessionstore.impl;

import java.util.Map;
import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.db.PersistentStoreFactory;
import org.forgerock.openam.amsessionstore.resources.GetRecordCountResource;
import org.forgerock.openam.amsessionstore.shared.Statistics;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Implements the get record count functionality
 * 
 * @author steve
 */
public class GetRecordCountResourceImpl extends ServerResource implements GetRecordCountResource {
    @Get
    @Override
    public Map<String, Long> getRecordCount(String uuid) {
        Map<String, Long> sessions = null;
        long startTime = 0;
        
        if (Statistics.isEnabled()) {
            startTime = System.currentTimeMillis();
        }
        
        try {
            sessions = PersistentStoreFactory.getPersistentStore().getRecordCount(uuid);
        } catch (Exception ex) {
            Log.logger.log(Level.WARNING, "Unable to get record count", ex.getMessage());
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
        
        if (Statistics.isEnabled()) {
            Statistics.getInstance().incrementTotalReadRecordCount();
            
            if (startTime != 0) {
                Statistics.getInstance().updateReadRecordCountTime(System.currentTimeMillis() - startTime);   
            }
        }
        
        return sessions;
    }
}
