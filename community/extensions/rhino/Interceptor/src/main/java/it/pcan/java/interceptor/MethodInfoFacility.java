/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor;

import it.pcan.java.interceptor.entity.*;
import it.pcan.java.interceptor.util.ConstantPoolUtils;
import it.pcan.java.interceptor.util.Constants;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pierantonio
 */
class MethodInfoFacility {

    private final int thisClassIndex;
    private final DataInputStream is;
    private final DataOutputStream os;
    private final DataOutputStream constantStream;
    private final ConstantPool constantPool;
    private final List<MethodInfo> methods = new ArrayList<MethodInfo>();
    private final AttributeFacility attributeFacility;

    public MethodInfoFacility(DataInputStream is, DataOutputStream os, DataOutputStream constantStream, ConstantPool constantPool, int thisClassIndex) {
        this.is = is;
        this.os = os;
        this.constantStream = constantStream;
        this.constantPool = constantPool;
        this.thisClassIndex = thisClassIndex;
        this.attributeFacility = new AttributeFacility(is, os, this.constantPool);
    }

    void processMethods(InterceptorInfo loader) throws IOException {

        int methodsCount = is.readUnsignedShort();
        os.writeShort(methodsCount);

        System.out.println(" : ( " + methodsCount + " ) methods ");
        for (int i = 0; i < methodsCount; i++) {
            MethodInfo methodInfo = readMethod();
            if (loader.shouldIntercept(methodInfo.getMethodName())) {
                methodInfo.setInterceptorClass(loader.getInterceptorClass());
                injectCode(methodInfo);
            }
            methods.add(methodInfo);
        }

        writeMethods();
    }

    private MethodInfo readMethod() throws IOException {
        MethodInfo methodInfo = new MethodInfo();

        methodInfo.setAccessFlags(is.readUnsignedShort());
        methodInfo.setNameIndex(is.readUnsignedShort());
        methodInfo.setDescriptorIndex(is.readUnsignedShort());
        int attributesCount = is.readUnsignedShort();

        methodInfo.setAttributes(new AbstractAttribute[attributesCount]);

        for (int i = 0; i < attributesCount; i++) {
            AbstractAttribute attribute = attributeFacility.readAttribute();
            if (isInterceptorAnnotation(attribute)) {
                methodInfo.setInterceptorClass(((RuntimeInvisibleAnnotations) attribute).getInterceptorClass());
            }
            methodInfo.getAttributes()[i] = attribute;
            if (attribute.getClass() == Code.class) {
                methodInfo.setCode((Code) attribute);
            }
        }

        methodInfo.setSignature(getMethodSignature(methodInfo));
        methodInfo.setMethodName(constantPool.getUtf8(methodInfo.getNameIndex()).getString());

        return methodInfo;
    }

    private MethodSignature getMethodSignature(MethodInfo methodInfo) throws IOException {
        return new SignatureFacility(methodInfo, thisClassIndex, constantPool).getSignature();
    }

    private void writeMethods() throws IOException {
        for (MethodInfo method : methods) {
            os.writeShort(method.getAccessFlags());
            os.writeShort(method.getNameIndex());
            os.writeShort(method.getDescriptorIndex());
            os.writeShort(method.getAttributes().length);
            attributeFacility.writeAttributes(method.getAttributes());
        }
    }

    private boolean isInterceptorAnnotation(AbstractAttribute attribute) throws InvalidClassException {
        return attribute.getClass() == RuntimeInvisibleAnnotations.class
                && ((RuntimeInvisibleAnnotations) attribute).getInterceptorClass() != null;
    }

    private void injectCode(MethodInfo methodInfo) throws IOException {
        if (methodInfo.getCode() == null) {
            return;
        }

        InterceptorConstants interceptorConstants = getOrInsertInterceptorConstants(methodInfo.getInterceptorClass());

        fixupSignature(methodInfo.getSignature());

        Map<String, Integer> wrapperMethods = getOrInsertPrimitiveWrapperMethods(methodInfo.getSignature());

        ByteArrayOutputStream codeToInject = new ByteArrayOutputStream();
        InstructionsFacility instructionsFacility = new InstructionsFacility(
                new DataOutputStream(codeToInject), methodInfo.getSignature(), interceptorConstants, wrapperMethods);

        instructionsFacility.generateInterceptorInstantiationCode();

        instructionsFacility.generateInterceptorMethodParametersCode(getObjectClassIndex());

        instructionsFacility.generateObjectArrayFillingCode();

        instructionsFacility.generateInterceptorInvocationCode(interceptorConstants.getInterceptorMethod());
        
        Code code = methodInfo.getCode();

        int originalStackSize = code.getMaxStack();
        code.setMaxStack(Math.max(originalStackSize, instructionsFacility.getStackSizeNeeded()));

        code.setInjected(codeToInject.toByteArray());
        code.refresh();
    }

    private void fixupSignature(MethodSignature signature) throws IOException {
        int classNameIndex = signature.getClassNameIndex();
        String dottedName = constantPool.getUtf8(classNameIndex).getString().replace("/", ".");
        signature.setClassNameIndex(getOrInsertUtf8(dottedName));

        signature.setMethodNameIndex(getOrInsertString(signature.getMethodNameIndex())); //will be a String resource now
        signature.setClassNameIndex(getOrInsertString(signature.getClassNameIndex())); //will be a String resource now
    }

    private int getObjectClassIndex() {
        int objectClassName = constantPool.lookForUtf8Reference(Object.class.getName().replace(".", "/"));
        return constantPool.lookForClass(objectClassName);
    }

