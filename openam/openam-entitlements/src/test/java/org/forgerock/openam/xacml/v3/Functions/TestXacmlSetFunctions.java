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

/**
 * A.3.11 Set functions
 These functions operate on bags mimicking sets by eliminating duplicate elements from a bag.

 urn:oasis:names:tc:xacml:x.x:function:type-intersection
 This function SHALL take two arguments that are both a bag of ‘type’ values.
 It SHALL return a bag of ‘type’ values such that it contains only elements that are common between the two bags,
 which is determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal".
 No duplicates, as determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal", SHALL exist in the result.

 urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
 This function SHALL take two arguments that are both a bag of ‘type’ values.
 It SHALL return a “http://www.w3.org/2001/XMLSchema#boolean”.  The function SHALL evaluate to "True" if and
 only if at least one element of the first argument is contained in the second argument as determined by
 "urn:oasis:names:tc:xacml:x.x:function:type-is-in".

 urn:oasis:names:tc:xacml:x.x:function:type-union
 This function SHALL take two or more arguments that are both a bag of ‘type’ values.
 The expression SHALL return a bag of ‘type’ such that it contains all elements of all the argument bags.
 No duplicates, as determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal", SHALL exist in the result.

 urn:oasis:names:tc:xacml:x.x:function:type-subset
 This function SHALL take two arguments that are both a bag of ‘type’ values.
 It SHALL return a “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the
 first argument is a subset of the second argument.  Each argument SHALL be considered to have had its
 duplicates removed, as determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal", before the subset calculation.

 urn:oasis:names:tc:xacml:x.x:function:type-set-equals
 This function SHALL take two arguments that are both a bag of ‘type’ values.
 It SHALL return a “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return the result of applying
 "urn:oasis:names:tc:xacml:1.0:function:and" to the application of "urn:oasis:names:tc:xacml:x.x:function:type-subset"
 to the first and second arguments and the application of "urn:oasis:names:tc:xacml:x.x:function:type-subset"
 to the second and first arguments.

 */

/**
 * XACML Set Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlSetFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_AnyuriIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_Base64BinaryIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_BooleanIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DateIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DatetimeIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DaytimedurationIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DoubleIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_HexbinaryIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_IntegerIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_Rfc822NameIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_StringIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_TimeIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_X500NameIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_YearmonthdurationIntersection() throws XACML3EntitlementException {
        // TODO :: Finish...
    }


    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_AnyuriAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_Base64BinaryAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_BooleanAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_DateAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_DatetimeAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_DaytimedurationAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_DoubleAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_HexbinaryAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_IntegerAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_Rfc822NameAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_StringAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_TimeAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_X500NameAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-at-least-one-member-of
     */
    @Test
    public void test_YearmonthdurationAtLeastOneMemberOf() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_AnyuriUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_Base64BinaryUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_BooleanUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_DateUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_DatetimeUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_DaytimedurationUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_DoubleUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_HexbinaryUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_IntegerUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_Rfc822NameUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_StringUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_TimeUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_X500NameUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-union
     */
    @Test
    public void test_YearmonthdurationUnion() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_AnyuriSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_Base64BinarySubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_BooleanSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_DateSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_DatetimeSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_DaytimedurationSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_DoubleSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_HexbinarySubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_IntegerSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_Rfc822NameSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_StringSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_TimeSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_X500NameSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-subset
     */
    @Test
    public void test_YearmonthdurationSubset() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_AnyuriSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_Base64BinarySetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_BooleanSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_DateSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_DatetimeSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_DaytimedurationSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_DoubleSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_HexbinarySetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_IntegerSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_Rfc822NameSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_StringSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_TimeSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_X500NameSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_YearmonthdurationSetEquals() throws XACML3EntitlementException {
        // TODO :: Finish...
    }



}
