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

import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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
public class TestXacmlBagSetFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    static final DataValue HELLO_WORLD = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");
    static final DataValue HELLO_WORLD_FORGEROCK = new DataValue(DataType.XACMLSTRING, "HELLO WORLD From ForgeRock!");
    static final DataValue ONE = new DataValue(DataType.XACMLSTRING, "HELLO WORLD ONE");
    static final DataValue TWO = new DataValue(DataType.XACMLSTRING, "HELLO WORLD TWO");
    static final DataValue THREE = new DataValue(DataType.XACMLSTRING, "HELLO WORLD THREE");
    static final DataValue FOUR = new DataValue(DataType.XACMLSTRING, "HELLO WORLD FOUR");
    static final DataValue FIVE = new DataValue(DataType.XACMLSTRING, "HELLO WORLD FIVE");
    static final DataValue SIX = new DataValue(DataType.XACMLSTRING, "HELLO WORLD SIX");

    static final FunctionArgument anyuri1 = new DataValue(DataType.XACMLANYURI, "/openam/xacml");
    static final FunctionArgument anyuri2 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");
    static final FunctionArgument anyuri3 = new DataValue(DataType.XACMLANYURI, "/");
    static final FunctionArgument anyuri4 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");

    // base64data1 and base64data2 contained the Base 64 encoding of:
    // ForgeRock - OpenAM XACML says Hello!
    final FunctionArgument base64data1 = new DataValue(DataType.XACMLBASE64BINARY,
            "Rm9yZ2VSb2NrIC0gT3BlbkFNIFhBQ01MIHNheXMgSGVsbG8h");
    // This is a very small Test!
    final FunctionArgument base64data2 = new DataValue(DataType.XACMLBASE64BINARY,
            "VGhpcyBpcyBhIHZlcnkgc21hbGwgVGVzdCE=");
    // This is a very small Test as well!
    final FunctionArgument base64data3 = new DataValue(DataType.XACMLBASE64BINARY,
            "VGhpcyBpcyBhIHZlcnkgc21hbGwgVGVzdCBhcyB3ZWxsIQ==");
    // ForgeRock - OpenAM XACML says Hello!
    final FunctionArgument base64data4 = new DataValue(DataType.XACMLBASE64BINARY,
            "Rm9yZ2VSb2NrIC0gT3BlbkFNIFhBQ01MIHNheXMgSGVsbG8h");

    final Date date1 = XACML3PrivilegeUtils.stringToDate("2013-03-11");
    final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATE, date1, true);

    final Date date2 = XACML3PrivilegeUtils.stringToDate("2013-03-12");
    final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDATE, date2, true);

    final Date date3 = XACML3PrivilegeUtils.stringToDate("2013-03-11");
    final FunctionArgument dateObject3 = new DataValue(DataType.XACMLDATE, date3, true);

    final Date date4 = XACML3PrivilegeUtils.stringToDate("2014-03-11");
    final FunctionArgument dateObject4 = new DataValue(DataType.XACMLDATE, date4, true);

    final Date date5 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
    final FunctionArgument dateTimeObject1 = new DataValue(DataType.XACMLDATETIME, date5, true);

    final Date date6 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.124");
    final FunctionArgument dateTimeObject2 = new DataValue(DataType.XACMLDATETIME, date6, true);

    final Date date7 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
    final FunctionArgument dateTimeObject3 = new DataValue(DataType.XACMLDATETIME, date7, true);

    final Date date8 = XACML3PrivilegeUtils.stringToDateTime("2014-03-11:01:45:30.126");
    final FunctionArgument dateTimeObject4 = new DataValue(DataType.XACMLDATETIME, date8, true);

    final FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
    final FunctionArgument double2 = new DataValue(DataType.XACMLDOUBLE, 456789D, true);
    final FunctionArgument double3 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
    final FunctionArgument double4 = new DataValue(DataType.XACMLDOUBLE, 2D, true);

    final FunctionArgument hexdata1 = new DataValue(DataType.XACMLHEXBINARY, "0123456789abcdef");
    final FunctionArgument hexdata2 = new DataValue(DataType.XACMLHEXBINARY, "FF");
    final FunctionArgument hexdata3 = new DataValue(DataType.XACMLHEXBINARY, "0123456789ABCDEF");
    final FunctionArgument hexdata4 = new DataValue(DataType.XACMLHEXBINARY, "06F2");
    final FunctionArgument hexdata5 = new DataValue(DataType.XACMLHEXBINARY, "CED");

    final FunctionArgument integer1 = new DataValue(DataType.XACMLINTEGER, 22, true);
    final FunctionArgument integer2 = new DataValue(DataType.XACMLINTEGER, 456789, true);
    final FunctionArgument integer3 = new DataValue(DataType.XACMLINTEGER, 22, true);
    final FunctionArgument integer4 = new DataValue(DataType.XACMLINTEGER, 0, true);

    final FunctionArgument rfc822Name1 = new DataValue(DataType.XACMLRFC822NAME,
            "joe@example.org");
    final FunctionArgument rfc822Name2 = new DataValue(DataType.XACMLRFC822NAME,
            "joe.smith@example.org");
    final FunctionArgument rfc822Name3 = new DataValue(DataType.XACMLRFC822NAME,
            "joe.smith@example.org");
    final FunctionArgument rfc822Name4 = new DataValue(DataType.XACMLRFC822NAME,
            "joe.smith@ExAmPlE.oRg");

    final Date time1 = XACML3PrivilegeUtils.stringToTime("01:45:30.126");
    final FunctionArgument timeObject1 = new DataValue(DataType.XACMLTIME, time1, true);

    final Date time2 = XACML3PrivilegeUtils.stringToTime("02:45:30.126");
    final FunctionArgument timeObject2 = new DataValue(DataType.XACMLTIME, time2, true);

    final Date time3 = XACML3PrivilegeUtils.stringToTime("01:45:30.126");
    final FunctionArgument timeObject3 = new DataValue(DataType.XACMLTIME, time3, true);

    final Date time4 = XACML3PrivilegeUtils.stringToTime("01:45:30.127");
    final FunctionArgument timeObject4 = new DataValue(DataType.XACMLTIME, time4, true);

    final FunctionArgument x500Name1 = new DataValue(DataType.XACMLX500NAME,
            "/c=us/o=ForgeRock/ou=Components/cn=OpenAM");
    final FunctionArgument x500Name2 = new DataValue(DataType.XACMLX500NAME,
            "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");
    final FunctionArgument x500Name3 = new DataValue(DataType.XACMLX500NAME,
            "/cn=Bob Smith");
    final FunctionArgument x500Name4 = new DataValue(DataType.XACMLX500NAME,
            "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");


    final XACML3YearMonthDuration duration1 = new XACML3YearMonthDuration("0020-03");
    final FunctionArgument dateObject5 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration1, true);

    final XACML3YearMonthDuration duration2 = new XACML3YearMonthDuration("0016-03");
    final FunctionArgument dateObject6 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration2, true);

    final XACML3YearMonthDuration duration3 = new XACML3YearMonthDuration("0013-03");
    final FunctionArgument dateObject7 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration3, true);

    final XACML3YearMonthDuration duration4 = new XACML3YearMonthDuration("0020-03");
    final FunctionArgument dateObject8 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration4, true);


    final Long duration5 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("011:01:45:30.126");
    final FunctionArgument dateObject9 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration5, true);

    final Long duration6 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("012:01:45:30.124");
    final FunctionArgument dateObjectA = new DataValue(DataType.XACMLDAYTIMEDURATION, duration6, true);

    final Long duration7 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("011:01:45:30.126");
    final FunctionArgument dateObjectB = new DataValue(DataType.XACMLDAYTIMEDURATION, duration7, true);

    final Long duration8 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("001:01:45:30.126");
    final FunctionArgument dateObjectC = new DataValue(DataType.XACMLDAYTIMEDURATION, duration8, true);


    final FunctionArgument dnsName1 = new DataValue(DataType.XACMLDNSNAME,
            "www.example.org");
    final FunctionArgument dnsName2 = new DataValue(DataType.XACMLDNSNAME,
            "example.com");
    final FunctionArgument dnsName3 = new DataValue(DataType.XACMLDNSNAME,
            "www.example.com");
    final FunctionArgument dnsName4 = new DataValue(DataType.XACMLDNSNAME,
            "openam.example.org");

    final FunctionArgument ipaddr1 = new DataValue(DataType.XACMLIPADDRESS,
            "10.0.0.1");
    final FunctionArgument ipaddr2 = new DataValue(DataType.XACMLIPADDRESS,
            "10.0.200.1");
    final FunctionArgument ipaddr3 = new DataValue(DataType.XACMLIPADDRESS,
            "10.0.12.1");
    final FunctionArgument ipaddr4 = new DataValue(DataType.XACMLIPADDRESS,
            "10.0.195.1");



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
        // Create a Array of Bags and stuff it with DataValues.
        AnyuriBag[] bags = new  AnyuriBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new AnyuriBag();
            bags[i].addArgument(anyuri1);
            bags[i].addArgument(anyuri2);
            bags[i].addArgument(anyuri3);
            bags[i].addArgument(anyuri4);
            bags[i].addArgument(anyuri1);
            bags[i].addArgument(anyuri2);
            bags[i].addArgument(anyuri3);
            bags[i].addArgument(anyuri4);
            bags[i].addArgument(anyuri1);
            bags[i].addArgument(anyuri2);
            bags[i].addArgument(anyuri3);
            bags[i].addArgument(anyuri4);

        }

        // Establish Intersection Function
        AnyuriIntersection intersection = new AnyuriIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_Base64BinaryIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        Base64BinaryBag[] bags = new  Base64BinaryBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new Base64BinaryBag();
            bags[i].addArgument(base64data1);
            bags[i].addArgument(base64data2);
            bags[i].addArgument(base64data3);
            bags[i].addArgument(base64data4);
            bags[i].addArgument(base64data1);
            bags[i].addArgument(base64data2);
            bags[i].addArgument(base64data3);
            bags[i].addArgument(base64data4);
            bags[i].addArgument(base64data1);
            bags[i].addArgument(base64data2);
            bags[i].addArgument(base64data3);
            bags[i].addArgument(base64data4);

        }

        // Establish Intersection Function
        Base64BinaryIntersection intersection = new Base64BinaryIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_BooleanIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        BooleanBag[] bags = new  BooleanBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new BooleanBag();
            bags[i].addArgument(trueObject);
            bags[i].addArgument(trueObject);
            bags[i].addArgument(trueObject);
            bags[i].addArgument(falseObject);
            bags[i].addArgument(trueObject);
            bags[i].addArgument(trueObject);
            bags[i].addArgument(falseObject);
            bags[i].addArgument(trueObject);
            bags[i].addArgument(falseObject);
            bags[i].addArgument(falseObject);
            bags[i].addArgument(falseObject);
            bags[i].addArgument(falseObject);

        }

        // Establish Intersection Function
        BooleanIntersection intersection = new BooleanIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 2);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DateIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        DateBag[] bags = new  DateBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new DateBag();
            bags[i].addArgument(dateObject1);
            bags[i].addArgument(dateObject2);
            bags[i].addArgument(dateObject3);
            bags[i].addArgument(dateObject4);
            bags[i].addArgument(dateObject1);
            bags[i].addArgument(dateObject2);
            bags[i].addArgument(dateObject3);
            bags[i].addArgument(dateObject4);
            bags[i].addArgument(dateObject1);
            bags[i].addArgument(dateObject2);
            bags[i].addArgument(dateObject3);
            bags[i].addArgument(dateObject4);
            bags[i].addArgument(dateObject1);
            bags[i].addArgument(dateObject2);
            bags[i].addArgument(dateObject3);
            bags[i].addArgument(dateObject4);

        }

        // Establish Intersection Function
        DateIntersection intersection = new DateIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DatetimeIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        DatetimeBag[] bags = new  DatetimeBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new DatetimeBag();
            bags[i].addArgument(dateTimeObject1);
            bags[i].addArgument(dateTimeObject2);
            bags[i].addArgument(dateTimeObject3);
            bags[i].addArgument(dateTimeObject4);
            bags[i].addArgument(dateTimeObject1);
            bags[i].addArgument(dateTimeObject2);
            bags[i].addArgument(dateTimeObject3);
            bags[i].addArgument(dateTimeObject4);
            bags[i].addArgument(dateTimeObject1);
            bags[i].addArgument(dateTimeObject2);
            bags[i].addArgument(dateTimeObject3);
            bags[i].addArgument(dateTimeObject4);
            bags[i].addArgument(dateTimeObject1);
            bags[i].addArgument(dateTimeObject2);
            bags[i].addArgument(dateTimeObject3);
            bags[i].addArgument(dateTimeObject4);
            bags[i].addArgument(dateTimeObject1);
            bags[i].addArgument(dateTimeObject2);
            bags[i].addArgument(dateTimeObject3);
            bags[i].addArgument(dateTimeObject4);
        }

        // Establish Intersection Function
        DatetimeIntersection intersection = new DatetimeIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DaytimedurationIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        DaytimedurationBag[] bags = new  DaytimedurationBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new DaytimedurationBag();
            bags[i].addArgument(dateObject9);
            bags[i].addArgument(dateObjectA);
            bags[i].addArgument(dateObjectB);
            bags[i].addArgument(dateObjectC);
            bags[i].addArgument(dateObject9);
            bags[i].addArgument(dateObjectA);
            bags[i].addArgument(dateObjectB);
            bags[i].addArgument(dateObjectC);
            bags[i].addArgument(dateObject9);
            bags[i].addArgument(dateObjectA);
            bags[i].addArgument(dateObjectB);
            bags[i].addArgument(dateObjectC);
            bags[i].addArgument(dateObject9);
            bags[i].addArgument(dateObjectA);
            bags[i].addArgument(dateObjectB);
            bags[i].addArgument(dateObjectC);

        }

        // Establish Intersection Function
       DaytimedurationIntersection intersection = new DaytimedurationIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_DoubleIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        DoubleBag[] bags = new  DoubleBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new DoubleBag();
            bags[i].addArgument(double1);
            bags[i].addArgument(double2);
            bags[i].addArgument(double3);
            bags[i].addArgument(double4);
            bags[i].addArgument(double1);
            bags[i].addArgument(double2);
            bags[i].addArgument(double3);
            bags[i].addArgument(double4);
            bags[i].addArgument(double1);
            bags[i].addArgument(double2);
            bags[i].addArgument(double3);
            bags[i].addArgument(double4);
            bags[i].addArgument(double1);
            bags[i].addArgument(double2);
            bags[i].addArgument(double3);
            bags[i].addArgument(double4);
            bags[i].addArgument(double1);
            bags[i].addArgument(double2);
            bags[i].addArgument(double3);
            bags[i].addArgument(double4);

        }

        // Establish Intersection Function
        DoubleIntersection intersection = new DoubleIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_HexbinaryIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        HexbinaryBag[] bags = new  HexbinaryBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new HexbinaryBag();
            bags[i].addArgument(hexdata1);
            bags[i].addArgument(hexdata2);
            bags[i].addArgument(hexdata3);
            bags[i].addArgument(hexdata4);
            bags[i].addArgument(hexdata5);
            bags[i].addArgument(hexdata1);
            bags[i].addArgument(hexdata2);
            bags[i].addArgument(hexdata3);
            bags[i].addArgument(hexdata4);
            bags[i].addArgument(hexdata5);
            bags[i].addArgument(hexdata1);
            bags[i].addArgument(hexdata2);
            bags[i].addArgument(hexdata3);
            bags[i].addArgument(hexdata4);
            bags[i].addArgument(hexdata5);
            bags[i].addArgument(hexdata1);
            bags[i].addArgument(hexdata2);
            bags[i].addArgument(hexdata3);
            bags[i].addArgument(hexdata4);
            bags[i].addArgument(hexdata5);


        }

        // Establish Intersection Function
        HexbinaryIntersection intersection = new HexbinaryIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 5);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_IntegerIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        IntegerBag[] bags = new  IntegerBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new IntegerBag();
            bags[i].addArgument(integer1);
            bags[i].addArgument(integer2);
            bags[i].addArgument(integer3);
            bags[i].addArgument(integer4);
            bags[i].addArgument(integer1);
            bags[i].addArgument(integer2);
            bags[i].addArgument(integer3);
            bags[i].addArgument(integer4);
            bags[i].addArgument(integer1);
            bags[i].addArgument(integer2);
            bags[i].addArgument(integer3);
            bags[i].addArgument(integer4);
            bags[i].addArgument(integer1);
            bags[i].addArgument(integer2);
            bags[i].addArgument(integer3);
            bags[i].addArgument(integer4);

        }

        // Establish Intersection Function
        IntegerIntersection intersection = new IntegerIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_Rfc822NameIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        Rfc822NameBag[] bags = new  Rfc822NameBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new Rfc822NameBag();
            bags[i].addArgument(rfc822Name1);
            bags[i].addArgument(rfc822Name2);
            bags[i].addArgument(rfc822Name3);
            bags[i].addArgument(rfc822Name4);
            bags[i].addArgument(rfc822Name1);
            bags[i].addArgument(rfc822Name2);
            bags[i].addArgument(rfc822Name3);
            bags[i].addArgument(rfc822Name4);
            bags[i].addArgument(rfc822Name1);
            bags[i].addArgument(rfc822Name2);
            bags[i].addArgument(rfc822Name3);
            bags[i].addArgument(rfc822Name4);

        }

        // Establish Intersection Function
        Rfc822NameIntersection intersection = new Rfc822NameIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_StringIntersection() throws XACML3EntitlementException {

        // Create a StringBag Array and stuff it with DataValues.
        StringBag[] stringBags = new StringBag[2];
        for (int i = 0; i < stringBags.length; i++) {
            stringBags[i] = new StringBag();
            stringBags[i].addArgument(HELLO_WORLD);
            stringBags[i].addArgument(HELLO_WORLD_FORGEROCK);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
        }

        // Establish Intersection Function
        StringIntersection intersection = new StringIntersection();
        // Push Bags Into Function
        for (int i = 0; i < stringBags.length; i++) {
            intersection.addArgument(stringBags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 8);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_TimeIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        TimeBag[] bags = new  TimeBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new TimeBag();
            bags[i].addArgument(timeObject1);
            bags[i].addArgument(timeObject2);
            bags[i].addArgument(timeObject3);
            bags[i].addArgument(timeObject4);
            bags[i].addArgument(timeObject1);
            bags[i].addArgument(timeObject2);
            bags[i].addArgument(timeObject3);
            bags[i].addArgument(timeObject4);
            bags[i].addArgument(timeObject1);
            bags[i].addArgument(timeObject2);
            bags[i].addArgument(timeObject3);
            bags[i].addArgument(timeObject4);


        }

        // Establish Intersection Function
        TimeIntersection intersection = new TimeIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_X500NameIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        X500NameBag[] bags = new  X500NameBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new X500NameBag();
            bags[i].addArgument(x500Name1);
            bags[i].addArgument(x500Name2);
            bags[i].addArgument(x500Name3);
            bags[i].addArgument(x500Name4);
            bags[i].addArgument(x500Name1);
            bags[i].addArgument(x500Name2);
            bags[i].addArgument(x500Name3);
            bags[i].addArgument(x500Name4);
            bags[i].addArgument(x500Name1);
            bags[i].addArgument(x500Name2);
            bags[i].addArgument(x500Name3);
            bags[i].addArgument(x500Name4);
            bags[i].addArgument(x500Name1);
            bags[i].addArgument(x500Name2);
            bags[i].addArgument(x500Name3);
            bags[i].addArgument(x500Name4);

        }

        // Establish Intersection Function
        X500NameIntersection intersection = new X500NameIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-intersection
     */
    @Test
    public void test_YearmonthdurationIntersection() throws XACML3EntitlementException {
        // Create a Array of Bags and stuff it with DataValues.
        YearmonthdurationBag[] bags = new  YearmonthdurationBag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new YearmonthdurationBag();
            bags[i].addArgument(dateObject5);
            bags[i].addArgument(dateObject6);
            bags[i].addArgument(dateObject7);
            bags[i].addArgument(dateObject8);
            bags[i].addArgument(dateObject5);
            bags[i].addArgument(dateObject6);
            bags[i].addArgument(dateObject7);
            bags[i].addArgument(dateObject8);
            bags[i].addArgument(dateObject5);
            bags[i].addArgument(dateObject6);
            bags[i].addArgument(dateObject7);
            bags[i].addArgument(dateObject8);

        }

        // Establish Intersection Function
        YearmonthdurationIntersection intersection = new YearmonthdurationIntersection();
        // Push Bags Into Function
        for (int i = 0; i < bags.length; i++) {
            intersection.addArgument(bags[i]);
        }
        // Trigger Evaluation to Create Intersection
        FunctionArgument result = intersection.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 3);
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

        // Create a StringBag Array and stuff it with DataValues.
        StringBag[] stringBags = new StringBag[2];
        for (int i = 0; i < stringBags.length; i++) {
            stringBags[i] = new StringBag();
            stringBags[i].addArgument(HELLO_WORLD);
            stringBags[i].addArgument(HELLO_WORLD_FORGEROCK);
            if (i == 1) {
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
            }
        }

        // Establish Function
        StringAtLeastOneMemberOf atLeastOneMemberOf = new StringAtLeastOneMemberOf();
        // Push Bags Into Function
        for (int i = 0; i < stringBags.length; i++) {
            atLeastOneMemberOf.addArgument(stringBags[i]);
        }
        // Trigger Evaluation to Create Union
        FunctionArgument result = atLeastOneMemberOf.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
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

        // Create a StringBag Array and stuff it with DataValues.
        StringBag[] stringBags = new StringBag[6];
        for (int i = 0; i < stringBags.length; i++) {
            stringBags[i] = new StringBag();
            stringBags[i].addArgument(HELLO_WORLD);
            stringBags[i].addArgument(HELLO_WORLD_FORGEROCK);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
        }

        // Establish Union Function
        StringUnion union = new StringUnion();
        // Push Bags Into Function
        for (int i = 0; i < stringBags.length; i++) {
            union.addArgument(stringBags[i]);
        }
        // Trigger Evaluation to Create Union
        FunctionArgument result = union.evaluate(null);
        assertTrue(result instanceof DataBag);
        // Cast
        DataBag dataBag = (DataBag) result;
        assertEquals(dataBag.size(), 8);

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

        // Create a StringBag Array and stuff it with DataValues.
        StringBag[] stringBags = new StringBag[2];
        for (int i = 0; i < stringBags.length; i++) {
            stringBags[i] = new StringBag();
            stringBags[i].addArgument(HELLO_WORLD);
            stringBags[i].addArgument(HELLO_WORLD_FORGEROCK);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            if (i == 1) {
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
            }
        }

        // Establish Intersection Function
        StringSubset stringSubset = new StringSubset();
        // Push Bags Into Function
        for (int i = 0; i < stringBags.length; i++) {
            stringSubset.addArgument(stringBags[i]);
        }
        // Trigger Evaluation to verify if we have a Subset or not...
        FunctionArgument result = stringSubset.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
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

        // Create a StringBag Array and stuff it with DataValues.
        StringBag[] stringBags = new StringBag[2];
        for (int i = 0; i < stringBags.length; i++) {
            stringBags[i] = new StringBag();
            stringBags[i].addArgument(HELLO_WORLD);
            stringBags[i].addArgument(HELLO_WORLD_FORGEROCK);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
            stringBags[i].addArgument(ONE);
            stringBags[i].addArgument(TWO);
            stringBags[i].addArgument(THREE);
            stringBags[i].addArgument(FOUR);
            stringBags[i].addArgument(FIVE);
            stringBags[i].addArgument(SIX);
        }

        // Establish Intersection Function
        StringSetEquals stringsetEquals = new StringSetEquals();
        // Push Bags Into Function
        for (int i = 0; i < stringBags.length; i++) {
            stringsetEquals.addArgument(stringBags[i]);
        }
        // Trigger Evaluation to verify if we have a Subset or not...
        FunctionArgument result = stringsetEquals.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());
    }

    /**
     * urn:oasis:names:tc:xacml:x.x:function:type-set-equals
     */
    @Test
    public void test_StringSetEquals_FALSE() throws XACML3EntitlementException {

        // Create a StringBag Array and stuff it with DataValues.
        StringBag[] stringBags = new StringBag[2];
        for (int i = 0; i < stringBags.length; i++) {
            stringBags[i] = new StringBag();
            stringBags[i].addArgument(HELLO_WORLD);
            stringBags[i].addArgument(HELLO_WORLD_FORGEROCK);
            if (i==0) {
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
                stringBags[i].addArgument(ONE);
                stringBags[i].addArgument(TWO);
                stringBags[i].addArgument(THREE);
                stringBags[i].addArgument(FOUR);
                stringBags[i].addArgument(FIVE);
                stringBags[i].addArgument(SIX);
            }
        }

        // Establish Intersection Function
        StringSetEquals stringsetEquals = new StringSetEquals();
        // Push Bags Into Function
        for (int i = 0; i < stringBags.length; i++) {
            stringsetEquals.addArgument(stringBags[i]);
        }
        // Trigger Evaluation to verify if we have a Subset or not...
        FunctionArgument result = stringsetEquals.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());
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
