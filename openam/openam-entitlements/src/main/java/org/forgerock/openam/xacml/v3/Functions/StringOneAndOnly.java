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
 * urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
 This function SHALL take a bag of ‘type’ values as an argument and SHALL return a value of ‘type’.
 It SHALL return the only value in the bag.  If the bag does not have one and only one value,
 then the expression SHALL evaluate to "Indeterminate".
 */

import org.forgerock.openam.xacml.v3.model.*;

import java.util.List;

public class StringOneAndOnly extends XACMLFunction {

    public StringOneAndOnly()  {
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
        if (!dv.getType().isType(DataType.Type.XACMLSTRINGTYPE)) {
            throw new IndeterminateException("Wrong Type");
        }
        return dv;
    }
}
