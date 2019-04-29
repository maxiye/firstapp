package com.maxiye.first.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串助手
 *
 * @author due
 * @date 2019/3/15
 */
public class StringUtil {

    /**
     * 优化pattern性能
     * {@code 第6条：避免创建不必要的对象}
     * 创建一个Pattern实例是昂贵的，因为它需要将正则表达式编译成一个有限状态机。
     */
    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([a-f0-9A-F]{4})");
    private static final Pattern UNICODE_DIGIT_PATTERN = Pattern.compile("[a-f0-9A-F]{4}");
    private static final Pattern UNESCAPE_PATTERN = Pattern.compile("\\\\(?=[/\"])");
    /**
     * unicode字符串(\\uxxxx)转为中文，5倍速优化
     * {@code 第63条：注意字符串连接的性能}
     * 不要使用字符串连接操作符组合多个字符串。而是使用StringBuilder的append方法。
     * 或者，使用字符数组，或者一次处理一个字符串，而不是把它们组合起来。
     * 14ms
     * @param unicode String
     * @return String
     */
    public static String unicode2Chinese(String unicode) {
        char[] chars = unicode.toCharArray();
        char[] resultChars = new char[chars.length];
        int i = 0, j = 0;
        for (;i < chars.length;) {
            if (chars[i] == '\\' && chars[i + 1] == 'u') {
                String item = String.valueOf(chars, i + 2, 4);
                if (UNICODE_DIGIT_PATTERN.matcher(item).matches()) {
                    resultChars[j++] = (char) Integer.parseInt(item, 16);
                    i += 6;
                    continue;
                }
                resultChars[j++] = '\\';
                resultChars[j++] = 'u';
                i += 2;
                continue;
            }
            resultChars[j++] = chars[i];
            ++i;
        }
        return String.valueOf(resultChars, 0, j);
    }

    /**
     * unicode字符串(\\uxxxx)转为中文，优化1
     * 70ms
     * @param unicode String
     * @return String
     */
    @SuppressWarnings({"unused"})
    public static String unicode2ChineseOri2(String unicode) {
        Matcher mt = UNICODE_PATTERN.matcher(unicode);
        String result = unicode;
        while (mt.find()) {
            String item = mt.group(1);
            if (notBlank(item)) {
                result = result.replace(mt.group(0), String.valueOf((char) (Integer.parseInt(item, 16))));
            }
        }
        return result;
    }


    /**
     * unicode字符串(\\uxxxx)转为中文，优化前
     * 72ms
     * @param unicode String
     * @return String
     */
    @SuppressWarnings({"unused"})
    public static String unicode2ChineseOri(String unicode) {
        Matcher mt = UNICODE_PATTERN.matcher(unicode);
        String result = unicode;
        while (mt.find()) {
            int byte1, byte2;
            String item = mt.group(1);
            // 第一个byte是高位，相当于‘十位’
            byte1 = Integer.parseInt(item.substring(0, 2), 16) << 8;
            // 这是‘个位’
            byte2 = Integer.parseInt(item.substring(2), 16);
            // 位或优化性能
            result = result.replace(mt.group(0), String.valueOf((char) (byte1 | byte2)));
        }
        return result;
    }

    /**
     * 消除 ‘"’ 和 ‘/’ 转义后生成的前边的 ‘\’，以及中文unicode编码转为原字符
     * @param content string
     * @return string
     */
    public static String unescape(String content) {
        content = UNESCAPE_PATTERN.matcher(content).replaceAll("");
        content = unicode2Chinese(content);
        MyLog.w("unescape", content);
        return content;
    }

    /**
     * 判断字符串不为空
     * @param string String
     * @return boolean
     */
    public static boolean notBlank(String string) {
        return !(string == null || "".equals(string) || string.isEmpty());
    }
}
