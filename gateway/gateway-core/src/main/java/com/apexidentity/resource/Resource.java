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

/**
 * Exposes methods to access an addressable resource. The universal methods for all resources
 * are: create, read, update and delete (a.k.a. CRUD). Concrete resource implementations are
 * free to support additional methods as applicable.
 *
 * @author Paul C. Bryan
 */
public interface Resource {

    /**
     * Creates the resource with the specified representation. If the resource already
     * exists, this method will indicate failure by throwing a {@code ResourceException}.
     *
     * @throws ResourceException if the resource could not be created.
     */
    void create(Representation representation) throws ResourceException;

    /**
     * Reads the resource into the specified representation.
     *
     * @param representation the representation to read the resource into.
     * @throws ResourceException if the resource could not be read into the representation.
     */
    void read(Representation representation) throws ResourceException;

    /**
     * Updates the resource with the specified representation.
     *
     * @param representation the representation to be written to the resource.
     * @throws ResourceException if the representation could not be written to the resource.
     */
    void update(Representation representation) throws ResourceException;

    /**
     * Deletes the resource. If the resource does not exist, this method will indicate
     * failure by throwing a {@code ResourceException}.
     *
     * @throws ResourceException if the resource could not be deleted.
     */
    void delete() throws ResourceException;

    /**
     * Returns the URI that the resource addresses.
     *
     * @throws ResourceException if an exception occurred establishing the URI of the resource.
     */
    URI getURI() throws ResourceException;
}
