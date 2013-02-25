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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberMatcher.field;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;
import static org.testng.Assert.*;

import com.iplanet.sso.SSOToken;
import com.sun.identity.saml2.jaxb.metadata.SSODescriptorType;
import com.sun.identity.sm.*;
import org.forgerock.json.resource.*;
import org.forgerock.openam.forgerockrest.IdentityResource;
import org.forgerock.openam.forgerockrest.session.query.SessionQueryManager;
import org.junit.runner.RunWith;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.forgerock.openam.forgerockrest.RestDispatcher;
import org.forgerock.json.resource.servlet.HttpServlet;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Forgerock-Rest Test Suite
 *
 * @author alin.brici@forgerock.com
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RestDispatcher.class)
@SuppressStaticInitializationFor("com.sun.identity.sm.OrganizationConfigManager")

class OrgnazationConfigManagerTest extends OrganizationConfigManager{
    public OrgnazationConfigManagerTest(SSOToken token, String realm) throws SMSException {
        super(token, realm);
    }
}

public class RestDispatcherTest {
    private static ServletTester servletTester;

    private final static String resourceName = "/realm/subrealm/users/demo";

    @Test
    public void testGetRequestDetails() {
        //TODO Mock OrganizationConfigManger
        /*suppress(constructor(OrganizationConfigManager.class));
        Map<String, String> details = new HashMap<String, String>(3);
        Map<String, String> parsedDetails = new HashMap<String, String>(3);
        try {

            details.put("realmPath", "/realm/subrealm");
            details.put("resourceName", "/users");
            details.put("resourceId", "demo");
            OrganizationConfigManager ocm = Whitebox.newInstance(OrgnazationConfigManagerTest.class);
            whenNew(OrganizationConfigManager.class).withArguments(any(SSOToken.class), anyString()).thenReturn(ocm);
            RestDispatcher rD = RestDispatcher.getInstance();
            parsedDetails = Whitebox.invokeMethod(RestDispatcher.class, "getRequestDetails", resourceName);

            assertTrue(parsedDetails.equals(details));

        } catch (IOException ioe) {

        } catch (Exception e) {

        } */
    }
}
