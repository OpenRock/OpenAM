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

package com.apexidentity.header;

// Java Standard Edition
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ApexIdentity Core Library
import com.apexidentity.util.CaseInsensitiveMap;

/**
 * Utility class for processing values in HTTP header fields.
 * 
 * @author Paul C. Bryan
 */
public class HeaderUtil {

    /** Static methods only */
    private HeaderUtil() {
    }

    /**
     * Parses an HTTP header value, splitting it into multiple values around the specified
     * separator. Quoted strings are not split into multiple values if they contain separator
     * characters. All leading and trailing white space in values is trimmed. All quotations
     * remain intact.
     * <p>
     * Note: This method is liberal in its interpretation of malformed header values; namely
     * the incorrect use of string and character quoting mechanisms and unquoted white space.
     * If a {@code null} or empty string is supplied as a value, this method yields an empty
     * list. 
     *
     * @param value the header value to be split.
     * @param separator the separator character to split headers around.
     */
    public static List<String> split(String value, char separator) {
        if (separator == '"' || separator == '\\') {
            throw new IllegalArgumentException("invalid separator: " + separator);
        }
        ArrayList<String> values = new ArrayList<String>();
        if (value != null) {
            int length = value.length();
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            boolean quoted = false;
            for (int n = 0, cp; n < length; n += Character.charCount(cp)) {
                cp = value.codePointAt(n);
                if (escaped) { // single-character quoting mechanism per RFC 2616 §2.2
                    sb.appendCodePoint(cp);
                    escaped = false;
                }
                else if (cp == '\\') {
                    sb.appendCodePoint(cp);
                    if (quoted) { // single-character quoting mechanism per RFC 2616 §2.2
                        escaped = true;
                    }
                }
                else if (cp == '"') {
                    sb.appendCodePoint(cp); // quotation marks remain intact here
                    quoted = !quoted;
                }
                else if (cp == separator && !quoted) { // only separator if not in quoted string
                    String s = sb.toString().trim();
                    if (s.length() > 0) {
                        values.add(s);
                    }
                    sb.setLength(0); // reset for next token
                }
                else {
                    sb.appendCodePoint(cp);
                }
            }
            String s = sb.toString().trim();
            if (s.length() > 0) {
                values.add(s);
            }
        }
        return values;
    }

    /**
     * Joins a collection of header values into a single header value, with a specified
     * specified separator. A {@code null} or empty collection of header values yeilds a
     * {@code null} return value.
     *
     * @param values the values to be joined.
     * @param separator the separator to separate values within the returned value.
     * @return a single header value, with values separated by the separator.
     */
    public static String join(Collection<String> values, char separator) {
        if (separator == '"' || separator == '\\') {
            throw new IllegalArgumentException("invalid separator: " + separator);
        }
        StringBuilder sb = new StringBuilder();
        if (values != null) {
            for (String s : values) {
                if (s != null) {
                    if (sb.length() > 0) {
                        sb.append(separator).append(' ');
                    }
                    sb.append(s);
                }
            }
        }
        return (sb.length() > 0 ? sb.toString() : null);
    }

    /**
     * Splits a single HTTP header parameter name and value from an input string value. The
     * input string value is presumed to have been extracted from a collection provided by the
     * {@link #split(String, char)} method.
     * <p>
     * This method returns the parameter name-value pair split into an array of
     * {@code String}s. Element {@code [0]} contains the parameter name; element {@code [1]}
     * contains contains the parameter value or {@code null} if there is no value.
     * <p>
     * A value that is contained within a quoted-string is processed such that the surrounding
     * '"' (quotation mark) characters are removed and single-character quotations hold the
     * character being quoted without the escape '\' (backslash) character. All white space
     * outside of the quoted-string is removed. White space within the quoted-string is
     * retained.
     * <p>
     * Note: This method is liberal in its interpretation of a malformed header value; namely
     * the incorrect use of string and character quoting mechanisms and unquoted white space.
     *
     * @param value the string to parse the name-value parameter from.
     * @return the name-value pair split into a {@code String} array.
     */
    public static String[] parseParameter(String value) {
        String[] ss = new String[2];
        boolean inValue = false;
        boolean quoted = false;
        boolean escaped = false;
        int length = value.length();
        StringBuilder sb = new StringBuilder();
        for (int n = 0, cp; n < length; n += Character.charCount(cp)) {
            cp = value.codePointAt(n);
            if (escaped) { // single-character quoting mechanism per RFC 2616 §2.2
                sb.appendCodePoint(cp);
                escaped = false;
            }
            else if (cp == '\\') {
                if (quoted) {
                    escaped = true; // next character is literal
                }
                else {
                    sb.appendCodePoint(cp); // not quoted, push the backslash literal (header probably malformed) 
                }
            }
            else if (cp == '"') {
                quoted = !quoted; // toggle quoted status
            }
            else if (!quoted && !inValue && cp == '=') { // only separator if in key and not in quoted-string
                ss[0] = sb.toString().trim();
                sb.setLength(0); // reset for next token
                inValue = true;
            }
            else if (!quoted && Character.isWhitespace(cp)) {
                // drop unquoted white space (header probably malformed if not at beginning or end)
            }
            else {
                sb.appendCodePoint(cp);
            }
        }
        if (!inValue) {
            ss[0] = sb.toString().trim();
        }
        else {
            ss[1] = sb.toString();
        }
        return ss;
    }

    /**
     * Parses a set of HTTP header parameters from a collection of values. The input
     * collection of values is presumed to have been provided from the
     * {@link #split(String, char)} method.
     * <p>
     * A well-formed parameter contains an attribute and optional value, separated by an '='
     * (equals sign) character. If the parameter contains no value, it is represented by a
     * {@code null} value in the returned map.
     * <p>
     * Values that are contained in quoted-strings are processed such that the surrounding
     * '"' (quotation mark) characters are removed and single-character quotations hold the
     * character being quoted without the escape '\' (backslash) character. All white space
     * outside of quoted-strings is removed. White space within quoted-strings is retained.
     * <p>
     * Note: This method is liberal in its interpretation of malformed header values; namely
     * the incorrect use of string and character quoting mechanisms and unquoted white space. 
     *
     * @param values the HTTP header parameters.
     * @return a map of parameter name-value pairs.
     */
    public static Map<String, String> parseParameters(Collection<String> values) {
        CaseInsensitiveMap<String> map = new CaseInsensitiveMap<String>(new HashMap<String, String>());
        if (values != null) {
            StringBuilder sb = new StringBuilder();
            for (String value : values) {
                String[] param = parseParameter(value);
                if (param[0] != null && param[0].length() > 0 && !map.containsKey(param[0])) {
                    map.put(param[0], param[1]);
                }
            }
        }
        return map;
    }

    /**
     * Encloses a string in quotation marks. Quotation marks and backslash characters are
     * escaped with the single-character quoting mechanism. For more information, see
     * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a> §2.2.
     *
     * @param value the value to be enclosed in quotation marks.
     * @return the value enclosed in quotation marks.
     */
    public static String quote(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder('"');
        int length = value.length();
        for (int n = 0, cp; n < length; n += Character.charCount(cp)) {
            cp = value.codePointAt(n);
            if (cp == '\\' || cp == '"') {
                sb.append('\\');
            }
            sb.appendCodePoint(cp);
        }
        sb.append('"');
        return sb.toString();
    }
}
