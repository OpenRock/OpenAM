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

package org.forgerock.openam.uma;

/**
 * UMA Constants.
 *
 * @since 13.0.0
 */
public final class UmaConstants {

    public static final String RESOURCE_SET_ID = "resource_set_id";
    public static final String SCOPES = "scopes";

    private UmaConstants() {
        //Private utility constructor
    }

    public static final String SERVICE_NAME = "UmaProvider";
    public static final String SERVICE_VERSION = "1.0";

    /** Constant for the UMA policy scheme. */
    public static final String UMA_POLICY_SCHEME = "uma://";
    /** Constant for the UMA policy scheme pattern. */
    public static final String UMA_POLICY_SCHEME_PATTERN = UMA_POLICY_SCHEME + "-*-";

    /** */
    public static final String UMA_POLICY_APPLICATION_TYPE = "iPlanetAMWebAgentService"; //TODO
    /** Constant for the PolicyResource backend bound in Guice. */
    public static final String UMA_BACKEND_POLICY_RESOURCE_HANDLER = "UMAPolicyResourceBackend";

    static final String RPT_LIFETIME_ATTR_NAME = "uma-rpt-lifetime";
    static final String PERMISSION_TIKCET_LIFETIME_ATTR_NAME = "uma-permission-ticket-lifetime";
    public static final String DELETE_POLICIES_ON_RESOURCE_SERVER_DELETION =
            "uma-delete-policies-on-resource-server-deletion";
    public static final String DELETE_RESOURCE_SETS_ON_RESOURCE_SERVER_DELETION
            = "uma-delete-resource-sets-on-resource-server-deletion";
    public static final String EMAIL_RESOURCE_OWNER_ON_PENDING_REQUEST_CREATION =
            "emailResourceOwnerOnPendingRequestCreation";
    public static final String EMAIL_REQUESTING_PARTY_ON_PENDING_REQUEST_APPROVAL
            = "emailRequestingPartyOnPendingRequestApproval";
    public static final String USER_PROFILE_PREFERRED_LOCAL_ATTRIBUTE = "userProfileLocaleAttribute";
    public static final String RESHARING_MODE = "resharingMode";
    public static final String TRUST_ELEVATION_REQUIRED = "requireTrustElevation";
    static final String SUPPORTED_PAT_PROFILES_ATTR_NAME = "uma-supported-pat-profiles";
    static final String SUPPORTED_AAT_PROFILES_ATTR_NAME = "uma-supported-aat-profiles";
    static final String SUPPORTED_RPT_PROFILES_ATTR_NAME = "uma-supported-rpt-profiles";
    static final String SUPPORTED_CLAIM_TOKEN_PROFILES_ATTR_NAME = "uma-supported-claim-token-profiles";
    static final String SUPPORTED_UMA_PROFILES_ATTR_NAME = "uma-supported-uma-profiles";
    static final String AUDIT_CONNECTION_CONFIG = "uma-audit-connection-config";

    static final String PAT_SCOPE = "uma_protection";
    static final String AAT_SCOPE = "uma_authorization";

    public static final String PERMISSION_REQUEST_ENDPOINT = "permission-request-endpoint";
    public static final String AUTHORIZATION_REQUEST_ENDPOINT = "authz-request-endpoint";

    static final String NOT_AUTHORISED_ERROR_CODE = "not_authorised";
    static final String REQUEST_SUBMITTED_ERROR_CODE = "request_submitted";
    static final String NEED_INFO_ERROR_CODE = "need_info";
    static final String INVALID_TICKET_ERROR_CODE = "invalid_ticket";
    static final String EXPIRED_TICKET_ERROR_CODE = "expired_ticket";

    static final class Introspection {
        static final String PERMISSIONS = "permissions";
    }

    public static final class UmaPolicy {
        public static final String POLICY_ID_KEY = "policyId";
        public static final String PERMISSIONS_KEY = "permissions";
        public static final String SUBJECT_KEY = "subject";
        public static final String SCOPES_KEY = "scopes";
        static final String POLICY_NAME = "name";
    }

    public static final class BackendPolicy {
        public static final String BACKEND_POLICY_RESOURCE_TYPE_KEY = "resourceTypeUuid";
        public static final String BACKEND_POLICY_ACTION_VALUES_KEY = "actionValues";
        public static final String BACKEND_POLICY_SUBJECT_CLAIM_VALUE_KEY = "claimValue";
        static final String BACKEND_POLICY_NAME_KEY = "name";
        static final String BACKEND_POLICY_RESOURCES_KEY = "resources";
        static final String BACKEND_POLICY_SUBJECT_KEY = "subject";
        static final String BACKEND_POLICY_SUBJECT_TYPE_KEY = "type";
        static final String BACKEND_POLICY_SUBJECT_TYPE_OR = "OR";
        static final String BACKEND_POLICY_SUBJECT_TYPE_JWT_CLAIM = "JwtClaim";
        static final String BACKEND_POLICY_SUBJECTS_KEY = "subjects";
        static final String BACKEND_POLICY_SUBJECT_CLAIM_NAME_KEY = "claimName";
        static final String BACKEND_POLICY_SUBJECT_CLAIM_NAME = "sub";
    }

}
