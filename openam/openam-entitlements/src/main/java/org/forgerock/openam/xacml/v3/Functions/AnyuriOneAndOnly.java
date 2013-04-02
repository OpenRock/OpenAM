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

/**
 * urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only
 */
public class AnyuriOneAndOnly extends XACMLFunction {

    public AnyuriOneAndOnly()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        // Only should have one Argument, a Bag of the applicable type.
        int args = getArgCount();
        if (args != 1) {
            throw new IndeterminateException("Function Requires 1 argument, " +
                    "however " + getArgCount() + " in stack.");
        }
        // Ensure Contents are of Applicable Type.
        FunctionArgument functionArgument = getArg(0).evaluate(pip);
        if (!functionArgument.getType().isType(DataType.Type.XACMLANYURITYPE)) {
            throw new IndeterminateException("Expecting a Any URI Type of Bag, but encountered a "+
                    functionArgument.getType().getTypeName());
        }
        // Ensure we have a DataBag.
        if (!(functionArgument instanceof DataBag))  {
            throw new IndeterminateException("Expecting a Bag, but encountered instead a "+
                    functionArgument.getType().getTypeName());
        }
        // return the one and Only Entry, if Applicable.
        if ( ((DataBag) functionArgument).size() == 1  ) {
            return ((DataBag) functionArgument).get(0).evaluate(pip);
        } else if ( ((DataBag) functionArgument).size() > 1  ) {
            throw new IndeterminateException("Multiple Values in Bag: "+((DataBag) functionArgument).size());
        } else {
            throw new IndeterminateException("Nothing in Bag");
        }
    }
}
