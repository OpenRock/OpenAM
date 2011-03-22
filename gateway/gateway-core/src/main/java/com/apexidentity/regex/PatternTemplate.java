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
import java.util.regex.MatchResult;

/**
 * Expresses a transformation to be applied to a regular expression pattern match. A template
 * may contain references to groups captured in the match. Each occurrence of
 * {@code $}<em>g</em> will be substituted by capture group <em>g</em> in a match result. A
 * dollar sign or numeral literal immediately following a capture group reference may be
 * included as a literal in the template by preceding it with a backslash ({@code \}).
 * Backslash itself must be also escaped in this manner.
 *
 * @author Paul C. Bryan
 */
public class PatternTemplate {

    /** The transformation template to apply to regular expression pattern matches. */
    public final String value;

    /**
     * Constructs a new template with the specified value.
     *
     * @param value the template to apply to regular expression pattern matches.
     */
    public PatternTemplate(String value) {
        this.value = value;
    }

    /**
     * Performs a transformation of a match result by applying the template. References to
     * matching groups that are not in the match result resolve to a blank ({@code ""}) value.
     *
     * @param result the match result to apply the template to.
     * @return the value of the matching result with the template applied.
     */
    public String applyTo(MatchResult result) {
        int len = value.length();
        int groups = result.groupCount();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int n = 0; n < len; n++) {
            char c = value.charAt(n);
            if (escaped) {
                sb.append(c);
                escaped = false;
            }
            else if (c == '\\') {
                escaped = true;
            }
            else if (c == '$') {
                int group = -1;
                while (n + 1 < len) {
                    int digit = value.charAt(n + 1) - '0';
                    if (digit < 0 || digit > 9) {
                        break;
                    }
                    group = (group == -1 ? 0 : group) * 10 + digit; // add digit
                    n++;
                }
                if (group >= 0 && group <= groups) {
                    sb.append(result.group(group));
                }
            }
            else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Returns the literal template value.
     */
    @Override
    public String toString() {
        return value;
    }
}
