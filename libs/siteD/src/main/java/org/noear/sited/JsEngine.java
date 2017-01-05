package org.noear.sited;

import android.app.Application;

import com.eclipsesource.v8.JavaVoidCallback;
import com.eclipsesource.v8.Releasable;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;

/**
 * Created by yuety on 15/8/2.
 */
class JsEngine {
    private V8 engine = null;
    private SdSource source = null;

    protected JsEngine(Application app, SdSource sd) {
        this.source = sd;
        this.engine = V8.createV8Runtime(null, app.getApplicationInfo().dataDir);

        JavaVoidCallback callback = new JavaVoidCallback() {
            public void invoke(final V8Object receiver, final V8Array parameters) {
                if (parameters.length() > 0) {
                    Object arg1 = parameters.get(0);

                    Util.log(source, "JsEngine.print", (String) arg1);

                    if (arg1 instanceof Releasable) {
                        ((Releasable) arg1).release();
                    }
                }
            }
        };

        engine.registerJavaMethod(callback, "print");

        SdExt ext = new SdExt(sd);

        V8Object v8Ext = new V8Object(this.engine);
        this.engine.add("SdExt", v8Ext);

        v8Ext.registerJavaMethod(ext, "get", "get", new Class<?>[]{String.class});
        v8Ext.registerJavaMethod(ext, "set", "set", new Class<?>[]{String.class, String.class});
        v8Ext.release();
    }


    public JsEngine loadJs(String funs) {

        try {
            engine.executeVoidScript(funs);//预加载了批函数
        } catch (Exception ex) {
            ex.printStackTrace();
            Util.log(source, "JsEngine.loadJs", ex.getMessage(), ex);
            throw ex;
        }

        return this;
    }

    //调用函数;可能传参数
    public String callJs(String fun, String... args) {
        V8Array params = new V8Array(engine);

        try {
            for (String p : args) {
                params.push(p.replaceAll("[^\\u0000-\\uFFFF]", ""));
            }

            return engine.executeStringFunction(fun, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            Util.log(source, "JsEngine.callJs:" + fun, ex.getMessage(), ex);
            return null;
        }
    }
}
