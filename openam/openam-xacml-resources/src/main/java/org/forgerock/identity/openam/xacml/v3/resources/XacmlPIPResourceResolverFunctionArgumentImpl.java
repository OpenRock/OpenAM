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
import org.forgerock.openam.xacml.v3.model.*;

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
        // Normalize these type Values either to a Long Object or Date Object whichever is applicable.
        //
        if (dataType.equalsIgnoreCase(DataType.XACMLDATE)) {
            value = this.normalizeDateTimeValuesToDate(XACML3PrivilegeUtils.YEAR_MONTH_DAY, value);
        } else if (dataType.equalsIgnoreCase(DataType.XACMLDATETIME)) {
            value = this.normalizeDateTimeValuesToDate(XACML3PrivilegeUtils
                    .YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLISECONDS, value);
        } else if (dataType.equalsIgnoreCase(DataType.XACMLTIME)) {
            value = this.normalizeDateTimeValuesToDate(XACML3PrivilegeUtils.HOUR_MINUTE_SECOND_MILLISECONDS, value);
        } else if (dataType.equalsIgnoreCase(DataType.XACMLDAYTIMEDURATION)) {
            value = this.normalizeDateTimeValuesToLong(XACML3PrivilegeUtils
                    .YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_MILLISECONDS, value);
        } else if (dataType.equalsIgnoreCase(DataType.XACMLYEARMONTHDURATION)) {
            value = this.normalizeDateTimeValuesToLong(XACML3PrivilegeUtils.YEAR_MONTH, value);
        } // End of Check for Date and Time related Data Values for Normalization.

         // ***********************************************************************
       //// Add any additional Normalization requirements here.
       //// TODO :: This could be a plug-in, as to what is normalized and how.
         // ***********************************************************************

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

    /**
     * Normalize Date and Time Values for future usage, into a Date Object.
     * @param formatPattern - Format Pattern in String Form, if the value is a String instance.
     * @param value - Value of DataType, which will be normalized to a Long.
     * @return Long - Normalization value.
     */
    private Date normalizeDateTimeValuesToDate(String formatPattern, Object value) {

        // Determine Actual Value Type from the Parse.
        if (value instanceof String) {
            return XACML3PrivilegeUtils.stringToDate((String)value, formatPattern);
        } else if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        } else if (value instanceof Date) {
            return (Date)value;
        } else if (value instanceof Long) {
            return new Date( ((Long) value).longValue() );
        } else {
            return null;
        }
    }

    /**
     * Normalize all Data and Time Values for future usage.
     * @param formatPattern - Format Pattern in String Form, if the value is a String instance.
     * @param value - Value of DataType, which will be normalized to a Long.
     * @return Long - Normalization value.
     */
    private Long normalizeDateTimeValuesToLong(String formatPattern, Object value) {

        // Determine Actual Value Type from the Parse.
        Long normalizedValue = null;
        if (value instanceof String) {
            Date dateValue = XACML3PrivilegeUtils.stringToDate((String)value, formatPattern);
            normalizedValue = new Long( dateValue.getTime() );
        } else if (value instanceof Calendar) {
            normalizedValue = new Long( ((Calendar) value).getTimeInMillis() );
        } else if (value instanceof Date) {
            normalizedValue = new Long( ((Date) value).getTime() );
        } else if (value instanceof Long) {
            normalizedValue = new Long( ((Long) value).longValue());
        }
        // Set the Normalized Override.
        return normalizedValue;
    }

}
