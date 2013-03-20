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

import static org.testng.Assert.*;

/**
 * A.3.9 String functions
 The following functions operate on strings and convert to and from other data types.

 urn:oasis:names:tc:xacml:2.0:function:string-concatenate
 This function SHALL take two or more arguments of data-type "http://www.w3.org/2001/XMLSchema#string" and SHALL return a "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the concatenation, in order, of the arguments.

 urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be the string converted to a boolean. If the argument is not a valid lexical representation of a boolean, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-boolean
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#boolean", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the boolean converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:integer-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#integer".  The result SHALL be the string converted to an integer. If the argument is not a valid lexical representation of an integer, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-integer
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#integer", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the integer converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:double-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#double".  The result SHALL be the string converted to a double. If the argument is not a valid lexical representation of a double, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-double
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#double", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the double converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:time-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#time".  The result SHALL be the string converted to a time. If the argument is not a valid lexical representation of a time, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-time
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#time", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the time converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:date-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#date".  The result SHALL be the string converted to a date. If the argument is not a valid lexical representation of a date, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-date
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#date", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the date converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#dateTime".  The result SHALL be the string converted to a dateTime. If the argument is not a valid lexical representation of a dateTime, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#dateTime", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the dateTime converted to a string in the canonical form specified in [XS].

 urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return a "http://www.w3.org/2001/XMLSchema#anyURI".  The result SHALL be the URI constructed by converting the argument to an URI. If the argument is not a valid lexical representation of a URI, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
 This function SHALL take one argument of data-type  "http://www.w3.org/2001/XMLSchema#anyURI", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the URI converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#dayTimeDuration ".  The result SHALL be the string converted to a dayTimeDuration. If the argument is not a valid lexical representation of a dayTimeDuration, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#dayTimeDuration ", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the dayTimeDuration converted to a string in the canonical form specified in [XPathFunc].

 urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "http://www.w3.org/2001/XMLSchema#yearMonthDuration".  The result SHALL be the string converted to a yearMonthDuration. If the argument is not a valid lexical representation of a yearMonthDuration, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#yearMonthDuration", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the yearMonthDuration converted to a string in the canonical form specified in [XPathFunc].

 urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "urn:oasis:names:tc:xacml:1.0:data-type:x500Name".  The result SHALL be the string converted to an x500Name. If the argument is not a valid lexical representation of a X500Name, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:1.0:data-type:x500Name", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the x500Name converted to a string in the form it was originally represented in XML form..

 urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name".  The result SHALL be the string converted to an rfc822Name. If the argument is not a valid lexical representation of an rfc822Name, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the rfc822Name converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress".  The result SHALL be the string converted to an ipAddress. If the argument is not a valid lexical representation of an ipAddress, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the ipAddress converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
 This function SHALL take one argument of data-type "http://www.w3.org/2001/XMLSchema#string", and SHALL return an "urn:oasis:names:tc:xacml:2.0:data-type:dnsName".  The result SHALL be the string converted to a dnsName. If the argument is not a valid lexical representation of a dnsName, then the result SHALL be Indeterminate with status code urn:oasis:names:tc:xacml:1.0:status:syntax-error.

 urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
 This function SHALL take one argument of data-type "urn:oasis:names:tc:xacml:2.0:data-type:dnsName", and SHALL return an "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the dnsName converted to a string in the form it was originally represented in XML form.

 urn:oasis:names:tc:xacml:3.0:function:string-starts-with
 This function SHALL take two arguments of data-type "http://www.w3.org/2001/XMLSchema#string" and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the second string begins with the first string, and false otherwise. Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
 This function SHALL take a first argument of data-type"http://www.w3.org/2001/XMLSchema#string"  and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a string with urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI begins with the string, and false otherwise. Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:string-ends-with
 This function SHALL take two arguments of data-type "http://www.w3.org/2001/XMLSchema#string" and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the second string ends with the first string, and false otherwise. Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string" and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a string with urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI ends with the string, and false otherwise. Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:string-contains
 This function SHALL take two arguments of data-type "http://www.w3.org/2001/XMLSchema#string" and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the second string contains the first string, and false otherwise. Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string" and an a second argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and SHALL return a "http://www.w3.org/2001/XMLSchema#boolean".  The result SHALL be true if the URI converted to a string with urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI contains the string, and false otherwise. Equality testing SHALL be done as defined for urn:oasis:names:tc:xacml:1.0:function:string-equal.

 urn:oasis:names:tc:xacml:3.0:function:string-substring
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#string" and a second and a third argument of type "http://www.w3.org/2001/XMLSchema#integer" and SHALL return a "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the substring of the first argument beginning at the position given by the second argument and ending at the position before the position given by the third argument. The first character of the string has position zero. The negative integer value -1 given for the third arguments indicates the end of the string. If the second or third arguments are out of bounds, then the function MUST evaluate to Indeterminate with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error.

 urn:oasis:names:tc:xacml:3.0:function:anyURI-substring
 This function SHALL take a first argument of data-type "http://www.w3.org/2001/XMLSchema#anyURI" and a second and a third argument of type "http://www.w3.org/2001/XMLSchema#integer" and SHALL return a "http://www.w3.org/2001/XMLSchema#string".  The result SHALL be the substring of the first argument converted to a string with urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI beginning at the position given by the second argument and ending at the position before the position given by the third argument. The first character of the URI converted to a string has position zero. The negative integer value -1 given for the third arguments indicates the end of the string. If the second or third arguments are out of bounds, then the function MUST evaluate to Indeterminate with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error. If the resulting substring is not syntactically a valid URI, then the function MUST evaluate to Indeterminate with a status code of urn:oasis:names:tc:xacml:1.0:status:processing-error.

 */

/**
 * XACML String Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlStringFunctions {

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

    /**
     *

      urn:oasis:names:tc:xacml:2.0:function:string-concatenate
      urn:oasis:names:tc:xacml:3.0:function:boolean-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-boolean
      urn:oasis:names:tc:xacml:3.0:function:integer-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-integer
      urn:oasis:names:tc:xacml:3.0:function:double-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-double
      urn:oasis:names:tc:xacml:3.0:function:time-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-time
      urn:oasis:names:tc:xacml:3.0:function:date-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-date
      urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime
      urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI
      urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration
      urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration
      urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
      urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name
      urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress
      urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string
      urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
      urn:oasis:names:tc:xacml:3.0:function:string-starts-with
      urn:oasis:names:tc:xacml:3.0:function:anyURI-starts-with
      urn:oasis:names:tc:xacml:3.0:function:string-ends-with
      urn:oasis:names:tc:xacml:3.0:function:anyURI-ends-with
      urn:oasis:names:tc:xacml:3.0:function:string-contains
      urn:oasis:names:tc:xacml:3.0:function:anyURI-contains
      urn:oasis:names:tc:xacml:3.0:function:string-substring
      urn:oasis:names:tc:xacml:3.0:function:anyURI-substring

     */


}
