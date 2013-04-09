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


/**
   This class Encapsulates a DataValue from the XACML policy.
   In this case, we have the actual Data in the object

  @author Allan.Foster@forgerock.com

*/

import org.json.JSONException;
import org.json.JSONObject;

public class DataValue extends FunctionArgument {
    /**
     * Data Value Object.
     */
    private Object data;
    private boolean includeInResult = false;  // Used only for Request Objects

    /**
     * Default Constructor
     */
    public DataValue() {
    }

    /**
     * Constructor used to specify whether raw value was supplied or not.
     * When we create the value, is HAS to be of the type.

     * @param type
     * @param value
     * @param rawType
     */
    public DataValue(String type, Object value, boolean rawType) {
        setType(type);
        if (!rawType) {
            data = getType().typedValue((String)value);
        } else {
            data = value;
        }
    }

    /**
     * Constructor used to specify the value represented by String Data.

     * @param type
     * @param value
     */
    public DataValue(String type, String value) {
        setType(type);
        data = getType().typedValue(value);
    }

    public void setIncludeInResult(boolean val) {
        includeInResult = val;
    }
    public boolean getIncludeInResult() {
        return includeInResult;
    }

    /**
     * Evaluate the Function Argument Set.
     *
     * @param pip
     * @return
     * @throws XACML3EntitlementException
     */
    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        return this;
    }

    /**
     * Get the current Data Value.
     *
     * @param pip
     * @return
     * @throws XACML3EntitlementException
     */
    public Object getValue(XACMLEvalContext pip) throws XACML3EntitlementException {
        return data;
    }

    /**
     * Get the current value in JSON Form.
     *
     * @return
     * @throws JSONException
     */
    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = super.toJSONObject();
        jo.put("value", data);
        return jo;
    }

    /**
     * Initialize Data Value from a JSON Object.
     *
     * @param jo
     * @throws JSONException
     */
    protected void init(JSONObject jo) throws JSONException {
        super.init(jo);
        this.data = getType().typedValue(jo.optString("value"));
        return;
    }

    /**
     * UnMarshal the exiting DataType to XML.
     *
     * @param type
     * @return
     */
    public String toXML(String type) {
        /*
             Handle Match AnyOf and AllOf specially
        */
        String retVal = "<AttributeValue DataType=" + getType() + data + "</AttributeValue>";

        return retVal;
    }

    /**
     * Provides Override for Equals method to ensure the 'data' Object is considered.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataValue otherDataValue = (DataValue) o;
        DataType otherDataType = otherDataValue.getType();
        // depending upon the DataType, we may need to perform an equalsIgnoreCase, instead of equals.
        if ( (otherDataType.isType(DataType.Type.XACMLDNSNAMETYPE)) ||
             (otherDataType.isType(DataType.Type.XACMLX500NAMETYPE)) ||
             (otherDataType.isType(DataType.Type.XACMLRFC822NAMETYPE)) ) {
            if (data != null ?
                    !((String)data).equalsIgnoreCase((String)otherDataValue.data) : otherDataValue.data != null) {
                    return false;
            }
        } else if ( (otherDataType.isType(DataType.Type.XACMLHEXBINARYTYPE)) &&
                    (otherDataValue.data instanceof String) ) {
                byte[] this_byteArray = XACML3PrivilegeUtils.convertHexBinaryStringToByteArray((String)data);
                byte[] other_byteArray = XACML3PrivilegeUtils.convertHexBinaryStringToByteArray((String)otherDataValue.data);
            if (this_byteArray != null ? !this_byteArray.equals(other_byteArray) : other_byteArray != null) {
                return false;
            }
        } else {
            if (data != null ? !data.equals(otherDataValue.data) : otherDataValue.data != null) return false;
        }
        return true;
    }

    /**
     * Provides Override for Hash code of our wrapped Object.
     * @return
     */
    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }
}
