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
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * A.3.10 Bag functions
 These functions operate on a bag of ‘type’ values, where type is one of the primitive data-types,
 and x.x is a version of XACML where the function has been defined.
 Some additional conditions defined for each function below SHALL cause the expression to evaluate to "Indeterminate".

 urn:oasis:names:tc:xacml:x.x:function:type-bag-size
 This function SHALL take a bag of ‘type’ values as an argument and SHALL return an
 “http://www.w3.org/2001/XMLSchema#integer” indicating the number of values in the bag.

 */

/**
 * XACML Bag Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlBagFunctionsGroup2 {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * Size Methods
     *  urn:oasis:names:tc:xacml:x.x:function:type-bag-size
     This function SHALL take a bag of ‘type’ values as an argument and SHALL return an
     “http://www.w3.org/2001/XMLSchema#integer” indicating the number of values in the bag.

     urn:oasis:names:tc:xacml:1.0:function:string-bag-size
     urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size
     urn:oasis:names:tc:xacml:1.0:function:integer-bag-size
     urn:oasis:names:tc:xacml:1.0:function:double-bag-size
     urn:oasis:names:tc:xacml:1.0:function:time-bag-size
     urn:oasis:names:tc:xacml:1.0:function:date-bag-size
     urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size
     urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size
     urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size
     urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size
     urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size
     urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size
     urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size
     urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size
     urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size
     urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size

     */


    /**
     * urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size
     */
    @Test
    public void test_AnyuriBagSize() throws XACML3EntitlementException {
        final FunctionArgument anyuri1 = new DataValue(DataType.XACMLANYURI, "/openam/xacml");
        final FunctionArgument anyuri2 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");
        final FunctionArgument anyuri3 = new DataValue(DataType.XACMLANYURI, "/");
        final FunctionArgument anyuri4 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");

        // Establish a Bag with Several Elements.
        AnyuriBag bag = new AnyuriBag();
        bag.addArgument(anyuri1);
        bag.addArgument(anyuri2);
        bag.addArgument(anyuri3);
        bag.addArgument(anyuri4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        AnyuriBagSize size = new AnyuriBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size
     */
    @Test
    public void test_Base64BinaryBagSize() throws XACML3EntitlementException {
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


        // Establish a Bag with Several Elements.
        Base64BinaryBag bag = new Base64BinaryBag();
        bag.addArgument(base64data1);
        bag.addArgument(base64data2);
        bag.addArgument(base64data3);
        bag.addArgument(base64data4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        Base64BinaryBagSize size = new Base64BinaryBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size
     */
    @Test
    public void test_BooleanBagSize() throws XACML3EntitlementException {

        // Establish a Bag with Several Elements.
        BooleanBag bag = new BooleanBag();
        bag.addArgument(trueObject);
        bag.addArgument(trueObject);
        bag.addArgument(falseObject);
        bag.addArgument(trueObject);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        BooleanBagSize size = new BooleanBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:date-bag-size
     */
    @Test
    public void test_DateBagSize() throws XACML3EntitlementException {
        final Date date1 = XACML3PrivilegeUtils.stringToDate("2013-03-11");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATE, date1, true);

        final Date date2 = XACML3PrivilegeUtils.stringToDate("2013-03-12");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDATE, date2, true);

        final Date date3 = XACML3PrivilegeUtils.stringToDate("2013-03-11");
        final FunctionArgument dateObject3 = new DataValue(DataType.XACMLDATE, date3, true);

        final Date date4 = XACML3PrivilegeUtils.stringToDate("2014-03-11");
        final FunctionArgument dateObject4 = new DataValue(DataType.XACMLDATE, date4, true);

        // Establish a Bag with Several Elements.
        DateBag bag = new DateBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        bag.addArgument(dateObject3);
        bag.addArgument(dateObject4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DateBagSize size = new DateBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size
     */
    @Test
    public void test_DatetimeBagSize() throws XACML3EntitlementException {
        final Date date1 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATETIME, date1, true);

        final Date date2 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.124");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDATETIME, date2, true);

        final Date date3 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
        final FunctionArgument dateObject3 = new DataValue(DataType.XACMLDATETIME, date3, true);

        final Date date4 = XACML3PrivilegeUtils.stringToDateTime("2014-03-11:01:45:30.126");
        final FunctionArgument dateObject4 = new DataValue(DataType.XACMLDATETIME, date4, true);


        // Establish a Bag with Several Elements.
        DatetimeBag bag = new DatetimeBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        bag.addArgument(dateObject3);
        bag.addArgument(dateObject4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DatetimeBagSize size = new DatetimeBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-bag-size
     */
    @Test
    public void test_DaytimedurationBagSize() throws XACML3EntitlementException {
        final Long duration1 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("011:01:45:30.126");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration1, true);

        final Long duration2 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("012:01:45:30.124");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration2, true);

        final Long duration3 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("011:01:45:30.126");
        final FunctionArgument dateObject3 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration3, true);

        final Long duration4 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("001:01:45:30.126");
        final FunctionArgument dateObject4 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration4, true);


        // Establish a Bag with Several Elements.
        DaytimedurationBag bag = new DaytimedurationBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        bag.addArgument(dateObject3);
        bag.addArgument(dateObject4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DaytimedurationBagSize size = new DaytimedurationBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:double-bag-size
     */
    @Test
    public void test_DoubleBagSize() throws XACML3EntitlementException {
        final FunctionArgument double1 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
        final FunctionArgument double2 = new DataValue(DataType.XACMLDOUBLE, 456789D, true);
        final FunctionArgument double3 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
        final FunctionArgument double4 = new DataValue(DataType.XACMLDOUBLE, 2D, true);

        // Establish a Bag with Several Elements.
        DoubleBag bag = new DoubleBag();
        bag.addArgument(double1);
        bag.addArgument(double2);
        bag.addArgument(double3);
        bag.addArgument(double4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DoubleBagSize size = new DoubleBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size
     */
    @Test
    public void test_HexbinaryBagSize() throws XACML3EntitlementException {
        final FunctionArgument hexdata1 = new DataValue(DataType.XACMLHEXBINARY, "0123456789abcdef");
        final FunctionArgument hexdata2 = new DataValue(DataType.XACMLHEXBINARY, "FF");
        final FunctionArgument hexdata3 = new DataValue(DataType.XACMLHEXBINARY, "0123456789ABCDEF");
        final FunctionArgument hexdata4 = new DataValue(DataType.XACMLHEXBINARY, "06F2");
        final FunctionArgument hexdata5 = new DataValue(DataType.XACMLHEXBINARY, "CED");

        // Establish a Bag with Several Elements.
        HexbinaryBag bag = new HexbinaryBag();
        bag.addArgument(hexdata1);
        bag.addArgument(hexdata2);
        bag.addArgument(hexdata3);
        bag.addArgument(hexdata4);
        bag.addArgument(hexdata5);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        HexbinaryBagSize size = new HexbinaryBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 5);


    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:integer-bag-size
     */
    @Test
    public void test_IntegerBagSize() throws XACML3EntitlementException {
        final FunctionArgument integer1 = new DataValue(DataType.XACMLINTEGER, 22, true);
        final FunctionArgument integer2 = new DataValue(DataType.XACMLINTEGER, 456789, true);
        final FunctionArgument integer3 = new DataValue(DataType.XACMLINTEGER, 22, true);
        final FunctionArgument integer4 = new DataValue(DataType.XACMLINTEGER, 0, true);

        // Establish a Bag with Several Elements.
        IntegerBag bag = new IntegerBag();
        bag.addArgument(integer1);
        bag.addArgument(integer2);
        bag.addArgument(integer3);
        bag.addArgument(integer4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        IntegerBagSize size = new IntegerBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size
     */
    @Test
    public void test_Rfc822NameBagSize() throws XACML3EntitlementException {
        final FunctionArgument rfc822Name1 = new DataValue(DataType.XACMLRFC822NAME,
                "joe@example.org");
        final FunctionArgument rfc822Name2 = new DataValue(DataType.XACMLRFC822NAME,
                "joe.smith@example.org");
        final FunctionArgument rfc822Name3 = new DataValue(DataType.XACMLRFC822NAME,
                "joe.smith@example.org");
        final FunctionArgument rfc822Name4 = new DataValue(DataType.XACMLRFC822NAME,
                "joe.smith@ExAmPlE.oRg");

        // Establish a Bag with Several Elements.
        Rfc822NameBag bag = new Rfc822NameBag();
        bag.addArgument(rfc822Name1);
        bag.addArgument(rfc822Name2);
        bag.addArgument(rfc822Name3);
        bag.addArgument(rfc822Name4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        Rfc822NameBagSize size = new Rfc822NameBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:string-bag-size
     */
    @Test
    public void test_StringBagSize() throws XACML3EntitlementException {
        final DataValue HELLO_WORLD = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");
        final DataValue HELLO_WORLD_FORGEROCK = new DataValue(DataType.XACMLSTRING, "HELLO WORLD From ForgeRock!");

        // Establish a Bag with Several Elements.
        StringBag stringBag = new StringBag();
        stringBag.addArgument(HELLO_WORLD);
        stringBag.addArgument(HELLO_WORLD_FORGEROCK);
        DataBag dataBag = (DataBag) stringBag.evaluate(null);
        assertNotNull(dataBag);

        StringBagSize size = new StringBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 2);

    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:time-bag-size
     */
    @Test
    public void test_TimeBagSize() throws XACML3EntitlementException {
        final Date time1 = XACML3PrivilegeUtils.stringToTime("01:45:30.126");
        final FunctionArgument timeObject1 = new DataValue(DataType.XACMLTIME, time1, true);

        final Date time2 = XACML3PrivilegeUtils.stringToTime("02:45:30.126");
        final FunctionArgument timeObject2 = new DataValue(DataType.XACMLTIME, time2, true);

        final Date time3 = XACML3PrivilegeUtils.stringToTime("01:45:30.126");
        final FunctionArgument timeObject3 = new DataValue(DataType.XACMLTIME, time3, true);

        final Date time4 = XACML3PrivilegeUtils.stringToTime("01:45:30.127");
        final FunctionArgument timeObject4 = new DataValue(DataType.XACMLTIME, time4, true);

        // Establish a Bag with Several Elements.
        TimeBag bag = new TimeBag();
        bag.addArgument(timeObject1);
        bag.addArgument(timeObject2);
        bag.addArgument(timeObject3);
        bag.addArgument(timeObject4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        TimeBagSize size = new TimeBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);
    }

    /**
     * urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size
     */
    @Test
    public void test_X500NameBagSize() throws XACML3EntitlementException {
        final FunctionArgument x500Name1 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=Components/cn=OpenAM");
        final FunctionArgument x500Name2 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");
        final FunctionArgument x500Name3 = new DataValue(DataType.XACMLX500NAME,
                "/cn=Bob Smith");
        final FunctionArgument x500Name4 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");

        // Establish a Bag with Several Elements.
        X500NameBag bag = new X500NameBag();
        bag.addArgument(x500Name1);
        bag.addArgument(x500Name2);
        bag.addArgument(x500Name3);
        bag.addArgument(x500Name4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        X500NameBagSize size = new X500NameBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);


    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-bag-size
     */
    @Test
    public void test_YearmonthdurationBagSize() throws XACML3EntitlementException {
        final XACML3YearMonthDuration duration1 = new XACML3YearMonthDuration("0020-03");
        final FunctionArgument dateObject1 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration1, true);

        final XACML3YearMonthDuration duration2 = new XACML3YearMonthDuration("0016-03");
        final FunctionArgument dateObject2 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration2, true);

        final XACML3YearMonthDuration duration3 = new XACML3YearMonthDuration("0013-03");
        final FunctionArgument dateObject3 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration3, true);

        final XACML3YearMonthDuration duration4 = new XACML3YearMonthDuration("0020-03");
        final FunctionArgument dateObject4 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration4, true);

        // Establish a Bag with Several Elements.
        YearmonthdurationBag bag = new YearmonthdurationBag();
        bag.addArgument(dateObject1);
        bag.addArgument(dateObject2);
        bag.addArgument(dateObject3);
        bag.addArgument(dateObject4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        YearmonthdurationBagSize size = new YearmonthdurationBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     * urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size
     */
    @Test
    public void test_DNSNameBagSize() throws XACML3EntitlementException {
        final FunctionArgument dnsName1 = new DataValue(DataType.XACMLDNSNAME,
                "www.example.org");
        final FunctionArgument dnsName2 = new DataValue(DataType.XACMLDNSNAME,
                "example.com");
        final FunctionArgument dnsName3 = new DataValue(DataType.XACMLDNSNAME,
                "www.example.com");
        final FunctionArgument dnsName4 = new DataValue(DataType.XACMLDNSNAME,
                "openam.example.org");

        // Establish a Bag with Several Elements.
        DNSNameBag bag = new DNSNameBag();
        bag.addArgument(dnsName1);
        bag.addArgument(dnsName2);
        bag.addArgument(dnsName3);
        bag.addArgument(dnsName4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        DNSNameBagSize size = new DNSNameBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

    /**
     * urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size
     */
    @Test
    public void test_IPAddressBagSize() throws XACML3EntitlementException {
        final FunctionArgument ipaddr1 = new DataValue(DataType.XACMLIPADDRESS,
                "10.0.0.1");
        final FunctionArgument ipaddr2 = new DataValue(DataType.XACMLIPADDRESS,
                "10.0.200.1");
        final FunctionArgument ipaddr3 = new DataValue(DataType.XACMLIPADDRESS,
                "10.0.12.1");
        final FunctionArgument ipaddr4 = new DataValue(DataType.XACMLIPADDRESS,
                "10.0.195.1");

        // Establish a Bag with Several Elements.
        IPAddressBag bag = new IPAddressBag();
        bag.addArgument(ipaddr1);
        bag.addArgument(ipaddr2);
        bag.addArgument(ipaddr3);
        bag.addArgument(ipaddr4);
        DataBag dataBag = (DataBag) bag.evaluate(null);
        assertNotNull(dataBag);

        IPAddressBagSize size = new IPAddressBagSize();
        size.addArgument(dataBag);
        DataValue dv = (DataValue) size.evaluate(null);
        assertNotNull(dv);
        assertEquals(dv.asInteger(null).intValue(), 4);

    }

}
