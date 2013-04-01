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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class is the parent class for Function Arguments.
 * <p/>
 * It has three subclasses.
 * DataValue for known values
 * DataDesignator for values to fetch at runtime
 * XACMLFunction as a parent class for all functions
 *
 * @author Allan.Foster@forgerock.com
 *
 */
public abstract class FunctionArgument {
    public static FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    public static FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    private DataType dataType;
    private String issuer;

    /**
     * Set Data Type from String Value
     * @param type
     */
    public void setType(String type) {
        this.dataType = new DataType(type);
    }

    /**
     * Set Data Type from DataType
     * @param type
     */
    public void setType(DataType type) {
        this.dataType = type;
    }

    /**
     * Get DataType Object
     * @return DataType
     */
    public DataType getType() {
        return dataType;
    }

    /**
     * Set the Issuer of this FunctionArgument
     * @param issuer
     */
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    /**
     * Get Issuer of this FunctionArgument
     * @return
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * If this FunctionArgument represents a Boolean
     * DataType, then the Boolean value will be returned,
     * otherwise "False" will be returned.
     *
     * @return boolean
     */
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
        } catch (XACML3EntitlementException ex) {
            return false;
        }
    }

    /**
     * If this FunctionArgument represents a Boolean
     * DataType, then the Boolean negative value will be returned,
     *
     * @return boolean
     */
    public boolean isFalse() {
        return !isTrue();
    }

    /**
     * to JSON Object
     *
     * @return
     * @throws JSONException
     */
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put("className", getClass().getName());
        if (dataType != null) {
            jo.put("dataType", dataType.getTypeName());
        } else {
            jo.put("dataType", "");
        }
        jo.put("issuer", issuer);
        return jo;
    }

    /**
     * Initialize the JSONObject
     *
     * @param jo
     * @throws JSONException
     */
    protected void init(JSONObject jo) throws JSONException {
        this.dataType = new DataType(jo.optString("dataType"));
        this.issuer = jo.optString("issuer");
        return;
    }

    /**
     * Obtain a FunctionArgument Object based upon the incoming
     * JSONObject.
     *
     * @param jo
     * @return
     */
    public static FunctionArgument getInstance(JSONObject jo) {
        String className = jo.optString("className");
        try {
            Class clazz = Class.forName(className);
            FunctionArgument farg = (FunctionArgument) clazz.newInstance();
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

    /**
     * Return the jaxbElement for the Function.
     *
     * @param type
     * @return String representing JSON/XML Data Fragment.
     */
    public String toXML(String type) {
        // TODO ::
        return "";
    }

    /**
     * Function Argument Abstract evaluation method. All Implementations are based upon the XACML v3
     * Functions Library.
     *
     * @param pip
     * @return FunctionArgument -- Result based upon the FunctionArgument's return value.
     * @throws XACML3EntitlementException
     */
    public abstract FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException;

    /**
     * Provides retrieval of the raw / untyped Data Value of this FunctionArgument.
     *
     * @param pip
     * @return Object
     * @throws XACML3EntitlementException
     */
    public abstract Object getValue(XACMLEvalContext pip) throws XACML3EntitlementException;

    /**
     * Returns Data Container for this FunctionArgument Implementation
     *
     * @param pip
     * @return FunctionArgument
     * @throws XACML3EntitlementException
     */
    private FunctionArgument getDataContainer(XACMLEvalContext pip) throws XACML3EntitlementException {
        if ((this instanceof DataDesignator) || (this instanceof DataValue)) {
            return this;
        } else {
            return this.evaluate(pip);
        }
    }

    /**
     * Return DataValue as a String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asString(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLSTRINGTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLSTRINGTYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Boolean.
     *
     * @param pip
     * @return Boolean -- Value
     * @throws XACML3EntitlementException
     */
    public Boolean asBoolean(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLBOOLEANTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLBOOLEANTYPE.getTypeName());
        }
        return (Boolean) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Integer.
     *
     * @param pip
     * @return Integer -- Value
     * @throws XACML3EntitlementException
     */
    public Integer asInteger(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLINTEGERTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLINTEGERTYPE.getTypeName());
        }
        return (Integer) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Double.
     *
     * @param pip
     * @return Double -- Value
     * @throws XACML3EntitlementException
     */
    public Double asDouble(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDOUBLETYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLDOUBLETYPE.getTypeName());
        }
        return (Double) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Time Representation within a Date Object.
     *
     * @param pip
     * @return Date -- Value
     * @throws XACML3EntitlementException
     */
    public Date asTime(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLTIMETYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLTIMETYPE.getTypeName());
        }
        return (Date) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Date.
     *
     * @param pip
     * @return Date -- Value
     * @throws XACML3EntitlementException
     */
    public Date asDate(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDATETYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLDATETYPE.getTypeName());
        }
        return (Date) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a DateTime.
     *
     * @param pip
     * @return Date -- Value
     * @throws XACML3EntitlementException
     */
    public Date asDateTime(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDATETIMETYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLDATETIMETYPE.getTypeName());
        }
        return (Date) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a URI String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asAnyURI(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLANYURITYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLANYURITYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Hex Binary String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asHexBinary(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLHEXBINARYTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLHEXBINARYTYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Base64 encoded String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asBase64Binary(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLBASE64BINARYTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLBASE64BINARYTYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Long containing Day Time Duration in Milliseconds.
     *
     * @param pip
     * @return Long -- Value
     * @throws XACML3EntitlementException
     */
    public Long asDayTimeDuration(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDAYTIMEDURATIONTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLDAYTIMEDURATIONTYPE.getTypeName());
        }
        return (Long) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a Year / Month Duration Object Wrapper.
     *
     * @param pip
     * @return XACML3YearMonthDuration -- Value
     * @throws XACML3EntitlementException
     */
    public XACML3YearMonthDuration asYearMonthDuration(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLYEARMONTHDURATIONTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLYEARMONTHDURATIONTYPE.getTypeName());
        }
        return (XACML3YearMonthDuration) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a X500 Name String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asX500Name(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLX500NAMETYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLX500NAMETYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a RFC822 (Email Address) String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asRfc822Name(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLRFC822NAMETYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLRFC822NAMETYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a IP Address String, Either IP V4 or V6 can be handled.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asIpAddress(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLIPADDRESSTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLIPADDRESSTYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a DNS Name String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    public String asDnsName(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLDNSNAMETYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLDNSNAMETYPE.getTypeName());
        }
        return (String) fArg.getValue(pip);
    }

    /**
     * Return DataValue as a XPath Expression String.
     *
     * @param pip
     * @return String -- Value
     * @throws XACML3EntitlementException
     */
    @Deprecated
    public String asXpathExpression(XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument fArg = getDataContainer(pip);

        if (!fArg.dataType.isType(DataType.Type.XACMLXPATHEXPRESSIONTYPE)) {
            throw new IndeterminateException("type conflict found " + fArg.dataType.getTypeName() + ", but requires: " +
                    DataType.Type.XACMLXPATHEXPRESSIONTYPE.getTypeName());
        }
        throw new IndeterminateException("unsupported type conflict");
    }

}
