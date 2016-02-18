package io.goshin.bukadarkness.sited;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

import org.json.JSONObject;
import org.noear.sited.SdApi;
import org.noear.sited.SdLogListener;
import org.noear.sited.SdNodeFactory;
import org.noear.sited.SdSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;

import io.goshin.bukadarkness.SiteDBridge;

public class Server extends Service {
    public static final int PORT = 2203;
    private ProcessHandler processHandler;
    private ServerSocket serverSocket;

    public Server() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
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
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                    }
                }
            });
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
                    public void run(String result) {
                        try {
                            Writer out = new OutputStreamWriter(clientSocket.getOutputStream(), "utf-8");
                            out.write(URLEncoder.encode(result, "utf-8") + "\n");
                            out.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                int sourceID = Integer.parseInt("0" + params.optString("fp"));
                MangaSource mangaSource = SiteDBridge.sources.get(sourceID);
                switch (params.optString("f").toLowerCase()) {
                    case "func_getmangagroups":
                        SiteDBridge.initSource(0, "http://sited.noear.org/addin/site1035.sited.xml", new Runnable() {
                            @Override
                            public void run() {
                                sendResponseCallback.run("[{\"gname\":\"比比猴漫画站\",\"gid\":\"0\"}]");
                            }
                        });
                        break;
                    case "func_getgroupitems":
                        if (mangaSource == null) {
                            clientSocket.close();
                            break;
                        }
                        mangaSource.getHots(sendResponseCallback);
                        break;
                    case "func_search":
                        if (mangaSource == null) {
                            clientSocket.close();
                            break;
                        }
                        mangaSource.search(params.optString("text"), sendResponseCallback);
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
            }

        }
    }
}
