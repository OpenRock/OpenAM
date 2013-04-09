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
 * urn:oasis:names:tc:xacml:x.x:function:type-union
 This function SHALL take two or more arguments that are both a bag of ‘type’ values.
 The expression SHALL return a bag of ‘type’ such that it contains all elements of all the argument bags.
 No duplicates, as determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal", SHALL exist in the result.
 */

import org.forgerock.openam.xacml.v3.model.*;

public class DatetimeUnion extends XACMLFunction {

    public DatetimeUnion()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        int args = getArgCount();
        if ( args < 2) {
            throw new IndeterminateException("Function Requires at least 2 arguments, " +
                    "however " + getArgCount() + " in stack.");
        }
        // Create our union DataBag from other Bags.
        DataBag unionBag = new DataBag();
        // Iterate Over All DataBag's in Stack, Evaluate and create a Union of all Bags with Unique Objects.
        try {
            for (int i=0; i<args; i++) {
                DataBag bag  = (DataBag) getArg(i).evaluate(pip);
                if (bag == null) {
                    continue;
                }
                // Set our Union Data Bag with First Bag's Data Type and check subsequent Bags.
                if (i==0) {
                    unionBag.setType(bag.getType());
                } else {
                    // Verify our Data Type with First Data Bag's Data Type.
                    if (bag.getType().getIndex() != unionBag.getType().getIndex()) {
                        throw new IndeterminateException("First Bag Type: "+unionBag.getType().getTypeName()+
                                ", however a subsequent Bag Type was "+bag.getType()
                                .getTypeName());
                    }
                }
                // Iterate over the current Bag.
                for (int b=0; b<bag.size(); b++) {
                    DataValue dataValue = (DataValue) bag.get(b).evaluate(pip);
                    boolean contained = false;
                    for (int z=0; z<unionBag.size(); z++) {
                        // Apply the Typed Equal Function to determine if
                        // the object already exists in the Union Bag.
                        DatetimeEqual fEquals = new DatetimeEqual();
                        fEquals.addArgument(unionBag.get(z));
                        fEquals.addArgument(dataValue);
                        FunctionArgument result = fEquals.evaluate(pip);
                        if (result.isTrue()) {
                            contained=true;
                            break;
                        }
                    }
                    // Add the Object if not contained.
                    if (!contained) {
                        // Add the Unique DataValue Element into the Union Bag.
                        unionBag.add(dataValue);
                    }
                } // End of Inner For Loop.
            } // End of Outer For Loop.
        } catch (Exception e) {
            throw new IndeterminateException("Iterating over Arguments Exception: "+e.getMessage());
        }
        // Return our UnionBag Value.
        return unionBag;
    }
}
