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

import org.forgerock.openam.xacml.v3.model.DataBag;
import org.forgerock.openam.xacml.v3.model.DataDesignator;
import org.forgerock.openam.xacml.v3.model.XACML3Request;

import java.util.ArrayList;
import java.util.List;

public class XACML3ECProfile implements XACML3AttributeHandler {
    private ArrayList<String> supportedAttributes;

    public XACML3ECProfile() {
        supportedAttributes = new ArrayList<String>();
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:resource:classification");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:resource:eccn");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:resource:usml");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:subject:nationality");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:subject:current-nationality");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:subject:location");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:subject:organization");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:ec-us:subject:us-person");

    }

    public List<String> getProfileAttributes() {
        return supportedAttributes;
    };

    // for now,  we just return the value in the request.
    // This should probably use the IDRepo for the subject attributes

    public DataBag resolve(DataDesignator designator, XACML3Request req) {
        return req.getReqData(designator);
    };

}
