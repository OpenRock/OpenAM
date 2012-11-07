/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor.entity;

/**
 *
 * @author Pierantonio
 */
public class MethodHandleConstant extends AbstractConstant {

    private final int referenceKind;
    private final int referenceIndex;

    public MethodHandleConstant(int referenceKind, int referenceIndex) {
        this.referenceKind = referenceKind;
        this.referenceIndex = referenceIndex;
    }

    public int getReferenceIndex() {
        return referenceIndex;
    }

    public int getReferenceKind() {
        return referenceKind;
    }

    

}
