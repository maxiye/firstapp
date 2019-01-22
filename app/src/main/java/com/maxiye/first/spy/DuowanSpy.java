package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.Util;

import java.util.HashMap;

/**
 * duowan爬手
 * Created by due on 2018/11/22.
 */
class DuowanSpy extends BaseSpy {
    DuowanSpy(JsonObject webCfg, boolean modeFlg) {
        super(webCfg, modeFlg);
    }

    @Override
    void handleImgInfo(HashMap<String, String> imgInfo) {
        imgInfo.put("title", Util.unicode2Chinese(imgInfo.get("title")));//title被unicode转义（exp. \u5168\u7403\u641e\u7b11GIF\u56fe）
        imgInfo.put("url", imgInfo.get("url").replace("\\", ""));
    }
}
