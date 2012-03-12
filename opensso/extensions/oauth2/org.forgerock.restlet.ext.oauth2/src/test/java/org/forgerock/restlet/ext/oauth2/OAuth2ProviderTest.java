package org.forgerock.restlet.ext.oauth2;

import org.forgerock.restlet.ext.oauth2.internal.OAuth2Component;
import org.forgerock.restlet.ext.oauth2.provider.OAuth2Provider;
import org.forgerock.restlet.ext.oauth2.provider.OAuth2RealmRouter;
import org.forgerock.restlet.ext.oauth2.representation.ClassDirectoryServerResource;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class OAuth2ProviderTest {

    protected OAuth2Provider pathProvider;
    protected OAuth2Provider queryProvider;
    protected Component component = new Component();

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

    @Test
    public void testGetRequestParameter() throws Exception {
        OAuth2Component c = new OAuth2Component();
        c.getConfiguration().put(OAuth2.Custom.REALM, "test");
        c.setProvider(pathProvider);
        c.activate();

        /*ClientResource client = new ClientResource(component.getContext().createChildContext(), "riap://component/test/oauth2/authorize");
        ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
        client.setChallengeResponse(challengeResponse);
        TestResource testable = client.wrap(TestResource.class);
        testable.get();*/

        Client client = component.getContext().getClientDispatcher();

        Reference reference = new Reference("riap://component/test/oauth2/authorize?response_type=token&client_iid=cid&scope=read%20write&state=random&redirect_uri=valami");
        Request request = new Request(Method.GET, reference);
        Response response = new Response(request);

        client.handle(request, response);
        Form token = new Form(response.getLocationRef().getFragment());
        assertNotNull(token.getFirstValue(OAuth2.Params.ACCESS_TOKEN));
        assertEquals(token.getFirstValue(OAuth2.Params.TOKEN_TYPE), OAuth2.Bearer.BEARER.toLowerCase());


        /*client.handle(request, new Uniform() {
            @Override
            public void handle(Request request, Response response) {
                System.out.println(response.getStatus());
                System.out.println(request.getResourceRef());
                assertNotNull(response);
            }
        });*/

    }
}
