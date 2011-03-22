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

package com.apexidentity.regex;

// Java Standard Edition
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// ApexIdentity Core Library
import com.apexidentity.util.EnumerableMap;

/**
 * Extracts regular expression patterns and/or applied templates from character streams. If
 * a pattern has a corresponding template, then the template will be applied to yield the
 * extraction result. If no corresponding template exists, then the entire match is yielded
 * verbatim.
 *
 * @author Paul C. Bryan
 * @see PatternTemplate
 */
public class StreamPatternExtractor {

    /** Mapping of names to regular expression patterns to extract from the stream. */
    public final LinkedHashMap<String, Pattern> patterns = new LinkedHashMap<String, Pattern>();

    /** Mapping of names to optional templates to use for yielding pattern match results. */
    public final HashMap<String, PatternTemplate> templates = new HashMap<String, PatternTemplate>();

    /**
     * Extracts regular expression patterns from a character streams. Returns a mapping of
     * names to the results of pattern extraction (literal match or applied template).
     * <p>
     * Patterns are resolved lazily; only as much of the stream is read in order to satisfy
     * a request for a specific key in the returned map.
     * <p>
     * <strong>Note:</strong> If an {@link IOException} is encountered when accessing the
     * stream, the exception is caught and suppressed. This results in {@code null} values
     * being returned for values not extracted before the exception occurred.
     *
     * @param reader the character stream .
     * @return a mapping of names to pattern match results (literal match or applied template).
     */
    public EnumerableMap<String, String> extract(final Reader reader) {
        return new EnumerableMap<String, String>() {
            private final HashMap<String, String> values = new HashMap<String, String>();
            private Map.Entry[] entries = patterns.entrySet().toArray(new Map.Entry[patterns.size()]);
            private final StreamPatternMatches matches = new StreamPatternMatches(reader, patterns.values(), true);
            @Override public String get(Object key) {
                String value = values.get(key);
                if (keySet().contains(key)) {
                    try {
                        while (value == null && matches.hasNext()) {
                            Matcher matcher = matches.next();
                            Pattern pattern = matcher.pattern();
                            for (int n = 0; n < entries.length; n++) {
                                if (entries[n] != null) {
                                    String entryKey = (String)(entries[n].getKey());
                                    Pattern entryPattern = (Pattern)(entries[n].getValue());
                                    if (entryPattern == pattern) { // identity equality for accurate (and quick) correlation
                                        PatternTemplate t = templates.get(entryKey);
                                        String v = (t != null ? t.applyTo(matcher) : matcher.group());
                                        values.put(entryKey, value);
                                        if (entryKey.equals(key)) { // found the value we were looking for
                                            value = v;
                                        }
                                        entries[n] = null; // used entry; deference for efficiency
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException ioe) {
                        // any failure to read stream yields null value in mapping
                    }
                }
                return value;
            }
            @Override public boolean containsKey(Object key) {
                return patterns.containsKey(key);
            }
            @Override public Set<String> keySet() {
                return patterns.keySet();
            }
            @Override public int size() {
                return patterns.size();
            }
        };
    }
}
