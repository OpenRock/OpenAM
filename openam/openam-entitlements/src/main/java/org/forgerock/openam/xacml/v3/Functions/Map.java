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
 * urn:oasis:names:tc:xacml:3.0:function:map
 This function converts a bag of values to another bag of values.
 This function SHALL take n+1 arguments, where n is one or greater.

 The first argument SHALL be a <Function> element naming a function that takes a n arguments of a primitive
 data-type and returns a value of a primitive data-type.

 Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one SHALL be a bag of a
 primitive data-type.

 The expression SHALL be evaluated as if the function named in the <Function> argument were applied to the n-1
 non-bag arguments and each element of the bag argument and resulting in a bag of the converted value.

 The result SHALL be a bag of the primitive data-type that is returned by the
 function named in the <xacml:Function> element.

 For example, the following expression,
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:map”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case”>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Hello</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>World!</AttributeValue>
 </Apply>
 </Apply>
 Evaluates to a bag containing “hello” and “world!”.
 */

import org.forgerock.openam.xacml.v3.model.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Map extends XACMLFunction {

    public Map() {
    }

    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        // Initialize
        XACMLFunction func = null;
        int args = getArgCount();
        // Validate the number of Function Arguments.
        if (args < 2) {
            throw new NotApplicableException("Not Correct Number of Arguments, must provide at least 2");
        }
        // Obtain our Primary Function to be Applied...
        if ((getArg(0) == null) || (!(getArg(0) instanceof XACMLFunction))) {
            throw new NotApplicableException("Map first argument is null or not a XACML Function");
        }
        // Cast our Function to be Applied.
        func = (XACMLFunction) getArg(0);
        // Create and initialize our Argument Result Stack.
        List<DataValue> results = new ArrayList<DataValue>();

        // Iterate over all arguments within the Argument Stack.
        for (int i = 1; i < args; i++) {
            func.clearArguments();
            FunctionArgument functionArgument = getArg(i).evaluate(pip);
            Object topDataValue = functionArgument.getValue(pip);

            // Check for Collection
            if (topDataValue instanceof Collection) {
                // Iterate over Collection DataValue Contents.
                for (DataValue innerDataValue : ((Collection<DataValue>) topDataValue)) {
                    func.clearArguments();
                    func.addArgument(innerDataValue);
                    // Evaluate the Function with required Arguments and Save Results.
                    FunctionArgument result = func.evaluate(pip);
                    results.add((DataValue) result);
                } // End of Inner While Loop.
            } else {
                func.clearArguments();
                func.addArgument((DataValue) topDataValue);
                // Evaluate the Function with required Argument and Save Result.
                FunctionArgument result = func.evaluate(pip);
                results.add((DataValue) result);
            } // End of Else.

        } // End of Outer Main For Loop.

        // Initialize Empty Bag.
        DataBag dataBag = new DataBag();
        if (results.size() > 0) {
            dataBag.setType(results.get(0).getType());
        }
        // Iterate Over our DataValue Results and Create an Appropriate Bag to Return with the results.
        for(DataValue dataValue : results) {
            dataBag.add(dataValue);
        }
        // Return our Resultant Data Bag Containing our Results.
        return dataBag;
    }

}
