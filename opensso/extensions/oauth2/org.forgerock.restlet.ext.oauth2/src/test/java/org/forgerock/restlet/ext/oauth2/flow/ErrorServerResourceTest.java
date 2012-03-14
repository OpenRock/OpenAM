/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2012 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * $Id$
 */
package org.forgerock.restlet.ext.oauth2.flow;

import org.forgerock.restlet.ext.oauth2.OAuth2Utils;
import org.restlet.Context;
import org.testng.annotations.Test;

import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class ErrorServerResourceTest {
    @Test
    public void testCheckedScope() throws Exception {
        Set<String> allowedGrantScopes = OAuth2Utils.split("read list write", OAuth2Utils.getScopeDelimiter(null));
        assertThat(allowedGrantScopes).hasSize(3).containsOnly("read", "list", "write");
        Context ctx = new Context();
        OAuth2Utils.setScopeDelimiter(",", ctx);
        Set<String> defaultGrantScopes = OAuth2Utils.split("read,list", OAuth2Utils.getScopeDelimiter(ctx));
        assertThat(defaultGrantScopes).hasSize(2).containsOnly("read", "list");

        ErrorServerResource testable = new ErrorServerResource();
        Set<String> checkedScope = testable.getCheckedScope(null, allowedGrantScopes, defaultGrantScopes);
        assertThat(checkedScope).isEqualTo(defaultGrantScopes);

        checkedScope = testable.getCheckedScope("read", allowedGrantScopes, defaultGrantScopes);
        assertThat(checkedScope).containsOnly("read");

        checkedScope = testable.getCheckedScope("read delete", allowedGrantScopes, defaultGrantScopes);
        assertThat(checkedScope).containsOnly("read");
    }
}
