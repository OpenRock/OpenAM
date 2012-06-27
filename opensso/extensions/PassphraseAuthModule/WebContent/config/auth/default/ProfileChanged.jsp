<%@ page contentType="text/html"%>
<%@page info="Authentication Failed" language="java"%>
<%@taglib uri="/WEB-INF/jato.tld" prefix="jato"%>
<%@taglib uri="/WEB-INF/auth.tld" prefix="auth"%>
<%@include file="init.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<jato:useViewBean className="com.sun.identity.authentication.UI.LoginViewBean">
		<head>
			<title>Profile Updated</title>
			<%
				String ServiceURI = (String) viewBean.getDisplayFieldValue(viewBean.SERVICE_URI);
			%>
			<link rel="stylesheet" href="<%=serviceURL%>/css/main.css" type="text/css" />
			<!--[if lt IE 8]>
			<link rel="stylesheet" type="text/css" href="<%=serviceURL%>/css/ielt8.css" />
			<![endif]-->
			<!--[if IE 7]>
			<link rel="stylesheet" type="text/css" href="<%=serviceURL%>/css/ie7.css" />
			<![endif]-->
			<!--[if IE 6]>
			<link rel="stylesheet" type="text/css" href="<%=serviceURL%>/css/ie6.css" />
			<![endif]-->
			<script language="JavaScript" src="<%=ServiceURI%>/js/browserVersion.js"></script>
			<script language="JavaScript" src="<%=ServiceURI%>/js/auth.js"></script>
			<script language="javascript">writeCSS('<%=ServiceURI%>');</script>
			<script type="text/javascript">
				<!--// Empty script so IE5.0 Windows will draw table and button borders //-->
			</script>
		</head>
		<body>			
			<div id="outer">
				<div id="container">
					<div id="inner">						
						<div id="logo"><a href="/"><img src="<%=serviceURL%>/images/logo150.gif" alt=". Logo" /></a></div>			
						<div id="messagebox">
							<div id="message">
								<div class="indented">
									<span id="title">Profile Updated</span>
									<br>
								</div>	
								<div class="indented" style="margin-top:10px;">
									<span id="body"><a href="<%=ServiceURI%>/UI/Login">Click here to login</a></span>
								</div>	
							</div>
						</div>
					</div>
					<div id="footer"></div>
				</div>
			</div>		
		</body>
	</jato:useViewBean>
</html>