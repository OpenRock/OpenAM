package org.forgerock.restlet.ext.oauth2;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public interface TestResource {

    @Get("html?response_type=code&client_id=cid&scope=read%20write&state=random")
    public Representation get();

    @Post("form|json?response_type=code&client_id=cid&scope=read%20write&state=random")
    public Representation post(Representation entry);

}
