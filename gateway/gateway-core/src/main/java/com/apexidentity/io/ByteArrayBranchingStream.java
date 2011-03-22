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

package com.apexidentity.io;

// Java Standard Edition
import java.io.IOException;

/**
 * Wraps a byte array with a stream that can branch to perform divergent reads.
 *
 * @author Paul C. Bryan
 */
public class ByteArrayBranchingStream extends BranchingInputStream {

    /** Branch that this was spawned from, or {@code null} if this is the trunk. */
    private ByteArrayBranchingStream parent = null;

    /** The index of the next byte to read from the byte array. */
    private int position = 0;

    /** The currently marked position in the stream. */
    private int mark = -1;

    /** The byte array to expose as the input stream. */
    private byte[] data;

    /**
     * Constructs a new branching input stream that wraps a byte array.
     *
     * @param data byte array to wrap with the branching input stream.
     */
    public ByteArrayBranchingStream(byte[] data) {
        this.data = data;
    }

    @Override
    public ByteArrayBranchingStream branch() {
        ByteArrayBranchingStream branch = new ByteArrayBranchingStream(data);
        branch.position = this.position;
        branch.parent = this;
        return branch;
    }

    @Override
    public ByteArrayBranchingStream getParent() {
        return parent;
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return the next byte of data, or {@code -1} if the end of the stream is reached.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public synchronized int read() {
        return (position < data.length ? data[position++] & 0xff : -1);
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer
     * array {@code b}.
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or {@code -1} is there is no more data because the end of the stream has been reached. 
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }

    /**
     * Reads up to {@code len} bytes of data from the input stream into an array of bytes.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in array {@code b} at which the data is written.
     * @param len the maximum number of bytes to read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is no more data because the end of the stream has been reached.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public synchronized int read(byte b[], int off, int len) {
        if (off < 0 || len < 0 || len > b.length - off) { // throws NullPointerException
            throw new IndexOutOfBoundsException();
        }
        if (position >= data.length) { // end of stream has been reached
            return -1;
        }
        len = Math.min(len, data.length - position);
        System.arraycopy(data, position, b, off, len);
        position += len;
        return len;
    }

    /**
     * Skips over and discards {@code n} bytes of data from this input stream.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public synchronized long skip(long n) {
        if (n <= 0) {
            return 0;
        }
        n = Math.min(n, data.length - position);
        position += n;
        return n;
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next invocation of a method for this input
     * stream.
     *
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public synchronized int available() {
        return data.length - position;
    }

    /**
     * Returns {@code true} unconditionally; mark and reset are supported.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the current position in this input stream.
     *
     * @param readlimit the maximum limit of bytes that can be read before the mark position becomes invalid.
     */
    @Override
    public void mark(int readlimit) {
        mark = position;
    }

    /**
     * Repositions this stream to the position at the time the {@code mark} method was last
     * called on this input stream.
     *
     * @throws IOException if the position was not previously marked.
     */
    @Override
    public synchronized void reset() throws IOException {
        if (mark < 0) {
            throw new IOException("position was not marked");
        }
        position = mark;
    }

    /**
     * Has no effect.
     */
    @Override
    public void close() {
    }

    /**
     * Returns {@code false} unconditionally.
     */
    @Override
    public boolean isClosed() {
        return false;
    }

    /**
     * Has no effect.
     */
    @Override
    public void closeBranches() {
    }
}
