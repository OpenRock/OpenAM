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
 * urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string"
 and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a
 "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a string with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI ends with the string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.
 */

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

/**
 * urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
 */
public class AnyUriEndswith extends XACMLFunction {

    public AnyUriEndswith()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() != 2) {
            return retVal;
        }
        if ( getArg(1).asAnyURI(pip).endsWith(getArg(0).asString(pip))) {
            retVal =   FunctionArgument.trueObject;
        }
        return retVal;
    }
}
