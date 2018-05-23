package com.maxiye.first.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

/**数据库助手
 * Created by Administrator on 2017-05-25.
 */

public class DBHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "first.db";
    private final static int DB_VERSION = 3;
    public final static String TB_BOOK = "book";
    public final static String TB_IMG_WEB = "img_web";
    public final static String TB_IMG_WEB_ITEM = "img_web_item";
    private final static String CREATE_BOOK = "create table " + TB_BOOK + "("
            + "id integer primary key autoincrement,"
            + "author text, "
            + "price real, "
            + "pages integer, "
            + "name text)";
    private final static String CREATE_GIF_WEB = "create table " + TB_IMG_WEB + "("
            + "id integer primary key autoincrement,"
            + "web_name text, "
            + "type text, "//gif|bitmap
            + "art_id integer, "
            + "web_url text, "
            + "title text, "
            + "pages integer, "
            + "time text);"
            + "CREATE INDEX idx_art_id on " + TB_IMG_WEB
            + " (art_id);";
    private final static String CREATE_GIF_WEB_ITEM = "create table " + TB_IMG_WEB_ITEM + "("
            + "id integer primary key autoincrement,"
            + "web_name text, "
            + "type text, "
            + "art_id integer , "
            + "page integer , "
            + "title text, "
            + "url text, "
            + "ext text);"//gif|jpg|jpeg|bmp|png
            + "CREATE INDEX idx_art_id on " + TB_IMG_WEB_ITEM
            + " (art_id);";
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
        db.execSQL(CREATE_GIF_WEB);
        db.execSQL(CREATE_GIF_WEB_ITEM);
        Toast.makeText(mCont, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) update1to2(db);
        if (oldVersion == 2) update2to3(db);
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
}
