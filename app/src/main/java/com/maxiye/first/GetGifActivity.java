package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static boolean getNewFlg = true;
    private static int artId = 1042254;
    private static String title = "动态图";
    private static int webPage = 1;
    private static boolean endFlg = false;
    protected static ArrayList<String[]> gifList = new ArrayList<>();

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
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
        Log.w("end", "onCreateOver");
        gifList.clear();
        webPage = 1;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getNewFlg = getIntent().getExtras().getBoolean(TestActivity.GET_NEW_FLG, false);
            Log.w("end", getNewFlg ? "true" : "false");
        }
//        Glide.get(this).clearDiskCache();
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] pers, @NonNull int[] res) {
        switch (reqCode) {
            case PER_REQ_STORAGE_WRT: {
                if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
//                    alert("权限已获取");
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
                Log.w("end", pf.txt + "");
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
        private static Lock lock = new ReentrantLock();
        private static final String ARG_SECTION_NUMBER = "section_number";
        private int gifIndex = 1;
        private int downloadPosition = 1;
        private Handler handler;
        private String gifUrl;
        private String txt;
        private static final int MSG_TYPE_START = 100;
        private static final int MSG_TYPE_DOWNLOADED = 101;

        public PlaceholderFragment() {
        }

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
            handler = new Handler(msg -> {
                textView.setText(title + "：" + getArguments().getInt(ARG_SECTION_NUMBER));
                int gifId;
                int txtId;
                switch (msg.what) {
                    case MSG_TYPE_START:
                        switch (gifIndex) {
                            case 1:
                                gifId = R.id.gif_1;
                                txtId = R.id.gtxt_1;
                                break;
                            case 2:
                                gifId = R.id.gif_2;
                                txtId = R.id.gtxt_2;
                                break;
                            case 3:
                                gifId = R.id.gif_3;
                                txtId = R.id.gtxt_3;
                                break;
                            default:
                                gifId = R.id.gif_1;
                                txtId = R.id.gtxt_1;

                        }
                        ImageView iv = rootView.findViewById(gifId);
                        TextView tv = rootView.findViewById(txtId);
                        iv.setImageBitmap(null);
                        GlideApp.with(this)
                                .asGif()
                                .load(gifUrl)
                                .placeholder(R.drawable.ic_sync_black_24dp)
                                .error(android.R.drawable.ic_delete)
                                .centerCrop()
                                .into(iv);
                        tv.setText(txt);
                        gifIndex++;
                        if (gifIndex < 4) {
                            new Thread(this::loadGif).start();
                        } else {
                            gifIndex = 1;
                        }
                        break;
                    case MSG_TYPE_DOWNLOADED:
                        String info = (String) msg.obj;
                        Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            });
            rootView.setLongClickable(true);
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
            if (isVisibleToUser) {
                gifIndex = 1;
                new Thread(this::loadGif).start();
            } else {
                if (getActivity() != null) {
                    ((ImageView) getActivity().findViewById(R.id.gif_1)).setImageResource(R.drawable.ic_sync_black_24dp);
                    ((ImageView) getActivity().findViewById(R.id.gif_2)).setImageResource(R.drawable.ic_sync_black_24dp);
                    ((ImageView) getActivity().findViewById(R.id.gif_3)).setImageResource(R.drawable.ic_sync_black_24dp);
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
                String gifUrl = gifInfo[0];
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
                    URL gurl = new URL(gifUrl);
                    InputStream fis = gurl.openStream();
                    RandomAccessFile raf = new RandomAccessFile(gif, "rwd");
                    byte[] b = new byte[2096];int n;
                    while ((n = fis.read(b)) != -1) {
                        raf.write(b, 0, n);
                    }
                    raf.close();
                    handler.sendMessage(handler.obtainMessage(MSG_TYPE_DOWNLOADED, "下载完成"));
                } catch (Exception e) {
                    handler.sendMessage(handler.obtainMessage(MSG_TYPE_DOWNLOADED, "下载失败"));
                    e.printStackTrace();
                }
            }).start();

        }

        public void loadGif() {
            int startOffset = getGifOffset(gifIndex);
            Log.w("info", "loadGif:" + startOffset);
            String[] gifInfo = getGifInfo(startOffset);
            if (gifInfo != null) {
                Log.w("info", "loadGif:" + gifInfo[1]);
                gifUrl = gifInfo[0];
                txt = gifInfo[1];
                handler.sendMessage(handler.obtainMessage(MSG_TYPE_START, ""));
            }
        }

        private void loadGifList() {
            if (endFlg) {
                return;
            }
            lock.lock();
            Log.w("start", "loadGifList(lock):" + webPage);
            if (webPage == 1 && getNewFlg) {
                getNewArtId();
            }
            String baseUrl = "http://wap.gamersky.com/news/Content-" + artId;
            String reg = "alt=\".+\"[^>]*?src=\"(http://[^\"]+.gif)\"[^>]*>(<br>\\r\\n([^<]+))?</p>";
            Pattern pt = Pattern.compile(reg);
            try {
                String url = baseUrl + (webPage > 1 ? "_" + webPage : "") + ".html";
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
                }
                webPage++;
            } catch (Exception e) {
                endFlg = webPage > 27;
                e.printStackTrace();
            } finally {
                lock.unlock();
                Log.w("end", "loadGifList(unlock):end:" + webPage);
                System.out.println(gifList.toString());
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
                read.close();
                Matcher mt = pt.matcher(content.toString());
                if (mt.find()) {
                    title = mt.group(3);
                    artId = Integer.parseInt(mt.group(2));
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
                if(!endFlg) {
                    loadGifList();
                    return getGifInfo(offset);
                } else {
                    return null;
                }
            } else {
                return gifList.get(offset);
            }
        }

        private Bitmap getHttpGif(String url) {
            Bitmap bitmap;
            try {
                URL gurl = new URL(url);
                InputStream fis = gurl.openStream();
                bitmap = BitmapFactory.decodeStream(fis);
            } catch (Exception e) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_fingerprint_orange_24dp);
                e.printStackTrace();
            }
            return bitmap;
        }

        public void refresh() {
            this.gifIndex = 1;
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
            // Show 3 total pages.
            return 31;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            currentFragment = (PlaceholderFragment) object;
            super.setPrimaryItem(container, position, object);
        }
    }
}
