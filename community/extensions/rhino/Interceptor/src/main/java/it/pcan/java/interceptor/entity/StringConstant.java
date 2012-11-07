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
public class StringConstant extends AbstractConstant {

    private final int stringIndex;

    public StringConstant(int stringIndex) {
        this.stringIndex = stringIndex;
    }

    public int getStringIndex() {
        return stringIndex;
    }


    public void serializeToStream(DataOutputStream os) throws IOException {
        os.writeByte(Constants.CONSTANT_String);
        os.writeShort(stringIndex);
    }
    
}
