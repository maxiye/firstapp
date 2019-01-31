package com.maxiye.first.spy;

import com.google.gson.JsonObject;

/**
 * 爬虫助手
 * Created by due on 2018/11/22.
 */
public class SpyGetter {
    private BaseSpy base, gamersky, yxdown, duowan, w17173, ali213;
    public static boolean modeFlg = true;
    private static final SpyGetter instance = new SpyGetter();//饿汉

    public static SpyGetter getInstance() {
        return instance;
    }

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
            case "ali213":
                spy = ali213 == null ? ali213 = new Ali213Spy(webCfg, modeFlg) : ali213;
                break;
            default:
                spy = base == null ? base = new BaseSpy(webCfg, modeFlg) : base;
                if (spy.webName != null && !spy.webName.equals(web)) spy.modeFlg = !modeFlg;
                break;
        }
        spy.webName = web;
        if (spy.modeFlg != modeFlg)
            spy.applyCfg(webCfg);
        return spy;
    }
}
