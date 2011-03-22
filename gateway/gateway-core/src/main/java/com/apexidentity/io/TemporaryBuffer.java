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
import java.io.File;
import java.io.IOException;

/**
 * A buffer that fist uses memory, then a temporary file for data storage. Initially, a
 * {@link MemoryBuffer} is used; when the memory buffer limit is exceeded it promotes to
 * the use of a {@link FileBuffer}.
 *
 * @author Paul C. Bryan
 */
public class TemporaryBuffer implements Buffer {

    /** File used for temporary storage. */
    private File file = null;

    /** The directory where temporary files are created. */
    private File directory;

    /** The length limit of the file buffer. */
    private int fileLimit;

    /** The buffer currently in use. */
    private Buffer buffer;

    /**
     * Constructs a new temporary buffer.
     *
     * @param initialLength the initial length of memory buffer byte array.
     * @param memoryLimit the length limit of the memory buffer.
     * @param fileLimit the length limit of the file buffer.
     * @param directory the directory where temporary files are created, or {@code null} to use the system-dependent default temporary directory.
     */
    public TemporaryBuffer(int initialLength, int memoryLimit, int fileLimit, File directory) {
        buffer = new MemoryBuffer(initialLength, memoryLimit);
        this.fileLimit = fileLimit;
        this.directory = directory;
    }

    @Override
    public int read(int pos, byte[] b, int off, int len) throws IOException {
        notClosed();
        return buffer.read(pos, b, off, len);
    }

    @Override
    public void append(byte[] b, int off, int len) throws IOException, OverflowException {
        notClosed();
        try {
            buffer.append(b, off, len);
        }
        catch (OverflowException oe) {
            promote(); // may throw OverflowException to indicate no promotion possible
            append(b, off, len); // recursively retry after promotion
        }
    }

    @Override
    public int length() throws IOException {
        notClosed();
        return buffer.length();
    }

    @Override
    public void close() throws IOException {
        if (buffer != null) {
            try {
                buffer.close();
            }
            finally {
                buffer = null;
                if (file != null) {
                    file.delete();
                }
                file = null;
            }
        }
    }

    /**
     * Throws an {@link IOException} if the buffer is closed.
     */
    private void notClosed() throws IOException {
        if (buffer == null) {
            throw new IOException("buffer is closed");
        }
    }

    private void promote() throws IOException, OverflowException {
        if (buffer instanceof MemoryBuffer) {
            MemoryBuffer membuf = (MemoryBuffer)buffer;
            file = File.createTempFile("buf", null, directory);
            buffer = new FileBuffer(file, fileLimit);
            buffer.append(membuf.data, 0, membuf.length()); // accesses byte array directly
            membuf.close();
        }
        else { // no further promotion possible
            throw new OverflowException();
        }
    }
}
