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

import org.forgerock.identity.openam.xacml.v3.Entitlements.FunctionArgument;

import java.util.HashMap;
import java.util.Map;

/**
 * XACML PIP Resource In-Memory Resource Bucket Service.
 * <p/>
 * Policy Information Point (PIP)
 *
 * The system entity that acts as a source of various Attribute Values.
 *
 * This Implementation will provide and In-Memory Concrete PIP Object.
 *
 * @author Jeff.Schenk@forgerock.com
 */
public class XacmlPIPResourceResolverFunctionArgumentImpl implements XacmlPIPResource {

    private Map<XacmlPIPResourceIdentifier, FunctionArgument> resourceResolutionMap;

    /**
     * Default Constructor.
     */
    public XacmlPIPResourceResolverFunctionArgumentImpl() {
        this.clear();
    }

    /**
     * Put a new instance of a FunctionArgument based upon Category and Attribute ID, which
     * have been parsed upstream.
     *
     * @param category
     * @param attributeId
     * @param functionArgument
     * @return
     */
    public boolean put(String category, String attributeId, String dataType, String value,
                       boolean includeInResult, FunctionArgument functionArgument) {
        XacmlPIPResourceIdentifier xacmlPIPResourceIdentifier =
                new XacmlPIPResourceIdentifier(category, attributeId, dataType, value, includeInResult);
        this.resourceResolutionMap.put(xacmlPIPResourceIdentifier, functionArgument);
        return true;
    }

    /**
     * Remove an instance of a FunctionArgument based upon Category and Attribute ID.
     *
     * @param category
     * @param attributeId
     * @return
     */
    public boolean remove(String category, String attributeId) {
        XacmlPIPResourceIdentifier xacmlPIPResourceIdentifier =
                new XacmlPIPResourceIdentifier(category, attributeId);
        this.resourceResolutionMap.remove(xacmlPIPResourceIdentifier);
        return true;
    }

    /**
     * Resolve a Policy Resource Request Function Argument by using the Category and Attribute ID.
     *
     * @param category
     * @param attributeId
     * @return
     */
    public FunctionArgument resolve(String category, String attributeId) {
        XacmlPIPResourceIdentifier xacmlPIPResourceIdentifier =
                new XacmlPIPResourceIdentifier(category, attributeId);
        return this.resourceResolutionMap.get(xacmlPIPResourceIdentifier);
    }

    /**
     * Clear out the Entire Map.
     */
    public void clear() {
        this.resourceResolutionMap = new HashMap<XacmlPIPResourceIdentifier, FunctionArgument>();
    }
}
