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

// Java Standard Edition
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * TODO: Description.
 *
 * @author Paul C. Bryan
 */
public class ISO8601 {

    /** Format to output dates in. Must be used in synchronized block. */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        SDF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * TODO: Description.
     *
     * @param date TODO.
     * @return TODO.
     */
    public static String format(Date date) {
        synchronized(SDF) {
            return SDF.format(date);
        }
    }

    /**
     * TODO: Description.
     *
     * @param date TODO.
     * @return TODO.
     */
    public static String format(long date) {
        return format(new Date(date));
    }
}
