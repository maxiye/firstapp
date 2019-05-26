package com.maxiye.first;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

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
}
