package com.maxiye.first.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 数据库助手
 * Created by due on 2018/8/6.
 */
public class NetworkUtil {

    private ConnectivityManager.NetworkCallback netCB;
    private ConnectivityManager connMgr;

    public NetworkUtil(Context context) {
        connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
    }

    public NetworkUtil(Context context, ConnectivityManager.NetworkCallback networkCallback) {
        this(context);
        watch(networkCallback);
    }

    /**
     * 判断网络是否连接
     * @param context contect
     * @return boolean
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public static boolean isGprs(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public void watch(ConnectivityManager.NetworkCallback networkCallback) {
        if (networkCallback != null) {
            netCB = networkCallback;
            connMgr.registerDefaultNetworkCallback(netCB);
        }
    }

    public void watch() {
        watch(netCB);
    }

    public void unwatch() {
        if (netCB != null) {
            connMgr.unregisterNetworkCallback(netCB);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (netCB != null) connMgr.unregisterNetworkCallback(netCB);
        super.finalize();
    }
}
