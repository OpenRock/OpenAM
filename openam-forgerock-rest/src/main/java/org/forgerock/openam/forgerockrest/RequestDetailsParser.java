package org.forgerock.openam.forgerockrest;

import com.iplanet.sso.SSOToken;
import com.sun.identity.cli.realm.RealmUtils;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SMSException;
import org.apache.commons.lang.StringUtils;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.resource.AbstractFutureResultHandler;
import org.forgerock.json.resource.BadRequestException;
import org.forgerock.json.resource.ConflictException;
import org.forgerock.json.resource.ForbiddenException;
import org.forgerock.json.resource.NotFoundException;
import org.forgerock.json.resource.PermanentException;
import org.forgerock.json.resource.Resource;
import org.forgerock.json.resource.ResultHandler;
import org.forgerock.json.resource.ServerContext;
import org.forgerock.json.resource.UpdateRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static org.forgerock.openam.forgerockrest.RestUtils.hasPermission;

/**
 * Copyright 2013 ForgeRock, Inc.
 * <p/>
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 * <p/>
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 * <p/>
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 */

/**
 * Alin: The logic of the parsing looked fairly complicated so my intial reaction was to break it out
 * into a separate class where we can provide better focus on the business logic it is trying to perform.
 * In addition, I have updated the method arguments and constructor arguments to improve dependency
 * injection and thus testability.
 */
public class RequestDetailsParser {

    public static Debug debug = Debug.getInstance("frRest");

    private OrganizationConfigManagerFactory factory;

    /**
     * Alin: Importantly, we are exposing the dependencies that this code needs. These can now be provided by mocking.
     */
    public RequestDetailsParser(OrganizationConfigManagerFactory factory) {
        this.factory = factory;
    }

    /**
     * Alin: The main task will be to break down this code further so it is more testable. Can this behaviour
     * be generalised? What is the essence it is trying to achieve?
     *
     * @param token
     * @param resourceName
     * @return
     * @throws NotFoundException
     */
    public Map<String, String> parseRequest(SSOToken token, String resourceName) throws NotFoundException {
        Map<String, String> details = new HashMap<String, String>(3);
        if (StringUtils.isBlank(resourceName)) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(resourceName.trim(), "/", false);
        boolean topLevel = true;
        String lastNonBlank = null;
        String lastNonBlankID = null;
        String tmp = null;
        StringBuilder realmPath = new StringBuilder("/"); //fqdn path to resource
        StringBuilder resourceID = null; //resource id
        StringBuilder endpoint = null; //defined endpoint

        OrganizationConfigManager ocm = null;

        ocm = factory.getManager(token, realmPath.toString());

        while (tokenizer.hasMoreElements()) {
            String next = tokenizer.nextToken();
            if (StringUtils.isNotBlank(next)) {
                if (null != lastNonBlank) {
                    try { // test to see if its a realm
                        if (realmPath.toString().equalsIgnoreCase("/") && topLevel) {
                            ocm = factory.getManager(token, realmPath.toString() + lastNonBlank);
                            realmPath.append(lastNonBlank);
                            topLevel = false;
                        } else {
                            ocm = factory.getManager(token, realmPath.toString() + "/" + lastNonBlank);
                            realmPath.append("/").append(lastNonBlank);
                        }
                        ocm = factory.getManager(token, realmPath.toString());
                    } catch (SMSException smse) {
                        // cannot retrieve realm, must be endpoint
                        debug.warning(next + "is the endpoint because it is not a realm");
                        endpoint = new StringBuilder("/");
                        endpoint.append(lastNonBlank);
                        if (!checkValidEndpoint(endpoint.toString())) {
                            debug.warning(endpoint.toString() + "is the endpoint because it is not a realm");
                            throw new NotFoundException("Endpoint " + endpoint.toString()
                                    + " is not a defined endpoint.");
                        }
                        // add the rest of tokens as resource name
                        lastNonBlankID = next;
                        while (tokenizer.hasMoreElements()) {
                            next = tokenizer.nextToken();
                            if (StringUtils.isNotBlank(next)) {
                                if (null != lastNonBlankID) {
                                    if (null == resourceID) {
                                        resourceID = new StringBuilder(lastNonBlankID);
                                    } else {
                                        resourceID.append("/").append(lastNonBlankID);
                                    }
                                }
                                lastNonBlankID = next;
                            }
                        }

                    }
                }
                lastNonBlank = next;
            }
        }

        details.put("realmPath", realmPath.toString());

        if (null != endpoint && !endpoint.toString().isEmpty()) {
            details.put("resourceName", endpoint.toString());
        } else {
            endpoint = new StringBuilder("/");
            details.put("resourceName", endpoint.append(lastNonBlank).toString());
        }
        if (null != resourceID) {
            details.put("resourceId", resourceID.append("/").append(lastNonBlankID).toString());
        } else if (null != lastNonBlank) {
            details.put("resourceId", lastNonBlankID);
        } else {
            throw new NotFoundException("Resource ID has not been provided.");
        }

        return details;
    }

}
