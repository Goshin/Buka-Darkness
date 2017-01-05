package org.noear.sited;

import java.io.File;

/**
 * Created by yuety on 15/12/19.
 */
public class SdApi {

    protected static SdNodeFactory _factory;
    protected static SdLogListener _listener;
    protected static File _cacheRoot;

    public static void tryInit(SdNodeFactory factory, SdLogListener listener, File cacheRoot) {
        _factory = factory;
        _listener = listener;
        _cacheRoot = cacheRoot;
    }

    protected static void check() throws Exception {
        if (_factory == null || _listener == null) {
            throw new Exception("未初始化");
        }
    }

}
