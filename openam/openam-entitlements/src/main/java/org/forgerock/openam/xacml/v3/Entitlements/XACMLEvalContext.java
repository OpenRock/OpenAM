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

import com.sun.identity.entitlement.xacml3.core.DecisionType;
import com.sun.identity.entitlement.xacml3.core.Obligation;
import com.sun.identity.entitlement.xacml3.core.Obligations;
import com.sun.identity.entitlement.xacml3.core.Result;

import java.util.List;

public class XACMLEvalContext  {
    private XACML3Policy policyRef;
    private XACML3EvalContextInterface pip;

    public XACMLEvalContext() {
        policyRef = null;
    }
    public XACMLEvalContext(XACML3Policy pRef) {
        policyRef = pRef;
    }

    public void setPip(XACML3EvalContextInterface pip) {
        this.pip = pip;
    };

    public void setPolicy(XACML3Policy pol) {
        this.policyRef = pol;
    };

    public FunctionArgument getDefinedVariable(String variableID){
         return policyRef.getDefinedVariable(variableID);
    }
    public FunctionArgument resolve(String category, String AttributeID) {
        return pip.resolve(category, AttributeID);
    }
    public void setResult(XACML3Decision decision)  {
        List<Result> results = pip.getResult();
        Result r = new Result();
        r.setStatus(decision.getStatus());
        r.setDecision(decision.getDecision());
        /*
        Obligations obs = new Obligations();
        List<Obligation> ob = obs.getObligation();
        Obligation newOb = new Obligation();
        newOb.
        ob.addAll(decision.getObligations());

        r.setObligations(decision.getObligations());

          */

    }
}
