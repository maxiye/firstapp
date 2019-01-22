package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.GifActivity;
import com.maxiye.first.util.MyLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    String webName;
    JsonObject webCfg;
    private int urlIdx,extIdx,titleIdx,realUrlIdx;
    public String curUrl;
    String urlTpl, urlTpl2;
    private Pattern pt;
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
        MyLog.println(curUrl);
        return new Request.Builder().url(curUrl).build();
    }

    public int insertItem(String content, GifActivity activity) {
        Matcher matcher = pt.matcher(content);
        ArrayList<HashMap<String, String>> list = activity.imgList;
        int count = 0;
        while (matcher.find()) {
            String ext = matcher.group(extIdx);
            String name = handleTitle(matcher.group(titleIdx)) + ext;
            String realUrl = realUrlIdx == -1 ? "" : matcher.group(realUrlIdx);
            realUrl = realUrl == null ? "" : realUrl;
            String gifUrl = matcher.group(urlIdx);
            MyLog.println("title: " + name + "；url: " + gifUrl + "；ext: " + ext + "；realUrl: " + realUrl);
            HashMap<String, String> imgInfo = new HashMap<>();
            imgInfo.put("url", gifUrl);
            imgInfo.put("title", name);
            imgInfo.put("ext", ext);
            imgInfo.put("real_url", realUrl);
            handleImgInfo(imgInfo);
            list.add(imgInfo);
            activity.saveDbImgList(imgInfo);
            ++count;
        }
        return count;
    }

    void handleImgInfo(HashMap<String, String> imgInfo) {}

    private String handleTitle(String group) {
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
    public String getNewTitle(OkHttpClient okHttpClient, String content, String artId) {
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
                content = responseBody.string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Matcher mt = pt.matcher(content);
        if (mt.find()) {
            return mt.group(titleIdx);
        }
        return null;
    }

    public String[] getNewArticle(OkHttpClient okHttpClient) {
        String url = webCfg.get("spy_root").getAsString();
        JsonObject regObj = webCfg.getAsJsonObject("img_web_reg");
        int artIdIdx = regObj.get("art_id_idx").getAsInt();
        int titleIdx = regObj.get("title_idx").getAsInt();
        Pattern pt = Pattern.compile(regObj.get("reg").getAsString(), Pattern.CASE_INSENSITIVE);
        try {
            Request req = new Request.Builder().url(url).build();
            ResponseBody responseBody = okHttpClient.newCall(req).execute().body();
            assert responseBody != null;
            String content = responseBody.string();
            Matcher mt = pt.matcher(content);
            if (mt.find()) {
                String title = mt.group(titleIdx);
                String artId = mt.group(artIdIdx);
                return new String[]{artId, title};
            } else {
                MyLog.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUrl(String artId, Integer page) {
        if (page == null) {
            return String.format(urlTpl, artId);
        } else {
            return page > 2 ? String.format(urlTpl2, artId, page - 1) : String.format(urlTpl, artId);
        }
    }
}
