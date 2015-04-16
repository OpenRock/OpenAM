/*
 * Copyright 2014-2015 ForgeRock AS.
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
 * information: "Portions copyright [year] [name of copyright owner]".
 */

package org.forgerock.openam.forgerockrest.guice;

import static org.forgerock.openam.forgerockrest.entitlements.query.AttributeType.*;
import static org.forgerock.openam.uma.UmaConstants.*;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.forgerock.guice.core.GuiceModule;
import org.forgerock.json.resource.ConnectionFactory;
import org.forgerock.json.resource.RequestType;
import org.forgerock.json.resource.ResourceException;
import org.forgerock.json.resource.Resources;
import org.forgerock.json.resource.VersionSelector;
import org.forgerock.oauth2.restlet.resources.ResourceSetRegistrationListener;
import org.forgerock.openam.cts.utils.JSONSerialisation;
import org.forgerock.openam.entitlement.EntitlementRegistry;
import org.forgerock.openam.errors.ExceptionMappingHandler;
import org.forgerock.openam.forgerockrest.IdentityResourceV1;
import org.forgerock.openam.forgerockrest.IdentityResourceV2;
import org.forgerock.openam.forgerockrest.cts.CoreTokenResource;
import org.forgerock.openam.forgerockrest.entitlements.ApplicationsResource;
import org.forgerock.openam.forgerockrest.entitlements.EntitlementEvaluatorFactory;
import org.forgerock.openam.forgerockrest.entitlements.EntitlementsExceptionMappingHandler;
import org.forgerock.openam.forgerockrest.entitlements.JsonPolicyParser;
import org.forgerock.openam.forgerockrest.entitlements.PolicyEvaluatorFactory;
import org.forgerock.openam.forgerockrest.entitlements.PolicyParser;
import org.forgerock.openam.forgerockrest.entitlements.PolicyResource;
import org.forgerock.openam.forgerockrest.entitlements.PolicyStoreProvider;
import org.forgerock.openam.forgerockrest.entitlements.PrivilegePolicyStoreProvider;
import org.forgerock.openam.forgerockrest.entitlements.query.QueryAttribute;
import org.forgerock.openam.forgerockrest.utils.MailServerLoader;
import org.forgerock.openam.forgerockrest.utils.RestLog;
import org.forgerock.openam.forgerockrest.utils.SoapSTSAgentIdentity;
import org.forgerock.openam.forgerockrest.utils.SoapSTSAgentIdentityImpl;
import org.forgerock.openam.forgerockrest.utils.SpecialUserIdentity;
import org.forgerock.openam.forgerockrest.utils.SpecialUserIdentityImpl;
import org.forgerock.openam.rest.RestEndpointServlet;
import org.forgerock.openam.rest.RestEndpoints;
import org.forgerock.openam.rest.authz.CoreTokenResourceAuthzModule;
import org.forgerock.openam.rest.authz.PrivilegeDefinition;
import org.forgerock.openam.rest.resource.PromisedRequestHandler;
import org.forgerock.openam.rest.resource.PromisedRequestHandlerImpl;
import org.forgerock.openam.rest.router.CTSPersistentStoreProxy;
import org.forgerock.openam.rest.router.DelegationEvaluatorProxy;
import org.forgerock.openam.rest.router.RestEndpointManager;
import org.forgerock.openam.rest.router.RestEndpointManagerProxy;
import org.forgerock.openam.rest.scripting.ScriptExceptionMappingHandler;
import org.forgerock.openam.rest.sms.SmsCollectionProvider;
import org.forgerock.openam.rest.sms.SmsCollectionProviderFactory;
import org.forgerock.openam.rest.sms.SmsGlobalSingletonProvider;
import org.forgerock.openam.rest.sms.SmsGlobalSingletonProviderFactory;
import org.forgerock.openam.rest.sms.SmsRequestHandler;
import org.forgerock.openam.rest.sms.SmsRequestHandlerFactory;
import org.forgerock.openam.rest.sms.SmsSingletonProvider;
import org.forgerock.openam.rest.sms.SmsSingletonProviderFactory;
import org.forgerock.openam.rest.uma.UmaIdRepoCreationListener;
import org.forgerock.openam.rest.uma.UmaPolicyServiceImpl;
import org.forgerock.openam.rest.uma.UmaResourceSetRegistrationListener;
import org.forgerock.openam.scripting.ScriptException;
import org.forgerock.openam.uma.UmaPolicyService;
import org.forgerock.openam.utils.AMKeyProvider;
import org.forgerock.openam.utils.Config;
import org.forgerock.util.SignatureUtil;
import org.restlet.routing.Router;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.iplanet.am.util.SystemProperties;
import com.iplanet.dpro.session.service.SessionService;
import com.sun.identity.delegation.DelegationEvaluator;
import com.sun.identity.delegation.DelegationEvaluatorImpl;
import com.sun.identity.entitlement.Application;
import com.sun.identity.entitlement.EntitlementException;
import com.sun.identity.entitlement.Privilege;
import com.sun.identity.entitlement.PrivilegeManager;
import com.sun.identity.entitlement.opensso.PolicyPrivilegeManager;
import com.sun.identity.idm.IdRepoCreationListener;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.debug.Debug;

