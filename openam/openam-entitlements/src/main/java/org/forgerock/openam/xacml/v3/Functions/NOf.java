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
urn:oasis:names:tc:xacml:1.0:function:n-of
The first argument to this function SHALL be of data-type http://www.w3.org/2001/XMLSchema#integer.

The remaining arguments SHALL be of data-type http://www.w3.org/2001/XMLSchema#boolean.

The first argument specifies the minimum number of the remaining arguments that MUST evaluate to "True"
for the expression to be considered "True".

If the first argument is 0, the result SHALL be "True".

If the number of arguments after the first one is less than the value of the first argument,
then the expression SHALL result in "Indeterminate".

The order of evaluation SHALL be: first evaluate the integer value, and then evaluate each subsequent argument.
The evaluation SHALL stop and return "True" if the specified number of arguments evaluate to "True".
The evaluation of arguments SHALL stop if it is determined that evaluating the remaining arguments
will not satisfy the requirement.

*/

import org.forgerock.openam.xacml.v3.Entitlements.*;

/**
 * urn:oasis:names:tc:xacml:1.0:function:n-of
 */
public class NOf extends XACMLFunction {

    public NOf()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        int args = getArgCount();
        if ( args == 0) {
            return FunctionArgument.falseObject;
        }
        // Obtain our First Element, which is the number of remaining Evaluations which must be met.
        Object argumentZero = getArg(0).getValue(pip);
        if ( (argumentZero == null) || (!(argumentZero instanceof Integer)) ) {
            throw new IndeterminateException("First Argument null or Not an Integer!");
        }
        // Cast our Object
        Integer conditionsRequired = (Integer) argumentZero;
        if (conditionsRequired.intValue() == 0) {
            return FunctionArgument.trueObject;
        }
        // Determine if the number of Required Conditions exceed the number of Arguments.
        if ( (args-1) < conditionsRequired.intValue() )  {
            throw new IndeterminateException("The number of arguments after the first one is less than the value of " +
                    "the first argument!");
        }
        // Iterate over the remaining Arguments counting Conditions evaluating to True.
        int evaluatedTrue = 0;
        for (int i=1;i<args;i++) {
            Object argument = getArg(i).getValue(pip);
            if (argument == null) {
                throw new IndeterminateException("Argument is null!");
            }
            if(argument instanceof Boolean) {
                if (((Boolean)argument).booleanValue() == true) {
                    evaluatedTrue++;
                }
            } else {
                throw new IndeterminateException("Expecting Boolean, but received: "+argument.getClass().getName());
            }
        }
        // Determine if we have a correct number of successful Evaluations which were True.
        if (conditionsRequired == evaluatedTrue) {
            return FunctionArgument.trueObject;
        }
        return FunctionArgument.falseObject;
    }
}
