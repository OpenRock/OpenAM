package org.forgerock.restlet.ext.oauth2.flow;

import org.fest.assertions.Condition;
import org.fest.assertions.MapAssert;
import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.testng.annotations.Test;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.testng.Assert.assertTrue;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class RefreshTokenServerResourceTest extends AbstractFlowTest {
    @Test
    public void testRepresent() throws Exception {

        //Reference reference = new Reference("riap://component/test/oauth2/access_token?grant_type=refresh_token&refresh_token=abcd&scope=read%20write&state=random");
        Reference reference = new Reference("riap://component/test/oauth2/access_token");
        Request request = new Request(Method.POST, reference);
        Response response = new Response(request);
        Form parameters = new Form();
        parameters.add(OAuth2.Params.GRANT_TYPE, OAuth2.Params.REFRESH_TOKEN);
        parameters.add(OAuth2.Params.REFRESH_TOKEN, "abcd");
        parameters.add(OAuth2.Params.SCOPE, "read write");
        parameters.add(OAuth2.Params.STATE, "random");
        request.setEntity(parameters.getWebRepresentation());


        getClient().handle(request, response);
        assertTrue(MediaType.APPLICATION_JSON.equals(response.getEntity().getMediaType()));
        JacksonRepresentation representation = new JacksonRepresentation(response.getEntity(), Map.class);
        assertThat((Map) representation.getObject()).includes(MapAssert.entry(OAuth2.Params.TOKEN_TYPE, OAuth2.Bearer.BEARER.toLowerCase()),
                MapAssert.entry(OAuth2.Params.EXPIRES_IN, 3600)).
                is(new Condition<Map<?, ?>>() {
                    @Override
                    public boolean matches(Map<?, ?> value) {
                        return value.containsKey(OAuth2.Params.TOKEN_TYPE) && value.containsKey(OAuth2.Params.ACCESS_TOKEN);
                    }
                });
    }
}
