package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.maxiye.first.part.GifWebRvAdapter;
import com.maxiye.first.util.CacheUtil;
import com.maxiye.first.util.DBHelper;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.ProtocolException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    private static JsonObject webCfg;
    private static boolean getNewFlg = true;
    private static int artId = 1023742;
    private static String title = "动态图";
    private static int webPage = 1;
    private static boolean endFlg = false;
    private static final ArrayList<String[]> gifList = new ArrayList<>();
    private final int HISTORY_PAGE_SIZE = 10;
    private static SQLiteDatabase db;
    private static OkHttpClient okHttpClient;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_gif);
        Toolbar toolbar = findViewById(R.id.gif_activity_toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.gif_activity_viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        FloatingActionButton fab = findViewById(R.id.gif_activity_fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
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
        initPage();
        Log.w("end", "onCreateOver");
    }

    private void initPage() {
        mViewPager.setCurrentItem(0, true);
        okHttpClient.dispatcher().cancelAll();
        title = "动态图";
        webPage = 1;
        endFlg = false;
        gifList.clear();
        if (mSectionsPagerAdapter.currentFragment != null) {
            Drawable initShow = getDrawable(android.R.drawable.ic_menu_gallery);
            for (int i = 1;i<4;i++) {
                GifImageView giv = findViewById(getResources().getIdentifier("gif_" + i, "id", getPackageName()));
                giv.clearAnimation();
                giv.setImageDrawable(initShow);
                giv.setMinimumHeight(24);
                giv.setMinimumWidth(24);
            }
        }
    }

    private JsonObject getWebCfg(String webName) {
        JsonObject webCfg = null;
        try {
            JsonObject cfgs = new Gson().fromJson(new InputStreamReader(getAssets().open("gif_spy_config.json")), JsonObject.class);
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
        if (CacheUtil.getSize(this, CacheUtil.UNIT_MB) > 400) CacheUtil.clearAllCache(this);
        db.close();
        okHttpClient = null;
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

    private List<Map<String,Object>> getWebList() {
        JsonObject all_web = getWebCfg(null);
        List<Map<String,Object>> webList = new ArrayList<>();
        for (String key: all_web.keySet()) {
            HashMap<String,Object> item = new HashMap<>();
            item.put("name", key);
            try {
                item.put("icon", BitmapFactory.decodeStream(getAssets().open(all_web.getAsJsonObject(key).get("local_icon").getAsString())));
            } catch (Exception e) {
                e.printStackTrace();
                item.put("icon", BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery));
            }
            webList.add(item);
        }
        return webList;
    }

    public void refresh(MenuItem item) {
        mSectionsPagerAdapter.currentFragment.refresh();
    }

    @SuppressLint("InflateParams")
    public void skipTo(MenuItem item) {
        //实例化布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_item_edittext, null);
        //找到并对自定义布局中的控件进行操作的示例
        EditText pageEdit = view.findViewById(R.id.gif_dialog_input);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_info_black_24dp)//设置图标
                .setTitle("请输入文章id")//设置标题
                .setView(view)//添加布局
                .create();
        //设置按键
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "前往", (dialog1, which) -> {
            if (pageEdit.getText().toString().equals("")) {
                pageEdit.setText("1");
            }
            mViewPager.setCurrentItem(Integer.parseInt(pageEdit.getText().toString()) - 1, true);
            if (endFlg) checkPageEnd();
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", (dialog1, which) -> {
        });
        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("InflateParams")
    public void changeUrl(MenuItem item) {
        //实例化布局
        View view2 = LayoutInflater.from(this).inflate(R.layout.dialog_item_edittext, null);
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
                mSectionsPagerAdapter.currentFragment.refresh();
            }
        });
        dialog2.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", (dialog1, which) -> {});
        dialog2.show();
        dialog2.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @SuppressLint("InflateParams")
    public void switchWeb(MenuItem item) {
        PopupWindow popupWindow = new PopupWindow(400, 600);
        popupWindow.setContentView(LayoutInflater.from(this).inflate(R.layout.popupwindow_view, null));
        popupWindow.setOutsideTouchable(true);
        RecyclerView rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        GifWebRvAdapter ma = new GifWebRvAdapter();
        List<Map<String,Object>> webList = getWebList();
        ma.setData(webList);
        ma.setOnItemClickListener(position -> {
            webName = (String) webList.get(position).get("name");
            Log.w("rvClick", webName);
            webCfg = getWebCfg(webName);
            getNewFlg = true;
            initPage();
            mSectionsPagerAdapter.currentFragment.refresh();
            popupWindow.dismiss();
        });
        rv.setAdapter(ma);
        popupWindow.showAsDropDown(findViewById(R.id.gif_activity_appbar), 0, 0, Gravity.END);
    }

    public void browserOpen(MenuItem item) {
        //设置反面按钮
        //设置中立按钮
        new AlertDialog.Builder(this)
                .setTitle("提示：")
                .setMessage("使用浏览器打开")
                .setIcon(R.drawable.ic_public_orange_60dp)
                //点击对话框以外的区域是否让对话框消失
                .setCancelable(true)
                //设置正面按钮
                .setPositiveButton("首页", (dialog, which) -> {
                    Uri url = Uri.parse(String.format(webCfg.get("gif_web").getAsString(), artId));
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                    dialog.dismiss();
                })
                .setNegativeButton("当前页", (dialog, which) -> {
                    Uri url = Uri.parse(webPage > 1 ? String.format(webCfg.get("gif_web_2nd").getAsString(), artId, webPage - 1) : String.format(webCfg.get("gif_web").getAsString(), artId));
                    startActivity(new Intent(Intent.ACTION_VIEW, url));
                    dialog.dismiss();
                })
                .setNeutralButton("取消", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public void listHistory(MenuItem item) {
        PopupWindow popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, 1400);
        View root = LayoutInflater.from(this).inflate(R.layout.popupwindow_view, null);
        popupWindow.setContentView(root);
        popupWindow.setOutsideTouchable(true);
        RecyclerView rv = popupWindow.getContentView().findViewById(R.id.popupwindow_rv);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        GifWebRvAdapter ma = new GifWebRvAdapter();
        int historyCount = getHistoryCount();
        List<Map<String,Object>> historyList = getHistoryList(1);
        //设置页面相关
        if (historyCount > HISTORY_PAGE_SIZE) {
            EditText page = root.findViewById(R.id.popup_page);
            Button prev = root.findViewById(R.id.popup_prev_page);
            Button next = root.findViewById(R.id.popup_next_page);
            prev.setOnClickListener(v -> {
                int nowPage = Integer.parseInt(page.getText().toString());
                if (nowPage > 1) {
                    int pre = nowPage - 1;
                    ma.setData(getHistoryList(pre));
                    page.setText(pre + "");
                    next.setVisibility(View.VISIBLE);
                    prev.setVisibility(pre == 1 ? View.GONE : View.VISIBLE);
                }
            });
            next.setOnClickListener(v -> {
                int nowPage = Integer.parseInt(page.getText().toString());
                int nxt = nowPage + 1;
                ma.setData(getHistoryList(nxt));
                page.setText(nxt + "");
                prev.setVisibility(View.VISIBLE);
                next.setVisibility(nxt * HISTORY_PAGE_SIZE < historyCount ? View.VISIBLE : View.GONE);
            });
            page.setText("1");
            page.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
        }
        ma.setData(historyList);
        ma.setOnItemClickListener(position -> {
            webName = (String) historyList.get(position).get("web_name");
            Log.w("rvClick", webName);
            webCfg = getWebCfg(webName);
            artId = (int) historyList.get(position).get("art_id");
            initPage();
            mSectionsPagerAdapter.currentFragment.refresh();
            popupWindow.dismiss();
        });
        ma.setOnItemLongClickListener(position -> {
            PopupMenu pMenu = new PopupMenu(this, rv.getChildAt(position));
            pMenu.getMenuInflater().inflate(R.menu.gif_history_popupmenu, pMenu.getMenu());
            pMenu.setOnMenuItemClickListener(item1 -> {
                switch (item1.getItemId()) {
                    case R.id.delete_gif_record:
                        //删除记录
                        int delArtId = (int) historyList.get(position).get("art_id");
                        String delWebName = (String) historyList.get(position).get("web_name");
                        deleteHistory(delArtId, delWebName);
                        EditText page = root.findViewById(R.id.popup_page);
                        ma.setData(getHistoryList(Integer.parseInt(page.getText().toString())));
                        ma.notifyItemRemoved(position);
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
        Cursor cus = db.query(DBHelper.TB_GIF_WEB, new String[]{"count(*) as count"}, "art_id != 0", new String[]{}, null, null, null);
        cus.moveToFirst();
        int count = cus.getInt(cus.getColumnIndex("count"));
        cus.close();
        return count;
    }

    private void deleteHistory(int delArtId, String delWebName) {
        Log.w("deleteHistory", "art_id：" + delArtId + "，web_name：" + delWebName);
        db.delete(DBHelper.TB_GIF_WEB, "art_id = ? and web_name = ?", new String[]{delArtId + "", delWebName});
        db.delete(DBHelper.TB_GIF_WEB_ITEM, "art_id = ? and web_name = ?", new String[]{delArtId + "", delWebName});
    }

    private List<Map<String,Object>> getHistoryList(int page) {
        int offset = (page - 1) * HISTORY_PAGE_SIZE;
        JsonObject all_web = getWebCfg(null);
        String sql = "select * from " + DBHelper.TB_GIF_WEB + " where art_id != 0 order by id desc limit " + HISTORY_PAGE_SIZE + " offset " + offset;
//        Cursor cus = db.query(DBHelper.TB_GIF_WEB, new String[]{"*"}, "art_id != 0", new String[]{}, null, null, "id desc", HISTORY_PAGE_SIZE + " OFFSET " + offset);
        Cursor cus = db.rawQuery(sql, null);
        Log.w("getHistoryList：", cus.getCount() + "");
        List<Map<String,Object>> historyList = new ArrayList<>();
        int count = cus.getCount();
        if (count > 0) {
            cus.moveToFirst();
            for (int i = 0; i < count; i++) {
                HashMap<String,Object> item = new HashMap<>();
                item.put("web_name", cus.getString(cus.getColumnIndex("web_name")));
                item.put("name", cus.getString(cus.getColumnIndex("title")));
                item.put("web_url", cus.getString(cus.getColumnIndex("web_url")));
                item.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                try {
                    item.put("icon", BitmapFactory.decodeStream(getAssets().open(all_web.getAsJsonObject((String) item.get("web_name")).get("local_icon").getAsString())));
                } catch (Exception e) {
                    e.printStackTrace();
                    item.put("icon", BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_gallery));
                }
                historyList.add(item);
                cus.moveToNext();
            }
        }
        cus.close();
        return historyList;
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
    public static class PlaceholderFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
//        private static Lock lock = new ReentrantLock();
        private static final String ARG_SECTION_NUMBER = "section_number";
        private int gifPosition = 1;
        private int downloadPosition = 1;
        private Handler handler;
        private String gifUrl;
        private String gufTitle;
        private static final int MSG_TYPE_PRE = 100;
        private static final int MSG_TYPE_LOAD = 101;
        private static final int MSG_TYPE_DOWNLOADED = 102;
        private static final int MSG_TYPE_DOWNLOAD_ERR = 103;
        private static final int MSG_TYPE_EMPTY = 104;
        private static final int MSG_TYPE_OVER = 105;
        private Context activity;
        private OnPFListener mListener;

        public PlaceholderFragment() {}

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @SuppressLint("SetTextI18n")
        static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            fragment.handler = new Handler((Message msg) -> {
                View rootView = fragment.getView();
                if (rootView == null) return false;
                TextView textView = rootView.findViewById(R.id.section_label);
                assert fragment.getArguments() != null;
                textView.setText(title + "：" + fragment.getArguments().getInt(ARG_SECTION_NUMBER));
                Drawable errShow = fragment.activity.getDrawable(android.R.drawable.ic_delete);
                switch (msg.what) {
                    case MSG_TYPE_PRE:
                        TextView tv = rootView.findViewById(fragment.getResources().getIdentifier("gtxt_" + fragment.gifPosition, "id", fragment.activity.getPackageName()));
                        tv.setText(fragment.gufTitle);
                        GifImageView iv1 = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + fragment.gifPosition, "id", fragment.activity.getPackageName()));
                        if (!(iv1.getDrawable() instanceof  GifDrawable)) {
                            Drawable initShow = fragment.activity.getDrawable(R.drawable.ic_sync_black_24dp);
                            iv1.setImageDrawable(initShow);
                            iv1.setMinimumHeight(24);
                            iv1.setMinimumWidth(24);
                            iv1.setAnimation(AnimationUtils.loadAnimation(fragment.activity, R.anim.load_rotate));
                        }
                        if (++fragment.gifPosition < 4) {
                            new Thread(fragment::loadGif).start();
                        } else {
                            fragment.gifPosition = 1;
                        }
                        break;
                    case MSG_TYPE_LOAD:
                        GifImageView iv = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", fragment.activity.getPackageName()));
                        iv.clearAnimation();
                        Drawable gifFromStream = (GifDrawable) msg.obj;
                        /*iv.setImageDrawable(gifFromStream);//fixme 删除
                        iv.setMinimumHeight((int)Math.round(gifFromStream.getIntrinsicHeight() * 2.5));
                        iv.setMinimumWidth((int)Math.round(gifFromStream.getIntrinsicWidth() * 2.5));*/
                        break;
                    case MSG_TYPE_OVER:
                        fragment.mListener.checkPageEnd();
                        break;
                    case MSG_TYPE_DOWNLOADED:
                        File gif = (File) msg.obj;
                        Snackbar.make(rootView, "下载完成", Snackbar.LENGTH_SHORT)
                                .setAction("打开", v -> {
                                    Intent imgView = new Intent(Intent.ACTION_VIEW);
                                    imgView.setDataAndType(FileProvider.getUriForFile(fragment.activity, "com.maxiye.first.fileprovider", gif), "image/gif");
                                    imgView.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//不加黑屏读取不了
                                    fragment.activity.startActivity(imgView);
                                }).show();
                        break;
                    case MSG_TYPE_EMPTY:
                        GifImageView iv2 = rootView.findViewById(fragment.getResources().getIdentifier("gif_" + msg.arg1, "id", fragment.activity.getPackageName()));
                        iv2.clearAnimation();
                        iv2.setImageDrawable(errShow);
                        break;
                }
                return true;
            });
            return fragment;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            activity = context;
            if (context instanceof OnPFListener) {
                mListener = (OnPFListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnPFListener");
            }
            Log.w("onAttach", context.toString());
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_get_gif, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            assert getArguments() != null;
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            rootView.findViewById(R.id.gif_1).setOnLongClickListener((View view) -> {
                longClickCb(1, view);
                return true;
            });
            rootView.findViewById(R.id.gif_2).setOnLongClickListener((View view) -> {
                longClickCb(2, view);
                return true;
            });
            rootView.findViewById(R.id.gif_3).setOnLongClickListener((View view) -> {
                longClickCb(3, view);
                return true;
            });
            return rootView;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            okHttpClient.dispatcher().cancelAll();
            if (isVisibleToUser) {
                if (gifPosition == 1) {
                    new Thread(this::loadGif).start();
                }
            }
        }

        private int getGifOffset(int index) {
            assert getArguments() != null;
            return (getArguments().getInt(ARG_SECTION_NUMBER) - 1) * 3 + index - 1;
        }

        private void longClickCb(int position, View view) {
            downloadPosition = position;
            PopupMenu pMenu = new PopupMenu(activity, view);
            pMenu.getMenuInflater().inflate(R.menu.gif_image_popupmenu, pMenu.getMenu());
            pMenu.setOnMenuItemClickListener(this);
            pMenu.show();
        }

        private void download() {
            Log.w("download", "download:start");
            PermissionUtil.req(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PermissionUtil.PER_REQ_STORAGE_WRT, () -> {
                new Thread(() -> {
                    String[] gifInfo = getGifInfo(getGifOffset(downloadPosition));
                    File gif = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/gif/" + gifInfo[1]);
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
                        File cacheGif = new File(activity.getCacheDir(), artId + "-" + getGifOffset(downloadPosition) + ".gif");
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
                        handler.obtainMessage(MSG_TYPE_DOWNLOADED, gif).sendToTarget();
                    } catch (Exception e) {
                        handler.obtainMessage(MSG_TYPE_DOWNLOAD_ERR, "下载失败").sendToTarget();
                        e.printStackTrace();
                    }
                }).start();
                Toast.makeText(activity, "开始下载文件...", Toast.LENGTH_SHORT).show();
            });
        }

        void loadGif() {
            int nowPos = gifPosition;
            int startOffset = getGifOffset(nowPos);
            Log.w("info", "loadGif:" + startOffset);
            String[] gifInfo = getGifInfo(startOffset);
            if (gifInfo == null)
                return;
            try {
                gifUrl = gifInfo[0];
                gufTitle = gifInfo[1];
                handler.obtainMessage(MSG_TYPE_PRE, "").sendToTarget();
                File cacheGif = new File(activity.getCacheDir(), artId + "-" + startOffset + ".gif");
                if (cacheGif.exists()) {
                    Log.w("info", "loadGif(fromCache):" + gifInfo[1]);
                    GifDrawable gifFromStream = new GifDrawable(cacheGif);
                    handler.obtainMessage(MSG_TYPE_LOAD, nowPos, 0, gifFromStream).sendToTarget();
                } else {
                    Log.w("info", "loadGif:" + gifInfo[1]);
                    Request request = new Request.Builder().url(gifUrl).build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            handler.obtainMessage(MSG_TYPE_EMPTY, nowPos, 0, "").sendToTarget();
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            try {
                                if (response.body() != null) {
                                    Log.w("info", "loadGif(fromNet):" + gifInfo[1]);
                                    byte[] b = response.body().bytes();
                                    if (cacheGif.createNewFile()) {
                                        RandomAccessFile raf = new RandomAccessFile(cacheGif, "rwd");
                                        raf.write(b);
                                        raf.close();
                                    }
                                    GifDrawable gifFromStream = new GifDrawable(b);//始终占用bis;
                                    handler.obtainMessage(MSG_TYPE_LOAD, nowPos, 0, gifFromStream).sendToTarget();
                                } else {
                                    handler.obtainMessage(MSG_TYPE_EMPTY, nowPos, 0, "").sendToTarget();
                                    throw new Exception("图片资源获取失败");
                                }
                            } catch (Exception e) {
                                cacheGif.delete();
                                handler.obtainMessage(MSG_TYPE_EMPTY, nowPos, 0, "").sendToTarget();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private synchronized void loadGifList() {
            //数据库获取
            if (webPage == 1) {
                if (getNewFlg)
                    getNewArtId();
                if (getDbGifList())
                    return;
            }
            if (endFlg) return;
            Log.w("start", "loadGifList(lock):" + webPage);
            String baseUrl = String.format(webCfg.get("gif_web").getAsString(), artId);
            JsonObject regObj = webCfg.getAsJsonObject("gif_reg");
            int urlIdx = regObj.get("gif_url_idx").getAsInt();
            int titleIdx = regObj.get("git_title_idx").getAsInt();
            Pattern pt = Pattern.compile(regObj.get("reg").getAsString());
            try {
                String url = webPage > 1 ? String.format(webCfg.get("gif_web_2nd").getAsString(), artId, webPage) : baseUrl;
                System.out.println(url);
                Request req = new Request.Builder().url(url).build();
                String content = new String(okHttpClient.newCall(req).execute().body().bytes(), "utf-8");
                Matcher mt = pt.matcher(content);
                if (!mt.find()) {
                    throw new ProtocolException("over");
                }
                mt.reset();
                while (mt.find()) {
                    String name = (mt.group(titleIdx) == null ? UUID.randomUUID().toString() : mt.group(titleIdx)) + ".gif";
                    System.out.println(name);
                    System.out.println(mt.group(urlIdx));
                    String gifUrl;
                    if (webName.equals("duowan")) {
                        name = Util.unicode2Chinese(name);
                        gifUrl = mt.group(urlIdx).replace("\\", "");
                    } else {
                        gifUrl = mt.group(urlIdx);
                    }
                    String[] gifInfo = new String[]{gifUrl, name};
                    gifList.add(gifInfo);
                    setDbGifList(DBHelper.TB_GIF_WEB_ITEM, gifInfo);
                }
                if (webPage == 1 && gifList.size() > 0) {
                    Log.w("titleGet", title);
                    if (title.equals("动态图")) getNewTitle(content);
                    setDbGifList(DBHelper.TB_GIF_WEB, new String[]{url, title});
                }
                webPage++;
            } catch (Exception e) {//java.net.ProtocolException: Too many follow-up requests: 21
                if (e instanceof ProtocolException) {
                    endFlg = true;
                    if (gifList.size() > 0) updateDbField("pages", String.valueOf(webPage - 1));
                    handler.obtainMessage(MSG_TYPE_OVER, "").sendToTarget();
                }
                e.printStackTrace();
            } finally {
                Log.w("end", "loadGifList(unlock):end:" + webPage);
                System.out.println(gifList.toString());
            }
        }

        private void getNewArtId() {
            Log.w("getNewArtId", "获取最新内容...");
            String url = webCfg.get("spy_root").getAsString();
            JsonObject regObj = webCfg.getAsJsonObject("gif_web_reg");
            int artIdIdx = regObj.get("art_id_idx").getAsInt();
            int titleIdx = regObj.get("title_idx").getAsInt();
            Pattern pt = Pattern.compile(regObj.get("reg").getAsString());
            try {
                Request req = new Request.Builder().url(url).build();
                String content = new String(okHttpClient.newCall(req).execute().body().bytes(), "utf-8");
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
            JsonObject regObj = webCfg.getAsJsonObject("title_reg");
            int titleIdx = regObj.get("title_idx").getAsInt();
            Pattern pt = Pattern.compile(regObj.get("reg").getAsString());
            if (content == null) {
                content = "";
                Request req = new Request.Builder().url(String.format(webCfg.get("gif_web").getAsString(), artId)).build();
                try {
                    content = new String(okHttpClient.newCall(req).execute().body().bytes(), "utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Matcher mt = pt.matcher(content);
            if (mt.find()) {
                title = mt.group(titleIdx);
                //updateDbField("title", title);//fixme 待删
                Log.w("getNewTitle", title);
            }
        }

        private boolean getDbGifList() {
            Cursor cus = db.query(DBHelper.TB_GIF_WEB, new String[]{"*"}, "art_id = ? and web_name = ?", new String[]{artId + "", webName}, null, null, "id desc", "1");
            Log.w("db_web", cus.getCount() + "");
            if (cus.getCount() > 0) {
                cus.moveToFirst();
                title = cus.getString(cus.getColumnIndex("title"));
                //getNewTitle(null);//fixme 删
                int totalPage = cus.getInt(cus.getColumnIndex("pages"));
                Cursor cus2 = db.query(DBHelper.TB_GIF_WEB_ITEM, new String[]{"page,title,url"}, "art_id = ? and web_name = ?", new String[]{artId + "", webName}, null, null, "id asc");
                int count = cus2.getCount();
                if (count > 0) {
                    Log.w("db_item", count + "");
                    cus2.moveToFirst();
                    for (int i = 0; i < count; i++) {
                        String[] gifInfo = new String[2];
                        gifInfo[0] = cus2.getString(cus2.getColumnIndex("url"));
                        gifInfo[1] = cus2.getString(cus2.getColumnIndex("title"));
                        gifList.add(gifInfo);
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


        private void updateDbField(String field, String val) {
            ContentValues ctv = new ContentValues();
            ctv.put(field, val);
            int rows = db.update(DBHelper.TB_GIF_WEB, ctv, "art_id = ? and web_name = ?", new String[]{artId + "",webName});
            Log.w("db_web_update: ", rows + "");
        }

        private void setDbGifList(String dbName, String[] data) {
            ContentValues ctv = new ContentValues();
            if (dbName.equals(DBHelper.TB_GIF_WEB)) {
                ctv.put("art_id", artId);
                ctv.put("web_url", data[0]);
                ctv.put("web_name", webName);
                ctv.put("title", data[1]);
                ctv.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
                long newId = db.insert(dbName, null, ctv);
                Log.w("db_web_insert: ", newId + "");
            } else {
                ctv.put("art_id", artId);
                ctv.put("page", webPage);
                ctv.put("web_name", webName);
                ctv.put("title", data[1]);
                ctv.put("url", data[0]);
                long newId = db.insert(dbName, null, ctv);
                Log.w("db_web_item_insert: ", newId + "");
            }

        }

        String[] getGifInfo(int offset) {
            Log.w("start", "getGifInfo:" + offset + "-" + webPage + "-" + gifList.size() + "-" + endFlg);
            if (gifList.size() <= offset) {
                if (endFlg)
                    return null;
                loadGifList();
                return getGifInfo(offset);
            } else {
                return gifList.get(offset);
            }
        }

        void refresh() {
            okHttpClient.dispatcher().cancelAll();
            this.gifPosition = 1;
            new Thread(this::loadGif).start();
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.download_gif:
                    download();
                    break;
            }
            return false;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {
        PlaceholderFragment currentFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.w("start", "getItem:" + position);
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 33;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentFragment = (PlaceholderFragment) object;
            super.setPrimaryItem(container, position, object);
        }
    }
}

interface OnPFListener {
    void checkPageEnd();
}
