package io.goshin.bukadarkness.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.goshin.bukadarkness.MangaAdapter;
import io.goshin.bukadarkness.database.MangaMapDatabase;

public class SimpleInfo extends MangaAdapter {

    public static final int THREADS = 10;

    @Override
    protected Boolean needRedirect(JSONObject params) throws Throwable {
        return false;
    }

    @Override
    protected String getResult(JSONObject params, JSONObject originalResult) throws Throwable {
        if (MangaMapDatabase.getInstance() == null) {
            return "{ret:-1}";
        }

        final JSONObject result = originalResult;
        JSONArray ids = params.getJSONArray("ids");

        List<Integer> matchMangaIDs = new LinkedList<>();
        for (int i = 0; i < ids.length(); i++) {
            int mid = ids.getInt(i);
            if (MangaMapDatabase.getInstance().getUrl(String.valueOf(mid)).equals("")) {
                continue;
            }
            matchMangaIDs.add(mid);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        for (final int mid : matchMangaIDs) {
            final JSONObject requestJSON = new JSONObject("{\"f\":\"func_getdetail\"}");
            requestJSON.put("mid", mid);
            executorService.execute(new Runnable() {
                @SuppressWarnings("SpellCheckingInspection")
                @Override
                public void run() {
                    try {
                        JSONObject simpleDetail = new JSONObject(new Detail().getResult(requestJSON, null));

                        simpleDetail.put("mid", String.valueOf(mid));
                        simpleDetail.put("type", "0");
                        simpleDetail.put("lastchap", simpleDetail.getString("lastupcid"));
                        simpleDetail.put("lastuptime", simpleDetail.getString("lastuptimeex"));
                        simpleDetail.put("cindex", simpleDetail.getJSONArray("links").length());
                        simpleDetail.put("ctype", "0");
                        simpleDetail.put("cname", simpleDetail.getString("lastup"));
                        simpleDetail.put("fctxt", "");
                        simpleDetail.put("fc", "");

                        simpleDetail.remove("links");
                        simpleDetail.remove("res");

                        result.getJSONArray("items").put(simpleDetail);
                    } catch (Throwable ignored) {
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(100, TimeUnit.SECONDS);

        return result.toString();
    }
}
