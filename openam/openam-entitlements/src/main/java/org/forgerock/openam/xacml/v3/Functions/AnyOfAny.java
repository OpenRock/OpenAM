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
 * urn:oasis:names:tc:xacml:3.0:function:any-of-any
 This function applies a Boolean function on each tuple from the cross product on all bags
 arguments, and returns "True" if and only if the predicate is "True" for at least one inside-function call.

 This function SHALL take n+1 arguments, where n is one or greater.

 The first argument SHALL be an <Function> element that names a Boolean function that takes n arguments.

 The remaining arguments are either primitive data types or bags of primitive types.

 The expression SHALL be evaluated as if the function named in the <Function> argument was applied between
 every tuple of the cross product on all bags and the primitive values, and the results were
 combined using “urn:oasis:names:tc:xacml:1.0:function:or”.

 The semantics are that the result of the expression SHALL be "True" if and only if the applied predicate is
 "True" for at least one function call on the tuples from the bags and primitive values.

 For example, the following expression SHALL evaluate to "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:any-of-any”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-equal”/>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Mary</AttributeValue>
 </Apply>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>John</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>George</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
 </Apply>
 </Apply>
 This expression is "True" because at least one of the elements of the first bag, namely “Ringo”,
 is equal to at least one of the elements of the second bag.

 */

import org.forgerock.openam.xacml.v3.model.*;

import java.util.*;


public class AnyOfAny extends XACMLFunction {

    public AnyOfAny() {
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
            throw new NotApplicableException("AnyOfAny first argument is null or not a XACML Function");
        }
        // Cast our Function to be Applied.
        func = (XACMLFunction) getArg(0);

        // Create and initialize our Argument List Stack.
        List<DataArgumentStack> dataArgumentStacks = new ArrayList<DataArgumentStack>();

        // Itrate over all argument to create necessary Argument Stack.
        for (int i = 1; i < args-1; i++) {
            FunctionArgument functionArgument = getArg(i).evaluate(pip);
            Object topDataValue = functionArgument.getValue(pip);

            // Check for Collection
            if (topDataValue instanceof Collection) {
                    // Iterate over Collection DataValue Contents.
                    for ( DataValue innerDataValue : ((Collection<DataValue>) topDataValue) ) {
                        // Get every Available Cross DataValue Arguments.
                        List<DataValue> preparedDataArgumentStack =
                                prepareCrossArgumentArray(pip, i+1, args);
                        DataArgumentStack dataArgumentStack = new DataArgumentStack(innerDataValue,
                                preparedDataArgumentStack);
                        dataArgumentStacks.add(dataArgumentStack);
                    } // End of Inner While Loop.

            } else {
                // Get every Available Cross DataValue Arguments.
                List<DataValue> preparedDataArgumentStack =
                        prepareCrossArgumentArray(pip, i+1, args);
                DataArgumentStack dataArgumentStack = new DataArgumentStack((DataValue) topDataValue,
                        preparedDataArgumentStack);
                dataArgumentStacks.add(dataArgumentStack);
            } // End of Else.
        } // End of Outer Main For Loop.

        // Create our Result List for Applying an And against all results.
        List<DataValue>  results = new ArrayList<DataValue>();

        // Perform the Function against our Argument Stacks.
        // All DataValues at this point have all been flattened.
        for (DataArgumentStack dataArgumentStack : dataArgumentStacks)  {
            // Build up the Function to be Applied.
            func.clearArguments();
            int functionArguments = 1;
            func.addArgument(dataArgumentStack.getArgument());
            // Iterate over Arument Stack
            for (DataValue dataValue : dataArgumentStack.getDataValues()) {
                if (functionArguments < args-1) {
                    func.addArgument(dataValue);
                    functionArguments++;
                } else {
                    // Evaluate the Function with required Arguments.
                    FunctionArgument result = func.evaluate(pip);
                    results.add((DataValue) result);

                    func.clearArguments();
                    functionArguments=1;
                    func.addArgument(dataArgumentStack.getArgument());
                    func.addArgument(dataValue);
                    functionArguments++;

                    System.out.println("A Function Result: "+result.asBoolean(pip));
                }
            }
            // Anything left to be evaluated?
            if (func.getArgCount() == args-1) {
                FunctionArgument result = func.evaluate(pip);
                results.add((DataValue) result);

                System.out.println("Last Function Result: "+result.asBoolean(pip));
            }
        } // End of For Each Loop for Data Argument Stack.

        // Now Perform an Or Function based upon all Results Received.
        Or _OrFunction = new Or();
        for (DataValue result : results) {
            _OrFunction.addArgument(result);
        }
        FunctionArgument result = _OrFunction.evaluate(pip);
        if ((result == null) || (!(result instanceof DataValue))) {
            throw new NotApplicableException("AnyOfAny Resultant Function Argument is Invalid");
        }
        // Return final And Resultant
        return result;
    }

    /**
     * Private Helper Method to prepare a Cross Argument Array to be used with application of the intended
     * requested function.
     *
     * @param pip
     * @param start
     * @param end
     * @return
     * @throws XACML3EntitlementException
     */
    private List<DataValue> prepareCrossArgumentArray(XACMLEvalContext pip, int start, int end)
                                                throws XACML3EntitlementException {
        List<DataValue>  crossArgumentArray = new ArrayList<DataValue>();
        // Iterate over Values for Prior Value.
        for (int i = start; i < end; i++) {
            FunctionArgument functionArgument = getArg(i).evaluate(pip);
            Object dataValue = functionArgument.getValue(pip);
            if (dataValue instanceof Collection) {
                // Iterate over Collection DataValue Contents.
                for ( DataValue innerDataValue : ((Collection<DataValue>) dataValue) ) {
                    crossArgumentArray.add(innerDataValue);
                }
            } else {
                crossArgumentArray.add( (DataValue) dataValue);
            }
        }
        // Return the formulated Array for Function Application.
        return crossArgumentArray;
    }

}
