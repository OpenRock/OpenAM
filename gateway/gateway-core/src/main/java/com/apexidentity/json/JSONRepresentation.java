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

package com.apexidentity.json;

// Java Standard Edition
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

// JSON.simple
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

// ApexIdentity Core Library
import com.apexidentity.model.ValueNode;
import com.apexidentity.resource.Representation;
import com.apexidentity.resource.ResourceException;

/**
 * Represents a JSON tree structure as a JSON-encoded stream. See
 * <a href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a> for more information.
 *
 * @author Paul C. Bryan
 */
public class JSONRepresentation implements Representation {

    /** Filename extension for JSON content. */
    public static final String EXTENSION = "json";

    /** MIME type for JSON content. */
    public static final String CONTENT_TYPE = "application/json";

    /** The character set that the JSON representation is expected in. */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /** Java object to be be represented in JSON notation. */
    public Object object;

    /**
     * Constructs a new empty JSON representation.
     */
    public JSONRepresentation() {
    }

    /**
     * Constructs a new JSON representation initialized with the specified object to be
     * represented in JSON notation.
     *
     * @param object the Java object to be represented in JSON notation.
     */
    public JSONRepresentation(Object object) {
        this.object = object;
    }

    /**
     * Returns filename extension for JSON content.
     */
    @Override
    public String getExtension() {
        return EXTENSION;
    }

    /**
     * Returns the MIME type for JSON content.
     */
    @Override
    public String getContentType() {
        return CONTENT_TYPE;
    }

    /**
     * Returns {@code -1}, as length is unknown until encoding occurs.
     */
    @Override
    public int getLength() {
        return -1;
    }

    /**
     * Accepts any type; always assumes {@code application/json} encoded in UTF-8.
     */
    @Override
    public void setContentType(String type) throws ResourceException {
        // ignore
    }

    /**
     * Reads JSON-encoded data from the input stream into an object model value.
     */
    @Override
    public void read(InputStream in) throws IOException, ResourceException {
        JSONParser parser = new JSONParser();
        InputStreamReader reader = new InputStreamReader(in, CHARSET);
        try {
            object = parser.parse(reader);
        }
        catch (ParseException pe) {
            throw new ResourceException(pe);
        }
    }

    /**
     * Writes the object model value as JSON-encoded data to the output stream.
     */ 
    @Override
    public void write(OutputStream out) throws IOException, ResourceException {
        JSONValue.writeJSONString(object, new OutputStreamWriter(out, CHARSET));
    }
}
