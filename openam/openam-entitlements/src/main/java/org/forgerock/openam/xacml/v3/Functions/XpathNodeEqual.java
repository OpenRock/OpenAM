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
 * urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal
 *
 This function SHALL take two “urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression” arguments and SHALL
 return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The function SHALL return "True" if any of the XML nodes in the node-set matched by the first argument
 equals any of the XML nodes in the node-set matched by the second argument.
 Two nodes are considered equal if they have the same identity. If the <Content> element of the category
 to which either XPath expression applies to is not present in the request, this function SHALL return a value of “False”.
 */

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

@Deprecated
public class XpathNodeEqual extends XACMLFunction {

    public XpathNodeEqual()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() != 2) {
            return retVal;
        }
        String s = (String)getArg(0).getValue(pip);
        if ( s.equals((String)getArg(1).getValue(pip))) {
            retVal =   FunctionArgument.trueObject;
        }
        return retVal;
    }
}
