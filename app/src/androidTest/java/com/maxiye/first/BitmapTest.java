package com.maxiye.first;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.runner.AndroidJUnit4;

import com.maxiye.first.util.BitmapUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class BitmapTest {
    @Test
    public void singleChannelTest() throws IOException {
        Bitmap ori = BitmapFactory.decodeFile("I:\\test\\test.jpeg");
        Bitmap res = BitmapUtil.convertSingleChannel(ori);
        File file = new File("I:\\test\\test-1.png");
        if (!file.exists()) {
            file.createNewFile();
        }
        try {
            FileOutputStream fos = new FileOutputStream(new File("I:\\test\\test-1.png"));
            res.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
