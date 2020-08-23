package com.maxiye.first.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import android.widget.Toast;

import java.util.function.IntConsumer;

/**
 * 权限请求助手
 *
 * {@code 第37条：使用{@link java.util.EnumMap}代替序数索引数组}
 * new EnumMap<>(PermissionUtil.RequestCode.class);
 * Arrays.stream(reqs).collect(groupingBy(p -> p.reqCode, () -> new EnumMap<>(RequestCode.class), toSet()))
 * @author due
 * @date 2018/5/7
 */
public class PermissionUtil {
    /**
     * 性能影响
     * {@code 第34条：用枚举替换常量}
     * {@code 第35条：使用实例域来替换序数} code
     * 可以在 <b>特定于常量的主体</b> 覆盖抽象方法
     * public static final int CALL = 200;
     * public static final int STORAGE_READ = 201;
     * public static final int CAPTURE = 202;
     * public static final int STORAGE_WRITE = 203;
     */
    public enum RequestCode {
        /**
         * 各种权限请求code
         */
        CALL(200),
        STORAGE_READ(201),
        CAPTURE(202),
        STORAGE_WRITE(203);

        private final int code;

        RequestCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    /**
     * 权限同意后的回调方法接口
     */
    private static IntConsumer cb;

    @SuppressWarnings("UnusedParameters")
    public static void res(@NonNull Activity activity, int reqCode, @NonNull String[] pers, @NonNull int[] res) {
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
            cb.accept(res[0]);
        } else {
            Toast.makeText(activity, "权限被拒绝", Toast.LENGTH_SHORT).show();
        }
    }
    public static void req(@NonNull Activity activity, @NonNull String[] permissions, @NonNull RequestCode requestCode, IntConsumer callback) {
        cb = callback;
        activity.requestPermissions(permissions, requestCode.getCode());
    }
}
