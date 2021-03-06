package com.raincat.dolby_beta.hook;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.raincat.dolby_beta.utils.Setting;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * <pre>
 *     author : RainCat
 *     time   : 2019/10/23
 *     desc   : 去广告和升级
 *     version: 1.0
 * </pre>
 */

public class AdAndUpdateHook {
    private static String okHttpClientClassString = "okhttp3.OkHttpClient";
    private static String newCallMethodString = "newCall";
    private static String httpUrlFieldString = "url";
    private static String urlFieldString = "url";

    private static boolean removeAd = true;
    private static boolean removeUpdate = true;

    public AdAndUpdateHook(Context context, final int versionCode) {
        if (versionCode < 138) {
            okHttpClientClassString = "okhttp3.x";
            newCallMethodString = "a";
            httpUrlFieldString = "a";
            urlFieldString = "j";
        }
        removeAd = Setting.isBlackEnabled();
        removeUpdate = Setting.isUpdateEnabled();

        //去广告和升级
        hookAllMethods(findClass(okHttpClientClassString, context.getClassLoader()), newCallMethodString, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args != null && param.args.length == 1 && param.args[0].getClass().getName().contains("okhttp")) {
                    Object request = param.args[0];
                    Field httpUrl = request.getClass().getDeclaredField(httpUrlFieldString);
                    httpUrl.setAccessible(true);
                    Object urlObj = httpUrl.get(request);
                    if ((removeAd && urlObj.toString().contains("api/ad")) || (removeUpdate && (urlObj.toString().contains("android/version") || urlObj.toString().contains("android/upgrade")))) {
                        Field url = urlObj.getClass().getDeclaredField(urlFieldString);
                        boolean urlAccessible = url.isAccessible();
                        url.setAccessible(true);
                        url.set(urlObj, "https://33.123.21.14/");
                        url.setAccessible(urlAccessible);
                        param.args[0] = request;
                    }
                }
            }
        });

        if (removeAd && XposedHelpers.findClass("com.netease.cloudmusic.activity.LoadingAdActivity", context.getClassLoader()) != null)
            findAndHookMethod("com.netease.cloudmusic.activity.LoadingAdActivity", context.getClassLoader(),
                    "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            ((Activity) param.thisObject).finish();
                            param.setResult(null);
                        }
                    });
    }
}
