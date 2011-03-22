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

// Java Standard Edition
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map; // Javadoc

// ApexIdentity Core Library
import com.apexidentity.el.Expression;
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.HTTPUtil;
import com.apexidentity.http.Message;
import com.apexidentity.http.MessageType;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.ListNode;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.model.ValueNode;
import com.apexidentity.regex.StreamPatternExtractor;
import com.apexidentity.regex.PatternTemplate;
import com.apexidentity.util.EnumerableMap;

/**
 * Extracts regular expression patterns from a message entity. Extraction occurs either
 * before the exchange is handled if {@code messageType} is {@link MessageType#REQUEST}, or
 * after the exchange is handled if it is {@link MessageType#RESPONSE}. Each pattern can have
 * an associated template, which is applied to its match result.
 * <p>
 * The extraction results are contained in a {@link Map} object, whose location is specified
 * by the {@code target} expression. For a given matched pattern, the value stored in the map
 * is either the result of applying its associated pattern template (if specified) or the
 * match result itself otherwise.
 *
 * @author Paul C. Bryan
 *
 * @see StreamPatternExtractor
 * @see PatternTemplate
 */
public class EntityExtractFilter extends GenericFilter {

    /** Extracts regular expression patterns from entities. */
    public final StreamPatternExtractor extractor = new StreamPatternExtractor(); 

    /** The message type in the exchange to extract patterns from. */
    public MessageType messageType;

    /** Overrides the character set encoding specified in message. If {@code null}, the message encoding is used. */
    public Charset charset;

    /** Expression that yields the target object that will contain the mapped extraction results. */
    public Expression target;

    /**
     * Filters the request and/or response of an exchange by extracting regular expression
     * patterns from a message entity.
     */
    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (messageType == MessageType.REQUEST) {
            process(exchange, exchange.request);
        }
        chain.handle(exchange);
        if (messageType == MessageType.RESPONSE) {
            process(exchange, exchange.response);
        }
        timer.stop();
    }

    private void process(Exchange exchange, Message message) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (message != null && message.entity != null) {
            try {
                Reader reader = HTTPUtil.entityReader(message, true, charset);
                try {
                    EnumerableMap<String, String> extract = extractor.extract(reader);
                    for (String key : extract.keySet()) { // get 'em all now
                        map.put(key, extract.get(key));
                    }
                }
                finally {
                    reader.close();
                }
            }
            catch (IOException ioe) {
                // may yield partial or unresolved attributes
            }
        }
        target.set(exchange, map);
    }

    /** Creates and initializes an entity extract handler in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            EntityExtractFilter filter = new EntityExtractFilter();
            filter.messageType = config.get("messageType").required().asEnum(MessageType.class);
            filter.charset = config.get("charset").asCharset(); // optional
            filter.target = config.get("target").required().asExpression();
            ListNode bindings = config.get("bindings").required().asListNode();
            for (Iterator<ValueNode> i = bindings.iterator(); i.hasNext(); ) {
                MapNode node = i.next().required().asMapNode();
                String key = node.get("key").required().unique(filter.extractor.patterns).asString();
                filter.extractor.patterns.put(key, node.get("pattern").required().asPattern());
                String template = node.get("template").asString(); // optional
                if (template != null) {
                    filter.extractor.templates.put(key, new PatternTemplate(template));
                }
            }
            return filter;
        }
    }
}
