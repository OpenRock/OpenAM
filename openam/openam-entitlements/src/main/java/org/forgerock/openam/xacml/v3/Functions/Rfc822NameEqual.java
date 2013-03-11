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

/*
urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
This function SHALL take two arguments of data-type
“urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name”
and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
It SHALL return “True” if and only if the two arguments are equal.
Otherwise, it SHALL return “False”.

An RFC822 name consists of a local-part followed by "@" followed by a domain-part.
The local-part is case-sensitive, while the domain-part (which is usually a DNS host name) is not case-sensitive.
Perform the following operations:

1. Normalize the domain-part of each argument to lower case

2. Compare the expressions by applying the function
    “urn:oasis:names:tc:xacml:1.0:function:string-equal” to the normalized arguments.
*/

import org.forgerock.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.openam.xacml.v3.Entitlements.XACMLEvalContext;

/**
 * urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal
 */
public class Rfc822NameEqual extends XACMLFunction {

    public Rfc822NameEqual()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip){
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() != 2) {
            return retVal;
        }
        if ( (getArg(0).getValue(pip)==null) || (getArg(1).getValue(pip)==null ) )  {
            return retVal;
        }
        // Split at the @ sign.
        String[] names1 = ((String)getArg(0).getValue(pip)).split("@");
        String[] names2 = ((String)getArg(0).getValue(pip)).split("@");
        if ( (names1 == null) || (names1.length != 2) ) {
            return retVal;
        }
        if ( (names2 == null) || (names2.length != 2) ) {
            return retVal;
        }
        if ( (names1[0].equals(names2[0])) && (names1[1].equalsIgnoreCase(names2[1])) ) {
            retVal =   FunctionArgument.trueObject;
        } else {
            retVal =   FunctionArgument.falseObject;
        }
        return retVal;
    }
}
