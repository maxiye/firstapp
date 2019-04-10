package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Switch;


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
        showSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(SHOW_SYSTEM, isChecked).apply();
        });
        showSystemApps.setChecked(sp.getBoolean(SHOW_SYSTEM, false));
    }
}
