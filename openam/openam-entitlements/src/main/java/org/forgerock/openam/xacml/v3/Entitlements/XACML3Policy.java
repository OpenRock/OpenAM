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

import java.util.List;

public class XACML3Policy {
    private Policy policy;
    private String policyName;

    private FunctionArgument target;
    private List<XACML3PolicyRule> rules;
    private java.util.Map<String,FunctionArgument> definedVars;
    private String ruleCombiner;


    public XACML3Policy(Policy policy) {
        this.policy = policy;
        policyName = policy.getPolicyId();
        ruleCombiner = policy.getRuleCombiningAlgId();

        target = XACML3PrivilegeUtils.getTargetFunction(policy.getTarget());

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


    /*
     * After constructing the policy, we can parse the policy object,  extracting
     * the target info,  and then pulling each rule out, into a separate rule
     *
     * 1) Extract the target function from the Policy
     */

    public FunctionArgument evaluate() {


          return FunctionArgument.indeterminateObject;
    }


    /*
     *
     */
}
