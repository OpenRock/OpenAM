/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock US. All Rights Reserved
 ~
 ~ The contents of this file are subject to the terms
 ~ of the Common Development and Distribution License
 ~ (the License). You may not use this file except in
 ~ compliance with the License.
 ~
 ~ You can obtain a copy of the License at
 ~ http://forgerock.org/license/CDDLv1.0.html
 ~ See the License for the specific language governing
 ~ permission and limitations under the License.
 ~
 ~ When distributing Covered Code, include this CDDL
 ~ Header Notice in each file and include the License file
 ~ at http://forgerock.org/license/CDDLv1.0.html
 ~ If applicable, add the following below the CDDL Header,
 ~ with the fields enclosed by brackets [] replaced by
 ~ your own identifying information:
 ~ "Portions Copyrighted [year] [name of copyright owner]"
 *
 */
package org.forgerock.openam.xacml.v3.Functions;

import org.forgerock.openam.xacml.v3.model.DataValue;
import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;

import java.util.ArrayList;
import java.util.List;

/**
 * XACML Test Data Utility Tools.
 *
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlDataUtils {

    /**
     * Return DataValue as a List<DataValue> representing a StringBag.
     *
     * @param fArg
     * @return List<String>
     * @throws org.forgerock.openam.xacml.v3.model.XACML3EntitlementException
     */
    public static List<String> asStringCollection(FunctionArgument fArg) throws XACML3EntitlementException {
        // Loop to UnWrap the DataValues
        List<DataValue> bag = (List<DataValue>) fArg.getValue(null);   // Simple Cast for easy Manipulation.
        List<String> collection = new ArrayList<String>(bag.size());
        for (int i=0; i<bag.size(); i++) {
            // Cast and Add Object Element to Native Collection.
            collection.add( (String) bag.get(i).getValue(null) );
        }
        return collection;
    }
}
