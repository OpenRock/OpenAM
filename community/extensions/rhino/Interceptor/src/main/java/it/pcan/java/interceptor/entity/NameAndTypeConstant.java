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
public class NameAndTypeConstant extends ClassConstant {

    private final int descriptorIndex;

    public NameAndTypeConstant(int nameIndex, int descriptorIndex) {
        super(nameIndex);
        this.descriptorIndex = descriptorIndex;
    }

    public int getDescriptorIndex() {
        return descriptorIndex;
    }

    @Override
    public void serializeToStream(DataOutputStream os) throws IOException {
        os.writeByte(Constants.CONSTANT_NameAndType);
        os.writeShort(this.getNameIndex());
        os.writeShort(descriptorIndex);
    }
}
