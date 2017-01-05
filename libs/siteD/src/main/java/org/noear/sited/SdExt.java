package org.noear.sited;

/**
 * Created by yuety on 2016/11/1.
 */

public class SdExt {
    private SdSource source;

    public SdExt(SdSource s) {
        this.source = s;
    }

    public void set(final String key, final String val) {

        Util.set(source, key, val);
    }

    public String get(final String key) {

        return Util.get(source, key);
    }
}
