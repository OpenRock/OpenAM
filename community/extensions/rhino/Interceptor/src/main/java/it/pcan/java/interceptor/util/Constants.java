/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.util;

/**
 *
 * @author Pierantonio
 */
public class Constants {

    public static final int magic = 0xCAFEBABE;
    public final static int CONSTANT_Class = 7;
    public final static int CONSTANT_Fieldref = 9;
    public final static int CONSTANT_Methodref = 10;
    public final static int CONSTANT_InterfaceMethodref = 11;
    public final static int CONSTANT_String = 8;
    public final static int CONSTANT_Integer = 3;
    public final static int CONSTANT_Float = 4;
    public final static int CONSTANT_Long = 5;
    public final static int CONSTANT_Double = 6;
    public final static int CONSTANT_NameAndType = 12;
    public final static int CONSTANT_Utf8 = 1;
    public final static int CONSTANT_MethodHandle = 15;
    public final static int CONSTANT_MethodType = 16;
    public final static int CONSTANT_InvokeDynamic = 18;

    //----------------------------------------------------------------------//


    public final static String DEFAULT_CONSTRUCTOR_NAME = "<init>";
    public final static String DEFAULT_CONSTRUCTOR_TYPE = "()V";
    public final static String INTERCEPTOR_METHOD_NAME = "methodInvoked";
    public final static String INTERCEPTOR_METHOD_TYPE = "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V";

}
