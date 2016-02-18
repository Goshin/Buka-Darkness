package io.goshin.bukadarkness.adapter;


import android.support.v4.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import io.goshin.bukadarkness.sited.Client;

public class Index {
    private String mid;
    private String cid;
    public static final String PIC_PREFIX = "986";
    public static final String BASE_PREFIX = "buka-darkness-pic";
    /* <pid, url> */
    public static HashMap<String, String> pIDMap = new HashMap<>();

    public Index(String mid, String cid) {
        this.mid = mid;
        this.cid = cid;
    }

    public Boolean match() throws Throwable {
        return mid.startsWith(Items.MANGA_PREFIX);
    }

    public String getClip() throws Throwable {
        JSONObject result = new JSONObject("{\n" +
                "    \"clip\": [],\n" +
                "    \"pics\": []\n" +
                "}");

        JSONObject params = new JSONObject();
        params.put("f", "get_index");
        Pair<String, String> pair = Detail.ClipIDMap.get(mid + cid);
        params.put("fp", pair.first);
        params.put("url", pair.second);

        JSONArray picList = new JSONArray(Client.request(params));
        for (int i = 0; i < picList.length(); i++) {
            String url = picList.getString(i);
            JSONObject clip = new JSONObject("{\n" +
                    "    \"r\": 772,\n" +
                    "    \"b\": 1070,\n" +
                    "    \"pic\": 0,\n" +
                    "    \"l\": 0,\n" +
                    "    \"t\": 0\n" +
                    "}");
            clip.put("pic", i);

            /*
            HttpURLConnection imageConnection = (HttpURLConnection) new URL(url).openConnection();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageConnection.getInputStream(), null, options);
            clip.put("r", options.outWidth);
            clip.put("b", options.outHeight);
            imageConnection.disconnect();
            */

            String picID = PIC_PREFIX + mid + Math.abs(url.hashCode());
            pIDMap.put(picID, url);

            result.getJSONArray("pics").put(picID + url.substring(url.lastIndexOf(".")));
            result.getJSONArray("clip").put(clip);
        }

        return result.toString();
    }

    public String getBase() {
        //noinspection SpellCheckingInspection
        return "{\"resbk\":\"http:\\/\\/c-r2.buka-darkness-pic.cn\\/pich\",\"resbklist\":[\"http:\\/\\/c-r5.buka-darkness-pic.cn\\/pich\"],\"restype\":1}";
    }

    public static String getRealPicUrl(String fakeUrl) {
        String pid = fakeUrl.substring(fakeUrl.lastIndexOf("/") + 1, fakeUrl.lastIndexOf("."));
        String result = pIDMap.get(pid);
        return result == null ? "" : result;
    }
}
