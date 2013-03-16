/**
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.identity.openam.xacml.v3.services;

import com.iplanet.dpro.session.service.InternalSession;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.authentication.internal.AuthPrincipal;
import com.sun.identity.common.SystemConfigurationUtil;

import com.sun.identity.log.LogConstants;
import com.sun.identity.log.LogRecord;
import com.sun.identity.log.Logger;
import com.sun.identity.log.messageid.LogMessageProvider;
import com.sun.identity.log.messageid.MessageProviderFactory;
import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.security.AdminDNAction;
import com.sun.identity.security.AdminPasswordAction;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;

import org.forgerock.identity.openam.xacml.v3.commons.*;
import org.forgerock.identity.openam.xacml.v3.model.AuthenticationDigest;
import org.forgerock.identity.openam.xacml.v3.model.XACML3Constants;
import org.forgerock.identity.openam.xacml.v3.model.XACMLRequestInformation;
import org.forgerock.identity.openam.xacml.v3.resources.*;

import org.json.JSONException;
import org.xml.sax.SAXException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * XACML v3 Resource Router
 * <p/>
 * Provides main end-point for all XACML v3.0 requests,
 * either XML or JSON based over HTTP/HTTPS REST based protocol flow.
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
 * <tr><td>GET</td><td><ul><li>&#47;openam&#47;xacml&#47;</li></ul></td><td><em>Default, Provides Home Document</em></td></tr>
 * <tr><td>GET</td><td><ul><li>&#47;openam&#47;xacml&#47;home&#47;</li></ul></td><td><em>Provides Home Document</em></td></tr>
 * <tr><td>GET</td><td><ul><li>&#47;openam&#47;xacml&#47;status&#47;</li></ul></td><td><em>Provides Status and Home
 * Document</em></td></tr>
 * <tr><td>GET</td><td><ul><li>&#47;openam&#47;xacml&#47;ping&#47;</li></ul></td><td><em>Provides
 * Roundtrip from Ping Request to Ping Response.  Ping Response will always be in the form of a Decision Type of
 * INDETERMINATE to conform to a standard XACML Decision Type. [ForgeRockOnly]
 * </em></td></tr>
 * <p/>
 * <tr><td>POST</td><td><ul><li>&#47;openam&#47;xacml&#47;</li></ul></td><td><em>Default, Request from PEP</em></td></tr>
 * <tr><td>POST</td><td><ul><li>&#47;openam&#47;xacml&#47;pdp&#47;</li></ul></td><td><em>Request from PEP</em></td></tr>
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
 * WWW-Authentication via Digest definition can be found here:
 * http://en.wikipedia.org/wiki/Digest_Access_Authentication#Alternative_authentication_protocols
 *
 * @author Jeff.Schenk@forgerock.com
 */
public class XacmlContentHandlerService extends HttpServlet  {
    private static String LOG_PROVIDER = "amXACML";
    /**
     * Initialize our Resource Bundle.
     */
    protected static final String RESOURCE_BUNDLE_NAME = "amXACML";
    protected static ResourceBundle resourceBundle =
            com.sun.identity.shared.locale.Locale.getInstallResourceBundle(RESOURCE_BUNDLE_NAME);
    /**
     * Attribute that specifies maximum content length for SAML request in
     * <code>AMConfig.properties</code> file.
     */
    public static final String HTTP_MAX_CONTENT_LENGTH =
            "com.sun.identity.xacml.request.maxContentLength";

    /**
     * Default maximum content length is set to 16k.
     */
    public static final int defaultMaxLength = 16384;

    /**
     * Default maximum content length in string format.
     */
    public static final String DEFAULT_CONTENT_LENGTH =
            String.valueOf(defaultMaxLength);

    private static int maxContentLength = 0;

    /**
     * Define our Static resource Bundle for our debugger.
     */
    private static Debug debug;

    /**
     * Access Logging
     */
    private static final String amXACMLErrorLogFile = "amXACML.error";
    private static final String amXACMLLogFile = "amXACML.access";

    private static Logger logger = null;
    private static Logger errorLogger = null;
    private static LogMessageProvider logProvider = null;

    private static boolean logStatus = false;

    /**
     * Preserve our Servlet Context PlaceHolder,
     * for referencing Artifacts.
     */
    private static ServletContext servletCtx;

    /**
     * XACML Schemata for Validation.
     */
    private static Schema xacmlSchema;

    /**
     * Digest Authentication Objects.
     */
    private static String nonce;
    private static ScheduledExecutorService nonceRefreshExecutor;

    /**
     * SSO Token Manager Instance Reference.
     */
    private static SSOTokenManager ssoManager = null;

    /**
     * This token is used to satisfy the admin interfaces
     */
    private static SSOToken adminToken = null;

    /**
     * OpenAM Admin DN and Credentials...
     * TODO : Credentials should be obfuscated!
     */
    private static String dsameAdminDN = null;
    private static String dsameAdminPassword = null;

