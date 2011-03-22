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

package com.apexidentity.text;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads records with delimiter-separated values from a character stream.
 *
 * @author Paul C. Bryan
 */
public class SeparatedValuesReader {

    private static int CR = '\r';
    private static int LF = '\n';

    /** The character stream to read from. */
    private final Reader input;

    /** The separator specification to parse the file with. */
    private final Separator separator;

    /** The number of  expected in the record; adjusted and used to set the ArrayList initial capacity. */
    private int fields = 1;

    /** Read-ahead of next character (needed to check separator escapes). */
    private int next = -1;

    /** Flag indicating that the parse state is currently within quotations. */
    private boolean quoted = false;

    /**
     * Constructs a new separated values reader, to read a character stream from the
     * specified reader and use the specified separator specification.
     *
     * @param input the character stream to read from.
     * @param separator the separator specification to parse the file with.
     */
    public SeparatedValuesReader(Reader input, Separator separator) {
        this.input = input;
        this.separator = separator;
    }

    /**
     * Reads the next record from the character input stream.
     *
     * @return a list of fields contained in the next record, or {@code null} if the end of stream has been reached.
     * @throws IOException if an I/O exception occurs.
     */
    public List<String> next() throws IOException {
        ArrayList<String> list = new ArrayList<String>(this.fields);
        StringBuilder sb = new StringBuilder();
        int c;
        boolean escaped = false;
        while ((c = read()) != -1) {
            if (escaped) {
                sb.append((char)c);
                escaped = false;
            }
            else if (c == separator.escape) {
                escaped = true;
            }
            else if (c == separator.quote && sb.length() == 0) {
                quoted = true;
            }
            else if (c == separator.quote && quoted) {
                c = read();
                if (c == separator.quote) {
                    sb.append((char)c);
                }
                else {
                    next = c;
                    quoted = false;
                }
            }
            else if (c == separator.character && !quoted) {
                list.add(sb.toString());
                sb.setLength(0);
            }
            else if (c == LF && !quoted) {
                break;
            }
            else {
                sb.append((char)c);
            }
        }
        if (list.size() > 0 || sb.length() > 0) {
            list.add(sb.toString());
        }
        if (list.size() == 0 && c == -1) {
            return null; // end of stream
        }
        else {
            this.fields = Math.max(this.fields, list.size()); // more efficient array allocation for next record
            return list;
        }
    }

    /**
     * Closes the reader and releases any system resources associated with it. Once the
     * reader has been closed, further {@code next()} invocations will throw an
     * {@code IOException}. Closing a previously closed reader has no effect.
     */
    public void close() {
        try {
            input.close();
        }
        catch (IOException ioe) {
            // exceptions closing the reader are not reported
        }
    }

    private int read() throws IOException {
        int c;
        if (next != -1) {
            c = next;
            next = -1;
        }
        else {
            c = input.read();
        }
        if (c == CR && !quoted) {
            int n = input.read();
            if (n == LF) { // translate unquoted CR+LF into LF
                c = LF;
            }
            else { // CR not followed by LF; remember read value and return CR 
                next = n;
            }
        }
        return c;
    }
}
