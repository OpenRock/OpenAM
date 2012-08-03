/*
 * DO NOT REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 ForgeRock Inc. All rights reserved.
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
 * "Portions Copyrighted [2012] [Forgerock Inc]"
 */

package org.forgerock.openam.oauth2.utils;

/**
 * Stores the constants nessesary for the OAuth2 implementation
 * @author Jason Lemay
 */
public class OAuth2Constants {

    public class OAuth2ProviderService{
        //service name and version
        public static final String NAME = "OAuth2Provider";
        public static final String VERSION = "1.0";

        //service config fields
        public static final String AUTHZ_CODE_LIFETIME_NAME = "forgerock-oauth2-provider-authorization-code-lifetime";
        public static final String REFRESH_TOKEN_LIFETIME_NAME = "forgerock-oauth2-provider-refresh-token-lifetime";
        public static final String ACCESS_TOKEN_LIFETIME_NAME = "forgerock-oauth2-provider-access-token-lifetime";
        public static final String ISSUE_REFRESH_TOKEN = "forgerock-oauth2-provider-issue-refresh-token";
    }

}
