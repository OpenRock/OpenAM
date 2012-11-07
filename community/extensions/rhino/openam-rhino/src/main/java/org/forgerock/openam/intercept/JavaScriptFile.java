package org.forgerock.openam.intercept;

/**
 * Created with IntelliJ IDEA.
 * User: allan
 * Date: 11/2/12
 * Time: 11:10
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;

import org.forgerock.openam.script.javascript.JavaScript;


public class JavaScriptFile {
    private String name;
    private File file;
    private long modDate;
    private JavaScript jScript;

    public JavaScriptFile(File f) {
        this.file = f;
        this.name = f.getName();
        if (name.contains(".")) {
            name = name.substring(0, name.indexOf("."));
        }
        this.modDate = 0L;
    }

    private String readFully(InputStream inputStream)
            throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return new String(baos.toByteArray());
        } catch (IOException ex) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public boolean hasChanged() {
        return (file.lastModified() != modDate);
    }

    public JavaScript getContents() {
        if ((jScript == null) || (hasChanged())) {
            try {
                FileInputStream fs = new FileInputStream(file);
                String contents = readFully(fs);
                fs.close();
                this.modDate = file.lastModified();
                jScript = new JavaScript(name, contents);
            } catch (Exception ex) {
            }
        }
        return jScript;
    }
}