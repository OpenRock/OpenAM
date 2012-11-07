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
 * Copyright © 2012 ForgeRock AS. All rights reserved.
 */

package org.forgerock.openam.script.javascript;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.forgerock.openam.script.Function;

import org.forgerock.json.fluent.JsonPointer;
import org.forgerock.json.fluent.JsonValue;

import org.forgerock.util.Factory;
import org.forgerock.util.LazyMap;

import org.mozilla.javascript.Scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Constructs a logger property for exposing SLF4J logging facility
 * in JavaScript scope
 * 
 * @author aegloff
 */
class LoggerPropertyFactory {

    /**
     * @return the logger property Scriptable for adding to 
     * a JavaScript scope
     */
    public static Scriptable get(final String loggerName) {
        Map loggerMap = new LazyMap<String, Object>(new Factory<Map<String, Object>>() {
            @Override public Map<String, Object> newInstance() {
                final Logger logger = LoggerFactory.getLogger(loggerName /*"org.forgerock.openidm.script.javascript.JavaScript." + (file == null ? "embedded-source" : file.getName())*/);
                HashMap<String, Object> loggerWrap = new HashMap<String, Object>();
                // error(string id, object... param)
                // Wraps SLF4j error(String format, Object[] argArray)
                // Log a message at the error level according to the specified format and arguments.
                loggerWrap.put("error", new Function() {
                    @Override
                    public Object call(Map<String, Object> scope,
                     Map<String, Object> _this, List<Object> params) throws Throwable {
                        JsonValue p = paramsValue(params);
                        logger.error(p.get(0).required().asString(), params.size() > 1 ? params.subList(1, params.size()).toArray() : null);
                        return null; // no news is good news
                    }
                });
                // warn(string id, object... param)
                // Wraps SLF4j warn(String format, Object[] argArray)
                // Log a message at the warn level according to the specified format and arguments.
                loggerWrap.put("warn", new Function() {
                    @Override
                    public Object call(Map<String, Object> scope,
                     Map<String, Object> _this, List<Object> params) throws Throwable {
                        JsonValue p = paramsValue(params);
                        logger.warn(p.get(0).required().asString(), params.size() > 1 ? params.subList(1, params.size()).toArray() : null);
                        return null; // no news is good news
                    }
                });
                // info(string id, object... param)
                // Wraps SLF4j info(String format, Object[] argArray)
                // Log a message at the info level according to the specified format and arguments.
                loggerWrap.put("info", new Function() {
                    @Override
                    public Object call(Map<String, Object> scope,
                     Map<String, Object> _this, List<Object> params) throws Throwable {
                        JsonValue p = paramsValue(params);
                        logger.info(p.get(0).required().asString(), params.size() > 1 ? params.subList(1, params.size()).toArray() : null);
                        return null; // no news is good news
                    }
                });
                // debug(string id, object... param)
                // Wraps SLF4j debug(String format, Object[] argArray)
                // Log a message at the debug level according to the specified format and arguments.
                loggerWrap.put("debug", new Function() {
                    @Override
                    public Object call(Map<String, Object> scope,
                     Map<String, Object> _this, List<Object> params) throws Throwable {
                        JsonValue p = paramsValue(params);
                        logger.debug(p.get(0).required().asString(), params.size() > 1 ? params.subList(1, params.size()).toArray() : null);
                        return null; // no news is good news
                    }
                });
                // trace(string id, object... param)
                // Wraps SLF4j trace(String format, Object[] argArray)
                // Log a message at the trace level according to the specified format and arguments.
                loggerWrap.put("trace", new Function() {
                    @Override
                    public Object call(Map<String, Object> scope,
                     Map<String, Object> _this, List<Object> params) throws Throwable {
                        JsonValue p = paramsValue(params);
                        logger.trace(p.get(0).required().asString(), params.size() > 1 ? params.subList(1, params.size()).toArray() : null);
                        return null; // no news is good news
                    }
                });
                
                return loggerWrap;
            }
        });
        return (Scriptable) ScriptableWrapper.wrap(loggerMap);
    }
    
    private static JsonValue paramsValue(List<Object> params) {
        return new JsonValue(params, new JsonPointer("params"));
    }
}
