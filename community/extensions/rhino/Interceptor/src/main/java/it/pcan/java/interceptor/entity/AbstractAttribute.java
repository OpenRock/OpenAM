/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.entity;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Pierantonio
 */
public abstract class AbstractAttribute {

    private String attrName;

    private int attributeNameIndex;
    private int attributeLength;
    private byte[] info;

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    

    public byte[] getInfo() {
        return info;
    }

    public void setInfo(byte[] info) {
        this.info = info;
    }

    public int getAttributeLength() {
        return attributeLength;
    }

    public void setAttributeLength(int attributeLength) {
        this.attributeLength = attributeLength;
    }

    public int getAttributeNameIndex() {
        return attributeNameIndex;
    }

    public void setAttributeNameIndex(int attributeNameIndex) {
        this.attributeNameIndex = attributeNameIndex;
    }

    @Override
    public String toString() {
        return attrName;
    }

    public void serializeToStream(DataOutputStream os) throws IOException{
        os.writeShort(attributeNameIndex);
        os.writeInt(attributeLength);
        os.write(info);
    }

}
