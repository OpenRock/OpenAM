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


    /**
     *   Expected Results:

     Date: 2013-03-11T01:45:30.126
     Duration: 781230001
     ----------- ---------------------------------------------
     result: 2013-03-20T02:46:00.127


     Date: 2013-03-11T01:45:30.124
     Duration: 1648922002
     ----------- ---------------------------------------------
     result: 2013-03-30T03:47:32.126


     Date: 2013-03-11T01:45:30.126
     Duration: 2516583003
     ----------- ---------------------------------------------
     result: 2013-04-09T04:48:33.129


     *
     */


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
    public void testDateTimeAddDayTimeDuration() throws XACML3EntitlementException{

        DatetimeAddDaytimeduration dateTimeAddDaytimeduration = new DatetimeAddDaytimeduration();
        dateTimeAddDaytimeduration.addArgument(dateObject1);
        dateTimeAddDaytimeduration.addArgument(durationObject1);

        FunctionArgument result = dateTimeAddDaytimeduration.evaluate(null);
        assertNotNull(result);

        System.out.println("      Date: "+XACML3PrivilegeUtils.dateToString(dateObject1.asDateTime(null)));
        System.out.println("  Duration: "+durationObject1.asDayTimeDuration(null).toString());
        System.out.println("----------- ---------------------------------------------");
        System.out.println("    result: "+XACML3PrivilegeUtils.dateToString(result.asDateTime(null)) +"\n\n");

        assertTrue(dateResult1.asDateTime(null).getTime() == result.asDateTime(null).getTime());

        dateTimeAddDaytimeduration = new DatetimeAddDaytimeduration();
        dateTimeAddDaytimeduration.addArgument(dateObject2);
        dateTimeAddDaytimeduration.addArgument(durationObject2);

        result = dateTimeAddDaytimeduration.evaluate(null);
        assertNotNull(result);

        System.out.println("      Date: "+XACML3PrivilegeUtils.dateToString(dateObject2.asDateTime(null)));
        System.out.println("  Duration: "+durationObject2.asDayTimeDuration(null).toString());
        System.out.println("----------- ---------------------------------------------");
        System.out.println("    result: "+XACML3PrivilegeUtils.dateToString(result.asDateTime(null)) +"\n\n");

        assertTrue(dateResult2.asDateTime(null).getTime() == result.asDateTime(null).getTime());


        dateTimeAddDaytimeduration = new DatetimeAddDaytimeduration();
        dateTimeAddDaytimeduration.addArgument(dateObject3);
        dateTimeAddDaytimeduration.addArgument(durationObject3);

        result = dateTimeAddDaytimeduration.evaluate(null);
        assertNotNull(result);

        System.out.println("      Date: "+XACML3PrivilegeUtils.dateToString(dateObject3.asDateTime(null)));
        System.out.println("  Duration: "+durationObject3.asDayTimeDuration(null).toString());
        System.out.println("----------- ---------------------------------------------");
        System.out.println("    result: "+XACML3PrivilegeUtils.dateToString(result.asDateTime(null)) +"\n\n");

        assertTrue(dateResult3.asDateTime(null).getTime() == result.asDateTime(null).getTime());

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration
     */
    @Test
    public void testDateTimeAddYearMonthDuration() throws XACML3EntitlementException{

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration
     */
    @Test
    public void testDateTimeSubtractDayTimeDuration() throws XACML3EntitlementException{

    }

    /**
     *  urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration
     */
    @Test
    public void testDateTimeSubtractYearMonthDuration() throws XACML3EntitlementException{

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration
     */
    @Test
    public void testDateAddYearMonthDuration() throws XACML3EntitlementException{

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration
     */
    @Test
    public void testDateSubtractYearMonthDuration() throws XACML3EntitlementException{

    }


}
