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
 * This function SHALL take any number of arguments of &lsquo;type&rsquo; and return a bag of &lsquo;type&rsquo;
 * values containing the values of the arguments.  An application of this function to zero arguments SHALL produce
 * an empty bag of the specified data-type.
 */

import org.forgerock.openam.xacml.v3.model.*;

/**
 *  urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag
 */
public class HexbinaryBag extends XACMLFunction {

    public HexbinaryBag()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        // Initialize Empty Bag.
        DataBag dataBag = new DataBag();
        // Set the Type of the Contents.
        dataBag.setType(DataType.XACMLHEXBINARY);
        // Loop Through Arguments in stack and evaluate to Build up Final Content, if any exists.
        int args = getArgCount();
        for (int i=0; i<args; i++) {
            FunctionArgument result = getArg(i).evaluate(pip);
            if (!(result instanceof DataValue)) {
                throw new IndeterminateException("Not a DataValue");
            }
            dataBag.add((DataValue)result);
        }
        // Return the DataBag.
        return dataBag;
    }
}
