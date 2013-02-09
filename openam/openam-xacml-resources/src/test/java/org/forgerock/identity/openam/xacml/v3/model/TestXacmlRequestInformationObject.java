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
package org.forgerock.identity.openam.xacml.model;

import org.forgerock.identity.openam.xacml.commons.ContentType;
import org.forgerock.identity.openam.xacml.services.XacmlContentHandlerService;
import org.junit.runner.RunWith;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Enumeration;

import static org.testng.Assert.*;


/**
 * XACML Request Information Object Tests.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlRequestInformationObject {

    /**
     * Test Request Information.
     */
    private static final String testRequestURI = "openam/xacml/pdp";
    private static final String testQueryMetaAlias = "meta:{aaaa}";
    private static final String testPDPEntityID = "fabdef";
    private static final String testRealm = "testRealm";

    /**
     * Test XACMLAuthzDecisionQuery Inner Class Tests.
     */
    private static final String testID = "ID_1e469be0-ecc4-11da-8ad9-0800200c9a66";
    private static final String testConsent = "/openam/xacml/pdp/consent/ID_1e469be0-ecc4-11da-8ad9-0800200c9a66/";
    private static final String testIssueInstant = "2001-12-17T09:30:47.0Z";
    private static final String testDestination = "/openam/xacml/pdp/consent/ID_1e469be0-ecc4-11da-8ad9-0800200c9a66/";
    private static final String testVersion = "2.0";


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    @Test
    public void testUseCase_SettingAttributesInInnerClass() {

        XACMLRequestInformation xacmlRequestInformation = new XACMLRequestInformation(ContentType.XACML_PLUS_XML,
                testRequestURI,
                testQueryMetaAlias, testPDPEntityID,
                testRealm);

        assertNotNull(xacmlRequestInformation);
        assertNotNull(xacmlRequestInformation.getXacmlAuthzDecisionQuery());
        assertTrue(xacmlRequestInformation.getXacmlAuthzDecisionQuery().setByName("id",testID));
        assertTrue(xacmlRequestInformation.getXacmlAuthzDecisionQuery().setByName("issueinstant",testIssueInstant));
        assertTrue(xacmlRequestInformation.getXacmlAuthzDecisionQuery().setByName("consent",testConsent));
        assertTrue(xacmlRequestInformation.getXacmlAuthzDecisionQuery().setByName("destination",testDestination));
        assertTrue(xacmlRequestInformation.getXacmlAuthzDecisionQuery().setByName("version",testVersion));

        assertEquals(xacmlRequestInformation.getXacmlAuthzDecisionQuery().getId(),testID);
        assertEquals(xacmlRequestInformation.getXacmlAuthzDecisionQuery().getIssueInstant(),testIssueInstant);
        assertEquals(xacmlRequestInformation.getXacmlAuthzDecisionQuery().getConsent(),testConsent);
        assertEquals(xacmlRequestInformation.getXacmlAuthzDecisionQuery().getDestination(),testDestination);
        assertEquals(xacmlRequestInformation.getXacmlAuthzDecisionQuery().getVersion(),testVersion);

    }

}
