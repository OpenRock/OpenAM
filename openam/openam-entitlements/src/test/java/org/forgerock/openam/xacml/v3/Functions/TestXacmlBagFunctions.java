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

import org.forgerock.openam.xacml.v3.model.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * A.3.10 Bag functions
 These functions operate on a bag of ‘type’ values, where type is one of the primitive data-types,
 and x.x is a version of XACML where the function has been defined.
 Some additional conditions defined for each function below SHALL cause the expression to evaluate to "Indeterminate".


 urn:oasis:names:tc:xacml:x.x:function:type-bag

 urn:oasis:names:tc:xacml:1.0:function:string-bag
 urn:oasis:names:tc:xacml:1.0:function:boolean-bag
 urn:oasis:names:tc:xacml:1.0:function:integer-bag
 urn:oasis:names:tc:xacml:1.0:function:double-bag

 urn:oasis:names:tc:xacml:1.0:function:time-bag
 urn:oasis:names:tc:xacml:1.0:function:date-bag
 urn:oasis:names:tc:xacml:1.0:function:dateTime-bag

 urn:oasis:names:tc:xacml:1.0:function:anyURI-bag
 urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag
 urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag
 urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag
 urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag

 urn:oasis:names:tc:xacml:1.0:function:x500Name-bag
 urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag
 urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag
 urn:oasis:names:tc:xacml:2.0:function:dnsName-bag

 This function SHALL take any number of arguments of ‘type’ and return a bag of ‘type’ values containing
 the values of the arguments.  An application of this function to zero arguments SHALL produce
 an empty bag of the specified data-type.

 */

/**
 * XACML Bag Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * X500Name
 */
public class TestXacmlBagFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }


    /**
     *  urn:oasis:names:tc:xacml:1.0:function:anyURI-bag
     */
    @Test
    public void test_AnyuriBag() throws XACML3EntitlementException {
        AnyuriBag function = new AnyuriBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag
     */
    @Test
    public void test_Base64BinaryBag() throws XACML3EntitlementException {
        Base64BinaryBag function = new Base64BinaryBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:boolean-bag
     */
    @Test
    public void test_BooleanBag() throws XACML3EntitlementException {
        BooleanBag function = new BooleanBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);


    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:date-bag
     */
    @Test
    public void test_DateBag() throws XACML3EntitlementException {
        DateBag function = new DateBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:dateTime-bag
     */
    @Test
    public void test_DatetimeBag() throws XACML3EntitlementException {
        DatetimeBag function = new DatetimeBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     *  urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag
     */
    @Test
    public void test_DaytimedurationBag() throws XACML3EntitlementException {
        DaytimedurationBag function = new DaytimedurationBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:double-bag
     */
    @Test
    public void test_DoubleBag() throws XACML3EntitlementException {
        DoubleBag function = new DoubleBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag
     */
    @Test
    public void test_HexbinaryBag() throws XACML3EntitlementException {
        HexbinaryBag function = new HexbinaryBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-bag
     */
    @Test
    public void test_IntegerBag() throws XACML3EntitlementException {
        IntegerBag function = new IntegerBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag
     */
    @Test
    public void test_Rfc822NameBag() throws XACML3EntitlementException {
        Rfc822NameBag function = new Rfc822NameBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:string-bag
     */
    @Test
    public void test_StringBag() throws XACML3EntitlementException {

        final DataValue HELLO_WORLD = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");
        final DataValue HELLO_WORLD_NUMBER = new DataValue(DataType.XACMLSTRING, "HELLO WORLD Number: ");

        StringBag function = new StringBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
        // Check native unwrapped Result
        List<String> collection = TestXacmlDataUtils.asStringCollection(result);
        assertNotNull(collection);
        assertEquals(collection.size(), 0);

        // Check Single Element Added.
        function = new StringBag();
        function.addArgument(HELLO_WORLD);
        result = function.evaluate(null);
        assertNotNull(result);

        collection = TestXacmlDataUtils.asStringCollection(result);
        assertNotNull(collection);
        assertEquals(collection.size(), 1);
        assertEquals( collection.get(0), HELLO_WORLD.asString(null) );

        // Check Multiple Elements Added.
        function = new StringBag();
        for(int i=0; i<6; i++) {
            function.addArgument(HELLO_WORLD);
        }
        result = function.evaluate(null);
        assertNotNull(result);
        collection = TestXacmlDataUtils.asStringCollection(result);
        assertNotNull(collection);
        assertEquals(collection.size(), 6);
        for(int i=0; i<6; i++) {
            assertEquals( collection.get(i), HELLO_WORLD.asString(null) );
        }

        // Check Multiple Elements Added.
        function = new StringBag();
        for(int i=0; i<6; i++) {
            function.addArgument(new DataValue(DataType.XACMLSTRING, "HELLO WORLD Number: "+i));
        }
        result = function.evaluate(null);
        assertNotNull(result);
        collection = TestXacmlDataUtils.asStringCollection(result);
        assertNotNull(collection);
        assertEquals(collection.size(), 6);
        for(int i=0; i<6; i++) {
            assertEquals( collection.get(i), HELLO_WORLD_NUMBER.asString(null)+i );
        }

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:time-bag
     */
    @Test
    public void test_TimeBag() throws XACML3EntitlementException {
        TimeBag function = new TimeBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:x500Name-bag
     */
    @Test
    public void test_X500NameBag() throws XACML3EntitlementException {
        X500NameBag function = new X500NameBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

    /**
     *  urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag
     */
    @Test
    public void test_YearmonthdurationBag() throws XACML3EntitlementException {
        YearmonthdurationBag function = new YearmonthdurationBag();
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        // Check raw Result
        List<DataValue> bagValues = (List<DataValue>) result.getValue(null);
        assertNotNull(bagValues);
        assertEquals(bagValues.size(),0);
    }

}
