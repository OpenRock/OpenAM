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

import org.forgerock.openam.xacml.v3.model.DataType;
import org.forgerock.openam.xacml.v3.model.DataValue;
import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A.3.6 Numeric comparison functions
 These functions form a minimal set for comparing two numbers, yielding a Boolean result.
 For doubles they SHALL comply with the rules governed by IEEE 754 [IEEE754].

 urn:oasis:names:tc:xacml:1.0:function:integer-greater-than
 urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
 urn:oasis:names:tc:xacml:1.0:function:integer-less-than
 urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal
 urn:oasis:names:tc:xacml:1.0:function:double-greater-than
 urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal
 urn:oasis:names:tc:xacml:1.0:function:double-less-than
 urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal

 */

/**
 * XACML Numeric Comparison Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlNumericComparisonFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    static final FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
    static final FunctionArgument double2 = new DataValue(DataType.XACMLDOUBLE, 456789D, true);
    static final FunctionArgument double3 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
    static final FunctionArgument double4 = new DataValue(DataType.XACMLDOUBLE, 2D, true);

    static final FunctionArgument integer1 = new DataValue(DataType.XACMLINTEGER, 25000000, true);
    static final FunctionArgument integer2 = new DataValue(DataType.XACMLINTEGER, 2500, true);
    static final FunctionArgument integer3 = new DataValue(DataType.XACMLINTEGER, 25000000, true);
    static final FunctionArgument integer4 = new DataValue(DataType.XACMLINTEGER, 2, true);

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-greater-than
     */
    @Test
    public void testIntegerGreaterThan() throws XACML3EntitlementException {
        IntegerGreaterThan integerGreaterThan = new IntegerGreaterThan();
        integerGreaterThan.addArgument(integer1);
        integerGreaterThan.addArgument(integer2);

        FunctionArgument result = integerGreaterThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        integerGreaterThan = new IntegerGreaterThan();
        integerGreaterThan.addArgument(integer1);
        integerGreaterThan.addArgument(integer3);

        result = integerGreaterThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        integerGreaterThan = new IntegerGreaterThan();
        integerGreaterThan.addArgument(integer1);
        integerGreaterThan.addArgument(integer4);

        result = integerGreaterThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
     */
    @Test
    public void testIntegerGreaterThanOrEqual() throws XACML3EntitlementException {
        IntegerGreaterThanOrEqual integerGreaterThanOrEqual = new IntegerGreaterThanOrEqual();
        integerGreaterThanOrEqual.addArgument(integer1);
        integerGreaterThanOrEqual.addArgument(integer2);

        FunctionArgument result = integerGreaterThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        integerGreaterThanOrEqual = new IntegerGreaterThanOrEqual();
        integerGreaterThanOrEqual.addArgument(integer1);
        integerGreaterThanOrEqual.addArgument(integer3);

        result = integerGreaterThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        integerGreaterThanOrEqual = new IntegerGreaterThanOrEqual();
        integerGreaterThanOrEqual.addArgument(integer1);
        integerGreaterThanOrEqual.addArgument(integer4);

        result = integerGreaterThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-less-than
     */
    @Test
    public void testIntegerLessThan() throws XACML3EntitlementException {
        IntegerLessThan integerLessThan = new IntegerLessThan();
        integerLessThan.addArgument(integer1);
        integerLessThan.addArgument(integer2);

        FunctionArgument result = integerLessThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        integerLessThan = new IntegerLessThan();
        integerLessThan.addArgument(integer4);
        integerLessThan.addArgument(integer3);

        result = integerLessThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        integerLessThan = new IntegerLessThan();
        integerLessThan.addArgument(integer4);
        integerLessThan.addArgument(integer2);

        result = integerLessThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal
     */
    @Test
    public void testIntegerLessThanOrEqual() throws XACML3EntitlementException {
        IntegerLessThanOrEqual integerLessThanOrEqual = new IntegerLessThanOrEqual();
        integerLessThanOrEqual.addArgument(integer1);
        integerLessThanOrEqual.addArgument(integer2);

        FunctionArgument result = integerLessThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        integerLessThanOrEqual = new IntegerLessThanOrEqual();
        integerLessThanOrEqual.addArgument(integer1);
        integerLessThanOrEqual.addArgument(integer3);

        result = integerLessThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        integerLessThanOrEqual = new IntegerLessThanOrEqual();
        integerLessThanOrEqual.addArgument(integer4);
        integerLessThanOrEqual.addArgument(integer1);

        result = integerLessThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:double-greater-than
     */
    @Test
    public void testDoublerGreaterThan() throws XACML3EntitlementException {

        DoubleGreaterThan doubleGreaterThan = new DoubleGreaterThan();
        doubleGreaterThan.addArgument(double1);
        doubleGreaterThan.addArgument(double2);

        FunctionArgument result = doubleGreaterThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        doubleGreaterThan = new DoubleGreaterThan();
        doubleGreaterThan.addArgument(double1);
        doubleGreaterThan.addArgument(double3);

        result = doubleGreaterThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        doubleGreaterThan = new DoubleGreaterThan();
        doubleGreaterThan.addArgument(double1);
        doubleGreaterThan.addArgument(double4);

        result = doubleGreaterThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal
     */
    @Test
    public void testDoubleGreaterThanOrEqual() throws XACML3EntitlementException {

        DoubleGreaterThanOrEqual doubleGreaterThanOrEqual = new DoubleGreaterThanOrEqual();
        doubleGreaterThanOrEqual.addArgument(double1);
        doubleGreaterThanOrEqual.addArgument(double2);

        FunctionArgument result = doubleGreaterThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        doubleGreaterThanOrEqual = new DoubleGreaterThanOrEqual();
        doubleGreaterThanOrEqual.addArgument(double1);
        doubleGreaterThanOrEqual.addArgument(double3);

        result = doubleGreaterThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        doubleGreaterThanOrEqual = new DoubleGreaterThanOrEqual();
        doubleGreaterThanOrEqual.addArgument(double1);
        doubleGreaterThanOrEqual.addArgument(double4);

        result = doubleGreaterThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:double-less-than
     */
    @Test
    public void testDoubleLessThan() throws XACML3EntitlementException {

        DoubleLessThan doubleLessThan = new DoubleLessThan();
        doubleLessThan.addArgument(double1);
        doubleLessThan.addArgument(double2);

        FunctionArgument result = doubleLessThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        doubleLessThan = new DoubleLessThan();
        doubleLessThan.addArgument(double4);
        doubleLessThan.addArgument(double3);

        result = doubleLessThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        doubleLessThan = new DoubleLessThan();
        doubleLessThan.addArgument(double4);
        doubleLessThan.addArgument(double2);

        result = doubleLessThan.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal
     */
    @Test
    public void testDoubleLessThanOrEqual() throws XACML3EntitlementException {

        DoubleLessThanOrEqual doubleLessThanOrEqual = new DoubleLessThanOrEqual();
        doubleLessThanOrEqual.addArgument(double1);
        doubleLessThanOrEqual.addArgument(double2);

        FunctionArgument result = doubleLessThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        doubleLessThanOrEqual = new DoubleLessThanOrEqual();
        doubleLessThanOrEqual.addArgument(double1);
        doubleLessThanOrEqual.addArgument(double3);

        result = doubleLessThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        doubleLessThanOrEqual = new DoubleLessThanOrEqual();
        doubleLessThanOrEqual.addArgument(double4);
        doubleLessThanOrEqual.addArgument(double1);

        result = doubleLessThanOrEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }


}
