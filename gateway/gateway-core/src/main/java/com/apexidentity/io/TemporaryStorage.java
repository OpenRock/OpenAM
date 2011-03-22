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

// ApexIdentity Core Library
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.model.ModelException;
import com.apexidentity.util.Factory;

/**
 * Allocates temporary buffers for caching streamed content during request processing.
 *
 * @author Paul C. Bryan
 */
public class TemporaryStorage implements Factory<Buffer> {

    /**
     * The initial length of memory buffer byte array. Default: 8 KiB.
     */
    public int initialLength = 8 * 1024;

    /**
     * The length limit of the memory buffer. Attempts to exceed this limit will result in
     * promoting the buffer from a memory to a file buffer. Default: 64 KiB.
     */
    public int memoryLimit = 64 * 1024;

    /**
     * The length limit of the file buffer. Attempts to exceed this limit will result in an
     * {@link OverflowException} being thrown. Default: 1 MiB.
     */
    public int fileLimit = 1 * 1024 * 1024;

    /**
     * The directory where temporary files are created. If {@code null}, then the
     * system-dependent default temporary directory will be used. Default: {@code null}.
     *
     * @see java.io.File#createTempFile(String, String, File)
     */
    public File directory;

    /**
     * Creates and returns a new instance of a temporary buffer.
     */
    public Buffer newInstance() {
        return new TemporaryBuffer(initialLength, memoryLimit, fileLimit, directory);
    }

    /** Creates and initializes a temporary storage object in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            TemporaryStorage storage = new TemporaryStorage();
            storage.initialLength = config.get("initialLength").defaultTo(storage.initialLength).asInteger();
            storage.memoryLimit = config.get("memoryLimit").defaultTo(storage.memoryLimit).asInteger();
            storage.fileLimit = config.get("fileLimit").defaultTo(storage.fileLimit).asInteger();
            storage.directory = config.get("directory").asFile();
            return storage;
        }
    }
}
