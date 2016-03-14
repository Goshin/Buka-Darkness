package io.goshin.bukadarkness;

import org.json.JSONObject;

public abstract class MangaAdapter {

    protected abstract Boolean needRedirect(JSONObject params) throws Throwable;

    protected abstract String getResult(JSONObject params, JSONObject originalResult) throws Throwable;

}
