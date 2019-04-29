package com.maxiye.first.util;

/**
 * 计时器
 * Created by due on 2019/4/29.
 */
public class TimeCounter {
    public static long run(Runnable runnable) {
        long s = System.currentTimeMillis();
        runnable.run();
        long t = System.currentTimeMillis() - s;
        System.out.println("time(ms):" + t);
        return t;
    }
}
