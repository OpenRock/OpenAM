package org.forgerock.openam.scripting.common.javascript;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrapFactory;

import java.util.Map;

public class CommonWrapFactory extends WrapFactory {
    @Override
    public Object wrap(Context cx, Scriptable scope,
                       Object obj, Class<?> staticType) {
        if (Map.class.isAssignableFrom(obj.getClass())) {
            return new NativeJavaMap(scope, (Map) obj);
        }
        return super.wrap(cx, scope, obj, staticType);
    }
}
