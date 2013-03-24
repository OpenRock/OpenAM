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

import org.forgerock.openam.xacml.v3.commons.ContentType;
import org.forgerock.identity.openam.xacml.v3.commons.JsonToMapUtility;
import org.forgerock.identity.openam.xacml.v3.commons.XACML3Utils;
import org.forgerock.identity.openam.xacml.v3.commons.XmlToMapUtility;
import org.forgerock.identity.openam.xacml.v3.model.XACMLRequestInformation;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * XACML Collection Query using JoSQL.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlCollectionQuery {
    private final static String testSOAPEnvelope_ResourceName = "test_data/request-curtiss.xml";
    private final static String testJSON_ResourceName = "test_data/request-curtiss.json";

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    @Test
    public void testUseCase_Building_PIP_MAP_FROM_XML_CONTENT_AND_QUERY_COLLECTION() {

        String testData = XACML3Utils.getResourceContents(testSOAPEnvelope_ResourceName);
        assertNotNull(testData);
        XACMLRequestInformation xacmlRequestInformation
                = new XACMLRequestInformation(ContentType.XML);
        xacmlRequestInformation.setOriginalContent(testData);

        try {
            // The Original Content will be UnMarshaled into a Map Object stored in XACMLRequestInformation Content.
            xacmlRequestInformation.setContent(XmlToMapUtility.fromString(xacmlRequestInformation.getOriginalContent()));
            xacmlRequestInformation.setParsedCorrectly(true);
        } catch (Exception exception) {
            xacmlRequestInformation.setContent(null);
            xacmlRequestInformation.setParsedCorrectly(false);
        }

        assertTrue(xacmlRequestInformation.isParsedCorrectly());
        assertNotNull(xacmlRequestInformation.getContent());

        // Now perform Test Builder functions
        assertTrue(XacmlPIPResourceBuilder.buildXacmlPIPResourceForRequests(xacmlRequestInformation));
        assertEquals(xacmlRequestInformation.getPipResourceResolver().size(), 19);
        assertTrue(xacmlRequestInformation.isRequestNodePresent());

        System.out.println("\nXML Content Result:\n\n" + xacmlRequestInformation);

        System.out.flush();

    }

    @Test
    public void testUseCase_Building_PIP_MAP_FROM_JSON_CONTENT_AND_QUERY_COLLECTION() {

        String testData = XACML3Utils.getResourceContents(testJSON_ResourceName);
        assertNotNull(testData);
        XACMLRequestInformation xacmlRequestInformation
                = new XACMLRequestInformation(ContentType.JSON);
        xacmlRequestInformation.setOriginalContent(testData);

        try {
            // The Original Content will be UnMarshaled into a Map Object stored in XACMLRequestInformation Content.
            xacmlRequestInformation.setContent(JsonToMapUtility.fromString(xacmlRequestInformation.getOriginalContent()
            ));
            xacmlRequestInformation.setParsedCorrectly(true);
        } catch (Exception exception) {
            xacmlRequestInformation.setContent(null);
            xacmlRequestInformation.setParsedCorrectly(false);
        }

        assertTrue(xacmlRequestInformation.isParsedCorrectly());
        assertNotNull(xacmlRequestInformation.getContent());

        // Now perform Test Builder functions
        assertTrue(XacmlPIPResourceBuilder.buildXacmlPIPResourceForRequests(xacmlRequestInformation));
        assertEquals(xacmlRequestInformation.getPipResourceResolver().size(), 19);
        assertTrue(xacmlRequestInformation.isRequestNodePresent());

        System.out.println("\nJSON Content Result:\n\n" + xacmlRequestInformation);

        System.out.flush();

    }

}
