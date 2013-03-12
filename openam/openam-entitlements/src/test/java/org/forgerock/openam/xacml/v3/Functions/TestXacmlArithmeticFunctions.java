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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


/**
 * XACML Equality Predicate Functions
 *
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlArithmeticFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


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
    public void testInteger_Add() {

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:double-add
     * This function MUST accept two or more arguments.
     */
    @Test
    public void testDouble_Add() {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-subtract
     * The result is the second argument subtracted from the first argument.
     */
    @Test
    public void testInteger_Subtract() {

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

    /**
     *   urn:oasis:names:tc:xacml:1.0:function:double-subtract
     * The result is the second argument subtracted from the first argument.
     */
    @Test
    public void testDouble_Subtract() {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-multiply
     * This function MUST accept two or more arguments.
     */
    @Test
    public void testInteger_Multiply() {

    }

    /**
     *
     *  urn:oasis:names:tc:xacml:1.0:function:double-multiply
     * This function MUST accept two or more arguments.
     */
    @Test
    public void testDouble_Multiply() {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-divide
     * The result is the first argument divided by the second argument.
     */
    @Test
    public void testInteger_Divide() {

    }

    /**
     *
     *  urn:oasis:names:tc:xacml:1.0:function:double-divide
     * The result is the first argument divided by the second argument.
     */
    @Test
    public void testDouble_Divide() {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:integer-mod
     * The result is remainder of the first argument divided by the second argument.
     */
    @Test
    public void testInteger_Mod() {

    }

    /**
     * The following functions SHALL take a single argument of the specified data-type.
     * The round and floor functions SHALL take a single argument of data-type “http://www.w3
     * .org/2001/XMLSchema#double”
     * and return a value of the data-type “http://www.w3.org/2001/XMLSchema#double”.
     * urn:oasis:names:tc:xacml:1.0:function:integer-abs
     */
    @Test
    public void testInteger_Abs() {

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:double-abs
     */
    @Test
    public void testDouble_Abs() {

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:round
     */
    @Test
    public void testRound() {

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:floor
     */
    @Test
    public void testFloor() {

    }


}
