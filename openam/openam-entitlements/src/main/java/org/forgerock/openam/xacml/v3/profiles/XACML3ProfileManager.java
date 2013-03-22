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
package org.forgerock.openam.xacml.v3.profiles;


import org.forgerock.openam.xacml.v3.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/*
    This class manages the supported profiles, and does the dispatching based on
    Category type.
 */
public class XACML3ProfileManager {
    private Map<String,XACML3AttributeHandler> handlers;
    private static XACML3ProfileManager instance;

    private  XACML3ProfileManager() {
        handlers = new HashMap<String,XACML3AttributeHandler>();
        ServiceLoader<XACML3AttributeHandler> profiles = ServiceLoader.load(
                XACML3AttributeHandler.class);
        for (XACML3AttributeHandler p : profiles) {
            List<String> cats = p.getProfileAttributes();
            for (String s : cats) {
                handlers.put(s, p);
            }
        }
    }

    public static XACML3ProfileManager getInstance() {
        if (instance == null) {
              instance = new XACML3ProfileManager();
        }
        return instance;
    }

    public DataBag resolve(DataDesignator designator,XACML3Request req) throws XACML3EntitlementException {

        DataBag bag = null;
        XACML3AttributeHandler handler = handlers.get(designator.getAttributeID());
        if (handler != null) {
             bag = handler.resolve(designator,req);
        } else {
            // Debug error that Attribute is Not Handled..
            // Fall back to just using the Request
             bag = req.getReqData(designator);
        }
        if ((bag == null) && designator.mustExist()) {
            throw new NotApplicableException("Required attribute not present");
        }
        return bag;
    };
}
