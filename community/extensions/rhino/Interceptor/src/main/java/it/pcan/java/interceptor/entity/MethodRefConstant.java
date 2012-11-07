/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pcan.java.interceptor.entity;

import it.pcan.java.interceptor.util.Constants;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Pierantonio
 */
public class MethodRefConstant extends RefConstant {

    public MethodRefConstant(int classIndex, int nameAndTypeIndex) {
        super(classIndex, nameAndTypeIndex);
    }

    public void serializeToStream(DataOutputStream os) throws IOException {
        os.writeByte(Constants.CONSTANT_Methodref);
        os.writeShort(this.getClassIndex());
        os.writeShort(this.getNameAndTypeIndex());
    }
}
