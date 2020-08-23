package com.maxiye.first;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxiye.first.part.ApplistFragment;
import com.maxiye.first.util.CacheUtil;


/**
 * @author due
 */
public class ApplistActivity extends AppCompatActivity implements ApplistFragment.OnFrgActionListener, ActivityCompat.OnRequestPermissionsResultCallback, SearchView.OnQueryTextListener {
    public static final String EXTRA_URL = "com.maxiye.first.URL";

    private ApplistFragment frg;
    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);
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
    protected void onDestroy() {
        super.onDestroy();
        if (CacheUtil.getSize(this, CacheUtil.Unit.MB) > 500) {
            CacheUtil.clearAllCache(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 为ActionBar扩展菜单项
        getMenuInflater().inflate(R.menu.applist_activity_actions, menu);
        SearchView mSearchView = (SearchView) menu.findItem(R.id.main_action_search).getActionView();
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(() -> {
            search("");
            return false;
        });
        mSearchView.setQueryHint(getString(R.string.edit_message));
        return super.onCreateOptionsMenu(menu);
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

    /**
     * 简写toast
     *
     * @param msg 消息
     */
    private void alert(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * ListView项目点击事件，实现OnFrgActionListener接口
     * @param view View
     */
    @Override
    public void onItemClick(View view) {
        TextView tv = view.findViewById(R.id.applist_package_name);
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        assert clm != null;
        clm.setPrimaryClip(ClipData.newPlainText(null, tv.getText().toString()));
        alert(getString(R.string.clip_toast));
    }

    @Override
    public void onItemLongClick(View view) {
        onItemClick(view);
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData;
        if (clm != null && (clipData = clm.getPrimaryClip()) != null) {
            String url = "https://m.downloadatoz.com/search.html?q=" + clipData.getItemAt(0).getText();
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(EXTRA_URL, url);
            startActivity(intent);
        } else {
            alert("Clipboard Is Empty");
        }
    }

    @Override
    public void onListScroll(Slide slide) {
        //上滑
        if (slide.equals(Slide.UP) && ab.isShowing()) {
            ab.hide();
        }
        //下滑
        if (slide.equals(Slide.DOWN) && !ab.isShowing()) {
            ab.show();
        }
    }

    /**
     * 查询应用
     * @param search String
     */
    private void search(String search) {
        if (frg != null) {
            frg.search(search);
        }
    }
}
