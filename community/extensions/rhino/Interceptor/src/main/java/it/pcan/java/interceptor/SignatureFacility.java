/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor;

import it.pcan.java.interceptor.entity.ClassConstant;
import it.pcan.java.interceptor.entity.ConstantPool;
import it.pcan.java.interceptor.entity.MethodInfo;
import it.pcan.java.interceptor.entity.MethodSignature;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 *
 * @author Pierantonio
 */
class SignatureFacility {

    private final MethodInfo methodInfo;
    private final int classIndex;
    private final ConstantPool constantPool;

    public SignatureFacility(MethodInfo methodInfo, int classIndex, ConstantPool constantPool) {
        this.methodInfo=methodInfo;
        this.classIndex = classIndex;
        this.constantPool = constantPool;
    }

    public MethodSignature getSignature() throws IOException {
        IOException exc = new InvalidClassException("Invalid method signature.");

        MethodSignature signature = new MethodSignature();
        signature.setParamTypes(new ArrayList<String>());

        ClassConstant classConstant = (ClassConstant)constantPool.getById(classIndex);
        signature.setMethodNameIndex(methodInfo.getNameIndex());
        signature.setClassNameIndex(classConstant.getNameIndex());
        signature.setIsStatic((methodInfo.getAccessFlags() & 0x0008) != 0);

        String signatureStr = constantPool.getUtf8(methodInfo.getDescriptorIndex()).getString();

        StringTokenizer tokenizer = new StringTokenizer(signatureStr, "()", true);

        if (!tokenizer.nextToken().equals("(")) {
            throw exc;
        }

        String nextToken = tokenizer.nextToken();
        if (!nextToken.equals(")")) {
            parseParamTypes(nextToken, signature);

            if (!tokenizer.nextToken().equals(")")) {
                throw exc;
            }
        }
        parseReturnType(tokenizer.nextToken(), signature);

        fillReferences(signature);

        return signature;
    }

    private void parseParamTypes(String token, MethodSignature signature) {
        String delim = "[BCDFIJSZL;";
        StringTokenizer tokenizer = new StringTokenizer(token, delim, true);
        while (tokenizer.hasMoreTokens()) {
            String t = tokenizer.nextToken(delim);
            if(t.equals(";")) {
                continue;
            }
            signature.getParamTypes().add(parseParam(tokenizer, t));
        }

    }

    private String parseParam(StringTokenizer tokenizer, String t) {

        String paramType = t;

        if (t.equals("[")) {
            while (t.equals("[")) {
                t = tokenizer.nextToken();
                paramType += t;
            }
            if (t.equals("L")) {
                paramType += getClassName(tokenizer);
            } else {
                paramType += t;
            }
        } else if (t.equals("L")) {
            paramType += getClassName(tokenizer);
        }
        return paramType;
    }

    private String getClassName(StringTokenizer tokenizer) {
        return tokenizer.nextToken(";") + tokenizer.nextToken(";");
    }

    private void parseReturnType(String token, MethodSignature signature) {
        StringTokenizer tokenizer = new StringTokenizer(token, "[BCDFIJSZLV;", true);
        signature.setReturnType(parseParam(tokenizer, token));
    }

    private void fillReferences(MethodSignature signature) throws InvalidClassException {
        Integer[] paramTypesRef = new Integer[signature.getParamTypes().size()];
        int i = 0;
        for (String paramType : signature.getParamTypes()) {
            int reference = constantPool.lookForUtf8Reference(paramType);
            if(reference == -1) {
                throw new InvalidClassException("Reference to utf8 string '" + paramType + "' not found in Constant Pool.");
            }
            paramTypesRef[i++] = reference;
        }

        if(!signature.getReturnType().equals("V")) {
            signature.setReturnTypeRef(constantPool.lookForUtf8Reference(signature.getReturnType()));
        }
        signature.setParamTypesRef(Arrays.asList(paramTypesRef));
    }


}
