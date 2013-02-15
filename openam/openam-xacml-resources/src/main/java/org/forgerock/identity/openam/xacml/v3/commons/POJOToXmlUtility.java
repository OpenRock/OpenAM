/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 ForgeRock US, Inc. All Rights Reserved
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


import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;

/**
 * XACML Response Object to XML Utility Class.
 * <p/>
 * Very simple Marshaller to place a XACML Response or any Object into a XML Object.
 *
 * @author Jeff.Schenk@forgerock.com
 */

public class POJOToXmlUtility {

    /**
     * POJO to XML String Data Object.
     *
     * @param object - Object to be Marshaled into a JSON Representation.
     * @return
     * @throws java.io.IOException
     */
    public static final String toString(Object object) throws IOException {
        XmlMapper mapper = new XmlMapper();
        return mapper.writeValueAsString(object);
    }


}
