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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 This class Encapsulates a DataValue from the XACML policy.
 In this case, we have the actual Data in the object

 @author Allan.Foster@forgerock.com

 */
public class DataBag extends FunctionArgument {
    /**
     * Data Value Object.
     */
    private List<DataValue> data = new ArrayList<DataValue>();

    /**
     * Default Constructor
     */
    public DataBag() {
        setType((DataType)null);
    }

    /**
     * Constructor used to specify the value represented by DataType.
     *
     * @param type - DataType Of the Bag.
     */
    public DataBag(final DataType.Type type) throws XACML3EntitlementException {
        setType(type.getTypeName());
    }

    /**
     * Constructor used to specify the value represented by String Data.
     *
     * @param value
     */
    public DataBag(DataValue value) throws XACML3EntitlementException {
        this.add(value);
    }

    public DataBag add(DataValue value) throws XACML3EntitlementException {
        if (getType() == null) {
            setType(value.getType());
        }
        if (getType().getIndex() != value.getType().getIndex()) {
            throw new XACML3EntitlementException("Unable to add wrong typed Element to Bag");
        }
        data.add(value);
        return this;
    }

    /**
     * Get DataValue within the Bag by it's Index Value.
     * @param index
     * @return DataValue -- Element within Bag.
     * @throws XACML3EntitlementException
     */
    public DataValue get(int index) throws XACML3EntitlementException {
        return data.get(index);
    }

    /**
     * Return the Size of the Data Collection
     * @return int - number of entries in Collection.
     */
    public int size() {
        return data.size();
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
    public List<DataValue> getValue(XACMLEvalContext pip) throws XACML3EntitlementException {
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
        for (DataValue arg : data ) {
            jo.append("data", arg.toJSONObject());
        }
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
        JSONArray array = jo.getJSONArray("data");
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = (JSONObject)array.get(i);
            data.add((DataValue)FunctionArgument.getInstance(json));
        }
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


}
