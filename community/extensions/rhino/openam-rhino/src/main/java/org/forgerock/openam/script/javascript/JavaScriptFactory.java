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
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.openam.script.javascript;

// Java Standard Edition

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

// JSON-Fluent
import org.apache.commons.codec.binary.StringUtils;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.json.fluent.JsonValueException;

// OpenIDM
//import org.forgerock.openidm.core.IdentityServer;
import org.forgerock.openam.script.Script;
import org.forgerock.openam.script.ScriptException;
import org.forgerock.openam.script.ScriptFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Implementation of a script factory for JavaScript.
 * <p/>
 * Expects {@code "type"} configuration property of {@code "text/javascript"} and
 * {@code "source"} property, which contains the script source code.
 * <p/>
 * The optional boolean property {@code "sharedScope"} indicates if a shared scope should be
 * used. If {@code true}, a sealed shared scope containing standard JavaScript objects
 * (Object, String, Number, Date, etc.) will be used for script execution rather than
 * allocating a new unsealed scope for each execution.
 *
 * @author Paul C. Bryan
 */
public class JavaScriptFactory implements ScriptFactory {

    //Logger
//    private static final Logger logger = LoggerFactory.getLogger(JavaScriptFactory.class);

    /**
     * The JavaScript file extension<br>
     * <br>
     * Value is <code>js</code>
     */
    public static final String JS_EXTENSION = ".js";

    private ContextFactory.Listener debugListener = null;
    private volatile Boolean debugInitialised = null;

    private static final String EXTERNAL_JS_SOURCE = "External JavaScript Source/";
    private static final String CONFIG_SOURCE_PROPERTY = "openidm.script.javascript.sources";
    private static final String CONFIG_DEBUG_PROPERTY = "openidm.script.javascript.debug";
    private File externalSourcesFolder = null;

    private synchronized void initDebugListener() throws ScriptException {
        /*
        if (null == debugInitialised) {
            // Get here only once when the first factory initialised.
            String configString = IdentityServer.getInstance().getProperty(CONFIG_DEBUG_PROPERTY);
            if (null != configString) {
                String externalSources = IdentityServer.getInstance().getProperty(CONFIG_SOURCE_PROPERTY);
                if (null != externalSources && externalSources.endsWith(EXTERNAL_JS_SOURCE) && new File(externalSources).isDirectory()) {
                    try {
                        if (null == debugListener) {
                            debugListener = new org.eclipse.wst.jsdt.debug.rhino.debugger.RhinoDebugger(configString);
                            Context.enter().getFactory().addListener(debugListener);
                            Context.exit();
                        }
                        ((org.eclipse.wst.jsdt.debug.rhino.debugger.RhinoDebugger) debugListener).start();
                        debugInitialised = Boolean.TRUE;
                        externalSourcesFolder = new File(externalSources).getAbsoluteFile();
                    } catch (Throwable ex) {
                        //Catch NoClassDefFoundError exception
                        if (!(ex instanceof NoClassDefFoundError)) {
                            //TODO What to do if there is an exception?
                            //throw new ScriptException("Failed to stop RhinoDebugger", ex);
                            logger.error("RhinoDebugger can not be started", ex);
                        } else {
                            //TODO add logging to WARN about the missing RhinoDebugger class
                            logger.warn("RhinoDebugger can not be started because the JSDT RhinoDebugger and Transport bundles must be deployed.");
                        }
                    }
                } else {
                    logger.error("RhinoDebugger can not initialise the source because the {} property must set and has absolute path to '{}' folder.", CONFIG_SOURCE_PROPERTY, EXTERNAL_JS_SOURCE);
                }
                debugInitialised = null == debugInitialised ? Boolean.FALSE : debugInitialised;
            } else if (false /* TODO How to stop ) {
                try {
                    ((org.eclipse.wst.jsdt.debug.rhino.debugger.RhinoDebugger) debugListener).stop();
                } catch (Throwable ex) {
                    //We do not care about the NoClassDefFoundError when we "Stop"
                    if (!(ex instanceof NoClassDefFoundError)) {
                        //TODO What to do if there is an exception?
                        //throw new ScriptException("Failed to stop RhinoDebugger", ex);
                    }
                } finally {
                    debugInitialised = Boolean.FALSE;
                }
            } else {
                debugInitialised = Boolean.FALSE;
            }
        }
        */
    }

    @Override
    public Script newInstance(String name, JsonValue config) throws JsonValueException {
        String type = config.get("type").asString();
        if (type != null && type.equalsIgnoreCase("text/javascript")) {
            boolean sharedScope = config.get("sharedScope").defaultTo(true).asBoolean();
            if (config.isDefined("source")) {
                try {
                    return initializeScript(name, config.get("source").asString(), sharedScope);
                } catch (ScriptException se) { // re-cast to show exact value of failure 
                    throw new JsonValueException(config.get("source"), se);
                }
            } else if (config.isDefined("file")) { // TEMPORARY
                try {
 //                   return initializeScript(name, IdentityServer.getFileForPath(config.get("file").asString()), sharedScope);
                    return initializeScript(name, config.get("file").asString(), sharedScope);
                } catch (ScriptException se) { // re-cast to show exact value of failure
                    throw new JsonValueException(config.get("file"), se);
                }
            } else {
                throw new JsonValueException(config, "expected 'source' or 'file' property");
            }
        }
        return null;
    }


    private Script initializeScript(String name, File source, boolean sharedScope) throws ScriptException {
        initDebugListener();
        if (debugInitialised) {
            try {
                FileChannel inChannel = new FileInputStream(source).getChannel();
                FileChannel outChannel = new FileOutputStream(getTargetFile(name)).getChannel();
                FileLock outLock = outChannel.lock();
                FileLock inLock = inChannel.lock(0, inChannel.size(), true);
                inChannel.transferTo(0, inChannel.size(), outChannel);

                outLock.release();
                inLock.release();

                inChannel.close();
                outChannel.close();
            } catch (IOException e) {
//                logger.warn("JavaScript source was not updated for {}", name, e);
            }
        }
        return new JavaScript(name, source, sharedScope);
    }

    private Script initializeScript(String name, String source, boolean sharedScope) throws ScriptException {
        initDebugListener();
        if (debugInitialised) {
            try {
                FileChannel outChannel = new FileOutputStream(getTargetFile(name)).getChannel();
                FileLock outLock = outChannel.lock();
                ByteBuffer buf = ByteBuffer.allocate(source.length());
                buf.put(source.getBytes("UTF-8"));
                buf.flip();
                outChannel.write(buf);
                outLock.release();
                outChannel.close();
            } catch (IOException e) {
//                logger.warn("JavaScript source was not updated for {}", name, e);
            }
        }
        return new JavaScript(name, source, sharedScope);
    }

    private File getTargetFile(String name) {
        File f = new File(externalSourcesFolder.toURI().resolve(name + JS_EXTENSION));
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        return f;
    }
}

