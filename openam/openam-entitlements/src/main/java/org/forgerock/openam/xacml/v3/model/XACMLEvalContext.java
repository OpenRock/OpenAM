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

package org.forgerock.openam.xacml.v3.model;

import com.sun.identity.entitlement.Entitlement;
import com.sun.identity.entitlement.Evaluator;
import com.sun.identity.entitlement.xacml3.core.*;
import org.forgerock.openam.xacml.v3.profiles.XACML3AttributeHandler;
import org.forgerock.openam.xacml.v3.profiles.XACML3ProfileManager;

import javax.security.auth.Subject;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class XACMLEvalContext  {
    private XACML3Policy policyRef;
    private Response response;
    private XACML3Request requestContext;

    public XACMLEvalContext() {
        policyRef = null;
    }
    public XACMLEvalContext(XACML3Policy pRef) {
        policyRef = pRef;
    }
    public void setPolicy(XACML3Policy pol) {
        this.policyRef = pol;
    };

    public FunctionArgument getDefinedVariable(String variableID){
         return policyRef.getDefinedVariable(variableID);
    }
    public void setReponse(Response response) {
        this.response = response;
    }
    public Response getResponse() {
        return response;
    }
    public void setRequest(XACML3Request request) {
        this.requestContext = request;
    }
    public XACML3Request getRequest() {
        return requestContext;
    }
    public FunctionArgument resolve(DataDesignator designator) throws XACML3EntitlementException {
        return XACML3ProfileManager.getInstance().resolve(designator,requestContext);
    }

    public void setResult(XACML3Decision decision)  {
        Result r = new Result();
        r.setStatus(decision.getStatus());
        r.setDecision(decision.getDecision());
        response.getResult().add(r);
        /*
        Obligations obs = new Obligations();
        List<Obligation> ob = obs.getObligation();
        Obligation newOb = new Obligation();
        newOb.
        ob.addAll(decision.getObligations());

        r.setObligations(decision.getObligations());

          */

    }
    public static Response XACMLEvaluate(Request request, Subject adminSubject, String appname) {
        XACML3Request xReq = new  XACML3Request(request);
        XACMLEvalContext eContext =  new XACMLEvalContext();
        eContext.setRequest(xReq);

        try {
            Evaluator eval = new Evaluator(adminSubject,appname);
            Set<String> rNames = xReq.getResources();
            List<Entitlement> ent = eval.evaluate("/", adminSubject,rNames,eContext,xReq.getContextID());

        } catch (Exception ex) {

        }
        return null;
    }

}
