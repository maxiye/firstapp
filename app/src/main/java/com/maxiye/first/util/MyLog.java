package com.maxiye.first.util;

import android.util.Log;

import com.maxiye.first.BuildConfig;

/**
 * 数据库助手
 *
 * @author due
 * @date 2019/1/21
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MyLog {
    private static final boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void d(String tag, String msg) {
        if (IS_DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (IS_DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable e) {
        if (IS_DEBUG) {
            Log.w(tag, msg, e);
        }
    }

    public static void w(String tag, String msg) {
        if (IS_DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void println(String msg) {
        if (IS_DEBUG) {
            System.out.println(msg);
        }
    }

    // 更多log输出方法 ....

    public static boolean isDebug() {
        return IS_DEBUG;
    }
}
