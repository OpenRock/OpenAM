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

// ApexIdentity Core Library
import com.apexidentity.util.FieldMap;

/**
 * Log entry data that provides a measurement.
 *
 * @author Paul C. Bryan
 */
public class LogMetric extends FieldMap {

    /** The numeric value of the metric. */
    public final Number value;

    /** The unit of measurement the metric is expressed in. */
    public final String units;

    /**
     * Constructs a new metric with the specific value and units.
     *
     * @param value the numeric value of the metric.
     * @param units the unit of measurement that the metric is expressed in.
     */
    public LogMetric(Number value, String units) {
        this.value = value;
        this.units = units;
    }

    /**
     * Returns the metric in the form <em>value</em> SP <em>units</em>. For example, if
     * value is {@code 100} and units are {@code "ms"}, then the returned value would be
     * {@code "100 ms"}.
     */ 
    @Override
    public String toString() {
        return value.toString() + ' ' + units;
    }
}
