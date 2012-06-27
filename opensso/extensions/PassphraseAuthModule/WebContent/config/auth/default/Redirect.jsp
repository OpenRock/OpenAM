<%@taglib uri="/WEB-INF/jato.tld" prefix="jato"%>
<%@taglib uri="/WEB-INF/auth.tld" prefix="auth"%>
<%@include file="init.jsp"%>

<html>
	<jato:useViewBean className="com.sun.identity.authentication.UI.LoginViewBean">
		<%@ page contentType="text/html"%>
		<head>
			<title>&nbsp;</title>
			<%
				String ServiceURI = (String) viewBean.getDisplayFieldValue(viewBean.SERVICE_URI);
				String encoded = "false";
				String gotoURL = (String) viewBean.getValidatedInputURL(request.getParameter("goto"), request.getParameter("encoded"), request);
				if ((gotoURL != null) && (gotoURL.length() != 0)) {
				    encoded = "true";
				}
			%>			
			<link rel="stylesheet" href="<%= serviceURL %>/css/main.css" type="text/css" />
			<!--[if lt IE 8]>
			<link rel="stylesheet" type="text/css" href="<%=serviceURL%>/css/ielt8.css" />
			<![endif]-->
			<!--[if IE 7]>
			<link rel="stylesheet" type="text/css" href="<%=serviceURL%>/css/ie7.css" />
			<![endif]-->
			<!--[if IE 6]>
			<link rel="stylesheet" type="text/css" href="<%=serviceURL%>/css/ie6.css" />
			<![endif]-->
			<script language="JavaScript">
				writeCSS('<%= ServiceURI %>');
				<jato:content name="validContent">
				    function defaultSubmit() {
				        var hiddenFrm = document.forms['Login'];
				        if (hiddenFrm != null) {
					    hiddenFrm.elements['IDButton'].value = 'Submit';
				               hiddenFrm.submit();
				        }
				    }
				</jato:content>
			</script>
		</head>
		<body onload="defaultSubmit()" style="background-color:#0404B4">			
				<div id="outer">
					<div id="container">
						<div id="inner">
							<jato:content name="validContent">
								<auth:form name="Login" method="post" defaultCommandChild="DefaultLoginURL">
									<input name="IDButton" type="hidden">
									<input type="hidden" name="goto" value="<%= gotoURL %>">
									<input type="hidden" name="encoded" value="<%= encoded %>">
									<input type="hidden" name="realm" value="<%=request.getParameter("realm")%>">
									<input type="hidden" name="state" value="1">
								</auth:form>
							</jato:content>
						</div>
					</div>
				</div>				
		</body>
	</jato:useViewBean>
</html>