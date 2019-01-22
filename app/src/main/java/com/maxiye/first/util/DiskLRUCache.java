package com.maxiye.first.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;


/**
 * 数据库助手
 * Created by due on 2018/7/12.
 */
public class DiskLRUCache extends LinkedHashMap<String, String> {
    private static final long serialVersionUID = 2244286267665222070L;
    private final int capacity;
    private long now = 0;
    private String keyword;
    private final Context context;

    private DiskLRUCache(int capacity, Context context) {
        super(1000, 1, true);
        this.capacity = capacity;
        this.context = context;
    }

    public static DiskLRUCache getInstance(Context context, String keyword) {
        int capacity;
        switch (keyword) {
            case "bitmap":
                capacity = 50 * 1024 * 1024;
                break;
            case "gif":
                capacity = 400 * 1024 * 1024;
                break;
            default:
                capacity = 100 * 1024 * 1024;
                break;
        }
        File diskLru = new File(context.getCacheDir(), "diskLru_" + keyword);
        DiskLRUCache diskLRUCache = new DiskLRUCache(capacity, context);
        if (diskLru.exists()) {
            try (FileReader fr = new FileReader(diskLru)) {
                JsonObject jsonObject = new Gson().fromJson(fr, JsonObject.class);
                for (String key: jsonObject.keySet()) {
                    diskLRUCache._put(key, jsonObject.get(key).getAsString());
                }
                diskLRUCache.now = Integer.parseInt(jsonObject.get("size").getAsString());
                MyLog.w("DiskLRU:getInstance", "now:" + diskLRUCache.now);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        diskLRUCache.keyword = keyword;
        return diskLRUCache;
    }

    public void serialize() {
        File diskLru = new File(context.getCacheDir(), "diskLru_" + keyword);
        try (FileWriter fw = new FileWriter(diskLru)) {
            put("size", now + "");
            String json = new Gson().toJson(this);
            _remove("size");
            fw.write(json);
            MyLog.w("DiskLRU:serialize", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File get(String key) {
        String val = super.get(key);
//        MyLog.w("DiskLRU:get", "key:" + key + ";val:" + val + ";now:" + now);
        if (val != null) {
            File f = new File(context.getCacheDir(), val);
            if (f.exists() && f.isFile()) {
                return f;
            } else {
                super.remove(key);
                clear();
            }
        }
        return null;
    }

    public synchronized void put(String key, String value, long fzise) {
        removeEldest();
        now += fzise;
//        MyLog.w("DiskLRU:put", "key:" + key + ";val:" + value + ";size:" + fzise + ";now:" + now);
        super.put(key, value);
    }

    private void removeEldest() {
        while (now > capacity) {
            remove(entrySet().iterator().next().getKey());
        }
    }

    @Override
    public void clear() {
        MyLog.w("DiskLRU:clear", "now:" + now);
        CacheUtil.clearAllCache(context);
        now = 0;
        super.clear();
    }

    @Override
    public String remove(Object key) {
        String val = super.remove(key);
        if (val != null) {
            File f = new File(context.getCacheDir(), val);
            long len = f.length();
            if (f.isFile() && f.delete()) {
                now -= len;
            }
        }
//        MyLog.w("DiskLRU:remove", "string:" + toString() + "key:" + key + ";now:" + now);
        return val;
    }

    private void _put(String key, String value) {
        super.put(key, value);
    }

    @SuppressWarnings("SameParameterValue")
    private void _remove(String key) {
        super.remove(key);
    }
}
