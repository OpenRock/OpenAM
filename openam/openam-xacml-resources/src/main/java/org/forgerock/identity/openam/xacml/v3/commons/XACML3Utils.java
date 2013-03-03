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
package org.forgerock.identity.openam.xacml.v3.commons;

import com.sun.identity.saml2.common.SAML2Exception;
import com.sun.identity.saml2.common.SAML2Utils;

import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.locale.Locale;

import org.forgerock.identity.openam.xacml.v3.model.XACML3Constants;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * XACML
 * Various Utils to self contain and reduce dependency.
 * Some of these methods have been copied from SAML2 code
 *
 * @author jeff.schenk@forgerock.com
 * @see SAML2Utils
 */
public class XACML3Utils implements XACML3Constants {
    /**
     * Define our Static resource Bundle for our debugger.
     */
    private static Debug debug = Debug.getInstance("amXACML");

    //  XACML Resource bundle
    public static final String BUNDLE_NAME = "amXACML";
    // The resource bundle for XACML 3.0 implementation.
    public static ResourceBundle bundle = Locale.getInstallResourceBundle(BUNDLE_NAME);

    /**
     * Simple Helper Method to read in Resources as a Stream
     *
     * @param resourceName
     * @return String containing the Resource Contents or null if issue.
     */
    public static String getResourceContents(final String resourceName) {
        InputStream inputStream = null;
        try {
            if (resourceName != null) {
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
                return (inputStream != null) ? new Scanner(inputStream).useDelimiter("\\A").next() : null;
            }
        } catch (Exception e) {
            // Do nothing...
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // Do nothing...
                }
            }
        }
        // Catch All.
        return null;
    }

    /**
     * Simple Helper Method to read in Resources as a Stream
     *
     * @param resourceName
     * @return InputStream containing the Resource Contents or null if issue.
     */
    public static InputStream getResourceContentStream(final String resourceName) {
        try {
            if (resourceName != null) {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
            }
        } catch (Exception e) {
            // TODO
        }
        // Catch All.
        return null;
    }


    /**
     * Returns metaAlias embedded in uri.
     *
     * @param uri The uri string.
     * @return the metaAlias embedded in uri or null if not found.
     */
    public static String getMetaAliasByUri(String uri) {
        if (uri == null) {
            return null;
        }

        int index = uri.indexOf(NAME_META_ALIAS_IN_URI);
        if (index == -1 || index + 9 == uri.length()) {
            return null;
        }

        return uri.substring(index + 9);
    }

    /**
     * Returns the realm by parsing the metaAlias. MetaAlias format is
     * <pre>
     * &lt;realm>/&lt;any string without '/'> for non-root realm or
     * /&lt;any string without '/'> for root realm.
     * </pre>
     *
     * @param metaAlias The metaAlias.
     * @return the realm associated with the metaAlias.
     */
    public static String getRealmByMetaAlias(String metaAlias) {
        if (metaAlias == null) {
            return null;
        }

        int index = metaAlias.lastIndexOf("/");
        if (index == -1 || index == 0) {
            return "/";
        }

        return metaAlias.substring(0, index);
    }

    /**
     * Returns entity ID associated with the metaAlias.
     *
     * @param metaAlias The metaAlias.
     * @return entity ID associated with the metaAlias or null if not found.
     * @throws SAML2Exception if unable to retrieve the entity ids.
     */
    public static String getEntityByMetaAlias(String metaAlias)
            throws SAML2Exception {
        if (SAML2Utils.getSAML2MetaManager() == null) {
            return null;
        }
        return SAML2Utils.getSAML2MetaManager().getEntityByMetaAlias(metaAlias);
    }

    /**
     * Simple Helper Method to read File in as a Stream
     *
     * @param resourceFileName -- File Name of Contents to be consumed as a String.
     * @return String containing the Resource Contents or null if issue.
     */
    public static String getFileContents(final String resourceFileName) {
        InputStream inputStream = null;
        try {
            if ( (resourceFileName == null) || (resourceFileName.isEmpty()) ) {
              return null;
            }
            File file = new File(resourceFileName);
            if ( (file.exists()) && (file.canRead()) )
            {
                inputStream = new FileInputStream(resourceFileName);
                return (inputStream != null) ? new Scanner(inputStream).useDelimiter("\\A").next() : null;
            }
        } catch (Exception e) {
            // Do Nothing...
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // Do nothing...
                }
            }
        }
        // Catch All.
        return null;
    }

    /**
     * Return the Request Body Content.
     *
     * @param request
     * @return String - Request Content Body.
     */
    public static String getRequestBody(final HttpServletRequest request) {
        // Get the body content of the HTTP request,
        // remember we have no normal WS* SOAP Body, just String
        // data either XML or JSON.
        InputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (IOException ioe) {
            // Do Nothing...
        } catch (NoSuchElementException nse) {   // runtime exception.
            //Do Nothing...
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    // Do nothing...
                }
            }
        }
        return null;
    }

}
