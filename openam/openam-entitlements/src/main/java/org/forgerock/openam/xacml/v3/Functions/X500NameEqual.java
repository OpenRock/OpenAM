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
urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
This function SHALL take two arguments of
"urn:oasis:names:tc:xacml:1.0:data-type:x500Name"
and SHALL return an "http://www.w3.org/2001/XMLSchema#boolean".
It SHALL return “True” if and only if each Relative Distinguished Name (RDN) in the two arguments matches.
Otherwise, it SHALL return “False”.

Two RDNs shall be said to match if and only if the result of the following operations is “True” .

1. Normalize the two arguments according to IETF RFC 2253
"Lightweight Directory Access Protocol (v3): UTF-8 String Representation of Distinguished Names".

2. If any RDN contains multiple attributeTypeAndValue pairs,
re-order the Attribute ValuePairs in that RDN in ascending order when compared as octet strings
(described in ITU-T Rec. X.690 (1997 E) Section 11.6 "Set-of components").

3. Compare RDNs using the rules in IETF RFC 3280
 "Internet X.509 Public Key Infrastructure Certificate and Certificate Revocation List (CRL)
 Profile", Section 4.1.2.4 "Issuer".

 */

import org.forgerock.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.openam.xacml.v3.Entitlements.XACML3EntitlementException;
import org.forgerock.openam.xacml.v3.Entitlements.XACMLEvalContext;
import org.forgerock.openam.xacml.v3.Entitlements.XACMLFunction;

// TODO : Needs to be addressed per comparison to recognize all of the above Requirements...

/**
 * urn:oasis:names:tc:xacml:1.0:function:x500Name-equal
 */
public class X500NameEqual extends XACMLFunction {

    public X500NameEqual()  {
    }

    public FunctionArgument evaluate( XACMLEvalContext pip) throws XACML3EntitlementException {
        FunctionArgument retVal =  FunctionArgument.falseObject;

        if ( getArgCount() != 2) {
            return retVal;
        }
        if ( (getArg(0).getValue(pip)==null) || (getArg(1).getValue(pip)==null ) )  {
            return retVal;
        }
        String s = (String)getArg(0).getValue(pip);
        if ( s.equalsIgnoreCase((String)getArg(1).getValue(pip))) {
            retVal =   FunctionArgument.trueObject;
        } else {
            retVal =   FunctionArgument.falseObject;
        }
        return retVal;
    }
}
