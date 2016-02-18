package io.goshin.bukadarkness.adapter;

import org.json.JSONObject;

import io.goshin.bukadarkness.MangaAdapter;

public class Comments implements MangaAdapter {
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
        return "{\n" +
                "    \"ret\": 0,\n" +
                "    \"postrestrict\": 1,\n" +
                "    \"ctt\": [\n" +
                "        {\n" +
                "            \"disid\": \"20525055\",\n" +
                "            \"userid\": \"5174424\",\n" +
                "            \"t\": \"2016-02-03 15:51:34\",\n" +
                "            \"tlast\": \"2016-02-03 15:51:34\",\n" +
                "            \"s\": \"0\",\n" +
                "            \"text\": \"评论功能可能会在下一个版本中实现\",\n" +
                "            \"mid\": \"" + params.optString("mid") + "\",\n" +
                "            \"rc\": \"0\",\n" +
                "            \"unread\": \"0\",\n" +
                "            \"ver\": \"0\",\n" +
                "            \"top\": \"0\",\n" +
                "            \"ustatus\": \"0\",\n" +
                "            \"name\": \"Buka Darkness\",\n" +
                "            \"head\": \"http://i11.tietuku.com/3847fcf0b726bad8.png\",\n" +
                "            \"gender\": \"1\",\n" +
                "            \"v\": \"\",\n" +
                "            \"tdiff\": 1869,\n" +
                "            \"timeintext\": \"31分钟前\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"hasnext\": 0\n" +
                "}";
    }
}
