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
urn:oasis:names:tc:xacml:1.0:function:or
This function SHALL return "False" if it has no arguments and SHALL return "True"
if at least one of its arguments evaluates to "True". The order of evaluation SHALL be from first argument to last.
The evaluation SHALL stop with a result of "True" if any argument evaluates to "True",
leaving the rest of the arguments unevaluated.
*/

import org.forgerock.openam.xacml.v3.Entitlements.*;

/**
 * urn:oasis:names:tc:xacml:1.0:function:or
 */
public class Or extends XACMLFunction {

    public Or()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() == 0) {
            return retVal;
        }
        int args = getArgCount();

        for (int i=0;i<args;i++) {
            Object argument = getArg(i).getValue(pip);
            if (argument == null) {
                throw new IndeterminateException("Argument is null!");
            }
            if(argument instanceof Boolean) {
                if (((Boolean)argument).booleanValue() == true) {
                    return  FunctionArgument.trueObject;
                }
            } else {
                throw new IndeterminateException("Expecting Boolean, but received: "+argument.getClass().getName());
            }
        }
        return retVal;
    }
}
