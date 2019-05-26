package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

/**
 * @author due
 */
public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sp;
    public static final String SETTING = "com.maxiye.first.SETTING";
    /**
     * 是否显示所有应用
     */
    public static final String SHOW_SYSTEM = "show_system_apps";
    /**
     * 上次备份时间
     */
    public static final String BACKUP_TIME = "backup_time";
    /**
     * 书签json数据
     */
    public static final String BOOKMARK = "bookmark";
    /**
     * web-dav user
     */
    public static final String WEBDAV_USER = "webdav_user";
    /**
     * web-dav pwd
     */
    public static final String WEBDAV_PWD = "webdav_pwd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        //初始化设置
        initSetting();
    }

    /**
     * {@code 第42条：lambda表达式优于匿名类}
     */
    private void initSetting() {
        Switch showSystemApps = findViewById(R.id.setting_show_system);
        TextView webdavUser = findViewById(R.id.webdav_user_view);
        TextView webdavPwd = findViewById(R.id.webdav_pwd_view);
        showSystemApps.setChecked(sp.getBoolean(SHOW_SYSTEM, false));
        webdavUser.setText(sp.getString(WEBDAV_USER, getText(R.string.not_set).toString()));
        webdavPwd.setText(sp.getString(WEBDAV_PWD, getText(R.string.not_set).toString()));
        showSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(SHOW_SYSTEM, isChecked)
                    .apply();
        });
        findViewById(R.id.webdav_user_cfg).setOnClickListener(view -> editWebdavUser());
        findViewById(R.id.webdav_pwd_cfg).setOnClickListener(view -> editWebdavPwd());

    }

    private void editWebdavPwd() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, findViewById(R.id.webdav_pwd_cfg), false);
        EditText editor = view.findViewById(R.id.dialog_input);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editor.setHint(R.string.webdav_password_tip);
        editor.setText(sp.getString(WEBDAV_PWD, ""));
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.webdav_password)
                // 添加布局
                .setView(view)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> {
                    String input = editor.getText().toString();
                    sp.edit().putString(WEBDAV_PWD, input)
                            .apply();
                    TextView webdavPwd = findViewById(R.id.webdav_pwd_view);
                    webdavPwd.setText(input);
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog.show();
    }

    private void editWebdavUser() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, findViewById(R.id.webdav_user_cfg), false);
        EditText editor = view.findViewById(R.id.dialog_input);
        editor.setText(sp.getString(WEBDAV_USER, ""));
        editor.setHint(R.string.webdav_user_tip);
        editor.setInputType(InputType.TYPE_CLASS_TEXT);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(R.string.webdav_user)
                // 添加布局
                .setView(view)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> {
                    String input = editor.getText().toString();
                    sp.edit().putString(WEBDAV_USER, input)
                            .apply();
                    TextView webdavUser = findViewById(R.id.webdav_user_view);
                    webdavUser.setText(input);
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog.show();
    }
}
