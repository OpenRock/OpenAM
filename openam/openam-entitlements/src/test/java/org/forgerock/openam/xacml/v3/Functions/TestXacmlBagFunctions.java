/**
 *
 ~ DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 ~
 ~ Copyright (c) 2011-2013 ForgeRock AS. All Rights Reserved
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

import org.forgerock.openam.xacml.v3.Entitlements.DataType;
import org.forgerock.openam.xacml.v3.Entitlements.DataValue;
import org.forgerock.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.openam.xacml.v3.Entitlements.XACML3EntitlementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A.3.10 Bag functions
 These functions operate on a bag of ‘type’ values, where type is one of the primitive data-types, and x.x is a version of XACML where the function has been defined.    Some additional conditions defined for each function below SHALL cause the expression to evaluate to "Indeterminate".

 urn:oasis:names:tc:xacml:x.x:function:type-one-and-only
 This function SHALL take a bag of ‘type’ values as an argument and SHALL return a value of ‘type’.  It SHALL return the only value in the bag.  If the bag does not have one and only one value, then the expression SHALL evaluate to "Indeterminate".

 urn:oasis:names:tc:xacml:x.x:function:type-bag-size
 This function SHALL take a bag of ‘type’ values as an argument and SHALL return an “http://www.w3.org/2001/XMLSchema#integer” indicating the number of values in the bag.

 urn:oasis:names:tc:xacml:x.x:function:type-is-in
 This function SHALL take an argument of ‘type’ as the first argument and a bag of ‘type’ values as the second argument and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  The function SHALL evaluate to "True" if and only if the first argument matches by the "urn:oasis:names:tc:xacml:x.x:function:type-equal" any value in the bag.  Otherwise, it SHALL return “False”.

 urn:oasis:names:tc:xacml:x.x:function:type-bag
 This function SHALL take any number of arguments of ‘type’ and return a bag of ‘type’ values containing the values of the arguments.  An application of this function to zero arguments SHALL produce an empty bag of the specified data-type.
 */

/**
 * XACML Bag Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlBagFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     *
     */
    @Test
    public void testOne() throws XACML3EntitlementException {

    }

}
