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
 * urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#dayTimeDuration ".

 The result SHALL be the string converted to a dayTimeDuration.
 If the argument is not a valid lexical representation of a dayTimeDuration,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 */

import org.forgerock.openam.xacml.v3.model.*;


/**
 * urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
 */
public class DayTimeDurationFromString extends XACMLFunction {

    public DayTimeDurationFromString()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        if (getArgCount() != 1) {
            throw new XACML3EntitlementException("Not Correct Number of Arguments");
        }
        String value = getArg(0).asString(pip);
        if (value == null) {
            throw new XACML3EntitlementException("Syntax Error, No Value", URN_SYNTAX_ERROR);
        }
        try {
            Long result = XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration(value);
            return new DataValue(DataType.XACMLDAYTIMEDURATION, result, true);
        } catch(Exception e) {
            throw new XACML3EntitlementException("Syntax Error, "+e.getMessage(), URN_SYNTAX_ERROR);
        }
    }
}
