<%--
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  
   Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
   
   The contents of this file are subject to the terms
   of the Common Development and Distribution License
   (the License). You may not use this file except in
   compliance with the License.
 
   You can obtain a copy of the License at
   https://opensso.dev.java.net/public/CDDLv1.0.html or
   opensso/legal/CDDLv1.0.txt
   See the License for the specific language governing
   permission and limitations under the License.
 
   When distributing Covered Code, include this CDDL
   Header Notice in each file and include the License file
   at opensso/legal/CDDLv1.0.txt.
   If applicable, add the following below the CDDL Header,
   with the fields enclosed by brackets [] replaced by
   your own identifying information:
   "Portions Copyrighted [year] [name of copyright owner]"

--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ page import="com.sun.identity.saml2.assertion.*"%>
<%@ page import="com.sun.identity.saml2.common.*" %>
<%@ page import="com.sun.identity.fedlet.ag.*"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<%
    AssertionGen ag = new AssertionGen();

    String[] attrs = {"ATTR_UID"};
    String[] vals =  {"VALUE"};

        vals[0] = request.getParameter("ATTR_UID");

        String encodedResMsg = SAML2Utils.encodeForPOST(ag.getResponse(attrs,vals));
        MetaDataParser lparser = new MetaDataParser();
        String relayState = null;
        String acsURL = lparser.getSPbaseUrl();

        SAML2Utils.postToTarget(response, "SAMLResponse",
                   encodedResMsg, "RelayState", relayState, acsURL);
%>
