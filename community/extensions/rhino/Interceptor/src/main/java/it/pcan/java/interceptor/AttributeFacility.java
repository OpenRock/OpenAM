/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor;

import it.pcan.java.interceptor.annotations.InterceptedBy;
import it.pcan.java.interceptor.entity.Code.ExceptionTableEntry;
import it.pcan.java.interceptor.entity.LocalVariableTable.LocalVariableTableEntry;
import it.pcan.java.interceptor.entity.*;
import it.pcan.java.interceptor.entity.LineNumberTable.LineNumberEntry;
import it.pcan.java.interceptor.entity.LocalVariableTypeTable.LocalVariableTypeTableEntry;
import it.pcan.java.interceptor.util.ConstantPoolUtils;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;

/**
 *
 * @author Pierantonio
 */
class AttributeFacility {

    private final DataInputStream is;
    private final DataOutputStream os;
    private final ConstantPool constantPool;
    private final static String INTERCEPTED_BY_ANNOTATION_CLASS_NAME = ConstantPoolUtils.getTypeStringFromClass(InterceptedBy.class);

    AttributeFacility(DataInputStream is, DataOutputStream os, ConstantPool constantPool) {
        this.is = is;
        this.os = os;
        this.constantPool = constantPool;
    }

    AbstractAttribute readAttribute() throws IOException {

        int nameIndex = is.readUnsignedShort();
        int length = is.readInt();

        AbstractAttribute attribute;
        String attrName = constantPool.getUtf8(nameIndex).getString();
        if (attrName.equals(RuntimeInvisibleAnnotations.class.getSimpleName())) {
            attribute = readRuntimeInvisibleAnnotation(nameIndex, length);
        } else if (attrName.equals(Code.class.getSimpleName())) {
            attribute = readCode(nameIndex, length);
        } else if (attrName.equals(LocalVariableTable.class.getSimpleName())) {
            attribute = readLocalVariableTable(nameIndex, length);
        } else if (attrName.equals(LocalVariableTypeTable.class.getSimpleName())) {
            attribute = readLocalVariableTypeTable(nameIndex, length);
        } else if (attrName.equals(LineNumberTable.class.getSimpleName())) {
            attribute = readLineNumberTable(nameIndex, length);
        } else {
            attribute = readUnknownAttribute(nameIndex, length);
        }

        attribute.setAttrName(attrName);
        return attribute;
    }

    private UnknownAttribute readUnknownAttribute(int nameIndex, int length) throws IOException {
        UnknownAttribute attribute = new UnknownAttribute();
        readRawData(attribute, nameIndex, length);
        return attribute;
    }

    private void readRawData(AbstractAttribute attribute, int nameIndex, int length) throws IOException {
        attribute.setAttributeNameIndex(nameIndex);
        attribute.setAttributeLength(length);
        attribute.setInfo(new byte[length]);
        is.read(attribute.getInfo());
    }

    void writeAttributes(AbstractAttribute[] attributes) throws IOException {
        if(os != null) {
            for (AbstractAttribute attribute : attributes) {
                os.writeShort(attribute.getAttributeNameIndex());
                os.writeInt(attribute.getInfo().length);
                os.write(attribute.getInfo());
            }
        }
    }

    private AbstractAttribute readRuntimeInvisibleAnnotation(int nameIndex, int length) throws IOException {
        RuntimeInvisibleAnnotations attribute = new RuntimeInvisibleAnnotations();
        readRawData(attribute, nameIndex, length);

        DataInputStream attrStream = new DataInputStream(new ByteArrayInputStream(attribute.getInfo()));

        int numAnnotations = attrStream.readUnsignedShort();

        Class interceptorClass = findInterceptorAnnotationValue(attrStream, numAnnotations);
        attribute.setInterceptorClass(interceptorClass);

        return attribute;
    }

    private Class findInterceptorAnnotationValue(DataInputStream attrStream, int numAnnotations) throws IOException {
        for (int i = 0; i < numAnnotations; i++) {
            int typeIndex = attrStream.readUnsignedShort();
            int numElementValues = attrStream.readUnsignedShort();
            if (constantPool.getUtf8(typeIndex).getString().equals(INTERCEPTED_BY_ANNOTATION_CLASS_NAME) && numElementValues == 1) {
                int elementNameIndex = attrStream.readUnsignedShort(); // points to "value" string.
                byte tag = attrStream.readByte();
                if (tag != 'c') {
                    throw new InvalidClassException("Invalid annotation argument. Not a class.");
                }
                String interceptorClassStr = constantPool.getUtf8(attrStream.readUnsignedShort()).getString();
                if (!interceptorClassStr.startsWith("L")) {
                    throw new InvalidClassException("Invalid annotation argument descriptor. Not a class.");
                }
                interceptorClassStr = interceptorClassStr.substring(1, interceptorClassStr.length() - 1).replace("/", ".");
                try {
                    return Class.forName(interceptorClassStr);
                } catch (ClassNotFoundException ex) {
                    throw new InvalidClassException("Interceptor class not found: " + interceptorClassStr);
                }
            }

        }
        return null;
    }

