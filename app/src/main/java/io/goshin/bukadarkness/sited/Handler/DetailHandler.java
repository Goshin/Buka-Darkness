package io.goshin.bukadarkness.sited.Handler;

import org.json.JSONObject;

import io.goshin.bukadarkness.sited.MangaSource;

public class DetailHandler implements RequestHandler {
    @Override
    public void process(MangaSource mangaSource, MangaSource.Callback sendResponseCallback, JSONObject params) {
        if (mangaSource == null) {
            sendResponseCallback.run("");
            return;
        }
        mangaSource.getBookDetail(params.optString("url"), sendResponseCallback);
    }
}
