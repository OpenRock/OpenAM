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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


/**
 * XACML String Conversion Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * X500Name
 */
public class TestXacmlStringConversionFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:string-normalize-space
     */
    @Test
    public void testStringNormalizeSpace() throws XACML3EntitlementException {
        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING, "     Hello World!      ");
        FunctionArgument string2 = new DataValue(DataType.XACMLSTRING, "     HELLO WORLD!      ");
        FunctionArgument string3 = new DataValue(DataType.XACMLSTRING, "Hello World!");
        FunctionArgument string4 = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");

        StringNormalizeSpace stringNormalizeSpace = new StringNormalizeSpace();
        // Place Object in Argument stack.
        stringNormalizeSpace.addArgument(string1);
        FunctionArgument result = stringNormalizeSpace.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), string3.asString(null));

        stringNormalizeSpace = new StringNormalizeSpace();
        // Place Objects in Argument stack for comparison.
        stringNormalizeSpace.addArgument(string2);
        result = stringNormalizeSpace.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), string4.asString(null));

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:string-normalize-space
     */
    @Test(expectedExceptions = XACML3EntitlementException.class)
    public void testStringNormalizeSpace_Failure() throws XACML3EntitlementException {
        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING, "     Hello World!      ");
        FunctionArgument string2 = new DataValue(DataType.XACMLSTRING, "     HELLO WORLD!        ");
        FunctionArgument string3 = new DataValue(DataType.XACMLSTRING, "Hello World");
        FunctionArgument string4 = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");

        StringNormalizeSpace stringNormalizeSpace = new StringNormalizeSpace();
        // Place Objects in Argument stack for comparison.
        stringNormalizeSpace.addArgument(string1);
        stringNormalizeSpace.addArgument(string2);
        stringNormalizeSpace.addArgument(string3);
        stringNormalizeSpace.addArgument(string4);
        stringNormalizeSpace.evaluate(null);
        // Should never hit here...
        assertTrue(false);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case
     */
    @Test
    public void testStringNormalizeToLowerCase() throws XACML3EntitlementException {
        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING, "     Hello World!        ");
        FunctionArgument string2 = new DataValue(DataType.XACMLSTRING, "     hello world!        ");
        FunctionArgument string3 = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");
        FunctionArgument string4 = new DataValue(DataType.XACMLSTRING, "hello world!");

        StringNormalizeToLowerCase stringNormalizeToLowerCase = new StringNormalizeToLowerCase();
        // Place Object in Argument stack.
        stringNormalizeToLowerCase.addArgument(string1);
        FunctionArgument result = stringNormalizeToLowerCase.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), string2.asString(null));

        stringNormalizeToLowerCase = new StringNormalizeToLowerCase();
        // Place Objects in Argument stack for comparison.
        stringNormalizeToLowerCase.addArgument(string3);
        result = stringNormalizeToLowerCase.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), string4.asString(null));

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:string-normalize-space
     */
    @Test(expectedExceptions = XACML3EntitlementException.class)
    public void testStringNormalizeToLowerCase_Failure() throws XACML3EntitlementException {
        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING, "     Hello World!        ");
        FunctionArgument string2 = new DataValue(DataType.XACMLSTRING, "     HELLO WORLD!        ");
        FunctionArgument string3 = new DataValue(DataType.XACMLSTRING, "Hello World");
        FunctionArgument string4 = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");

        StringNormalizeToLowerCase stringNormalizeToLowerCase = new StringNormalizeToLowerCase();
        // Place Objects in Argument stack
        stringNormalizeToLowerCase.addArgument(string1);
        stringNormalizeToLowerCase.addArgument(string2);
        stringNormalizeToLowerCase.addArgument(string3);
        stringNormalizeToLowerCase.addArgument(string4);
        stringNormalizeToLowerCase.evaluate(null);
        // Should never hit here...
        assertTrue(false);
    }

}
