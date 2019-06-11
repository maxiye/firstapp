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

/**
 * 常用方法助手
 *
 * {@code 第68条：遵守普遍接受的命名约定}
 * 包和模块名称应该是分层的，每个部分应包含小写字母字符，很少包含数字，通常为8个或更少的字符。鼓励使用有意义的缩写
 * 类和接口名称（包括枚举和注解类型名称）应由一个或多个单词组成，每个单词的首字母大写，例如List或FutureTask。 除了首字母缩略词和某些常用缩写（如max和min）之外，应避免使用缩写。首字母缩略词只支持大写第一个字母
 * 方法和属性名遵循与类和接口名相同的字面约定，首字母缩略词作为方法或属性名称的第一个单词出现，则它应该是小写的
 * “常量属性”，它的名称应该由一个或多个大写单词组成，由下划线分隔，例如VALUES或NEGATIVE_INFINITY。
 * 局部变量名称与成员名称具有相似的字面命名约定，但允许使用缩写除外
 * 类型参数名通常由单个字母组成。最常见的是以下五种类型之一：T表示任意类型，E表示集合的元素类型，K和V表示映射的键和值类型，X表示异常
 *
 * @author due
 * @date 2018/5/16
 */
public class Util {

    private static ExecutorService singleThreadPool;

    /**
     * Checks if external storage is available for read and write
      * @return boolean
     */
    @SuppressWarnings({"WeakerAccess"})
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
    public static Menu enableMenuIcon(Menu menu) {
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
     * {@code 第58条：for-each循环优于传统for循环}
     * @param name String
     * @return int
     */
    @SuppressWarnings({"unused"})
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


    /**
     * 通过收藏文件名获取收藏数据库id
     * @param fileName 文件名
     * @return id
     */
    public static int getFavId(String fileName) {
        int pos = fileName.indexOf("_");
        // id < 99999
        if (pos < 6 && pos > 0) {
            return Integer.valueOf(fileName.substring(0, pos));
        }
        return 0;
    }
}
