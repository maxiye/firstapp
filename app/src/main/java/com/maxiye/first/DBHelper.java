package com.maxiye.first;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Administrator on 2017-05-25.
 */

public class DBHelper extends SQLiteOpenHelper {
    private final static String DB_NAME = "first.db";
    private final static int DB_VERSION = 1;
    public final static String TB_BOOK = "book";
    public final static String CREATE_BOOK = "create table " + TB_BOOK + "("
            + "id integer primary key autoincrement,"
            + "author text, "
            + "price real, "
            + "pages integer, "
            + "name text)";
    private static final String DROP_BOOK =
            "DROP TABLE IF EXISTS "+TB_BOOK;
    private Context mCont;
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mCont = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//只要没.db文件时才执行
        db.execSQL(CREATE_BOOK);
        Toast.makeText(mCont, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_BOOK);
        onCreate(db);
    }
}
