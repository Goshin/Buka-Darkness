package io.goshin.bukadarkness.sited;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import org.json.JSONObject;
import org.noear.sited.SdApi;
import org.noear.sited.SdLogListener;
import org.noear.sited.SdNodeFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import io.goshin.bukadarkness.Hook;
import io.goshin.bukadarkness.SiteDBridge;
import io.goshin.bukadarkness.sited.Handler.HandlerFactory;
import io.goshin.bukadarkness.sited.Handler.RequestHandler;

public class Server extends HandlerThread {
    private static ProcessHandler processHandler;
    private static PrintWriter logWriter = null;
    private static Server instance;
    private Application application;

    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("SpellCheckingInspection")
    private Server(Application app) {
        super("DarknessHandlerThread");
        application = app;

        try {
            Context remoteContext = application.createPackageContext("io.goshin.bukadarkness", Context.CONTEXT_RESTRICTED);
            //noinspection deprecation
            if (remoteContext.getSharedPreferences("pref", Context.MODE_WORLD_READABLE).getBoolean("verbose", false)) {
                try {
                    logWriter = new PrintWriter(new FileWriter(application.getFileStreamPath("error.log"), false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        SdApi.tryInit(new SdNodeFactory(), new SdLogListener() {
        }, application.getExternalCacheDir());
        SiteDBridge.setApplication(application);
    }

    private static synchronized void log(String text) {
        if (logWriter != null) {
            logWriter.println(text);
            logWriter.flush();
        }
    }

    private static void log(Throwable t) {
        if (logWriter != null) {
            StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            t.printStackTrace();
            log(stringWriter.toString());
        }
    }


    public static void init(final Application application) {
        if (instance != null) {
            return;
        }
        instance = new Server(application);
        instance.start();

        processHandler = new ProcessHandler();
        processHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    SiteDBridge.loadSources();
                } catch (IOException ignored) {
                    Hook.getInstance().alert("读取订阅源列表失败，你可能需要打开一次 Buka Darkness 完成初始化。");
                }
            }
        });
    }

    public static boolean isOnDuty() {
        return processHandler != null && instance.isAlive();
    }

    public static ProcessHandler getProcessHandler() {
        return processHandler;
    }

    private static class ProcessHandler extends Handler {
        public ProcessHandler() {
            super(instance.getLooper());
        }

        @Override
        public void handleMessage(final Message msg) {
            final Packet packet = (Packet) msg.obj;
            MangaSource.Callback sendResponseCallback = new MangaSource.Callback() {
                @Override
                public void run(Object... objects) {
                    if (packet.getStatus() == Packet.Status.COMPLITED) {
                        return;
                    }
                    String result = (String) objects[0];
                    if (result == null) {
                        result = "";
                    }
                    synchronized (packet) {
                        packet.setResponse(result);
                        packet.notify();
                    }
                }
            };

            try {
                JSONObject params = packet.getRequest();
                MangaSource mangaSource = SiteDBridge.sources.get(params.optString("fp"));

                RequestHandler requestHandler = HandlerFactory.getHandler(params);
                requestHandler.process(mangaSource, sendResponseCallback, params);
            } catch (Throwable e) {
                log(e);
                sendResponseCallback.run("");
            }

        }
    }

}
