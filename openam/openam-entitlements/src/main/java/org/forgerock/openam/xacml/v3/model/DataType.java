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

package org.forgerock.openam.xacml.v3.model;

public class DataType {
    public static String XACMLSTRING = "http://www.w3.org/2001/XMLSchema#string";
    public static String XACMLBOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";
    public static String XACMLINTEGER = "http://www.w3.org/2001/XMLSchema#integer";
    public static String XACMLDOUBLE = "http://www.w3.org/2001/XMLSchema#double";
    public static String XACMLTIME = "http://www.w3.org/2001/XMLSchema#time";
    public static String XACMLDATE = "http://www.w3.org/2001/XMLSchema#date";
    public static String XACMLDATETIME = "http://www.w3.org/2001/XMLSchema#dateTime";
    public static String XACMLANYURI = "http://www.w3.org/2001/XMLSchema#anyURI";
    public static String XACMLHEXBINARY = "http://www.w3.org/2001/XMLSchema#hexBinary";
    public static String XACMLBASE64BINARY = "http://www.w3.org/2001/XMLSchema#base64Binary";
    public static String XACMLDAYTIMEDURATION = "http://www.w3.org/2001/XMLSchema#dayTimeDuration";
    public static String XACMLYEARMONTHDURATION = "http://www.w3.org/2001/XMLSchema#yearMonthDuration";
    public static String XACMLX500NAME = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name";
    public static String XACMLRFC822NAME = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name";
    public static String XACMLIPADDRESS = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress";
    public static String XACMLDNSNAME = "urn:oasis:names:tc:xacml:2.0:data-type:dnsName";
    public static String XACMLXPATHEXPRESSION = "urn:oasis:names:tc:xacml:3.0:data-type:xpathExpression";
    public static String XACMLUNDEFINED = "";

    public static enum Type {
    XACMLUNDEFINEDTYPE       (0,XACMLUNDEFINED),
    XACMLSTRINGTYPE          (1,XACMLSTRING),
    XACMLBOOLEANTYPE         (2,XACMLBOOLEAN),
    XACMLINTEGERTYPE         (3,XACMLINTEGER),
    XACMLDOUBLETYPE          (4,XACMLDOUBLE),
    XACMLTIMETYPE            (5,XACMLTIME),
    XACMLDATETYPE            (6,XACMLDATE),
    XACMLDATETIMETYPE        (7,XACMLDATETIME),
    XACMLANYURITYPE          (8,XACMLANYURI),
    XACMLHEXBINARYTYPE       (9,XACMLHEXBINARY),
    XACMLBASE64BINARYTYPE    (10,XACMLBASE64BINARY),
    XACMLDAYTIMEDURATIONTYPE (11,XACMLDAYTIMEDURATION),
    XACMLYEARMONTHDURATIONTYPE(12,XACMLYEARMONTHDURATION),
    XACMLX500NAMETYPE        (13,XACMLX500NAME),
    XACMLRFC822NAMETYPE      (14,XACMLRFC822NAME),
    XACMLIPADDRESSTYPE       (15,XACMLIPADDRESS),
    XACMLDNSNAMETYPE         (16,XACMLDNSNAME),
    XACMLXPATHEXPRESSIONTYPE (17,XACMLXPATHEXPRESSION);

        private int index;
        private String typeName;

        Type (int i,String t) {
            index = i;
            this.typeName = t;
        }
        int getIndex() {
            return index;
        }
        String getTypeName() {
            return typeName;
        }
    }
    private int typeIndex;

    public DataType(String name) {
        typeIndex = 0; // Unknown XACML DataType.
        if (name==null) {
            return;
        }
        for (Type s : Type.values()) {
            if (name.equals(s.getTypeName())) {
                typeIndex = s.getIndex();
                break;
            }
        }
    }
    public String getTypeName() {
        String retVal = null;
        for (Type s : Type.values()) {
            if (typeIndex == s.getIndex()) {
                retVal =  s.getTypeName();
                break;
            }
        }
        return retVal;
    }
    public int getIndex() {
        return typeIndex;
    }

    public boolean isType(DataType.Type t)  {
        return (typeIndex == t.getIndex());
    }

    public Object typedValue(String s) {

        if (isType(DataType.Type.XACMLSTRINGTYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLBOOLEANTYPE)) {
            return Boolean.valueOf(s);
        }
        if (isType(DataType.Type.XACMLINTEGERTYPE)) {
            return Integer.valueOf(s);
        }
        if (isType(DataType.Type.XACMLDOUBLETYPE)) {
            return Double.valueOf(s);
        }
        if (isType(DataType.Type.XACMLTIMETYPE)) {
            return XACML3PrivilegeUtils.stringToTime(s);
        }
        if (isType(DataType.Type.XACMLDATETYPE)) {
            return XACML3PrivilegeUtils.stringToDate(s);
        }
        if (isType(DataType.Type.XACMLDATETIMETYPE)) {
            return XACML3PrivilegeUtils.stringToDateTime(s);
        }
        if (isType(DataType.Type.XACMLANYURITYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLHEXBINARYTYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLBASE64BINARYTYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLDAYTIMEDURATIONTYPE)) {
            return XACML3PrivilegeUtils.stringDayTimeDurationToLongDuration(s);
        }
        if (isType(DataType.Type.XACMLYEARMONTHDURATIONTYPE)) {
            return XACML3PrivilegeUtils.stringYearMonthdurationToLongDuration(s);
        }
        if (isType(DataType.Type.XACMLX500NAMETYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLRFC822NAMETYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLIPADDRESSTYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLDNSNAMETYPE)) {
            return s;
        }
        if (isType(DataType.Type.XACMLXPATHEXPRESSIONTYPE)) {
            return s;
        }
        return s;
    }


}
