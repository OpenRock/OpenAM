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
 * urn:oasis:names:tc:xacml:x.x:function:type-intersection
 This function SHALL take two arguments that are both a bag of ‘type’ values.
 It SHALL return a bag of ‘type’ values such that it contains only elements that are common between the two bags,
 which is determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal".
 No duplicates, as determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal", SHALL exist in the result.
 */

import org.forgerock.openam.xacml.v3.model.*;

public class Rfc822NameIntersection extends XACMLFunction {

    public Rfc822NameIntersection()  {
    }
    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        int args = getArgCount();
        if (args != 2) {
            throw new IndeterminateException("Function Requires 2 arguments, " +
                    "however " + args + " in stack.");
        }
        // Create our union DataBag from other Bags.
        DataBag intersection = new DataBag();
        // Iterate Over the 2 DataBag's in Stack, Evaluate and create an Intersection of all common unique Objects.
        try {
            DataBag[] bags = new DataBag[2];
            bags[0] = (DataBag) getArg(0).evaluate(pip);
            bags[1] = (DataBag) getArg(1).evaluate(pip);

            // Verify our Data Type with First Data Bag's Data Type.
            if (bags[0].getType().getIndex() != bags[1].getType().getIndex()) {
                throw new IndeterminateException("First Bag Type: " + bags[0].getType().getTypeName() +
                        ", however the subsequent Bag Type was " + bags[1].getType()
                        .getTypeName());
            }

            // Iterate over the current Bag.
            for (int b = 0; b < bags[0].size(); b++) {
                DataValue dataValue = (DataValue) bags[0].get(b).evaluate(pip);
                boolean contained = false;
                for (int z=0; z<intersection.size(); z++) {
                    // Apply the Typed Equal Function to determine if
                    // the object already exists in the Intersection Bag.
                    Rfc822NameEqual fequals = new Rfc822NameEqual();
                    fequals.addArgument(intersection.get(z));
                    fequals.addArgument(dataValue);
                    FunctionArgument result = fequals.evaluate(null);
                    if (result.isTrue()) {
                        contained=true;
                        break;
                    }
                }
                // Add the Object if not contained.
                if (!contained) {
                    for (int z=0; z<bags[1].size(); z++) {
                        // Apply the Typed Equal Function to determine if
                        // the object already exists in the Intersection Bag.
                        Rfc822NameEqual fequals = new Rfc822NameEqual();
                        fequals.addArgument(bags[1].get(z));
                        fequals.addArgument(dataValue);
                        FunctionArgument result = fequals.evaluate(null);
                        if (result.isTrue()) {
                            contained=true;
                            break;
                        }
                    } // End of Inner For Loop.
                    // This will have tripped to true, within above loop.
                    if (contained) {
                        // Add the Unique DataValue Element into the Union Bag.
                        intersection.add(dataValue);
                    }
                }
            } // End of Outer For Loop.
        } catch (Exception e) {
            throw new IndeterminateException("Iterating over Arguments Exception: " + e.getMessage());
        }
        // Return our Intersection Value.
        return intersection;
    }
}
