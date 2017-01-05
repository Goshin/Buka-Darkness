package io.goshin.bukadarkness.sited.Handler;

import org.json.JSONObject;

import io.goshin.bukadarkness.SiteDBridge;
import io.goshin.bukadarkness.sited.MangaSource;

public class GroupHandler implements RequestHandler {
    @Override
    public void process(MangaSource mangaSource, MangaSource.Callback sendResponseCallback, JSONObject params) {
        sendResponseCallback.run(SiteDBridge.getGroupJson());
    }
}
