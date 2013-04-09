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
 *  urn:oasis:names:tc:xacml:3.0:function:any-of
 This function applies a Boolean function between specific primitive values and a bag of values, and SHALL return
 "True" if and only if the predicate is "True" for at least one element of the bag.
 This function SHALL take n+1 arguments, where n is one or greater. The first argument SHALL be an
 <Function> element that names a Boolean function that takes n arguments of primitive types.
 Under the remaining n arguments, n-1 parameters SHALL be values of primitive data-types and one SHALL be a
 bag of a primitive data-type.  The expression SHALL be evaluated as if the function named in the
 <Function> argument were applied to the n-1 non-bag arguments and each element of the bag argument
 and the results are combined with “urn:oasis:names:tc:xacml:1.0:function:or”.

 For example, the following expression SHALL return "True":
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:any-of”>
 <Function FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-equal”/>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
 <Apply FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-bag”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>John</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Paul</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>George</AttributeValue>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>Ringo</AttributeValue>
 </Apply>
 </Apply>
 This expression is "True" because the first argument is equal to at least one of the elements of the bag,
 according to the function.

 */

import org.forgerock.openam.xacml.v3.model.*;

import java.util.List;

public class AnyOf extends XACMLFunction {

    public AnyOf()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {

        FunctionArgument retVal = FunctionArgument.falseObject;

        int args = getArgCount();
        if (args < 3) {
            throw new NotApplicableException("Not enough arguments");
        }
        XACMLFunction func = (XACMLFunction)getArg(0);
        FunctionArgument bag = getArg(args-1).evaluate(pip);
        if (bag instanceof DataValue) {
            bag = new DataBag((DataValue)bag);
        }
        if (!(bag instanceof DataBag))  {
            throw new NotApplicableException("AnyOf applied to NON bag");
        }

        for (int i = 1; i < args -1; i++) {
            FunctionArgument res = getArg(i).evaluate(pip);
            List<DataValue> bagVals = (List<DataValue>)bag.getValue(pip);
            boolean oneIsTrue = false;

            for (DataValue dv :  bagVals)  {
                func.clearArguments();
                func.addArgument(res).addArgument(dv);
                FunctionArgument result = func.evaluate(pip);
                if (result.isTrue()) {
                    oneIsTrue = true;
                }
            }
            if (oneIsTrue) {
                retVal = FunctionArgument.trueObject;
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
            retVal = "<AnyOf>" ;
        } else if (type.equals("Allow")) {
            retVal = "<Allow FunctionId=\"" + functionID + "\">" ;
        }

        for (FunctionArgument arg : arguments){
            retVal = retVal + arg.toXML(type);
        }
        if (type.equals("Match")) {
            retVal = retVal + "</AnyOf>" ;
        } else if (type.equals("Allow")) {
            retVal = retVal + "</Allow>" ;
        }

        return retVal;
    }

}
