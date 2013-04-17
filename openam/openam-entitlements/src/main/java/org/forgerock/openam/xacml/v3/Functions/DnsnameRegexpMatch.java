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
 * urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
 This function decides a regular expression match.  It SHALL take two arguments; the first is of type
 “http://www.w3.org/2001/XMLSchema#string” and the second is of type “urn:oasis:names:tc:xacml:2.0:data-type:dnsName”.
 It SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  The first argument SHALL be a regular expression
 and the second argument SHALL be a DNS name.  The function SHALL convert the second argument to
 type “http://www.w3.org/2001/XMLSchema#string” with urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName,
 then apply “urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”.
 */

import org.forgerock.openam.xacml.v3.model.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
 */
public class DnsnameRegexpMatch extends XACMLFunction {

    public DnsnameRegexpMatch()  {
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
        StringFromDNSName stringFromDNSName = new StringFromDNSName();
        stringFromDNSName.addArgument(dataValue);
        dataValue = (DataValue) stringFromDNSName.evaluate(pip);
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
