package io.goshin.bukadarkness.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.CoverMapDatabase;
import io.goshin.bukadarkness.database.GroupMapDatabase;
import io.goshin.bukadarkness.database.ImageReferrerMapDatabase;
import io.goshin.bukadarkness.database.MangaMapDatabase;
import io.goshin.bukadarkness.sited.Client;

public class Items extends MangaAdapter {
    public static final String COVER_PREFIX = "http://buka-darkness-cover-";
    private static final String MANGA_INFO_STRING = "{\n" +
            "    \"mid\": \"212204\",\n" +
            "    \"name\": \"M站动漫资讯\",\n" +
            "    \"author\": \"Unknown\",\n" +
            "    \"logos\": \"http://c-r5.sosobook.cn/logo/logo5/212204/201501041820/43m.jpg\",\n" +
            "    \"logodir\": \"http://c-r5.sosobook.cn/logo/logo5/212204/201501041820\",\n" +
            "    \"logo\": \"http://c-r5.sosobook.cn/logo/logo5/212204/201501041820/43m.jpg\",\n" +
            "    \"lastchap\": \"0\",\n" +
            "    \"finish\": \"0\",\n" +
            "    \"rate\": \"99\",\n" +
            "    \"type\": \"0\",\n" +
            "    \"lastup\": \"xx话\"\n" +
            "}";
    private static final String ITEM_LIST_STRING = "{\n" +
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
            "}";

    @Override
    public Boolean needRedirect(JSONObject params) throws Throwable {
        return !params.optString("f").equals("func_search")
                && !GroupMapDatabase.getInstance().getFilename(Long.parseLong(params.optString("fp"))).equals("");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (originalResult != null && params.optString("f").equals("func_getgroupitems")) {
            return originalResult.toString();
        }

        JSONObject result = originalResult != null ? originalResult : new JSONObject(ITEM_LIST_STRING);

        if (params.optString("f").equals("func_getgroupitems")) {
            params.put("fp", GroupMapDatabase.getInstance().getFilename(Long.parseLong(params.getString("fp"))));
        }

        JSONArray list = new JSONArray(Client.request(params));
        if (list.length() > 0 && result.optInt("recom") == 1) {
            result.put("recom", 0);
            result.put("items", new JSONArray());
        }
        if (params.optString("f").equals("func_search")) {
            list = sortSearchItems(list, params.optString("text"));
        }
        synchronized (CoverMapDatabase.getInstance()) {
            synchronized (ImageReferrerMapDatabase.getInstance()) {
                CoverMapDatabase coverMap = CoverMapDatabase.getInstance();
                ImageReferrerMapDatabase imageReferrerMap = ImageReferrerMapDatabase.getInstance();
                coverMap.putPrepare();
                imageReferrerMap.putPrepare();
                for (int i = 0; i < list.length(); i++) {
                    JSONObject book = new JSONObject(MANGA_INFO_STRING);
                    JSONObject data = list.getJSONObject(i);
                    book.put("name", data.optString("name"));

                    String mangaID = String.valueOf(MangaMapDatabase.getInstance().getID(data.optString("sourceID"), data.optString("url")));
                    book.put("mid", mangaID);

                    String logo = Utils.getEncodedUrl(data.optString("logo"));
                    String logoHash = mangaID + Math.abs(logo.hashCode());
                    String fakeCoverDir = COVER_PREFIX + logoHash + "/";
                    coverMap.put(logoHash, logo);
                    imageReferrerMap.put(logo, data.optString("url"));
                    book.put("logo", fakeCoverDir + "1.jpg");
                    book.put("logos", fakeCoverDir + "1.jpg");
                    book.put("logodir", fakeCoverDir);

                    book.put("author", data.optString("sourceName"));

                    result.getJSONArray("items").put(book);
                }
                coverMap.commit();
                imageReferrerMap.commit();
            }
        }

        return result.toString();
    }

    private JSONArray sortSearchItems(JSONArray list, final String keyword) throws Exception {
        List<JSONObject> values = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            values.add(list.getJSONObject(i));
        }

        Collections.sort(values, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                double leftSimilarity = Utils.similarity(lhs.optString("name"), keyword);
                double rightSimilarity = Utils.similarity(rhs.optString("name"), keyword);
                return (int) ((rightSimilarity - leftSimilarity) * 100);
            }
        });

        JSONArray sorted = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            sorted.put(values.get(i));
        }
        return sorted;
    }
}
