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
 * A.3.7 Date and time arithmetic functions
 These functions perform arithmetic operations with date and time.

 urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration
 This function SHALL take two arguments, the first SHALL be of data-type “http://www.w3.org/2001/XMLSchema#dateTime”
 and the second SHALL be of data-type “http://www.w3.org/2001/XMLSchema#dayTimeDuration”.
 It SHALL return a result of “http://www.w3.org/2001/XMLSchema#dateTime”.
 This function SHALL return the value by adding the second argument to the first argument according to the
 specification of adding durations to date and time [XS] Appendix E.

 urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration
 This function SHALL take two arguments, the first SHALL be a “http://www.w3.org/2001/XMLSchema#dateTime” and the second
 SHALL be a “http://www.w3.org/2001/XMLSchema#yearMonthDuration”.
 It SHALL return a result of “http://www.w3.org/2001/XMLSchema#dateTime”.
 This function SHALL return the value by adding the second argument to the first argument according to
 the specification of adding durations to date and time [XS] Appendix E.

 urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
 This function SHALL take two arguments, the first SHALL be a “http://www.w3.org/2001/XMLSchema#dateTime” and the second
 SHALL be a “http://www.w3.org/2001/XMLSchema#dayTimeDuration”.
 It SHALL return a result of “http://www.w3.org/2001/XMLSchema#dateTime”.
 If the second argument is a positive duration, then this function SHALL return the value by adding
 the corresponding negative duration, as per the specification [XS] Appendix E.
 If the second argument is a negative duration, then the result SHALL be as if the function
 “urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration” had been applied to the corresponding
 positive duration.

 urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration
 This function SHALL take two arguments, the first SHALL be a “http://www.w3.org/2001/XMLSchema#dateTime” and the second
 SHALL be a “http://www.w3.org/2001/XMLSchema#yearMonthDuration”.
 It SHALL return a result of “http://www.w3.org/2001/XMLSchema#dateTime”.
 If the second argument is a positive duration, then this function SHALL return the value by
 adding the corresponding negative duration, as per the specification [XS] Appendix E.
 If the second argument is a negative duration, then the result SHALL be as if the function
 “urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration” had been applied to the
 corresponding positive duration.

 urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration
 This function SHALL take two arguments, the first SHALL be a “http://www.w3.org/2001/XMLSchema#date” and the second
 SHALL be a “http://www.w3.org/2001/XMLSchema#yearMonthDuration”.
 It SHALL return a result of “http://www.w3.org/2001/XMLSchema#date”.
 This function SHALL return the value by adding the second argument to the first argument according to
 the specification of adding duration to date [XS] Appendix E.

 urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration
 This function SHALL take two arguments, the first SHALL be a “http://www.w3.org/2001/XMLSchema#date” and the second
 SHALL be a “http://www.w3.org/2001/XMLSchema#yearMonthDuration”.
 It SHALL return a result of “http://www.w3.org/2001/XMLSchema#date”.
 If the second argument is a positive duration, then this function SHALL return the value by adding the
 corresponding negative duration, as per the specification [XS] Appendix E.
 If the second argument is a negative duration, then the result SHALL be as if the function
 “urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration” had been applied to the corresponding
 positive duration.
 */

