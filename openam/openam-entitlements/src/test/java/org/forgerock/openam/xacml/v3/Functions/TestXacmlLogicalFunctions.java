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

import org.forgerock.openam.xacml.v3.Entitlements.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A.3.5 Logical functions
 This section contains the specification for logical functions that operate on arguments of data-type
 “http://www.w3.org/2001/XMLSchema#boolean”.

 urn:oasis:names:tc:xacml:1.0:function:or
 This function SHALL return "False" if it has no arguments and SHALL return "True" if at least one of its arguments
 evaluates to "True".  The order of evaluation SHALL be from first argument to last.
 The evaluation SHALL stop with a result of "True" if any argument evaluates to "True",
 leaving the rest of the arguments unevaluated.

 urn:oasis:names:tc:xacml:1.0:function:and
 This function SHALL return "True" if it has no arguments and SHALL return "False" if one of its arguments
 evaluates to "False".  The order of evaluation SHALL be from first argument to last.
 The evaluation SHALL stop with a result of "False" if any argument evaluates to "False",
 leaving the rest of the arguments unevaluated.

 urn:oasis:names:tc:xacml:1.0:function:n-of
 The first argument to this function SHALL be of data-type http://www.w3.org/2001/XMLSchema#integer.
 The remaining arguments SHALL be of data-type http://www.w3.org/2001/XMLSchema#boolean.
 The first argument specifies the minimum number of the remaining arguments that MUST evaluate to "True" for
 the expression to be considered "True".  If the first argument is 0, the result SHALL be "True".
 If the number of arguments after the first one is less than the value of the first argument, then the expression
 SHALL result in "Indeterminate".  The order of evaluation SHALL be: first evaluate the integer value, and then evaluate each subsequent argument.  The evaluation SHALL stop and return "True" if the specified number of arguments evaluate to "True".  The evaluation of arguments SHALL stop if it is determined that evaluating the remaining arguments will not satisfy the requirement.

 urn:oasis:names:tc:xacml:1.0:function:not
 This function SHALL take one argument of data-type “http://www.w3.org/2001/XMLSchema#boolean”.
 If the argument evaluates to "True", then the result of the expression SHALL be "False".
 If the argument evaluates to "False", then the result of the expression SHALL be "True".

 Note: When evaluating and, or, or n-of, it MAY NOT be necessary to attempt a full evaluation of each
 argument in order to determine whether the evaluation of the argument would result in "Indeterminate".  Analysis of the argument regarding the availability of its attributes, or other analysis regarding errors, such as "divide-by-zero", may render the argument error free.  Such arguments occurring in the expression in a position after the evaluation is stated to stop need not be processed.

 */

/**
 * XACML Logical Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlLogicalFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    static final FunctionArgument string1 = new DataValue(DataType.XACMLSTRING, "Hello World!");
    static final FunctionArgument string2 = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");
    static final FunctionArgument string3 = new DataValue(DataType.XACMLSTRING, "Hello");
    static final FunctionArgument string4 = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");

    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:or
     */
    @Test
    public void test_Logical_Or() throws XACML3EntitlementException {

        Or _OR = new Or();
            StringEqual stringEqual = new StringEqual();
            // Place Objects in Argument stack for comparison.
            stringEqual.addArgument(string1);
            stringEqual.addArgument(string2);
        _OR.addArgument(stringEqual);
        FunctionArgument result = _OR.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        _OR = new Or();
        stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string2);
        stringEqual.addArgument(string3);
        _OR.addArgument(stringEqual);
        // Place Objects in Argument stack for comparison.
        StringEqual stringEqual2 = new StringEqual();
        stringEqual2.addArgument(string3);
        stringEqual2.addArgument(string4);
        _OR.addArgument(stringEqual2);
        // Place Objects in Argument stack for comparison.
        StringEqual stringEqual3 = new StringEqual();
        stringEqual3.addArgument(string2);
        stringEqual3.addArgument(string4);
        _OR.addArgument(stringEqual3);

        result = _OR.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:and
     */
    @Test
    public void test_Logical_And() throws XACML3EntitlementException {
        And _AND = new And();
        StringEqual stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string1);
        stringEqual.addArgument(string2);
        _AND.addArgument(stringEqual);
        FunctionArgument result = _AND.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        _AND = new And();
        stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string2);
        stringEqual.addArgument(string4);
        _AND.addArgument(stringEqual);

        StringEqual stringEqual2 = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual2.addArgument(string2);
        stringEqual2.addArgument(string4);
        _AND.addArgument(stringEqual2);

        StringEqual stringEqual3 = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual3.addArgument(string2);
        stringEqual3.addArgument(string4);
        _AND.addArgument(stringEqual3);

        result = _AND.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:n-of
     */
    @Test
    public void test_Logical_Nof() throws XACML3EntitlementException {
        NOf _NOf = new NOf();
        FunctionArgument requiredToBeTrue = new DataValue(DataType.XACMLINTEGER, 1, true);
        _NOf.addArgument(requiredToBeTrue);

        StringEqual stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string2);
        stringEqual.addArgument(string3);
        _NOf.addArgument(stringEqual);

        // Place Objects in Argument stack for comparison.
        StringEqual stringEqual2 = new StringEqual();
        stringEqual2.addArgument(string3);
        stringEqual2.addArgument(string4);
        _NOf.addArgument(stringEqual2);

        // Place Objects in Argument stack for comparison.
        StringEqual stringEqual3 = new StringEqual();
        stringEqual3.addArgument(string2);
        stringEqual3.addArgument(string4);
        _NOf.addArgument(stringEqual3);

        FunctionArgument result = _NOf.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:n-of
     */
    @Test(expectedExceptions = IndeterminateException.class)
    public void test_Logical_Nof_Failure() throws XACML3EntitlementException {
        NOf _NOf = new NOf();
        FunctionArgument requiredToBeTrue = new DataValue(DataType.XACMLINTEGER, 4, true);
        _NOf.addArgument(requiredToBeTrue);

        StringEqual stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string2);
        stringEqual.addArgument(string3);
        _NOf.addArgument(stringEqual);

        // Place Objects in Argument stack for comparison.
        StringEqual stringEqual2 = new StringEqual();
        stringEqual2.addArgument(string3);
        stringEqual2.addArgument(string4);
        _NOf.addArgument(stringEqual2);

        // Place Objects in Argument stack for comparison.
        StringEqual stringEqual3 = new StringEqual();
        stringEqual3.addArgument(string2);
        stringEqual3.addArgument(string4);
        _NOf.addArgument(stringEqual3);

        _NOf.evaluate(null);
        // Should never get here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:n-of
     */
    @Test
    public void test_Logical_Nof_Nothing() throws XACML3EntitlementException {
        NOf _NOf = new NOf();
        FunctionArgument requiredToBeTrue = new DataValue(DataType.XACMLINTEGER, 0, true);
        _NOf.addArgument(requiredToBeTrue);
        FunctionArgument result = _NOf.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }



    /**
     * urn:oasis:names:tc:xacml:1.0:function:not
     */
    @Test
    public void test_Logical_Not() throws XACML3EntitlementException {
        Not _NOT = new Not();
        StringEqual stringEqual = new StringEqual();
        // Place Objects in Argument stack for comparison.
        stringEqual.addArgument(string1);
        stringEqual.addArgument(string2);
        _NOT.addArgument(stringEqual);
        FunctionArgument result = _NOT.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

}
