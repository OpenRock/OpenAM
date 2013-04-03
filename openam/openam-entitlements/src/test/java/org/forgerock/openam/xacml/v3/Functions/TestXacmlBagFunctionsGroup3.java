/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock US. All Rights Reserved
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
 * A.3.10 Bag functions
 These functions operate on a bag of ‘type’ values, where type is one of the primitive data-types,
 and x.x is a version of XACML where the function has been defined.
 Some additional conditions defined for each function below SHALL cause the expression to evaluate to "Indeterminate".

 urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
 This function SHALL take a bag of ‘type’ values as an argument and SHALL return a value of ‘type’.
 It SHALL return the only value in the bag.  If the bag does not have one and only one value,
 then the expression SHALL evaluate to "Indeterminate".

 */

/**
 * XACML Bag Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlBagFunctionsGroup3 {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * One and Only
     * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
     This function SHALL take a bag of ‘type’ values as an argument and SHALL return a value of ‘type’.
     It SHALL return the only value in the bag.  If the bag does not have one and only one value,
     then the expression SHALL evaluate to "Indeterminate".

     urn:oasis:names:tc:xacml:1.0:function:string-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:double-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:time-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:date-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only
     urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only
     urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only
     urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only
     urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only
     urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only

     */

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_AnyuriOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument anyuri1 = new DataValue(DataType.XACMLANYURI, "/openam/xacml");
        final FunctionArgument anyuri2 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");

        // Establish a Bag with Several Elements.
        AnyuriBag bag = new AnyuriBag();
        bag.addArgument(anyuri1);
        bag.addArgument(anyuri2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        AnyuriOneAndOnly oneAndOnly = new AnyuriOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only
     */
    @Test
    public void test_AnyuriOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument anyuri1 = new DataValue(DataType.XACMLANYURI, "/openam/xacml");

        // Establish a Bag with Several Elements.
        AnyuriBag bag = new AnyuriBag();
        bag.addArgument(anyuri1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        AnyuriOneAndOnly oneAndOnly = new AnyuriOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asAnyURI(null), "/openam/xacml");

    }

    /**
     *urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_Base64BinaryOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        // base64data1 and base64data2 contained the Base 64 encoding of:
        // ForgeRock - OpenAM XACML says Hello!
        final FunctionArgument base64data1 = new DataValue(DataType.XACMLBASE64BINARY,
                "Rm9yZ2VSb2NrIC0gT3BlbkFNIFhBQ01MIHNheXMgSGVsbG8h");
        // This is a very small Test!
        final FunctionArgument base64data2 = new DataValue(DataType.XACMLBASE64BINARY,
                "VGhpcyBpcyBhIHZlcnkgc21hbGwgVGVzdCE=");

        // Establish a Bag with Several Elements.
        Base64BinaryBag bag = new Base64BinaryBag();
        bag.addArgument(base64data1);
        bag.addArgument(base64data2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        Base64BinaryOneAndOnly oneAndOnly = new Base64BinaryOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);
    }

    /**
     *urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only
     */
    @Test
    public void test_Base64BinaryOneAndOnly() throws XACML3EntitlementException {
        // base64data1 and base64data2 contained the Base 64 encoding of:
        // ForgeRock - OpenAM XACML says Hello!
        final FunctionArgument base64data1 = new DataValue(DataType.XACMLBASE64BINARY,
                "Rm9yZ2VSb2NrIC0gT3BlbkFNIFhBQ01MIHNheXMgSGVsbG8h");

        // Establish a Bag with Several Elements.
        Base64BinaryBag bag = new Base64BinaryBag();
        bag.addArgument(base64data1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        Base64BinaryOneAndOnly oneAndOnly = new Base64BinaryOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asBase64Binary(null), "Rm9yZ2VSb2NrIC0gT3BlbkFNIFhBQ01MIHNheXMgSGVsbG8h");
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_BooleanOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        // Establish a Bag with Several Elements.
        BooleanBag bag = new BooleanBag();
        bag.addArgument(trueObject);
        bag.addArgument(falseObject);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        BooleanOneAndOnly oneAndOnly = new BooleanOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only
     */
    @Test
    public void test_BooleanOneAndOnly() throws XACML3EntitlementException {
        // Establish a Bag with Several Elements.
        BooleanBag bag = new BooleanBag();
        bag.addArgument(trueObject);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        BooleanOneAndOnly oneAndOnly = new BooleanOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asBoolean(null), new Boolean(true));
    }

    /**
     *urn:oasis:names:tc:xacml:1.0:function:date-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_DateOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final Date date1 = XACML3PrivilegeUtils.stringToDate("2013-03-11");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATE, date1, true);

        final Date date2 = XACML3PrivilegeUtils.stringToDate("2013-03-12");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDATE, date2, true);

        // Establish a Bag with Several Elements.
        DateBag bag = new DateBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DateOneAndOnly oneAndOnly = new DateOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     *urn:oasis:names:tc:xacml:1.0:function:date-one-and-only
     */
    @Test
    public void test_DateOneAndOnly() throws XACML3EntitlementException {
        final Date date1 = XACML3PrivilegeUtils.stringToDate("2013-03-11");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATE, date1, true);

        // Establish a Bag with Several Elements.
        DateBag bag = new DateBag();
        bag.addArgument(dateObject1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DateOneAndOnly oneAndOnly = new DateOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asDate(null), date1);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_DatetimeOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final Date date1 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATETIME, date1, true);

        final Date date2 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.124");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDATETIME, date2, true);

        // Establish a Bag with Several Elements.
        DatetimeBag bag = new DatetimeBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DatetimeOneAndOnly oneAndOnly = new DatetimeOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only
     */
    @Test
    public void test_DatetimeOneAndOnly() throws XACML3EntitlementException {
        final Date date1 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATETIME, date1, true);

        // Establish a Bag with Several Elements.
        DatetimeBag bag = new DatetimeBag();
        bag.addArgument(dateObject1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DatetimeOneAndOnly oneAndOnly = new DatetimeOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asDateTime(null), date1);

    }

    /**
     *  urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_DaytimedurationOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final Long duration1 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("011:01:45:30.126");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration1, true);

        final Long duration2 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("012:01:45:30.124");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration2, true);

        // Establish a Bag with Several Elements.
        DaytimedurationBag bag = new DaytimedurationBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DaytimedurationOneAndOnly oneAndOnly = new DaytimedurationOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     *  urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-one-and-only
     */
    @Test
    public void test_DaytimedurationOneAndOnly() throws XACML3EntitlementException {
        final Long duration1 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("011:01:45:30.126");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration1, true);

        // Establish a Bag with Several Elements.
        DaytimedurationBag bag = new DaytimedurationBag();
        bag.addArgument(dateObject1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DaytimedurationOneAndOnly oneAndOnly = new DaytimedurationOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asDayTimeDuration(null), duration1);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:double-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_DoubleOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
        final FunctionArgument double2 = new DataValue(DataType.XACMLDOUBLE, 456789D, true);

        // Establish a Bag with Several Elements.
        DoubleBag bag = new DoubleBag();
        bag.addArgument(double1);
        bag.addArgument(double2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DoubleOneAndOnly oneAndOnly = new DoubleOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:double-one-and-only
     */
    @Test
    public void test_DoubleOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, 456789D, true);

        // Establish a Bag with Several Elements.
        DoubleBag bag = new DoubleBag();
        bag.addArgument(double1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DoubleOneAndOnly oneAndOnly = new DoubleOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asDouble(null).doubleValue(), 456789D);

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_HexbinaryOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument hexdata1 = new DataValue(DataType.XACMLHEXBINARY, "0123456789abcdef");
        final FunctionArgument hexdata2 = new DataValue(DataType.XACMLHEXBINARY, "FF");

        // Establish a Bag with Several Elements.
        HexbinaryBag bag = new HexbinaryBag();
        bag.addArgument(hexdata1);
        bag.addArgument(hexdata2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        HexbinaryOneAndOnly oneAndOnly = new HexbinaryOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only
     */
    @Test
    public void test_HexbinaryOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument hexdata1 = new DataValue(DataType.XACMLHEXBINARY, "0123456789abcdef");

        // Establish a Bag with Several Elements.
        HexbinaryBag bag = new HexbinaryBag();
        bag.addArgument(hexdata1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        HexbinaryOneAndOnly oneAndOnly = new HexbinaryOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asHexBinary(null), "0123456789abcdef");

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_IntegerOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument integer1 = new DataValue(DataType.XACMLINTEGER, 22, true);
        final FunctionArgument integer2 = new DataValue(DataType.XACMLINTEGER, 456789, true);

        // Establish a Bag with Several Elements.
        IntegerBag bag = new IntegerBag();
        bag.addArgument(integer1);
        bag.addArgument(integer2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        IntegerOneAndOnly oneAndOnly = new IntegerOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only
     */
    @Test
    public void test_IntegerOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument integer1 = new DataValue(DataType.XACMLINTEGER, 22, true);

        // Establish a Bag with Several Elements.
        IntegerBag bag = new IntegerBag();
        bag.addArgument(integer1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        IntegerOneAndOnly oneAndOnly = new IntegerOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asInteger(null).intValue(), 22);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_Rfc822NameOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument rfc822Name1 = new DataValue(DataType.XACMLRFC822NAME,
                "joe@example.org");
        final FunctionArgument rfc822Name2 = new DataValue(DataType.XACMLRFC822NAME,
                "joe.smith@example.org");

        // Establish a Bag with Several Elements.
        Rfc822NameBag bag = new Rfc822NameBag();
        bag.addArgument(rfc822Name1);
        bag.addArgument(rfc822Name2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        Rfc822NameOneAndOnly oneAndOnly = new Rfc822NameOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only
     */
    @Test
    public void test_Rfc822NameOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument rfc822Name1 = new DataValue(DataType.XACMLRFC822NAME,
                "joe@example.org");

        // Establish a Bag with Several Elements.
        Rfc822NameBag bag = new Rfc822NameBag();
        bag.addArgument(rfc822Name1);
         DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        Rfc822NameOneAndOnly oneAndOnly = new Rfc822NameOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asRfc822Name(null), "joe@example.org");
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:string-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_StringOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final DataValue HELLO_WORLD = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");
        final DataValue HELLO_WORLD_FORGEROCK = new DataValue(DataType.XACMLSTRING, "HELLO WORLD From ForgeRock!");

        // Establish a Bag with Several Elements.
        StringBag stringBag = new StringBag();
        stringBag.addArgument(HELLO_WORLD);
        stringBag.addArgument(HELLO_WORLD_FORGEROCK);
        DataBag dataBag = (DataBag) stringBag.evaluate(null);
        assertNotNull(dataBag);

        StringOneAndOnly oneAndOnly = new StringOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:string-one-and-only
     */
    @Test
    public void test_StringOneAndOnly() throws XACML3EntitlementException {
        final DataValue HELLO_WORLD = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");

        // Establish a Bag with Several Elements.
        StringBag stringBag = new StringBag();
        stringBag.addArgument(HELLO_WORLD);
        DataBag dataBag = (DataBag) stringBag.evaluate(null);
        assertNotNull(dataBag);

        StringOneAndOnly oneAndOnly = new StringOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asString(null), "HELLO WORLD!");
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:time-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_TimeOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final Date time1 = XACML3PrivilegeUtils.stringToTime("01:45:30.126");
        final FunctionArgument timeObject1 = new DataValue(DataType.XACMLTIME, time1, true);

        final Date time2 = XACML3PrivilegeUtils.stringToTime("02:45:30.126");
        final FunctionArgument timeObject2 = new DataValue(DataType.XACMLTIME, time2, true);

        // Establish a Bag with Several Elements.
        TimeOneAndOnly bag = new TimeOneAndOnly();
        bag.addArgument(timeObject1);
        bag.addArgument(timeObject2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        TimeOneAndOnly oneAndOnly = new TimeOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:time-one-and-only
     */
    @Test
    public void test_TimeOneAndOnly() throws XACML3EntitlementException {
        final Date time1 = XACML3PrivilegeUtils.stringToTime("01:45:30.126");
        final FunctionArgument timeObject1 = new DataValue(DataType.XACMLTIME, time1, true);

        // Establish a Bag with Several Elements.
        TimeBag bag = new TimeBag();
        bag.addArgument(timeObject1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        TimeOneAndOnly oneAndOnly = new TimeOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asTime(null), time1);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_X500NameOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument x500Name1 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=Components/cn=OpenAM");
        final FunctionArgument x500Name2 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");

        // Establish a Bag with Several Elements.
        X500NameBag bag = new X500NameBag();
        bag.addArgument(x500Name1);
        bag.addArgument(x500Name2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        X500NameOneAndOnly oneAndOnly = new X500NameOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only
     */
    @Test
    public void test_X500NameOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument x500Name1 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=Components/cn=OpenAM");

        // Establish a Bag with Several Elements.
        X500NameBag bag = new X500NameBag();
        bag.addArgument(x500Name1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        X500NameOneAndOnly oneAndOnly = new X500NameOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asX500Name(null), "/c=us/o=ForgeRock/ou=Components/cn=OpenAM");

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_YearmonthdurationOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final XACML3YearMonthDuration duration1 = new XACML3YearMonthDuration("0020-03");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration1, true);

        final XACML3YearMonthDuration duration2 = new XACML3YearMonthDuration("0016-03");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration2, true);

        // Establish a Bag with Several Elements.
        YearmonthdurationBag bag = new YearmonthdurationBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        YearmonthdurationOneAndOnly oneAndOnly = new YearmonthdurationOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-one-and-only
     */
    @Test
    public void test_YearmonthdurationOneAndOnly() throws XACML3EntitlementException {
        final XACML3YearMonthDuration duration1 = new XACML3YearMonthDuration("0020-03");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration1, true);

        // Establish a Bag with Several Elements.
        YearmonthdurationBag bag = new YearmonthdurationBag();
        bag.addArgument(dateObject1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        YearmonthdurationOneAndOnly oneAndOnly = new YearmonthdurationOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asYearMonthDuration(null), duration1);

    }

    /**
     * urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_DNSNameOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument dnsName1 = new DataValue(DataType.XACMLDNSNAME,
                "www.example.org");
        final FunctionArgument dnsName2 = new DataValue(DataType.XACMLDNSNAME,
                "example.com");

        // Establish a Bag with Several Elements.
        DNSNameBag bag = new DNSNameBag();
        bag.addArgument(dnsName1);
        bag.addArgument(dnsName2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DNSNameOneAndOnly oneAndOnly = new DNSNameOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);
    }

    /**
     * urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only
     */
    @Test
    public void test_DNSNameOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument dnsName1 = new DataValue(DataType.XACMLDNSNAME,
                "www.example.org");

        // Establish a Bag with Several Elements.
        DNSNameBag bag = new DNSNameBag();
        bag.addArgument(dnsName1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DNSNameOneAndOnly oneAndOnly = new DNSNameOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asDnsName(null), "www.example.org");
    }

    /**
     * urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only
     */
    @Test(expectedExceptions = {IndeterminateException.class})
    public void test_IPAddressOneAndOnly_Indeterminate() throws XACML3EntitlementException {
        final FunctionArgument ipaddr1 = new DataValue(DataType.XACMLIPADDRESS,
                "10.0.0.1");
        final FunctionArgument ipaddr2 = new DataValue(DataType.XACMLIPADDRESS,
                "10.0.200.1");


        // Establish a Bag with Several Elements.
        IPAddressBag bag = new IPAddressBag();
        bag.addArgument(ipaddr1);
        bag.addArgument(ipaddr2);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        IPAddressOneAndOnly oneAndOnly = new IPAddressOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        oneAndOnly.evaluate(null);
        // Should not get to Here...
        assertTrue(false);

    }

    /**
     * urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only
     */
    @Test
    public void test_IPAddressOneAndOnly() throws XACML3EntitlementException {
        final FunctionArgument ipaddr1 = new DataValue(DataType.XACMLIPADDRESS,
                "10.0.0.1");

        // Establish a Bag with Several Elements.
        IPAddressBag bag = new IPAddressBag();
        bag.addArgument(ipaddr1);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        IPAddressOneAndOnly oneAndOnly = new IPAddressOneAndOnly();
        oneAndOnly.addArgument(dataBag);
        FunctionArgument functionArgument = oneAndOnly.evaluate(null);
        assertNotNull(functionArgument);
        assertEquals(functionArgument.asIpAddress(null), "10.0.0.1");

    }

}
