package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.GifActivity;
import com.maxiye.first.util.DBHelper;

import java.util.ArrayList;
import java.util.regex.Matcher;

import okhttp3.Request;

/**
 * 17173爬手
 * Created by due on 2018/11/22.
 */
public class W17173Spy extends BaseSpy {

    W17173Spy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
    }

    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? urlTpl2 : String.format(urlTpl, artId);
        System.out.println(curUrl);
        return new Request.Builder()
                .url(curUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 8.1.0; zh-cn; BLA-AL00 Build/HUAWEIBLA-AL00) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/8.9 Mobile Safari/537.36")
                .build();
    }

    public int insertItem(String content, GifActivity activity) {
        Matcher matcher = pt.matcher(content);
        ArrayList<String[]> list = activity.gifList;
        int count = 0;
        while (matcher.find()) {
            String ext = matcher.group(extIdx);
            String name = handleTitle(matcher.group(titleIdx)) + ext;
            String gifUrl = "http://" + matcher.group(urlIdx);//捕获的url以//（exp. //i.17173cdn.com/2fhnvk/YWxqaGBf/cms3/IMnCErbmxubneda.gif!a-3-480x.jpg）开头，已舍弃
            System.out.println("title: " + name + "；url: " + gifUrl + "；ext: " + ext + "；realUrl: ");
            String[] gifInfo = new String[]{gifUrl, name, ext, ""};
            list.add(gifInfo);
            activity.saveDbGifList(DBHelper.TB_IMG_WEB_ITEM, gifInfo);
            ++count;
        }
        return count;
    }
}
