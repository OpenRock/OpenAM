/*
 * Copyright (c) 2012 ForgeRock AS. All rights reserved.
 *
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
 * information: "Portions Copyrighted [2012] [ForgeRock Inc]".
 *
 */
package org.forgerock.openam.oauth2.rest;

import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.*;
import org.forgerock.json.resource.exception.NotSupportedException;
import org.forgerock.json.resource.exception.ResourceException;
import org.forgerock.json.resource.provider.CollectionResourceProvider;
import org.forgerock.openam.ext.cts.CoreTokenService;
import org.forgerock.openam.ext.cts.repo.OpenDJTokenRepo;
import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.restlet.data.Status;


public class TokenResource implements CollectionResourceProvider {

    private JsonResource repository;

    public TokenResource() {
        try {
            repository = new CoreTokenService(new OpenDJTokenRepo());
        } catch (Exception e) {
            throw new OAuthProblemException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode(),
                    "Service unavailable", "Could not create underlying storage", null);
        }
    }

    @Override
    public void actionCollection(Context context, ActionRequest actionRequest, ResultHandler<JsonValue> handler){
        final ResourceException e =
                new NotSupportedException("Actions are not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void actionInstance(Context context, ActionRequest actionRequest, ResultHandler<JsonValue> handler){
        final ResourceException e =
                new NotSupportedException("Actions are not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void createInstance(Context context, CreateRequest createRequest, ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Create is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void deleteInstance(Context context, DeleteRequest deleteRequest, ResultHandler<Resource> handler){
        try{
            JsonValue query = new JsonValue(null);
            JsonValue response = null;
            Resource resource = null;
            JsonResourceAccessor accessor =
                    new JsonResourceAccessor(repository, JsonResourceContext.newRootContext());
            try {
                //TODO what is the significance of revision
                response = accessor.delete(deleteRequest.getResourceId(), "1");
            } catch (JsonResourceException e) {
                throw ResourceException.getException(ResourceException.UNAVAILABLE, "Can't delete token in CTS", null, e);
            }
            resource = new Resource(deleteRequest.getResourceId(), "1", response);
            handler.handleResult(resource);
        } catch (ResourceException e){
            handler.handleError(e);
        }
    }

    @Override
    public void patchInstance(Context context, PatchRequest patchRequest, ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Path is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void queryCollection(Context context, QueryRequest queryRequest, QueryResultHandler handler){
        final ResourceException e =
                new NotSupportedException("Query is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void readInstance(Context context, ReadRequest readRequest, ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Read is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void updateInstance(Context context, UpdateRequest updateRequest, ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Update is not supported for resource instances");
        handler.handleError(e);
    }

}
