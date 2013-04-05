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
 * urn:oasis:names:tc:xacml:x.x:function:type-subset
 This function SHALL take two arguments that are both a bag of ‘type’ values.
 It SHALL return a “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the
 first argument is a subset of the second argument.  Each argument SHALL be considered to have had its
 duplicates removed, as determined by "urn:oasis:names:tc:xacml:x.x:function:type-equal", before the subset calculation.
 */

import org.forgerock.openam.xacml.v3.model.*;

public class IntegerSubset extends XACMLFunction {

    public IntegerSubset()  {
    }
    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        int args = getArgCount();
        if (args != 2) {
            throw new IndeterminateException("Function Requires 2 arguments, " +
                    "however " + args + " in stack.");
        }
        // Iterate Over the 2 DataBag's in Stack, Evaluate and determine if First Bag is a Subset of the Second Bag.
        boolean isSubSet = false;
        DataBag potentialSubset = null;
        DataBag fullSet = null;
        try {
            potentialSubset = (DataBag) getArg(0).evaluate(pip);
            fullSet = (DataBag) getArg(1).evaluate(pip);

            // Verify our Data Type with First Data Bag's Data Type.
            if (potentialSubset.getType().getIndex() != fullSet.getType().getIndex()) {
                throw new IndeterminateException("First Bag Type: " + potentialSubset.getType().getTypeName() +
                        ", however the subsequent Bag Type was " + fullSet.getType()
                        .getTypeName());
            }
            // Iterate over the current Bag.
            isSubSet = this.SubSet(potentialSubset, fullSet, pip);
        } catch (Exception e) {
            throw new IndeterminateException("Iterating over Arguments Exception: " + e.getMessage());
        }
        // Determine if we have in fact a subSet.
        return (isSubSet) ? FunctionArgument.trueObject: FunctionArgument.falseObject;
    }

    /**
     * Perform a SubSet function against two defined Bags.
     * @param firstBag
     * @param secondBag
     * @param pip
     * @return
     * @throws XACML3EntitlementException
     */
    private boolean SubSet(DataBag firstBag, DataBag secondBag, XACMLEvalContext pip) throws
            XACML3EntitlementException {
        int subSetCount = 0;
        // Iterate over the First Bag.
        for (int b = 0; b < firstBag.size(); b++) {
            DataValue dataValue = (DataValue) firstBag.get(b).evaluate(pip);
            // Although specification requires the use of Equal Function and iterate over Bag, the
            // contains method provides the same result.
            if (secondBag.contains(dataValue)) {
                subSetCount++;
            }
        } // End of Inner For Loop.
        // Determine if we have in-fact a subSet.
        return (subSetCount == firstBag.size());
    }
}
