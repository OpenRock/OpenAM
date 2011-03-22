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

package com.apexidentity.filter;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apexidentity.el.Expression;
import com.apexidentity.el.ExpressionException;
import com.apexidentity.handler.StaticResponseHandler;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.Request;

/**
 * @author Paul C. Bryan
 */
public class AssignmentFilterTest {

    @Test
    public void onRequest() throws ExpressionException, HandlerException, IOException {
        AssignmentFilter filter = new AssignmentFilter();
        AssignmentFilter.Binding binding = new AssignmentFilter.Binding();
        binding.target = new Expression("${exchange.newAttr}");
        binding.value = new Expression("${exchange.request.method}");
        filter.onRequest.add(binding);
        Exchange exchange = new Exchange();
        exchange.request = new Request();
        exchange.request.method = "DELETE";
        Chain chain = new Chain();
        chain.filters.add(filter);
        StaticResponseHandler handler = new StaticResponseHandler();
        handler.status = 200;
        chain.handler = handler;
        assertThat(binding.target.eval(exchange)).isNull();
        chain.handle(exchange);
        assertThat(exchange.get("newAttr")).isEqualTo("DELETE");
    }

    @Test
    public void onResponse() throws ExpressionException, HandlerException, IOException {
        AssignmentFilter filter = new AssignmentFilter();
        AssignmentFilter.Binding binding = new AssignmentFilter.Binding();
        binding.target = new Expression("${exchange.newAttr}");
        binding.value = new Expression("${exchange.response.status}");
        filter.onResponse.add(binding);
        Exchange exchange = new Exchange();
        Chain chain = new Chain();
        chain.filters.add(filter);
        StaticResponseHandler handler = new StaticResponseHandler();
        handler.status = 200;
        chain.handler = handler;
        assertThat(binding.target.eval(exchange)).isNull();
        chain.handle(exchange);
        assertThat(exchange.get("newAttr")).isEqualTo(200);
    }
}
