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

import java.math.BigDecimal;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * A.3.9 String functions
 The following functions operate on strings and convert to and from other data types.

 urn:oasis:names:tc:xacml:2.0:function:string-concatenate
 This function SHALL take two or more arguments of data-type
 "http://www.w3.org/2001/XMLSchema#string" and SHALL return a "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the concatenation, in order, of the arguments.

 urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#boolean".
 The result SHALL be the string converted to a boolean.
 If the argument is not a valid lexical representation of a boolean,
 then the result SHALL be Indeterminate with status code
 urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-boolean
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#boolean",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the boolean converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:integer-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#integer".
 The result SHALL be the string converted to an integer.
 If the argument is not a valid lexical representation of an integer,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-integer
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#integer",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the integer converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:double-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#double".
 The result SHALL be the string converted to a double.
 If the argument is not a valid lexical representation of a double,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-double
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#double",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the double converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:time-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#time".  The result SHALL be the string converted to a time.
 If the argument is not a valid lexical representation of a time,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-time
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#time",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the time converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:date-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#date".
 The result SHALL be the string converted to a date.
 If the argument is not a valid lexical representation of a date, then the result SHALL be
 Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-date
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#date",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the date converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#dateTime".
 The result SHALL be the string converted to a dateTime.
 If the argument is not a valid lexical representation of a dateTime,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#dateTime",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the dateTime converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return a "http://www.w3.org/2001/XMLSchema#anyURI".
 The result SHALL be the URI constructed by converting the argument to an URI.
 If the argument is not a valid lexical representation of a URI,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#anyURI",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the URI converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#dayTimeDuration ".
 The result SHALL be the string converted to a dayTimeDuration.
 If the argument is not a valid lexical representation of a dayTimeDuration,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#dayTimeDuration ",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the dayTimeDuration converted to a string in the canonical form specified in [XPathFunc].

 urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL
 return an "http://www.w3.org/2001/XMLSchema#yearMonthDuration".
 The result SHALL be the string converted to a yearMonthDuration.
 If the argument is not a valid lexical representation of a yearMonthDuration,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#yearMonthDuration",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the yearMonthDuration converted to a string in the canonical form specified in [XPathFunc].

 urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "urn:oasis:names:tc:xacml:1.0:data-type:x500Name".
 The result SHALL be the string converted to an x500Name.
 If the argument is not a valid lexical representation of a X500Name, then the result SHALL be
 Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:1.0:data-type:x500Name",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the x500Name converted to a string in the form it was originally represented in XML form..

 urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name".
 The result SHALL be the string converted to an rfc822Name.
 If the argument is not a valid lexical representation of an rfc822Name,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the rfc822Name converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL
 return an "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress".
 The result SHALL be the string converted to an ipAddress.
 If the argument is not a valid lexical representation of an ipAddress,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the ipAddress converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "urn:oasis:names:tc:xacml:2.0:data-type:dnsName".
 The result SHALL be the string converted to a dnsName.
 If the argument is not a valid lexical representation of a dnsName,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:2.0:data-type:dnsName",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the dnsName converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:string-starts-with
 This function SHALL take two arguments of data-type "http://www.w3.org/2001/XMLSchema#string" and
 SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".
 The result SHALL be true if the second string begins with the first string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
 This function SHALL take a first argument of data-type"http://www.w3.org/2001/XMLSchema#string"
 and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI"
 and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".
 The result SHALL be true if the URI converted to a string with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI begins with the string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:string-ends-with
 This function SHALL take two arguments of data-type "http://www.w3.org/2001/XMLSchema#string"
 and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".
 The result SHALL be true if the second string ends with the first string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string"
 and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a
 "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a string with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI ends with the string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:string-contains
 This function SHALL take two arguments of data-type "http://www.w3.org/2001/XMLSchema#string" and
 SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".
 The result SHALL be true if the second string contains the first string, and false otherwise
 . Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string" and an a
 second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a
 "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a
 string with urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI contains the string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:string-substring
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string" and a
 second and a third argument of type "http://www.w3.org/2001/XMLSchema#integer" and
 SHALL return a "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the substring of the first argument beginning at the position given by the second argument
 and ending at the position before the position given by the third argument.
 The first character of the string has position zero.
 The negative integer value -1 given for the third arguments indicates the end of the string.
 If the second or third arguments are out of bounds, then the function MUST evaluate to Indeterminate with a
 status code of urn:oasis:names:tc:xacml:1.0:status:processing-error.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and a
 second and a third argument of type "http://www.w3.org/2001/XMLSchema#integer" and
 SHALL return a "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the substring of the first argument converted to a string with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI beginning at the position given by the second argument
 and ending at the position before the position given by the third argument.
 The first character of the URI converted to a string has position zero.
 The negative integer value -1 given for the third arguments indicates the end of the string.
 If the second or third arguments are out of bounds, then the function MUST evaluate to Indeterminate
 with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error.
 If the resulting substring is not syntactically a valid URI, then the function MUST evaluate to
 Indeterminate with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error.

 */

