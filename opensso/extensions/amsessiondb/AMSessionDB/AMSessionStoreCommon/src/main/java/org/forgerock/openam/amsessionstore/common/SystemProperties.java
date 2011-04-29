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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;

/**
 *
 * @author steve
 */
public class SystemProperties {
    private static Map<String, String> properties;
    
    static {
        initialize();
    }
    
    private static void initialize() {
        properties = new HashMap();
        
        InputStream pin = ClassLoader.getSystemResourceAsStream(Constants.PROPERTIES_FILE);
        PropertyResourceBundle propertyBundle = null;
        
        try {
            propertyBundle = new PropertyResourceBundle(pin);
        } catch (IOException ioe) {
            System.out.println("IOException " + ioe.getMessage());
        }
        
        for (String key : propertyBundle.keySet()) {
            properties.put(key, propertyBundle.getString(key));
        }
    }
    
    public static String get(String key, String defaultValue) {
        String value = properties.get(key);
        return (value != null) ? value : defaultValue;
    }
}
