package io.goshin.bukadarkness.adapter;

import org.json.JSONObject;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.MangaMapDatabase;

public class Comments extends MangaAdapter {
    @Override
    public Boolean needRedirect(JSONObject params) throws Throwable {
        return !MangaMapDatabase.getInstance().getUrl(params.optString("mid")).equals("");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (originalResult != null) {
            return originalResult.toString();
        }

        return "{\n" +
                "    \"ret\": 0,\n" +
                "    \"postrestrict\": 1,\n" +
                "    \"locked\": 1,\n" +
                "    \"lockedmsg\": \"本漫画已关闭评论\"," +
                "    \"ctt\": [\n" +
                "        {\n" +
                "            \"disid\": \"99999999\",\n" +
                "            \"userid\": \"9999999\",\n" +
                "            \"t\": \"2016-02-03 15:51:34\",\n" +
                "            \"tlast\": \"2016-02-03 15:51:34\",\n" +
                "            \"s\": \"0\",\n" +
                "            \"text\": \"评论功能不考虑予以实现\",\n" +
                "            \"mid\": \"" + params.optString("mid") + "\",\n" +
                "            \"rc\": \"0\",\n" +
                "            \"unread\": \"0\",\n" +
                "            \"ver\": \"0\",\n" +
                "            \"top\": \"0\",\n" +
                "            \"ustatus\": \"0\",\n" +
                "            \"name\": \"Buka Darkness\",\n" +
                "            \"head\": \"http://i.imgur.com/iMjdpW4.png\",\n" +
                "            \"gender\": \"1\",\n" +
                "            \"v\": \"\",\n" +
                "            \"tdiff\": 1869,\n" +
                "            \"timeintext\": \"0分钟前\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"hasnext\": 0\n" +
                "}";
    }
}
