package com.maxiye.first;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxiye.first.util.DBHelper;

import java.util.Date;


public class MainActivity extends AppCompatActivity implements ApplistFragment.OnFrgActionListener, ActivityCompat.OnRequestPermissionsResultCallback, SearchView.OnQueryTextListener {
    public final static String EXTRA_MESSAGE = "com.maxiye.first.MESSAGE";
    public static final String EXTRA_URL = "com.maxiye.first.URL";
    private final static int INTERVAL = 600;
    private long last_press_time = 0;

    private ApplistFragment frg;
    private SearchView mSearchView;
    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检测是否备份数据库
        DBHelper.checkBakup(this);
        ab = getSupportActionBar();
        assert ab != null;
        frg = new ApplistFragment();
        if (findViewById(R.id.applist_fragment) != null) {
            if (savedInstanceState != null) {
                return;
            }
            frg.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.applist_fragment, frg).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 为ActionBar扩展菜单项
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        mSearchView = (SearchView) menu.findItem(R.id.main_action_search).getActionView();
        setupSearchView();
        return super.onCreateOptionsMenu(menu);
    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(() -> {
            search("");
            return false;
        });
        mSearchView.setQueryHint(getString(R.string.edit_message));
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //implement the filterng techniques
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        search(query);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_action_setting:
                setting();
                return true;
            case R.id.main_action_test:
                Intent intent = new Intent(this, TestActivity.class);
                startActivity(intent);
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
    private void setting() {
        Intent intent = new Intent(this, SettingActivity.class);
        String msg = "helloworld";
        intent.putExtra(EXTRA_MESSAGE, msg);
        startActivity(intent);
    }

    /**
     * 简写toast
     *
     * @param msg 消息
     */
    private void alert(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    //ListView项目点击事件，实现OnFrgActionListener接口
    public void onItemClick(View view) {
        TextView tv = view.findViewById(R.id.applist_package_name);
        String str = tv.getText().toString();
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        assert clm != null;
        clm.setPrimaryClip(ClipData.newPlainText(null, str));
        alert(getString(R.string.clip_toast));
    }

    @Override
    public void onItemLongClick(View view) {
        onItemClick(view);
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String url = "https://m.downloadatoz.com/search.html?q=" + (clm != null ? clm.getPrimaryClip().getItemAt(0).getText() : null);
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        startActivity(intent);
    }

    @Override
    public void onListScroll(boolean flg) {
        if (flg) {
            //上滑
            if (ab.isShowing())
                ab.hide();
        } else {
            if (!ab.isShowing())
                ab.show();
        }
    }

    //查询应用
    private void search(String search) {
        if (frg != null) {
            frg.keyword = search;
            frg.search();
        }
    }
}
