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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A buffer that uses a local file for data storage.
 * <p>
 * <strong>Note:</strong> This implementation is not synchronized. If multiple threads access
 * a buffer concurrently, threads that append to the buffer should synchronize on the instance
 * of this object.
 *
 * @author Paul C. Bryan
 */
public class FileBuffer implements Buffer {

    /** File to store buffered data in. */
    private RandomAccessFile raf;

    /** Maximum file size, after which an {@link OverflowException} will be thrown. */
    private final int limit;

    /**
     * Constructs a new file buffer.
     *
     * @param file the file to use as storage for the buffer.
     * @param limit the buffer length limit, after which an {@link OverflowException} will be thrown.
     * @throws FileNotFoundException if the file cannot be created or opened for writing.
     * @throws SecurityException if a security manager denies access to the specified file.
     */
    public FileBuffer(File file, int limit) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
        this.limit = limit;
    }

    @Override
    public int read(int pos, byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || len > b.length - off) { // throws NullPointerException
            throw new IndexOutOfBoundsException();
        }
        notClosed();
        int n = 0;
        if (pos < raf.length()) {
            synchronized(raf) {
                raf.seek(pos);
                if ((n = raf.read(b, off, len)) == -1) {
                    n = 0; // obey the contract of buffer reads
                }
            }
        }
        return n;
    }

    @Override
    public void append(byte[] b, int off, int len) throws IOException, OverflowException {
        if (off < 0 || len < 0 || len > b.length - off) { // throws NullPointerException
            throw new IndexOutOfBoundsException();
        }
        notClosed();
        synchronized(raf) {
            int rafLength = (int)Math.min(Integer.MAX_VALUE, raf.length());
            if (rafLength + len > limit) {
                throw new OverflowException();
            }
            raf.seek(rafLength);
            raf.write(b, off, len);
        }
    }

    @Override
    public int length() throws IOException {
        notClosed();
        return (int)Math.min(Integer.MAX_VALUE, raf.length());
    }

    @Override
    public void close() throws IOException {
        if (raf != null) {
            try {
                raf.close();
            }
            finally {
                raf = null;
            }
        }
    }

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
     * Throws an {@link IOException} if the buffer is closed.
     */
    private void notClosed() throws IOException {
        if (raf == null) {
            throw new IOException("buffer is closed");
        }
    }
}
