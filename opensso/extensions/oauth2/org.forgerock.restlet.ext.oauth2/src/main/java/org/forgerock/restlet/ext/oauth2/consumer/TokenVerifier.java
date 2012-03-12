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
package org.forgerock.restlet.ext.oauth2.consumer;

import org.forgerock.restlet.ext.oauth2.OAuthProblemException;

import java.util.Map;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public interface TokenVerifier {

    /*
    @param parameters
    {
        "access_token":"1/fFBGRNJru1FQd44AzqT3Zg"
    }

    @return
    {
        "audience":"8819981768.apps.googleusercontent.com",
        "user_id":"123456789",
        "scope":"https://gdata.youtube.com",
        "expires_in":436
    }
     */
    public OAuth2User verify(Map<String, Object> parameters) throws OAuthProblemException;
}
