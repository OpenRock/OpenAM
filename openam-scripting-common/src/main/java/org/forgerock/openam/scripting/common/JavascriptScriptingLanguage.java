package org.forgerock.openam.scripting.common;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Created by brmiller on 8/1/16.
 */
public class JavascriptScriptingLanguage implements ScriptingLanguage {

    /**
     * JSR 223 engine name to use for Javascript support. We use a distributed Rhino copy on all
     * platforms for consistency across JVM versions and vendors.
     */
    public static final String JAVASCRIPT_ENGINE_NAME = "rhino";

    final ScriptValidator scriptValidator;

    public JavascriptScriptingLanguage(ScriptValidator scriptValidator) {
        this.scriptValidator = scriptValidator;
    }

    /**
     * {@inheritDoc}
     */
    public ScriptEngine getScriptEngine(final ScriptEngineManager scriptEngineManager) {
        return scriptEngineManager.getEngineByName(JAVASCRIPT_ENGINE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public ScriptValidator getScriptValidator() {
        return scriptValidator;
    }
}
