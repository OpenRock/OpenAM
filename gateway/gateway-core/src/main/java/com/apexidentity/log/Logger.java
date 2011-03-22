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
 * Wraps a log sink and exposes a set of convenience methods for various logging activities.
 *
 * @author Paul C. Bryan
 */
public class Logger implements LogSink {

    /** The sink to write log entries to. */
    private final LogSink sink;

    /** The base source to write all log entries with. */
    private final String source;

    /**
     * Constructs a new logger. If the supplied sink is {@code null}, then a
     * {@link NullLogSink} will be used.
     *
     * @param sink the sink to write log entries to.
     * @param source the base source to write all log entries with.
     */
    public Logger(LogSink sink, String source) {
        this.sink = (sink != null ? sink : new NullLogSink());
        this.source = source;
    }

    /**
     * Logs the message at the specified log level.
     *
     * @param level the log level to set in the log entry.
     * @param message the message to be logged.
     */
    public void logMessage(LogLevel level, String message) {
        LogEntry entry = newEntry();
        entry.level = level;
        entry.message = message;
        log(entry);
    }

    /**
     * Logs the specified exception.
     *
     * @param level the log level to set in the log entry.
     * @param throwable the exception to be logged.
     * @return the exception being logged.
     */
    public <T extends Throwable> T logException(LogLevel level, T throwable) {
        LogEntry entry = newEntry();
        entry.level = level;
        entry.message = throwable.toString();
        entry.data = throwable;
        log(entry);
        return throwable;
    }

    /**
     * Logs the specified message at the {@code ERROR} log level.
     *
     * @param message the message to be logged.
     */
    public void error(String message) {
        logMessage(LogLevel.ERROR, message);
    }

    /**
     * Logs the specified exception at the {@code ERROR} log level.
     *
     * @param throwable the exception to be logged.
     * @return the exception being logged.
     */
    public <T extends Throwable> T error(T throwable) {
        return logException(LogLevel.ERROR, throwable);
    }

    /**
     * Logs the specified message at the {@code WARNING} log level.
     *
     * @param message the message to be logged.
     */
    public void warning(String message) {
        logMessage(LogLevel.WARNING, message);
    }

    /**
     * Logs the specified exception at the {@code WARNING} log level.
     *
     * @param throwable the exception to be logged.
     * @return the exception being logged.
     */
    public <T extends Throwable> T warning(T throwable) {
        return logException(LogLevel.WARNING, throwable);
    }

    /**
     * Logs the specified message at the {@code INFO} log level.
     *
     * @param message the message to be logged.
     */
    public void info(String message) {
        logMessage(LogLevel.INFO, message);
    }

    /**
     * Logs the specified exception at the {@code INFO} log level.
     *
     * @param throwable the exception to be logged.
     * @return the exception being logged.
     */
    public <T extends Throwable> T info(T throwable) {
        return logException(LogLevel.INFO, throwable);
    }

    /**
     * Logs the specified message at the {@code CONFIG} log level.
     *
     * @param message the message to be logged.
     */
    public void config(String message) {
        logMessage(LogLevel.CONFIG, message);
    }

    /**
     * Logs the specified exception at the {@code CONFIG} log level.
     *
     * @param throwable the exception to be logged.
     * @return the exception being logged.
     */
    public <T extends Throwable> T config(T throwable) {
        return logException(LogLevel.CONFIG, throwable);
    }

    /**
     * Logs the specified message at the {@code DEBUG} log level.
     *
     * @param message the message to be logged.
     */
    public void debug(String message) {
        logMessage(LogLevel.DEBUG, message);
    }

    /**
     * Logs the specified exception at the {@code DEBUG} log level.
     *
     * @param throwable the exception to be logged.
     * @return the exception being logged.
     */
    public <T extends Throwable> T debug(T throwable) {
        return logException(LogLevel.DEBUG, throwable);
    }

    /**
     * Logs the specified message at the {@code TRACE} log level.
     *
     * @param message the message to be logged.
     */
    public void trace(String message) {
        logMessage(LogLevel.TRACE, message);
    }

    /**
     * Logs the specified exception at the {@code TRACE} log level.
     *
     * @param throwable the exception to be logged.
     * @return the exception being logged.
     */
    public <T extends Throwable> T trace(T throwable) {
        return logException(LogLevel.TRACE, throwable);
    }

    /**
     * Returns a new timer to measure elapsed time. Entries are written to the log with a
     * {@code STAT} log level.
     */
    public LogTimer getTimer() {
        return new LogTimer(this, LogLevel.STAT);
    }

    /**
     * Returns a new timer to measure elapsed time for a specified event. The event is
     * appended to the source in hierarchical fashion. Entries are written to the log with a
     * {@code STAT} log level.
     *
     * @param event the event that is being timed.
     */
    public LogTimer getTimer(String event) {
        return new LogTimer(this, LogLevel.STAT, event);
    }

    /**
     * Logs an entry. This implementation will prepend the logger source to all log entries.
     *
     * @param entry the entry to be logged.
     */
    @Override
    public void log(LogEntry entry) {
        entry.source = source(source, entry.source);
        sink.log(entry);
    }

    /**
     * Returns {@code true} if the entry may be logged, given the specified event and log
     * level.
     *
     * @param event the event (sub-source) that is intended to be logged.
     * @param level the log level of the entry to be logged.
     * @return {@code true} if the entry may be logged.
     */
    @Override
    public boolean isLoggable(String event, LogLevel level) {
        return sink.isLoggable(source(this.source, event), level);
    }

    /**
     * Returns {@code true} if the entry may be logged, given the source of this logger and
     * the specified log level.
     *
     * @param level the log level of the entry to be logged.
     * @return {@code true} if the entry may be logged.
     */
    public boolean isLoggable(LogLevel level) {
        return sink.isLoggable(this.source, level);
    }

    /**
     * Returns a new log entry, initialized with timestamp and source.
     */
    private LogEntry newEntry() {
        LogEntry entry = new LogEntry();
        entry.time = System.currentTimeMillis();
        entry.source = this.source;
        return entry;
    }

    private String source(String source, String event) {
        StringBuilder sb = new StringBuilder();
        if (source != null) {
            sb.append(source);
        }
        if (event != null) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(event);
        }
        return sb.toString();
    }
}
