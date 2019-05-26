package com.maxiye.first.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.maxiye.first.SettingActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;

/**
 * 数据库助手
 * Created by 91287 on 2019/5/26.
 */
public class WebdavUtil {
    public static final String BASE_URL = "https://dav.jianguoyun.com/dav/maxiye/";

    private OkHttpClient client;

    public WebdavUtil(String user, String password) {
        client = new OkHttpClient.Builder()
                .authenticator(new WebdavUtil.BasicAuthenticator(user, password))
                .build();
    }

    public WebdavUtil(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
        String user = sharedPreferences.getString(SettingActivity.WEBDAV_USER, "null");
        String pwd = sharedPreferences.getString(SettingActivity.WEBDAV_PWD, "null");
        MyLog.w("WebdavUtil", user + "   " + pwd);
        client = new OkHttpClient.Builder()
                .authenticator(new WebdavUtil.BasicAuthenticator(user, pwd))
                .build();
    }

    public boolean put(String url, File file) {
        return put(url, file, null);
    }

    public boolean put(String url, byte[] data) {
        return put(url, data, null);
    }

    /**
     * 上传文件
     * @param url String
     * @param file File
     * @param contentType contentType eg. application/octet-stream
     * @return boolean
     */
    public boolean put(String url, File file, String contentType) {
        MediaType mediaType = contentType == null ? null : MediaType.parse(contentType);
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(mediaType, file))
                // .add("Expect", "100-Continue") 是否继续上传，选择是会timeout
                .headers(new Headers.Builder().build())
                .build();
        return executeForBoolean(request);
    }

    public boolean put(String url, byte[] data, String contentType) {
        MediaType mediaType = contentType == null ? null : MediaType.parse(contentType);
        RequestBody requestBody = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .headers(new Headers.Builder().add("Expect", "100-Continue").build())
                .build();
        return executeForBoolean(request);
    }

    /**
     * 请求是否成功
     * @param request Request
     * @return bool
     */
    private boolean executeForBoolean(Request request) {
        try {
            Response response = client.newCall(request).execute();
            return response.isSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载文件
     * @param url Url
     * @return InputStream
     */
    public ResponseBody get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }
        throw new FileNotFoundException("File does not exist");
    }

    public boolean delete(String url) {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        return executeForBoolean(request);
    }

    private static class BasicAuthenticator implements Authenticator {
        private String userName;
        private String password;

        BasicAuthenticator(@NonNull String userName, @NonNull String password) {
            this.userName = userName;
            this.password = password;
        }

        /**
         * Returns a request that includes a credential to satisfy an authentication challenge in {@code
         * response}. Returns null if the challenge cannot be satisfied.
         *
         * <p>The route is best effort, it currently may not always be provided even when logically
         * available. It may also not be provided when an authenticator is re-used manually in an
         * application interceptor, such as when implementing client-specific retries.
         *
         * @param route    Route
         * @param response Res
         */
        @Nullable
        @Override
        public Request authenticate(@Nullable Route route, @NonNull Response response) {
            if (response.request().header("Authorization") != null) {
                return null; // Give up, we've already attempted to authenticate.
            }

            //MyLog.w("BasicAuthenticator", "Authenticating for response: " + response);
            //MyLog.w("BasicAuthenticator", "Challenges: " + response.challenges());
            String credential = Credentials.basic(userName, password);
            return response.request().newBuilder()
                    .header("Authorization", credential)
                    .build();
        }
    }

    @SuppressWarnings("unused")
    public class AuthenticationInterceptor implements Interceptor {

        private String userName;
        private String password;

        public AuthenticationInterceptor(@NonNull String userName, @NonNull String password) {
            this.userName = userName;
            this.password = password;
        }

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request().newBuilder().addHeader("Authorization", Credentials.basic(userName, password)).build();
            return chain.proceed(request);
        }
    }
}
