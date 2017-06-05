package com.maxiye.first;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static android.R.attr.name;

public class MainActivity extends AppCompatActivity implements BlankFragment.OnFrgActionListener,ActivityCompat.OnRequestPermissionsResultCallback {
    public final static String EXTRA_MESSAGE = "com.maxiye.first.MESSAGE";
    private final static int CONTACT_PICK_CODE = 100;
    private final static int PERMISSION_REQUEST_CALL = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handlePic();//图片intent
        EditText et = (EditText) findViewById(R.id.edit_message);
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keycode, KeyEvent ke) {
                if (keycode == KeyEvent.KEYCODE_ENTER) {//修改回车键功能
                    sendMsgNow(v);
                }
                return false;
            }
        });
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            BlankFragment frg = new BlankFragment();
            frg.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frg).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 为ActionBar扩展菜单项
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_setting:
                setting(new View(this));
                return true;
            case R.id.action_share:
                share();
                return true;
            case R.id.action_net_set:
                addShortcut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    //设置
    public void setting(View view) {
        Intent intent = new Intent(this, SettingActivity.class);
        EditText et = (EditText) findViewById(R.id.edit_message);
        String msg = et.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, msg);
        startActivity(intent);
    }
    public void share(){
        Intent itt = new Intent(Intent.ACTION_SEND);
        itt.setType("text/plain");
        itt.putExtra(Intent.EXTRA_TEXT,"哈哈，你好阿斯蒂芬sad");
        startActivity(itt.createChooser(itt,"选择文本发送到……"));
        /*itt.setType("image*//*");//未完成
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bim = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        bim.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        itt.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///storage/emulated/0/Download/1.jpg"));
        itt.putExtra(Intent.EXTRA_STREAM, baos.toByteArray());
        startActivity(itt);*/

    }
    //添加快捷方式
    private void addShortcut() {
        Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

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
                Intent.ShortcutIconResource.fromContext(MainActivity.this,
                        R.drawable.ic_perm_data_setting_black_24dp));

        // 设置关联程序
        Intent launcherIntent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);//设置网络页面intent

        addShortcutIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        // 发送广播
        sendBroadcast(addShortcutIntent);
    }

    //处理图片intent
    private void handlePic() {
        Intent itt = getIntent();
        if(itt.getAction() != "android.intent.action.MAIN"){
            Uri data = itt.getData();
            if (itt.getType().indexOf("image/") != -1) {
                Toast toast = Toast.makeText(this, data.toString(),Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                LinearLayout ll = (LinearLayout)toast.getView();
                ImageView iv = new ImageView(this);
                iv.setImageURI(data);
                ll.addView(iv,0);
                toast.show();
            }
            if (itt.getType().indexOf("text/plain") != -1){
                Toast.makeText(this, itt.getExtras().get(Intent.EXTRA_TEXT).toString(),Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, itt.getType(),Toast.LENGTH_SHORT).show();
            Intent res = new Intent(getPackageName()+".RESULT_ACTION", Uri.parse("content://result_uri"));
            setResult(Activity.RESULT_OK, res);
            finish();

        }
    }

    @Override
    //ListView项目点击事件，实现OnFrgActionListener接口
    public void onItemClick(View view) {
        TextView tv = (TextView) view;
        String str = tv.getText().toString().split("：")[1];
        ClipboardManager clm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clm.setPrimaryClip(ClipData.newPlainText(null, str));
        Toast.makeText(this, R.string.clip_toast, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onItemLongClick(View view){
        onItemClick(view);
        Uri url = Uri.parse("https://apkdownloader.com/");
        Intent itt = new Intent(Intent.ACTION_VIEW,url);
        startActivity(itt);
    }

    //查询应用
    public void sendMsgNow(View view) {
        BlankFragment frg = new BlankFragment();
        EditText et = (EditText) findViewById(R.id.edit_message);
        String msg = et.getText().toString();
        Bundle args = new Bundle();
        args.putString(BlankFragment.ARG_1, msg);
        frg.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frg).addToBackStack(null).commit();
    }
    //创建数据库
    public void createDB(View view) {
        DBHelper dbh = new DBHelper(this);
        SQLiteDatabase db = dbh.getWritableDatabase();//此时创建数据库,生成.db文件
        //增
        ContentValues ctv = new ContentValues();
        /*ctv.put("author","zzz");
        ctv.put("price",9.99f);
        ctv.put("pages",180);
        ctv.put("name","花儿与老年");
        long newId = db.insert(DBHelper.TB_BOOK,null,ctv);*/
        //删
        db.delete(DBHelper.TB_BOOK, "id = ?", new String[]{"2"});
        //改
        ctv.put("price", 16.09d);
        db.update(DBHelper.TB_BOOK, ctv, "id = ?", new String[]{"1"});
        //查
//        Cursor cus = db.rawQuery("select * from "+DBHelper.TB_BOOK+" where name = ? ", new String[]{"花儿与老年"});
        Cursor cus = db.query(DBHelper.TB_BOOK, new String[]{"*"}, "author = ?", new String[]{"zzz"}, null, null, "id desc");
        cus.moveToFirst();//必须，不然报错
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] pers, int[] res){
        switch (reqCode){
            case PERMISSION_REQUEST_CALL:{
                if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"权限已获取", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"权限被拒绝", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public void sentIntent(View view) {
        //打电话
        Uri tel = Uri.parse("tel:10086");
        Intent intent = new Intent(Intent.ACTION_CALL, tel);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CALL_PHONE)){
                Toast.makeText(this,"打电话当然要打电话的权限啊，扑街", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Called failed,no permission", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CALL_PHONE},PERMISSION_REQUEST_CALL);
            return;
        }else{
            startActivity(intent);
        }

        //打开网页
        /*Uri url = Uri.parse("https://apkdownloader.com/");
        Intent itt = new Intent(Intent.ACTION_VIEW,url);
        startActivity(itt);*/

        //打开地图
        /*Uri location = Uri.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");
        Intent itt = new Intent(Intent.ACTION_VIEW,location);
        startActivity(itt);*/

        //发邮件
        /*Intent itt = new Intent(Intent.ACTION_SEND);
        itt.setType("text/plain");
        itt.putExtra(Intent.EXTRA_EMAIL,new String[]{"912877398@qq.com"});
        itt.putExtra(Intent.EXTRA_SUBJECT,"你好");
        itt.putExtra(Intent.EXTRA_TEXT,"hahahaahahahahahaha");
        itt.putExtra(Intent.EXTRA_STREAM,Uri.parse("content://path/to/email/attachment"));
        PackageManager pm = getPackageManager();
        List<ResolveInfo> acts = pm.queryIntentActivities(itt,PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri:acts){
            Toast.makeText(this, ri.toString(), Toast.LENGTH_LONG).show();
        }
        startActivity(itt);*/

        //选择联系人
        /*Intent telitt = new Intent(Intent.ACTION_PICK,Uri.parse("content://contacts"));
        telitt.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(telitt,CONTACT_PICK_CODE);*/

        //选择图片
        /*Intent picitt = new Intent(Intent.ACTION_GET_CONTENT);
        picitt.setType("image*//*");
        startActivityForResult(picitt,111);*/
    }
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data){
        if (reqCode == CONTACT_PICK_CODE){
            if (resCode == RESULT_OK){
                Uri contact = data.getData();
                String[] proj = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                Cursor cur = getContentResolver().query(contact,proj,null,null,null);
                cur.moveToFirst();
                String num = cur.getString(cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Toast.makeText(this, num, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "res not ok", Toast.LENGTH_LONG).show();
            }
        }
    }
}