    /**
     * Initialize our Servlet/Restlet Request Handler for All
     * XACML v3 Requests.
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        // ******************************************************
        // Acquire our Logging Interface.
        debug = Debug.getInstance("amXACML");
        // ******************************************************
        // Acquire Servlet Context.
        servletCtx = config.getServletContext();

        // *****************************************************
        // Initialize our OpenAM principal and credentials
        // for performing administrative functions if necessary.
        dsameAdminDN = (String) AccessController
                .doPrivileged(new AdminDNAction());
        dsameAdminPassword = (String) AccessController
                .doPrivileged(new AdminPasswordAction());

        // ***************************************************
        // Acquire MaxContent Length
        try {
            maxContentLength = Integer.parseInt(SystemConfigurationUtil.
                    getProperty(HTTP_MAX_CONTENT_LENGTH, DEFAULT_CONTENT_LENGTH));
        } catch (NumberFormatException ne) {
            debug.error("Wrong format of XACML request max content "
                    + "length. Using Default Value.");
            maxContentLength = defaultMaxLength;
        }
        // ***************************************************
        // Get Schema for Validation.
        try {
            SchemaFactory constraintFactory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // Create Streams for every applicable schema.
            InputStream xmlCoreSchemaResourceContentStream = XACML3Utils.getResourceContentStream
                    (XACML3Constants.xmlCoreSchemaResourceName);
            InputStream resourceContentStream = XACML3Utils.getResourceContentStream(XACML3Constants.xacmlCoreSchemaResourceName);
            // Create the schema object from our Input Source Streams.
            if ((xmlCoreSchemaResourceContentStream != null) && (resourceContentStream != null)) {
                xacmlSchema = constraintFactory.newSchema(new StreamSource[]{new StreamSource
                        (xmlCoreSchemaResourceContentStream), new StreamSource(resourceContentStream)});
                xmlCoreSchemaResourceContentStream.close();
                resourceContentStream.close();
            }
        } catch (SAXException se) {
            debug.error("SAX Exception obtaining XACML Schema for Validation,", se);
        } catch (IOException ioe) {
            debug.error("IO Exception obtaining XACML Schema for Validation,", ioe);
        }
        // ***************************************************
        // Ensure we are ok and have necessary assets to run.
        if (xacmlSchema != null) {
            debug.error("Initialization of XACML Content Resource Router, Server Information: " + servletCtx.getServerInfo());
            debug.error("XACML v3 Schema Version: "+ XACML3Constants.XACML3_NAMESPACE);
        }
        // ***************************************************
        // Initialize our Authentication Digest Thread.
        nonce = calculateNonce();

        nonceRefreshExecutor = Executors.newScheduledThreadPool(1);

        nonceRefreshExecutor.scheduleAtFixedRate(new Runnable() {

            public void run() {
                nonce = calculateNonce();
            }
        }, 1, 1, TimeUnit.MINUTES);

        // *****************************************************
        // Allow Parent to initialize as well.
        super.init(config);
    }

    // ******************************************************************************************************
    // Servlet/Restlet Processing Methods
    // ******************************************************************************************************

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "ForgeRock OpenAM XACML v3 PDP Content Handler Implementation, Standards per OASIS, 2013.";
    }

    /**
     * Handles the HTTP <code>GET</code> method XACML REST Request.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /**
         * Process our GET Method.
         */
        processRequest(request, response);

        // GET operations to PDP.

        /**
         * Id: urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:status
         ￼
         Normative Source
         ￼GET on the home location MUST return status code 200
         ￼
         Target
         Response to GET request on the home location
         ￼
         Predicate
         ￼ The HTTP status code in the [response] is 200
         ￼
         Prescription Level
         ￼mandatory
         */


        /**
         * Id ￼
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:body

         Normative Source￼
         GET on the home location MUST return a home document
         ￼
         Target￼
         Response to GET request on the home location
         ￼
         Predicate
         The HTTP body in the [response] follows the home document schema
         [HomeDocument]
         ￼
         Prescription Level
         mandatory
         */


        /**
         * Id
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:pdp
         ￼
         Normative Source
         The XACML entry point representation SHOULD contain a link to the PDP
         ￼
         Target
         Response to GET request on the home location
         ￼
         Predicate
         The home document in the [response] body contains a resource with link relation
         http://docs.oasis-open.org/ns/xacml/relation/pdp and a valid URL
         ￼
         Prescription Level
         mandatory
         */


    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  the <code>HttpServletRequest</code> object.
     * @param response the <code>HttpServletResponse</code> object.
     * @throws ServletException    if the request could not be
     *                             handled.
     * @throws java.io.IOException if an input or output error occurs.
     */
    @Override
    public void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        /**
         * Process our POST Method.
         */
        processRequest(request, response);

        // POST operations to PDP.

        /**
         * ￼
         Id   ￼
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:pdp:xacml:status
         ￼
         Normative Source     ￼
         POST on the PDP with a valid XACML request MUST return status code 200
         ￼
         Target
         Response to POST request on the PDP location with valid XACML request in the body
         ￼
         Predicate
         The HTTP status code in the [response] is 200
         ￼
         Prescription Level
         mandatory
         */


        /**
         * Id￼
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:pdp:xacml:body
         ￼
         Normative Source
         POST on the PDP with a valid XACML request MUST return a valid XACML response in the body
         ￼
         Target
         Response to POST request on the PDP location with valid XACML request in the body
         ￼
         Predicate
         The HTTP body in the [response] is a valid XACML response
         ￼
         Prescription Level
         mandatory
         */


        /**
         * ￼
         Id
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:pdp:xacml:invalid
         ￼
         Normative Source
         POST on the PDP with an invalid XACML request MUST return status code 400 (Bad Request)
         ￼
         Target
         Response to POST request on the PDP location with invalid XACML request in the body
         ￼
         Predicate
         The HTTP status code in the [response] is 400
         ￼
         Prescription Level
         mandatory
         */


