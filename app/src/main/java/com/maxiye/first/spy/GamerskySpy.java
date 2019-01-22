package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * gamersky爬手
 * Created by due on 2018/11/22.
 */
class GamerskySpy extends BaseSpy {
    private final String questBody;
    private final JsonObject headers;

    GamerskySpy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
        questBody = webCfg.get("request_body").getAsString();
        headers = webCfg.get("headers").getAsJsonObject();
    }

    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? String.format(urlTpl2, artId, webPage) : String.format(urlTpl, artId);
        MyLog.println(curUrl);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), String.format(questBody, artId));//app接口post调用
        Request.Builder builder =  new Request.Builder().url(curUrl).post(requestBody);
        for (String key : headers.keySet()) {
            builder.header(key, headers.get(key).getAsString());
        }
        return builder.build();
    }

    @Override
    public String getUrl(String artId, Integer page) {
        return String.format(webCfg.get("url").getAsString(), artId);
    }
}
