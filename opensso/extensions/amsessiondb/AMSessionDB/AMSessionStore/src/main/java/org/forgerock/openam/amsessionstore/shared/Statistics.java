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

package org.forgerock.openam.amsessionstore.shared;

import org.forgerock.openam.amsessionstore.common.Constants;
import org.forgerock.openam.amsessionstore.common.SystemProperties;
import java.io.Serializable;

/**
 *
 * @author steve
 */
public class Statistics implements Serializable {
    private static boolean enabled = true;
    private static String enabledProperty = null;
    private static Statistics instance = null;
    
    static {
        initialize();
    }
    
    private static void initialize() {
        enabledProperty = SystemProperties.get(Constants.STATS_ENABLED,
                Constants.TRUE);        
        enabled = (enabledProperty.equalsIgnoreCase(Constants.TRUE)) ? true : false;
    }
    
    private int totalRequests;
    private int totalReads;
    private int totalWrites;
    private int totalDeletes;
    private int totalReadSessionCount;
    
    
    public Statistics() {
        // do nothing
    }
    
    public static Statistics getInstance() {
        if (instance == null) {
            instance = new Statistics();
        }
        
        return instance;
    }
    
    public int getTotalRequests() {
        return totalRequests;
    }
    
    public int getTotalReads() {
        return totalReads;
    }
    
    public void incrementTotalReads() {
        totalReads++;
        totalRequests++;
    }
    
    public int getTotalWrites() {
        return totalWrites;
    }
    
    public void incrementTotalWrites() {
        totalWrites++;
        totalRequests++;
    }
    
    public int getTotalDeletes() {
        return totalDeletes;
    }
    
    public void incrementTotalDeletes() {
        totalDeletes++;
        totalRequests++;
    }
    
    public int getTotalReadSessionCount() {
        return totalReadSessionCount;
    }
    
    public void incrementsTotalReadSessionCount() {
        totalReadSessionCount++;
        totalRequests++;
    }
    
    public void resetStatistics() {
        totalRequests = 0;
        totalReads = 0;
        totalWrites = 0;
        totalDeletes = 0;
        totalReadSessionCount = 0;
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
}