/**
 * Guice Module for configuring bindings for the AuthenticationRestService classes.
 */
@GuiceModule
public class ForgerockRestGuiceModule extends AbstractModule {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bind(AMKeyProvider.class).in(Singleton.class);
        bind(SignatureUtil.class).toProvider(new Provider<SignatureUtil>() {
            public SignatureUtil get() {
                return SignatureUtil.getInstance();
            }
        });

        bind(EntitlementRegistry.class).toInstance(EntitlementRegistry.load());

        bind(Debug.class).annotatedWith(Names.named("frRest")).toInstance(Debug.getInstance("frRest"));

        // PolicyResource configuration
        bind(PrivilegeManager.class).to(PolicyPrivilegeManager.class);
        bind(new TypeLiteral<ExceptionMappingHandler<EntitlementException, ResourceException>>() {})
                .to(EntitlementsExceptionMappingHandler.class);
        bind(PolicyParser.class).to(JsonPolicyParser.class);
        bind(PolicyStoreProvider.class).to(PrivilegePolicyStoreProvider.class);
        bind(new TypeLiteral<Map<Integer, Integer>>() {})
                .annotatedWith(Names.named(EntitlementsExceptionMappingHandler.RESOURCE_ERROR_MAPPING))
                .toProvider(EntitlementsResourceErrorMappingProvider.class)
                .asEagerSingleton();

        // Error code overrides for particular request types. Maps NOT FOUND errors on Create requests to BAD REQUESTs.
        final Map<RequestType, Map<Integer, Integer>> errorCodeOverrides =
                new EnumMap<RequestType, Map<Integer, Integer>>(RequestType.class);
        errorCodeOverrides.put(RequestType.CREATE,
                Collections.singletonMap(ResourceException.NOT_FOUND, ResourceException.BAD_REQUEST));

        bind(new TypeLiteral<Map<RequestType, Map<Integer, Integer>>>() {})
                .annotatedWith(Names.named(EntitlementsExceptionMappingHandler.REQUEST_TYPE_ERROR_OVERRIDES))
                .toInstance(errorCodeOverrides);

        bind(new TypeLiteral<Map<Integer, Integer>>() {})
                .annotatedWith(Names.named(EntitlementsExceptionMappingHandler.DEBUG_TYPE_OVERRIDES))
                .toProvider(EntitlementsResourceDebugMappingProvider.class)
                .asEagerSingleton();

        bind(new TypeLiteral<Map<String, QueryAttribute>>() {
        })
                .annotatedWith(Names.named(PrivilegePolicyStoreProvider.POLICY_QUERY_ATTRIBUTES))
                .toProvider(PolicyQueryAttributesMapProvider.class)
                .asEagerSingleton();
        bind(PolicyEvaluatorFactory.class).to(EntitlementEvaluatorFactory.class).in(Singleton.class);

        bind(new TypeLiteral<Map<String, QueryAttribute>>() {})
                .annotatedWith(Names.named(ApplicationsResource.APPLICATION_QUERY_ATTRIBUTES))
                .toProvider(ApplicationQueryAttributesMapProvider.class)
                .asEagerSingleton();

        bind(RestEndpointManager.class).to(RestEndpointManagerProxy.class);
        bind(VersionSelector.class).in(Singleton.class);
        bind(DelegationEvaluatorImpl.class).in(Singleton.class);
        bind(DelegationEvaluator.class).to(DelegationEvaluatorProxy.class).in(Singleton.class);

