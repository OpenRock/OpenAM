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

import org.forgerock.openam.amsessionstore.resources.DeleteByDateResource;
import org.restlet.resource.ClientResource;

/**
 *
 * @author steve
 */
public class DeleteByDateTask extends AbstractTask {
    protected long expTime;
    
    public DeleteByDateTask(String resourceURL, long expTime) {
        this.resourceURL = resourceURL;
        this.expTime = expTime;
    }

    public void doTask() 
    throws Exception {
        ClientResource resource = new ClientResource(resourceURL + DeleteByDateResource.URI);
        DeleteByDateResource purgeResource = resource.wrap(DeleteByDateResource.class);

        purgeResource.remove(expTime);

        if (debug.messageEnabled()) {
            debug.message("records deleted by exp data: " + expTime);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append(DeleteByDateTask.class).append(": expTime=").append(expTime);
        
        return output.toString();        
    }
}
