package io.goshin.bukadarkness.sited;

import android.app.Application;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.noear.sited.ISdViewModel;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;
import org.noear.sited.SdSourceCallback;

import java.nio.charset.Charset;

public class MangaSource extends SdSource {

    private static final int ENGINE_VERSION = 27;
    private SdNodeSet home;
    private SdNodeSet main;

    private String intro;
    private String author;
    private int type;

    public MangaSource(Application app, String xml) throws Exception {
        if (xml.startsWith("sited::")) {
            int start = xml.indexOf("::") + 2;
            int end = xml.lastIndexOf("::");
            String txt = xml.substring(start, end);
            String key = xml.substring(end + 2);
            xml = unsuan(txt, key);
        }

        doInit(app, xml);

        xmlHeadName = "meta";
        xmlBodyName = "main";
        xmlScriptName = "jscript";

        doLoad(app);

        main = body;
        home = (SdNodeSet) main.get("home");

        author = head.attrs.getString("author");
        intro = head.attrs.getString("intro");
        type = main.attrs.getInt("dtype");
    }

    public static String unsuan(String str, String key) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, len = str.length(); i < len; i++) {
            if (i % 2 == 0) {
                sb.append(str.charAt(i));
            }
        }

        str = sb.toString();
        str = new String(Base64.decode(str, Base64.NO_WRAP));
        key = key + "ro4w78Jx";

        Charset coder = Charset.forName("UTF-8");

        byte[] data = str.getBytes(coder);
        byte[] keyData = key.getBytes(coder);
        int keyIndex = 0;

        for (int x = 0; x < data.length; x++) {
            data[x] = (byte) (data[x] ^ keyData[keyIndex]);
            keyIndex += 1;

            if (keyIndex == keyData.length) {
                keyIndex = 0;
            }
        }
        str = new String(data, coder);

        return new String(Base64.decode(str, Base64.NO_WRAP));
    }

    public static String getEngineVersionName() {
        return String.valueOf(ENGINE_VERSION);
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

    public boolean isRestricted() {
        return head.attrs.getInt("vip") == 1;
    }

    public void getHots(final Callback callback) {
        SdNode hots = (SdNode) home.get("hots");
        final DataModel hotsViewModel = new DataModel();
        getNodeViewModel(hotsViewModel, true, null, 1, hots, new SdSourceCallback() {
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
