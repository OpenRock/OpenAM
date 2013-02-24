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

public class XACML3Policy {
    private Policy policy;
    private String policyName;

    private FunctionArgument target;
    private List<XACML3PolicyRule> rules;
    private java.util.Map<String,FunctionArgument> definedVars;
    private String ruleCombiner;
    private Set<String> resourceSelectors;


    public XACML3Policy(Policy policy) {
        this.policy = policy;
        policyName = policy.getPolicyId();
        ruleCombiner = policy.getRuleCombiningAlgId();
        resourceSelectors = new HashSet<String>();

        target = XACML3PrivilegeUtils.getTargetFunction(policy.getTarget(),resourceSelectors);

        definedVars =  XACML3PrivilegeUtils.getVariableDefinitions(policy);

        rules =  XACML3PrivilegeUtils.getRules(policy);

    }

    public FunctionArgument getDefinedVariable(String variableID){
        FunctionArgument retVal = definedVars.get(variableID);
        if (retVal == null) {
            retVal = FunctionArgument.indeterminateObject;
        }
        return retVal;
    }

    public Policy getPolicy() {
        return this.policy;
    }

    public void setPolicy(Policy pol) {
         this.policy = pol;
    }

    public Set<String> getResourceSelectors() {
         return resourceSelectors;
    }
    public void setResourceSelectors(Set<String> rSel) {
        resourceSelectors = rSel;
    }

    public XACML3Decision evaluate(XACMLEvalContext pip) {

        XACML3Decision result = new XACML3Decision();
        boolean indeterminate = true;

        FunctionArgument evalResult = target.evaluate(pip);

        if (evalResult.isTrue())        {    // we  match,  so evaluate
            for (XACML3PolicyRule r : rules) {
                XACML3Decision decision = r.evaluate(pip);
                if (decision.getStatus() ==  XACML3Decision.XACML3DecisionStatus.TRUE_VALUE) {
                    return decision;
                }
                if (decision.getStatus() ==  XACML3Decision.XACML3DecisionStatus.FALSE_VALUE) {
                    indeterminate = false;
                }
            }
        }
        if (indeterminate) {
            result.setStatus(XACML3Decision.XACML3DecisionStatus.INDETERMINATE);
        } else  {
            result.setStatus(XACML3Decision.XACML3DecisionStatus.FALSE_VALUE);
        }

        return result;
    }


    /*
     *
     */
}
