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
package com.sun.identity.rest;


import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.xml.bind.JAXBElement;

import com.sun.identity.entitlement.xacml3.core.*;
import com.sun.identity.shared.debug.Debug;

import org.forgerock.openam.xacml.v3.model.*;

import com.sun.identity.entitlement.opensso.SubjectUtils;

import org.forgerock.openam.xacml.v3.resources.XACML3HomeResource;


/**
 * XACML v3 Request Handler
 * <p/>
 * Provides main end-point for all XACML v3.0 requests,
 * either XML or JSON based over HTTP/HTTPS REST based protocol flow.
 * Handles XACML end-point for RESTful operations in either XML or JSON representation.
 * <p/>
 * This ForgeRock developed XACML Resource Router complies with the following OASIS Specifications:
 * <ul>
 * <li>xacml-3.0-core-spec-cs-01-en.pdf</li>
 * <li>xacml-rest-v1.0-csprd01.pdf</li>
 * <li>xacml-json-http-v1-1.0-wd09.doc</li>
 * <li>...</li>
 * </ul>
 * <p/>
 * <b>The following XACML v3 End Points are currently viable:</b>
 * <table>
 * <tr><th>Method</th><th>XACML Path</th><th>Description</th></tr>
 * <tr><td>GET</td><td><ul><li>&#47;xacml&#47;</li></ul></td><td><em>Default, Provides Home Document</em></td></tr>
 * <p/>
 * <tr><td>POST</td><td><ul><li>&#47;xacml&#47;</li></ul></td><td><em>Default, Request from PEP</em></td></tr>
 * <tr><td>POST</td><td><ul><li>&#47;xacml&#47;pdp&#47;</li></ul></td><td><em>Request from PEP</em></td></tr>
 * </table>
 * <b><i>Future intended EndPoints which are not Implemented yet:</i></b>
 * <table>
 * <tr><th>Method</th><th>XACML Path</th><th>Description</th></tr>
 * <p/>
 * <tr><td>GET
 * </td><td><ul><li>&#47;openam&#47;xacml&#47;pip&#47;&lt;query&gt;</li></ul></td><td><em>Policy Information Point
 * Query</em></td></tr>
 * <tr><td>GET &amp; POST
 * </td><td><ul><li>&#47;openam&#47;xacml&#47;pap&#47;*</li></ul></td><td><em>Policy Administration Point</em></td></tr>
 * <tr><td>POST
 * </td><td><ul><li>&#47;openam&#47;xacml&#47;pap&#47;import&#47</li></ul></td><td><em>PAP Import Policy</em></td></tr>
 * <tr><td>POST
 * </td><td><ul><li>&#47;openam&#47;xacml&#47;pap&#47;export&#47</li></ul></td><td><em>PAP Export Policy</em></td></tr>
 * </table>
 *
 * @author allan.foster@forgerock.com
 */
@Path("/xacml")
public class XACML3RequestHandler {

    /**
     * Define our Static resource Bundle for our debugger.
     */
    private static Debug DEBUG = Debug.getInstance("amXACML");

    /**
     * GET
     * Provides the [HomeDocument] per OASIS Specification.
     *
     * @param httpServletRequest --  HttpServletRequest
     * @param securityContext    -- SecurityContext
     * @return String - rendered content.
     */
    @GET
    @Consumes // Consume any Application or Media Types
    @Produces({"application/xml", "application/json", "application/json-home"})
    public String getDefaultHomeResource(@Context javax.servlet.http.HttpServletRequest httpServletRequest,
                                         @Context javax.ws.rs.core.SecurityContext securityContext) {
        // Obtain our Content Type we are dealing with...
        ContentType requestContentType = getContentType(httpServletRequest);
        // Render the Proper HOME Document Resource.
        return XACML3HomeResource.getHomeDocument(httpServletRequest, requestContentType);
    }

    /**
     * GET
     * Provides the [HomeDocument] per OASIS Specification.
     *
     * @param httpServletRequest --  HttpServletRequest
     * @param securityContext    -- SecurityContext
     * @return String - rendered content.
     */
    @GET
    @Consumes // Consume All and any Application or Media Types
    @Produces({"application/xml", "application/json", "application/json-home"})
    @Path("/home")
    public String getHomeResource(@Context javax.servlet.http.HttpServletRequest httpServletRequest,
                                  @Context javax.ws.rs.core.SecurityContext securityContext) {
        return getDefaultHomeResource(httpServletRequest, securityContext);
    }

