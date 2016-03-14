package io.goshin.bukadarkness.adapter;

import org.json.JSONObject;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.MangaMapDatabase;

public class Rate extends MangaAdapter {
    @Override
    protected Boolean needRedirect(JSONObject params) throws Throwable {
        return !MangaMapDatabase.getInstance().getUrl(params.optString("mid")).equals("");
    }

    @Override
    protected String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (originalResult != null) {
            return originalResult.toString();
        }

        throw new Exception("又不是布卡的漫画，你评分干嘛？");
    }
}
