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
import org.forgerock.openam.xacml.v3.Entitlements.XACMLEvalContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


/**
 * XACML Equality Predicate Functions
 *
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlEqualityPredicateFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN,"true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN,"false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    @Test
    public void testAnyuriEqual() {

    }

    @Test
    public void testBase64BinaryEqual() {

    }

    @Test
    public void testBooleanEqual() {
        BooleanEqual booleanEqual = new BooleanEqual();
        // Place Objects in Argument stack for comparison.
        booleanEqual.addArgument(trueObject);
        booleanEqual.addArgument(falseObject);
        FunctionArgument result = booleanEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        booleanEqual = new BooleanEqual();
        // Place Objects in Argument stack for comparison.
        booleanEqual.addArgument(falseObject);
        booleanEqual.addArgument(falseObject);
        result = booleanEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        booleanEqual = new BooleanEqual();
        // Place Objects in Argument stack for comparison.
        booleanEqual.addArgument(trueObject);
        booleanEqual.addArgument(trueObject);
        result = booleanEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    @Test
    public void testDateEqual() {

    }

    @Test
    public void testDatetimeEqual() {

    }

    @Test
    public void testDaytimedurationEqual() {

    }

    @Test
    public void testDoubleEqual() {

    }

    @Test
    public void testHexbinaryEqual() {

    }

    @Test
    public void testIntegerEqual() {

    }

    @Test
    public void testRfc822NameEqual() {

    }

    @Test
    public void testStringEqual() {
        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING,"Hello World!");
        FunctionArgument string2 = new DataValue(DataType.XACMLSTRING,"HELLO WORLD!");
        FunctionArgument string3 = new DataValue(DataType.XACMLSTRING,"Hello");
        FunctionArgument string4 = new DataValue(DataType.XACMLSTRING,"HELLO WORLD!");

        StringEqual stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string1);
        stringEqual.addArgument(string2);
        FunctionArgument result = stringEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string1);
        stringEqual.addArgument(string3);
        result = stringEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string2);
        stringEqual.addArgument(string4);
        result = stringEqual.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    @Test
    public void testStringEqualIgnoreCase() {
        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING,"Hello World!");
        FunctionArgument string2 = new DataValue(DataType.XACMLSTRING,"HELLO WORLD!");
        FunctionArgument string3 = new DataValue(DataType.XACMLSTRING,"Hello World");
        FunctionArgument string4 = new DataValue(DataType.XACMLSTRING,"HELLO WORLD!");

        StringEqualIgnoreCase stringEqualIgnoreCase = new StringEqualIgnoreCase();
        // Place Objects in Argument stack for comparison.
        stringEqualIgnoreCase.addArgument(string1);
        stringEqualIgnoreCase.addArgument(string2);
        FunctionArgument result = stringEqualIgnoreCase.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        stringEqualIgnoreCase = new StringEqualIgnoreCase();
        // Place Objects in Argument stack for comparison.
        stringEqualIgnoreCase.addArgument(string1);
        stringEqualIgnoreCase.addArgument(string3);
        result = stringEqualIgnoreCase.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        stringEqualIgnoreCase = new StringEqualIgnoreCase();
        // Place Objects in Argument stack for comparison.
        stringEqualIgnoreCase.addArgument(string1);
        stringEqualIgnoreCase.addArgument(string4);
        result = stringEqualIgnoreCase.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    @Test
    public void testTimeEqual() {

    }

    @Test
    public void testX500NameEqual() {

    }

    @Test
    public void testYearmonthdurationEqual() {

    }


}
