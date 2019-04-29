package com.maxiye.first.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.maxiye.first.GifActivity;
import com.maxiye.first.R;
import com.maxiye.first.SettingActivity;
import com.maxiye.first.MainActivity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**数据库助手
 * {@code 第27条：消除未检查警告 }
 * SuppressWarnings注解可以在任意声明上使用，从单独的局部变量到整个类都可以。
 * 应该在尽可能小的作用域上使用SuppressWarnings注解。它通常是个变量声明或者是一个非常短的方法或者是构造器。
 * 永远不要在整个类上使用SuppressWarnings注解，这么做会掩盖某些重要的警告
 * @author due
 * @date 2017-05-25
 */
public class DbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "first.db";
    private static final int DB_VERSION = 5;
    public static final String TB_BOOK = "book";
    public static final String TB_IMG_WEB = "img_web";
    public static final String TB_IMG_WEB_ITEM = "img_web_item";
    public static final String TB_IMG_FAVORITE = "img_favorite";
    private static final String CREATE_BOOK = "create table " + TB_BOOK + "("
            + "id integer primary key autoincrement,"
            + "author text default '', "
            + "price real default 0, "
            + "pages integer default 0, "
            + "name text default '')";
    private static final String CREATE_IMG_WEB = "create table " + TB_IMG_WEB + "("
            + "id integer primary key autoincrement,"
            + "web_name text default '', "
            + "type text default '', "//gif|bitmap
            + "art_id TEXT(35) default 0, "
            + "web_url text default '', "
            + "title text default '', "
            + "pages integer default 0, "
            + "time text default '')";
    /**
     * 索引名不能相同
     */
    private static final String INDEX_IMG_WEB = "CREATE INDEX IF NOT EXISTS web_idx_art_id on " + TB_IMG_WEB + " (art_id, web_name)";
    private static final String CREATE_IMG_WEB_ITEM = "create table " + TB_IMG_WEB_ITEM + "("
            + "id integer primary key autoincrement,"
            + "web_name text default '', "
            + "type text default '', "
            + "art_id TEXT(35) default 0, "
            + "page integer default 0, "
            + "title text default '', "
            + "url text default '', "
            + "real_url text default '', "//真实图片地址
            + "fav_flg integer default 0, "//1已收藏，0未收藏
            + "ext text default '', "//gif|jpg|jpeg|bmp|png
            + "time text default '')";
    private static final String INDEX_IMG_WEB_ITEM = "CREATE INDEX IF NOT EXISTS web_item_idx_art_id on " + TB_IMG_WEB_ITEM + " (art_id, web_name)";


    private static final String CREATE_IMG_FAVORITE = "create table " + TB_IMG_FAVORITE + "("
            + "id integer primary key autoincrement, "
            + "item_id integer default 0, "
            + "web_name text default '', "
            + "type text default '', "
            + "art_id TEXT(35) default 0, "
            + "page integer default 0, "
            + "title text default '', "
            + "url text default '', "
            + "real_url text default '', "
            + "ext text default '', "//gif|jpg|jpeg|bmp|png
            + "time text default '')";
    private static final String INDEX_IMG_FAVORITE = "CREATE INDEX IF NOT EXISTS img_favorite_idx_art_id on " + TB_IMG_WEB_ITEM + " (art_id, web_name)";

    private static final String DROP_BOOK = "DROP TABLE IF EXISTS " + TB_BOOK;
    private static final String DROP_IMG_WEB = "DROP TABLE IF EXISTS " + TB_IMG_WEB;
    private static final String DROP_IMG_WEB_ITEM = "DROP TABLE IF EXISTS " + TB_IMG_WEB_ITEM;
    private final Context mCont;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mCont = context;
        checkBakup();
    }

    public static SQLiteDatabase newDb(Context context) {
        return new DbHelper(context).getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//只要没.db文件时才执行
        db.execSQL(CREATE_BOOK);
        db.execSQL(CREATE_IMG_WEB);
        db.execSQL(INDEX_IMG_WEB);
        db.execSQL(CREATE_IMG_WEB_ITEM);
        db.execSQL(INDEX_IMG_WEB_ITEM);
        db.execSQL(CREATE_IMG_FAVORITE);
        db.execSQL(INDEX_IMG_FAVORITE);
        Toast.makeText(mCont, "Create succeeded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            update1to2(db);
        }
        if (oldVersion == 2) {
            update2to3(db);
        }
        if (oldVersion == 3) {
            update3to4(db);
        }
        if (oldVersion == 4) {
            update4to5(db);
        }
        Toast.makeText(mCont, "Update succeeded", Toast.LENGTH_SHORT).show();
    }

    private void update1to2(@NonNull SQLiteDatabase db) {
        Cursor cur1 = db.rawQuery("select * from gif_web where art_id > 1000000", new String[]{});
        Cursor cur2 = db.rawQuery("select * from gif_web_item where art_id > 1000000", new String[]{});
        MyLog.w("dd", cur1.getCount() + "");
        MyLog.w("dd", cur2.getCount() + "");
        db.execSQL(DROP_BOOK);
        db.execSQL(DROP_IMG_WEB);
        db.execSQL(DROP_IMG_WEB_ITEM);
        onCreate(db);
        cur1.moveToFirst();
        for (int i = 0; i < cur1.getCount(); i++) {
            ContentValues ctv = new ContentValues(6);
            ctv.put("art_id", cur1.getInt(cur1.getColumnIndex("art_id")));
            ctv.put("pages", cur1.getInt(cur1.getColumnIndex("pages")));
            ctv.put("web_name", "gamersky");
            ctv.put("web_url", cur1.getString(cur1.getColumnIndex("web_url")));
            ctv.put("title", cur1.getString(cur1.getColumnIndex("title")));
            ctv.put("time", cur1.getString(cur1.getColumnIndex("time")));
            long newId = db.insert(TB_IMG_WEB, null, ctv);
            MyLog.w("dd", newId + "");
            cur1.moveToNext();
        }
        cur1.close();
        cur2.moveToFirst();
        for (int i = 0; i < cur2.getCount(); i++) {
            ContentValues ctv = new ContentValues(5);
            ctv.put("art_id", cur2.getInt(cur2.getColumnIndex("art_id")));
            ctv.put("page", cur2.getInt(cur2.getColumnIndex("page")));
            ctv.put("web_name", "gamersky");
            ctv.put("url", cur2.getString(cur2.getColumnIndex("url")));
            ctv.put("title", cur2.getString(cur2.getColumnIndex("title")));
            long newId = db.insert(TB_IMG_WEB_ITEM, null, ctv);
            MyLog.w("dd", newId + "");
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

    private void update4to5(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE img_web_item ADD COLUMN real_url text DEFAULT '';");
        db.execSQL("ALTER TABLE img_web_item ADD COLUMN fav_flg integer DEFAULT 0;");
        db.execSQL("ALTER TABLE img_web_item ADD COLUMN time text DEFAULT '';");
        db.execSQL(CREATE_IMG_FAVORITE);
        db.execSQL(INDEX_IMG_FAVORITE);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void backup(Context context) {
        File db = context.getDatabasePath(DB_NAME);
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File bak = new File(downloadDir, DB_NAME + ".bak." + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        try {
            Files.copy(db.toPath(), bak.toPath());
            Toast.makeText(context, "Success：" + bak.getPath(), Toast.LENGTH_SHORT).show();
            context.getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).edit().putLong(SettingActivity.BACKUP_TIME, System.currentTimeMillis()).apply();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error：" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        File[] baks = downloadDir.listFiles((file, name) -> name.contains(DB_NAME + ".bak"));
        int maxLength = 5;
        if (baks != null && baks.length > maxLength) {
            Arrays.sort(baks, (o1, o2) -> (int) (o1.lastModified() - o2.lastModified()));
            MyLog.w("backdb", Arrays.toString(baks));
            for (int i = 0,k = baks.length - maxLength; i < k; i++) {
                baks[i].delete();
            }
        }
    }

    public static void restore(MainActivity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.choose_a_backup)), MainActivity.INTENT_PICK_DB_BAK_REQCODE);
    }

    /**
     * 大于五天备份
     */
    private void checkBakup() {
        long lastBackupTime = mCont.getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE).getLong(SettingActivity.BACKUP_TIME, 0);
        if (System.currentTimeMillis() - lastBackupTime > 86400 * 5 * 1000) {
            backup(mCont);
        }
    }

    public int scanIntoFav() {
        int count = 0;
        String[] types = GifActivity.getTypeList();
        SQLiteDatabase db = getWritableDatabase();
        String datetime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        for(String type : types) {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type);
            String[] fileList = dir.list((file, s) -> file.isFile());
            if (fileList != null) {
                for (String fName : fileList) {
                    Cursor cus = db.query(DbHelper.TB_IMG_WEB_ITEM, new String[]{"*"}, "title = ?", new String[]{fName}, null, null, "id desc", "1");
                    if (cus.getCount() > 0) {
                        cus.moveToFirst();
                        int favFlg = cus.getInt(cus.getColumnIndex("fav_flg"));
                        if (favFlg != 1) {
                            ContentValues ctv = new ContentValues(10);
                            ctv.put("item_id", cus.getInt(cus.getColumnIndex("id")));
                            ctv.put("art_id", cus.getInt(cus.getColumnIndex("art_id")));
                            ctv.put("page", cus.getInt(cus.getColumnIndex("page")));
                            ctv.put("web_name", cus.getString(cus.getColumnIndex("web_name")));
                            ctv.put("type", cus.getString(cus.getColumnIndex("type")));
                            ctv.put("title", cus.getString(cus.getColumnIndex("title")));
                            ctv.put("url", cus.getString(cus.getColumnIndex("url")));
                            ctv.put("ext", cus.getString(cus.getColumnIndex("ext")));
                            ctv.put("real_url", cus.getString(cus.getColumnIndex("real_url")));
                            ctv.put("time", datetime);
                            long newId = db.insert(TB_IMG_FAVORITE, null, ctv);
                            MyLog.w("db_img_fav_insert: ", newId + "");
                            ContentValues ctv2 = new ContentValues(1);
                            ctv2.put("fav_flg", 1);
                            int rows = db.update(DbHelper.TB_IMG_WEB_ITEM, ctv2, "id = ?", new String[]{cus.getString(cus.getColumnIndex("id"))});
                            MyLog.w("db_web_item_update: ", rows + "");
                            count++;
                        }
                        cus.close();
                    }
                }
            }
        }
        return count;

    }

    /**
     * {@code 第58条：for-each循环优于传统for循环}
     * @return int
     */
    public int fixFavFile() {
        int count = 0;
        String[] types = GifActivity.getTypeList();
        SQLiteDatabase db = getWritableDatabase();
        for(String type : types) {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + type);
            File[] fileList = dir.listFiles(File::isFile);
            if (fileList != null) {
                for (File file : fileList) {
                    String fName = file.getName();
                    Cursor cus = db.query(DbHelper.TB_IMG_FAVORITE, new String[]{"id"}, "title = ?", new String[]{fName}, null, null, "id desc", "1");
                    if (cus.getCount() > 0) {
                        cus.moveToFirst();
                        String favId = cus.getString(cus.getColumnIndex("id"));
                        if (file.renameTo(new File(dir, favId + "_" + fName))) {
                            count++;
                        }
                        cus.close();
                    }
                }
            }
        }
        return count;
    }
}
