package com.maxiye.first.spy;

import com.google.gson.JsonObject;

/**
 * 爬虫助手
 * Created by due on 2018/11/22.
 */
public class SpyGetter {
    private BaseSpy base, gamersky, yxdown, duowan, w17173;
    public boolean modeFlg = true;
    public BaseSpy getSpy(String web, JsonObject webCfg) {
        BaseSpy spy;
        switch (web) {
            case "gamersky":
                spy = gamersky == null ? gamersky = new GamerskySpy(webCfg, modeFlg) : gamersky;
                break;
            case "duowan":
                spy = duowan == null ? duowan = new DuowanSpy(webCfg, modeFlg) : duowan;
                break;
            case "yxdown":
                spy = yxdown == null ? yxdown = new YxdownSpy(webCfg, modeFlg) : yxdown;
                break;
            case "17173":
                spy = w17173 == null ? w17173 = new W17173Spy(webCfg, modeFlg) : w17173;
                break;
            default:
                spy = base == null ? base = new BaseSpy(webCfg, modeFlg) : base;
                break;
        }
        if (spy.modeFlg != modeFlg)
            spy.applyCfg(webCfg);
        return spy;
    }
}
