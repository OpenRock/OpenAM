/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2012 ForgeRock AS. All rights reserved.
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
 * $Id$
 */
package org.forgerock.restlet.ext.oauth2.flow;

import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class ErrorServerResource extends AbstractFlow {

    /**
     * Effectively handles a call without content negotiation of the response
     * entity. The default behavior is to dispatch the call to one of the
     * {@link #get()}, {@link #post(org.restlet.representation.Representation)},
     * {@link #put(org.restlet.representation.Representation)}, {@link #delete()}, {@link #head()} or
     * {@link #options()} methods.
     *
     * @return The response entity.
     * @throws org.restlet.resource.ResourceException
     *
     */
    protected Representation doHandle() throws ResourceException {
        Representation result = null;
        OAuthProblemException e = OAuthProblemException.popException(getRequest());
        result = getPage("templates/error.html", null != e ? e.getErrorMessage() : getRequest().getAttributes());
        if (null == result) {
            result = new StringRepresentation("Error");
        }
        return result;
    }

    protected Representation doConditionalHandle() throws ResourceException {
        return doHandle();
    }
}
