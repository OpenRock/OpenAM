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
 * Copyright 2009 Sun Microsystems Inc. All rights reserved.
 * Portions Copyrighted © 2010–2011 ApexIdentity Inc.
 */

package com.apexidentity.util;

// Java Standard Edition
import java.util.Arrays;
import java.util.Iterator;

/**
 * Miscellaneous string utility methods.
 *
 * @author Paul C. Bryan
 */
public class StringUtil {

    /**
     * Joins a collection of elements into a single string value, with a specified separator.
     *
     * @param separator the separator to place between joined elements.
     * @param elements the collection of strings to be joined.
     * @return the string containing the joined elements.
     */
    public static String join(String separator, Iterable<?> elements) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<?> i = elements.iterator(); i.hasNext();) {
            sb.append(i.next().toString());
            if (i.hasNext() && separator != null) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * Joins an array of strings into a single string value, with a specified separator.
     *
     * @param separator the separator to place between joined elements.
     * @param elements the array of strings to be joined.
     * @return the string containing the joined string array.
     */
    public static String join(String separator, Object... elements) {
        return join(separator, Arrays.asList(elements));
    }
}
