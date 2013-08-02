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
import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.entitlement.xacml3.core.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class XACML3PolicyRule {
    private FunctionArgument target;
    private FunctionArgument condition;

    private String ruleName;
    private String effect;
    private List<XACML3Obligation> obligations;
    private List<XACML3Advice> advices;

    public XACML3PolicyRule() {

    }

    public String getName() {
        return ruleName;
    }


    public XACML3PolicyRule(Rule rule) {
        target = XACML3PrivilegeUtils.getTargetFunction(rule.getTarget(), new HashSet<String>());
        ruleName = rule.getRuleId();
        effect = rule.getEffect().value();
        condition = XACML3PrivilegeUtils.getConditionFunction(rule.getCondition());
        obligations = new ArrayList<XACML3Obligation>();
        advices = new ArrayList<XACML3Advice>();

        try {
            for (ObligationExpression o : rule.getObligationExpressions().getObligationExpression()) {
                obligations.add(new XACML3Obligation(o));
            }
        } catch (Exception ex) {

        }
        try {
            for (AdviceExpression a : rule.getAdviceExpressions().getAdviceExpression()) {
                advices.add(new XACML3Advice(a));
            }
        } catch (Exception ex) {

        }
    }

    public XACML3Decision evaluate(XACMLEvalContext pip) {

        XACML3Decision result = new XACML3Decision(getName(),pip.getRequest().getContextID(),effect);

        try {
            FunctionArgument evalResult = target.evaluate(pip);

            if (evalResult.isTrue()) {    // we match on target,  so evaluate

                evalResult = condition.evaluate(pip);
                if (evalResult.isTrue() || evalResult.isFalse()) {    // we Match Target,  and Condition

                    if (evalResult.isTrue()) {
                        result.setEffect(true);
                    } else {
                        result.setEffect(false);
                    }

                    if (obligations != null) {
                        ObligationExpressions ob = new ObligationExpressions();
                        List<ObligationExpression> obs = ob.getObligationExpression();
                        for (XACML3Obligation o : obligations) {
                            obs.add(o.getXACML(pip));
                        }
                        result.setObligations(ob);
                    }

                    if (advices != null) {
                        AdviceExpressions ad = new AdviceExpressions();
                        List<AdviceExpression> adv = ad.getAdviceExpression();
                        for (XACML3Advice o : advices) {
                            adv.add(o.getXACML(pip));
                        }
                        result.setAdvices(ad);
                    }
                    return result;
                }
            } else {
                result.setDecision("NotApplicable");
            }
        } catch (XACML3EntitlementException ex) {
            result.setDecision("Indeterminate");
        }

        return result;
    }

    public void init(JSONObject jo) throws JSONException {
        ruleName = jo.optString("ruleName");
        effect = jo.optString("effect");

        target = FunctionArgument.getInstance(jo.getJSONObject("target"));
        condition = FunctionArgument.getInstance(jo.getJSONObject("condition"));
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("classname", this.getClass().getName());
        jo.put("ruleName", ruleName);
        jo.put("effect", effect);
        jo.put("target", target.toJSONObject());
        jo.put("condition", condition.toJSONObject());

        return jo;
    }


    static public XACML3PolicyRule getInstance(JSONObject jo) {
        String className = jo.optString("classname");
        try {
            Class clazz = Class.forName(className);
            XACML3PolicyRule farg = (XACML3PolicyRule) clazz.newInstance();
            farg.init(jo);

            return farg;
        } catch (InstantiationException ex) {
            PrivilegeManager.debug.error("FunctionArgument.getInstance", ex);
        } catch (IllegalAccessException ex) {
            PrivilegeManager.debug.error("FunctionArgument.getInstance", ex);
        } catch (ClassNotFoundException ex) {
            PrivilegeManager.debug.error("FunctionArgument.getInstance", ex);
        } catch (JSONException ex) {
            PrivilegeManager.debug.error("FunctionArgument.getInstance", ex);
        }
        return null;
    }

}
