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

import org.forgerock.json.resource.NotSupportedException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.CollectionResourceProvider;
import org.forgerock.openam.ext.cts.CoreTokenService;
import org.forgerock.openam.ext.cts.repo.OpenDJTokenRepo;
import org.forgerock.openam.oauth2.OAuth2Constants;
import org.forgerock.openam.oauth2.exceptions.OAuthProblemException;
import org.restlet.data.Status;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public void actionCollection(ServerContext context, ActionRequest actionRequest, ResultHandler<JsonValue> handler){
        final ResourceException e =
                new NotSupportedException("Actions are not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void actionInstance(ServerContext context, String resourceId, ActionRequest request,
                               ResultHandler<JsonValue> handler){
        final ResourceException e =
                new NotSupportedException("Actions are not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void createInstance(ServerContext context, CreateRequest createRequest, ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Create is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void deleteInstance(ServerContext context, String resourceId, DeleteRequest request,
                               ResultHandler<Resource> handler){
        try{
            JsonValue query = new JsonValue(null);
            JsonValue response = null;
            Resource resource = null;
            JsonResourceAccessor accessor =
                    new JsonResourceAccessor(repository, JsonResourceContext.newRootContext());
            try {
                response = accessor.delete(resourceId, "1");
            } catch (JsonResourceException e) {
                throw ResourceException.getException(ResourceException.UNAVAILABLE, "Can't delete token in CTS", null, e);
            }
            resource = new Resource(request.getResourceName(), "1", response);
            handler.handleResult(resource);
        } catch (ResourceException e){
            handler.handleError(e);
        }
    }

    @Override
    public void patchInstance(ServerContext context, String resourceId, PatchRequest request,
                              ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Patch is not supported for resource instances");
        handler.handleError(e);
    }

    @Override
    public void queryCollection(ServerContext context, QueryRequest queryRequest, QueryResultHandler handler){
        try{
            JsonValue response;
            Resource resource;
            JsonResourceAccessor accessor =
                    new JsonResourceAccessor(repository, JsonResourceContext.newRootContext());
            try {
                Map query = new HashMap<String,String>();
                String id = queryRequest.getQueryId();
                if (id.equalsIgnoreCase(OAuth2Constants.Params.REFRESH_TOKEN)){
                    query.put(OAuth2Constants.StoredToken.TYPE, id);
                } else if (id.equalsIgnoreCase(OAuth2Constants.Params.ACCESS_TOKEN)){
                    query.put(OAuth2Constants.StoredToken.TYPE, id);
                } else if (id.equalsIgnoreCase(OAuth2Constants.Params.CODE)){
                    query.put(OAuth2Constants.StoredToken.TYPE, id);
                } else {
                    query = null;
                }
                JsonValue queryFilter = new JsonValue(new HashMap<String, HashMap<String, String>>());
                if (query != null){
                    queryFilter.put("filter", query);
                }
                response = accessor.query("1", queryFilter);
            } catch (JsonResourceException e) {
                throw ResourceException.getException(ResourceException.UNAVAILABLE, "Can't query CTS", null, e);
            }
            resource = new Resource("result", "1", response);
            JsonValue value = resource.getContent();
            Set<HashMap<String,Set<String>>> list = (Set<HashMap<String,Set<String>>>) value.getObject();
            Resource res = null;
            JsonValue val = null;
            for (HashMap<String,Set<String>> entry : list){
                val = new JsonValue(entry);
                res = new Resource("result", "1", val);
                handler.handleResource(res);
            }
            handler.handleResult(new QueryResult());
        } catch (ResourceException e){
            handler.handleError(e);
        }
    }

    @Override
    public void readInstance(ServerContext context, String resourceId, ReadRequest request,
                             ResultHandler<Resource> handler){
        try{
            JsonValue response;
            Resource resource;
            JsonResourceAccessor accessor =
                    new JsonResourceAccessor(repository, JsonResourceContext.newRootContext());
            try {
                response = accessor.read(resourceId);
            } catch (JsonResourceException e) {
                throw ResourceException.getException(ResourceException.NOT_FOUND, "Not found in CTS", "CTS", e);
            }
            resource = new Resource(OAuth2Constants.Params.ID, "1", response);
            handler.handleResult(resource);
        } catch (ResourceException e){
            handler.handleError(e);
        }
    }

    @Override
    public void updateInstance(ServerContext context, String resourceId, UpdateRequest request,
                               ResultHandler<Resource> handler){
        final ResourceException e =
                new NotSupportedException("Update is not supported for resource instances");
        handler.handleError(e);
    }


}
