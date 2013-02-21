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
package org.forgerock.identity.openam.xacml.v3.Functions;
import org.forgerock.identity.openam.xacml.v3.Entitlements.FunctionArgument;
import org.forgerock.identity.openam.xacml.v3.Entitlements.XACMLPIPObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
    This interface defines the XACML functions.
    The Syntax,  is to create a function object with a
    ResourceID, and a Value, which will be checked
    when the function is evaluated.
 */
public abstract class XACMLFunction extends FunctionArgument {
    static java.util.Map<String,String> functions;
    private List<FunctionArgument> arguments;

    public XACMLFunction() {
        arguments = new ArrayList<FunctionArgument>();
    }

    public XACMLFunction addArgument(FunctionArgument arg) {
        arguments.add(arg);
        return this;
    };
    public XACMLFunction addArgument(List<FunctionArgument> args) {
        arguments.addAll(args);
        return this;
    };
    public Object getValue(XACMLPIPObject pip) {
        return evaluate(pip).getValue(pip);
    };
    abstract public FunctionArgument evaluate( XACMLPIPObject pip);

    /* Protected methods only for subclasses */

    protected FunctionArgument getArg(int index) {
        return arguments.get(index);
    }
    protected int getArgCount() {
        return arguments.size();
    }

    public static XACMLFunction getInstance(String name)  {
        if (functions == null) {
            initFunctionTable();
        }
        String cName = functions.get(name);
        XACMLFunction retVal = null;
        if (cName != null) {
            try {
             retVal = (XACMLFunction) Class.forName(cName).newInstance();
            } catch (Exception ex) {
               retVal = null;
            }
        }
        return retVal;
    }

