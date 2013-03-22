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

public class XACML3CoreProfile implements XACML3AttributeHandler {
    private ArrayList<String> supportedAttributes;

    public XACML3CoreProfile() {
        supportedAttributes = new ArrayList<String>();
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:environment:current-time");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:environment:current-date");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:subject:subject-id-qualifier");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:subject:key-info");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:subject:authentication-time");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:subject:authentication-method");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:subject:request-time");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:subject:session-start-time");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:subject:authn-locality:ip-address");
        supportedAttributes.add("urn:oasis:names:tc:xacml:3.0:subject:authn-locality:dns-name");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
        supportedAttributes.add("urn:oasis:names:tc:xacml:2.0:resource:target-namespace");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:action:action-id");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:action:implied-action");
        supportedAttributes.add("urn:oasis:names:tc:xacml:1.0:action:action-namespace");
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
