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
 * Statistics implementation used by the store to keep track of the number
 * and type of received requests.
 * 
 * Statistics can be enabled/disabled using the configuration file.
 * 
 * @author steve
 */
public class Statistics implements Serializable {
    private static boolean enabled = true;
    private static Statistics instance = null;
    
    static {
        initialize();
    }
    
    private static void initialize() {
        enabled = SystemProperties.getAsBoolean(Constants.STATS_ENABLED, true);
    }
    
    private int totalRequests;
    private int totalReads;
    private int totalWrites;
    private int totalDeletes;
    private int totalReadSessionCount;
    
    
    public Statistics() {
        // do nothing
    }
    
    /**
     * Returns the singleton instance
     * 
     * @return 
     */
    public static synchronized Statistics getInstance() {
        if (instance == null) {
            instance = new Statistics();
        }
        
        return instance;
    }
    
    /**
     * Returns the total number of requests
     * 
     * @return The total number of requests
     */
    public int getTotalRequests() {
        return totalRequests;
    }
    
    /**
     * Returns the total number of reads
     * 
     * @return The total number of reads
     */
    public int getTotalReads() {
        return totalReads;
    }
    
    /**
     * Increment the total read count
     */
    public void incrementTotalReads() {
        totalReads++;
        totalRequests++;
    }
    
    /**
     * Get the total number of writes
     * 
     * @return The total number of writes
     */
    public int getTotalWrites() {
        return totalWrites;
    }
    
    /**
     * Increment the total number of writes
     */
    public void incrementTotalWrites() {
        totalWrites++;
        totalRequests++;
    }
    
    /**
     * Get the total number of deletes
     * 
     * @return The total number of deletes 
     */
    public int getTotalDeletes() {
        return totalDeletes;
    }
    
    /**
     * Increment the total number of deletes
     */
    public void incrementTotalDeletes() {
        totalDeletes++;
        totalRequests++;
    }
    
    /**
     * Get the total number of reads session count
     * 
     * @return 
     */
    public int getTotalReadSessionCount() {
        return totalReadSessionCount;
    }
    
    /**
     * Increment the total read session count total
     */
    public void incrementsTotalReadSessionCount() {
        totalReadSessionCount++;
        totalRequests++;
    }
    
    /**
     * Resets the statistics counters to zero.
     */
    public void resetStatistics() {
        totalRequests = 0;
        totalReads = 0;
        totalWrites = 0;
        totalDeletes = 0;
        totalReadSessionCount = 0;
    }
    
    /** 
     * Used to determine if statistics is enabled in the server 
     * 
     * @return true if stats are enabled, false otherwise.
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
}
