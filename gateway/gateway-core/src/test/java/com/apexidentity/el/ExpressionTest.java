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

package com.apexidentity.el;

import java.util.HashMap;

import static org.fest.assertions.Assertions.assertThat;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apexidentity.http.Exchange;
import com.apexidentity.http.Request;

/**
 * @author Paul C. Bryan
 */
public class ExpressionTest {

    @Test
    public void bool() throws ExpressionException {
        Expression expr = new Expression("${1==1}");
        Object o = expr.eval(null); // no scope required for non-resolving expression
        assertThat(o).isInstanceOf(Boolean.class);
        assertThat(o).isEqualTo(true);
    }

    @Test
    public void empty() throws ExpressionException {
        Expression expr = new Expression("string-literal");
        Object o = expr.eval(null); // no scope required for non-resolving expression
        assertThat(o).isInstanceOf(String.class);
        assertThat(o).isEqualTo("string-literal");
    }

    @Test
    public void scope() throws ExpressionException {
        HashMap<String, String> scope = new HashMap<String, String>();
        scope.put("a", "foo");
        Expression expr = new Expression("${a}bar");
        Object o = expr.eval(scope);
        assertThat(o).isInstanceOf(String.class);
        assertThat(o).isEqualTo("foobar");
    }

    @Test
    public void exchangeRequestHeader() throws ExpressionException {
        Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.headers.put("Host", "www.example.com");
        Expression expr = new Expression("${exchange.request.headers['Host'][0]}");
        String host = expr.eval(exchange, String.class);
        assertThat(host).isEqualTo("www.example.com");
    }

    @Test
    public void exchangeSetAttribute() throws ExpressionException {
        Exchange exchange = new Exchange();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("foo", "bar");
        Expression expr = new Expression("${exchange.testmap}");
        expr.set(exchange, map);
        expr = new Expression("${exchange.testmap.foo}");
        assertThat(expr.eval(exchange, String.class)).isEqualTo("bar");
    }
}
