/**
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.identity.openam.xacml.v3.resources;

import com.sun.identity.entitlement.xacml3.core.Response;

import org.forgerock.identity.openam.xacml.v3.model.XACML3Constants;
import org.forgerock.identity.openam.xacml.v3.model.XACMLRequestInformation;


/**
 * XACML PDP Resource
 * <p/>
 * Policy decision point (PDP)
 * The system entity that evaluates applicable policy and renders an authorization decision.
 *
 * This term is defined in a joint effort by the IETF Policy Framework Working Group
 * and the Distributed Management Task Force (DMTF)/Common Information Model (CIM) in [RFC3198].
 * This term corresponds to "Access Decision Function" (ADF) in [ISO10181-3].
 * <p/>
 * Provides for XACML 3 PDP Evaluation requests, either SOAP, XML or REST based.
 *
 *
 * @author Jeff.Schenk@forgerock.com
 */
public interface XacmlPDPResource  {

    /**
     * Provide a PDP Evaluation Method to return a XACML 3 Response Object.
     *
     * @param xacmlRequestInformation
     * @return Response
     */
    public Response XACMLEvaluate(XACMLRequestInformation xacmlRequestInformation);

}
