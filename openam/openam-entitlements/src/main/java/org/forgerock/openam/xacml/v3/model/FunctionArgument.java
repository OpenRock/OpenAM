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


import com.sun.identity.entitlement.PrivilegeManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/*
   This class is the parent class for Function Arguments.
   It has three subclasses.
       DataValue for known values
       DataDesignator for values to fetch at runtime
       XACMLFunction as a parent class for all functions

*/
public abstract class FunctionArgument  {
    public static FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN,"true");
    public static FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN,"false");

    private DataType dataType;
    private String issuer ;

    public void setType(String type) {
        this.dataType = new DataType(type);
    }
    public void setType(DataType type) {
        this.dataType = type;
    }
    public DataType getType() {
        return dataType;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    public String getIssuer() {
        return issuer;
    }
    public boolean isTrue() {
        try {
        if (this instanceof DataValue) {
            if (dataType.isType(DataType.Type.XACMLBOOLEANTYPE)) {
                return Boolean.valueOf(this.asBoolean(null));
            } else {
                return false;
            }
        } else {
            return false;
        }
        } catch (XACML3EntitlementException ex)  {
            return false;
        }
    }
    public boolean isFalse() {
        return !isTrue();
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put("className", getClass().getName());
        if (dataType != null) {
        jo.put("dataType",dataType.getTypeName());
        } else {
            jo.put("dataType","");
        }
        jo.put("issuer",issuer);
        return jo;
    }

    protected void init(JSONObject jo) throws JSONException {
        this.dataType = new DataType(jo.optString("dataType"));
        this.issuer = jo.optString("issuer");
        return;
    }

    public static FunctionArgument getInstance(JSONObject jo) {
        String className = jo.optString("className");
        try {
            Class clazz = Class.forName(className);
            FunctionArgument farg = (FunctionArgument)clazz.newInstance();
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

    /*
    return the    jaxbElement for the Function.
    */
    public String toXML(String type) {
        return "";
    }


    public abstract FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException;
    public abstract Object getValue(XACMLEvalContext pip) throws XACML3EntitlementException ;

    private FunctionArgument getDataContainer(XACMLEvalContext pip) throws XACML3EntitlementException {
        if ((this instanceof DataDesignator) || (this instanceof DataValue)) {
            return this;
        } else {
            return this.evaluate(pip);
        }
    }
    public String asString(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLSTRINGTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public Boolean asBoolean(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLBOOLEANTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (Boolean) fArg.getValue(pip);
    }
    public Integer asInteger(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLINTEGERTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (Integer) fArg.getValue(pip);
    }
    public Double asDouble(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDOUBLETYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (Double) fArg.getValue(pip);
    }
    public Date asTime(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLTIMETYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (Date) fArg.getValue(pip);
    }
    public Date asDate(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDATETYPE)) {
            throw new IndeterminateException("type conflict found "+fArg.dataType.getTypeName()+", but requires: "+
                DataType.Type.XACMLDATETYPE.getTypeName());
        }
        return (Date) fArg.getValue(pip);
    }
    public Date asDateTime(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDATETIMETYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (Date) fArg.getValue(pip);
    }
    public String asAnyURI(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLANYURITYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public String asHexBinary(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLHEXBINARYTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public String asBase64Binary(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLBASE64BINARYTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public Long asDayTimeDuration(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDAYTIMEDURATIONTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (Long) fArg.getValue(pip);
    }
    public XACML3YearMonthDuration asYearMonthDuration(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLYEARMONTHDURATIONTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (XACML3YearMonthDuration) fArg.getValue(pip);
    }
    public String asX500Name(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLX500NAMETYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public String asRfc822Name(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLRFC822NAMETYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public String asIpAddress(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLIPADDRESSTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public String asDnsName(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDNSNAMETYPE)) {
            throw new IndeterminateException("type conflict");
        }
        return (String) fArg.getValue(pip);
    }
    public String asXpathExpression(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLXPATHEXPRESSIONTYPE)) {
            throw new IndeterminateException("type conflict");
        }
        throw new IndeterminateException("type conflict");
    }

}
