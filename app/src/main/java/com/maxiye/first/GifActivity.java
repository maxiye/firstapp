package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
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
import android.text.Html;
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
import com.maxiye.first.part.PageListPopupWindow;
import com.maxiye.first.spy.BaseSpy;
import com.maxiye.first.spy.SpyGetter;
import com.maxiye.first.util.DBHelper;
import com.maxiye.first.util.DiskLRUCache;
import com.maxiye.first.util.NetworkUtil;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.Util;

import java.io.BufferedInputStream;
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
import java.net.SocketTimeoutException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class GifActivity extends AppCompatActivity {

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
    private static boolean getNewFlg = true;//是否获取新的文章
    private static boolean endFlg = false;
    private static String webName = "yxdown";
    private static String type = "bitmap";
    private static String title = "动态图";
    private static int artId = 1023742;
    private static int webPage = 1;
    private final int HISTORY_PAGE_SIZE = 10;
    private final int FAVORITE_PAGE_SIZE = 20;
    private int favImgPos = 0;//收藏图片浏览位置
    private int page = 1;
    private int tryCount = 0;//loadGifList错误计数器
    private boolean isGprs = false;//手机网络
    private boolean gprsContinue = false;//手机网络继续访问
    private SectionsPagerAdapter mSectionsPagerAdapter;
    public final ArrayList<String[]> gifList = new ArrayList<>(60);
    public JsonObject webCfg;
    private String[] webList;
    private HashMap<String, Drawable> iconCacheList;
    private SQLiteDatabase db;
    private OkHttpClient okHttpClient;
    private ThreadPoolExecutor threadPoolExecutor;
    private DiskLRUCache diskLRUCache;
    private NetworkUtil netUtil;
    private MyHandler myHandler;
    private ImgViewTask favViewTask;
    private SpyGetter spyGetter;

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
        //检测是否备份数据库
        db = new DBHelper(this).getWritableDatabase();//没有则此时创建数据库,生成.db文件
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNewFlg = bundle.getBoolean(GET_NEW_FLG, true);
            webName = bundle.getString(WEB_NAME, "gamersky");
            Log.w("end", getNewFlg ? "true" : "false");
        }
        //网站配置信息
        spyGetter = new SpyGetter();
        webCfg = getWebCfg(webName);
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(8, TimeUnit.SECONDS)
                    .build();
        }
        threadPoolExecutor = new ThreadPoolExecutor(4, 7, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), (r, executor) -> executor.shutdown());