    private AbstractAttribute readCode(int nameIndex, int length) throws IOException {
        Code attribute = new Code();
        readRawData(attribute, nameIndex, length);

        DataInputStream attrStream = new DataInputStream(new ByteArrayInputStream(attribute.getInfo()));

        int maxStack = attrStream.readUnsignedShort();
        int maxLocals = attrStream.readUnsignedShort();
        int codeLength = attrStream.readInt();
        byte code[] = new byte[codeLength];
        attrStream.read(code);
        int exceptionTableLength = attrStream.readUnsignedShort();
        ExceptionTableEntry[] exceptionTable = new ExceptionTableEntry[exceptionTableLength];
        for (int i = 0; i < exceptionTableLength; i++) {
            ExceptionTableEntry exceptionEntry = readExceptionTable(attrStream);
            exceptionTable[i] = exceptionEntry;
        }
        int attributesCount = attrStream.readUnsignedShort();
        AbstractAttribute [] attributes = new AbstractAttribute[attributesCount];
        AttributeFacility innerFacility = new AttributeFacility(attrStream, null, constantPool);
        for (int i = 0; i < attributesCount; i++) {
            AbstractAttribute innerAttribute = innerFacility.readAttribute();
            if(innerAttribute.getClass() == LineNumberTable.class) {
                attribute.setLineNumberTable((LineNumberTable)innerAttribute);
            } else if (innerAttribute.getClass() == LocalVariableTable.class) {
                attribute.setLocalVariableTable((LocalVariableTable)innerAttribute);
            } else if (innerAttribute.getClass() == LocalVariableTypeTable.class) {
                attribute.setLocalVariableTypeTable((LocalVariableTypeTable)innerAttribute);
            }
            attributes[i] = innerAttribute;
        }

        attribute.setMaxStack(maxStack);
        attribute.setMaxLocals(maxLocals);
        attribute.setCode(code);
        attribute.setExceptionTable(exceptionTable);
        attribute.setAttributes(attributes);

        return attribute;
    }

    private ExceptionTableEntry readExceptionTable(DataInputStream attrStream) throws IOException {
        ExceptionTableEntry exceptionTable = new ExceptionTableEntry();
        exceptionTable.setStartPc(attrStream.readUnsignedShort());
        exceptionTable.setEndPc(attrStream.readUnsignedShort());
        exceptionTable.setHandlerPc(attrStream.readUnsignedShort());
        exceptionTable.setCatchType(attrStream.readUnsignedShort());
        return exceptionTable;
    }

    private AbstractAttribute readLocalVariableTable(int nameIndex, int length) throws IOException {
        LocalVariableTable attribute = new LocalVariableTable();
        readRawData(attribute, nameIndex, length);
        DataInputStream attrStream = new DataInputStream(new ByteArrayInputStream(attribute.getInfo()));

        int localVariableTableLength = attrStream.readUnsignedShort();
        attribute.setTable(new LocalVariableTableEntry[localVariableTableLength]);
        for (int i = 0; i < localVariableTableLength; i++) {
            LocalVariableTableEntry entry = readLocalVariableEntry(attrStream);
            attribute.getTable()[i] = entry;
        }
        return attribute;
    }

    private AbstractAttribute readLocalVariableTypeTable(int nameIndex, int length) throws IOException {
        LocalVariableTypeTable attribute = new LocalVariableTypeTable();
        readRawData(attribute, nameIndex, length);
        DataInputStream attrStream = new DataInputStream(new ByteArrayInputStream(attribute.getInfo()));

        int localVariableTypeTableLength = attrStream.readUnsignedShort();
        attribute.setTable(new LocalVariableTypeTableEntry[localVariableTypeTableLength]);
        for (int i = 0; i < localVariableTypeTableLength; i++) {
            LocalVariableTypeTableEntry entry = readLocalVariableTypeEntry(attrStream);
            attribute.getTable()[i] = entry;
        }
        return attribute;
    }

    private LocalVariableTableEntry readLocalVariableEntry(DataInputStream attrStream) throws IOException {
        LocalVariableTableEntry entry = new LocalVariableTableEntry();
        entry.setStartPc(attrStream.readUnsignedShort());
        entry.setLength(attrStream.readUnsignedShort());
        entry.setNameIndex(attrStream.readUnsignedShort());
        entry.setDescriptorIndex(attrStream.readUnsignedShort());
        entry.setIndex(attrStream.readUnsignedShort());
        return entry;
    }

    private LocalVariableTypeTableEntry readLocalVariableTypeEntry(DataInputStream attrStream) throws IOException {
        LocalVariableTypeTableEntry entry = new LocalVariableTypeTableEntry();
        entry.setStartPc(attrStream.readUnsignedShort());
        entry.setLength(attrStream.readUnsignedShort());
        entry.setNameIndex(attrStream.readUnsignedShort());
        entry.setSignatureIndex(attrStream.readUnsignedShort());
        entry.setIndex(attrStream.readUnsignedShort());
        return entry;
    }

    private AbstractAttribute readLineNumberTable(int nameIndex, int length) throws IOException {
        LineNumberTable attribute = new LineNumberTable();
        readRawData(attribute, nameIndex, length);
        DataInputStream attrStream = new DataInputStream(new ByteArrayInputStream(attribute.getInfo()));

        int lineNumberTableLength = attrStream.readUnsignedShort();
        attribute.setTable(new LineNumberEntry[lineNumberTableLength]);
        for (int i = 0; i < lineNumberTableLength; i++) {
            LineNumberEntry entry = readLineNumberEntry(attrStream);
            attribute.getTable()[i] = entry;
        }
        return attribute;
    }

    private LineNumberEntry readLineNumberEntry(DataInputStream attrStream) throws IOException {
        LineNumberEntry entry = new LineNumberEntry();
        entry.setStartPc(attrStream.readUnsignedShort());
        entry.setLineNumber(attrStream.readUnsignedShort());
        return entry;
    }
}
