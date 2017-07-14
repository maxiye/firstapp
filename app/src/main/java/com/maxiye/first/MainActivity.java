package com.maxiye.first;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


public class MainActivity extends AppCompatActivity implements BlankFragment.OnFrgActionListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public final static String EXTRA_MESSAGE = "com.maxiye.first.MESSAGE";
    private final static int INTERVAL = 600;
    private long last_press_time = 0;

    private BlankFragment frg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText et = (EditText) findViewById(R.id.edit_message);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent ke) {
                switch (keycode) {
                    case KeyEvent.KEYCODE_ENTER://修改回车键功能
                        sendMsgNow(v);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        frg = new BlankFragment();
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            frg.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frg).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 为ActionBar扩展菜单项
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                setting(new View(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long now = new Date().getTime();
                long interval = now - last_press_time;
                if (interval < INTERVAL) {
                    finish();
                } else {
                    last_press_time = now;
                    alert(getString(R.string.alert_exit));
                }
                break;

            default:
                break;
        }
        return false;
    }

    //设置
    public void setting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        EditText et = (EditText) findViewById(R.id.edit_message);
        String msg = et.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, msg);
        startActivity(intent);
    }

    /**
     * 简写toast
     * @param msg 消息
     */
    private void alert(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    //测试
    public void test(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    @Override
    //ListView项目点击事件，实现OnFrgActionListener接口
    public void onItemClick(View view) {
        TextView tv = (TextView) view;
        String str = tv.getText().toString().split("：")[1];
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clm.setPrimaryClip(ClipData.newPlainText(null, str));
        Toast.makeText(this, R.string.clip_toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(View view) {
        onItemClick(view);
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        Uri url = Uri.parse("https://apps.evozi.com/apk-downloader/?id=" + clm.getPrimaryClip().getItemAt(0).getText());
        Intent itt = new Intent(Intent.ACTION_VIEW, url);
        startActivity(itt);
    }

    //查询应用
    public void sendMsgNow(View view) {
        if (!frg.thread.isAlive()) {
            EditText et = (EditText) findViewById(R.id.edit_message);
            frg.keyword = et.getText().toString();
            new Thread(frg.thread).start();
        } else {
            alert(getString(R.string.loading));
        }
        /*BlankFragment frg = new BlankFragment();
        EditText et = (EditText) findViewById(R.id.edit_message);
        String msg = et.getText().toString();
        Bundle args = new Bundle();
        args.putString(BlankFragment.ARG_1, msg);
        frg.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frg).commit();*/
    }

}
