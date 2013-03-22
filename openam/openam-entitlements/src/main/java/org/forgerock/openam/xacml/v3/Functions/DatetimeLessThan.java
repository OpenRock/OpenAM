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
 * urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#dateTime”
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 It SHALL return "True" if and only if the first argument is less than the second argument according to the order
 relation specified for “http://www.w3.org/2001/XMLSchema#dateTime” by [XS, part 2, section 3.2.7].
 Otherwise, it SHALL return “False”.  Note: if a dateTime value does not include a time-zone value,
 then an implicit time-zone value SHALL be assigned, as described in [XS].
 */

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than
 */
public class DatetimeLessThan extends XACMLFunction {

    public DatetimeLessThan()  {
    }
    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.falseObject;
        if (getArgCount() != 2) {
            return retVal;
        }

        Date date1 = getArg(0).asTime(pip);
        Date date2 = getArg(1).asTime(pip);
        if ((date1 == null) || (date2 == null)) {
            return retVal;
        }

        // Ensure TimeZone's are in Sync between the two arguments.
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        if (cal1.getTimeZone() == null) {
            cal1.setTimeZone(TimeZone.getDefault());
        }
        if (cal2.getTimeZone() == null) {
            cal1.setTimeZone(TimeZone.getDefault());
        }

        // Compare...
        if (cal1.compareTo(cal2) < 0) {
            retVal = FunctionArgument.trueObject;
        }
        return retVal;
    }
}