/**
 * XACML String Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlStringFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    static final FunctionArgument testString1 = new DataValue(DataType.XACMLSTRING, "     Hello World!      ");
    static final FunctionArgument testString2 = new DataValue(DataType.XACMLSTRING, "     HELLO WORLD!      ");
    static final FunctionArgument testString3 = new DataValue(DataType.XACMLSTRING, "Hello World!");
    static final FunctionArgument testString4 = new DataValue(DataType.XACMLSTRING, "HELLO WORLD!");

    static final FunctionArgument testString5 = new DataValue(DataType.XACMLSTRING, "true");
    static final FunctionArgument testString6 = new DataValue(DataType.XACMLSTRING, "false");
    static final FunctionArgument testString7 = new DataValue(DataType.XACMLSTRING, "yes");
    static final FunctionArgument testString8 = new DataValue(DataType.XACMLSTRING, "no");
    static final FunctionArgument testString9 = new DataValue(DataType.XACMLSTRING, "1");
    static final FunctionArgument testStringA = new DataValue(DataType.XACMLSTRING, "0");
    static final FunctionArgument testStringB = new DataValue(DataType.XACMLSTRING,
            "HELLO WORLD!  It is a Beautiful Day!");

    static final FunctionArgument testStringF = new DataValue(DataType.XACMLSTRING, null);

    static final FunctionArgument testInteger1 = new DataValue(DataType.XACMLINTEGER, 22, true);
    static final FunctionArgument testInteger2 = new DataValue(DataType.XACMLINTEGER, 456789, true);
    static final FunctionArgument testInteger3 = new DataValue(DataType.XACMLINTEGER, 1024, true);
    static final FunctionArgument testInteger4 = new DataValue(DataType.XACMLINTEGER, 0, true);

    static final FunctionArgument testStringInteger1 = new DataValue(DataType.XACMLSTRING, "22", true);
    static final FunctionArgument testStringInteger2 = new DataValue(DataType.XACMLSTRING, "456789", true);
    static final FunctionArgument testStringInteger3 = new DataValue(DataType.XACMLSTRING, "1024", true);
    static final FunctionArgument testStringInteger4 = new DataValue(DataType.XACMLSTRING, "0", true);

    static final FunctionArgument testDouble1 = new DataValue(DataType.XACMLDOUBLE, 2111111111111111111290876D, true);
    static final FunctionArgument testDouble2 = new DataValue(DataType.XACMLDOUBLE, 456789D, true);
    static final FunctionArgument testDouble3 = new DataValue(DataType.XACMLDOUBLE, 4111223344556677889290876D, true);
    static final FunctionArgument testDouble4 = new DataValue(DataType.XACMLDOUBLE, 2D, true);

    static final FunctionArgument testStringDouble1 = new DataValue(DataType.XACMLSTRING, "2111111111111111111290876",
            true);
    static final FunctionArgument testStringDouble2 = new DataValue(DataType.XACMLSTRING, "456789", true);

    // Since the number is large the String from Double May not be exactly accurate!
    static final FunctionArgument testStringDouble3 = new DataValue(DataType.XACMLSTRING, "4111223344556678123094016",
            true);
    static final FunctionArgument testStringDouble4 = new DataValue(DataType.XACMLSTRING, "2", true);

    static final String timeString1 = new String("04:20:30.126");
    static final Date time1 = XACML3PrivilegeUtils.stringToTime(timeString1);
    static final FunctionArgument timeObject1 = new DataValue(DataType.XACMLTIME, time1, true);

    static final String timeString2 = new String("02:05:30.003");
    static final Date time2 = XACML3PrivilegeUtils.stringToTime(timeString2);
    static final FunctionArgument timeObject2 = new DataValue(DataType.XACMLTIME, time2, true);


    static final String dateString1 = "2013-03-11";
    static final Date date1 = XACML3PrivilegeUtils.stringToDate(dateString1);
    static final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATE, date1, true);

    static final String dateString2 = "2014-02-22";
    static final Date date2 = XACML3PrivilegeUtils.stringToDate(dateString2);
    static final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDATE, date2, true);


    static final String datetimeString3 = "2014-03-11:01:45:30.126";
    static final Date date3 = XACML3PrivilegeUtils.stringToDateTime(datetimeString3);
    static final FunctionArgument dateObject3 = new DataValue(DataType.XACMLDATETIME, date3, true);

    static final String datetimeString4 = "2014-03-11:01:45:30.124";
    static final Date date4 = XACML3PrivilegeUtils.stringToDateTime(datetimeString4);
    static final FunctionArgument dateObject4 = new DataValue(DataType.XACMLDATETIME, date4, true);

    static final String durationString1 = "011:01:45:30.126";
    static final Long duration1 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration(durationString1);
    static final FunctionArgument durationObject1 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration1, true);

    static final String durationString2 = "012:01:45:30.124";
    static final Long duration2 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration(durationString2);
    static final FunctionArgument durationObject2 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration2, true);

    static final String durationString3 = "0020-03";
    static final XACML3YearMonthDuration duration3 = new XACML3YearMonthDuration(durationString3);
    static final FunctionArgument durationObject3 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration3, true);

    static final String durationString4 = "0016-03";
    static final XACML3YearMonthDuration duration4 = new XACML3YearMonthDuration(durationString4);
    static final FunctionArgument durationObject4 = new DataValue(DataType.XACMLYEARMONTHDURATION, duration4, true);


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * urn:oasis:names:tc:xacml:2.0:function:string-concatenate
     */
    @Test
    public void testStringconcatenate() throws XACML3EntitlementException {
        StringConcatenate stringConcatenate = new StringConcatenate();

        // One String
        // Place Object in Argument stack.
        stringConcatenate.addArgument(testString1);
        FunctionArgument result = stringConcatenate.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), testString1.asString(null));

        // Multiple Strings
        stringConcatenate = new StringConcatenate();
        // Place Object in Argument stack.
        stringConcatenate.addArgument(testString3);
        stringConcatenate.addArgument(testString4);
        result = stringConcatenate.evaluate(null);
        assertNotNull(result);
        StringBuilder sb = new StringBuilder();
        sb.append(testString3.asString(null)).append(testString4.asString(null));
        assertEquals(result.asString(null), sb.toString());

        // Multiple Strings
        stringConcatenate = new StringConcatenate();
        // Place Object in Argument stack.
        stringConcatenate.addArgument(testString1);
        stringConcatenate.addArgument(testString2);
        stringConcatenate.addArgument(testString3);
        stringConcatenate.addArgument(testString4);
        result = stringConcatenate.evaluate(null);
        assertNotNull(result);
        sb = new StringBuilder();
        sb.append(testString1.asString(null)).append(testString2.asString(null)).append(testString3.asString(null)).append(testString4.asString(null));
        assertEquals(result.asString(null), sb.toString());

    }

    // String Conversions From String and To String Function Testing.

    /**
     * urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
     */
    @Test
    public void testBooleanFromString() throws XACML3EntitlementException {
        BooleanFromString function = new BooleanFromString();
        function.addArgument(testString5);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        function = new BooleanFromString();
        function.addArgument(testString6);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        function = new BooleanFromString();
        function.addArgument(testString7);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        function = new BooleanFromString();
        function.addArgument(testString8);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        function = new BooleanFromString();
        function.addArgument(testString9);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        function = new BooleanFromString();
        function.addArgument(testStringA);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
     */
    @Test(expectedExceptions = {XACML3EntitlementException.class})
    public void testBooleanFromString_Exception() throws XACML3EntitlementException {
        BooleanFromString function = new BooleanFromString();
        function.addArgument(testStringF);
        function.evaluate(null);
        // Should never get here...
        assertTrue(false);
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
     */
    @Test(expectedExceptions = {XACML3EntitlementException.class})
    public void testBooleanFromString_Exception_2() throws XACML3EntitlementException {
        BooleanFromString function = new BooleanFromString();
        function.evaluate(null);
        // Should never get here...
        assertTrue(false);
    }


    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-boolean
     */
    @Test
    public void testStringFromBoolean() throws XACML3EntitlementException {
        StringFromBoolean function = new StringFromBoolean();
        function.addArgument(trueObject);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.asString(null).equalsIgnoreCase("true"));

        function = new StringFromBoolean();
        function.addArgument(falseObject);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.asString(null).equalsIgnoreCase("false"));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:integer-from-string
     */
    @Test
    public void testIntegerFromString() throws XACML3EntitlementException {
        IntegerFromString function = new IntegerFromString();
        function.addArgument(testStringInteger1);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null), testInteger1.asInteger(null));

        function = new IntegerFromString();
        function.addArgument(testStringInteger2);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asInteger(null), testInteger2.asInteger(null));

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-integer
     */
    @Test
    public void testStringFromInteger() throws XACML3EntitlementException {
        StringFromInteger function = new StringFromInteger();
        function.addArgument(testInteger3);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), testStringInteger3.asString(null));

        function = new StringFromInteger();
        function.addArgument(testInteger4);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), testStringInteger4.asString(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:double-from-string
     */
    @Test
    public void testDoubleFromString() throws XACML3EntitlementException {
        DoubleFromString function = new DoubleFromString();
        function.addArgument(testStringDouble1);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDouble(null), testDouble1.asDouble(null));

        function = new DoubleFromString();
        function.addArgument(testStringDouble2);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDouble(null), testDouble2.asDouble(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-double
     */
    @Test
    public void testStringFromDouble() throws XACML3EntitlementException {
        StringFromDouble function = new StringFromDouble();
        function.addArgument(testDouble3);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), testStringDouble3.asString(null));

        function = new StringFromDouble();
        function.addArgument(testDouble4);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), testStringDouble4.asString(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:time-from-string
     */
    @Test
    public void testTimeFromString() throws XACML3EntitlementException {
        TimeFromString function = new TimeFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, timeString1));
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asTime(null), timeObject1.asTime(null));

        function = new TimeFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, timeString2));
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asTime(null), timeObject2.asTime(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-time
     */
    @Test
    public void testStringFromTime() throws XACML3EntitlementException {
        StringFromTime function = new StringFromTime();
        function.addArgument(timeObject1);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), XACML3PrivilegeUtils.timeToString(timeObject1.asTime(null)));

        function = new StringFromTime();
        function.addArgument(timeObject2);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), XACML3PrivilegeUtils.timeToString(timeObject2.asTime(null)));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:date-from-string
     */
    @Test
    public void testDateFromString() throws XACML3EntitlementException {
        DateFromString function = new DateFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, dateString1));
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDate(null), dateObject1.asDate(null));

        function = new DateFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, dateString2));
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDate(null), dateObject2.asDate(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-date
     */
    @Test
    public void testStringFromDate() throws XACML3EntitlementException {
        StringFromDate function = new StringFromDate();
        function.addArgument(dateObject1);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), XACML3PrivilegeUtils.dateToString(dateObject1.asDate(null)));

        function = new StringFromDate();
        function.addArgument(dateObject2);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), XACML3PrivilegeUtils.dateToString(dateObject2.asDate(null)));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string
     */
    @Test
    public void testDateTimeFromString() throws XACML3EntitlementException {
        DatetimeFromString function = new DatetimeFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, datetimeString3));
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDateTime(null), dateObject3.asDateTime(null));

        function = new DatetimeFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, datetimeString4));
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDateTime(null), dateObject4.asDateTime(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime
     */
    @Test
    public void testStringFromDateTime() throws XACML3EntitlementException {
        StringFromDatetime function = new StringFromDatetime();
        function.addArgument(dateObject3);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), XACML3PrivilegeUtils.dateTimeToString(dateObject3.asDateTime(null),':'));

        function = new StringFromDatetime();
        function.addArgument(dateObject4);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), XACML3PrivilegeUtils.dateTimeToString(dateObject4.asDateTime(null), ':'));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
     */
    @Test
    public void testDayTimeDurationFromString() throws XACML3EntitlementException {
        DayTimeDurationFromString function = new DayTimeDurationFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, durationString1));
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDayTimeDuration(null), durationObject1.asDayTimeDuration(null));

        function = new DayTimeDurationFromString();
        function.addArgument(new DataValue(DataType.XACMLSTRING, durationString2));
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asDayTimeDuration(null), durationObject2.asDayTimeDuration(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration
     */
    @Test
    public void testStringfromdayTimeDuration() throws XACML3EntitlementException {
        StringFromDayTimeDuration function = new StringFromDayTimeDuration();
        function.addArgument(durationObject1);

        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), durationString1);

        function = new StringFromDayTimeDuration();
        function.addArgument(durationObject2);
        result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), durationString2);
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string
     */
    @Test
    public void testYearMonthDurationFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration
     */
    @Test
    public void testStringfromyearMonthDuration() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
     */
    @Test
    public void testAnyURIFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
     */
    @Test
    public void testStringFromAnyURI() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
     */
    @Test
    public void testX500NameFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
     */
    @Test
    public void testStringFromx500Name() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
     */
    @Test
    public void testRfc822NameFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
     */
    @Test
    public void testStringFromrfc822Name() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
     */
    @Test
    public void testIpAddressFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
     */
    @Test
    public void testStringFromipAddress() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
     */
    @Test
    public void testDnsNameFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
     */
    @Test
    public void testStringFromdnsName() throws XACML3EntitlementException {

    }

    // String Starts and Ends Comparisons

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-starts-with
     */
    @Test
    public void testStringStartsWith() throws XACML3EntitlementException {
        StringStartswith function = new StringStartswith();
        function.addArgument(testString4);
        function.addArgument(testStringB);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        function = new StringStartswith();
        function.addArgument(testString5);
        function.addArgument(testStringB);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
     */
    @Test
    public void testAnyURIStartsWith() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-ends-with
     */
    @Test
    public void testStringendswith() throws XACML3EntitlementException {
         // TODO ::
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
     */
    @Test
    public void testAnyURIendswith() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-contains
     */
    @Test
    public void testStringcontains() throws XACML3EntitlementException {
        StringContains function = new StringContains();
        function.addArgument(testString4);
        function.addArgument(testStringB);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        function = new StringContains();
        function.addArgument(testString5);
        function.addArgument(testStringB);
        result = function.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
     */
    @Test
    public void testAnyURIcontains() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-substring
     */
    @Test
    public void testStringsubstring() throws XACML3EntitlementException {
        // TODO ::
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
     */
    @Test
    public void testAnyURIsubstring() throws XACML3EntitlementException {

    }


}
