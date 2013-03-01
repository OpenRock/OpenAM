/**
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.identity.openam.xacml.v3.resources;

import com.sun.identity.shared.debug.Debug;
import org.forgerock.identity.openam.xacml.v3.commons.CommonType;
import org.forgerock.identity.openam.xacml.v3.model.XACML3Constants;
import org.forgerock.identity.openam.xacml.v3.model.XACMLRequestInformation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * XACML PIP Resource Builder.
 * <p/>
 * Policy Information Point (PIP) Static Helper.
 * <p/>
 * Provides Helper methods to properly Interrogate our Parsed Map
 * object from our incoming PEP request.
 * <p/>
 * This Helper, used by XacmlContentHandlerService.
 *
 * @author Jeff.Schenk@forgerock.com
 */
public class XacmlPIPResourceBuilder implements XACML3Constants {

    private static Debug debug = Debug.getInstance("amXACML");
    private static String className = XacmlPIPResourceBuilder.class.getSimpleName();

    /**
     * Private Constructor, to inhibit instantiation.
     */
    private XacmlPIPResourceBuilder() {
    }

    /**
     * Build Up our Xacml PIP Resource For Requests Object within our XacmlRequestInformation Object.
     * <p/>
     * Additional Elements will be set within the Xacml Request Information Object for UpStream Callers.
     *
     * @param xacmlRequestInformation
     */
    public static boolean buildXacmlPIPResourceForRequests(XACMLRequestInformation xacmlRequestInformation) {
        if ((xacmlRequestInformation == null) || (xacmlRequestInformation.getContent() == null)) {
            return false;
        }
        // Recursively Iterate to Build up the PIP Resource Object
        if (xacmlRequestInformation.getContent() == null) {
            // Initialize our PIP Resource Resolver.
            xacmlRequestInformation.setPipResourceResolver((new XacmlPIPResourceResolverFunctionArgumentImpl()));
        }
        // Now Iterate over the Content Parsed Object.
        return buildXacmlPIPResourceForRequests(xacmlRequestInformation, xacmlRequestInformation.getContent(), null);
    }

    /**
     * Show the Parsed Content Information for debugging.
     *
     * @param xacmlRequestInformation
     */
    public static StringBuilder dumpContentInformation(XACMLRequestInformation xacmlRequestInformation) {
        StringBuilder sb = new StringBuilder();
        if ((xacmlRequestInformation != null) && (xacmlRequestInformation.getContent() != null)) {
            dumpContentInformation(xacmlRequestInformation.getContent(), sb, 0);
            return sb;
        }
        return sb;
    }

    // *****************************************************************************
    // Private static methods to support available public methods.
    // *****************************************************************************

