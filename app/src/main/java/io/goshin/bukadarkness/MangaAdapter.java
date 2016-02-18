package io.goshin.bukadarkness;

import org.json.JSONObject;

public interface MangaAdapter {

    Boolean needRedirect(JSONObject params) throws Throwable;

    String getResult(JSONObject params, JSONObject originalResult) throws Throwable;
}
