/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.entity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Pierantonio
 */
public class Code extends AbstractAttribute {

    private LineNumberTable lineNumberTable;
    private LocalVariableTable localVariableTable;
    private LocalVariableTypeTable localVariableTypeTable;
    private int maxStack;
    private int maxLocals;
    private byte[] code;
    private ExceptionTableEntry[] exceptionTable;
    private AbstractAttribute[] attributes;
    private byte[] injected;

    public byte[] getInjected() {
        return injected;
    }

    public void setInjected(byte[] injected) {
        this.injected = injected;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public void setMaxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public ExceptionTableEntry[] getExceptionTable() {
        return exceptionTable;
    }

    public void setExceptionTable(ExceptionTableEntry[] exceptionTable) {
        this.exceptionTable = exceptionTable;
    }

    public AbstractAttribute[] getAttributes() {
        return attributes;
    }

    public void setAttributes(AbstractAttribute[] attributes) {
        this.attributes = attributes;
    }

    public LineNumberTable getLineNumberTable() {
        return lineNumberTable;
    }

    public void setLineNumberTable(LineNumberTable lineNumberTable) {
        this.lineNumberTable = lineNumberTable;
    }

    public LocalVariableTable getLocalVariableTable() {
        return localVariableTable;
    }

    public void setLocalVariableTable(LocalVariableTable localVariableTable) {
        this.localVariableTable = localVariableTable;
    }

    public LocalVariableTypeTable getLocalVariableTypeTable() {
        return localVariableTypeTable;
    }

    public void setLocalVariableTypeTable(LocalVariableTypeTable localVariableTypeTable) {
        this.localVariableTypeTable = localVariableTypeTable;
    }



    public static class ExceptionTableEntry {

        private int startPc;
        private int endPc;
        private int handlerPc;
        private int catchType;

        public int getCatchType() {
            return catchType;
        }

        public void setCatchType(int catchType) {
            this.catchType = catchType;
        }

        public int getEndPc() {
            return endPc;
        }

        public void setEndPc(int endPc) {
            this.endPc = endPc;
        }

        public int getHandlerPc() {
            return handlerPc;
        }

        public void setHandlerPc(int handlerPc) {
            this.handlerPc = handlerPc;
        }

        public int getStartPc() {
            return startPc;
        }

        public void setStartPc(int startPc) {
            this.startPc = startPc;
        }

        public void serializeToStream(DataOutputStream os) throws IOException{
            os.writeShort(startPc);
            os.writeShort(endPc);
            os.writeShort(handlerPc);
            os.writeShort(catchType);
        }
    }

    public void refresh() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(byteArrayOutputStream);

        int injectedCodeLength = getInjectedLength();

        if(this.lineNumberTable != null) {
            lineNumberTable.refresh(injectedCodeLength);
        }
        if(this.localVariableTable != null) {
            this.localVariableTable.refresh(injectedCodeLength);
        }
        if(this.localVariableTypeTable != null) {
            this.localVariableTypeTable.refresh(injectedCodeLength);
        }

        this.setAttributeLength(calcAttributeLength());

        os.writeShort(maxStack);
        os.writeShort(maxLocals);

        os.writeInt(injectedCodeLength + code.length);

        if(injected != null) {
            os.write(injected);
        }
       
        os.write(code);
        
        os.writeShort(exceptionTable.length);
        for(ExceptionTableEntry entry : exceptionTable) {
            refreshExceptionEntry(entry, injectedCodeLength);
            entry.serializeToStream(os);
        }

        os.writeShort(attributes.length);
        for(AbstractAttribute attribute : attributes) {
            attribute.serializeToStream(os);
        }

        this.setInfo(byteArrayOutputStream.toByteArray());
    }

    private void refreshExceptionEntry(ExceptionTableEntry entry, int injectedCodeLength) {
        entry.setStartPc(entry.getStartPc() + injectedCodeLength);
        entry.setEndPc(entry.getEndPc() + injectedCodeLength);
        entry.setHandlerPc(entry.getHandlerPc() + injectedCodeLength);
    }

    private int calcAttributeLength(){
        int length = 14  // 2 + 4 + 2 + 2 + 4;
                     + code.length
                     + 2
                     + exceptionTable.length * 8
                     + 2;

        for(AbstractAttribute attribute : attributes) {
            length += (6 + attribute.getInfo().length);
        }

        length += getInjectedLength();
        
        return length;
    }

    private int getInjectedLength(){
        return (injected==null ? 0 : injected.length);
    }

}
