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
 * Copyright 2013-2015 ForgeRock AS.
 * Portions Copyrighted 2015 Nomura Research Institute, Ltd.
 */

package org.forgerock.openam.core.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.iplanet.dpro.session.SessionID;
import com.iplanet.dpro.session.monitoring.SessionMonitoringStore;
import com.iplanet.dpro.session.operations.ServerSessionOperationStrategy;
import com.iplanet.dpro.session.operations.SessionOperationStrategy;
import com.iplanet.dpro.session.service.SessionConstants;
import com.iplanet.dpro.session.service.SessionServerConfig;
import com.iplanet.dpro.session.service.SessionService;
import com.iplanet.dpro.session.service.SessionServiceConfig;
import com.iplanet.services.ldap.DSConfigMgr;
import com.iplanet.services.ldap.LDAPServiceException;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import com.sun.identity.common.configuration.ConfigurationObserver;
import com.sun.identity.delegation.DelegationManager;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoCreationListener;
import com.sun.identity.security.AdminTokenAction;
import com.sun.identity.setup.ServicesDefaultValues;
import com.sun.identity.shared.configuration.SystemPropertiesManager;
import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.stats.Stats;
import com.sun.identity.shared.validation.URLValidator;
import com.sun.identity.sm.DNMapper;
import com.sun.identity.sm.OrganizationConfigManager;
import com.sun.identity.sm.SMSEntry;
import com.sun.identity.sm.SMSException;
import com.sun.identity.sm.ServiceConfigManager;
import com.sun.identity.sm.ServiceManagementDAO;
import com.sun.identity.sm.ServiceManagementDAOWrapper;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializationProblemHandler;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;
import org.forgerock.guice.core.GuiceModule;
import org.forgerock.guice.core.InjectorHolder;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.oauth2.core.OAuth2Constants;
import org.forgerock.openam.cts.CTSPersistentStore;
import org.forgerock.openam.cts.CTSPersistentStoreImpl;
import org.forgerock.openam.cts.CoreTokenConfig;
import org.forgerock.openam.cts.adapters.OAuthAdapter;
import org.forgerock.openam.cts.adapters.SAMLAdapter;
import org.forgerock.openam.cts.adapters.TokenAdapter;
import org.forgerock.openam.cts.api.CoreTokenConstants;
import org.forgerock.openam.cts.api.tokens.SAMLToken;
import org.forgerock.openam.cts.impl.query.reaper.ReaperConnection;
import org.forgerock.openam.cts.impl.query.reaper.ReaperQuery;
import org.forgerock.openam.cts.impl.queue.ResultHandlerFactory;
import org.forgerock.openam.cts.monitoring.CTSConnectionMonitoringStore;
import org.forgerock.openam.cts.monitoring.CTSOperationsMonitoringStore;
import org.forgerock.openam.cts.monitoring.CTSReaperMonitoringStore;
import org.forgerock.openam.cts.monitoring.impl.CTSMonitoringStoreImpl;
import org.forgerock.openam.cts.monitoring.impl.queue.MonitoredResultHandlerFactory;
import org.forgerock.openam.entitlement.monitoring.PolicyMonitor;
import org.forgerock.openam.entitlement.monitoring.PolicyMonitorImpl;
import org.forgerock.openam.federation.saml2.SAML2TokenRepository;
import org.forgerock.openam.identity.idm.AMIdentityRepositoryFactory;
import org.forgerock.openam.session.SessionCache;
import org.forgerock.openam.session.SessionCookies;
import org.forgerock.openam.session.SessionPollerPool;
import org.forgerock.openam.session.SessionServiceURLService;
import org.forgerock.openam.session.SessionURL;
import org.forgerock.openam.session.blacklist.BloomFilterSessionBlacklist;
import org.forgerock.openam.session.blacklist.CTSSessionBlacklist;
import org.forgerock.openam.session.blacklist.CachingSessionBlacklist;
import org.forgerock.openam.session.blacklist.NoOpSessionBlacklist;
import org.forgerock.openam.session.blacklist.SessionBlacklist;
import org.forgerock.openam.sm.SMSConfigurationFactory;
import org.forgerock.openam.sm.ServerGroupConfiguration;
import org.forgerock.openam.sm.datalayer.api.ConnectionType;
import org.forgerock.openam.sm.datalayer.api.DataLayer;
import org.forgerock.openam.sm.datalayer.api.DataLayerConstants;
import org.forgerock.openam.sm.datalayer.api.DataLayerException;
import org.forgerock.openam.sm.datalayer.api.QueueConfiguration;
import org.forgerock.openam.utils.Config;
import org.forgerock.util.promise.Function;
import org.forgerock.util.promise.NeverThrowsException;
import org.forgerock.util.thread.ExecutorServiceFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Guice Module for configuring bindings for the OpenAM Core classes.
 */
