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

import com.sun.identity.entitlement.xacml3.core.AdviceExpressions;
import com.sun.identity.entitlement.xacml3.core.ObligationExpressions;
import com.sun.identity.entitlement.xacml3.core.Rule;

import java.util.HashSet;
import java.util.List;

public class XACML3PolicyRule {
    private FunctionArgument target;
    private FunctionArgument condition;

    private String ruleName;
    private String effect;
    private ObligationExpressions obligations;
    private AdviceExpressions advices;



    public XACML3PolicyRule(Rule rule) {
        target = XACML3PrivilegeUtils.getTargetFunction(rule.getTarget(),new HashSet<String>());
        ruleName = rule.getRuleId();
        effect = rule.getEffect().value();
        condition = XACML3PrivilegeUtils.getConditionFunction(rule.getCondition());

        obligations = rule.getObligationExpressions();
        advices = rule.getAdviceExpressions();
    }


    public XACML3Decision evaluate(XACMLEvalContext pip) {

        XACML3Decision result = new XACML3Decision();

        FunctionArgument evalResult = target.evaluate(pip);

        if (evalResult.isTrue())        {    // we Dont match,  so evaluate
            evalResult = condition.evaluate(pip);
            if (evalResult.isTrue())        {    // we Match Target,  and Condition
                result.setStatus(XACML3Decision.XACML3DecisionStatus.TRUE_VALUE);
                result.setEffect(effect);
                if (obligations != null) {
                    result.setObligations(obligations);
                }
                if (advices != null) {
                    result.setAdvices(advices);
                }
                return result;
            }
        }

        if (evalResult.isIndeterminate()) {
            result.setStatus(XACML3Decision.XACML3DecisionStatus.INDETERMINATE);
        } else if (evalResult.isNotApplicable()) {
            result.setStatus(XACML3Decision.XACML3DecisionStatus.NOTAPPLICABLE);
        } else if (evalResult.isFalse()) {
            result.setStatus(XACML3Decision.XACML3DecisionStatus.FALSE_VALUE);
        }

        return result;
    }

}
