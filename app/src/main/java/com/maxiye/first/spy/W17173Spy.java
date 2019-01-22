package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;

import java.util.HashMap;

import okhttp3.Request;

/**
 * 17173爬手
 * Created by due on 2018/11/22.
 */
class W17173Spy extends BaseSpy {

    W17173Spy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
    }

    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? urlTpl2 : String.format(urlTpl, artId);
        MyLog.println(curUrl);
        return new Request.Builder()
                .url(curUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.1.0; zh-cn; BLA-AL00 Build/HUAWEIBLA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/8.9 Mobile Safari/537.36")
                .build();
    }

    @Override
    void handleImgInfo(HashMap<String, String> imgInfo) {
        //捕获的url以//（exp. //i.17173cdn.com/2fhnvk/YWxqaGBf/cms3/IMnCErbmxubneda.gif!a-3-480x.jpg）开头
        String url = imgInfo.get("url");
        if (url.startsWith("//")) {
            imgInfo.put("url", "http:" + url);
        } else if (!url.contains("http")) {
            imgInfo.put("url", "http://" + url);
        }

    }
}
