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

import com.sun.identity.entitlement.xacml3.core.Attribute;
import com.sun.identity.entitlement.xacml3.core.AttributeValue;
import com.sun.identity.entitlement.xacml3.core.Attributes;
import com.sun.identity.entitlement.xacml3.core.Request;
import org.joda.time.DateTime;

import java.util.*;

public class XACML3Request {
    private Map<String,Map<String,DataBag>> requestData;
    private Set<String> resources;
    private boolean returnPolicyIDList;
    private boolean combinedDecision;
    private String  XACML3RequestContextID  = DateTime.now().toString();

    public XACML3Request(Request request) {
        requestData = new HashMap<String, Map<String, DataBag>>();
        resources = new HashSet<String>();
        combinedDecision = request.isCombinedDecision();
        returnPolicyIDList = request.isReturnPolicyIdList();

        List<Attributes> categories = request.getAttributes();
        for (Attributes c : categories) {
            String cat =  c.getCategory();
            boolean isResource = cat.contains(":resource");
            List<Attribute> attribs = c.getAttribute();
            Map<String,DataBag> catMap = new HashMap<String, DataBag>();

            for (Attribute a : attribs) {
                String attID = a.getAttributeId();
                List<AttributeValue> vals = a.getAttributeValue();
                DataBag bag = new DataBag();
                for (AttributeValue v : vals) {
                    DataValue dv = new DataValue(v.getDataType(),(String)v.getContent().get(0));
                    dv.setIncludeInResult(a.isIncludeInResult());
                    try {
                        bag.add(dv);
                    } catch(XACML3EntitlementException xee) {
                        // TODO :: Show Error Message...
                    }
                }
                catMap.put(attID,bag);
                if (isResource) {
                    resources.add(attID);
                }
            }
            requestData.put(cat,catMap);
        }
    }
    public Set<String> getResources() {
        return resources;
    }

    public DataBag getReqData(DataDesignator designator) {
        DataBag bag = null;
        Map cData = requestData.get(designator.getCategory());
        if (cData != null) {
             bag = (DataBag)cData.get(designator.getAttributeID());
        }
        return bag;
    }

    public String getContextID() {
        return XACML3RequestContextID;
    }
}
