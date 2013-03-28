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
 * urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return a "http://www.w3.org/2001/XMLSchema#anyURI".
 The result SHALL be the URI constructed by converting the argument to an URI.
 If the argument is not a valid lexical representation of a URI,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.
 */

import org.forgerock.openam.xacml.v3.model.*;
import org.forgerock.openam.xacml.v3.model.DataType;
import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

/**
 * urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
 */
public class AnyuriFromString extends XACMLFunction {

    public AnyuriFromString()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        if (getArgCount() != 1) {
            throw new XACML3EntitlementException("Not Correct Number of Arguments");
        }
        String value = getArg(0).asString(pip);
        if ( (value == null) || (value.isEmpty()) ) {
            throw new XACML3EntitlementException("Syntax Error, No Value", URN_SYNTAX_ERROR);
        }
        try {
            return new DataValue(DataType.XACMLANYURI, value, true);
        } catch(Exception e) {
            throw new XACML3EntitlementException("Syntax Error, "+e.getMessage(), URN_SYNTAX_ERROR);
        }
    }
}
