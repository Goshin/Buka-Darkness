package io.goshin.bukadarkness.adapter;


import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import io.goshin.bukadarkness.database.ChapterMapDatabase;
import io.goshin.bukadarkness.database.ImageReferrerMapDatabase;
import io.goshin.bukadarkness.database.MangaMapDatabase;
import io.goshin.bukadarkness.database.PictureIDMapDatabase;
import io.goshin.bukadarkness.sited.Client;

public class Index {
    public static final String PIC_PREFIX = "986";
    public static final String BASE_PREFIX = "buka-darkness-pic";
    private String mid;
    private String cid;

    public Index(String mid, String cid) {
        this.mid = mid;
        this.cid = cid;
    }

    public static String getRealPicUrl(String fakeUrl) {
        String pid = fakeUrl.substring(fakeUrl.lastIndexOf("/") + 1);
        String result = PictureIDMapDatabase.getInstance().get(pid);
        return result == null ? "" : result;
    }

    public Boolean match() throws Throwable {
        return !MangaMapDatabase.getInstance().getUrl(mid).equals("");
    }

    public String getClip() throws Throwable {
        JSONObject result = new JSONObject("{\n" +
                "    \"clip\": [],\n" +
                "    \"pics\": []\n" +
                "}");

        JSONObject params = new JSONObject();
        params.put("f", "get_index");
        Pair<String, String> pair = ChapterMapDatabase.getInstance().get(mid, cid);
        params.put("fp", pair.first);
        params.put("url", pair.second);

        JSONArray picList = new JSONArray(Client.request(params));
        synchronized (PictureIDMapDatabase.getInstance()) {
            synchronized (ImageReferrerMapDatabase.getInstance()) {
                PictureIDMapDatabase pIDMap = PictureIDMapDatabase.getInstance();
                ImageReferrerMapDatabase imageReferrerMap = ImageReferrerMapDatabase.getInstance();
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
                    imageReferrerMap.put(url, MangaMapDatabase.getInstance().getUrl(mid));

                    result.getJSONArray("pics").put(picID);
                    result.getJSONArray("clip").put(clip);
                }
                pIDMap.commit();
                imageReferrerMap.commit();
            }
        }

        return result.toString();
    }

    public String getBase() {
        //noinspection SpellCheckingInspection
        return "{\"resbk\":\"http:\\/\\/c-r2.buka-darkness-pic.cn\\/pich\",\"resbklist\":[\"http:\\/\\/c-r5.buka-darkness-pic.cn\\/pich\"],\"restype\":1}";
    }
}
