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

import java.util.Arrays;
import java.net.URI;

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
public class FunctionTest {

    private Exchange exchange;

    @BeforeMethod
    public void beforeMethod() {
        exchange = new Exchange();
        exchange.request = new Request();
    }

    @Test
    public void _toString() throws ExpressionException {
        exchange.request.uri = URI.create("http://www.apexidentity.com/");
        Object o = new Expression("${toString(exchange.request.uri)}").eval(exchange);
        assertThat(o).isInstanceOf(String.class);
        assertThat(o).isEqualTo(exchange.request.uri.toString());
    }

    @Test
    public void keyMatch() throws ExpressionException {
        Object o = new Expression("${exchange[keyMatch(exchange, '^requ.*')]}").eval(exchange);
        assertThat(o).isInstanceOf(Request.class);
        assertThat(o).isSameAs(exchange.request);
    }

    @Test
    public void lengthString() throws ExpressionException {
        exchange.put("foo", "12345678901");
        Object o = new Expression("${length(exchange.foo)}").eval(exchange);
        assertThat(o).isInstanceOf(Integer.class);
        assertThat(o).isEqualTo(11);
    }

    @Test
    public void lengthCollection() throws ExpressionException {
        exchange.put("foo", Arrays.asList("1", "2", "3", "4", "5"));
        Object o = new Expression("${length(exchange.foo)}").eval(exchange);
        assertThat(o).isInstanceOf(Integer.class);
        assertThat(o).isEqualTo(5);
    }

    @Test
    public void split() throws ExpressionException {
        Object o = new Expression("${split('a,b,c,d,e', ',')[2]}").eval(exchange);
        assertThat(o).isInstanceOf(String.class);
        assertThat(o).isEqualTo("c");
    }

    @Test
    public void join() throws ExpressionException {
        String[] s = { "a", "b", "c" };
        exchange.put("foo", s);
        Object o = new Expression("${join(exchange.foo, ',')}").eval(exchange);
        assertThat(o).isInstanceOf(String.class);
        assertThat(o).isEqualTo("a,b,c");
    }

    @Test
    public void contains() throws ExpressionException {
        String s = "allyoucaneat";
        exchange.put("s", s);
        Object o = new Expression("${contains(exchange.s, 'can')}").eval(exchange);
        assertThat(o).isInstanceOf(Boolean.class);
        assertThat(o).isEqualTo(true);
    }

    @Test
    public void notContains() throws ExpressionException {
        String s = "allyoucaneat";
        exchange.put("s", s);
        Object o = new Expression("${contains(exchange.s, 'foo')}").eval(exchange);
        assertThat(o).isInstanceOf(Boolean.class);
        assertThat(o).isEqualTo(false);
    }

    @Test
    public void containsSplit() throws ExpressionException {
        String s = "all,you,can,eat";
        exchange.put("s", s);
        Object o = new Expression("${contains(split(exchange.s, ','), 'can')}").eval(exchange);
        assertThat(o).isInstanceOf(Boolean.class);
        assertThat(o).isEqualTo(true);
    }

    @Test
    public void matches() throws ExpressionException {
        String s = "I am the very model of a modern Major-General";
        exchange.put("s", s);
        Object o = new Expression("${matches(exchange.s, 'the (.*) model')}").eval(exchange);
        assertThat(o).isInstanceOf(String[].class);
        String[] ss = (String[])o;
        assertThat(ss[0]).isEqualTo("the very model");
        assertThat(ss[1]).isEqualTo("very");
    }
}
