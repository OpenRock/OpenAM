/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 * Copyright © 2011 Cybernetica AS.
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
 *
 */


package org.forgerock.openam.authentication.modules.oauth2;

interface OAuthParam {
    // openam parameters
    String PARAM_GOTO = "goto";
    String PARAM_REALM = "realm";
    String PARAM_MODULE = "module";

    // facebook parameters
    String PARAM_CODE = "code";
    String PARAM_REDIRECT_URI = "redirect_uri";
    String PARAM_SCOPE = "scope";
    String PARAM_CLIENT_SECRET = "client_secret";
    String PARAM_CLIENT_ID = "client_id";

    // custom parameters for OAuth module internal use
    String PARAM_EXPECT_RESPONSE = "expect_response";
    String PARAM_LOGIN_URL = "login_url";
}

