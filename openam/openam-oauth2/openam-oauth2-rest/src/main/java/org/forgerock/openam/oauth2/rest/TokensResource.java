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
import org.forgerock.openam.oauth2.OAuth2;
import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.restlet.data.Status;

import java.util.HashMap;
import java.util.Map;

public class TokensResource implements CollectionResourceProvider {

    private JsonResource repository;

    private static final String REFRESH_TOKEN = "(type=refresh_token)";

    public TokensResource() {
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
    public void actionInstance(Context context, String resourceId, ActionRequest request,
                               ResultHandler<JsonValue> handler){
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
    public void deleteInstance(Context context, String resourceId, DeleteRequest request,
                               ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Delete is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void patchInstance(Context context, String resourceId, PatchRequest request,
                              ResultHandler<Resource> handler){
        System.out.println("TEST");
        final ResourceException e =
                new NotSupportedException("Patch is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void queryCollection(Context context, QueryRequest queryRequest, QueryResultHandler handler){
        try{
            JsonValue response;
            Resource resource;
            JsonResourceAccessor accessor =
                    new JsonResourceAccessor(repository, JsonResourceContext.newRootContext());
            try {
                response = accessor.query("1", null);
            } catch (JsonResourceException e) {
                throw ResourceException.getException(ResourceException.UNAVAILABLE, "Can't query CTS", null, e);
            }
            resource = new Resource(OAuth2.Params.ID, "1", response);
            handler.handleResource(resource);
        } catch (ResourceException e){
            handler.handleError(e);
        }
    }

    @Override
    public void readInstance(Context context, String resourceId, ReadRequest request,
                             ResultHandler<Resource> handler){
        try{
            JsonValue response;
            Resource resource;
            JsonResourceAccessor accessor =
                    new JsonResourceAccessor(repository, JsonResourceContext.newRootContext());
            try {
                Map query = new HashMap<String,String>();
                if (request.getResourceName() == null){
                    query.put(OAuth2.StoredToken.TYPE, "*");
                } else if (request.getResourceName().equalsIgnoreCase(OAuth2.Params.REFRESH_TOKEN)){
                    query.put(OAuth2.StoredToken.TYPE, OAuth2.Params.REFRESH_TOKEN);
                } else if (request.getResourceName().equalsIgnoreCase(OAuth2.Params.ACCESS_TOKEN)){
                    query.put(OAuth2.StoredToken.TYPE, OAuth2.Params.ACCESS_TOKEN);
                } else if (request.getResourceName().equalsIgnoreCase(OAuth2.Params.CODE)) {
                    query.put(OAuth2.StoredToken.TYPE, OAuth2.Params.CODE);
                } else {
                    query.put(OAuth2.StoredToken.TYPE, "*");
                }
                JsonValue queryj = new JsonValue(new HashMap<String, HashMap<String, String>>());
                queryj.put("filter", query);
                response = accessor.query("1", queryj);
            } catch (JsonResourceException e) {
                throw ResourceException.getException(ResourceException.UNAVAILABLE, "Can't query CTS", null, e);
            }
            resource = new Resource(OAuth2.Params.ID, "1", response);
            handler.handleResult(resource);
        } catch (ResourceException e){
            handler.handleError(e);
        }
    }

    @Override
    public void updateInstance(Context context, String resourceId, UpdateRequest request,
                               ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Update is not supported for resource instances");
        handler.handleError(e);
    }


}
