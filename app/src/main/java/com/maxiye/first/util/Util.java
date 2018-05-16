package com.maxiye.first.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 函数助手
 * Created by due on 2018/5/16.
 */
public class Util {

    public static String unicode2Chinese(String unicode) {
        Pattern p = Pattern.compile("\\\\u([a-f0-9]{4})");
        Matcher mt = p.matcher(unicode);
        String result = unicode;
        while (mt.find()){
            int byte1, byte2;
            String item = mt.group(1);
            byte1 = Integer.parseInt(item.substring(0, 2), 16) << 8;//必须是小端在前
            byte2 = Integer.parseInt(item.substring(2), 16);
            result = result.replace(mt.group(0), String.valueOf((char) (byte1 + byte2)));
        }
        return result;
    }
}
