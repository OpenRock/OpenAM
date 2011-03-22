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

package com.apexidentity.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class for accessing Java enum types.
 *
 * @author Paul C. Bryan
 */
public class EnumUtil {

    /** Cache of name sets for repeated call efficiency. */
    private static final HashMap<Class, Set<String>> NAMESETS = new HashMap<Class, Set<String>>();

    /** Static methods only. */
    private EnumUtil() {
    }

    /**
     * Returns a set of the names of the enum constants of the specified enum type. The
     * returned set maintains iteration order of the constants in the order they're declared
     * in the enum type.
     *
     * @param enumType the class of the enum type from which to return the names.
     * @return a set of the names of the enum constants in the specified enum.
     */
    public static <T extends Enum<T>> Set<String> names(Class<T> enumType) {
        Set<String> set = NAMESETS.get(enumType);
        if (set == null) { // cached for repeated call efficiency
            set = new LinkedHashSet<String>();
            for (T constant : enumType.getEnumConstants()) {
                set.add(constant.toString());
            }
            NAMESETS.put(enumType, Collections.unmodifiableSet(set));
        }
        return set;
    }

    /**
     * Returns the enum constant of the specified enum type with the specified name, or
     * {@code null} if the specified enum type has no constant with the specified name, or
     * if the specified class object does not represent an enum type.
     *
     * @param enumType the class of the enum type from which to return a constant.
     * @param name the name of the constant to return.
     * @return the matching enum constant or {@code null} if no match found.
     */
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, Object name) {
        T value = null;
        if (name instanceof CharSequence) {
            try {
                value = Enum.valueOf(enumType, name.toString());
            }
            catch (IllegalArgumentException iae) {
                // result in null return value
            }
        }
        return value;
    }
}
