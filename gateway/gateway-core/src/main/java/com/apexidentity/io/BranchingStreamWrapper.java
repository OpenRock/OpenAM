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
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

// ApexIdentity Core Library
import com.apexidentity.util.Factory;

/**
 * Wraps an standard input stream with a stream that can branch to perform divergent reads.
 * All divergence between branches is maintained in a temporary buffer.
 * <p>
 * <strong>Note:</strong> This stream and any branches it creates are not safe for use by
 * multiple concurrent threads.
 *
 * @author Paul C. Bryan
 */
public class BranchingStreamWrapper extends BranchingInputStream {

    /** A shared object by all branches of the same input stream. */ 
    private Trunk trunk;

    /** Points to this branch's parent. */
    private BranchingStreamWrapper parent;

    /** This branch's position relative to the trunk buffer. */
    private int position;

    /**
     * Constructs a new branching input stream to wrap another input stream.
     *
     * If the stream being wrapped is a branching input stream, this constructor will simply
     * branch off of that existing stream rather than wrapping it with another branching
     * input stream.
     *
     * @param in the stream to be wrapped.
     * @param bufferFactory an object that can create new temporary buffers (e.g. @link TemporaryStorage}).
     */
    public BranchingStreamWrapper(InputStream in, Factory<Buffer> bufferFactory) {
        if (in instanceof BranchingStreamWrapper) { // branch off of existing trunk
            BranchingStreamWrapper bsw = (BranchingStreamWrapper)in;
            this.parent = bsw;
            this.trunk = bsw.trunk;
            this.position = bsw.position;
            this.trunk.branches.add(this);
        }
        else { // wrapping a non-wrapping stream; sprout a new trunk
            parent = null;
            trunk = new Trunk();
            trunk.branches.add(this);
            trunk.in = in;
            trunk.bufferFactory = bufferFactory;
        }
    }

    @Override
    public BranchingStreamWrapper branch() throws IOException {
        notClosed();
        return new BranchingStreamWrapper(this, null); // constructor will branch
    }

    @Override
    public boolean isClosed() {
        return (trunk == null);
    }

    @Override
    public BranchingStreamWrapper getParent() {
        return parent;
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return the next byte of data, or {@code -1} if the end of the stream is reached.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        return (read(b, 0, 1) > 0 ? (b[0] & 0xff) : -1);
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
    public int read(byte[] b) throws IOException {
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
    public int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || len > b.length - off) { // throws NullPointerException
            throw new IndexOutOfBoundsException();
        }
        notClosed();
        int n;
        if ((n = readBuffer(b, off, len)) == 0) { // try reading from buffer first
            if ((n = trunk.in.read(b, off, len)) >= 0) { // not buffered; cascade the call
                writeBuffer(b, off, n); // write result to buffer if necessary
            }
        }
        return n;
    }

    /**
     * Skips over and discards {@code n} bytes of data from this input stream.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public long skip(long n) throws IOException {
        if (n < 0) {
            return 0;
        }
        notClosed();
        if (trunk.buffer == null && trunk.branches.size() == 1) {
            return trunk.in.skip(n); // not buffering; safely cascade call
        }
        // stream nowhere, just to buffer (or unbuffer) the result skipped
        return Streamer.stream(this, new NullOutputStream(), (int)Math.min(Integer.MAX_VALUE, n));
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next invocation of a method for this input
     * stream.
     *
     * @throws IOException if an I/O exception occurs.
     */
    @Override
    public int available() throws IOException {
        notClosed();
        if (trunk.buffer != null) {
            int length = trunk.buffer.length();
            if (position < length) { // this branch is still reading from buffer
                return length - position; // report buffer availability
            }
        }
        return trunk.in.available();
    }

    @Override
    public void close() throws IOException {
        if (trunk != null) { // multiple calls to close are harmless
            try {
                closeBranches();
                trunk.branches.remove(this);
                reviewBuffer(); // close buffer if applicable
                if (trunk.branches.size() == 0) { // last one out turn off the lights
                    trunk.in.close();
                }
            }
            finally { // if all else fails, this branch thinks it's closed
                trunk = null;
            }
        }
    }

    @Override
    public void closeBranches() throws IOException {
        if (trunk != null) { // multiple calls are harmless
            ArrayList<BranchingStreamWrapper> branches = new ArrayList<BranchingStreamWrapper>(trunk.branches);
            for (BranchingStreamWrapper branch : branches) {
                if (branch.parent == this) {
                    branch.close(); // recursively closes its children
                }
            }
        }
    }

    /**
     * Closes this branching stream and all of the branches created from it.
     */
    @Override
    public void finalize() {
        try {
            close();
        }
        catch (IOException ioe) {
            // inappropriate to throw an exception when object is being collected
        }
    }

    /**
     * Closes the trunk buffer if there is no divergence between branches and all remaining
     * branch positions are outside the buffer.
     *
     * @throws IOException if an I/O exception occurs.
     */
    private void reviewBuffer() throws IOException {
        if (trunk.buffer == null) {
            return; // no buffer to review
        }
        int length = trunk.buffer.length();
        for (BranchingStreamWrapper branch : trunk.branches) {
            if (branch.position < length) {
                return; // branch is still using buffer; leave it alone
            }
        } // any remaining branches are non-divergent and outside buffer
        trunk.buffer.close();
        trunk.buffer = null;
    }

    /**
     * Throws an {@link IOException} if the stream is closed.
     */
    private void notClosed() throws IOException {
        if (trunk == null) {
            throw new IOException("stream is closed");
        }
    }

    private int readBuffer(byte[] b, int off, int len) throws IOException {
        int n = 0;
        if (trunk.buffer != null && trunk.buffer.length() > position) {
            n = trunk.buffer.read(position, b, off, len);
        }
        position += n;
        reviewBuffer(); // see if the buffer can be closed after this operation
        return n;
    }

    private void writeBuffer(byte[] b, int off, int len) throws IOException {
        if (trunk.buffer == null && trunk.branches.size() > 1) {
            trunk.buffer = trunk.bufferFactory.newInstance(); // diverging branches; allocate new buffer
            for (BranchingStreamWrapper branch : trunk.branches) {
                branch.position = 0; // set each branch position to beginning of new buffer
            }
        }
        if (trunk.buffer != null) {
            trunk.buffer.append(b, off, len);
            position += len;
        }
    }

    /** Object shared by all branches. */
    private class Trunk {
        /** Keeps track of all branches on this trunk. */
        private ArrayList<BranchingStreamWrapper> branches = new ArrayList<BranchingStreamWrapper>();
        /** The input stream being wrapped by the branches. */
        private InputStream in;
        /** An object that creates new temporary buffers. */
        private Factory<Buffer> bufferFactory;
        /** A buffer to track diverging streams. Is {@code null} if there is no divergence. */ 
        private Buffer buffer;
    }
}