        bind(UmaPolicyService.class).to(UmaPolicyServiceImpl.class);
        bind(SoapSTSAgentIdentity.class).to(SoapSTSAgentIdentityImpl.class);
        bind(SpecialUserIdentity.class).to(SpecialUserIdentityImpl.class);

        Multibinder.newSetBinder(binder(), IdRepoCreationListener.class)
                .addBinding().to(UmaIdRepoCreationListener.class);

        Multibinder.newSetBinder(binder(), ResourceSetRegistrationListener.class)
                .addBinding().to(UmaResourceSetRegistrationListener.class);

        // Scripting configuration
        bind(new TypeLiteral<ExceptionMappingHandler<ScriptException, ResourceException>>() {})
                .to(ScriptExceptionMappingHandler.class);

        install(new FactoryModuleBuilder()
                .implement(SmsRequestHandler.class, SmsRequestHandler.class)
                .build(SmsRequestHandlerFactory.class));

        install(new FactoryModuleBuilder()
                .implement(SmsCollectionProvider.class, SmsCollectionProvider.class)
                .build(SmsCollectionProviderFactory.class));

        install(new FactoryModuleBuilder()
                .implement(SmsSingletonProvider.class, SmsSingletonProvider.class)
                .build(SmsSingletonProviderFactory.class));

