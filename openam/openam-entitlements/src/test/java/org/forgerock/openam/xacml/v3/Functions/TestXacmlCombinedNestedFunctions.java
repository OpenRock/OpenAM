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

import org.forgerock.openam.xacml.v3.model.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * XACML Combined FUnction Tests
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * X500Name
 */
public class TestXacmlCombinedNestedFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    static final FunctionArgument testString1 = new DataValue(DataType.XACMLSTRING, "Forge");
    static final FunctionArgument testString2 = new DataValue(DataType.XACMLSTRING, "Rock");
    static final FunctionArgument testString3 = new DataValue(DataType.XACMLSTRING, " says Hello World!");
    static final FunctionArgument testString4 = new DataValue(DataType.XACMLSTRING, " HELLO WORLD!");

    FunctionArgument testInteger1 = new DataValue(DataType.XACMLINTEGER, 195, true);

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * Test a Combined Nested set of Functions
     */
    @Test
    public void test_UseCase_Combined_Nested_Functions() throws XACML3EntitlementException {
        StringBag stringBag = new StringBag();

        StringConcatenate stringConcatenate = new StringConcatenate();
        // Place Objects in Argument stack.
        stringConcatenate.addArgument(testString1);
        stringConcatenate.addArgument(testString2);
        stringConcatenate.addArgument(testString3);
        stringConcatenate.addArgument(testString4);

        StringFromInteger stringFromInteger = new StringFromInteger();
        stringFromInteger.addArgument(testInteger1);

        StringFromBoolean stringFromBoolean = new StringFromBoolean();
        stringFromBoolean.addArgument(trueObject);
        StringFromBoolean stringFromBoolean2 = new StringFromBoolean();
        stringFromBoolean2.addArgument(falseObject);

        stringBag.addArgument(stringConcatenate);
        stringBag.addArgument(stringFromInteger);
        stringBag.addArgument(stringFromBoolean);
        stringBag.addArgument(stringFromBoolean2);

        // Trigger Evaluation
        DataBag dataBag = (DataBag) stringBag.evaluate(null);
        // Check raw Result
        assertNotNull(dataBag);
        assertEquals(dataBag.size(), 4);

        // Check native unwrapped Result
        List<String> collection = TestXacmlDataUtils.asStringCollection(dataBag);
        assertNotNull(collection);
        assertEquals(collection.size(), 4);
        assertEquals(collection.get(0),"ForgeRock says Hello World! HELLO WORLD!");
        assertEquals(collection.get(1),"195");
        assertEquals(collection.get(2),"true");
        assertEquals(collection.get(3),"false");

    }

}
