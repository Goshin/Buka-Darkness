package io.goshin.bukadarkness.sited;

import android.app.Application;

import org.json.JSONArray;
import org.json.JSONObject;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;
import org.noear.sited.SdSourceCallback;

public class MangaSource extends SdSource {

    private SdNodeSet home;
    private SdNodeSet main;

    private String intro;
    private String author;
    private int type;

    public MangaSource(Application app, String xml) throws Exception {
        super(app, xml);
        main = getBody("main");
        home = (SdNodeSet) main.get("home");

        author = attrs.getString("author");
        intro = attrs.getString("intro");
        type = main.attrs.getInt("dtype");
    }

    public int getType() {
        return type;
    }

    public String getAuthor() {
        return author;
    }

    public String getIntro() {
        return intro;
    }

    public void getHots(final Callback callback) {
        SdNode hots = (SdNode) home.get("hots");
        final DataModel hotsViewModel = new DataModel();
        getNodeViewModel(hotsViewModel, true, 1, hots, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(hotsViewModel.getJsonData(), MangaSource.this);
            }
        });
    }

    public void search(String keyword, final Callback callback) {
        SdNode search = (SdNode) main.get("search");
        final DataModel searchViewModel = new DataModel();
        getNodeViewModel(searchViewModel, true, keyword, 1, search, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(searchViewModel.getJsonData(), MangaSource.this);
            }
        });
    }

    public void getBookDetail(String url, final Callback callback) {
        SdNode book = (SdNode) main.get("book");
        final DataModel dataModel = new DataModel();
        getNodeViewModel(dataModel, true, url, book, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(dataModel.getJsonData());
            }
        });
    }

    public void getSection(String url, final Callback callback) {
        SdNode section = (SdNode) main.get("section");
        final DataModel dataModel = new DataModel();
        getNodeViewModel(dataModel, true, url, section, new SdSourceCallback() {
            @Override
            public void run(Integer code) {
                callback.run(dataModel.getJsonData());
            }
        });
    }

    public interface Callback {
        void run(Object... objects);
    }

    private class DataModel implements ISdViewModel {
        private String jsonData;

        @Override
        public void loadByConfig(SdNode config) {
        }

        @Override
        public void loadByJson(SdNode config, String... json) {
            String newJson;
            if (json.length == 0 || json[0] == null) {
                newJson = "";
            } else {
                newJson = json[0];
            }

            if (jsonData == null || jsonData.isEmpty()) {
                jsonData = newJson;
                return;
            }

            if (jsonData.trim().startsWith("{") && newJson.trim().startsWith("{")) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONObject newJsonObject = new JSONObject(newJson);
                    JSONArray names = newJsonObject.names();
                    for (int i = 0; i < names.length(); i++) {
                        String key = names.getString(i);
                        Object value = newJsonObject.get(key);

                        if (!jsonObject.isNull(key)) {
                            if (value instanceof JSONArray && ((JSONArray) value).length() == 0) {
                                continue;
                            } else if (value instanceof JSONObject && ((JSONObject) value).length() == 0) {
                                continue;
                            } else if (value instanceof String && ((String) value).length() == 0) {
                                continue;
                            }
                        }

                        jsonObject.put(key, value);
                    }
                    jsonData = jsonObject.toString();
                } catch (Exception ignored) {
                }
            }
        }

        public String getJsonData() {
            return jsonData;
        }
    }
}
