/*
 * Copyright © 2010 ApexIdentity Inc. All rights reserved.
 */

package com.apexidentity.saml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.apexidentity.el.Expression;
import com.apexidentity.filter.HeaderFilter;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.GenericHeaplet;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.HeapUtil;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.servlet.GenericServletHeaplet;

import com.apexidentity.model.MapNode;
import com.apexidentity.model.ListNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.NodeException;
import com.apexidentity.model.ValueNode;
import com.apexidentity.servlet.DispatchServlet;
import com.apexidentity.servlet.GenericServletHeaplet;
import com.apexidentity.servlet.HandlerServlet;
import com.apexidentity.util.CaseInsensitiveMap;
import com.apexidentity.config.ConfigUtil;
import com.apexidentity.util.CaseInsensitiveSet;
import com.apexidentity.util.MultiValueMap;
import com.sun.identity.shared.encode.URLEncDec;
import com.sun.identity.federation.common.FSUtils;
import com.sun.identity.saml.common.SAMLUtils;
import com.sun.identity.saml2.common.SAML2Constants;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2Utils;
import com.sun.identity.saml2.logging.LogUtil;
import com.sun.identity.saml2.meta.SAML2MetaException;
import com.sun.identity.saml2.meta.SAML2MetaManager;
import com.sun.identity.saml2.meta.SAML2MetaUtils;
import com.sun.identity.saml2.profile.ResponseInfo; 
import com.sun.identity.saml2.profile.SPACSUtils;
import com.sun.identity.saml2.profile.IDPProxyUtil;
import com.sun.identity.saml2.profile.SPCache;
import com.sun.identity.saml2.profile.SPSingleLogout;
import com.sun.identity.saml2.profile.SPSSOFederate;
import com.sun.identity.saml2.protocol.Response;
import com.sun.identity.saml2.assertion.Subject;
import com.sun.identity.plugin.session.SessionManager;
import com.sun.identity.plugin.session.SessionProvider;
import com.sun.identity.plugin.session.SessionException;

import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Receives HTTP requests from the Dispatcher for all federation end points.
 * Requests are then diverted to the correct end point processing module.
 * Processing modules are for single sign-on and single logout.
 * 
 */

