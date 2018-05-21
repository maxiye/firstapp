package com.maxiye.first.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * 权限请求助手
 * Created by due on 2018/5/7.
 */
public class PermissionUtil {
    public static final int PER_REQ_CALL = 200;
    public static final int PER_REQ_STORAGE_READ = 201;
    public static final int PER_REQ_CAPTURE = 202;
    public static final int PER_REQ_STORAGE_WRT = 203;
    private static CB cb;
    public static void res(@NonNull Activity activity, int reqCode, @NonNull String[] pers, @NonNull int[] res) {
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
            cb.callback();
        } else {
            Toast.makeText(activity, "权限被拒绝", Toast.LENGTH_SHORT).show();
        }
    }
    public static void req(@NonNull Activity activity, @NonNull String[] permissions, int requestCode, CB callback) {
        cb = callback;
        activity.requestPermissions(permissions, requestCode);
    }
    public interface CB {
        void callback();
    }
}