/**
 * XACML Numeric Comparison Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlDateAndTimeArithmetricFunctions {

    static final Long duration1 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("010:01:00:30.001");
    static final FunctionArgument durationObject1 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration1, true);

    static final Long duration2 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("020:02:02:02.002");
    static final FunctionArgument durationObject2 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration2, true);

    static final Long duration3 = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration("030:03:03:03.003");
    static final FunctionArgument durationObject3 = new DataValue(DataType.XACMLDAYTIMEDURATION, duration3, true);


    static final Date date1 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
    static final FunctionArgument dateObject1 = new DataValue(DataType.XACMLDATETIME, date1, true);

    static final Date date2 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.124");
    static final FunctionArgument dateObject2 = new DataValue(DataType.XACMLDATETIME, date2, true);

    static final Date date3 = XACML3PrivilegeUtils.stringToDateTime("2013-03-11:01:45:30.126");
    static final FunctionArgument dateObject3 = new DataValue(DataType.XACMLDATETIME, date3, true);


    static final Date date4 = XACML3PrivilegeUtils.stringToDateTime("2013-03-20:02:46:00.127");
    static final FunctionArgument dateResult1 = new DataValue(DataType.XACMLDATETIME, date4, true);

    static final Date date5 = XACML3PrivilegeUtils.stringToDateTime("2013-03-30:03:47:32.126");
    static final FunctionArgument dateResult2 = new DataValue(DataType.XACMLDATETIME, date5, true);

    static final Date date6 = XACML3PrivilegeUtils.stringToDateTime("2013-04-09:04:48:33.129");
    static final FunctionArgument dateResult3 = new DataValue(DataType.XACMLDATETIME, date6, true);


    static final Date date7 = XACML3PrivilegeUtils.stringToDateTime("2014-06-11:01:45:30.126");
    static final FunctionArgument dateResult7 = new DataValue(DataType.XACMLDATETIME, date7, true);

    static final Date date8 = XACML3PrivilegeUtils.stringToDateTime("2015-06-11:01:45:30.124");
    static final FunctionArgument dateResult8 = new DataValue(DataType.XACMLDATETIME, date8, true);

    static final Date date9 = XACML3PrivilegeUtils.stringToDateTime("2016-06-11:01:45:30.126");
    static final FunctionArgument dateResult9 = new DataValue(DataType.XACMLDATETIME, date9, true);


    static final Date date10 = XACML3PrivilegeUtils.stringToDateTime("2013-03-02:00:45:00.125");
    static final FunctionArgument dateResult10 = new DataValue(DataType.XACMLDATETIME, date10, true);

    static final Date date11 = XACML3PrivilegeUtils.stringToDateTime("2013-02-19:23:43:28.122");
    static final FunctionArgument dateResult11 = new DataValue(DataType.XACMLDATETIME, date11, true);

    static final Date date12 = XACML3PrivilegeUtils.stringToDateTime("2013-02-09:22:42:27.123");
    static final FunctionArgument dateResult12 = new DataValue(DataType.XACMLDATETIME, date12, true);


    static final Date date13 = XACML3PrivilegeUtils.stringToDateTime("2011-12-11:02:45:30.126");
    static final FunctionArgument dateResult13 = new DataValue(DataType.XACMLDATETIME, date13, true);

    static final Date date14 = XACML3PrivilegeUtils.stringToDateTime("2010-12-11:02:45:30.124");
    static final FunctionArgument dateResult14 = new DataValue(DataType.XACMLDATETIME, date14, true);

    static final Date date15 = XACML3PrivilegeUtils.stringToDateTime("2009-12-11:02:45:30.126");
    static final FunctionArgument dateResult15 = new DataValue(DataType.XACMLDATETIME, date15, true);


    static final Date date16 = XACML3PrivilegeUtils.stringToDate("2013-03-06");
    static final FunctionArgument dateObject16 = new DataValue(DataType.XACMLDATE, date16, true);

    static final Date date17 = XACML3PrivilegeUtils.stringToDate("2013-03-11");
    static final FunctionArgument dateObject17 = new DataValue(DataType.XACMLDATE, date17, true);

    static final Date date18 = XACML3PrivilegeUtils.stringToDate("2013-03-28");
    static final FunctionArgument dateObject18 = new DataValue(DataType.XACMLDATE, date18, true);


    static final Date date19 = XACML3PrivilegeUtils.stringToDate("2014-06-05");
    static final FunctionArgument dateResultObject19 = new DataValue(DataType.XACMLDATE, date19, true);

    static final Date date20 = XACML3PrivilegeUtils.stringToDate("2015-06-11");
    static final FunctionArgument dateResultObject20 = new DataValue(DataType.XACMLDATE, date20, true);

    static final Date date21 = XACML3PrivilegeUtils.stringToDate("2016-06-28");
    static final FunctionArgument dateResultObject21 = new DataValue(DataType.XACMLDATE, date21, true);


    static final Date date22 = XACML3PrivilegeUtils.stringToDate("2011-12-06");
    static final FunctionArgument dateResultObject22 = new DataValue(DataType.XACMLDATE, date22, true);

    static final Date date23 = XACML3PrivilegeUtils.stringToDate("2010-12-11");
    static final FunctionArgument dateResultObject23 = new DataValue(DataType.XACMLDATE, date23, true);

    static final Date date24 = XACML3PrivilegeUtils.stringToDate("2009-12-28");
    static final FunctionArgument dateResultObject24 = new DataValue(DataType.XACMLDATE, date24, true);



    static final XACML3YearMonthDuration  ymDuration1 = new XACML3YearMonthDuration("0001-03");
    static final FunctionArgument ymDurationObject1 = new DataValue(DataType.XACMLYEARMONTHDURATION, ymDuration1, true);

    static final XACML3YearMonthDuration  ymDuration2 = new XACML3YearMonthDuration("0002-03");
    static final FunctionArgument ymDurationObject2 = new DataValue(DataType.XACMLYEARMONTHDURATION, ymDuration2, true);

    static final XACML3YearMonthDuration  ymDuration3 = new XACML3YearMonthDuration("0003-03");
    static final FunctionArgument ymDurationObject3 = new DataValue(DataType.XACMLYEARMONTHDURATION, ymDuration3, true);


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration
     */
    @Test
    public void testDateTimeAddDayTimeDuration() throws XACML3EntitlementException {
        System.out.println("testDateTimeAddDayTimeDuration: ");
        DatetimeAddDaytimeduration dateTimeAddDaytimeduration = new DatetimeAddDaytimeduration();
        dateTimeAddDaytimeduration.addArgument(dateObject1);
        dateTimeAddDaytimeduration.addArgument(durationObject1);

        FunctionArgument result = dateTimeAddDaytimeduration.evaluate(null);
        assertNotNull(result);
        showDayTimeDurationResultForDatetime("+", result, dateObject1, durationObject1);
        assertEquals(result.asDateTime(null).getTime(), dateResult1.asDateTime(null).getTime());

        dateTimeAddDaytimeduration = new DatetimeAddDaytimeduration();
        dateTimeAddDaytimeduration.addArgument(dateObject2);
        dateTimeAddDaytimeduration.addArgument(durationObject2);

        result = dateTimeAddDaytimeduration.evaluate(null);
        assertNotNull(result);
        showDayTimeDurationResultForDatetime("+", result, dateObject2, durationObject2);
        assertEquals(result.asDateTime(null).getTime(), dateResult2.asDateTime(null).getTime());

        dateTimeAddDaytimeduration = new DatetimeAddDaytimeduration();
        dateTimeAddDaytimeduration.addArgument(dateObject3);
        dateTimeAddDaytimeduration.addArgument(durationObject3);

        result = dateTimeAddDaytimeduration.evaluate(null);
        assertNotNull(result);
        showDayTimeDurationResultForDatetime("+", result, dateObject3, durationObject3);
        assertEquals(result.asDateTime(null).getTime(), dateResult3.asDateTime(null).getTime());

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration
     */
    @Test
    public void testDateTimeAddYearMonthDuration() throws XACML3EntitlementException {
        System.out.println("testDateTimeAddYearMonthDuration: ");
        DatetimeAddYearmonthduration datetimeAddYearmonthduration = new DatetimeAddYearmonthduration();
        datetimeAddYearmonthduration.addArgument(dateObject1);
        datetimeAddYearmonthduration.addArgument(ymDurationObject1);

        FunctionArgument result = datetimeAddYearmonthduration.evaluate(null);
        assertNotNull(result);
        showYearMonthDurationResultForDatetime("+", result, dateObject1, ymDurationObject1);
        assertEquals(result.asDateTime(null).getTime(), dateResult7.asDateTime(null).getTime() );

        datetimeAddYearmonthduration = new DatetimeAddYearmonthduration();
        datetimeAddYearmonthduration.addArgument(dateObject2);
        datetimeAddYearmonthduration.addArgument(ymDurationObject2);

        result = datetimeAddYearmonthduration.evaluate(null);
        assertNotNull(result);
        showYearMonthDurationResultForDatetime("+", result, dateObject2, ymDurationObject2);
        assertEquals(result.asDateTime(null).getTime(), dateResult8.asDateTime(null).getTime());

        datetimeAddYearmonthduration = new DatetimeAddYearmonthduration();
        datetimeAddYearmonthduration.addArgument(dateObject3);
        datetimeAddYearmonthduration.addArgument(ymDurationObject3);

        result = datetimeAddYearmonthduration.evaluate(null);
        assertNotNull(result);
        showYearMonthDurationResultForDatetime("+", result, dateObject3, ymDurationObject3);
        assertEquals(result.asDateTime(null).getTime(), dateResult9.asDateTime(null).getTime());
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
     */
    @Test
    public void testDateTimeSubtractDayTimeDuration() throws XACML3EntitlementException {
        System.out.println("testDateTimeSubtractDayTimeDuration: ");
        DatetimeSubtractDaytimeduration dateTimeSubtractDaytimeduration = new DatetimeSubtractDaytimeduration();
        dateTimeSubtractDaytimeduration.addArgument(dateObject1);
        dateTimeSubtractDaytimeduration.addArgument(durationObject1);

        FunctionArgument result = dateTimeSubtractDaytimeduration.evaluate(null);
        assertNotNull(result);
        showDayTimeDurationResultForDatetime("-", result, dateObject1, durationObject1);
        assertEquals(result.asDateTime(null).getTime(), dateResult10.asDateTime(null).getTime());

        dateTimeSubtractDaytimeduration = new DatetimeSubtractDaytimeduration();
        dateTimeSubtractDaytimeduration.addArgument(dateObject2);
        dateTimeSubtractDaytimeduration.addArgument(durationObject2);

        result = dateTimeSubtractDaytimeduration.evaluate(null);
        assertNotNull(result);
        showDayTimeDurationResultForDatetime("-", result, dateObject2, durationObject2);
        assertEquals(result.asDateTime(null).getTime(), dateResult11.asDateTime(null).getTime());


        dateTimeSubtractDaytimeduration = new DatetimeSubtractDaytimeduration();
        dateTimeSubtractDaytimeduration.addArgument(dateObject3);
        dateTimeSubtractDaytimeduration.addArgument(durationObject3);

        result = dateTimeSubtractDaytimeduration.evaluate(null);
        assertNotNull(result);
        showDayTimeDurationResultForDatetime("-", result, dateObject3, durationObject3);
       assertEquals(result.asDateTime(null).getTime(), dateResult12.asDateTime(null).getTime());

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration
     */
    @Test
    public void testDateTimeSubtractYearMonthDuration() throws XACML3EntitlementException {
        System.out.println("testDateTimeSubtractYearMonthDuration: ");
        DatetimeSubtractyearmonthduration datetimeSubtractYearmonthduration = new DatetimeSubtractyearmonthduration();
        datetimeSubtractYearmonthduration.addArgument(dateObject1);
        datetimeSubtractYearmonthduration.addArgument(ymDurationObject1);

        FunctionArgument result = datetimeSubtractYearmonthduration.evaluate(null);
        assertNotNull(result);
        showYearMonthDurationResultForDatetime("-", result, dateObject1, ymDurationObject1);
        assertEquals(result.asDateTime(null).getTime(), dateResult13.asDateTime(null).getTime() );

        datetimeSubtractYearmonthduration = new DatetimeSubtractyearmonthduration();
        datetimeSubtractYearmonthduration.addArgument(dateObject2);
        datetimeSubtractYearmonthduration.addArgument(ymDurationObject2);

        result = datetimeSubtractYearmonthduration.evaluate(null);
        assertNotNull(result);
        showYearMonthDurationResultForDatetime("-", result, dateObject2, ymDurationObject2);
        assertEquals(result.asDateTime(null).getTime(), dateResult14.asDateTime(null).getTime());

        datetimeSubtractYearmonthduration = new DatetimeSubtractyearmonthduration();
        datetimeSubtractYearmonthduration.addArgument(dateObject3);
        datetimeSubtractYearmonthduration.addArgument(ymDurationObject3);

        result = datetimeSubtractYearmonthduration.evaluate(null);
        assertNotNull(result);
        showYearMonthDurationResultForDatetime("-", result, dateObject3, ymDurationObject3);
        assertEquals(result.asDateTime(null).getTime(), dateResult15.asDateTime(null).getTime());
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration
     */
    @Test
    public void testDateAddYearMonthDuration() throws XACML3EntitlementException {
        System.out.println("testDateAddYearMonthDuration: ");
        DateAddYearmonthduration dateAddYearmonthduration = new DateAddYearmonthduration();
        dateAddYearmonthduration.addArgument(dateObject16);
        dateAddYearmonthduration.addArgument(ymDurationObject1);

        FunctionArgument result = dateAddYearmonthduration.evaluate(null);
        assertNotNull(result);

        showYearMonthDurationResultForDate("+", result, dateObject16, ymDurationObject1);
        assertTrue(checkResultForYearMonthDurationCalculation(result, dateResultObject19));

        dateAddYearmonthduration = new DateAddYearmonthduration();
        dateAddYearmonthduration.addArgument(dateObject17);
        dateAddYearmonthduration.addArgument(ymDurationObject2);

        result = dateAddYearmonthduration.evaluate(null);
        assertNotNull(result);

        showYearMonthDurationResultForDate("+", result, dateObject17, ymDurationObject2);
        assertTrue(checkResultForYearMonthDurationCalculation(result, dateResultObject20));

        dateAddYearmonthduration = new DateAddYearmonthduration();
        dateAddYearmonthduration.addArgument(dateObject18);
        dateAddYearmonthduration.addArgument(ymDurationObject3);

        result = dateAddYearmonthduration.evaluate(null);
        assertNotNull(result);

        showYearMonthDurationResultForDate("+", result, dateObject18, ymDurationObject3);
        assertTrue(checkResultForYearMonthDurationCalculation(result, dateResultObject21));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration
     */
    @Test
    public void testDateSubtractYearMonthDuration() throws XACML3EntitlementException {
        System.out.println("testDateSubtractYearMonthDuration: ");
        DateSubtractYearmonthduration datetimeSubtractYearmonthduration = new DateSubtractYearmonthduration();
        datetimeSubtractYearmonthduration.addArgument(dateObject16);
        datetimeSubtractYearmonthduration.addArgument(ymDurationObject1);

        FunctionArgument result = datetimeSubtractYearmonthduration.evaluate(null);
        assertNotNull(result);

        showYearMonthDurationResultForDate("-", result, dateObject16, ymDurationObject1);
        assertTrue(checkResultForYearMonthDurationCalculation(result, dateResultObject22));

        datetimeSubtractYearmonthduration = new DateSubtractYearmonthduration();
        datetimeSubtractYearmonthduration.addArgument(dateObject17);
        datetimeSubtractYearmonthduration.addArgument(ymDurationObject2);

        result = datetimeSubtractYearmonthduration.evaluate(null);
        assertNotNull(result);

        showYearMonthDurationResultForDate("-", result, dateObject17, ymDurationObject2);
        assertTrue(checkResultForYearMonthDurationCalculation(result, dateResultObject23));

        datetimeSubtractYearmonthduration = new DateSubtractYearmonthduration();
        datetimeSubtractYearmonthduration.addArgument(dateObject18);
        datetimeSubtractYearmonthduration.addArgument(ymDurationObject3);

        result = datetimeSubtractYearmonthduration.evaluate(null);
        assertNotNull(result);

        showYearMonthDurationResultForDate("-", result, dateObject18, ymDurationObject3);
        assertTrue(checkResultForYearMonthDurationCalculation(result, dateResultObject24));
    }

    /**
     * Private helper to verify that the Result for the YearMonthDuration calculation to correct and equal to our
     * expected value.
     * <p/>
     * With Date Calculations using a "Year Month Duration", we must only compare the YEAR-MONTH-DAY",
     *
     * @param result         DataType.XACMLDATE
     * @param expectedResult DataType.XACMLDATE
     * @return boolean -- True indicates Test Result is Equal and Value to Expected.
     */
    private boolean checkResultForYearMonthDurationCalculation(FunctionArgument result, FunctionArgument expectedResult) {
        try {
            if (XACML3PrivilegeUtils.dateToString(result.asDate(null)).trim().equalsIgnoreCase(
                    XACML3PrivilegeUtils.dateToString(expectedResult.asDate(null)).trim())) {
                System.out.println("\n\n");
                return true;
            } else {
                System.out.println("    Expected: " + XACML3PrivilegeUtils.dateToString(expectedResult.asDate(null))
                        + "\n\n");
            }
        } catch (XACML3EntitlementException ee) {
            // Show Issue in Test Suite...
            System.out.println("Showing Result Error: " + ee.getMessage());
            ee.printStackTrace();
        }
        return false;
    }

    /**
     * Private helper to dump the Results for YearMonthDuration Calculations.
     *
     * @param result
     * @param dateObject
     * @param ymDuration
     */
    private void showYearMonthDurationResultForDate(String op, FunctionArgument result, FunctionArgument dateObject,
                                                    FunctionArgument ymDuration) {
        try {
            System.out.println("        Date: " + XACML3PrivilegeUtils.dateToString(dateObject.asDate(null)));
            System.out.println(op + "   Duration: " + ymDuration.asYearMonthDuration(null).toString());
            System.out.println("============= =======================");
            System.out.println("      Result: " + XACML3PrivilegeUtils.dateToString(result.asDate(null)));
        } catch (XACML3EntitlementException ee) {
            System.out.println("Showing Result Error: " + ee.getMessage());
            ee.printStackTrace();
        }

    }

    /**
     * Private helper to dump the Results for YearMonthDuration Calculations.
     *
     * @param result
     * @param dateObject
     * @param ymDuration
     */
    private void showYearMonthDurationResultForDatetime(String op, FunctionArgument result,
                                                        FunctionArgument dateObject,
                                                        FunctionArgument ymDuration) {
        try {
            System.out.println("        Date: " + XACML3PrivilegeUtils.dateTimeToString(dateObject.asDateTime(null),
                    ' '));
            System.out.println(op + "   Duration: " + ymDuration.asYearMonthDuration(null).toString());
            System.out.println("============= =======================");
            System.out.println("      Result: " + XACML3PrivilegeUtils.dateTimeToString(result.asDateTime(null), ' ')
                    + "\n\n");
        } catch (XACML3EntitlementException ee) {
            System.out.println("Showing Result Error: " + ee.getMessage());
            ee.printStackTrace();
        }

    }

    /**
     * Private helper to dump the Results for DayTimeDuration Calculations.
     *
     * @param result
     * @param dateObject
     * @param dtDuration
     */
    private void showDayTimeDurationResultForDatetime(String op, FunctionArgument result, FunctionArgument dateObject,
                                                      FunctionArgument dtDuration) {
        try {
            System.out.println("        Date: " + XACML3PrivilegeUtils.dateTimeToString(dateObject.asDateTime(null),
                    ' '));
            System.out.println(op + "   Duration: " + XACML3PrivilegeUtils.
                    dayTimeDurationToString(dtDuration.asDayTimeDuration(null)));
            System.out.println("============= =======================");
            System.out.println("      Result: " + XACML3PrivilegeUtils.dateTimeToString(result.asDateTime(null), ' ')
                    + "\n\n");
        } catch (XACML3EntitlementException ee) {
            System.out.println("Showing Result Error: " + ee.getMessage());
            ee.printStackTrace();
        }

    }

}
