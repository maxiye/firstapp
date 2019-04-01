package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtils;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * ali213爬手
 * @author due
 * Created by due on 2018/11/22.
 */
class Ali213Spy extends BaseSpy {
    private final JsonObject headers;

    Ali213Spy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
        headers = webCfg.get("headers").getAsJsonObject();
    }

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

    @Override
    String handleContent(String content) {
        return StringUtils.unescape(content);
    }

    @Override
    public String getUrl(String artId, Integer page) {
        return String.format(webCfg.get("url").getAsString(), artId);
    }
}