    /**
     * Parse Content Information from Map Object to see the XacmlPIPResourceResolver Object
     *
     * @param currentContent
     */
    private static boolean buildXacmlPIPResourceForRequests(XACMLRequestInformation xacmlRequestInformation,
                                                            Object currentContent, String currentEmbeddedKey) {
        if (currentContent == null) {
            return false;
        }
        // Determine the Type of Object and Iterate Over the Contents Recursively...
        if (currentContent instanceof Map) {
            // Cast our Object.
            Map<String, Object> contentMap = (Map<String, Object>) currentContent;
            // Iterate over the Key Set
            for (String key : contentMap.keySet()) {

                // TODO : When we go Java 7 or above use a String Switch here...

                // Start Checking for SOAP Envelop, SOAP Body, XACMLAuthzDecisionQuery, Request.
                if (key.toLowerCase().contains(SOAP_ENVELOPE.toLowerCase())) {
                    xacmlRequestInformation.setSoapEnvelopeNodePresent(true);
                    // Recursively access Inner or Embedded Contents...
                    buildXacmlPIPResourceForRequests(xacmlRequestInformation, contentMap.get(key), key);
                } else if (key.toLowerCase().contains(SOAP_HEADER.toLowerCase())) {
                    // Nothing interesting in the SOAP Header, ignore...
                    xacmlRequestInformation.setSoapEnvelopeNodePresent(true);
                    continue;
                } else if (key.toLowerCase().contains(SOAP_BODY.toLowerCase())) {
                    xacmlRequestInformation.setSoapEnvelopeNodePresent(true);
                    // Recursively access Inner or Embedded Contents...
                    buildXacmlPIPResourceForRequests(xacmlRequestInformation, contentMap.get(key), key);
                } else if (key.toLowerCase().contains(XACML_AUTHZ_QUERY.toLowerCase())) {
                    // Recursively access Inner or Embedded Contents...
                    buildXacmlPIPResourceForRequests(xacmlRequestInformation, contentMap.get(key), key);
                } else if (key.toLowerCase().contains(REQUEST.toLowerCase())) {
                    // Get the Request Immediate Attributes.
                    if (!(contentMap.get(key) instanceof Map)) {
                        debug.error("Request does not contain a Map Object, improperly parsed Object, " +
                                "ignoring Request!");
                        return false;
                    }
                    // **************************
                    // Process Request Contents
                    // TODO : Handle Multiple Requests.
                    xacmlRequestInformation.setRequestNodePresent(true);
                    Map requestMap = (Map) contentMap.get(key);
                    for (Object requestAttributes : requestMap.keySet()) {
                        // Process Attributes
                        if (requestAttributes instanceof Map) {
                            buildXacmlPIPResourceForRequests(xacmlRequestInformation,
                                    requestMap.get(requestAttributes), key);
                            continue;
                        } else if (requestAttributes instanceof List) {
                            processAttributes(xacmlRequestInformation, null, requestMap.get(requestAttributes));
                            continue;
                        } else {
                            // Save All Applicable Request Node Attributes.
                            // Cast our Object.
                            String attributeName = (String) requestAttributes;
                            // Determine our attribute name context.
                            if (removeNamespace(attributeName).equalsIgnoreCase(REQUEST_COMBINED_DECISION)) {
                                xacmlRequestInformation.setRequest_CombinedDecision(
                                        ((Boolean) requestMap.get(requestAttributes)).booleanValue());
                            } else if (removeNamespace(attributeName).equalsIgnoreCase(REQUEST_RETURN_POLICY_ID_LIST)) {
                                xacmlRequestInformation.setRequest_ReturnPolicyIdList(
                                        ((Boolean) requestMap.get(requestAttributes)).booleanValue());
                            } else if (removeNamespace(attributeName).equalsIgnoreCase(REQUEST_XMLNS)) {
                                xacmlRequestInformation.setRequest_NameSpace((String) requestMap.get(requestAttributes));
                            } else if (removeNamespace(attributeName).equalsIgnoreCase(ATTRIBUTES)) {
                                // Process our Attributes for this Request...
                                processAttributes(xacmlRequestInformation, null, requestMap.get(attributeName));
                            } else if (
                                    (xacmlRequestInformation.getContentType().commonType().equals(CommonType.JSON)) &&
                                            ((removeNamespace(attributeName).equalsIgnoreCase(REQUEST_ENVIRONMENT)) ||
                                                    (removeNamespace(attributeName).equalsIgnoreCase(REQUEST_RESOURCE)) ||
                                                    (removeNamespace(attributeName).equalsIgnoreCase(REQUEST_SUBJECT)))) {
                                // Process these Attributes as Outer Objects.
                                Map<String, Object> innerMap = (Map<String, Object>) requestMap.get(attributeName);
                                String innerCategory = null;
                                for (String innerKey : innerMap.keySet()) {
                                    if (innerKey.equalsIgnoreCase(ATTRIBUTE_CATEGORY)) {
                                        innerCategory = (String) innerMap.get(innerKey);
                                    } else if ((innerKey.equalsIgnoreCase(ATTRIBUTE)) &&
                                            (innerMap.get(innerKey) instanceof List)) {
                                        processAttributes(xacmlRequestInformation,
                                                innerCategory, innerMap.get(innerKey));
                                    } else {
                                        debug.error("Unknown Request Attribute Found: " + attributeName + ", Type: " +
                                                innerMap.get(innerKey).getClass().getName() +
                                                ", Value: " + innerMap.get(innerKey));
                                    }
                                } // End of Inner For Each Loop.
                            } else {
                                // Show any stragglers, if applicable.
                                debug.error("Unknown Request Attribute Found: " + attributeName + ", Type: " +
                                        requestMap.get(attributeName).getClass().getName() +
                                        ", Value: " + requestMap.get(attributeName));
                            }
                        }
                    } // End of Inner For Each Loop.

                } else {
                    if (contentMap.get(key) instanceof Collection) {
                        buildXacmlPIPResourceForRequests(xacmlRequestInformation,
                                contentMap.get(key), key);
                    } else {
                        // XACMLAuthzDecisionQuery Node Attributes
                        if (removeNamespace(currentEmbeddedKey).equalsIgnoreCase(XACML_AUTHZ_QUERY)) {
                            if (key.equalsIgnoreCase(XACML_AUTHZ_QUERY_ID)) {
                                xacmlRequestInformation.getXacmlAuthzDecisionQuery().setId(
                                        (String) contentMap.get(key));
                            } else if (key.equalsIgnoreCase(XACML_AUTHZ_QUERY_VERSION)) {
                                xacmlRequestInformation.getXacmlAuthzDecisionQuery().setVersion(
                                        (Integer) contentMap.get(key));
                            } else if (key.equalsIgnoreCase(XACML_AUTHZ_QUERY_ISSUE_INSTANT)) {
                                xacmlRequestInformation.getXacmlAuthzDecisionQuery().setIssueInstant(
                                        (String) contentMap.get(key));
                            } else if (key.equalsIgnoreCase(XACML_AUTHZ_QUERY_DESTINATION_NAME)) {
                                xacmlRequestInformation.getXacmlAuthzDecisionQuery().setDestination(
                                        (String) contentMap.get(key));
                            } else if (key.equalsIgnoreCase(XACML_AUTHZ_QUERY_CONSTENT_NAME)) {
                                xacmlRequestInformation.getXacmlAuthzDecisionQuery().setConsent(
                                        (String) contentMap.get(key));
                            }

                            // SOAP Node Attributes
                        } else if ((removeNamespace(currentEmbeddedKey).equalsIgnoreCase(SOAP_ENVELOPE)) ||
                                (removeNamespace(currentEmbeddedKey).equalsIgnoreCase(SOAP_HEADER)) ||
                                (removeNamespace(currentEmbeddedKey).equalsIgnoreCase(SOAP_BODY))) {
                            xacmlRequestInformation.setSoapEnvelopeNodePresent(true);
                            // Nothing else we need at this point!
                        } else {
                            // Show any stragglers, if applicable.
                            debug.error(className + " Logic Issue, Unknown Embedded Key: " + currentEmbeddedKey + ", " +
                                    "InnerKey: " + key + ", " +
                                    "Type: " + contentMap.get(key).getClass().getName() + " " + contentMap.get(key));
                        }
                    } // End of Inner Else
                } // End of Outer Else
            } // End of outer for each loop.

        } else {
            debug.error(className + " Content not a Map Object: " + currentContent.getClass().getName() + ", " +
                    "" + currentContent);
            return false;
        }
        // ************************************
        // Return to indicate successful Parse
        xacmlRequestInformation.setParsedCorrectly(true);
        return true;
    }

