package com.maxiye.first.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.maxiye.first.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import pl.droidsonroids.gif.GifImageView;

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

    /**
     * 根据intent获取文件描述符
     * @param context Context
     * @param intent Intent
     * @return FileDescriptor
     */
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
     * 根据结果intent获取文件路径
     * Intent的类型必须是{@link Intent#ACTION_GET_CONTENT}
     * @param intent Intent
     * @return String
     */
    public static String getPathFromIntent(Intent intent) {
        try {
            Uri uri = intent.getData();
            assert uri != null;
            if ("content".equals(uri.getScheme())) {
                assert uri.getLastPathSegment() != null;
                /*
                 * W/uri2string: content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Ffirst.db.bak.20190827095416
                 * W/getPath: /document/raw:/storage/emulated/0/Download/first.db.bak.20190827095416
                 * W/getLastPathSegment: raw:/storage/emulated/0/Download/first.db.bak.20190827095416
                 */
                return uri.getLastPathSegment().substring(4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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

    /**
     * 分享
     */
    public static void share(File file, Activity activity) {
        if (file != null && file.exists()) {
            try {
                Uri fileUri = FileProvider.getUriForFile(activity, "com.maxiye.first.fileprovider", file);
                if (fileUri != null) {
                    /*Intent itt = new Intent(Intent.ACTION_VIEW,fileUri);// 错误？不能直接使用fileUri
                    itt.setType(getContentResolver().getType(fileUri));*/
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    // "image/jpeg"也可
                    // 微信 qq 获取资源失败
                    // intent.setDataAndType(fileUri, activity.getContentResolver().getType(fileUri));
                    // intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    intent.setType(activity.getContentResolver().getType(fileUri));
                    activity.startActivity(Intent.createChooser(intent, "Share Image"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "文件获取错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "文件获取错误", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unused")
    public static void showEditDialog(Activity context, String title, String value,@NonNull Consumer<String> consumer) {
        showEditDialog(context, title, value, InputType.TYPE_CLASS_TEXT, null, consumer);
    }

    /**
     * 默认的编辑修改弹框
     * @param title 标题
     * @param value 编辑框默认值
     * @param inputType 输入类型，默认为{@link InputType#TYPE_CLASS_TEXT}
     * @param hint 输入框的hint
     * @param consumer 成功回调
     */
    public static void showEditDialog(Activity context, String title, String value, int inputType, @Nullable String hint, @NonNull Consumer<String> consumer) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edittext, context.findViewById(R.id.setting_layout), false);
        EditText editor = view.findViewById(R.id.dialog_input);
        editor.setText(value);
        if (StringUtil.notBlank(hint)) {
            editor.setHint(hint);
        }
        editor.setInputType(inputType);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(context)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(title)
                // 添加布局
                .setView(view)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> {
                    String input = editor.getText().toString();
                    consumer.accept(input);
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog.show();
    }

    /**
     * 操作确认弹窗
     * @param context Context
     * @param runnable Runnable
     */
    public static void confirmDo(Activity context, Runnable runnable) {
        //创建对话框
        AlertDialog dialog2 = new AlertDialog.Builder(context)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.confirm_tip)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> runnable.run())
                .setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog2.show();
    }

    /**
     * 静态导入方法，打印
     * @param obj Object
     */
    public static void log(Object obj) {
        System.out.println(obj);
    }

    private static Dialog loading;

    /**
     * 公用加载浮窗
     * @param context 上下文
     */
    public static void loading(Context context) {
        loading = new Dialog(context, android.R.style.Theme_Material_Dialog_Alert);
        GifImageView imgView = new GifImageView(context);
        imgView.setImageDrawable(context.getDrawable(R.drawable.ic_autorenew_black_24dp));
        imgView.setMinimumHeight(180);
        imgView.setMinimumWidth(180);
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.load_rotate);
        Objects.requireNonNull(loading.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        loading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loading.setContentView(imgView);
        // back不消失
        loading.setCancelable(false);
        loading.setOnShowListener(dialog -> imgView.startAnimation(anim));
        loading.show();
    }

    public static void loaded() {
        loading.dismiss();
    }

    /**
     * 解析url参数
     * @param url Url
     * @return map
     */
    public static Map<String, String> getUrlParam(String url) {
        if (StringUtil.notBlank(url)) {
            int index = url.indexOf("?");
            String param = url.substring(index + 1);
            String[] params = param.split("&");
            Map<String, String> map = new HashMap<>();
            for (String item : params) {
                String[] kv = item.split("=");
                map.put(kv[0], kv[1]);
            }
            return map;
        } else {
            return new HashMap<>();
        }
    }

    /**
     * 计时器类
     * Created by due on 2019/4/29.
     */
    public static class TimeCounter {
        public static long run(Runnable runnable) {
            long s = System.currentTimeMillis();
            runnable.run();
            long t = System.currentTimeMillis() - s;
            System.out.println("time(ms):" + t);
            return t;
        }
    }

    /**
     * 压缩一个文件为zip文件
     * @param file 源文件
     * @param target 目标文件
     * @return File
     */
    public static File zipFile(File file, File target) {
        if (file == null || !file.isFile()) {
            return null;
        }
        if (target == null) {
            target = new File(file.getParentFile(), file.getName() + ".zip");
        }
        if (!target.exists()) {
            try {
                if (!target.createNewFile()) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try (
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(target));
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))
        ) {
            // 越高压缩效果越好
            zos.setLevel(7);
            // zip文件的注释
            zos.setComment("zip file");
            // zip内部展示的文件，前边加上文件夹层次可以放到文件夹中；文件夹名需要最后加上 “/”(文件夹分隔符)
            ZipEntry zipEntry = new ZipEntry(file.getName());
//            zipEntry.setComment(file.getName());
            zos.putNextEntry(zipEntry);// 自动关闭上一个entry
            byte[] buffer = new byte[1024000];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
            zos.finish();
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解压一个单文件压缩生成的zip，还原为单文件
     * 需要保证没有目录层次
     * @param zip zip文件
     * @param dist 解压后的文件
     * @return File
     */
    public static File unzipSingleFile(File zip, File dist) {
        if (zip == null || !zip.isFile()) {
            return null;
        }
        if (dist == null) {
            int dotIndex = zip.getName().lastIndexOf(".");
            if (dotIndex > 0) {
                dist = new File(zip.getParentFile(), zip.getName().substring(0, dotIndex));
            } else {
                dist = new File(zip.getParentFile(), zip.getName() + ".unzip");
            }
        }
        if (!dist.exists()) {
            try {
                if (!dist.createNewFile()) {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try (ZipFile zipFile = new ZipFile(zip)) {
            ZipEntry zipEntry = zipFile.entries().nextElement();
            try (
                 BufferedInputStream in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dist))
            ) {
                byte[] buffer = new byte[1024000];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
            return dist;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
