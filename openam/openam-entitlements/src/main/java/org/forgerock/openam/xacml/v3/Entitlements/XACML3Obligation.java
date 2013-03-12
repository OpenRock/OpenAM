
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

import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.entitlement.xacml3.core.*;
import com.sun.identity.entitlement.xacml3.core.ObligationExpression;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class XACML3Obligation {

    private String obligationID;
    private String fullFillOn;
    private List<DataAssignment> assignments;

    public XACML3Obligation(ObligationExpression obligation) {
        obligationID = obligation.getObligationId();
        fullFillOn = obligation.getFulfillOn().value();
        assignments = new ArrayList<DataAssignment>();

        for (AttributeAssignmentExpression ex : obligation.getAttributeAssignmentExpression()) {
            assignments.add(new DataAssignment(ex));
        }
    }

    public XACML3Obligation() {
    }


    public XACML3Decision evaluate(XACMLEvalContext pip) {
        return null;
    }


    public JSONObject toJSONObject() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("classname",this.getClass().getName());
        jo.put("obligationID", obligationID);
        jo.put("fullFillOn", fullFillOn);

        JSONObject dassigns = new JSONObject();
        for (DataAssignment da : assignments) {
            jo.append("assignments", da.toJSONObject());
        }
        return jo;
    }

    public void init(JSONObject jo) throws JSONException {


        JSONObject dassigns = new JSONObject();
        for (DataAssignment da : assignments) {
            jo.append("assignments",da.toJSONObject());
        }

        obligationID = jo.optString("obligationID");
        fullFillOn = jo.optString("fullFillOn");

        assignments = new ArrayList<DataAssignment>() ;

        JSONArray array = jo.getJSONArray("assignments");
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = (JSONObject)array.get(i);
            assignments.add((DataAssignment)FunctionArgument.getInstance(json));
        }
    }

    static public XACML3Obligation getInstance(JSONObject jo)  {
        String className = jo.optString("classname");
        try {
            Class clazz = Class.forName(className);
            XACML3Obligation farg = (XACML3Obligation)clazz.newInstance();
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

    public ObligationExpression getXACML(XACMLEvalContext pip) {
        ObligationExpression ret = new ObligationExpression();
        ret.setFulfillOn(EffectType.fromValue(fullFillOn));
        ret.setObligationId(obligationID);
        List<AttributeAssignmentExpression> exp = ret.getAttributeAssignmentExpression();

        for( DataAssignment d : assignments) {
            exp.add(d.getXACML(pip));
        }
        return ret;
    }
}
