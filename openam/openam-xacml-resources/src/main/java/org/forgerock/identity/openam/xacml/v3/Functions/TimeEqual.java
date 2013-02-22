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
package org.forgerock.identity.openam.xacml.v3.Functions;

/*
urn:oasis:names:tc:xacml:1.0:function:time-equal
This function SHALL take two arguments of data-type
“http://www.w3.org/2001/XMLSchema#time” and SHALL return an
“http://www.w3.org/2001/XMLSchema#boolean”.
It SHALL perform its evaluation according to the “op:time-equal” function [XF] Section 10.4.12.
*/

import org.forgerock.identity.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.identity.openam.xacml.v3.Entitlements.XACMLEvalContext;

public class TimeEqual extends XACMLFunction {

    public TimeEqual()  {
    }
    public FunctionArgument evaluate( XACMLEvalContext pip){
        return FunctionArgument.falseObject;
    }
}