    private InterceptorConstants getOrInsertInterceptorConstants(Class interceptorClass) throws IOException {
        if (interceptorClass.getDeclaredMethods().length == 0) {
            throw new InvalidClassException("Interceptor class format not valid.");
        }

        InterceptorConstants interceptorConstants = new InterceptorConstants();

        interceptorConstants.setInterceptorClassName(getOrInsertUtf8(interceptorClass.getName().replace(".", "/")));
        interceptorConstants.setInterceptorClass(getOrInsertClass(interceptorConstants.getInterceptorClassName()));

        interceptorConstants.setInterceptorMethodName(getOrInsertUtf8(Constants.INTERCEPTOR_METHOD_NAME));
        interceptorConstants.setInterceptorMethodType(getOrInsertUtf8(Constants.INTERCEPTOR_METHOD_TYPE));
        interceptorConstants.setInterceptorMethodNameAndType(
                getOrInsertNameAndType(interceptorConstants.getInterceptorMethodName(), interceptorConstants.getInterceptorMethodType()));
        interceptorConstants.setInterceptorMethod(
                getOrInsertMethodRef(interceptorConstants.getInterceptorClass(), interceptorConstants.getInterceptorMethodNameAndType()));

        interceptorConstants.setConstructorName(getOrInsertUtf8(Constants.DEFAULT_CONSTRUCTOR_NAME));
        interceptorConstants.setConstructorType(getOrInsertUtf8(Constants.DEFAULT_CONSTRUCTOR_TYPE));
        interceptorConstants.setConstructorNameAndType(
                getOrInsertNameAndType(interceptorConstants.getConstructorName(), interceptorConstants.getConstructorType()));
        interceptorConstants.setConstructor(
                getOrInsertMethodRef(interceptorConstants.getInterceptorClass(), interceptorConstants.getConstructorNameAndType()));

        return interceptorConstants;
    }

    private int getOrInsertUtf8(String string) throws IOException {
        int nameIndex = constantPool.lookForUtf8Reference(string);
        if (nameIndex == -1) {
            nameIndex = constantPool.poolSize()  + 1;
            Utf8Constant name = new Utf8Constant(string);
            constantPool.add(name);
            name.serializeToStream(constantStream);
        }
        return nameIndex;
    }

    private int getOrInsertNameAndType(int nameIndex, int typeIndex) throws IOException {
        int nameAndTypeIndex = constantPool.lookForNameAndTypeReference(nameIndex, typeIndex);
        if (nameAndTypeIndex == -1) {
            nameAndTypeIndex = constantPool.poolSize() + 1;
            NameAndTypeConstant nameAndType = new NameAndTypeConstant(nameIndex, typeIndex);
            constantPool.add(nameAndType);
            nameAndType.serializeToStream(constantStream);
        }
        return nameAndTypeIndex;
    }

    private int getOrInsertClass(int nameIndex) throws IOException {
        int classIndex = constantPool.lookForClass(nameIndex);
        if (classIndex == -1) {
            classIndex = constantPool.poolSize() + 1;
            ClassConstant classConstant = new ClassConstant(nameIndex);
            constantPool.add(classConstant);
            classConstant.serializeToStream(constantStream);
        }
        return classIndex;
    }

    private int getOrInsertMethodRef(int classIndex, int nameAndTypeIndex) throws IOException {
        int methodRef = constantPool.lookForRef(classIndex, nameAndTypeIndex);
        if (methodRef == -1) {
            methodRef = constantPool.poolSize() + 1;
            MethodRefConstant methodRefConstant = new MethodRefConstant(classIndex, nameAndTypeIndex);
            constantPool.add(methodRefConstant);
            methodRefConstant.serializeToStream(constantStream);
        }
        return methodRef;
    }

    private int getOrInsertString(int utf8Index) throws IOException {
        int stringIndex = constantPool.lookForString(utf8Index);
        if (stringIndex == -1) {
            stringIndex = constantPool.poolSize() + 1;
            StringConstant stringConstant = new StringConstant(utf8Index);
            constantPool.add(stringConstant);
            stringConstant.serializeToStream(constantStream);
        }
        return stringIndex;
    }

    private Map<String,Integer> getOrInsertPrimitiveWrapperMethods(MethodSignature signature) throws IOException {
        Map<String,Integer> methodRefs = new HashMap<String, Integer>();

        int valueOfString = getOrInsertUtf8("valueOf");

        for(String paramType : signature.getParamTypes()) {
            char baseType = paramType.charAt(0);
            Class wrapperClass = getWrapperClass(baseType);
            if(wrapperClass != null) {
                int wrapperMethod = getOrInsertPrimitiveWrappeMethod(wrapperClass, baseType, valueOfString);
                methodRefs.put(paramType, wrapperMethod);
            }
        }

        return methodRefs;
    }

    private int getOrInsertPrimitiveWrappeMethod(Class wrapperClass, char baseType, int valueOfString) throws IOException {
        int descriptorString = getOrInsertUtf8("("+ baseType + ")" + ConstantPoolUtils.getTypeStringFromClass(wrapperClass));
        int nameAndType = getOrInsertNameAndType(valueOfString, descriptorString);
        int wrapperClassName = getOrInsertUtf8(wrapperClass.getName().replace(".", "/"));
        int wrapperClassRef = getOrInsertClass(wrapperClassName);
        int wrapperMethodRef = getOrInsertMethodRef(wrapperClassRef, nameAndType);
        return wrapperMethodRef;
    }


    private Class getWrapperClass(char baseType) {
        switch(baseType) {
            case 'B':
                return Byte.class;
            case 'C':
                return Character.class;
            case 'D':
                return Double.class;
            case 'F':
                return Float.class;
            case 'I':
                return Integer.class;
            case 'J':
                return Long.class;
            case 'S':
                return Short.class;
            case 'Z':
                return Boolean.class;
            default:
                return null;
        }
    }
}
