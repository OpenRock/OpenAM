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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.util.Loader;

/**
 * Uses resource factory service providers to create resource instances.
 *
 * @author Paul C. Bryan
 */
 public class Resources {
 
     /** Loads the resource factories. */
    private static final List<ResourceFactory> FACTORIES =
     Collections.unmodifiableList(Loader.loadList(ResourceFactory.class));

    /** Exposes static methods only. Cannot be constructed. */
    private Resources() {
    }

    /**
     * Creates and returns an object that provides access to the resource specified by the URI.
     *
     * @param uri the URI to create a resource access object for.
     * @return a resource object to access the URI.
     * @throws ResourceException if no matching resource class is available, or there is an exception creating the object.
     */
    public static Resource newInstance(URI uri) throws ResourceException {
        for (ResourceFactory factory : FACTORIES) {
            Resource resource = factory.newInstance(uri);
            if (resource != null) {
                return resource;
            }
        }
        throw new ResourceException("no resource object available to handle " + uri);
    }

    /**
     * Creates and returns an object that provides access to the resource specified by the URI.
     *
     * @param uri the URI to create a resource access object for.
     * @return a resource object to access the URI.
     * @throws ResourceException if the URI is malformed, no matching resource class is available, or there is an exception creating the object.
     */
    public static Resource newInstance(String uri) throws ResourceException {
        try {
            return newInstance(new URI(uri));
        }
        catch (URISyntaxException use) {
            throw new ResourceException(use);
        }
    }
}
