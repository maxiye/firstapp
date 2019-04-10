package com.maxiye.first;

import android.support.annotation.NonNull;

import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.StringUtils;
import com.maxiye.first.util.Util;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 * {@code 第39条：注解优先于命名模式} 使用{@link Test}注解测试方法
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
// @RunWith(BlockJUnit4ClassRunner.class) //default
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void unicodeConvert() {
        String res = StringUtils.unicode2Chinese("\n" +
                "<!doctype html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\">\n" +
                "    <meta name=\"description\" content=\"\">\n" +
                "    <meta name=\"keywords\" content=\"\">\n" +
                "    <meta name=\"format-detection\" content=\"telephone=no,address=no,email=no\">\n" +
                "    <meta name=\"apple-itunes-app\" content=\"app-id=\" />\n" +
                "        <link rel=\"apple-touch-icon\" sizes=\"114x114\" href=\"\" />\n" +
                "    <title>全球搞笑GIF第2372弹：搭讪也是有风险的。。-多玩图库</title>\n" +
                "    <link href=\"http://assets.dwstatic.com/project/mobipic/css/pic-detail.css?20150612.css?v=20180425\" rel=\"stylesheet\">\n" +
                "</head>\n" +
                "<body>\n" +
                "        <header class=\"ui-picHeader\">\n" +
                "        <div class=\"ui-picHeader__backBtn\">\n" +
                "\t\t\t<a onclick=\"history.go(-1)\">\n" +
                "            <svg xmlns=\"http://www.w3.org/2000/svg\" x=\"0px\" y=\"0px\" width=\"11px\" height=\"16px\">\n" +
                "                <polygon fill-rule=\"evenodd\" clip-rule=\"evenodd\" fill=\"#fff\" points=\"11.038,-0.02 8.052,-0.007 0.096,7.876 0.22,8 0.096,8.123 7.95,16.001 10.947,16.014 3.011,8.006\"/>\n" +
                "            </svg>\n" +
                "        </div>\n" +
                "        <div class=\"ui-picHeader__title\">图集推荐</div>\n" +
                "        <a href=\"#\" id=\"comment_url\"><div class=\"ui-picHeader__reply\">0</div></a>\n" +
                "    </header>\n" +
                "\t    <div class=\"swiper-container ui-picShow\">\n" +
                "        <div class=\"swiper-wrapper\">\n" +
                "                        <!-- 图集推荐 -->\n" +
                "            <div class=\"swiper-slide swiper-item\">\n" +
                "                <div class=\"wrapper\" id=\"rec-imgs\">\n" +
                "                    <div class=\"m-container\">\n" +
                "                        <ul class=\"rec-list\">\n" +
                "                                                        <a href=\"http://tu.duowan.cn/gallery/138255.html\">\n" +
                "\t\t\t\t\t\t\t<li style=\"background-image: url(http://s1.dwstatic.com/group1/M00/93/89/6becea2c7451788680862c869a8388ee.gif)\">\n" +
                "                                    <span class=\"r-l-txt\">全球搞笑GIF第2371弹：感觉保安姐姐好暖心好好看啊。</span>\n" +
                "                            </li>\n" +
                "                            </a>\n" +
                "\t\t\t\t\t\t\t                            <a href=\"http://tu.duowan.cn/gallery/138254.html\">\n" +
                "\t\t\t\t\t\t\t<li style=\"background-image: url(http://s1.dwstatic.com/group1/M00/93/89/6becea2c7451788680862c869a8388ee.gif)\">\n" +
                "                                    <span class=\"r-l-txt\">删除</span>\n" +
                "                            </li>\n" +
                "                            </a>\n" +
                "\t\t\t\t\t\t\t                            <a href=\"http://tu.duowan.cn/gallery/138241.html\">\n" +
                "\t\t\t\t\t\t\t<li style=\"background-image: url(http://s1.dwstatic.com/group1/M00/4A/50/0d62ed8d8fec9e69bb70d00f02696871.gif)\">\n" +
                "                                    <span class=\"r-l-txt\">全球搞笑GIF第2370弹：自从换了这把锁，手机就没有丢过</span>\n" +
                "                            </li>\n" +
                "                            </a>\n" +
                "\t\t\t\t\t\t\t                            <a href=\"http://tu.duowan.cn/gallery/138230.html\">\n" +
                "\t\t\t\t\t\t\t<li style=\"background-image: url(http://s1.dwstatic.com/group1/M00/CD/FD/738eec32254eab3ec722dc2c443e7b35.gif)\">\n" +
                "                                    <span class=\"r-l-txt\">全球搞笑GIF第2369弹：第一次恋爱和女朋友接吻，大概就是这种画风</span>\n" +
                "                            </li>\n" +
                "                            </a>\n" +
                "\t\t\t\t\t\t\t                            \n" +
                "                        </ul>\n" +
                "\t\t\t\t\t\t<!--\n" +
                "                        <div class=\"rec-ad\">\n" +
                "                            <a href=\"\"><img src=\"http://art.yypm.com/620x120\" class=\"2015-hao-ad\"></a>\n" +
                "                        </div>\n" +
                "                        -->\n" +
                "                    </div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "\t\t\t        </div>\n" +
                "        <div class=\"img-info-n\">\n" +
                "            <div class=\"img-info-title g-textHidden\"></div>\n" +
                "            <div class=\"m-img_num\"></div>\n" +
                "            <div id=\"infoContent\" class=\"img-info-content\">\n" +
                "                <p id=\"imgInfoContent\"></p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "\n" +
                "<script>\n" +
                "var imgJson = {\"gallery_title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002\",\"updated\":\"1547376618\",\"picInfo\":[{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (1)\",\"pic_id\":\"2479096\",\"ding\":\"139\",\"cai\":\"9\",\"old\":\"239\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/35\\/2C\\/2b2ec9471071a8218738b82bf413d829.gif\",\"file_url\":null,\"file_width\":\"400\",\"file_height\":\"262\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/35\\/2C\\/52f4a8dabb382e3afaceff33d00232f5.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/35\\/2C\\/52f4a8dabb382e3afaceff33d00232f5.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377695338.mp4\",\"video_url\":\"\",\"sort\":\"0\",\"cmt_md5\":\"c606fae2853b52f942810c530bb35c92\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479096\",\"add_intro\":\"\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=c606fae2853b52f942810c530bb35c92&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%281%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (2)\",\"pic_id\":\"2479097\",\"ding\":\"337\",\"cai\":\"9\",\"old\":\"30\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/4B\\/13\\/e05fd65cf48391cd785d9a418ebdb520.gif\",\"file_url\":null,\"file_width\":\"304\",\"file_height\":\"310\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/4B\\/13\\/047b4fd9801dac8aa5e43d200df07076.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/4B\\/13\\/047b4fd9801dac8aa5e43d200df07076.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377691599.mp4\",\"video_url\":\"\",\"sort\":\"1\",\"cmt_md5\":\"9feb53fd401226d60fabffff5970bf36\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479097\",\"add_intro\":\"\\u81c2\\u529b\\u60ca\\u4eba\",\"num\":\"18\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=9feb53fd401226d60fabffff5970bf36&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%282%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (3)\",\"pic_id\":\"2479098\",\"ding\":\"153\",\"cai\":\"6\",\"old\":\"13\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/CC\\/D1\\/0f35b8fb860ee5f4b5a6c00a99690571.gif\",\"file_url\":null,\"file_width\":\"400\",\"file_height\":\"260\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/CC\\/D1\\/0d594f9c6075fe6580c8cb4500020ddb.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/CC\\/D1\\/0d594f9c6075fe6580c8cb4500020ddb.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377690311.mp4\",\"video_url\":\"\",\"sort\":\"2\",\"cmt_md5\":\"b0e02f4fe497a5609a4e26fa140c28c5\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479098\",\"add_intro\":\"\\u4e0d\\u8981\\u52a8\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=b0e02f4fe497a5609a4e26fa140c28c5&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%283%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (4)\",\"pic_id\":\"2479099\",\"ding\":\"180\",\"cai\":\"8\",\"old\":\"174\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/D6\\/EF\\/6a16d035c026ce19dbb0b2e44f35ef17.gif\",\"file_url\":null,\"file_width\":\"206\",\"file_height\":\"360\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/D6\\/EF\\/3ab2804fff93a138a0ebb518acf9a77d.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/D6\\/EF\\/3ab2804fff93a138a0ebb518acf9a77d.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377684178.mp4\",\"video_url\":\"\",\"sort\":\"3\",\"cmt_md5\":\"5a0ba6eb4a5831dbe77e4fb8809d18ab\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479099\",\"add_intro\":\"\\u4e0d\\u8981\\u505c\\uff0c\\u7ee7\\u7eed\\u3002\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=5a0ba6eb4a5831dbe77e4fb8809d18ab&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%284%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (5)\",\"pic_id\":\"2479100\",\"ding\":\"232\",\"cai\":\"113\",\"old\":\"133\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/10\\/8D\\/ad968e28fdff26fe1c5b260ac8f26ce2.gif\",\"file_url\":null,\"file_width\":\"296\",\"file_height\":\"213\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/10\\/8D\\/73677b892aeee03fda9b3b0eff914b23.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/10\\/8D\\/73677b892aeee03fda9b3b0eff914b23.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377682144.mp4\",\"video_url\":\"\",\"sort\":\"4\",\"cmt_md5\":\"1fb4d6ffa9df802d10f36fc50214340e\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479100\",\"add_intro\":\"\\u6210\\u9f99\\u5927\\u54e5\\u4e5f\\u2026\\u2026\",\"num\":\"49\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=1fb4d6ffa9df802d10f36fc50214340e&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%285%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (6)\",\"pic_id\":\"2479101\",\"ding\":\"244\",\"cai\":\"9\",\"old\":\"44\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/98\\/BA\\/0a3d66fdd630cb4ce9e00c544af30a32.gif\",\"file_url\":null,\"file_width\":\"235\",\"file_height\":\"403\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/98\\/BA\\/074983727abf39d3523bde342c7cf6b2.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/98\\/BA\\/074983727abf39d3523bde342c7cf6b2.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7374814124.mp4\",\"video_url\":\"\",\"sort\":\"5\",\"cmt_md5\":\"b3cccbb3935d728880e40201cf1d4a8c\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479101\",\"add_intro\":\"\\u5230\\u5e95\\u6709\\u591a\\u51b7\\uff1f\",\"num\":\"26\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=b3cccbb3935d728880e40201cf1d4a8c&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%286%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (7)\",\"pic_id\":\"2479102\",\"ding\":\"138\",\"cai\":\"430\",\"old\":\"51\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/47\\/FF\\/bed3a537139c4dd4075dfd5784430183.gif\",\"file_url\":null,\"file_width\":\"238\",\"file_height\":\"398\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/47\\/FF\\/ccc3e8e4fbad1fffaabadbb9cc9bdd32.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/47\\/FF\\/ccc3e8e4fbad1fffaabadbb9cc9bdd32.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7374812765.mp4\",\"video_url\":\"\",\"sort\":\"6\",\"cmt_md5\":\"176e2bfb1eba75a5a49bfbf1f6f64033\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479102\",\"add_intro\":\"\\u516c\\u4ea4\\u8f66\\u4e0a\\u8001\\u4eba\\u5f3a\\u8feb\\u5b69\\u5b50\\u8ba9\\u5750\",\"num\":\"44\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=176e2bfb1eba75a5a49bfbf1f6f64033&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%287%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (8)\",\"pic_id\":\"2479103\",\"ding\":\"195\",\"cai\":\"7\",\"old\":\"14\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/8F\\/93\\/4830aec1b1bbd3472c0c991b70863237.gif\",\"file_url\":null,\"file_width\":\"360\",\"file_height\":\"479\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/8F\\/93\\/89b2a7fb839ebaf8a2679e2ba873f222.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/8F\\/93\\/89b2a7fb839ebaf8a2679e2ba873f222.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7374810349.mp4\",\"video_url\":\"\",\"sort\":\"7\",\"cmt_md5\":\"7a6a587eb1182b257f802d9eb81e4f24\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479103\",\"add_intro\":\"\\u8fd8\\u6709\\u70b9\\u5bb3\\u7f9e\\u5462\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=7a6a587eb1182b257f802d9eb81e4f24&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%288%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (9)\",\"pic_id\":\"2479104\",\"ding\":\"165\",\"cai\":\"27\",\"old\":\"21\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/7D\\/D7\\/8a1ba04cd330541394b557135d0a957e.gif\",\"file_url\":null,\"file_width\":\"360\",\"file_height\":\"203\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/7D\\/D7\\/e3f32899f1fe3cc5848f9328bcf54884.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/7D\\/D7\\/e3f32899f1fe3cc5848f9328bcf54884.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7374805910.mp4\",\"video_url\":\"\",\"sort\":\"8\",\"cmt_md5\":\"6ba3ddbb133e01c0f743ca9ed71da810\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479104\",\"add_intro\":\"\\u597d\\u50cf\\u76d7\\u7248\\u5927\\u6218\\u6b63\\u7248\",\"num\":\"20\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=6ba3ddbb133e01c0f743ca9ed71da810&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%289%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (10)\",\"pic_id\":\"2479105\",\"ding\":\"82\",\"cai\":\"18\",\"old\":\"593\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/87\\/53\\/a398823059aff5e7ca1b35402c30668d.gif\",\"file_url\":null,\"file_width\":\"320\",\"file_height\":\"180\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/87\\/53\\/f9af65f21755db6665108e3d382d6e14.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/87\\/53\\/f9af65f21755db6665108e3d382d6e14.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7374802182.mp4\",\"video_url\":\"\",\"sort\":\"9\",\"cmt_md5\":\"dd5f8725aa3dbda822a46fde4abc602d\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479105\",\"add_intro\":\"\\u706b\\u8f66\\u6f02\\u79fb\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=dd5f8725aa3dbda822a46fde4abc602d&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2810%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (11)\",\"pic_id\":\"2479106\",\"ding\":\"314\",\"cai\":\"14\",\"old\":\"28\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/24\\/52\\/9cf501e8a3bd330f601390c30246177e.gif\",\"file_url\":null,\"file_width\":\"202\",\"file_height\":\"360\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/24\\/52\\/be236a88c62037a6653672895d4d2e4d.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/24\\/52\\/be236a88c62037a6653672895d4d2e4d.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7382736797.mp4\",\"video_url\":\"\",\"sort\":\"10\",\"cmt_md5\":\"1646232d32eb1d48b5988a7005ad5e77\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479106\",\"add_intro\":\"\\u7a7a\\u59d0\\u4f11\\u606f\\u5ba4\\uff1f\",\"num\":\"34\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=1646232d32eb1d48b5988a7005ad5e77&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2811%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (12)\",\"pic_id\":\"2479107\",\"ding\":\"156\",\"cai\":\"6\",\"old\":\"8\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/EB\\/D7\\/ebfd85a365cc0c88576869ad7d3fbfb4.gif\",\"file_url\":null,\"file_width\":\"658\",\"file_height\":\"494\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/EB\\/D7\\/7820ec7ba77cc3e2fded163918a0fb1f.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/EB\\/D7\\/7820ec7ba77cc3e2fded163918a0fb1f.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7382734791.mp4\",\"video_url\":\"\",\"sort\":\"11\",\"cmt_md5\":\"f28c7d37a72a973decc178a43f9c126a\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479107\",\"add_intro\":\"\\u4e24\\u4eea\\u751f\\u56db\\u8c61\\uff0c\\u56db\\u8c61\\u751f\\u4e07\\u7269\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=f28c7d37a72a973decc178a43f9c126a&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2812%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (13)\",\"pic_id\":\"2479108\",\"ding\":\"158\",\"cai\":\"10\",\"old\":\"15\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/80\\/35\\/c7b7f970075d359cd5c55152e6f91b3d.gif\",\"file_url\":null,\"file_width\":\"610\",\"file_height\":\"345\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/80\\/35\\/cc4135b31f45629509e113f93f06332d.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/80\\/35\\/cc4135b31f45629509e113f93f06332d.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7382731414.mp4\",\"video_url\":\"\",\"sort\":\"12\",\"cmt_md5\":\"19ce617ed8c1395d4568201e81f0ce3b\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479108\",\"add_intro\":\"\\u6478\\u5934\\u6740\",\"num\":\"19\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=19ce617ed8c1395d4568201e81f0ce3b&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2813%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (14)\",\"pic_id\":\"2479109\",\"ding\":\"200\",\"cai\":\"148\",\"old\":\"592\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C4\\/CD\\/8c05a6212ef7ce95c3769252080610c3.gif\",\"file_url\":null,\"file_width\":\"328\",\"file_height\":\"640\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C4\\/CD\\/09b9a0845cb8aceaa5472a1f974cec79.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C4\\/CD\\/09b9a0845cb8aceaa5472a1f974cec79.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7382728195.mp4\",\"video_url\":\"\",\"sort\":\"13\",\"cmt_md5\":\"a11f21794f2bf322bacacf53975a0eb5\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479109\",\"add_intro\":\"\\u4f60\\u4eec\\u8981\\u7684\\u6b63\\u653e\\u3002\\u3002\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=a11f21794f2bf322bacacf53975a0eb5&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2814%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (15)\",\"pic_id\":\"2479110\",\"ding\":\"176\",\"cai\":\"8\",\"old\":\"42\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/79\\/91\\/9f0d75b329f06efca34c53a7405c874c.gif\",\"file_url\":null,\"file_width\":\"202\",\"file_height\":\"360\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/79\\/91\\/fe734f967d9c70db5d2fe1f4fcc099af.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/79\\/91\\/fe734f967d9c70db5d2fe1f4fcc099af.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7382722775.mp4\",\"video_url\":\"\",\"sort\":\"14\",\"cmt_md5\":\"8c08f9825f876a611a127c0556a77f35\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479110\",\"add_intro\":\"\\u5e73\\u780d\\u8fde\\u51fb\\u5e26\\u987a\\u5288\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=8c08f9825f876a611a127c0556a77f35&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2815%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (16)\",\"pic_id\":\"2479111\",\"ding\":\"315\",\"cai\":\"41\",\"old\":\"40\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/0A\\/68\\/f9858e8eed31acaee0248d75ca8f5113.gif\",\"file_url\":null,\"file_width\":\"320\",\"file_height\":\"268\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/0A\\/68\\/74ec8071876ab50d4916c0e51bb76420.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/0A\\/68\\/74ec8071876ab50d4916c0e51bb76420.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377572790.mp4\",\"video_url\":\"\",\"sort\":\"15\",\"cmt_md5\":\"2b8bdea4a9907fceff91bf9b0f334ae8\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479111\",\"add_intro\":\"\\u51ed\\u5b9e\\u529b\\u6ce8\\u5b64\\u751f\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=2b8bdea4a9907fceff91bf9b0f334ae8&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2816%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (17)\",\"pic_id\":\"2479112\",\"ding\":\"280\",\"cai\":\"7\",\"old\":\"10\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/1B\\/A5\\/210ca2d232276fd1f25151c6d5d16c37.gif\",\"file_url\":null,\"file_width\":\"175\",\"file_height\":\"380\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/1B\\/A5\\/0e19f90972eec412be42c24dbe4c748d.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/1B\\/A5\\/0e19f90972eec412be42c24dbe4c748d.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377570870.mp4\",\"video_url\":\"\",\"sort\":\"16\",\"cmt_md5\":\"6e231ca66976fb6ee39f2f2371efe51d\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479112\",\"add_intro\":\"\\u4ec0\\u4e48\\u53eb\\u4ed9\\u6c14\",\"num\":\"20\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=6e231ca66976fb6ee39f2f2371efe51d&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2817%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (18)\",\"pic_id\":\"2479113\",\"ding\":\"216\",\"cai\":\"11\",\"old\":\"133\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C6\\/FD\\/fd43f41840c06796032c6dc70c053f16.gif\",\"file_url\":null,\"file_width\":\"313\",\"file_height\":\"136\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C6\\/FD\\/70ff5513a8c6d0e87e853fb8f84141fd.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C6\\/FD\\/70ff5513a8c6d0e87e853fb8f84141fd.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377569623.mp4\",\"video_url\":\"\",\"sort\":\"17\",\"cmt_md5\":\"3c54842e6316013fbcc0e1ba0ccf0e00\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479113\",\"add_intro\":\"\\u662f\\u4e2a\\u8bef\\u4f1a\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=3c54842e6316013fbcc0e1ba0ccf0e00&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2818%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (19)\",\"pic_id\":\"2479114\",\"ding\":\"160\",\"cai\":\"6\",\"old\":\"70\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/00\\/CD\\/cc60691fe4807dd9268ab5e52c967027.gif\",\"file_url\":null,\"file_width\":\"306\",\"file_height\":\"360\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/00\\/CD\\/7e4c5032b0dfd1f9f30dbb8d858e6a8f.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/00\\/CD\\/7e4c5032b0dfd1f9f30dbb8d858e6a8f.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377564658.mp4\",\"video_url\":\"\",\"sort\":\"18\",\"cmt_md5\":\"5503a0153c502fdc63bb1dfb6fc06142\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479114\",\"add_intro\":\"\\u7761\\u7684\\u5f88\\u5b89\\u8be6\\u554a\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=5503a0153c502fdc63bb1dfb6fc06142&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2819%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (20)\",\"pic_id\":\"2479115\",\"ding\":\"179\",\"cai\":\"7\",\"old\":\"89\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C0\\/7E\\/c17d3013ec6f57bf045176e41a1d2667.gif\",\"file_url\":null,\"file_width\":\"355\",\"file_height\":\"234\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C0\\/7E\\/fbbada25cc57e73903c5edfdbf7f1a13.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/C0\\/7E\\/fbbada25cc57e73903c5edfdbf7f1a13.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377561903.mp4\",\"video_url\":\"\",\"sort\":\"19\",\"cmt_md5\":\"310b09eb93826c0173a94b8c68f11e7c\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479115\",\"add_intro\":\"\\u6b7b\\u795e\\u6765\\u4e86\\uff0c\\u53c8\\u8d70\\u4e86\\uff01\",\"num\":\"24\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=310b09eb93826c0173a94b8c68f11e7c&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2820%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (21)\",\"pic_id\":\"2479116\",\"ding\":\"177\",\"cai\":\"6\",\"old\":\"52\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/CA\\/AE\\/dfeda2dcf0c014e29817a36c8878dfcc.gif\",\"file_url\":null,\"file_width\":\"234\",\"file_height\":\"302\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/CA\\/AE\\/268ca4203d24d971b5b78cbfc38347dd.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/CA\\/AE\\/268ca4203d24d971b5b78cbfc38347dd.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7380095733.mp4\",\"video_url\":\"\",\"sort\":\"20\",\"cmt_md5\":\"0bd4def473ba6c022d49c69a827ee647\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479116\",\"add_intro\":\"\\u5438\\u4e00\\u53e3\\u4ed9\\u6c14\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=0bd4def473ba6c022d49c69a827ee647&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2821%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (22)\",\"pic_id\":\"2479117\",\"ding\":\"69\",\"cai\":\"103\",\"old\":\"1323\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/23\\/A7\\/3af9724da2e5aadd0aef3e02c7925b3a.gif\",\"file_url\":null,\"file_width\":\"420\",\"file_height\":\"513\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/23\\/A7\\/9f01645f7c397836676d94abd279e526.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/23\\/A7\\/9f01645f7c397836676d94abd279e526.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7380087763.mp4\",\"video_url\":\"\",\"sort\":\"21\",\"cmt_md5\":\"bfcf513b5723f42f6ee65ad8c391120d\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479117\",\"add_intro\":\"\\u5413\\u6b7b\\u4e86\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=bfcf513b5723f42f6ee65ad8c391120d&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2822%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (23)\",\"pic_id\":\"2479118\",\"ding\":\"163\",\"cai\":\"6\",\"old\":\"21\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/B7\\/08\\/eea7b74e3c04e3ee121673a5576317e2.gif\",\"file_url\":null,\"file_width\":\"600\",\"file_height\":\"290\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/B7\\/08\\/3d99d6b7ad1fe21644710b584e8d3139.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/B7\\/08\\/3d99d6b7ad1fe21644710b584e8d3139.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7380085486.mp4\",\"video_url\":\"\",\"sort\":\"22\",\"cmt_md5\":\"83f0ca5df955e0694bbe826c673fc61c\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479118\",\"add_intro\":\"\\u4e00\\u628a\\u6293\\u4f4f\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=83f0ca5df955e0694bbe826c673fc61c&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2823%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (24)\",\"pic_id\":\"2479119\",\"ding\":\"183\",\"cai\":\"7\",\"old\":\"11\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/87\\/E3\\/50b8508e455d92b8a5494947e920f678.gif\",\"file_url\":null,\"file_width\":\"275\",\"file_height\":\"345\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/87\\/E3\\/2dcb510b912d05647b4c39d1a601265a.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/87\\/E3\\/2dcb510b912d05647b4c39d1a601265a.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7380083657.mp4\",\"video_url\":\"\",\"sort\":\"23\",\"cmt_md5\":\"7ef246325be9b85ab560994715e042da\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479119\",\"add_intro\":\"\\u4e00\\u53ea\\u5c0f\\u9e1f\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=7ef246325be9b85ab560994715e042da&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2824%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (25)\",\"pic_id\":\"2479120\",\"ding\":\"108\",\"cai\":\"7\",\"old\":\"74\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/BD\\/31\\/78849e729e6da7bc8dd926cfd70408f1.gif\",\"file_url\":null,\"file_width\":\"320\",\"file_height\":\"575\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/BD\\/31\\/8c0842b499956e9d6cd631a4c6fb3b06.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/BD\\/31\\/8c0842b499956e9d6cd631a4c6fb3b06.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7380082811.mp4\",\"video_url\":\"\",\"sort\":\"24\",\"cmt_md5\":\"bb8da9fd845bfcdce4269ca2b68c6c06\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479120\",\"add_intro\":\"\\u8fd9\\u624d\\u53eb\\u80a5\\u5b85\\u5feb\\u4e50\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=bb8da9fd845bfcdce4269ca2b68c6c06&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2825%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (26)\",\"pic_id\":\"2479121\",\"ding\":\"113\",\"cai\":\"4\",\"old\":\"8\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/23\\/10\\/54008500cf583e4dd784dc9e84a82959.gif\",\"file_url\":null,\"file_width\":\"480\",\"file_height\":\"840\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/23\\/10\\/cd2dbbca420b889636a943439ae2939a.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/23\\/10\\/cd2dbbca420b889636a943439ae2939a.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377467803.mp4\",\"video_url\":\"\",\"sort\":\"25\",\"cmt_md5\":\"fe14b267c854c1e9bd19c87bef0d3b0f\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479121\",\"add_intro\":\"\\u8fd9\\u817f\\u771f\\u7684\\u957f\\u3002\\u3002\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=fe14b267c854c1e9bd19c87bef0d3b0f&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2826%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (27)\",\"pic_id\":\"2479122\",\"ding\":\"134\",\"cai\":\"3\",\"old\":\"9\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/A7\\/25\\/80a3ceb071342436620ae5bfdd724b38.gif\",\"file_url\":null,\"file_width\":\"400\",\"file_height\":\"260\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/A7\\/25\\/5aa9535dbd319a58460f57e2d9183f1d.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/A7\\/25\\/5aa9535dbd319a58460f57e2d9183f1d.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377461742.mp4\",\"video_url\":\"\",\"sort\":\"26\",\"cmt_md5\":\"2edabe6f376b02eb51ee3dba0291da0e\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479122\",\"add_intro\":\"\\u6253\\u4e0d\\u8fc7\\u6253\\u4e0d\\u8fc7\\uff0c\\u5168\\u90fd\\u6253\\u4e0d\\u8fc7\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=2edabe6f376b02eb51ee3dba0291da0e&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2827%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (28)\",\"pic_id\":\"2479123\",\"ding\":\"199\",\"cai\":\"21\",\"old\":\"55\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/14\\/22\\/aea1199045d0371efd3341c50fe41664.gif\",\"file_url\":null,\"file_width\":\"300\",\"file_height\":\"450\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/14\\/22\\/f278d76a0ea885137e6294c511618c69.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/14\\/22\\/f278d76a0ea885137e6294c511618c69.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377455470.mp4\",\"video_url\":\"\",\"sort\":\"27\",\"cmt_md5\":\"47b6d45bde8265c88c2cddf179f26a50\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479123\",\"add_intro\":\"\\u5916\\u9762\\u7684\\u4eba\\u60f3\\u8fdb\\u53bb\\uff0c\\u91cc\\u9762\\u7684\\u4eba\\u60f3\\u51fa\\u6765\",\"num\":\"36\",\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=47b6d45bde8265c88c2cddf179f26a50&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2828%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (29)\",\"pic_id\":\"2479124\",\"ding\":\"112\",\"cai\":\"18\",\"old\":\"474\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/2E\\/63\\/b5e263757cd3deaf16ed3d974a1737f3.gif\",\"file_url\":null,\"file_width\":\"525\",\"file_height\":\"497\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/2E\\/63\\/0f8b5dab3822376b12c453dc2611d341.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/2E\\/63\\/0f8b5dab3822376b12c453dc2611d341.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377449829.mp4\",\"video_url\":\"\",\"sort\":\"28\",\"cmt_md5\":\"16ad152e9ae05422462f7a43098bf6e1\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479124\",\"add_intro\":\"\\u5fc3\\u673a\\u554a\\u554a..\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=16ad152e9ae05422462f7a43098bf6e1&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2829%29\"},{\"title\":\"\\u5168\\u7403\\u641e\\u7b11GIF\\u7b2c2372\\u5f39\\uff1a\\u642d\\u8baa\\u4e5f\\u662f\\u6709\\u98ce\\u9669\\u7684\\u3002\\u3002 (30)\",\"pic_id\":\"2479125\",\"ding\":\"119\",\"cai\":\"14\",\"old\":\"46\",\"cover_url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/E0\\/D2\\/2cadf942159100e27dd4306e1cb8a4bb.gif\",\"file_url\":null,\"file_width\":\"320\",\"file_height\":\"569\",\"url\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/E0\\/D2\\/bfbfc143bf9c6fc33ead22fbfc1c2168.gif\",\"source\":\"http:\\/\\/s1.dwstatic.com\\/group1\\/M00\\/E0\\/D2\\/bfbfc143bf9c6fc33ead22fbfc1c2168.gif\",\"mp4_url\":\"https:\\/\\/gmp4.dwstatic.com\\/1902\\/7377443904.mp4\",\"video_url\":\"\",\"sort\":\"29\",\"cmt_md5\":\"5a5511ebb549ce8fe4f6780af59ff62f\",\"cmt_url\":\"http:\\/\\/tu.duowan.com\\/gallery\\/138259.html#2479125\",\"add_intro\":\"\\u5b66\\u5230\\u4e86\",\"num\":0,\"comment_url\":\"http:\\/\\/www.duowan.com\\/mComment\\/index.html?domain=tu.duowan.com&uniqid=5a5511ebb549ce8fe4f6780af59ff62f&url=\\/gallery\\/138259.html&title=%E5%85%A8%E7%90%83%E6%90%9E%E7%AC%91GIF%E7%AC%AC2372%E5%BC%B9%EF%BC%9A%E6%90%AD%E8%AE%AA%E4%B9%9F%E6%98%AF%E6%9C%89%E9%A3%8E%E9%99%A9%E7%9A%84%E3%80%82%E3%80%82%20%2830%29\"}],\"hiidoId\":[\"_19cda8a\"]};\n" +
                "</script>\n" +
                "\n" +
                "    <!-- Seajs入口开始 -->\n" +
                "    <script src=\"http://assets.dwstatic.com/mobile/src/js/main/seajs/sea-lego.js\"></script>\n" +
                "    <script src=\"http://assets.dwstatic.com/project/mobipic/js/pic-detail.js\"></script>\n" +
                "    <!-- Seajs入口结束 -->\n" +
                "\t<script src=\"http://www.duowan.com/duowan.js\"></script>\n" +
                "\t<script>\n" +
                "var _hmt = _hmt || [];\n" +
                "(function() {\n" +
                "  var hm = document.createElement(\"script\");\n" +
                "  hm.src = \"//hm.baidu.com/hm.js?f01fcd42c8412250c435a1912e63ce89\";\n" +
                "  var s = document.getElementsByTagName(\"script\")[0]; \n" +
                "  s.parentNode.insertBefore(hm, s);\n" +
                "})();\n" +
                "</script> \n" +
                "\n" +
                "<!-- baidu统计 全站 -->\t\n" +
                " <!-- 全站百度统计 -->\n" +
                "<script>\n" +
                "var _hmt = _hmt || [];\n" +
                "(function() {\n" +
                "  var hm = document.createElement(\"script\");\n" +
                "  hm.src = \"https://hm.baidu.com/hm.js?ca78e9e216cc8c25379e6a6809ef0e69\";\n" +
                "  var s = document.getElementsByTagName(\"script\")[0]; \n" +
                "  s.parentNode.insertBefore(hm, s);\n" +
                "})();\n" +
                "</script>\n" +
                "<script src=\"//www.duowan.com/duowan.js\"></script>\n" +
                "\n" +
                "\t\n" +
                "\t\n" +
                "</body>\n" +
                "</html>\n");
        System.out.println(res);
    }

    @Test
    public void objEqual() {
        Object a = new Object();
        Object b = new Object();
        System.out.println(a.hashCode());
        System.out.println(b.hashCode());
        System.out.println(new Object().hashCode());
        System.out.println(new Object().hashCode());
    }

    /**
     * {@code 第26条：不要使用原始类型}
     */
    @Test(expected = ClassCastException.class)
    @SuppressWarnings("all")
    public void genericsTest() {
        Set<Object> set2 = new HashSet<>();
        set2.add(new Util());
        set2.add(Integer.MAX_VALUE);
        set2.add("aa");
        System.out.println(set2.iterator().next());
        // 你无法将任意元素（null除外）放入一个Collection<?>。试图这么做的化将产生编译时错误
        // 错误: 对于add(Util), 找不到合适的方法
        // 方法 Collection.add(CAP#1)不适用
        Set<?> set3 = new HashSet<>();
        /*set3.add(new Util());
        set3.add(Integer.MAX_VALUE);
        set3.add("aa");*/
        System.out.println(set3);
        Set<? extends Util> set4 = new HashSet<>();
        // 找不到合适的方法
        // set4.add(new Util());
        System.out.println(set4);
        // 原始类型 你将会失去泛型所带来的安全性和可读性
        Set set = new HashSet();
        set.add(new Util());
        set.add(Integer.MAX_VALUE);
        set.add("aa");
        // ClassCastException
        System.out.println((Util) set.iterator().next());
    }

    /**
     * {@code 第30条：优先使用泛型方法}
     * 总而言之，泛型方法就像泛型类型，比起那些要求客户端将参数及返回值进行显示强转的方法，它们更安全更简单。
     * 就像类型一样，你应该保证你的方法不用强转就能用，这意味着要将这些方法泛型化，你也应该将现有方法泛型化，让新用户用起来更简单，而且不用破坏现有客户端（条目26）。
     * @param map HashMap
     * @param key String
     * @return T
     */
    @SuppressWarnings("SameParameterValue")
    private static <T> T genericFun(@NonNull HashMap<String, T> map, String key) {
        return map.get(key);
    }

    /**
     * {@code 第31条：使用有限制通配符来增加API的灵活性}
     * @param map HashMap
     * @param key String
     * @return T
     */
    @SuppressWarnings("SameParameterValue")
    private static Number genericExtendFun(@NonNull  HashMap<String, ? extends Number> map, String key) {
        return map.get(key);
    }

    /**
     * {@code 第32条：合理结合泛型和变长参数}
     * 给方法一个泛型的可变参数数组是不安全的
     * SafeVarargs注解消除堆污染提示
     * @param stringLists List
     */
    @SafeVarargs
    @SuppressWarnings("unused")
    static void dangerous(@NonNull List<String>... stringLists) {
        List<Integer> intList = new ArrayList<>(2);
        ((Object[]) stringLists)[0] = intList;// Heap pollution 堆污染
        String s = stringLists[0].get(0); // ClassCastException 类型转换异常
    }

    @Test
    @SuppressWarnings("all")
    public void genericTest() {
        HashMap<String, Number> hashMap = new HashMap<>(3);
        hashMap.put("1", 2);
        hashMap.put("2", 3f);
        System.out.println(genericFun(hashMap, "1"));
        System.out.println(genericExtendFun(hashMap, "2"));
        HashMap<String, String> map = new HashMap<>(2);
        map.put("1", "2");
        //System.out.println(genericExtendFun(map, "1"));
        hashMap.put("3", Integer.valueOf(2));
    }


    @Test
    public void xPathTest() {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("test.txt"));
            XPath xPath = XPathFactory.newInstance().newXPath();
            Object res = xPath.evaluate("//div[@class='art-bd']/div/p[@class='p-image']/img", doc, XPathConstants.NODESET);
            if (res instanceof NodeList) {
                NodeList nodeList = (NodeList) res;
                for (int i = 0; i < nodeList.getLength(); i++) {
                    MyLog.println(nodeList.item(i).getAttributes().getNamedItem("data-src").getNodeValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void streamTest() {
        ArrayList<String> strings = new ArrayList<>(20);
        // 空
        strings.stream()
                .forEach(System.out::println);
    }

}