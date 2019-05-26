package com.maxiye.first;


import com.maxiye.first.util.WebdavUtil;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * 数据库助手
 * Created by 91287 on 2019/5/26.
 */
public class WebdavTest {
    @Test
    public void sardineTest() throws IOException {
//        Sardine sardine = new OkHttpSardine();
//        sardine.setCredentials("912877398@qq.com", "");
//        List<DavResource> files = sardine.list("https://dav.jianguoyun.com/dav/");
//        System.out.println(files);
        System.out.println(new File("D:\\java\\jd-gui.cfg"));
//        sardine.put("https://dav.jianguoyun.com/dav/maxiye/jdd2", new File("D:\\java\\jd-gui.cfg"), "");
//        InputStream is = sardine.get("https://dav.jianguoyun.com/dav/maxiye/jdd2");
    }

    @Test
    public void putTest() throws IOException {
        WebdavUtil webdavUtil = new WebdavUtil("912877398@qq.com", "avcm4cpviyp3p4hx");
        boolean res = webdavUtil.put("https://dav.jianguoyun.com/dav/maxiye/jdd2", new File("D:\\java\\jd-gui.cfg"));
        System.out.println(res);
    }

    @Test
    public void getTest() {
        WebdavUtil webdavUtil = new WebdavUtil("912877398@qq.com", "");
        try {
            String res = webdavUtil.get("https://dav.jianguoyun.com/dav/maxiye/jdd2").string();
            System.out.println(res);
        } catch (IOException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    @Test
    public void deleteTest() {
        WebdavUtil webdavUtil = new WebdavUtil("912877398@qq.com", "");
        System.out.println(webdavUtil.delete("https://dav.jianguoyun.com/dav/maxiye/jdd2"));
    }
}
