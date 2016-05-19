package io.goshin.bukadarkness;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import org.json.JSONObject;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.goshin.bukadarkness.adapter.Detail;

public class ReaderHook {
    private static Activity readerActivity;
    private static boolean popupBars;
    private static boolean consumed = false;
    private static boolean in3rdPartyManga = false;
    private static boolean splitLinkedPage = true;

    public static void initHook(final String packageName, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        popupBars = Hook.config.getBoolean("popupBars");
        splitLinkedPage = Hook.config.getBoolean("splitLinkedPage");

        final int apiLevel = Build.VERSION.SDK_INT;
        XC_MethodHook onMoveToBackHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                ((Activity) param.thisObject).finish();
                param.setResult(true);
            }
        };

        XC_MethodHook onReaderCreateHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                readerActivity = (Activity) param.thisObject;
                Intent intent = readerActivity.getIntent();
                int mid = intent.getIntExtra("mid", 0);
                in3rdPartyManga = new Detail().needRedirect(new JSONObject("{mid:\"" + mid + "\"}"));
            }
        };
        XC_MethodHook showReaderMenuHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Window window = readerActivity.getWindow();
                if (apiLevel >= 19 && popupBars && Hook.bukaPref.getBoolean("use_immersive_mode", false)) {
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    window.setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
            }
        };

        XC_MethodHook hideReaderMenuHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Window window = readerActivity.getWindow();
                if (Build.VERSION.SDK_INT >= 19 && popupBars && Hook.bukaPref.getBoolean("use_immersive_mode", false)) {
                    window.getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );
                    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
            }
        };

        XposedHelpers.findAndHookMethod(packageName + ".ActivityBukaReader", loadPackageParam.classLoader, "onCreate", Bundle.class, onReaderCreateHook);
        XposedHelpers.findAndHookMethod(packageName + ".ActivityBukaReader", loadPackageParam.classLoader, "onBackPressed", onMoveToBackHook);

        XposedHelpers.findAndHookMethod(packageName + ".ViewReaderMenu", loadPackageParam.classLoader, "b", showReaderMenuHook);
        XposedHelpers.findAndHookMethod(packageName + ".ViewReaderMenu", loadPackageParam.classLoader, "c", hideReaderMenuHook);

        XposedHelpers.findAndHookMethod("cn.ibuka.common.widget.BukaImageView_SpecArea", loadPackageParam.classLoader, "getFitImagePosition", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                RectF imageRectF = (RectF) XposedHelpers.callMethod(param.thisObject, "getImageResourceRectF");
                float imageWidth = imageRectF.right;
                float imageHeight = imageRectF.bottom;
                int viewWidth = (Integer) XposedHelpers.callMethod(param.thisObject, "getWidth");
                int viewHeight = (Integer) XposedHelpers.callMethod(param.thisObject, "getHeight");
                Matrix matrix = new Matrix();
                float scale = viewHeight / imageHeight;
                scale = Math.min(scale, viewWidth / imageWidth);
                matrix.setScale(scale, scale);
                matrix.postTranslate((viewWidth - imageWidth * scale) / 2, (viewHeight - imageHeight * scale) / 2);
                param.setResult(matrix);
            }
        });

        XposedHelpers.findAndHookMethod("cn.ibuka.common.widget.BukaImageView", loadPackageParam.classLoader, "onLayout", "boolean", "int", "int", "int", "int", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!splitLinkedPage || !in3rdPartyManga) {
                    return;
                }
                RectF imageRectF = (RectF) XposedHelpers.callMethod(param.thisObject, "getImageResourceRectF");
                float imageWidth = imageRectF.right;
                float imageHeight = imageRectF.bottom;
                int viewWidth = (Integer) XposedHelpers.callMethod(param.thisObject, "getWidth");
                int viewHeight = (Integer) XposedHelpers.callMethod(param.thisObject, "getHeight");
                Matrix matrix = new Matrix();
                float scale = viewHeight / imageHeight;
                if (imageHeight > imageWidth) {
                    scale = Math.min(scale, viewWidth / imageWidth);
                } else {
                    scale = Math.min(scale, viewWidth * 2 / imageWidth);
                }
                matrix.setScale(scale, scale);
                float offset = 0;
                if (Hook.bukaPref.getBoolean("vert_reading_rtl", false)) {
                    offset = viewWidth - imageWidth * scale;
                }
                matrix.postTranslate(offset, (viewHeight - imageHeight * scale) / 2);
                XposedHelpers.callMethod(param.thisObject, "setImageMatrix", matrix);
            }
        });

        XposedHelpers.findAndHookMethod("android.view.GestureDetector.SimpleOnGestureListener", loadPackageParam.classLoader, "onSingleTapUp", MotionEvent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (!splitLinkedPage ||
                            !in3rdPartyManga ||
                            !param.thisObject.getClass().getCanonicalName().equals("cn.ibuka.common.widget.g")) {
                        return;
                    }
                } catch (NullPointerException ignored) {
                    return;
                }
                Object layout = XposedHelpers.getObjectField(param.thisObject, "a");
                if ((Boolean) XposedHelpers.callMethod(layout, "b")) {
                    return;
                }
                Object view = XposedHelpers.callMethod(layout, "getForegroundView");
                if (!view.getClass().getCanonicalName().contains("ViewBukaImageInSwitcher")) {
                    return;
                }
                ImageView imageView = (ImageView) XposedHelpers.getObjectField(view, "d");
                int viewWidth = imageView.getWidth();
                int viewHeight = imageView.getHeight();
                RectF imageRectF = (RectF) XposedHelpers.callMethod(imageView, "getImageResourceRectF");
                float imageWidth = imageRectF.right;

                float[] matrixValues = new float[9];
                Matrix matrix = imageView.getImageMatrix();
                matrix.getValues(matrixValues);
                int offset = (int) matrixValues[2];
                boolean rightToLeft = Hook.bukaPref.getBoolean("vert_reading_rtl", false);
                MotionEvent motionEvent = (MotionEvent) param.args[0];
                consumed = false;
                if (-offset > 0.75 * viewWidth &&
                        ((rightToLeft && tapAction(motionEvent, viewWidth, viewHeight) == 3)
                                || (!rightToLeft && tapAction(motionEvent, viewWidth, viewHeight) == 1))
                        ) {
                    matrix.postTranslate(-offset, 0);
                    consumed = true;
                } else if (imageWidth * matrixValues[0] + offset > 1.75 * viewWidth &&
                        ((rightToLeft && tapAction(motionEvent, viewWidth, viewHeight) == 1)
                                || (!rightToLeft && tapAction(motionEvent, viewWidth, viewHeight) == 3))) {
                    matrix.postTranslate(viewWidth - offset - imageWidth * matrixValues[0], 0);
                    consumed = true;
                }
                if (consumed) {
                    imageView.setImageMatrix(matrix);
                    imageView.postInvalidate();
                }
            }
        });

        XposedHelpers.findAndHookMethod(packageName + ".ActivityBukaReader", loadPackageParam.classLoader, "b", "int", "int", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (splitLinkedPage && in3rdPartyManga && consumed) {
                    param.setResult(true);
                }
            }
        });
    }

    private static int tapAction(MotionEvent motionEvent, int viewWidth, int viewHeight) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (!Hook.bukaPref.getBoolean("regionpotr", true)) {
            x = viewWidth - x;
        }
        if (x <= 0.3 * viewWidth) {
            return 1;
        }
        if (x < 0.7 * viewWidth && y > 0.2 * viewHeight && y < 0.8 * viewHeight) {
            return 2;
        }
        return 3;
    }
}
