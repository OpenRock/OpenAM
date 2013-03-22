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
package org.forgerock.openam.xacml.v3.rest;


import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.*;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import java.util.HashMap;
import java.util.Map;

@Provider
@Produces({"application/xml","application/json"})
public class XACML3ContextResolver implements ContextResolver<JAXBContext> {
    private final static String ENTITY_PACKAGE = "com.sun.identity.entitlement.xacml3.core";
    private final static JAXBContext context;
    static {
        try {
            Map<String,String> nameSpace = new HashMap<String,String>();
            nameSpace.put("urn:oasis:names:tc:xacml:3.0:core:schema:wd-17","");
            context = new JSONJAXBContext( JSONConfiguration.mappedJettison()
                                                .xml2JsonNs(nameSpace)
                                                .build(), ENTITY_PACKAGE);
        } catch (final JAXBException ex) {
            throw new IllegalStateException("Could not resolve JAXBContext.", ex);
        }
    }

    public JAXBContext getContext(final Class<?> type) {
        try {
            if (type.getPackage().getName().contains(ENTITY_PACKAGE)) {
                return context;
            }
        } catch (final Exception ex) {
            // trap, just return null
        }
        return null;
    }
}