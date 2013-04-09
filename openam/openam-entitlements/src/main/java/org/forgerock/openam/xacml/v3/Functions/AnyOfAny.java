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

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

public class AnyOfAny extends XACMLFunction {

    public AnyOfAny()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        return FunctionArgument.falseObject;
    }
}
