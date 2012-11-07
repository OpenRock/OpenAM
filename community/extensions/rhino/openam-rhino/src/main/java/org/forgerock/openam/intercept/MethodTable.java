package org.forgerock.openam.intercept;

import org.forgerock.openam.script.Script;
import org.forgerock.openam.script.javascript.JavaScript;
import org.forgerock.openam.script.ScriptException;

import java.io.IOException;
import java.lang.ClassLoader;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.lang.*;
import java.util.Enumeration;
import java.net.URL;

import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MethodTable {
    private Map<String, JavaScriptFile> scripts;
    private static MethodTable me;

    private MethodTable() {
        scripts = new HashMap<String, JavaScriptFile>();
    }

    public static MethodTable getInstance() {
        if (me == null) me = new MethodTable();
        return me;
    }

    public boolean shouldIntercept(String name) {
        String sName = (name.contains(".")) ? name.substring(name.lastIndexOf(".") + 1) : name;
        return (null != scripts.get(sName));
    }

    public void initialize() {
        try {
            // First, lets look in WEB-INF/classes/InterceptorScripts
            Enumeration<URL> scriptFolders = this.getClass().getClassLoader().getResources("/InterceptorScripts");

            if (scriptFolders.hasMoreElements()) {

                File sFolder = new File(scriptFolders.nextElement().toURI());
                File[] scriptList = sFolder.listFiles();

                for (File f : scriptList) {
                    JavaScriptFile cf = new JavaScriptFile(f);
                    scripts.put(cf.getName(), cf);
                }
            }
            // Now look in Home Directory
            String home = System.getProperty("user.home");
            File newPath = new File(home + "/Scripts/Interceptor");

            if ((newPath.exists() && (newPath.isDirectory()) {
                File[] scriptList = newPath.listFiles();

                for (File f : scriptList) {
                    JavaScriptFile cf = new JavaScriptFile(f);
                    scripts.put(cf.getName(), cf);
                }
            }

        } catch (Exception ex) {
            // Just let it slide
        }
    }

    public Script getScript(String name) {

        String sName = (name.contains(".")) ? name.substring(name.lastIndexOf(".") + 1) : name;
        Script sc = scripts.get(sName).getContents();
        return sc;
    }

}
