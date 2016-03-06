package io.goshin.bukadarkness;

import android.content.Context;

import org.json.JSONObject;

public abstract class MangaAdapter {
    protected static Context context;

    public static void setContext(Context context) {
        MangaAdapter.context = context;
    }

    protected abstract Boolean needRedirect(JSONObject params) throws Throwable;

    protected abstract String getResult(JSONObject params, JSONObject originalResult) throws Throwable;
}
