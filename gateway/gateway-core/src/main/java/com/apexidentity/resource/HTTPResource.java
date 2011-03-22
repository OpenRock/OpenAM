/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2010–2011 ApexIdentity Inc. All rights reserved.
 */

package com.apexidentity.resource;

// Java Standard Edition
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Accesses resources via the HTTP(S) protocol.
 *
 * @author Paul C. Bryan
 */
public class HTTPResource implements Resource {

    /** Uniform resource locator for the resource to be accessed. */
    public final URL url;

    /**
     * Constructs a new HTTP(S) resource object to access the specified uniform resource locator.
     *
     * @param url the URL of the HTTP(S) resource to access.
     */
    public HTTPResource(URL url) {
        this.url = url;
    }

    /**
     * Opens and returns an HTTP connection to the URL.
     *
     * @returns the newly opened HTTP connection.
     * @throws ResourceException if the URL is malformed.
     */
    private HttpURLConnection openConnection() throws ResourceException {
        try {
            return (HttpURLConnection)url.openConnection();
        }
        catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
        catch (ClassCastException cce) {
            throw new ResourceException("resource does not appear to use the HTTP(s) protocol");
        }
    }

    @Override
    public void create(Representation representation) throws ResourceException {
// TODO: check for existence and WebDAV create-directory-goodness
        update(representation);
    }

    @Override
    public void read(Representation representation) throws ResourceException {
        HttpURLConnection connection = openConnection(); 
        try {
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            InputStream in = connection.getInputStream();
            try {
                representation.setContentType(connection.getContentType());
                representation.read(in);
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            }
        }
        catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
        finally {
            connection.disconnect();
        }
    }

    @Override
    public void update(Representation representation) throws ResourceException {
        HttpURLConnection connection = openConnection();
        try {
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", representation.getContentType());
            int length = representation.getLength();
            if (length != -1) {
                connection.setFixedLengthStreamingMode(length);
            }
            else {
                connection.setChunkedStreamingMode(-1);
            }
            OutputStream out = connection.getOutputStream();
            try {
                representation.write(out);
                out.flush();
            }
            finally {
                try {
                    out.close();
                }
                catch (IOException ioe) {
                }
            }
        }
        catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
        finally {
            connection.disconnect();
        }
    }

    @Override
    public void delete() throws ResourceException {
        HttpURLConnection connection = openConnection(); 
        try {
            connection.setRequestMethod("DELETE");
            connection.connect();
        }
        catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
        finally {
            connection.disconnect();
        }
    }

    @Override
    public URI getURI() throws ResourceException {
        try {
            return url.toURI();
        }
        catch (URISyntaxException use) {
            throw new ResourceException(use); // shouldn't happen
        }
    }

    /**
     * Creates resource objects that access HTTP resources. Supports URIs with a
     * {@code "http://"} or {@code "https://"} scheme.
     */
    public static class Factory implements ResourceFactory {
        @Override
        public Resource newInstance(URI uri) throws ResourceException {
            String scheme = uri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                try {
                    return new HTTPResource(uri.toURL());
                }
                catch (MalformedURLException mue) {
                    throw new ResourceException(mue); // shouldn't happen
                }
            }
            return null; // scheme not supported
        }
    }
}
