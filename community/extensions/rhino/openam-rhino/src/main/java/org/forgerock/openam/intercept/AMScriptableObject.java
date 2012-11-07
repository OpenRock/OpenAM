package org.forgerock.openam.intercept;

import org.mozilla.javascript.ScriptableObject;

import java.lang.Object;
import java.lang.String;


public class AMScriptableObject extends ScriptableObject {
    Object object;

    public  AMScriptableObject(Object object )   {
        this.object = object;
    }

    public java.lang.String getClassName() {
        return  object.getClass().getCanonicalName();
    }
}