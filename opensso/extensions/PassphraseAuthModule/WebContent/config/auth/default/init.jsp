<%
				String protocol = request.getProtocol();
				String serviceURL  = request.getHeader("X-Forwarded-Server");				
				if(serviceURL != null){
					serviceURL = "/DistAuthUI";
				}else{
					serviceURL = "/opensso";				
				}	
					
%>


        