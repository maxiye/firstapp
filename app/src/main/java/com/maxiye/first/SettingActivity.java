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
    public final static String SETTING = "com.maxiye.first.SETTING";
    public final static String SHOW_SYSTEM = "show_system_apps";
    public final static String BACKUP_TIME = "backup_time";
    public final static String BOOKMARK = "bookmark";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        //初始化设置
        initSetting();
    }

    private void initSetting() {
        Switch showSystemApps = findViewById(R.id.setting_show_system);
        showSystemApps.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(SHOW_SYSTEM, isChecked).apply();
        });
        showSystemApps.setChecked(sp.getBoolean(SHOW_SYSTEM, false));
    }
}
