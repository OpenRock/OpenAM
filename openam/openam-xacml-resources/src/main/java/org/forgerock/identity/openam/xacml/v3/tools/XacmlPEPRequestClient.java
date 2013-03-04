/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 ForgeRock US, Inc. All Rights Reserved
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
package org.forgerock.identity.openam.xacml.v3.tools;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.*;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import org.forgerock.identity.openam.xacml.v3.commons.ContentType;
import org.forgerock.identity.openam.xacml.v3.commons.XACML3Utils;
import org.forgerock.identity.openam.xacml.v3.model.XACML3Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Xacml PEP Client Request
 * <p/>
 * Provides a Command Line/Shell Tool to perform XACML PEP Client Requests
 * to our specified OpenAM PDP end point.
 *
 * @author jeff.schenk@forgerock.com
 * @since 10.2.0
 */
public class XacmlPEPRequestClient {
    private static final String OUR_VERSION = "ForgeRock Incorporated -- XacmlPEPRequestClient Tool Version 10.2.0, " +
            "2013.";

    private String url = null;
    private String method = "POST";
    private String principal = null;
    private String credential = null;
    private ContentType contentType = ContentType.XML;
    private String requestFileName = null;

    /**
     * Default Constructor.
     */
    public XacmlPEPRequestClient() {
    }

    /**
     * Constructor with all Parameters specified.
     *
     * @param url
     * @param method
     * @param principal
     * @param credential
     * @param contentType
     * @param requestFileName
     */
    public XacmlPEPRequestClient(String url, String method, String principal, String credential, ContentType contentType, String requestFileName) {
        this.url = url;
        this.method = method;
        this.principal = principal;
        this.credential = credential;
        this.contentType = contentType;
        this.requestFileName = requestFileName;
    }

    /**
     * Constructor with all Parameters specified, except for the Request Content File Name, which will
     * assume a zero byte content length when sending the request.
     *
     * @param url
     * @param method
     * @param principal
     * @param credential
     * @param contentType
     */
    public XacmlPEPRequestClient(String url, String method, String principal, String credential, ContentType contentType) {
        this.url = url;
        this.method = method;
        this.principal = principal;
        this.credential = credential;
        this.contentType = contentType;
        this.requestFileName = null;
    }

    /**
     * Method to perform the XACML Request to the specified OpenAM Server URL.
     *
     * @return String - XACML Response.
     */
    public String performRequest() throws Exception {
        String response = null;
        if (this.method.equalsIgnoreCase("GET")) {
            response = this.getMethod();
        } else if (this.method.equalsIgnoreCase("POST")) {
            response = this.postMethod();
        } else {
            throw new IllegalArgumentException("Specified Method is not GET or POST, please re-specify Method!");
        }
        // Return our Response.
        return response;
    }

    /**
     * Standard Getter's and Setter's...
     */

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getRequestFileName() {
        return requestFileName;
    }

    public void setRequestFileName(String requestFileName) {
        this.requestFileName = requestFileName;
    }

    /**
     * Perform a GET Method to our PDP.
     *
     * @throws IOException
     */
    private String getMethod() throws IllegalAccessException, IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        DefaultHttpClient httpclient2 = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(this.getUrl());

        // Set Headers
        httpGet.setHeader("content-type", this.getContentType().applicationType());
        System.out.println("GET Requesting: " + httpGet.getURI());

