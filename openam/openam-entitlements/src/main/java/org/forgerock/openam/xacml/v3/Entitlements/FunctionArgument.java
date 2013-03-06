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


import com.sun.identity.entitlement.Entitlement;
import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.shared.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import sun.tools.tree.NewArrayExpression;

/*
   This class is the parent class for Function Arguments.
   It has three subclasses.
       DataValue for known values
       DataDesignator for values to fetch at runtime
       XACMLFunction as a parent class for all functions

*/
public abstract class FunctionArgument {
    public static FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN,"true");
    public static FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN,"false");
    public static FunctionArgument indeterminateObject = new IndeterminateValue();
    public static FunctionArgument notApplicableObject = new NotApplicableValue();

    private String dataType;
    private String issuer ;

    public void setType(String type) {
        this.dataType = type;
    }
    public String getType() {
        return dataType;
    }
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
    public String getIssuer() {
        return issuer;
    }
    public boolean isTrue() {
        if (this instanceof DataValue) {
            if (this.getType().equals(DataType.XACMLBOOLEAN) && this.getValue(null).equals("true")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public boolean isFalse() {
        if (this instanceof DataValue) {
            if (this.getType().equals(DataType.XACMLBOOLEAN) && this.getValue(null).equals("false")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public boolean isIndeterminate() {
        if (this == FunctionArgument.indeterminateObject) {
            return true ;
        } else {
            return false;
        }
    }
    public boolean isNotApplicable() {
        if (this == FunctionArgument.notApplicableObject) {
            return true;
        } else {
            return false;
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put("className", getClass().getName());
        jo.put("dataType",dataType);
        jo.put("issuer",issuer);
        return jo;
    }

    protected void init(JSONObject jo) throws JSONException {
        this.dataType = jo.optString("dataType");
        this.issuer = jo.optString("issuer");
        return;
    };

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
    };
    public abstract FunctionArgument evaluate(XACMLEvalContext pip);
    public abstract Object getValue(XACMLEvalContext pip);
}
