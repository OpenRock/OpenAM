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
 * Copyright 2015 ForgeRock AS.
 */
package org.forgerock.openam.audit;

import static java.util.Collections.*;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.openam.audit.AMAuditEventBuilderUtils.*;
import static org.forgerock.openam.audit.AuditConstants.*;
import static org.forgerock.openam.utils.ClientUtils.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.forgerock.audit.events.AccessAuditEventBuilder;
import org.forgerock.http.MutableUri;
import org.forgerock.http.protocol.Form;
import org.forgerock.http.protocol.Header;
import org.forgerock.http.protocol.Headers;
import org.forgerock.http.protocol.Request;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.utils.StringUtils;
import org.forgerock.services.context.ClientContext;
import org.forgerock.services.context.Context;

import com.iplanet.sso.SSOToken;

/**
 * Builder for OpenAM audit access events.
 *
 * @since 13.0.0
 */
public final class AMAccessAuditEventBuilder extends AccessAuditEventBuilder<AMAccessAuditEventBuilder> {

    /**
     * Provide value for "component" audit log field.
     *
     * @param value one of the predefined names from {@link AuditConstants.Component}
     * @return this builder for method chaining.
     */
    public AMAccessAuditEventBuilder component(Component value) {
        putComponent(jsonValue, value.toString());
        return this;
    }

    /**
     * Adds trackingId from property of {@link SSOToken}, if the provided
     * <code>SSOToken</code> is not <code>null</code>.
     *
     * @param ssoToken The SSOToken from which the trackingId value will be retrieved.
     * @return this builder
     */
    public AMAccessAuditEventBuilder trackingIdFromSSOToken(SSOToken ssoToken) {
        trackingId(getTrackingIdFromSSOToken(ssoToken));
        return this;
    }

    /**
     * Sets client, server and http details from HttpServletRequest.
     *
     * @param request HttpServletRequest from which client, server and http details will be retrieved.
     * @return this builder
     */
    public final AMAccessAuditEventBuilder forHttpServletRequest(HttpServletRequest request) {
        client(
                getClientIPAddress(request),
                request.getRemotePort(),
                isReverseDnsLookupEnabled() ? request.getRemoteHost() : "");
        server(
                request.getLocalAddr(),
                request.getLocalPort(),
                request.getLocalName());
        httpRequest(
                request.isSecure(),
                request.getMethod(),
                request.getRequestURL().toString(),
                getQueryParametersAsMap(request),
                getHeadersAsMap(request));
        return this;
    }

    /**
     * Sets client, server and http details from CHF Request and Context.
     *
     * @param request Request from which client, server and http details will be retrieved.
     * @param context Context from which client, server and http details will be retrieved.
     * @return this builder
     */
    public final AMAccessAuditEventBuilder forRequest(Request request, Context context) {
        ClientContext clientInfo = context.asContext(ClientContext.class);
        client(
                getClientIPAddress(context, request),
                clientInfo.getRemotePort(),
                isReverseDnsLookupEnabled() ? clientInfo.getRemoteHost() : "");
        MutableUri uri = request.getUri();
        String uriScheme = request.getUri().getScheme();
        if (StringUtils.isNotEmpty(uriScheme)) {
            uriScheme = uriScheme.toLowerCase();
        }
        boolean isSecure = "https".equals(uriScheme);
        httpRequest(
                isSecure,
                request.getMethod(),
                uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort() + uri.getPath(),
                getQueryParametersAsMap(request.getForm()),
                getHeadersAsMap(request.getHeaders()));
        return this;
    }

    /**
     * Sets the provided name for the event. This method is preferred over
     * {@link org.forgerock.audit.events.AuditEventBuilder#eventName(String)} as it allows OpenAM to manage event
     * names better and documentation to be automatically generated for new events.
     *
     * @param name one of the predefined names from {@link AuditConstants.EventName}
     * @return this builder
     */
    public AMAccessAuditEventBuilder eventName(EventName name) {
        return eventName(name.toString());
    }

    /**
     * Adds a JSON object of detail for the request.
     * @param detail A JsonValue object containing extra attributes of the request.
     * @return This builder.
     */
    public AMAccessAuditEventBuilder requestDetail(JsonValue detail) {
        return addDetail(detail, REQUEST);
    }

    private AMAccessAuditEventBuilder addDetail(JsonValue detail, String field) {
        if (detail != null) {
            if (jsonValue.contains(field)) {
                jsonValue.get(field).put(DETAIL, detail.getObject());
            } else {
                jsonValue.put(field, object(field(DETAIL, detail.getObject())));
            }
        }
        return this;
    }

    /**
     * Provide value for "realm" audit log field.
     *
     * @param realm The "realm" value.
     * @return this builder for method chaining.
     */
    public final AMAccessAuditEventBuilder realm(String realm) {
        putRealm(jsonValue, realm);
        return this;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getHeadersAsMap(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
            String name = e.nextElement();
            headers.put(name, list(request.getHeaders(name)));
        }
        return headers;
    }

    private Map<String, List<String>> getHeadersAsMap(Headers requestHeaders) {
        Map<String, List<String>> headers = new HashMap<>();
        for (Map.Entry<String, Header> header : requestHeaders.asMapOfHeaders().entrySet()) {
            headers.put(header.getKey(), new ArrayList<>(header.getValue().getValues()));
        }
        return headers;
    }

    private Map<String, List<String>> getQueryParametersAsMap(HttpServletRequest request) {
        return AMAuditEventBuilderUtils.getQueryParametersAsMap(request.getQueryString());
    }

    private Map<String, List<String>> getQueryParametersAsMap(Form form) {
        Map<String, List<String>> queryParameters = new LinkedHashMap<>();
        queryParameters.putAll(form);
        return queryParameters;
    }
}
