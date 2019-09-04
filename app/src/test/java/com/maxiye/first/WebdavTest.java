package com.maxiye.first;


import com.maxiye.first.util.WebdavUtil;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.maxiye.first.util.Util.log;

/**
 * 数据库助手
 * Created by 91287 on 2019/5/26.
 */
public class WebdavTest {
    @Test
    public void sardineTest() throws IOException {
        Sardine sardine = new OkHttpSardine();
        sardine.setCredentials("912877398@qq.com", "");
        List<DavResource> files = sardine.list("https://dav.jianguoyun.com/dav/maxiye");
        log(files);
//        System.out.println(files);
        System.out.println(new File("D:\\java\\jd-gui.cfg"));
//        sardine.put("https://dav.jianguoyun.com/dav/maxiye/jdd2", new File("D:\\java\\jd-gui.cfg"), "");
//        InputStream is = sardine.get("https://dav.jianguoyun.com/dav/maxiye/jdd2");
    }

    @Test
    public void putTest() {
        WebdavUtil webdavUtil = new WebdavUtil("912877398@qq.com", "");
        boolean res = webdavUtil.put("https://dav.jianguoyun.com/dav/maxiye/jdd2", new File("D:\\phplist.txt"));
        log(res);
    }

    @Test
    public void getTest() {
        WebdavUtil webdavUtil = new WebdavUtil("912877398@qq.com", "");
        try {
            String res = webdavUtil.get("https://dav.jianguoyun.com/dav/maxiye/jdd2").string();
            log(res);
        } catch (IOException e) {
            log(e.toString());
            e.printStackTrace();
        }
    }

    @Test
    public void deleteTest() {
        WebdavUtil webdavUtil = new WebdavUtil("912877398@qq.com", "");
        log(webdavUtil.delete("https://dav.jianguoyun.com/dav/maxiye/jdd2"));
    }

    @Test
    public void listTest() throws IOException {
        WebdavUtil webdavUtil = new WebdavUtil("912877398@qq.com", "");
        log(Arrays.toString(webdavUtil.list("https://dav.jianguoyun.com/dav/maxiye/", 1)));
        log(webdavUtil.getRawRes());
    }
}