    static void initFunctionTable() {
        functions = new HashMap<String,String>();

        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-equal","org.forgerock.identity.openam.xacml.v3.Functions.StringEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-equal","org.forgerock.identity.openam.xacml.v3.Functions.BooleanEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-equal","org.forgerock.identity.openam.xacml.v3.Functions.IntegerEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-equal","org.forgerock.identity.openam.xacml.v3.Functions.DoubleEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-equal","org.forgerock.identity.openam.xacml.v3.Functions.DateEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-equal","org.forgerock.identity.openam.xacml.v3.Functions.TimeEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-equal","org.forgerock.identity.openam.xacml.v3.Functions.X500nameEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-add","org.forgerock.identity.openam.xacml.v3.Functions.IntegerAdd");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-add","org.forgerock.identity.openam.xacml.v3.Functions.DoubleAdd");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-subtract","org.forgerock.identity.openam.xacml.v3.Functions.IntegerSubtract");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-subtract","org.forgerock.identity.openam.xacml.v3.Functions.DoubleSubtract");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-multiply","org.forgerock.identity.openam.xacml.v3.Functions.IntegerMultiply");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-multiply","org.forgerock.identity.openam.xacml.v3.Functions.DoubleMultiply");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-divide","org.forgerock.identity.openam.xacml.v3.Functions.IntegerDivide");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-divide","org.forgerock.identity.openam.xacml.v3.Functions.DoubleDivide");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-mod","org.forgerock.identity.openam.xacml.v3.Functions.IntegerMod");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-abs","org.forgerock.identity.openam.xacml.v3.Functions.IntegerAbs");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-abs","org.forgerock.identity.openam.xacml.v3.Functions.DoubleAbs");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:round","org.forgerock.identity.openam.xacml.v3.Functions.Round");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:floor","org.forgerock.identity.openam.xacml.v3.Functions.Floor");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-normalize-space","org.forgerock.identity.openam.xacml.v3.Functions.StringNormalizeSpace");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case","org.forgerock.identity.openam.xacml.v3.Functions.StringNormalizeToLowerCase");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-to-integer","org.forgerock.identity.openam.xacml.v3.Functions.DoubleToInteger");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-to-double","org.forgerock.identity.openam.xacml.v3.Functions.IntegerToDouble");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:or","org.forgerock.identity.openam.xacml.v3.Functions.Or");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:and","org.forgerock.identity.openam.xacml.v3.Functions.And");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:n-of","org.forgerock.identity.openam.xacml.v3.Functions.NOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:not","org.forgerock.identity.openam.xacml.v3.Functions.Not");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than","org.forgerock.identity.openam.xacml.v3.Functions.IntegerGreaterThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.IntegerGreaterThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-less-than","org.forgerock.identity.openam.xacml.v3.Functions.IntegerLessThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.IntegerLessThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-greater-than","org.forgerock.identity.openam.xacml.v3.Functions.DoubleGreaterThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.DoubleGreaterThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-less-than","org.forgerock.identity.openam.xacml.v3.Functions.DoubleLessThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.DoubleLessThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeAddDayTimeduration");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeAddYearmonthduration");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeSubtractDaytimeduration");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-subtractyearMonthDuration","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeSubtractyearmonthduration");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration","org.forgerock.identity.openam.xacml.v3.Functions.DateAddYearmonthduration");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration","org.forgerock.identity.openam.xacml.v3.Functions.DateSubtractYearmonthduration");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-greater-than","org.forgerock.identity.openam.xacml.v3.Functions.StringGreaterThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.StringGreaterThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-less-than","org.forgerock.identity.openam.xacml.v3.Functions.StringLessThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.StringLessThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-greater-than","org.forgerock.identity.openam.xacml.v3.Functions.TimeGreaterThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.TimeGreaterThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-less-than","org.forgerock.identity.openam.xacml.v3.Functions.TimeLessThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.TimeLessThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:time-in-range","org.forgerock.identity.openam.xacml.v3.Functions.TimeInRange");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeGreaterThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeGreaterThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeLessThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeLessThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-greater-than","org.forgerock.identity.openam.xacml.v3.Functions.DateGreaterThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.DateGreaterThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-less-than","org.forgerock.identity.openam.xacml.v3.Functions.DateLessThan");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal","org.forgerock.identity.openam.xacml.v3.Functions.DateLessThanOrEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.StringOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.StringBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-is-in","org.forgerock.identity.openam.xacml.v3.Functions.StringIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-bag","org.forgerock.identity.openam.xacml.v3.Functions.StringBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.BooleanOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.BooleanBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-is-in","org.forgerock.identity.openam.xacml.v3.Functions.BooleanIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-bag","org.forgerock.identity.openam.xacml.v3.Functions.BooleanBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.IntegerOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.IntegerBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-is-in","org.forgerock.identity.openam.xacml.v3.Functions.IntegerIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-bag","org.forgerock.identity.openam.xacml.v3.Functions.IntegerBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.DoubleOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.DoubleBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-is-in","org.forgerock.identity.openam.xacml.v3.Functions.DoubleIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-bag","org.forgerock.identity.openam.xacml.v3.Functions.DoubleBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.TimeOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.TimeBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-is-in","org.forgerock.identity.openam.xacml.v3.Functions.TimeIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-bag","org.forgerock.identity.openam.xacml.v3.Functions.TimeBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.DateOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.DateBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-is-in","org.forgerock.identity.openam.xacml.v3.Functions.DateIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-bag","org.forgerock.identity.openam.xacml.v3.Functions.DateBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.X500nameOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.X500nameBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in","org.forgerock.identity.openam.xacml.v3.Functions.X500nameIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag","org.forgerock.identity.openam.xacml.v3.Functions.X500nameBag");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameOneAndOnly");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameBagSize");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameIsIn");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameBag");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:string-concatenate","org.forgerock.identity.openam.xacml.v3.Functions.StringConcatenate");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate","org.forgerock.identity.openam.xacml.v3.Functions.UriStringConcatenate");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:any-of","org.forgerock.identity.openam.xacml.v3.Functions.AnyOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:all-of","org.forgerock.identity.openam.xacml.v3.Functions.AllOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:any-of-any","org.forgerock.identity.openam.xacml.v3.Functions.AnyOfAny");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:all-of-any","org.forgerock.identity.openam.xacml.v3.Functions.AllOfAny");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:any-of-all","org.forgerock.identity.openam.xacml.v3.Functions.AnyOfAll");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:all-of-all","org.forgerock.identity.openam.xacml.v3.Functions.AllOfAll");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:map","org.forgerock.identity.openam.xacml.v3.Functions.Map");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-match","org.forgerock.identity.openam.xacml.v3.Functions.X500nameMatch");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameMatch");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match","org.forgerock.identity.openam.xacml.v3.Functions.StringRegexpMatch");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:anyURI-regexp-match","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriRegexpMatch");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:ipAddress-regexp-match","org.forgerock.identity.openam.xacml.v3.Functions.IpaddressRegexpMatch");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:dnsName-regexp-match","org.forgerock.identity.openam.xacml.v3.Functions.dnsNameRegexpMatch");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:rfc822Name-regexp-match","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameRegexpMatch");
        functions.put("urn:oasis:names:tc:xacml:2.0:function:x500Name-regexp-match","org.forgerock.identity.openam.xacml.v3.Functions.X500nameRegexpMatch");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:xpath-node-count","org.forgerock.identity.openam.xacml.v3.Functions.XpathNodeCount");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:xpath-node-equal","org.forgerock.identity.openam.xacml.v3.Functions.XpathNodeEqual");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:xpath-node-match","org.forgerock.identity.openam.xacml.v3.Functions.XpathNodeMatch");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-intersection","org.forgerock.identity.openam.xacml.v3.Functions.StringIntersection;
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.StringAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-union","org.forgerock.identity.openam.xacml.v3.Functions.StringUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-subset","org.forgerock.identity.openam.xacml.v3.Functions.StringSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:string-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.StringSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-intersection","org.forgerock.identity.openam.xacml.v3.Functions.BooleanIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.BooleanAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-union","org.forgerock.identity.openam.xacml.v3.Functions.BooleanUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-subset","org.forgerock.identity.openam.xacml.v3.Functions.BooleanSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.BooleanSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-intersection","org.forgerock.identity.openam.xacml.v3.Functions.IntegerIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.IntegerAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-union","org.forgerock.identity.openam.xacml.v3.Functions.IntegerUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-subset","org.forgerock.identity.openam.xacml.v3.Functions.IntegerSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:integer-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.IntegerSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-intersection","org.forgerock.identity.openam.xacml.v3.Functions.DoubleIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.DoubleAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-union","org.forgerock.identity.openam.xacml.v3.Functions.DoubleUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-subset","org.forgerock.identity.openam.xacml.v3.Functions.DoubleSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:double-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.DoubleSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-intersection","org.forgerock.identity.openam.xacml.v3.Functions.TimeIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.TimeAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-union","org.forgerock.identity.openam.xacml.v3.Functions.TimeUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-subset","org.forgerock.identity.openam.xacml.v3.Functions.timeSubset;
        functions.put("urn:oasis:names:tc:xacml:1.0:function:time-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.TimeSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-intersection","org.forgerock.identity.openam.xacml.v3.Functions.DateIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.DateAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-union","org.forgerock.identity.openam.xacml.v3.Functions.DateUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-subset","org.forgerock.identity.openam.xacml.v3.Functions.DateSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:date-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.DateSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-union","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-subset","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.DatetimeSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-union","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-subset","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.AnyuriSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-union","org.forgerock.identity.openam.xacml.v3.Functions.HexbinaryUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset","org.forgerock.identity.openam.xacml.v3.Functions.HexbinarySubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.HexbinarySetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-union","org.forgerock.identity.openam.xacml.v3.Functions.Base64binaryUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset","org.forgerock.identity.openam.xacml.v3.Functions.Base64binarySubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.Base64binarySetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.DaytimedurationSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.YearmonthdurationSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection","org.forgerock.identity.openam.xacml.v3.Functions.X500nameIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.X500nameAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-union","org.forgerock.identity.openam.xacml.v3.Functions.X500nameUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-subset","org.forgerock.identity.openam.xacml.v3.Functions.X500nameSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.X500nameSetEquals");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameIntersection");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameAtLeastOneMemberOf");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameUnion");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameSubset");
        functions.put("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals","org.forgerock.identity.openam.xacml.v3.Functions.Rfc822nameSetEquals");

        functions.put("urn:oasis:names:forgerock:xacml:1.0:function:VariableDereference","org.forgerock.identity.openam.xacml.v3.Functions.VariableDereference");

    }
}
