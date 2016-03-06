package io.goshin.bukadarkness.adapter;

import org.json.JSONObject;

import io.goshin.bukadarkness.MangaAdapter;

public class Rate extends MangaAdapter {
    @Override
    protected Boolean needRedirect(JSONObject params) throws Throwable {
        return !Items.mangaMapDatabase.getUrl(params.optString("mid")).equals("");
    }

    @Override
    protected String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (originalResult != null) {
            return originalResult.toString();
        }

        throw new Exception("又不是布卡的漫画，你评分干嘛？");
    }
}
