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

import org.forgerock.openam.xacml.v3.model.DataType;
import org.forgerock.openam.xacml.v3.model.DataValue;
import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A.3.10 Bag functions
 These functions operate on a bag of ‘type’ values, where type is one of the primitive data-types,
 and x.x is a version of XACML where the function has been defined.
 Some additional conditions defined for each function below SHALL cause the expression to evaluate to "Indeterminate".

 urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
 This function SHALL take a bag of ‘type’ values as an argument and SHALL return a value of ‘type’.
 It SHALL return the only value in the bag.  If the bag does not have one and only one value,
 then the expression SHALL evaluate to "Indeterminate".

 urn:oasis:names:tc:xacml:x.x:function:type-bag-size
 This function SHALL take a bag of ‘type’ values as an argument and SHALL return an
 “http://www.w3.org/2001/XMLSchema#integer” indicating the number of values in the bag.

 urn:oasis:names:tc:xacml:x.x:function:type-is-in
 This function SHALL take an argument of ‘type’ as the first argument and a bag of ‘type’ values as the second argument
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The function SHALL evaluate to "True" if and only if the first argument matches by the
 "urn:oasis:names:tc:xacml:x.x:function:type-equal" any value in the bag.  Otherwise, it SHALL return “False”.

 */

/**
 * XACML Bag Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlBagFunctionsGroup2 {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * One and Only
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     This function SHALL take a bag of ‘type’ values as an argument and SHALL return a value of ‘type’.
     It SHALL return the only value in the bag.  If the bag does not have one and only one value,
     then the expression SHALL evaluate to "Indeterminate".

     urn:oasis:names:tc:xacml:1.0:function:string-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:double-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:time-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:date-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only
     urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only
     urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only
     urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only
     urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only

     */

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_AnyuriOneAndOnly() throws XACML3EntitlementException {
                        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_Base64BinaryOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_BooleanOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_DateOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_DatetimeOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_DaytimedurationOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_DoubleOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_HexbinaryOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_IntegerOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_Rfc822NameOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_StringOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_TimeOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_X500NameOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     */
    @Test
    public void test_YearmonthdurationOneAndOnly() throws XACML3EntitlementException {
        // TODO :: Finish...
    }
    /**
     * Size Methods
     *  urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     This function SHALL take a bag of ‘type’ values as an argument and SHALL return an
     “http://www.w3.org/2001/XMLSchema#integer” indicating the number of values in the bag.

     urn:oasis:names:tc:xacml:1.0:function:string-bag-size
     urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size
     urn:oasis:names:tc:xacml:1.0:function:integer-bag-size
     urn:oasis:names:tc:xacml:1.0:function:double-bag-size
     urn:oasis:names:tc:xacml:1.0:function:time-bag-size
     urn:oasis:names:tc:xacml:1.0:function:date-bag-size
     urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size
     urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size
     urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size
     urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size
     urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size
     urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size
     urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size
     urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size
     urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size
     urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size


     */

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_AnyuriBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_Base64BinaryBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_BooleanBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_DateBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_DatetimeBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_DaytimedurationBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_DoubleBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_HexbinaryBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_IntegerBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_Rfc822NameBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_StringBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_TimeBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_X500NameBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     */
    @Test
    public void test_YearmonthdurationBagSize() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * Is In Methods
     *  urn:oasis:names:tc:xacml:x.x:function:type-is-in
     This function SHALL take an argument of ‘type’ as the first argument and a bag of ‘type’ values as the second argument
     and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
     The function SHALL evaluate to "True" if and only if the first argument matches by the
     "urn:oasis:names:tc:xacml:x.x:function:type-equal" any value in the bag.  Otherwise, it SHALL return “False”.

     urn:oasis:names:tc:xacml:1.0:function:string-is-in
     urn:oasis:names:tc:xacml:1.0:function:boolean-is-in
     urn:oasis:names:tc:xacml:1.0:function:integer-is-in
     urn:oasis:names:tc:xacml:1.0:function:double-is-in
     urn:oasis:names:tc:xacml:1.0:function:time-is-in
     urn:oasis:names:tc:xacml:1.0:function:date-is-in
     urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in
     urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in
     urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in
     urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in
     urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-is-in
     urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-is-in
     urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in
     urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in


     */

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_AnyuriIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_Base64BinaryIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_BooleanIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_DateIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_DatetimeIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_DaytimedurationIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_DoubleIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_HexbinaryIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_IntegerIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_Rfc822NameIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_StringIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_TimeIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_X500NameIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-is-in
     */
    @Test
    public void test_YearmonthdurationIsIn() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

}
