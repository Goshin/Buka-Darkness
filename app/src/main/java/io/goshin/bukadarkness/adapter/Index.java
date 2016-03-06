package io.goshin.bukadarkness.adapter;


import android.content.Context;
import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import io.goshin.bukadarkness.database.KVMapDatabase;
import io.goshin.bukadarkness.sited.Client;

public class Index {
    private String mid;
    private String cid;
    public static final String PIC_PREFIX = "986";
    public static final String BASE_PREFIX = "buka-darkness-pic";
    /* <pid, url> */
    public static KVMapDatabase pIDMap;
    /* <url, referrer> */
    public static KVMapDatabase imageReferrerMap;

    public static void initDatabase(Context context) {
        pIDMap = new KVMapDatabase(context, "pic_map");
        imageReferrerMap = new KVMapDatabase(context, "image_referrer");
    }

    public Index(String mid, String cid) {
        this.mid = mid;
        this.cid = cid;
    }

    public Boolean match() throws Throwable {
        return !Items.mangaMapDatabase.getUrl(mid).equals("");
    }

    public String getClip() throws Throwable {
        JSONObject result = new JSONObject("{\n" +
                "    \"clip\": [],\n" +
                "    \"pics\": []\n" +
                "}");

        JSONObject params = new JSONObject();
        params.put("f", "get_index");
        Pair<String, String> pair = Detail.chapterMapDatabase.get(mid, cid);
        params.put("fp", pair.first);
        params.put("url", pair.second);

        JSONArray picList = new JSONArray(Client.request(params));
        synchronized (this) {
            pIDMap.putPrepare();
            imageReferrerMap.putPrepare();
            for (int i = 0; i < picList.length(); i++) {
                String url = Utils.getEncodedUrl(picList.getString(i));
                JSONObject clip = new JSONObject("{\n" +
                        "    \"r\": 772,\n" +
                        "    \"b\": 1070,\n" +
                        "    \"pic\": 0,\n" +
                        "    \"l\": 0,\n" +
                        "    \"t\": 0\n" +
                        "}");
                clip.put("pic", i);

                String picID = PIC_PREFIX + mid + Math.abs(url.hashCode());
                pIDMap.put(picID, url);
                imageReferrerMap.put(url, Items.mangaMapDatabase.getUrl(mid));

                result.getJSONArray("pics").put(picID);
                result.getJSONArray("clip").put(clip);
            }
            pIDMap.commit();
            imageReferrerMap.commit();
        }

        return result.toString();
    }

    public String getBase() {
        //noinspection SpellCheckingInspection
        return "{\"resbk\":\"http:\\/\\/c-r2.buka-darkness-pic.cn\\/pich\",\"resbklist\":[\"http:\\/\\/c-r5.buka-darkness-pic.cn\\/pich\"],\"restype\":1}";
    }

    public static String getRealPicUrl(String fakeUrl) {
        String pid = fakeUrl.substring(fakeUrl.lastIndexOf("/") + 1);
        String result = pIDMap.get(pid);
        return result == null ? "" : result;
    }
}
