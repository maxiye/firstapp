package com.maxiye.first.spy;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtils;

import okhttp3.Request;

/**
 * ali213爬手
 * @author due
 * Created by due on 2018/11/22.
 */
final class Ali213Spy extends BaseSpy {
    private final JsonObject headers;

    Ali213Spy(JsonObject webCfg, String web) {
        super(webCfg, web);
        headers = webCfg.get("headers").getAsJsonObject();
    }

    /**
     * 组装请求
     * @implNote 添加请求header，
     * @param artId String
     * @param webPage int
     * @return Request
     */
    @NonNull
    @Override
    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? String.format(urlTpl2, artId, webPage) : String.format(urlTpl, artId);
        MyLog.println(curUrl);
        Request.Builder builder =  new Request.Builder().url(curUrl).get();
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
        return StringUtils.unescape(content);
    }

    /**
     * @implNote 只有一页内容，默认第一页
     * @param artId String
     * @param page Integer
     * @return String
     */
    @Override
    public String getUrl(String artId, Integer page) {
        return String.format(webCfg.get("url").getAsString(), artId);
    }
}