        try {
            // Initial request without credentials returns "HTTP/1.1 401 Unauthorized, if using Digest Auth.."
            HttpResponse response = httpclient.execute(httpGet);
            if ( (this.getCredential() == null) ||
                    (this.getCredential().equalsIgnoreCase(XACML3Constants.ANONYMOUS)) ) {
                System.out.println("GET Request Response: " + response.getStatusLine());
                return getFinalResponseContent(response);
            }
            System.out.println("Initial GET Request Response: " + response.getStatusLine());

            // Initially Return Status should be a 401.
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {

                // Obtain current "WWW-Authenticate" header from PDP response
                // WWW-Authenticate:Digest realm="OpenAM_XACML_PDP_Realm", qop="auth",
                //   nonce="cdcf6cbe6ee17ae0790ed399935997e8", opaque="ae40d7c8ca6a35af15460d352be5e71c"
                Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
                System.out.println("Received Authentication Header: " + authHeader);

                // Parse realm, nonce sent by server.
                DigestScheme digestScheme = new DigestScheme();
                digestScheme.processChallenge(authHeader);
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(this.getPrincipal(), this.getCredential());
                httpGet.addHeader(digestScheme.authenticate(creds, httpGet));

                // Obtain Response from Negotiated Authentication/Authorization.
                response = httpclient2.execute(httpGet);
                // Process Final Response.
                return getFinalResponseContent(response);
            }
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return getFinalResponseContent(response);
            } else {
                throw new IllegalAccessException("Unable to Access OpenAM XACML PDP to Send GET Request, we should " +
                        "have received an initial " + HttpStatus.SC_UNAUTHORIZED + " or " + HttpStatus.SC_OK + ", " +
                        " depending upon specified URI, however we received a " +
                        response.getStatusLine() + ", this is a incorrect Data Flow, Server side is suspect!");
            }
        } catch (MalformedChallengeException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
            httpclient2.getConnectionManager().shutdown();
        }
        return null;
    }


    /**
     * Perform a POST Method to our PDP.
     *
     * @throws IOException
     */
    private String postMethod() throws IllegalAccessException, IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        DefaultHttpClient httpclient2 = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(this.getUrl());
        // Set Headers
        httpPost.setHeader("content-type", this.getContentType().applicationType());
        System.out.println("POST Requesting : " + httpPost.getURI());

        try {
            // Initial request without credentials returns "HTTP/1.1 401 Unauthorized, if using Digest Auth.."
            HttpResponse response = httpclient.execute(httpPost);
            if ( (this.getCredential() == null) ||
                 (this.getCredential().equalsIgnoreCase(XACML3Constants.ANONYMOUS)) ) {
                System.out.println("POST Request Response: " + response.getStatusLine());
                return getFinalResponseContent(response);
            }
            System.out.println("Initial POST Request Response: " + response.getStatusLine());

            // Initially Return Status should be a 401.
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {

                // Obtain current "WWW-Authenticate" header from PDP response
                // WWW-Authenticate:Digest realm="My Test Realm", qop="auth",
                //   nonce="cdcf6cbe6ee17ae0790ed399935997e8", opaque="ae40d7c8ca6a35af15460d352be5e71c"
                Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
                System.out.println("Received Authentication Header: " + authHeader);

                // Parse realm, nonce sent by server.
                DigestScheme digestScheme = new DigestScheme();
                digestScheme.processChallenge(authHeader);
                UsernamePasswordCredentials creds = new UsernamePasswordCredentials(this.getPrincipal(), this.getCredential());
                httpPost.addHeader(digestScheme.authenticate(creds, httpPost));

                // Obtain Content from a File, if applicable, and stuff into Request.
                if ((this.getRequestFileName() != null) && (!this.getRequestFileName().isEmpty())) {
                    String content = getFileContents(this.getRequestFileName());
                    if (content != null) {
                        HttpEntity httpEntity = new ByteArrayEntity(content.getBytes(Charset.forName("UTF-8")));
                        // Associate our Content Entity to our POST Method Request.
                        httpPost.setEntity(httpEntity);
                    }
                }
                // Obtain Response from Negotiated Authentication/Authorization.
                response = httpclient2.execute(httpPost);
                // Process Final Response.
                return getFinalResponseContent(response);
            } else {
                throw new IllegalAccessException("Unable to Access OpenAM XACML PDP to Send POST Request, we should " +
                        "have received an initial " + HttpStatus.SC_UNAUTHORIZED + ", however we received a " +
                        response.getStatusLine() + ", this is a incorrect Data Flow, Server side is suspect!");
            }
        } catch (MalformedChallengeException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
            httpclient2.getConnectionManager().shutdown();
        }
        return null;
    }

    /**
     * Perform a PUT Method to our PDP.
     *
     * @throws IOException
     */
    private String putMethod() throws IllegalAccessException, IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(this.getUrl());
        // Set Headers
        httpPut.setHeader("content-type", this.getContentType().applicationType());
        System.out.println("PUT Requesting : " + httpPut.getURI());

        try {
            // Initial request without credentials returns "HTTP/1.1 401 Unauthorized"
            HttpResponse response = httpclient.execute(httpPut);
            System.out.println("Initial PUT Request Response: " + response.getStatusLine());

            // Until OpenAM Implements, we will received a 501, No Implementation.
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_IMPLEMENTED) {
                System.out.println("Correctly Received a Not Implemented Yet Response: "+response
                        .getStatusLine());
            } else {
                System.out.println("Received a Response we have not Implemented yet!: "+response.getStatusLine());
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return null;
    }

    /**
     * Perform a DELETE Method to our PDP.
     *
     * @throws IOException
     */
    private String deleteMethod() throws IllegalAccessException, IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(this.getUrl());
        // Set Headers
        httpDelete.setHeader("content-type", this.getContentType().applicationType());
        System.out.println("PUT Requesting : " + httpDelete.getURI());

        try {
            // Initial request without credentials returns "HTTP/1.1 401 Unauthorized"
            HttpResponse response = httpclient.execute(httpDelete);
            System.out.println("Initial DELETE Request Response: " + response.getStatusLine());

            // Until OpenAM Implements, we will received a 501, No Implementation.
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_IMPLEMENTED) {
                System.out.println("Correctly Received a Not Implemented Yet Response: "+response
                        .getStatusLine());
            } else {
                System.out.println("Received a Response we have not Implemented yet!: "+response.getStatusLine());
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return null;
    }


    /**
     * Private Helper Method to obtain the Response Content.
     *
     * @param response
     * @return
     * @throws IOException
     */
    private String getFinalResponseContent(HttpResponse response) throws IOException {
        System.out.println("Final Request Response: " + response.getStatusLine());
        if ((response.getEntity().getContentLength() > 0) &&
                (response.getEntity().getContent() != null)) {
            // Show our response Content.
            String content = getResponseBody(response);
            System.out.println("XACML Response Content: " + content);
            return content;
        } else {
            // Show our response Content.
            System.out.println("No Response Data Available.");
            return "";
        }
    }

    /**
     * Helper Method to perform Command Line Arguments.
     */
    private int parseCommandLineArguments(String[] args) {
        int validationErrors = 0;
        int argumentIndex = args.length;
        // Spin through our Arguments, building up our required variables...
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }

            // Need a Java 7 String Switch!

            if ((argumentIndex >= 2) && (args[i].equalsIgnoreCase("--url"))) {
                argumentIndex = argumentIndex - 2;
                try {
                    URL parsedURL = new URL(args[i + 1]);
                    if (parsedURL != null) {
                        this.url = parsedURL.toExternalForm();
                    }
                } catch (MalformedURLException mue) {
                    System.err.println("Invalid URL Specified!");
                    validationErrors++;
                }
            } else if ((argumentIndex >= 2) && (args[i].equalsIgnoreCase("--method"))) {
                argumentIndex = argumentIndex - 2;
                if ((args[i + 1].equalsIgnoreCase("GET")) ||
                        (args[i + 1].equalsIgnoreCase("POST")) ||
                        (args[i + 1].equalsIgnoreCase("PUT")) ||
                        (args[i + 1].equalsIgnoreCase("DELETE"))) {
                    this.method = args[i + 1];
                } else {
                    System.err.println("Invalid Method Specified!");
                    validationErrors++;
                }


            } else if ((argumentIndex >= 2) && (args[i].equalsIgnoreCase("--principal"))) {
                argumentIndex = argumentIndex - 2;
                this.principal = args[i + 1];

            } else if ((argumentIndex >= 2) && (args[i].equalsIgnoreCase("--credential"))) {
                argumentIndex = argumentIndex - 2;
                this.credential = args[i + 1];

            } else if ((argumentIndex >= 2) && (args[i].equalsIgnoreCase("--contenttype"))) {
                argumentIndex = argumentIndex - 2;
                if (args[i + 1].equalsIgnoreCase("xml")) {
                    this.contentType = ContentType.XML;
                } else if (args[i + 1].equalsIgnoreCase("json")) {
                    this.contentType = ContentType.JSON;
                } else {
                    System.err.println("Invalid Content Type!");
                    validationErrors++;
                }

            } else if ((argumentIndex >= 2) && (args[i].equalsIgnoreCase("--requestfile"))) {
                argumentIndex = argumentIndex - 2;
                File requestFile = new File(args[i+1]);
                if ( (requestFile.exists() && (requestFile.canRead()) ) ) {
                    this.requestFileName = requestFile.getAbsolutePath();
                } else {
                    System.err.println("Request FileName does not Exist or Unreadable!");
                    validationErrors++;
                }
            }
        }
        // Return with the number of validation errors.
        return validationErrors;
    }

    /**
     * Main -- Invoked using Command Line Tools.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        // Construct our Utility Tool class.
        XacmlPEPRequestClient xacmlPEPRequestClient = new XacmlPEPRequestClient();
        // Initialize and set the Defaults...
        System.out.println(OUR_VERSION + "\n\n");
        System.out.flush();
        // Determine if we have any arguments or not....
        if ((args == null) || (args.length <= 0)) {
            System.out.println("No arguments specified!");
            usage();
            System.exit(0);
        }
        // Parse the Command Line Argument.
        if (xacmlPEPRequestClient.parseCommandLineArguments(args) > 0) {
            System.out.println("Validation Errors Exist based upon specified Command Line Arguments.");
            usage();
            System.exit(0);
        }
        // Perform the Appropriate Operation.
        try {
            xacmlPEPRequestClient.performRequest();
            System.out.println("\nDone.");
        } catch (Exception e) {
            System.err.println("Exception occurred performing Request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Simple Usage Method.
     */
    private static void usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Required Arguments:\n");
        sb.append(" --url <specify full URL of OpenAM PDP>\n");
        sb.append(" --method <specify GET or POST>\n");
        sb.append(" --principal <specify the username access PDP>\n");
        sb.append(" --credential <specify the password to access PDP>\n");
        sb.append(" --contenttype <specify valid content type of request:" + ContentType.JSON.toString() + " or " +
                ContentType.XML.toString() + " >\n");
        sb.append(" [--requestfile] <specify optional File path of Request Source, in JSON or XML format>\n");

        sb.append("\nExample: \n\n");
        sb.append("XacmlPEPRequestClient \\ \n");
        sb.append(" --url http://localhost:18080/openam/xacml/pdp/authorize \\ \n");
        sb.append(" --method POST \\ \n");
        sb.append(" --principal amadmin \\\n");
        sb.append(" --credential cangetin \\\n");
        sb.append(" --contenttype " + ContentType.XML.toString() + " \\ \n");
        sb.append(" --requestfile /Users/jaschenk/MyWorkspaces/OPENAM/branches/openam_10.2.0_xacml3_JAS/openam/openam-xacml-resources/src/test/resources/test_data/request-curtiss.xml\n");
        // Show Usage...
        System.out.println(sb.toString());
    }

    /**
     * Return the Response Body Content.
     *
     * @param response
     * @return String - Request Content Body.
     */
    private static String getResponseBody(HttpResponse response) {
        // Get the body content of the HTTP Response,
        // remember we have no normal WS* SOAP Body, just String
        // data either XML or JSON.
        InputStream inputStream = null;
        try {
            inputStream = response.getEntity().getContent();
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException ioe) {
            // Do Nothing...
        } catch (NoSuchElementException nse) {   // runtime exception.
            //Do Nothing...
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // Do nothing...
                }
            }
        }
        return null;
    }

    /**
     * Simple Helper Method to read File in as a Stream
     *
     * @param resourceFileName -- File Name of Contents to be consumed as a String.
     * @return String containing the Resource Contents or null if issue.
     */
    public static String getFileContents(final String resourceFileName) {
        InputStream inputStream = null;
        try {
            if ( (resourceFileName == null) || (resourceFileName.isEmpty()) ) {
                return null;
            }
            File file = new File(resourceFileName);
            if ( (file.exists()) && (file.canRead()) )
            {
                inputStream = new FileInputStream(resourceFileName);
                return (inputStream != null) ? new Scanner(inputStream).useDelimiter("\\A").next() : null;
            }
        } catch (Exception e) {
            // Do Nothing...
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // Do nothing...
                }
            }
        }
        // Catch All.
        return null;
    }


}
