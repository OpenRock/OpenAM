package org.forgerock.openam.xacml.v3.resources;

import com.sun.identity.shared.debug.Debug;

import org.forgerock.openam.xacml.v3.model.CommonType;
import org.forgerock.openam.xacml.v3.model.ContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * XACML Resource for Home Documents
 * <p/>
 * Provides main end-point for all XACML Home requests.
 * <p/>
 * X500Name
 */
public class XACML3HomeResource extends XACML3Resource {

    /**
     * Define our Static resource Bundle for our debugger.
     */
    private static Debug DEBUG = Debug.getInstance("amXACML");

    /**
     * Do not allow instantiation, only static methods.
     */
    private XACML3HomeResource() {
    }

    /**
     * Creates Home Document Content providing hints.
     *
     * @return String -- Containing Response in requested ContentType.
     */
    public static String getHomeDocument(HttpServletRequest httpServletRequest, ContentType requestContentType) {
        // Determine Rendering to respond based upon Content Type.
        if (requestContentType.getCommonType().equals(CommonType.XML)) {
            return getXMLHomeDocument(httpServletRequest);
        } else {
            // Perform JSON rendering...
            return getJSONHomeDocument(httpServletRequest);
        }
    }

    /**
     * Home Document
     * XML Home Document using ATOM RFC4287
     *
     * @return String -- Containing Rendered HomeDocument in Atom format.
     */
    private static String getXMLHomeDocument(HttpServletRequest httpServletRequest) {
        StringBuilder sb = new StringBuilder();
        // Formulate the Home Document for XML Consumption, based upon Atom - RFC4287
        sb.append(XML_HEADER);
        sb.append("<resources xmlns=\042http://ietf.org/ns/home-documents\042");
        sb.append(" xmlns:atom=\042http://www.w3.org/2005/Atom\042>");
        sb.append("<resource rel=\042http://docs.oasis-open.org/ns/xacml/relation/pdp\042>");
        sb.append("<atom:link href=\042" + PDP_ENDPOINT + "\042/>");
        sb.append("</resource>");
        sb.append("</resources>");
        return sb.toString();
    }

    /**
     * Formulate our Home Document.
     *
     * @return String -- Containing Rendering of Home Document in JSON
     */
    private static String getJSONHomeDocument(HttpServletRequest httpServletRequest) {
        JSONObject resources = new JSONObject();
        JSONArray resourceArray = new JSONArray();
        try {
            // Main End-Point
            JSONObject resource_1 = new JSONObject();
            resource_1.append("href", PDP_ENDPOINT);
            resource_1.append("hints", getPDPHints(httpServletRequest));
            JSONObject resource_1A = new JSONObject();
            resource_1A.append(getPDPHome(httpServletRequest), resource_1);
            // Assemble
            resourceArray.put(resource_1A);
            resources.append("resources", resourceArray);
        } catch (JSONException je) {
            DEBUG.error("JSON Processing Exception: "+je.getMessage(),je);
        }
        return resources.toString();
    }

    /**
     * Formulate our Hints for our REST EndPoint to allow Discovery.
     * Per Internet Draft: draft-nottingham-json-home-02
     *
     * @return JSONObject - Containing Hints for our Home Application.
     *
     */
    private static JSONObject getPDPHints(HttpServletRequest httpServletRequest) {
        JSONObject hints = new JSONObject();
        try {
            /**
             * Hints the HTTP methods that the current client will be able to use to
             * interact with the resource; equivalent to the Allow HTTP response
             * header.
             *
             * Content MUST be an array of strings, containing HTTP methods.
             */
            JSONArray allow = new JSONArray();
            allow.put("POST");
            hints.append("allow", allow);

            /**
             * Hints the representation types that the resource produces and
             * consumes, using POST methods respectively, subject to the
             * ’allow’ hint.
             *
             * Content MUST be an array of strings, containing media types.
             */
            JSONArray representations = new JSONArray();
            representations.put(ContentType.JSON.getApplicationType());
            representations.put(ContentType.XML.getApplicationType());
            representations.put(ContentType.XACML_PLUS_JSON.getApplicationType());
            representations.put(ContentType.XACML_PLUS_XML.getApplicationType());
            hints.append("representations", representations);

            /**
             * Hints the POST request formats accepted by the resource for this
             * client.
             *
             * Content MUST be an array of strings, containing media types.
             *
             * When this hint is present, "POST" SHOULD be listed in the "allow"
             * hint.
             */
            JSONArray accept_post = new JSONArray();
            accept_post.put(ContentType.JSON.getApplicationType());
            accept_post.put(ContentType.XML.getApplicationType());
            accept_post.put(ContentType.XACML_PLUS_JSON.getApplicationType());
            accept_post.put(ContentType.XACML_PLUS_XML.getApplicationType());
            hints.append("accept-post", accept_post);
        } catch (JSONException je) {
            DEBUG.error("JSON Processing Exception: "+je.getMessage(),je);
        }
        /**
         * Return our Hints for consumption by requester.
         */
        return hints;
    }

    /**
     * Get our Server URL construct from our incoming Request.
     *
     * @return - Base XACML URL yields "schema://serverName:LocalPort/contextPath/servletPath"
     */
    public final static String getPDPHome(HttpServletRequest httpServletRequest) {
        StringBuilder sb = new StringBuilder();
        sb.append(httpServletRequest.getScheme());
        sb.append("://");
        sb.append(httpServletRequest.getServerName());
        sb.append(":");
        sb.append(httpServletRequest.getLocalPort());
        sb.append(httpServletRequest.getContextPath());
        sb.append(httpServletRequest.getServletPath());
        return sb.toString();
    }


}
