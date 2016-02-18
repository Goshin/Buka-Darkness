package io.goshin.bukadarkness.adapter;

import org.json.JSONObject;

import io.goshin.bukadarkness.MangaAdapter;

public class Contributions implements MangaAdapter {
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
        String groupID = Items.mangaIDMap.get(params.optString("mid")).first;
        String sourceName = Groups.sourceMap.get(Groups.GROUP_PREFIX + groupID).second;
        return "{\n" +
                "    \"ret\": 0,\n" +
                "    \"copyrighttext\": \"版权声明\",\n" +
                "    \"copyrightinfo\": \"http://soft.bukamanhua.com:8000/copyright.php\",\n" +
                "    \"contributiontext\": \"投稿须知\",\n" +
                "    \"contributioninfo\": \"http://soft.bukamanhua.com:8000/contribution.php\",\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"cid\": \"65542\",\n" +
                "            \"cname\": \"内容来自 " + sourceName + "\",\n" +
                "            \"pubtime\": \"2016-01-21\",\n" +
                "            \"userid\": \"6751367\",\n" +
                "            \"username\": \"由 Buka Darkness 和 多多猫插件引擎 提供数据转换和适配\",\n" +
                "            \"head\": \"http://i11.tietuku.com/3847fcf0b726bad8.png\",\n" +
                "            \"gender\": \"0\",\n" +
                "            \"v\": \"\",\n" +
                "            \"text\": \"内容来自 " + sourceName + "\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }
}
