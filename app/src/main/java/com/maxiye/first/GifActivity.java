package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.text.Html;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
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
import com.google.gson.reflect.TypeToken;
import com.maxiye.first.part.CircleProgressDrawable;
import com.maxiye.first.part.PageListPopupWindow;
import com.maxiye.first.spy.BaseSpy;
import com.maxiye.first.spy.SpyGetter;
import com.maxiye.first.util.BitmapUtil;
import com.maxiye.first.util.DbHelper;
import com.maxiye.first.util.DiskLruCache;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.NetworkUtil;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.StringUtils;
import com.maxiye.first.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * @author due
 */
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
    /**
     * 是否获取新的文章
      */
    private static boolean getNewFlg = true;
    private static boolean endFlg = false;
    private static String webName = "duowan";
    private static String type = "bitmap";
    private final static String DEFAULT_TITLE = "动态图";
    private static String title = DEFAULT_TITLE;
    private static String artId = "1023742";
    private static int webPage = 1;
    private final int HISTORY_PAGE_SIZE = 10;
    private final int FAVORITE_PAGE_SIZE = 15;
    /**
     * 收藏图片浏览位置
     */
    private int favImgPos = 0;
    private int page = 1;
    /**
     * loadGifList错误计数器
     */
    private volatile int tryCount = 0;
    private final static int MAX_TRY_TIMES = 3;
    /**
     * 手机网络
     */
    private boolean isGprs = false;
    /**
     * 手机网络继续访问
     */
    private boolean gprsContinue = false;
    private PlaceholderFragment currentFragment;
    public final ArrayList<HashMap<String, String>> imgList = new ArrayList<>(60);
    private JsonObject webCfg;
    private String[] webList;
    private HashMap<String, Drawable> iconCacheList;
    private SQLiteDatabase db;
    private OkHttpClient okHttpClient;
    private ThreadPoolExecutor threadPoolExecutor;
    private DiskLruCache diskLRUCache;
    private NetworkUtil netUtil;
    private MyHandler myHandler;
    private ImgViewTask favViewTask;
    private Dialog loading;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_gif);
        // Create the adapter that will return a fragment
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.gif_activity_viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNewFlg = bundle.getBoolean(GET_NEW_FLG, true);
            webName = bundle.getString(WEB_NAME, "gamersky");
            MyLog.w("end", getNewFlg ? "true" : "false");
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
        // onkey 发生拥堵 --已修复
        threadPoolExecutor = new ThreadPoolExecutor(4, 7, 30, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardOldestPolicy());
