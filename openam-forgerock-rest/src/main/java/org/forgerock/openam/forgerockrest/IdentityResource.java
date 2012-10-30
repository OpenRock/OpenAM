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
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2012 ForgeRock AS.
 */
package org.forgerock.openam.forgerockrest;

import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.lang.System;
import java.util.*;


import com.iplanet.am.util.SystemProperties;
import com.sun.identity.idsvcs.*;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.fluent.JsonValueException;
import org.forgerock.json.resource.ActionRequest;
import org.forgerock.json.resource.ServerContext;
import org.forgerock.json.resource.CreateRequest;
import org.forgerock.json.resource.DeleteRequest;
import org.forgerock.json.resource.PatchRequest;
import org.forgerock.json.resource.QueryRequest;
import org.forgerock.json.resource.QueryResult;
import org.forgerock.json.resource.QueryResultHandler;
import org.forgerock.json.resource.ReadRequest;
import org.forgerock.json.resource.Resource;
import org.forgerock.json.resource.ResultHandler;
import org.forgerock.json.resource.UpdateRequest;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.InternalServerErrorException;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.NotSupportedException;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.CollectionResourceProvider;

import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOTokenManager;

import java.security.AccessController;

import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.idsvcs.opensso.IdentityServicesImpl;

import org.forgerock.json.resource.servlet.HttpContext;

/**
 * A simple {@code Map} based collection resource provider.
 */
public final class IdentityResource implements CollectionResourceProvider {
    // TODO: filters, sorting, paged results.


    private final List<Attribute> idSvcsAttrList = new ArrayList();
    private String realm = null;
    private String userType = null;

