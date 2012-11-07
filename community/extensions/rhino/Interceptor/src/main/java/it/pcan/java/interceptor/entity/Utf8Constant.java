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
public class Utf8Constant extends AbstractConstant{

    private final String string;

    public Utf8Constant(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public byte[] getBytes() {
        return string.getBytes();
    }

    public void serializeToStream(DataOutputStream os) throws IOException {
        byte [] bytes = string.getBytes();
        os.writeByte(Constants.CONSTANT_Utf8);
        os.writeShort(bytes.length);
        os.write(bytes);
    }

}
