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

package org.forgerock.identity.openam.xacml.v3.Entitlements;

import com.sun.identity.entitlement.Entitlement;
import com.sun.identity.entitlement.EntitlementCondition;
import com.sun.identity.entitlement.EntitlementException;
import com.sun.identity.entitlement.EntitlementSubject;
import com.sun.identity.entitlement.Privilege;
import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.entitlement.ReferralPrivilege;
import com.sun.identity.entitlement.ResourceAttribute;

import com.sun.identity.entitlement.UserSubject;

import com.sun.identity.entitlement.opensso.XACMLOpenSSOPrivilege;
import com.sun.identity.entitlement.xacml3.core.AllOf;
import com.sun.identity.entitlement.xacml3.core.Apply;
import com.sun.identity.entitlement.xacml3.core.AnyOf;
import com.sun.identity.entitlement.xacml3.core.AttributeValue;
import com.sun.identity.entitlement.xacml3.core.AttributeDesignator;
import com.sun.identity.entitlement.xacml3.core.AttributeSelector;
import com.sun.identity.entitlement.xacml3.core.Condition;
import com.sun.identity.entitlement.xacml3.core.Function;
import com.sun.identity.entitlement.xacml3.core.VariableReference;
import com.sun.identity.entitlement.xacml3.core.EffectType;
import com.sun.identity.entitlement.xacml3.core.Match;
import com.sun.identity.entitlement.xacml3.core.ObjectFactory;
import com.sun.identity.entitlement.xacml3.core.Policy;
import com.sun.identity.entitlement.xacml3.core.PolicySet;
import com.sun.identity.entitlement.xacml3.core.Rule;
import com.sun.identity.entitlement.xacml3.core.Target;
import com.sun.identity.entitlement.xacml3.core.VariableDefinition;
import com.sun.identity.entitlement.xacml3.core.Version;

import com.sun.identity.shared.JSONUtils;
import com.sun.identity.shared.xml.XMLUtils;

import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;

import org.forgerock.identity.openam.xacml.v3.Functions.*;

/**
 * Class with utility methods to map from
 * </code>com.sun.identity.entitlement.xacml3.core.Policy</code>
 */
public class XACML3PrivilegeUtils {


    static FunctionArgument getTargetFunction(Target target) {
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
                    DataDesignator dd = new DataDesignator(attrd.getDataType(),attrd.getCategory(),attrd.getAttributeId());

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
                varsMap.put (it.getVariableId(),XACML3PrivilegeUtils.getFunction((it.getExpression()));
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
            DataDesignator dd = new DataDesignator(attr.getDataType(),attr.getCategory(),attr.getAttributeId());
            retVal = dd;

        } else if (clazz.equals(Function.class)) {

        } else if (clazz.equals(AttributeSelector.class)) {

        } else if (clazz.equals(VariableReference.class)) {
            VariableReference vr = (VariableReference)je.getValue();
            XACMLFunction it = XACMLFunction.getInstance("urn:oasis:names:forgerock:xacml:1.0:function:VariableDereference");
            it.addArgument(new DataValue(DataType.XACMLSTRING,vr.getVariableId());
            retVal = it;
        }
         return retVal;
    }


    public static FunctionArgument getConditionFunction(Condition cond) {

        return (getFunction(cond.getExpression()));
    }

}

