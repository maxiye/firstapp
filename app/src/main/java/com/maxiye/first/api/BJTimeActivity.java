package com.maxiye.first.api;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;

public class BJTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bjtime);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.bj_time_ret)));
        bjTime(null);
    }

    public void bjTime(View view) {
        ((TextView) findViewById(R.id.bj_time_ret)).setText("");
        new Thread(() -> {
            String ret = ApiUtil.getInstance().getBJTime();
            runOnUiThread(() -> ((TextView) findViewById(R.id.bj_time_ret)).setText(ret));
        }).start();
    }
}
