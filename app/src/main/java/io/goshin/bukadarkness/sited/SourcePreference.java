package io.goshin.bukadarkness.sited;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SourcePreference {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String REMOTE_PACKAGE_NAME = "io.goshin.bukadarkness";
    SharedPreferences sharedPreferences;
    private boolean readable = true;

    @SuppressWarnings("deprecation")
    @SuppressLint("WorldReadableFiles")
    public SourcePreference(Context context) {
        if (!context.getPackageName().equals(REMOTE_PACKAGE_NAME)) {
            try {
                context = context.createPackageContext(REMOTE_PACKAGE_NAME, Context.CONTEXT_RESTRICTED);
                @SuppressLint("SdCardPath") File sharedPrefFile = new File("/data/data/" + REMOTE_PACKAGE_NAME + "/shared_prefs/sources.xml");
                readable = sharedPrefFile.exists() && sharedPrefFile.canRead();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        sharedPreferences = context.getSharedPreferences("sources", Context.MODE_WORLD_READABLE);
        if (!sharedPreferences.contains("first_time")) {
            sharedPreferences.edit().putBoolean("first_time", false).apply();
        }
    }

    public boolean isReadable() {
        return readable;
    }

    public Set<String> getSourceList() {
        return sharedPreferences.getStringSet("source_list", new HashSet<String>());
    }

    public boolean isEnabled(String name) {
        return sharedPreferences.getBoolean(name + "_enabled", true);
    }

    public boolean isSearchEnabled(String name) {
        return sharedPreferences.getBoolean(name + "_search_enabled", true);
    }

    public void setEnable(String name, boolean enabled) {
        sharedPreferences.edit().putBoolean(name + "_enabled", enabled).apply();
    }

    public void setSearchEnabled(String name, boolean enabled) {
        sharedPreferences.edit().putBoolean(name + "_search_enabled", enabled).apply();
    }

    public void addSource(String name) {
        Set<String> set = getSourceList();
        set.add(name);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("source_list")
                .putBoolean(name + "_enabled", true)
                .putBoolean(name + "_search_enabled", true)
                .putStringSet("source_list", set)
                .apply();
    }

    public void deleteSource(String name) {
        Set<String> set = getSourceList();
        set.remove(name);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("source_list")
                .remove(name + "_search_enabled")
                .remove(name + "_enabled")
                .putStringSet("source_list", set)
                .apply();
    }
}
