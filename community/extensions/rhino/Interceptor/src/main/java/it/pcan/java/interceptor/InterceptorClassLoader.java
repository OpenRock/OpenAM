/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor;

import java.io.*;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;


import org.apache.catalina.loader.WebappClassLoader;
import org.apache.catalina.loader.ResourceEntry;
import org.apache.catalina.LifecycleException;


/**
 * @author Pierantonio
 */
public class InterceptorClassLoader
        extends org.apache.catalina.loader.WebappClassLoader {

    Class intercept = null;
    private boolean enabled = false;
    private boolean logNamesLoaded = false;
    private boolean saveByteCodes = true;
    private MethodInterceptor toBeInvoked = null;

    public InterceptorClassLoader() {
        super();
    }

    public InterceptorClassLoader(ClassLoader parent) {
        super(parent);
    }

    public void loadConfig() {
        try {
            String logged = System.getProperty("com.forgerock.methodintercept.logenabled", "false");
            String saveCode = System.getProperty("com.forgerock.methodintercept.saveByteCode", "false");
            String classname = System.getProperty("com.forgerock.methodintercept.classname", "org.forgerock.openam.intercept.interceptor");
            logNamesLoaded = Boolean.parseBoolean(logged);
            saveByteCodes = Boolean.parseBoolean(saveCode);
            intercept = Class.forName(classname, true, this);
            toBeInvoked = (MethodInterceptor) intercept.newInstance();
            toBeInvoked.initialize( );
            enabled = true;
        } catch (Exception ex) {
            enabled = false;
        }
    }

    protected ResourceEntry findResourceInternal(String name, String path) {
        ResourceEntry entry = super.findResourceInternal(name, path);

        if ((null == entry) || (null != entry.loadedClass)) return entry;

        fixupLoading(name, entry);
        return entry;
    }


    public void fixupLoading(String name, ResourceEntry entry) {

        if (name.startsWith("com.sun.identity") || name.startsWith("com.iplanet") || name.startsWith("org.forgerock")) {

            if (!enabled) return;

            String s = name;
            if (name.contains(".")) s = name.substring(name.lastIndexOf(".") + 1);
            if (!toBeInvoked.shouldIntercept(s)) return;

            InterceptorInfo methods = new InterceptorInfo(intercept);

            InputStream resourceAsStream = new ByteArrayInputStream(entry.binaryContent);

            if (null != resourceAsStream) {
                try {
                    System.out.print("Patching class " + name);
                    InterceptorCodeInjector injector = new InterceptorCodeInjector(resourceAsStream, name);
                    byte[] injectedCode = injector.inject(methods);
                    if (saveByteCodes) {
                        File f = File.createTempFile(name, ".class");
                        FileOutputStream fop = new FileOutputStream(f);
                        fop.write(injectedCode);
                        fop.flush();
                        fop.close();
                    }
                    entry.binaryContent = injectedCode;
                    resourceAsStream.close();
                    return;

                } catch (IOException ex) {
                    Logger.getLogger(InterceptorClassLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(InterceptorClassLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return;
    }

    public void start() throws LifecycleException {
        super.start();

        try {
            loadConfig();
        } catch (Exception ex) {
            System.out.println("Exception :" + ex);
        }
    }

}
