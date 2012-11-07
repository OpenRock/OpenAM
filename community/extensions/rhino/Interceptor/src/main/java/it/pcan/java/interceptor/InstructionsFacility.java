/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor;

import java.util.Map;
import it.pcan.java.interceptor.entity.InterceptorConstants;
import it.pcan.java.interceptor.entity.MethodSignature;
import java.io.DataOutputStream;
import java.io.IOException;
import static it.pcan.java.interceptor.InstructionSet.*;

/**
 * The core of this library. All methods generate bytecode instructions for interceptor invocation.
 * So, comments above methods DO NOT SAY what methods do; they say what the GENERATED INSTRUCTIONS will do.
 * 
 * NOTE:
 * The stack represented in comments grows from left to right:
 * 
 * Stack before push(Element3): 
 * |Element1|Element2|
 *
 * Stack after push(Element3):
 * |Element1|Element2|Element3|
 *
 *
 * @author Pierantonio
 */
class InstructionsFacility {

    private final DataOutputStream os;
    private final MethodSignature signature;
    private final InterceptorConstants interceptorConstants;
    private final Map<String, Integer> wrapperMethods;
    private final int REQUIRED_STACK_SIZE = 9;

    public InstructionsFacility(DataOutputStream os, MethodSignature signature, InterceptorConstants interceptorConstants, Map<String, Integer> wrapperMethods) {
        this.os = os;
        this.signature = signature;
        this.interceptorConstants = interceptorConstants;
        this.wrapperMethods = wrapperMethods;
    }

    /**
     * Instantiatiate interceptor: (new InterceptorClass())
     *
     * Stack after these instructions:
     * |Interceptor Obj.|
     *
     * @throws IOException
     */
    public void generateInterceptorInstantiationCode() throws IOException {
        b(NEW);
        s(interceptorConstants.getInterceptorClass());
        b(DUP); //una copia del riferimento resta sullo stack per la chiamata all'interceptor method
        b(INVOKESPECIAL);
        s(interceptorConstants.getConstructor());
    }

    /**
     * Loads interceptor method's parameters onto the stack.
     *
     * Stack after these instructions:
     * |Interceptor Obj.|Intercepted class name|Intercepted method name|new Object[numparams]|
     *
     * @param objectClassIndex
     * @throws IOException
     */
    public void generateInterceptorMethodParametersCode(int objectClassIndex) throws IOException {

        if(!signature.isIsStatic()) {
            b(ALOAD);
            b(0);
        }else{
            b(ACONST_NULL);
        }
        
        b(LDC_W);
        s(signature.getClassNameIndex());

        b(LDC_W);
        s(signature.getMethodNameIndex());

        b(SIPUSH);
        s(signature.getParamTypes().size());
        b(ANEWARRAY);
        s(objectClassIndex);
    }

    /**
     * Fills the object array with the actual parameters of the intercepted method.
     *
     * Stack after these instructions:
     * |Interceptor Obj.|Intercepted class name|Intercepted method name|new Object[numparams] (Filled)|
     *
     * @param objectClassIndex
     * @throws IOException
     */
    public void generateObjectArrayFillingCode() throws IOException {

        int locVar = signature.isIsStatic() ? 0 : 1;
        for (int i = 0; i < signature.getParamTypes().size(); i++) {
            String paramType = signature.getParamTypes().get(i);
            Integer wrapperMethodRef = wrapperMethods.get(paramType);
            if (wrapperMethodRef == null) {
                generateReferenceParamInsertionCode(i, locVar);
            } else {
                char baseType = paramType.charAt(0);
                generatePrimitiveParamInsertionCode(i, locVar, baseType, wrapperMethodRef.intValue());
                if (baseType == 'J' || baseType == 'D') {
                    locVar++;
                }
            }
            locVar++;
        }

    }

    /**
     * A primitive-type parameter needs to be "wrapped" before the insertion in the object array.
     * To do this, for each primitive type, a static method "valueOf" is called: it returns
     * a reference to the wrapper object.
     *
     * Stack after these instructions:
     * |Interceptor Obj.|Intercepted class name|Intercepted method name|new Object[numparams] (partially filled)|
     *
     * @param arrayIndex
     * @param localVarIndex
     * @param paramType
     * @param wrapperMethodRef
     * @throws IOException
     */
    private void generatePrimitiveParamInsertionCode(int arrayIndex, int localVarIndex, char paramType, int wrapperMethodRef) throws IOException {
        b(DUP);
        b(SIPUSH);
        s(arrayIndex);

        switch (paramType) {
            case 'J':
                b(LLOAD);
                break;
            case 'F':
                b(FLOAD);
                break;
            case 'D':
                b(DLOAD);
                break;
            default:
                b(ILOAD);
        }

        b(localVarIndex); // ILOAD, DLOAD, LLOAD and FLOAD require only one byte (so the maximum number of params is limited to 255)
        b(INVOKESTATIC);
        s(wrapperMethodRef);
        b(AASTORE);
    }

    /**
     * Insert a parameter reference into the object array.
     *
     * Stack after these instructions:
     * |Interceptor Obj.|Intercepted class name|Intercepted method name|new Object[numparams] (partially filled)|
     *
     * @param arrayIndex
     * @param localVarIndex
     * @throws IOException
     */
    private void generateReferenceParamInsertionCode(int arrayIndex, int localVarIndex) throws IOException {
        b(DUP);
        b(SIPUSH);
        s(arrayIndex);
        b(ALOAD);
        b(localVarIndex);  // as above, ALOAD require only one byte (so the maximum number of params is limited to 255)
        b(AASTORE);
    }

    /**
     *  Invokes interceptor method.
     *
     * Stack after these instructions:
     * --empty--
     * 
     * @param methodRefIndex
     * @throws IOException
     */
    public void generateInterceptorInvocationCode(int methodRefIndex) throws IOException {
        b(INVOKEVIRTUAL);
        s(methodRefIndex);
    }

    /**
     * The value returned by this method has been calculated manually, taking in account
     * the maximum stack depth needed during the execution of the generated code.
     *
     * @return
     */
    public int getStackSizeNeeded() {
        return REQUIRED_STACK_SIZE;
    }

    /**
     * Shortening method for single byte insertion.
     * @param opCode
     * @throws IOException
     */
    private void b(int opCode) throws IOException {
        os.writeByte(opCode);
    }

    /**
     * Shortening method for double byte insertion.
     * @param shortValue
     * @throws IOException
     */
    private void s(int shortValue) throws IOException {
        os.writeShort(shortValue);
    }
}
