package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.StringUtil;


/**
 * duowan爬手
 * Created by due on 2018/11/22.
 */
final class DuowanSpy extends BaseSpy {
    DuowanSpy(JsonObject webCfg, String web) {
        super(webCfg, web);
    }

    /**
     * @implNote 删除转义产生的反斜杠，转义后的unicode形式汉字转换会汉字
     * @param content String
     * @return string
     */
    @Override
    String handleContent(String content) {
        //title被unicode转义（exp. \u5168\u7403\u641e\u7b11GIF\u56fe）
        return StringUtil.unescape(content);
    }
}
