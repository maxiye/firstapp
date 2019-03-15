package com.maxiye.first.util;

/**
 * 字符串助手
 *
 * @author due
 * @date 2019/3/15
 */
public class StringUtils {
    public static boolean isBlank(String string) {
        return string == null || "".equals(string) || string.isEmpty();
    }
}
