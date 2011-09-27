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
import java.util.Set;
import org.forgerock.openam.upgrade.UpgradeException;

/**
 * Used to upgrade the iPlanetAMAuthService. 
 * 
 * @author steve
 */
public class AuthServiceHelper extends AbstractUpgradeHelper {    
    private final static String SECURID = "com.sun.identity.authentication.modules.securid.SecurID";
    private final static String ADAPTIVE = "org.forgerock.openam.authentication.modules.adaptive.Adaptive";
    private final static String ATTR = "iplanet-am-auth-authenticators";
    
    static {
        initialize();
    }
    
    private static void initialize() {
        attributes.add(ATTR);
    }
    
    @Override
    public AttributeSchemaImpl upgradeAttribute(AttributeSchemaImpl existingAttr, AttributeSchemaImpl newAttr)
    throws UpgradeException {
        if (!(newAttr.getName().equals(ATTR))) {
            return newAttr;
        }
        
        Set<String> defaultValues = existingAttr.getDefaultValues();
        defaultValues.add(SECURID);
        defaultValues.add(ADAPTIVE);
        newAttr = updateDefaultValues(newAttr, defaultValues);
        
        return newAttr;
    }    
}
