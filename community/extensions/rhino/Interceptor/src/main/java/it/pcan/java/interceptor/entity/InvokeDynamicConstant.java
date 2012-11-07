/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor.entity;

/**
 *
 * @author Pierantonio
 */
public class InvokeDynamicConstant extends AbstractConstant {

    private final int bootstrapMethodAttrIndex;
    private final int nameAndTypeIndex;

    public InvokeDynamicConstant(int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
        this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
        this.nameAndTypeIndex = nameAndTypeIndex;
    }

    public int getBootstrapMethodAttrIndex() {
        return bootstrapMethodAttrIndex;
    }

    public int getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }
    
    

}
