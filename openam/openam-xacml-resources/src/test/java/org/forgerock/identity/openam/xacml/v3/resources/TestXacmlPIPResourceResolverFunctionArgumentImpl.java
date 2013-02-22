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
package org.forgerock.identity.openam.xacml.v3.resources;

import org.forgerock.openam.xacml.v3.Entitlements.FunctionArgument;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


/**
 * XACML Request Information Object Tests.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlPIPResourceResolverFunctionArgumentImpl {

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    @Test
    public void testUseCase_ResolverManipulation() {

         // Test Constructors.
        XacmlPIPResourceResolverFunctionArgumentImpl resolver =
                            new XacmlPIPResourceResolverFunctionArgumentImpl();
        assertNotNull(resolver);
        assertNull(resolver.resolve("requestId", "category", "attributeId"));

        // Add an Entry
        resolver.put("1", "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",
                "urn:oasis:names:tc:xacml:3.0:ipc:subject:organization",
                "http://www.w3.org/2001/XMLSchema#string", "Curtiss1", true);

        // Check the Size.
        assertEquals(resolver.size(), 1);

        // Add an Entry
        resolver.put("2", "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",
                "urn:oasis:names:tc:xacml:3.0:ipc:subject:organization",
                "http://www.w3.org/2001/XMLSchema#string", "Curtiss2", true);

        // Check the Size.
        assertEquals(resolver.size(),2);

        // Now Find a FunctionArgument from our Resolver.
        FunctionArgument functionArgument = resolver.resolve("1", "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",
                "urn:oasis:names:tc:xacml:3.0:ipc:subject:organization");
        assertNotNull(functionArgument);
        // I am the PIP Resource Resolver,
        // so get actual Data Value.
        assertEquals("Curtiss1", functionArgument.getValue(null));



    }

}
