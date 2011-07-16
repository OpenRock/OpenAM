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

/**
 * Implements the read resource functionality
 * 
 * @author steve
 */

import java.util.logging.Level;
import org.forgerock.openam.amsessionstore.common.AMRecord;
import org.forgerock.openam.amsessionstore.common.Log;
import org.forgerock.openam.amsessionstore.db.NotFoundException;
import org.forgerock.openam.amsessionstore.db.PersistentStoreFactory;
import org.forgerock.openam.amsessionstore.db.StoreException;
import org.forgerock.openam.amsessionstore.resources.ReadResource;
import org.forgerock.openam.amsessionstore.shared.Statistics;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class ReadResourceImpl extends ServerResource implements ReadResource {
    @Get
    @Override
    public AMRecord read(String id) {
        AMRecord record = null;
        long startTime = 0;
        
        if (Statistics.isEnabled()) {
            startTime = System.currentTimeMillis();
        }
        
        try {
            record = PersistentStoreFactory.getPersistentStore().read(id);
        } catch (StoreException sex) {
            Log.logger.log(Level.WARNING, "Unable to read", sex.getMessage());
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, sex.getMessage());
        } catch (NotFoundException nfe) {
            Log.logger.log(Level.WARNING, "Unable to read", nfe.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, nfe.getMessage());
        } catch (Exception ex) {
            Log.logger.log(Level.WARNING, "Unable to read", ex.getMessage());
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
        
        if (Statistics.isEnabled()) {
            Statistics.getInstance().incrementTotalReads();
            
            if (startTime != 0) {
                Statistics.getInstance().updateReadTime(System.currentTimeMillis() - startTime);    
            }
        }
        
        return record;
    }
}
