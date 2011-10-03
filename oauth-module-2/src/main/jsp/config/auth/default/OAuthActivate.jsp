<%--
   DO  NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  
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

<%@ page  language="java"%>
<%@ page import="org.owasp.esapi.ESAPI" %>
<%@ page import="com.iplanet.am.util.SystemProperties" %>
<%@ page import="com.sun.identity.shared.Constants" %>

<%
   String ServiceURI = SystemProperties.get(Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR);   
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
        <title>Activate code</title>

    </head>

<body>


    <div style="height: 50px; width: 100%;">

    </div>
    <center>
        <div style="background-image:url('<%= ServiceURI%>/images/login-backimage.jpg'); background-repeat:no-repeat; 
             height: 435px; width: 728px; vertical-align: middle; text-align: center;">

            <table>
                <tr height="100px">
                    <td width="295px"></td>
                    <td></td>
                </tr>
                <tr><td width="295px"></td>
                <form name="Login" method="POST" action="">

                    <td align="left"><img src="<%= ServiceURI %>/images/PrimaryProductName.png" /></td></tr>    
                    <tr><td width="295px"></td>
                        <td>
                            <p>You were sent an activation code to the email address configured in your profile. 
                                Please check your mail and click the link provided. 
                                If you have a problem when clicking the link, 
                                then copy and paste the activation code here and hit Enter. 
                                Thanks
                            </p>
                            <table align="center" border="0" cellpadding="2" cellspacing="2" >

                                <tr><td>
                                        <label for="activation" >Activation Code:</label>
                                    </td>
                                    <td>
                                        <input type="text" size="30" 
                                               name="<%= ESAPI.encoder().encodeForHTML("activation") %>">
                                    </td>
                                </tr>


                            </table>
                </form> 
                </td>
                </tr>

            </table>
        </div>
    </center>
</body>
</html>
