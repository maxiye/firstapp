package com.maxiye.first.part;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

/**
 * 数据库助手
 * Created by 91287 on 2019/5/26.
 */
public class NotificationTool {
    public static final String CHANNEL_1 = "1";

    public static NotificationManager getNotificationManager(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_1, "一般通知", NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setLightColor(Color.MAGENTA);
        channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        channel.enableVibration(false);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
        return notificationManager;
    }
}