    /**
     * GET
     * Provides the [HomeDocument] per OASIS Specification.
     *
     * @param httpServletRequest --  HttpServletRequest
     * @param securityContext    -- SecurityContext
     * @return String - rendered content.
     */
    @GET
    @Consumes // Consume All and any Application or Media Types
    @Produces({"application/xml", "application/json", "application/json-home"})
    @Path("/pdp")
    public String getPDPHomeResource(@Context javax.servlet.http.HttpServletRequest httpServletRequest,
                                     @Context javax.ws.rs.core.SecurityContext securityContext) {
        return getDefaultHomeResource(httpServletRequest, securityContext);
    }

    /**
     * POST
     * Handle XML Requests
     *
     * @param req JAXBElement<Request>
     * @return JAXBElement<Response>
     */
    @POST
    @Consumes({"application/xml", "application/xacml+xml"})
    @Produces({"application/xml", "application/xacml+xml"})
    @Path("/pdp/{appname}")
    public JAXBElement<Response> getXMLDecision(JAXBElement<Request> req, @PathParam("appname") String appname) {
        final String methodName = "XACML3RequestHandler.getXMLDecision: ";
        Response response = null;
        try {
            Request request = req.getValue();
            Subject adminSubject = SubjectUtils.createSuperAdminSubject();
            // TODO :: I do not see a correct Request UnMarshal Functions Appearing in Stack?
            // TODO :: Nothing is available to evaluate.
            response = XACMLEvalContext.XACMLEvaluate(request, adminSubject, appname);
        } catch (Exception exception) {
            if (exception instanceof XACML3EntitlementException) {
                DEBUG.error(methodName + "Entitlement Exception Occurred: " + exception.getMessage(), exception);
                if (exception instanceof NotApplicableException) {
                    response = new XACML3NotApplicableResponse();
                } else {
                    response = new XACML3IndeterminateResponse();
                }
            } else {
                DEBUG.error(methodName + "Exception Occurred: " + exception.getMessage() + ", Returning Indeterminate.",
                        exception);
                response = new XACML3IndeterminateResponse();
            }
        }
        // If we have a unknown Response, indicate that the response is Indeterminate
        if ((response == null) || (response.getResult() == null) || (response.getResult().size() <= 0)) {
            response = new XACML3IndeterminateResponse();
        }
        // Marshal Response.
        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createResponse(response);
    }

    /**
     * POST
     * Handle JSON Requests
     *
     * @param req -- String representation of JSON XACML Request.
     * @return Response
     */
    @POST
    @Consumes({"application/json", "application/xacml+json"})
    @Produces({"application/json", "application/xacml+json"})
    @Path("/pdp/{appname}")
    public JAXBElement<Response> getJSONDecision(JAXBElement<Request> req, @PathParam("appname") String appname) {
        final String methodName = "XACML3RequestHandler.getJSONDecision: ";
        Response response = null;
        try {
            Request request = req.getValue();
            Subject adminSubject = SubjectUtils.createSuperAdminSubject();
            // TODO :: I do not see a correct Request UnMarshal Functions Appearing in Stack?
            // TODO :: Nothing is available to evaluate.
                response = XACMLEvalContext.XACMLEvaluate(request, adminSubject, appname);
        } catch (Exception exception) {
            if (exception instanceof XACML3EntitlementException) {
                DEBUG.error(methodName + "Entitlement Exception Occurred: " + exception.getMessage(), exception);
                if (exception instanceof NotApplicableException) {
                    response = new XACML3NotApplicableResponse();
                } else {
                    response = new XACML3IndeterminateResponse();
                }
            } else {
                DEBUG.error(methodName + "Exception Occurred: " + exception.getMessage() + ", Returning Indeterminate.",
                        exception);
                response = new XACML3IndeterminateResponse();
            }
        }
        // If we have a unknown Response, indicate that the response is Indeterminate
        if ((response == null) || (response.getResult() == null) || (response.getResult().size() <= 0)) {
            response = new XACML3IndeterminateResponse();
        }
        // Marshal Response.
        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createResponse(response);
    }

