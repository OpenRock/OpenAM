/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock US. All Rights Reserved
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
package com.sun.identity.rest;


import com.sun.identity.entitlement.opensso.SubjectUtils;
import com.sun.identity.entitlement.xacml3.core.ObjectFactory;
import com.sun.identity.entitlement.xacml3.core.Request;
import com.sun.identity.entitlement.xacml3.core.Response;
import org.forgerock.openam.xacml.v3.model.XACML3PrivilegeUtils;
import org.forgerock.openam.xacml.v3.model.XACMLEvalContext;

import javax.security.auth.Subject;
import javax.ws.rs.*;
import javax.xml.bind.JAXBElement;


@Path("/xacmln")
public class XACML3nRequestHandler {

    @GET
    @Produces({"application/xml","application/xacml+xml"})
    public String getHomeResource() {
        return "This is a Home Resource";
    }

    @GET
    @Produces({"application/json","application/xacml+json"})
    public String getJSONHomeResource() {
        return "This is a Home Resource";
    }

    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    @Path("/pdp")
    public JAXBElement<Response> getDecision( JAXBElement<Request> req   ) {

        Response response = null;
        Request request = req.getValue();
        Subject adminSubject = SubjectUtils.createSuperAdminSubject();

        response = XACMLEvalContext.XACMLEvaluate( request,adminSubject);
        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createResponse(response);
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/pdp")
    public Response getJSONDecision( String req   ) {


        Response response = null;
        Request request = XACML3PrivilegeUtils.parseJSON(req);
        Subject adminSubject = SubjectUtils.createSuperAdminSubject();

        response = XACMLEvalContext.XACMLEvaluate( request,adminSubject);
        return response;

        //ObjectFactory objectFactory = new ObjectFactory();
        //JSONObject jo = new JSONObject(response);
        //return jo;
    }

    @POST
    @Consumes({"application/xml","application/json"})
    @Produces({"application/xml","application/json"})
    @Path("/dumprequest")
    public JAXBElement<Request> getRequest( JAXBElement<Request> req   ) {

        Response response = null;
        Request request = req.getValue();
        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createRequest(request);
    }




}

