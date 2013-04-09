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
 * urn:oasis:names:tc:xacml:x.x:function:type-is-in
 This function SHALL take an argument of ‘type’ as the first argument and a bag of ‘type’ values as the second argument
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The function SHALL evaluate to "True" if and only if the first argument matches by the
 "urn:oasis:names:tc:xacml:x.x:function:type-equal" any value in the bag.  Otherwise, it SHALL return “False”.
 */

import org.forgerock.openam.xacml.v3.model.*;

/**
 * urn:oasis:names:tc:xacml:1.0:function:dnsName-is-in
 */
public class DNSNameIsIn extends XACMLFunction {

    public DNSNameIsIn()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument returnValue = FunctionArgument.falseObject;
        if ( getArgCount() != 2) {
            throw new IndeterminateException("Function Requires 2 arguments, " +
                    "however " + getArgCount() + " in stack.");
        }
        // Verify the Arguments
        DataBag bag = null;
        DataValue bagElement = null;
        try {
             bagElement = (DataValue) getArg(0).evaluate(pip);
             bag = (DataBag) getArg(1).evaluate(pip);
        } catch (Exception e) {
            throw new IndeterminateException("Accessing Arguments Exception: "+e.getMessage());
        }
        if ( (bag == null) || (bagElement == null) ) {
            throw new IndeterminateException("No Element or Bag Arguments");
        }
        if (bag.getType().getIndex() != bagElement.getType().getIndex()) {
            throw new IndeterminateException("Bag Type: "+bag.getType().getTypeName()+", trying to compare against "+
                    bagElement.getType().getTypeName());
        }
        // Now Iterate over Bag contents to find bagElement hit.
        for(int i=0; i<bag.size(); i++) {
            DataValue dataValue = bag.get(i);
            // Check Equality by using this Types Equality Function.
            DNSNameEqual fEquals = new DNSNameEqual();
            fEquals.addArgument(bagElement);
            fEquals.addArgument(dataValue);
            FunctionArgument result = fEquals.evaluate(null);
            if (result.isTrue()) {
                returnValue = FunctionArgument.trueObject;
                break;
            }
        } // End of Bag Iteration Loop.
        // Return our Evaluated Return Value.
        return returnValue;
    }
}
