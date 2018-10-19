package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.maxiye.first.part.CircleProgressDrawable;
import com.maxiye.first.part.GifWebRvAdapter;
import com.maxiye.first.util.DBHelper;
import com.maxiye.first.util.DiskLRUCache;
import com.maxiye.first.util.NetworkUtil;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.ProtocolException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class GifActivity extends AppCompatActivity implements OnPFListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public static final String GET_NEW_FLG = "GifActivity.getNewFlg";
    public static final String WEB_NAME = "GifActivity.webName";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static String webName = "gamersky";
    private static String type = "bitmap";
    private JsonObject webCfg;
    private boolean isGprs = false;//手机网络
    private boolean gprsContinue = false;//手机网络继续访问
    private static boolean getNewFlg = true;
    private static int artId = 1023742;
    private static String title = "动态图";
    private static int webPage = 1;
    private static boolean endFlg = false;
    private final ArrayList<String[]> gifList = new ArrayList<>();
    private final int HISTORY_PAGE_SIZE = 10;
    private final int FAVORITE_PAGE_SIZE = 20;
    private SQLiteDatabase db;
    private OkHttpClient okHttpClient;
    private ThreadPoolExecutor threadPoolExecutor;
    private DiskLRUCache diskLRUCache;
    private NetworkUtil netUtil;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_gif);
        // Create the adapter that will return a fragment
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.gif_activity_viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        FloatingActionButton fab = findViewById(R.id.gif_activity_fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        //检测是否备份数据库
        DBHelper.checkBakup(this);
        db = new DBHelper(this).getWritableDatabase();//没有则此时创建数据库,生成.db文件
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNewFlg = bundle.getBoolean(GET_NEW_FLG, true);
            webName = bundle.getString(WEB_NAME, "gamersky");
            Log.w("end", getNewFlg ? "true" : "false");
        }
        //网站配置信息
        webCfg = getWebCfg(webName);
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .build();
        }
        threadPoolExecutor = new ThreadPoolExecutor(3, 7, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), (r, executor) -> executor.shutdown());
        //CacheUtil.clearAllCache(this);//清楚所有缓存
        diskLRUCache = DiskLRUCache.getInstance(this, type);
        initPage();
        Log.w("end", "onCreateOver");
    }

    private void initPage() {
        okHttpClient.dispatcher().cancelAll();
        title = "动态图";
        webPage = 1;
        endFlg = false;
        gifList.clear();
        if (mViewPager.getCurrentItem() == 0) {
            if (mSectionsPagerAdapter.currentFragment != null) {
                for (int i = 1;i<4;i++) {
                    GifImageView giv = findViewById(getResources().getIdentifier("gif_" + i, "id", getPackageName()));
                    giv.clearAnimation();
                    giv.setImageResource(R.drawable.ic_image_black_24dp);
                    giv.setMinimumHeight(90);
                    giv.setMinimumWidth(90);
                }
                mSectionsPagerAdapter.currentFragment.refresh();
            }
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    private JsonObject getWebCfg(String webName) {
        JsonObject webCfg = null;
        try {
            JsonObject cfgs = new Gson().fromJson(new InputStreamReader(getAssets().open("img_spy_config.json")), JsonObject.class).getAsJsonObject(type);
            if (webName == null) return cfgs;
            webCfg = cfgs.getAsJsonObject(webName);
            Log.w("JSON", webCfg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webCfg;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        okHttpClient.dispatcher().cancelAll();
        okHttpClient = null;
        netUtil = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isGprs = NetworkUtil.isGprs(this);
        if (isGprs) {
            alert(getText(R.string.gprs_network).toString());
            gprsContinue = false;
        }
        if (netUtil == null) {
            netUtil = new NetworkUtil(this, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    isGprs = NetworkUtil.isGprs(getApplicationContext());
                    if (isGprs) {
                        alert(getText(R.string.gprs_network).toString());
                        gprsContinue = false;
                    }
                    super.onAvailable(network);
                }

                @Override
                public void onLost(Network network) {
                    alert(getText(R.string.no_internet).toString());
                    super.onLost(network);
                }
            });
        } else {
            netUtil.watch();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        netUtil.unwatch();
        diskLRUCache.serialize();
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] pers, @NonNull int[] res) {
        PermissionUtil.res(this, reqCode, pers, res);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gif_activity_actions, menu);
        return true;
    }

    private ArrayList<HashMap<String, Object>> getWebList() {
        JsonObject all_web = getWebCfg(null);
        ArrayList<HashMap<String, Object>> webList = new ArrayList<>();
        for (String key: all_web.keySet()) {
            HashMap<String,Object> item = new HashMap<>();
            item.put("name", key);
            try {
                item.put("icon", BitmapDrawable.createFromStream(getAssets().open(all_web.getAsJsonObject(key).get("local_icon").getAsString()), null));
            } catch (Exception e) {
                e.printStackTrace();
                item.put("icon", getDrawable(R.drawable.ic_image_black_24dp));
            }
            webList.add(item);
        }
        return webList;
    }

    public void refresh(MenuItem item) {
        item.setEnabled(false);
        mSectionsPagerAdapter.currentFragment.refresh();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            item.setEnabled(true);
        }
    }

    @SuppressLint("InflateParams")
    public void skipTo(MenuItem item) {
        //实例化布局
        View view = LayoutInflater.from(this).inflate(R.layout.gif_goto_dialog_edittext, null);
        //找到并对自定义布局中的控件进行操作的示例
        EditText pageEdit = view.findViewById(R.id.gif_dialog_input);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_info_black_24dp)//设置图标
                .setTitle("请输入页码")//设置标题
                .setView(view)//添加布局
                .create();
        //设置按键
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "前往", (dialog1, which) -> {
            String itemIdxStr = pageEdit.getText().toString();
            itemIdxStr = itemIdxStr.equals("") ? "1" : itemIdxStr;
            mViewPager.setCurrentItem(Integer.parseInt(itemIdxStr) - 1, true);
            if (endFlg) checkPageEnd();
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.cancel), (dialog1, which) -> {});
        dialog.show();
        if (dialog.getWindow() != null)
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("InflateParams")
    public void changeUrl(MenuItem item) {
        //实例化布局
        View view2 = LayoutInflater.from(this).inflate(R.layout.gif_goto_dialog_edittext, null);
        //找到并对自定义布局中的控件进行操作的示例
        EditText articleId = view2.findViewById(R.id.gif_dialog_input);
        //创建对话框
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_info_black_24dp)//设置图标
                .setTitle("请输入文章id")//设置标题
                .setView(view2)//添加布局
                .create();
        //设置按键
        dialog2.setButton(AlertDialog.BUTTON_POSITIVE, "前往", (dialog1, which) -> {
            String txt = articleId.getText().toString();
            if (txt.equals("")) {
                alert("文章id不能为空");
            } else {
                artId = Integer.parseInt(txt);
                initPage();
            }
        });
        dialog2.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.cancel), (dialog1, which) -> {});
        dialog2.show();
        if (dialog2.getWindow() != null)
            dialog2.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("InflateParams")
    public void switchWeb(MenuItem item) {
        PopupWindow popupWindow = new PopupWindow(400, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(LayoutInflater.from(this).inflate(R.layout.gif_history_popupwindow_view, null));
        popupWindow.setOutsideTouchable(true);
        RecyclerView rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.gif_rv_divider)));
        rv.addItemDecoration(divider);//分隔线
        rv.setLayoutManager(new LinearLayoutManager(this));
        GifWebRvAdapter ma = new GifWebRvAdapter();
        ArrayList<HashMap<String, Object>> webList = getWebList();
        ma.setData(webList);
        ma.setOnItemClickListener(position -> {
            webName = (String) webList.get(position).get("name");
            Log.w("rvClick", webName);
            webCfg = getWebCfg(webName);
            getNewFlg = true;
            initPage();
            popupWindow.dismiss();
        });
        rv.setAdapter(ma);
        popupWindow.showAtLocation(findViewById(R.id.get_img_ll), Gravity.TOP | Gravity.END, 0, 210);
    }

    public void browserOpen(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.browser_open)
                .setIcon(R.drawable.ic_public_orange_60dp)
                //点击对话框以外的区域是否让对话框消失
                .setCancelable(true)
                //设置正面按钮
                .setPositiveButton("首页", (dialog, which) -> {
                    Uri url = Uri.parse(String.format(webCfg.get("img_web").getAsString(), artId));
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                    dialog.dismiss();
                })
                .setNegativeButton("当前页", (dialog, which) -> {
                    Uri url = Uri.parse(webPage > 1 ? String.format(webCfg.get("img_web_2nd").getAsString(), artId, webPage - 1) : String.format(webCfg.get("img_web").getAsString(), artId));
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                    dialog.dismiss();
                })
                .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public void listHistory(MenuItem item) {
        PopupWindow popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, 1200);
        View root = LayoutInflater.from(this).inflate(R.layout.gif_history_popupwindow_view, null);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(root);
        popupWindow.setOutsideTouchable(true);
        RecyclerView rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.gif_rv_divider)));
        rv.addItemDecoration(divider);//分隔线
        GifWebRvAdapter ma = new GifWebRvAdapter();
        int historyCount = getHistoryCount();
        final ArrayList<HashMap<String, Object>> historyList = new ArrayList<>(HISTORY_PAGE_SIZE);
        //设置页面相关
        EditText page = root.findViewById(R.id.popup_page);
        page.setText("1");
        if (historyCount > HISTORY_PAGE_SIZE) {
            TextView totalPage = root.findViewById(R.id.popup_total_page);
            Button prev = root.findViewById(R.id.popup_prev_page);
            Button next = root.findViewById(R.id.popup_next_page);
            int total = historyCount % HISTORY_PAGE_SIZE == 0 ? historyCount / HISTORY_PAGE_SIZE : historyCount / HISTORY_PAGE_SIZE + 1;
            prev.setOnClickListener(v -> {
                int nowPage = Integer.parseInt(page.getText().toString());
                if (nowPage > 1) {
                    int pre = nowPage - 1;
                    ma.setData(getHistoryList(pre, historyList));
                    ma.notifyDataSetChanged();
                    page.setText(pre + "");
                    next.setVisibility(View.VISIBLE);
                    prev.setVisibility(pre == 1 ? View.GONE : View.VISIBLE);
                }
            });
            next.setOnClickListener(v -> {
                int nxt = Integer.parseInt(page.getText().toString()) + 1;
                ma.setData(getHistoryList(nxt, historyList));
                ma.notifyDataSetChanged();
                page.setText(nxt + "");
                prev.setVisibility(View.VISIBLE);
                next.setVisibility(nxt < total ? View.VISIBLE : View.GONE);
            });
            page.setOnEditorActionListener((textView, i, keyEvent) -> {
                String nowPageStr = page.getText().toString();
                nowPageStr = nowPageStr.equals("") ? "1" : nowPageStr;
                int nowPage = Integer.parseInt(nowPageStr);
                nowPage = nowPage > total ? total : nowPage;
                Log.w("HistoryGoPage", "page:" + nowPage);
                page.setText(nowPage + "");
                ma.setData(getHistoryList(nowPage, historyList));
                ma.notifyDataSetChanged();
                prev.setVisibility(nowPage > 1 ? View.VISIBLE : View.GONE);
                next.setVisibility(nowPage < total ? View.VISIBLE : View.GONE);
                InputMethodManager imm = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                if (imm != null) imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.HIDE_NOT_ALWAYS);
                page.clearFocus();
                return true;
            });
            page.setVisibility(View.VISIBLE);
            totalPage.setVisibility(View.VISIBLE);
            totalPage.setText(" / " + total);
            next.setVisibility(View.VISIBLE);
        }
        ma.setData(getHistoryList(1, historyList));
        ma.setOnItemClickListener(position -> {
            webName = (String) ma.getItemData(position).get("web_name");
            Log.w("rvClick", webName);
            webCfg = getWebCfg(webName);
            artId = (int) ma.getItemData(position).get("art_id");
            initPage();
            popupWindow.dismiss();
        });
        ma.setOnItemLongClickListener(position -> {
            PopupMenu pMenu = new PopupMenu(this, rv.getLayoutManager().findViewByPosition(position));//使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
            pMenu.getMenuInflater().inflate(R.menu.gif_history_popupmenu, pMenu.getMenu());
            pMenu.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.delete_gif_record:
                        //删除记录
                        int delArtId = (int) ma.getItemData(position).get("art_id");
                        String delWebName = (String) ma.getItemData(position).get("web_name");
                        deleteHistory(delArtId, delWebName);
                        historyList.remove(position);
                        ma.notifyItemRemoved(position);
                        ma.notifyItemRangeChanged(position, historyList.size());
                        break;
                }
                return false;
            });
            pMenu.show();
            return true;
        });
        rv.setAdapter(ma);
        popupWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
    }

    private int getHistoryCount() {
        Cursor cus = db.rawQuery("select count(*) from " + DBHelper.TB_IMG_WEB + " where art_id > 0 and type = '" + type + "'", null);
        cus.moveToFirst();
        int count = cus.getInt(0);
        cus.close();
        return count;
    }

    private void deleteHistory(int delArtId, String delWebName) {
        Log.w("deleteHistory", "art_id：" + delArtId + "，web_name：" + delWebName);
        db.delete(DBHelper.TB_IMG_WEB, "art_id = ? and web_name = ?", new String[]{delArtId + "", delWebName});
        db.delete(DBHelper.TB_IMG_WEB_ITEM, "art_id = ? and web_name = ?", new String[]{delArtId + "", delWebName});
    }

    private ArrayList<HashMap<String, Object>> getHistoryList(int page, ArrayList<HashMap<String, Object>> historyList) {
        int offset = (page - 1) * HISTORY_PAGE_SIZE;
        JsonObject all_web = getWebCfg(null);
        String sql = "select * from " + DBHelper.TB_IMG_WEB + " where art_id > 0 and type = '" + type + "' order by id desc limit " + HISTORY_PAGE_SIZE + " offset " + offset;
        Cursor cus = db.rawQuery(sql, null);
        Log.w("getHistoryList：", cus.getCount() + "");
        int count = cus.getCount();
        historyList.clear();
        if (count > 0) {
            cus.moveToFirst();
            for (int i = 0; i < count; i++) {
                HashMap<String,Object> item = new HashMap<>();
                item.put("web_name", cus.getString(cus.getColumnIndex("web_name")));
                item.put("name", cus.getString(cus.getColumnIndex("title")));
                item.put("type", cus.getString(cus.getColumnIndex("type")));
                item.put("web_url", cus.getString(cus.getColumnIndex("web_url")));
                item.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                try {
                    item.put("icon", BitmapDrawable.createFromStream(getAssets().open(all_web.getAsJsonObject((String) item.get("web_name")).get("local_icon").getAsString()), null));
                } catch (Exception e) {
                    e.printStackTrace();
                    item.put("icon", getDrawable(R.drawable.ic_image_black_24dp));
                }
                historyList.add(item);
                cus.moveToNext();
            }
        }
        cus.close();
        return historyList;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public void listFavorite(MenuItem item) {
        PopupWindow popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, 1600);
        View root = LayoutInflater.from(this).inflate(R.layout.gif_history_popupwindow_view, null);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(root);
        popupWindow.setOutsideTouchable(true);
        RecyclerView rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.gif_rv_divider)));
        rv.addItemDecoration(divider);//分隔线
        GifWebRvAdapter ma = new GifWebRvAdapter();
        int favoriteCount = getFavoriteCount();
        final ArrayList<HashMap<String, Object>> favoriteList = new ArrayList<>(FAVORITE_PAGE_SIZE);
        //设置页面相关
        EditText page = root.findViewById(R.id.popup_page);
        page.setText("1");
        if (favoriteCount > FAVORITE_PAGE_SIZE) {
            TextView totalPage = root.findViewById(R.id.popup_total_page);
            Button prev = root.findViewById(R.id.popup_prev_page);
            Button next = root.findViewById(R.id.popup_next_page);
            int total = favoriteCount % FAVORITE_PAGE_SIZE == 0 ? favoriteCount / FAVORITE_PAGE_SIZE : favoriteCount / FAVORITE_PAGE_SIZE + 1;
            prev.setOnClickListener(v -> {
                int nowPage = Integer.parseInt(page.getText().toString());
                if (nowPage > 1) {
                    int pre = nowPage - 1;
                    ma.setData(getFavoriteList(pre, favoriteList));
                    ma.notifyDataSetChanged();
                    page.setText(pre + "");
                    next.setVisibility(View.VISIBLE);
                    prev.setVisibility(pre == 1 ? View.GONE : View.VISIBLE);
                }
            });
            next.setOnClickListener(v -> {
                int nxt = Integer.parseInt(page.getText().toString()) + 1;
                ma.setData(getFavoriteList(nxt, favoriteList));
                ma.notifyDataSetChanged();
                page.setText(nxt + "");
                prev.setVisibility(View.VISIBLE);
                next.setVisibility(nxt < total ? View.VISIBLE : View.GONE);
            });
            page.setOnEditorActionListener((textView, i, keyEvent) -> {
                String nowPageStr = page.getText().toString();
                nowPageStr = nowPageStr.equals("") ? "1" : nowPageStr;
                int nowPage = Integer.parseInt(nowPageStr);
                nowPage = nowPage > total ? total : nowPage;
                Log.w("FavoriteGoPage", "page:" + nowPage);
                page.setText(nowPage + "");
                ma.setData(getFavoriteList(nowPage, favoriteList));
                ma.notifyDataSetChanged();
                prev.setVisibility(nowPage > 1 ? View.VISIBLE : View.GONE);
                next.setVisibility(nowPage < total ? View.VISIBLE : View.GONE);
                InputMethodManager imm = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
                if (imm != null) imm.toggleSoftInput(InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.HIDE_NOT_ALWAYS);
                page.clearFocus();
                return true;
            });
            page.setVisibility(View.VISIBLE);
            totalPage.setVisibility(View.VISIBLE);
            totalPage.setText(" / " + total);
            next.setVisibility(View.VISIBLE);
        }
        ma.setData(getFavoriteList(1, favoriteList));
        ma.setOnItemClickListener(position -> viewFav(favoriteList, position, root));
        ma.setOnItemLongClickListener(position -> {
            PopupMenu pMenu = new PopupMenu(this, rv.getLayoutManager().findViewByPosition(position));//使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
            pMenu.getMenuInflater().inflate(R.menu.gif_history_popupmenu, pMenu.getMenu());
            pMenu.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.delete_gif_record:
                        //删除记录
                        String id = (String) ma.getItemData(position).get("id");
                        String itemId = (String) ma.getItemData(position).get("item_id");
                        deleteFavorite(id, itemId);
                        favoriteList.remove(position);
                        ma.notifyItemRemoved(position);
                        ma.notifyItemRangeChanged(position, favoriteList.size());
                        break;
                }
                return false;
            });
            pMenu.show();
            return true;
        });
        rv.setAdapter(ma);
        popupWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
    }

    private int getFavoriteCount() {
        Cursor cus = db.rawQuery("select count(*) from " + DBHelper.TB_IMG_FAVORITE + " where type = '" + type + "'", null);
        cus.moveToFirst();
        int count = cus.getInt(0);
        cus.close();
        return count;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void viewFav(ArrayList<HashMap<String, Object>> favoriteList, int position, View root) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
        GifImageView imgView = new GifImageView(this);
        imgView.setImageDrawable(getDrawable(R.drawable.ic_image_black_24dp));
        GifActivity activity = this;
        imgView.setOnTouchListener(new View.OnTouchListener() {
            int storePos = position;
            float mPosX;
            float mPosY;
            float mCurPosX;
            float mCurPosY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPosX = mCurPosX = event.getX();
                        mPosY = mCurPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        mCurPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
//                        if (mCurPosY - mPosY > 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {//向下滑動
//                        } else if (mCurPosY - mPosY < 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {//向上滑动
//                        }
                        if (mCurPosY - mPosY > 300) dialog.dismiss();
                        if (mCurPosX - mPosX > 150) {//向左滑動
                            viewPre();
                        } else if (mCurPosX - mPosX < -150) {//向右滑动
                            viewNext();
                        }
                        break;
                }
                return true;
            }

            private void viewPre() {
                if (storePos > 0) {
                    storePos--;
                } else {
                    Button button = root.findViewById(R.id.popup_prev_page);
                    if (button.getVisibility() == Button.VISIBLE) {
                        button.performClick();
                        storePos = FAVORITE_PAGE_SIZE - 1;
                    } else {
                        Toast.makeText(activity, getString(R.string.no_more), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                loadFavImg(imgView, favoriteList.get(storePos));
            }

            private void viewNext() {
                if (storePos < favoriteList.size() - 1) {
                    storePos++;
                } else {
                    Button button = root.findViewById(R.id.popup_next_page);
                    if (button.getVisibility() == Button.VISIBLE) {
                        storePos = 0;
                        button.performClick();
                    } else {
                        Toast.makeText(activity, getString(R.string.no_more), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                loadFavImg(imgView, favoriteList.get(storePos));
            }
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(imgView);
        loadFavImg(imgView, favoriteList.get(position));
        dialog.show();
    }

    private void loadFavImg(GifImageView imgView, HashMap<String, Object> item) {
        String title = (String) item.get("name");
        String url = (String) item.get("real_url");
        url = url.equals("") ? (String) item.get("url") : url;
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
        new ImgViewTask(imgView, this).execute(title, url, (String) item.get("id"));
    }

    static class ImgViewTask extends AsyncTask<String, Integer, Drawable> {

        private WeakReference<GifActivity> gifActivityWR;
        private WeakReference<GifImageView> imgViewWR;
        private File cache;

        ImgViewTask(GifImageView imgView, GifActivity gifActivity) {
            imgViewWR = new WeakReference<>(imgView);
            gifActivityWR = new WeakReference<>(gifActivity);
        }

        /**
         * 加载图片
         * @param strings title,url,id
         * @return Drawable
         */
        @Override
        protected Drawable doInBackground(String... strings) {
            GifActivity activity = gifActivityWR.get();
            String imgKey = "favorite_" + strings[2] + "_" + type;
            cache = activity.diskLRUCache.get(imgKey);
            if (cache != null) {
                try {
                    Log.w("loadImg(fromCache)", "title：" + strings[0] + ";url：" + strings[1]);
                    return type.equals("gif") ? new GifDrawable(cache) : BitmapDrawable.createFromPath(cache.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                cache = new File(activity.getCacheDir(), imgKey);
                Request request = new Request.Builder().url(strings[1]).build();
                try {
                    Log.w("loadImg(fromNet)", "title：" + strings[0] + ";url：" + strings[1]);
                    Response response = activity.okHttpClient.newCall(request).execute();
                    ResponseBody responseBody = response.body();
                    assert responseBody != null;
                    int contentLength = (int) responseBody.contentLength();
                    byte[] bytes;
                    if (contentLength > 0 && contentLength > 40960 * 2) {
                        publishProgress(0, contentLength);
                        InputStream is = responseBody.byteStream();
                        bytes = new byte[contentLength];
                        int len;int sum = 0;int readLen = contentLength > 5 * 1024 * 1024 ? 102400 : 40960;
                        while ((len = is.read(bytes, sum, readLen)) > 0) {
                            sum += len;
                            readLen = readLen > bytes.length - sum ? bytes.length - sum : readLen;
                            publishProgress(sum, contentLength);
                        }
                    } else {
                        bytes = responseBody.bytes();
                    }
                    Drawable gifFromStream = type.equals("gif") ? new GifDrawable(bytes) : BitmapDrawable.createFromStream(new ByteArrayInputStream(bytes), null);
                    if (gifFromStream == null) throw new Exception("get image err");
                    RandomAccessFile raf = new RandomAccessFile(cache, "rwd");
                    raf.write(bytes);
                    raf.close();
                    gifActivityWR.get().diskLRUCache.put(imgKey, imgKey, cache.length());
                    return gifFromStream;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cache.delete()) Log.d("cacheDel", "cacheGif deleted");
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            GifImageView imgView = imgViewWR.get();
            CircleProgressDrawable circleProgress = new CircleProgressDrawable.Builder()
                    .capacity(100)
                    .color(gifActivityWR.get().getColor(R.color.myPrimaryColor))
                    .build();
            assert imgView != null;
            imgView.setImageDrawable(circleProgress);
            imgView.setMinimumHeight(180);
            imgView.setMinimumWidth(180);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            GifImageView imgView = imgViewWR.get();
            assert imgView != null;
            if (drawable == null) {
                Drawable errShow = gifActivityWR.get().getDrawable(R.drawable.ic_close_black_24dp);
                imgView.setImageDrawable(errShow);
            } else {
                imgView.setImageDrawable(drawable);
                float zoom = type.equals("gif") ? 4.5f : 6.5f;
                int width = Math.round(drawable.getIntrinsicWidth() * zoom);
                int height = Math.round(drawable.getIntrinsicHeight() * zoom);
                imgView.setMinimumHeight(height);
                imgView.setMinimumWidth(width);
            }
            super.onPostExecute(drawable);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            GifImageView imgView = imgViewWR.get();
            assert imgView != null;
            CircleProgressDrawable circleProgress = (CircleProgressDrawable) imgView.getDrawable();
            circleProgress.setMaxProgress(values[1]);
            circleProgress.setCurProgress(values[0]);
            super.onProgressUpdate(values);
        }
    }

    private void deleteFavorite(String delId, String itemId) {
        ContentValues ctv = new ContentValues();
        ctv.put("fav_flg", 0);
        int rows = db.update(DBHelper.TB_IMG_WEB_ITEM, ctv, "id = ?", new String[]{itemId});
        Log.w("db_item_remove_fav: ", rows + "");
        db.delete(DBHelper.TB_IMG_FAVORITE, "id = ?", new String[]{delId});
        Log.w("deleteFavorite", "id：" + delId);
    }

    private ArrayList<HashMap<String, Object>> getFavoriteList(int page, ArrayList<HashMap<String, Object>> favoriteList) {
        int offset = (page - 1) * FAVORITE_PAGE_SIZE;
        String sql = "select * from " + DBHelper.TB_IMG_FAVORITE + " where type = '" + type + "' order by id desc limit " + FAVORITE_PAGE_SIZE + " offset " + offset;
        Cursor cus = db.rawQuery(sql, null);
        Log.w("getFavoriteList：", cus.getCount() + "");
        favoriteList.clear();
        int count = cus.getCount();
        if (count > 0) {
            cus.moveToFirst();
            for (int i = 0; i < count; i++) {
                HashMap<String,Object> item = new HashMap<>();
                item.put("id", cus.getString(cus.getColumnIndex("id")));
                item.put("item_id", cus.getString(cus.getColumnIndex("item_id")));
                item.put("name", cus.getString(cus.getColumnIndex("title")));
                item.put("type", cus.getString(cus.getColumnIndex("type")));
                item.put("url", cus.getString(cus.getColumnIndex("url")));
                item.put("real_url", cus.getString(cus.getColumnIndex("real_url")));
                item.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                try {
                    item.put("icon", getDrawable(R.drawable.ic_image_black_24dp));
                } catch (Exception e) {
                    e.printStackTrace();
                    item.put("icon", getDrawable(R.drawable.ic_image_black_24dp));
                }
                favoriteList.add(item);
                cus.moveToNext();
            }
        }
        cus.close();
        return favoriteList;
    }

    @SuppressLint("InflateParams")
    public void switchType(MenuItem item) {
        PopupWindow popupWindow = new PopupWindow(400, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(LayoutInflater.from(this).inflate(R.layout.gif_history_popupwindow_view, null));
        popupWindow.setOutsideTouchable(true);
        RecyclerView rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.gif_rv_divider)));
        rv.addItemDecoration(divider);//分隔线
        rv.setLayoutManager(new LinearLayoutManager(this));
        GifWebRvAdapter ma = new GifWebRvAdapter();
        ArrayList<HashMap<String, Object>> typeList = getTypeList();
        ma.setData(typeList);
        ma.setOnItemClickListener(position -> {
            type = (String) typeList.get(position).get("name");
            okHttpClient.dispatcher().cancelAll();
            Log.w("typeListRvClick", type);
            webCfg = getWebCfg(webName);
            if (webCfg == null) webCfg = getWebCfg("gamersky");
            getNewFlg = true;
            diskLRUCache.serialize();
            diskLRUCache = DiskLRUCache.getInstance(this, type);
            initPage();
            popupWindow.dismiss();
        });
        rv.setAdapter(ma);
        popupWindow.showAtLocation(findViewById(R.id.get_img_ll), Gravity.TOP | Gravity.END, 0, 210);
    }

    private ArrayList<HashMap<String, Object>> getTypeList() {
        ArrayList<HashMap<String, Object>> typeList = new ArrayList<>();
        String[] types = new String[]{"gif", "bitmap"};
        for (String type : types) {
            HashMap<String, Object> typeItem = new HashMap<>();
            typeItem.put("name", type);
            typeItem.put("icon", getDrawable(R.drawable.ic_image_black_24dp));
            typeList.add(typeItem);
        }
        return typeList;
    }

    @Override
    public void checkPageEnd() {
        int nowPage = mViewPager.getCurrentItem() + 1;
        int pages = gifList.size() % 3 == 0 ? gifList.size() / 3 : gifList.size() / 3 + 1;
        if (nowPage > pages) {
            alert("已自动跳回最后一页...");
            mViewPager.setCurrentItem(pages - 1, true);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private int gifPosition = 1;
        private int focusedPosition = 1;
        private static final int MSG_TYPE_PRE = 100;
        private static final int MSG_TYPE_LOAD = 101;
        private static final int MSG_TYPE_DOWNLOADED = 102;
        private static final int MSG_TYPE_DOWNLOAD_ERR = 103;
        private static final int MSG_TYPE_EMPTY = 104;
        private static final int MSG_TYPE_OVER = 105;
        private static final int MSG_TYPE_LOADING = 106;
        private GifActivity activity;
        private OnPFListener mListener;
        private MyHandler myHandler;

        public PlaceholderFragment() {}

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (myHandler != null) myHandler.removeCallbacksAndMessages(null);
            myHandler = null;
            mListener = null;
            activity = null;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @SuppressLint("SetTextI18n")
        static PlaceholderFragment newInstance(int sectionNumber, GifActivity activity) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            fragment.myHandler = new MyHandler(fragment);
            fragment.activity = activity;
            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            if (context instanceof OnPFListener) {
                mListener = (OnPFListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnPFListener");
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_get_gif, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            assert getArguments() != null;
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            for (int i = 1;i<4;i++) {
                int pos = i;
                GifImageView giv = rootView.findViewById(getResources().getIdentifier("gif_" + i, "id", activity.getPackageName()));
                giv.setMinimumHeight(90);
                giv.setMinimumWidth(90);
                giv.setOnLongClickListener(view -> {
                    longClickCb(pos, rootView);
                    return true;
                });
            }
            return rootView;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            activity.okHttpClient.dispatcher().cancelAll();
            if (myHandler != null) myHandler.removeCallbacksAndMessages(null);
            if (isVisibleToUser) {
                if (activity.threadPoolExecutor.getActiveCount() == activity.threadPoolExecutor.getMaximumPoolSize()) {
                    activity.threadPoolExecutor.shutdownNow();
                    Log.w("threadPoolExcutor", "shutdownNow");
                }
                if (gifPosition == 1) {
                    checkLoad();
                }
            }
        }
        public void send(int what, Object obj) {
            if (myHandler != null) {
                myHandler.sendMessage(myHandler.obtainMessage(what, obj));
            }
        }
        public void send(int what, int arg1, int arg2, Object obj) {
            if (myHandler != null) {
                myHandler.sendMessage(myHandler.obtainMessage(what, arg1, arg2, obj));
            }
        }

        private int getGifOffset(int index) {
            assert getArguments() != null;
            return (getArguments().getInt(ARG_SECTION_NUMBER) - 1) * 3 + index - 1;
        }

        private void longClickCb(int position, View view) {
            focusedPosition = position;
            ListPopupWindow listMenu = new ListPopupWindow(activity);
            listMenu.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, new
                    String[]{getString(R.string.add_to_fav), getString(R.string.download)}));
            DisplayMetrics  dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            listMenu.setAnchorView(view);
            listMenu.setWidth(dm.widthPixels * 2 / 3);
            listMenu.setHorizontalOffset(dm.widthPixels / 6);
            listMenu.setVerticalOffset(dm.heightPixels / 2);
            listMenu.setOnItemClickListener((parent, view1, position1, id) -> {
                switch (position1) {
                    case 0:
                        addFav();
                        break;
                    case 1:
                        download();
                        break;
                }
                listMenu.dismiss();
            });
            listMenu.show();
        }

        private void addFav() {
            String[] gifInfo = getGifInfo(getGifOffset(focusedPosition));
            Cursor cus = activity.db.query(DBHelper.TB_IMG_WEB_ITEM, new String[]{"*"}, "art_id = ? and title = ?", new String[]{artId + "", gifInfo[1]}, null, null, "id desc", "1");
            if (cus.getCount() > 0) {
                cus.moveToFirst();
                int favFlg = cus.getInt(cus.getColumnIndex("fav_flg"));
                if (favFlg != 1) {
                    ContentValues ctv = new ContentValues();
                    ctv.put("item_id", cus.getInt(cus.getColumnIndex("id")));
                    ctv.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                    ctv.put("page", cus.getInt(cus.getColumnIndex("page")));
                    ctv.put("web_name", cus.getString(cus.getColumnIndex("web_name")));
                    ctv.put("type", cus.getString(cus.getColumnIndex("type")));
                    ctv.put("title", cus.getString(cus.getColumnIndex("title")));
                    ctv.put("url", cus.getString(cus.getColumnIndex("url")));
                    ctv.put("ext", cus.getString(cus.getColumnIndex("ext")));
                    ctv.put("real_url", cus.getString(cus.getColumnIndex("real_url")));
                    ctv.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                    long newId = activity.db.insert(DBHelper.TB_IMG_FAVORITE, null, ctv);
                    Log.w("db_img_fav_insert: ", newId + "");
                    ContentValues ctv2 = new ContentValues();
                    ctv2.put("fav_flg", 1);
                    int rows = activity.db.update(DBHelper.TB_IMG_WEB_ITEM, ctv2, "id = ?", new String[]{cus.getString(cus.getColumnIndex("id"))});
                    Log.w("db_web_item_update: ", rows + "");
                    activity.alert(getString(R.string.added_to_fav));
                } else {
                    activity.alert(getString(R.string.already_in_fav));
                }
                cus.close();
            }
        }

        private void download() {
            addFav();
            Log.w("download", "download:start");
            PermissionUtil.req(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.PER_REQ_STORAGE_WRT, () -> {
                Toast.makeText(activity, "开始下载文件...", Toast.LENGTH_SHORT).show();
                activity.threadPoolExecutor.execute(() -> {
                    String[] gifInfo = getGifInfo(getGifOffset(focusedPosition));
                    File gif = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type + "/" + gifInfo[1]);
                    Log.w("download", "download:path:" + gif.getAbsolutePath());
                    try {
                        if (!gif.exists()) {
                            if (!gif.getParentFile().exists() && !gif.getParentFile().mkdirs()) {
                                Log.w("doing", gif.getParentFile().getAbsolutePath());
                                throw new Exception("create dir error");
                            }
                            if (!gif.createNewFile()) {
                                throw new Exception("create file error");
                            }
                        }
                        File cacheGif = activity.diskLRUCache.get(webName + "_" + artId + "-" + getGifOffset(focusedPosition));
                        if (!cacheGif.exists())
                            throw new Exception("未发现缓存文件");
                        if (Build.VERSION.SDK_INT > 26) {
                            Files.copy(cacheGif.toPath(), gif.toPath());
                        } else {
                            try {
                                FileInputStream input = new FileInputStream(cacheGif);
                                FileOutputStream output = new FileOutputStream(gif);
                                byte[] buf = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = input.read(buf)) > 0) {
                                    output.write(buf, 0, bytesRead);
                                }
                                input.close();
                                output.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //发送广播
                        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        scanIntent.setData(Uri.fromFile(gif));
                        activity.sendBroadcast(scanIntent);
                        send(MSG_TYPE_DOWNLOADED, gif);
                    } catch (Exception e) {
                        send(MSG_TYPE_DOWNLOAD_ERR, "下载失败");
                        e.printStackTrace();
                    }
                });
            });
        }

        void checkLoad() {
            String imgKey = webName + "_" + artId + "-" + getGifOffset(gifPosition);
            //移动网络禁止 gif
            if (activity.isGprs && type.equals("gif") && !activity.diskLRUCache.containsKey(imgKey)) {
                if (!activity.gprsContinue) {
                    @SuppressLint("InflateParams")
                    View view = LayoutInflater.from(activity).inflate(R.layout.gif_gprs_popup_confirm, null);
                    PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    view.findViewById(R.id.bt_ok).setOnClickListener(v -> {
                        activity.threadPoolExecutor.execute(this::loadGif);
                        activity.gprsContinue = true;
                        popupWindow.dismiss();
                    });
                    view.findViewById(R.id.bt_cancel).setOnClickListener(v -> popupWindow.dismiss());
                    popupWindow.showAtLocation(getView(), Gravity.CENTER,0,0);
                } else {
                    activity.threadPoolExecutor.execute(this::loadGif);
                }
                activity.alert("正在使用移动网络");
            } else {
                activity.threadPoolExecutor.execute(this::loadGif);
            }
        }

        void loadGif() {
            int nowPos = gifPosition;
            int startOffset = getGifOffset(nowPos);
            Log.w("info", "loadGif:" + startOffset);
            String[] gifInfo = getGifInfo(startOffset);
            if (gifInfo == null)
                return;
            String imgKey = webName + "_" + artId + "-" + startOffset;
            File cacheGif = activity.diskLRUCache.get(imgKey);
            send(MSG_TYPE_PRE, nowPos, 0, gifInfo[1]);
            if (cacheGif != null) {
                try {
                    Drawable gifFromStream = type.equals("gif") ? new GifDrawable(cacheGif) : BitmapDrawable.createFromPath(cacheGif.getAbsolutePath());
                    send(MSG_TYPE_LOAD, nowPos, 0, gifFromStream);
                    Log.w("loadGif", "fromCache:" + gifInfo[1]);
                } catch (IOException e) {
                    send(MSG_TYPE_EMPTY, nowPos, 0, "");
                    e.printStackTrace();
                }
            } else {
                String url = gifInfo[3].equals("") ? gifInfo[0] : gifInfo[3];
                Request request = new Request.Builder().url(url).build();
                activity.okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        send(MSG_TYPE_EMPTY, nowPos, 0, "");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        try {
                            Log.w("info", "loadGif(fromNet):" + gifInfo[1] + ";url:" + url + ";index:" + request.toString());
                            ResponseBody responseBody = response.body();
                            assert responseBody != null;
                            int contentLength = (int) responseBody.contentLength();
                            byte[] bytes;
                            if (contentLength > 0 && contentLength > 40960 * 2) {
                                send(MSG_TYPE_LOADING, nowPos, 0, contentLength);
                                InputStream is = responseBody.byteStream();
                                bytes = new byte[contentLength];
                                int len;int sum = 0;int readLen = contentLength > 5 * 1024 * 1024 ? 102400 : 40960;
                                while ((len = is.read(bytes, sum, readLen)) > 0) {
                                    sum += len;
                                    readLen = readLen > bytes.length - sum ? bytes.length - sum : readLen;
                                    send(MSG_TYPE_LOADING, nowPos, 0, sum);
                                }
                            } else {
                                bytes = responseBody.bytes();
                            }
                            Drawable gifFromStream = type.equals("gif") ? new GifDrawable(bytes) : BitmapDrawable.createFromStream(new ByteArrayInputStream(bytes), null);
                            if (gifFromStream == null) throw new Exception("get image err");
                            send(MSG_TYPE_LOAD, nowPos, 0, gifFromStream);
                            RandomAccessFile raf = new RandomAccessFile(finalCacheGif, "rwd");
                            raf.write(bytes);
                            raf.close();
                            activity.diskLRUCache.put(imgKey, imgKey + gifInfo[2], finalCacheGif.length());
                        } catch (Exception e) {
                            if (finalCacheGif.delete()) Log.d("cacheDel", "cacheGif deleted");
                            send(MSG_TYPE_EMPTY, nowPos, 0, "");
                            e.printStackTrace();
                        }
                    }
                    File finalCacheGif = new File(activity.getCacheDir(), imgKey + gifInfo[2]);
                });
            }

        }

        private synchronized void loadGifList() {
            //数据库获取
            if (webPage == 1) {
                if (getNewFlg)
                    getNewArtId();
                if (loadDbGifList())
                    return;
            }
            if (endFlg) return;
            Log.w("start", "loadGifList(lock):" + webPage);
            String baseUrl = String.format(activity.webCfg.get("img_web").getAsString(), artId);
            JsonObject regObj = activity.webCfg.getAsJsonObject("img_reg");
            int urlIdx = regObj.get("img_url_idx").getAsInt();
            int extIdx = regObj.get("img_ext_idx").getAsInt();
            int titleIdx = regObj.get("img_title_idx").getAsInt();
            int realUrlIdx = regObj.get("img_real_url_idx") != null ? regObj.get("img_real_url_idx").getAsInt() : -1;
            Pattern pt = Pattern.compile(regObj.get("reg").getAsString(), Pattern.CASE_INSENSITIVE);
            try {
                String url = webPage > 1 ? String.format(activity.webCfg.get("img_web_2nd").getAsString(), artId, webPage) : baseUrl;
                System.out.println(url);
                Request req = new Request.Builder().url(url).build();
                ResponseBody responseBody = activity.okHttpClient.newCall(req).execute().body();
                assert responseBody != null;
                String content = new String(responseBody.bytes(), "utf-8");
                Matcher mt = pt.matcher(content);
                if (!mt.find()) {
                    System.out.print(content);
                    throw new ProtocolException("over");
                }
                mt.reset();
                while (mt.find()) {
                    String ext = mt.group(extIdx);
                    String name = getTitle(mt.group(titleIdx)) + ext;
                    String realUrl = realUrlIdx == -1 ? "" : mt.group(realUrlIdx);
                    realUrl = realUrl == null ? "" : realUrl;
                    System.out.println("title: " + name + ";url: " + mt.group(urlIdx) + ";ext: " + ext + ";realUrl: " + realUrl);
                    String gifUrl = mt.group(urlIdx);
                    if (webName.contains("duowan")) {
                        name = Util.unicode2Chinese(name);
                        gifUrl = gifUrl.replace("\\", "");
                    }
                    String[] gifInfo = new String[]{gifUrl, name, ext, realUrl};
                    activity.gifList.add(gifInfo);
                    saveDbGifList(DBHelper.TB_IMG_WEB_ITEM, gifInfo);
                }
                if (webPage == 1 && activity.gifList.size() > 0) {
                    Log.w("titleGet", title);
                    if (title.equals("动态图")) getNewTitle(content);
                    saveDbGifList(DBHelper.TB_IMG_WEB, new String[]{url, title});
                }
                webPage++;
            } catch (Exception e) {//java.net.ProtocolException: Too many follow-up requests: 21
                if (e instanceof ProtocolException) {
                    endFlg = true;
                    if (activity.gifList.size() > 0) updateDbField(String.valueOf(webPage - 1));
                    send(MSG_TYPE_OVER, "");
                }
                e.printStackTrace();
            } finally {
                Log.w("end", "loadGifList(unlock):end:" + webPage);
                System.out.println(activity.gifList.toString());
            }
        }

        private String getTitle(String group) {
            boolean notNull =  group != null && !group.replaceAll("[\r\n\\s\t]", "").equals("");
            return notNull ? group : UUID.randomUUID().toString();
        }

        private void getNewArtId() {
            Log.w("getNewArtId", "获取最新内容...");
            String url = activity.webCfg.get("spy_root").getAsString();
            JsonObject regObj = activity.webCfg.getAsJsonObject("img_web_reg");
            int artIdIdx = regObj.get("art_id_idx").getAsInt();
            int titleIdx = regObj.get("title_idx").getAsInt();
            Pattern pt = Pattern.compile(regObj.get("reg").getAsString(), Pattern.CASE_INSENSITIVE);
            try {
                Request req = new Request.Builder().url(url).build();
                ResponseBody responseBody = activity.okHttpClient.newCall(req).execute().body();
                assert responseBody != null;
                String content = new String(responseBody.bytes(), "utf-8");
                Matcher mt = pt.matcher(content);
//                System.out.println(content);
                if (mt.find()) {
                    title = mt.group(titleIdx);
                    artId = Integer.parseInt(mt.group(artIdIdx));
                    endFlg = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                getNewFlg = false;
                Log.w("getNewArtId", title);
                System.out.println(title);
            }
        }

        private void getNewTitle(String content) {
            Log.w("getNewTitle", "获取标题...");
            JsonObject regObj = activity.webCfg.getAsJsonObject("title_reg");
            int titleIdx = regObj.get("title_idx").getAsInt();
            Pattern pt = Pattern.compile(regObj.get("reg").getAsString(), Pattern.CASE_INSENSITIVE);
            if (content == null) {
                content = "";
                Request req = new Request.Builder().url(String.format(activity.webCfg.get("img_web").getAsString(), artId)).build();
                try {
                    ResponseBody responseBody = activity.okHttpClient.newCall(req).execute().body();
                    assert responseBody != null;
                    content = new String(responseBody.bytes(), "utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Matcher mt = pt.matcher(content);
            if (mt.find()) {
                title = mt.group(titleIdx);
                Log.w("getNewTitle", title);
            }
        }

        private boolean loadDbGifList() {
            Cursor cus = activity.db.query(DBHelper.TB_IMG_WEB, new String[]{"*"}, "art_id = ? and web_name = ?", new String[]{artId + "", webName}, null, null, "id desc", "1");
            Log.w("db_web", cus.getCount() + "");
            if (cus.getCount() > 0) {
                cus.moveToFirst();
                title = cus.getString(cus.getColumnIndex("title"));
                int totalPage = cus.getInt(cus.getColumnIndex("pages"));
                Cursor cus2 = activity.db.query(DBHelper.TB_IMG_WEB_ITEM, new String[]{"page,title,url,ext,real_url"}, "art_id = ? and web_name = ?", new String[]{artId + "", webName}, null, null, "id asc");
                int count = cus2.getCount();
                if (count > 0) {
                    Log.w("db_item", count + "");
                    cus2.moveToFirst();
                    for (int i = 0; i < count; i++) {
                        String[] gifInfo = new String[4];
                        gifInfo[0] = cus2.getString(cus2.getColumnIndex("url"));
                        gifInfo[1] = cus2.getString(cus2.getColumnIndex("title"));
                        gifInfo[2] = cus2.getString(cus2.getColumnIndex("ext"));
                        gifInfo[3] = cus2.getString(cus2.getColumnIndex("real_url"));
                        activity.gifList.add(gifInfo);
                        webPage = cus2.getInt(cus2.getColumnIndex("page"));
                        cus2.moveToNext();
                    }
                }
                cus2.close();
                if (totalPage == webPage)
                    endFlg = true;
                webPage++;
                return true;
            }
            cus.close();
            return false;
        }


        private void updateDbField(String val) {
            ContentValues ctv = new ContentValues();
            ctv.put("pages", val);
            int rows = activity.db.update(DBHelper.TB_IMG_WEB, ctv, "art_id = ? and web_name = ?", new String[]{artId + "",webName});
            Log.w("db_web_update: ", rows + "");
        }

        private void saveDbGifList(String dbName, String[] data) {
            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            ContentValues ctv = new ContentValues();
            if (dbName.equals(DBHelper.TB_IMG_WEB)) {
                ctv.put("art_id", artId);
                ctv.put("web_url", data[0]);
                ctv.put("web_name", webName);
                ctv.put("type", type);
                ctv.put("title", data[1]);
                ctv.put("time", datetime);
                long newId = activity.db.insert(dbName, null, ctv);
                Log.w("db_web_insert: ", newId + "");
            } else {
                ctv.put("art_id", artId);
                ctv.put("page", webPage);
                ctv.put("web_name", webName);
                ctv.put("type", type);
                ctv.put("title", data[1]);
                ctv.put("url", data[0]);
                ctv.put("ext", data[2]);
                ctv.put("real_url", data[3]);
                ctv.put("time", datetime);
                long newId = activity.db.insert(dbName, null, ctv);
                Log.w("db_web_item_insert: ", newId + "");
            }

        }

        String[] getGifInfo(int offset) {
            Log.w("start", "getGifInfo:" + offset + "-" + webPage + "-" + activity.gifList.size() + "-" + endFlg);
            if (activity.gifList.size() <= offset) {
                if (endFlg)
                    return null;
                loadGifList();
                return getGifInfo(offset);
            } else {
                return activity.gifList.get(offset);
            }
        }

        void refresh() {
//            CacheUtil.clearAllCache(activity);
            activity.okHttpClient.dispatcher().cancelAll();
            this.gifPosition = 1;
            if (activity.threadPoolExecutor.getActiveCount() == activity.threadPoolExecutor.getPoolSize()) {
                activity.threadPoolExecutor.shutdownNow();
            }
            checkLoad();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {
        PlaceholderFragment currentFragment;
        GifActivity activity;

        SectionsPagerAdapter(FragmentManager fm, GifActivity activity) {
            super(fm);
            this.activity = activity;
        }

        @Override
        public Fragment getItem(int position) {
            Log.w("start", "getItem:" + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, activity);
        }

        @Override
        public int getCount() {
            return 60;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentFragment = (PlaceholderFragment) object;
            super.setPrimaryItem(container, position, object);
        }
    }
    private static class MyHandler extends Handler {
        private final WeakReference<PlaceholderFragment> mFragment;

        MyHandler(PlaceholderFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            PlaceholderFragment fragment = mFragment.get();
            GifActivity context = fragment.activity;
            View rootView = fragment.getView();
            if (rootView == null) return;
            TextView textView = rootView.findViewById(R.id.section_label);
            assert fragment.getArguments() != null;
            textView.setText(title + "：" + fragment.getArguments().getInt(PlaceholderFragment.ARG_SECTION_NUMBER));
            Drawable errShow = context.getDrawable(R.drawable.ic_close_black_24dp);
            switch (msg.what) {
                case PlaceholderFragment.MSG_TYPE_PRE:
                    TextView tv = rootView.findViewById(fragment.getResources().getIdentifier("gtxt_" + msg.arg1, "id", context.getPackageName()));
                    tv.setText((String) msg.obj);
                    int startOffset = fragment.getGifOffset(msg.arg1);
                    if (!context.diskLRUCache.containsKey(webName + "_" + artId + "-" + startOffset)) {
                        GifImageView iv1 = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", context.getPackageName()));
                        iv1.setImageDrawable(context.getDrawable(R.drawable.ic_autorenew_black_24dp));
                        iv1.setMinimumHeight(90);
                        iv1.setMinimumWidth(90);
                        iv1.setAnimation(AnimationUtils.loadAnimation(context, R.anim.load_rotate));
                    }
                    if (++fragment.gifPosition < 4) {
                        fragment.checkLoad();
                    } else {
                        fragment.gifPosition = 1;
                    }
                    break;
                case PlaceholderFragment.MSG_TYPE_LOADING:
                    GifImageView iv2 = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", context.getPackageName()));
                    if (iv2.getDrawable() instanceof  CircleProgressDrawable) {
                        CircleProgressDrawable circleProgress = (CircleProgressDrawable) iv2.getDrawable();
                        circleProgress.setCurProgress((int) msg.obj);
                    } else {
                        CircleProgressDrawable circleProgress = new CircleProgressDrawable.Builder()
                                .capacity((int) msg.obj)
                                .color(context.getColor(R.color.actionTitle))
                                .build();
                        iv2.clearAnimation();
                        iv2.setImageDrawable(circleProgress);
                    }
                    break;
                case PlaceholderFragment.MSG_TYPE_LOAD:
                    GifImageView iv3 = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", context.getPackageName()));
                    iv3.clearAnimation();
                    Drawable gifFromStream = (Drawable) msg.obj;
                    float zoom = type.equals("gif") ? 2.5f : 4.5f;
                    iv3.setImageDrawable(gifFromStream);
                    int width = Math.round(gifFromStream.getIntrinsicWidth() * zoom);
                    int height = Math.round(gifFromStream.getIntrinsicHeight() * zoom);
                    int layoutWidth = rootView.getWidth();
                    height = width > layoutWidth ? height * layoutWidth / width : height;
                    iv3.setMinimumHeight(height);
                    iv3.setMinimumWidth(width);
                    break;
                case PlaceholderFragment.MSG_TYPE_OVER:
                    fragment.mListener.checkPageEnd();
                    break;
                case PlaceholderFragment.MSG_TYPE_DOWNLOADED:
                    File gif = (File) msg.obj;
                    Snackbar.make(rootView, "下载完成", Snackbar.LENGTH_SHORT)
                            .setAction("打开", v -> {
                                Intent imgView = new Intent(Intent.ACTION_VIEW);
                                imgView.setDataAndType(FileProvider.getUriForFile(context, "com.maxiye.first.fileprovider", gif), "image/*");
                                imgView.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//不加黑屏读取不了
                                context.startActivity(imgView);
                            }).show();
                    break;
                case PlaceholderFragment.MSG_TYPE_EMPTY:
                    GifImageView iv4 = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", context.getPackageName()));
                    iv4.clearAnimation();
                    iv4.setImageDrawable(errShow);
                    break;
            }
        }
    }
}

interface OnPFListener {
    void checkPageEnd();
}
