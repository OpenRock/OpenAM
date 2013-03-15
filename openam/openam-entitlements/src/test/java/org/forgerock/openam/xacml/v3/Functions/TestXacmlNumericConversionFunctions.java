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


/**
 * XACML Numeric Conversion Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlNumericConversionFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:double-to-integer
     */
    @Test
    public void testDoubleToInteger() throws XACML3EntitlementException {
        FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, -7566D, true);
        FunctionArgument double2 = new DataValue(DataType.XACMLDOUBLE, -9D, true);

        DoubleToInteger doubleToInteger = new DoubleToInteger();
        // Place Objects in Argument stack.
        doubleToInteger.addArgument(double1);
        FunctionArgument result = doubleToInteger.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null).intValue(), -7566);

        doubleToInteger = new DoubleToInteger();
        // Place Objects in Argument stack.
        doubleToInteger.addArgument(double2);
        result = doubleToInteger.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null).intValue(), -9);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-to-double
     */
    @Test
    public void testIntegerToDouble() throws XACML3EntitlementException {
        FunctionArgument integer1 = new DataValue(DataType.XACMLINTEGER, -7566, true);
        FunctionArgument integer2 = new DataValue(DataType.XACMLINTEGER, -9, true);

        IntegerToDouble integerToDouble = new IntegerToDouble();
        // Place Objects in Argument stack.
        integerToDouble.addArgument(integer1);
        FunctionArgument result = integerToDouble.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDouble(null), -7566D);

        integerToDouble = new IntegerToDouble();
        // Place Objects in Argument stack.
        integerToDouble.addArgument(integer2);
        result = integerToDouble.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDouble(null), -9D);
    }
}
