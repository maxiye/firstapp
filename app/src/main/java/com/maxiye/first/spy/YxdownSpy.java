package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.GifActivity;
import com.maxiye.first.util.DBHelper;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * yxdown爬手
 * Created by due on 2018/11/22.
 */
class YxdownSpy extends BaseSpy {
    YxdownSpy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
    }

    @Override
    public int insertItem(String content, GifActivity activity) {
        Matcher matcher = pt.matcher(content);
        ArrayList<String[]> list = activity.gifList;
        String type = webCfg.get("type").getAsString();
        int count = 0;
        while (matcher.find()) {
            String ext = type.equals("gif") ? ".gif" : matcher.group(extIdx);//部分出现jpg后缀
            String name = handleTitle(matcher.group(titleIdx)) + ext;
            String realUrl = realUrlIdx == -1 ? "" : matcher.group(realUrlIdx);
            realUrl = realUrl == null ? "" : realUrl;
            String gifUrl = matcher.group(urlIdx);
            System.out.println("title: " + name + "；url: " + gifUrl + "；ext: " + ext + "；realUrl: " + realUrl);
            String[] gifInfo = new String[]{gifUrl, name, ext, realUrl};
            list.add(gifInfo);
            activity.saveDbGifList(DBHelper.TB_IMG_WEB_ITEM, gifInfo);
            ++count;
        }
        return count;
    }
}
