package com.maxiye.first;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.google.gson.Gson;
import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.BitmapUtil;
import com.maxiye.first.util.MyLog;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

import static com.maxiye.first.util.Util.log;

/**
 * mock测试
 * Created by zhangyl on 2019/7/3.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BitmapFactory.class, Bitmap.class, BitmapUtil.class, MyLog.class})
@PowerMockIgnore({ "javax.xml.*",
        "javax.management.*","com.sun.org.apache.xerces.*","javax.net.ssl.*"})
public class MockTest {
    @Test
    public void singleChannelTest() throws IOException {
        PowerMockito.mockStatic(BitmapFactory.class);
        PowerMockito.mockStatic(Bitmap.class);
        PowerMockito.mockStatic(BitmapUtil.class);
        Bitmap ori = BitmapFactory.decodeFile("D:\\test\\test.jpeg");
        Bitmap res = BitmapUtil.convertSingleChannel(ori);
        File file = new File("D:\\test\\test-1.png");
        if (!file.exists()) {
            file.createNewFile();
        }
        try {
            FileOutputStream fos = new FileOutputStream(new File("D:\\test\\test-1.png"));
            res.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void mockLoginTest() {
        PowerMockito.mockStatic(MyLog.class);
        Map<String, String> map = new HashMap<>();
        map.put("PHPSESSID", "ipjrd9u2up5q1l4kdti13");
        map.put("C_LOGIN_ARRAY", "a%3A1%7D");
        String json = new Gson().toJson(map, Map.class);
        log(json);
//        log(ApiUtil.getInstance().mockLogin());
    }
}
