package org.forgerock.openam.forgerockrest.dispatcher;

/**
 * Copyright 2013 ForgeRock, Inc.
 * <p/>
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 * <p/>
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 * <p/>
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 */

import com.iplanet.sso.SSOToken;
import com.sun.identity.sm.OrganizationConfigManager;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.openam.forgerockrest.OrganizationConfigManagerFactory;
import org.forgerock.openam.forgerockrest.RequestDetailsParser;
import org.testng.annotations.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Alin:
 * Given, When, Then are the three concepts introduced in http://en.wikipedia.org/wiki/Behavior-driven_development
 * Essentially each test should read like a sentence. Given some setup stuff, when I make this call, then this happens.
 * When should always strive to be one line.
 *
 * As you can see, these kinds of requirements place specific demands on the code being tested. It has be to very
 * open and clear as to what it is doing. Its dependencies have to be public and clear, so that both the test code
 * and the users of the code know what they are dealing with.
 *
 * See Mockito's page for good documentation: http://code.google.com/p/mockito/
 * http://docs.mockito.googlecode.com/hg/latest/org/mockito/Mockito.html
 */
public class RequestDetailsParserTest {
    @Test
    public void shouldParseBasicResource() throws NotFoundException {
        // Given
        String resourcePath = "some/test/path";
        SSOToken mockToken = mock(SSOToken.class);

        // Alin: Example of mocking something and it returning a useful value.
        // You may need numerous 'given' lines to build up the correct behaviour for the test.
        OrganizationConfigManager manager = mock(OrganizationConfigManager.class);
        given(manager.getOrganizationName()).willReturn("test details");

        OrganizationConfigManagerFactory mockFactory = mock(OrganizationConfigManagerFactory.class);

        RequestDetailsParser parser = new RequestDetailsParser(mockFactory);

        // When
        parser.parseRequest(mockToken, resourcePath);

        // Then
        verify(manager, times(1)).getOrganizationName();
    }
}
