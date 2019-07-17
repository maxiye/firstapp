package com.maxiye.first;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.maxiye.first.util.BitmapUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * mock测试
 * Created by zhangyl on 2019/7/3.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BitmapFactory.class, Bitmap.class, BitmapUtil.class})
public class BitmapTest {
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
}
