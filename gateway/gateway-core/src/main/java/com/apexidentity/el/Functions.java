/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2010–2011 ApexIdentity Inc. All rights reserved.
 */

package com.apexidentity.el;

// Java Standard Edition
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// Java Enterprise Edition
import javax.el.FunctionMapper;

// ApexIdentity Core Library
import com.apexidentity.util.EnumerableMap;
import com.apexidentity.util.MapDecorator;
import com.apexidentity.util.ReadableList;
import com.apexidentity.util.StringUtil;

/**
 * Maps between EL function names and methods. In this implementation all public, static
 * methods that are prefixed with an '_' (underscore) character are automatically exposed
 * (sans prefix) as functions.  
 *
 * @author Paul C. Bryan
 */
public class Functions extends FunctionMapper {

    /** A mapping of function names with methods to return. */
    private static final Map<String, Method> METHODS = mapMethods();

    /**
     * Resolves the specified prefix and local name into a method. In this implementation,
     * the only supported supported prefix is none ({@code ""}).
     *
     * @param prefix the prefix of the function, or {@code ""} if no prefix.
     * @param localName the short name of the function.
     * @return the static method to invoke, or {@code null} if no match was found.
     */
    @Override
    public Method resolveFunction(String prefix, String localName) {
        if (prefix != null && localName != null && prefix.length() == 0) {
            return METHODS.get(localName);
        }
        return null; // no match was found
    }

    /**
     * Returns {@code true} if the object contains the value.
     *
     * @param object the object whose length is to be determined.
     * @return the length of the object, or {@code 0} if length could not be deteremined.
     */
    public static boolean _contains(Object object, Object value) {
        if (object == null || value == null) {
            return false;
        }
        else if (object instanceof CharSequence && value instanceof CharSequence) {
            return (object.toString().contains(value.toString()));
        }
        else if (object instanceof Collection) {
            return ((Collection)object).contains(value);
        }
        else if (object instanceof Object[]) { // doesn't handle primitives (but is cheap)
            for (Object o : (Object[])object) {
                if (o.equals(value)) {
                    return true;
                }
            }
        }
        else if (object.getClass().isArray()) { // handles primitives (slightly more expensive)
            int length = Array.getLength(object);
            for (int n = 0; n < length; n++) {
                if (Array.get(object, n).equals(value)) {
                    return true;
                }
            }
        }
        else if (ReadableList.DUCK.isInstance(object)) { // duck typing (more expensive)
            for (Object o : ReadableList.DUCK.cast(object)) {
                if (o.equals(value)) {
                    return true;
                }
            }
        }
        return false; // value not contained in object
    }

    /**
     * Returns the index within a string of the first occurance of a specified substring.
     *
     * @param string the string to be searched.
     * @param substring the value to search for within the string
     * @return the index of the first instance of substring, or {@code -1} if not found.
     */
    public static int _indexOf(String string, String substring) {
        return (string != null && substring != null ? string.indexOf(substring) : null);
    } 

    /**
     * Joins an array of strings into a single string value, with a specified separator.
     *
     * @param separator the separator to place between joined elements.
     * @param strings the array of strings to be joined.
     * @return the string containing the joined strings.
     */
    public static String _join(String[] strings, String separator) {
        return (strings != null ? StringUtil.join(separator, (Object[])strings) : null);
    }

    /**
     * Returns the first key found in a map that matches the specified regular expression
     * pattern, or {@code null} if no such match is found.
     *
     * @param map the map whose keys are to be searched.
     * @param pattern a string containing the regular expression pattern to match.
     * @return the first matching key, or {@code null} if no match found. 
     */
    @SuppressWarnings("unchecked")
    public static String _keyMatch(Object map, String pattern) {
        EnumerableMap enumerable = null;
        if (map instanceof Map) { // avoid unnecessary proxying via duck typing
            enumerable = new MapDecorator((Map)map);
        }
        else if (EnumerableMap.DUCK.isInstance(map)) {
            enumerable = EnumerableMap.DUCK.cast(map);
        }
        if (enumerable != null) {
            Pattern p = null;
            try {
                p = Pattern.compile(pattern); // TODO: cache oft-used patterns?
            }
            catch (PatternSyntaxException pse) {
                return null; // invalid pattern results in no match
            }
            for (Object key : enumerable.keySet()) {
                if (key instanceof String) {
                    if (p.matcher((String)key).matches()) {
                        return (String)key;
                    }
                }
            }
        }
        return null; // no match
    }