        install(new FactoryModuleBuilder()
                .implement(SmsGlobalSingletonProvider.class, SmsGlobalSingletonProvider.class)
                .build(SmsGlobalSingletonProviderFactory.class));
    }

    @Provides
    @Inject
    @Singleton
    @Named(UMA_BACKEND_POLICY_RESOURCE_HANDLER)
    PromisedRequestHandler getPolicyResource(PolicyResource policyResource) {
        org.forgerock.json.resource.Router router = new org.forgerock.json.resource.Router();
        router.setDefaultRoute(Resources.newCollection(policyResource));
        return new PromisedRequestHandlerImpl(router);
    }

    @Provides
    @Inject
    @Named(RestEndpointServlet.CREST_CONNECTION_FACTORY_NAME)
    @Singleton
    public ConnectionFactory getConnectionFactory(RestEndpoints restEndpoints) {
        return Resources.newInternalConnectionFactory(restEndpoints.getResourceRouter());
    }

    @Inject
    @Provides
    @Named("OAuth2Router")
    @Singleton
    public Router getOAuth2Router(RestEndpoints restEndpoints) {
        return restEndpoints.getOAuth2ServiceRouter();
    }

    @Provides
    @Named("UsersResource")
    @Inject
    @Singleton
    public IdentityResourceV1 getUsersResourceV1(MailServerLoader mailServerLoader) {
        return new IdentityResourceV1(IdentityResourceV1.USER_TYPE, mailServerLoader);
    }

    @Provides
    @Named("GroupsResource")
    @Inject
    @Singleton
    public IdentityResourceV1 getGroupsResourceV1(MailServerLoader mailServerLoader) {
        return new IdentityResourceV1(IdentityResourceV1.GROUP_TYPE, mailServerLoader);
    }

    @Provides
    @Singleton
    public RestLog getRestLog() {
        return new RestLog();
    }

    @Provides
    @Named("AgentsResource")
    @Inject
    @Singleton
    public IdentityResourceV1 getAgentsResourceV1(MailServerLoader mailServerLoader) {
        return new IdentityResourceV1(IdentityResourceV1.AGENT_TYPE, mailServerLoader);
    }

    @Provides
    @Named("UsersResource")
    @Inject
    @Singleton
    public IdentityResourceV2 getUsersResource(MailServerLoader mailServerLoader) {
        return new IdentityResourceV2(IdentityResourceV2.USER_TYPE, mailServerLoader);
    }

    @Provides
    @Named("GroupsResource")
    @Inject
    @Singleton
    public IdentityResourceV2 getGroupsResource(MailServerLoader mailServerLoader) {
        return new IdentityResourceV2(IdentityResourceV2.GROUP_TYPE, mailServerLoader);
    }

    @Provides
    @Named("AgentsResource")
    @Inject
    @Singleton
    public IdentityResourceV2 getAgentsResource(MailServerLoader mailServerLoader) {
        return new IdentityResourceV2(IdentityResourceV2.AGENT_TYPE, mailServerLoader);
    }

    @Provides
    @Inject
    @Singleton
    public CoreTokenResource getCoreTokenResource(JSONSerialisation jsonSerialisation,
            CTSPersistentStoreProxy ctsPersistentStore, @Named("frRest") Debug debug) {
        return new CoreTokenResource(jsonSerialisation, ctsPersistentStore, debug);
    }

    @Provides
    @Inject
    @Singleton
    public CoreTokenResourceAuthzModule getCoreTokenResourceAuthzModule(
            Config<SessionService> sessionService, @Named("frRest") Debug debug) {
        boolean coreTokenResourceEnabled = SystemProperties.getAsBoolean(Constants.CORE_TOKEN_RESOURCE_ENABLED);
        return new CoreTokenResourceAuthzModule(sessionService, debug, coreTokenResourceEnabled);
    }

    public static Map<Integer, Integer> getEntitlementsErrorHandlers() {
        return new EntitlementsResourceErrorMappingProvider().get();
    }

    @Provides
    @Singleton
    public Map<String, PrivilegeDefinition> getPrivilegeDefinitions() {
        final Map<String, PrivilegeDefinition> definitions = new HashMap<String, PrivilegeDefinition>();

        final PrivilegeDefinition evaluateDefinition = PrivilegeDefinition
                .getInstance("evaluate", PrivilegeDefinition.Action.READ);
        definitions.put("evaluate", evaluateDefinition);
        definitions.put("evaluateTree", evaluateDefinition);

        return definitions;
    }

    /**
     * Provides the mapping between entitlements exceptions and CREST resource exceptions, based on the entitlements
     * error code. Anything not explicitly mapped here will be treated as an internal server error.
     */
    private static class EntitlementsResourceErrorMappingProvider implements Provider<Map<Integer, Integer>> {
        @Override
        public Map<Integer, Integer> get() {
            final Map<Integer, Integer> handlers = new HashMap<Integer, Integer>();

            handlers.put(EntitlementException.EMPTY_PRIVILEGE_NAME, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.NULL_ENTITLEMENT, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.UNSUPPORTED_OPERATION, ResourceException.NOT_SUPPORTED);
            handlers.put(EntitlementException.INVALID_XML, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_WSDL_LOCATION, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.MISSING_PRIVILEGE_JSON, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.SESSION_HAS_EXPIRED, ResourceException.FORBIDDEN);
            handlers.put(EntitlementException.INVALID_JSON, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.MISSING_PRIVILEGE_NAME, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.NO_SUCH_POLICY, ResourceException.NOT_FOUND);
            handlers.put(EntitlementException.APPLICATION_ALREADY_EXISTS, ResourceException.CONFLICT);
            handlers.put(EntitlementException.NO_SUCH_REFERRAL_PRIVILEGE, ResourceException.NOT_FOUND);
            handlers.put(EntitlementException.INCONSISTENT_WILDCARDS, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_PORT, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.MALFORMED_URL, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_RESOURCE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.NO_SUCH_APPLICATION, ResourceException.NOT_FOUND);
            handlers.put(EntitlementException.NOT_FOUND, ResourceException.NOT_FOUND);
            handlers.put(EntitlementException.PERMISSION_DENIED, ResourceException.FORBIDDEN);
            handlers.put(EntitlementException.SUBJECT_REQUIRED, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_SEARCH_FILTER, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_PROPERTY_VALUE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.START_DATE_AFTER_END_DATE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.MISSING_RESOURCE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.JSON_PARSE_ERROR, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.AUTHENTICATION_ERROR, ResourceException.FORBIDDEN);
            handlers.put(EntitlementException.INVALID_VALUE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.POLICY_NAME_MISMATCH, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.APP_RETRIEVAL_ERROR, ResourceException.NOT_FOUND);
            handlers.put(EntitlementException.UNKNOWN_POLICY_CLASS,         ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.POLICY_CLASS_CAST_EXCEPTION,  ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.POLICY_CLASS_NOT_INSTANTIABLE,ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.POLICY_CLASS_NOT_ACCESSIBLE,  ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_PROPERTY_VALUE_UNKNOWN_VALUE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.POLICY_ALREADY_EXISTS, ResourceException.CONFLICT);
            handlers.put(EntitlementException.END_IP_BEFORE_START_IP, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.IP_CONDITION_CONFIGURATION_REQUIRED, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.PAIR_PROPERTY_NOT_DEFINED, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.APP_NOT_CREATED_POLICIES_EXIST, ResourceException.CONFLICT);
            handlers.put(EntitlementException.INVALID_APP_TYPE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_APP_REALM, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.PROPERTY_CONTAINS_BLANK_VALUE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.APPLICATION_NAME_MISMATCH, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.INVALID_CLASS, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.RESOURCE_TYPE_ALREADY_EXISTS, ResourceException.CONFLICT);
            handlers.put(EntitlementException.NO_SUCH_RESOURCE_TYPE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.RESOURCE_TYPE_IN_USE, ResourceException.CONFLICT);
            handlers.put(EntitlementException.MISSING_RESOURCE_TYPE_NAME, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.RESOURCE_TYPE_REFERENCED, ResourceException.CONFLICT);
            handlers.put(EntitlementException.INVALID_RESOURCE_TYPE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.POLICY_DEFINES_INVALID_RESOURCE_TYPE, ResourceException.BAD_REQUEST);
            handlers.put(EntitlementException.MISSING_RESOURCE_TYPE, ResourceException.BAD_REQUEST);

            return handlers;
        }
    }

    private static class EntitlementsResourceDebugMappingProvider implements Provider<Map<Integer, Integer>> {
        @Override
        public Map<Integer, Integer> get() {
            final Map<Integer, Integer> handlers = new HashMap<Integer, Integer>();
            handlers.put(EntitlementException.NO_SUCH_POLICY, Debug.MESSAGE);

            return handlers;
        }
    }

    /**
     * Defines all allowed query attributes in queries against the policy endpoint.
     */
    private static class PolicyQueryAttributesMapProvider implements Provider<Map<String, QueryAttribute>> {
        @Override
        public Map<String, QueryAttribute> get() {
            final Map<String, QueryAttribute> attributes = new HashMap<String, QueryAttribute>();

            attributes.put("name", new QueryAttribute(STRING, Privilege.NAME_ATTRIBUTE));
            attributes.put("description", new QueryAttribute(STRING, Privilege.DESCRIPTION_ATTRIBUTE));
            attributes.put("applicationName", new QueryAttribute(STRING, Privilege.APPLICATION_ATTRIBUTE));
            attributes.put("createdBy", new QueryAttribute(STRING, Privilege.CREATED_BY_ATTRIBUTE));
            attributes.put("creationDate", new QueryAttribute(TIMESTAMP, Privilege.CREATION_DATE_ATTRIBUTE));
            attributes.put("lastModifiedBy", new QueryAttribute(STRING, Privilege.LAST_MODIFIED_BY_ATTRIBUTE));
            attributes.put("lastModifiedDate", new QueryAttribute(TIMESTAMP, Privilege.LAST_MODIFIED_DATE_ATTRIBUTE));
            attributes.put("resourceTypeUuid", new QueryAttribute(STRING, Privilege.RESOURCE_TYPE_UUID_ATTRIBUTE));

            return attributes;
        }
    }

    /**
     * Defines all allowed query attributes in queries against the application endpoint.
     */
    private static class ApplicationQueryAttributesMapProvider implements Provider<Map<String, QueryAttribute>> {
        @Override
        public Map<String, QueryAttribute> get() {
            final Map<String, QueryAttribute> attributes = new HashMap<String, QueryAttribute>();

            attributes.put("name", new QueryAttribute(STRING, Application.NAME_ATTRIBUTE));
            attributes.put("description", new QueryAttribute(STRING, Application.DESCRIPTION_ATTRIBUTE));
            attributes.put("createdBy", new QueryAttribute(STRING, Application.CREATED_BY_ATTRIBUTE));
            attributes.put("creationDate", new QueryAttribute(TIMESTAMP, Application.CREATION_DATE_ATTRIBUTE));
            attributes.put("lastModifiedBy", new QueryAttribute(STRING, Application.LAST_MODIFIED_BY_ATTRIBUTE));
            attributes.put("lastModifiedDate", new QueryAttribute(TIMESTAMP, Application.LAST_MODIFIED_DATE_ATTRIBUTE));

            return attributes;
        }
    }
}
