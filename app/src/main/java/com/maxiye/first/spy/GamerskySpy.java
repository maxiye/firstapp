package com.maxiye.first.spy;

import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * gamersky爬手
 * Created by due on 2018/11/22.
 */
public class GamerskySpy extends BaseSpy {
    private String questBody;

    GamerskySpy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
        questBody = webCfg.get("request_body").getAsString();
    }

    public Request buildRequest(int artId, int webPage) {
        curUrl = webPage > 1 ? String.format(urlTpl2, artId, webPage) : String.format(urlTpl, artId);
        System.out.println(curUrl);
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), String.format(questBody, artId));
        return new Request.Builder().url(curUrl).post(requestBody).build();
    }
}
