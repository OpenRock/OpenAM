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
urn:oasis:names:tc:xacml:1.0:function:string-equal
This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#string”
and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
The function SHALL return "True" if and only if the value of both of its arguments
are of equal length and each string is determined to be equal.
Otherwise, it SHALL return “False”.
The comparison SHALL use Unicode codepoint collation,
as defined for the identifier http://www.w3.org/2005/xpath-functions/collation/codepoint by [XF].
*/

import org.forgerock.openam.xacml.v3.model.*;

import java.util.List;

public class DaytimedurationOneAndOnly extends XACMLFunction {

    public DaytimedurationOneAndOnly()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {

        if ( getArgCount() != 1) {
            throw new IndeterminateException("Should only be one");
        }
        FunctionArgument fArg = getArg(0).evaluate(pip);
        if (!(fArg instanceof DataBag)) {
            throw new IndeterminateException("Not a DataBag");
        }
        List<DataValue> vals = (List<DataValue>)fArg.getValue(pip);
        if (vals.size() > 1) {
            throw new IndeterminateException("Multiple Values in Bag");
        }
        DataValue dv =  vals.get(0);
        if (!dv.getType().isType(DataType.Type.XACMLDAYTIMEDURATIONTYPE)) {
            throw new IndeterminateException("Wrong Type");
        }
        return dv;
    }
}
