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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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

        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING, "Hello World!");
        FunctionArgument pattern1 = new DataValue(DataType.XACMLSTRING, ".ello\\b");

        FunctionArgument string2 = new DataValue(DataType.XACMLSTRING, "dog dog dog doggie dogg");
        FunctionArgument pattern2 = new DataValue(DataType.XACMLSTRING, "\\bdog\\b");

        FunctionArgument string3 = new DataValue(DataType.XACMLSTRING, "This order was places for QT3000! OK?");
        FunctionArgument pattern3 = new DataValue(DataType.XACMLSTRING, "(.*)(\\d+)(.*)");


        StringRegexpMatch regexMatch = new StringRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern1);
        regexMatch.addArgument(string1);
        FunctionArgument result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new StringRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern2);
        regexMatch.addArgument(string2);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new StringRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern3);
        regexMatch.addArgument(string3);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new StringRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern3);
        regexMatch.addArgument(string1);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());
    }

    /**
     *  urn:oasis:names:tc:xacml:1.0:function:string-regexp-match
     */
    @Test(expectedExceptions = {XACML3EntitlementException.class})
    public void testStringRegularExpressionMatch_EXCEPTION() throws XACML3EntitlementException {

        FunctionArgument string1 = new DataValue(DataType.XACMLSTRING, "Hello World!");
        FunctionArgument pattern1 = new DataValue(DataType.XACMLSTRING, "\\\\\\Hello");   // Bad Pattern.

        StringRegexpMatch regexMatch = new StringRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern1);
        regexMatch.addArgument(string1);
        regexMatch.evaluate(null);
        // Should never hit here....
        assertTrue(false,"Should never had reached this code point, very bad!");

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match
     */
    @Test
    public void testAnyUriRegularExpressionMatch() throws XACML3EntitlementException {
        FunctionArgument anyuri1 = new DataValue(DataType.XACMLANYURI, "/openam/xacml");
        FunctionArgument pattern1 = new DataValue(DataType.XACMLSTRING, "\\/openam");

        FunctionArgument anyuri2 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");
        FunctionArgument pattern2 = new DataValue(DataType.XACMLSTRING, "\\^/b");

        FunctionArgument anyuri3 = new DataValue(DataType.XACMLANYURI, "/");
        FunctionArgument pattern3 = new DataValue(DataType.XACMLSTRING, "^/");


        AnyuriRegexpMatch regexMatch = new AnyuriRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern1);
        regexMatch.addArgument(anyuri1);
        FunctionArgument result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new AnyuriRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern2);
        regexMatch.addArgument(anyuri2);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

        regexMatch = new AnyuriRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern3);
        regexMatch.addArgument(anyuri3);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match
     */
    @Test
    public void testIpAddressRegularExpressionMatch() throws XACML3EntitlementException {

        final String IPv4Pattern =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        final String IPv6Pattern = "^\\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}"
                + "(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
                + "(.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3})|:))|(([0-9A-Fa-f]{1,4}:){5}"
                + "(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
                + "(.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3})|:))|(([0-9A-Fa-f]{1,4}:){4}"
                + "(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
                + "(.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}"
                + "(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
                + "(.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}"
                + "(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
                + "(.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}"
                + "(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
                + "(.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})"
                + "|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])"
                + "(.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])){3}))|:)))(%.+)?\\s*$";


        FunctionArgument anyIP4 = new DataValue(DataType.XACMLIPADDRESS, "10.0.12.5");
        FunctionArgument pattern4 = new DataValue(DataType.XACMLSTRING, IPv4Pattern);
        FunctionArgument anyIP6 = new DataValue(DataType.XACMLIPADDRESS, "fdff:cafe:babe:cab1:426c:8fff:fe2c:1e65");
        FunctionArgument pattern6 = new DataValue(DataType.XACMLSTRING, IPv6Pattern);

        IpaddressRegexpMatch regexMatch = new IpaddressRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern4);
        regexMatch.addArgument(anyIP4);
        FunctionArgument result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new IpaddressRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern6);
        regexMatch.addArgument(anyIP6);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match
     */
    @Test
    public void testDnsNameRegularExpressionMatch() throws XACML3EntitlementException {
        FunctionArgument anyDNSName1 = new DataValue(DataType.XACMLDNSNAME, "example.org");
        FunctionArgument pattern1 = new DataValue(DataType.XACMLSTRING, "^example\\.org");
        FunctionArgument anyDNSName2 = new DataValue(DataType.XACMLDNSNAME, "www.example.org");
        FunctionArgument pattern2 = new DataValue(DataType.XACMLSTRING, "^example\\.org");

        DnsnameRegexpMatch regexMatch = new DnsnameRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern1);
        regexMatch.addArgument(anyDNSName1);
        FunctionArgument result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new DnsnameRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern2);
        regexMatch.addArgument(anyDNSName2);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());
    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match
     */
    @Test
    public void testRfc822NameRegularExpressionMatch() throws XACML3EntitlementException {
        FunctionArgument rfc822Name1 = new DataValue(DataType.XACMLRFC822NAME,
                "joe@example.org");
        FunctionArgument pattern1 = new DataValue(DataType.XACMLSTRING,
                "^joe@example.org");
        FunctionArgument rfc822Name2 = new DataValue(DataType.XACMLRFC822NAME,
                "joe.smith@example.org");
        FunctionArgument pattern2 = new DataValue(DataType.XACMLSTRING,
                "[.]+@example.org");

        Rfc822NameRegexpMatch regexMatch = new Rfc822NameRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern1);
        regexMatch.addArgument(rfc822Name1);
        FunctionArgument result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new Rfc822NameRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern2);
        regexMatch.addArgument(rfc822Name2);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());
    }

    /**
     *  urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match
     */
    @Test
    public void testX500NameRegularExpressionMatch() throws XACML3EntitlementException {
        FunctionArgument x500Name1 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=Components/cn=OpenAM");
        FunctionArgument pattern1 = new DataValue(DataType.XACMLSTRING,
                "^/c=us*");
        FunctionArgument x500Name2 = new DataValue(DataType.XACMLX500NAME,
                "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");
        FunctionArgument pattern2 = new DataValue(DataType.XACMLSTRING,
                "/ou=[Pp]eople");

        X500NameRegexpMatch regexMatch = new X500NameRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern1);
        regexMatch.addArgument(x500Name1);
        FunctionArgument result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isTrue());

        regexMatch = new X500NameRegexpMatch();
        // Place Objects in Argument stack for comparison.
        regexMatch.addArgument(pattern2);
        regexMatch.addArgument(x500Name2);
        result = regexMatch.evaluate(null);
        assertNotNull(result);
        assertTrue(result.isFalse());

    }

}
