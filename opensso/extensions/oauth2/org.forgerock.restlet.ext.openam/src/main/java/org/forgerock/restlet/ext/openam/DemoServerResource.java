package org.forgerock.restlet.ext.openam;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class DemoServerResource extends ServerResource {
    @Get
    public Representation demo() {
        StringBuilder sb = new StringBuilder("");
        if (getRequest().getClientInfo().getUser() instanceof OpenAMUser) {
            OpenAMUser user = (OpenAMUser) getRequest().getClientInfo().getUser();
            sb.append("Identifier: ").append(user.getIdentifier()).append("\n");
            sb.append("Realm: ").append(user.getRealm()).append("\n");
            sb.append("Token: ").append(user.getToken()).append("\n");
            sb.append("UniversalId: ").append(user.getUniversalId()).append("\n");
        } else {
            sb.append("ERROR - No OpenAM User");
            sb.append(getRequest().getClientInfo().getUser());
        }
        return new StringRepresentation(sb);
    }

}
