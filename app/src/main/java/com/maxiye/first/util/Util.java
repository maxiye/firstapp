package com.maxiye.first.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 函数助手
 * Created by due on 2018/5/16.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Util {
    /**
     * unicode字符串(\\uxxxx)转为中文
     *
     * @param unicode String
     * @return String
     */
    public static String unicode2Chinese(String unicode) {
        Pattern p = Pattern.compile("\\\\u([a-f0-9]{4})");
        Matcher mt = p.matcher(unicode);
        String result = unicode;
        while (mt.find()) {
            int byte1, byte2;
            String item = mt.group(1);
            byte1 = Integer.parseInt(item.substring(0, 2), 16) << 8;//第一个byte是高位，相当于‘十位’
            byte2 = Integer.parseInt(item.substring(2), 16);//这是‘个位’
            result = result.replace(mt.group(0), String.valueOf((char) (byte1 + byte2)));
        }
        return result;
    }

    public static String unescape(String content) {
        /*content = content.replaceAll("\\\\/", "");
        content = content.replaceAll("\\\\\"", "'");*/
        content = content.replaceAll("\\\\(?=[/|\"])", "");
        content = unicode2Chinese(content);
        MyLog.w("unescape", content);
        return content;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
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
            if (pfd == null)
                throw new FileNotFoundException();
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
}
