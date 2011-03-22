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

// TODO: more permanent way to expose "exchange.request.form" parameters.

package com.apexidentity.http;

// Java Standard Edition
import java.io.IOException;
import java.util.List;

// ApexIdentity Core Library
import com.apexidentity.io.BranchingInputStream;
import com.apexidentity.util.ReadableMap;

/**
 * Exposes query parameters and posted form entity as values.
 *
 * @author Paul C. Bryan
 */
public class FormAttributes implements ReadableMap<String, List<String>> {

    /** The request to read form attributes from. */
    private Request request;

    /**
     * Constructs a new form attributes object that reads attributes from the specified
     * request.
     *
     * @param request the request to read form attributes from.
     */
    public FormAttributes(Request request) {
        this.request = request;
    }

    /**
     * Returns a list of form values for the specified field name.
     *
     * @param name the field name to return the form value(s) for.
     * @return a list of form values for the specified field name.
     */
    @Override
    public List<String> get(Object name) {
        BranchingInputStream entity = null;
        if (request.entity != null) {
            entity = request.entity;
            try {
                request.entity = entity.branch();
            }
            catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        }
        try {
            Form form = new Form();
            form.fromRequestQuery(request);
            form.fromRequestEntity(request);
            return form.get(name);
        }
        catch (IOException ioe) {
            return null;
        }
        finally {
            if (entity != null) {
                try {
                    entity.closeBranches();
                }
                catch (IOException ioe) {
                    throw new IllegalStateException(ioe);
                }
            }
            request.entity = entity;
        }
    }
}
