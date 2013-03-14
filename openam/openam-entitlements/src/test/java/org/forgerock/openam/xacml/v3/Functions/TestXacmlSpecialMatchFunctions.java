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
 *  A.3.14 Special match functions
 These functions operate on various types and evaluate to “http://www.w3.org/2001/XMLSchema#boolean” based on the specified standard matching algorithm.

 urn:oasis:names:tc:xacml:1.0:function:x500Name-match
 This function shall take two arguments of "urn:oasis:names:tc:xacml:1.0:data-type:x500Name" and shall return an "http://www.w3.org/2001/XMLSchema#boolean".  It shall return “True” if and only if the first argument matches some terminal sequence of RDNs from the second argument when compared using x500Name-equal.
 As an example (non-normative), if the first argument is “O=Medico Corp,C=US” and the second argument is “cn=John Smith,o=Medico Corp, c=US”, then the function will return “True”.

 urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match
 This function SHALL take two arguments, the first is of data-type “http://www.w3.org/2001/XMLSchema#string” and the second is of data-type “urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  This function SHALL evaluate to "True" if the first argument matches the second argument according to the following specification.
 An RFC822 name consists of a local-part followed by "@" followed by a domain-part.  The local-part is case-sensitive, while the domain-part (which is usually a DNS name) is not case-sensitive.
 The second argument contains a complete rfc822Name.  The first argument is a complete or partial rfc822Name used to select appropriate values in the second argument as follows.
 In order to match a particular address in the second argument, the first argument must specify the complete mail address to be matched.  For example, if the first argument is “Anderson@sun.com”, this matches a value in the second argument of “Anderson@sun.com” and “Anderson@SUN.COM”, but not “Anne.Anderson@sun.com”, “anderson@sun.com” or “Anderson@east.sun.com”.
 In order to match any address at a particular domain in the second argument, the first argument must specify only a domain name (usually a DNS name).  For example, if the first argument is “sun.com”, this matches a value in the second argument of “Anderson@sun.com” or “Baxter@SUN.COM”, but not “Anderson@east.sun.com”.
 In order to match any address in a particular domain in the second argument, the first argument must specify the desired domain-part with a leading ".".  For example, if the first argument is “.east.sun.com”, this matches a value in the second argument of "Anderson@east.sun.com" and "anne.anderson@ISRG.EAST.SUN.COM" but not "Anderson@sun.com".

 */

/**
 * XACML Special Match Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlSpecialMatchFunctions {

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
