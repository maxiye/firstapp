package com.maxiye.first.util;

import android.util.Log;

import com.maxiye.first.BuildConfig;

/**
 * 自定义日志
 *
 * @author due
 * @date 2019/1/21
 */
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

    public static void i(String tag, String msg) {
        if (IS_DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void wtf(String tag, String msg) {
        if (IS_DEBUG) {
            Log.wtf(tag, msg);
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

    public static void e(String tag, String msg) {
        if (IS_DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (IS_DEBUG) {
            Log.e(tag, msg, e);
        }
    }

    public static void println(String msg) {
        if (IS_DEBUG) {
            System.out.println(msg);
        }
    }

    // 更多log输出方法 ....

    @SuppressWarnings({"unused"})
    public static boolean isDebug() {
        return IS_DEBUG;
    }
}
