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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import com.sun.identity.entitlement.xacml3.core.AllOf;
import com.sun.identity.entitlement.xacml3.core.AnyOf;
import org.forgerock.openam.xacml.v3.Functions.*;

/**
 * Class with utility methods to map from
 * </code>com.sun.identity.entitlement.xacml3.core.Policy</code>
 */
public class XACML3PrivilegeUtils {


    static FunctionArgument getTargetFunction(Target target, Set<String> rSelectors) {
        List<AnyOf> anyOfList = target.getAnyOf();

        XACMLFunction retVal = XACMLFunction.getInstance("urn:oasis:names:tc:xacml:1.0:function:any-of");

        for (AnyOf anyOf : anyOfList) {
            List<AllOf> allOfList = anyOf.getAllOf();
            XACMLFunction parent = XACMLFunction.getInstance("urn:oasis:names:tc:xacml:1.0:function:all-of");
            for (AllOf allOf : allOfList) {
                List<Match> matchList = allOf.getMatch();
                for (Match match : matchList) {
                    String mName = match.getMatchId();
                    AttributeValue attr = match.getAttributeValue();
                    DataValue dv = new DataValue(attr.getDataType(),attr.getContent().get(0));

                    AttributeDesignator attrd = match.getAttributeDesignator();
                    if (attrd == null) { continue; };
                    DataDesignator dd = new DataDesignator(attrd.getDataType(),attrd.getCategory(),attrd.getAttributeId(),attrd.isMustBePresent());
                    if (attrd.getCategory().contains(":resource")) {
                        rSelectors.add(attrd.getAttributeId());
                    }

                    parent.addArgument(XACMLFunction.getInstance(mName).addArgument(dv).addArgument(dd));

                }
            }
            retVal.addArgument(parent);
        }
        return retVal;
    }

    public static Map<String,FunctionArgument> getVariableDefinitions(Policy policy) {
        if (policy == null) {
            return null;
        }
        Map<String,FunctionArgument> varsMap = new HashMap<String,FunctionArgument>();
        List<Object> obList = policy.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
        for (Object ob : obList) {
            if (ob instanceof VariableDefinition) {
                VariableDefinition it = (VariableDefinition)ob;
                varsMap.put(it.getVariableId(), XACML3PrivilegeUtils.getFunction((it.getExpression())));
            }
        }
        return varsMap;
    }

    public static List<XACML3PolicyRule> getRules(Policy policy) {
        if (policy == null) {
            return null;
        }
        List<XACML3PolicyRule> ruleList = new ArrayList<XACML3PolicyRule>();
        List<Object> obList = policy.getCombinerParametersOrRuleCombinerParametersOrVariableDefinition();
        for (Object ob : obList) {
            if (ob instanceof Rule) {
                ruleList.add(new XACML3PolicyRule((Rule)ob));
            }
        }
        return ruleList;
    }

    /*
    *     {@link JAXBElement }{@code <}{@link AttributeValue }{@code >}
    *     {@link JAXBElement }{@code <}{@link AttributeDesignator }{@code >}
    *     {@link JAXBElement }{@code <}{@link Function }{@code >}
    *     {@link JAXBElement }{@code <}{@link AttributeSelector }{@code >}
    *     {@link JAXBElement }{@code <}{@link Expression }{@code >}
    *     {@link JAXBElement }{@code <}{@link Apply }{@code >}
    *     {@link JAXBElement }{@code <}{@link VariableReference }{@code >}


    */
    static private  FunctionArgument getFunction(JAXBElement je) {
        FunctionArgument retVal = null;
        Class clazz = je.getDeclaredType();

        if (clazz.equals(Apply.class)) {
            Apply apply = (Apply)je.getValue();
            String functionId = apply.getFunctionId();
            XACMLFunction it = XACMLFunction.getInstance(functionId);

            List<JAXBElement<?>> expressionList = apply.getExpression();
            for (JAXBElement jaxe : expressionList) {
                it.addArgument(getFunction(jaxe));
            }
            retVal = it;

        } else if (clazz.equals(AttributeValue.class)) {

            AttributeValue attr = (AttributeValue)je.getValue();
            DataValue dv = new DataValue(attr.getDataType(),attr.getContent().get(0));
            retVal = dv;

        } else if (clazz.equals(AttributeDesignator.class)) {

            AttributeDesignator attr = (AttributeDesignator)je.getValue();
            DataDesignator dd = new DataDesignator(attr.getDataType(),attr.getCategory(),attr.getAttributeId(),attr.isMustBePresent());
            retVal = dd;

        } else if (clazz.equals(Function.class)) {

        } else if (clazz.equals(AttributeSelector.class)) {

        } else if (clazz.equals(VariableReference.class)) {
            VariableReference vr = (VariableReference)je.getValue();
            XACMLFunction it = XACMLFunction.getInstance("urn:oasis:names:forgerock:xacml:1.0:function:VariableDereference");
            it.addArgument(new DataValue(DataType.XACMLSTRING,vr.getVariableId()));
            retVal = it;
        }
         return retVal;
    }


    public static FunctionArgument getConditionFunction(Condition cond) {

        return (getFunction(cond.getExpression()));
    }
    public static FunctionArgument getAssignmentFunction(AttributeAssignmentExpression assign) {

        return (getFunction(assign.getExpression()));
    }

    public static Date stringToDate(String dateString) {

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd:HH:mm:ss.SSSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateString = dateString.replace("T", ":");
        Date retVal = new Date();
        try {
            retVal = sdf.parse(dateString);
        } catch (java.text.ParseException pe) {
            //TODO: log debug warning
        }
        return retVal;

    }

    public static String dateToString(Date date){

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
        sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));

        String retVal = sdf1.format (date) + "T" + sdf2.format(date);
        return retVal;
    }

    public static Date stringToTimeCalendar(String timeString) {
        Date retVal = null;
        SimpleDateFormat sdf = new SimpleDateFormat(
                "HH:mm:ss.SSSS");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            retVal = sdf.parse(timeString);
        } catch (java.text.ParseException pe) {
            //TODO: log debug warning
        }
        return retVal;

    }


}
