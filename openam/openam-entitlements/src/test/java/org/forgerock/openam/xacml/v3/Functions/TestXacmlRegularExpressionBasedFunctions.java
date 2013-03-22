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
 * A.3.13 Regular-expression-based functions
 These functions operate on various types using regular expressions and evaluate to
 “http://www.w3.org/2001/XMLSchema#boolean”.

 urn:oasis:names:tc:xacml:1.0:function:string-regexp-match
 This function decides a regular expression match.  It SHALL take two arguments of
 “http://www.w3.org/2001/XMLSchema#string” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The first argument SHALL be a regular expression and the second argument SHALL be a general string.
 The function specification SHALL be that of the “xf:matches” function with the arguments reversed [XF] Section 7.6.2.

 urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
 This function decides a regular expression match.  It SHALL take two arguments; the first is of type
 “http://www.w3.org/2001/XMLSchema#string” and the second is of type “http://www.w3.org/2001/XMLSchema#anyURI”.
 It SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  The first argument SHALL be a
 regular expression and the second argument SHALL be a URI.
 The function SHALL convert the second argument to type “http://www.w3.org/2001/XMLSchema#string” with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI,
 then apply “urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”.

 urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
 This function decides a regular expression match.
 It SHALL take two arguments; the first is of type “http://www.w3.org/2001/XMLSchema#string” and the
 second is of type “urn:oasis:names:tc:xacml:2.0:data-type:ipAddress”.
 It SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The first argument SHALL be a regular expression and the second argument SHALL be an IPv4 or IPv6 address.
 The function SHALL convert the second argument to type “http://www.w3.org/2001/XMLSchema#string”
 with urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress, then apply
 “urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”.

 urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
 This function decides a regular expression match.  It SHALL take two arguments; the first is of type
 “http://www.w3.org/2001/XMLSchema#string” and the second is of type “urn:oasis:names:tc:xacml:2.0:data-type:dnsName”.
 It SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  The first argument SHALL be a regular expression
 and the second argument SHALL be a DNS name.  The function SHALL convert the second argument to
 type “http://www.w3.org/2001/XMLSchema#string” with urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName,
 then apply “urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”.

 urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
 This function decides a regular expression match.  It SHALL take two arguments; the first is of type
 “http://www.w3.org/2001/XMLSchema#string” and the second is of type
 “urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name”.  It SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The first argument SHALL be a regular expression and the second argument SHALL be an RFC 822 name.
 The function SHALL convert the second argument to type “http://www.w3.org/2001/XMLSchema#string”
 with urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name,
 then apply “urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”.

 urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
 This function decides a regular expression match.
 It SHALL take two arguments; the first is of type “http://www.w3.org/2001/XMLSchema#string”
 and the second is of type “urn:oasis:names:tc:xacml:1.0:data-type:x500Name”.
 It SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 The first argument SHALL be a regular expression and the second argument SHALL be an X.500 directory name.
 The function SHALL convert the second argument to type “http://www.w3.org/2001/XMLSchema#string”
 with urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name,
 then apply “urn:oasis:names:tc:xacml:1.0:function:string-regexp-match”.

 */

/**
 * XACML Reqular Expression Based Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlRegularExpressionBasedFunctions {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:string-regexp-match
     */
    @Test
    public void testStringRegularExpressionMatch() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
     */
    @Test
    public void testAnyUriRegularExpressionMatch() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
     */
    @Test
    public void testIpAddressRegularExpressionMatch() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
     */
    @Test
    public void testDnsNameRegularExpressionMatch() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
     */
    @Test
    public void testRfc822NameRegularExpressionMatch() throws XACML3EntitlementException {

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
     */
    @Test
    public void testX500NameRegularExpressionMatch() throws XACML3EntitlementException {

    }

}
