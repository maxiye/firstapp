package com.maxiye.first.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.PopupMenu;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.maxiye.first.R;
import com.maxiye.first.api.BjTimeActivity;
import com.maxiye.first.api.ExchangeRateActivity;
import com.maxiye.first.api.IdAddressActivity;
import com.maxiye.first.api.IpAddressActivity;
import com.maxiye.first.api.PhoneAddressActivity;
import com.maxiye.first.api.PostcodeActivity;
import com.maxiye.first.api.WeatherActivity;
import com.maxiye.first.api.WorkdayActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * 接口请求助手
 *
 * @author due
 * @date 2018/10/8
 */
public class ApiUtil {
    private static ApiUtil instance;
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final String appKey = "37185";
    private final String sign = "0c74aa000b3b57398e386b872ab67412";
    private final String successMsg = "success";
    private final String successStatus = "1";

    /**
     * 不可实例化
     * {@code 第4条：通过私有化构造器强化不可实例化的能力}
     */
    private ApiUtil() {
    }

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

    public static boolean showPopupmenu(Activity context, View view) {
        PopupMenu pMenu = new PopupMenu(context, view);
        pMenu.getMenuInflater().inflate(R.menu.test_activity_api_popupmenu, pMenu.getMenu());
        pMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            switch (itemId) {
                case R.id.weather_api:
                    context.startActivity(new Intent(context, WeatherActivity.class));
                    break;
                case R.id.exchange_rate_api:
                    context.startActivity(new Intent(context, ExchangeRateActivity.class));
                    break;
                case R.id.ip_address_api:
                    context.startActivity(new Intent(context, IpAddressActivity.class));
                    break;
                case R.id.phone_address_api:
                    context.startActivity(new Intent(context, PhoneAddressActivity.class));
                    break;
                case R.id.id_address_api:
                    context.startActivity(new Intent(context, IdAddressActivity.class));
                    break;
                case R.id.postcode_api:
                    context.startActivity(new Intent(context, PostcodeActivity.class));
                    break;
                case R.id.bj_time_api:
                    context.startActivity(new Intent(context, BjTimeActivity.class));
                    break;
                case R.id.workday_api:
                    context.startActivity(new Intent(context, WorkdayActivity.class));
                    break;
                default:
                    break;
            }
            return false;
        });
        pMenu.show();
        return true;
    }

    private String callApi(String url) {
        String ret = "";
        MyLog.w("ApiUtil_url:", url);
        try {
            ResponseBody resBody = okHttpClient.newCall(new Request.Builder().url(url).build())
                    .execute()
                    .body();
            assert resBody != null;
            ret = resBody.string();
            MyLog.w("ApiUtil_ret", ret);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String getExchangeRate(String scur, String tcur) {
        String exchangeRateApi = "http://api.k780.com/?app=finance.rate&scur=%s&tcur=%s&appkey=%s&sign=%s";
        String url = String.format(exchangeRateApi, scur, tcur, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (StringUtil.notBlank(ret) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
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
        String weatherApi = "http://api.k780.com/?app=weather.future&weaid=%s&&appkey=%s&sign=%s";
        String url = String.format(weatherApi, city, appKey, sign);
        String retTmp = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(retTmp, JsonObject.class);
        List<String[]> list = new ArrayList<>();
        if (StringUtil.notBlank(retTmp) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
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

    public String getIPAddress(String ip) {
        String iPAddressApi = "http://api.k780.com/?app=ip.get&ip=%s&appkey=%s&sign=%s&format=json";
        String url = String.format(iPAddressApi, ip, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (StringUtil.notBlank(ret) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
            try {
                JsonObject retObj = jsonObject.get("result").getAsJsonObject();
                ret = "状态：" + retObj.get("status").getAsString() +
                        "\r\nIP：" + retObj.get("ip").getAsString() +
                        "\r\nIP段开始：" + retObj.get("ip_str").getAsString() +
                        "\r\nIP段结束：" + retObj.get("ip_end").getAsString() +
                        "\r\n数字地址：" + retObj.get("inet_ip").getAsString() +
                        "\r\n数字地址段开始：" + retObj.get("inet_str").getAsString() +
                        "\r\n数字地址段结束：" + retObj.get("inet_end").getAsString() +
                        "\r\n区号：" + retObj.get("areano").getAsString() +
                        "\r\n邮编：" + retObj.get("postno").getAsString() +
                        "\r\n运营商：" + retObj.get("operators").getAsString() +
                        "\r\n归属地：" + retObj.get("att").getAsString() +
                        "\r\n详细归属地：" + retObj.get("detailed").getAsString() +
                        "\r\n归属地样式1：" + retObj.get("area_style_simcall").getAsString() +
                        "\r\n归属地样式2：" + retObj.get("area_style_areanm").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public String getPhoneAddress(String phone) {
        String phoneAddressApi = "http://api.k780.com/?app=phone.get&phone=%s&appkey=%s&sign=%s&format=json";
        String url = String.format(phoneAddressApi, phone, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (StringUtil.notBlank(ret) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
            try {
                JsonObject retObj = jsonObject.get("result").getAsJsonObject();
                ret = "状态：" + retObj.get("status").getAsString() +
                        "\r\n电话：" + retObj.get("phone").getAsString() +
                        "\r\n区号：" + retObj.get("area").getAsString() +
                        "\r\n邮编：" + retObj.get("postno").getAsString() +
                        "\r\n归属地：" + retObj.get("att").getAsString() +
                        "\r\n卡种：" + retObj.get("ctype").getAsString() +
                        "\r\npar：" + retObj.get("par").getAsString() +
                        "\r\n前缀：" + retObj.get("prefix").getAsString() +
                        "\r\n运营商：" + retObj.get("operators").getAsString() +
                        "\r\n归属地样式1：" + retObj.get("style_simcall").getAsString() +
                        "\r\n归属地样式2：" + retObj.get("style_citynm").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public String getIDAddress(String id) {
        String idAddressApi = "http://api.k780.com/?app=idcard.get&idcard=%s&appkey=%s&sign=%s&format=json";
        String url = String.format(idAddressApi, id, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (StringUtil.notBlank(ret) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
            try {
                JsonObject retObj = jsonObject.get("result").getAsJsonObject();
                ret = "状态：" + retObj.get("status").getAsString() +
                        "\r\n身份证号码：" + retObj.get("idcard").getAsString() +
                        "\r\n身份证前缀：" + retObj.get("par").getAsString() +
                        "\r\n出生年月日：" + retObj.get("born").getAsString() +
                        "\r\n性别：" + retObj.get("sex").getAsString() +
                        "\r\n归属地：" + retObj.get("att").getAsString() +
                        "\r\n区号：" + retObj.get("areano").getAsString() +
                        "\r\n邮编：" + retObj.get("postno").getAsString() +
                        "\r\n归属地样式1：" + retObj.get("style_simcall").getAsString() +
                        "\r\n归属地样式2：" + retObj.get("style_citynm").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public String getPostcode(String area) {//广东省广州市
        String postcodeApi = "http://api.k780.com/?app=life.postcode&areaname=%s&appkey=%s&sign=%s&format=json";
        String url = String.format(postcodeApi, area, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (StringUtil.notBlank(ret) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
            try {
                JsonObject retObj = jsonObject.get("result").getAsJsonObject().get("lists").getAsJsonArray().get(0).getAsJsonObject();
                ret = "地区：" + retObj.get("areanm").getAsString() +
                        "\r\n区号：" + retObj.get("areacode").getAsString() +
                        "\r\n邮编：" + retObj.get("postcode").getAsString() +
                        "\r\n地区样式2：" + retObj.get("simcall").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public String getBJTime() {
        String bjTimeApi = "http://api.k780.com/?app=life.time&appkey=%s&sign=%s&format=json";
        String url = String.format(bjTimeApi, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (StringUtil.notBlank(ret) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
            try {
                JsonObject retObj = jsonObject.get("result").getAsJsonObject();
                ret = "时间戳：" + retObj.get("timestamp").getAsString() +
                        "\r\n时间1：" + retObj.get("datetime_1").getAsString() +
                        "\r\n时间2：" + retObj.get("datetime_2").getAsString() +
                        "\r\n星期1：" + retObj.get("week_1").getAsString() +
                        "\r\n星期2：" + retObj.get("week_2").getAsString() +
                        "\r\n星期3：" + retObj.get("week_3").getAsString() +
                        "\r\n星期4：" + retObj.get("week_4").getAsString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * 判断是否为工作日或假日
     * @param dates String 20160101,20160102，最多100个
     * @return String
     */
    public String getWorkday(String dates) {
        String workdayApi = "http://api.k780.com/?app=life.workday&date=%s&appkey=%s&sign=%s&format=json";
        String url = String.format(workdayApi, dates, appKey, sign);
        String ret = callApi(url);
        JsonObject jsonObject = new Gson().fromJson(ret, JsonObject.class);
        if (StringUtil.notBlank(ret) && successStatus.equals(jsonObject.get(successMsg).getAsString())) {
            try {
                String multSeparator = ",";
                if (dates.contains(multSeparator)) {
                    JsonArray retArr = jsonObject.get("result").getAsJsonArray();
                    StringBuilder retBuilder = new StringBuilder();
                    for(int i = 0; i<retArr.size(); i++) {
                        JsonObject retTmp = retArr.get(i).getAsJsonObject();
                        retBuilder.append("日期：").append(retTmp.get("date").getAsString())
                                .append("\r\n日期类型：").append(retTmp.get("worknm").getAsString())
                                .append("\r\n星期1：").append(retTmp.get("week_1").getAsString())
                                .append("\r\n星期2：").append(retTmp.get("week_2").getAsString())
                                .append("\r\n星期3：").append(retTmp.get("week_3").getAsString())
                                .append("\r\n星期4：").append(retTmp.get("week_4").getAsString())
                                .append("\r\n备注：").append(retTmp.get("remark").getAsString())
                                .append("\r\n\r\n");
                    }
                    ret = retBuilder.toString();
                } else {
                    JsonObject retObj = jsonObject.get("result").getAsJsonObject();
                    ret = "日期：" + retObj.get("date").getAsString() +
                            "\r\n日期类型：" + retObj.get("worknm").getAsString() +
                            "\r\n星期1：" + retObj.get("week_1").getAsString() +
                            "\r\n星期2：" + retObj.get("week_2").getAsString() +
                            "\r\n星期3：" + retObj.get("week_3").getAsString() +
                            "\r\n星期4：" + retObj.get("week_4").getAsString() +
                            "\r\n备注：" + retObj.get("remark").getAsString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
