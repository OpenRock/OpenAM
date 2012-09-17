package org.forgerock.openam.oauth2.rest;

import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.sm.SMSEntry;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    FilterConfig filterConfig = null;

    public void init(FilterConfig filterConfig) throws ServletException{
        this.filterConfig = filterConfig;
    }

    public void doFilter ( ServletRequest request, ServletResponse response, FilterChain chain )
            throws IOException, ServletException{
        boolean authorized = false;
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        try {
            request.setCharacterEncoding("UTF-8");
            SSOTokenManager manager = SSOTokenManager.getInstance();
            SSOToken ssoToken = manager.createSSOToken(req);
            manager.validateToken(ssoToken);

            if (ssoToken.getPrincipal().getName().equalsIgnoreCase(
                    "id=amadmin,ou=user," + SMSEntry.getRootSuffix())
                    ) {
                //Is an admin continue
                authorized = true;
            } else {
                authorized = false;
            }
        } catch (SSOException e) {
            authorized = false;
        }

        if (authorized){
            chain.doFilter(request, response);
            return;
        } else {
            filterConfig.getServletContext().getRequestDispatcher("/UI/Login?goto="+req.getRequestURL().toString()).
                    forward(request, response);

            return;
        }

    }

    public void destroy(){

    }

}
