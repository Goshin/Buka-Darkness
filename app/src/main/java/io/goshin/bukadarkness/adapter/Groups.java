package io.goshin.bukadarkness.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.GroupMapDatabase;
import io.goshin.bukadarkness.sited.Client;

public class Groups extends MangaAdapter {
    public static GroupMapDatabase groupMapDatabase;

    public static void initDatabase() {
        groupMapDatabase = new GroupMapDatabase(context);
    }

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
                    "    \"logo\": \"http://i12.tietuku.com/bb7bdf152701d147.png\",\n" +
                    "    \"cx\": \"0\",\n" +
                    "    \"cy\": \"0\",\n" +
                    "    \"locked\": \"0\",\n" +
                    "    \"supportsort\": \"0\"\n" +
                    "}");

            String name = data.optString("gname");
            String gid = String.valueOf(groupMapDatabase.getID(data.optString("gid"), name));

            group.put("gid", gid);
            group.put("gname", name);
            group.put("param", gid);
            originalResult.getJSONArray("groups").put(group);
        }

        return originalResult.toString();
    }
}
