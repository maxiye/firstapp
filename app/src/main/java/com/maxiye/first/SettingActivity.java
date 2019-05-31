package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.maxiye.first.util.StringUtil;

import java.util.function.Consumer;

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
        showEditDialog(
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
        showEditDialog(
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
        showEditDialog(
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

    @SuppressWarnings("unused")
    private void showEditDialog(String title, String value,@NonNull Consumer<String> consumer) {
        this.showEditDialog(title, value, InputType.TYPE_CLASS_TEXT, null, consumer);
    }

    /**
     * 默认的编辑修改弹框
     * @param title 标题
     * @param value 编辑框默认值
     * @param inputType 输入类型，默认为{@link InputType#TYPE_CLASS_TEXT}
     * @param hint 输入框的hint
     * @param consumer 成功回调
     */
    private void showEditDialog(String title, String value, int inputType, @Nullable String hint, @NonNull Consumer<String> consumer) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edittext, findViewById(R.id.setting_layout), false);
        EditText editor = view.findViewById(R.id.dialog_input);
        editor.setText(value);
        if (StringUtil.notBlank(hint)) {
            editor.setHint(hint);
        }
        editor.setInputType(inputType);
        //创建对话框
        AlertDialog dialog = new AlertDialog.Builder(this)
                // 设置图标
                .setIcon(R.drawable.ic_info_black_24dp)
                // 设置标题
                .setTitle(title)
                // 添加布局
                .setView(view)
                .setPositiveButton(R.string.confirm, (dialog1, which) -> {
                    String input = editor.getText().toString();
                    consumer.accept(input);
                }).setNegativeButton(R.string.cancel, (dialog1, which) -> {
                })
                .create();
        dialog.show();
    }
}
