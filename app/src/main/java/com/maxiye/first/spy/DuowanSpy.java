package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.GifActivity;
import com.maxiye.first.util.DBHelper;
import com.maxiye.first.util.Util;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * duowan爬手
 * Created by due on 2018/11/22.
 */
public class DuowanSpy extends BaseSpy {
    DuowanSpy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
    }

    public int insertItem(String content, GifActivity activity) {
        Matcher matcher = pt.matcher(content);
        ArrayList<String[]> list = activity.gifList;
        int count = 0;
        while (matcher.find()) {
            String ext = matcher.group(extIdx);
            String name = handleTitle(matcher.group(titleIdx)) + ext;
            String realUrl = realUrlIdx == -1 ? "" : matcher.group(realUrlIdx);
            realUrl = realUrl == null ? "" : realUrl;
            String gifUrl = matcher.group(urlIdx);
            System.out.println("title: " + name + "；url: " + gifUrl + "；ext: " + ext + "；realUrl: " + realUrl);
            name = Util.unicode2Chinese(name);//title被unicode转义（exp. \u5168\u7403\u641e\u7b11GIF\u56fe）
            gifUrl = gifUrl.replace("\\", "");
            String[] gifInfo = new String[]{gifUrl, name, ext, realUrl};
            list.add(gifInfo);
            activity.saveDbGifList(DBHelper.TB_IMG_WEB_ITEM, gifInfo);
            ++count;
        }
        return count;
    }
}
