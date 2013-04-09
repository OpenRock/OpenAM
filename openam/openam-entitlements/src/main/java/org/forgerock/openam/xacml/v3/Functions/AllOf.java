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

import java.util.List;

/**
 * urn:oasis:names:tc:xacml:3.0:function:all-of
 * This function applies a Boolean function between a specific primitive value and a bag of values, and
 * returns "True" if and only if the predicate is "True" for every element of the bag.
 *
 * This function SHALL take n+1 arguments, where n is one or greater.
 *
 * The first argument SHALL be a <Function> element that names a Boolean function that takes n arguments of
 * primitive types. Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one
 * SHALL be a bag of a primitive data-type.
 *
 * The expression SHALL be evaluated as if the function named
 * in the <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument and
 * the results are combined with “urn:oasis:names:tc:xacml:1.0:function:and”.
 *
 * <p/>
 * For example, the following expression SHALL evaluate to "True":
 * <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:all-of”>
 * <Function FunctionId=”urn:oasis:names:tc:xacml:2.0:function:integer-greater-than”/>
 * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>10</AttributeValue>
 * <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:integer-bag”>
 * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>9</AttributeValue>
 * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>3</AttributeValue>
 * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>4</AttributeValue>
 * <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#integer”>2</AttributeValue>
 * </Apply>
 * </Apply>
 * This expression is "True" because the first argument (10) is greater than all of the elements of the bag (9,3,4 and 2).
 */

public class AllOf extends XACMLFunction {

    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.trueObject;

        int args = getArgCount();
        if (args < 3) {
            throw new NotApplicableException("Not enough arguments");
        }
        XACMLFunction func = (XACMLFunction) getArg(0);
        FunctionArgument bag = getArg(args - 1).evaluate(pip);
        if (bag instanceof DataValue) {
            bag = new DataBag((DataValue) bag);
        }
        if (!(bag instanceof DataBag)) {
            throw new NotApplicableException("AllOf can not be applied to a NON bag");
        }

        for (int i = 1; i < args - 1; i++) {
            FunctionArgument res = getArg(i).evaluate(pip);
            List<DataValue> bagVals = (List<DataValue>) bag.getValue(pip);
            boolean oneIsTrue = false;

            for (DataValue dv : bagVals) {
                func.clearArguments();
                func.addArgument(res).addArgument(dv);
                FunctionArgument result = func.evaluate(pip);
                if (result.isTrue()) {
                    oneIsTrue = true;
                }
            }
            if (!oneIsTrue) {
                retVal = FunctionArgument.falseObject;
            }
        }
        return retVal;
    }

    public String toXML(String type) {
        String retVal = "";
        /*
             Handle Match AnyOf and AllOf specially
        */

        if (type.equals("Match")) {
            retVal = "<AllOf>";
        } else if (type.equals("Allow")) {
            retVal = "<Allow FunctionId=\"" + functionID + "\">";
        }

        for (FunctionArgument arg : arguments) {
            retVal = retVal + arg.toXML(type);
        }
        if (type.equals("Match")) {
            retVal = retVal + "</AllOf>";
        } else if (type.equals("Allow")) {
            retVal = retVal + "</Allow>";
        }

        return retVal;
    }

}
