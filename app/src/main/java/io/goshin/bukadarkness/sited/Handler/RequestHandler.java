package io.goshin.bukadarkness.sited.Handler;

import org.json.JSONObject;

import io.goshin.bukadarkness.sited.MangaSource;

public interface RequestHandler {
    void process(MangaSource mangaSource, MangaSource.Callback sendResponseCallback, JSONObject params);
}
