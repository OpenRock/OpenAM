/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.xacml.v3.model;

import com.sun.identity.entitlement.Entitlement;
import com.sun.identity.entitlement.xacml3.core.Status;

import java.util.Set;

public class XACML3Entitlement extends Entitlement {
    private  XACML3Decision decision;
    private String ruleName;

    public XACML3Entitlement(
            String applicationName,
            String resourceName,
            Set<String> actionNames
    ) {
            super(applicationName,resourceName,actionNames);

    }
    public void setRuleName(String ruleid) {
        ruleName = ruleid;
    }
    public String getRuleName() {
        return ruleName;
    }
    public void setDecision(XACML3Decision decision) {
        this.decision = decision;
    }
    public XACML3Decision getDecision() {
        return decision;
    }
}