    /**
     * Returns the number of items in a collection, or the number of characters in a string.
     *
     * @param object the object whose length is to be determined.
     * @return the length of the object, or {@code 0} if length could not be deteremined.
     */
    public static int _length(Object object) {
        if (object == null) {
            return 0;
        }
        else if (object instanceof CharSequence) {
            return ((CharSequence)object).length();
        }
        else if (object instanceof Collection) {
            return ((Collection)object).size();
        }
        else if (object instanceof Map) {
            return ((Map)object).size();
        }
        else if (object instanceof Object[]) { // doesn't handle primitives (but is cheap)
            return ((Object[])object).length;
        }
        else if (object.getClass().isArray()) { // handles primitives (slightly more expensive)
            return Array.getLength(object);
        }
        else if (ReadableList.DUCK.isInstance(object)) { // duck typing (more expensive)
            return ReadableList.DUCK.cast(object).size();
        }
        else if (EnumerableMap.DUCK.isInstance(object)) { // duck typing (more expensive)
            return EnumerableMap.DUCK.cast(object).size();
        }
        return 0; // no items
    }

    /**
     * Returns an array of matches of a regular expression pattern against a string, or
     * {@code null} if no such match is found. The first element of the array is the entire
     * match, and each subsequent element correlates to any capture group specified within
     * the regular expression.
     *
     * @param string the string to be searched.
     * @param pattern a string containing the regular expression pattern to match.
     * @return an array of matches, or {@code null} if no match found.
     */  
    public static String[] _matches(String string, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(string);
            if (m.find()) {
                int count = m.groupCount();
                String[] matches = new String[count + 1];
                matches[0] = m.group(0);
                for (int n = 1; n <= count; n++) {
                    matches[n] = m.group(n);
                }
                return matches;
            }
        }
        catch (PatternSyntaxException pse) {
            // ignore invalid pattern
        }
        return null;
    }

    /**
     * Splits a string into an array of substrings around matches of the given regular
     * expression.
     *
     * @param string the string to be split.
     * @param regex the regular expression to split substrings around.
     * @return the resulting array of split substrings.
     */
    public static String[] _split(String string, String regex) {
        return (string != null ? string.split(regex) : null);
    }

    /**
     * Converts all of the characters in a string to lower case.
     *
     * @param string the string whose characters are to be converted.
     * @return the string with characters converted to lower case.
     */
    public static String _toLowerCase(String string) {
        return (string != null ? string.toLowerCase() : null);
    }

    /**
     * Returns the string value of an aribtrary object.
     *
     * @param object the object whose string value is to be returned.
     * @return the string value of the object.
     */
    public static String _toString(Object object) {
        return (object != null ? object.toString() : null); 
    }

    /**
     * Converts all of the characters in a string to upper case.
     *
     * @param string the string whose characters are to be converted.
     * @return the string with characters converted to upper case.
     */
    public static String _toUpperCase(String string) {
        return (string != null ? string.toUpperCase() : null);
    }

    /**
     * Returns a copy of a string with leading and trailing whitespace omitted.
     *
     * @param string the string whose white space is to be omitted.
     * @return the string with leading and trailing white space omitted.
     */
    public static String _trim(String string) {
        return (string != null ? string.trim() : null);
    }

    private static Map<String, Method> mapMethods() {
        HashMap<String, Method> map = new HashMap<String, Method>();
        for (Method method : Functions.class.getMethods()) {
            if (method.getName().charAt(0) == '_' && Modifier.isStatic(method.getModifiers())) {
                map.put(method.getName().substring(1), method);
            }
        }
        return map;
    }
}
