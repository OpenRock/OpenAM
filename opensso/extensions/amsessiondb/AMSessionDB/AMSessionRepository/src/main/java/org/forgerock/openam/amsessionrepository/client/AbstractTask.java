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

import com.sun.identity.ha.FAMRecordUtils;
import com.sun.identity.shared.debug.Debug;

/**
 * Abstract class to the session persister tasks
 * 
 * TODO: Would be nice to have a retry count and be able to put failed tasks
 * back in the queue to be retried.
 * 
 * @author steve
 */
public abstract class AbstractTask implements Runnable {
    protected static Debug debug = null;
    protected String resourceURL = null;
    
    static {
        initialize();
    }
    
    private static void initialize() {
        debug = FAMRecordUtils.debug;
    }
    
    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception ex) {
            debug.warning("Unable to execute task: " + toString());
        }
    }
    
    public abstract void doTask() throws Exception;
}
