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

import org.forgerock.openam.xacml.v3.model.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * A.3.9 String functions Group 2 Continued...
 The following functions operate on strings and convert to and from other data types.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return a "http://www.w3.org/2001/XMLSchema#anyURI".
 The result SHALL be the URI constructed by converting the argument to an URI.
 If the argument is not a valid lexical representation of a URI,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#anyURI",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the URI converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "urn:oasis:names:tc:xacml:1.0:data-type:x500Name".
 The result SHALL be the string converted to an x500Name.
 If the argument is not a valid lexical representation of a X500Name, then the result SHALL be
 Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:1.0:data-type:x500Name",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the x500Name converted to a string in the form it was originally represented in XML form..

 urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name".
 The result SHALL be the string converted to an rfc822Name.
 If the argument is not a valid lexical representation of an rfc822Name,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the rfc822Name converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL
 return an "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress".
 The result SHALL be the string converted to an ipAddress.
 If the argument is not a valid lexical representation of an ipAddress,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the ipAddress converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string",
 and SHALL return an "urn:oasis:names:tc:xacml:2.0:data-type:dnsName".
 The result SHALL be the string converted to a dnsName.
 If the argument is not a valid lexical representation of a dnsName,
 then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:2.0:data-type:dnsName",
 and SHALL return an "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the dnsName converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
 This function SHALL take a first argument of data-type"http://www.w3.org/2001/XMLSchema#string"
 and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI"
 and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".
 The result SHALL be true if the URI converted to a string with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI begins with the string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string"
 and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a
 "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a string with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI ends with the string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string" and an a
 second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a
 "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a
 string with urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI contains the string, and false otherwise.
 Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and a
 second and a third argument of type "http://www.w3.org/2001/XMLSchema#integer" and
 SHALL return a "http://www.w3.org/2001/XMLSchema#string".
 The result SHALL be the substring of the first argument converted to a string with
 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI beginning at the position given by the second argument
 and ending at the position before the position given by the third argument.
 The first character of the URI converted to a string has position zero.
 The negative integer value -1 given for the third arguments indicates the end of the string.
 If the second or third arguments are out of bounds, then the function MUST evaluate to Indeterminate
 with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error.
 If the resulting substring is not syntactically a valid URI, then the function MUST evaluate to
 Indeterminate with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error.

 */

/**
 * XACML String Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlStringFunctionsGroup2 {

    static final FunctionArgument trueObject = new DataValue(DataType.XACMLBOOLEAN, "true");
    static final FunctionArgument falseObject = new DataValue(DataType.XACMLBOOLEAN, "false");

    static final String anyuriString1 = "/openam/xacml";
    static final FunctionArgument anyuriString1D = new DataValue(DataType.XACMLSTRING, anyuriString1);
    static final FunctionArgument anyuri1 = new DataValue(DataType.XACMLANYURI, anyuriString1);

    static final FunctionArgument anyuri2 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");
    static final FunctionArgument anyuri3 = new DataValue(DataType.XACMLANYURI, "/");
    static final FunctionArgument anyuri4 = new DataValue(DataType.XACMLANYURI, "/a/b/c/e/f");

    // base64data1 and base64data2 contained the Base 64 encoding of:
    // ForgeRock - OpenAM XACML says Hello!
    static final FunctionArgument base64data1 = new DataValue(DataType.XACMLBASE64BINARY,
            "Rm9yZ2VSb2NrIC0gT3BlbkFNIFhBQ01MIHNheXMgSGVsbG8h");
    // This is a very small Test!
    static final FunctionArgument base64data2 = new DataValue(DataType.XACMLBASE64BINARY,
            "VGhpcyBpcyBhIHZlcnkgc21hbGwgVGVzdCE=");
    // This is a very small Test as well!
    static final FunctionArgument base64data3 = new DataValue(DataType.XACMLBASE64BINARY,
            "VGhpcyBpcyBhIHZlcnkgc21hbGwgVGVzdCBhcyB3ZWxsIQ==");
    // ForgeRock - OpenAM XACML says Hello!
    static final FunctionArgument base64data4 = new DataValue(DataType.XACMLBASE64BINARY,
            "Rm9yZ2VSb2NrIC0gT3BlbkFNIFhBQ01MIHNheXMgSGVsbG8h");

    static final FunctionArgument hexdata1 = new DataValue(DataType.XACMLHEXBINARY, "0123456789abcdef");
    static final FunctionArgument hexdata2 = new DataValue(DataType.XACMLHEXBINARY, "FF");
    static final FunctionArgument hexdata3 = new DataValue(DataType.XACMLHEXBINARY, "0123456789ABCDEF");
    static final FunctionArgument hexdata4 = new DataValue(DataType.XACMLHEXBINARY, "06F2");

    static final FunctionArgument rfc822Name1 = new DataValue(DataType.XACMLX500NAME,
            "joe@example.org");
    static final FunctionArgument rfc822Name2 = new DataValue(DataType.XACMLX500NAME,
            "joe.smith@example.org");
    static final FunctionArgument rfc822Name3 = new DataValue(DataType.XACMLX500NAME,
            "joe.smith@example.org");
    static final FunctionArgument rfc822Name4 = new DataValue(DataType.XACMLX500NAME,
            "joe.smith@ExAmPlE.oRg");

    static final FunctionArgument x500Name1 = new DataValue(DataType.XACMLX500NAME,
            "/c=us/o=ForgeRock/ou=Components/cn=OpenAM");
    static final FunctionArgument x500Name2 = new DataValue(DataType.XACMLX500NAME,
            "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");
    static final FunctionArgument x500Name3 = new DataValue(DataType.XACMLX500NAME,
            "/cn=Bob Smith");
    static final FunctionArgument x500Name4 = new DataValue(DataType.XACMLX500NAME,
            "/c=us/o=ForgeRock/ou=People/cn=Bob Smith");


    @BeforeClass
    public void before() throws Exception {
    }

    @AfterClass
    public void after() throws Exception {
    }


    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
     */
    @Test
    public void testAnyURIFromString() throws XACML3EntitlementException {
        AnyuriFromString function = new AnyuriFromString();
        function.addArgument(anyuriString1D);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asAnyURI(null), anyuri1.asAnyURI(null));
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
     */
    @Test
    public void testStringFromAnyURI() throws XACML3EntitlementException {
        StringFromAnyURI function = new StringFromAnyURI();
        function.addArgument(anyuri1);
        FunctionArgument result = function.evaluate(null);
        assertNotNull(result);
        assertEquals(result.asString(null), anyuriString1);
    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
     */
    @Test
    public void testX500NameFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
     */
    @Test
    public void testStringFromx500Name() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
     */
    @Test
    public void testRfc822NameFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
     */
    @Test
    public void testStringFromrfc822Name() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
     */
    @Test
    public void testIpAddressFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
     */
    @Test
    public void testStringFromipAddress() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
     */
    @Test
    public void testDnsNameFromString() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
     */
    @Test
    public void testStringFromdnsName() throws XACML3EntitlementException {

    }

    // String Starts and Ends Comparisons

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
     */
    @Test
    public void testAnyURIStartsWith() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
     */
    @Test
    public void testAnyURIendswith() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
     */
    @Test
    public void testAnyURIcontains() throws XACML3EntitlementException {

    }

    /**
     * urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
     */
    @Test
    public void testAnyURIsubstring() throws XACML3EntitlementException {

    }


}