    /**
     * Process Attributes for Request, will create entries in the PIP Resource Implementation Object.
     *
     * @param xacmlRequestInformation
     * @param currentCategory
     * @param attributes
     */
    private static void processAttributes(XACMLRequestInformation xacmlRequestInformation, String currentCategory,
                                          Object attributes) {
        // *********************************
        // Initialize our internal variables
        // for saving object data.
        String attributeId = null;
        boolean includeInResult = false;
        Object attributeValue = null;
        String dataType = null;

        // Obtain our Attributes Contents.
        if (attributes instanceof List) {
            for (Object attribute : (List) attributes) {
                if (attribute instanceof Map) {
                    Map<String, Object> attributeMap = (Map<String, Object>) attribute;
                    for (String attributeKey : attributeMap.keySet()) {

                        // Do we have a pending Attribute to write?
                        if ((attributeId != null) && (attributeValue != null)) {
                            xacmlRequestInformation.getPipResourceResolver().
                                    put(currentCategory, attributeId, dataType, attributeValue, includeInResult);
                            attributeId = null;
                            attributeValue = null;
                        }

                        // Check the Attribute Element Names...
                        if (removeNamespace(attributeKey).equalsIgnoreCase(ATTRIBUTE_CATEGORY)) {
                            currentCategory = new String((String) attributeMap.get(attributeKey));
                            continue;
                        } else if (removeNamespace(attributeKey).equalsIgnoreCase(ATTRIBUTE_INCLUDE_IN_RESULT)) {
                            includeInResult = ((Boolean) attributeMap.get(attributeKey)).booleanValue();
                            continue;
                        } else if (removeNamespace(attributeKey).equalsIgnoreCase(ATTRIBUTE_ID)) {
                            attributeId = (String) attributeMap.get(attributeKey);
                            continue;
                        } else if (removeNamespace(attributeKey).equalsIgnoreCase(ATTRIBUTE_VALUE)) {
                            if (attributeMap.get(attributeKey) instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) attributeMap.get(attributeKey);
                                for (String valueKey : valueMap.keySet()) {
                                    if (removeNamespace(valueKey).equalsIgnoreCase(ATTRIBUTE_VALUE_DATATYPE)) {
                                        dataType = (String) valueMap.get(valueKey);
                                    } else if (removeNamespace(valueKey).equalsIgnoreCase(ATTRIBUTE_VALUE_CONTENT)) {
                                        attributeValue = valueMap.get(valueKey);
                                    } else {
                                        // Show any stragglers, if applicable.
                                        debug.error(className + " Not handling Attribute Found: " + valueKey + ", " +
                                                attributes.getClass().getName() +
                                                ", should be a Map Object, Ignoring.");
                                    }
                                }
                                continue;
                            } else {
                                // Show any stragglers, if applicable.
                                debug.error(className + " No routine for handling Attribute Value Type Found: " +
                                        attributes.getClass().getName() +
                                        ", should be a Map Object, Ignoring.");
                                continue;
                            }
                        } else if (removeNamespace(attributeKey).equalsIgnoreCase(ATTRIBUTE)) {
                            processAttributes(xacmlRequestInformation, currentCategory, attributeMap.get(attributeKey));
                            continue;
                        } // End of Check for specific Element Names.

                        // Check for Inner List Object, if so, process Request Attributes...
                        if (attributeMap.get(attributeKey) instanceof List) {
                            processAttributes(xacmlRequestInformation, currentCategory,
                                    attributeMap.get(attributeKey));
                        } else if (xacmlRequestInformation.getContentType().commonType().equals(CommonType.JSON)) {
                            // Do we have a pending Attribute to write?
                            if ((attributeId != null) && (attributeValue != null)) {
                                xacmlRequestInformation.getPipResourceResolver().
                                        put(currentCategory, attributeId, dataType, attributeValue, includeInResult);
                                attributeId = null;
                                attributeValue = null;
                            }
                            // Determine the Inner Object for eventual Attribute Write to PIP.
                            if (attributeKey.equalsIgnoreCase(ATTRIBUTE_VALUE_DATATYPE)) {
                                dataType = (String) attributeMap.get(attributeKey);
                                continue;
                            } else if (attributeKey.equalsIgnoreCase(ATTRIBUTE_VALUE_VALUE)) {
                                attributeValue = attributeMap.get(attributeKey);
                                continue;
                            } else {
                                // Show any stragglers, if applicable.
                                debug.error(className + " Unknown Attribute Key: " + attributeKey + ", Attribute Type Found: " +
                                        attributeMap.get(attributeKey).getClass().getName() +
                                        ", for Category: " + currentCategory);
                            }
                        } else {
                            // Show any stragglers, if applicable.
                            debug.error(className + " Unknown Attribute Key: " + attributeKey + ", Attribute Type Found: " +
                                    attributeMap.get(attributeKey).getClass().getName() +
                                    ", should be a List Object, Ignoring.");
                        }

                    } // End Of Inner Map For Each loop.
                } else {
                    // Show any stragglers, if applicable.
                    debug.error(className + " No routine for handling Attribute Type Found: " +
                            attributes.getClass().getName() +
                            ", should be a Map Object, Ignoring.");
                }
            } // End of List For Each Loop.
        } else {
            // Show any stragglers, if applicable.
            debug.error(className + " Unknown Attributes Type Found: " +
                    attributes.getClass().getName() +
                    ", should be a List Object, Ignoring.");
        }
        // *****************************************************
        // Check if we have a final pending Attribute to write?
        if ((attributeId != null) && (attributeValue != null)) {
            xacmlRequestInformation.getPipResourceResolver().
                    put(currentCategory, attributeId, dataType, attributeValue, includeInResult);
        }
    }

    /**
     * Dump the Parsed Content Information.
     *
     * @param contentObject
     */
    private static void dumpContentInformation(Object contentObject, StringBuilder sb, int level) {
        if (contentObject == null) {
            return;
        }
        // Determine the Type of Object and Iterate Over the Contents Recursively...
        if (contentObject instanceof Map) {
            level++;
            // Cast our Object.
            Map<String, Object> contentMap = (Map<String, Object>) contentObject;
            // Iterate over the Key Set
            for (String key : contentMap.keySet()) {
                sb.append(levelToDots(level) + " Element: " + key + ", Content Type: " + contentMap.get(key)
                        .getClass()
                        .getName()
                        + "\n");
                dumpContentInformation(contentMap.get(key), sb, level);
            }
            level--;
        } else if (contentObject instanceof List) {
            // Cast our Object.
            level++;
            List<Object> contentList = (List<Object>) contentObject;
            for (Object innerObject : contentList) {
                dumpContentInformation(innerObject, sb, level);
            }
            level--;
        } else {
            sb.append(levelToDots(level) + " Content Value: " + contentObject.toString() + "\n");
        }
    }

    /**
     * Simple private helper method to prefix a content level...
     *
     * @param level
     * @return String - containing prefix for showing content level.
     */
    private static String levelToDots(int level) {
        StringBuilder levelPrefix = new StringBuilder();
        for (int i = 0; i < level; i++) {
            levelPrefix.append(".");
        }
        return levelPrefix.toString();
    }

    /**
     * Simple private helper method to strip out NameSpace from a Key Value.
     *
     * @param key
     * @return
     */
    private static final String removeNamespace(final String key) {
        if ((key != null) || (key.contains(":"))) {
            int x = key.lastIndexOf(":");
            if (x < key.length()) {
                return new String(key.substring(x + 1));
            }
        }
        return key;
    }

}
