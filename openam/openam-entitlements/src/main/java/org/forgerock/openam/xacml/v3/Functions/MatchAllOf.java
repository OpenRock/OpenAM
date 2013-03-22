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

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

/*
urn:oasis:names:tc:xacml:1.0:function:integer-equal
This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#integer”
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The function SHALL return “True” if and only if the two arguments represent the same number.
 */

public class MatchAllOf extends XACMLFunction {

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.trueObject;

        for (int i = 0; i < getArgCount(); i++) {
            FunctionArgument res = getArg(i).evaluate(pip);
            if (!res.isTrue()) {
                retVal = FunctionArgument.falseObject;
            }
        }
        return retVal;
    }
    public String toXML(String type) {
        String retVal = "";
        /*
             Handle Match AnyOf and AllOf specially
        */

        retVal = "<AllOf>" ;

        for (FunctionArgument arg : arguments){
            retVal = retVal + arg.toXML(type);
        }
        retVal = retVal + "</AllOf>" ;
        return retVal;
    }

}
