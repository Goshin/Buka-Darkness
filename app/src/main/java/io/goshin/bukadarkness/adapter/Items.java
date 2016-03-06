package io.goshin.bukadarkness.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.KVMapDatabase;
import io.goshin.bukadarkness.database.MangaMapDatabase;
import io.goshin.bukadarkness.sited.Client;

public class Items extends MangaAdapter {
    public static final String COVER_PREFIX = "http://buka-darkness-cover-";
    /* <cid, url> */
    public static KVMapDatabase coverMap;
    public static MangaMapDatabase mangaMapDatabase;

    public static void initDatabase() {
        mangaMapDatabase = new MangaMapDatabase(context);
        coverMap = new KVMapDatabase(context, "cover");
    }

    @Override
    public Boolean needRedirect(JSONObject params) throws Throwable {
        return !params.optString("f").equals("func_search")
                && !Groups.groupMapDatabase.getFilename(Long.parseLong(params.optString("fp"))).equals("");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (originalResult != null && params.optString("f").equals("func_getgroupitems")) {
            return originalResult.toString();
        }

        JSONObject result = originalResult != null ? originalResult : new JSONObject("{\n" +
                "    \"ret\": 0,\n" +
                "    \"recom\": 0,\n" +
                "    \"hasnext\": 0,\n" +
                "    \"items\": [],\n" +
                "    \"sort\": [\n" +
                "        {\n" +
                "            \"val\": 1,\n" +
                "            \"name\": \"综合\",\n" +
                "            \"sel\": 1\n" +
                "        }\n" +
                "    ],\n" +
                "    \"remembersort\": 1\n" +
                "}");

        if (params.optString("f").equals("func_getgroupitems")) {
            params.put("fp", Groups.groupMapDatabase.getFilename(Long.parseLong(params.getString("fp"))));
        }

        JSONArray list = new JSONArray(Client.request(params));
        if (list.length() > 0 && result.optInt("recom") == 1) {
            result.put("recom", 0);
            result.put("items", new JSONArray());
        }
        synchronized (this) {
            coverMap.putPrepare();
            Index.imageReferrerMap.putPrepare();
            for (int i = 0; i < list.length(); i++) {
                JSONObject book = new JSONObject("        {\n" +
                        "            \"mid\": \"212204\",\n" +
                        "            \"name\": \"M站动漫资讯\",\n" +
                        "            \"author\": \"Unknown\",\n" +
                        "            \"logos\": \"http://c-r5.sosobook.cn/logo/logo5/212204/201501041820/43m.jpg\",\n" +
                        "            \"logodir\": \"http://c-r5.sosobook.cn/logo/logo5/212204/201501041820\",\n" +
                        "            \"logo\": \"http://c-r5.sosobook.cn/logo/logo5/212204/201501041820/43m.jpg\",\n" +
                        "            \"lastchap\": \"0\",\n" +
                        "            \"finish\": \"0\",\n" +
                        "            \"rate\": \"99\",\n" +
                        "            \"type\": \"0\",\n" +
                        "            \"lastup\": \"xx话\"\n" +
                        "        }");
                JSONObject data = list.getJSONObject(i);
                book.put("name", data.optString("name"));

                String mangaID = String.valueOf(mangaMapDatabase.getID(data.optString("sourceID"), data.optString("url")));
                book.put("mid", mangaID);

                String logo = Utils.getEncodedUrl(data.optString("logo"));
                String logoHash = mangaID + Math.abs(logo.hashCode());
                String fakeCoverDir = COVER_PREFIX + logoHash + "/";
                coverMap.put(logoHash, logo);
                Index.imageReferrerMap.put(logo, data.optString("url"));
                book.put("logo", fakeCoverDir + "1.jpg");
                book.put("logos", fakeCoverDir + "1.jpg");
                book.put("logodir", fakeCoverDir);

                book.put("author", data.optString("sourceName"));

                result.getJSONArray("items").put(book);
            }
            coverMap.commit();
            Index.imageReferrerMap.commit();
        }

        return result.toString();
    }
}
