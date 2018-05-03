package com.maxiye.first;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**数据库助手
 * Created by Administrator on 2017-05-25.
 */

class DBHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "first.db";
    private final static int DB_VERSION = 1;
    final static String TB_BOOK = "book";
    final static String TB_GIF_WEB = "gif_web";
    final static String TB_GIF_WEB_ITEM = "gif_web_item";
    private final static String CREATE_BOOK = "create table " + TB_BOOK + "("
            + "id integer primary key autoincrement,"
            + "author text, "
            + "price real, "
            + "pages integer, "
            + "name text)";
    private final static String CREATE_GIF_WEB = "create table " + TB_GIF_WEB + "("
            + "id integer primary key autoincrement,"
            + "art_id integer, "
            + "web_url text, "
            + "title text, "
            + "pages integer, "
            + "time text)";
    private final static String CREATE_GIF_WEB_ITEM = "create table " + TB_GIF_WEB_ITEM + "("
            + "id integer primary key autoincrement,"
            + "art_id integer , "
            + "page integer , "
            + "title text, "
            + "url text)";
    private static final String DROP_BOOK =
            "DROP TABLE IF EXISTS " + TB_BOOK;
    private Context mCont;

    DBHelper(Context context) {
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
        db.execSQL(DROP_BOOK);
        onCreate(db);
    }
}
