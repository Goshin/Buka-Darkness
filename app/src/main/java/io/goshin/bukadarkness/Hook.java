package io.goshin.bukadarkness;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.goshin.bukadarkness.adapter.Comments;
import io.goshin.bukadarkness.adapter.Contributions;
import io.goshin.bukadarkness.adapter.Detail;
import io.goshin.bukadarkness.adapter.Groups;
import io.goshin.bukadarkness.adapter.Index;
import io.goshin.bukadarkness.adapter.Items;
import io.goshin.bukadarkness.adapter.Utils;

@SuppressWarnings("deprecation")
public class Hook implements IXposedHookLoadPackage {

    public static final boolean VERBOSE = false;

    private void log(String text) {
        if (VERBOSE) {
            XposedBridge.log(text);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private MangaAdapter getAdapter(String f) {
        String action = f.substring(f.indexOf("_") + 1).toLowerCase();
        MangaAdapter adapter = null;
        switch (action) {
            case "getmangagroups":
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
        }
        return adapter;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.toLowerCase().contains("cn.ibuka.manga")) {
            return;
        }

        XposedHelpers.findAndHookMethod("org.apache.http.impl.client.AbstractHttpClient", loadPackageParam.classLoader, "execute", HttpUriRequest.class, new XC_MethodHook() {
            private JSONObject getParam(HttpPost request) throws Throwable {
                try {
                    String i = URLDecoder.decode(URLEncodedUtils.parse(request.getEntity()).get(0).getValue());
                    log("HttpPost i " + i);
                    return new JSONObject(i);
                } catch (Exception ignored) {
                }
                return new JSONObject();
            }

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!param.args[0].getClass().getCanonicalName().contains("HttpPost")) {
                    return;
                }
                HttpPost request = (HttpPost) param.args[0];

                String url = request.getURI().toString();
                JSONObject params = getParam(request);
                MangaAdapter adapter = getAdapter(params.optString("f"));
                if (url.toLowerCase().contains("bug") || (adapter != null && adapter.needRedirect(params))) {
                    request.setURI(new URI("http://www.baidu.com/"));
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!param.args[0].getClass().getCanonicalName().contains("HttpPost")) {
                    return;
                }
                HttpPost request = (HttpPost) param.args[0];
                HttpResponse response = (HttpResponse) param.getResult();

                String url = request.getURI().toString();
                log("HttpPostURL " + url);

                JSONObject params = getParam(request);
                MangaAdapter adapter = getAdapter(params.optString("f"));
                if (adapter == null) {
                    return;
                }

                String result;
                if (!adapter.needRedirect(params)) {
                    GZIPInputStream gzipInputStream = new GZIPInputStream(response.getEntity().getContent());
                    byte[] responseReadBuffer = new byte[1024];
                    ByteArrayOutputStream responseBytesOutputStream = new ByteArrayOutputStream();
                    int n;
                    while ((n = gzipInputStream.read(responseReadBuffer)) > 0) {
                        responseBytesOutputStream.write(responseReadBuffer, 0, n);
                    }
                    String responseString = responseBytesOutputStream.toString();
                    log("Decode Response Stream " + responseString);

                    result = adapter.getResult(params, new JSONObject(responseString));
                } else {
                    result = adapter.getResult(params, null);
                }

                log("set result");
                ByteArrayOutputStream compressOutputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressOutputStream);
                gzipOutputStream.write(result.getBytes());
                gzipOutputStream.close();
                response.setEntity(new ByteArrayEntity(compressOutputStream.toByteArray()));
            }
        });

        XposedHelpers.findAndHookConstructor("java.net.URL", loadPackageParam.classLoader, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String url = (String) param.args[0];
                log("URL construct " + url);
                if (url.contains(Index.BASE_PREFIX)) {
                    param.args[0] = Index.getRealPicUrl(url);
                    log("Change URL to " + param.args[0]);
                } else if (url.contains(Items.COVER_PREFIX)) {
                    param.args[0] = Items.coverMap.get(url.substring(url.lastIndexOf("-") + 1, url.lastIndexOf("/")));
                    log("Change URL to " + param.args[0]);
                }
            }
        });

        XC_MethodHook URLGetInputStreamHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                URL url = ((URLConnection) param.thisObject).getURL();
                log("URLConnection getInputStream " + url);
                if (!url.toString().contains("req3.php")) {
                    return;
                }

                HashMap<String, String> params = new HashMap<>();
                for (NameValuePair p : URLEncodedUtils.parse(url.toURI(), "UTF-8")) {
                    params.put(p.getName(), p.getValue());
                }
                Index index = new Index(params.get("mid"), params.get("cid"));
                if (!index.match()) {
                    return;
                }
                param.setResult(Utils.indexEncode(index.getClip(), index.getBase(), Integer.parseInt(params.get("mid")), Integer.parseInt(params.get("cid"))));
            }
        };

        if (Build.VERSION.SDK_INT >= 19) {
            //noinspection SpellCheckingInspection
            XposedHelpers.findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", loadPackageParam.classLoader, "getInputStream", URLGetInputStreamHook);
        } else {
            //noinspection SpellCheckingInspection
            XposedHelpers.findAndHookMethod("libcore.net.http.HttpURLConnectionImpl", loadPackageParam.classLoader, "getInputStream", URLGetInputStreamHook);
        }


        final Intent serviceIntent = new Intent("BukaDarknessServer");
        //noinspection SpellCheckingInspection
        serviceIntent.setComponent(new ComponentName("io.goshin.bukadarkness", "io.goshin.bukadarkness.sited.Server"));
        XC_MethodHook startServiceHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ((Activity) param.thisObject).startService(serviceIntent);
            }
        };
        XC_MethodHook stopServiceHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ((Activity) param.thisObject).stopService(serviceIntent);
            }
        };

        if (loadPackageParam.packageName.toLowerCase().contains("hd")) {
            XposedHelpers.findAndHookMethod("cn.ibuka.manga.hd.ActivityStartup", loadPackageParam.classLoader, "onCreate", Bundle.class, startServiceHook);
            XposedHelpers.findAndHookMethod("cn.ibuka.manga.hd.hd.HDActivityMain", loadPackageParam.classLoader, "onDestroy", stopServiceHook);
        } else {
            XposedHelpers.findAndHookMethod("cn.ibuka.manga.ui.ActivityStartup", loadPackageParam.classLoader, "onCreate", Bundle.class, startServiceHook);
            XposedHelpers.findAndHookMethod("cn.ibuka.manga.ui.ActivityMain", loadPackageParam.classLoader, "onDestroy", stopServiceHook);
        }


        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ApplicationInfo> applicationList = (List) param.getResult();
                List<ApplicationInfo> resultApplicationList = new ArrayList<>();
                for (ApplicationInfo applicationInfo : applicationList) {
                    if (!applicationInfo.processName.toLowerCase().contains("darkness")) {
                        resultApplicationList.add(applicationInfo);
                    }
                }
                param.setResult(resultApplicationList);
            }
        });

        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @SuppressWarnings("unchecked")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<PackageInfo> packageInfoList = (List) param.getResult();
                List<PackageInfo> resultPackageInfoList = new ArrayList<>();
                for (PackageInfo packageInfo : packageInfoList) {
                    if (!packageInfo.packageName.toLowerCase().contains("darkness")) {
                        resultPackageInfoList.add(packageInfo);
                    }
                }
                param.setResult(resultPackageInfoList);
            }
        });
    }
}
