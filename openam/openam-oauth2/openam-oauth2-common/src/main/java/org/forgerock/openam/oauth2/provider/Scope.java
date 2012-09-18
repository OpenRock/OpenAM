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
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.forgerock.openam.oauth2.provider;


import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.forgerock.openam.oauth2.model.AccessToken;

import java.util.Map;

public interface Scope {

    /**
     * Init is called before any token processing is done. This method should be used to initialize any
     * data the class requires.
     */
    public void init();

    /**
     * This method is called after the token is processed and before it is returned to the user as authenticated. This
     * method should be used to implement your scope tasks.
     * @param token An AccessToken that contains all the information about the token
     * @param error This value will be null unless the token is expired. If the token being expired is of no interest to
     *              you set this to null.
     * @return returns a map of data to be added to the token json object that will be returned to the client.
     *          This map should contain a field called "authenticated" with a value of true or false depending on if the
     *          token has passed your policies.
     */
    public Map<String, Object> process(AccessToken token, OAuthProblemException error);

    /**
     * This method is called just before the tokeninfo endpoint is returns the tokeninfo.
     * Use this method to cleanup any variables left over in this class
     */
    public void destroy();
}
