package com.maxiye.first.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * 网络工具
 *
 * @author due
 * @date 2018/8/6
 */
@SuppressWarnings("unused")
public class NetworkUtil {

    private HashMap<Object, ConnectivityManager.NetworkCallback> callbackList = new HashMap<>(2);
    private static NetworkUtil instance;

    private NetworkUtil() {

    }

    public static NetworkUtil getInstance() {
        if (instance == null) {
            synchronized (NetworkUtil.class) {
                if (instance == null) {
                    instance = new NetworkUtil();
                }
            }
        }
        return instance;
    }

    public static synchronized void register(Context context, ConnectivityManager.NetworkCallback networkCallback) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null && networkCallback != null) {
            getInstance().callbackList.put(context, networkCallback);
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    public static synchronized void unregister(Context context) {
        ConnectivityManager.NetworkCallback networkCallback = getInstance().callbackList.remove(context);
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        checkDestroy();
    }

    private static void checkDestroy() {
        if (instance != null && instance.callbackList.isEmpty()) {
            instance = null;
        }
    }

    /**
     * 判断网络是否连接
     * @param context contect
     * @return boolean
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * 判断是否wifi
     * @param context context
     * @return boolean
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * 判断是否为gprs网络
     * @param context context
     * @return boolean
     */
    public static boolean isGprs(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    @SuppressWarnings("WeakerAccess")
    public static String download(String url, File path) {
        try {
            if (path.exists()) {
                return path.getAbsolutePath();
            }
            Request request = new Request.Builder().url(url).build();
            ResponseBody responseBody = new OkHttpClient().newBuilder()
                    .build()
                    .newCall(request)
                    .execute()
                    .body();
            assert responseBody != null;
            byte[] bytes = responseBody.bytes();
            RandomAccessFile raf = new RandomAccessFile(path, "rwd");
            raf.write(bytes);
            raf.close();
            return path.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
