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

// Java Standard Edition
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

// Java Enterprise Edition
import javax.servlet.ServletContext;

// ApexIdentity Core Library
import com.apexidentity.json.JSONRepresentation;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.resource.FileResource;
import com.apexidentity.resource.Representation;
import com.apexidentity.resource.Resource;
import com.apexidentity.resource.ResourceException;
import com.apexidentity.resource.Resources;

/**
 * A resource for accessing application configuration data.
 * <p>
 * There are two modes the configuration resource resolve: simple and bootstrap. Simple mode
 * is meant for single local deployments of a web application in a user account; bootstrap
 * mode supports remotely configured or multiple deployments of an application for a given
 * user account.
 * <p>
 * In both cases, all file resources are located a directory that is either
 * <tt><strong>$AppData/</strong><em>vendor</em><strong>/</strong><em>product</em><strong>/</strong></tt>
 * if the <strong>{@code $AppData}</strong> environment variable is defined (typical in
 * Windows installations), or otherwise
 * <tt><em>user-home</em><strong>/.</strong><em>vendor</em><strong>/</strong><em>product</em><strong>/</strong></tt>
 * (typical in Unix installations).
 * <p>
 * This class first tries to locate the configuration file using simple mode by looking for
 * a file named <strong>{@code config.json}</strong> in the configuration directory specified
 * above. If the file does not exist, then this class reverts to boostrap mode.
 * <p>
 * In bootstrap mode, the name of a bootstrap configuration resource is generated based on
 * the instance name supplied (or dervied from the servlet context) and takes the form
 * <tt><em>instance</em><strong>.json</strong></tt>. This file is expected to contain a single
 * JSON object with a single value with the name <strong>{@code config-uri}</strong>. The
 * value is the URI of the configuration resource.
 *
 * @author Paul C. Bryan
 */
public class ConfigResource implements Resource {

    /** The underlying resource that this object represents. */
    private final Resource resource;

    /**
     * Constructs a new configuration resource, with a path based-on the specified vendor,
     * product and servlet context.
     *
     * @param vendor the vendor name.
     * @param product the product name.
     * @param context the servlet context from which the product instance name can be derived.
     * @throws ResourceException if the configuration (or bootstrap) resource could not be found.
     */
    public ConfigResource(String vendor, String product, ServletContext context) throws ResourceException {
        this(vendor, product, context.getRealPath("/"));
    }

    /**
     * Constructs a new configuration resource, with a path based-on the specified vendor,
     * product and instance name.
     *
     * @param vendor the vendor name.
     * @param product the product name.
     * @param instance the product instance name.
     * @throws ResourceException if the configuration (or bootstrap) resource could not be found.
     */
    public ConfigResource(String vendor, String product, String instance) throws ResourceException {
        File config = ConfigUtil.getFile(vendor, product, "config");
        if (config.exists()) { // simplistic config.json file
            this.resource = new FileResource(config);
        }
        else { // bootstrap location of instance-based configuration file
            File boot = ConfigUtil.getFile(vendor, product, instance != null ? instance : "bootstrap");
            if (!boot.exists()) {
                throw new ResourceException("could not find local configuration file at " +
                 config.getPath() + " or bootstrap file at " + boot.getPath());
            }
            FileResource bootResource = new FileResource(boot);
            JSONRepresentation representation = new JSONRepresentation();
            bootResource.read(representation);
            try {
                this.resource = Resources.newInstance(new MapNode(representation.object,
                 "['" + bootResource.getURI().toString() + "']").get("configURI").required().asURI());
            }
            catch (ModelException me) {
                throw new ResourceException(me);
            }
        }
    }

    @Override
    public void create(Representation representation) throws ResourceException {
        resource.create(representation);
    }

    @Override
    public void read(Representation representation) throws ResourceException {
        resource.read(representation);
    }

    @Override
    public void update(Representation representation) throws ResourceException {
        resource.update(representation);
    }

    @Override
    public void delete() throws ResourceException {
        resource.delete();
    }

    @Override
    public URI getURI() throws ResourceException {
        return resource.getURI();
    }
}
