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
urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
functions form a minimal set for comparing two numbers, yielding a Boolean result.
For doubles they SHALL comply with the rules governed by IEEE 754 [IEEE754].*/

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

/**
 * urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal
 */
public class IntegerGreaterThanOrEqual extends XACMLFunction {

    public IntegerGreaterThanOrEqual()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() != 2) {
            return retVal;
        }

        Integer arg0 = getArg(0).asInteger(pip);
        Integer arg1 = getArg(1).asInteger(pip);

        if (arg0.intValue() >= arg1.intValue()) {
            retVal = FunctionArgument.trueObject;
        } else {
            retVal = FunctionArgument.falseObject;
        }
        return retVal;
    }
}
