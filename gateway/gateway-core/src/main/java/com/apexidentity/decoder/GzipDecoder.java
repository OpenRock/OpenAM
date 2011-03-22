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

package com.apexidentity.decoder;

// Java Standard Edition
import java.io.InputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Decodes an entity encoded with <strong>{@code gzip}</strong> encoding. See 
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a> §3.5 for more information.
 *
 * @author Paul C. Bryan
 */
public class GzipDecoder implements Decoder {

    @Override
    public String getKey() {
        return "gzip";
    }

    @Override
    public InputStream decode(InputStream in) throws IOException {
        return new GZIPInputStream(in);
    }
}
