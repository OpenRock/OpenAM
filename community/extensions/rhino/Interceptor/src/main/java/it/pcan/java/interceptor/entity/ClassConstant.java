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
public class ClassConstant extends AbstractConstant{

    private final int nameIndex;

    public ClassConstant(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    public int getNameIndex() {
        return nameIndex;
    }


    public void serializeToStream(DataOutputStream os) throws IOException {
        os.writeByte(Constants.CONSTANT_Class);
        os.writeShort(nameIndex);
    }

    

}
