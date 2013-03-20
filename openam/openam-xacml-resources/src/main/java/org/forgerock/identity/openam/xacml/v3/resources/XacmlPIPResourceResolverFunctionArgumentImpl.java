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


import com.sun.identity.entitlement.xacml3.core.Response;
import com.sun.identity.entitlement.xacml3.core.Result;
import org.forgerock.identity.openam.xacml.v3.model.XACMLRequestInformation;
import org.forgerock.openam.xacml.v3.Entitlements.*;

import java.util.*;

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
public class XacmlPIPResourceResolverFunctionArgumentImpl implements XacmlPIPResourceResolver,
        XACML3EvalContextInterface {

    private Map<XacmlPIPResourceIdentifier, FunctionArgument> resourceResolutionMap;
    private  XACMLRequestInformation parent;

    /**
     * Default Constructor.
     */
     XacmlPIPResourceResolverFunctionArgumentImpl() {
        this.clear();
    }

    public XacmlPIPResourceResolverFunctionArgumentImpl(XACMLRequestInformation parent) {
        this.clear();
        this.parent = parent;
    }

    /**
     * Put a new instance of a FunctionArgument based upon Category and Attribute ID, which
     * have been parsed upstream.
     *
     * @param category
     * @param attributeId
     * @return
     */
    public boolean put(String category, String attributeId, String dataType, Object value,
                       boolean includeInResult) {
        if (this.resourceResolutionMap == null) {
            this.clear();
        }
        XacmlPIPResourceIdentifier xacmlPIPResourceIdentifier =
                new XacmlPIPResourceIdentifier(category, attributeId, includeInResult);
        if (dataType == null) {
            dataType = DataType.XACMLUNDEFINED;
        }
        // ******************************************
        // Depending upon certain Object Value Types,
        // we may need to Normalize it's values.
        //
        // Process: XACMLDATE, XACMLDATETIME, XACMLTIME, XACMLDAYTIMEDURATION, XACMLYEARMONTHDURATION
        //
        if ( (dataType.equalsIgnoreCase(DataType.XACMLDATE)) ||
             (dataType.equalsIgnoreCase(DataType.XACMLDATETIME)) ||
             (dataType.equalsIgnoreCase(DataType.XACMLTIME)) ||
             (dataType.equalsIgnoreCase(DataType.XACMLDAYTIMEDURATION)) ||
             (dataType.equalsIgnoreCase(DataType.XACMLYEARMONTHDURATION)) )
        {
            // Determine Actual Value Type from the Parse.
            Long normalizedValue = new Long(0);
            if (value instanceof String) {



            } else if (value instanceof Calendar) {
                normalizedValue = ((Calendar) value).getTimeInMillis();
            } else if (value instanceof Date) {
                 normalizedValue = ((Date) value).getTime();
            } else if (value instanceof Long) {
                 normalizedValue = new Long( ((Long) value).longValue());
            }
            // Set the Normalized Override.
            value = normalizedValue;
        }
        // ******************************************
        // Add Entry to Resource Resolution Map.
        this.resourceResolutionMap.put(xacmlPIPResourceIdentifier,  new DataValue(dataType, value, true));
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
        if (this.resourceResolutionMap == null) {
            return true;
        }
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
        if (this.resourceResolutionMap == null) {
            return null;
        }
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

    /**
     * Provide the Size of our Resource Map at Top Level.
     * @return
     */
    public int size() {
        if (this.resourceResolutionMap == null) {
            return 0;
        } else {
            return this.resourceResolutionMap.size();
        }
    }

    /**
     * Obtain all Resource Names
     *
     * @return Set<String>
     */
    public Set<String>  getResourceNames() {

        Set<String> retVal = new HashSet<String>();
        Set<XacmlPIPResourceIdentifier> keys = resourceResolutionMap.keySet();
        for (XacmlPIPResourceIdentifier it : keys){
            String category = it.getCategory();
            if (category.contains(":resource")){
                retVal.add(it.getAttributeId());
            }

        }
        return retVal;
    }

    /**
     * Obtain Request Result List.
     *
     * @return
     */
    public void addResult( Result res ) {
        Response resp = parent.getXacmlResponse();
        List<Result> resList = resp.getResult();
        resList.add(res);
        parent.setXacmlResponse(resp);
    }

    /**
     * Provide the String Equivalent of Object in String form for Debugging/Logging.
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(XacmlPIPResourceIdentifier key : this.resourceResolutionMap.keySet()) {
            FunctionArgument functionArgument = this.resourceResolutionMap.get(key);
            sb.append("Category: "+key.getCategory()+", Attribute Id: "+key.getAttributeId()+", " +
                    "Included In Result: "+key.isIncludeInResult()+"\n");
            sb.append("    Type: "+functionArgument.getType()+", Value: "+functionArgument.toString());
            sb.append("\n");
        }
        // return String representation of our Internal Map Object.
        return sb.toString();
    }

}
