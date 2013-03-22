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
urn:oasis:names:tc:xacml:1.0:function:date-equal
This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#date”
and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
It SHALL perform its evaluation according to the “op:date-equal” function [XF] Section 10.4.9.
*/

import org.forgerock.openam.xacml.v3.model.*;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;

import java.util.Date;

/**
 * urn:oasis:names:tc:xacml:1.0:function:date-equal
 */
public class DateEqual extends XACMLFunction {

    public DateEqual()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.falseObject;
        if ( getArgCount() != 2) {
            return retVal;
        }

        Date d1 = getArg(0).asDate(pip);
        Date d2 = getArg(1).asDate(pip);

        if ( (d1==null) || (d2==null ) )  {
            return retVal;
        }

        if( d1.equals(d2)) {
            retVal = FunctionArgument.trueObject;
        };
        return retVal;
    }
}
