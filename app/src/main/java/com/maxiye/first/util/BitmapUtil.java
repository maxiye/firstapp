package com.maxiye.first.util;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.maxiye.first.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

/**
 * bitmap工具
 * Created by due on 2018/5/16.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BitmapUtil {

    /**
     * 计算图片压缩倍率,降低内存消耗
     *
     * @param options   Options
     * @param reqWidth  int
     * @param reqHeight int
     * @return int
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height >> 1;
            final int halfWidth = width >> 1;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize <<= 1;
            }
        }
        MyLog.w("calculateInSampleSize", String.valueOf(inSampleSize));
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

    /**
     * 心理学公式：Gray = R*0.299 + G*0.587 + B*0.114
     * 优化：Gray = (R*299 + G*587 + B*114 + 500) / 1000  注意后面那个除法是整数 除法，所以需要加上500来实现四舍五入
     * 16位精度 Gray = (R*19595 + G*38469 + B*7472) >> 16 无误差
     * 10位精度 Gray = (R*306 + G*601 + B*117) >> 10 2% 误差 1
     * 7位精度 Gray = (R*38 + G*75 + B*15) >> 7    20%+ 误差 1
     * 转换图片为黑白效果
     *
     * @param bitmap Bitmap
     * @return Bitmap
     */
    public static Bitmap convertGray(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        MyLog.w("convertGray", "wh:" + w + "," + h);
        int alpha = 0xFF << 24;
        int[] pixels = new int[w * h];//3840*2312 8k
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        int avg = 0;
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            int gray = (r * 306 + g * 601 + b * 117) >> 10;
            pixels[i] = alpha | (gray << 16) | (gray << 8) | gray;//位或替换+，优化性能
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap convertGray2(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        MyLog.w("convertGray", "wh:" + w + "," + h);
        int alpha = 0xFF << 24;
        Bitmap ret = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = bitmap.getPixel(i, j);
                int r = (color & 0x00FF0000) >> 16;
                int g = (color & 0x0000FF00) >> 8;
                int b = color & 0x000000FF;
                /*int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                int avg = (r + g + b) / 3;*/
                //min 比较深黑 max比较浅 avg正好
                int gray = (r * 306 + g * 601 + b * 117) >> 10;
                int rgb = alpha | (gray << 16) | (gray << 8) | gray;//位或替换+，优化性能
//                MyLog.w("convertGray", "rgb:" + r + "," + g + "," + b + ";" + "max,min,avg:" + max + "," + min + "," + avg + ";" + "rgb:" + rgb);
                ret.setPixel(i, j, rgb);
            }
        }
        return ret;
    }

    /**
     * 转换为反色
     * 原型
     * int alpha = 0xFF << 24;
     * int r = (color & 0xFF0000) >> 16;
     * int g = (color & 0xFF00) >> 8;
     * int b = color & 0xFF;
     * int rgb = alpha | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
     * @param bitmap Bitmap
     * @return Bitmap
     */
    public static Bitmap convertReverse(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] ^= 0xFFFFFF;
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    /**
     * 转换为只有黑白色的位图
     * @param bitmap Bitmap
     * @return Bitmap
     */
    public static Bitmap convertDot(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int avg = 0;
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            pixels[i] = (r * 306 + g * 601 + b * 117) >> 10;//转灰度
            avg += pixels[i];
        }
        avg = avg / (width * height);
        long meta = 0;
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = pixels[i] > avg ? 0xFFFFFFFF : 0xFF000000;
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565);
    }

    /**
     * 转换为只有黑白色的文本
     * @param bitmap Bitmap
     * @return String
     */
    public static String convertDotTxt(Bitmap bitmap) {
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        float scale = 150f / Math.max(w , h);
        bitmap = scaleBitmap(bitmap, scale, scale);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int avg = 0;
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = (color & 0xFF0000) >> 16;
            int g = (color & 0xFF00) >> 8;
            int b = color & 0xFF;
            pixels[i] = (r * 306 + g * 601 + b * 117) >> 10;//转灰度
            avg += pixels[i];
        }
        avg = avg / (width * height);
        long meta = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pixels.length; i++) {
            sb.append(pixels[i] > avg ? "  " : "**");
            if ((i + 1) % width == 0) {
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * 生成渐变色图片
     * @param color color
     * @param w width
     * @param h height
     * @return bitmap
     */
    public static Bitmap gradualBitmap(int color, int w, int h) {
        int[] pixels = new int[w * h];
        for (int i = 0; i < h; i++) {
            int rgb = gradualColor(color, i);
            int offset = w * i;
            for (int j = 0; j < w; j++) {
                pixels[offset + j] = rgb;
            }
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888);
    }

    /**
     * generate graduanl color
     *
     * @param color  int
     * @param offset int
     * @return int
     */
    public static int gradualColor1(int color, int offset) {
        int a = 0xff000000, r = (color & 0xff0000) >> 16, g = (color & 0xff00) >> 8, b = color & 0xff;
        int[] rgb = new int[]{r, g, b};
        int i = 0;
        boolean mode = true;//+
        if (mode) {
            rgb[i] += offset;
        } else {
            rgb[i] -= offset;
        }
        while (rgb[i] < 0 || rgb[i] > 255) {
            if (mode) {
                offset = rgb[i] - 255;
                rgb[i] = 255;
            } else {
                offset = -rgb[i];
                rgb[i] = 0;
            }
            if (i == 2) {
                mode = !mode;
                i = 0;
            } else {
                ++i;
            }
            if (mode) {
                rgb[i] += offset;
            } else {
                rgb[i] -= offset;
            }
        }
        return a | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    /**
     * generate graduanl color
     *
     * @param color  int
     * @param offset int
     * @return int
     */
    public static int gradualColor(int color, int offset) {
        int a = 0xff000000, r = (color & 0xff0000) >> 16, g = (color & 0xff00) >> 8, b = color & 0xff;
        int[] rgb = new int[]{r, g, b};
        boolean[] modes = new boolean[]{true, false, false};
        offset = offset / 3 + 1;
        while (offset > 0) {
            int off = offset > 255 ? 255 : offset;
            for (int i = 0; i < rgb.length; i++) {
                if (modes[i]) {
                    rgb[i] += off;
                    if (rgb[i] > 255) {
                        rgb[i] = 510 - rgb[i];
                        modes[i] = !modes[i];
                    }
                } else {
                    rgb[i] -= off;
                    if (rgb[i] < 0) {
                        rgb[i] = -rgb[i];
                        modes[i] = !modes[i];
                    }
                }
            }
            offset -= 255;
        }
        return a | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    /**
     * 从文件获取指定大小压缩比的bitmap
     *
     * @param file file
     * @param w    width
     * @param h    height
     * @return bitmap
     */
    public static Bitmap getBitmap(File file, int w, int h) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //读取图片信息，此时把options.inJustDecodeBounds 设回true，不返回bitmap
        opts.inJustDecodeBounds = true;
        try (FileInputStream fis = new FileInputStream(file); FileInputStream fis2 = new FileInputStream(file)) {
            BitmapFactory.decodeStream(fis, null, opts);
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = calculateInSampleSize(opts, w, h);
            return BitmapFactory.decodeStream(fis2, null, opts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算图片信息meta值
     * 缩放图片 {@link #calculateInSampleSize(BitmapFactory.Options, int, int)}
     * 灰度处理 {@link #convertGray(Bitmap)}
     * 分别依次计算图像每行像素点的平均值
     * 对得到的所有平均值进行计算方差，得到的方差就是图像的特征值
     * 不关注方差的大小，只关注两个方差的差值的大小。方差差值越小图像越相似！
     *
     * @param file File
     * @return float[] avg 和 方差
     */
    public static float[] calcImgMeta(File file) {
        try {
            Bitmap bitmap = getBitmap(file, 64, 64);
            assert bitmap != null;
            //bitmap = convertGray(bitmap);//优化内部计算
            int w = bitmap.getWidth(), h = bitmap.getHeight();
            int[] sumw = new int[h];
            int total = 0;
            for (int i = 0; i < h; i++) {
                int sumj = 0;
                for (int j = 0; j < w; j++) {
                    //转灰度
                    int color = bitmap.getPixel(j, i);
                    int r = (color & 0xFF0000) >> 16;
                    int g = (color & 0xFF00) >> 8;
                    int b = color & 0xFF;
                    int gray = (r * 306 + g * 601 + b * 117) >> 10;
                    sumj += gray;
                }
                sumw[i] = sumj;
                total += sumj;
            }
            float avgw = ((float) total) / h;
            float avg = avgw / w;
            float ret = 0;
            for (int i : sumw) {
                ret += Math.pow(i - avgw, 2);
            }
            return new float[]{avg, ret / (w * w)};
            //BigDecimal bg = new BigDecimal(d).setScale(2, RoundingMode.UP);bg.floatValue();
            //MyLog.w("getFavoriteList-oompress", "fileSize：" + file.length() >> 10 + " kB；compress：" + newOpts.inSampleSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new float[]{1f, 1f};
        }
    }

    public static boolean cmpImgMata(float[] meta1, float[] meta2) {
        float offAvg = Math.abs(meta1[0] - meta2[0]);
        float offDx = Math.abs(meta1[1] - meta2[1]);
        return offAvg < 1 && offDx < 160;
    }

    /**
     * 计算图片信息meta值,long
     * 第一步，缩小尺寸：图片缩小到8x8的尺寸，总共64个像素 {@link #calculateInSampleSize(BitmapFactory.Options, int, int)}
     * 第二步，简化色彩：将缩小后的图片，转为64级灰度 {@link #convertGray(Bitmap)}
     * 第三步，计算平均值：计算所有64个像素的灰度平均值
     * 第四步，比较像素的灰度：将每个像素的灰度，与平均值进行比较。大于或等于平均值，记为1；小于平均值，记为0。
     * 第五步，计算哈希值：将上一步的比较结果，组合在一起，就构成了一个64位的整数
     * diff(s1, s2)：结果在1~5说明两张照片极其相似，6~10说明较为相似，10以上说明不相似
     *
     * @param bmp bitmap
     * @return long
     */
    public static long calcImgMeta2(Bitmap bmp) {
        try {
            Bitmap bitmap = ThumbnailUtils.extractThumbnail(bmp, 8, 8);
            bitmap = convertGray(bitmap);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            int avg = 0;
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] &= 0xFF;
                avg += pixels[i];
            }
            avg = avg >> 6;//improve
            long meta = 0;
            for (int pix : pixels) {
                meta = meta << 1 | (pix >= avg ? 1 : 0);
            }
            return meta;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean cmpImgMeta2(long meta1, long meta2) {
        return Long.bitCount(meta1 ^ meta2) < 6;
    }

    /*public static boolean compImgMeta2(String meta1, String meta2) {
        char[] s1s = meta1.toCharArray();
        char[] s2s = meta2.toCharArray();
        int diffNum = 0;
        for (int i = 0; i<s1s.length; i++) {
            if (s1s[i] != s2s[i]) {
                diffNum++;
            }
        }
        return diffNum < 5;
    }*/

    /**
     * 加载图片动画
     * @param context activity
     * @return ImageView
     */
    public static ImageView loadImg(Context context) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Material_Dialog_Alert);
        GifImageView imgView = new GifImageView(context);
        imgView.setImageDrawable(context.getDrawable(R.drawable.ic_autorenew_orange_24dp));
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0x3f009688));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        imgView.setMinimumHeight(180);
        imgView.setMinimumWidth(180);
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.load_rotate);
        imgView.startAnimation(anim);
        dialog.setContentView(imgView);
        dialog.show();
        return imgView;
    }

    /**
     * 保存显示的bitmap
     *
     * @param context activity
     * @param file    file
     * @param bitmap  bitmap
     */
    public static void saveBitmap(Activity context, File file, Bitmap bitmap) {
        PermissionUtil.req(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.PER_REQ_STORAGE_WRT, () -> {
            try {
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    Toast.makeText(context, "create dir error: " + file.getParentFile().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
                if (!file.exists() && !file.createNewFile()) {
                    Toast.makeText(context, "create file error: " + file.getParentFile().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    /**
     * 判断数据是否为gif图片数据
     * 根据 文件头 的 Signature （3B） 判断，应为 GIF 三个字符的ascii码
     * @param data byte[]
     * @return boolean
     */
    public static boolean isGif(byte[] data) {
        return data[0] == 71 && data[1] == 73 && data[2] == 70;//71=>G 73=>I 70=>F
    }

    /**
     *  缩放bitmap
     * @param bitmap bitmap
     * @param scaleX X缩放倍率
     * @param scaleY Y缩放倍率
     * @return Bitmap
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, float scaleX, float scaleY) {
        Matrix matrix = new Matrix();
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        matrix.postScale(scaleX, scaleY);
        // 得到新的圖片
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    /**
     * 缩放drawable
     * @param drawable drawable
     * @return drawable
     */
    public static Drawable scaleDrawable(Drawable drawable, int w, int h) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        float scale = 1f;
        if (w > 0) {
            scale = ((float) w) / bitmap.getWidth();
        } else if(h > 0) {
            scale = ((float) h) / bitmap.getHeight();
        }
        return new BitmapDrawable(null, scaleBitmap(((BitmapDrawable) drawable).getBitmap(), scale, scale));
    }
}
