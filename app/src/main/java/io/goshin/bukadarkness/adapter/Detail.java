package io.goshin.bukadarkness.adapter;


import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.sited.Client;

public class Detail implements MangaAdapter {
    /* <cid, <sourceID, url>> */
    public static HashMap<String, Pair<String, String>> ClipIDMap = new HashMap<>();

    @Override
    public Boolean needRedirect(JSONObject params) throws Throwable {
        return params.optString("mid").startsWith(Items.MANGA_PREFIX);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (originalResult != null) {
            return originalResult.toString();
        }

        Pair<String, String> pair = Items.mangaIDMap.get(params.optString("mid"));
        String sourceID = pair.first;
        String url = pair.second;
        if (url == null) {
            return "{ret:-1}";
        }
        params.put("fp", sourceID);
        params.put("url", url);

        JSONObject result = new JSONObject("{\n" +
                "    \"ret\": 0,\n" +
                "    \"logo\": \"http://c-r5.sosobook.cn/logo/logo5/217176/201601211756/43m.jpg\",\n" +
                "    \"logos\": \"http://c-r5.sosobook.cn/logo/logo5/217176/201601211756/43m.jpg\",\n" +
                "    \"logodir\": \"http://c-r5.sosobook.cn/logo/logo5/217176/201601211756\",\n" +
                "    \"name\": \"绅士壹周刊\",\n" +
                "    \"author\": \"17173动漫频道\",\n" +
                "    \"rate\": \"97\",\n" +
                "    \"intro\": \"还有一大堆绅士盘点等着你哟！\",\n" +
                "    \"lastup\": \" \",\n" +
                "    \"lastupcid\": \"-1\",\n" +
                "    \"lastuptime\": \"2016-01-21\",\n" +
                "    \"lastuptimeex\": \"2016-01-21 18:01:15\",\n" +
                "    \"resupno\": \"1453370539\",\n" +
                "    \"upno\": \"2211\",\n" +
                "    \"readmode3\": \"0\",\n" +
                "    \"readmode2\": 0,\n" +
                "    \"readmode\": 50331648,\n" +
                "    \"popular\": 0,\n" +
                "    \"populars\": \"D+\",\n" +
                "    \"favor\": 0,\n" +
                "    \"finish\": \"0\",\n" +
                "    \"discount\": \"1\",\n" +
                "    \"fav_exceed\": \"超过 1%\",\n" +
                "    \"read_exceed\": \"超过 17%\",\n" +
                "    \"shareurl\": \"http://m.sosobook.cn/manga.php?page=1\",\n" +
                "    \"detail_shareurl\": \"http://m.buka.cn/detail.php?mid=217176&src=android\",\n" +
                "    \"recomctrltype\": 0,\n" +
                "    \"recomctrlparam\": \"\",\n" +
                "    \"recomwords\": \"\",\n" +
                "    \"recomenter\": \"\",\n" +
                "    \"recomdelay\": 0,\n" +
                "    \"urls\": [\n" +
                "        {\n" +
                "            \"name\": \"投稿须知\",\n" +
                "            \"url\": \"http://soft.bukamanhua.com:8000/contribution.php\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"内容举报\",\n" +
                "            \"url\": \"http://soft.bukamanhua.com:8000/report.php\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"footer\": [\n" +
                "        {\n" +
                "            \"text\": \"投稿须知\",\n" +
                "            \"ctrltype\": 11,\n" +
                "            \"ctrlparam\": \"http://soft.bukamanhua.com:8000/contribution.php,投稿须知\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"text\": \"内容举报\",\n" +
                "            \"ctrltype\": 11,\n" +
                "            \"ctrlparam\": \"http://soft.bukamanhua.com:8000/report.php,内容举报\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"links\": [],\n" +
                "    \"res\": []\n" +
                "}");

        JSONObject response = new JSONObject(Client.request(params));
        String logo = response.optString("logo");
        String logoHash = params.optString("mid") + Math.abs(logo.hashCode());
        String fakeCoverDir = Items.COVER_PREFIX + logoHash + "/";
        Items.coverMap.put(logoHash, logo);
        result.put("logo", fakeCoverDir + "1.jpg");
        result.put("logos", fakeCoverDir + "1.jpg");
        result.put("logodir", fakeCoverDir);

        result.put("name", response.optString("name"));
        result.put("author", response.optString("author"));
        result.put("intro", response.optString("intro"));

        String updateTime = response.optString("updateTime");
        result.put("lastuptime", updateTime);
        result.put("lastuptimeex", updateTime + " 00:00:00");

        JSONArray sections = response.getJSONArray("sections");
        int length = sections.length();
        for (int i = 0; i < length; i++) {
            JSONObject section = sections.getJSONObject(i);
            JSONObject link = new JSONObject("{\n" +
                    "    \"cid\": \"65542\",\n" +
                    "    \"idx\": \"6\",\n" +
                    "    \"type\": \"0\",\n" +
                    "    \"title\": \"第六期\",\n" +
                    "    \"size\": \"8888\",\n" +
                    "    \"ressupport\": \"7\"\n" +
                    "}");
            JSONObject res = new JSONObject("{\n" +
                    "    \"cid\": \"65537\",\n" +
                    "    \"restype\": \"1\",\n" +
                    "    \"csize\": \"8888\"\n" +
                    "}");
            link.put("idx", String.valueOf(length - i));
            link.put("title", section.optString("name"));

            String clipID = String.valueOf(65536 + length - i);
            ClipIDMap.put(params.optString("mid") + clipID, new Pair<>(params.optString("fp"), section.optString("url")));
            link.put("cid", clipID);
            res.put("cid", clipID);

            result.put("lastup", section.optString("name"));
            result.put("lastupcid", clipID);
            result.getJSONArray("links").put(link);
            result.getJSONArray("res").put(res);
        }

        return result.toString();
    }
}
