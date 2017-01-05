package io.goshin.bukadarkness.sited.Handler;

import org.json.JSONObject;

import io.goshin.bukadarkness.sited.MangaSource;

public class HandlerFactory {
    @SuppressWarnings("SpellCheckingInspection")
    public static RequestHandler getHandler(JSONObject params) {
        switch (params.optString("f").toLowerCase()) {
            case "func_getmangagroups":
            case "func_getcategory":
                return new GroupHandler();
            case "func_getgroupitems":
                return new GroupItemHandler();
            case "func_search":
                return new SearchHandler();
            case "func_getdetail":
                return new DetailHandler();
            case "get_index":
                return new SectionHandler();
        }
        return new RequestHandler() {
            @Override
            public void process(MangaSource mangaSource, MangaSource.Callback sendResponseCallback, JSONObject params) {
                sendResponseCallback.run("");
            }
        };
    }
}
