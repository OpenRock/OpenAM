<%--
   DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  
   Copyright © 2011 ForgeRock AS. All rights reserved.
  
   The contents of this file are subject to the terms
   of the Common Development and Distribution License
   (the License). You may not use this file except in
   compliance with the License.
                                                                                
   You can obtain a copy of the License at
   http://forgerock.org/license/CDDLv1.0.html 
   See the License for the specific language governing
   permission and limitations under the License.
                                                                                
   When distributing Covered Code, include this CDDL
   Header Notice in each file and include the License file
   at http://forgerock.org/license/CDDLv1.0.html
   If applicable, add the following below the CDDL Header,
   with the fields enclosed by brackets [] replaced by
   your own identifying information:
   "Portions Copyrighted [year] [name of copyright owner]"
                                                              
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%@ page language="java"%>
<%@ page import="org.owasp.esapi.*" %>
<%@ page import="com.iplanet.am.util.SystemProperties" %>
<%@ page import="com.sun.identity.shared.Constants" %>



<%
   String gotoURL = request.getParameter("goto");
   String ServiceURI = SystemProperties.get(Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR); 
   if (gotoURL == null || gotoURL.isEmpty() ) {
      gotoURL = ServiceURI + "/UI/Logout"; 
   } else {
       boolean isValidURL = ESAPI.validator().
               isValidInput("URLContext", gotoURL, "URL", 255, false); 
       boolean isValidURI = ESAPI.validator().
               isValidInput("HTTP URI: " + gotoURL, gotoURL, "HTTPURI", 2000, false);      
       if (!isValidURL && !isValidURI) {
           gotoURL = "wronggotoURL";
       }
   }
   
   System.out.println("gotoURL is: " + gotoURL);
   
   String logoutURL = request.getParameter("logoutURL"); 
   if (logoutURL == null) {
      logoutURL = "";
   } else {
       boolean isValidURL = ESAPI.validator().
               isValidInput("URLContext", logoutURL, "URL", 255, false); 
       if (!isValidURL) {     
           logoutURL = "wronglogoutURL";
       }
   }
 
   System.out.println("logoutURL is: " + logoutURL);
   
   String loggedout = request.getParameter("loggedout");
   if (loggedout != null && !loggedout.equalsIgnoreCase("donot") && 
               !loggedout.equalsIgnoreCase("logmeout")) {
           loggedout = "donot";
   }
   
%>

<html>


    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <link rel="stylesheet" href="<%= ServiceURI %>/css/styles.css" type="text/css" />
        <script language="JavaScript" src="<%= ServiceURI %>/js/browserVersion.js"></script>
        <script language="JavaScript" src="<%= ServiceURI %>/js/auth.js"></script>

        <script language="JavaScript">
            writeCSS('<%= ServiceURI %>');
        </script>
        <script type="text/javascript"><!--// Empty script so IE5.0 Windows will draw table and button borders
            //-->
        </script>
        <script>
                function adios() { 
                    window.location = "<%= gotoURL %>";
                }
        </script>
        <title>Logout</title>
    </head>

    <body>


        <div style="height: 50px; width: 100%;">

        </div>
        <center>
            <div style="background-image:url('<%= ServiceURI%>/images/login-backimage.jpg'); background-repeat:no-repeat; 
                 height: 435px; width: 728px; vertical-align: middle; text-align: center;">

                <table>
                    <tr height="100px"><td width="295px"></td>
                        <td></td></tr>
                    <tr><td width="295px"></td>
                        <td align="left"><img src="<%= ServiceURI %>/images/PrimaryProductName.png" /></td></tr>    
                    <tr><td width="295px"></td>
                        <td>

                            <% if (loggedout != null && loggedout.equalsIgnoreCase("logmeout")) {
                                System.out.println("logmeout was selected");
                            %>
                            <p> Logging you out from the IdP </p>
                            <div style="display:none">
                                <iframe border="0" width="0" height="0" src="<%= logoutURL %>" onload="adios()" >
                                    <p>Your browser does not support iframes</p>
                                </iframe>
                            </div>
                            <noscript>
                                <p>You have been loggedout from the OAuth IdP</p>
                                Click <a href="<%= ESAPI.encoder().encodeForHTMLAttribute(gotoURL) %>">here</a> to continue
                            </noscript>
                            <%                         
                                  } 
                                  if (loggedout != null && loggedout.equalsIgnoreCase("donot")){
                                    response.sendRedirect(gotoURL);
                                    return;
                                  }
                            %>

                            <%
                            if (loggedout == null) {
                            %>
                            <form name="signedForm" method="POST" action="">
                                <p>Do you want to logout from the OAuth 2.0 Service?</p>
                                <input name="<%= ESAPI.encoder().encodeForHTML("loggedout") %>" type="submit" 
                                       value="<%= ESAPI.encoder().encodeForHTML("logmeout") %>">
                                <Input name="<%= ESAPI.encoder().encodeForHTML("loggedout") %>" type="submit" 
                                       value="<%= ESAPI.encoder().encodeForHTML("donot") %>">
                                <%
                                  if (gotoURL != null && !gotoURL.isEmpty()) {
                                %>
                                <input name="<%= ESAPI.encoder().encodeForHTML("goto") %>" type="hidden" 
                                       value="<%= ESAPI.encoder().encodeForHTML(gotoURL) %>">
                                <%
                                         }
                                %>
                                <input name="<%= ESAPI.encoder().encodeForHTML("logoutURL") %>" type="hidden" 
                                       value="<%= ESAPI.encoder().encodeForHTML(logoutURL) %>">
                            </form>  
                            <% }  %>

                        </td>
                    </tr>

                </table>
            </div>
        </center>

    </body>
</html>      