    /**
     public Response getJSONDecision( String req   ) {

     Request request = XACML3PrivilegeUtils.parseJSON(req);
     Subject adminSubject = SubjectUtils.createSuperAdminSubject();

     Response response = XACMLEvalContext.XACMLEvaluate(request, adminSubject);
     return response;

     }
     **/
    /**
     * POST
     * Handle either XML or JSON Requests
     *
     * @param req JAXBElement<Request>
     * @return JAXBElement<Response>
     */
    @POST
    @Consumes // Consume All and any Application or Media Types
    @Produces({"application/xml", "application/xacml+xml", "application/json", "application/xacml+json"})
    @Path("/pdp")
    public JAXBElement<Response> getDecision(JAXBElement<Request> req,
                                             @Context javax.servlet.http.HttpServletRequest httpServletRequest,
                                             @Context javax.ws.rs.core.SecurityContext securityContext) {
        // Obtain our Content Type we are dealing with...
        ContentType requestContentType = getContentType(httpServletRequest);
        // Perform Appropriate Decision Method by Content Type.
        if (requestContentType.getCommonType().equals(CommonType.XML)) {
           return getXMLDecision(req,"xacml3");
        } else {
           return getJSONDecision(req,"xacml3");
        }
    }

    /**
     * POST
     * Handle either XML or JSON Requests
     *
     * @param req JAXBElement<Request>
     * @return JAXBElement<Response>
     */
    @POST
    @Consumes // Consume All and any Application or Media Types
    @Produces({"application/xml", "application/xacml+xml", "application/json", "application/xacml+json"})
    public JAXBElement<Response> getDecisionall(JAXBElement<Request> req,
                                             @Context javax.servlet.http.HttpServletRequest httpServletRequest,
                                             @Context javax.ws.rs.core.SecurityContext securityContext) {
        // Obtain our Content Type we are dealing with...
       // ContentType requestContentType = getContentType(httpServletRequest);
        // Perform Appropriate Decision Method by Content Type.
       // if (requestContentType.getCommonType().equals(CommonType.XML)) {
           // return getXMLDecision(req);
       // } else {
           // return getJSONDecision(req);
       // }
        return null;
    }

    /**
     * POST
     * provides a simple debugging tool to dump and echo the request back to ensure
     * Data is in sync and correct for a quick eyeballing.
     *
     * @param req -- JAXBElement<Request>
     * @return JAXBElement<Request>
     */
    @POST
    @Consumes // Consume All and any Application or Media Types
    @Produces({"application/xml", "application/xacml+xml", "application/json", "application/xacml+json"})
    @Path("/dumprequest")
    public JAXBElement<Request> getDumpRequest(JAXBElement<Request> req) {
        Request request = req.getValue();
        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createRequest(request);
    }
    /**
     * POST
     * provides a simple debugging tool to dump and echo the request back to ensure
     * Data is in sync and correct for a quick eyeballing.
     *
     * @param req -- JAXBElement<Request>
     * @return JAXBElement<Request>
     */
    @POST
    @Consumes // Consume All and any Application or Media Types
    @Produces({"application/xml", "application/xacml+xml", "application/json", "application/xacml+json"})
    @Path("/dumprequest/{appname}")
    public JAXBElement<Request> getDecision(JAXBElement<Request> req,
                                             @Context javax.servlet.http.HttpServletRequest httpServletRequest,
                                             @Context javax.ws.rs.core.SecurityContext securityContext,
                                             @PathParam("appname") String appname) {

        Request request = req.getValue();
        ObjectFactory objectFactory = new ObjectFactory();
        return objectFactory.createRequest(request);
    }

    /**
     * Private helper method to obtain the Application Request Type.
     *
     * @param request HttpServletRequest
     * @return ContentType -- Enum Content Type Value for easy upstream comparisons.
     */
    private static ContentType getContentType(HttpServletRequest request) {
        return ((request.getContentType() == null) ? ContentType.XML :
                (ContentType.getNormalizedContentType(request.getContentType()) == null) ? ContentType.XML :
                        ContentType.getNormalizedContentType(request.getContentType()));
    }

}

