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

package org.forgerock.openam.amsessionstore.common;

/**
 *
 * @author steve
 */
public class Constants {
    public static final String PROPERTIES_FILE = "amsessiondb.properties";
    
    public static final String PERSISTER_KEY = 
        "amsessiondb.amrecordpersister"; 
    
    public static final String STATS_ENABLED = 
        "amsessiondb.enabled";
    
    public static final String PORT = "amsessiondb.port";
    
    public static final String URI = "amsessiondb.uri";
    
    public static final String DB_URL = "amsessiondb.db.url";
    
    public static final String DB_ADMIN = "amsessiondb.db.admin";
    
    public static final String DB_PASSWORD = "amsessiondb.db.password";
    
    public static final String DB_POOL_MIN = "amsessiondb.db.poolMinSize";
    
    public static final String DB_POOL_MAX = "amsessiondb.db.poolMaxSize";
    
    public static final String TRUE = "true";
    
    public static final String FALSE = "false";
    
    public static final String LOCAL = "local";
}
