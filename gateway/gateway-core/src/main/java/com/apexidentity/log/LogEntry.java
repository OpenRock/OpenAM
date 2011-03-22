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

package com.apexidentity.log;

/**
 * The log entry data structure.
 *
 * @author Paul C. Bryan
 */
public class LogEntry {

    /** The time of the event being logged (milliseconds since the 1970 epoch). */
    public Long time;

    /** The subject and/or event being logged, in hierarchical dot-delimited notation. */
    public String source;

    /** The logging level of the entry. */
    public LogLevel level;

    /** Human-readable message text, suitable for display in any entry listings. */
    public String message;

    /** The data being logged or {@code null} if no data. */
    public Object data;
}
