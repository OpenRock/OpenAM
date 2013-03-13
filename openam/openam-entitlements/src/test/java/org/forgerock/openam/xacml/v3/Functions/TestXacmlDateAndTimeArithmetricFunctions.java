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
