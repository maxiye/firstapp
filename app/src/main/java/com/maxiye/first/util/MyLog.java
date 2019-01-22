package com.maxiye.first.util;

import android.util.Log;

import com.maxiye.first.BuildConfig;

/**
 * 数据库助手
 * Created by due on 2019/1/21.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MyLog {
    private static final boolean isDebug = BuildConfig.DEBUG;

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable e) {
        if (isDebug) {
            Log.w(tag, msg, e);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, msg);
        }
    }

    public static void println(String msg) {
        if (isDebug) {
            System.out.println(msg);
        }
    }

    // 更多log输出方法 ....

    public static boolean isDebug() {
        return isDebug;
    }
}
