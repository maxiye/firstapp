package com.maxiye.first;

import android.annotation.SuppressLint;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.maxiye.first.util.MyLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
//        DevicePolicyManager mDPM = getManager(context);
        MyLog.d("deviceAdmin", intent.toString());
    }

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        super.onEnabled(context, intent);
        MyLog.d("deviceAdmin-enable", intent.toString());
    }

    @Nullable
    @Override
    public CharSequence onDisableRequested(@NonNull Context context, @NonNull Intent intent) {
        MyLog.d("deviceAdmin-deny", intent.toString());
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        super.onDisabled(context, intent);
        MyLog.d("deviceAdmin-disable", intent.toString());
    }
}