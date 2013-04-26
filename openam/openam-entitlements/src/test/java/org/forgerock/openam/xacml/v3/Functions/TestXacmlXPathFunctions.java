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

import org.forgerock.openam.xacml.v3.model.DataType;
import org.forgerock.openam.xacml.v3.model.DataValue;
import org.forgerock.openam.xacml.v3.model.FunctionArgument;
import org.forgerock.openam.xacml.v3.model.XACML3EntitlementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A.3.15 XPath-based functions
 This section specifies functions that take XPath expressions for arguments.  An XPath expression evaluates to a node-set, which is a set of XML nodes that match the expression.  A node or node-set is not in the formal data-type system of XACML.  All comparison or other operations on node-sets are performed in isolation of the particular function specified.  The context nodes and namespace mappings of the XPath expressions are defined by the XPath data-type, see section B.3.  The following functions are defined:

 urn:oasis:names:tc:xacml:3.0:function:xpath-node-count
 This function SHALL take an “urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression” as an argument and evaluates to an “http://www.w3.org/2001/XMLSchema#integer”.  The value returned from the function SHALL be the count of the nodes within the node-set that match the given XPath expression. If the <Content> element of the category to which the XPath expression applies to is not present in the request, this function SHALL return a value of zero.

 urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal
 This function SHALL take two “urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression” arguments and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  The function SHALL return "True" if any of the XML nodes in the node-set matched by the first argument equals any of the XML nodes in the node-set matched by the second argument. Two nodes are considered equal if they have the same identity. If the <Content> element of the category to which either XPath expression applies to is not present in the request, this function SHALL return a value of “False”.

 urn:oasis:names:tc:xacml:3.0:function:xpath-node-match
 This function SHALL take two “urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression” arguments and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”. This function SHALL evaluate to "True" if one of the following two conditions is satisfied: (1) Any of the XML nodes in the node-set matched by the first argument is equal to any of the XML nodes in the node-set matched by the second argument; (2) any node below any of the XML nodes in the node-set matched by the first argument is equal to any of the XML nodes in the node-set matched by the second argument. Two nodes are considered equal if they have the same identity. If the <Content> element of the category to which either XPath expression applies to is not present in the request, this function SHALL return a value of “False”.
 NOTE: The first condition is equivalent to "xpath-node-equal", and guarantees that "xpath-node-equal"
 is a special case of "xpath-node-match".

 */

/**
 * XACML XPath Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * X500Name
 */
public class TestXacmlXPathFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:xpath-node-count
     */
    @Test(enabled = false)
    @Deprecated
    public void testXPathNodeCount() throws XACML3EntitlementException {
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:xpath-node-equal
     */
    @Test(enabled = false)
    @Deprecated
    public void testXPathNodeEqual() throws XACML3EntitlementException {
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:xpath-node-match
     */
    @Test(enabled = false)
    @Deprecated
    public void testXPathNodeMatch() throws XACML3EntitlementException {
    }

}
