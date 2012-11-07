/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor.entity;

/**
 *
 * @author Pierantonio
 */
public class IntegerConstant extends AbstractConstant{

    private final byte[] bytes;

    public IntegerConstant(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    

}
