package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtil;

import java.util.HashMap;

import okhttp3.Request;

/**
 * 17173爬手
 * Created by due on 2018/11/22.
 */
final class W17173Spy extends BaseSpy {

    W17173Spy(JsonObject webCfg, String web) {
        super(webCfg, web);
    }

    /**
     * @implNote 添加请求头，限制移动页面模式
     * @param artId String
     * @param webPage int
     * @return Request
     */
    @Override
    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? urlTpl2 : String.format(urlTpl, artId);
        MyLog.println(curUrl);
        return new Request.Builder()
                .url(curUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.1.0; zh-cn; BLA-AL00 Build/HUAWEIBLA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/8.9 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                .build();
    }

    /**
     * @implNote 处理爬取的不规则url，捕获的url以//（exp. //i.17173cdn.com/2fhnvk/YWxqaGBf/cms3/IMnCErbmxubneda.gif!a-3-480x.jpg）开头
     * @param imgInfo HashMap
     */
    @Override
    void handleImgInfo(HashMap<String, String> imgInfo) {
        String url = imgInfo.get("url");
        if (StringUtil.notBlank(url)) {
            if (url.startsWith("//")) {
                imgInfo.put("url", "http:" + url);
            } else if (!url.contains("http")) {
                imgInfo.put("url", "http://" + url);
            }
        }

    }
}
