package org.forgerock.restlet.ext.openam;

import org.forgerock.restlet.ext.openam.internal.OpenAMServerAuthorizer;
import org.forgerock.restlet.ext.openam.server.OpenAMServletAuthenticator;
import org.restlet.Application;
import org.restlet.Restlet;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class DemoApplication extends Application {
    /**
     * Creates a inbound root Restlet that will receive all incoming calls. In
     * general, instances of Router, Filter or Finder classes will be used as
     * initial application Restlet. The default implementation returns null by
     * default. This method is intended to be overridden by subclasses.
     *
     * @return The inbound root Restlet.
     */
    @Override
    public Restlet createInboundRoot() {
        OpenAMServletAuthenticator root = new OpenAMServletAuthenticator(getContext(), null);
        root.setEnroler(new OpenAMEnroler());
        OpenAMServerAuthorizer authorizer = new OpenAMServerAuthorizer("OAUTH2");
        authorizer.setNext(DemoServerResource.class);
        root.setNext(authorizer);
        return root;
    }
}
