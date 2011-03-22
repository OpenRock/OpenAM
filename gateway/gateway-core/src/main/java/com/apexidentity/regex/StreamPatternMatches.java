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
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Iterates through multiple regular expression matches within a character stream.
 *
 * @author Paul C. Bryan
 */
public class StreamPatternMatches implements Closeable {

    /** The patterns to match. */
    private final Pattern[] patterns;

    /** The character stream to search. */
    private BufferedReader input;

    /** Should patterns be discarded after they yield a match. */
    private boolean discard;

    /** The string pattern match iterator to search each line of input. */
    private StringPatternMatches matches = null;

    /**
     * Constructs a new stream pattern match iterator. If {@code discard} is {@code true},
     * then a pattern is discarded after it is first matched.
     *
     * @param input the character stream to match regular expression patterns against.
     * @param patterns a collection of regular expression patterns to match.
     * @param discard indicates patterns be discarded after they yield a match. 
     */
    public StreamPatternMatches(Reader input, Collection<Pattern> patterns, boolean discard) {
        this.input = (BufferedReader)(input instanceof BufferedReader ? input : new BufferedReader(input));
        this.patterns = patterns.toArray(new Pattern[patterns.size()]);
        this.discard = discard;
    }

    /**
     * Returns the next match from the character stream. Matches are returned in the order
     * they are encountered in the character stream, then by the order they are expressed in
     * the supplied patterns collection.
     *
     * @throws IOException if an I/O exception occurs.
     * @throws NoSuchElementException if the reader has no more matches.
     */
    public Matcher next() throws IOException {
        readahead();
        if (matches == null) {
            throw new NoSuchElementException();
        }
        Matcher matcher = matches.next();
        if (discard) {
            Pattern pattern = matcher.pattern();
            for (int n = 0; n < patterns.length; n++) {
                if (patterns[n] == pattern) {
                    patterns[n] = null;
                    break;
                }
            }
        }
        return matcher;
    }

    /**
     * Returns {@code true} if the character stream has more matches.
     *
     * @throws IOException if an I/O exception occurs.
     */
    public boolean hasNext() throws IOException {
        readahead();
        return (matches != null && matches.hasNext());
    }

    /**
     * Closes this character stream, as well as the the reader it its iterating over.
     *
     * @throws IOException if an I/O exception occurs.
     */
    public void close() throws IOException {
        if (input != null) {
            input.close();
            input = null;
        }
    }

    /**
     * Throws an {@link IOException} if the stream is closed.
     */
    private void notClosed() throws IOException {
        if (input == null) {
            throw new IOException("stream is closed");
        }
    }

    private void readahead() throws IOException {
        notClosed();
        while (matches == null || !matches.hasNext()) {
            String line = input.readLine();
            if (line == null) {
                break;
            }
            matches = new StringPatternMatches(line, Arrays.asList(patterns), discard);
        }
    }
}
