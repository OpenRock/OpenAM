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
 * Creates objects that provide access to resources specified by uniform resource identifiers.
 *
 * @author Paul C. Bryan
 */
public interface ResourceFactory {

    /**
     * Creates and returns an object that provides access to the resource specified by the
     * URI, or {@code null} if the factory is not applicable for the URI.
     *
     * @param uri the URI to create a resource access object for.
     * @return a resource access object, or {@code null} if the URI is not applicable to the factory instance.
     * @throws ResourceException if there is an exception creating a resource access object.
     */
    Resource newInstance(URI uri) throws ResourceException;
}
