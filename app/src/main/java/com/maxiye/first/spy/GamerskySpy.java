package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtil;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * gamersky爬手
 * Created by due on 2018/11/22.
 */
final class GamerskySpy extends BaseSpy {
    private final String questBody;
    private final JsonObject headers;

    GamerskySpy(JsonObject webCfg, String web) {
        super(webCfg, web);
        questBody = webCfg.get("request_body").getAsString();
        headers = webCfg.get("headers").getAsJsonObject();
    }

    /**
     * @implNote 组装post请求，并添加请求头限定请求格式为json
     * @param artId String
     * @param webPage int
     * @return Request
     */
    @Override
    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? String.format(urlTpl2, artId, webPage) : String.format(urlTpl, artId);
        MyLog.println(curUrl);
        // app接口post调用
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), String.format(questBody, artId));
        Request.Builder builder =  new Request.Builder().url(curUrl).post(requestBody);
        for (String key : headers.keySet()) {
            builder.header(key, headers.get(key).getAsString());
        }
        return builder.build();
    }

    /**
     * @implNote 删除转义生成的反斜杠 '\'
     * @param content String
     * @return String
     */
    @Override
    String handleContent(String content) {
        return StringUtil.unescape(content);
    }

    @Override
    void handleImgInfo(HashMap<String, String> imgInfo) {
        // gif时不取真实大图地址
        if (".gif".equals(imgInfo.get("ext"))) {
            imgInfo.put("real_url", "");
        }
    }

    /**
     * @implNote 一页显示全部内容，默认第一页
     * @param artId String
     * @param page Integer
     * @return String
     */
    @Override
    public String getUrl(String artId, int page) {
        return String.format(webCfg.get("url").getAsString(), artId);
    }
}
