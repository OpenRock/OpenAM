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

package org.forgerock.openam.upgrade.helpers;

import com.iplanet.am.util.SystemProperties;
import com.sun.identity.sm.AbstractUpgradeHelper;
import com.sun.identity.sm.AttributeSchemaImpl;
import java.util.HashSet;
import java.util.Set;
import org.forgerock.openam.upgrade.UpgradeException;

/**
 * This class is used by the upgrade mechanism (pre-upgrade) to set the value
 * of the iplanet-am-session-constraint-resulting-behavior attribute in the 
 * session service to maintain the correct custom value. The behaviour is as 
 * follows:
 * 
 * <UL>
 * <LI>Destroy All Sessions property is set to true and service settings is
 * DESTROY_OLD_SESSION; post upgrade setting should be DestroyAllAction
 * <LI>Destroy All Sessions property is set to false and service settings is 
 * DESTROY_OLD_SESSION; This is the default, no action need be taken. 
 * <LI>Deny Access; If this is the current value then post upgrade value should 
 * be DenyAccessAction.
 * </UL>
 * 
 * @author steve
 */
public class SessionServiceHelper extends AbstractUpgradeHelper {
    private final static String DENY_ACCESS = "DENY_ACCESS";
    private final static String DESTROY_OLD_SESSION = "DESTROY_OLD_SESSION";
    private final static String NEW_DENY_ACCESS = 
            "org.forgerock.openam.session.service.DenyAccessAction";
    private final static String DESTROY_ALL_SESSIONS_CLASS = 
            "org.forgerock.openam.session.service.DestroyAllAction";
    private final static String DESTROY_OLDEST_SESSIONS_CLASS = 
            "org.forgerock.openam.session.service.DestroyOldestAction";
    private static final String DESTROY_ALL_SESSIONS =
            "openam.session.destroy_all_sessions";
    
    static {
        initialize();
    }
    
    private static void initialize() {
        attributes.add("iplanet-am-session-constraint-resulting-behavior");
    }
            
    public AttributeSchemaImpl upgradeAttribute(AttributeSchemaImpl existingAttr, AttributeSchemaImpl newAttr) 
    throws UpgradeException {
        if (!(newAttr.getName().equals("iplanet-am-session-constraint-resulting-behavior"))) {
            return newAttr;
        }
        
        Set<String> defaultValues = existingAttr.getDefaultValues();
        
        if (defaultValues.contains(DESTROY_ALL_SESSIONS_CLASS) || 
            defaultValues.contains(NEW_DENY_ACCESS) ||
            defaultValues.contains(DESTROY_OLDEST_SESSIONS_CLASS)) {
            // nothing to do
            return null;
        }
        
        if (destroyAllSessionsSet() && defaultValues.contains(DESTROY_OLD_SESSION)) {
            Set<String> newDefaultValues = new HashSet<String>();
            newDefaultValues.add(DESTROY_ALL_SESSIONS_CLASS);
            newAttr = updateDefaultValues(newAttr, newDefaultValues);
            
            return newAttr;
        }
        
        if (defaultValues.contains(DENY_ACCESS)) {
            Set<String> newDefaultValues = new HashSet<String>();
            newDefaultValues.add(NEW_DENY_ACCESS);
            newAttr = updateDefaultValues(newAttr, newDefaultValues);
        } 
        
        return newAttr;
    }
    
    private boolean destroyAllSessionsSet() {
        return SystemProperties.getAsBoolean(DESTROY_ALL_SESSIONS);
    }
}

