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
 * Records elapsed time in a log in milliseconds.
 *
 * @author Paul C. Bryan
 */ 
public class LogTimer {

    /** The time that the timer was started. */
    private long started = Long.MIN_VALUE; // indicates the timer has not been started

    /** The sink to record log entries to. */
    private final LogSink sink;

    /** The event the (within the source) that is being timed. */
    private final String event;

    /** The log level to log timer events with. */
    private final LogLevel level;

    /**
     * Constructs a new timer with a logging level of {@link LogLevel#STAT STAT}.
     *
     * @param sink the sink to record timer log entries to.
     */
    public LogTimer(LogSink sink) {
        this(sink, LogLevel.STAT);
    }

    /**
     * Constructs a new timer to log events at a specified logging level.
     *
     * @param sink the sink to record timer log entries to.
     * @param level the logging level to record timer log entries with.
     */
    public LogTimer(LogSink sink, LogLevel level) {
        this(sink, level, null);
    }

    /**
     * Constructs a new timer to log events of a specific type at a specific logging level.
     *
     * @param sink the sink to record timer log entries to.
     * @param level the logging level to record timer log entries with.
     * @param event the event being timed.
     */
    public LogTimer(LogSink sink, LogLevel level, String event) {
        System.nanoTime(); // avoid call to nanoTime improbably yielding Long.MIN_VALUE
        this.sink = sink;
        this.event = event;
        this.level = level;
    }

    /**
     * Starts the timer. Records a log entry indicating the timer has been started.
     *
     * @return this timer instance.
     */
    public LogTimer start() {
        if (sink != null) {
            LogEntry entry = new LogEntry();
            entry.time = System.currentTimeMillis();
            entry.source = source("started");
            entry.level = this.level;
            entry.message = "Started";
            sink.log(entry);
        }
        started = System.nanoTime();
        return this;
    }

    /**
     * Stops the timer and records the elapsed time in a metric.
     */
    public void stop() {
        long stopped = System.nanoTime();
        if (sink != null && started != Long.MIN_VALUE) {
            LogEntry entry = new LogEntry();
            entry.time = System.currentTimeMillis();
            entry.source = source("elapsed");
            entry.level = this.level;
            entry.data = new LogMetric((stopped - started) / 1000000, "ms");
            entry.message = "Elapsed time: " + entry.data.toString();
            sink.log(entry);
        }
    }

    private String source(String event) {
        StringBuilder sb = new StringBuilder();
        if (this.event != null) {
            sb.append(this.event).append('.');
        }
        sb.append(event);
        return sb.toString();
    }
}
