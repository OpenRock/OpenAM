/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
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

import org.forgerock.openam.xacml.v3.Entitlements.DataType;
import org.forgerock.openam.xacml.v3.Entitlements.DataValue;
import org.forgerock.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.openam.xacml.v3.Entitlements.XACML3EntitlementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


/**
 * XACML Arithmetic Functions
 *
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlArithmeticFunctions {

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-add
     * This function MUST accept two or more arguments.
     */
    @Test
    public void testInteger_Add() throws XACML3EntitlementException {
        FunctionArgument int1 = new DataValue(DataType.XACMLINTEGER, 1);
        FunctionArgument int2 = new DataValue(DataType.XACMLINTEGER, 222);
        FunctionArgument int3 = new DataValue(DataType.XACMLINTEGER, 420);
        FunctionArgument int4 = new DataValue(DataType.XACMLINTEGER, 2);

        IntegerAdd integerAdd = new IntegerAdd();
        // Place Objects in Argument stack for comparison.
        integerAdd.addArgument(int1);
        integerAdd.addArgument(int2);
        FunctionArgument result = integerAdd.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null).intValue(), 223);

        integerAdd = new IntegerAdd();
        // Place Objects in Argument stack for accumulation.
        integerAdd.addArgument(int1);
        integerAdd.addArgument(int2);
        integerAdd.addArgument(int3);
        integerAdd.addArgument(int4);
        result = integerAdd.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null).intValue(), 645);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-add
     * This function MUST accept two or more arguments.
     */
    @Test(expectedExceptions = XACML3EntitlementException.class)
    public void testInteger_Add_Exception() throws XACML3EntitlementException {
        FunctionArgument int1 = new DataValue(DataType.XACMLINTEGER, 1);
        FunctionArgument bad2 = new DataValue(DataType.XACMLSTRING, "1");

        IntegerAdd integerAdd = new IntegerAdd();
        // Place Objects in Argument stack for comparison.
        integerAdd.addArgument(int1);
        integerAdd.addArgument(bad2);
        FunctionArgument result = integerAdd.evaluate(null);
        // We should never hit here....
        assertTrue(false);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:double-add
     * This function MUST accept two or more arguments.
     */
    @Test
    public void testDouble_Add() throws XACML3EntitlementException {
        FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, 10000000001D);
        FunctionArgument double2 = new DataValue(DataType.XACMLDOUBLE, 10000000222D);
        FunctionArgument double3 = new DataValue(DataType.XACMLDOUBLE, 10000000420D);
        FunctionArgument double4 = new DataValue(DataType.XACMLDOUBLE, 10000000002D);

        DoubleAdd doubleAdd = new DoubleAdd();
        // Place Objects in Argument stack for accumulation.
        doubleAdd.addArgument(double1);
        doubleAdd.addArgument(double2);
        FunctionArgument result = doubleAdd.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDouble(null).doubleValue(), 20000000223D);

        doubleAdd = new DoubleAdd();
        // Place Objects in Argument stack for comparison.
        doubleAdd.addArgument(double1);
        doubleAdd.addArgument(double2);
        doubleAdd.addArgument(double3);
        doubleAdd.addArgument(double4);
        result = doubleAdd.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDouble(null).doubleValue(), 40000000645D);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-subtract
     * The result is the second argument subtracted from the first argument.
     */
    @Test
    public void testInteger_Subtract() throws XACML3EntitlementException {
        FunctionArgument int1 = new DataValue(DataType.XACMLINTEGER, 6);
        FunctionArgument int2 = new DataValue(DataType.XACMLINTEGER, 66);
        FunctionArgument int3 = new DataValue(DataType.XACMLINTEGER, 6);
        FunctionArgument int4 = new DataValue(DataType.XACMLINTEGER, 2);

        IntegerSubtract integerSubtract = new IntegerSubtract();
        // Place Objects in Argument stack for deduction.
        integerSubtract.addArgument(int1);
        integerSubtract.addArgument(int2);
        FunctionArgument result = integerSubtract.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null).intValue(), -60);

        integerSubtract = new IntegerSubtract();
        // Place Objects in Argument stack for deduction.
        integerSubtract.addArgument(int3);
        integerSubtract.addArgument(int4);
        result = integerSubtract.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null).intValue(), 4);
    }

    /**
     *   urn:oasis:names:tc:xacml:1.0:function:double-subtract
     * The result is the second argument subtracted from the first argument.
     */
    @Test
    public void testDouble_Subtract() throws XACML3EntitlementException {
        FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, 10000000066D);
        FunctionArgument double2 = new DataValue(DataType.XACMLDOUBLE, 10000000006D);
        FunctionArgument double3 = new DataValue(DataType.XACMLDOUBLE, 10000000002D);
        FunctionArgument double4 = new DataValue(DataType.XACMLDOUBLE, 10000000004D);

        DoubleSubtract doubleSubtract = new DoubleSubtract();
        // Place Objects in Argument stack for deduction.
        doubleSubtract.addArgument(double1);
        doubleSubtract.addArgument(double2);
        FunctionArgument result = doubleSubtract.evaluate(null);
        assertNotNull(result);
        assertTrue(result.asDouble(null).doubleValue() == 60D);

        doubleSubtract = new DoubleSubtract();
        // Place Objects in Argument stack for deduction.
        doubleSubtract.addArgument(double3);
        doubleSubtract.addArgument(double4);
        result = doubleSubtract.evaluate(null);
        assertNotNull(result);
        assertTrue(result.asDouble(null).doubleValue() == -2D);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-multiply
     * This function MUST accept two or more arguments.
     */
    @Test
    public void testInteger_Multiply() throws XACML3EntitlementException {

    }

    /**
     *
     *  urn:oasis:names:tc:xacml:1.0:function:double-multiply
     * This function MUST accept two or more arguments.
     */
    @Test
    public void testDouble_Multiply() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-divide
     * The result is the first argument divided by the second argument.
     */
    @Test
    public void testInteger_Divide() throws XACML3EntitlementException {

    }

    /**
     *
     *  urn:oasis:names:tc:xacml:1.0:function:double-divide
     * The result is the first argument divided by the second argument.
     */
    @Test
    public void testDouble_Divide() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-mod
     * The result is remainder of the first argument divided by the second argument.
     */
    @Test
    public void testInteger_Mod() throws XACML3EntitlementException {

    }

    /**
     * The following functions SHALL take a single argument of the specified data-type.
     * The round and floor functions SHALL take a single argument of data-type “http://www.w3
     * .org/2001/XMLSchema#double”
     * and return a value of the data-type “http://www.w3.org/2001/XMLSchema#double”.
     * urn:oasis:names:tc:xacml:1.0:function:integer-abs
     */
    @Test
    public void testInteger_Abs() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:double-abs
     */
    @Test
    public void testDouble_Abs() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:round
     */
    @Test
    public void testRound() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:floor
     */
    @Test
    public void testFloor() throws XACML3EntitlementException {

    }


}
