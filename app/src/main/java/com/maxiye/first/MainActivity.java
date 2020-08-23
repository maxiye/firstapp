package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.maxiye.first.part.PageListPopupWindow;
import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.BitmapUtil;
import com.maxiye.first.util.DbHelper;
import com.maxiye.first.util.MyLog;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.StringUtil;
import com.maxiye.first.util.Util;
import com.maxiye.first.util.WebdavUtil;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;

/**
 * @author due
 * // @SuppressWarnings({"unused", "WeakerAccess"})  SameParameterValue unchecked FieldCanBeLocal deprecation(过时)   UnusedParameters  InflateParams
 */
@SuppressWarnings("AlibabaRemoveCommentedCode")
public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private final int INTENT_CONTACT_PICK_REQCODE = 100;
    private final int INTENT_IMG_VIEW_REQCODE = 101;
    private final int INTENT_IMG_PICK_REQCODE = 102;
    private final int INTENT_IMG_CAPTURE_REQCODE = 103;
    public static final int INTENT_PICK_DB_BAK_REQCODE = 104;
    private static final int INTENT_IMG_GRAY_REQCODE = 105;
    private static final int INTENT_IMG_DOT_REQCODE = 106;
    private static final int INTENT_IMG_REVERSE_REQCODE = 107;
    private static final int INTENT_IMG_DOT_TXT_REQCODE = 108;
    private static final int INTENT_IMG_SINGLE_CHAN_REQCODE = 109;
    private static final int INTENT_IMG_DOT_EX_REQCODE = 110;
    private long lastPressTime = 0;
    public static Context contextOfApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextOfApplication = getApplicationContext();
        setContentView(R.layout.activity_main);
        /* if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
        } */
        handleItt();//图片intent
