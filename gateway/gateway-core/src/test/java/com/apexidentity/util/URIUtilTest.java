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

package com.apexidentity.util;

import java.net.URI;
import java.net.URISyntaxException;

import static org.fest.assertions.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Paul C. Bryan
 */
public class URIUtilTest {

    @Test
    public void toURIandBack() throws URISyntaxException {
        URI u1 = URIUtil.create("a", "b", "c", 4, "/e%3D", "x=%3D", "g%3D");
        URI u2 = URIUtil.create(u1.getScheme(), u1.getRawUserInfo(), u1.getHost(),
         u1.getPort(), u1.getRawPath(), u1.getRawQuery(), u1.getRawFragment());
        assertThat(u1).isEqualTo(u2);
    }

    @Test
    public void rawParams() throws URISyntaxException {
        URI uri = URIUtil.create("http", "user", "example.com", 80, "/raw%3Dpath",
         "x=%3D", "frag%3Dment");
        assertThat(uri.toString()).isEqualTo("http://user@example.com:80/raw%3Dpath?x=%3D#frag%3Dment");
    }

    @Test
    public void rebase() throws URISyntaxException {
        URI uri = new URI("https://doot.doot.doo.org/all/good/things?come=to&those=who#breakdance");
        URI base = new URI("http://www.example.com/");
        URI rebased = URIUtil.rebase(uri, base);
        assertThat(rebased.toString()).isEqualTo("http://www.example.com/all/good/things?come=to&those=who#breakdance");
    }
}
