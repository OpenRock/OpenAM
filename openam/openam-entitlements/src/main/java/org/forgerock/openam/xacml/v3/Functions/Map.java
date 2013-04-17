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
 data-type and returns a value of a primitive data-type Under the remaining n arguments,
 n-1 parameters SHALL be values of primitive data-types and one SHALL be a bag of a primitive data-type.

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

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

public class Map extends XACMLFunction {

    public Map()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        return FunctionArgument.falseObject;
    }
}
