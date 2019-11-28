package com.maxiye.first;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.Util.TimeCounter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;

import static com.maxiye.first.util.Util.log;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.firstapp.com.first", appContext.getPackageName());
    }

    @Test
    public void notifyTest() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getTargetContext();
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("1", "一般通知", NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setLightColor(Color.MAGENTA);
        channel.setShowBadge(true);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(appContext, "1")
                .setSmallIcon(R.drawable.ic_cloud_done_black_24dp)
                .setContentTitle(appContext.getText(R.string.backup_db))
                .setAutoCancel(true)
                .setOngoing(true)// 不能滑动关闭
                .setProgress(100, 0, true)
                .build();
        notificationManager.notify(1, notification);
        Thread.sleep(6000);
        Notification notification2 = new Notification.Builder(appContext, "1")
                .setSmallIcon(R.drawable.ic_cloud_done_black_24dp)
                .setContentTitle(appContext.getText(R.string.backup_db))
                .setContentText(appContext.getText(R.string.success))
                .setAutoCancel(true)
                .setTimeoutAfter(2000)
                .build();
        notificationManager.notify(1, notification2);
        Thread.sleep(10000);
    }

    @Test
    public void HashKeyTest() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "bitmap");
        File[] fileList = dir.listFiles(file -> file.isFile() && file.length() > 1024);
        int count = fileList.length;
        long t1 = TimeCounter.run(() -> {
            HashMap<File, Integer> name2IdMap = new HashMap<>(count);
            for (File f : fileList) {
                String name = f.getName();
                int pos = name.indexOf("_");
                int id = Integer.MAX_VALUE;
                if (pos < 6 && pos > 0) {
                    id = Integer.valueOf(name.substring(0, pos));
                }
                name2IdMap.put(f, id);
            }
            Arrays.sort(fileList, (f1, f2) -> {
                int id1 = name2IdMap.get(f1);
                int id2 = name2IdMap.get(f2);
                return Integer.compare(id1, id2);
            });
        });
        long t2 = TimeCounter.run(() -> {
            HashMap<String, Integer> name2IdMap = new HashMap<>(count);
            for (File f : fileList) {
                String name = f.getName();
                int pos = name.indexOf("_");
                int id = Integer.MAX_VALUE;
                if (pos < 6 && pos > 0) {
                    id = Integer.valueOf(name.substring(0, pos));
                }
                name2IdMap.put(name, id);
            }
            Arrays.sort(fileList, (f1, f2) -> {
                int id1 = name2IdMap.get(f1.getName());
                int id2 = name2IdMap.get(f2.getName());
                return Integer.compare(id1, id2);
            });
        });
        Log.w("ttt1", t1 + "");
        Log.w("ttt2", t2 + "");
    }

    @Test
    public void apiListTest() {
        log(ApiUtil.getInstance().apiList());
    }

    @Test
    public void dateTimeFormatterTest() {
        log(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        log(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
        log(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
//        log(LocalDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        log(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        log(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
//        log(LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE));
//        log(LocalDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        log(LocalDateTime.now().format(DateTimeFormatter.ISO_TIME));
        log(LocalDateTime.now().format(DateTimeFormatter.ISO_ORDINAL_DATE));
        log(LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE));
//        log(LocalDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
//        log(LocalDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
    }
}
