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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * A.3.12 Higher-order bag functions
 This section describes functions in XACML that perform operations on bags such that functions
 may be applied to the bags in general.

 urn:oasis:names:tc:xacml:3.0:function:any-of
 This function applies a Boolean function between specific primitive values and a bag of values, and SHALL return
 "True" if and only if the predicate is "True" for at least one element of the bag.
 This function SHALL take n+1 arguments, where n is one or greater. The first argument SHALL be an
 <Function> element that names a Boolean function that takes n arguments of primitive types.
 Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one SHALL be a
 bag of a primitive data-type.  The expression SHALL be evaluated as if the function named in the
 <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument
 and the results are combined with “urn:oasis:names:tc:xacml:1.0:function:or”.

 For example, the following expression SHALL return "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:any-of”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-equal”/>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>John</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>George</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
 </Apply>
 </Apply>
 This expression is "True" because the first argument is equal to at least one of the elements of the bag,
 according to the function.


 urn:oasis:names:tc:xacml:3.0:function:all-of
 This function applies a Boolean function between a specific primitive value and a bag of values, and
 returns "True" if and only if the predicate is "True" for every element of the bag.
 This function SHALL take n+1 arguments, where n is one or greater.
 The first argument SHALL be a <Function> element that names a Boolean function that takes n arguments of
 primitive types. Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one
 SHALL be a bag of a primitive data-type. The expression SHALL be evaluated as if the function named
 in the <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument and
 the results are combined with “urn:oasis:names:tc:xacml:1.0:function:and”.

 For example, the following expression SHALL evaluate to "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:all-of”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>10</AttributeValue>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>9</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
 </Apply>
 </Apply>
 This expression is "True" because the first argument (10) is greater than all of the elements of the bag (9,3,4 and 2).


 urn:oasis:names:tc:xacml:3.0:function:any-of-any
 This function applies a Boolean function on each tuple from the cross product on all bags
 arguments, and returns "True" if and only if the predicate is "True" for at least one inside-function call.
 This function SHALL take n+1 arguments, where n is one or greater.
 The first argument SHALL be an <Function> element that names a Boolean function that takes n arguments.
 The remaining arguments are either primitive data types or bags of primitive types.
 The expression SHALL be evaluated as if the function named in the <Function> argument was applied between
 every tuple of the cross product on all bags and the primitive values, and the results were
 combined using “urn:oasis:names:tc:xacml:1.0:function:or”.
 The semantics are that the result of the expression SHALL be "True" if and only if the applied predicate is
 "True" for at least one function call on the tuples from the bags and primitive values.

 For example, the following expression SHALL evaluate to "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:any-of-any”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-equal”/>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Mary</AttributeValue>
 </Apply>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>John</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>George</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
 </Apply>
 </Apply>
 This expression is "True" because at least one of the elements of the first bag, namely “Ringo”,
 is equal to at least one of the elements of the second bag.


 urn:oasis:names:tc:xacml:1.0:function:all-of-any
 This function applies a Boolean function between the elements of two bags.
 The expression SHALL be “True” if and only if the supplied predicate is “True” between each element of the
 first bag and any element of the second bag.
 This function SHALL take three arguments.  The first argument SHALL be an <Function> element that names a
 Boolean function that takes two arguments of primitive types.  The second argument SHALL be a bag of a
 primitive data-type.  The third argument SHALL be a bag of a primitive data-type.
 The expression SHALL be evaluated as if the “urn:oasis:names:tc:xacml:3.0:function:any-of” function had been
 applied to each value of the first bag and the whole of the second bag using the supplied xacml:Function, and the
 results were then combined using “urn:oasis:names:tc:xacml:1.0:function:and”.

 For example, the following expression SHALL evaluate to "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:all-of-any”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>10</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>20</AttributeValue>
 </Apply>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>1</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>5</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>19</AttributeValue>
 </Apply>
 </Apply>
 This expression is “True” because each of the elements of the first bag is greater than at
 least one of the elements of the second bag.

 urn:oasis:names:tc:xacml:1.0:function:any-of-all
 This function applies a Boolean function between the elements of two bags.
 The expression SHALL be “True” if and only if the supplied predicate is “True” between each
 element of the second bag and any element of the first bag.
 This function SHALL take three arguments.
 The first argument SHALL be an <Function> element that names a Boolean function that takes two
 arguments of primitive types.  The second argument SHALL be a bag of a primitive data-type.
 The third argument SHALL be a bag of a primitive data-type.
 The expression SHALL be evaluated as if the ”urn:oasis:names:tc:xacml:3.0:function:any-of”
 function had been applied to each value of the second bag and the whole of the
 first bag using the supplied xacml:Function, and the results were then
 combined using “urn:oasis:names:tc:xacml:1.0:function:and”.

 For example, the following expression SHALL evaluate to "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:any-of-all”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>5</AttributeValue>
 </Apply>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>1</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
 </Apply>
 </Apply>
 This expression is “True” because, for all of the values in the second bag,
 there is a value in the first bag that is greater.


 urn:oasis:names:tc:xacml:1.0:function:all-of-all
 This function applies a Boolean function between the elements of two bags.
 The expression SHALL be "True" if and only if the supplied predicate is "True" between each and every element of the
 first bag collectively against all the elements of the second bag.
 This function SHALL take three arguments.  The first argument SHALL be an <Function> element that names a
 Boolean function that takes two arguments of primitive types.
 The second argument SHALL be a bag of a primitive data-type.
 The third argument SHALL be a bag of a primitive data-type.
 The expression is evaluated as if the function named in the <Function> element were applied between every element
 of the second argument and every element of the third argument
 and the results were combined using “urn:oasis:names:tc:xacml:1.0:function:and”.
 The semantics are that the result of the expression is "True" if and only if the applied predicate is "True" for
 all elements of the first bag compared to all the elements of the second bag.

 For example, the following expression SHALL evaluate to "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:all-of-all”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>6</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>5</AttributeValue>
 </Apply>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>1</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
 </Apply>
 </Apply>
 This expression is "True" because all elements of the first bag, “5” and “6”,
 are each greater than all of the integer values “1”, ”2”, ”3”, ”4” of the second bag.


 urn:oasis:names:tc:xacml:3.0:function:map
 This function converts a bag of values to another bag of values.
 This function SHALL take n+1 arguments, where n is one or greater.
 The first argument SHALL be a <Function> element naming a function that takes a n arguments of a primitive
 data-type and returns a value of a primitive data-type Under the remaining n arguments,
 n-1 parameters SHALL be values of primitive data-types and one SHALL be a bag of a primitive data-type.
 The expression SHALL be evaluated as if the function named in the <Function> argument were applied to the n-1
 non-bag arguments and each element of the bag argument and resulting in a bag of the converted value.
 The result SHALL be a bag of the primitive data-type that is returned by the
 function named in the <xacml:Function> element.

 For example, the following expression,
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:map”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case”>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Hello</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>World!</AttributeValue>
 </Apply>
 </Apply>
 evaluates to a bag containing “hello” and “world!”.

 */

