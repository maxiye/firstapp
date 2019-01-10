package com.maxiye.first.util;

import android.graphics.BitmapFactory;
import android.os.Environment;

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
     * @param unicode String
     * @return String
     */
    public static String unicode2Chinese(String unicode) {
        Pattern p = Pattern.compile("\\\\u([a-f0-9]{4})");
        Matcher mt = p.matcher(unicode);
        String result = unicode;
        while (mt.find()){
            int byte1, byte2;
            String item = mt.group(1);
            byte1 = Integer.parseInt(item.substring(0, 2), 16) << 8;//第一个byte是高位，相当于‘十位’
            byte2 = Integer.parseInt(item.substring(2), 16);//这是‘个位’
            result = result.replace(mt.group(0), String.valueOf((char) (byte1 + byte2)));
        }
        return result;
    }

    /**
     * 计算图片压缩倍率,降低内存消耗
     * @param options Options
     * @param reqWidth int
     * @param reqHeight int
     * @return int
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize <<= 1;
            }
        }

        return inSampleSize;
    }

    public static int predictInSampleSize(long fileSize, String type) {
        if (type.equals("gif"))
            return 4;
        if (fileSize < 30720) {//30
            return 4;
        } else if (fileSize < 122880) {//120
            return 8;
        } else if (fileSize < 491520) {//480
            return 16;
        } else {
            return 32;
        }
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
}