public class FederationServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	// Attribute Mapping from the incoming assertion to the HttpSession
	// The default (empty) is to map all attr=val pairs to the same attr=val pairs in the HttpSession
	// If 

	final Map<String,String> attributeMapping = new HashMap<String,String>();
	     
    // URI to redirect to upon success,  this may be already set in the relayState (goto)
    // maybe we should use the relayState for this purpose, it is the standard
    // way to do it and the IDP is the one that has to remember the relayState (goto)
    // for the artifact profile
	
	private String subjectMapping;
    private String redirectURI;
    private String logoutURI;
    private String assertionConsumerEndpoint;
    private String SPinitiatedSSOEndpoint;
    private String singleLogoutEndpoint;
    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
    	
    	//Dispatch to IDPInitiatedSSO, SPInititatedSSO, or IDPInitiatedSLO based
    	//on the URI
    	
        try {
        	String path = request.getPathInfo();
            if (path.indexOf(assertionConsumerEndpoint) > 0) {
        	    serviceAssertionConsumer(request, response);
            }
            else if (path.indexOf(SPinitiatedSSOEndpoint) > 0) {
        	    serviceSPInitiatedSSO(request, response);
            }
            else if (path.indexOf(singleLogoutEndpoint) > 0){
        	    serviceIDPInitiatedSLO(request, response);
            }
            else {
        	    System.out.println("FederationServlet: URI not in service");
            }
            
        } 
        catch (NodeException ne) {
            SAMLUtils.sendError(request, response,
                response.SC_INTERNAL_SERVER_ERROR, "Failed to process SSO request",
                ne.getMessage());
            return;
        } 
        catch (SAML2Exception sme) {
            SAMLUtils.sendError(request, response,
                response.SC_INTERNAL_SERVER_ERROR, "Failed to process SSO request",
                sme.getMessage());
            return;
        } 
        catch (SessionException se) {
            SAMLUtils.sendError(request, response,
                response.SC_INTERNAL_SERVER_ERROR, "Failed to process SSO request",
                se.getMessage());
            return;
        }
        
        
    }
    
    /*
     * Whether IDP or SP initiated, the final request ends up here.  The assertion is
     * validated, attributes are retrieved from and set in the HttpSession where
     * downstream filters can access them and pass them on to the target application.
     */
    
    @SuppressWarnings("unchecked")
	private void serviceAssertionConsumer(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException, SAML2Exception, SessionException, NodeException
    {
        Map map = SPACSUtils.processResponseForFedlet(request, response);
	    String relayURI = (String) map.get(SAML2Constants.RELAY_STATE);
	    if (relayURI != null & !relayURI.equals("")) {
	    	redirectURI = relayURI;
	    }        
        addAttributesToSession(request, map);
        
        // Redirect back to the original target application's login page and let the filters take over
        // If the relayURI is set in the assertion we must use that, otherwise we will use the configured
        // value, which should be the login page for the target application.
 
        response.sendRedirect(redirectURI);
    }
    
     /** 
      * Store attribute value pairs in the session based on the assertionMapping 
      * found in config.json.  
      * The intent is to have a filter use one of these attributes as the subject and
      * possibly the password. The presence of these attributes in the Session implies the
      * assertion has been processed and validated. 
      * Format of the attributeMapping  sessionAttributename:assertionAttribute
      * sessionAttributeName:  attribute name added to the session
	  * 
	  * assertionAttribute: Name of the attribute to fetch from the assertion, the value becomes
	  * the value in the session
	  * 
      * @param request
      * @param attrs
      */
    
    private void addAttributesToSession(HttpServletRequest request, Map assertion) throws NodeException {
    	
    	HttpSession httpSession = request.getSession();
    	String sessionValue = null;
    	
    	Map attributeStatement = (Map)assertion.get(SAML2Constants.ATTRIBUTE_MAP);
    	System.out.println("FederationServlet Assertion attributes: " + attributeStatement);
    	for (String key : attributeMapping.keySet()) {
    	    sessionValue = (String)(((HashSet)attributeStatement.get(attributeMapping.get(key))).iterator().next());
    	    System.out.println("FederationServlet adding to session: " + key +"="+ sessionValue);
            httpSession.setAttribute(key, sessionValue);
        }
    	if (subjectMapping != null) {
    	    String subjectValue = ((Subject)assertion.get(SAML2Constants.SUBJECT)).getNameID().getValue();
    	    System.out.println("FederationServlet adding subject to session: " + subjectMapping +"="+ subjectValue);
    	    httpSession.setAttribute(subjectMapping, subjectValue);
    	}
    }
    
    
    @SuppressWarnings("unchecked")
	private void serviceSPInitiatedSSO (HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException, SAML2Exception, SessionException
    {
    	
    	// The metaAlias is the identifier for the SP to fetch the metaData
    	// If metaAlias is null, then use the first one found in the GW metaData
    	
    	String metaAlias = request.getParameter("metaAlias");
    	if ((metaAlias ==  null) || (metaAlias.length() == 0)) {
            SAML2MetaManager manager = new SAML2MetaManager();
            List spMetaAliases = 
                manager.getAllHostedServiceProviderMetaAliases("/");
            if ((spMetaAliases != null) && !spMetaAliases.isEmpty()) {
                metaAlias = (String) spMetaAliases.get(0);
            }
        }
    	String idpEntityID = request.getParameter("idpEntityID");
        Map paramsMap = SAML2Utils.getParamsMap(request);
        List list = new ArrayList();
        list.add(SAML2Constants.NAMEID_TRANSIENT_FORMAT);
        
        //next line testing to see if we can change the name format
        //list.add("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
        
        paramsMap.put(SAML2Constants.NAMEID_POLICY_FORMAT, list);
        
        //TODO add option to specify artifact 
        if (paramsMap.get(SAML2Constants.BINDING) == null) {
            // use POST binding
            list = new ArrayList();
            list.add(SAML2Constants.HTTP_POST);
            paramsMap.put(SAML2Constants.BINDING, list);
        }
        if ((idpEntityID == null) || (idpEntityID.length() == 0)) {
            SAML2MetaManager manager = new SAML2MetaManager();
            List idpEntities = manager.getAllRemoteIdentityProviderEntities("/");
            if ((idpEntities != null) && !idpEntities.isEmpty()) {
            	idpEntityID = (String)idpEntities.get(0);
            }
        }
        if (metaAlias == null || idpEntityID == null) {
        	throw new SAML2Exception("No metadata for SP or IDP");
        }
        SPSSOFederate.initiateAuthnRequest(request,response,metaAlias,
                idpEntityID, paramsMap);
    }
       
    private void serviceIDPInitiatedSLO (HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException, SAML2Exception, SessionException
    {
    	String relayState = request.getParameter(SAML2Constants.RELAY_STATE);
    	String samlRequest = request.getParameter(SAML2Constants.SAML_REQUEST);
    	SPSingleLogout.processLogoutRequest(request,response,
                samlRequest,relayState);
    	System.out.println("FederationServlet serviceIDPInitiatedSLO success redirect to logout of the app at " + logoutURI);
    	response.sendRedirect(logoutURI);
    }
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
    
        	final Map<String,String> tagSwapMap = new HashMap<String,String>();
        	
            FederationServlet servlet = new FederationServlet();
            MapNode mappings = config.get("assertionMapping").required().asMapNode();
            for (String key : mappings.keySet()) {
                servlet.attributeMapping.put(key, mappings.get(key).asString());
            }
            servlet.subjectMapping = config.get("subjectMapping").asString();
            servlet.redirectURI = config.get("redirectURI").asString();
            servlet.logoutURI = config.get("logoutURI").asString();
            servlet.assertionConsumerEndpoint = config.get("assertionConsumerEndpoint").defaultTo("fedletapplication").asString();
            servlet.SPinitiatedSSOEndpoint = config.get("SPinitiatedSSOEndpoint").defaultTo("SPInitiatedSSO").asString();
            servlet.singleLogoutEndpoint = config.get("singleLogoutEndpoint").defaultTo("fedletSlo").asString();
            
            // Get the Gateway configuration directory and set it as a system property to
            // override the default openFed location
            // Federation config files will reside in the SAML directory
            
            String openFedConfigDir = ConfigUtil.getDirectory("ApexIdentity", "SAML").getPath();
            System.out.println("FederationServlet init: " + openFedConfigDir);
            Properties p = System.getProperties();
            p.setProperty("com.sun.identity.fedlet.home",openFedConfigDir);
            System.setProperties(p);
                       
            return servlet;
        }
    }
}

