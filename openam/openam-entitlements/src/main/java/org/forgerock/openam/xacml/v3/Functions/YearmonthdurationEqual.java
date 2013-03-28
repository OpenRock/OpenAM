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

/*
urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
This function SHALL take two arguments of data-type
 "http://www.w3.org/2001/XMLSchema#yearMonthDuration”
 and SHALL return an "http://www.w3.org/2001/XMLSchema#boolean".
 This function shall perform its evaluation according to the "op:duration-equal" function [XF] Section 10.4.5.
 Note that the lexical representation of each argument MUST be converted to a value expressed in fractional seconds
 [XF] Section 10.3.2.
*/

import org.forgerock.openam.xacml.v3.model.*;

import java.util.Calendar;

/**
 * urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal
 */
public class YearmonthdurationEqual extends XACMLFunction {

    public YearmonthdurationEqual()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.falseObject;
        if ( getArgCount() != 2) {
            return retVal;
        }

        XACML3YearMonthDuration duration1 = getArg(0).asYearMonthDuration(pip);
        XACML3YearMonthDuration duration2 = getArg(1).asYearMonthDuration(pip);
        if ( (duration1==null) || (duration2==null ) )  {
            return retVal;
        }

        if (duration1.equals(duration2)) {
            retVal = FunctionArgument.trueObject;
        }
        return retVal;
    }
}
