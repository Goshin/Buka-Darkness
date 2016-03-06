package io.goshin.bukadarkness;

import android.app.Application;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import io.goshin.bukadarkness.database.SourceSettingsDatabase;
import io.goshin.bukadarkness.sited.MangaSource;

public class SiteDBridge {
    private static Application application;
    public static HashMap<String, MangaSource> sources = new HashMap<>();

    public static void setApplication(Application application) {
        SiteDBridge.application = application;
    }

    public static void loadSources(Context context) {
        sources = new HashMap<>();
        String[] fileList = context.fileList();
        for (String filename : fileList) {
            try {
                FileInputStream fileInputStream = context.openFileInput(filename);
                byte[] buffer = new byte[fileInputStream.available()];
                if (fileInputStream.read(buffer) == -1) {
                    continue;
                }
                String xml = new String(buffer);
                fileInputStream.close();

                if (new SourceSettingsDatabase(context).isEnabled(filename)) {
                    sources.put(filename, new MangaSource(application, xml));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getGroupJson() {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, MangaSource> entry : sources.entrySet()) {
            try {
                JSONObject jsonObject = new JSONObject();
                //noinspection SpellCheckingInspection
                jsonObject.put("gname", entry.getValue().title);
                jsonObject.put("gid", entry.getKey());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }

    public static String filenameOfMangaSource(MangaSource mangaSource) {
        for (Map.Entry<String, MangaSource> entry : sources.entrySet()) {
            if (entry.getValue() == mangaSource) {
                return entry.getKey();
            }
        }
        return null;
    }
}
