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
urn:oasis:names:tc:xacml:2.0:function:string-concatenate

This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#string”
This function SHALL take two or more arguments of data-type
     "http://www.w3.org/2001/XMLSchema#string" and SHALL return a
     "http://www.w3.org/2001/XMLSchema#string".
     The result SHALL be the concatenation, in order, of the arguments.
*/

import org.forgerock.openam.xacml.v3.model.*;

/**
 * urn:oasis:names:tc:xacml:2.0:function:string-concatenate
 */
public class StringConcatenate extends XACMLFunction {

    public StringConcatenate()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {

        if ( getArgCount() < 2) {
            if (getArgCount() == 1) {
                return new DataValue(DataType.XACMLSTRING, getArg(0).asString(pip));
            } else {
                throw new XACML3EntitlementException("Nothing to Concatenate");
            }
        }
        // Loop Through Arguments to Build up Final Content.
        int args = getArgCount();
        StringBuilder sb = new StringBuilder( getArg(0).asString(pip)) ;
        for (int i=1; i<args; i++) {
            sb.append(getArg(i).asString(pip));
        }
        return new DataValue(DataType.XACMLSTRING, sb.toString());
    }

}