        /**
         * Id
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:pdp:saml:status
         ￼
         Normative Source
         POST on the PDP with a valid XACML request MUST return status code 200
         ￼
         Target
         Response to POST request on the PDP location with valid XACML request wrapped in a
         xacml-samlp:XACMLAuthzDecisionQuery in the body
         ￼
         Predicate
         The HTTP status code in the [response] is 200
         ￼
         Prescription Level
         optional
         */


        /**
         * Id
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:pdp:saml:body
         ￼
         Normative Source
         POST on the PDP with a valid XACML request MUST return a valid XACML response in the body
         ￼
         Target
         Response to POST request on the PDP location with valid XACML request wrapped in a
         xacml-samlp:XACMLAuthzDecisionQuery in the body
         ￼
         Predicate
         The HTTP body in the [response] is a valid XACML response wrapped in a
         samlp:Response
         ￼
         Prescription Level
         optional
         */


        /**
         * ￼
         Id
         urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:pdp:saml:invalid
         ￼
         Normative Source
         POST on the PDP with an invalid XACML request MUST return status code 400 (Bad Request)
         ￼
         Target
         Response to POST request on the PDP location with invalid XACML request
         wrapped in a xacml-samlp:XACMLAuthzDecisionQuery in the body
         ￼
         Predicate
         The HTTP status code in the [response] is 400
         ￼
         Prescription Level
         optional
         */


    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Put Method Not Implemented.
        this.renderNotImplemented(resp);
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Delete Method Not Implemented.
        this.renderNotImplemented(resp);
    }

    // ******************************************************************************************************
    // Common Private Methods
    // ******************************************************************************************************

    /**
     * Generate the Authentication Header with our Digest.
     *
     * @return String of Generated Authentication Header.
     */
    private String generateAuthenticateHeader(String realm) {
        StringBuilder header = new StringBuilder();
        header.append("Digest realm=\"").append(realm).append("\",");
        if (!StringUtils.isBlank(XACML3Constants.AUTHENTICATION_METHOD)) {
            header.append("qop=\"").append(XACML3Constants.AUTHENTICATION_METHOD).append("\",");
        }
        header.append("algorithm=\"").append("md5").append("\",");
        header.append("nonce=\"").append(nonce).append("\",");
        header.append("opaque=\"").append(this.getOpaque(realm, nonce)).append("\"");
        // Return generated Authentication Header value.
        return header.toString();
    }

    /**
     * Private Helper Method to Generate a random Seed for Authentication Digest.
     *
     * @return String of Calculated Nonce
     */
    private String calculateNonce() {
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
        String fmtDate = f.format(d);
        Random rand = new Random(100000);
        Long randomLong = rand.nextLong();
        return DigestUtils.md5Hex(fmtDate + randomLong.toString());
    }

    /**
     * Private Helper Method to Generate the Opaque Generated String
     *
     * @param domain
     * @param nonce
     * @return String of Calculated Opaque
     */
    private String getOpaque(String domain, String nonce) {
        return DigestUtils.md5Hex(domain + nonce);
    }

    /**
     * Authenticate using a Digest.
     * <p/>
     * Per RFC2617: @see http://tools.ietf.org/html/rfc2617
     *
     * @param authenticationHeader -Example of Data for WWW-Authenticate.
     *                             Digest realm="example.org",qop=auth,nonce="9fc422776b40c52a8a107742f9a08d5c",
     *                             opaque="aba7d38a079f1a7d2e0ba2d4b84f3aa2"
     * @param requestBody
     * @param request
     * @return AuthenticationDigest valid Object or Null is UnAuthorized.
     * @throws ServletException
     * @throws IOException
     */
    private AuthenticationDigest authenticateUsingDigest(final String authenticationHeader,
                                                         final String requestBody,
                                                         final HttpServletRequest request, String realm)
            throws ServletException, IOException {
        final String classMethod = "XacmlContentHandlerService:authenticateUsingDigest";
        // *********************************************
        // Parse the Authentication Header Information.
        if (debug.messageEnabled()) {
            debug.message(classMethod+" authenticationHeader:[" + authenticationHeader + "]");
        }
        String headerStringWithoutAuthScheme = authenticationHeader.substring(authenticationHeader.indexOf(" ") + 1)
                .trim();
        // Obtain a Map of our Authentication Header.
        HashMap<String, String> headerValues = new HashMap<String, String>();
        String keyValueArray[] = headerStringWithoutAuthScheme.split(",");
        for (String keyval : keyValueArray) {
            if (keyval.contains("=")) {
                String key = keyval.substring(0, keyval.indexOf("="));
                String value = keyval.substring(keyval.indexOf("=") + 1);
                headerValues.put(key.trim(), value.replaceAll("\"", "").trim());
            }
        }

        // *****************************************
        // Obtain Each Value for Authentication
        // of the Digest.
        String method = request.getMethod();
        String credential = headerValues.get(XACML3Constants.USERNAME);
        if ( (credential == null)||(credential.isEmpty()) ) {
            debug.error(classMethod+"Unable to obtain the PEP's Credentials: No '"+XACML3Constants.USERNAME+"' Header Value " +
                    "Supplied by Client.");
            return null;
        }
        // ***************************************************
        // Here we need to perform a look up of the Credential
        // from the configured XACML Store to obtain the password
        // to push into the MD5 Algorithm via Digest Utilities.
        // TODO :
        // Obtain the password from the User Directory PIP.
        String ha1 = DigestUtils.md5Hex(credential + ":" + realm + ":" + "cangetin");   // TODO : FIX ME, LookUp.

        // Obtain values to compute.
        String qop = headerValues.get("qop");
        String ha2;
        String requestURI = headerValues.get("uri");
        // Only use AUTH Digest Method Details, never AUTH-INT
        ha2 = DigestUtils.md5Hex(method + ":" + requestURI);
        AuthenticationDigest authenticationDigest = new AuthenticationDigest(method, ha1, qop, ha2, requestURI, realm);
        // ******************************************
        // Now consume the Server Response.
        String serverResponse;
        if (StringUtils.isBlank(qop)) {
            serverResponse = DigestUtils.md5Hex(ha1 + ":" + nonce + ":" + ha2);
        } else {
            String nonceCount = headerValues.get("nc");
            String clientNonce = headerValues.get("cnonce");

            serverResponse = DigestUtils.md5Hex(ha1 + ":" + nonce + ":"
                    + nonceCount + ":" + clientNonce + ":" + qop + ":" + ha2);
        }
        // ******************************************************
        // Now Compare our Server Response with Client Response.
        String clientResponse = headerValues.get("response");
        if ( (clientResponse == null) || (clientResponse.isEmpty()) ) {
            clientResponse = headerValues.get("solution");
        }
        // ******************************************************
        // Show both calculated and received value from client.
        if (debug.messageEnabled()) {
            debug.error("*** Server Response: "+serverResponse+", *** Client Response: "+clientResponse);
        }
        // ******************************************************
        // Check for any Nulls on either side.
        if ( (clientResponse == null) || (clientResponse.isEmpty()) ||
                (serverResponse == null) || (serverResponse.isEmpty()) ||
                (!serverResponse.equals(clientResponse)) ) {
            return null;
        } else {
            // Authenticated Digest is Valid, Allow Access.
            return authenticationDigest;
        }
    }

    /**
     * Private helper method to be performed for each incoming Request.
     * This helper method simply performs some basic checks and validates
     * our content type.  If not valid content type, then preProcessing
     * method has set the appropriate HTTP Return Status in the response
     * and will return null.
     *
     * @param request
     * @param response
     * @return
     * @throws ServletException
     * @throws IOException
     */
    private ContentType preProcessingRequest(HttpServletRequest request, HttpServletResponse response) throws
            ServletException, IOException {
        final String classMethod = "XacmlContentHandlerService:preProcessingRequest";
        // ******************************************************************
        // Handle any DoS Attacks and Threats to the integrity of the PDP.
        if (maxContentLength != 0) {
            if ((!request.getMethod().equalsIgnoreCase("GET")) && (request.getContentLength() < 0)) {
                // We do not have any valid Content Length set.
                response.setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);  // 411
                response.setCharacterEncoding("UTF-8");
                return ContentType.NONE;
            }
            if (request.getContentLength() > maxContentLength) {
                if (debug.messageEnabled()) {
                    debug.message(
                            "Content length too large: " + request.getContentLength());
                }
                // We do not have any valid Content Length set.
                response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE); // 413
                response.setCharacterEncoding("UTF-8");
                return ContentType.NONE;
            }
        }
        // ******************************************************************
        // Validate Request Media Type
        ContentType requestContentType = ((request.getContentType() == null) ? null :
                ContentType.getNormalizedContentType(request.getContentType()));
        if (requestContentType == null) {
            // We do not have a valid Application Content Type!
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);  // 415
            response.setCharacterEncoding("UTF-8");
            response.setContentLength(0);
            return ContentType.NONE;
        }
        // ******************************************************************
        // Indicate preProcessing was completed with no Issues and Content
        // Type is valid with our derived ContentType.
        return requestContentType;
    }

    /**
     * Common Process Request Method.
     * Provides processing for both GET and POST methods.
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String classMethod = "XacmlContentHandlerService:processRequest";
        // ******************************************************************
        // Validate Request and Obtain Media Type
        ContentType requestContentType = this.preProcessingRequest(request, response);
        if ((requestContentType == null) || (requestContentType.equals(ContentType.NONE))) {
            // We do not have a valid Application Content Type or other issue.
            // Response Status set in preProcessingRequest method.
            response.setCharacterEncoding("UTF-8");
            response.setContentLength(0);
            return;
        }
        // ******************************************************************
        // Parse our Request...
        XACMLRequestInformation xacmlRequestInformation = this.parseRequestInformation(requestContentType, request);
        if (xacmlRequestInformation == null) {
            // This Starts the Authorization via Digest Flow...
            this.renderUnAuthorized(XACML3Constants.XACML3_PDP_DEFAULT_REALM, requestContentType, response);
            return;
        }
        // ******************************************************************
        // Check for any HTTP Digest Authorization Request or content.
        if ( (xacmlRequestInformation.getRequestMethod().equalsIgnoreCase("GET")) &&
                ((xacmlRequestInformation.getAuthenticationHeader() == null) ||
                 (xacmlRequestInformation.getAuthenticationHeader().isEmpty())) ) {
            // With No Authentication Header and a GET, attempt to Respond with our Home Document.
            if (this.processHomeRequest(xacmlRequestInformation, request, response)) {
                return;
            }
        } // End of outer if Check for GET and no authentication Information.

        // **************************************************************************************
        // Continue with Authentication and Authorization of the PEP.
        // **************************************************************************************

        // **************************************************************************************
        // Other Authentication and Authorization algorithm's such as SAML, OpenID and ...
        // TODO : Add code to pull in an AA Plugin or Connect to a Framework Component.
        // **************************************************************************************

        // ******************************************************************
        // Check for Authentication Digest.
        // Did we receive a valid WWW Authentication header using Digest?
        //
        if ((xacmlRequestInformation.getAuthenticationHeader() != null) &&
            (xacmlRequestInformation.getAuthenticationHeader().startsWith(XACML3Constants.DIGEST))) {
                AuthenticationDigest authenticationDigestResponse =
                    authenticateUsingDigest(xacmlRequestInformation.getAuthenticationHeader(),
                            xacmlRequestInformation.getOriginalContent(), request, xacmlRequestInformation.getRealm());
                // If we receive a valid authenticationDigestResponse Object, we have successfully Authenticated the
                // Client.
                if (authenticationDigestResponse == null) {
                    // Not Authenticated.
                    // This Starts the Authorization via Digest Flow...
                    this.renderUnAuthorized(XACML3Constants.XACML3_PDP_DEFAULT_REALM, requestContentType, response);
                    return;
                } else {
                    // Authentication is valid, set our POJO indicators, we had a valid digest and authenticated.
                    xacmlRequestInformation.setAuthenticated(true);
                } // End of Inner Else.
        // ******************************************************************
        // Check for any XACMLAuthzDecisionQuery Request?
        } else if (xacmlRequestInformation.getXacmlAuthzDecisionQuery().getId() != null) {

            debug.error("Processing XACMLAuthzDecisionQuery Wrapper Value: "+
                    xacmlRequestInformation.getXacmlAuthzDecisionQuery().toString() );


            // TODO : AME-302 Address processing a XACMLAuthzDecisionQuery Wrapper to
            // TODO : Authorize and Authenticate the PEP sending the request.



        }
        // **************************************************
        // Determine if the Authentication Wrapper provided
        // a Request which was already satisfied.
        if ((xacmlRequestInformation.isAuthenticated()) && (xacmlRequestInformation.isRequestProcessed())) {

             // TODO : AME-302
            // Check for other Authentication Patterns Here...


            // *****************************************************************
            // Render our Response
            renderResponse(requestContentType,
                    xacmlRequestInformation.getXacmlStringResponseBasedOnContent(requestContentType), response);
        } // End of Check for XACMLAuthzDecisionQuery Object and possible request Resolution for a [SAML4XACML] request.

        // ***********************************************************************
        // Check for a existence of a special auto-trusted End-Point, for a POST.
        // This allows any incoming request if using the end-point of
        // /openam/xacml/pdp/pep-trusted, will automatically trust the incoming
        // request.
        // TODO ::
        // This will be removed once PEP Trust and security Authentication and
        // Authorization has been decided upon.
        // Correct Request Content and if no Request Object in XML or JSON
        // form we render a Bad POST Request.
AUTO_TRUST_PEP:
        if ( (xacmlRequestInformation.getRequestMethod().equalsIgnoreCase("POST")) &&
             (xacmlRequestInformation.isRequestNodePresent()) &&
             (xacmlRequestInformation.getRequestURI().trim().
                        toLowerCase().contains("/openam/xacml/pdp/pep-trusted".toLowerCase())) ) {
            // Auto-Trust the PEP for the incoming request.
            xacmlRequestInformation.setAuthenticated(true);
        }

        // **********************************************************************
        // Only Continue if we have authenticated or Trust the PEP.
IS_AUTHENTICATED:
        if (!xacmlRequestInformation.isAuthenticated()) {
            // ******************************************************************
            // Not Authenticated nor Authorized.
            // This Starts the Authorization via Digest Flow...
            this.renderUnAuthorized(XACML3Constants.XACML3_PDP_DEFAULT_REALM, requestContentType, response);
            return;
        }

        // ******************************************************************************************************
        // PEP must be Authenticated and Authorized proceeding past this code point
        // ******************************************************************************************************
MUST_BE_AUTHENTICATED_AND_AUTHORIZED:

        // ******************************************************************
        // Check for a existence of a XACML Request, for a POST, we must have
        // Correct Request Content and if no Request Object in XML or JSON
        // form we render a Bad POST Request.
        if ( (xacmlRequestInformation.getRequestMethod().equalsIgnoreCase("POST")) &&
                (!xacmlRequestInformation.isRequestNodePresent()) ) {
            // No Request Node found within the document, bad request.
            this.renderBadRequest(requestContentType, response);
            return;
        }
        // ******************************************************************
        // Check for a Request with Multiple Requests,
        // As of this time we do not support this capability.  So to
        // conform to the OASIS specification, we must acknowledge this
        // request and return a Indeterminate Response immediately!
        if ( (xacmlRequestInformation.getRequestMethod().equalsIgnoreCase("POST")) &&
             (xacmlRequestInformation.isRequestWithMultiRequestsPresent()) ) {
            // Multiple nested Requests Node within the document, not Supported.
            // Render our Response, it will set a default Indeterminate Result Response.
            String responseContent = xacmlRequestInformation.getXacmlStringResponseBasedOnContent(requestContentType);
            renderServerOKResponse(xacmlRequestInformation.getContentType(), responseContent, response);
            return;
        }
        // ******************************************************************
        // Now this session has been Authenticated, using Auth-Digest or
        // other Authentication Algorithm, process the Authenticated Request...
        //
        // Check the Request Path Information.
        //

        // PDP Evaluation?
        if ( (xacmlRequestInformation.getRequestMethod().equalsIgnoreCase("POST")) &&
             (xacmlRequestInformation.getRequestURI().trim().
                     toLowerCase().contains("/openam/xacml/pdp/".toLowerCase())) ) {
            // ***********************************************************
            // Perform the PEP Request Evaluation:
            XacmlPDPResource xacmlPDPResource = new XacmlPDPResourceImpl();
            xacmlRequestInformation.setXacmlResponse(xacmlPDPResource.XACMLEvaluate(xacmlRequestInformation));

            // *****************************************************************
            // Render our Response, response Setting should be set prior, if not
            // it will set a default Indeterminate Result Response.
            String responseContent = xacmlRequestInformation.getXacmlStringResponseBasedOnContent(requestContentType);
            renderServerOKResponse(requestContentType, responseContent, response);
            return;

        // OpenAM Monitoring?
        } else if (xacmlRequestInformation.getRequestURI().trim().equalsIgnoreCase("/openam/xacml/ping")) {
                try{
                    // TODO : Perform ForgeRock Proprietary Ping Request, addition to specification.
                    renderServerOKResponse(xacmlRequestInformation.getContentType(),
                            XacmlPingResource.getPing(xacmlRequestInformation, request), response);
                    return;
                } catch (JSONException jsonException) {
                    // If any Exceptions, show exception for debugging.
                    debug.error(classMethod + " JSON Exception Occurred: " + jsonException.getMessage(), jsonException);
                    // No Valid Request URI Found, render Bad Request.
                    this.renderBadRequest(requestContentType, response);
                    return;
                }

        // XACML v3 Home Document / Discovery?
        } else if (this.processHomeRequest(xacmlRequestInformation, request, response)) {
            return;

        // Bad Request...
        } else {
            // No Valid Request URI Found, render Bad Request.
            this.renderBadRequest(requestContentType, response);
            return;
        }
    }

    /**
     * Process the Home Document Discover Request.
     * If this method returns, false, the request was invalid.
     *
     * @param xacmlRequestInformation
     * @param request
     * @param response
     * @return boolean - True indicates response returned.
     * @throws ServletException
     * @throws IOException
     */
    private boolean processHomeRequest(XACMLRequestInformation xacmlRequestInformation, HttpServletRequest request,
                                       HttpServletResponse response) throws ServletException, IOException {
        String classMethod = "XacmlContentHandlerService:processHomeRequest";
        // ***********************************************************************************************
        // Check the Request Path End-Point Information, provide the Home Document to the PEP Requester
        // by Rendering our Response.
        if ( ((xacmlRequestInformation.getRequestURI() == null) ||
                (xacmlRequestInformation.getRequestURI().isEmpty()) ||
                (xacmlRequestInformation.getRequestURI().trim().equalsIgnoreCase("/openam/xacml")) ||
                (xacmlRequestInformation.getRequestURI().trim().equalsIgnoreCase("/openam/xacml/home")) ||
                (xacmlRequestInformation.getRequestURI().trim().equalsIgnoreCase("/openam/xacml/status")) ) ||
             ((xacmlRequestInformation.getRequestMethod().equalsIgnoreCase("GET")) &&
                (xacmlRequestInformation.getRequestURI().trim().
                        toLowerCase().contains("/openam/xacml/pdp".toLowerCase())) ) ) {
            try {
                renderServerOKResponse(xacmlRequestInformation.getContentType(),
                        XacmlHomeResource.getHome(xacmlRequestInformation, request), response);
                return true;
            } catch (JSONException jsonException) {
                // If any Exceptions, Force Unauthorized and show exception for debugging.
                debug.error(classMethod + " JSON Exception Occurred: " + jsonException.getMessage(), jsonException);
            }
        }
        // Return, indicate URI Request from PEP was not satisfied by a Home Request.
        return false;
    }


    /**
     * Provide common Entry point Method for Parsing Initial Requests
     * to obtain information on how to process and route the request.
     *
     * @param request
     * @return XACMLRequestInformation - Object returned with Parsed Request Information.
     * @throws ServletException
     */
    private XACMLRequestInformation parseRequestInformation(ContentType contentType, HttpServletRequest request)
            throws ServletException {
        final String classMethod = "XacmlContentHandlerService:parseRequestInformation: ";

        // Get URI and MetaAlias Data.
        String requestURI = request.getRequestURI();
        String queryMetaAlias =
                XACML3Utils.getMetaAliasByUri(requestURI);
        String realm = XACML3Utils.getRealmByMetaAlias(queryMetaAlias);
        if ( (realm == null) || (realm.isEmpty()) ) {
            realm = XACML3Constants.XACML3_PDP_DEFAULT_REALM;
        }

        // Attempt to get the PDP Entity ID...
        String pdpEntityID = null;
        try {
            // Get PDP entity ID
            pdpEntityID =
                    XACML3Utils.getEntityByMetaAlias(queryMetaAlias);
        } catch (SAML2Exception xe) {
            debug.error(classMethod + " SAML2 Exception obtaining PDP Entity ID: " + xe.getMessage(), xe);
            // Absorb this issue and continue without our PDP Entity ID.
        }

        // Bootstrap our Request Information Object for this Request.
        XACMLRequestInformation xacmlRequestInformation = new XACMLRequestInformation(contentType,
                queryMetaAlias, pdpEntityID, realm, request);
        // Consume the Request Content, by parsing
        // the Content Depending upon the Content Type.
        if (contentType.commonType().equals(CommonType.XML)) {
            parseXMLRequest(xacmlRequestInformation);
        } else {
            // Only can be JSON at this point in Data FLow...
            parseJSONRequest(xacmlRequestInformation);
        }
        // **************************************
        // Show Parsed Content for debugging if
        // applicable.
        if (debug.messageEnabled()) {
            StringBuilder sb = XacmlPIPResourceBuilder.dumpContentInformation(xacmlRequestInformation);
            if (sb.length() > 0) {
                debug.error(classMethod+"Parsed Request Map====>\n"+sb.toString());
            }
        }
        // ****************************************
        // Build out our Xacml PIP Resource Object.
        XacmlPIPResourceBuilder.buildXacmlPIPResourceForRequests(xacmlRequestInformation);
        // **************************************
        // Return our Request Information for
        // processing request.
        return xacmlRequestInformation;
    }

    /**
     * Private Helper to Parse the XML Request Body.
     *
     * @param xacmlRequestInformation
     */
    private void parseXMLRequest(XACMLRequestInformation xacmlRequestInformation) {
        if ((xacmlRequestInformation.getOriginalContent() == null) ||
                (xacmlRequestInformation.getOriginalContent().isEmpty())) {
            return;
        }
        final String classMethod = "XacmlContentHandlerService:parseXMLRequest: ";

        try {
            // The Original Content will be UnMarshaled into a Map Object stored in XACMLRequestInformation Content.
            xacmlRequestInformation.setContent(XmlToMapUtility.fromString(xacmlRequestInformation.getOriginalContent()));
            xacmlRequestInformation.setParsedCorrectly(true);
        } catch (IOException ioe) {
            debug.error(classMethod+" Issue UnMarshaling Original Content to Common Map Object Exception: " + ioe
                    .getMessage() + "], content will be ignored!",ioe);
            xacmlRequestInformation.setContent(null);
            xacmlRequestInformation.setParsedCorrectly(false);
        }  catch (JSONException jsone) {
            debug.error(classMethod+" Issue UnMarshaling Original Content to Common Map Object Exception: " + jsone
                    .getMessage() + "], content will be ignored!",jsone);
            xacmlRequestInformation.setContent(null);
            xacmlRequestInformation.setParsedCorrectly(false);
        }
    }

    /**
     * Private Helper to Parse the JSON Request Body.
     *
     * @param xacmlRequestInformation
     */
    private void parseJSONRequest(XACMLRequestInformation xacmlRequestInformation) {
        if ((xacmlRequestInformation.getOriginalContent() == null) ||
                (xacmlRequestInformation.getOriginalContent().isEmpty())) {
            return;
        }
        final String classMethod = "XacmlContentHandlerService:parseJSONRequest: ";
        try {
            // The Original Content will be UnMarshaled into a Map Object stored in XACMLRequestInformation Content.
            xacmlRequestInformation.setContent(JsonToMapUtility.fromString(xacmlRequestInformation.getOriginalContent()));
            xacmlRequestInformation.setParsedCorrectly(true);
        } catch (IOException ioe) {
            debug.error(classMethod+"parseJSONRequest Exception: " + ioe.getMessage() + "], content will be ignored!");
            xacmlRequestInformation.setContent(null);
            xacmlRequestInformation.setParsedCorrectly(false);
        }
    }

    // ******************************************************************************************************
    // Common Rendering Response Methods
    // ******************************************************************************************************

    /**
     * Private Helper Method to Render Response Content.
     *
     * @param contentType
     * @param xacmlStringResponse
     * @param response
     */
    private void renderResponse(final ContentType contentType, final String xacmlStringResponse,
                                HttpServletResponse response) {
        OutputStream outputStream = null;
        try {
            response.setContentType(contentType.applicationType());
            response.setCharacterEncoding("UTF-8");
            if ((xacmlStringResponse != null) && (!xacmlStringResponse.trim().isEmpty())) {
                outputStream = response.getOutputStream();
                response.setContentLength(xacmlStringResponse.length());
                outputStream.write(xacmlStringResponse.getBytes());
            } else {
                response.setContentLength(0);
            }
        } catch (IOException ioe) {
            // Debug
        } finally {
            try {
            if (outputStream != null) {
                outputStream.close();
            }
            } catch(IOException ioe) {
                // Do Nothing...
            }

        }
    }

    /**
     * Simple Helper Method to provide common Not Authorized render Method.
     *
     * @param requestContentType
     * @param response
     */
    private void renderUnAuthorized(final String realm, final ContentType requestContentType,
                                    HttpServletResponse response) {
        response.addHeader(XACML3Constants.WWW_AUTHENTICATE_HEADER, this.generateAuthenticateHeader(realm));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
        renderResponse(requestContentType, null, response);
    }

    /**
     * Simple Helper Method to provide common Bad Request render Method.
     *
     * @param requestContentType
     * @param response
     */
    private void renderBadRequest(final ContentType requestContentType, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);  // 400
        renderResponse(requestContentType, null, response);
    }

    /**
     * Simple Helper Method to provide common OK for Server PDP Status.
     *
     * @param requestContentType
     * @param responseContent
     * @param response
     */
    private void renderServerOKResponse(final ContentType requestContentType,
                                        String responseContent, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);  // 200
        renderResponse(requestContentType, responseContent, response);
    }

    /**
     * Simple Helper Method to provide common Not Implemented render Method.
     *
     * @param response
     */
    private void renderNotImplemented(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);  // 501
        response.setContentLength(0);
    }

    // ******************************************************************************************************
    // Methods for Access Logging
    // ******************************************************************************************************

    /**
     * Private Helper to Obtain the Logger for our AM XACML Log File.
     * @return
     */
    private Logger getLogger() {
        if (logger == null) {
            logger = (Logger) Logger.getLogger(amXACMLLogFile);
        }
        return logger;
    }

    /**
     * Obtain a Log Message Provider.
     *
     * @return LogMessageProvider
     * @throws Exception
     */
    private LogMessageProvider getLogMessageProvider()
            throws Exception {

        if (logProvider == null) {
            logProvider =
                    MessageProviderFactory.getProvider(LOG_PROVIDER);
        }
        return logProvider;
    }

    /**
     * Private Helper method to Log Explicit Operations being performed.
     *
     * @param sess
     * @param id
     */
    private void logIt(InternalSession sess, String id) {
        if (!logStatus) {
            return;
        }
        try {
            String sidString = sess.getID().toString();
            String clientID = sess.getClientID();
            String uidData = null;
            if ((clientID == null) || (clientID.length() < 1)) {
                uidData = "N/A";
            } else {
                StringTokenizer st = new StringTokenizer(clientID, ",");
                uidData = (st.hasMoreTokens()) ? st.nextToken() : clientID;
            }
            String[] data = {uidData};
            LogRecord lr =
                    getLogMessageProvider().createLogRecord(id, data, null);

            lr.addLogInfo(LogConstants.LOGIN_ID_SID, sidString);

            String amCtxID = sess.getProperty(Constants.AM_CTX_ID);
            String clientDomain = sess.getClientDomain();
            String ipAddress = sess.getProperty("Host");
            String hostName = sess.getProperty("HostName");

            lr.addLogInfo(LogConstants.CONTEXT_ID, amCtxID);
            lr.addLogInfo(LogConstants.LOGIN_ID, clientID);
            lr.addLogInfo(LogConstants.LOG_LEVEL, lr.getLevel().toString());
            lr.addLogInfo(LogConstants.DOMAIN, clientDomain);
            lr.addLogInfo(LogConstants.IP_ADDR, ipAddress);
            lr.addLogInfo(LogConstants.HOST_NAME, hostName);
            getLogger().log(lr, getSessionServiceToken());
        } catch (Exception ex) {
            debug.error("XacmlContentHandlerService.logIt(): " +
                    "Cannot write to the session log file: ", ex);
        }
    }

    /**
     * Private Helper Method to Log System Messages
     * @param msgID
     * @param level
     */
    private void logSystemMessage(String msgID, Level level) {

        if (!logStatus) {
            return;
        }
        if (errorLogger == null) {
            errorLogger =
                    (Logger) Logger.getLogger(amXACMLErrorLogFile);
        }
        try {
            String[] data = {msgID};
            LogRecord lr =
                    getLogMessageProvider().createLogRecord(msgID,
                            data,
                            null);
            SSOToken serviceToken = getSessionServiceToken();
            lr.addLogInfo(LogConstants.LOGIN_ID_SID,
                    serviceToken.getTokenID().toString());
            lr.addLogInfo(LogConstants.LOGIN_ID,
                    serviceToken.getPrincipal().getName());
            errorLogger.log(lr, serviceToken);
        } catch (Exception ex) {
            debug.error("SessionService.logSystemMessage(): " +
                    "Cannot write to the session error " +
                    "log file: ", ex);
        }
    }

    // ******************************************************************************************************
    // Methods for Security Session Token and associated Managers.
    // ******************************************************************************************************

    /**
     * Obtain our SSO Token Manager
     *
     * @return SSOTokenManager
     * @throws SSOException
     */
    private SSOTokenManager getSSOTokenManager() throws SSOException {
        if (ssoManager == null) {
            ssoManager = SSOTokenManager.getInstance();
        }
        return ssoManager;
    }

    /**
     * Obtain a Session Service Token
     *
     * @return SSOToken
     * @throws Exception
     */
    private SSOToken getSessionServiceToken() throws Exception {
        return ((SSOToken) AccessController.doPrivileged(
                AdminTokenAction.getInstance()));
    }

    /**
     * Obtain a Internal Admin SSO Token
     * @return SSOToken - Admin Token.
     * @throws SSOException
     */
    private SSOToken getAdminToken() throws SSOException {
        if (adminToken == null) {

            adminToken = getSSOTokenManager().createSSOToken(
                    new AuthPrincipal(dsameAdminDN), dsameAdminPassword);
            return adminToken;
        }

        return adminToken;
    }

}
