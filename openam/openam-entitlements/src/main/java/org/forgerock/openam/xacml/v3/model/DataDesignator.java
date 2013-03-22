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



/*
    This class Encapsulates a DataDesignator from the XACML policy.
    In this case, we have To Fetch the data from PIP

 */


import org.json.JSONException;
import org.json.JSONObject;

public class DataDesignator extends FunctionArgument {
    private String category;
    private String attributeID;
    private boolean mustExist;

    public DataDesignator() {
    }
    public DataDesignator(String type, String category, String attributeID,boolean presence) {
        setType(type);
        this.category = category;
        this.attributeID = attributeID;
        this.mustExist = presence;
    }
    public String getCategory() {
        return category;
    }
    public String getAttributeID() {
        return attributeID;
    }
    public boolean mustExist() {
        return mustExist;
    }

    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        //return pip.resolve(category,attributeID);
        return pip.resolve(this);
    }

    public Object getValue(XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument fArg = evaluate(pip);
        if (fArg == null) {
            if (mustExist) {
                throw new IndeterminateException("Required attrib not found");
            } else {
                return null;
            }
        }
        Object ob = fArg.getValue(pip);
        if (ob == null)  {
            return "false";
        }
        return ob;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = super.toJSONObject();
        jo.put("category",category);
        jo.put("attributeID",attributeID);
        return jo;
    }
    protected void init(JSONObject jo) throws JSONException {
        super.init(jo);
        this.category = jo.optString("category");
        this.attributeID = jo.optString("attributeID");
        return;
    };


    public String toXML(String type) {
        /*
             Handle Match AnyOf and AllOf specially
        */
        String retVal = "<AttributeDesignator DataType=\"" + getType() + "\" "
                + "AttributeId=\"" + attributeID + "\" "
                + "Category=\"" + category + "\" "
                + "MustBePresent=\"" + mustExist + "\" >" ;

        return retVal;
    }



}
