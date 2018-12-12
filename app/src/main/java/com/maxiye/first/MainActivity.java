package com.maxiye.first;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.DBHelper;
import com.maxiye.first.util.PermissionUtil;
import com.maxiye.first.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private final int INTENT_CONTACT_PICK_REQCODE = 100;
    private final int INTENT_IMG_VIEW_REQCODE = 101;
    private final int INTENT_IMG_PICK_REQCODE = 102;
    private final int INTENT_IMG_CAPTURE_REQCODE = 103;
    public static final int INTENT_PICK_DB_BAK_REQCODE = 104;
    private long last_press_time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleItt();//图片intent
    }

    @Override
    protected void onStop() {
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);//不再调节游戏或者音乐的音量
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
        int INTERVAL = 800;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long now = new Date().getTime();
                if (now - last_press_time < INTERVAL) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.test_action_share:
                share();
                return true;
            case R.id.test_action_net_set:
                addShortcut();
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
        switch (reqCode) {
            case INTENT_CONTACT_PICK_REQCODE:
                if (resCode == RESULT_OK) {
                    Uri contact = data.getData();
                    String[] proj = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                    assert contact != null;
                    Cursor cur = getContentResolver().query(contact, proj, null, null, null);
                    assert cur != null;
                    cur.moveToFirst();
                    String num = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    cur.close();
                    alert(num);
                } else {
                    alert("Res not ok");
                }
                break;
            case INTENT_IMG_VIEW_REQCODE:
                if (resCode == RESULT_OK) {
                    Uri picview = data.getData();
                    alert(picview != null ? picview.toString() : null);
                }
                break;
            case INTENT_IMG_PICK_REQCODE:
                if (resCode == RESULT_OK) {
                    Uri pic = data.getData();
                    try {
                        assert pic != null;
                        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(pic, "r");
                        if (pfd == null)
                            throw new FileNotFoundException();
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
                        //iv.setImageURI(pic);
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
                }
                break;
            case INTENT_IMG_CAPTURE_REQCODE:
                if (resCode == RESULT_OK && data != null) {
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
                }
                break;
            case INTENT_PICK_DB_BAK_REQCODE:
                if (resCode == RESULT_OK) {
                    Uri bak = data.getData();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            assert bak != null;//raw:/storage/emulated/0/Download/xxx.db.bak
                            Files.copy(Paths.get(bak.getLastPathSegment().substring(4)), getDatabasePath(DBHelper.DB_NAME).toPath(), StandardCopyOption.REPLACE_EXISTING);
                            alert("Success");
                        } catch (Exception e) {
                            e.printStackTrace();
                            alert("Error：" + e.getMessage());
                        }
                    }
                }
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

    //处理图片intent
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
                //noinspection ConstantConditions
                alert(itt.getExtras().get(Intent.EXTRA_TEXT).toString());
            }
            alert(itt.getType());
            Intent res = new Intent(getPackageName() + ".RESULT_ACTION", Uri.parse("content://result_uri"));
            setResult(Activity.RESULT_OK, res);
            finish();

        }
    }

    //创建数据库
    @SuppressWarnings("unused")
    public void createDB(View view) {
        DBHelper dbh = new DBHelper(this);
        SQLiteDatabase db = dbh.getWritableDatabase();//此时创建数据库,生成.db文件
        //增
        ContentValues ctv = new ContentValues();
        ctv.put("author", "zzz");
        ctv.put("price", 9.99f);
        ctv.put("pages", 180);
        ctv.put("name", "花儿与老年");
        long newId = db.insert(DBHelper.TB_BOOK, null, ctv);
        //删
        db.delete(DBHelper.TB_BOOK, "id = ?", new String[]{"2"});
        //改
        ctv.put("price", 16.09d);
        db.update(DBHelper.TB_BOOK, ctv, "id = ?", new String[]{"1"});
        //查
        //Cursor cus = db.rawQuery("select * from "+DBHelper.TB_BOOK+" where name = ? ", new String[]{"花儿与老年"});
        Cursor cus = db.query(DBHelper.TB_BOOK, new String[]{"*"}, "author = ?", new String[]{"zzz"}, null, null, "id desc");
        cus.moveToFirst();//必须，不然报错
        cus.close();
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
        //分享文件
        if (Util.isExternalStorageReadable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "1.jpg");
            try {
                Uri fileUri = FileProvider.getUriForFile(this, "com.maxiye.first.fileprovider", file);
                if (fileUri != null) {
                    /*Intent itt = new Intent(Intent.ACTION_VIEW,fileUri);//错误？不能直接使用fileUri
                    itt.setType(getContentResolver().getType(fileUri));*/
                    Intent itt = new Intent(Intent.ACTION_VIEW);
                    itt.setDataAndType(fileUri, getContentResolver().getType(fileUri));//"image/jpeg"也可
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
    private void addShortcut() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager scm = (ShortcutManager) getSystemService(SHORTCUT_SERVICE);
            Intent launcherIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);//设置网络页面intent
            ShortcutInfo si = new ShortcutInfo.Builder(this, "dataroam")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_perm_data_setting_black_24dp))
                    .setShortLabel("网络设置")
                    .setIntent(launcherIntent)
                    .build();
            assert scm != null;
            scm.requestPinShortcut(si, null);
        } else {
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
        }

    }

    public void testNFC(View view) {
        NfcAdapter nfc;
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC)) {
            alert("没有NFC功能");
        } else {
            nfc = NfcAdapter.getDefaultAdapter(this);
            nfc.setBeamPushUrisCallback(new FileUrisCallBack(), this);
            PermissionUtil.req(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PermissionUtil.PER_REQ_STORAGE_READ, () -> {

            });
        }

    }

    @SuppressWarnings("deprecation")
    public void testAudio(View view) {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor sp_e = sp.edit();
        boolean is_reg_mb = sp.getBoolean("ReceiveMediaBtn", false);
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        assert am != null;
        if (is_reg_mb) {
            am.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), MyReceiver.class.getName()));
            sp_e.putBoolean("ReceiveMediaBtn", false).apply();
            setVolumeControlStream(AudioManager.STREAM_RING);
        } else {
            am.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), MyReceiver.class.getName()));
            sp_e.putBoolean("ReceiveMediaBtn", true).commit();
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

    }

    /**
     * Called when pointer capture is enabled or disabled for the current window.
     *
     * @param hasCapture True if the window has pointer capture.
     */
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int item_id = item.getItemId();
        Intent intent;
        switch (item_id) {
            case R.id.test_call:
                PermissionUtil.req(this, new String[]{Manifest.permission.CALL_PHONE}, PermissionUtil.PER_REQ_CALL, () -> {
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
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, INTENT_IMG_PICK_REQCODE);
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
        getGifInt.putExtra(GifActivity.WEB_NAME, "gamersky");
        startActivity(getGifInt);
    }

    public void showDbPopup(View view) {
        PopupMenu pMenu = new PopupMenu(this, view);
        pMenu.getMenuInflater().inflate(R.menu.test_activity_db_popupmenu, pMenu.getMenu());
        pMenu.setOnMenuItemClickListener(item -> {
            int item_id = item.getItemId();
            switch (item_id) {
                case R.id.create_db:
                    createDB(view);
                    break;
                case R.id.backup_db:
                    DBHelper.backup(this);
                    break;
                case R.id.restore_db:
                    DBHelper.restore(this);
                    break;
                case R.id.scan_into_fav_db:
                    int count = new DBHelper(this).scanIntoFav();
                    alert(getString(R.string.scan_fav_tips, count));
                    break;
                case R.id.fix_fav_file:
                    int count2 = new DBHelper(this).fixFavFile();
                    alert(getString(R.string.fix_fav_tips, count2));
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
            Uri[] mFileUris = new Uri[10];
            File sendfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "1.jpg");
            boolean ret = sendfile.setReadable(true, true);
            if (ret) {
                Uri senduri = Uri.fromFile(sendfile);
                mFileUris[0] = senduri;
                alert(senduri.toString());
                return mFileUris;
            } else {
                return null;
            }
        }
    }

    private File createTempFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String img_name = "JPEG_" + timestamp + "_";
        File storedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(img_name, ".jpg", storedir);
    }

    public void capture(View view) {
        PermissionUtil.req(this, new String[]{Manifest.permission.CAMERA}, PermissionUtil.PER_REQ_CAPTURE, () -> {
            Intent cap = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cap.resolveActivity(getPackageManager()) != null) {
                File img_f;
                try {
                    img_f = createTempFile();
                    cap.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "com.maxiye.first.fileprovider", img_f));
                    startActivityForResult(cap, INTENT_IMG_CAPTURE_REQCODE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
