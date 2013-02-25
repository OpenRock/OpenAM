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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.forgerock.identity.openam.xacml.v3.commons.ContentType;
import sun.security.pkcs11.wrapper.CK_VERSION;

import java.io.IOException;

/**
 * Xacml PEP Client Request
 * <p/>
 * Provides a way to perform Xacml PEP Client Requests to our specified OpenAM PDP end point.
 *
 * @author jeff.schenk@forgerock.com
 */
public class XacmlPEPRequestClient {
    private static final String OUR_VERSION = "ForgeRock Incorporated -- XacmlPEPRequestClient Tool Version 10.2.0, " +
            "2013.";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        // Initialize and set the Defaults...
        System.out.println(OUR_VERSION+"\n\n");
        System.out.flush();

        String url = null;
        String method = "POST";
        String principal = null;
        String credential = null;
        ContentType contentType = ContentType.XML;
        String requestFileName = null;

        // Determine if we have any arguments or not....
        if ((args == null) || (args.length <= 0)) {
            System.out.println("No arguments specified!");
            usage();
            System.exit(0);
        }

        // Spin through our Arguments, building up our required variables...
        int argumentIndex = args.length;
        for(int i = 0; i<args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            // Need a Java 7 String Switch!

            if ( (argumentIndex >= 2) && (args[i].equalsIgnoreCase("--url")) ) {
                argumentIndex = argumentIndex - 2;
                url = args[i+1];
            } else if ( (argumentIndex >= 2) && (args[i].equalsIgnoreCase("--method")) ) {
                argumentIndex = argumentIndex - 2;
                method = args[i+1];
            } else if ( (argumentIndex >= 2) && (args[i].equalsIgnoreCase("--principal")) ) {
                argumentIndex = argumentIndex - 2;
                principal = args[i+1];
            } else if ( (argumentIndex >= 2) && (args[i].equalsIgnoreCase("--credential")) ) {
                argumentIndex = argumentIndex - 2;
                credential = args[i+1];
            } else if ( (argumentIndex >= 2) && (args[i].equalsIgnoreCase("--contenttype")) ) {
                argumentIndex = argumentIndex - 2;
                if (args[i+1].equalsIgnoreCase("xml")) {
                    contentType = ContentType.XML;
                } else if (args[i+1].equalsIgnoreCase("json")) {
                    contentType = ContentType.JSON;
                }
            } else if ( (argumentIndex >= 2) && (args[i].equalsIgnoreCase("--requestfile")) ) {
                argumentIndex = argumentIndex - 2;
                requestFileName = args[i+1];
            } else {
               // Invalid Argument...
               continue;
            }

        } // End of Argument For Each Loop.

        // Verify and Show what we will be using....


        try {
            postMethod();
        } catch (IOException e) {
            System.out.println("POST Exception: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            getJSONMethod();
        } catch (IOException e) {
            System.out.println("POST Exception: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            getMethod();
        } catch (Exception e) {
            System.out.println("GET Exception: " + e.getMessage());
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
        sb.append(" --requestfile <specify File path of Request Source, in JSON or XML format>\n");

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
     * Perform a POST Method to our PDP.
     *
     * @throws IOException
     */
    public static void postMethod() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        DefaultHttpClient httpclient2 = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://localhost:18080/openam/xacml/pdp/authorize");
        httpPost.setHeader("content-type", "application/xml");
        System.out.println("Requesting : " + httpPost.getURI());

        try {
            //Initial request without credentials returns "HTTP/1.1 401 Unauthorized"
            HttpResponse response = httpclient.execute(httpPost);
            System.out.println(response.getStatusLine());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {

                //Get current current "WWW-Authenticate" header from response
                // WWW-Authenticate:Digest realm="My Test Realm", qop="auth",
                //nonce="cdcf6cbe6ee17ae0790ed399935997e8", opaque="ae40d7c8ca6a35af15460d352be5e71c"
                Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
                System.out.println("authHeader = " + authHeader);

                DigestScheme digestScheme = new DigestScheme();

                //Parse realm, nonce sent by server.
                digestScheme.processChallenge(authHeader);

                UsernamePasswordCredentials creds = new UsernamePasswordCredentials("username", "password");
                httpPost.addHeader(digestScheme.authenticate(creds, httpPost));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                String responseBody = httpclient2.execute(httpPost, responseHandler);
                System.out.println("responseBody : " + responseBody);
            }

        } catch (MalformedChallengeException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
            httpclient2.getConnectionManager().shutdown();
        }

    }

    /**
     * Perform a GET Method to our PDP.
     *
     * @throws IOException
     */
    public static void getMethod() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        DefaultHttpClient httpclient2 = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:18080/openam/xacml/pdp/authorize");
        httpGet.setHeader("content-type", "application/xml");
        System.out.println("Requesting : " + httpGet.getURI());

        try {
            //Initial request without credentials returns "HTTP/1.1 401 Unauthorized"
            HttpResponse response = httpclient.execute(httpGet);
            System.out.println(response.getStatusLine());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {

                //Get current current "WWW-Authenticate" header from response
                // WWW-Authenticate:Digest realm="OpenAM_XACML_PDP_Realm", qop="auth",
                //nonce="cdcf6cbe6ee17ae0790ed399935997e8", opaque="ae40d7c8ca6a35af15460d352be5e71c"
                Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
                System.out.println("authHeader = " + authHeader);

                DigestScheme digestScheme = new DigestScheme();

                //Parse realm, nonce sent by server.
                digestScheme.processChallenge(authHeader);

                UsernamePasswordCredentials creds = new UsernamePasswordCredentials("username", "password");
                httpGet.addHeader(digestScheme.authenticate(creds, httpGet));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                String responseBody = httpclient2.execute(httpGet, responseHandler);
                System.out.println("responseBody : " + responseBody);


            }

        } catch (MalformedChallengeException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
            httpclient2.getConnectionManager().shutdown();
        }

    }

    public static void getJSONMethod() throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        DefaultHttpClient httpclient2 = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://localhost:18080/openam/xacml/pdp/authorize");
        httpGet.setHeader("content-type", "application/json");
        System.out.println("Requesting : " + httpGet.getURI());

        try {
            //Initial request without credentials returns "HTTP/1.1 401 Unauthorized"
            HttpResponse response = httpclient.execute(httpGet);
            System.out.println(response.getStatusLine());

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {

                //Get current current "WWW-Authenticate" header from response
                // WWW-Authenticate:Digest realm="OpenAM_XACML_PDP_Realm", qop="auth",
                //nonce="cdcf6cbe6ee17ae0790ed399935997e8", opaque="ae40d7c8ca6a35af15460d352be5e71c"
                Header authHeader = response.getFirstHeader(AUTH.WWW_AUTH);
                System.out.println("authHeader = " + authHeader);

                DigestScheme digestScheme = new DigestScheme();

                //Parse realm, nonce sent by server.
                digestScheme.processChallenge(authHeader);

                UsernamePasswordCredentials creds = new UsernamePasswordCredentials("username", "password");
                httpGet.addHeader(digestScheme.authenticate(creds, httpGet));

                ResponseHandler<String> responseHandler = new BasicResponseHandler();

                String responseBody = httpclient2.execute(httpGet, responseHandler);
                System.out.println("responseBody : " + responseBody);
            }

        } catch (MalformedChallengeException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
            httpclient2.getConnectionManager().shutdown();
        }

    }


}
