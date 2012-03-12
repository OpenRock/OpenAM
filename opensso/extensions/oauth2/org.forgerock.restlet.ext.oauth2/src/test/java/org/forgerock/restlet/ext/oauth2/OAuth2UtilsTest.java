package org.forgerock.restlet.ext.oauth2;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.InputRepresentation;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class OAuth2UtilsTest {
    @Test
    public void testGetRequestParameter() throws Exception {
        Reference ref = new Reference("https://client.example.com/cb#error=access_denied&state=xyz");
        Form form = new Form(ref.getFragment());
        form.add("access_token", "value");
        ref.setFragment(form.getQueryString());
    }

    //@Test
    public void testJsonPostRequestParameter() throws Exception {
        InputStream is = OAuth2UtilsTest.class.getResourceAsStream("code.json");
        assertNotNull(is);
        InputRepresentation representation = new InputRepresentation(is, MediaType.TEXT_PLAIN);
        Request request = new Request(Method.POST, "riap://test.json", representation);
        Map<String, String> form = OAuth2Utils.ParameterLocation.HTTP_BODY.getParameters(request);
        assertEquals(form.get(OAuth2.Params.RESPONSE_TYPE), "code");
    }
}
