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
package org.forgerock.openam.forgerockrest.dispatcher;
import static org.testng.Assert.*;

import org.junit.runner.RunWith;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.forgerock.openam.forgerockrest.RestDispatcher;
import org.forgerock.json.resource.servlet.HttpServlet;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Enumeration;


/**
 * Forgerock-Rest Test Suite
 *
 * @author alin.brici@forgerock.com
 */
public class RestDispatcherTest {
    private static ServletTester servletTester;

    private final static String resourceName = "/subrealm/realms/newRealm";

    @BeforeClass
    public void before() throws Exception {

        servletTester = new ServletTester();
        servletTester.addServlet(HttpServlet.class, "/json");
        servletTester.start();
    }

    @AfterClass
    public void after() throws Exception {
        servletTester.stop();
    }

    @Test
    public void testGetUser() {

        String hold = null;
        HttpTester request = new HttpTester();
        request.setMethod("GET");
        request.addHeader("Host", "example.org");
        request.setURI("/json/users/demo");
        request.setVersion("HTTP/1.1");

        try {
            // Check for a 411 No Content Length Provided.
            HttpTester response = new HttpTester();
            hold = response.parse(servletTester.getResponses(request.generate()));

            //assertEquals(response.getStatus(),404);
        } catch (IOException ioe) {

        } catch (Exception e) {

        }
    }
}
