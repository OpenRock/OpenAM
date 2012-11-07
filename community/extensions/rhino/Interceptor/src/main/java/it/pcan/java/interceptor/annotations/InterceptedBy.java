/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor.annotations;

import it.pcan.java.interceptor.MethodInterceptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this annotation on methods that must be intercepted. During invocation, before the first
 * instruction, the class specified as value (implementation of <code>MethodInterceptor</code> interface)
 * will be instantiated, and its <code>methodInvoked</code> method will be automatically invoked.
 *
 * @author Pierantonio
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface InterceptedBy {
    Class<? extends MethodInterceptor> value();
}
