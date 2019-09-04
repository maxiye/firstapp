package com.maxiye.first.util;

/*
  数据库助手
  Created by due on 2018/5/3.
 */
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 清除缓存
 *
 * @author due
 */
public class CacheUtil {
    /**
     * {@code 第34条：用枚举替换常量}
     * {@code 第62条：当有其他更合适的类型时就不用字符串}
     * 字符串是枚举类型的不良替代品。
     * public static final String KB = "KB";
     * public static final String MB = "MB";
     * public static final String GB = "GB";
     */
    public enum Unit {
        /**
         * 单位KB
         */
        KB,
        /**
         * 单位MB
         */
        MB,
        /**
         * 单位GB
         */
        GB
    }

    /**
     * @param context Activity
     * @return string String
     *             获取当前缓存
     */
    @NonNull
    @SuppressWarnings({"unused"})
    public static String getTotalCacheSize(@NonNull Context context) {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        return getFormatSize(cacheSize);
    }

    /**
     * @param context
     *            删除缓存
     */
    public static void clearAllCache(@NonNull Context context) {
        deleteDir(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteDir(context.getExternalCacheDir());
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                int size;
                size = children.length;
                for (int i = 0; i < size; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }

        }
        return dir == null || dir.delete();
    }

    /**
    * 获取文件
    * Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/
    * 目录，一般放一些长时间保存的数据
    * Context.getExternalCacheDir() -->
    * SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
     * @param file File
    */
    private static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File aFileList : fileList) {
                    // 如果下面还有文件
                    if (aFileList.isDirectory()) {
                        size = size + getFolderSize(aFileList);
                    } else {
                        size = size + aFileList.length();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 格式化单位
     * 计算缓存的大小
     * @param size int
     * @return string
     */
    @NonNull
    private static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
            // return size + "Byte";
            return "0K";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "KB";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "MB";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }

    public static double getSize(@NonNull Context context, Unit unit) {
        long cacheSize = getFolderSize(context.getCacheDir());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            cacheSize += getFolderSize(context.getExternalCacheDir());
        }
        if (unit == null) {
            return cacheSize;
        }
        // / 10
        double kiloByte = cacheSize >> 10;
        if (unit.equals(Unit.KB)) {
            return fix2(kiloByte);
        }
        double megaByte = kiloByte / 1024;
        if (unit.equals(Unit.MB)) {
            return fix2(megaByte);
        }
        double gigaByte = megaByte / 1024;
        if (unit.equals(Unit.GB)) {
            return fix2(gigaByte);
        }
        return fix2(megaByte);
    }

    private static double fix2(double size) {
        BigDecimal result = new BigDecimal(size);
        return result.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 清除部分缓存
     * @param directory File
     * @param size total size
     */
    private static void clearOld(@NonNull File directory, double size) {
        File[] fileList = directory.listFiles(File::isFile);
        if (fileList == null) {
            return;
        }
        Arrays.sort(fileList, (f1, f2) -> {
            long diff = f1.lastModified() - f2.lastModified();
            if (diff > 0) {
                return 1;
            } else if (diff == 0) {
                return 0;
            } else {
                // 如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
                return -1;
            }
        });
        for (File f : fileList) {
            long fLength = f.length();
            if (size > 0 && f.delete()) {
                size -= fLength;
            } else {
                break;
            }
        }
    }

    /**
     * 缓存超过400M，自动清空
     * @param context Context
     */
    @SuppressWarnings({"unused"})
    public static void checkClear(Context context) {
        double cacheSize = CacheUtil.getSize(context, null);
        if (cacheSize > 400 << 10 << 10) {
            CacheUtil.clearOld(context.getCacheDir(), cacheSize - (250 << 10 << 10));
        }
    }
}
