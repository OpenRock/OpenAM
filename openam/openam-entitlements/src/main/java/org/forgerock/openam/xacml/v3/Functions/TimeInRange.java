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

/**
 * urn:oasis:names:tc:xacml:2.0:function:time-in-range

 This function SHALL take three arguments of data-type “http://www.w3.org/2001/XMLSchema#time”
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.

 It SHALL return "True" if the first argument falls in the range defined inclusively by
 the second and third arguments.  Otherwise, it SHALL return “False”.

 Regardless of its value, the third argument SHALL be interpreted
 as a time that is equal to, or later than by less than twenty-four hours, the second argument.

 If no time zone is provided for the first argument,
 it SHALL use the default time zone at the context handler.

 If no time zone is provided for the second or third arguments,
 then they SHALL use the time zone from the first argument.

 */

import org.forgerock.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.openam.xacml.v3.Entitlements.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.Entitlements.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.Entitlements.XACMLFunction;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * urn:oasis:names:tc:xacml:2.0:function:time-in-range
 */
public class TimeInRange extends XACMLFunction {

    public TimeInRange()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.falseObject;
        if ( getArgCount() != 3) {
            return retVal;
        }

        Date date1 = getArg(0).asTime(pip);
        Date date2 = getArg(1).asTime(pip);
        Date date3 = getArg(2).asTime(pip);
        if ( (date1==null) || (date2==null ) || (date3==null ) )  {
            return retVal;
        }

        // Obtain a Calendar Object for each Date object for precision.
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        Calendar cal3 = Calendar.getInstance();
        cal3.setTime(date3);

        // Check the TimeZone across all specified Time Objects.
        if (cal1.getTimeZone() == null) {
            cal1.setTimeZone(TimeZone.getDefault());
        }
        if (cal2.getTimeZone() == null) {
            cal2.setTimeZone(cal1.getTimeZone());
        }
        if (cal3.getTimeZone() == null) {
            cal3.setTimeZone(cal3.getTimeZone());
        }

        // Now Check if the First Time Object is in Range?
        if  ( ( cal1.equals(cal2) ) || ( cal1.equals(cal2) ) ||
              ( (cal1.after(cal2) && cal1.before(cal3)) ) ) {
            retVal = FunctionArgument.trueObject;
        }
        return retVal;
    }
}
