/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor.entity;

/**
 *
 * @author Pierantonio
 */
public class RuntimeInvisibleAnnotations extends AbstractAttribute{

    private Class interceptorClass;

    public Class getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(Class interceptorClass) {
        this.interceptorClass = interceptorClass;
    }


}
