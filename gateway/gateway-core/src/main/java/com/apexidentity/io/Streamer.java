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

package com.apexidentity.io;

// Java Standard Edition
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Utility class that can stream to and from streams.
 *
 * @author Paul C. Bryan
 */
public class Streamer {

    /** Size of buffer to use during streaming. */
    private static final int BUF_SIZE = 8 * 1024;

    /** Static methods only. */
    private Streamer() {
    }

    /**
     * Streams all data from an input stream to an output stream.
     *
     * @param in the input stream to stream the data from.
     * @param out the output stream to stream the data to.
     * @throws IOException if an I/O exception occurs.
     */
    public static void stream(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        int n;
        while ((n = in.read(buf, 0, BUF_SIZE)) != -1) {
            out.write(buf, 0, n);
        }
    }

    /**
     * Streams data from an input stream to an output stream, up to a specified
     * length.
     *
     * @param in the input stream to stream the data from.
     * @param out the output stream to stream the data to.
     * @param len the number of bytes to stream.
     * @return the actual number of bytes streamed.
     * @throws IOException if an I/O exception occurs.
     */
    public static int stream(InputStream in, OutputStream out, int len) throws IOException {
        int remaining = len;
        byte[] buf = new byte[BUF_SIZE];
        int n;
        while (remaining > 0 && (n = in.read(buf, 0, Math.min(remaining, BUF_SIZE))) >= 0) {
            out.write(buf, 0, n);
            remaining -= n;
        }
        return len - remaining;
    }

    /**
     * Streams all characters from a reader to a writer.
     *
     * @param in reader to stream the characters from.
     * @param out the writer to stream the characters to.
     * @throws IOException if an I/O exception occurs.
     */
    public static void stream(Reader in, Writer out) throws IOException {
        char[] buf = new char[BUF_SIZE];
        int n;
        while ((n = in.read(buf, 0, BUF_SIZE)) != -1) {
            out.write(buf, 0, n);
        }
    }
}
