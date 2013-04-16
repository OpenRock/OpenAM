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

import org.forgerock.openam.xacml.v3.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * urn:oasis:names:tc:xacml:1.0:function:all-of-all
 This function applies a Boolean function between the elements of two bags.
 The expression SHALL be "True" if and only if the supplied predicate is "True" between each and every element of the
 first bag collectively against all the elements of the second bag.

 This function SHALL take three arguments.

 The first argument SHALL be an <Function> element that names a
 Boolean function that takes two arguments of primitive types.

 The second argument SHALL be a bag of a primitive data-type.

 The third argument SHALL be a bag of a primitive data-type.

 The expression is evaluated as if the function named in the <Function> element were applied between every element
 of the second argument and every element of the third argument and the results were combined using
 “urn:oasis:names:tc:xacml:1.0:function:and”.

 The semantics are that the result of the expression is "True" if and only if the applied predicate is "True" for
 all elements of the first bag compared to all the elements of the second bag.

 For example, the following expression SHALL evaluate to "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:all-of-all”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>6</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>5</AttributeValue>
 </Apply>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>1</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
 </Apply>
 </Apply>

 This expression is "True" because all elements of the first bag, “5” and “6”,
 are each greater than all of the integer values “1”, ”2”, ”3”, ”4” of the second bag.

 */

public class AllOfAll extends XACMLFunction {

    public AllOfAll() {
    }

    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        // Initialize
        XACMLFunction func = null;
        DataBag bagOne = null;
        DataBag bagTwo = null;
        int args = getArgCount();
        // Validate the number of Function Arguments.
        if (args != 3) {
            throw new NotApplicableException("Not Correct Number of Arguments, must provide 3");
        }

        if ((getArg(0) == null) || (!(getArg(0) instanceof XACMLFunction))) {
            throw new NotApplicableException("AllOfAll first argument is null or not a XACML Function");
        }
        if ((getArg(1) == null) || (!(getArg(1) instanceof XACMLFunction))) {
            throw new NotApplicableException("AllOfAll second argument is null or not a Bag");
        }
        if ((getArg(2) == null) || (!(getArg(2) instanceof XACMLFunction))) {
            throw new NotApplicableException("AllOfAll third argument is null or not a Bag");
        }
        // Cast our Arguments...
        func = (XACMLFunction) getArg(0);
        bagOne = (DataBag) getArg(1).evaluate(pip);
        bagTwo = (DataBag) getArg(2).evaluate(pip);

        // Create our Result List for Applying an And against all results.
        List<DataValue> results = new ArrayList<DataValue>();
        // Cast the Bag Values.
        List<DataValue> bagOneValues = bagOne.getValue(pip);
        List<DataValue> bagTwoValues = bagTwo.getValue(pip);
        // Iterate over our First Bag against our Second Bag.
        for (DataValue dataValue1 : bagOneValues) {
            dataValue1.evaluate(pip);
            for (DataValue dataValue2 : bagTwoValues) {
                dataValue1.evaluate(pip);
                // Perform the Function upon our two Arguments.
                func.clearArguments();
                func.addArgument(dataValue1);
                func.addArgument(dataValue2);
                FunctionArgument result = func.evaluate(pip);
                if ((result == null) || (!(result instanceof DataValue))) {
                    throw new NotApplicableException("AllOfAll Resultant Function Argument is Invalid");
                }
                results.add((DataValue) result);
            } // End of Inner First Bag Loop.
        } // End of Outer Second Bag Loop.

        // Now Perform an And Function based upon all Results Received.
        And fAnd = new And();
        for(DataValue result : results) {
            fAnd.addArgument(result);
        }
        FunctionArgument result = fAnd.evaluate(pip);
        if ((result == null) || (!(result instanceof DataValue))) {
            throw new NotApplicableException("AllOfAll Resultant Function Argument is Invalid");
        }
        // Return final And Resultant
        return result;
    }

}
