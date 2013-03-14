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

/**
 * A.3.12 Higher-order bag functions
 This section describes functions in XACML that perform operations on bags such that functions may be applied to the bags in general.
 urn:oasis:names:tc:xacml:3.0:function:any-of
 This function applies a Boolean function between specific primitive values and a bag of values, and SHALL return "True" if and only if the predicate is "True" for at least one element of the bag.
 This function SHALL take n+1 arguments, where n is one or greater. The first argument SHALL be an <Function> element that names a Boolean function that takes n arguments of primitive types.  Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one SHALL be a bag of a primitive data-type.  The expression SHALL be evaluated as if the function named in the <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument and the results are combined with “urn:oasis:names:tc:xacml:1.0:function:or”.

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
 This expression is "True" because the first argument is equal to at least one of the elements of the bag, according to the function.

 urn:oasis:names:tc:xacml:3.0:function:all-of
 This function applies a Boolean function between a specific primitive value and a bag of values, and returns "True" if and only if the predicate is "True" for every element of the bag.
 This function SHALL take n+1 arguments, where n is one or greater.  The first argument SHALL be a <Function> element that names a Boolean function that takes n arguments of primitive types. Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one SHALL be a bag of a primitive data-type. The expression SHALL be evaluated as if the function named in the <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument and the results are combined with “urn:oasis:names:tc:xacml:1.0:function:and”.

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
 This function applies a Boolean function on each tuple from the cross product on all bags arguments, and returns "True" if and only if the predicate is "True" for at least one inside-function call.
 This function SHALL take n+1 arguments, where n is one or greater.  The first argument SHALL be an <Function> element that names a Boolean function that takes n arguments. The remaining arguments are either primitive data types or bags of primitive types.  The expression SHALL be evaluated as if the function named in the <Function> argument was applied between every tuple of the cross product on all bags and the primitive values, and the results were combined using “urn:oasis:names:tc:xacml:1.0:function:or”.  The semantics are that the result of the expression SHALL be "True" if and only if the applied predicate is "True" for at least one function call on the tuples from the bags and primitive values.

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
 This expression is "True" because at least one of the elements of the first bag, namely “Ringo”, is equal to at least one of the elements of the second bag.

 urn:oasis:names:tc:xacml:1.0:function:all-of-any
 This function applies a Boolean function between the elements of two bags.  The expression SHALL be “True” if and only if the supplied predicate is “True” between each element of the first bag and any element of the second bag.
 This function SHALL take three arguments.  The first argument SHALL be an <Function> element that names a Boolean function that takes two arguments of primitive types.  The second argument SHALL be a bag of a primitive data-type.  The third argument SHALL be a bag of a primitive data-type.  The expression SHALL be evaluated as if the “urn:oasis:names:tc:xacml:3.0:function:any-of” function had been applied to each value of the first bag and the whole of the second bag using the supplied xacml:Function, and the results were then combined using “urn:oasis:names:tc:xacml:1.0:function:and”.
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
 This expression is “True” because each of the elements of the first bag is greater than at least one of the elements of the second bag.

 urn:oasis:names:tc:xacml:1.0:function:any-of-all
 This function applies a Boolean function between the elements of two bags.  The expression SHALL be “True” if and only if the supplied predicate is “True” between each element of the second bag and any element of the first bag.
 This function SHALL take three arguments.  The first argument SHALL be an <Function> element that names a Boolean function that takes two arguments of primitive types.  The second argument SHALL be a bag of a primitive data-type.  The third argument SHALL be a bag of a primitive data-type.  The expression SHALL be evaluated as if the ”urn:oasis:names:tc:xacml:3.0:function:any-of” function had been applied to each value of the second bag and the whole of the first bag using the supplied xacml:Function, and the results were then combined using “urn:oasis:names:tc:xacml:1.0:function:and”.
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
 This expression is “True” because, for all of the values in the second bag, there is a value in the first bag that is greater.

 urn:oasis:names:tc:xacml:1.0:function:all-of-all
 This function applies a Boolean function between the elements of two bags.  The expression SHALL be "True" if and only if the supplied predicate is "True" between each and every element of the first bag collectively against all the elements of the second bag.
 This function SHALL take three arguments.  The first argument SHALL be an <Function> element that names a Boolean function that takes two arguments of primitive types.  The second argument SHALL be a bag of a primitive data-type.  The third argument SHALL be a bag of a primitive data-type.  The expression is evaluated as if the function named in the <Function> element were applied between every element of the second argument and every element of the third argument  and the results were combined using “urn:oasis:names:tc:xacml:1.0:function:and”.  The semantics are that the result of the expression is "True" if and only if the applied predicate is "True" for all elements of the first bag compared to all the elements of the second bag.
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
 This expression is "True" because all elements of the first bag, “5” and “6”, are each greater than all of the integer values “1”, ”2”, ”3”, ”4” of the second bag.

 urn:oasis:names:tc:xacml:3.0:function:map
 This function converts a bag of values to another bag of values.
 This function SHALL take n+1 arguments, where n is one or greater.  The first argument SHALL be a <Function> element naming a function that takes a n arguments of a primitive data-type and returns a value of a primitive data-type Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one SHALL be a bag of a primitive data-type. The expression SHALL be evaluated as if the function named in the <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument and resulting in a bag of the converted value.  The result SHALL be a bag of the primitive data-type that is returned by the function named in the <xacml:Function> element.
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
 * @author Jeff.Schenk@ForgeRock.com
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
     *
     */
    @Test
    public void testOne() throws XACML3EntitlementException {

    }

}
