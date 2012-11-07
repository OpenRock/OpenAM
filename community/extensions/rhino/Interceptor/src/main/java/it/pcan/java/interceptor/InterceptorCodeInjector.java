/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor;

import it.pcan.java.interceptor.entity.*;
import it.pcan.java.interceptor.util.ConstantPoolUtils;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.util.ArrayList;
import it.pcan.java.interceptor.util.Constants;

/**
 *
 * @author Pierantonio
 */
public class InterceptorCodeInjector {

    private final DataInputStream is;
    private final String className;
    private final ByteArrayOutputStream outputByteArray = new ByteArrayOutputStream();
    private final ByteArrayOutputStream tempConstantPoolStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream tempFieldsStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream tempMethodsStream = new ByteArrayOutputStream();

    private final DataOutputStream os = new DataOutputStream(outputByteArray);
    private ConstantPool constants = new ConstantPool();
    private List<Integer> interfaces = new ArrayList<Integer>();

    public InterceptorCodeInjector(InputStream classData, String className) {
        this.is = new DataInputStream(classData);
        this.className = className;
    }

    public byte[] inject(InterceptorInfo methods) throws IOException {

        processHeader();

        boolean interceptorClassFound = readConstantPool();

        if (interceptorClassFound) {

            int accessFlags = is.readUnsignedShort();
            int thisClass = is.readUnsignedShort();
            int superClass = is.readUnsignedShort();
            int interfacesCount = is.readUnsignedShort();
            readInterfaces(interfacesCount);

            readFields();

            DataOutputStream methodDataStream = new DataOutputStream(tempMethodsStream);
            DataOutputStream constantDataStream = new DataOutputStream(tempConstantPoolStream);

            MethodInfoFacility methodInfoFacility = new MethodInfoFacility(is, methodDataStream, constantDataStream, constants, thisClass);
            //this call does the magic :)
            methodInfoFacility.processMethods(methods);

            writeConstantPool();

            os.writeShort(accessFlags);
            os.writeShort(thisClass);
            os.writeShort(superClass);
            os.writeShort(interfacesCount);
            writeInterfaces();

            writeFields();

            writeMethods();
        } else {
            writeConstantPool();
        }

        //pass through all the remaining data
        passThrough(is);

        return outputByteArray.toByteArray();
    }

    private void passThrough(DataInputStream stream) throws IOException {
        try {
            while (true) {
                os.writeByte(stream.readByte());
            }
        } catch (EOFException ex) {
        }
    }

    /**
     * processes from "magic" field to "major_version" field
     * @throws IOException
     */
    private void processHeader() throws IOException {
        int magic = is.readInt();
        if (magic != Constants.magic) {
            throw new InvalidClassException(className, "Magic number mismatch");
        }
        os.writeInt(magic);
        short minor = is.readShort();
        short major = is.readShort();
        os.writeShort(minor);
        os.writeShort(major);
    }

    /**
     * processes from "constant_pool_count" to "constant_pool" field
     * @return true if a reference to Interceptor class is found, false otherwise
     * @throws IOException
     */
    private boolean readConstantPool() throws IOException {
        int constantPoolSize = is.readUnsignedShort();


        DataOutputStream tempDataStream = new DataOutputStream(tempConstantPoolStream);

        for (int i = 0; i < constantPoolSize - 1; i++) {
            int tag = is.readUnsignedByte();
            tempDataStream.writeByte(tag);

            switch (tag) {
                case Constants.CONSTANT_Class:
                    readClassConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_Fieldref:
                case Constants.CONSTANT_Methodref:
                case Constants.CONSTANT_InterfaceMethodref:
                    readRefConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_String:
                    readStringConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_Integer:
                    readIntegerConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_Float:
                    readFloatConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_Long:
                    i++; // see section 4.4.5.: The CONSTANT_Long_info and CONSTANT_Double_info Structures
                    readLongConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_Double:
                    i++; // as above
                    readDoubleConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_NameAndType:
                    readNameAndTypeConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_Utf8:
                    readUtf8Constant(tempDataStream);
                    break;
                case Constants.CONSTANT_MethodHandle:
                    readMethodHandleConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_MethodType:
                    readMethodTypeConstant(tempDataStream);
                    break;
                case Constants.CONSTANT_InvokeDynamic:
                    readInvokeDynamicConstant(tempDataStream);
                    break;
                default:
                    throw new InvalidClassException(className, String.format("Invalid tag found in Constant Pool: %02X", tag));
            }

        }

        return true;
    }

    private void writeConstantPool() throws IOException {
        os.writeShort(constants.poolSize() + 1);
        DataInputStream passThroughConstants = new DataInputStream(new ByteArrayInputStream(tempConstantPoolStream.toByteArray()));
        passThrough(passThroughConstants);
    }

    private void readClassConstant(DataOutputStream tempDataStream) throws IOException {
        int nameIndex = is.readUnsignedShort();
        constants.add(new ClassConstant(nameIndex));
        tempDataStream.writeShort(nameIndex);
    }