/**
 * XACML Higher-order Bag Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * X500Name
 */
public class TestXacmlHigherOrderBagFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * **
     * urn:oasis:names:tc:xacml:3.0:function:all-of
     * This function applies a Boolean function between a specific primitive value and a bag of values, and
     * returns "True" if and only if the predicate is "True" for every element of the bag.
     * <p/>
     * This function SHALL take n+1 arguments, where n is one or greater.
     * <p/>
     * The first argument SHALL be a <Function> element that names a Boolean function that takes n arguments of
     * primitive types. Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one
     * SHALL be a bag of a primitive data-type.
     * <p/>
     * The expression SHALL be evaluated as if the function named
     * in the <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument and
     * the results are combined with “urn:oasis:names:tc:xacml:1.0:function:and”.
     * <p/>
     * <p/>
     * For example, the following expression SHALL evaluate to "True":
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:all-of”>
     * <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>10</AttributeValue>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>9</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
     * </Apply>
     * </Apply>
     * This expression is "True" because the first argument (10) is greater than all of the elements of the bag (9,3,4 and 2).
     */
    @Test
    public void testAllOf() throws XACML3EntitlementException {
        final FunctionArgument testInteger = new DataValue(DataType.XACMLINTEGER, 10, true);
        final FunctionArgument testInteger1 = new DataValue(DataType.XACMLINTEGER, 9, true);
        final FunctionArgument testInteger2 = new DataValue(DataType.XACMLINTEGER, 3, true);
        final FunctionArgument testInteger3 = new DataValue(DataType.XACMLINTEGER, 4, true);
        final FunctionArgument testInteger4 = new DataValue(DataType.XACMLINTEGER, 2, true);

        AllOf allOf = new AllOf();

        IntegerGreaterThan function = new IntegerGreaterThan();

        IntegerBag bag = new IntegerBag();
        bag.addArgument(testInteger1);
        bag.addArgument(testInteger2);
        bag.addArgument(testInteger3);
        bag.addArgument(testInteger4);

        allOf.addArgument(function);
        allOf.addArgument(testInteger);
        allOf.addArgument(bag);

        FunctionArgument result = allOf.evaluate(null);
        assertNotNull(result);
        assertEquals(result.isTrue(), true);

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:any-of
     * This function applies a Boolean function between specific primitive values and a bag of values, and SHALL return
     * "True" if and only if the predicate is "True" for at least one element of the bag.
     * This function SHALL take n+1 arguments, where n is one or greater. The first argument SHALL be an
     * <Function> element that names a Boolean function that takes n arguments of primitive types.
     * Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one SHALL be a
     * bag of a primitive data-type.  The expression SHALL be evaluated as if the function named in the
     * <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument
     * and the results are combined with “urn:oasis:names:tc:xacml:1.0:function:or”.
     * <p/>
     * For example, the following expression SHALL return "True":
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:any-of”>
     * <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-equal”/>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>John</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>George</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
     * </Apply>
     * </Apply>
     * This expression is "True" because the first argument is equal to at least one of the elements of the bag,
     * according to the function.
     */
    @Test
    public void testAnyOf() throws XACML3EntitlementException {
        final FunctionArgument testString1 = new DataValue(DataType.XACMLSTRING, "John");
        final FunctionArgument testString2 = new DataValue(DataType.XACMLSTRING, "Paul");
        final FunctionArgument testString3 = new DataValue(DataType.XACMLSTRING, "George");
        final FunctionArgument testString4 = new DataValue(DataType.XACMLSTRING, "Ringo");

        AnyOf anyOf = new AnyOf();

        StringEqual equals = new StringEqual();

        StringBag bag = new StringBag();
        bag.addArgument(testString1);
        bag.addArgument(testString2);
        bag.addArgument(testString3);
        bag.addArgument(testString4);

        anyOf.addArgument(equals);
        anyOf.addArgument(testString2);
        anyOf.addArgument(bag);

        FunctionArgument result = anyOf.evaluate(null);
        assertNotNull(result);
        assertEquals(result.isTrue(), true);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:all-of-any
     * This function applies a Boolean function between the elements of two bags.
     * The expression SHALL be “True” if and only if the supplied predicate is “True” between each element of the
     * first bag and any element of the second bag.
     * This function SHALL take three arguments.  The first argument SHALL be an <Function> element that names a
     * Boolean function that takes two arguments of primitive types.  The second argument SHALL be a bag of a
     * primitive data-type.  The third argument SHALL be a bag of a primitive data-type.
     * The expression SHALL be evaluated as if the “urn:oasis:names:tc:xacml:3.0:function:any-of” function had been
     * applied to each value of the first bag and the whole of the second bag using the supplied xacml:Function, and the
     * results were then combined using “urn:oasis:names:tc:xacml:1.0:function:and”.
     * <p/>
     * For example, the following expression SHALL evaluate to "True":
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:all-of-any”>
     * <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>10</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>20</AttributeValue>
     * </Apply>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>1</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>5</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>19</AttributeValue>
     * </Apply>
     * </Apply>
     * This expression is “True” because each of the elements of the first bag is greater than at
     * least one of the elements of the second bag.
     *
     * @throws XACML3EntitlementException
     */
    @Test
    public void testAllOfAny() throws XACML3EntitlementException {
        final FunctionArgument testInteger1 = new DataValue(DataType.XACMLINTEGER, 10, true);
        final FunctionArgument testInteger2 = new DataValue(DataType.XACMLINTEGER, 20, true);
        final FunctionArgument testInteger3 = new DataValue(DataType.XACMLINTEGER, 1, true);
        final FunctionArgument testInteger4 = new DataValue(DataType.XACMLINTEGER, 3, true);
        final FunctionArgument testInteger5 = new DataValue(DataType.XACMLINTEGER, 5, true);
        final FunctionArgument testInteger6 = new DataValue(DataType.XACMLINTEGER, 19, true);

        AllOfAny allOfAny = new AllOfAny();

        IntegerGreaterThan integerGreaterThan = new IntegerGreaterThan();

        IntegerBag bag1 = new IntegerBag();
        bag1.addArgument(testInteger1);
        bag1.addArgument(testInteger2);

        IntegerBag bag2 = new IntegerBag();
        bag2.addArgument(testInteger3);
        bag2.addArgument(testInteger4);
        bag2.addArgument(testInteger5);
        bag2.addArgument(testInteger6);

        allOfAny.addArgument(integerGreaterThan);
        allOfAny.addArgument(bag1);
        allOfAny.addArgument(bag2);

        FunctionArgument result = allOfAny.evaluate(null);
        assertNotNull(result);
        assertEquals(result.isTrue(), true);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:any-of-all
     * This function applies a Boolean function between the elements of two bags.
     * The expression SHALL be “True” if and only if the supplied predicate is “True” between each
     * element of the second bag and any element of the first bag.
     *
     * This function SHALL take three arguments.
     *
     * The first argument SHALL be an <Function> element that names a Boolean function that takes two
     * arguments of primitive types.  The second argument SHALL be a bag of a primitive data-type.
     * The third argument SHALL be a bag of a primitive data-type.
     * The expression SHALL be evaluated as if the ”urn:oasis:names:tc:xacml:3.0:function:any-of”
     * function had been applied to each value of the second bag and the whole of the
     * first bag using the supplied xacml:Function, and the results were then
     * combined using “urn:oasis:names:tc:xacml:1.0:function:and”.
     * <p/>
     * For example, the following expression SHALL evaluate to "True":
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:any-of-all”>
     * <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>5</AttributeValue>
     * </Apply>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>1</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
     * </Apply>
     * </Apply>
     * This expression is “True” because, for all of the values in the second bag,
     * there is a value in the first bag that is greater.
     *
     * @throws XACML3EntitlementException
     */
    @Test
    public void testAnyOfAll() throws XACML3EntitlementException {
        final FunctionArgument testInteger1 = new DataValue(DataType.XACMLINTEGER, 3, true);
        final FunctionArgument testInteger2 = new DataValue(DataType.XACMLINTEGER, 5, true);
        final FunctionArgument testInteger3 = new DataValue(DataType.XACMLINTEGER, 1, true);
        final FunctionArgument testInteger4 = new DataValue(DataType.XACMLINTEGER, 2, true);
        final FunctionArgument testInteger5 = new DataValue(DataType.XACMLINTEGER, 3, true);
        final FunctionArgument testInteger6 = new DataValue(DataType.XACMLINTEGER, 4, true);

        AnyOfAll anyOfAll = new AnyOfAll();

        IntegerGreaterThan integerGreaterThan = new IntegerGreaterThan();

        IntegerBag bag1 = new IntegerBag();
        bag1.addArgument(testInteger1);
        bag1.addArgument(testInteger2);

        IntegerBag bag2 = new IntegerBag();
        bag2.addArgument(testInteger3);
        bag2.addArgument(testInteger4);
        bag2.addArgument(testInteger5);
        bag2.addArgument(testInteger6);

        anyOfAll.addArgument(integerGreaterThan);
        anyOfAll.addArgument(bag1);
        anyOfAll.addArgument(bag2);

        FunctionArgument result = anyOfAll.evaluate(null);
        assertNotNull(result);
        assertEquals(result.isTrue(), true);

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:any-of-any
     * This function applies a Boolean function on each tuple from the cross product on all bags
     * arguments, and returns "True" if and only if the predicate is "True" for at least one inside-function call.
     * This function SHALL take n+1 arguments, where n is one or greater.
     * The first argument SHALL be an <Function> element that names a Boolean function that takes n arguments.
     * The remaining arguments are either primitive data types or bags of primitive types.
     * The expression SHALL be evaluated as if the function named in the <Function> argument was applied between
     * every tuple of the cross product on all bags and the primitive values, and the results were
     * combined using “urn:oasis:names:tc:xacml:1.0:function:or”.
     * The semantics are that the result of the expression SHALL be "True" if and only if the applied predicate is
     * "True" for at least one function call on the tuples from the bags and primitive values.
     * <p/>
     * For example, the following expression SHALL evaluate to "True":
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:any-of-any”>
     * <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-equal”/>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Mary</AttributeValue>
     * </Apply>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>John</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>George</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
     * </Apply>
     * </Apply>
     * This expression is "True" because at least one of the elements of the first bag, namely “Ringo”,
     * is equal to at least one of the elements of the second bag.
     */
    @Test
    public void testAnyOfAny() throws XACML3EntitlementException {
        final FunctionArgument testString1 = new DataValue(DataType.XACMLSTRING, "John");
        final FunctionArgument testString2 = new DataValue(DataType.XACMLSTRING, "Paul");
        final FunctionArgument testString3 = new DataValue(DataType.XACMLSTRING, "George");
        final FunctionArgument testString4 = new DataValue(DataType.XACMLSTRING, "Ringo");
        final FunctionArgument testString5 = new DataValue(DataType.XACMLSTRING, "Mary");

        AnyOfAny anyOfAny = new AnyOfAny();

        StringEqual equals = new StringEqual();

        StringBag bag1 = new StringBag();
        bag1.addArgument(testString4);
        bag1.addArgument(testString5);

        StringBag bag2 = new StringBag();
        bag2.addArgument(testString1);
        bag2.addArgument(testString2);
        bag2.addArgument(testString3);
        bag2.addArgument(testString4);

        anyOfAny.addArgument(equals);
        anyOfAny.addArgument(bag1);
        anyOfAny.addArgument(bag2);

        FunctionArgument result = anyOfAny.evaluate(null);
        assertNotNull(result);
        assertEquals(result.isTrue(), true);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:all-of-all
     * This function applies a Boolean function between the elements of two bags.
     * The expression SHALL be "True" if and only if the supplied predicate is "True" between each and every element of the
     * first bag collectively against all the elements of the second bag.
     * This function SHALL take three arguments.  The first argument SHALL be an <Function> element that names a
     * Boolean function that takes two arguments of primitive types.
     * The second argument SHALL be a bag of a primitive data-type.
     * The third argument SHALL be a bag of a primitive data-type.
     * The expression is evaluated as if the function named in the <Function> element were applied between every element
     * of the second argument and every element of the third argument
     * and the results were combined using “urn:oasis:names:tc:xacml:1.0:function:and”.
     * The semantics are that the result of the expression is "True" if and only if the applied predicate is "True" for
     * all elements of the first bag compared to all the elements of the second bag.
     * <p/>
     * For example, the following expression SHALL evaluate to "True":
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:all-of-all”>
     * <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>6</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>5</AttributeValue>
     * </Apply>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>1</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
     * </Apply>
     * </Apply>
     * This expression is "True" because all elements of the first bag, “5” and “6”,
     * are each greater than all of the integer values “1”, ”2”, ”3”, ”4” of the second bag.
     *
     * @throws XACML3EntitlementException
     */
    @Test
    public void testAllOfAll() throws XACML3EntitlementException {
        final FunctionArgument testInteger1 = new DataValue(DataType.XACMLINTEGER, 6, true);
        final FunctionArgument testInteger2 = new DataValue(DataType.XACMLINTEGER, 5, true);
        final FunctionArgument testInteger3 = new DataValue(DataType.XACMLINTEGER, 1, true);
        final FunctionArgument testInteger4 = new DataValue(DataType.XACMLINTEGER, 2, true);
        final FunctionArgument testInteger5 = new DataValue(DataType.XACMLINTEGER, 3, true);
        final FunctionArgument testInteger6 = new DataValue(DataType.XACMLINTEGER, 4, true);

        AllOfAll allOfAll = new AllOfAll();

        IntegerGreaterThan integerGreaterThan = new IntegerGreaterThan();

        IntegerBag bag1 = new IntegerBag();
        bag1.addArgument(testInteger1);
        bag1.addArgument(testInteger2);

        IntegerBag bag2 = new IntegerBag();
        bag2.addArgument(testInteger3);
        bag2.addArgument(testInteger4);
        bag2.addArgument(testInteger5);
        bag2.addArgument(testInteger6);

        allOfAll.addArgument(integerGreaterThan);
        allOfAll.addArgument(bag1);
        allOfAll.addArgument(bag2);

        FunctionArgument result = allOfAll.evaluate(null);
        assertNotNull(result);
        assertEquals(result.isTrue(), true);
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:map
     * This function converts a bag of values to another bag of values.
     *
     * This function SHALL take n+1 arguments, where n is one or greater.
     *
     * The first argument SHALL be a <Function> element naming a function that takes a n arguments of a primitive
     * data-type and returns a value of a primitive data-type Under the remaining n arguments,
     * n-1 parameters SHALL be values of primitive data-types and one SHALL be a bag of a primitive data-type.
     * The expression SHALL be evaluated as if the function named in the <Function> argument were applied to the n-1
     * non-bag arguments and each element of the bag argument and resulting in a bag of the converted value.
     *
     * The result SHALL be a bag of the primitive data-type that is returned by the
     * function named in the <xacml:Function> element.
     *
     * <p/>
     * For example, the following expression,
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:map”>
     * <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case”>
     * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Hello</AttributeValue>
     * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>World!</AttributeValue>
     * </Apply>
     * </Apply>
     * evaluates to a bag containing “hello” and “world!”.
     *
     * @throws XACML3EntitlementException
     */
    @Test
    public void testMap() throws XACML3EntitlementException {
        final FunctionArgument testString1 = new DataValue(DataType.XACMLSTRING, "Hello");
        final FunctionArgument testString2 = new DataValue(DataType.XACMLSTRING, "World!");

        Map map = new Map();
        StringNormalizeToLowerCase stringNormalizeToLowerCase = new StringNormalizeToLowerCase();

        StringBag bag = new StringBag();
        bag.addArgument(testString1);
        bag.addArgument(testString2);

        map.addArgument(stringNormalizeToLowerCase);
        map.addArgument(bag);

        FunctionArgument result = map.evaluate(null);
        assertNotNull(result);
        assertTrue(result instanceof DataBag, "Result is not a DataBag, very Bad!");
        assertEquals(((DataBag) result).size(), 2);

        // Check Bag Elements...
        assertEquals(((DataBag) result).get(0).asString(null), "hello");
        assertEquals(((DataBag) result).get(1).asString(null), "world!");

    }


}
