package io.goshin.bukadarkness;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.goshin.bukadarkness.sited.MangaSource;
import io.goshin.bukadarkness.sited.SourcePreference;

public class SiteDBridge {
    public static HashMap<String, MangaSource> sources = new HashMap<>();
    public static HashMap<String, MangaSource> searchSources = new HashMap<>();
    private static Application application;

    public static void setApplication(Application application) {
        SiteDBridge.application = application;
    }

    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    public static void loadSources() throws IOException {
        Context context;
        try {
            context = application.createPackageContext(SiteDBridge.class.getPackage().getName(), Context.CONTEXT_RESTRICTED);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        sources = new HashMap<>();
        SourcePreference sourcePreference = new SourcePreference(application);
        if (!sourcePreference.isReadable()) {
            throw new IOException("Shared Preference not readable");
        }
        for (String filename : sourcePreference.getSourceList()) {
            try {
                if (filename.length() != 32) {
                    continue;
                }
                FileInputStream fileInputStream = context.openFileInput(filename);
                byte[] buffer = new byte[fileInputStream.available()];
                if (fileInputStream.read(buffer) == -1) {
                    continue;
                }
                String xml = new String(buffer);
                fileInputStream.close();

                if (sourcePreference.isEnabled(filename)) {
                    MangaSource mangaSource = new MangaSource(application, xml);
                    sources.put(filename, mangaSource);
                    if (sourcePreference.isSearchEnabled(filename)) {
                        searchSources.put(filename, mangaSource);
                    }
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
