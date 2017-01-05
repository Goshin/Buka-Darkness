package io.goshin.bukadarkness.sited.Handler;

import org.json.JSONObject;

import io.goshin.bukadarkness.sited.MangaSource;

public class SectionHandler implements RequestHandler {
    @Override
    public void process(MangaSource mangaSource, MangaSource.Callback sendResponseCallback, JSONObject params) {
        if (mangaSource == null) {
            sendResponseCallback.run("");
            return;
        }
        mangaSource.getSection(params.optString("url"), sendResponseCallback);
    }
}
