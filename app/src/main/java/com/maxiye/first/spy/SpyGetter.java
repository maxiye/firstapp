package com.maxiye.first.spy;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;

/**
 * 爬虫助手
 * {@code 第23条：优先使用类层次，而不是标签类}
 * @author due
 * @date 2018/11/22
 */
public class SpyGetter {

    private static BaseSpy cacheSpy;

    private static String cacheKey;

    /**
     * 获取相应爬取器
     * {@code 第5条：优先使用依赖注入而不是硬连接资源}
     * {@code 第17条：使可变性最小化}
     * @param web String web
     * @param type String 图片类型
     * @param webCfg JsonObject spy配置json
     * @return BaseSpy
     */
    public static BaseSpy getSpy(String web, String type, JsonObject webCfg) {
        // 增加一层缓存
        String uniqKey = web + type;
        if (uniqKey.equals(cacheKey)) {
            return cacheSpy;
        }
        MyLog.w("Spygetter-getSpy", uniqKey);
        switch (web) {
            case "gamersky":
                cacheSpy = new GamerskySpy(webCfg, web);
                break;
            case "duowan":
                cacheSpy = new DuowanSpy(webCfg, web);
                break;
            case "yxdown":
                cacheSpy = new YxdownSpy(webCfg, web);
                break;
            case "17173":
                cacheSpy = new W17173Spy(webCfg, web);
                break;
            case "ali213":
                cacheSpy = new Ali213Spy(webCfg, web);
                break;
            default:
                cacheSpy = new BaseSpy(webCfg, web);
                break;
        }
        cacheKey = uniqKey;
        return cacheSpy;
    }
}