//        startActivity(new Intent(this, GifActivity.class));
    }

    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

        @Override
    protected void onStop() {
        // 不再调节游戏或者音乐的音量
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 为ActionBar扩展菜单项
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long now = Instant.now().getEpochSecond();
            if (now - lastPressTime < 800) {
                finish();
            } else {
                lastPressTime = now;
                alert(getString(R.string.alert_exit));
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.test_action_share:
                share();
                return true;
            case R.id.test_action_net_set:
                addNetSetShortcut();
            case R.id.proxy_shortcut:
                addProxyShortcut();
                return true;
            case R.id.main_action_setting:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] pers, @NonNull int[] res) {
        PermissionUtil.res(this, reqCode, pers, res);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode != RESULT_OK || data == null) {
            alert("bad res!");
            return;
        }
        switch (reqCode) {
            case INTENT_CONTACT_PICK_REQCODE: {
                Uri contact = data.getData();
                String[] proj = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                assert contact != null;
                Cursor cur = getContentResolver().query(contact, proj, null, null, null);
                assert cur != null;
                cur.moveToFirst();
                String num = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                cur.close();
                alert(num);
            }
            break;
            case INTENT_IMG_VIEW_REQCODE:
                Uri picview = data.getData();
                alert(picview != null ? picview.toString() : null);
                break;
            case INTENT_IMG_PICK_REQCODE:
                Uri pic = data.getData();
                try {
                    assert pic != null;
                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(pic, "r");
                    if (pfd == null) {
                        throw new FileNotFoundException();
                    }
                    Bitmap bm = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                    Cursor cur = getContentResolver().query(pic, null, null, null, null);
                    assert cur != null;
                    cur.moveToFirst();
                    String fname = cur.getString(cur.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                    long flen = cur.getLong(cur.getColumnIndexOrThrow(OpenableColumns.SIZE));
                    cur.close();
                    Toast toast = Toast.makeText(this, fname + "(" + flen + "bytes)===>" + data.getData().toString(), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    ImageView iv = new ImageView(this);
                    iv.setImageBitmap(bm);
                    LinearLayout ll = (LinearLayout) toast.getView();
                    ll.addView(iv, 0);
                    toast.show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                    /*Intent itt = new Intent(Intent.ACTION_VIEW);
                    itt.setDataAndType(pic, getContentResolver().getType(pic));//"image/jpeg"也可
                    itt.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//ACTION_GET_CONTENT和ES打开必须设置
                    startActivityForResult(itt.createChooser(itt, "Open img"), INTENT_IMG_VIEW_REQCODE);*/
                break;
            case INTENT_IMG_CAPTURE_REQCODE:
                Bundle bdl = data.getExtras();
                assert bdl != null;
                Bitmap bmp = (Bitmap) bdl.get("data");
                Toast toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                LinearLayout ll = (LinearLayout) toast.getView();
                ImageView im = new ImageView(this);
                im.setImageBitmap(bmp);
                ll.addView(im);
                toast.show();
                break;
            case INTENT_PICK_DB_BAK_REQCODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        String path = Util.getPathFromIntent(data);
                        // raw:/storage/emulated/0/Download/xxx.db.bak
                        DbHelper.restore(this, Paths.get(path));
                        alert("Success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        alert("Error：" + e.getMessage());
                    }
                }
                break;
            case INTENT_IMG_GRAY_REQCODE:
                showBitmap(data, BitmapUtil::convertGray, "gray");
                break;
            case INTENT_IMG_DOT_REQCODE:
                showBitmap(data, BitmapUtil::convertDot, "dot");
                break;
            case INTENT_IMG_REVERSE_REQCODE:
                showBitmap(data, BitmapUtil::convertReverse, "reverse");
                break;
            case INTENT_IMG_DOT_TXT_REQCODE:
                saveDotTxt(data);
                break;
            case INTENT_IMG_SINGLE_CHAN_REQCODE:
                showBitmap(data, BitmapUtil::convertSingleChannel, "single-chan");
                break;
            case INTENT_IMG_DOT_EX_REQCODE:
                showBitmap(data, BitmapUtil::convertComic, "dot-ex");
                break;
            default:
                break;
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

    public void openApplist(View view) {
        startActivity(new Intent(this, ApplistActivity.class));
    }

    public void sentIntent(View view) {
        PopupMenu pMenu = new PopupMenu(this, view);
        pMenu.getMenuInflater().inflate(R.menu.test_activity_intent_popupmenu, pMenu.getMenu());
        pMenu.setOnMenuItemClickListener(this);
        pMenu.show();
    }

    /**
     * 处理图片intent
     */
    private void handleItt() {
        Intent itt = getIntent();
        if (itt.getType() != null) {
            Uri data = itt.getData();
            if (itt.getType().contains("image/")) {
                assert data != null;
                Toast toast = Toast.makeText(this, data.toString(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                LinearLayout ll = (LinearLayout) toast.getView();
                ImageView iv = new ImageView(this);
                iv.setImageURI(data);
                ll.addView(iv, 0);
                toast.show();
            }
            if (itt.getType().contains("text/plain")) {
                // noinspection ConstantConditions
                alert(itt.getExtras().get(Intent.EXTRA_TEXT).toString());
            }
            alert(itt.getType());
            Intent res = new Intent(getPackageName() + ".RESULT_ACTION", Uri.parse("content://result_uri"));
            setResult(Activity.RESULT_OK, res);
            finish();

        }
    }

    /**
     * 创建数据库
     */
    @SuppressWarnings("unused")
    private void createDB() {
        // 此时创建数据库,生成.db文件
        SQLiteDatabase db = DbHelper.newDb(this);
        //增
        ContentValues ctv = new ContentValues();
        ctv.put("author", "zzz");
        ctv.put("price", 9.99f);
        ctv.put("pages", 180);
        ctv.put("name", "花儿与老年");
        long newId = db.insert(DbHelper.TB_BOOK, null, ctv);
        //删
        db.delete(DbHelper.TB_BOOK, "id = ?", new String[]{"2"});
        //改
        ctv.put("price", 16.09d);
        db.update(DbHelper.TB_BOOK, ctv, "id = ?", new String[]{"1"});
        //查
        //Cursor cus = db.rawQuery("select * from "+DbHelper.TB_BOOK+" where name = ? ", new String[]{"花儿与老年"});
        Cursor cus = db.query(DbHelper.TB_BOOK, new String[]{"*"}, "author = ?", new String[]{"zzz"}, null, null, "id desc");
        cus.moveToFirst();//必须，不然报错
        cus.close();
        db.close();
    }

    private void share() {
        //分享文本
        /*Intent itt = new Intent(Intent.ACTION_SEND);
        itt.setType("text/plain");
        itt.putExtra(Intent.EXTRA_TEXT, "哈哈，你好阿斯蒂芬sad");
        startActivity(itt.createChooser(itt, "选择文本发送到……"));*/
        /*Intent itt= new Intent(Intent.ACTION_VIEW);
        itt.setType("image");//targetSDK25禁止直接对fileURI共享
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bim = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        bim.compress(Bitmap.CompressFormat.PNG, 100, baos);
        //itt.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///storage/emulated/0/Download/1.jpg"));
        itt.putExtra(Intent.EXTRA_STREAM, baos.toByteArray());
        startActivity(itt);*/
        // 分享文件
        if (Util.isExternalStorageReadable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "1.jpg");
            try {
                Uri fileUri = FileProvider.getUriForFile(this, "com.maxiye.first.fileprovider", file);
                if (fileUri != null) {
                    /*Intent itt = new Intent(Intent.ACTION_VIEW,fileUri);//错误？不能直接使用fileUri
                    itt.setType(getContentResolver().getType(fileUri));*/
                    Intent itt = new Intent(Intent.ACTION_VIEW);
                    // "image/jpeg"也可
                    itt.setDataAndType(fileUri, getContentResolver().getType(fileUri));
                    itt.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(Intent.createChooser(itt, "Open img"), INTENT_IMG_VIEW_REQCODE);
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                alert("文件获取错误");
            }
        } else {
            alert("需要外部存储读取权限");
        }

    }

    //添加快捷方式
    private void addNetSetShortcut() {
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");//"com.android.launcher.action.INSTALL_SHORTCUT"
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
            // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
            // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
            // 屏幕上没有空间时会提示
            // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式

            // 名字
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "网络设置");
            // 图标
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_perm_data_setting_black_24dp));

            // 设置关联程序
            Intent launcherIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);//设置网络页面intent
            // 设置关联程序
            //        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            //        launcherIntent.setClass(ApplistActivity.this, ApplistActivity.class);
            //        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

            // 发送广播
            sendBroadcast(addShortcutIntent);
        }*/
        ShortcutManager scm = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
        // 设置网络页面intent
        Intent launcherIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        ShortcutInfo si = new ShortcutInfo.Builder(this, "dataroam")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_perm_data_setting_black_24dp))
                .setShortLabel("网络设置")
                .setIntent(launcherIntent)
                .build();
        assert scm != null;
        scm.requestPinShortcut(si, null);

    }

    private void addProxyShortcut() {
        ShortcutManager scm = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
        // 设置网络页面intent
        Intent launcherIntent = new Intent(this, CommonActivity.class);
        launcherIntent.putExtra(CommonActivity.TYPE, CommonActivity.TYPE_PROXY);
        launcherIntent.setAction(Intent.ACTION_VIEW);
        ShortcutInfo si = new ShortcutInfo.Builder(this, "proxy_setting")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_near_me_black_24dp))
                .setShortLabel("代理设置")
                .setIntent(launcherIntent)
                .build();
        assert scm != null;
        scm.requestPinShortcut(si, null);
    }

    public void testNFC(View view) {
        NfcAdapter nfc;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            alert("没有NFC功能");
        } else {
            nfc = NfcAdapter.getDefaultAdapter(this);
            nfc.setBeamPushUrisCallback(new FileUrisCallBack(), this);
            PermissionUtil.req(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionUtil.RequestCode.STORAGE_READ, (result) -> {});
        }

    }

    @SuppressWarnings("deprecation")
    public void testAudio(View view) {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();
        boolean isRegistered = sp.getBoolean("ReceiveMediaBtn", false);
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        assert am != null;
        if (isRegistered) {
            am.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), MyReceiver.class.getName()));
            spEditor.putBoolean("ReceiveMediaBtn", false).apply();
            setVolumeControlStream(AudioManager.STREAM_RING);
        } else {
            am.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), MyReceiver.class.getName()));
            spEditor.putBoolean("ReceiveMediaBtn", true).commit();
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

    }

    /**
     * Called when pointer capture is enabled or disabled for the progress window.
     *
     * @param hasCapture True if the window has pointer capture.
     */
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.test_call:
                PermissionUtil.req(this, new String[]{Manifest.permission.CALL_PHONE}, PermissionUtil.RequestCode.CALL, (result) -> {
                    Uri tel = Uri.parse("tel:10086");
                    startActivity(new Intent(Intent.ACTION_CALL, tel));
                });
                break;
            case R.id.test_contact:
                intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, INTENT_CONTACT_PICK_REQCODE);
                break;
            case R.id.test_email:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"912877398@qq.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "你好");
                intent.putExtra(Intent.EXTRA_TEXT, "hahahaahahahahahaha");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://path/to/email/attachment"));
                /*PackageManager pm = getPackageManager();
                List<ResolveInfo> acts = pm.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
                acts.forEach(ri -> alert(ri.toString()));*/
                startActivity(intent);
                break;
            case R.id.test_map:
                Uri location = Uri.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");
                intent = new Intent(Intent.ACTION_VIEW, location);
                startActivity(intent);
                break;
            case R.id.test_pic:
                startBitmapActivity(INTENT_IMG_PICK_REQCODE);
                /*Intent picitt = new Intent(Intent.ACTION_PICK);
                picitt.setType("image/*");
                startActivityForResult(picitt.createChooser(picitt,"选择图片"),INTENT_IMG_PICK_REQCODE);*/
                break;
            case R.id.test_web:
                Uri url = Uri.parse("https://apkdownloader.com/");
                intent = new Intent(Intent.ACTION_VIEW, url);
                startActivity(intent);
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * 获取动态图
     *
     * @param view View
     */
    public void getGif(View view) {
        Intent getGifInt = new Intent(this, GifActivity.class);
        getGifInt.putExtra(GifActivity.WEB_NAME_ARG, "gamersky");
        startActivity(getGifInt);
    }

    /**
     * 数据库操作menu
     * @param view View
     */
    public void showDbPopup(View view) {
        PopupMenu pMenu = new PopupMenu(this, view);
        pMenu.getMenuInflater().inflate(R.menu.test_activity_db_popupmenu, pMenu.getMenu());
        pMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.create_db:
                    createDB();
                    break;
                case R.id.backup_db:
                    Util.getDefaultSingleThreadExecutor().execute(() -> DbHelper.backup(this));
                    break;
                case R.id.restore_db:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.choose_a_backup)), INTENT_PICK_DB_BAK_REQCODE);
                    break;
                case R.id.restore_db_from_net:
                    restoreDbFromWebDav();
                    break;
                case R.id.scan_into_fav_db:
                    int count = new DbHelper(this).scanIntoFav();
                    alert(getResources().getQuantityString(R.plurals.scan_fav_tips, count));
                    break;
                case R.id.fix_fav_file:
                    int count2 = new DbHelper(this).fixFavFile();
                    alert(getResources().getQuantityString(R.plurals.fix_fav_tips, count2));
                    break;
                default:
                    break;
            }
            return false;
        });
        pMenu.show();
    }

    /**
     * 从webdav恢复db
     */
    private void restoreDbFromWebDav() {
        Util.loading(this);
        Util.getDefaultSingleThreadExecutor().execute(() -> {
            WebdavUtil webdav = new WebdavUtil(this);
            List<Map<String, Object>> lists = webdav.list(WebdavUtil.BASE_URL);
            if (lists == null || lists.isEmpty()) {
                runOnUiThread(() -> {
                    Util.loaded();
                    alert(getString(R.string.fail) + ": " + webdav.getError());
                });
                return;
            }
            Drawable icon = getDrawable(R.drawable.ic_insert_drive_file_black_24dp);
            final List<Map<String, Object>>  files = lists.stream()
                    .filter(item -> "file".equals(item.get("type").toString()))
                    .sorted((f1, f2) -> -f1.get("last_modified").toString().compareTo(f2.get("last_modified").toString()))
                    .peek(item -> {
                        String name = item.get("name").toString();
                        String lastModified = item.get("last_modified_format").toString();
                        String size = item.get("length").toString();
                        item.put("fname", name);
                        item.put("name", "<span style='color: #00796b;'>" + name + "</span><br/><span style='color: #969696;'>" + size + "  " + lastModified + "</span>");
                        item.put("icon", icon);
                    })
                    .collect(Collectors.toList());
            MyLog.w("Webdav list", files.toString());
            runOnUiThread(() -> {
                Util.loaded();
                PopupWindow pageWindow = new PageListPopupWindow.Builder(this)
                        .setListCountGetter(where -> files.size())
                        .setListGetter((page, list1, where) -> files)
                        .setItemClickListener(this::dbRestoreActions)
                        .setItemLongClickListener((pageListPopupWindow, position) -> false)
                        .setPageSize(files.size())
                        .setWindowHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                        .setMask(findViewById(R.id.main_activity_view))
                        .build();
                pageWindow.showAtLocation(findViewById(R.id.main_activity_view), Gravity.BOTTOM, 0, 0);
            });
        });
    }

    /**
     * webdab操作
     * @param popupWindow 窗口
     * @param position 位置
     */
    private void dbRestoreActions(PageListPopupWindow popupWindow, int position) {
        ListPopupWindow listMenu = new ListPopupWindow(this);
        listMenu.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new
                String[]{getString(R.string.restore_db), getString(R.string.delete)}));
        listMenu.setAnchorView(popupWindow.getItemView(position));
        listMenu.setWidth(450);
        listMenu.setOnItemClickListener((parent, view1, position1, id) -> {
            final Map<String, Object> item = popupWindow.getItemData(position);
            switch (position1) {
                case 0:
                    Util.confirmDo(this, () -> {
                        Util.loading(this);
                        Util.getDefaultSingleThreadExecutor().execute(() -> {
                            String path = item.get("path").toString();
                            String res;
                            try {
                                Path backFile = Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/maxiye/", item.get("fname").toString());
                                if (Files.notExists(backFile.getParent())) {
                                    Files.createDirectories(backFile.getParent());
                                }
                                if (Files.notExists(backFile)) {
                                    Files.createFile(backFile);
                                }
                                ResponseBody responseBody = new WebdavUtil(this).get(WebdavUtil.HOST + path);
                                Files.write(backFile, responseBody.bytes());
                                DbHelper.restore(this, backFile);
                                res = getString(R.string.success) + ": " + backFile.toString();
                            } catch (Exception e) {
                                res = e.getLocalizedMessage();
                                e.printStackTrace();
                            }
                            String finalRes = res;
                            runOnUiThread(() -> {
                                Util.loaded();
                                alert(finalRes);
                            });
                        });
                    });
                    break;
                case 1:
                    Util.confirmDo(this, () -> Util.getDefaultSingleThreadExecutor().execute(() -> {
                        String path = item.get("path").toString();
                        final boolean res = new WebdavUtil(this).delete(WebdavUtil.HOST + path);
                        runOnUiThread(() -> {
                            popupWindow.remove(position);
                            alert(getString(res ? R.string.success : R.string.error_tip));
                        });
                    }));
                    break;
                default:
                    break;
            }
            listMenu.dismiss();
        });
        listMenu.show();
    }

    /**
     * bitmap操作menu
     * @param view View
     */
    public void showBitmapPopup(View view) {
        PopupMenu pMenu = new PopupMenu(this, view);
        pMenu.getMenuInflater().inflate(R.menu.main_activity_bitmap_popupmenu, Util.enableMenuIcon(pMenu.getMenu()));
        pMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.gradual_color:
                    genGradualColor();
                    break;
                case R.id.gray_bitmap:
                    startBitmapActivity(INTENT_IMG_GRAY_REQCODE);
                    break;
                case R.id.dot_bitmap:
                    startBitmapActivity(INTENT_IMG_DOT_REQCODE);
                    break;
                case R.id.reverse_bitmap:
                    startBitmapActivity(INTENT_IMG_REVERSE_REQCODE);
                    break;
                case R.id.single_chan_bitmap:
                    startBitmapActivity(INTENT_IMG_SINGLE_CHAN_REQCODE);
                    break;
                case R.id.dot_bitmap_txt:
                    startBitmapActivity(INTENT_IMG_DOT_TXT_REQCODE);
                    break;
                case R.id.dot_bitmap_comic:
                    startBitmapActivity(INTENT_IMG_DOT_EX_REQCODE);
                    break;
                default:
                    break;
            }
            return false;
        });
        pMenu.show();
    }

    public void testApi(View view) {
        ApiUtil.showPopupmenu(this, view);
    }

    private class FileUrisCallBack implements NfcAdapter.CreateBeamUrisCallback {

        @Override
        public Uri[] createBeamUris(NfcEvent event) {
            File sendfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "1.jpg");
            if (sendfile.setReadable(true, true)) {
                Uri[] mFileUris = new Uri[2];
                Uri senduri = Uri.fromFile(sendfile);
                mFileUris[0] = senduri;
                alert(senduri.toString());
                return mFileUris;
            } else {
                return new Uri[0];
            }
        }
    }

    @NonNull
    private File createTempFile() throws IOException {
        String timestamp = DateTimeFormatter.ofPattern("YYYYMMdd_HHmmss").format(LocalDateTime.now());
        String imgName = "JPEG_" + timestamp + "_";
        File storedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imgName, ".jpg", storedir);
    }

    public void capture(View view) {
        PermissionUtil.req(this, new String[]{Manifest.permission.CAMERA}, PermissionUtil.RequestCode.CAPTURE, (result) -> {
            Intent cap = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cap.resolveActivity(getPackageManager()) != null) {
                try {
                    File imgFile = createTempFile();
                    cap.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "com.maxiye.first.fileprovider", imgFile));
                    startActivityForResult(cap, INTENT_IMG_CAPTURE_REQCODE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * {@code 第59条：熟悉并使用Java类库}
     * 从Java 7开始，就不应再使用{@link java.util.Random}了。
     * 对于大多数用途，选择的随机数生成器现在是{@link ThreadLocalRandom}。 它产生更高质量的随机数，而且速度非常快。 在我的机器上，它比Random快3.6倍。
     * 对于fork-join池和并行流的应用，请使用{@link java.util.SplittableRandom}。
     */
    public void genGradualColor() {
        //实例化布局
        // attachToRoot true : The specified child already has a parent. You must call removeView() on the child's parent first.
        View view2 = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, findViewById(R.id.main_activity_view), false);
        EditText editText = view2.findViewById(R.id.dialog_input);
        editText.setHint("13ba94 1080*1920");
        //创建对话框
        AlertDialog dialog2 = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.gradual_color)
                // 添加布局
                .setView(view2)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> {
                    ImageView imgView = BitmapUtil.loadImg(this);
                    new Thread(() -> {
                        String txt = editText.getText().toString();
                        int color, w, h;
                        try {
                            if (StringUtil.notBlank(txt)) {
                                char hashPrefix = '#';
                                if (txt.charAt(0) == hashPrefix) {
                                    txt = txt.substring(1);
                                }
                                String sizeSeparator = " ";
                                if (txt.contains(sizeSeparator)) {
                                    String[] set = txt.split(" ");
                                    color = Integer.parseInt(set[0], 16);
                                    String[] wh = set[1].split("\\*");
                                    w = Integer.parseInt(wh[0]);
                                    h = Integer.parseInt(wh[1]);
                                } else {
                                    color = Integer.parseInt(txt, 16);
                                    w = 1080;
                                    h = 1920;
                                }
                            } else {
                                color = 0x13ba94; w = 1080; h = 1920;
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> alert(e.getLocalizedMessage()));
                            e.printStackTrace();
                            return;
                        }
                        Bitmap bitmap = BitmapUtil.gradualBitmap(color, w, h);
                        imgView.setOnLongClickListener(v -> {
                            String fName = "#" + Integer.toHexString(color) + "_" + w + "×" + h + "_" + ThreadLocalRandom.current().nextInt() + ".png";
                            File img = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/gradual/" + fName);
                            BitmapUtil.saveBitmap(this, img, bitmap);
                            return false;
                        });
                        runOnUiThread(() -> {
                            imgView.clearAnimation();
                            imgView.setImageDrawable(new BitmapDrawable(null, bitmap));
                            imgView.setMinimumHeight(bitmap.getHeight() << 1);
                            imgView.setMinimumWidth(bitmap.getWidth() << 1);
                        });
                    }).start();
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog2.show();
        Objects.requireNonNull(dialog2.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    /**
     * 图片选择intent
     * @param reqCode int
     */
    private void startBitmapActivity(int reqCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, reqCode);
    }

    /**
     * {@code 第44条：优先使用标准的函数式接口}
     * java.util.function包提供了大量标准函数式接口供你使用。
     * 如果其中一个标准函数式接口完成这项工作，则通常应该优先使用它，而不是专门构建的函数式接口。
     * 这将使你的API更容易学习，通过减少其不必要概念，并将提供重要的互操作性好处，因为许多标准函数式接口提供了有用的默认方法。
     * 例如，Predicate接口提供了组合判断的方法。 在我们的LinkedHashMap示例中，标准的BiPredicate<Map<K,V>, Map.Entry<K,V>>接口应优先于自定义的EldestEntryRemovalFunction接口的使用。
     * @param data Intent
     * @param handler UnaryOperator
     * @param tag 类型标签
     */
    private void showBitmap(Intent data, UnaryOperator<Bitmap> handler, String tag) {
        FileDescriptor fd = Util.getFileDescriptor(this, data);
        BitmapUtil.showBitmap4Save(this, BitmapFactory.decodeFileDescriptor(fd), handler, tag);
    }

    private void saveDotTxt(Intent data) {
        FileDescriptor fd = Util.getFileDescriptor(this, data);
        String dotTxt = BitmapUtil.convertDotTxt(BitmapFactory.decodeFileDescriptor(fd));
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "bitmap_txt/" + UUID.randomUUID().toString() + "_dot.txt");
        Util.saveFileEx(file, dotTxt);
        alert(getString(R.string.success));
    }

    public void proxySetting(View view) {
        Intent proxy = new Intent(this, CommonActivity.class);
        proxy.putExtra(CommonActivity.TYPE, CommonActivity.TYPE_PROXY);
        startActivity(proxy);
    }
}
