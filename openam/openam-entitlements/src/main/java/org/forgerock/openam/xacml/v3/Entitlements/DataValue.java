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


/*
   This class Encapsulates a DataValue from the XACML policy.
   In this case, we have the actual Data in the object

*/

import org.json.JSONException;
import org.json.JSONObject;

public class DataValue extends FunctionArgument {
    private Object data;

    public DataValue() {
    }
    /* When we create the value,  is HAS to be of the type */

    public DataValue(String type, Object value, boolean rawType) {
        setType(type);
        data = value;
    }

    public DataValue(String type, String value) {
        setType(type);
        data = getType().typedValue(value);
    }


    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        return this;
    };

    public Object getValue(XACMLEvalContext pip) throws XACML3EntitlementException {
        return data;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = super.toJSONObject();
        jo.put("value", data);
        return jo;
    }

    protected void init(JSONObject jo) throws JSONException {
        super.init(jo);
        this.data = getType().typedValue(jo.optString("value"));
        return;
    };

    public String toXML(String type) {
        /*
             Handle Match AnyOf and AllOf specially
        */
        String retVal = "<AttributeValue DataType=" + getType() + data + "</AttributeValue>";

        return retVal;
    }


}
