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

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * XACML 3 Year Month Duration Object
 *
 * Provides consistent preservation of distinct Year and Month
 * data points.
 *
 * @author Jeff.Schenk@forgerock.com
 */
public class XACML3YearMonthDuration implements Serializable {
    /**
     * Number of Years of Duration.
     */
    private Integer years;
    /**
     * Number of Months of Duration.
     */
    private Integer months;

    /**
     * No Parameter Default Constructor
     */
    public XACML3YearMonthDuration() {
    }

    /**
     * Default Constructor
     * Parse  the Year Month String to object elements.
     *
     * @param yearMonthString
     *
     */
    public XACML3YearMonthDuration(String yearMonthString) {
           this.parse(yearMonthString);
    }

    /**
     * Constructor from String representation in the
     * form of Years-Months.
     *
     * @param years
     * @param months
     */
    public XACML3YearMonthDuration(int years, int months) {
        this.years = years;
        this.months = months;
    }

    /**
     * Obtain Years as an Integer
     * @return Integer of Years
     */
    public Integer getYears() {
        return years;
    }

    /**
     * Set the Number of Years in this Duration.
     * @param years
     */
    public void setYears(Integer years) {
        this.years = years;
    }

    /**
     * Obtain Months as an Integer
     * @return Integer of Months
     */
    public Integer getMonths() {
        return months;
    }

    /**
     * Set the Number of Months in this Duration.
     * @param months
     */
    public void setMonths(Integer months) {
        this.months = months;
    }

    /**
     * Parse the String Representation of the YearMothDuration Object.
     * @param yearMonthString
     */
    private void parse(String yearMonthString) {
        if ( (yearMonthString == null) || (yearMonthString.isEmpty()) ||
                (!yearMonthString.contains("-")) ||
                (yearMonthString.length() > 7) ) {
            throw new IllegalArgumentException("Illegal Argument");
        }
        try {
            String[] yearMonthArray = yearMonthString.split("-",2);
            if ( (yearMonthArray == null) || (yearMonthArray.length != 2) ) {
                throw new IllegalArgumentException("Must be in YYYY-MM Format.");
            }
            this.years  = Integer.parseInt(yearMonthArray[0]);
            this.months = Integer.parseInt(yearMonthArray[1]);
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Override our toString Method.
     * @return String representation of Years and Month as YYYY-MM
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Format the Numbers.
        sb.append(String.format("%04d", this.years)).append("-").append(String.format("%02d", this.months));
        return sb.toString();
    }

    /**
     * Override Equals
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XACML3YearMonthDuration that = (XACML3YearMonthDuration) o;

        if (months != null ? !months.equals(that.months) : that.months != null) return false;
        if (years != null ? !years.equals(that.years) : that.years != null) return false;

        return true;
    }

    /**
     * Override our hasCode for this Object.
     * @return
     */
    @Override
    public int hashCode() {
        int result = years != null ? years.hashCode() : 0;
        result = 31 * result + (months != null ? months.hashCode() : 0);
        return result;
    }

    /**
     * Substract out the Defined Duration from the specified MilliSeconds.
     * @param baseMilliSeconds
     * @return Long
     */
    public Long add(final Long baseMilliSeconds) {
        if (baseMilliSeconds == null) {
            throw new IllegalArgumentException();
        }
        DateTime dt = new DateTime(baseMilliSeconds);
        dt = dt.plusYears(this.getYears());
        dt = dt.plusMonths(this.getMonths());
        return dt.getMillis();
    }

    /**
     * Substract out the Defined Duration from the specified MilliSeconds.
     * @param baseMilliSeconds
     * @return Long
     */
    public Long sub(final Long baseMilliSeconds) {
        if (baseMilliSeconds == null) {
            throw new IllegalArgumentException();
        }
        DateTime dt = new DateTime(baseMilliSeconds);
        dt = dt.minusYears(this.getYears());
        dt = dt.minusMonths(this.getMonths());
        return dt.getMillis();
    }
}
