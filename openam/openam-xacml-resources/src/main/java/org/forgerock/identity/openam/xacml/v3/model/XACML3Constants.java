/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.identity.openam.xacml.v3.model;

import com.sun.identity.entitlement.xacml3.XACMLConstants;


/**
 * Model XACML3 Constants
 *
 * @author jeff.schenk@forgerock.com
 */
public interface XACML3Constants extends XACMLConstants {
    /**
     * Current Standards Schema Resource Name.
     */
    public static final String xacmlCoreSchemaResourceName =
            "xsd/xacml-core-v3-schema-wd-17.xsd";
    /**
     * XML Core Schema Resource Name.
     */
    public static final String xmlCoreSchemaResourceName =
            "xsd/xml.xsd";
    /**
     *  XACML 3 Default Namespace.
     */
    public static final String XACML3_NAMESPACE = "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17";
    /**
     *  XACML 3 Default PDP Realm.
     */
    public static final String XACML3_PDP_DEFAULT_REALM = "OpenAM_XACML_PDP_Realm";
    /**
     * Constant used to identify meta alias.
     */
    public static final String NAME_META_ALIAS_IN_URI = "metaAlias";

    /**
     * Digest Authentication Constants.
     */
    public static final String AUTHENTICATION_METHOD = "auth";   // See http://tools.ietf.org/html/rfc2617, only AUTH.
    public static final String USERNAME = "username";
    public static final String ANONYMOUS = "anonymous";
    /**
     * Common Key Definitions
     */
    public static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";
    public static final String AUTHORIZATION = "authorization";
    public static final String DIGEST = "Digest";
    public static final String REQUEST = "Request";

    public static final String REQUEST_COMBINED_DECISION = "CombinedDecision";
    public static final String REQUEST_RETURN_POLICY_ID_LIST = "ReturnPolicyIdList";
    public static final String REQUEST_MULTIREQUESTS =  "MultiRequests";
    public static final String REQUEST_XMLNS = "xmlns";

    public static final String REQUEST_ENVIRONMENT = "Environment";
    public static final String REQUEST_RESOURCE = "Resource";
    public static final String REQUEST_SUBJECT = "Subject";

    public static final String ATTRIBUTE = "Attribute";
    public static final String ATTRIBUTE_CATEGORY = "Category";
    public static final String ATTRIBUTES = "Attributes";
    public static final String ATTRIBUTE_ID = "AttributeId";
    public static final String ATTRIBUTE_VALUE = "AttributeValue";
    public static final String ATTRIBUTE_INCLUDE_IN_RESULT = "IncludeInResult";
    public static final String ATTRIBUTE_VALUE_CONTENT = "content";
    public static final String ATTRIBUTE_VALUE_VALUE = "Value";
    public static final String ATTRIBUTE_VALUE_DATATYPE = "DataType";

    public static final String SOAP_ENVELOPE = "Envelope";
    public static final String SOAP_HEADER = "Header";
    public static final String SOAP_BODY = "Body";

    // [SAML4XACML] Constants
    public static final String XACML_AUTHZ_QUERY = "XACMLAuthzDecisionQuery";
    public static final String XACML_AUTHZ_QUERY_ID = "ID";
    public static final String XACML_AUTHZ_QUERY_VERSION = "Version";
    public static final String XACML_AUTHZ_QUERY_ISSUE_INSTANT = "IssueInstant";

    public static final String XACML_AUTHZ_QUERY_DESTINATION_NAME = "Destination";
    public static final String XACML_AUTHZ_QUERY_CONSTENT_NAME = "Consent";

    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public static final String PDP_AUTHORIZATION_ENDPOINT = "/xacml/pdp/"+AUTHORIZATION;

    /**
     * RESTful XACML 3.0 Name Space Definitions.
     */
    public static final String URN_HTTP = "urn:oasis:names:tc:xacml:3.0:profile:rest:http";
    public static final String URN_HOME = "urn:oasis:names:tc:xacml:3.0:profile:rest:home";
    public static final String URN_PDP = "urn:oasis:names:tc:xacml:3.0:profile:rest:pdp";

    /**
     * Network Transport
     * Client and Server MUST use HTTP as the underlying Network Transport between each other.
     * Also, SSL/TLS is encouraged to be used as well to protect Data over Transport.
     */
    public static final String URN_CLIENT = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:http:client";
    public static final String URN_SERVER = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:http:server";

    /**
     * A RESTful XACML system MUST have a single entry point at a known location
     * Each implementation of this profile MUST document the location of the entry point
     */
    public static final String URN_ENTRY_POINT = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:documentation";

    /**
     * ￼
     * Normative Source: GET on the home location MUST return status code 200
     * ￼
     * Target: Response to GET request on the home location
     * ￼
     * Predicate: The HTTP status code in the [response] is 200
     * ￼
     * Prescription Level: mandatory
     */
    public static final String URN_HOME_STATUS = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:status";
    public static final String URN_HOME_BODY = "urn:oasis:names:tc:xacml:3.0:profile:rest:assertion:home:body";

}
