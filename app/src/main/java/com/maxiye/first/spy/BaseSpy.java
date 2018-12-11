package com.maxiye.first.spy;

import android.util.Log;

import com.google.gson.JsonObject;
import com.maxiye.first.GifActivity;
import com.maxiye.first.util.DBHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * 爬手
 * Created by due on 2018/11/21.
 */
public class BaseSpy {
    JsonObject webCfg;
    int urlIdx,extIdx,titleIdx,realUrlIdx;
    public String curUrl;
    String urlTpl, urlTpl2;
    Pattern pt;
    boolean modeFlg;

    BaseSpy(JsonObject web_cfg, boolean flg) {
        modeFlg = !flg;
        applyCfg(web_cfg);
    }

    void applyCfg(JsonObject web_cfg) {
        modeFlg = !modeFlg;
        webCfg = web_cfg;
        JsonObject regObj = webCfg.getAsJsonObject("img_reg");
        urlIdx = regObj.get("img_url_idx").getAsInt();
        extIdx = regObj.get("img_ext_idx").getAsInt();
        titleIdx = regObj.get("img_title_idx").getAsInt();
        realUrlIdx = regObj.get("img_real_url_idx") != null ? regObj.get("img_real_url_idx").getAsInt() : -1;
        urlTpl = webCfg.get("img_web").getAsString();
        urlTpl2 = webCfg.get("img_web_2nd").getAsString();
        pt = Pattern.compile(regObj.get("reg").getAsString(), Pattern.CASE_INSENSITIVE);
    }

    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? String.format(urlTpl2, artId, webPage) : String.format(urlTpl, artId);
        System.out.println(curUrl);
        return new Request.Builder().url(curUrl).build();
    }

    public int insertItem(String content, GifActivity activity) {
        Matcher matcher = pt.matcher(content);
        ArrayList<String[]> list = activity.gifList;
        int count = 0;
        while (matcher.find()) {
            String ext = matcher.group(extIdx);
            String name = handleTitle(matcher.group(titleIdx)) + ext;
            String realUrl = realUrlIdx == -1 ? "" : matcher.group(realUrlIdx);
            realUrl = realUrl == null ? "" : realUrl;
            String gifUrl = matcher.group(urlIdx);
            System.out.println("title: " + name + "；url: " + gifUrl + "；ext: " + ext + "；realUrl: " + realUrl);
            String[] gifInfo = new String[]{gifUrl, name, ext, realUrl};
            list.add(gifInfo);
            activity.saveDbGifList(DBHelper.TB_IMG_WEB_ITEM, gifInfo);
            ++count;
        }
        return count;
    }

    String handleTitle(String group) {
        boolean notNull = group != null && !group.replaceAll("[\r\n\\s\t]", "").equals("");
        String title = notNull ? group : UUID.randomUUID().toString();
        return title.replaceAll("[\r\n\\s\t]", "");
    }

    /**
     * 获取文章标题
     * @param okHttpClient okHttpClient
     * @param content 文字内容
     * @param artId 文字id
     */
    public void getNewTitle(OkHttpClient okHttpClient, String content, String artId) {
        Log.w("getNewTitle", "获取标题...");
        JsonObject regObj = webCfg.getAsJsonObject("title_reg");
        int titleIdx = regObj.get("title_idx").getAsInt();
        Pattern pt = Pattern.compile(regObj.get("reg").getAsString(), Pattern.CASE_INSENSITIVE);
        if (content == null) {
            content = "";
            try {
                ResponseBody responseBody = okHttpClient
                        .newCall(buildRequest(artId, 1))
                        .execute()
                        .body();
                assert responseBody != null;
                content = new String(responseBody.bytes(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Matcher mt = pt.matcher(content);
        String title;
        if (mt.find()) {
            GifActivity.setTitle(title = mt.group(titleIdx));
            Log.w("getNewTitle", title);
        }
        Log.w("getNewTitleErr", "Null");
    }

    public String getNewArtId(OkHttpClient okHttpClient) {
        Log.w("getNewArtId", "获取最新内容……");
        String url = webCfg.get("spy_root").getAsString();
        JsonObject regObj = webCfg.getAsJsonObject("img_web_reg");
        int artIdIdx = regObj.get("art_id_idx").getAsInt();
        int titleIdx = regObj.get("title_idx").getAsInt();
        Pattern pt = Pattern.compile(regObj.get("reg").getAsString(), Pattern.CASE_INSENSITIVE);
        try {
            Request req = new Request.Builder().url(url).build();
            ResponseBody responseBody = okHttpClient.newCall(req).execute().body();
            assert responseBody != null;
            String content = new String(responseBody.bytes(), "utf-8");
            Matcher mt = pt.matcher(content);
            if (mt.find()) {
                String title;
                GifActivity.setTitle(title = mt.group(titleIdx));
                Log.w("getNewArtId", title);
                return mt.group(artIdIdx);
            } else {
                System.out.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