    /**
     * Creates a backend
     */
    public IdentityResource(String userType, String realm) {
        String[] userval = {userType};
        String[] realmval = {realm};
        this.realm = realm;
        this.userType = userType;
        idSvcsAttrList.add(new Attribute("objecttype", userval));
        idSvcsAttrList.add(new Attribute("realm", realmval));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionCollection(final ServerContext context, final ActionRequest request,
                                 final ResultHandler<JsonValue> handler) {
        final ResourceException e = new NotSupportedException("Patch operations are not supported");
        handler.handleError(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionInstance(final ServerContext context, final String resourceId, final ActionRequest request,
                               final ResultHandler<JsonValue> handler) {
        final ResourceException e =
                new NotSupportedException("Actions are not supported for resource instances");
        handler.handleError(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createInstance(final ServerContext context, final CreateRequest request,
                               final ResultHandler<Resource> handler) {
        //anyone can create an account add
        Token admin = new Token();
        admin.setId(getCookieFromServerContext(context));

        final JsonValue jVal = request.getContent();

        try {
            IdentityServicesImpl idsvc = new IdentityServicesImpl();
            IdentityDetails identity = jsonValueToIdentityDetails(jVal);
            try {
                CreateResponse success = idsvc.create(identity, admin);
            } catch (Exception e) {
                throw new NotFoundException("Cannot create idenity" + identity.getName());
            }
            IdentityDetails dtls = idsvc.read(identity.getName(), idSvcsAttrList, admin);
            Resource resource = new Resource("0", "0", identityDetailsToJsonValue(dtls));
            handler.handleResult(resource);
        } catch (final ResourceException e) {
            handler.handleError(e);
        } catch (final Exception e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteInstance(final ServerContext context, final String resourceId, final DeleteRequest request,
                               final ResultHandler<Resource> handler) {
        Token admin = new Token();
        admin.setId(getCookieFromServerContext(context));

        JsonValue result = null;
        Resource resource = null;
        IdentityDetails dtls = null;
        IdentityServicesImpl idsvc = null;

        try {
            result = new JsonValue(new LinkedHashMap<String, Object>(1));
            idsvc = new IdentityServicesImpl();

            //read to see if resource is available to user
            dtls = idsvc.read(resourceId, idSvcsAttrList, admin);

            //delete the resource
            DeleteResponse success = idsvc.delete(dtls, admin);

            result.put("success", "true");
            resource = new Resource("0", "0", result);
            handler.handleResult(resource);

        } catch (final NeedMoreCredentials ex) {
            RestDispatcher.debug.error("IdentityResource :: Cannot DELETE " + resourceId +
                    ": User does not have enough privileges.");
            handler.handleError(new NotFoundException(resourceId, ex));
        } catch (final ObjectNotFound notFound) {
            RestDispatcher.debug.error("IdentityResource :: Cannot DELETE " + resourceId +
                    ": Resource cannot be found.");
            handler.handleError(new NotFoundException(resourceId, notFound));
        } catch (final TokenExpired tokenExpired) {
            RestDispatcher.debug.error("IdentityResource :: Cannot DELETE " + resourceId +
                    ": Token is not valid.");
            handler.handleError(new NotFoundException(resourceId, tokenExpired));
        } catch (final GeneralFailure generalFailure) {
            RestDispatcher.debug.error("IdentityResource :: Cannot DELETE " + generalFailure.getMessage());
            handler.handleError(new NotFoundException(resourceId, generalFailure));
        } catch (final Exception exception) {
            RestDispatcher.debug.error("IdentityResource :: Cannot DELETE! " + exception.getMessage());
            result.put("success", "false");
            resource = new Resource("0", "0", result);
            handler.handleResult(resource);
        }
    }

    /**
     * Returns TokenID from headers
     *
     * @param context ServerContext which contains the headers.
     * @return String with TokenID
     */
    private String getCookieFromServerContext(ServerContext context) {
        List<String> cookies = null;
        String cookieName = null;
        HttpContext header = null;
        try {
            cookieName = SystemProperties.get("com.iplanet.am.cookie.name");
            if (cookieName == null || cookieName.isEmpty()) {
                RestDispatcher.debug.error("Cannot retrieve SystemProperty :: com.iplanet.am.cookie.name");
                return null;
            }
            header = context.asContext(HttpContext.class);
            if (header == null) {
                RestDispatcher.debug.error("Cannot retrieve ServerContext as HttpContext");
                return null;
            }
            cookies = header.getHeaders().get("cookie");//Get parameter cookie from the headers
            if (cookies != null || !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    String cookieNames[] = cookie.split(";"); //Split parameter up
                    for (String c : cookieNames) {
                        if (c.contains(cookieName)) { //if com.iplanet.am.cookie.name exists in cookie param
                            String amCookie = c.replace(cookieName + "=", "").trim();
                            return amCookie; //return com.iplanet.am.cookie.name value
                        }
                    }
                }
            }
        } catch (Exception e) {
            RestDispatcher.debug.error("Cannot get cookie from ServerContext!" + e.getMessage());
        }
        return null;
    }

    /**
     * Returns a JsonValue containing appropriate identity details
     *
     * @param details The IdentityDetails of a Resource
     * @return The JsonValue Object
     */
    private JsonValue identityDetailsToJsonValue(IdentityDetails details) {
        JsonValue result = new JsonValue(new LinkedHashMap<String, Object>(1));
        try {
            result.put("name", details.getName());
            result.put("realm", details.getRealm());
            Attribute[] attrs = details.getAttributes();

            for (Attribute aix : attrs) {
                result.put(aix.getName(), aix.getValues());
            }
            return result;
        } catch (final Exception e) {
            throw new JsonValueException(result);
        }
    }

    /**
     * Returns an IdenityDetails from a JsonValue
     *
     * @param jVal The JsonValue Object to be converted
     * @return The IdentityDetails object
     */
    private IdentityDetails jsonValueToIdentityDetails(JsonValue jVal) {

        IdentityDetails identity = new IdentityDetails();
        List<Attribute> identityAttrList = new ArrayList();
        identityAttrList.addAll(idSvcsAttrList);

        try {
            identity.setType(userType); //set type ex. user
            identity.setRealm(realm); //set realm
            identity.setName(jVal.get("name").asString());//set name from JsonValue object

            try {
                for (String s : jVal.keys()) {
                    JsonValue childValue = jVal.get(s);
                    if (childValue.isString()) {
                        String[] tArray = {childValue.asString()};
                        identityAttrList.add(new Attribute(s, tArray));
                    } else if (childValue.isList()) {
                        ArrayList<String> tList = (ArrayList<String>) childValue.getObject();
                        String[] tArray = tList.toArray(new String[tList.size()]);
                        identityAttrList.add(new Attribute(s, tArray));
                    }
                }
            } catch (Exception e) {
                RestDispatcher.debug.error("jsonValueToIdenityDetails() : Cannot Traverse JsonValue");
            }
            Attribute[] attr = identityAttrList.toArray(new Attribute[identityAttrList.size()]);
            identity.setAttributes(attr);

        } catch (final Exception e) {
            RestDispatcher.debug.error("jsonValueToIdenityDetails() : Cannot convert JsonValue to IdentityDetials.");
            //deal with better exceptions
        }
        return identity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void patchInstance(final ServerContext context, final String resourceId, final PatchRequest request,
                              final ResultHandler<Resource> handler) {
        final ResourceException e = new NotSupportedException("Patch operations are not supported");
        handler.handleError(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queryCollection(final ServerContext context, final QueryRequest request,
                                final QueryResultHandler handler) {

        SSOToken adminToken = (SSOToken) AccessController.doPrivileged(
                AdminTokenAction.getInstance());
        Token ret = new Token();
        ret.setId(adminToken.getTokenID().toString());
        try {

            IdentityServicesImpl id = new IdentityServicesImpl();
            List<String> users = id.search("*", idSvcsAttrList, ret);

            for (final String user : users) {
                JsonValue val = new JsonValue(user);
                Resource resource = new Resource("0", "0", val);
                handler.handleResource(resource);
            }
        } catch (Exception ex) {

        }

        handler.handleResult(new QueryResult());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readInstance(final ServerContext context, final String resourceId, final ReadRequest request,
                             final ResultHandler<Resource> handler) {

        Token admin = new Token();
        admin.setId(getCookieFromServerContext(context));

        IdentityServicesImpl idsvc = null;
        IdentityDetails dtls = null;
        try {
            idsvc = new IdentityServicesImpl();
            dtls = idsvc.read(resourceId, idSvcsAttrList, admin);
            Resource resource = new Resource("0", "0", identityDetailsToJsonValue(dtls));
            handler.handleResult(resource);
        } catch (final ObjectNotFound o) {
            //
        } catch (final Exception ex) {
            //
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateInstance(final ServerContext context, final String resourceId, final UpdateRequest request,
                               final ResultHandler<Resource> handler) {

        Token admin = new Token();
        admin.setId(getCookieFromServerContext(context));

        final JsonValue jVal = request.getNewContent();
        final String rev = request.getRevision();


        IdentityDetails dtls = null, newDtls = null;
        IdentityServicesImpl idsvc = null;
        try {
            idsvc = new IdentityServicesImpl();
            dtls = idsvc.read(resourceId, idSvcsAttrList, admin);//Retrieve details about user to be updated
            //Continue modifying the identity
            newDtls = jsonValueToIdentityDetails(jVal);
            newDtls.setName(resourceId);
            UpdateResponse message = idsvc.update(newDtls, admin);
            IdentityDetails checkIdent = idsvc.read(dtls.getName(), idSvcsAttrList, admin);
            Resource resource = new Resource("0", "0", identityDetailsToJsonValue(checkIdent));
            handler.handleResult(resource);
        } catch (final ObjectNotFound o) {
            //create object because it does not exist
            try {
                dtls = jsonValueToIdentityDetails(jVal);
                dtls.setName(resourceId);
                try {
                    CreateResponse success = idsvc.create(dtls, admin);
                } catch (Exception e) {
                    throw new NotFoundException("Cannot create identity " + dtls.getName());
                }
                IdentityDetails checkIdent = idsvc.read(dtls.getName(), idSvcsAttrList, admin);
                Resource resource = new Resource("0", "0", identityDetailsToJsonValue(checkIdent));
                handler.handleResult(resource);
            } catch (final ResourceException e) {
                handler.handleError(e);
            } catch (final NeedMoreCredentials ex) {
                //do something useful
            } catch (final ObjectNotFound notFound) {
                //do something useful
            } catch (final Exception exception) {
                //do something useful
            }
        } catch (final Exception e) {
            //
        }
    }

    /*
     * Add the ID and revision to the JSON content so that they are included
     * with subsequent responses. We shouldn't really update the passed in
     * content in case it is shared by other components, but we'll do it here
     * anyway for simplicity.
     */
    private void addIdAndRevision(final Resource resource) throws ResourceException {
        final JsonValue content = resource.getContent();
        try {
            content.asMap().put("_id", resource.getResourceName());
            content.asMap().put("_rev", resource.getRevision());
        } catch (final JsonValueException e) {
            throw new BadRequestException(
                    "The request could not be processed because the provided "
                            + "content is not a JSON object");
        }
    }

    private String getNextRevision(final String rev) throws ResourceException {
        try {
            return String.valueOf(Integer.parseInt(rev) + 1);
        } catch (final NumberFormatException e) {
            throw new InternalServerErrorException("Malformed revision number '" + rev
                    + "' encountered while updating a resource");
        }
    }

}