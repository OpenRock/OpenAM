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

package com.apexidentity.resource;

// Java Standard Edition
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Accesses local file resources.
 *
 * @author Paul C. Bryan
 */
public class FileResource implements Resource {

    /** The file that the resource is providing access to. */
    public final File file;

    /**
     * Constructs a new file resource object to access the specified file.
     *
     * @param file the file to be accessed.
     */
    public FileResource(File file) {
        this.file = file;
    }

    @Override
    public void create(Representation representation) throws ResourceException {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new ResourceException("couldn't create directory " + parent.getPath());
            }
        }
        try {
            if (!file.createNewFile()) {
                throw new ResourceException("resource already exists");
            }
        }
        catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
        update(representation);
    }

    @Override
    public void read(Representation representation) throws ResourceException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            representation.read(in);
        }
        catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                }
            }
        }
    }

    @Override
    public void update(Representation representation) throws ResourceException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            representation.write(out);
        }
        catch (IOException ioe) {
            throw new ResourceException(ioe);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ioe) {
                }
            }
        }
    }

    @Override
    public void delete() throws ResourceException {
        if (!file.delete()) {
            throw new ResourceException("file resource could not be deleted");
        }
    }

    @Override
    public URI getURI() {
        return file.toURI();
    }

    /**
     * Tests whether the file denoted by the resource exists.
     *
     * @return {@code true} if and only if the file denoted by the resource exists.
     */
    public boolean exists() {
        return file.exists();
    }

    /**
     * Creates resource objects that access file resources. Supports URIs with a
     * {@code "file://"} or {@code null} scheme.
     */
    public static class Factory implements ResourceFactory {
        @Override public Resource newInstance(URI uri) throws ResourceException {
            String scheme = uri.getScheme();
            if (scheme == null || "file".equalsIgnoreCase(scheme)) {
                return new FileResource(new File(uri.getPath()));
            }
            return null; // scheme not supported
        }
    }
}
