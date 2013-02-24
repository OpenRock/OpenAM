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
package org.forgerock.openam.xacml.v3.Entitlements;

import com.sun.identity.entitlement.xacml3.core.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* A decision encapsulates a
    Decision String,
    Advices,
    and Obligations
*/

public class XACML3Decision {
    public enum XACML3DecisionStatus  {FALSE_VALUE, TRUE_VALUE, INDETERMINATE, NOTAPPLICABLE };

    private  XACML3DecisionStatus status;
    private String effect;
    private ObligationExpressions obligations;
    private AdviceExpressions advices;

    public  XACML3Decision() {

    }

    public XACML3DecisionStatus getStatus() {
        return status;
    }
    public void setStatus(XACML3DecisionStatus stat) {
        status = stat;
    }
    public String getEffect() {
        return effect;
    }
    public void setEffect(String eff) {
        effect = eff;
    }
    public ObligationExpressions getObligations() {
        return obligations;
    }
    public void setObligations(ObligationExpressions ob) {
        obligations = ob;
    }
    public AdviceExpressions getAdvices() {
        return advices;
    }
    public void setAdvices(AdviceExpressions ad) {
        advices = ad;
    }
}
