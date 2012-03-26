package org.forgerock.openam.oauth2;

import com.iplanet.dpro.session.SessionException;
import com.iplanet.dpro.session.share.SessionBundle;
import com.iplanet.services.naming.WebtopNaming;
import com.sun.identity.common.GeneralTaskRunnable;
import com.sun.identity.common.SystemTimer;
import com.sun.identity.ha.FAMPersisterManager;
import com.sun.identity.ha.FAMRecordPersister;
import com.sun.identity.shared.Constants;
import com.sun.identity.shared.configuration.SystemPropertiesManager;
import com.sun.identity.shared.debug.Debug;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.fluent.JsonValueException;
import org.forgerock.json.resource.JsonResource;
import org.forgerock.json.resource.JsonResourceException;

import java.util.Date;

/**
 * @author $author$
 * @version $Revision$ $Date$
 * @see org.forgerock.json.resource.SimpleJsonResource
 */
public class DefaultOAuthTokenRepository extends GeneralTaskRunnable implements JsonResource {

    private static final String BRIEF_DB_ERROR_MSG =
            "OAuth2 failover service is not functional due to DB unavailability.";

    private static final String DB_ERROR_MSG =
            "OAuth2 database is not available at this moment."
                    + "Please check with the system administrator " +
                    "for appropriate actions";


    // Private data members
    String serverId;

    /* Config data */
    private static boolean isDatabaseUp = true;
    static Debug debug = null;//SAML2Utils.debug;
    private String OAUTH2 = "oauth2";


    private FAMRecordPersister pSession = null;

    /**
     * Constructs new JMQSAML2Repository
     *
     * @throws Exception when cannot create a new SAML2 repository
     */
    public DefaultOAuthTokenRepository() throws Exception {

        String thisSessionServerProtocol = SystemPropertiesManager
                .get(Constants.AM_SERVER_PROTOCOL);
        String thisSessionServer = SystemPropertiesManager
                .get(Constants.AM_SERVER_HOST);
        String thisSessionServerPortAsString = SystemPropertiesManager
                .get(Constants.AM_SERVER_PORT);
        String thisSessionURI = SystemPropertiesManager
                .get(Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR);

        if (thisSessionServerProtocol == null
                || thisSessionServerPortAsString == null
                || thisSessionServer == null) {
            throw new SessionException(SessionBundle.rbName,
                    "propertyMustBeSet", null);
        }

        serverId = WebtopNaming.getServerID(thisSessionServerProtocol,
                thisSessionServer, thisSessionServerPortAsString,
                thisSessionURI);
        initPersistSession();

        SystemTimer.getTimer().schedule(this, new Date((
                System.currentTimeMillis() / 1000) * 1000));
    }

    /**
     * Initialize new FAMRecord persister
     */
    private void initPersistSession() {
        try {
            pSession = FAMPersisterManager.getInstance().
                    getFAMRecordPersister();
            isDatabaseUp = true;
        } catch (Exception e) {
            isDatabaseUp = false;
            debug.error(BRIEF_DB_ERROR_MSG);
            if (debug.messageEnabled()) {
                debug.message(DB_ERROR_MSG, e);
            }
        }

    }

    public boolean addElement(Object o) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean removeElement(Object o) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isEmpty() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public long getRunPeriod() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void run() {

    }


    // JsonResource


    /**
     * Standard JSON resource request methods.
     */
    public enum Method {
        create, read, update, delete, patch, query, action
    }

    /**
     * Handles a JSON resource request by dispatching to the method corresponding with the
     * method member of the request. If the request method is not one of the standard JSON
     * resource request methods, a {@code JsonResourceException} is thrown.
     * <p/>
     * This method catches any thrown {@code JsonValueException}, and rethrows it as a
     * {@link JsonResourceException#BAD_REQUEST}. This allows the use of JsonValue methods
     * to validate the content of the request.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    @Override
    public JsonValue handle(JsonValue request) throws JsonResourceException {
        try {
            try {
                switch (request.get("method").required().asEnum(Method.class)) {
                    case create:
                        return create(request);
                    case read:
                        return read(request);
                    case update:
                        return update(request);
                    case delete:
                        return delete(request);
                    case patch:
                        return patch(request);
                    case query:
                        return query(request);
                    case action:
                        return action(request);
                    default:
                        throw new JsonResourceException(JsonResourceException.BAD_REQUEST);
                }
            } catch (JsonValueException jve) {
                throw new JsonResourceException(JsonResourceException.BAD_REQUEST, jve);
            }
        } catch (Exception e1) {
            try {
                onException(e1); // give handler opportunity to throw its own exception
                throw e1;
            } catch (Exception e2) {
                if (e2 instanceof JsonResourceException) { // no rethrowing necessary
                    throw (JsonResourceException) e2;
                } else { // need to rethrow as resource exception
                    throw new JsonResourceException(JsonResourceException.INTERNAL_ERROR, e2);
                }
            }
        }
    }

    /**
     * Called to handle a "create" JSON resource request. This implementation throws a
     * {@link JsonResourceException#FORBIDDEN} exception.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    protected JsonValue create(JsonValue request) throws JsonResourceException {
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Called to handle a "read" JSON resource request. This implementation throws a
     * {@link JsonResourceException#FORBIDDEN} exception.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    protected JsonValue read(JsonValue request) throws JsonResourceException {
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Called to handle a "update" JSON resource request. This implementation throws a
     * {@link JsonResourceException#FORBIDDEN} exception.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    protected JsonValue update(JsonValue request) throws JsonResourceException {
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Called to handle a "delete" JSON resource request. This implementation throws a
     * {@link JsonResourceException#FORBIDDEN} exception.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    protected JsonValue delete(JsonValue request) throws JsonResourceException {
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Called to handle a "patch" JSON resource request. This implementation throws a
     * {@link JsonResourceException#FORBIDDEN} exception.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    protected JsonValue patch(JsonValue request) throws JsonResourceException {
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Called to handle a "query" JSON resource request. This implementation throws a
     * {@link JsonResourceException#FORBIDDEN} exception.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    protected JsonValue query(JsonValue request) throws JsonResourceException {
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Called to handle an "action" JSON resource request. This implementation throws a
     * {@link JsonResourceException#FORBIDDEN} exception.
     *
     * @param request the JSON resource request.
     * @return the JSON resource response.
     * @throws if there is an exception handling the request.
     */
    protected JsonValue action(JsonValue request) throws JsonResourceException {
        throw new JsonResourceException(JsonResourceException.FORBIDDEN);
    }

    /**
     * Provides the ability to handle an exception by taking additional steps such as
     * logging, and optionally to override by throwing its own {@link JsonResourceException}.
     * This implementation does nothing; it is intended to be overridden by a subclass.
     *
     * @param exception the exception that was thrown.
     * @throws JsonResourceException an optional exception to be thrown instead.
     */
    protected void onException(Exception exception) throws JsonResourceException {
        // default implementation does nothing
    }
}
