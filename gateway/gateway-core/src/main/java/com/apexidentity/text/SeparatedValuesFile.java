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

// Java Standard Edition
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows records to be retrieved from a delimier-separated file using key and value. Once
 * constructed, an instance of this class is thread-safe, meaning the object can be long-lived,
 * and multiple concurrent calls to {@link #getRecord(String, String) getRecord} is fully
 * supported.
 *
 * @author Paul C. Bryan
 */
public class SeparatedValuesFile {

    /** The file containing the separated values to be read. */
    public File file;

    /** The character set the file is encoded in. */
    public Charset charset;

    /** The separator specification to split lines into fields. */
    public Separator separator;

    /** Does the first line of the file contain the set of defined field keys. */
    public boolean header;

    /** Explicit field keys in the order they appear in a record, overriding any existing field header, or {@code null} to use field header. */
    public List<String> fields = new ArrayList<String>();

    /**
     * Returns a record from the file where the specified key is equal to the specified value.
     *
     * @param key the key to use to lookup the record
     * @param value the value that the key should have to find a matching record.
     * @return the record with the matching value, or {@code null} if no such record could be found.
     * @throws IOException if an I/O exception occurs.
     */
    public Map<String, String> getRecord(String key, String value) throws IOException {
        Map<String, String> map = null;
        SeparatedValuesReader reader = new SeparatedValuesReader(new InputStreamReader(new FileInputStream(file), charset), separator);
        try {
            List<String> fields = this.fields;
            if (header) { // first line in the file is the field header
                List<String> record = reader.next();
                if (record != null && fields.size() == 0) { // explicit fields not specified
                    fields = record; // use header fields
                }
            }
            if (fields.size() > 0) {
                int index = fields.indexOf(key);
                if (index >= 0) { // requested key exists
                    List<String> record;
                    while ((record = reader.next()) != null) {
                        if (record.get(index).equals(value)) {
                            map = new HashMap<String, String>(fields.size());
                            Iterator<String> fi = fields.iterator();
                            Iterator<String> ri = record.iterator();
                            while (fi.hasNext() && ri.hasNext()) {
                                map.put(fi.next(), ri.next()); // assign field-value pairs in map
                            }
                            break;
                        }
                    }
                }
            }
        }
        finally {
            reader.close();
        }
        return map;
    }
}
