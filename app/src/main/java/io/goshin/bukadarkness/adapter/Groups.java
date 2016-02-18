package io.goshin.bukadarkness.adapter;

import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.sited.Client;

public class Groups implements MangaAdapter {
    public static final String GROUP_PREFIX = "987";
    /* <gid, <sourceID, name>> */
    public static HashMap<String, Pair<String, String>> sourceMap = new HashMap<>();

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

            String gid = GROUP_PREFIX + data.optString("gid");
            String name = data.optString("gname");
            sourceMap.put(gid, new Pair<>(data.optString("gid"), name));

            group.put("gid", gid);
            group.put("gname", name);
            group.put("param", gid);
            originalResult.getJSONArray("groups").put(group);
        }

        return originalResult.toString();
    }
}
