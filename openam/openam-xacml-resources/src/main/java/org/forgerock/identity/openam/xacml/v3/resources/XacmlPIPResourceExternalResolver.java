/**
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
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
package org.forgerock.identity.openam.xacml.v3.resources;

/**
 * XACML PIP External Resource Interface.
 * <p/>
 * Policy Information Point (PIP)
 *
 * The system entity that acts as a source of various Attribute Values.
 *
 * Depending upon the Implementation will provide where Policy Information is obtained.
 *
 * @author Jeff.Schenk@forgerock.com
 */
public interface XacmlPIPResourceExternalResolver extends XacmlPIPResource {


    /**
     * Resolves Object based Upon a PIP Resource Query.
     *
     * @return Object - Resolved Object based Upon a PIP Resource Query.
     */
    public Object resolveExternalResource(Object query);


}
