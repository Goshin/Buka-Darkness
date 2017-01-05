package io.goshin.bukadarkness.adapter;

import org.json.JSONObject;

import java.net.URLEncoder;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.GroupMapDatabase;
import io.goshin.bukadarkness.database.MangaMapDatabase;

public class Contributions extends MangaAdapter {
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
        String groupFilename = MangaMapDatabase.getInstance().getFilename(params.optString("mid"));
        String sourceName = GroupMapDatabase.getInstance().getName(groupFilename);
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
                "            \"username\": \"" + sourceName + "\",\n" +
                "            \"head\": \"http://www.baidu.com/?buka=group_cover&name=" + URLEncoder.encode(sourceName, "UTF-8") + "\",\n" +
                "            \"gender\": \"0\",\n" +
                "            \"v\": \"\",\n" +
                "            \"text\": \"内容来自 " + sourceName + "\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
    }
}
