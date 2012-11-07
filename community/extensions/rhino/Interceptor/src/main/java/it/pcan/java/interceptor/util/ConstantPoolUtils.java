/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.util;

import it.pcan.java.interceptor.annotations.InterceptedBy;
import it.pcan.java.interceptor.entity.*;
import java.util.List;

/**
 *
 * @author Pierantonio
 */
public class ConstantPoolUtils {

    public static boolean isInterceptorClassNamePresent(ConstantPool pool) {
        if (pool != null) {
            for (AbstractConstant constant : pool) {
                if(constant.getClass() == Utf8Constant.class) {
                    String className = ((Utf8Constant) constant).getString();
                    if(className.replace("/", ".").contains(InterceptedBy.class.getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static String getTypeStringFromClass(Class clazz) {
         return "L" + clazz.getName().replace(".", "/") + ";";
    }
}
