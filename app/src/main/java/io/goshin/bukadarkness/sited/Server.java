package io.goshin.bukadarkness.sited;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.noear.sited.SdApi;
import org.noear.sited.SdLogListener;
import org.noear.sited.SdNodeFactory;
import org.noear.sited.SdSource;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;

import io.goshin.bukadarkness.SiteDBridge;
import io.goshin.bukadarkness.database.DatabaseBase;

public class Server extends Service {
    public static final int PORT = 2203;
    private static PrintWriter logWriter = null;
    private ProcessHandler processHandler;
    private ServerSocket serverSocket;

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
            log(stringWriter.toString());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @SuppressLint("WorldReadableFiles")
    @Override
    public void onCreate() {
        //noinspection deprecation
        if (getSharedPreferences("pref", MODE_WORLD_READABLE).getBoolean("verbose", false)) {
            try {
                logWriter = new PrintWriter(new FileWriter(getFileStreamPath("error.log"), false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DatabaseBase.init(this);
        processHandler = new ProcessHandler(this);
        SdApi.tryInit(new SdNodeFactory(), new SdLogListener() {
            @Override
            public void run(SdSource source, String tag, String msg, Throwable tr) {
            }
        });
        SiteDBridge.setApplication(getApplication());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(PORT);
                    while (!serverSocket.isClosed()) {
                        try {
                            final Socket clientSocket = serverSocket.accept();
                            new InputProcessThread(clientSocket).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log(e);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log(e);
                }
            }
        }).start();
        SiteDBridge.loadSources(this);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            android.os.Process.killProcess(Process.myPid());
        }
    }

    private static class ProcessHandler extends Handler {
        private final WeakReference<Server> server;

        public ProcessHandler(Server server) {
            this.server = new WeakReference<>(server);
        }

        @SuppressWarnings("SpellCheckingInspection")
        @Override
        public void handleMessage(Message msg) {
            if (server.get() == null) {
                return;
            }
            final Socket clientSocket = (Socket) msg.obj;
            Bundle bundle = msg.getData();
            try {
                JSONObject params = new JSONObject(bundle.getString("params"));
                final MangaSource.Callback sendResponseCallback = new MangaSource.Callback() {
                    @Override
                    public void run(Object... objects) {
                        String result = (String) objects[0];
                        if (result == null) {
                            result = "";
                        }
                        try {
                            Writer out = new OutputStreamWriter(clientSocket.getOutputStream(), "utf-8");
                            out.write(URLEncoder.encode(result, "utf-8") + "\n");
                            out.flush();
                            clientSocket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log(e);
                        }
                    }
                };

                final MangaSource mangaSource = SiteDBridge.sources.get(params.optString("fp"));
                switch (params.optString("f").toLowerCase()) {
                    case "func_getmangagroups":
                        sendResponseCallback.run(SiteDBridge.getGroupJson());
                        break;
                    case "func_getgroupitems":
                        if (mangaSource == null) {
                            clientSocket.close();
                            break;
                        }
                        mangaSource.getHots(new MangaSource.Callback() {
                            @Override
                            public void run(Object... objects) {
                                String result = objects[0] == null ? "" : (String) objects[0];
                                MangaSource currentMangaSource = (MangaSource) objects[1];
                                try {
                                    JSONArray jsonArray = new JSONArray(result);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject book = jsonArray.getJSONObject(i);
                                        book.put("sourceID", SiteDBridge.filenameOfMangaSource(currentMangaSource));
                                        book.put("sourceName", currentMangaSource.title);
                                    }
                                    sendResponseCallback.run(jsonArray.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    log(e);
                                    sendResponseCallback.run("[]");
                                }
                            }
                        });
                        break;
                    case "func_search":
                        MangaSource.Callback searchCallback = new MangaSource.Callback() {
                            private int sourceCount = SiteDBridge.searchSources.size();
                            private JSONArray searchResult = new JSONArray();

                            @Override
                            public void run(Object... objects) {
                                String result = objects[0] == null ? "" : (String) objects[0];
                                MangaSource currentMangaSource = (MangaSource) objects[1];
                                try {
                                    JSONArray jsonArray = new JSONArray(result);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject book = jsonArray.getJSONObject(i);
                                        book.put("sourceID", SiteDBridge.filenameOfMangaSource(currentMangaSource));
                                        book.put("sourceName", currentMangaSource.title);
                                        searchResult.put(book);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    log(e);
                                }
                                if (sourceCount-- == 1) {
                                    sendResponseCallback.run(searchResult.toString());
                                }
                            }
                        };
                        if (SiteDBridge.searchSources.size() == 0) {
                            sendResponseCallback.run("[]");
                        } else {
                            for (MangaSource searchMangaSource : SiteDBridge.searchSources.values()) {
                                searchMangaSource.search(params.optString("text"), searchCallback);
                            }
                        }
                        break;
                    case "func_getdetail":
                        if (mangaSource == null) {
                            clientSocket.close();
                            break;
                        }
                        mangaSource.getBookDetail(params.optString("url"), sendResponseCallback);
                        break;
                    case "get_index":
                        if (mangaSource == null) {
                            clientSocket.close();
                            break;
                        }
                        mangaSource.getSection(params.optString("url"), sendResponseCallback);
                        break;
                    default:
                        clientSocket.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                log(e);
            }

        }
    }

    private class InputProcessThread extends Thread {
        /**
         * Constructs a new {@code Thread} with no {@code Runnable} object and a
         * newly generated name. The new {@code Thread} will belong to the same
         * {@code ThreadGroup} as the {@code Thread} calling this constructor.
         *
         * @see ThreadGroup
         * @see Runnable
         */
        public InputProcessThread(final Socket clientSocket) {
            super(new Runnable() {
                @Override
                public void run() {
                    try {
                        Reader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "utf-8"));
                        StringBuilder inputStringBuffer = new StringBuilder();
                        int c;
                        while ((c = in.read()) != -1) {
                            if (((char) c) == '\n')
                                break;
                            inputStringBuffer.append((char) c);
                        }
                        final String params = URLDecoder.decode(inputStringBuffer.toString(), "utf-8");
                        Bundle bundle = new Bundle();
                        bundle.putString("params", params);
                        Message message = new Message();
                        message.setData(bundle);
                        message.what = 1;
                        message.obj = clientSocket;
                        processHandler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log(e);
                    }
                }
            });
        }
    }
}
