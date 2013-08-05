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

import com.sun.identity.entitlement.Entitlement;
import com.sun.identity.entitlement.EntitlementCombiner;
import com.sun.identity.entitlement.EntitlementException;

import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class XACML3EntitlementCombiner extends EntitlementCombiner {
    private Entitlement currentResult = null;
    private boolean atDescision = false;
    private String applicationName;
    protected String overRideType;

    public XACML3EntitlementCombiner() {

    }

    @Override
    protected boolean combine(Boolean b1, Boolean b2) {
        return b1.booleanValue() && b2.booleanValue();
    }

    @Override
    protected boolean isCompleted() {
        return atDescision;
    }

    /**
     * Initializes the combiner.
     *
     * @param adminSubject Admin Subject.
     * @param realm Realm name.
     * @param applicationName Application Name.
     * @param resourceName Resource name to be evaluated.
     * @param actions Action names to be evaluated.
     * @param isRecursive <code>true<</code> for sub stree evaluation.
     */
    public void init(
            Subject adminSubject,
            String realm,
            String applicationName,
            String resourceName,
            Set<String> actions,
            boolean isRecursive
    ) throws EntitlementException {

        super.init(adminSubject,realm,applicationName,resourceName,actions,isRecursive);
        this.applicationName = applicationName;
    }
    /**
     * Adds a set of entitlements to the overall entitlement decision. These
     * entitlements will be combined with existing decision.
     *
     * @param entitlements Set of entitlements.
     */
    public void add(List<Entitlement> entitlements) {

        for (Entitlement e : entitlements) {

            Map<String,Boolean> actions = e.getActionValues();

            for (String s : actions.keySet()) {
                if ((s.equals("Indeterminate")) || (s.equals(("NotApplicable")))) {
                    if (currentResult == null) {
                        currentResult = e;
                    }
                    continue;
                }
                currentResult = e;
                if (s.equals(overRideType)) {
                    atDescision = true;
                }
            }
        }
    }
    /**
     * Returns entitlements which is the result of combining a set of
     * entitlement.
     *
     * @return entitlement results.
     */
    public List<Entitlement> getResults() {
        List<Entitlement> res = new ArrayList<Entitlement>();
        currentResult.setApplicationName(applicationName);
        res.add(currentResult);
        return res;
    }
}
