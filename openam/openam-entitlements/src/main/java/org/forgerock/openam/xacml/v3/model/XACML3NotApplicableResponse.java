/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock Incorporated. All Rights Reserved
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
package org.forgerock.openam.xacml.v3.model;


import com.sun.identity.entitlement.xacml3.core.DecisionType;
import com.sun.identity.entitlement.xacml3.core.Response;
import com.sun.identity.entitlement.xacml3.core.Result;

import java.io.Serializable;

/**
 * XACML3NotApplicableResponse Object
 * <p/>
 * Simple POJO to provide a Default Response Object whose status will
 * be statically set to Not Applicable.
 * XACML Operation Request.
 *
 * @author jeff.schenk@forgerock.com
 */
public class XACML3NotApplicableResponse extends XACML3DefaultResponse implements Serializable {

    /**
     * Default Constructor to provide a Not Applicable Object Status, with a provided Decision Type Status.
     */
    public XACML3NotApplicableResponse() {
        super(DecisionType.NOT_APPLICABLE);
    }


}
