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
package org.forgerock.restlet.ext.oauth2.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.forgerock.restlet.ext.oauth2.OAuth2;

import java.util.Set;

/**
 * @author $author$
 * @version $Revision$ $Date$
 * @see <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-24#section-10.10">10.10.  Credentials Guessing Attacks</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Token {
    /**
     * Get the string representation of the identifier of this token
     * <p/>
     * TODO Description
     *
     * @return unique identifier of the represented token
     */
    @JsonProperty(OAuth2.Params.ACCESS_TOKEN)
    public String getToken();

    @JsonIgnore
    public String getUserID();


    public String getRealm();


    @JsonIgnore
    public SessionClient getClient();

    @JsonIgnore
    public Set<String> getScope();

    /**
     * Get the exact expiration time in POSIX format
     *
     * @return long representation of the maximum valid date.
     */
    @JsonIgnore
    public long getExpireTime();

    public boolean isExpired();
}
