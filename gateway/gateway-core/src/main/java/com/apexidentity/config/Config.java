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

package com.apexidentity.config;

// Java Enterprise Edition
import javax.servlet.ServletContext;

// ApexIdentity Core Library
import com.apexidentity.json.JSONRepresentation;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.resource.Resource;
import com.apexidentity.resource.ResourceException;

/**
 * Reads and writes configuration data.
 *
 * @author Paul C. Bryan
 */
public class Config {

    /** The resource containing the configuration content. */
    private final Resource resource;

    /**
     * Constructs a new configuration object.
     *
     * @param resource the resource containing the configuration data.
     */
    public Config(Resource resource) {
        this.resource = resource;
    }

    /**
     * Reads a configuration object from the configuration resource.
     *
     * @return the configuration object read from from a JSON object resource.
     * @throws ModelException if either the bootstrap or configuration JSON resource is malformed.
     * @throws ResourceException if the configuration could not be read.
     */
    public MapNode read() throws ModelException, ResourceException {
        JSONRepresentation representation = new JSONRepresentation();
        resource.read(representation);
        return new MapNode(representation.object, "['" + resource.getURI().toString() + "']"); // configuration files are JSON objects
    }

    /**
     * Writes a configuration object to the configuration resource.
     *
     * @param config the configuration object to write as a JSON object resource.
     * @throws ResourceException if the configuration could not be written.
     */
    public void write(MapNode config) throws ResourceException {
        JSONRepresentation representation = new JSONRepresentation(config.object);
        try {
            resource.update(representation);
        }
        catch (ResourceException re) { // assume update failure is due to non-existence
            resource.create(representation); // attempt to create instead
        }
    }
}
