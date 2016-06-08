package io.goshin.bukadarkness.adapter;

import io.goshin.bukadarkness.MangaAdapter;

public class MangaAdapterFactory {
    @SuppressWarnings("SpellCheckingInspection")
    public static MangaAdapter getAdapter(String f) {
        String action = f.substring(f.indexOf("_") + 1).toLowerCase();
        MangaAdapter adapter = null;
        switch (action) {
            case "getmangagroups":
            case "getcategory":
                adapter = new Groups();
                break;
            case "getgroupitems":
                adapter = new Items();
                break;
            case "search":
                adapter = new Items();
                break;
            case "getdetail":
                adapter = new Detail();
                break;
            case "userdiscussm":
                adapter = new Comments();
                break;
            case "contributioninfo":
                adapter = new Contributions();
                break;
            case "mangarate":
                adapter = new Rate();
                break;
            case "getsimpleinfo":
                adapter = new SimpleInfo();
                break;
        }
        return adapter;
    }
}