//        CacheUtil.clearAllCache(this);//清楚所有缓存
        diskLRUCache = DiskLRUCache.getInstance(this, type);
        myHandler = new MyHandler(this);
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
                for (int i = 1; i < 4; i++) {
                    GifImageView giv = findViewById(getResources().getIdentifier("gif_" + i, "id", getPackageName()));
                    giv.clearAnimation();
                    giv.setImageDrawable(iconCacheList.get("default"));
                    giv.setMinimumHeight(90);
                    giv.setMinimumWidth(90);
                }
                mSectionsPagerAdapter.currentFragment.refresh();
            }
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    public static void setTitle(String title) {
        GifActivity.title = title;
    }

    @SuppressWarnings("unused")
    public String getAssetContent(String fName) {
        try (InputStream is = getAssets().open(fName)) {
            byte[] bytes = new byte[is.available()];
            return is.read(bytes) > 0 ? new String(bytes, "utf-8") : "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private JsonObject getWebCfg(String webName) {
        JsonObject webCfg = null;
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("img_spy_config.json");
            JsonObject cfgs = new Gson().fromJson(new InputStreamReader(is), JsonObject.class).getAsJsonObject(type);
            is.close();
            webCfg = cfgs.getAsJsonObject(webName);
            if (webList == null) {
                webList = new String[cfgs.size()];
                iconCacheList = new HashMap<>(5);
                int index = 0;
                for (String key : cfgs.keySet()) {
                    webList[index++] = key;
                    iconCacheList.put(key, BitmapDrawable.createFromStream(is = am.open(cfgs.getAsJsonObject(key).get("local_icon").getAsString()), null));
                    is.close();
                }
                iconCacheList.put("default", getDrawable(R.drawable.ic_image_black_24dp));
                iconCacheList.put("loading", getDrawable(R.drawable.ic_autorenew_black_24dp));
                Log.w("getWebCfg-webList", Arrays.toString(webList) + " -- " + iconCacheList.toString());
            }
            Log.w("getWebCfg", webCfg.toString());
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
        if (isGprs = NetworkUtil.isGprs(this)) {
            alert(getText(R.string.gprs_network).toString());
            gprsContinue = false;
        }
        if (netUtil == null) {
            netUtil = new NetworkUtil(this, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    if (isGprs = NetworkUtil.isGprs(getApplicationContext())) {
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
        EditText pageEdit = view.findViewById(R.id.gif_dialog_input);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_info_black_24dp)//设置图标
                .setTitle(R.string.input_page)//设置标题
                .setView(view)//添加布局
                .setPositiveButton(R.string.go_to, (dialog1, which) -> {
                    String itemIdxStr = pageEdit.getText().toString();
                    itemIdxStr = itemIdxStr.equals("") ? "1" : itemIdxStr;
                    mViewPager.setCurrentItem(Integer.parseInt(itemIdxStr) - 1, true);
                    if (endFlg)
                        checkPageEnd();
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("InflateParams")
    public void changeUrl(MenuItem item) {
        //实例化布局
        View view2 = LayoutInflater.from(this).inflate(R.layout.gif_goto_dialog_edittext, null);
        EditText articleId = view2.findViewById(R.id.gif_dialog_input);
        //创建对话框
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_info_black_24dp)//设置图标
                .setTitle(R.string.input_artid)//设置标题
                .setView(view2)//添加布局
                .setPositiveButton(R.string.go_to, (dialog1, which) -> {
                    String txt = articleId.getText().toString();
                    if (txt.equals("")) {
                        alert(getString(R.string.artid_can_not_be_empty));
                    } else {
                        artId = Integer.parseInt(txt);
                        initPage();
                    }
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog2.show();
        Objects.requireNonNull(dialog2.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
        ArrayList<HashMap<String, Object>> listData = new ArrayList<>(webList.length);
        for (String web : webList) {
            HashMap<String, Object> webItem = new HashMap<>(2);
            webItem.put("name", web);
            webItem.put("icon", iconCacheList.get(web));
            listData.add(webItem);
        }
        ma.setData(listData);
        ma.setOnItemClickListener(position -> {
            webName = (String) listData.get(position).get("name");
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
                .setPositiveButton(R.string.first_page, (dialog, which) -> {
                    Uri url = Uri.parse(String.format(webCfg.get("img_web").getAsString(), artId));
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.current_page, (dialog, which) -> {
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
        PopupWindow pageWindow = new PageListPopupWindow.Builder(this)
                .setListGetter(this::getHistoryList)
                .setItemClickListener((pageWin, position) -> {
                    webName = (String) pageWin.ma.getItemData(position).get("web_name");
                    Log.w("rvClick", webName);
                    webCfg = getWebCfg(webName);
                    artId = (int) pageWin.ma.getItemData(position).get("art_id");
                    initPage();
                    pageWin.popupWindow.dismiss();
                })
                .setItemLongClickListener((pageWin, position) -> {
                    PopupMenu pMenu = new PopupMenu(this, pageWin.rv.getLayoutManager().findViewByPosition(position));//使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
                    pMenu.getMenuInflater().inflate(R.menu.gif_history_popupmenu, pMenu.getMenu());
                    pMenu.setOnMenuItemClickListener(item1 -> {
                        switch (item1.getItemId()) {
                            case R.id.delete_fav:
                                //删除记录
                                String delArtId = pageWin.ma.getItemData(position).get("art_id").toString();
                                String delWebName = (String) pageWin.ma.getItemData(position).get("web_name");
                                deleteHistory(delArtId, delWebName);
                                pageWin.list.remove(position);
                                pageWin.ma.notifyItemRemoved(position);
                                pageWin.ma.notifyItemRangeChanged(position, pageWin.list.size());
                                break;
                            case R.id.copy_fav_artid:
                                String articleId = pageWin.ma.getItemData(position).get("art_id").toString();
                                ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                assert clm != null;
                                clm.setPrimaryClip(ClipData.newPlainText(null, articleId));
                                alert(getString(R.string.clip_toast) + "：" + articleId);
                                break;
                            case R.id.reload_art:
                                artId = (int) pageWin.ma.getItemData(position).get("art_id");
                                webName = (String) pageWin.ma.getItemData(position).get("web_name");
                                webCfg = getWebCfg(webName);
                                deleteHistory(Integer.toString(artId), webName);
                                initPage();
                                pageWin.popupWindow.dismiss();
                                break;
                        }
                        return false;
                    });
                    pMenu.show();
                    return true;
                }).setPageSize(HISTORY_PAGE_SIZE)
                .setTotal(getHistoryCount())
                .setWindowHeight(1200)
                .build();
        pageWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
    }

    private int getHistoryCount() {
        Cursor cus = db.rawQuery("select count(*) from " + DBHelper.TB_IMG_WEB + " where art_id > 0 and type = '" + type + "'", null);
        cus.moveToFirst();
        int count = cus.getInt(0);
        cus.close();
        return count;
    }

    private void deleteHistory(String delArtId, String delWebName) {
        Log.w("deleteHistory", "art_id：" + delArtId + "，web_name：" + delWebName);
        db.delete(DBHelper.TB_IMG_WEB, "art_id = ? and web_name = ?", new String[]{delArtId, delWebName});
        db.delete(DBHelper.TB_IMG_WEB_ITEM, "art_id = ? and web_name = ?", new String[]{delArtId, delWebName});
    }

    private ArrayList<HashMap<String, Object>> getHistoryList(int page, ArrayList<HashMap<String, Object>> historyList) {
        int offset = (page - 1) * HISTORY_PAGE_SIZE;
        String sql = "select * from " + DBHelper.TB_IMG_WEB + " where art_id > 0 and type = '" + type + "' order by id desc limit " + HISTORY_PAGE_SIZE + " offset " + offset;
        Cursor cus = db.rawQuery(sql, null);
        int count = cus.getCount();
        Log.w("getHistoryList：", count + "");
        cus.moveToFirst();
        for (int i = 0, listSize = historyList.size(); i < HISTORY_PAGE_SIZE; i++) {
            HashMap<String, Object> item;
            if (i < listSize) {
                item = historyList.get(i);
            } else {
                historyList.add(item = new HashMap<>(4));
            }
            if (count > i) {
                String web_name = cus.getString(cus.getColumnIndex("web_name"));
                item.put("web_name", web_name);
                item.put("name", cus.getString(cus.getColumnIndex("title")));
                item.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                //item.put("type", cus.getString(cus.getColumnIndex("type")));
                //item.put("web_url", cus.getString(cus.getColumnIndex("web_url")));
                item.put("icon", iconCacheList.get(web_name));
                cus.moveToNext();
            } else {
                item.clear();
            }
        }
        cus.close();
        return historyList;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public void listFavorite(MenuItem item) {
        PopupWindow pageWindow = new PageListPopupWindow.Builder(this)
                .setListGetter(this::getFavoriteList)
                .setItemClickListener((pageWin, position) -> viewFav(pageWin.list, position, pageWin.rootView))
                .setItemLongClickListener((pageWin, position) -> {
                    PopupMenu pMenu = new PopupMenu(this, pageWin.rv.getLayoutManager().findViewByPosition(position));//使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
                    pMenu.getMenuInflater().inflate(R.menu.gif_favorite_popupmenu, pMenu.getMenu());
                    pMenu.setOnMenuItemClickListener(item1 -> {
                        switch (item1.getItemId()) {
                            case R.id.delete_fav:
                                //删除记录
                                String id = (String) pageWin.ma.getItemData(position).get("id");
                                String itemId = (String) pageWin.ma.getItemData(position).get("item_id");
                                deleteFavorite(id, itemId);
                                pageWin.list.remove(position);
                                pageWin.ma.notifyItemRemoved(position);
                                pageWin.ma.notifyItemRangeChanged(position, pageWin.list.size());
                                break;
                            case R.id.delete_fav_file:
                                File favFile = new File((String) pageWin.ma.getItemData(position).get("path"));
                                if (favFile.exists() && favFile.delete()) {
                                    alert(getString(R.string.delete_success));
                                } else {
                                    alert(getString(R.string.file_is_lost));
                                }
                                break;
                            case R.id.delete_fav_and_file:
                                File favFile2 = new File((String) pageWin.ma.getItemData(position).get("path"));
                                if (favFile2.exists() && favFile2.delete()) {
                                    alert(getString(R.string.delete_success));
                                } else {
                                    alert(getString(R.string.file_is_lost));
                                }
                                String id2 = (String) pageWin.ma.getItemData(position).get("id");
                                String itemId2 = (String) pageWin.ma.getItemData(position).get("item_id");
                                deleteFavorite(id2, itemId2);
                                pageWin.list.remove(position);
                                pageWin.ma.notifyItemRemoved(position);
                                pageWin.ma.notifyItemRangeChanged(position, pageWin.list.size());
                                break;
                        }
                        return false;
                    });
                    pMenu.show();
                    return true;
                }).setPageSize(FAVORITE_PAGE_SIZE)
                .setTotal(getFavoriteCount())
                .setWindowHeight(1600)
                .build();
        pageWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
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
        GifActivity activity = this;
        favImgPos = position;
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
        GifImageView imgView = new GifImageView(this);
        imgView.setImageDrawable(iconCacheList.get("default"));
        imgView.setOnLongClickListener(v -> {
            HashMap<String, Object> item = favoriteList.get(favImgPos);
            String name = (String) item.get("title");
            String id = (String) item.get("id");
            String cacheKey = "favorite_" + item.get("id") + "_" + type;
            download(id + "_" + name, cacheKey, v);
            return false;
        });
        imgView.setOnTouchListener(new View.OnTouchListener() {
            int vScrollX;
            float mPosX, mPosY, mCurPosX, mCurPosY;
            private Runnable runnable = imgView::performLongClick;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        vScrollX = v.getScrollX();
                        mPosX = mCurPosX = event.getX();
                        mPosY = mCurPosY = event.getY();
                        myHandler.postDelayed(runnable, 500);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mCurPosX == mPosX) {
                            myHandler.removeCallbacks(runnable);
                        }
                        mCurPosX = event.getX();
                        v.setScrollX((int) (mPosX - mCurPosX + vScrollX));
                        break;
                    case MotionEvent.ACTION_UP:
                        mCurPosY = event.getY();
                        v.setScrollX(vScrollX);
                        myHandler.removeCallbacks(runnable);
//                        if (mCurPosY - mPosY > 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {//向下滑動
//                        } else if (mCurPosY - mPosY < 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {//向上滑动
//                        }
                        if (mCurPosX - mPosX > 200) {//向左滑動
                            viewPre();
                            break;
                        } else if (mCurPosX - mPosX < -200) {//向右滑动
                            viewNext();
                            break;
                        }
                        if (mCurPosY - mPosY > 250) {
                            dialog.dismiss();
                            break;
                        }
                        break;
                }
                return true;
            }

            private void viewPre() {
                if (favImgPos > 0) {
                    favImgPos--;
                } else {
                    Button button = root.findViewById(R.id.popup_prev_page);
                    if (button.getVisibility() == Button.VISIBLE) {
                        button.performClick();
                        favImgPos = FAVORITE_PAGE_SIZE - 1;
                    } else {
                        activity.alert(getString(R.string.no_more));
                        return;
                    }
                }
                loadFavImg(dialog, imgView, favoriteList.get(favImgPos));
            }

            private void viewNext() {
                if (favImgPos < favoriteList.size() - 1) {
                    if (favoriteList.get(favImgPos + 1).isEmpty()) {
                        activity.alert(getString(R.string.no_more));
                        return;
                    }
                    ++favImgPos;
                } else {
                    Button button = root.findViewById(R.id.popup_next_page);
                    if (button.getVisibility() == Button.VISIBLE) {
                        favImgPos = 0;
                        button.performClick();
                    } else {
                        activity.alert(getString(R.string.no_more));
                        return;
                    }
                }
                loadFavImg(dialog, imgView, favoriteList.get(favImgPos));
            }
        });
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0x3f009688));
        dialog.setContentView(imgView);
        loadFavImg(dialog, imgView, favoriteList.get(position));
        dialog.show();
    }

    private void download(String name, String cacheKey, View view) {
        PermissionUtil.req(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.PER_REQ_STORAGE_WRT, () -> {
            Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
            File img = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type + "/" + name);
            Log.w("download", "path:" + img.getAbsolutePath());
            try {
                if (!img.getParentFile().exists() && !img.getParentFile().mkdirs()) {
                    Log.w("create dir error", img.getParentFile().getAbsolutePath());
                    throw new Exception("create dir error");
                }
                File cacheGif = diskLRUCache.get(cacheKey);
                if (cacheGif == null || !cacheGif.exists())
                    throw new Exception(getString(R.string.cache_is_lost));
                if (Build.VERSION.SDK_INT > 25) {
                    Files.copy(cacheGif.toPath(), img.toPath());
                } else {
                    if (!img.exists() && !img.createNewFile()) {
                        throw new Exception("create file error");
                    }
                    try (FileInputStream input = new FileInputStream(cacheGif); FileOutputStream output = new FileOutputStream(img)) {
                        FileChannel ifc = input.getChannel(), ofc = output.getChannel();
                        long length = 102400, position = ifc.position(), fsize = ifc.size();
                        while (position != fsize) {
                            if ((fsize - position) < length) {
                                length = fsize - position;
                            }
                            MappedByteBuffer inbuffer = ifc.map(FileChannel.MapMode.READ_ONLY, position, length);
                            ofc.write(inbuffer);
                            ifc.position(position += length);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //发送广播
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(img)));
                Snackbar.make(view, R.string.download_success, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.open, v -> {
                            Intent imgView = new Intent(Intent.ACTION_VIEW);
                            imgView.setDataAndType(FileProvider.getUriForFile(this, "com.maxiye.first.fileprovider", img), "image/*");
                            imgView.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//不加黑屏读取不了
                            startActivity(imgView);
                        }).show();
            } catch (Exception e) {
                alert(getString(R.string.download_err) + e.toString());
                e.printStackTrace();
            }
        });
    }

    private void loadFavImg(Dialog dialog, GifImageView imgView, HashMap<String, Object> item) {
        String title = (String) item.get("title");
        String url = (String) item.get("real_url");
        url = url.equals("") ? (String) item.get("url") : url;
        dialog.setTitle(Html.fromHtml("<p style='color: #F66725; text-align: center'>" + title + "</p>", Html.FROM_HTML_MODE_COMPACT));
        if (favViewTask != null)
            favViewTask.cancel(true);
        favViewTask = new ImgViewTask(imgView, this);
        favViewTask.executeOnExecutor(threadPoolExecutor, title, url, (String) item.get("id"), (String) item.get("path"));
    }

    static class ImgViewTask extends AsyncTask<String, Integer, Drawable> {

        private WeakReference<GifActivity> gifActivityWR;
        private WeakReference<GifImageView> imgViewWR;
        private CircleProgressDrawable circleProgress;

        ImgViewTask(GifImageView imgView, GifActivity gifActivity) {
            imgViewWR = new WeakReference<>(imgView);
            gifActivityWR = new WeakReference<>(gifActivity);
        }

        /**
         * 加载图片
         *
         * @param strings title,url,id,path
         * @return Drawable
         */
        @Override
        protected Drawable doInBackground(String... strings) {
            GifActivity activity = gifActivityWR.get();
            String imgKey = "favorite_" + strings[2] + "_" + type;
            String path = strings[3];
            File cache = path.equals("") ? activity.diskLRUCache.get(imgKey) : new File(path);
            if (cache != null) {
                try {
                    Log.w("loadImg(fromCache)", "title：" + strings[0] + ";url：" + strings[1]);
                    FileInputStream fis = null;
                    Drawable imgDrawable = type.equals("gif") ? new GifDrawable(cache) : Drawable.createFromStream(fis = new FileInputStream(cache), null);
                    if (fis != null)
                        fis.close();
                    return imgDrawable;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //移动网络禁止 gif
                if (activity.isGprs && type.equals("gif")) {
                    activity.runOnUiThread(() -> activity.alert(activity.getString(R.string.gprs_network)));
                    if (!activity.gprsContinue)
                        return null;
                }
                cache = new File(activity.getCacheDir(), imgKey);
                Request request = new Request.Builder().url(strings[1]).build();
                try {
                    Log.w("loadImg(fromNet)", "title：" + strings[0] + ";url：" + strings[1]);
                    Response response = activity.okHttpClient.newCall(request).execute();
                    ResponseBody responseBody = response.body();
                    assert responseBody != null;
                    int contentLength = (int) responseBody.contentLength();
                    byte[] bytes;
                    if (contentLength > 40960 * 2) {
                        publishProgress(0, contentLength);
                        BufferedSource source = responseBody.source();
                        bytes = new byte[contentLength];
                        int chunk = contentLength / 90, threshold = 0, offset = 0, read;
                        while ((read = source.read(bytes, offset, contentLength - offset)) != -1) {
                            if ((offset += read) > threshold) {
                                publishProgress(offset, contentLength);
                                threshold = offset + chunk;
                            }
                        }
                        publishProgress(contentLength, contentLength);
                    } else {
                        bytes = responseBody.bytes();
                    }
                    Drawable imgDrawable = type.equals("gif") ? new GifDrawable(bytes) : Drawable.createFromStream(new ByteArrayInputStream(bytes), null);
                    if (imgDrawable == null)
                        throw new Exception("get image err");
                    RandomAccessFile raf = new RandomAccessFile(cache, "rwd");
                    raf.write(bytes);
                    raf.close();
                    gifActivityWR.get().diskLRUCache.put(imgKey, imgKey, cache.length());
                    return imgDrawable;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cache.delete())
                        Log.d("cacheDel", "cacheGif deleted");
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            GifImageView imgView = imgViewWR.get();
            circleProgress = new CircleProgressDrawable.Builder()
                    .color(gifActivityWR.get().getColor(R.color.myPrimaryColor))
                    .build();
            imgView.setImageDrawable(circleProgress);
            imgView.setMinimumHeight(180);
            imgView.setMinimumWidth(180);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            GifImageView imgView = imgViewWR.get();
            if (drawable == null) {
                Drawable errShow = gifActivityWR.get().getDrawable(R.drawable.ic_close_black_24dp);
                imgView.setImageDrawable(errShow);
            } else {
                imgView.setImageDrawable(drawable);
                float zoom = type.equals("gif") ? 3f : 5.5f;
                int width = Math.round(drawable.getIntrinsicWidth() * zoom);
                int height = Math.round(drawable.getIntrinsicHeight() * zoom);
                imgView.setMinimumHeight(height);
                imgView.setMinimumWidth(width);
            }
            super.onPostExecute(drawable);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0)
                circleProgress.setMaxProgress(values[1]);
            circleProgress.setCurProgress(values[0]);
            super.onProgressUpdate(values);
        }
    }

    private void deleteFavorite(String delId, String itemId) {
        ContentValues ctv = new ContentValues(1);
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
        int count = cus.getCount();
        Log.w("getFavoriteList：", count + "");
        cus.moveToFirst();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        //读取图片信息，此时把options.inJustDecodeBounds 设回true，不返回bitmap
        opts.inJustDecodeBounds = true;
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type + "/";
        for (int i = 0, listSize = favoriteList.size(); i < FAVORITE_PAGE_SIZE; i++) {
            HashMap<String, Object> item;
            if (i < listSize) {
                item = favoriteList.get(i);
            } else {
                favoriteList.add(item = new HashMap<>(8));
            }
            if (count > i) {
                item.put("id", cus.getString(cus.getColumnIndex("id")));
                item.put("item_id", cus.getString(cus.getColumnIndex("item_id")));
                String title = cus.getString(cus.getColumnIndex("title"));
                item.put("title", title);
                item.put("url", cus.getString(cus.getColumnIndex("url")));
                item.put("real_url", cus.getString(cus.getColumnIndex("real_url")));
                //item.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                //item.put("type", cus.getString(cus.getColumnIndex("type")));
                String name = item.get("id") + "_" + title;
                cus.moveToNext();
                File file = new File(dir + name);
                if (file.exists()) {
                    item.put("path", dir + name);
                    item.put("name", title + "<span style='color: #13b294'>&emsp;&emsp;√</span>");
                } else {
                    item.put("path", "");
                    item.put("name", title);
                    if ((file = diskLRUCache.get("favorite_" + item.get("id") + "_" + type)) == null || !file.exists()) {
                        item.put("icon", iconCacheList.get("default"));
//                        Log.w("getFavoriteList-fileNotFound", name);
                        continue;
                    }
                }
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                    bis.mark(1);
                    BitmapFactory.decodeStream(bis, null, opts);
                    newOpts.inSampleSize = Util.calculateInSampleSize(opts, 50, 50);
                    bis.reset();
                    item.put("icon", new BitmapDrawable(getResources(), BitmapFactory.decodeStream(bis, null, newOpts)));
                    //Log.w("getFavoriteList-oompress", "fileSize：" + file.length() / 1024 + " kB；compress：" + newOpts.inSampleSize);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                item.clear();
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
            spyGetter.modeFlg = !spyGetter.modeFlg;
            okHttpClient.dispatcher().cancelAll();
            Log.w("typeListRvClick", type);
            webCfg = getWebCfg(webName);
            if (webCfg == null)
                webCfg = getWebCfg("gamersky");
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
        ArrayList<HashMap<String, Object>> typeList = new ArrayList<>(2);
        String[] types = new String[]{"gif", "bitmap"};
        for (String type : types) {
            HashMap<String, Object> typeItem = new HashMap<>(2);
            typeItem.put("name", type);
            typeItem.put("icon", iconCacheList.get("default"));
            typeList.add(typeItem);
        }
        return typeList;
    }


    private void checkPageEnd() {
        int nowPage = mViewPager.getCurrentItem() + 1;
        int pages = gifList.size() % 3 == 0 ? gifList.size() / 3 : gifList.size() / 3 + 1;
        if (nowPage > pages) {
            alert(getString(R.string.auto_back_to_first_page));
            mViewPager.setCurrentItem(pages - 1, true);
        }
    }



    private boolean loadDbGifList() {
        Cursor cus = db.query(DBHelper.TB_IMG_WEB, new String[]{"*"}, "art_id = ? and web_name = ?", new String[]{artId + "", webName}, null, null, "id desc", "1");
        Log.w("db_web", cus.getCount() + "");
        if (cus.getCount() > 0) {
            cus.moveToFirst();
            title = cus.getString(cus.getColumnIndex("title"));
            int totalPage = cus.getInt(cus.getColumnIndex("pages"));
            Cursor cus2 = db.query(DBHelper.TB_IMG_WEB_ITEM, new String[]{"page,title,url,ext,real_url"}, "art_id = ? and web_name = ?", new String[]{artId + "", webName}, null, null, "id asc");
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
                    gifList.add(gifInfo);
                    webPage = cus2.getInt(cus2.getColumnIndex("page"));
                    cus2.moveToNext();
                }
            }
            cus2.close();
            if (totalPage == webPage)
                endFlg = true;
            ++webPage;
            return true;
        }
        cus.close();
        return false;
    }


    private void updateDbField(String val) {
        ContentValues ctv = new ContentValues(1);
        ctv.put("pages", val);
        int rows = db.update(DBHelper.TB_IMG_WEB, ctv, "art_id = ? and web_name = ?", new String[]{artId + "", webName});
        Log.w("db_web_update: ", rows + "");
    }

    public void saveDbGifList(String dbName, String[] data) {
        String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        if (dbName.equals(DBHelper.TB_IMG_WEB)) {
            ContentValues ctv = new ContentValues(6);
            ctv.put("art_id", artId);
            ctv.put("web_url", data[0]);
            ctv.put("web_name", webName);
            ctv.put("type", type);
            ctv.put("title", data[1]);
            ctv.put("time", datetime);
            long newId = db.insert(dbName, null, ctv);
            Log.w("db_web_insert: ", newId + "");
        } else {
            ContentValues ctv = new ContentValues(9);
            ctv.put("art_id", artId);
            ctv.put("page", webPage);
            ctv.put("web_name", webName);
            ctv.put("type", type);
            ctv.put("title", data[1]);
            ctv.put("url", data[0]);
            ctv.put("ext", data[2]);
            ctv.put("real_url", data[3]);
            ctv.put("time", datetime);
            long newId = db.insert(dbName, null, ctv);
            Log.w("db_web_item_insert: ", newId + "");
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
        private int page, gifPosition = 1, focusedPosition = 1;
        private static final int MSG_TYPE_PRE = 100;
        private static final int MSG_TYPE_LOAD = 101;
        private static final int MSG_TYPE_EMPTY = 102;
        private static final int MSG_TYPE_PRELOAD = 103;
        private static final int MSG_TYPE_LOADING = 104;
        private GifActivity activity;

        public PlaceholderFragment() {
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            activity = null;
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @SuppressLint("SetTextI18n")
        static PlaceholderFragment newInstance(int page, GifActivity activity) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.page = page;
            fragment.activity = activity;
            return fragment;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_get_gif, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(title + "：" + page);
            for (int i = 1; i < 4; i++) {
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
            activity.myHandler.removeCallbacksAndMessages(null);
            if (isVisibleToUser) {
                if (activity.threadPoolExecutor.getActiveCount() == activity.threadPoolExecutor.getMaximumPoolSize()) {
                    activity.threadPoolExecutor.shutdownNow();
                    Log.w("threadPoolExecutor", "shutdownNow");
                }
                if (gifPosition == 1) {
                    checkLoad();
                }
            }
        }

        public void send(int what, int arg1, int arg2, Object obj) {
            if (page == activity.page && activity.myHandler != null) {
                activity.myHandler.sendMessage(activity.myHandler.obtainMessage(what, arg1, arg2, obj));
            }
        }

        private int getGifOffset(int index) {
            return (page - 1) * 3 + index - 1;
        }

        private void longClickCb(int position, View view) {
            focusedPosition = position;
            ListPopupWindow listMenu = new ListPopupWindow(activity);
            listMenu.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, new
                    String[]{getString(R.string.add_to_fav), getString(R.string.download)}));
            DisplayMetrics dm = new DisplayMetrics();
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
                        long favId = addFav();
                        String[] gifInfo = getGifInfo(getGifOffset(focusedPosition));
                        String cacheKey = webName + "_" + artId + "-" + getGifOffset(focusedPosition);
                        activity.download(favId + "_" + gifInfo[1], cacheKey, activity.findViewById(android.R.id.content));
                        break;
                }
                listMenu.dismiss();
            });
            listMenu.show();
        }

        private long addFav() {
            String[] gifInfo = getGifInfo(getGifOffset(focusedPosition));
            Cursor cus = activity.db.query(DBHelper.TB_IMG_WEB_ITEM, new String[]{"*"}, "art_id = ? and title = ?", new String[]{artId + "", gifInfo[1]}, null, null, "id desc", "1");
            long favId = 0;
            if (cus.getCount() > 0) {
                cus.moveToFirst();
                if (cus.getInt(cus.getColumnIndex("fav_flg")) != 1) {
                    ContentValues ctv = new ContentValues(10);
                    ctv.put("item_id", cus.getLong(cus.getColumnIndex("id")));
                    ctv.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                    ctv.put("page", cus.getInt(cus.getColumnIndex("page")));
                    ctv.put("web_name", cus.getString(cus.getColumnIndex("web_name")));
                    ctv.put("type", cus.getString(cus.getColumnIndex("type")));
                    ctv.put("title", cus.getString(cus.getColumnIndex("title")));
                    ctv.put("url", cus.getString(cus.getColumnIndex("url")));
                    ctv.put("ext", cus.getString(cus.getColumnIndex("ext")));
                    ctv.put("real_url", cus.getString(cus.getColumnIndex("real_url")));
                    ctv.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                    favId = activity.db.insert(DBHelper.TB_IMG_FAVORITE, null, ctv);
                    Log.w("db_img_fav_insert: ", favId + "");
                    ContentValues ctv2 = new ContentValues(1);
                    ctv2.put("fav_flg", 1);
                    int rows = activity.db.update(DBHelper.TB_IMG_WEB_ITEM, ctv2, "id = ?", new String[]{cus.getString(cus.getColumnIndex("id"))});
                    Log.w("db_web_item_update: ", rows + "");
                    activity.alert(getString(R.string.added_to_fav));
                } else {
                    Cursor cus2 = activity.db.query(DBHelper.TB_IMG_FAVORITE, new String[]{"id"}, "item_id = ?", new String[]{cus.getString(cus.getColumnIndex("id"))}, null, null, "id desc", "1");
                    cus2.moveToFirst();
                    favId = cus2.getLong(cus.getColumnIndex("id"));
                    cus2.close();
                    activity.alert(getString(R.string.already_in_fav));
                }
                cus.close();
            }
            return favId;
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
                    popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
                } else {
                    activity.threadPoolExecutor.execute(this::loadGif);
                }
                activity.alert(getString(R.string.gprs_network));
            } else {
                activity.threadPoolExecutor.execute(this::loadGif);
            }
        }

        void loadGif() {
            int nowPos = gifPosition, startOffset = getGifOffset(nowPos);
            Log.w("info", "loadGif:" + startOffset);
            String[] gifInfo = getGifInfo(startOffset);
            if (gifInfo == null)
                return;
            String imgKey = webName + "_" + artId + "-" + startOffset;
            File cacheGif = activity.diskLRUCache.get(imgKey);
            send(MSG_TYPE_PRE, nowPos, 0, gifInfo[1]);
            if (cacheGif != null) {
                try {
                    FileInputStream fis = null;
                    Drawable imgDrawable = type.equals("gif") ? new GifDrawable(cacheGif) : Drawable.createFromStream(fis = new FileInputStream(cacheGif), null);
                    if (fis != null)
                        fis.close();
                    send(MSG_TYPE_LOAD, nowPos, 0, imgDrawable);
                    Log.w("loadGif", "fromCache:" + gifInfo[1]);
                } catch (IOException e) {
                    send(MSG_TYPE_EMPTY, nowPos, 0, null);
                    e.printStackTrace();
                }
            } else {
                String url = gifInfo[3].equals("") ? gifInfo[0] : gifInfo[3];
                Request request = new Request.Builder().url(url).build();
                activity.okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        send(MSG_TYPE_EMPTY, nowPos, 0, null);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        try {
                            Log.w("loadGif", "fromNet:" + gifInfo[1] + ";url:" + url + ";index:" + request.toString());
                            ResponseBody responseBody = response.body();
                            assert responseBody != null;
                            int contentLength = (int) responseBody.contentLength();
                            byte[] bytes;
                            if (contentLength > 40960 * 2) {
                                send(MSG_TYPE_PRELOAD, nowPos, contentLength, null);
                                BufferedSource source = responseBody.source();
                                bytes = new byte[contentLength];
                                int chunk = contentLength / 90, threshold = 0, offset = 0, read;
                                while ((read = source.read(bytes, offset, contentLength - offset)) != -1) {
                                    if ((offset += read) > threshold) {
                                        send(MSG_TYPE_LOADING, nowPos, offset, null);
                                        threshold = offset + chunk;
                                    }
                                }
                                send(MSG_TYPE_LOADING, nowPos, contentLength, null);
                            } else {
                                bytes = responseBody.bytes();
                            }
                            Drawable imgDrawable = type.equals("gif") ? new GifDrawable(bytes) : Drawable.createFromStream(new ByteArrayInputStream(bytes), null);
                            if (imgDrawable == null)
                                throw new Exception("get image err");
                            send(MSG_TYPE_LOAD, nowPos, 0, imgDrawable);
                            RandomAccessFile raf = new RandomAccessFile(finalCacheGif, "rwd");
                            raf.write(bytes);
                            raf.close();
                            activity.diskLRUCache.put(imgKey, imgKey + gifInfo[2], finalCacheGif.length());
                        } catch (Exception e) {
                            if (finalCacheGif.delete())
                                Log.d("cacheDel", "cacheGif deleted");
                            send(MSG_TYPE_EMPTY, nowPos, 0, null);
                            e.printStackTrace();
                        }
                    }
                    File finalCacheGif = new File(activity.getCacheDir(), imgKey + gifInfo[2]);
                });
            }
        }

        private synchronized void loadGifList() {
            BaseSpy spy = activity.spyGetter.getSpy(webName, activity.webCfg);
            //数据库获取
            if (webPage == 1) {
                if (getNewFlg) {
                    int articleId = spy.getNewArtId(activity.okHttpClient);
                    if (articleId > 0) {
                        artId = articleId;
                        endFlg = false;
                    }
                    getNewFlg = false;
                    Log.w("getNewArtId", title);
                }
                if (activity.loadDbGifList())
                    return;
            }
            if (endFlg)
                return;
            Log.w("start", "loadGifList(lock):" + webPage);
            try {
                Request req = spy.buildRequest(artId, webPage);
                ResponseBody responseBody = activity.okHttpClient.newCall(req).execute().body();
                assert responseBody != null;
                String content = new String(responseBody.bytes(), "utf-8");
                if (spy.insertItem(content, activity) == 0) {
                    System.out.print(content);
                    throw new ProtocolException("over");
                }
                if (webPage == 1 && activity.gifList.size() > 0) {
                    Log.w("titleGet", title);
                    if (title.equals("动态图"))
                        spy.getNewTitle(activity.okHttpClient, content, artId);
                    activity.saveDbGifList(DBHelper.TB_IMG_WEB, new String[]{spy.curUrl, title});
                }
                ++webPage;
                activity.tryCount = 0;
            } catch (Exception e) {//java.net.ProtocolException: Too many follow-up requests: 21//java.io.EOFException 返回空内容，responseBody.bytes()
                if (++activity.tryCount > 3 && !(e instanceof SocketTimeoutException)) {
                    endFlg = true;
                    activity.tryCount = 0;
                    if (activity.gifList.size() > 0)
                        activity.updateDbField(String.valueOf(webPage - 1));
                    activity.runOnUiThread(() -> activity.checkPageEnd());
                }
                e.printStackTrace();
            } finally {
                Log.w("end", "loadGifList(unlock):end:" + webPage);
                System.out.println(activity.gifList.toString());
            }
        }

        String[] getGifInfo(int offset) {
            Log.w("start", "getGifInfo:" + offset + "-" + webPage + "-" + activity.gifList.size() + "-" + endFlg);
            if (activity.gifList.size() <= offset) {
                if (endFlg)
                    return null;
                if (!NetworkUtil.isConnected(activity)) {
                    activity.runOnUiThread(() -> activity.alert(getString(R.string.no_internet)));
                    return null;
                }
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
            activity.page = currentFragment.page;
            super.setPrimaryItem(container, position, object);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<GifActivity> activityWR;

        MyHandler(GifActivity activity) {
            activityWR = new WeakReference<>(activity);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            GifActivity activity = activityWR.get();
            PlaceholderFragment fragment = activity.mSectionsPagerAdapter.currentFragment;
            View rootView = fragment.getView();
            if (rootView == null)
                return;
            GifImageView imageView = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", activity.getPackageName()));
            switch (msg.what) {
                case PlaceholderFragment.MSG_TYPE_PRE:
                    if (msg.arg1 == 1) {
                        TextView textView = rootView.findViewById(R.id.section_label);
                        textView.setText(title + "：" + fragment.page);
                    }
                    TextView tv = rootView.findViewById(fragment.getResources().getIdentifier("gtxt_" + msg.arg1, "id", activity.getPackageName()));
                    tv.setText((String) msg.obj);
                    if (!activity.diskLRUCache.containsKey(webName + "_" + artId + "-" + fragment.getGifOffset(msg.arg1))) {
                        imageView.setImageDrawable(activity.iconCacheList.get("loading"));
                        imageView.setMinimumHeight(90);
                        imageView.setMinimumWidth(90);
                        imageView.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.load_rotate));
                    }
                    if (++fragment.gifPosition < 4) {
                        fragment.checkLoad();
                    } else {
                        fragment.gifPosition = 1;
                    }
                    break;
                case PlaceholderFragment.MSG_TYPE_PRELOAD:
                    CircleProgressDrawable circleProgress = new CircleProgressDrawable.Builder()
                            .capacity(msg.arg2)
                            .color(activity.getColor(R.color.actionTitle))
                            .build();
                    imageView.clearAnimation();
                    imageView.setImageDrawable(circleProgress);
                    break;
                case PlaceholderFragment.MSG_TYPE_LOADING:
                    Drawable process = imageView.getDrawable();
                    if (process instanceof CircleProgressDrawable) {
                        ((CircleProgressDrawable) process).setCurProgress(msg.arg2);
                    }
                    break;
                case PlaceholderFragment.MSG_TYPE_LOAD:
                    imageView.clearAnimation();
                    Drawable imgDrawable = (Drawable) msg.obj;
                    float scale = type.equals("gif") ? 2.5f : 4.5f;
                    imageView.setImageDrawable(imgDrawable);
                    int width = Math.round(imgDrawable.getIntrinsicWidth() * scale);
                    int height = Math.round(imgDrawable.getIntrinsicHeight() * scale);
                    int layoutWidth = rootView.getWidth();
                    height = width > layoutWidth ? height * layoutWidth / width : height;
                    imageView.setMinimumHeight(height);
                    imageView.setMinimumWidth(width);
                    break;
                case PlaceholderFragment.MSG_TYPE_EMPTY:
                    imageView.clearAnimation();
                    imageView.setImageDrawable(activity.getDrawable(R.drawable.ic_close_black_24dp));
                    break;
            }
        }
    }
}
