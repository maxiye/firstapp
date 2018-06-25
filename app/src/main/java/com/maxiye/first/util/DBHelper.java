package com.maxiye.first.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.maxiye.first.SettingActivity;
import com.maxiye.first.TestActivity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**数据库助手
 * Created by Administrator on 2017-05-25.
 */

public class DBHelper extends SQLiteOpenHelper {
    public final static String DB_NAME = "first.db";
    private final static int DB_VERSION = 4;
    public final static String TB_BOOK = "book";
    public final static String TB_IMG_WEB = "img_web";
    public final static String TB_IMG_WEB_ITEM = "img_web_item";
    private final static String CREATE_BOOK = "create table " + TB_BOOK + "("
            + "id integer primary key autoincrement,"
            + "author text, "
            + "price real, "
            + "pages integer, "
            + "name text)";
    private final static String CREATE_IMG_WEB = "create table " + TB_IMG_WEB + "("
            + "id integer primary key autoincrement,"
            + "web_name text, "
            + "type text, "//gif|bitmap
            + "art_id integer, "
            + "web_url text, "
            + "title text, "
            + "pages integer, "
            + "time text)";
    //索引名不能相同
    private final static String INDEX_IMG_WEB = "CREATE INDEX IF NOT EXISTS web_idx_art_id on " + TB_IMG_WEB + " (art_id, web_name)";
    private final static String CREATE_IMG_WEB_ITEM = "create table " + TB_IMG_WEB_ITEM + "("
            + "id integer primary key autoincrement,"
            + "web_name text, "
            + "type text, "
            + "art_id integer , "
            + "page integer , "
            + "title text, "
            + "url text, "
            + "ext text)";//gif|jpg|jpeg|bmp|png
    private final static String INDEX_IMG_WEB_ITEM = "CREATE INDEX IF NOT EXISTS web_item_idx_art_id on " + TB_IMG_WEB_ITEM + " (art_id, web_name)";
    private static final String DROP_BOOK = "DROP TABLE IF EXISTS " + TB_BOOK;
    private static final String DROP_IMG_WEB = "DROP TABLE IF EXISTS " + TB_IMG_WEB;
    private static final String DROP_IMG_WEB_ITEM = "DROP TABLE IF EXISTS " + TB_IMG_WEB_ITEM;
    private final Context mCont;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mCont = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//只要没.db文件时才执行
        db.execSQL(CREATE_BOOK);
        db.execSQL(CREATE_IMG_WEB);
        db.execSQL(INDEX_IMG_WEB);
        db.execSQL(CREATE_IMG_WEB_ITEM);
        db.execSQL(INDEX_IMG_WEB_ITEM);
        Toast.makeText(mCont, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) update1to2(db);
        if (oldVersion == 2) update2to3(db);
        if (oldVersion == 3) update3to4(db);
        Toast.makeText(mCont, "Update succeeded", Toast.LENGTH_SHORT).show();
    }

    private void update1to2(@NonNull SQLiteDatabase db) {
        Cursor cur1 = db.rawQuery("select * from gif_web where art_id > 1000000", new String[]{});
        Cursor cur2 = db.rawQuery("select * from gif_web_item where art_id > 1000000", new String[]{});
        Log.w("dd", cur1.getCount() + "");
        Log.w("dd", cur2.getCount() + "");
        db.execSQL(DROP_BOOK);
        db.execSQL(DROP_IMG_WEB);
        db.execSQL(DROP_IMG_WEB_ITEM);
        onCreate(db);
        cur1.moveToFirst();
        for (int i = 0; i < cur1.getCount(); i++) {
            ContentValues ctv = new ContentValues();
            ctv.put("art_id", cur1.getInt(cur1.getColumnIndex("art_id")));
            ctv.put("pages", cur1.getInt(cur1.getColumnIndex("pages")));
            ctv.put("web_name", "gamersky");
            ctv.put("web_url", cur1.getString(cur1.getColumnIndex("web_url")));
            ctv.put("title", cur1.getString(cur1.getColumnIndex("title")));
            ctv.put("time", cur1.getString(cur1.getColumnIndex("time")));
            long newId = db.insert(TB_IMG_WEB, null, ctv);
            Log.w("dd", newId + "");
            cur1.moveToNext();
        }
        cur1.close();
        cur2.moveToFirst();
        for (int i = 0; i < cur2.getCount(); i++) {
            ContentValues ctv = new ContentValues();
            ctv.put("art_id", cur2.getInt(cur2.getColumnIndex("art_id")));
            ctv.put("page", cur2.getInt(cur2.getColumnIndex("page")));
            ctv.put("web_name", "gamersky");
            ctv.put("url", cur2.getString(cur2.getColumnIndex("url")));
            ctv.put("title", cur2.getString(cur2.getColumnIndex("title")));
            long newId = db.insert(TB_IMG_WEB_ITEM, null, ctv);
            Log.w("dd", newId + "");
            cur2.moveToNext();
        }
        cur2.close();
    }
    private void update2to3(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE gif_web RENAME TO img_web;");
        db.execSQL("ALTER TABLE img_web ADD COLUMN type text;");
        db.execSQL("UPDATE img_web SET type = 'gif' WHERE art_id > 0;");
        db.execSQL("ALTER TABLE gif_web_item RENAME TO img_web_item;");
        db.execSQL("ALTER TABLE img_web_item ADD COLUMN type text;");
        db.execSQL("ALTER TABLE img_web_item ADD COLUMN ext text;");
        db.execSQL("UPDATE img_web_item SET type = 'gif' WHERE art_id > 0;");
        db.execSQL("UPDATE img_web_item SET ext = '.gif' WHERE art_id > 0;");
    }

    private void update3to4(@NonNull SQLiteDatabase db) {
        db.execSQL(INDEX_IMG_WEB);
        db.execSQL(INDEX_IMG_WEB_ITEM);
    }
    public static void backup(Activity activity) {
        File db = activity.getDatabasePath(DB_NAME);
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        int i = 0;File bak;
        do {
            String fileName = DB_NAME + ".bak" + (i > 0 ? "_" + i : "");
            i++;
            bak = new File(downloadDir, fileName);
        } while (bak.exists());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Files.copy(db.toPath(), bak.toPath());
                Toast.makeText(activity, "Success：" + bak.getPath(), Toast.LENGTH_SHORT).show();
                activity.getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit().putLong(SettingActivity.BACKUP_TIME, System.currentTimeMillis()).apply();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(activity, "Error：" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void restore(TestActivity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        activity.startActivityForResult(Intent.createChooser(intent,"选择备份文件"), TestActivity.INTENT_PICK_DB_BAK_REQCODE);
    }

    public static void checkBakup(Activity activity) {
        long lastBackupTime = activity.getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).getLong(SettingActivity.BACKUP_TIME, 0);
        if (System.currentTimeMillis() - lastBackupTime > 86400 * 5 * 1000) {
            backup(activity);
        }
    }
}
