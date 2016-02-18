package io.goshin.bukadarkness;

import android.app.Application;
import android.os.Handler;
import android.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.goshin.bukadarkness.sited.MangaSource;

public class SiteDBridge {
    private static Application application;
    public static SparseArray<MangaSource> sources = new SparseArray<>();

    public static void setApplication(Application application) {
        SiteDBridge.application = application;
    }

    public static void initSource(final int id, final String xmlURL, final Runnable runnable) throws Throwable {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ByteArrayOutputStream responseBytesOutputStream = new ByteArrayOutputStream();
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(xmlURL).openConnection();
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    byte[] responseReadBuffer = new byte[1024];
                    int n;
                    while ((n = inputStream.read(responseReadBuffer)) != -1) {
                        responseBytesOutputStream.write(responseReadBuffer, 0, n);
                    }
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final String xmlString = responseBytesOutputStream.toString();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sources.put(id, new MangaSource(application, xmlString));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                handler.post(runnable);
            }
        }).start();
    }
}