//        CacheUtil.clearAllCache(this);//清楚所有缓存
        diskLRUCache = DiskLruCache.getInstance(this, type);
        myHandler = new MyHandler(this);
        // 没有则此时创建数据库,生成.db文件
        db = DbHelper.getDB(this);
        initPage();
        MyLog.w("end", "onCreateOver");
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

    private BaseSpy getSpy()
    {
        return SpyGetter.getInstance().getSpy(webName, webCfg);
    }

    private void initPage() {
        okHttpClient.dispatcher().cancelAll();
        title = DEFAULT_TITLE;
        webPage = 1;
        endFlg = false;
        imgList.clear();
        if (mViewPager.getCurrentItem() == 0) {
            if (currentFragment != null) {
                int gifCount = 3;
                for (int i = 1; i <= gifCount; i++) {
                    GifImageView img = findViewById(getResources().getIdentifier("gif_" + i, "id", getPackageName()));
                    img.clearAnimation();
                    img.setImageDrawable(iconCacheList.get("default"));
                    img.setMinimumHeight(90);
                    img.setMinimumWidth(90);
                }
                currentFragment.refresh();
            }
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    private void fetchNewArt() {
        String[] article = getSpy().getNewArticle(okHttpClient);
        if (article != null) {
            artId = article[0] != null ? article[0] : "xx";
            if (article[1] != null) {
                title = article[1];
            }
            endFlg = false;
        }
        getNewFlg = false;
        MyLog.w("getNewArticle", Arrays.toString(article));
    }

    private void fetchTitle(String content) {
        String head = getSpy().getNewTitle(okHttpClient, content, artId);
        MyLog.w("getNewArticle", head);
        if (head != null) {
            title = head;
        }
    }

    private void fetchItemList() {
        title = DEFAULT_TITLE;
        webPage = 1;
        endFlg = false;
        imgList.clear();
        loadImgList();
    }

    public void fetchAll(MenuItem item) {
        loading();
        threadPoolExecutor.execute(() -> {
            int oldPage = page;
            getImgInfo(9999);
            mViewPager.setCurrentItem(oldPage - 1, true);
            runOnUiThread(() -> {
                loading.dismiss();
                alert(getString(R.string.success_tip, imgList.size()));
            });
        });
    }

    /**
     * 爬取所有网页的图片
     * @return int 失败的网站个数
     */
    private String fetchAllWeb()
    {
        StringBuilder err = new StringBuilder();
        for (String name : webList) {
            int i = 0, gifCount = 3;
            setWebName(name);
            while (i < gifCount) {
                i++;
                getNewFlg = true;
                fetchItemList();
                MyLog.w("OneKey_Start", name + "；size=" + imgList.size() + "；try=" + i);
                if (!imgList.isEmpty()) {
                    MyLog.w("OneKey_End", name + "；title=" + title + "；size=" + imgList.size() + "；try=" + i);
                    break;
                } else if (i == 3) {
                    err.append(name).append("-").append(type).append("；");
                }
            }
        }
        return err.toString();
    }

    @SuppressWarnings("unused")
    public String getAssetContent(String fName) {
        try (InputStream is = getAssets().open(fName)) {
            byte[] bytes = new byte[is.available()];
            return is.read(bytes) > 0 ? new String(bytes, StandardCharsets.UTF_8) : "";
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
                iconCacheList = new HashMap<>(8);
                webList = new String[cfgs.size()];
                int index = 0;
                for (String key : cfgs.keySet()) {
                    webList[index++] = key;
                    iconCacheList.put(key, BitmapDrawable.createFromStream(is = am.open(cfgs.getAsJsonObject(key).get("local_icon").getAsString()), null));
                    is.close();
                }
                iconCacheList.put("default", getDrawable(R.drawable.ic_image_black_24dp));
                iconCacheList.put("loading", getDrawable(R.drawable.ic_autorenew_black_24dp));
                MyLog.w("getWebCfg-webList", Arrays.toString(webList) + " -- " + iconCacheList.toString());
            }
            if (webCfg != null) {
                webCfg.addProperty("name", webName);
                MyLog.w("getWebCfg", webCfg.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return webCfg;
    }

    private void loadWebList() {
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("img_spy_config.json");
            JsonObject cfgs = new Gson().fromJson(new InputStreamReader(is), JsonObject.class).getAsJsonObject(type);
            is.close();
            webList = new String[cfgs.size()];
            int index = 0;
            for (String key : cfgs.keySet()) {
                webList[index++] = key;
                iconCacheList.putIfAbsent(key, BitmapDrawable.createFromStream(is = am.open(cfgs.getAsJsonObject(key).get("local_icon").getAsString()), null));
                is.close();
            }
            MyLog.w("loadWebList", Arrays.toString(webList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setType(String t) {
        MyLog.w("setType", type);
        okHttpClient.dispatcher().cancelAll();
        if (!t.equals(type)) {
            type = t;
            webCfg = getWebCfg(webName);
            //3dm切换，一键获取问题
            if (webCfg == null) {
                MyLog.w("setTypeErr", t + "；" + webName);
            }
            SpyGetter.modeFlg = !SpyGetter.modeFlg;
            loadWebList();
            diskLRUCache.serialize();
            diskLRUCache = DiskLruCache.getInstance(this, type);
        }
    }

    private void setWebName(String web) {
        webName = web;
        webCfg = getWebCfg(web);
        MyLog.w("setWebName", web);
    }

    private String[] getTypeList() {
        return new String[]{"gif", "bitmap"};
    }

    private synchronized void loadImgList() {
        if (webCfg == null) {
            getNewFlg = false;
            endFlg = true;
            return;
        }
        //数据库获取
        if (webPage == 1) {
            if (getNewFlg) {
                fetchNewArt();
            }
            if (loadDbImgList()) {
                return;
            }
        }
        if (endFlg) {
            return;
        }
        MyLog.w("start", "loadImgList(lock):" + webPage);
        try {
            BaseSpy spy = getSpy();
            ResponseBody responseBody = okHttpClient.newCall(spy.buildRequest(artId, webPage))
                    .execute()
                    .body();
            assert responseBody != null;
            String content = responseBody.string();
            if (spy.insertItem(content, this) == 0) {
                MyLog.println(content);
                tryCount = MAX_TRY_TIMES;
                throw new ProtocolException("over");
            }
            if (webPage == 1 && imgList.size() > 0) {
                MyLog.w("titleGet", title);
                if (DEFAULT_TITLE.equals(title)) {
                    fetchTitle(content);
                }
                saveDbWeb(title, spy.curUrl);
            }
            ++webPage;
            tryCount = 0;
            // java.net.ProtocolException: Too many follow-up requests: 21//java.io.EOFException 返回空内容，responseBody.bytes()
        } catch (Exception e) {
            if (++tryCount > MAX_TRY_TIMES && !(e instanceof SocketTimeoutException)) {
                endFlg = true;
                tryCount = 0;
                if (imgList.size() > 0) {
                    updateDbField("pages", String.valueOf(webPage - 1));
                }
                runOnUiThread(this::checkPageEnd);
            }
            e.printStackTrace();
        } finally {
            MyLog.w("end", "loadImgList(unlock):end:" + webPage);
            MyLog.println(imgList.toString());
        }
    }

    private HashMap<String, String> getImgInfo(int offset) {
        MyLog.w("getImgInfo", offset + "-" + webPage + "-" + imgList.size() + "-" + endFlg);
        if (imgList.size() <= offset) {
            if (endFlg) {
                return null;
            }
            loadImgList();
            return getImgInfo(offset);
        } else {
            return imgList.get(offset);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gif_activity_actions, Util.iconMenu(menu));
        return true;
    }

    public void refresh(MenuItem item) {
        loading();
        currentFragment.refresh();
        threadPoolExecutor.execute(() -> {
            int t = 0, timeout = 30;
            try {
                ConnectionPool conPool = okHttpClient.connectionPool();
                while (t < timeout) {
                    MyLog.w("connectPool", "idle:" + conPool.idleConnectionCount() + ";total:" + conPool.connectionCount());
                    ++t;
                    Thread.sleep(300);
                    if (conPool.idleConnectionCount() == conPool.connectionCount()) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(loading::dismiss);
        });
    }

    @SuppressLint("InflateParams")
    public void skipTo(MenuItem item) {
        //实例化布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
        EditText pageEdit = view.findViewById(R.id.dialog_input);
        pageEdit.setHint(R.string.skip_to);
        pageEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.input_page)
                // 添加布局
                .setView(view)
                .setPositiveButton(R.string.go_to, (dialog1, which) -> {
                    String itemIdxStr = pageEdit.getText().toString();
                    itemIdxStr = StringUtils.isBlank(itemIdxStr) ? "1" : itemIdxStr;
                    mViewPager.setCurrentItem(Integer.parseInt(itemIdxStr) - 1, true);
                    if (endFlg) {
                        checkPageEnd();
                    }
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("InflateParams")
    public void changeUrl(MenuItem item) {
        //实例化布局
        View view2 = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
        EditText articleId = view2.findViewById(R.id.dialog_input);
        articleId.setHint(R.string.skip_to);
        //创建对话框
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.input_artid)
                // 添加布局
                .setView(view2)
                .setPositiveButton(R.string.go_to, (dialog1, which) -> {
                    String txt = articleId.getText().toString();
                    if (StringUtils.isBlank(txt)) {
                        alert(getString(R.string.artid_can_not_be_empty));
                    } else {
                        artId = txt;
                        initPage();
                    }
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog2.show();
        Objects.requireNonNull(dialog2.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("InflateParams")
    public void switchType(MenuItem item) {
        PopupMenu pMenu = new PopupMenu(this, findViewById(R.id.gif_tpl_hidden_top), Gravity.END);
        Menu menu = Util.iconMenu(pMenu.getMenu());
        String[] typeList = getTypeList();
        for (int i = 0; i < typeList.length; i++) {
            String t = typeList[i];
            if (t.equals(type)) {
                t += "          √";
            }
            menu.add(Menu.NONE, i, Menu.NONE, t).setIcon(iconCacheList.get("default"));
        }
        pMenu.setOnMenuItemClickListener(item1 -> {
            setType(typeList[item1.getItemId()]);
            getNewFlg = true;
            initPage();
            return true;
        });
        pMenu.show();
    }

    @SuppressLint("InflateParams")
    public void switchWeb(MenuItem item) {
        PopupMenu pMenu = new PopupMenu(this, findViewById(R.id.gif_tpl_hidden_top), Gravity.END);
        Menu menu = Util.iconMenu(pMenu.getMenu());
        for (int i = 0; i < webList.length; i++) {
            String web = webList[i];
            MenuItem mItem = menu.add(Menu.NONE, i, Menu.NONE, web).setIcon(BitmapUtil.scaleDrawable(iconCacheList.get(web), 100, 0));
            if (web.equals(webName)) {
                mItem.setTitle(web + "          √");
            }
        }
        pMenu.setOnMenuItemClickListener(item1 -> {
            setWebName(webList[item1.getItemId()]);
            getNewFlg = true;
            initPage();
            return true;
        });
        pMenu.show();
    }

    public void browserOpen(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.browser_open)
                .setIcon(R.drawable.ic_public_orange_60dp)
                .setPositiveButton(R.string.first_page, (dialog, which) -> {
                    String url = getSpy().getUrl(artId, null);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.current_page, (dialog, which) -> {
                    String url = getSpy().getUrl(artId, webPage);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    dialog.dismiss();
                })
                .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void loading() {
        if (loading == null) {
            loading = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
            GifImageView imgView = new GifImageView(this);
            imgView.setImageDrawable(iconCacheList.get("loading"));
            imgView.setMinimumHeight(180);
            imgView.setMinimumWidth(180);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.load_rotate);
            Objects.requireNonNull(loading.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
            loading.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loading.setContentView(imgView);
            // back不消失
            loading.setCancelable(false);
            loading.setOnShowListener(dialog -> imgView.startAnimation(anim));
        }
        loading.show();
    }

    public void oneKey(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.one_key_get)
                .setIcon(R.drawable.ic_flash_on_black_24dp)
                .setPositiveButton(R.string.all, (dialog, which) -> {
                    dialog.dismiss();
                    loading();
                    mark();
                    threadPoolExecutor.execute(() -> {
                        String[] types = getTypeList();
                        StringBuilder err = new StringBuilder();
                        for (String t : types) {
                            setType(t);
                            err.append(fetchAllWeb());
                        }
                        final String errString = err.toString();
                        runOnUiThread(() -> {
                            loading.dismiss();
                            seek(0, true);
                            if (errString.isEmpty()) {
                                alert(getString(R.string.success));
                            } else {
                                Toast.makeText(this, getString(R.string.error_tip, errString), Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                })
                .setNegativeButton(R.string.current_type, (dialog, which) -> {
                    mark();
                    dialog.dismiss();
                    loading();
                    threadPoolExecutor.execute(() -> {
                        String err = fetchAllWeb();
                        runOnUiThread(() -> {
                            loading.dismiss();
                            seek(0, true);
                            if (err.isEmpty()) {
                                alert(getString(R.string.success));
                            } else {
                                Toast.makeText(this, getString(R.string.error_tip, err), Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                })
                .setNeutralButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public void bookmarkOperation(MenuItem item) {
        ListPopupWindow bookmarkMenu = new ListPopupWindow(this);
        bookmarkMenu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new
                String[]{getString(R.string.bookmark), getString(R.string.add_bookmark), getString(R.string.go_to_last_mark)}));
        bookmarkMenu.setAnchorView(findViewById(R.id.gif_tpl_hidden_top));
        bookmarkMenu.setModal(true);
        bookmarkMenu.setWidth(450);
        bookmarkMenu.setDropDownGravity(Gravity.END);
        bookmarkMenu.setOnItemClickListener((parent1, view2, position2, id1) -> {
            switch (position2) {
                case 0:
                    this.listBookmark();
                    break;
                case 1:
                    this.mark();
                    break;
                case 2:
                    this.seek(0, false);
                    break;
                default:
                    break;
            }
            bookmarkMenu.dismiss();
        });
        bookmarkMenu.show();
    }

    private void listBookmark() {
        LinkedList<HashMap<String, String>> markList = getBookmark();
        if (markList.size() == 0){
            alert("Bookmark List is Empty");
            return;
        }
        PopupWindow listPage = new PageListPopupWindow.Builder(this)
                .setListCountGetter(where -> 8)
                .setListGetter((page, list, where) -> {
                    markList.forEach(hm -> {
                                HashMap<String, Object> item = new HashMap<>(2);
                                item.put("icon", iconCacheList.get(hm.get("web_name")));
                                item.put("name", hm.get("title") + "，" + hm.get("web_name") + "，" + hm.get("type") + "，" + hm.get("art_id") + "，" + hm.get("page"));
                                list.add(item);
                            });
                    return list;
                })
                .setItemClickListener((pageListPopupWindow, position) -> {
                    seek(position, false);
                    pageListPopupWindow.dismiss();
                })
                .setItemLongClickListener((pageListPopupWindow, position) -> {
                    // 使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
                    PopupMenu pMenu = new PopupMenu(this, pageListPopupWindow.rv.getLayoutManager().findViewByPosition(position));
                    pMenu.getMenuInflater().inflate(R.menu.gif_bookmark_popupmenu, pMenu.getMenu());
                    pMenu.setOnMenuItemClickListener(item1 -> {
                                switch (item1.getItemId()) {
                                    case R.id.seek_del_mark:
                                        seek(position, true);
                                        pageListPopupWindow.dismiss();
                                        break;
                                    case R.id.delete_mark:
                                        removeMark(position);
                                        if (markList.size() > 1) {
                                            pageListPopupWindow.remove(position);
                                        } else {
                                            pageListPopupWindow.dismiss();
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                return false;
                            });
                    pMenu.show();
                    return false;
                })
                .setWindowHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .build();
        listPage.showAsDropDown(findViewById(R.id.gif_tpl_hidden_top));
    }

    private LinkedList<HashMap<String,String>> getBookmark() {
        SharedPreferences sp = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Type typeToken = new TypeToken<LinkedList<HashMap<String,String>>>(){}.getType();
        return gson.fromJson(sp.getString(SettingActivity.BOOKMARK, "[]"), typeToken);
    }

    private void mark() {
        LinkedList<HashMap<String,String>> bookmark = getBookmark();
        HashMap<String, String> mark = new HashMap<>(5);
        mark.put("type", type);
        mark.put("web_name", webName);
        mark.put("art_id", artId);
        mark.put("page", page + "");
        mark.put("title", title);
        bookmark.addFirst(mark);
        int markNumLimit = 8;
        if (bookmark.size() > markNumLimit) {
            bookmark.removeLast();
        }
        Type typeToken = new TypeToken<LinkedList<HashMap<String,String>>>(){}.getType();
        getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit()
                .putString(SettingActivity.BOOKMARK, new Gson().toJson(bookmark, typeToken))
                .apply();
        alert(getString(R.string.add_successfully));
    }

    private void seek(int pos, boolean del) {
        LinkedList<HashMap<String,String>> bookmark = getBookmark();
        if (pos < bookmark.size()) {
            HashMap<String, String> mark = bookmark.get(pos);
            setType(mark.get("type"));
            setWebName(mark.get("web_name"));
            artId = mark.get("art_id");
            try {
                fetchItemList();//loadDb，非network
            } catch (Exception e) {
                alert(e.getMessage());
            }
            int newPage = Integer.parseInt(mark.get("page"));
            if (page == newPage) {
                currentFragment.refresh();
            } else {
                mViewPager.setCurrentItem(newPage - 1, true);
            }
            if (del) {
                bookmark.remove(pos);
                Type typeToken = new TypeToken<LinkedList<HashMap<String,String>>>(){}.getType();
                getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit()
                        .putString(SettingActivity.BOOKMARK, new Gson().toJson(bookmark, typeToken))
                        .apply();
            }
        } else {
            alert(getString(R.string.not_existed_bookmark));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void removeMark(int index) {
        LinkedList<HashMap<String,String>> bookmark = getBookmark();
        if (index < bookmark.size()) {
            bookmark.remove(index);
            Type typeToken = new TypeToken<LinkedList<HashMap<String,String>>>(){}.getType();
            getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit()
                    .putString(SettingActivity.BOOKMARK, new Gson().toJson(bookmark, typeToken))
                    .apply();
        } else {
            alert(getString(R.string.not_existed_bookmark));
        }
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public void listHistory(MenuItem item) {
        PopupWindow pageWindow = new PageListPopupWindow.Builder(this)
                .setListCountGetter(this::getHistoryCount)
                .setListGetter(this::getHistoryList)
                .setItemClickListener((pageWin, position) -> {
                    setWebName((String) pageWin.ma.getItemData(position).get("web_name"));
                    MyLog.w("historyClick", webName);
                    artId = (String) pageWin.ma.getItemData(position).get("art_id");
                    initPage();
                    pageWin.popupWindow.dismiss();
                })
                .setItemLongClickListener((pageWin, position) -> {
                    // 使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
                    PopupMenu pMenu = new PopupMenu(this, pageWin.rv.getLayoutManager().findViewByPosition(position));
                    pMenu.getMenuInflater().inflate(R.menu.gif_history_popupmenu, pMenu.getMenu());
                    pMenu.setOnMenuItemClickListener(item1 -> {
                        switch (item1.getItemId()) {
                            case R.id.delete_history:
                                // 删除记录
                                String delArtId = pageWin.ma.getItemData(position).get("art_id").toString();
                                String delWebName = (String) pageWin.ma.getItemData(position).get("web_name");
                                deleteHistory(delArtId, delWebName);
                                pageWin.remove(position);
                                break;
                            case R.id.copy_history_artid:
                                String articleId = pageWin.ma.getItemData(position).get("art_id").toString();
                                ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                assert clm != null;
                                clm.setPrimaryClip(ClipData.newPlainText(null, articleId));
                                alert(getString(R.string.clip_toast) + "：" + articleId);
                                break;
                            case R.id.reload_art:
                                artId = (String) pageWin.ma.getItemData(position).get("art_id");
                                setWebName((String) pageWin.ma.getItemData(position).get("web_name"));
                                deleteHistory(artId, webName);
                                initPage();
                                pageWin.popupWindow.dismiss();
                                break;
                            case R.id.filter_where:
                                showFilterInput(pageWin);
                                break;
                            default:
                                break;
                        }
                        return false;
                    });
                    pMenu.show();
                    return true;
                }).setPageSize(HISTORY_PAGE_SIZE)
                .setWindowHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .build();
        pageWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
    }

    private int getHistoryCount(String andWhere) {
        String where = "art_id <> '' and type = '" + type + "'";
        if (!StringUtils.isBlank(andWhere)) {
            where += " and " + andWhere;
        }
        Cursor cus = db.rawQuery("select count(*) from " + DbHelper.TB_IMG_WEB + " where " + where, null);
        cus.moveToFirst();
        int count = cus.getInt(0);
        cus.close();
        return count;
    }

    private void deleteHistory(String delArtId, String delWebName) {
        MyLog.w("deleteHistory", "art_id：" + delArtId + "，web_name：" + delWebName);
        db.delete(DbHelper.TB_IMG_WEB, "art_id = ? and web_name = ?", new String[]{delArtId, delWebName});
        db.delete(DbHelper.TB_IMG_WEB_ITEM, "art_id = ? and web_name = ?", new String[]{delArtId, delWebName});
    }

    private ArrayList<HashMap<String, Object>> getHistoryList(int page, ArrayList<HashMap<String, Object>> historyList, String andWhere) {
        int offset = (page - 1) * HISTORY_PAGE_SIZE;
        String where = "art_id <> '' and type = '" + type + "'";
        if (andWhere != null) {
            where += " and " + andWhere;
        }
        String sql = "select * from " + DbHelper.TB_IMG_WEB + " where " + where +  " order by id desc limit " + HISTORY_PAGE_SIZE + " offset " + offset;
        Cursor cus = db.rawQuery(sql, null);
        int count = cus.getCount();
        MyLog.w("getHistoryList：", count + "");
        cus.moveToFirst();
        for (int i = 0, listSize = historyList.size(); i < HISTORY_PAGE_SIZE; i++) {
            HashMap<String, Object> item;
            if (i < listSize) {
                item = historyList.get(i);
            } else {
                historyList.add(item = new HashMap<>(4));
            }
            if (count > i) {
                String web = cus.getString(cus.getColumnIndex("web_name"));
                item.put("web_name", web);
                item.put("name", cus.getString(cus.getColumnIndex("title")));
                item.put("art_id", cus.getString(cus.getColumnIndex("art_id")));
                item.put("icon", iconCacheList.get(web));
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
                .setListCountGetter(this::getFavoriteCount)
                .setListGetter(this::getFavoriteList)
                .setItemClickListener((pageWin, position) -> viewFav(pageWin.list, position, pageWin.rootView))
                .setItemLongClickListener((pageWin, position) -> {
                    // 使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
                    PopupMenu pMenu = new PopupMenu(this, pageWin.rv.getLayoutManager().findViewByPosition(position));
                    pMenu.getMenuInflater().inflate(R.menu.gif_favorite_popupmenu, pMenu.getMenu());
                    pMenu.setOnMenuItemClickListener(item1 -> {
                        switch (item1.getItemId()) {
                            case R.id.delete_fav:
                                //删除记录
                                String id = (String) pageWin.ma.getItemData(position).get("id");
                                String itemId = (String) pageWin.ma.getItemData(position).get("item_id");
                                deleteFavorite(id, itemId);
                                pageWin.remove(position);
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
                                pageWin.remove(position);
                                break;
                            case R.id.filter_where:
                                showFilterInput(pageWin);
                                break;
                            case R.id.find_repeated_files:
                                loading();
                                threadPoolExecutor.execute(() -> {
                                    try {
                                        String favIds = getRepeatedItems();
                                        runOnUiThread(() -> {
                                            if (StringUtils.isBlank(favIds)) {
                                                alert(getString(R.string.not_found));
                                            } else {
                                                pageWin.where = "id in (" + favIds + ")";
                                                pageWin.reset();
                                            }
                                            loading.dismiss();
                                        });
                                    } catch (Exception e) {
                                        runOnUiThread(() -> {
                                            alert(e.getLocalizedMessage());
                                            loading.dismiss();
                                        });
                                        e.printStackTrace();
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                        return false;
                    });
                    pMenu.show();
                    return true;
                }).setPageSize(FAVORITE_PAGE_SIZE)
                .setWindowHeight(ViewGroup.LayoutParams.MATCH_PARENT)
                .build();
        pageWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
    }

    @SuppressLint("InflateParams")
    private void showFilterInput(PageListPopupWindow pageWin) {
        //实例化布局
        View view2 = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, null);
        EditText where = view2.findViewById(R.id.dialog_input);
        where.setHint(R.string.filter_where);
        where.setText(pageWin.where);
        //创建对话框
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.filter_where)
                // 添加布局
                .setView(view2)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> {
                    pageWin.where = where.getText().toString();
                    try {
                        pageWin.reset();
                        if (pageWin.getTotal() == 0) {
                            throw new Exception("no data");
                        }
                    } catch (Exception e) {
                        alert(e.getLocalizedMessage());
                        pageWin.where = null;
                        pageWin.reset();
                        e.printStackTrace();
                    }
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog2.show();
        Objects.requireNonNull(dialog2.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private String getRepeatedItems() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type);
        File[] fileList = dir.listFiles(file -> file.isFile() && file.length() > 1024);
        Properties props = new Properties();
        File propFile = new File(dir, "meta");
        try (FileReader fr = new FileReader(propFile)) {
            props.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder ids = new StringBuilder();
        if (fileList != null) {
            long[] metas = new long[fileList.length];
            for (int i = 0; i < fileList.length; i++) {
                metas[i] = BitmapUtil.cachedImgMeta(fileList[i], props);
                /* MyLog.w("getRepeatedItems", fileList[i].getName() + "------" + Long.toBinaryString(metas[i])); */
            }
            for (int i = 0; i < fileList.length; i++) {
                if (metas[i] == 0) {
                    continue;
                }
                boolean flg = false;
                for (int j = i + 1; j < fileList.length; j++) {
                    if (metas[i] == 0) {
                        continue;
                    }
                    if (BitmapUtil.cmpImgMeta2(metas[i], metas[j])) {
                        flg = true;
                        /* MyLog.w("getRepeatedItems", fileList[j].getName() + "------" + Long.toBinaryString(metas[j])); */
                        metas[j] = 0;
                        String fname = fileList[j].getName();
                        int idx = fname.indexOf("_");
                        if (idx < 5 && idx > 0) {
                            ids.append(fname, 0, idx).append(",");
                        }
                    }
                }
                if (flg) {
                    /* MyLog.w("getRepeatedItems", fileList[i].getName() + "------" + Long.toBinaryString(metas[i])); */
                    metas[i] = 0;
                    String fname = fileList[i].getName();
                    int idx = fname.indexOf("_");
                    /* MyLog.w("getRepeatedItems", "___________________"); */
                    if (idx < 5 && idx > 0) {
                        ids.append(fname, 0, idx).append(",");
                    }
                }
            }
            if (ids.length() > 0) {
                ids.deleteCharAt(ids.lastIndexOf(","));
            }
        }
        MyLog.w("getRepeatedItems:ret", ids.toString());
        try (FileWriter fw = new FileWriter(propFile)) {
            props.store(fw, "img meta");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids.toString();
    }

    private int getFavoriteCount(String andWhere) {
        String where = "type = '" + type + "'";
        if (!StringUtils.isBlank(andWhere)) {
            where += " and " + andWhere;
        }
        Cursor cus = db.rawQuery("select count(*) from " + DbHelper.TB_IMG_FAVORITE + " where " + where, null);
        cus.moveToFirst();
        int count = cus.getInt(0);
        cus.close();
        MyLog.w("getFavoriteCount", count + "");
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
            String id = (String) item.get("id");
            String name = id + "_" + item.get("title");
            String cacheKey = genCacheKey(item.get("id").toString(), "favorite");
            // 混合图网页（3dm）
            download(item.get("type") + "/" + name, cacheKey, v);
            return false;
        });
        imgView.setOnTouchListener(new View.OnTouchListener() {
            int vScrollX;
            float mPosX, mPosY, mCurPosX, mCurPosY;
            private final Runnable runnable = imgView::performLongClick;

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
                        // 向左滑動
                        int hInteval = 200, vInteval = 250;
                        if (mCurPosX - mPosX > hInteval) {
                            viewPre();
                            break;
                        // 向右滑动
                        } else if (mCurPosX - mPosX < -hInteval) {
                            viewNext();
                            break;
                        }
                        if (mCurPosY - mPosY > vInteval) {
                            dialog.dismiss();
                            break;
                        }
                        break;
                    default:
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

    /**
     * 下载图片到download文件夹
     * @param pathname dir/name
     * @param cacheKey key
     * @param view view
     */
    private void download(String pathname, String cacheKey, View view) {
        PermissionUtil.req(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.PER_REQ_STORAGE_WRT, () -> {
            Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
            File img = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + pathname);
            MyLog.w("download", "path:" + img.getAbsolutePath());
            try {
                if (!img.getParentFile().exists() && !img.getParentFile().mkdirs()) {
                    MyLog.w("create dir error", img.getParentFile().getAbsolutePath());
                    throw new Exception("create dir error");
                }
                File cacheImg = diskLRUCache.get(cacheKey);
                if (cacheImg == null || !cacheImg.exists()) {
                    throw new Exception(getString(R.string.cache_is_lost));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.copy(cacheImg.toPath(), img.toPath());
                } else {
                    if (!img.exists() && !img.createNewFile()) {
                        throw new Exception("create file error");
                    }
                    try (FileInputStream input = new FileInputStream(cacheImg); FileOutputStream output = new FileOutputStream(img)) {
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
                // 发送广播
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(img)));
                Snackbar.make(view, R.string.download_success, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.open, v -> {
                            Intent imgView = new Intent(Intent.ACTION_VIEW);
                            imgView.setDataAndType(FileProvider.getUriForFile(this, "com.maxiye.first.fileprovider", img), "image/*");
                            // 不加黑屏读取不了
                            imgView.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        url = StringUtils.isBlank(url) ? (String) item.get("url") : url;
        dialog.setTitle(Html.fromHtml("<p style='color: #F66725; text-align: center'>" + title + "</p>", Html.FROM_HTML_MODE_COMPACT));
        if (favViewTask != null) {
            favViewTask.cancel(true);
        }
        favViewTask = new ImgViewTask(imgView, this);
        favViewTask.executeOnExecutor(threadPoolExecutor, title, url, (String) item.get("id"), (String) item.get("path"));
    }

    private String genCacheKey(String subfix, String keyType) {
        switch (keyType) {
            case "favorite":
                return "favorite_" + type + "-" + subfix;
            default:
                return webName + "_" + artId.replaceAll("[?*:\"\\\\<>/|]", "#") + "-" + subfix;
        }
    }

    static class ImgViewTask extends AsyncTask<String, Integer, Drawable> {

        private final WeakReference<GifActivity> gifActivityWR;
        private final WeakReference<GifImageView> imgViewWR;
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
            String imgKey = activity.genCacheKey(strings[2], "favorite");
            String path = strings[3], typeGif = "gif";
            File cache = StringUtils.isBlank(path) ? activity.diskLRUCache.get(imgKey) : new File(path);
            if (cache != null) {
                try {
                    MyLog.w("loadImg(fromCache)", "title：" + strings[0] + ";url：" + strings[1]);
                    FileInputStream fis = null;
                    Drawable imgDrawable = typeGif.equals(type) ? new GifDrawable(cache) : Drawable.createFromStream(fis = new FileInputStream(cache), null);
                    if (fis != null) {
                        fis.close();
                    }
                    return imgDrawable;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //移动网络禁止 gif
                if (activity.isGprs && typeGif.equals(type)) {
                    activity.runOnUiThread(() -> activity.alert(activity.getString(R.string.gprs_network)));
                    if (!activity.gprsContinue) {
                        return null;
                    }
                }
                cache = new File(activity.getCacheDir(), imgKey);
                Request request = new Request.Builder().url(strings[1]).build();
                try {
                    MyLog.w("loadImg(fromNet)", "title：" + strings[0] + ";url：" + strings[1]);
                    ResponseBody responseBody = activity.okHttpClient.newCall(request).execute().body();
                    assert responseBody != null;
                    int contentLength = (int) responseBody.contentLength(), lengthLimit = 40960 << 1;
                    byte[] bytes;
                    if (contentLength > lengthLimit) {
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
                        responseBody.close();
                    } else {
                        bytes = responseBody.bytes();
                    }
                    Drawable imgDrawable = typeGif.equals(type) ? new GifDrawable(bytes) : Drawable.createFromStream(new ByteArrayInputStream(bytes), null);
                    if (imgDrawable == null) {
                        throw new Exception("get image err");
                    }
                    RandomAccessFile raf = new RandomAccessFile(cache, "rwd");
                    raf.write(bytes);
                    raf.close();
                    gifActivityWR.get().diskLRUCache.put(imgKey, imgKey, cache.length());
                    return imgDrawable;
                } catch (Exception e) {
                    e.printStackTrace();
                    if (cache.delete()) {
                        MyLog.d("cacheDel", "cacheGif deleted");
                    }
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
                float zoom = "gif".equals(type) ? 3f : 5.5f;
                int width = Math.round(drawable.getIntrinsicWidth() * zoom);
                int height = Math.round(drawable.getIntrinsicHeight() * zoom);
                imgView.setMinimumHeight(height);
                imgView.setMinimumWidth(width);
            }
            super.onPostExecute(drawable);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 0) {
                circleProgress.setMaxProgress(values[1]);
            }
            circleProgress.setCurProgress(values[0]);
            super.onProgressUpdate(values);
        }
    }

    private void deleteFavorite(String delId, String itemId) {
        ContentValues ctv = new ContentValues(1);
        ctv.put("fav_flg", 0);
        int rows = db.update(DbHelper.TB_IMG_WEB_ITEM, ctv, "id = ?", new String[]{itemId});
        MyLog.w("db_item_remove_fav: ", rows + "");
        db.delete(DbHelper.TB_IMG_FAVORITE, "id = ?", new String[]{delId});
        MyLog.w("deleteFavorite", "id：" + delId);
    }

    private ArrayList<HashMap<String, Object>> getFavoriteList(int page, ArrayList<HashMap<String, Object>> favoriteList, String andWhere) {
        int offset = (page - 1) * FAVORITE_PAGE_SIZE;
        String where = "type = '" + type + "'";
        if (andWhere != null) {
            where += " and " + andWhere;
        }
        String sql = "select * from " + DbHelper.TB_IMG_FAVORITE + " where " + where + " order by id desc limit " + FAVORITE_PAGE_SIZE + " offset " + offset;
        Cursor cus = db.rawQuery(sql, null);
        int count = cus.getCount();
        MyLog.w("getFavoriteList：", count + "");
        cus.moveToFirst();
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type + "/";
        for (int i = 0, listSize = favoriteList.size(); i < FAVORITE_PAGE_SIZE; i++) {
            HashMap<String, Object> item;
            if (i < listSize) {
                item = favoriteList.get(i);
            } else {
                favoriteList.add(item = new HashMap<>(9));
            }
            if (count > i) {
                item.put("id", cus.getString(cus.getColumnIndex("id")));
                item.put("item_id", cus.getString(cus.getColumnIndex("item_id")));
                String title = cus.getString(cus.getColumnIndex("title"));
                item.put("title", title);
                item.put("url", cus.getString(cus.getColumnIndex("url")));
                item.put("real_url", cus.getString(cus.getColumnIndex("real_url")));
                item.put("type", cus.getString(cus.getColumnIndex("type")));
                String name = item.get("id") + "_" + title;
                cus.moveToNext();
                File file = new File(dir + name);
                if (file.exists()) {
                    item.put("path", dir + name);
                    // KB
                    long size = file.length() >> 10;
                    item.put("name", title + "（" + size + "K）" + "<span style='color: #13b294'>&emsp;√</span>");
                } else {
                    item.put("path", "");
                    item.put("name", title);
                    file = diskLRUCache.get(genCacheKey(item.get("id").toString(), "favorite"));
                    if (file == null) {
                        item.put("icon", iconCacheList.get("default"));
                        continue;
                    }
                    // KB
                    long size = file.length() >> 10;
                    item.put("name", title + "（" + size + "K）");
                }
                try {
                    item.put("icon", new BitmapDrawable(getResources(), BitmapUtil.getBitmap(file, 50, 50)));
                    /* MyLog.w("getFavoriteList-oompress", "fileSize：" + file.length() >> 10 + " kB；compress：" + newOpts.inSampleSize); */
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

    private void checkPageEnd() {
        int nowPage = mViewPager.getCurrentItem() + 1;
        int pages = imgList.size() % 3 == 0 ? imgList.size() / 3 : imgList.size() / 3 + 1;
        if (nowPage > pages) {
            alert(getString(R.string.back_to_last_page));
            mViewPager.setCurrentItem(pages - 1, true);
        }
    }

    private boolean loadDbImgList() {
        Cursor cus = db.query(DbHelper.TB_IMG_WEB, new String[]{"*"}, "art_id = ? and web_name = ?", new String[]{artId, webName}, null, null, "id desc", "1");
        MyLog.w("db_web", cus.getCount() + "");
        if (cus.getCount() > 0) {
            cus.moveToFirst();
            title = cus.getString(cus.getColumnIndex("title"));
            int totalPage = cus.getInt(cus.getColumnIndex("pages"));
            Cursor cus2 = db.query(DbHelper.TB_IMG_WEB_ITEM, new String[]{"page,title,url,ext,real_url"}, "art_id = ? and web_name = ?", new String[]{artId, webName}, null, null, "id asc");
            int count = cus2.getCount();
            if (count > 0) {
                MyLog.w("db_item", count + "");
                cus2.moveToFirst();
                for (int i = 0; i < count; i++) {
                    HashMap<String, String> imgInfo = new HashMap<>(4);
                    imgInfo.put("url", cus2.getString(cus2.getColumnIndex("url")));
                    imgInfo.put("title", cus2.getString(cus2.getColumnIndex("title")));
                    imgInfo.put("ext", cus2.getString(cus2.getColumnIndex("ext")));
                    imgInfo.put("real_url", cus2.getString(cus2.getColumnIndex("real_url")));
                    imgList.add(imgInfo);
                    webPage = cus2.getInt(cus2.getColumnIndex("page"));
                    cus2.moveToNext();
                }
            }
            cus2.close();
            if (totalPage == webPage) {
                endFlg = true;
            }
            ++webPage;
            return true;
        }
        cus.close();
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private void updateDbField(String field, String val) {
        ContentValues ctv = new ContentValues(1);
        ctv.put(field, val);
        int rows = db.update(DbHelper.TB_IMG_WEB, ctv, "art_id = ? and web_name = ?", new String[]{artId, webName});
        MyLog.w("db_web_update: ", rows + "");
    }

    private void updateExt(HashMap<String, String> imgInfo) {
        ContentValues ctv = new ContentValues(2);
        String ext = imgInfo.get("ext");
        String name = imgInfo.get("title");
        name = name.substring(0, name.lastIndexOf(".")) + ext;
        imgInfo.put("title", name);
        ctv.put("ext", ext);
        ctv.put("title", name);
        // 动位混合处理（3dm）
        ctv.put("type", ".gif".equals(ext) ? "gif" : "bitmap");
        db.update(DbHelper.TB_IMG_WEB_ITEM, ctv, "art_id = ? and url = ?", new String[]{artId, imgInfo.get("url")});
        MyLog.w("db_item_update_ext: ", name + "；" + ext + "；" + imgInfo.get("url"));
    }

    private void saveDbWeb(String title, String url) {
        String datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        ContentValues ctv = new ContentValues(6);
        ctv.put("art_id", artId);
        ctv.put("web_url", url);
        ctv.put("web_name", webName);
        ctv.put("type", type);
        ctv.put("title", title);
        ctv.put("time", datetime);
        long newId = db.insert(DbHelper.TB_IMG_WEB, null, ctv);
        MyLog.w("db_web_insert: ", newId + "");
    }

    public void saveDbImgList(HashMap<String, String> data) {
        String datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        ContentValues ctv = new ContentValues(9);
        ctv.put("art_id", artId);
        ctv.put("page", webPage);
        ctv.put("web_name", webName);
        // 动位混合处理（3dm）
        ctv.put("type", ".gif".equals(data.get("ext")) ? "gif" : "bitmap");
        ctv.put("title", data.get("title"));
        ctv.put("url", data.get("url"));
        ctv.put("ext", data.get("ext"));
        ctv.put("real_url", data.get("real_url"));
        ctv.put("time", datetime);
        long newId = db.insert(DbHelper.TB_IMG_WEB_ITEM, null, ctv);
        MyLog.w("db_web_item_insert: ", newId + "");
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private int page, imgPosition = 1, focusedPosition = 1;
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
            activity = null;
            super.onDestroy();
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
            int gifCount = 3;
            for (int i = 1; i <= gifCount; i++) {
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
                    MyLog.w("threadPoolExecutor", "shutdownNow");
                }
                if (imgPosition == 1) {
                    checkLoad();
                }
            }
        }

        void send(int what, int arg1, int arg2, Object obj) {
            if (activity != null && page == activity.page && activity.myHandler != null) {
                activity.myHandler.sendMessage(activity.myHandler.obtainMessage(what, arg1, arg2, obj));
            }
        }

        private int getImgOffset(int index) {
            return (page - 1) * 3 + index - 1;
        }

        private void longClickCb(int position, View view) {
            focusedPosition = position;
            ListPopupWindow listMenu = new ListPopupWindow(activity);
            listMenu.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, new
                    String[]{getString(R.string.add_bookmark), getString(R.string.add_to_fav), getString(R.string.download)}));
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            listMenu.setAnchorView(view);
            int width = dm.widthPixels >> 1, offsetX = dm.widthPixels >> 2, offsetY = dm.heightPixels * 2 / 3;
            listMenu.setWidth(width);
            listMenu.setHorizontalOffset(offsetX);
            listMenu.setVerticalOffset(offsetY);
            listMenu.setOnItemClickListener((parent, view1, position1, id) -> {
                switch (position1) {
                    case 0:
                        activity.mark();
                        break;
                    case 1:
                        addFav();
                        break;
                    case 2:
                        long favId = addFav();
                        HashMap<String, String> gifInfo = activity.getImgInfo(getImgOffset(focusedPosition));
                        String cacheKey = activity.genCacheKey(getImgOffset(focusedPosition) + "", "");
                        assert gifInfo != null;
                        // 混合图网页（3dm）
                        String dir = ".gif".equals(gifInfo.get("ext")) ? "gif" : "bitmap";
                        activity.download(dir + "/" + favId + "_" + gifInfo.get("title"), cacheKey, activity.findViewById(android.R.id.content));
                        break;
                    default:
                        break;
                }
                listMenu.dismiss();
            });
            listMenu.show();
        }

        private long addFav() {
            HashMap<String, String> imgInfo = activity.getImgInfo(getImgOffset(focusedPosition));
            assert imgInfo != null;
            Cursor cus = activity.db.query(DbHelper.TB_IMG_WEB_ITEM, new String[]{"*"}, "art_id = ? and title = ?", new String[]{artId, imgInfo.get("title")}, null, null, "id desc", "1");
            long favId = 0;
            if (cus.getCount() > 0) {
                cus.moveToFirst();
                if (cus.getInt(cus.getColumnIndex("fav_flg")) != 1) {
                    ContentValues ctv = new ContentValues(10);
                    ctv.put("item_id", cus.getLong(cus.getColumnIndex("id")));
                    ctv.put("art_id", cus.getString(cus.getColumnIndex("art_id")));
                    ctv.put("page", cus.getInt(cus.getColumnIndex("page")));
                    ctv.put("web_name", cus.getString(cus.getColumnIndex("web_name")));
                    ctv.put("type", cus.getString(cus.getColumnIndex("type")));
                    ctv.put("title", cus.getString(cus.getColumnIndex("title")));
                    ctv.put("url", cus.getString(cus.getColumnIndex("url")));
                    ctv.put("ext", cus.getString(cus.getColumnIndex("ext")));
                    ctv.put("real_url", cus.getString(cus.getColumnIndex("real_url")));
                    ctv.put("time", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
                    favId = activity.db.insert(DbHelper.TB_IMG_FAVORITE, null, ctv);
                    MyLog.w("db_img_fav_insert: ", favId + "");
                    ContentValues ctv2 = new ContentValues(1);
                    ctv2.put("fav_flg", 1);
                    int rows = activity.db.update(DbHelper.TB_IMG_WEB_ITEM, ctv2, "id = ?", new String[]{cus.getString(cus.getColumnIndex("id"))});
                    MyLog.w("db_web_item_update: ", rows + "");
                    activity.alert(getString(R.string.add_successfully));
                } else {
                    Cursor cus2 = activity.db.query(DbHelper.TB_IMG_FAVORITE, new String[]{"id"}, "item_id = ?", new String[]{cus.getString(cus.getColumnIndex("id"))}, null, null, "id desc", "1");
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
            String imgKey = activity.genCacheKey(getImgOffset(imgPosition) + "", "");
            //移动网络禁止 gif
            if (activity.isGprs && "gif".equals(type) && !activity.diskLRUCache.containsKey(imgKey)) {
                if (!activity.gprsContinue) {
                    @SuppressLint("InflateParams")
                    View view = LayoutInflater.from(activity).inflate(R.layout.gif_gprs_popup_confirm, null);
                    PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    view.findViewById(R.id.bt_ok).setOnClickListener(v -> {
                        activity.threadPoolExecutor.execute(this::loadImg);
                        activity.gprsContinue = true;
                        popupWindow.dismiss();
                    });
                    view.findViewById(R.id.bt_cancel).setOnClickListener(v -> popupWindow.dismiss());
                    popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
                } else {
                    activity.threadPoolExecutor.execute(this::loadImg);
                }
                activity.alert(getString(R.string.gprs_network));
            } else {
                activity.threadPoolExecutor.execute(this::loadImg);
            }
        }

        void loadImg() {
            int nowPos = imgPosition, startOffset = getImgOffset(nowPos);
            MyLog.w("info", "loadImg:" + startOffset);
            HashMap<String, String> imgInfo = activity.getImgInfo(startOffset);
            if (imgInfo == null) {
                return;
            }
            String imgKey = activity.genCacheKey(startOffset + "", "");
            String gifExt = ".gif";
            File cacheImg = activity.diskLRUCache.get(imgKey);
            send(MSG_TYPE_PRE, nowPos, 0, imgInfo.get("title"));
            if (cacheImg != null) {
                try {
                    FileInputStream fis = null;
                    Drawable imgDrawable = gifExt.equals(imgInfo.get("ext")) ? new GifDrawable(cacheImg) : Drawable.createFromStream(fis = new FileInputStream(cacheImg), null);
                    if (fis != null) {
                        fis.close();
                    }
                    send(MSG_TYPE_LOAD, nowPos, 0, imgDrawable);
                    MyLog.w("loadImg", "fromCache:" + imgInfo.get("title"));
                } catch (IOException e) {
                    send(MSG_TYPE_EMPTY, nowPos, 0, null);
                    e.printStackTrace();
                }
            } else {
                String url = StringUtils.isBlank(imgInfo.get("real_url")) ? imgInfo.get("url") : imgInfo.get("real_url");
                Request request = new Request.Builder().url(url).build();
                activity.okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        send(MSG_TYPE_EMPTY, nowPos, 0, null);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        File finalCacheFile = null;
                        try {
                            MyLog.w("loadImg", "fromNet:" + imgInfo.get("title") + ";url:" + url + ";index:" + request.toString());
                            ResponseBody responseBody = response.body();
                            assert responseBody != null;
                            int contentLength = (int) responseBody.contentLength(), lengthLimit = 40960 << 1;
                            byte[] bytes;
                            if (contentLength > lengthLimit) {
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
                                responseBody.close();
                            } else {
                                bytes = responseBody.bytes();
                            }
                            String ext = imgInfo.get("ext");
                            if (BitmapUtil.isGif(bytes) && !gifExt.equals(ext)) {
                                imgInfo.put("ext", ext = gifExt);
                                activity.updateExt(imgInfo);
                            }
                            Drawable imgDrawable = gifExt.equals(ext) ? new GifDrawable(bytes) : Drawable.createFromStream(new ByteArrayInputStream(bytes), null);
                            if (imgDrawable == null) {
                                throw new Exception("get image err");
                            }
                            send(MSG_TYPE_LOAD, nowPos, 0, imgDrawable);
                            finalCacheFile = new File(activity.getCacheDir(), imgKey + ext);
                            RandomAccessFile raf = new RandomAccessFile(finalCacheFile, "rwd");
                            raf.write(bytes);
                            raf.close();
                            activity.diskLRUCache.put(imgKey, imgKey + ext, finalCacheFile.length());
                        } catch (Exception e) {
                            if (finalCacheFile != null && finalCacheFile.delete()) {
                                MyLog.d("cacheDel", "cacheImg deleted");
                            }
                            send(MSG_TYPE_EMPTY, nowPos, 0, null);
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        void refresh() {
            /* CacheUtil.clearAllCache(activity); */
            activity.okHttpClient.dispatcher().cancelAll();
            this.imgPosition = 1;
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
        final GifActivity activity;

        SectionsPagerAdapter(FragmentManager fm, GifActivity activity) {
            super(fm);
            this.activity = activity;
        }

        @Override
        public Fragment getItem(int position) {
            MyLog.w("start", "getItem:" + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, activity);
        }

        @Override
        public int getCount() {
            return 80;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentFragment = (PlaceholderFragment) object;
            page = currentFragment.page;
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
            PlaceholderFragment fragment = activity.currentFragment;
            View rootView = fragment.getView();
            if (rootView == null) {
                return;
            }
            GifImageView imageView = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", activity.getPackageName()));
            switch (msg.what) {
                case PlaceholderFragment.MSG_TYPE_PRE:
                    if (msg.arg1 == 1) {
                        TextView textView = rootView.findViewById(R.id.section_label);
                        textView.setText(title + "：" + fragment.page);
                    }
                    TextView tv = rootView.findViewById(fragment.getResources().getIdentifier("gtxt_" + msg.arg1, "id", activity.getPackageName()));
                    tv.setText((String) msg.obj);
                    if (!activity.diskLRUCache.containsKey(activity.genCacheKey(fragment.getImgOffset(msg.arg1) + "", ""))) {
                        imageView.setImageDrawable(activity.iconCacheList.get("loading"));
                        imageView.setMinimumHeight(90);
                        imageView.setMinimumWidth(90);
                        imageView.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.load_rotate));
                    }
                    int imgCount = 3;
                    if (++fragment.imgPosition <= imgCount) {
                        fragment.checkLoad();
                    } else {
                        fragment.imgPosition = 1;
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
                    float scale = imgDrawable instanceof GifDrawable ? 2.5f : 4.5f;
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
                default:
                    break;
            }
        }
    }
}
