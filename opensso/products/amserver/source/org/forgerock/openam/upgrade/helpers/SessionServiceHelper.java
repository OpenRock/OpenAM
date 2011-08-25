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

import com.sun.identity.sm.AbstractUpgradeHelper;
import com.sun.identity.sm.AttributeSchemaImpl;
import java.util.HashSet;
import java.util.Set;
import org.forgerock.openam.upgrade.UpgradeException;

/**
 *
 * @author steve
 */
public class SessionServiceHelper extends AbstractUpgradeHelper {
    private final static String DENY_ACCESS = "DENY_ACCESS";
    private final static String NEW_DENY_ACCESS = 
            "org.forgerock.openam.session.service.DenyAccessAction";
            
    public AttributeSchemaImpl upgradeAttribute(AttributeSchemaImpl existingAttr, AttributeSchemaImpl newAttr) 
    throws UpgradeException {
        if (!(newAttr.getName().equals("iplanet-am-session-constraint-resulting-behavior"))) {
            return newAttr;
        }
        
        Set<String> defaultValues = existingAttr.getDefaultValues();
        
        if (defaultValues.contains(DENY_ACCESS)) {
            Set<String> newDefaultValues = new HashSet<String>();
            newDefaultValues.add(NEW_DENY_ACCESS);
            newAttr = updateDefaultValues(newAttr, newDefaultValues);
        } 
        
        return newAttr;
    }
    
}
