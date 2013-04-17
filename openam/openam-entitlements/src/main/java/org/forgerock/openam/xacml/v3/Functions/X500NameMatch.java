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
 *
 * A.3.14 Special match functions
 These functions operate on various types and evaluate to “http://www.w3.org/2001/XMLSchema#boolean”
 based on the specified standard matching algorithm.

 urn:oasis:names:tc:xacml:1.0:function:x500Name-match
 This function shall take two arguments of "urn:oasis:names:tc:xacml:1.0:data-type:x500Name"
 and shall return an "http://www.w3.org/2001/XMLSchema#boolean".

 It shall return “True” if and only if the first argument matches some terminal sequence of
 RDNs from the second argument when compared using x500Name-equal.

 As an example (non-normative), if the first argument is “O=Medico Corp,C=US” and
 the second argument is “cn=John Smith,o=Medico Corp, c=US”, then the function will return “True”.
 *
 */
import org.forgerock.openam.xacml.v3.model.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * urn:oasis:names:tc:xacml:1.0:function:x500Name-match
 */
public class X500NameMatch extends XACMLFunction {

    public X500NameMatch()  {
    }

    public FunctionArgument evaluate(XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal = FunctionArgument.falseObject;
        // Validate argument List
        if (getArgCount() != 2) {
            return retVal;
        }
        // Check and Cast arguments.
        DataValue patternValue = (DataValue) getArg(0).evaluate(pip);
        DataValue dataValue = (DataValue) getArg(1).evaluate(pip);
        if ((patternValue == null) || (dataValue == null)) {
            throw new XACML3EntitlementException("No Pattern or Data Value Specified");
        }
        // Convert our URI to a simple String as per specification.
        StringFromX500Name stringFromX500Name= new StringFromX500Name();
        stringFromX500Name.addArgument(dataValue);
        dataValue = (DataValue) stringFromX500Name.evaluate(pip);
        // Apply the Pattern
        try {
            Pattern pattern = Pattern.compile(patternValue.asString(pip));
            Matcher matcher = pattern.matcher(dataValue.asString(pip));
            if (matcher.lookingAt()) {
                retVal = FunctionArgument.trueObject;
            }
        } catch (java.util.regex.PatternSyntaxException pse) {
            throw new XACML3EntitlementException("Pattern Syntax Exception: " + pse.getMessage());
        }
        return retVal;
    }

}
