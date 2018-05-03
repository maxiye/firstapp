package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class GetGifActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static final int PER_REQ_STORAGE_WRT = 100;
    private static boolean getNewFlg = false;//todo
    private static int artId = 1023742;
    private static String title = "动态图";
    private static int webPage = 1;
    private static boolean endFlg = false;
    protected static ArrayList<String[]> gifList = new ArrayList<>();
    static SQLiteDatabase db;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_gif);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        Log.w("end", "onCreateOver");
        DBHelper dbh = new DBHelper(this);
        db = dbh.getWritableDatabase();//此时创建数据库,生成.db文件
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNewFlg = getIntent().getExtras().getBoolean(TestActivity.GET_NEW_FLG, false);
            Log.w("end", getNewFlg ? "true" : "false");
        }
        gifList.clear();
        webPage = 1;
        endFlg = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CacheUtil.clearAllCache(this);
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] pers, @NonNull int[] res) {
        switch (reqCode) {
            case PER_REQ_STORAGE_WRT: {
                if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("perm", "获取写权限");
                } else {
                    alert("权限被拒绝");
                }
            }
            default: {

            }
        }
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
        getMenuInflater().inflate(R.menu.menu_get_gif, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_refresh:
                PlaceholderFragment pf = mSectionsPagerAdapter.currentFragment;
                Log.w("end", pf.gufTitle + "");
                pf.refresh();
                return true;
            case R.id.action_skip_to:
                //实例化布局
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_item_edittext,null);
                //找到并对自定义布局中的控件进行操作的示例
                EditText pageEdit = view.findViewById(R.id.gif_dialog_input);
                //创建对话框
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setIcon(R.drawable.ic_info_black_24dp);//设置图标
                dialog.setTitle("请输入页码");//设置标题
                dialog.setView(view);//添加布局
                //设置按键
                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "前往", (dialog1, which) -> {
                    if (pageEdit.getText().toString().equals("")) {
                        pageEdit.setText("1");
                    }
                    mViewPager.setCurrentItem(Integer.parseInt(pageEdit.getText().toString()) -1, true);
                });
                dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", (dialog1, which) -> {});
                dialog.show();
                Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;
            case R.id.action_change_url:
                //实例化布局
                View view2 = LayoutInflater.from(this).inflate(R.layout.dialog_item_edittext, null);
                //找到并对自定义布局中的控件进行操作的示例
                EditText articleId = view2.findViewById(R.id.gif_dialog_input);
                //创建对话框
                AlertDialog dialog2 = new AlertDialog.Builder(this).create();
                dialog2.setIcon(R.drawable.ic_info_black_24dp);//设置图标
                dialog2.setTitle("请输入文章id");//设置标题
                dialog2.setView(view2);//添加布局
                //设置按键
                dialog2.setButton(AlertDialog.BUTTON_POSITIVE, "前往", (dialog1, which) -> {
                    String txt = articleId.getText().toString();
                    if (txt.equals("")) {
                        alert("文章id不能为空");
                    } else {
                        artId = Integer.parseInt(txt);
                        webPage = 1;
                        endFlg = false;
                        gifList.clear();
                        mViewPager.setCurrentItem(0, true);
                        mSectionsPagerAdapter.currentFragment.refresh();
                    }

                });
                dialog2.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", (dialog1, which) -> {});
                dialog2.show();
                Objects.requireNonNull(dialog2.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                break;
        }

        return super.onOptionsItemSelected(item);
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
        private static final int MSG_TYPE_START = 101;
        private static final int MSG_TYPE_DOWNLOADED = 102;
        private static final int MSG_TYPE_DOWNLOAD_ERR = 103;
        private static final int MSG_TYPE_EMPTY = 104;

        public PlaceholderFragment() {}

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_get_gif, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            assert getArguments() != null;
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            GifImageView gif_1 = rootView.findViewById(R.id.gif_1);
            GifImageView gif_2 = rootView.findViewById(R.id.gif_2);
            GifImageView gif_3 = rootView.findViewById(R.id.gif_3);
            handler = new Handler((Message msg) -> {
                textView.setText(title + "：" + getArguments().getInt(ARG_SECTION_NUMBER));
                switch (msg.what) {
                    case MSG_TYPE_PRE:
                        TextView tv = rootView.findViewById(getResources().getIdentifier("gtxt_" + gifPosition, "id", getActivity().getPackageName()));
                        tv.setText(gufTitle);
                        gifPosition++;
                        if (gifPosition < 4) {
                            new Thread(this::loadGif).start();
                        } else {
                            gifPosition = 1;
                        }
                        break;
                    case MSG_TYPE_START:
                        GifImageView iv = rootView.findViewById(getResources().getIdentifier("gif_" + msg.arg1, "id", getActivity().getPackageName()));
                        iv.clearAnimation();
                        GifDrawable gifFromStream = (GifDrawable) msg.obj;
                        iv.setImageDrawable(gifFromStream);
                        iv.setMinimumHeight(gifFromStream.getIntrinsicHeight() * 2);
                        iv.setMinimumWidth(gifFromStream.getIntrinsicWidth() * 2);
                        break;
                    case MSG_TYPE_DOWNLOADED:
                        File gif = (File) msg.obj;
                        Snackbar.make(rootView, "下载完成", Snackbar.LENGTH_SHORT)
                                .setAction("打开", v -> {
                                    Intent imgView = new Intent(Intent.ACTION_VIEW);
                                    imgView.setDataAndType(FileProvider.getUriForFile(getActivity(), "com.maxiye.first.fileprovider", gif), "image/gif");
                                    imgView.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//不加黑屏读取不了
                                    getActivity().startActivity(imgView);
                                }).show();
                        break;
                    case MSG_TYPE_EMPTY:
                        Toast.makeText(getActivity(), "资源获取失败", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            });
            rootView.setLongClickable(true);
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.load_rotate);
            gif_1.setAnimation(anim);
            gif_2.setAnimation(anim);
            gif_3.setAnimation(anim);
            gif_1.setOnLongClickListener((View view) -> {
                longClickCb(1, view);
                return true;
            });
            gif_2.setOnLongClickListener((View view) -> {
                longClickCb(2, view);
                return true;
            });
            gif_3.setOnLongClickListener((View view) -> {
                longClickCb(3, view);
                return true;
            });
            return rootView;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                if (gifPosition == 1) {
                    new Thread(this::loadGif).start();
                }
            } else {
                if (getActivity() != null) {
                    /*((GifImageView)getActivity().findViewById(R.id.gif_1)).setImageResource(R.drawable.ic_sync_black_24dp);
                    ((GifImageView)getActivity().findViewById(R.id.gif_2)).setImageResource(R.drawable.ic_sync_black_24dp);
                    ((GifImageView)getActivity().findViewById(R.id.gif_3)).setImageResource(R.drawable.ic_sync_black_24dp);*/
                }
            }
        }

        private int getGifOffset(int index) {
            assert getArguments() != null;
            return (getArguments().getInt(ARG_SECTION_NUMBER) - 1) * 3 + index - 1;
        }

        private void longClickCb(int position, View view) {
            downloadPosition = position;
            PopupMenu pMenu = new PopupMenu(getActivity(), view);
            pMenu.getMenuInflater().inflate(R.menu.gif_activity_popupmenu, pMenu.getMenu());
            pMenu.setOnMenuItemClickListener(this);
            pMenu.show();
        }

        private void download() {
            Log.w("download", "download:start");
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PER_REQ_STORAGE_WRT);
            Toast.makeText(getActivity(), "开始下载文件...", Toast.LENGTH_SHORT).show();
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
                        if (!gif.createNewFile()){
                            throw new Exception("create file error");
                        }
                    }
                    File cacheGif = new File(getActivity().getCacheDir(), artId + "-" + getGifOffset(downloadPosition) + ".gif");
                    if (!cacheGif.exists()) throw new Exception("未发现缓存文件");
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
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    //发送广播
                    Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    scanIntent.setData(Uri.fromFile(gif));
                    getActivity().sendBroadcast(scanIntent);
                    handler.sendMessage(handler.obtainMessage(MSG_TYPE_DOWNLOADED, gif));
                } catch (Exception e) {
                    handler.sendMessage(handler.obtainMessage(MSG_TYPE_DOWNLOAD_ERR, "下载失败"));
                    e.printStackTrace();
                }
            }).start();

        }

        public void loadGif() {
            int nowPos = gifPosition;
            int startOffset = getGifOffset(nowPos);
            Log.w("info", "loadGif:" + startOffset);
            String[] gifInfo = getGifInfo(startOffset);
            if (gifInfo == null) return;
            gifUrl = gifInfo[0];
            gufTitle = gifInfo[1];
            handler.sendMessage(handler.obtainMessage(MSG_TYPE_PRE, ""));
            try {
                File cacheGif = new File(getActivity().getCacheDir(), artId + "-" +startOffset + ".gif");
                if (cacheGif.exists()) {
                    Log.w("info", "loadGif(fromCache):" + gifInfo[1]);
                    GifDrawable gifFromStream = new GifDrawable(cacheGif);
                    handler.sendMessage(handler.obtainMessage(MSG_TYPE_START, nowPos, 0, gifFromStream));
                } else {
                    Log.w("info", "loadGif:" + gifInfo[1]);
                    Request request = new Request.Builder().url(gifUrl).build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            try {
                                GifDrawable gifFromStream = new GifDrawable( getResources(), R.drawable.ic_sync_black_24dp );
                                handler.sendMessage(handler.obtainMessage(MSG_TYPE_START, nowPos, 0, gifFromStream));
                            } catch (IOException e1) {
                                handler.sendMessage(handler.obtainMessage(MSG_TYPE_EMPTY, ""));
                                e1.printStackTrace();
                            }
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
                                    handler.sendMessage(handler.obtainMessage(MSG_TYPE_START, nowPos, 0, gifFromStream));
                                } else {
                                    handler.sendMessage(handler.obtainMessage(MSG_TYPE_EMPTY, ""));
                                    cacheGif.delete();
                                    throw new Exception("图片资源获取失败");
                                }
                            } catch (Exception e) {
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
                if (getNewFlg) getNewArtId();
                if (getDbGifList()) return;
            }
            if (endFlg) return;
            Log.w("start", "loadGifList(lock):" + webPage);
            String baseUrl = "http://wap.gamersky.com/news/Content-" + artId;
            String reg = "alt=\".+\"[^>]*?src=\"(http://[^\"]+.gif)\"[^>]*>(<br>\\r\\n([^<]+))?</p>";
            Pattern pt = Pattern.compile(reg);
            try {
                String url = baseUrl + (webPage > 1 ? "_" + webPage : "") + ".html";
                if (webPage == 1) {
                    String[] webInfo = new String[3];
                    webInfo[0] = url;
                    webInfo[1] = title;
                    webInfo[2] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    setDbGifList(DBHelper.TB_GIF_WEB, webInfo);
                }
                System.out.println(url);
                URL req = new URL(url);
                URLConnection uc = req.openConnection();
                uc.setConnectTimeout(5000);
                InputStream is = uc.getInputStream();
                StringBuilder content = new StringBuilder();
                BufferedReader read = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;//循环读取
                while ((line = read.readLine()) != null) {
                    content.append(line).append("\r\n");
                }
                is.close();
                read.close();
                Matcher mt = pt.matcher(content.toString());
                while (mt.find()) {
                    String name = (mt.group(3) == null ? UUID.randomUUID().toString() : mt.group(3)) + ".gif";
                    System.out.println(name);
                    String[] gifInfo = new String[2];
                    System.out.println(mt.group(1));
                    gifInfo[0] = mt.group(1);
                    gifInfo[1] = name;
                    gifList.add(gifInfo);
                    setDbGifList(DBHelper.TB_GIF_WEB_ITEM, gifInfo);
                }
                webPage++;
            } catch (Exception e) {
                endFlg = webPage > 27;
                e.printStackTrace();
            } finally {
                Log.w("end", "loadGifList(unlock):end:" + webPage);
                System.out.println(gifList.toString());
            }
        }

        private boolean getDbGifList() {
            Cursor cus = db.query(DBHelper.TB_GIF_WEB, new String[]{"*"}, "art_id = ?", new String[]{artId + ""}, null, null, "id desc", "1");
            Log.w("db_web", cus.getCount() + "");
            if (cus.getCount() > 0) {
                cus.moveToFirst();
                title = cus.getString(cus.getColumnIndex("title"));
                int totalPage = cus.getInt(cus.getColumnIndex("pages"));
                Cursor cus2 = db.query(DBHelper.TB_GIF_WEB_ITEM, new String[]{"page,title,url"}, "art_id = ?", new String[]{artId + ""}, null, null, "page asc");
                int count = cus2.getCount();
                if (count > 0) {
                    Log.w("db_item", count + "");
                    cus2.moveToFirst();
                    for (int i = 0;i < count;i++) {
                        String[] gifInfo = new String[2];
                        gifInfo[0] = cus2.getString(cus2.getColumnIndex("url"));
                        gifInfo[1] = cus2.getString(cus2.getColumnIndex("title"));
                        gifList.add(gifInfo);
                        webPage = cus2.getInt(cus2.getColumnIndex("page"));
                        cus2.moveToNext();
                    }
                }
                cus2.close();
                if (totalPage == webPage) endFlg = true;
            }
            cus.close();
            return false;
        }

        private void setDbGifList(String dbName, String[] data) {
            ContentValues ctv = new ContentValues();
            if (dbName.equals(DBHelper.TB_GIF_WEB)) {
                ctv.put("art_id", artId);
                ctv.put("web_url", data[0]);
                ctv.put("title", data[1]);
                ctv.put("time", data[2]);
                long newId = db.insert(dbName, null, ctv);
                Log.w("db_web_insert: ", newId + "");
            } else {
                ctv.put("art_id", artId);
                ctv.put("page", webPage);
                ctv.put("title", data[1]);
                ctv.put("url", data[0]);
                long newId = db.insert(dbName, null, ctv);
                Log.w("db_web_item_insert: ", newId + "");
            }

        }

        private void getNewArtId() {
            String url = "http://www.gamersky.com";
            String reg = "<a href=\"(http://[^\"]+/([0-9]+)\\.shtml)\"[^>]*?title=\"([^\"]+动态图)\"";
            Pattern pt = Pattern.compile(reg);
            try {
                URL req = new URL(url);
                URLConnection uc = req.openConnection();
                uc.setConnectTimeout(5000);
                InputStream is = uc.getInputStream();
                StringBuilder content = new StringBuilder();
                BufferedReader read = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;//循环读取
                while ((line = read.readLine()) != null) {
                    content.append(line).append("\r\n");
                }
                is.close();
                read.close();
                Matcher mt = pt.matcher(content.toString());
                if (mt.find()) {
                    title = mt.group(3);
                    artId = Integer.parseInt(mt.group(2));
                    endFlg = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println(title);
            }
        }

        public String[] getGifInfo(int offset) {
            Log.w("start", "getGifInfo:" + offset);
            if (gifList.size() <= offset) {
                if (endFlg) return null;
                loadGifList();
                return getGifInfo(offset);
            } else {
                return gifList.get(offset);
            }
        }

        public void refresh() {
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
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
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
