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

import static org.testng.Assert.*;

/**
 * Very simple test to check parsing and validation of URNs.
 *
 * @author jeff.schenk@forgerock.com
 */
public class TestURNValidation {

    /**
     * RESTful XACML 3.0 Name Space Definitions.
     */
    static final String URN_HTTP = "urn:oasis:names:tc:xacml:3.0:profile:rest:http";
    static final String URN_HOME = "urn:oasis:names:tc:xacml:3.0:profile:rest:home";
    static final String URN_PDP = "urn:oasis:names:tc:xacml:3.0:profile:rest:pdp";

    /**
     * Normative Source: GET on the home location MUST return status code 200
     *
     * Target: Response to GET request on the home location
     *
     * Predicate: The HTTP status code in the [response] is 200
     *
     * Prescription Level: mandatory
     */
    static final String URN_HOME_STATUS = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:status";
    static final String URN_HOME_BODY = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:body";


    /**
     * A RESTful XACML system MUST have a single entry point at a known location
     * Each implementation of this profile MUST document the location of the entry point
     */
    static final String URN_ENTRY_POINT = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:documentation";


    public final static String[] testURNs = {URN_HTTP,
            URN_HOME,
            URN_PDP,
            URN_HOME_BODY,
            URN_HOME_STATUS,
            URN_ENTRY_POINT
    };

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    @Test
    public void testURNValidation() {

        for (String string : testURNs) {
            URN urn = new URN(string);
            assertTrue(urn.isValid());
        }

    }


}
