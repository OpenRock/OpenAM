/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.entity;

/**
 *
 * @author Pierantonio
 */
public class MethodInfo {

    private Code code;
    private MethodSignature signature;
    private String methodName;
    private int accessFlags;
    private int nameIndex;
    private int descriptorIndex;
    private AbstractAttribute[] attributes;
    private Class interceptorClass;

    public Code getCode() {
        return code;
    }

    public void setCode(Code code) {
        this.code = code;
    }

    
    public MethodSignature getSignature() {
        return signature;
    }

    public void setSignature(MethodSignature signature) {
        this.signature = signature;
    }

    public Class getInterceptorClass() {
        return interceptorClass;
    }

    public void setInterceptorClass(Class interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public void setAccessFlags(int accessFlags) {
        this.accessFlags = accessFlags;
    }

    public AbstractAttribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(AbstractAttribute[] attributes) {
        this.attributes = attributes;
    }

    public int getDescriptorIndex() {
        return descriptorIndex;
    }

    public void setDescriptorIndex(int descriptorIndex) {
        this.descriptorIndex = descriptorIndex;
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }
}
