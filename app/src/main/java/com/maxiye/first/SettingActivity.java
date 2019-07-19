package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Switch;
import android.widget.TextView;

import com.maxiye.first.util.Util;

/**
 * @author due
 */
public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sp;
    public static final String SETTING = "com.maxiye.first.SETTING";
    /**
     * 是否显示所有应用
     * bool
     */
    public static final String SHOW_SYSTEM = "show_system_apps";
    /**
     * 上次备份时间
     * long
     */
    public static final String BACKUP_TIME = "backup_time";
    /**
     * 书签json数据
     * string
     */
    public static final String BOOKMARK = "bookmark";
    /**
     * web-dav user
     * string
     */
    public static final String WEBDAV_USER = "webdav_user";
    /**
     * web-dav pwd
     * string
     */
    public static final String WEBDAV_PWD = "webdav_pwd";
    /**
     * 是否开启web-dav备份
     * bool
     */
    public static final String WEBDAV_ON_OFF = "webdav_on_off";
    /**
     * 查重程度控制，越小越严格，默认5
     * int
     */
    public static final String DUPLICATE_LEVEL = "duplicate_level";
    /**
     * 代理ip
     * string
     */
    public static final String PROXY_HOST = "proxy_host";
    /**
     * 代理端口
     * string
     */
    public static final String PROXY_PORT = "proxy_port";
    /**
     * 是否开启代理
     * bool
     */
    public static final String PROXY_ON_OFF = "proxy_on_off";

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
        Switch webdavOnOff = findViewById(R.id.webdav_on_off);
        TextView webdavUser = findViewById(R.id.webdav_user_view);
        TextView webdavPwd = findViewById(R.id.webdav_pwd_view);
        TextView duplicateLevel = findViewById(R.id.duplicate_level);
        showSystemApps.setChecked(sp.getBoolean(SHOW_SYSTEM, false));
        webdavOnOff.setChecked(sp.getBoolean(WEBDAV_ON_OFF, false));
        webdavUser.setText(sp.getString(WEBDAV_USER, getText(R.string.not_set).toString()));
        webdavPwd.setText(sp.getString(WEBDAV_PWD, getText(R.string.not_set).toString()));
        duplicateLevel.setText(String.valueOf(sp.getInt(DUPLICATE_LEVEL, 5)));
        showSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> sp.edit()
                .putBoolean(SHOW_SYSTEM, isChecked)
                .apply());
        webdavOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> sp.edit()
                .putBoolean(WEBDAV_ON_OFF, isChecked)
                .apply());
        findViewById(R.id.webdav_user_cfg).setOnClickListener(view -> editWebdavUser());
        findViewById(R.id.webdav_pwd_cfg).setOnClickListener(view -> editWebdavPwd());
        findViewById(R.id.duplicate_level_cfg).setOnClickListener(view -> editDuplicateLevel());
    }

    private void editDuplicateLevel() {
        Util.showEditDialog(
                this,
                getString(R.string.duplicate_level),
                String.valueOf(sp.getInt(DUPLICATE_LEVEL, 5)),
                InputType.TYPE_CLASS_NUMBER,
                null,
                input -> {
                    sp.edit().putInt(DUPLICATE_LEVEL, Integer.valueOf(input))
                            .apply();
                    TextView duplicateLevel = findViewById(R.id.duplicate_level);
                    duplicateLevel.setText(input);
                });
    }

    private void editWebdavPwd() {
        Util.showEditDialog(
                this,
                getString(R.string.webdav_password),
                sp.getString(WEBDAV_PWD, ""),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
                getString(R.string.webdav_password_tip),
                input -> {
                    sp.edit().putString(WEBDAV_PWD, input)
                            .apply();
                    TextView webdavPwd = findViewById(R.id.webdav_pwd_view);
                    webdavPwd.setText(input);
                });
    }

    private void editWebdavUser() {
        Util.showEditDialog(
                this,
                getString(R.string.webdav_user),
                sp.getString(WEBDAV_USER, ""),
                InputType.TYPE_CLASS_TEXT,
                getString(R.string.webdav_user_tip),
                input -> {
                    sp.edit().putString(WEBDAV_USER, input).apply();
                    TextView webdavUser = findViewById(R.id.webdav_user_view);
                    webdavUser.setText(input);
                });
    }
}
