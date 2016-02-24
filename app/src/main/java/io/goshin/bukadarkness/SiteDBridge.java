package io.goshin.bukadarkness;

import android.app.Application;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;

import io.goshin.bukadarkness.database.SourceDatabase;
import io.goshin.bukadarkness.sited.MangaSource;

public class SiteDBridge {
    private static Application application;
    public static ArrayList<MangaSource> sources = new ArrayList<>();

    public static void setApplication(Application application) {
        SiteDBridge.application = application;
    }

    public static void loadSources(Context context) {
        sources = new ArrayList<>();
        String[] fileList = context.fileList();
        for (String filename : fileList) {
            try {
                FileInputStream fileInputStream = context.openFileInput(filename);
                byte[] buffer = new byte[fileInputStream.available()];
                fileInputStream.read(buffer);
                String xml = new String(buffer);
                fileInputStream.close();

                if (new SourceDatabase(context).isEnabled(filename)) {
                    sources.add(0, new MangaSource(application, xml));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getGroupJson() {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < sources.size(); i++) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("gname", sources.get(i).title);
                jsonObject.put("gid", String.valueOf(i));
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }
}
