package com.maxiye.first.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.view.Menu;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 常用方法助手
 *
 * @author due
 * @date 2018/5/16
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Util {

    private static ExecutorService singleThreadPool;

    /**
     * Checks if external storage is available for read and write
      * @return boolean
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     * @return boolean
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static FileDescriptor getFileDescriptor(Context context, Intent intent) {
        try {
            Uri pic = intent.getData();
            assert pic != null;
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(pic, "r");
            if (pfd == null) {
                throw new FileNotFoundException();
            }
            return pfd.getFileDescriptor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存到内部存储
     *
     * @param filename Filename
     * @param content File content
     */
    @SuppressWarnings("unused")
    public void saveFileIn(Context context, String filename, String content) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存到外部存储
     *
     * @param file   File
     * @param content File content
     */
    public static void saveFileEx(File file, String content) {
        if (isExternalStorageWritable()) {
            try {
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new IOException("create dir error");
                }
                if (!file.exists() && !file.createNewFile()) {
                    throw new IOException("create file error");
                }
                RandomAccessFile raf = new RandomAccessFile(file, "rwd");
                //raf.seek(file.length());//追加模式
                raf.write(content.getBytes());
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MyLog.w("saveFileEx", "Permission denied!");
        }

    }

    /**
     * 开启Menu的icon显示
     * @param menu Menu
     * @return Menu
     */
    @SuppressLint("PrivateApi")
    public static Menu iconMenu(Menu menu) {
        try {
            Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return menu;
    }

    /**
     * 取前48bit 计算位或hash
     * @param name String
     * @return int
     */
    public static long strHash(String name) {
        byte[] bytes = name.getBytes();
        long hash = 0;
        int lft = 40;
        for (byte aByte : bytes) {
            hash |= ((long)aByte << lft);
            if (lft >= 8) {
                lft -= 8;
            } else {
                break;
            }
        }
        return hash;
    }

    public static class MyThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(@NonNull Runnable r) {
            MyLog.w("MyThreadFactory", "new");
            return new Thread(r,"due-single-thread");
        }
    }

    public static ExecutorService getDefaultSingleThreadExecutor() {
        if (singleThreadPool == null) {
            singleThreadPool = new ThreadPoolExecutor(1, 2,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(5), new MyThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        }
        return singleThreadPool;
    }
}
