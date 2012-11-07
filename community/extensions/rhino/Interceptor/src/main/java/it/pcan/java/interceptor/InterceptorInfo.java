package it.pcan.java.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: allan
 * Date: 10/29/12
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */



public class InterceptorInfo {

    private Class intercept;
    boolean blockEdits = false;


    public InterceptorInfo(Class intercept) {
        this.intercept = intercept;
    }
    public boolean isEmpty() {
         if (intercept == null) {
             return true;
         }
        return false;
    }

    public boolean shouldIntercept(String name){
        if (blockEdits) return false;
        if (name.equalsIgnoreCase("<init>")) return false;

        if (intercept == null) {
            return false;
        } else {
            return true;
        }
    }
    public Class getInterceptorClass() {
         return intercept;
    }
}
