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
 * A.3.8 Non-numeric comparison functions
 These functions perform comparison operations on two arguments of non-numerical types.

 urn:oasis:names:tc:xacml:1.0:function:string-greater-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#string”
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 It SHALL return "True" if and only if the first argument is lexicographically strictly greater than the second
 argument.  Otherwise, it SHALL return “False”.
 The comparison SHALL use Unicode codepoint collation, as defined for the identifier
 http://www.w3.org/2005/xpath-functions/collation/codepoint by [XF].

 urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#string”
 and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.
 It SHALL return "True" if and only if the first argument is lexicographically greater than or equal to the second
 argument.  Otherwise, it SHALL return “False”.
 The comparison SHALL use Unicode codepoint collation, as defined for the identifier
 http://www.w3.org/2005/xpath-functions/collation/codepoint by [XF].

 urn:oasis:names:tc:xacml:1.0:function:string-less-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#string” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only the first argument is lexigraphically strictly less than the second argument.  Otherwise, it SHALL return “False”. The comparison SHALL use Unicode codepoint collation, as defined for the identifier http://www.w3.org/2005/xpath-functions/collation/codepoint by [XF].

 urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#string” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only the first argument is lexigraphically less than or equal to the second argument.  Otherwise, it SHALL return “False”. The comparison SHALL use Unicode codepoint collation, as defined for the identifier http://www.w3.org/2005/xpath-functions/collation/codepoint by [XF].

 urn:oasis:names:tc:xacml:1.0:function:time-greater-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#time” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is greater than the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#time” [XS] Section 3.2.8.  Otherwise, it SHALL return “False”.  Note: it is illegal to compare a time that includes a time-zone value with one that does not.  In such cases, the time-in-range function should be used.

 urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#time” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is greater than or equal to the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#time” [XS] Section 3.2.8.  Otherwise, it SHALL return “False”.  Note: it is illegal to compare a time that includes a time-zone value with one that does not.  In such cases, the time-in-range function should be used.

 urn:oasis:names:tc:xacml:1.0:function:time-less-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#time” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is less than the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#time” [XS] Section 3.2.8.  Otherwise, it SHALL return “False”.  Note: it is illegal to compare a time that includes a time-zone value with one that does not.  In such cases, the time-in-range function should be used.

 urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#time” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is less than or equal to the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#time” [XS] Section 3.2.8.  Otherwise, it SHALL return “False”.  Note: it is illegal to compare a time that includes a time-zone value with one that does not.  In such cases, the time-in-range function should be used.

 urn:oasis:names:tc:xacml:2.0:function:time-in-range
 This function SHALL take three arguments of data-type “http://www.w3.org/2001/XMLSchema#time” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if the first argument falls in the range defined inclusively by the second and third arguments.  Otherwise, it SHALL return “False”.  Regardless of its value, the third argument SHALL be interpreted as a time that is equal to, or later than by less than twenty-four hours, the second argument.  If no time zone is provided for the first argument, it SHALL use the default time zone at the context handler.  If no time zone is provided for the second or third arguments, then they SHALL use the time zone from the first argument.

 urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#dateTime” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is greater than the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#dateTime” by [XS] part 2, section 3.2.7.  Otherwise, it SHALL return “False”.  Note: if a dateTime value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#dateTime” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is greater than or equal to the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#dateTime” by [XS] part 2, section 3.2.7.  Otherwise, it SHALL return “False”.  Note: if a dateTime value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#dateTime” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is less than the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#dateTime” by [XS, part 2, section 3.2.7].  Otherwise, it SHALL return “False”.  Note: if a dateTime value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema# dateTime” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is less than or equal to the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#dateTime” by [XS] part 2, section 3.2.7.  Otherwise, it SHALL return “False”.  Note: if a dateTime value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 urn:oasis:names:tc:xacml:1.0:function:date-greater-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#date” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is greater than the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#date” by [XS] part 2, section 3.2.9.  Otherwise, it SHALL return “False”.  Note: if a date value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#date” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is greater than or equal to the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#date” by [XS] part 2, section 3.2.9.  Otherwise, it SHALL return “False”.  Note: if a date value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 urn:oasis:names:tc:xacml:1.0:function:date-less-than
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#date” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is less than the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#date” by [XS] part 2, section 3.2.9.  Otherwise, it SHALL return “False”.  Note: if a date value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal
 This function SHALL take two arguments of data-type “http://www.w3.org/2001/XMLSchema#date” and SHALL return an “http://www.w3.org/2001/XMLSchema#boolean”.  It SHALL return "True" if and only if the first argument is less than or equal to the second argument according to the order relation specified for “http://www.w3.org/2001/XMLSchema#date” by [XS] part 2, section 3.2.9.  Otherwise, it SHALL return “False”.  Note: if a date value does not include a time-zone value, then an implicit time-zone value SHALL be assigned, as described in [XS].

 */

/**
 * XACML Non-Numeric Comparison Functions
 * <p/>
 * Testing Functions as specified by OASIS XACML v3 Core specification.
 *
 * @author Jeff.Schenk@ForgeRock.com
 */
public class TestXacmlNonNumericComparisonFunctions {

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
