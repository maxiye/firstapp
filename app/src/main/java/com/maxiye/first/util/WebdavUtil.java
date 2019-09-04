package com.maxiye.first.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.maxiye.first.SettingActivity;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
 * WebDav工具
 * Created by 91287 on 2019/5/26.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class WebdavUtil {
    public static final String BASE_URL = "https://dav.jianguoyun.com/dav/maxiye/";

    private OkHttpClient client;
    private String rawRes;

    public WebdavUtil(String user, String password) {
        client = new OkHttpClient.Builder()
                .authenticator(new WebdavUtil.BasicAuthenticator(user, password))
                .readTimeout(1200, TimeUnit.SECONDS)
                .writeTimeout(1200, TimeUnit.SECONDS)
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

    public String getRawRes() {
        return rawRes;
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
                .headers(new Headers.Builder()/*.add("Expect", "100-Continue")*/.build())
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
            rawRes = Objects.requireNonNull(response.body()).string();
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

    /**
     * 获取文件列表
     * @param url Url
     * @param depth 目录深度
     * @return ResponseBody xml
     * <d:response>
     *    <d:href>/dav/</d:href>
     *    <d:propstat>
     *       <d:prop>
     *          <d:getlastmodified>Tue, 27 Aug 2019 03:25:42 GMT</d:getlastmodified>
     *          <d:getcontentlength>0</d:getcontentlength>
     *          <d:owner>912877398@qq.com</d:owner>
     *          <d:current-user-privilege-set>
     *             <d:privilege>
     *                <d:read/>
     *             </d:privilege>
     *          </d:current-user-privilege-set>
     *          <d:getcontenttype>httpd/unix-directory</d:getcontenttype>
     *          <d:displayname>dav</d:displayname>
     *          <d:resourcetype>
     *             <d:collection/>
     *          </d:resourcetype>
     *       </d:prop>
     *       <d:status>HTTP/1.1 200 OK</d:status>
     *    </d:propstat>
     * </d:response>
     */
    public String[] list(String url, int depth) {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<D:propfind xmlns:D=\"DAV:\">\n" +
                "   <D:allprop/>\n" +
                "</D:propfind>";
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/xml"), xml);
        Request request = new Request.Builder()
                .url(url)
                .header("Depth", depth < 0 ? "infinity" : Integer.toString(depth))
                .method("PROPFIND", requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                byte[] bytes = response.body().bytes();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                Document document = documentBuilder.parse(bais);
                rawRes = new String(bytes, StandardCharsets.UTF_8);
                NodeList nodeList = document.getElementsByTagName("d:href");
                int length = nodeList.getLength();
                String[] result = new String[length];
                for (int i = 0; i < length; i++) {
                    result[i] = URLDecoder.decode(nodeList.item(i).getTextContent(), "utf-8");
                }
                return result;
            }
            rawRes = Objects.requireNonNull(response.body()).string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     * 删除文件
     * @param url Url
     * @return boolean
     */
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
