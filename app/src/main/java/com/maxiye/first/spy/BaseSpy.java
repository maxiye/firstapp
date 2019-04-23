package com.maxiye.first.spy;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtils;

import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * 爬手
 * {@code 第12条：始终要覆盖toString}
 * {@code 第15条：最小化类和成员的可访问性}
 * {@code 第16条：在公有类中使用访问方法，而不是公有域}
 * {@code 第19条：若要设计继承，则提供文档说明，否则禁止继承}
 * {@code 第50条：必要时进行防御性拷贝}
 * 保证{@link BaseSpy#webCfg}的不可变性
 * 防御性拷贝是在检查参数(条目49)的有效性之前进行的，有效性检查是在拷贝上而不是在原始实例上进行的。
 * 虽然这看起来不自然，但却是必要的。它在检查参数和拷贝参数之间的漏洞窗口期间保护类不受其他线程对参数的更改的影响。
 * 在计算机安全社区中，这称为 time-of-check/time-of-use或TOCTOU攻击[Viega01]。
 *
 * {@code 第56条：为所有已公开的API元素编写文档注释}
 * @author due
 * @date 2018/11/21
 */
public class BaseSpy {

    private static final Pattern TITLE_PATTERN = Pattern.compile("[\r\n\\s\t\\\\/]");
    private static final HashMap<String, Pattern> PATTERN_CACHES = new HashMap<>(33);
    @SuppressWarnings("unused,FieldCanBeLocal")
    private String webName;
    JsonObject webCfg;
    private int urlIdx,extIdx,titleIdx,realUrlIdx;
    String curUrl;
    String urlTpl, urlTpl2;
    private Pattern pattern;
    private static OkHttpClient okHttpClient;

    /**
     * 构造
     * @param webCfg JsonObject webCfg一旦创建后，不能再次改变，无需拷贝
     * @param web String
     */
    BaseSpy(JsonObject webCfg, String web) {
        this.webName = web;
        this.webCfg = webCfg;
        applyCfg();
    }

    /**
     * 单例请求客户端
     * @return OkHttpClient
     */
    private OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .build();
        }
        return okHttpClient;
    }
    /**
     * 获取缓存的Pattern，无并发风险
     * @param regex String
     * @return Pattern
     */
    private Pattern getPattern(String regex) {
        if (!PATTERN_CACHES.containsKey(regex)) {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            PATTERN_CACHES.put(regex, pattern);
            return pattern;
        }
        return PATTERN_CACHES.get(regex);
    }

    /**
     * 内存回收
     */
    public void close() {
        PATTERN_CACHES.clear();
    }

    /**
     * 构造器一定不能调用可覆盖方法 ,private 方法不会覆盖
     */
    private void applyCfg() {
        JsonObject regObj = webCfg.getAsJsonObject("img_reg");
        urlIdx = regObj.get("img_url_idx").getAsInt();
        extIdx = regObj.get("img_ext_idx").getAsInt();
        titleIdx = regObj.get("img_title_idx").getAsInt();
        realUrlIdx = regObj.get("img_real_url_idx") != null ? regObj.get("img_real_url_idx").getAsInt() : -1;
        urlTpl = webCfg.get("img_web").getAsString();
        urlTpl2 = webCfg.get("img_web_2nd").getAsString();
        pattern = getPattern(regObj.get("reg").getAsString());
    }

    /**
     * 请求组装
     * @implSpec 各个web实现自己的请求组装方法
     * @param artId String
     * @param webPage int
     * @return Request
     */
    public Request buildRequest(String artId, int webPage) {
        curUrl = webPage > 1 ? String.format(urlTpl2, artId, webPage) : String.format(urlTpl, artId);
        MyLog.println(curUrl);
        return new Request.Builder().url(curUrl).build();
    }

    /**
     * 匹配内容
     * @see #handleContent(String) 每个网址自己的内容过滤方法
     * @param content String
     * @return Matcher
     */
    public Matcher match(String content) {
        return pattern.matcher(handleContent(content));
    }

    /**
     * 匹配获取图片项目
     * @see #handleTitle(String) 每个网址自己的内容标题处理方法
     * @see #handleImgInfo(HashMap)  每个网址处理相关项目的内容
     * @param matcher Matcher
     * @return HashMap<String, String>
     */
    public HashMap<String, String> findItem(Matcher matcher) {
        String ext = matcher.group(extIdx);
        String name = handleTitle(matcher.group(titleIdx)) + ext;
        String realUrl = realUrlIdx == -1 ? "" : matcher.group(realUrlIdx);
        realUrl = realUrl == null ? "" : realUrl;
        String gifUrl = matcher.group(urlIdx);
        MyLog.println("title: " + name + "；url: " + gifUrl + "；ext: " + ext + "；realUrl: " + realUrl);
        HashMap<String, String> imgInfo = new HashMap<>(4);
        imgInfo.put("url", gifUrl);
        imgInfo.put("title", name);
        imgInfo.put("ext", ext);
        imgInfo.put("real_url", realUrl);
        handleImgInfo(imgInfo);
        return imgInfo;
    }

    /**
     * 处理爬取内容
     * @implSpec 各个web实现自己的过滤方法
     * @param content String
     * @return String
     */
    String handleContent(String content) {
        return content;
    }

    private String handleTitle(String title) {
        boolean isNull = title == null || !StringUtils.notBlank(title = TITLE_PATTERN.matcher(title).replaceAll(""));
        if (isNull) {
            title = UUID.randomUUID().toString();
        }
        return title;
    }

    /**
     * 处理标题
     * @implSpec 各个web实现自己的标题处理方法
     * @param imgInfo HashMap
     */
    void handleImgInfo(HashMap<String, String> imgInfo) {}

    /**
     * 获取文章标题
     *
     * {@code 第52条：明智而审慎地使用重载}
     * 重载（overloaded）方法之间的选择是静态的，而重写（overridden）方法之间的选择是动态的。
     * 当调用重写方法时，对象的编译时类型对执行哪个方法没有影响; 总是会执行“最具体(most specific)”的重写方法。
     * 将此与重载进行比较，其中对象的运行时类型对执行的重载没有影响; 选择是在编译时完成的，完全基于参数的编译时类型。
     * 总是可以为方法赋予不同的名称，而不是重载它们。
     * 最好避免重载具有相同数量参数的多个签名的方法
     * @param artId 文字id
     */
    @SuppressWarnings("unused")
    public final String getNewTitleFromArtId(String artId) {
        String content = "";
        try {
            ResponseBody responseBody = getOkHttpClient()
                    .newCall(buildRequest(artId, 1))
                    .execute()
                    .body();
            assert responseBody != null;
            content = responseBody.string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getNewTitle(content);
    }

    /**
     * 获取文章标题
     * @param content 文字内容
     */
    @Nullable
    @SuppressWarnings("WeakerAccess")
    public final String getNewTitle(String content) {
        JsonObject regObj = webCfg.getAsJsonObject("title_reg");
        int titleIdx = regObj.get("title_idx").getAsInt();
        Pattern pattern1 = getPattern(regObj.get("reg").getAsString());
        if (StringUtils.notBlank(content)) {
            Matcher mt = pattern1.matcher(handleContent(content));
            if (mt.find()) {
                return mt.group(titleIdx);
            }
        }
        return null;
    }

    /**
     * 获取最新的内容
     * {@code 第54条：返回空的数组或集合不要返回null}
     * @return String[]
     */
    @NonNull
    @Contract(" -> new")
    public final String[] getNewArticle() {
        String url = webCfg.get("spy_root").getAsString();
        JsonObject regObj = webCfg.getAsJsonObject("img_web_reg");
        int artIdIdx = regObj.get("art_id_idx").getAsInt();
        int titleIdx = regObj.get("title_idx").getAsInt();
        Pattern pattern1 = getPattern(regObj.get("reg").getAsString());
        try {
            Request req = new Request.Builder().url(url).build();
            ResponseBody responseBody = getOkHttpClient().newCall(req).execute().body();
            assert responseBody != null;
            String content = responseBody.string();
            Matcher mt = pattern1.matcher(content);
            if (mt.find()) {
                String title = mt.group(titleIdx);
                String artId = mt.group(artIdIdx);
                return new String[]{artId, title};
            } else {
                MyLog.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     * 获取请求url
     * @implSpec 各个web可以有自己的url组装方法
     * @param artId String
     * @param page Integer
     * @return String
     */
    public String getUrl(String artId, Integer page) {
        if (page == null) {
            return String.format(urlTpl, artId);
        } else {
            return page > 2 ? String.format(urlTpl2, artId, page - 1) : String.format(urlTpl, artId);
        }
    }

    /**
     * 获取上次访问的链接
     * {@code 第16条：在公有类中使用访问方法，而不是公有域}
     * @return String
     */
    public String getCurrentUrl() {
        return curUrl;
    }

    @Override
    public String toString() {
        return webName + "：" + webCfg.toString();
    }
}
