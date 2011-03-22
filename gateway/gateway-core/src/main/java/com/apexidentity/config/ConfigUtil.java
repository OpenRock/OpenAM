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

package com.apexidentity.config;

// Java Standard Edition
import java.io.File;
import java.util.regex.Pattern;

// ApexIdentity Core Library
import com.apexidentity.json.JSONRepresentation;

/**
 * Utility class for determining configuration directories and files.
 *
 * The configuration directory is computed by this class as either
 * <tt><strong>$AppData/</strong><em>vendor</em><strong>/</strong><em>product</em></strong>/</strong></tt>
 * if the <strong>{@code $AppData}</strong> environment variable is defined (typical in
 * Windows installations), or otherwise
 * <tt><em>user-home</em><strong>/.</strong><em>vendor</em><strong>/</strong><em>product</em><strong>/</strong></tt>
 * otherwise (typical in Unix installations).

 * @author Paul C. Bryan
 */
public class ConfigUtil {

    /** Characters to filter from filenames: (SP) ? < > | * [ ] = + " \ / , . : ; */
    private static final Pattern DIRTY = Pattern.compile("[ \\?<>|\\*\\[\\]=+\\\"\\\\/,\\.:;]");

    /** Static methods only. */
    private ConfigUtil() {
    }

    /**
     * Returns the directory where local configuration files should be stored for the given
     * vendor and product.
     *
     * @param vendor the name of the vendor that released the product.
     * @param product the name of the product released by the vendor.
     * @return the file representing the directory where local configuration files should be stored.
     */
    public static File getDirectory(String vendor, String product) {
        StringBuilder sb = new StringBuilder();
        String appData = System.getenv("AppData");
        if (appData != null) { // windoze
            sb.append(appData);
            sb.append(File.separatorChar);
        }
        else { // eunuchs
            sb.append(System.getProperty("user.home"));
            sb.append(File.separatorChar);
            sb.append('.');
        }
        sb.append(DIRTY.matcher(vendor).replaceAll("_"));
        sb.append(File.separatorChar);
        sb.append(DIRTY.matcher(product).replaceAll("_"));
        return new File(sb.toString());
    }

    /**
     * Returns the local file where configuration information should be stored for the given
     * product instance. Assumes that the file is stored in JSON representation.
     *
     * @param directory the configuration directory where the file is stored.
     * @param instance a unique value identifying the specific instance of the product. 
     * @return the file where local configuration information should be stored.
     */
    public static File getFile(File directory, String instance) {
        return getFile(directory, instance, JSONRepresentation.EXTENSION);
    }

    /**
     * Returns the local file where configuration information should be stored for the given
     * vendor, product and instance. Assumes that the file is stored in JSON representation.
     *
     * @param vendor the name of the vendor that released the product.
     * @param product the name of the product released by the vendor.
     * @param instance a unique value identifying the specific instance of the product. 
     * @return the file where local configuration information should be stored.
     */
    public static File getFile(String vendor, String product, String instance) {
        return getFile(getDirectory(vendor, product), instance);
    }

    /**
     * Returns the local file where configuration information should be stored for the
     * specified directory, product instance and extension.
     *
     * @param directory the configuration directory where the file is stored.
     * @param instance a unique value identifying the specific instance of the product. 
     * @param extension the file extension to use to represent the file type.
     * @return the file where local configuration information should be stored.
     */
    public static File getFile(File directory, String instance, String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(DIRTY.matcher(instance).replaceAll("_"));
        sb.append('.');
        sb.append(extension);
        return new File(directory, sb.toString());
    }

    /**
     * Returns the local file where configuration information should be stored for the
     * specified vendor, product, instance and extension.
     *
     * @param vendor the name of the vendor that released the product.
     * @param product the name of the product released by the vendor.
     * @param instance a unique value identifying the specific instance of the product. 
     * @param extension the file extension to use to represent the file type.
     * @return the file where local configuration information should be stored.
     */
    public static File getFile(String vendor, String product, String instance, String extension) {
        return getFile(getDirectory(vendor, product), instance, extension);
    }
}
