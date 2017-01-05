package io.goshin.bukadarkness.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.GroupMapDatabase;
import io.goshin.bukadarkness.sited.Client;

public class Groups extends MangaAdapter {
    @Override
    public Boolean needRedirect(JSONObject params) throws Throwable {
        return false;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        JSONArray list = new JSONArray(Client.request(params));
        for (int i = 0; i < list.length(); i++) {
            JSONObject data = list.getJSONObject(i);
            JSONObject group = new JSONObject("{\n" +
                    "    \"gid\": \"67\",\n" +
                    "    \"gname\": \"ACG资讯\",\n" +
                    "    \"func\": \"1\",\n" +
                    "    \"param\": \"12011\",\n" +
                    "    \"logo\": \"http://i.imgur.com/l5e6bEo.jpg\",\n" +
                    "    \"logo2\": \"http://i.imgur.com/23L5FtE.png\",\n" +
                    "    \"cx\": \"0\",\n" +
                    "    \"cy\": \"0\",\n" +
                    "    \"locked\": \"0\",\n" +
                    "    \"supportsort\": \"0\"\n" +
                    "}");

            String name = data.optString("gname");
            String gid = String.valueOf(GroupMapDatabase.getInstance().getID(data.optString("gid"), name));

            group.put("gid", gid);
            group.put("gname", name);
            group.put("param", gid);
            String coverUrl = "http://www.baidu.com/?buka=group_cover&name=" + URLEncoder.encode(name, "UTF-8");
            group.put("logo", coverUrl);
            group.put("logo2", coverUrl);
            if (originalResult.getJSONArray("groups").getJSONObject(0).isNull("type")) {
                originalResult.getJSONArray("groups").put(group);
            } else {
                originalResult.getJSONArray("groups").getJSONObject(0).getJSONArray("items").put(group);
            }
        }

        return originalResult.toString();
    }
}
