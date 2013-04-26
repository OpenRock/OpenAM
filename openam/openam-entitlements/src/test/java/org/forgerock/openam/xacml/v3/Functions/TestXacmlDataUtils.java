/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock US. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.xacml.v3.Functions;

import org.forgerock.openam.xacml.v3.model.DataValue;
import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;

import java.util.ArrayList;
import java.util.List;

/**
 * XACML Test Data Utility Tools.
 *
 *
 * Defined Bags:
 *
 * urn:oasis:names:tc:xacml:1.0:function:string-bag
 * urn:oasis:names:tc:xacml:1.0:function:boolean-bag
 * urn:oasis:names:tc:xacml:1.0:function:integer-bag
 * urn:oasis:names:tc:xacml:1.0:function:double-bag
 *
 * urn:oasis:names:tc:xacml:1.0:function:time-bag
 * urn:oasis:names:tc:xacml:1.0:function:date-bag
 * urn:oasis:names:tc:xacml:1.0:function:dateTime-bag
 *
 * urn:oasis:names:tc:xacml:1.0:function:anyURI-bag
 * urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag
 * urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag
 * urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag
 * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag
 *
 * urn:oasis:names:tc:xacml:1.0:function:x500Name-bag
 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag
 * urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag
 * urn:oasis:names:tc:xacml:2.0:function:dnsName-bag
 *
 *
 * X500Name
 */
public class TestXacmlDataUtils {

    /**
     * Return Native Implementation of Collection Contents from a List<DataValue> representing a Bag.
     *
     * @param fArg
     * @return List<String>
     * @throws org.forgerock.openam.xacml.v3.model.XACML3EntitlementException
     */
    public static List<String> asStringCollection(FunctionArgument fArg) throws XACML3EntitlementException {
        // Loop to UnWrap the DataValues
        List<DataValue> bag = (List<DataValue>) fArg.getValue(null);   // Simple Cast for easy Manipulation.
        List<String> collection = new ArrayList<String>(bag.size());
        for (int i=0; i<bag.size(); i++) {
            // Cast and Add Object Element to Native Collection.
            collection.add( (String) bag.get(i).getValue(null) );
        }
        return collection;
    }

    /**
     * Return Native Implementation of Collection Contents from a List<DataValue> representing a Bag.
     *
     * @param fArg
     * @return List<Boolean>
     * @throws org.forgerock.openam.xacml.v3.model.XACML3EntitlementException
     */
    public static List<Boolean> asBooleanCollection(FunctionArgument fArg) throws XACML3EntitlementException {
        // Loop to UnWrap the DataValues
        List<DataValue> bag = (List<DataValue>) fArg.getValue(null);   // Simple Cast for easy Manipulation.
        List<Boolean> collection = new ArrayList<Boolean>(bag.size());
        for (int i=0; i<bag.size(); i++) {
            // Cast and Add Object Element to Native Collection.
            collection.add( (Boolean) bag.get(i).getValue(null) );
        }
        return collection;
    }

    /**
     * Return Native Implementation of Collection Contents from a List<DataValue> representing a Bag.
     *
     * @param fArg
     * @return List<Integer>
     * @throws org.forgerock.openam.xacml.v3.model.XACML3EntitlementException
     */
    public static List<Integer> asIntegerCollection(FunctionArgument fArg) throws XACML3EntitlementException {
        // Loop to UnWrap the DataValues
        List<DataValue> bag = (List<DataValue>) fArg.getValue(null);   // Simple Cast for easy Manipulation.
        List<Integer> collection = new ArrayList<Integer>(bag.size());
        for (int i=0; i<bag.size(); i++) {
            // Cast and Add Object Element to Native Collection.
            collection.add( (Integer) bag.get(i).getValue(null) );
        }
        return collection;
    }

    /**
     * Return Native Implementation of Collection Contents from a List<DataValue> representing a Bag.
     *
     * @param fArg
     * @return List<Integer>
     * @throws org.forgerock.openam.xacml.v3.model.XACML3EntitlementException
     */
    public static List<Double> asDoubleCollection(FunctionArgument fArg) throws XACML3EntitlementException {
        // Loop to UnWrap the DataValues
        List<DataValue> bag = (List<DataValue>) fArg.getValue(null);   // Simple Cast for easy Manipulation.
        List<Double> collection = new ArrayList<Double>(bag.size());
        for (int i=0; i<bag.size(); i++) {
            // Cast and Add Object Element to Native Collection.
            collection.add( (Double) bag.get(i).getValue(null) );
        }
        return collection;
    }

}
