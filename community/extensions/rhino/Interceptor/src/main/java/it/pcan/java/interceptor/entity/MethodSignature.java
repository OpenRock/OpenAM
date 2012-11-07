/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor.entity;

import java.util.List;

/**
 *
 * @author Pierantonio
 */
public class MethodSignature {

    private boolean isStatic;
    private int classNameIndex;
    private int methodNameIndex;

    private String returnType;
    private List<String> paramTypes;

    private int returnTypeRef;
    private List<Integer> paramTypesRef;


    public List<String> getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(List<String> paramTypes) {
        this.paramTypes = paramTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<Integer> getParamTypesRef() {
        return paramTypesRef;
    }

    public void setParamTypesRef(List<Integer> paramTypesRef) {
        this.paramTypesRef = paramTypesRef;
    }

    public int getReturnTypeRef() {
        return returnTypeRef;
    }

    public void setReturnTypeRef(int returnTypeRef) {
        this.returnTypeRef = returnTypeRef;
    }

    public int getClassNameIndex() {
        return classNameIndex;
    }

    public void setClassNameIndex(int classNameIndex) {
        this.classNameIndex = classNameIndex;
    }

    public int getMethodNameIndex() {
        return methodNameIndex;
    }

    public void setMethodNameIndex(int methodNameIndex) {
        this.methodNameIndex = methodNameIndex;
    }

    public boolean isIsStatic() {
        return isStatic;
    }

    public void setIsStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    
    
}
