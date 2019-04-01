package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * 爬手
 *
 * @author due
 * @date 2018/11/21
 */
public class BaseSpy {

    private static final Pattern TITLE_PATTERN = Pattern.compile("[\r\n\\s\t\\\\/]");
    private static final HashMap<String, Pattern> PATTERN_CACHES = new HashMap<>(33);

    String webName;
    JsonObject webCfg;
    private int urlIdx,extIdx,titleIdx,realUrlIdx;
    public String curUrl;
    String urlTpl, urlTpl2;
    private Pattern pattern;
    boolean modeFlg;

    BaseSpy(JsonObject webCfg, boolean flg) {
        modeFlg = !flg;
        applyCfg(webCfg);
    }

    /**
     * 获取缓存的Pattern，无并发
     * @param regex String
     * @return Pattern
     */
    private Pattern getPattern(String regex) {
        if (!PATTERN_CACHES.containsKey(regex)) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            PATTERN_CACHES.put(regex, pattern);
            return pattern;
        }
        return PATTERN_CACHES.get(regex);
    }

    /**
     * 内存回收
     */
    public void close() {
        PATTERN_CACHES.clear();
    }

    final void applyCfg(JsonObject webCfg) {
        modeFlg = !modeFlg;
        this.webCfg = webCfg;
        JsonObject regObj = this.webCfg.getAsJsonObject("img_reg");
        urlIdx = regObj.get("img_url_idx").getAsInt();
        extIdx = regObj.get("img_ext_idx").getAsInt();
        titleIdx = regObj.get("img_title_idx").getAsInt();
        realUrlIdx = regObj.get("img_real_url_idx") != null ? regObj.get("img_real_url_idx").getAsInt() : -1;
        urlTpl = this.webCfg.get("img_web").getAsString();
        urlTpl2 = this.webCfg.get("img_web_2nd").getAsString();
        pattern = getPattern(regObj.get("reg").getAsString());
    }

    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? String.format(urlTpl2, artId, webPage) : String.format(urlTpl, artId);
        MyLog.println(curUrl);
        return new Request.Builder().url(curUrl).build();
    }

    /**
     * 匹配内容
     * @param content String
     * @return Matcher
     */
    public Matcher match(String content) {
        return pattern.matcher(handleContent(content));
    }

    public HashMap<String, String> findItem(Matcher matcher) {
        String ext = matcher.group(extIdx);
        String name = handleTitle(matcher.group(titleIdx)) + ext;
        String realUrl = realUrlIdx == -1 ? "" : matcher.group(realUrlIdx);
        realUrl = realUrl == null ? "" : realUrl;
        String gifUrl = matcher.group(urlIdx);
        MyLog.println("title: " + name + "；url: " + gifUrl + "；ext: " + ext + "；realUrl: " + realUrl);
        HashMap<String, String> imgInfo = new HashMap<>(4);
        imgInfo.put("url", gifUrl);
        imgInfo.put("title", name);
        imgInfo.put("ext", ext);
        imgInfo.put("real_url", realUrl);
        handleImgInfo(imgInfo);
        return imgInfo;
    }

    String handleContent(String content) {
        return content;
    }

    void handleImgInfo(HashMap<String, String> imgInfo) {}

    private String handleTitle(String title) {
        boolean isNull = title == null || StringUtils.isBlank(title = TITLE_PATTERN.matcher(title).replaceAll(""));
        if (isNull) {
            title = UUID.randomUUID().toString();
        }
        return title;
    }

    /**
     * 获取文章标题
     * @param okHttpClient okHttpClient
     * @param artId 文字id
     */
    @SuppressWarnings("unused")
    public final String getNewTitle(OkHttpClient okHttpClient, String artId) {
        String content = "";
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
        return getNewTitle(content);
    }

    /**
     * 获取文章标题
     * @param content 文字内容
     */
    @SuppressWarnings("WeakerAccess")
    public final String getNewTitle(String content) {
        JsonObject regObj = webCfg.getAsJsonObject("title_reg");
        int titleIdx = regObj.get("title_idx").getAsInt();
        Pattern pattern1 = getPattern(regObj.get("reg").getAsString());
        if (!StringUtils.isBlank(content)) {
            Matcher mt = pattern1.matcher(handleContent(content));
            if (mt.find()) {
                return mt.group(titleIdx);
            }
        }
        return null;
    }

    /**
     * 获取最新的内容
     * @param okHttpClient OkHttpClient
     * @return String[]
     */
    public final String[] getNewArticle(OkHttpClient okHttpClient) {
        String url = webCfg.get("spy_root").getAsString();
        JsonObject regObj = webCfg.getAsJsonObject("img_web_reg");
        int artIdIdx = regObj.get("art_id_idx").getAsInt();
        int titleIdx = regObj.get("title_idx").getAsInt();
        Pattern pattern1 = getPattern(regObj.get("reg").getAsString());
        try {
            Request req = new Request.Builder().url(url).build();
            ResponseBody responseBody = okHttpClient.newCall(req).execute().body();
            assert responseBody != null;
            String content = responseBody.string();
            Matcher mt = pattern1.matcher(content);
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
