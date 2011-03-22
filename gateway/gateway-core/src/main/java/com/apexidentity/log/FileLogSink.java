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

// Java Standard Edition
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

// ApexIdentity Core Library
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.NodeException;
import com.apexidentity.util.ISO8601;

/**
 * A sink that writes log entries to a file.
 *
 * @author Paul C. Bryan
 */
public class FileLogSink implements LogSink {

    /** TODO: Description. */
    public File file;

    /** The level of log entries to display in the console (default: {@link LogLevel#INFO INFO}). */
    public LogLevel level = LogLevel.INFO;

    /** Character set to encode log output with (default: UTF-8). */
    public Charset charset = Charset.forName("UTF-8");

    /** TODO: Description. */
    private PrintWriter writer;

    @Override
    public void log(LogEntry entry) {
        if (isLoggable(entry.source, entry.level)) {
            synchronized(this) {
                try {
                    if (!file.exists() || writer == null) {
                        if (writer != null) {
                            writer.close();
                        }
                        writer = new PrintWriter(new OutputStreamWriter(
                         new FileOutputStream(file, true), charset), true);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(ISO8601.format(entry.time)).append(':').append(entry.source).append(':');
                    sb.append(entry.level).append(':').append(entry.message);
                    if (entry.data != null) {
                        sb.append(':').append(entry.data.toString());
                    }
                    writer.println(sb.toString());
                }
                catch (IOException ioe) {
                    System.err.println(ioe); // not much else we can do
                }
            }
        }
    }

    @Override
    public boolean isLoggable(String source, LogLevel level) {
        return (level.compareTo(this.level) >= 0);
    }

    /** Creates and initializes a console sink in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            FileLogSink sink = new FileLogSink();
            sink.level = config.get("level").defaultTo(sink.level.toString()).asEnum(LogLevel.class);
            sink.file = config.get("file").required().asFile();
            try { // try opening file to ensure it's writable at config time
                FileOutputStream out = new FileOutputStream(sink.file, true);
                out.close();
            }
            catch (IOException ioe) {
                throw new NodeException(config.get("file"), ioe);
            }
            return sink;
        }
    }
}