@GuiceModule
public class CoreGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new AdminTokenType()).toProvider(new AdminTokenProvider()).in(Singleton.class);
        bind(ServiceManagementDAO.class).to(ServiceManagementDAOWrapper.class).in(Singleton.class);
        bind(DNWrapper.class).in(Singleton.class);
        bind(URLValidator.class).toInstance(URLValidator.getInstance());

        bind(new TypeLiteral<TokenAdapter<JsonValue>>(){})
                .annotatedWith(Names.named(OAuth2Constants.CoreTokenParams.OAUTH_TOKEN_ADAPTER))
                .to(OAuthAdapter.class);

        bind(DSConfigMgr.class).toProvider(new Provider<DSConfigMgr>() {
            public DSConfigMgr get() {
                try {
                    return DSConfigMgr.getDSConfigMgr();
                } catch (LDAPServiceException e) {
                    throw new IllegalStateException(e);
                }
            }
        }).in(Singleton.class);

        bind(SSOTokenManager.class).toProvider(new Provider<SSOTokenManager>() {
            public SSOTokenManager get() {
                try {
                    return SSOTokenManager.getInstance();
                } catch (SSOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }).in(Singleton.class);

        /**
         * Core Token Service bindings are divided into a number of logical groups.
         */
        // CTS General
        bind(CTSPersistentStore.class).to(CTSPersistentStoreImpl.class);
        bind(Debug.class).annotatedWith(Names.named(CoreTokenConstants.CTS_DEBUG))
                .toInstance(Debug.getInstance(CoreTokenConstants.CTS_DEBUG));
        bind(Debug.class).annotatedWith(Names.named(CoreTokenConstants.CTS_REAPER_DEBUG))
                .toInstance(Debug.getInstance(CoreTokenConstants.CTS_REAPER_DEBUG));
        bind(Debug.class).annotatedWith(Names.named(CoreTokenConstants.CTS_ASYNC_DEBUG))
                .toInstance(Debug.getInstance(CoreTokenConstants.CTS_ASYNC_DEBUG));
        bind(Debug.class).annotatedWith(Names.named(CoreTokenConstants.CTS_MONITOR_DEBUG))
                .toInstance(Debug.getInstance(CoreTokenConstants.CTS_MONITOR_DEBUG));
        bind(Debug.class).annotatedWith(Names.named(DataLayerConstants.DATA_LAYER_DEBUG))
                .toInstance(Debug.getInstance(DataLayerConstants.DATA_LAYER_DEBUG));

        bind(Debug.class).annotatedWith(Names.named(PolicyMonitor.POLICY_MONITOR_DEBUG))
                .toInstance(Debug.getInstance(PolicyMonitor.POLICY_MONITOR_DEBUG));

        bind(CoreTokenConstants.class).in(Singleton.class);
        bind(CoreTokenConfig.class).in(Singleton.class);

        // CTS Connection Management
        bind(String.class).annotatedWith(Names.named(DataLayerConstants.ROOT_DN_SUFFIX)).toProvider(new Provider<String>() {
            public String get() {
                return SMSEntry.getRootSuffix();
            }
        }).in(Singleton.class);
        bind(ConfigurationObserver.class).toProvider(new Provider<ConfigurationObserver>() {
            public ConfigurationObserver get() {
                return ConfigurationObserver.getInstance();
            }
        }).in(Singleton.class);

        // CTS Monitoring
        bind(CTSOperationsMonitoringStore.class).to(CTSMonitoringStoreImpl.class);
        bind(CTSReaperMonitoringStore.class).to(CTSMonitoringStoreImpl.class);
        bind(CTSConnectionMonitoringStore.class).to(CTSMonitoringStoreImpl.class);
        // Enable monitoring of all CTS operations
        bind(ResultHandlerFactory.class).to(MonitoredResultHandlerFactory.class);

        // CTS Reaper configuration
        bind(ReaperQuery.class).to(ReaperConnection.class);

        // Policy Monitoring
        bind(PolicyMonitor.class).to(PolicyMonitorImpl.class);

        // SAML2 token repository dependencies
        bind(new TypeLiteral<TokenAdapter<SAMLToken>>(){}).to(SAMLAdapter.class);

        /**
         * Session related dependencies.
         */
        bind(SessionOperationStrategy.class).to(ServerSessionOperationStrategy.class);
        // TODO: Investigate whether or not this lazy-loading "Config<SessionService>" wrapper is still needed
        bind(new TypeLiteral<Config<SessionService>>() {}).toInstance(new Config<SessionService>() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public SessionService get() {
                return InjectorHolder.getInstance(SessionService.class);
            }
        });

        bind(Debug.class)
                .annotatedWith(Names.named(SessionConstants.SESSION_DEBUG))
                .toInstance(Debug.getInstance(SessionConstants.SESSION_DEBUG));

        bind(new TypeLiteral<Function<String, String, NeverThrowsException>>() {})
                .annotatedWith(Names.named("tagSwapFunc"))
                .toInstance(new Function<String, String, NeverThrowsException>() {

                    @Override
                    public String apply(String text) {
                        return ServicesDefaultValues.tagSwap(text, true);
                    }

                });

        install(new FactoryModuleBuilder()
                .implement(AMIdentityRepository.class, AMIdentityRepository.class)
                .build(AMIdentityRepositoryFactory.class));

        Multibinder.newSetBinder(binder(), IdRepoCreationListener.class);

        bind(Stats.class)
                .annotatedWith(Names.named(SessionConstants.STATS_MASTER_TABLE))
                .toInstance(Stats.getInstance(SessionConstants.STATS_MASTER_TABLE));

        bind(SessionCache.class).toInstance(SessionCache.getInstance());
        bind(SessionPollerPool.class).toInstance(SessionPollerPool.getInstance());
        bind(SessionCookies.class).toInstance(SessionCookies.getInstance());
        bind(SessionURL.class).toInstance(SessionURL.getInstance());
        bind(SessionServiceURLService.class).toInstance(SessionServiceURLService.getInstance());
    }

    @Provides @Inject @Named(PolicyMonitorImpl.EXECUTOR_BINDING_NAME)
    ExecutorService getPolicyMonitoringExecutorService(ExecutorServiceFactory esf) {
        return esf.createFixedThreadPool(5);
    }

    @Provides @Inject @Named(CTSMonitoringStoreImpl.EXECUTOR_BINDING_NAME)
    ExecutorService getCTSMonitoringExecutorService(ExecutorServiceFactory esf) {
        return esf.createFixedThreadPool(5);
    }

    @Provides @Inject @Named(SessionMonitoringStore.EXECUTOR_BINDING_NAME)
    ExecutorService getSessionMonitoringExecutorService(ExecutorServiceFactory esf) {
        return esf.createFixedThreadPool(5);
    }

    /**
     * The CTS Worker Pool provides a thread pool specifically for CTS usage.
     *
     * This is only utilised by the CTS asynchronous queue implementation, therefore
     * we can size the pool based on the configuration for that.
     *
     * @param esf Factory for generating an appropriate ExecutorService.
     * @param queueConfiguration Required to resolve how many threads are required.
     * @return A configured ExecutorService, appropriate for the CTS usage.
     *
     * @throws java.lang.RuntimeException If there was an error resolving the configuration.
     */
    @Provides @Inject @Named(CoreTokenConstants.CTS_WORKER_POOL)
    ExecutorService getCTSWorkerExecutorService(ExecutorServiceFactory esf,
            @DataLayer(ConnectionType.CTS_ASYNC) QueueConfiguration queueConfiguration) {
        try {
            int size = queueConfiguration.getProcessors();
            return esf.createFixedThreadPool(size, CoreTokenConstants.CTS_WORKER_POOL);
        } catch (DataLayerException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides @Inject @Named(CoreTokenConstants.CTS_SCHEDULED_SERVICE)
    ScheduledExecutorService getCTSScheduledService(ExecutorServiceFactory esf) {
        return esf.createScheduledService(1);
    }

    @Provides @Inject @Named(CoreTokenConstants.CTS_SMS_CONFIGURATION)
    ServerGroupConfiguration getCTSServerConfiguration(SMSConfigurationFactory factory) {
        return factory.getSMSConfiguration();
    }

    @Provides @Singleton
    SAML2TokenRepository getSAML2TokenRepository() {

        final String DEFAULT_REPOSITORY_CLASS =
                "org.forgerock.openam.cts.impl.SAML2CTSPersistentStore";

        final String REPOSITORY_CLASS_PROPERTY =
                "com.sun.identity.saml2.plugins.SAML2RepositoryImpl";

        final String CTS_SAML2_REPOSITORY_CLASS_NAME =
                SystemPropertiesManager.get(REPOSITORY_CLASS_PROPERTY, DEFAULT_REPOSITORY_CLASS);

        SAML2TokenRepository result;
        try {
            // Use Guice to create class to get all of its dependency goodness
            result = InjectorHolder.getInstance(
            Class.forName(CTS_SAML2_REPOSITORY_CLASS_NAME).asSubclass(SAML2TokenRepository.class));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    @Provides
    @Inject
    @Named(DelegationManager.DELEGATION_SERVICE)
    ServiceConfigManager getServiceConfigManagerForDelegation(final PrivilegedAction<SSOToken> adminTokenAction) {
        try {
            final SSOToken adminToken = AccessController.doPrivileged(adminTokenAction);
            return new ServiceConfigManager(DelegationManager.DELEGATION_SERVICE, adminToken);

        } catch (SMSException smsE) {
            throw new IllegalStateException("Failed to retrieve the service config manager for delegation", smsE);
        } catch (SSOException ssoE) {
            throw new IllegalStateException("Failed to retrieve the service config manager for delegation", ssoE);
        }
    }

    /**
     * Provides instances of the OrganizationConfigManager which requires an Admin
     * token to perform its operations.
     *
     * @param provider Non null.
     * @return Non null.
     */
    @Provides @Inject
    OrganizationConfigManager getOrganizationConfigManager(AdminTokenProvider provider) {
        SSOToken token = AccessController.doPrivileged(AdminTokenAction.getInstance());
        try {
            return new OrganizationConfigManager(token, "/");
        } catch (SMSException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * CTS Jackson Object Mapper.
     * <p>
     * Use a static singleton as per <a href="http://wiki.fasterxml.com/JacksonBestPracticesPerformance">performance
     * best practice.</a>
     */
    @Provides @Named(CoreTokenConstants.OBJECT_MAPPER) @Singleton
    ObjectMapper getCTSObjectMapper() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, true);

        /**
         * @see http://stackoverflow.com/questions/7105745/how-to-specify-jackson-to-only-use-fields-preferably-globally
         */
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        SimpleModule customModule = new SimpleModule("openam", Version.unknownVersion());
        customModule.addKeyDeserializer(SessionID.class, new SessionIDKeyDeserialiser());
        mapper.registerModule(customModule);
        mapper.getDeserializationConfig().addHandler(new CompatibilityProblemHandler());

        return mapper;
    }

    /**
     * This simple {@link org.codehaus.jackson.map.KeyDeserializer} implementation allows us to use the {@link SessionID#toString()} value as a
     * map key instead of a whole {@link SessionID} object. During deserialization this class will reconstruct the
     * original SessionID object from the session ID string.
     */
    private static class SessionIDKeyDeserialiser extends KeyDeserializer {

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            return new SessionID(key);
        }
    }

    /**
     * This extension allows us to ignore the now unmapped restrictedTokensByRestriction field in InternalSession. This
     * is especially helpful when dealing with legacy tokens that still contain this field. As the field is now
     * recalculated based on the restrictedTokensBySid map, we just ignore this JSON property.
     */
    private static class CompatibilityProblemHandler extends DeserializationProblemHandler {

        private static final String RESTRICTED_TOKENS_BY_RESTRICTION = "restrictedTokensByRestriction";

        @Override
        public boolean handleUnknownProperty(DeserializationContext ctxt, JsonDeserializer<?> deserializer,
                Object beanOrClass, String propertyName) throws IOException, JsonProcessingException {
            if (propertyName.equals(RESTRICTED_TOKENS_BY_RESTRICTION)) {
                ctxt.getParser().skipChildren();
                return true;
            }
            return false;
        }
    }

    // Implementation exists to capture the generic type of the PrivilegedAction.
    private static class AdminTokenType extends TypeLiteral<PrivilegedAction<SSOToken>> {
    }

    // Simple provider implementation to return the static instance of AdminTokenAction.
    private static class AdminTokenProvider implements Provider<PrivilegedAction<SSOToken>> {
        public PrivilegedAction<SSOToken> get() {
            // Provider used over bind(..).getInstance(..) to enforce a lazy loading approach.
            return AdminTokenAction.getInstance();
        }
    }

    // provides our stored servlet context to classes which require it
    @Provides @Named(ServletContextCache.CONTEXT_REFERENCE)
    ServletContext getServletContext() {
        return ServletContextCache.getStoredContext();
    }

    @Provides @Named(SessionConstants.PRIMARY_SERVER_URL) @Inject @Singleton
    String getPrimaryServerURL(SessionServerConfig serverConfig) {
        return serverConfig.getPrimaryServerURL().toString();
    }

    @Provides @Singleton @Inject
    public static SessionBlacklist getSessionBlacklist(final CTSSessionBlacklist ctsBlacklist,
                                                       final SessionServiceConfig serviceConfig) {

        if (!serviceConfig.isSessionBlacklistingEnabled()) {
            return NoOpSessionBlacklist.INSTANCE;
        }

        final long purgeDelayMs = serviceConfig.getSessionBlacklistPurgeDelay(TimeUnit.MILLISECONDS);
        final int cacheSize = serviceConfig.getSessionBlacklistCacheSize();
        final long pollIntervalMs = serviceConfig.getSessionBlacklistPollInterval(TimeUnit.MILLISECONDS);

        SessionBlacklist blacklist = ctsBlacklist;
        if (cacheSize > 0) {
            blacklist = new CachingSessionBlacklist(blacklist, cacheSize, purgeDelayMs);
        }

        if (pollIntervalMs > 0) {
            blacklist = new BloomFilterSessionBlacklist(blacklist, serviceConfig);
        }

        return blacklist;
    }

    /**
     * Wrapper class to remove coupling to DNMapper static methods.
     * <p/>
     * Until DNMapper is refactored, this class can be used to assist with DI.
     */
    public static class DNWrapper {

        /**
         * @see com.sun.identity.sm.DNMapper#orgNameToDN(String)
         */
        public String orgNameToDN(String orgName) {
            return DNMapper.orgNameToDN(orgName);
        }

        /**
         * @see DNMapper#orgNameToRealmName(String)
         */
        public String orgNameToRealmName(String orgName) {
            return DNMapper.orgNameToRealmName(orgName);
        }

    }
}
