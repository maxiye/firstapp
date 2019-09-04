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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
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
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
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
import com.maxiye.first.util.IntList;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.NetworkUtil;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.StringUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * {@code 第1条：考虑用静态方法而不是构造器}
 * {@code 第27条：消除未检查警告} 钻石操作符
 * {@code 第51条：仔细设计方法签名}
 * 不要过分地提供方便的方法。
 * 避免过长的参数列表。相同类型的长序列参数尤其有害
 * 对于参数类型，优先选择接口而不是类
 * 与布尔型参数相比，优先使用两个元素枚举类型
 * {@code 第66条：明智谨慎地使用本地方法} native methods
 * {@code 第69条：仅在发生异常的条件下使用异常}
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
    public static final String GET_NEW_FLG_ARG = "GifActivity.getNewFlg";
    public static final String WEB_NAME_ARG = "GifActivity.webName";
    private static final Pattern ART_FILTER_PATTERN = Pattern.compile("[?*:\"\\\\<>/|]");
    /**
     * 当前art_id 匹配模式 ，以后可能更新
     */
    private static final Pattern ART_ID_PATTERN = Pattern.compile("[\\d_/\\-]{1,20}");
    private static final String DEFAULT_TITLE = "动态图";
    public static final String TYPE_GIF = "gif";
    public static final String TYPE_BITMAP = "bitmap";

    /**
     * 是否获取新的文章
      */
    private static boolean getNewFlg = true;
    private static boolean endFlg = false;
    private static String webName = "duowan";
    private static String type = TYPE_BITMAP;
    private static String title = DEFAULT_TITLE;
    private static String artId = "1023742";
    private static int webPage = 1;
    private int page = 1;
    /**
     * loadGifList错误计数器
     */
    private volatile int tryCount = 0;
    private static final int MAX_TRY_TIMES = 3;
    /**
     * 手机网络
     */
    private boolean isGprs = false;
    /**
     * 手机网络继续访问
     */
    private boolean gprsContinue = false;
    private PlaceholderFragment currentFragment;
    /**
     * {@code 第6条：避免创建不必要的对象}
     * {@code 第26条：不要使用原始类型}
     * 如果你使用了原始类型，你将会失去泛型所带来的安全性和可读性
     * 安全的方式是使用无限制通配符类型（unbounded wildcard types），泛型类型Set<E>的无限制通配符类型是Set<?>
     * 你可以将任意元素放入一个原始类型集合，轻而易举地破坏了集合的类型约束（就如前面描述的unsafeAdd方法），
     * 而你无法将任意元素（null除外）放入一个Collection<?>
     * Set<Object>是一个参数化的类型，表示一个可以包含任意类型的集合，Set<?>是一个通配符类型，表示一个只能包含某个未知类型的对象的集合，而Set是一个原始类型，不在泛型类型系统之内。前面两种是安全的，而最后一种是不安全的。
     *
     */
    private final ArrayList<HashMap<String, String>> imgList = new ArrayList<>(60);
    private JsonObject webCfg;
    private String[] webList;
    private HashMap<String, Drawable> iconCacheList;
    private Properties prop = new Properties();
    private OkHttpClient okHttpClient;
    private ThreadPoolExecutor threadPoolExecutor;
    private DiskLruCache diskLruCache;
    /**
     * {@code 第6条：避免创建不必要的对象}
     */
    private SQLiteDatabase db;
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
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.gif_activity_viewpager);
        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNewFlg = bundle.getBoolean(GET_NEW_FLG_ARG, true);
            webName = bundle.getString(WEB_NAME_ARG, "gamersky");
            MyLog.w("end", getNewFlg ? "true" : "false");
        }
        //网站配置信息
        webCfg = getWebCfg(webName);
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        // onkey 发生拥堵 --已修复
        threadPoolExecutor = new ThreadPoolExecutor(4, 7, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new ThreadPoolExecutor.DiscardOldestPolicy());
        diskLruCache = DiskLruCache.newInstance(this, type);
        // db获取,ui线程运行的 toast 可能报错
        db = DbHelper.newDb(this);
        // 网络监听
        watchNetwork();
        initPage();
        // 下拉刷新配置
        swipeRefresh();
        MyLog.w("end", "onCreateOver");
    }

    /**
     * {@code 第7条：消除过时的对象引用 第三种常见的缓存泄漏的来源是监听器和其它调用。}
     */
    @Override
    protected void onDestroy() {
        getSpy().close();
        NetworkUtil.unregister(this);
        db.close();
        okHttpClient.dispatcher().cancelAll();
        okHttpClient = null;
        currentFragment.activity = null;
        currentFragment = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (isGprs = NetworkUtil.isGprs(this)) {
            alert(getString(R.string.gprs_network));
            gprsContinue = false;
        }
        // fix nullPointerException
        if (currentFragment == null || currentFragment.activity == null) {
            mViewPager.setCurrentItem(page - 1, true);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        diskLruCache.serialize();
        super.onPause();
    }

    private void watchNetwork() {
        NetworkUtil.register(this, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                if (isGprs = NetworkUtil.isGprs(getApplicationContext())) {
                    alert(getString(R.string.gprs_network));
                    gprsContinue = false;
                }
                super.onAvailable(network);
            }

            @Override
            public void onLost(Network network) {
                alert(getString(R.string.no_internet));
                super.onLost(network);
            }
        });
    }

    private void swipeRefresh() {
        SwipeRefreshLayout refreshLayout = findViewById(R.id.gif_swipe_layout);
        refreshLayout.setOnRefreshListener(() -> {
            refresh();
            refreshLayout.setRefreshing(false);
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int startPage = 0;
            private int endPage = 0;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // MyLog.w("onPageScrolled", position + ";" + positionOffset + ";" + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                // onPageSelected先触发，然后onPageScrollStateChanged state == 0 触发
                endPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // MyLog.w("onPageScrollStateChanged", state + "");
                // 左右滑动开始
                if (state == 1) {
                    startPage = endPage;
                    refreshLayout.setEnabled(false);
                }
                // 2 左划停止，开始右滑，或相反
                // 左右滚动停止
                if (state == 0 && startPage == endPage) {
                    // 滑动后返回原页开启下滑刷新
                    ScrollView scrollView = findViewById(R.id.gif_scroll_view);
                    refreshLayout.setEnabled(scrollView.getScrollY() == 0);
                }
            }
        });
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
    public void alert(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private BaseSpy getSpy()
    {
        return SpyGetter.INSTANCE.getSpy(webName + type, webName, webCfg);
    }

    private void initPage() {
        okHttpClient.dispatcher().cancelAll();
        title = DEFAULT_TITLE;
        webPage = 1;
        endFlg = false;
        imgList.clear();
        if (mViewPager.getCurrentItem() == 0) {
            if (currentFragment != null) {
                currentFragment.refresh();
            }
        } else {
            mViewPager.setCurrentItem(0, true);
        }
    }

    /**
     * 校验item是否存在
     * @param offset int
     * @return boolean
     */
    private boolean validateOffset(int offset) {
        return imgList.size() > offset;
    }

    private void fetchNewArt() {
        String[] article = getSpy().getNewArticle();
        if (article.length > 0) {
            artId = article[0] != null ? article[0] : "xx";
            if (article[1] != null) {
                title = article[1];
            }
            endFlg = false;
        }
        getNewFlg = false;
        MyLog.w("getNewArticle", Arrays.toString(article));
    }

    private void fetchItemList() {
        title = DEFAULT_TITLE;
        webPage = 1;
        endFlg = false;
        imgList.clear();
        loadImgList();
    }

    /**
     * 使用submit，然后{@link Future#get()}会阻塞进程，加载框弹不出来
     */
    public void fetchAll() {
        loading();
        threadPoolExecutor.execute(() -> {
            getImgInfo(9999);
            mViewPager.setCurrentItem(page - 1, true);
            runOnUiThread(() -> loaded(getString(R.string.success_tip, imgList.size())));
        });
    }

    /**
     * 爬取所有网页的图片
     * @return int 失败的网站个数
     */
    @NonNull
    private String fetchAllWeb()
    {
        StringBuilder err = new StringBuilder();
        for (String name : webList) {
            int i = 0;
            setWebName(name);
            while (i < 3) {
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

    /**
     * {@code 第9条：优先使用try-with-resources而不是try-finally}
     * @param fName String
     * @return String
     */
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

    public Drawable getCachedIcon(String name) {
        if (iconCacheList == null) {
            iconCacheList = new HashMap<>(8);
            iconCacheList.put("default", getDrawable(R.drawable.ic_image_black_24dp));
            iconCacheList.put("loading", getDrawable(R.drawable.ic_autorenew_black_24dp));
            try (InputStream is = getAssets().open("img_spy_config.json")) {
                JsonObject icons = new Gson().fromJson(new InputStreamReader(is), JsonObject.class).getAsJsonObject("icon");
                for (String key : icons.keySet()) {
                    prop.setProperty(key + "-icon", icons.get(key).getAsString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (iconCacheList.containsKey(name)) {
            return iconCacheList.get(name);
        } else {
            String iconPath = prop.getProperty(name + "-icon");
            try (InputStream is = getAssets().open(iconPath)) {
                Drawable icon = BitmapDrawable.createFromStream(is, null);
                iconCacheList.put(name, icon);
                return icon;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return iconCacheList.get("default");
        }
    }

    private JsonObject getWebCfg(String webName) {
        JsonObject webCfg = null;
        try (InputStream is = getAssets().open("img_spy_config.json")) {
            JsonObject cfgs = new Gson().fromJson(new InputStreamReader(is), JsonObject.class).getAsJsonObject(type);
            webCfg = cfgs.getAsJsonObject(webName);
            if (webList == null) {
                webList = new String[cfgs.size()];
                int index = 0;
                for (String key : cfgs.keySet()) {
                    webList[index++] = key;
                }
                MyLog.w("getWebCfg-webList", Arrays.toString(webList));
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
        try (InputStream is = getAssets().open("img_spy_config.json")) {
            JsonObject cfgs = new Gson().fromJson(new InputStreamReader(is), JsonObject.class).getAsJsonObject(type);
            webList = new String[cfgs.size()];
            int index = 0;
            for (String key : cfgs.keySet()) {
                webList[index++] = key;
            }
            MyLog.w("loadWebList", Arrays.toString(webList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setType(@NonNull String t) {
        MyLog.w("setType", type);
        okHttpClient.dispatcher().cancelAll();
        if (!t.equals(type)) {
            type = t;
            webCfg = getWebCfg(webName);
            //3dm切换，一键获取问题
            if (webCfg == null) {
                MyLog.w("setTypeErr", t + "；" + webName);
            }
            loadWebList();
            diskLruCache.serialize();
            diskLruCache = DiskLruCache.newInstance(this, type);
        }
    }

    private void setWebName(String web) {
        webName = web;
        webCfg = getWebCfg(web);
        MyLog.w("setWebName", web);
    }

    /**
     * 快速定位
     * @param imgType String
     * @param web Strng webName
     * @param art String artId
     * @param newPage int
     */
    private void locate(String imgType, String web, String art, int newPage) {
        MyLog.w("locate", type + "#" + web + "#" + page);
        okHttpClient.dispatcher().cancelAll();
        if (!type.equals(imgType)) {
            type = imgType;
            loadWebList();
            diskLruCache.serialize();
            diskLruCache = DiskLruCache.newInstance(this, type);
        }
        webName = web;
        webCfg = getWebCfg(webName);
        //3dm切换，一键获取问题
        if (webCfg == null) {
            alert(getString(R.string.not_found));
        } else {
            artId = art;
            // loadDb，非network
            fetchItemList();
            mViewPager.setCurrentItem(newPage - 1, true);
            refresh();
        }
    }

    @NonNull
    public static String[] getTypeList() {
        return new String[]{GifActivity.TYPE_GIF, GifActivity.TYPE_BITMAP};
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
            Matcher matcher = spy.match(content);
            if (StringUtil.notBlank(content) && matcher.find()) {
                do {
                    HashMap<String, String> item = spy.findItem(matcher);
                    imgList.add(item);
                    insertDbImgItem(item);
                } while (matcher.find());
            } else {
                MyLog.println(content);
                tryCount = MAX_TRY_TIMES;
                throw new ProtocolException("over");
            }
            if (webPage == 1 && imgList.size() > 0) {
                MyLog.w("newArticle", spy.getCurrentUrl());
                if (DEFAULT_TITLE.equals(title)) {
                    String head = spy.getNewTitle(content);
                    if (head != null) {
                        title = head;
                        MyLog.w("getNewTitle", head);
                    }
                }
                insertDbWeb(title, spy.getCurrentUrl());
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

    @Nullable
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
        getMenuInflater().inflate(R.menu.gif_activity_actions, Util.enableMenuIcon(menu));
        return true;
    }


    /**
     * bitmap操作menu
     * @param view View
     */
    public void showBitmapPopup(View view, @NonNull Bitmap bitmap) {
        PopupMenu pMenu = new PopupMenu(this, view);
        pMenu.setGravity(Gravity.CENTER);
        pMenu.getMenuInflater().inflate(R.menu.main_activity_bitmap_popupmenu, Util.enableMenuIcon(pMenu.getMenu()));
        pMenu.getMenu().removeItem(R.id.gradual_color);
        pMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.gray_bitmap:
                    BitmapUtil.showBitmap4Save(this, bitmap, BitmapUtil::convertGray, "gray");
                    break;
                case R.id.dot_bitmap:
                    BitmapUtil.showBitmap4Save(this, bitmap, BitmapUtil::convertDot, "dot");
                    break;
                case R.id.dot_bitmap_comic:
                    BitmapUtil.showBitmap4Save(this, bitmap, BitmapUtil::convertComic, "comic");
                    break;
                case R.id.reverse_bitmap:
                    BitmapUtil.showBitmap4Save(this, bitmap, BitmapUtil::convertReverse, "reverse");
                    break;
                case R.id.single_chan_bitmap:
                    BitmapUtil.showBitmap4Save(this, bitmap, BitmapUtil::convertSingleChannel, "single-chan");
                    break;
                case R.id.dot_bitmap_txt:
                    String dotTxt = BitmapUtil.convertDotTxt(bitmap);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "bitmap_txt/" + UUID.randomUUID().toString() + "_dot.txt");
                    Util.saveFileEx(file, dotTxt);
                    this.alert(getString(R.string.success));
                    break;
                default:
                    break;
            }
            return false;
        });
        pMenu.show();
    }

    public void refresh() {
        loading();
        currentFragment.refresh();
        threadPoolExecutor.execute(() -> {
            try {
                int t = 0;
                ConnectionPool conPool = okHttpClient.connectionPool();
                while (t++ < 30) {
                    MyLog.w("connectPool", "idle:" + conPool.idleConnectionCount() + ";total:" + conPool.connectionCount());
                    Thread.sleep(300);
                    if (conPool.idleConnectionCount() == conPool.connectionCount()) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> loaded(null));
        });
    }

    public void goPage(MenuItem item) {
        //实例化布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, findViewById(R.id.get_img_ll), false);
        EditText pageEdit = view.findViewById(R.id.dialog_input);
        pageEdit.setHint(R.string.input_page);
        pageEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.go_page)
                // 添加布局
                .setView(view)
                .setPositiveButton(R.string.go_to, (dialog1, which) -> {
                    String itemIdxStr = pageEdit.getText().toString();
                    itemIdxStr = StringUtil.notBlank(itemIdxStr) ? itemIdxStr : "1";
                    mViewPager.setCurrentItem(Integer.parseInt(itemIdxStr) - 1, true);
                    if (endFlg) {
                        checkPageEnd();
                    }
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                }).setNeutralButton(R.string.last_page, (dialogInterface, i) -> {
                    mViewPager.setCurrentItem(999, true);
                    if (endFlg) {
                        checkPageEnd();
                    }
                })
                .create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void changeUrl(MenuItem item) {
        //实例化布局
        View view2 = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, findViewById(R.id.get_img_ll), false);
        EditText articleId = view2.findViewById(R.id.dialog_input);
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClip;
        CharSequence copiedId = null;
        articleId.setHint(R.string.input_artid);
        if (clm != null && (primaryClip = clm.getPrimaryClip()) != null) {
            copiedId = primaryClip.getItemAt(0).getText();
            if (ART_ID_PATTERN.matcher(copiedId).matches()) {
                articleId.setHint(copiedId);
            }
        }
        //创建对话框
        String finalCopiedId = copiedId == null ? "" : copiedId.toString();
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.go_to)
                // 添加布局
                .setView(view2)
                .setPositiveButton(R.string.go_to, (dialog1, which) -> {
                    String txt = articleId.getText().toString();
                    if (StringUtil.notBlank(txt) || StringUtil.notBlank((txt = finalCopiedId))) {
                        artId = txt;
                        initPage();
                    } else {
                        alert(getString(R.string.artid_can_not_be_empty));
                    }
                }).setNegativeButton(R.string.next_article, (dialog1, which) -> {
                    String next_art_id = getNextArtId();
                    if (StringUtil.notBlank(next_art_id)) {
                        artId = next_art_id;
                        initPage();
                    } else {
                        alert(getString(R.string.no_more));
                    }
                }).setNeutralButton(R.string.prev_article, (dialogInterface, i) -> {
                    String prev_art_id = getPrevArtId();
                    if (StringUtil.notBlank(prev_art_id)) {
                        artId = prev_art_id;
                        initPage();
                    } else {
                        alert(getString(R.string.no_more));
                    }
                })
                .create();
        dialog2.show();
        //Objects.requireNonNull(dialog2.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void switchType(MenuItem item) {
        PopupMenu pMenu = new PopupMenu(this, findViewById(R.id.gif_tpl_hidden_top), Gravity.END);
        Menu menu = Util.enableMenuIcon(pMenu.getMenu());
        String[] typeList = getTypeList();
        for (int i = 0; i < typeList.length; i++) {
            String t = typeList[i];
            if (t.equals(type)) {
                t += "          √";
            }
            menu.add(Menu.NONE, i, Menu.NONE, t).setIcon(getCachedIcon("default"));
        }
        pMenu.setOnMenuItemClickListener(item1 -> {
            setType(typeList[item1.getItemId()]);
            getNewFlg = true;
            initPage();
            return true;
        });
        pMenu.show();
    }

    public void switchWeb(MenuItem item) {
        PopupMenu pMenu = new PopupMenu(this, findViewById(R.id.gif_tpl_hidden_top), Gravity.END);
        Menu menu = Util.enableMenuIcon(pMenu.getMenu());
        for (int i = 0; i < webList.length; i++) {
            String web = webList[i];
            MenuItem mItem = menu.add(Menu.NONE, i, Menu.NONE, web).setIcon(BitmapUtil.scaleDrawable(getCachedIcon(web), 100, 0));
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
                    String url = getSpy().getUrl(artId, 0);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    dialog.dismiss();
                })
                .setNeutralButton(R.string.current_page, (dialog, which) -> {
                    String url = getSpy().getUrl(artId, webPage);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void loading() {
        if (loading == null) {
            loading = new Dialog(this, android.R.style.Theme_Material_Dialog_Alert);
            GifImageView imgView = new GifImageView(this);
            imgView.setImageDrawable(getCachedIcon("loading"));
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

    private void loaded(String tip) {
        if (StringUtil.notBlank(tip)) {
            alert(tip);
        }
        loading.dismiss();
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
                            seek(0, true);
                            loaded(errString.isEmpty() ? getString(R.string.success) : getString(R.string.error_tip, errString));
                        });
                    });
                })
                .setNeutralButton(R.string.current_type, (dialog, which) -> {
                    mark();
                    dialog.dismiss();
                    loading();
                    threadPoolExecutor.execute(() -> {
                        String err = fetchAllWeb();
                        runOnUiThread(() -> {
                            seek(0, true);
                            loaded(err.isEmpty() ? getString(R.string.success) : getString(R.string.error_tip, err));
                        });
                    });
                })
                .setNegativeButton(R.string.get_all, (dialog, which) -> {
                    dialog.dismiss();
                    fetchAll();
                })
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

    /**
     * 显示书签列表
     * {@code 第63条：注意字符串连接的性能}
     */
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
                                item.put("icon", getCachedIcon(hm.get("web_name")));
                                item.put("name", String.format("%s，%s，%s，%s，%s", hm.get("title"), hm.get("web_name"), hm.get("type"), hm.get("art_id"), hm.get("page")));
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
                    PopupMenu pMenu = new PopupMenu(this, pageListPopupWindow.getItemView(position));
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
        int size = bookmark.size();
        if (size > 0) {
            HashMap<String, String> last = bookmark.getFirst();
            if (webName.equals(last.get("web_name")) && artId.equals(last.get("art_id"))) {
                bookmark.removeFirst();
            } else if (size > 7) {
                bookmark.removeLast();
            }
        }
        HashMap<String, String> mark = new HashMap<>(5);
        mark.put("type", type);
        mark.put("web_name", webName);
        mark.put("art_id", artId);
        mark.put("page", page + "");
        mark.put("title", title);
        bookmark.addFirst(mark);
        Type typeToken = new TypeToken<LinkedList<HashMap<String,String>>>(){}.getType();
        getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit()
                .putString(SettingActivity.BOOKMARK, new Gson().toJson(bookmark, typeToken))
                .apply();
        alert(getString(R.string.add_successfully));
    }

    /**
     * {@code 第49条：检查参数有效性}
     * @param pos int
     * @param del boolean
     */
    private void seek(int pos, boolean del) {
        LinkedList<HashMap<String,String>> bookmark = getBookmark();
        if (pos < bookmark.size() && pos >= 0) {
            try {
                HashMap<String, String> mark = bookmark.get(pos);
                locate(mark.get("type"), mark.get("web_name"), mark.get("art_id"), Integer.parseInt(mark.get("page")));
                if (del) {
                    bookmark.remove(pos);
                    Type typeToken = new TypeToken<LinkedList<HashMap<String,String>>>(){}.getType();
                    getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit()
                            .putString(SettingActivity.BOOKMARK, new Gson().toJson(bookmark, typeToken))
                            .apply();
                }
            } catch (Exception e) {
                alert(e.getMessage());
            }

        } else {
            alert(getString(R.string.not_existed_bookmark));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void removeMark(int index) {
        LinkedList<HashMap<String,String>> bookmark = getBookmark();
        if (index < bookmark.size() && index >= 0) {
            bookmark.remove(index);
            Type typeToken = new TypeToken<LinkedList<HashMap<String,String>>>(){}.getType();
            getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit()
                    .putString(SettingActivity.BOOKMARK, new Gson().toJson(bookmark, typeToken))
                    .apply();
        } else {
            alert(getString(R.string.not_existed_bookmark));
        }
    }

    class History {
        private static final int PAGE_SIZE = 10;
        /**
         * {@code 第43条：方法引用优于lambda表达式}
         */
        @SuppressLint({"SetTextI18n"})
        void show() {
            PopupWindow pageWindow = new PageListPopupWindow.Builder(GifActivity.this)
                    .setListCountGetter(this::getCount)
                    .setListGetter(this::getList)
                    .setItemClickListener((pageWin, position) -> {
                        setWebName((String) pageWin.getItemData(position).get("web_name"));
                        artId = (String) pageWin.getItemData(position).get("art_id");
                        initPage();
                        MyLog.w("historyClick", webName);
                        pageWin.dismiss();
                    })
                    .setItemLongClickListener(this::longClickCallback)
                    .setPageSize(PAGE_SIZE)
                    .setWindowHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    .build();
            pageWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
        }

        private int getCount(String andWhere) {
            String where = "art_id <> '' and type = '" + type + "'";
            if (StringUtil.notBlank(andWhere)) {
                where += " and " + andWhere;
            }
            Cursor cus = db.rawQuery("select count(*) from " + DbHelper.TB_IMG_WEB + " where " + where, null);
            cus.moveToFirst();
            int count = cus.getInt(0);
            cus.close();
            return count;
        }

        private void deleteAlert(@NonNull Map<String, Object> item, @NonNull Runnable callback) {
            String delArtId = item.get("art_id").toString();
            String delWebName = (String) item.get("web_name");
            //创建对话框
            AlertDialog dialog2 = new AlertDialog.Builder(GifActivity.this)
                    // 设置图标
                    .setIcon(R.drawable.ic_info_black_24dp)
                    // 设置标题
                    .setTitle(R.string.delete)
                    .setPositiveButton(R.string.confirm, (dialog1, which) -> {
                        delete(delArtId, delWebName);
                        callback.run();
                    }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                    })
                    .create();
            dialog2.show();
        }

        /**
         * 删除历史记录
         * @param delArtId artId
         * @param delWebName webName
         */
        private void delete(String delArtId, String delWebName) {
            MyLog.w("delete", "art_id：" + delArtId + "，web_name：" + delWebName);
            db.delete(DbHelper.TB_IMG_WEB, "art_id = ? and web_name = ?", new String[]{delArtId, delWebName});
            db.delete(DbHelper.TB_IMG_WEB_ITEM, "art_id = ? and web_name = ?", new String[]{delArtId, delWebName});
        }

        /**
         * 填充历史列表
         * {@code 第57条：最小化局部变量的作用域}
         * 优先选择for循环而不是while循环，最小化了局部变量的作用域 ：listSize
         * {@code 第64条：通过接口引用对象}
         * @param page int
         * @param historyList List
         * @param andWhere String
         * @return List
         */
        private List<Map<String, Object>> getList(int page, List<Map<String, Object>> historyList, String andWhere) {
            int offset = (page - 1) * PAGE_SIZE;
            String where = "art_id <> '' and type = '" + type + "'";
            if (andWhere != null) {
                where += " and " + andWhere;
            }
            String sql = "select * from " + DbHelper.TB_IMG_WEB + " where " + where +  " order by id desc limit " + PAGE_SIZE + " offset " + offset;
            Cursor cus = db.rawQuery(sql, null);
            int count = cus.getCount();
            MyLog.w("getList：", count + "");
            cus.moveToFirst();
            for (int i = 0, listSize = historyList.size(); i < PAGE_SIZE; i++) {
                Map<String, Object> item;
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
                    item.put("icon", getCachedIcon(web));
                    cus.moveToNext();
                } else {
                    item.clear();
                }
            }
            cus.close();
            return historyList;
        }
        private boolean longClickCallback(@NonNull PageListPopupWindow pageListPopupWindow, int position) {
            // 使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
            PopupMenu pMenu = new PopupMenu(GifActivity.this, pageListPopupWindow.getItemView(position));
            pMenu.getMenuInflater().inflate(R.menu.gif_history_popupmenu, pMenu.getMenu());
            pMenu.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.delete_history:
                        // 删除记录
                        deleteAlert(pageListPopupWindow.getItemData(position), () -> pageListPopupWindow.remove(position));
                        break;
                    case R.id.copy_history_artid:
                        String articleId = pageListPopupWindow.getItemData(position).get("art_id").toString();
                        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        assert clm != null;
                        clm.setPrimaryClip(ClipData.newPlainText(null, articleId));
                        alert(getString(R.string.clip_toast) + "：" + articleId);
                        break;
                    case R.id.reload_art:
                        artId = (String) pageListPopupWindow.getItemData(position).get("art_id");
                        setWebName((String) pageListPopupWindow.getItemData(position).get("web_name"));
                        delete(artId, webName);
                        initPage();
                        pageListPopupWindow.dismiss();
                        break;
                    case R.id.filter_where:
                        filterInputDialog(pageListPopupWindow.getWhere(), pageListPopupWindow::filter);
                        break;
                    default:
                        break;
                }
                return false;
            });
            pMenu.show();
            return true;
        }
    }

    /**
     * {@code 第43条：方法引用优于lambda表达式}
     * @param item MenuItem
     */
    @SuppressLint({"SetTextI18n"})
    public void history(MenuItem item) {
        History history = new History();
        history.show();
    }

    @SuppressLint({"SetTextI18n"})
    public void favorites(MenuItem item) {
        Favorite favorite = new Favorite();
        favorite.show();
    }

    /**
     * 收藏列表
     */
    class Favorite {
        private static final int PAGE_SIZE = 15;
        /**
         * 查重时暂存重复fav_id
         */
        private int[] duplicateIds;

        @SuppressLint({"SetTextI18n"})
        void show() {
            PopupWindow pageWindow = new PageListPopupWindow.Builder(GifActivity.this)
                    .setListCountGetter(this::getCount)
                    .setListGetter(this::getList)
                    .setItemClickListener(this::viewFav)
                    .setItemLongClickListener((pageWin, position) -> {
                        // 使用rv.getChildAt只能获取可见的item，0表示当前屏幕可见第一个item
                        PopupMenu pMenu = new PopupMenu(GifActivity.this, pageWin.getItemView(position));
                        pMenu.getMenuInflater().inflate(R.menu.gif_favorite_popupmenu, pMenu.getMenu());
                        pMenu.setOnMenuItemClickListener(item1 -> {
                            switch (item1.getItemId()) {
                                case R.id.share_fav:
                                    // 分享
                                    File favFile = new File((String) pageWin.getItemData(position).get("path"));
                                    Util.share(favFile, GifActivity.this);
                                    break;
                                case R.id.track_source:
                                    // 溯源
                                    trackSourceDialog(pageWin.getItemData(position), pageWin::dismiss);
                                    break;
                                case R.id.delete_fav:
                                    //删除记录
                                    deleteAlert(pageWin.getItemData(position), () -> pageWin.remove(position));
                                    break;
                                case R.id.filter_where:
                                    filterInputDialog(pageWin.getWhere(), pageWin::filter);
                                    break;
                                case R.id.image_meta:
                                    showImageMeta(pageWin.getItemData(position));
                                    break;
                                case R.id.find_repeated_files:
                                    findRepeatedFilesDialog(pageWin::filter);
                                    break;
                                case R.id.transfer:
                                    File favFile2 = new File((String) pageWin.getItemData(position).get("path"));
                                    try (FileInputStream fis = new FileInputStream(favFile2)) {
                                        showBitmapPopup(pageWin.getItemView(position), BitmapFactory.decodeStream(fis));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        });
                        pMenu.show();
                        return true;
                    }).setPageSize(PAGE_SIZE)
                    .setWindowHeight(ViewGroup.LayoutParams.MATCH_PARENT)
                    .build();
            pageWindow.showAtLocation(findViewById(R.id.gif_activity_fab), Gravity.BOTTOM, 0, 0);
        }

        private void showImageMeta(Map<String, Object> item) {
            String path = item.get("path").toString();
            if (StringUtil.notBlank(path)) {
                long[] meta = BitmapUtil.calcImgMeta2(BitmapUtil.getBitmap(new File(path), 16, 16));
                //创建对话框
                AlertDialog dialog2 = new AlertDialog.Builder(GifActivity.this)
                        // 设置图标
                        .setIcon(R.drawable.ic_info_black_24dp)
                        // 设置标题
                        .setTitle(R.string.image_meta)
                        .setMessage(Arrays.toString(meta))
                        .setPositiveButton(R.string.copy, (dialog, which) -> {
                            ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            assert clm != null;
                            clm.setPrimaryClip(ClipData.newPlainText(null, Arrays.toString(meta)));
                            alert(getString(R.string.clip_toast));
                        }).setNegativeButton(R.string.cancel, (dialog, which) -> {
                        })
                        .create();
                dialog2.show();
            } else {
                alert(getString(R.string.not_found));
            }
        }

        private void findRepeatedFilesDialog(Consumer<String> filter) {
            View view = LayoutInflater.from(GifActivity.this).inflate(R.layout.gif_find_repeated_dialog, findViewById(R.id.page_list_popup_window), false);
            SharedPreferences sharedPreferences = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            int duplicateLevel = sharedPreferences.getInt(SettingActivity.DUPLICATE_LEVEL, 5);
            SeekBar seekBar = view.findViewById(R.id.duplicate_level_seek_bar);
            TextView textView = view.findViewById(R.id.duplicate_level_seek_bar_tip);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textView.setText(String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBar.setProgress(duplicateLevel);
            //创建对话框
            AlertDialog dialog2 = new AlertDialog.Builder(GifActivity.this)
                    // 设置图标
                    .setIcon(R.drawable.ic_info_black_24dp)
                    // 设置标题
                    .setTitle(R.string.duplicate_level)
                    .setView(view)
                    .setPositiveButton(R.string.cmp_256_bit, (dialog, which) -> {
                        loading();
                        threadPoolExecutor.execute(() -> {
                            try {
                                duplicateIds = BitmapUtil.getDuplicateIds(type, seekBar.getProgress());
                                runOnUiThread(() -> {
                                    // $ 表示特殊解析模式，使用 in 查询，排序输出
                                    filter.accept("$dupl");
                                    loading.dismiss();
                                });
                            } catch (Exception e) {
                                runOnUiThread(() -> loaded(e.getLocalizedMessage()));
                                e.printStackTrace();
                            }
                        });
                    }).setNegativeButton(R.string.cmp_64_bit, (dialog, which) -> {
                        loading();
                        threadPoolExecutor.execute(() -> {
                            try {
                                duplicateIds = getRepeatedItems(seekBar.getProgress());
                                MyLog.w("getRepeatedItems", Arrays.toString(duplicateIds));
                                runOnUiThread(() -> {
                                    // $ 表示特殊解析模式，使用 in 查询，排序输出
                                    filter.accept("$dupl");
                                    loading.dismiss();
                                });
                            } catch (Exception e) {
                                runOnUiThread(() -> loaded(e.getLocalizedMessage()));
                                e.printStackTrace();
                            }
                        });
                    })
                    .create();
            dialog2.show();
        }

        private void trackSourceDialog(@NonNull Map<String, Object> item, @NonNull Runnable callback) {
            String itemId = item.get("item_id").toString();
            Cursor cursor = db.query(DbHelper.TB_IMG_WEB_ITEM, new String[]{"web_name", "art_id"}, "id = ?", new String[]{itemId}, null, null, null);
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                String web = cursor.getString(0);
                String art = cursor.getString(1);
                // 查询计算页码
                Cursor cursor2 = db.query(DbHelper.TB_IMG_WEB_ITEM, new String[]{"count(*)"}, "id < ? and art_id = ? and web_name = ?", new String[]{itemId, art, web}, null, null, null);
                cursor2.moveToFirst();
                int pageAt = cursor2.getInt(0) / 3 + 1;
                cursor2.close();
                // 查询文章标题
                Cursor cursor3 = db.query(DbHelper.TB_IMG_WEB, new String[]{"type", "title"}, "web_name = ? and art_id = ?", new String[]{web, art}, null, null, null);
                if (cursor3.getCount() == 0) {
                    alert(getString(R.string.not_found));
                    return;
                }
                cursor3.moveToFirst();
                String itemType = cursor3.getString(0);
                String head = cursor3.getString(1);
                cursor3.close();
                // 创建对话框
                AlertDialog dialog2 = new AlertDialog.Builder(GifActivity.this)
                        // 设置图标
                        .setIcon(R.drawable.ic_info_black_24dp)
                        // 设置标题
                        .setTitle(R.string.track_source)
                        .setMessage(web + "," + itemType + "," + head + "," + pageAt)
                        .setPositiveButton(R.string.current_page, (dialog, which) -> {
                            locate(itemType, web, art, pageAt);
                            dialog.dismiss();
                            callback.run();
                        }).setNegativeButton(R.string.cancel, (dialog, which) -> {
                        }).setNeutralButton(R.string.first_page, (dialog, which) -> {
                            locate(itemType, web, art, 1);
                            dialog.dismiss();
                            callback.run();
                        })
                        .create();
                dialog2.show();
            } else {
                alert(getString(R.string.not_found));
            }
            cursor.close();
        }

        /**
         * 查询重复的图片的favId
         * {@code 第9条：优先使用try-with-resources而不是try-finally}
         * @return String
         */
        @NonNull
        private int[] getRepeatedItems(int level) {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type);
            File[] fileList = dir.listFiles(file -> file.isFile() && file.length() > 1024);
            int count = fileList.length;
            Properties props = new Properties();
            File propFile = new File(dir, "meta");
            try (FileReader fr = new FileReader(propFile)) {
                props.load(fr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            IntList ids = new IntList((count >> 3) * level);
            BitmapUtil.setDuplicateLevel(level);
            long[] metas = new long[count];
            for (int i = 0; i < count; i++) {
                metas[i] = BitmapUtil.getCachedImgMeta(fileList[i], props);
                /* MyLog.w("getRepeatedItems", fileList[i].getName() + "------" + Long.toBinaryString(metas[i])); */
            }
            for (int i = 0; i < count; i++) {
                if (metas[i] == 0 || metas[i] == -1) {
                    continue;
                }
                boolean flg = false;
                for (int j = i + 1; j < count; j++) {
                    if (metas[j] == 0 || metas[j] == -1) {
                        continue;
                    }
                    if (BitmapUtil.cmpImgMeta(metas[i], metas[j])) {
                        flg = true;
                        /* MyLog.w("getRepeatedItems", fileList[j].getName() + "------" + Long.toBinaryString(metas[j])); */
                        metas[j] = 0;
                        String fname = fileList[j].getName();
                        int id = Util.getFavId(fname);
                        if (id > 0) {
                            ids.add(id);
                        }
                    }
                }
                if (flg) {
                    /* MyLog.w("getRepeatedItems", fileList[i].getName() + "------" + Long.toBinaryString(metas[i])); */
                    metas[i] = 0;
                    String fname = fileList[i].getName();
                    int id = Util.getFavId(fname);
                    /* MyLog.w("getRepeatedItems", "___________________"); */
                    if (id > 0) {
                        ids.add(id);
                    }
                }
            }
            MyLog.w("getRepeatedItems:ret", ids.toString());
            try (FileWriter fw = new FileWriter(propFile)) {
                props.store(fw, "img meta");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ids.toArray();
        }

        private int getCount(String andWhere) {
            String where = "type = '" + type + "'";
            if (StringUtil.notBlank(andWhere)) {
                // 查重模式，需排序结果
                if ("$dupl".equals(andWhere)) {
                    andWhere = "id in (" + DbHelper.buildInCondition(duplicateIds) + ")";
                }
                where += " and " + andWhere;
            }
            Cursor cus = db.rawQuery("select count(*) from " + DbHelper.TB_IMG_FAVORITE + " where " + where, null);
            cus.moveToFirst();
            int count = cus.getInt(0);
            cus.close();
            MyLog.w("getCount", count + "");
            return count;
        }

        @SuppressLint("ClickableViewAccessibility")
        private void viewFav(@NonNull PageListPopupWindow listPopupWindow, int position) {
            final Dialog dialog = new Dialog(GifActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            GifImageView imgView = new GifImageView(GifActivity.this);
            List<Map<String, Object>> favoriteList = listPopupWindow.getList();
            imgView.setImageDrawable(getCachedIcon("default"));
            imgView.setOnLongClickListener(v -> {
                Map<String, Object> item = favoriteList.get(position);
                String id = (String) item.get("id");
                String name = id + "_" + item.get("title");
                String cacheKey = getFavCacheKey(item.get("id").toString());
                // 混合图网页（3dm）
                download(item.get("type") + "/" + name, cacheKey);
                return false;
            });
            imgView.setOnTouchListener(new View.OnTouchListener() {
                private int favImgPos = position;
                private int vScrollX;
                private float mPosX, mPosY, mCurPosX, mCurPosY;
                private final Runnable runnable = imgView::performLongClick;
                private final Handler handler = new Handler();

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            vScrollX = v.getScrollX();
                            mPosX = mCurPosX = event.getX();
                            mPosY = mCurPosY = event.getY();
                            handler.postDelayed(runnable, 500);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (mCurPosX == mPosX) {
                                handler.removeCallbacks(runnable);
                            }
                            mCurPosX = event.getX();
                            v.setScrollX((int) (mPosX - mCurPosX + vScrollX));
                            break;
                        case MotionEvent.ACTION_UP:
                            mCurPosY = event.getY();
                            v.setScrollX(vScrollX);
                            handler.removeCallbacks(runnable);
//                        if (mCurPosY - mPosY > 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {//向下滑動
//                        } else if (mCurPosY - mPosY < 0
//                                && (Math.abs(mCurPosY - mPosY) > 25)) {//向上滑动
//                        }
                            // 向左滑動
                            int hInterval = 200, vInterval = 250;
                            if (mCurPosX - mPosX > hInterval) {
                                viewPre();
                                break;
                                // 向右滑动
                            } else if (mCurPosX - mPosX < -hInterval) {
                                viewNext();
                                break;
                            }
                            if (mCurPosY - mPosY > vInterval) {
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
                        if (listPopupWindow.hasPrePage()) {
                            listPopupWindow.prePage();
                            favImgPos = PAGE_SIZE - 1;
                        } else {
                            GifActivity.this.alert(getString(R.string.no_more));
                            return;
                        }
                    }
                    loadFavImg(dialog, imgView, favoriteList.get(favImgPos));
                }

                private void viewNext() {
                    if (favImgPos < favoriteList.size() - 1) {
                        if (favoriteList.get(favImgPos + 1).isEmpty()) {
                            GifActivity.this.alert(getString(R.string.no_more));
                            return;
                        }
                        ++favImgPos;
                    } else {
                        if (listPopupWindow.hasNextPage()) {
                            favImgPos = 0;
                            listPopupWindow.nextPage();
                        } else {
                            GifActivity.this.alert(getString(R.string.no_more));
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

        private void loadFavImg(@NonNull Dialog dialog, GifImageView imgView, @NonNull Map<String, Object> item) {
            String title = (String) item.get("title");
            if (!StringUtil.notBlank(title)) {
                // 删除item后翻页问题修复
                alert(getString(R.string.no_data));
                return;
            }
            String url = (String) item.get("real_url");
            url = StringUtil.notBlank(url) ? url : (String) item.get("url");
            dialog.setTitle(Html.fromHtml("<p style='color: #F66725; text-align: center'>" + title + "</p>", Html.FROM_HTML_MODE_COMPACT));
            ImgViewTask.newInstance(imgView, GifActivity.this)
                    .executeOnExecutor(threadPoolExecutor, title, url, (String) item.get("id"), (String) item.get("path"));
        }

        private void deleteAlert(@NonNull Map<String, Object> item, @NonNull Runnable callback) {
            String id = (String) item.get("id");
            String itemId = (String) item.get("item_id");
            //创建对话框
            AlertDialog dialog = new AlertDialog.Builder(GifActivity.this)
                    // 设置图标
                    .setIcon(R.drawable.ic_info_black_24dp)
                    // 设置标题
                    .setTitle(R.string.delete)
                    .setPositiveButton(R.string.delete, (dialog1, which) -> {
                        delete(id, itemId);
                        callback.run();
                    }).setNegativeButton(R.string.delete_file, (dialog1, which) -> {
                        File favFile = new File((String) item.get("path"));
                        if (favFile.exists() && favFile.delete()) {
                            alert(getString(R.string.delete_success));
                        } else {
                            alert(getString(R.string.file_is_lost));
                        }
                    }).setNeutralButton(R.string.delete_fav_and_file, (dialogInterface, i) -> {
                        File favFile = new File((String) item.get("path"));
                        if (favFile.exists() && favFile.delete()) {
                            alert(getString(R.string.delete_success));
                        } else {
                            alert(getString(R.string.file_is_lost));
                        }
                        delete(id, itemId);
                        callback.run();
                    })
                    .create();
            dialog.show();
        }

        /**
         * 删除收藏
         * @param delId fav_id
         * @param itemId art_id
         */
        private void delete(String delId, String itemId) {
            ContentValues ctv = new ContentValues(1);
            ctv.put("fav_flg", 0);
            int rows = db.update(DbHelper.TB_IMG_WEB_ITEM, ctv, "id = ?", new String[]{itemId});
            MyLog.w("db_item_remove_fav: ", rows + "");
            db.delete(DbHelper.TB_IMG_FAVORITE, "id = ?", new String[]{delId});
            MyLog.w("delete", "id：" + delId);
        }

        /**
         * 根据offset切出当前需要的部分id
         * @param offset offset
         * @return String[]
         */
        @Nullable
        private int[] cutOffIds(int offset) {
            if (duplicateIds.length > offset) {
                int limit = duplicateIds.length >= offset + PAGE_SIZE ? PAGE_SIZE + offset : duplicateIds.length;
                return Arrays.copyOfRange(duplicateIds, offset, limit);
            } else {
                return null;
            }
        }

        private List<Map<String, Object>> getList(int page, List<Map<String, Object>> favoriteList, String andWhere) {
            int offset = (page - 1) * PAGE_SIZE;
            String where = "type = '" + type + "'";
            int[] ids = null;
            if (andWhere != null) {
                // 查重模式，需排序结果
                if ("$dupl".equals(andWhere)) {
                    ids = cutOffIds(offset);
                    andWhere = "id in (" + DbHelper.buildInCondition(ids) + ")";
                    offset = 0;
                }
                where += " and " + andWhere;
            }
            String sql = "select * from " + DbHelper.TB_IMG_FAVORITE + " where " + where + " order by id desc limit " + PAGE_SIZE + " offset " + offset;
            Cursor cus = db.rawQuery(sql, null);
            int count = cus.getCount();
            MyLog.w("getList：", count + "");
            cus.moveToFirst();
            String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type + "/";
            for (int i = 0, listSize = favoriteList.size(); i < PAGE_SIZE; i++) {
                Map<String, Object> item;
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
                        file = diskLruCache.get(getFavCacheKey(item.get("id").toString()));
                        if (file == null) {
                            item.put("icon", getCachedIcon("default"));
                            continue;
                        }
                        // KB
                        long size = file.length() >> 10;
                        item.put("name", title + "（" + size + "K）");
                    }
                    try {
                        item.put("icon", new BitmapDrawable(getResources(), BitmapUtil.getBitmap(file, 50, 50)));
                        /* MyLog.w("getList-oompress", "fileSize：" + file.length() >> 10 + " kB；compress：" + newOpts.inSampleSize); */
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    item.clear();
                }
            }
            cus.close();
            // 排序结果
            if (ids != null) {
                for (int i = 0, index = 0; i < ids.length; i++) {
                    for (int j = index; j < count; j++) {
                        if (ids[i] == Integer.valueOf((favoriteList.get(j).get("id").toString()))) {
                            Map<String, Object> tmp = favoriteList.set(index++, favoriteList.get(j));
                            favoriteList.set(j, tmp);
                            break;
                        }
                    }
                }
            }
            return favoriteList;
        }
    }

    private void filterInputDialog(String conditions, @NonNull Consumer<String> consumer) {
        //实例化布局
        View view2 = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, findViewById(R.id.page_list_popup_window), false);
        EditText where = view2.findViewById(R.id.dialog_input);
        where.setHint(R.string.filter_where);
        where.setText(conditions);
        //创建对话框
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.filter_where)
                // 添加布局
                .setView(view2)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> consumer.accept(where.getText().toString()))
                .setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog2.show();
        Objects.requireNonNull(dialog2.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * 下载图片到download文件夹
     * {@code 第8条：避免使用终结方法和清理方法}
     * {@code 第9条：优先使用try-with-resources而不是try-finally}
     * @param pathname dir/name
     * @param cacheKey key
     */
    private void download(String pathname, String cacheKey) {
        PermissionUtil.req(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.RequestCode.STORAGE_WRITE, (result) -> {
            Toast.makeText(this, R.string.downloading, Toast.LENGTH_SHORT).show();
            File img = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + pathname);
            MyLog.w("download", "path:" + img.getAbsolutePath());
            try {
                if (!img.getParentFile().exists() && !img.getParentFile().mkdirs()) {
                    MyLog.w("create dir error", img.getParentFile().getAbsolutePath());
                    throw new Exception("create dir error");
                }
                File cacheImg = diskLruCache.get(cacheKey);
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
                Snackbar.make(findViewById(android.R.id.content), R.string.download_success, Snackbar.LENGTH_SHORT)
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

    /**
     * {@code 第24条：优先考虑静态成员类}
     * {@code 第53条：明智而审慎地使用可变参数}
     * 假设你已确定95％的调用是三个或更少的参数的方法，那么声明该方法的五个重载。每个重载方法包含0到3个普通参数，当参数数量超过3个时，使用一个可变参数方法:
     * {@link java.util.EnumSet#of} 静态工厂使用这种技术将创建枚举集合的成本降到最低。
     * 这是适当的，因为枚举集合为比特属性提供具有性能竞争力的替换（performance-competitive replacement for bit fields）是至关重要的(条目 36)。
     *
     * 当需要使用可变数量的参数定义方法时，可变参数非常有用。 在使用可变参数前加上任何必需的参数，并注意使用可变参数的性能后果。
     */
    static class ImgViewTask extends AsyncTask<String, Integer, Drawable> {

        private final WeakReference<GifActivity> gifActivityWR;
        private final WeakReference<GifImageView> imgViewWR;
        private CircleProgressDrawable circleProgress;
        static ImgViewTask instance;

        static ImgViewTask newInstance(GifImageView imgView, GifActivity gifActivity) {
            if (instance != null) {
                instance.cancel(true);
            }
            instance = new ImgViewTask(imgView, gifActivity);
            return instance;
        }

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
            String imgKey = activity.getFavCacheKey(strings[2]);
            String path = strings[3];
            File cache = StringUtil.notBlank(path) ? new File(path) : activity.diskLruCache.get(imgKey);
            if (cache != null) {
                try {
                    MyLog.w("loadImg(fromCache)", "title：" + strings[0] + ";url：" + strings[1]);
                    FileInputStream fis = null;
                    Drawable imgDrawable = TYPE_GIF.equals(type) ? new GifDrawable(cache) : Drawable.createFromStream(fis = new FileInputStream(cache), null);
                    if (fis != null) {
                        fis.close();
                    }
                    return imgDrawable;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //移动网络禁止 gif
                if (activity.isGprs && TYPE_GIF.equals(type)) {
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
                    int contentLength = (int) responseBody.contentLength();
                    byte[] bytes;
                    if (contentLength > 81920) {
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
                    Drawable imgDrawable = TYPE_GIF.equals(type) ? new GifDrawable(bytes) : Drawable.createFromStream(new ByteArrayInputStream(bytes), null);
                    if (imgDrawable == null) {
                        throw new Exception("get image err");
                    }
                    RandomAccessFile raf = new RandomAccessFile(cache, "rwd");
                    raf.write(bytes);
                    raf.close();
                    gifActivityWR.get().diskLruCache.put(imgKey, imgKey, cache.length());
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
            circleProgress = new CircleProgressDrawable.Builder().build();
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
                float zoom = TYPE_GIF.equals(type) ? 3f : 5.5f;
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

    /**
     * 获取Lru缓存的key
     * @param offset imgList的index
     * @return String
     */
    private String getCacheKey(int offset) {
        return webName + "_" + ART_FILTER_PATTERN.matcher(artId).replaceAll("#") + "-" + offset;
    }

    /**
     * 返回收藏列表的lru缓存key
     * @param id fav表id
     * @return key
     */
    @NonNull
    private String getFavCacheKey(String id) {
        return "favorite_" + type + "-" + id;
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

    private void updateExt(@NonNull HashMap<String, String> imgInfo) {
        ContentValues ctv = new ContentValues(2);
        String ext = imgInfo.get("ext");
        String name = imgInfo.get("title");
        name = name.substring(0, name.lastIndexOf(".")) + ext;
        imgInfo.put("title", name);
        ctv.put("ext", ext);
        ctv.put("title", name);
        // 动位混合处理（3dm）
        ctv.put("type", ".gif".equals(ext) ? TYPE_GIF : TYPE_BITMAP);
        db.update(DbHelper.TB_IMG_WEB_ITEM, ctv, "art_id = ? and url = ?", new String[]{artId, imgInfo.get("url")});
        MyLog.w("db_item_update_ext: ", name + "；" + ext + "；" + imgInfo.get("url"));
    }

    private void insertDbWeb(String title, String url) {
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

    private void insertDbImgItem(HashMap<String, String> data) {
        ContentValues ctv = new ContentValues(9);
        ctv.put("art_id", artId);
        ctv.put("page", webPage);
        ctv.put("web_name", webName);
        // 动位混合处理（3dm）
        ctv.put("type", ".gif".equals(data.get("ext")) ? TYPE_GIF : TYPE_BITMAP);
        ctv.put("title", data.get("title"));
        ctv.put("url", data.get("url"));
        ctv.put("ext", data.get("ext"));
        ctv.put("real_url", data.get("real_url"));
        long newId = db.insert(DbHelper.TB_IMG_WEB_ITEM, null, ctv);
        MyLog.w("db_web_item_insert: ", newId + "");
    }

    /**
     * 获取当前类型下一篇文章id
     * @return String
     */
    private String getNextArtId() {
        Cursor cus = db.query(DbHelper.TB_IMG_WEB, new String[]{"id"}, "art_id = ? and web_name = ?", new String[]{artId, webName}, null, null, "id desc", "1");
        if (cus.getCount() > 0) {
            cus.moveToFirst();
            String id = cus.getString(0);
            Cursor cus2 = db.query(DbHelper.TB_IMG_WEB, new String[]{"art_id"}, "id > ? and web_name = ? and type = ?", new String[]{id, webName, type}, null, null, "id asc", "1");
            if (cus2.getCount() > 0) {
                cus2.moveToFirst();
                return cus2.getString(0);
            }
            cus2.close();
        }
        cus.close();
        return null;
    }

    /**
     * 获取当前类型上一篇文章id
     * @return String
     */
    private String getPrevArtId() {
        Cursor cus = db.query(DbHelper.TB_IMG_WEB, new String[]{"id"}, "art_id = ? and web_name = ?", new String[]{artId, webName}, null, null, "id desc", "1");
        if (cus.getCount() > 0) {
            cus.moveToFirst();
            String id = cus.getString(0);
            Cursor cus2 = db.query(DbHelper.TB_IMG_WEB, new String[]{"art_id"}, "id < ? and web_name = ? and type = ?", new String[]{id, webName, type}, null, null, "id desc", "1");
            if (cus2.getCount() > 0) {
                cus2.moveToFirst();
                return cus2.getString(0);
            }
            cus2.close();
        }
        cus.close();
        return null;
    }

    /**
     * A placeholder fragment containing a simple view.
     * {@code 第24条：优先考虑静态成员类}
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private int page, imgPosition = 1, focusedPosition = 1;
        private static final int[] gifIds = new int[]{R.id.gif_1, R.id.gif_2, R.id.gif_3};
        private static final int MSG_TYPE_PRE = 100;
        private static final int MSG_TYPE_LOAD = 101;
        private static final int MSG_TYPE_EMPTY = 102;
        private static final int MSG_TYPE_PRELOAD = 103;
        private static final int MSG_TYPE_LOADING = 104;
        private GifActivity activity;
        private MyHandler handler;

        public PlaceholderFragment() {
        }

        @Override
        public void onDestroy() {
            activity = null;
            handler = null;
            super.onDestroy();
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        static PlaceholderFragment newInstance(int page, GifActivity activity) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.page = page;
            fragment.activity = activity;
            fragment.handler = new MyHandler(fragment);
            return fragment;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_get_gif, container, false);
            // 下拉刷新滑动冲突解决
            SwipeRefreshLayout refreshLayout = activity.findViewById(R.id.gif_swipe_layout);
            rootView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (oldScrollY == 0 && scrollY > 0) {
                    refreshLayout.setEnabled(false);
                }
                if (scrollY == 0) {
                    refreshLayout.setEnabled(true);
                }
            });
            ((TextView) rootView.findViewById(R.id.section_label)).setText(title + "：" + page);
            for (int i = 0; i < 3; i++) {
                final int pos = i + 1;
                rootView.findViewById(gifIds[i]).setOnLongClickListener(v -> longClickCb(pos, rootView));
            }
            return rootView;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            activity.okHttpClient.dispatcher().cancelAll();
            if (isVisibleToUser) {
                // 滑动冲突解决
                View view = getView();
                if (view != null) {
                    SwipeRefreshLayout refreshLayout = activity.findViewById(R.id.gif_swipe_layout);
                    refreshLayout.setEnabled(view.getScrollY() == 0);
                }
                checkLoad(1);
            }

        }

        void send(int what, int arg1, int arg2, Object obj) {
            if (activity != null && activity.page == this.page) {
                handler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
            }
        }

        /**
         * 获取imgList{@link GifActivity#imgList}中的index
         * @param index fragment中的index
         * @return int
         */
        private int getImgOffset(int index) {
            return (page - 1) * 3 + index - 1;
        }

        /**
         * 获取lru缓存的key
         * @param index fragment中的index
         * @return String
         */
        private String getCacheKey(int index) {
            return activity.getCacheKey(getImgOffset(index));
        }

        private boolean longClickCb(int position, View view) {
            focusedPosition = position;
            ListPopupWindow listMenu = new ListPopupWindow(activity);
            listMenu.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, new
                    String[]{getString(R.string.add_bookmark), getString(R.string.share), getString(R.string.add_to_fav), getString(R.string.download), getString(R.string.transfer)}));
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
                        File img = activity.diskLruCache.get(getCacheKey(focusedPosition));
                        Util.share(img, activity);
                        break;
                    case 2:
                        addFav();
                        break;
                    case 3:
                        long favId = addFav();
                        if (favId > 0) {
                            HashMap<String, String> gifInfo = activity.getImgInfo(getImgOffset(focusedPosition));
                            String cacheKey = getCacheKey(focusedPosition);
                            assert gifInfo != null;
                            // 混合图网页（3dm）
                            String dir = ".gif".equals(gifInfo.get("ext")) ? "gif" : "bitmap";
                            String path = dir + "/" + favId + "_" + gifInfo.get("title");
                            activity.download(path, cacheKey);
                        }
                        break;
                    case 4:
                        File img2 = activity.diskLruCache.get(getCacheKey(focusedPosition));
                        try (FileInputStream fis = new FileInputStream(img2)) {
                            activity.showBitmapPopup(view1, BitmapFactory.decodeStream(fis));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
                listMenu.dismiss();
            });
            listMenu.show();
            return true;
        }

        /**
         * 添加收藏
         * @return long
         */
        private long addFav() {
            int imgOffset = getImgOffset(focusedPosition);
            long favId = 0;
            if (activity.validateOffset(imgOffset)) {
                HashMap<String, String> imgInfo = activity.getImgInfo(imgOffset);
                assert imgInfo != null;
                Cursor cus = activity.db.query(DbHelper.TB_IMG_WEB_ITEM, new String[]{"*"}, "art_id = ? and title = ?", new String[]{artId, imgInfo.get("title")}, null, null, "id desc", "1");
                if (cus.getCount() > 0) {
                    cus.moveToFirst();
                    if (cus.getInt(cus.getColumnIndex("fav_flg")) != 1) {
                        ContentValues ctv = new ContentValues(10);
                        ctv.put("item_id", cus.getLong(cus.getColumnIndex("id")));
                        ctv.put("art_id", cus.getString(cus.getColumnIndex("art_id")));
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
                        // 3dm位图类型bitmap，web类型gif，特殊处理。
                        Cursor cus2 = activity.db.query(DbHelper.TB_IMG_FAVORITE, new String[]{"id"}, "type = ? and item_id = ?", new String[]{cus.getString(cus.getColumnIndex("type")), cus.getString(cus.getColumnIndex("id"))}, null, null, "id desc", "1");
                        cus2.moveToFirst();
                        favId = cus2.getLong(0);
                        cus2.close();
                        activity.alert(getString(R.string.already_in_fav));
                    }
                    cus.close();
                }
            } else {
                activity.alert(getString(R.string.file_is_lost));
            }
            return favId;
        }

        void checkLoad(int position) {
            String imgKey = getCacheKey(imgPosition = position);
            //移动网络禁止 gif
            MyLog.w("checkLoad", activity.isGprs + "  t: " + type + "  key: " + imgKey + activity.diskLruCache.isNotExists(imgKey));
            if (activity.isGprs && TYPE_GIF.equals(type) && activity.diskLruCache.isNotExists(imgKey)) {
                if (!activity.gprsContinue) {
                    View view = LayoutInflater.from(activity).inflate(R.layout.gif_gprs_popup_confirm, activity.findViewById(R.id.get_img_ll), false);
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
            final int nowPosition = imgPosition, startOffset = getImgOffset(nowPosition);
            MyLog.w("info", "loadImg:" + startOffset);
            HashMap<String, String> imgInfo = activity.getImgInfo(startOffset);
            if (imgInfo == null) {
                activity.runOnUiThread(() -> activity.alert(getString(R.string.no_more)));
                return;
            }
            String imgKey = activity.getCacheKey(startOffset);
            String gifExt = ".gif";
            File cacheImg = activity.diskLruCache.get(imgKey);
            send(MSG_TYPE_PRE, nowPosition, 0, imgInfo.get("title"));
            if (cacheImg != null) {
                try {
                    Drawable imgDrawable = gifExt.equals(imgInfo.get("ext")) ? new GifDrawable(cacheImg) : Drawable.createFromPath(cacheImg.getPath());
                    send(MSG_TYPE_LOAD, nowPosition, 0, imgDrawable);
                    MyLog.w("loadImg", "fromCache:" + imgInfo.get("title"));
                } catch (IOException e) {
                    send(MSG_TYPE_EMPTY, nowPosition, 0, null);
                    e.printStackTrace();
                }
            } else {
                String url = StringUtil.notBlank(imgInfo.get("real_url")) ? imgInfo.get("real_url") : imgInfo.get("url");
                Request request = new Request.Builder().url(url).build();
                activity.okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        send(MSG_TYPE_EMPTY, nowPosition, 0, null);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        File finalCacheFile = null;
                        try {
                            MyLog.w("loadImg", "fromNet:" + imgInfo.get("title") + ";url:" + url + ";index:" + request.toString());
                            ResponseBody responseBody = response.body();
                            assert responseBody != null;
                            int contentLength = (int) responseBody.contentLength();
                            byte[] bytes;
                            if (contentLength > 81920) {
                                send(MSG_TYPE_PRELOAD, nowPosition, contentLength, null);
                                BufferedSource source = responseBody.source();
                                bytes = new byte[contentLength];
                                int chunk = contentLength / 90, threshold = 0, offset = 0, read;
                                while ((read = source.read(bytes, offset, contentLength - offset)) != -1) {
                                    if ((offset += read) > threshold) {
                                        send(MSG_TYPE_LOADING, nowPosition, offset, null);
                                        threshold = offset + chunk;
                                    }
                                }
                                send(MSG_TYPE_LOADING, nowPosition, contentLength, null);
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
                            send(MSG_TYPE_LOAD, nowPosition, 0, imgDrawable);
                            finalCacheFile = new File(activity.getCacheDir(), imgKey + ext);
                            RandomAccessFile raf = new RandomAccessFile(finalCacheFile, "rwd");
                            raf.write(bytes);
                            raf.close();
                            activity.diskLruCache.put(imgKey, imgKey + ext, finalCacheFile.length());
                        } catch (Exception e) {
                            if (finalCacheFile != null && finalCacheFile.delete()) {
                                MyLog.d("cacheDel", "cacheImg deleted");
                            }
                            send(MSG_TYPE_EMPTY, nowPosition, 0, null);
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        void refresh() {
            /* CacheUtil.clearAllCache(activity); */
            activity.okHttpClient.dispatcher().cancelAll();
            checkLoad(1);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            MyLog.w("start", "getItem:" + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, GifActivity.this);
        }

        @Override
        public int getCount() {
            return 80;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentFragment = (PlaceholderFragment) object;
            page = position + 1;
            super.setPrimaryItem(container, position, object);
        }
    }

    /**
     * {@code 第24条：优先考虑静态成员类}
     * 如果你声明了一个不需要访问外围实例的成员类，那你总是应该static修饰符加到声明里去，使得这个成员类是静态的。
     * 如果你不加这个修饰符，那么每个实例都将包含一个隐藏的外围实例的引用。
     * 就如前面说的那样，存储这个引用将耗费时间和空间。
     * 更严重的是，当这个外围实例已经满足垃圾回收（条目7）的条件时，非静态成员类实例会导致外围实例被保留。
     * 因此而导致的内存泄露是灾难性的。由于这个引用是不可见的，所以这个问题也通常很难被检测到。
     *
     * {@code 第25条：将源文件限制为单个顶级类}
     * 如果你尝试将多个顶级类放入同一个源文件，可以考虑使用静态成员类（条目）作为将不同类拆分为单独源文件的替代办法。
     * 如果某些类是为其它类服务的，那么将这些类做成静态成员类通常是个更好的选择，因为它加强了可阅读性而且通过将它们声明为私有能减少类的可访问性
     *
     * 永远不要将多个顶级类或接口放到一个源文件里。遵守这条规则就能保证在编译时不会遇到一个类有多个定义的情况。
     * 这又保证了编译产生的class文件和随之产生的程序行为不会依赖于传给编译器的源文件顺序。
     */
    private static class MyHandler extends Handler {
        /**
         * {@code 第7条：消除过时的对象引用}
         */
        private final WeakReference<PlaceholderFragment> fragmentRef;

        MyHandler(PlaceholderFragment fragment) {
            fragmentRef = new WeakReference<>(fragment);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            PlaceholderFragment fragment = fragmentRef.get();
            if (fragment == null || fragment.activity == null || fragment.getView() == null) {
                return;
            }
            GifActivity activity = fragment.activity;
            View rootView = fragment.getView();
            int position = msg.arg1;
            GifImageView imageView = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + position, "id", activity.getPackageName()));
            switch (msg.what) {
                case PlaceholderFragment.MSG_TYPE_PRE:
                    if (position == 1) {
                        TextView textView = rootView.findViewById(R.id.section_label);
                        textView.setText(title + "：" + fragment.page);
                    }
                    TextView tv = rootView.findViewById(fragment.getResources().getIdentifier("gtxt_" + position, "id", activity.getPackageName()));
                    tv.setText((String) msg.obj);
                    if (activity.diskLruCache.isNotExists(fragment.getCacheKey(position))) {
                        imageView.setImageDrawable(activity.getCachedIcon("loading"));
                        imageView.setMinimumHeight(90);
                        imageView.setMinimumWidth(90);
                        imageView.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.load_rotate));
                    }
                    if (fragment.imgPosition < 3) {
                        fragment.checkLoad(fragment.imgPosition + 1);
                    }
                    break;
                case PlaceholderFragment.MSG_TYPE_PRELOAD:
                    CircleProgressDrawable circleProgress = new CircleProgressDrawable.Builder()
                            .threshold(msg.arg2)
                            .build();
                    imageView.clearAnimation();
                    imageView.setImageDrawable(circleProgress);
                    break;
                case PlaceholderFragment.MSG_TYPE_LOADING:
                    Drawable process = imageView.getDrawable();
                    // 消息延迟导致的类型异常
                    if (process instanceof CircleProgressDrawable) {
                        ((CircleProgressDrawable) process).setCurProgress(msg.arg2);
                    }
                    break;
                case PlaceholderFragment.MSG_TYPE_LOAD:
                    imageView.clearAnimation();
                    Drawable imgDrawable = (Drawable) msg.obj;
                    float scale = imgDrawable instanceof GifDrawable ? 2.5f : 4.5f;
                    int width = Math.round(imgDrawable.getIntrinsicWidth() * scale);
                    int height = Math.round(imgDrawable.getIntrinsicHeight() * scale);
                    int layoutWidth = rootView.getWidth();
                    height = width > layoutWidth ? height * layoutWidth / width : height;
                    imageView.setMinimumHeight(height);
                    imageView.setMinimumWidth(width);
                    imageView.setImageDrawable(imgDrawable);
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
