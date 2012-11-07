package org.forgerock.openam.intercept;

import it.pcan.java.interceptor.MethodInterceptor;
import org.forgerock.openam.script.Script;
import org.forgerock.openam.script.javascript.JavaScript;
import org.forgerock.openam.script.ScriptException;

import org.mozilla.javascript.NativeJavaClass;
import  org.mozilla.javascript.NativeJavaArray;

import java.io.IOException;
import java.lang.ClassLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.Enumeration;
import java.net.URL;


import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class interceptor implements MethodInterceptor {
    static Map<String,Object> globals;

    public interceptor() {
    }
    public void initialize( ) {
        MethodTable mt = MethodTable.getInstance();
        mt.initialize()  ;
    }

    public boolean shouldIntercept(String name) {
        MethodTable mt = MethodTable.getInstance();
        return mt.shouldIntercept( name)  ;
    };


    public void methodInvoked(Object object, String className, String methodName, Object[] params) {

        Map<String,Object> scope = new HashMap<String,Object>();
        if (globals == null) globals = new HashMap<String,Object>();
        scope.put("classname",className);
        scope.put("methodname",methodName);
        scope.put("object",object);
        scope.put("args",params);
        scope.put("globals",globals);

        try {
            MethodTable mt = MethodTable.getInstance();
            Script sc = mt.getScript(className);
            Object result = sc.exec(scope);
        } catch (Exception ex) {
            System.out.println("Exception in JavaScript" + ex);
        }
    }
}