    private void readRefConstant(DataOutputStream tempDataStream) throws IOException {
        int classIndex = is.readUnsignedShort();
        int nameAndTypeIndex = is.readUnsignedShort();
        constants.add(new RefConstant(classIndex, nameAndTypeIndex));
        tempDataStream.writeShort(classIndex);
        tempDataStream.writeShort(nameAndTypeIndex);
    }

    private void readStringConstant(DataOutputStream tempDataStream) throws IOException {
        int stringIndex = is.readUnsignedShort();
        constants.add(new StringConstant(stringIndex));
        tempDataStream.writeShort(stringIndex);
    }

    private void readIntegerConstant(DataOutputStream tempDataStream) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        constants.add(new IntegerConstant(bytes));
        tempDataStream.write(bytes);
    }

    private void readFloatConstant(DataOutputStream tempDataStream) throws IOException {
        byte[] bytes = new byte[4];
        is.read(bytes);
        constants.add(new FloatConstant(bytes));
        tempDataStream.write(bytes);
    }

    private void readLongConstant(DataOutputStream tempDataStream) throws IOException {
        byte[] bytes = new byte[8];
        is.read(bytes);
        constants.add(new LongConstant(bytes));
        tempDataStream.write(bytes);
    }

    private void readDoubleConstant(DataOutputStream tempDataStream) throws IOException {
        byte[] bytes = new byte[8];
        is.read(bytes);
        constants.add(new DoubleConstant(bytes));
        tempDataStream.write(bytes);
    }

    private void readNameAndTypeConstant(DataOutputStream tempDataStream) throws IOException {
        int nameIndex = is.readUnsignedShort();
        int descriptorIndex = is.readUnsignedShort();
        constants.add(new NameAndTypeConstant(nameIndex, descriptorIndex));
        tempDataStream.writeShort(nameIndex);
        tempDataStream.writeShort(descriptorIndex);
    }

    private void readUtf8Constant(DataOutputStream tempDataStream) throws IOException {
        int length = is.readUnsignedShort();
        byte[] buf = new byte[length];
        is.read(buf);
        constants.add(new Utf8Constant(new String(buf)));
        tempDataStream.writeShort(length);
        tempDataStream.write(buf);
    }

    private void readMethodHandleConstant(DataOutputStream tempDataStream) throws IOException {
        int referenceKind = is.readUnsignedByte();
        int referenceIndex = is.readUnsignedShort();
        constants.add(new MethodHandleConstant(referenceKind, referenceIndex));
        tempDataStream.writeByte(referenceKind);
        tempDataStream.writeShort(referenceIndex);
    }

    private void readMethodTypeConstant(DataOutputStream tempDataStream) throws IOException {
        int descriptorIndex = is.readUnsignedShort();
        constants.add(new MethodTypeConstant(descriptorIndex));
        tempDataStream.writeShort(descriptorIndex);
    }

    private void readInvokeDynamicConstant(DataOutputStream tempDataStream) throws IOException {
        int bootstrapMethodAttrIndex = is.readUnsignedShort();
        int nameAndTypeIndex = is.readUnsignedShort();
        constants.add(new InvokeDynamicConstant(bootstrapMethodAttrIndex, nameAndTypeIndex));
        tempDataStream.writeShort(bootstrapMethodAttrIndex);
        tempDataStream.writeShort(nameAndTypeIndex);
    }

    private void readInterfaces(int interfacesCount) throws IOException {
        for (int i = 0; i < interfacesCount; i++) {
            interfaces.add(is.readUnsignedShort());
        }
    }

    private void writeInterfaces() throws IOException {
        for (Integer i : interfaces) {
            os.writeShort(i.intValue());
        }
    }

    private void readFields() throws IOException {
        DataOutputStream tempDataStream = new DataOutputStream(tempFieldsStream);
        int fieldsCount = is.readUnsignedShort();
        tempDataStream.writeShort(fieldsCount);

        for (int i = 0; i < fieldsCount; i++) {
            int accessFlags = is.readUnsignedShort();
            int nameIndex = is.readUnsignedShort();
            int descriptorIndex = is.readUnsignedShort();
            int attributesCount = is.readUnsignedShort();

            tempDataStream.writeShort(accessFlags);
            tempDataStream.writeShort(nameIndex);
            tempDataStream.writeShort(descriptorIndex);
            tempDataStream.writeShort(attributesCount);

            for (int j = 0; j < attributesCount; j++) {
                readUnusedAttribute(tempDataStream);
            }
        }
    }
    
    private void writeFields() throws IOException {
        DataInputStream passThroughFields = new DataInputStream(new ByteArrayInputStream(tempFieldsStream.toByteArray()));
        passThrough(passThroughFields);
    }

    private void readUnusedAttribute(DataOutputStream tempDataStream) throws IOException {
        int attributeNameIndex = is.readUnsignedShort();
        int attributeLength = is.readInt();
        tempDataStream.writeShort(attributeNameIndex);
        tempDataStream.writeInt(attributeLength);
        for(int i = 0; i< attributeLength; i++) {
            tempDataStream.writeByte(is.readUnsignedByte());
        }
    }

    private void writeMethods() throws IOException {
        DataInputStream passThroughMethods = new DataInputStream(new ByteArrayInputStream(tempMethodsStream.toByteArray()));
        passThrough(passThroughMethods);
    }

}
