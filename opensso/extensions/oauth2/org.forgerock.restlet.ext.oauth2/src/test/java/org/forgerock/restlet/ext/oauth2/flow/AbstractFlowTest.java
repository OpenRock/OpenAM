package org.forgerock.restlet.ext.oauth2.flow;

import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.forgerock.restlet.ext.oauth2.internal.OAuth2Component;
import org.forgerock.restlet.ext.oauth2.model.SessionClient;
import org.forgerock.restlet.ext.oauth2.provider.ClientVerifier;
import org.forgerock.restlet.ext.oauth2.provider.OAuth2Provider;
import org.forgerock.restlet.ext.oauth2.provider.OAuth2RealmRouter;
import org.forgerock.restlet.ext.oauth2.representation.ClassDirectoryServerResource;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.MapVerifier;
import org.restlet.security.Verifier;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.testng.Assert.assertNotNull;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class AbstractFlowTest {
    protected OAuth2Provider pathProvider;
    protected OAuth2Provider queryProvider;
    protected Component component = new Component();
    protected OAuth2Component realm = null;

    @BeforeClass
    public void beforeClass() throws Exception {
        component.getClients().add(Protocol.RIAP); // Enable Client connectors
        component.getClients().add(Protocol.FILE); // Enable Client connectors
        component.getClients().add(Protocol.CLAP); // Enable Client connectors
        //component.getClients().add(Protocol.HTTP); // Enable Client connectors
        component.getStatusService().setEnabled(false); // The status service is disabled by default.
        Application application = new Application(component.getContext().createChildContext());
        application.getTunnelService().setQueryTunnel(false); // query string purism

        // create InboundRoot
        Router root = new Router(application.getContext());
        Directory directory = new Directory(root.getContext(), "clap:///resources");
        directory.setTargetClass(ClassDirectoryServerResource.class);
        root.attach("/resources", directory);

        OAuth2RealmRouter realmRouter = new OAuth2RealmRouter(application.getContext());
        root.attach("/{realm}/oauth2", realmRouter);
        pathProvider = realmRouter;
        realmRouter = new OAuth2RealmRouter(application.getContext());
        root.attach("/oauth2", realmRouter);
        queryProvider = realmRouter;
        application.setInboundRoot(root);

        //Attach to internal routes
        component.getInternalRouter().attach("", application);
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        assertNotNull(pathProvider);
        realm = new OAuth2Component();
        realm.getConfiguration().put(OAuth2.Custom.REALM, "test");
        realm.setClientVerifier(getClientVerifier());
        realm.setUserVerifier(getUserVerifier());
        realm.setProvider(pathProvider);
        realm.activate();
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        realm.deactivate();
    }

    protected Client getClient() {
        return component.getContext().getClientDispatcher();
    }

    public ClientVerifier getClientVerifier() {
        final org.forgerock.restlet.ext.oauth2.model.Client client = new org.forgerock.restlet.ext.oauth2.model.Client() {
            public String getClientId() {
                return "cid";
            }

            public ClientType getClientType() {
                return ClientType.CONFIDENTIAL;
            }

            public Set<URI> getRedirectionURIs() {
                return null;
            }

            public String getAccessTokenType() {
                return OAuth2.Bearer.BEARER;
            }

            public String getClientAuthenticationSchema() {
                return ChallengeScheme.HTTP_BASIC.getName();
            }

            public Set<String> allowedGrantScopes() {
                return Collections.emptySet();
            }

            public Set<String> defaultGrantScopes() {
                return Collections.emptySet();
            }

            @Override
            public boolean isAutoGrant() {
                return false;
            }
        };

        return new ClientVerifier() {
            @Override
            public org.forgerock.restlet.ext.oauth2.model.Client verify(ChallengeResponse challengeResponse) throws OAuthProblemException {
                return client;
            }

            @Override
            public org.forgerock.restlet.ext.oauth2.model.Client verify(String client_id, String client_secret) throws OAuthProblemException {
                return client;
            }

            @Override
            public Collection<ChallengeScheme> getRequiredAuthenticationScheme(String client_id) {
                return Collections.emptySet();
            }

            @Override
            public org.forgerock.restlet.ext.oauth2.model.Client findClient(String client_id) {
                return client;
            }
        };
    }


    public Verifier getUserVerifier() {
        MapVerifier mapVerifier = new MapVerifier();
        // Load a single static login/secret pair.
        mapVerifier.getLocalSecrets().put("admin", "admin".toCharArray());
        return mapVerifier;
    }
}
