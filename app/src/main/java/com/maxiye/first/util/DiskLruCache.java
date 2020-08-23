package com.maxiye.first.util;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.LinkedHashMap;


/**
 * 最近使用缓存工具
 * {@code 第3条：使用私有构造器或者枚举类型来强化Singleton属性}
 * {@code 第7条：消除过时的对象引用 缓存是内存泄露的另一个常见来源}
 * {@code 第17条：使可变性最小化}
 * {@code 第18条：组合优先于继承(继承基类)} 组合与转发
 * 被称为包装类，是因为每个InstrumentedSet实例都包装了另一个Set实例。
 * 这种模式也被称为装饰者模式[Gamma95]因为InstrumentedSet类通过添加计数功能“装饰”了一个Set实例。
 * 有时组合和转发放在一起也会被简单地称为代理。从技术角度来说，这并不是代理，除非包装者对象（wrapper class）将其自己传给被包装对象[Lieberman86; Gamma95]。
 * {@code 第41条：用标记接口定义类型} 三个标记接口: {@link java.util.RandomAccess}、{@link Cloneable}、{@link Serializable}
 * 这些接口我们不用实现任何的方法，它们的作用就是当某个类实现这个接口的时候,我们就认为这个类拥有了接口标记的某种功能。
 * 考虑使用标记接口替换 {@link java.lang.annotation.ElementType#TYPE} 目标的注解
 * @author due
 * @date 2018/7/12
 */
public class DiskLruCache implements Serializable {
    private final LinkedHashMap<String, String> map;
    private final int capacity;
    private long now = 0;
    private final String keyword;
    private final Context context;

    private DiskLruCache(String keyword, int capacity, Context context) {
        this.keyword = keyword;
        this.capacity = capacity;
        this.context = context;
        map = new LinkedHashMap<>(100, 0.75f, true);
    }

    /**
     * 获取缓存实例
     * {@code 第1条：考虑用静态方法而不是构造器}
     * @param context Context
     * @param keyword String
     * @return DiskLruCache
     */
    public static DiskLruCache newInstance(Context context, String keyword) {
        DiskLruCache diskLRUCache = new DiskLruCache(keyword, capacity(keyword), context);
        diskLRUCache.unserialize();
        return diskLRUCache;
    }

    /**
     * 根据内容设置容量
     * @param keyword String
     * @return int
     */
    private static int capacity(@NonNull String keyword) {
        int capacity;
        switch (keyword) {
            case "bitmap":
                capacity = 50 << 10 << 10;
                break;
            case "gif":
                capacity = 400 << 10 << 10;
                break;
            default:
                capacity = 100 << 10 << 10;
                break;
        }
        return capacity;
    }

    public void serialize() {
        File diskLru = new File(context.getCacheDir(), "diskLru_" + keyword);
        try (FileWriter fw = new FileWriter(diskLru)) {
            map.put("size", String.valueOf(now));
            String json = new Gson().toJson(map);
            map.remove("size");
            fw.write(json);
            MyLog.w("DiskLRU:serialize", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unserialize() {
        File diskLruFile = new File(context.getCacheDir(), "diskLru_" + keyword);
        if (diskLruFile.exists()) {
            try (FileReader fr = new FileReader(diskLruFile)) {
                JsonObject jsonObject = new Gson().fromJson(fr, JsonObject.class);
                if (jsonObject != null) {
                    now = Integer.parseInt(jsonObject.remove("size").getAsString());
                    for (String key: jsonObject.keySet()) {
                        map.put(key, jsonObject.get(key).getAsString());
                    }
                    MyLog.w("DiskLRU:newInstance", "now:" + now);
                } else {
                    MyLog.w("DiskLRU:newInstance", "No Serialize File");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public File get(String key) {
        String val = map.get(key);
//        MyLog.w("DiskLRU:get", "key:" + key + ";val:" + val + ";now:" + now);
        if (val != null) {
            File f = new File(context.getCacheDir(), val);
            if (f.exists() && f.isFile()) {
                return f;
            } else {
                map.remove(key);
                clear();
            }
        }
        return null;
    }

    public synchronized void put(String key, String value, long fzise) {
        removeEldest();
        now += fzise;
//        MyLog.w("DiskLRU:put", "key:" + key + ";val:" + value + ";size:" + fzise + ";now:" + now);
        map.put(key, value);
    }

    private void removeEldest() {
        while (now > capacity) {
            remove(map.entrySet().iterator().next().getKey());
        }
    }

    @SuppressWarnings("WeakerAccess")
    public void clear() {
        MyLog.w("DiskLRU:clear", "now:" + now);
        CacheUtil.clearAllCache(context);
        now = 0;
        map.clear();
    }

    @SuppressWarnings("WeakerAccess")
    public void remove(String key) {
        String val = map.remove(key);
        if (val != null) {
            File f = new File(context.getCacheDir(), val);
            long len = f.length();
            if (f.isFile() && f.delete()) {
                now -= len;
            }
        }
//        MyLog.w("DiskLRU:remove", "string:" + toString() + "key:" + key + ";now:" + now);
    }

    /**
     * 检查是否不存在缓存
     * @param key string
     * @return boolean
     */
    public boolean isNotExists(String key) {
        return !map.containsKey(key);
    }
}
