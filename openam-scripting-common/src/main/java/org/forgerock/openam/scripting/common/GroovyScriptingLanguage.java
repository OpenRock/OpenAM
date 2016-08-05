package org.forgerock.openam.scripting.common;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Created by brmiller on 8/1/16.
 */
public class GroovyScriptingLanguage implements ScriptingLanguage {
    /**
     * JSR 223 engine name for Groovy support.
     */
    public static final String GROOVY_ENGINE_NAME = "groovy";

    final ScriptValidator scriptValidator;

    public GroovyScriptingLanguage(ScriptValidator scriptValidator) {
        this.scriptValidator = scriptValidator;
    }

    public ScriptEngine getScriptEngine(final ScriptEngineManager scriptEngineManager) {
        return scriptEngineManager.getEngineByName(GROOVY_ENGINE_NAME);
    }

    public ScriptValidator getScriptValidator() {
        return scriptValidator;
    }
}
