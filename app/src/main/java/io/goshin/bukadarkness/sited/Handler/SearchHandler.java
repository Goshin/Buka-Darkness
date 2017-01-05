package io.goshin.bukadarkness.sited.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.goshin.bukadarkness.SiteDBridge;
import io.goshin.bukadarkness.sited.MangaSource;

public class SearchHandler implements RequestHandler {
    @Override
    public void process(MangaSource mangaSource, final MangaSource.Callback sendResponseCallback, JSONObject params) {
        MangaSource.Callback searchCallback = new MangaSource.Callback() {
            private int sourceCount = SiteDBridge.searchSources.size();
            private JSONArray searchResult = new JSONArray();

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
                        searchResult.put(book);
                    }
                } catch (JSONException ignored) {
                }
                if (sourceCount-- == 1) {
                    sendResponseCallback.run(searchResult.toString());
                }
            }
        };
        if (SiteDBridge.searchSources.size() == 0) {
            sendResponseCallback.run("[]");
        } else {
            for (MangaSource searchMangaSource : SiteDBridge.searchSources.values()) {
                try {
                    searchMangaSource.search(params.optString("text"), searchCallback);
                } catch (Exception ignored) {
                    searchCallback.run("", null);
                }
            }
        }
    }
}
