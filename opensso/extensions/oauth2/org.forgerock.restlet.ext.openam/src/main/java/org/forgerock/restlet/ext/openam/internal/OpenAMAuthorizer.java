package org.forgerock.restlet.ext.openam.internal;

import com.iplanet.sso.SSOException;
import com.sun.identity.policy.PolicyDecision;
import com.sun.identity.policy.PolicyEvaluator;
import com.sun.identity.policy.PolicyException;
import org.forgerock.restlet.ext.openam.OpenAMUser;
import org.forgerock.restlet.ext.openam.server.AbstractOpenAMAuthorizer;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class OpenAMAuthorizer extends AbstractOpenAMAuthorizer {

    protected PolicyEvaluator pe;

    /**
     * Default constructor.
     */
    public OpenAMAuthorizer() {
        super();
        init();
    }

    /**
     * Constructor.
     *
     * @param identifier The identifier unique within an application.
     */
    public OpenAMAuthorizer(String identifier) {
        super(identifier);
        init();
    }

    protected void init() {
        try {
            pe = new PolicyEvaluator(WEB_AGENT_SERVICE);
        } catch (SSOException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getL10NMessage(Locale.getDefault()), e);
        } catch (PolicyException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getCompleteL10NMessage(Locale.getDefault()), e);
        }
    }

    @Override
    protected boolean getPolicyDecision(OpenAMUser user, Request request, Response response) throws SSOException, PolicyException {
        Map<String, Set<String>> env = new HashMap<String, Set<String>>();
        Set<String> paramValue = new HashSet<String>(1);
        paramValue.add(getIdentifier());
        env.put("application_identifier", paramValue);
        //env.put(Condition.REQUEST_AUTHENTICATED_TO_REALMS, "/");

        Set<String> actions = new HashSet<String>();
        actions.add(request.getMethod().getName());
        PolicyDecision pd = pe.getPolicyDecision(user.getToken(),
                request.getResourceRef().toString(), actions, env);
        return pe.isAllowed(user.getToken(),
                request.getResourceRef().toString(), request.getMethod().getName(), env);
    }
}
