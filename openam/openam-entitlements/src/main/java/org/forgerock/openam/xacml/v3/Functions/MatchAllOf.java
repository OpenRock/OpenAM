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

import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.model.XACMLFunction;

/**
 * 7.6 Match evaluation
 The attribute matching element <Match> appears in the <Target> element of rules, policies and policy sets.
 This element represents a Boolean expression over attributes of the request context.
 A matching element contains a MatchId attribute that specifies the function to be used in performing the match
 evaluation, an <AttributeValue> and an <AttributeDesignator> or <AttributeSelector> element that specifies
 the attribute in the context that is to be matched against the specified value.

 The MatchId attribute SHALL specify a function that takes two arguments, returning a result type of
 "http://www.w3.org/2001/XMLSchema#boolean". The attribute value specified in the matching element SHALL be supplied
 to the MatchId function as its first argument. An element of the bag returned by the <AttributeDesignator> or
 <AttributeSelector> element SHALL be supplied to the MatchId function as its second argument, as explained below.
 The DataType of the <AttributeValue> SHALL match the data-type of the first argument expected by the MatchId function.
 The DataType of the <AttributeDesignator> or <AttributeSelector> element SHALL match the data-type of the second
 argument expected by the MatchId function.

 In addition, functions that are strictly within an extension to XACML MAY appear as a value for the MatchId attribute,
 and those functions MAY use data-types that are also extensions, so long as the extension function
 returns a Boolean result and takes two single base types as its inputs.

 The function used as the value for the MatchId attribute SHOULD be easily indexable.
 Use of non-indexable or complex functions may prevent efficient evaluation of decision requests.

 The evaluation semantics for a matching element is as follows. If an operational error were to occur while evaluating
 the <AttributeDesignator> or <AttributeSelector> element, then the result of the
 entire expression SHALL be "Indeterminate".

 If the<AttributeDesignator>or <AttributeSelector> element were to evaluate
 to an empty bag, then the result of the expression SHALL be"False". Otherwise,
 the MatchId function SHALL be applied between the <AttributeValue> and each element of the bag returned from the
 <AttributeDesignator> or <AttributeSelector> element. If at least one of those function applications were to evaluate
 to "True", then the result of the entire expression SHALL be "True". Otherwise,
 if at least one of the function applications results in "Indeterminate", then the result SHALL be "Indeterminate".
 Finally, if all function applications evaluate to "False", then the result of the entire expression SHALL be "False".
 It is also possible to express the semantics of a target matching element in a condition.
 For instance, the target match expression that compares a “subject-name” starting with the name “John” can
 be expressed as follows:

 <Match MatchId=”urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>
 John.*
 </AttributeValue>
 <AttributeDesignator
 subject"
 </Match>
 Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-
 AttributeId=”urn:oasis:names:tc:xacml:1.0:subject:subject-id”
 DataType=”http://www.w3.org/2001/XMLSchema#string”/>
 Alternatively, the same match semantics can be expressed as an <Apply> element in a condition by using the “urn:oasis:names:tc:xacml:3.0:function:any-of” function, as follows:
 <Apply FunctionId=”urn:oasis:names:tc:xacml:3.0:function:any-of”>
 <Function
 FunctionId=”urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”/>
 <AttributeValue DataType=”http://www.w3.org/2001/XMLSchema#string”>
 John.*
 </AttributeValue>
 <AttributeDesignator
 Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-
 subject"
 AttributeId=”urn:oasis:names:tc:xacml:1.0:subject:subject-id”
 DataType=”http://www.w3.org/2001/XMLSchema#string”/>
 </Apply>
 */

public class MatchAllOf extends XACMLFunction {

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.trueObject;

        for (int i = 0; i < getArgCount(); i++) {
            FunctionArgument res = getArg(i).evaluate(pip);
            if (!res.isTrue()) {
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

        retVal = "<AllOf>" ;

        for (FunctionArgument arg : arguments){
            retVal = retVal + arg.toXML(type);
        }
        retVal = retVal + "</AllOf>" ;
        return retVal;
    }

}
