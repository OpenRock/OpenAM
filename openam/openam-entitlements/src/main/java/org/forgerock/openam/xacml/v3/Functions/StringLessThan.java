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
 * urn:oasis:names:tc:xacml:1.0:function:string-less-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#string”
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 It SHALL return "True" if and only the first argument is lexigraphically strictly less than the second argument.
 Otherwise, it SHALL return “False”. The comparison SHALL use Unicode codepoint collation,
 as defined for the identifier http://www.w3.org/2005/xpath-functions/collation/codepoint by [XF].
 */

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

/**
 * urn:oasis:names:tc:xacml:1.0:function:string-less-than
 */
public class StringLessThan extends XACMLFunction {

    public StringLessThan()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() != 2) {
            return retVal;
        }

        String string1 = getArg(0).asString(pip);
        String string2 = getArg(1).asString(pip);

        // Yields =0: Equal, >0: Greater Than, <0: Less Than.
        int stringResult = string1.compareTo(string2);

        if ( stringResult < 0 ) {
            retVal = FunctionArgument.trueObject;
        }
        return retVal;
    }
}
