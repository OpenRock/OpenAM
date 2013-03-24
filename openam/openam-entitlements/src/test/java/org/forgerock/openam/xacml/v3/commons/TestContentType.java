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
package org.forgerock.openam.xacml.v3.commons;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;

import static org.testng.Assert.*;

/**
 * Very simple test to check parsing and validation of URNs.
 *
 * @author jeff.schenk@forgerock.com
 */
public class TestContentType {

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    @Test
    public void testContentType() {
        assertEquals(ContentType.JSON_HOME.toString(),ContentType.JSON_HOME.name());
        assertEquals(ContentType.JSON.getApplicationType(), MediaType.APPLICATION_JSON);
        assertNotEquals(ContentType.JSON.getApplicationType(), ContentType.JSON_HOME.getApplicationType());
        assertEquals("application/xacml+xml",ContentType.XACML_PLUS_XML.getApplicationType());
        assertEquals("application/xml",ContentType.XML.getApplicationType());
        assertEquals(MediaType.APPLICATION_XML.toString(),ContentType.XML.getApplicationType());
    }

    @Test
    public void testLookup() {
        ContentType requestContentType =
                ContentType.getNormalizedContentType(ContentType.JSON_HOME.getApplicationType()+"; charset=UTF-8");
        assertEquals(ContentType.JSON_HOME.getApplicationType(), requestContentType.getApplicationType());

        requestContentType =
                ContentType.getNormalizedContentType(ContentType.XACML_PLUS_XML.getApplicationType()+"; charset=UTF-8");
        assertEquals(ContentType.XACML_PLUS_XML.getApplicationType(), requestContentType.getApplicationType());

        requestContentType =
                ContentType.getNormalizedContentType(ContentType.XACML_PLUS_JSON.getApplicationType()+"; charset=UTF-8");
        assertEquals(ContentType.XACML_PLUS_JSON.getApplicationType(), requestContentType.getApplicationType());

    }


}
