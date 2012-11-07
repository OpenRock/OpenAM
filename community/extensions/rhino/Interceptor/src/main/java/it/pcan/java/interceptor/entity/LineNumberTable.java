/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.pcan.java.interceptor.entity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Pierantonio
 */
public class LineNumberTable extends AbstractAttribute {

    private LineNumberEntry[] table;

    public LineNumberEntry[] getTable() {
        return table;
    }

    public void setTable(LineNumberEntry[] table) {
        this.table = table;
    }


    public static class LineNumberEntry {
        private int startPc;
        private int lineNumber;

        public int getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }

        public int getStartPc() {
            return startPc;
        }

        public void setStartPc(int startPc) {
            this.startPc = startPc;
        }

    }

    public void refresh(int injectedCodeSize) throws IOException {
        int attrLength = table.length*4;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2 + attrLength);
        DataOutputStream os = new DataOutputStream(byteArrayOutputStream);
        os.writeShort(table.length);
        for(LineNumberEntry entry : table) {
//            int startPc = (entry.getStartPc() == 0 ? 0 : entry.getStartPc() + injectedCodeSize);
            int startPc = entry.getStartPc() + injectedCodeSize;
            os.writeShort(startPc);
            os.writeShort(entry.getLineNumber());
        }
        this.setInfo(byteArrayOutputStream.toByteArray());
    }

}
