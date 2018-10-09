package com.maxiye.first.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 接口请求助手
 * Created by due on 2018/10/8.
 */
public class ApiUtil {
    private static ApiUtil instance;
    private OkHttpClient okHttpClient = new OkHttpClient();
    private String appKey = "37185";
    private String sign = "0c74aa000b3b57398e386b872ab67412";
    private String exchangeRateApi = "http://api.k780.com/?app=finance.rate&scur=%s&tcur=%s&appkey=%s&sign=%s";
    private String weatherApi = "http://api.k780.com/?app=weather.future&weaid=%s&&appkey=%s&sign=%s";

    public static ApiUtil getInstance() {
        if (instance == null) {
            synchronized (ApiUtil.class) {
                // 注意：里面的判断是一定要加的，否则出现线程安全问题
                if (instance == null) {
                    instance = new ApiUtil();
                }
            }
        }

        return instance;
    }

    private String callApi(String url) {
        String ret = "";
        Log.w("ApiUtil_url:", url);
        try {
            Response response = okHttpClient.newCall(new Request.Builder().url(url).build()).execute();
            assert response.body() != null;
            ret = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String getExchangeRate(String scur, String tcur) {
        String url = String.format(exchangeRateApi, scur, tcur, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (!ret.equals("") && jsonObject.get("success").getAsString().equals("1")) {
            JsonObject retObj = jsonObject.get("result").getAsJsonObject();
            ret = "状态：" + retObj.get("status").getAsString() +
                    "\r\n原币种：" + retObj.get("scur").getAsString() +
                    "\r\n目标币种：" + retObj.get("tcur").getAsString() +
                    "\r\n兑换类型：" + retObj.get("ratenm").getAsString() +
                    "\r\n汇率：" + retObj.get("rate").getAsString() +
                    "\r\n更新时间：" + retObj.get("update").getAsString();
        }
        return ret;
    }

    public List<String[]> getWeather(String city, File cacheDir) {
        String url = String.format(weatherApi, city, appKey, sign);
        String retTmp = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(retTmp, JsonObject.class);
        List<String[]> list = new ArrayList<>();
        if (!retTmp.equals("") && jsonObject.get("success").getAsString().equals("1")) {
            JsonArray retArr = jsonObject.get("result").getAsJsonArray();
            JsonObject retObj;
            StringBuilder ret = new StringBuilder();
            JsonObject first = retArr.get(0).getAsJsonObject();
            ret.append(first.get("citynm").getAsString()).append(" - ")
                    .append(first.get("cityno").getAsString()).append(" - ")
                    .append(first.get("cityid").getAsString());
            list.add(new String[]{ret.toString(), "", ""});
            for (int i = 0; i < retArr.size(); i++) {
                retObj = retArr.get(i).getAsJsonObject();
                String[] tmp = new String[3];
                ret = new StringBuilder();
                ret.append(retObj.get("days").getAsString()).append(" ").append(retObj.get("week").getAsString())
                        .append("           ").append(retObj.get("weather").getAsString())
                        .append("   ").append(retObj.get("temperature").getAsString())
                        .append("\r\n风：").append(retObj.get("wind").getAsString())
                        .append("             ").append(retObj.get("winp").getAsString())
                        .append("\r\n最高温度：").append(retObj.get("temp_high").getAsString())
                        .append("                    最低温度：").append(retObj.get("temp_low").getAsString());
                tmp[0] = ret.toString();
                String imgUrl = retObj.get("weather_icon").getAsString();
                tmp[1] = NetworkUtil.download(imgUrl, new File(cacheDir, getWeatherImgName(imgUrl)));
                String imgUrl1 = retObj.get("weather_icon1").getAsString();
                tmp[2] = NetworkUtil.download(imgUrl1, new File(cacheDir, getWeatherImgName(imgUrl1)));
                list.add(tmp);
            }
        } else {
            list.add(new String[]{retTmp, "", ""});
        }
        return list;
    }

    private String getWeatherImgName(String url) {
        String[] parts = url.split("/");
        return "weather-" + parts[parts.length - 2] + "-" + parts[parts.length - 1];
    }
}
