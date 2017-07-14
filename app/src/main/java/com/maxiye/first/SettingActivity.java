package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;


public class SettingActivity extends AppCompatActivity {
    private SharedPreferences sp;
    public final static String SETTING = "com.maxiye.first.SETTING";
    public final static String SHOW_SYSTEM = "show_system_apps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        /*Intent intent = getIntent();
        String msg = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView tv = new TextView(this);
        tv.setTextSize(40);
        tv.setText(msg);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_setting);
        layout.addView(tv);*/
        sp = getSharedPreferences(SETTING, Context.MODE_PRIVATE);
        //初始化设置
        initSetting();
    }

    private void initSetting() {
        Switch show_system_apps = (Switch) findViewById(R.id.show_system_apps);
        show_system_apps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SHOW_SYSTEM, isChecked).apply();
            }
        });
        show_system_apps.setChecked(sp.getBoolean(SHOW_SYSTEM, false));
    }
}
