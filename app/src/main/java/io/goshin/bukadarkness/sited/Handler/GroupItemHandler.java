package io.goshin.bukadarkness.sited.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.goshin.bukadarkness.SiteDBridge;
import io.goshin.bukadarkness.sited.MangaSource;

public class GroupItemHandler implements RequestHandler {
    @Override
    public void process(final MangaSource mangaSource, final MangaSource.Callback sendResponseCallback, JSONObject params) {
        if (mangaSource == null) {
            sendResponseCallback.run("");
            return;
        }
        mangaSource.getHots(new MangaSource.Callback() {
            @Override
            public void run(Object... objects) {
                String result = objects[0] == null ? "" : (String) objects[0];
                MangaSource currentMangaSource = (MangaSource) objects[1];
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject book = jsonArray.getJSONObject(i);
                        book.put("sourceID", SiteDBridge.filenameOfMangaSource(currentMangaSource));
                        book.put("sourceName", currentMangaSource.title);
                    }
                    sendResponseCallback.run(jsonArray.toString());
                } catch (JSONException e) {
                    sendResponseCallback.run("[]");
                }
            }
        });
    }
}
