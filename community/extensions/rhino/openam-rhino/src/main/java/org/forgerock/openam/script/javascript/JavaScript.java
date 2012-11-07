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

// Java Standard Edition
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Object;
import java.util.HashMap;
import java.util.Map;

// Mozilla Rhino
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

// OpenIDM
//import org.forgerock.openidm.smartevent.EventEntry;
//import org.forgerock.openidm.smartevent.Name;
//import org.forgerock.openidm.smartevent.Publisher;
import org.forgerock.openam.script.Script;
import org.forgerock.openam.script.ScriptException;
import org.forgerock.openam.script.ScriptThrownException;

/**
 * A JavaScript script.
 * <p>
 * This implementation pre-compiles the provided script. Any syntax errors in the source code
 * will throw an exception during construction of the object.
 * <p>
 *
 * @author Paul C. Bryan
 * @author aegloff
 */
public class JavaScript implements Script {

    /** A sealed shared scope to improve performance; avoids allocating standard objects on every exec call. */
    private static ScriptableObject SHARED_SCOPE = null; // lazily initialized

    /** The script level scope to use */
    private Scriptable scriptScope = null;
    
    /** The compiled script to execute. */
    private final org.mozilla.javascript.Script script;
    
    /** The script name */
    private String scriptName;
    
    /** The script file if stand alone file, null if source is from embedded configuration */
    private File file;
    
    /** The event name to use for monitoring this script */
//    private Name monitoringEventName;

    /** Indicates if this script instance should use the shared scope. */
    private final boolean sharedScope;

    /**
     * Compiles the JavaScript source code into an executable script. A sealed shared scope
     * containing standard JavaScript objects (Object, String, Number, Date, etc.) will be
     * used for script execution rather than allocating a new unsealed scope for each
     * execution.
     *
     * @param source the source code of the JavaScript script.
     * @throws ScriptException if there was an exception encountered while compiling the script.
     */
    public JavaScript(String name, String source) throws ScriptException {
        this(name, source, true);
    }

    /**
     * Compiles the JavaScript source code into an executable script. If {@code useSharedScope}
     * is {@code true}, then a sealed shared scope containing standard JavaScript objects
     * (Object, String, Number, Date, etc.) will be used for script execution; otherwise a new
     * unsealed scope will be allocated for each execution.
     *
     * @param source the source code of the JavaScript script.
     * @param sharedScope if {@code true}, uses the shared scope, otherwise allocates new scope.
     * @throws ScriptException if there was an exception encountered while compiling the script.
     */
    public JavaScript(String name, String source, boolean sharedScope) throws ScriptException {
        this.scriptName = name;
//        this.monitoringEventName = generateEventName();
        this.sharedScope = sharedScope;
        Context cx = Context.enter();
        try {
            scriptScope = getScriptScope(cx);
            script = cx.compileString(source, name, 1, null);
        } catch (RhinoException re) {
            throw new ScriptException(re.getMessage());
        } finally {
            Context.exit();
        }
    }

    /**
     * TEMPORARY
     */
    public JavaScript(String name, File file, boolean sharedScope) throws ScriptException {
        this.scriptName = name;
        this.file = file;
//        this.monitoringEventName = generateEventName();
        FileReader reader = null;
        this.sharedScope = sharedScope;
        try {
            reader = new FileReader(file);
            Context cx = Context.enter();
            try {
                scriptScope = getScriptScope(cx);
                script = cx.compileReader(reader, name != null ? name : file.getPath(), 1, null);
            } catch (RhinoException re) {
                throw new ScriptException(re);
            } finally {
                Context.exit();
            }
        } catch (IOException ioe) {
            throw new ScriptException(ioe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // meaningless exception
                }
            }
        }
    }

    /**
     * Gets the JavaScript standard objects, 
     * either as the shared sealed scope or as a newly
     * allocated set of standard objects, depending on the value of {@code useSharedScope}.
     *
     * @param context The runtime context of the executing script.
     * @return the JavaScript standard objects.
     */
    private ScriptableObject getStandardObjects(Context context) {
        if (!sharedScope) {
            ScriptableObject scope = context.initStandardObjects(); // somewhat expensive
            return scope;
        }
        if (SHARED_SCOPE == null) { // lazy initialization race condition is harmless
            ScriptableObject scope = context.initStandardObjects(null, true);
            String loadMe = "RegExp; getClass; java; Packages; JavaAdapter;";
            context.evaluateString(scope , loadMe, "lazyLoad", 0, null);
            scope.sealObject(); // seal the whole scope (not just standard objects)
            SHARED_SCOPE = scope;
        }
        return SHARED_SCOPE;
    }
    
    /**
     * Get the scope scriptable re-used for this script
     * Holds common functionality such as the logger
     * 
     * @param The runtime context of the executing script.
     * @return the context scriptable for this script
     */
    private Scriptable getScriptScope(Context context) {
        Map<String, Object> scriptScopeMap = new HashMap<String, Object>();
        addLoggerProperty(scriptScopeMap);
        Scriptable scriptScopeScriptable = new ScriptableMap(scriptScopeMap);
        scriptScopeScriptable.setPrototype(getStandardObjects(context)); // standard objects included with every box
        scriptScopeScriptable.setParentScope(null);
        return scriptScopeScriptable;
    }
    
    /**
     * Add the logger property to the JavaScript scope
     * @param scope to add the property to
     */
    private void addLoggerProperty(Map<String, Object> scope) {
        String loggerName = "org.forgerock.openidm.script.javascript.JavaScript." 
                + (file == null ? "embedded-source" : file.getName());
//        scope.put("logger", LoggerPropertyFactory.get(loggerName));
    }

    @Override
    public Object exec(Map<String, Object> scope) throws ScriptException {
        if (scope == null) {
            throw new NullPointerException();
        }
//        EventEntry measure = Publisher.start(monitoringEventName, scope, null);
        Context context = Context.enter();
        try {
            for (String s : scope.keySet()) {
                scope.put(s, Context.javaToJS(scope.get(s),scriptScope)) ;
            }
            Scriptable outer = new ScriptableMap(scope);
            outer.setPrototype(scriptScope); // script level context and standard objects included with every box
            outer.setParentScope(null);
            Scriptable inner = context.newObject(outer); // inner transient scope for new properties
            inner.setPrototype(outer);
            inner.setParentScope(null);

            Object result = Converter.convert(script.exec(context, inner));
//            measure.setResult(result);
            return result;
        } catch (RhinoException re) {
            if (re instanceof JavaScriptException) { // thrown by the script itself
                throw new ScriptThrownException(Converter.convert(((JavaScriptException)re).getValue()));
            } else { // some other runtime exception encountered
                throw new ScriptException(re.getMessage());
            }
        } finally {
            Context.exit();
//            measure.end();
        } 
    }
    
//    Name generateEventName() {
//        return Name.get("openidm/internal/script/javascript/" + (file != null ? file.getName() : "embedded-source") + "/" + scriptName);
//    }
}
