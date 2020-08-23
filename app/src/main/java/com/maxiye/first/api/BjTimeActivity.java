package com.maxiye.first.api;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.Util;

/**
 * @author due
 */
public class BjTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bjtime);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.bj_time_ret)));
        bjTime(null);
    }

    public void bjTime(View view) {
        ((TextView) findViewById(R.id.bj_time_ret)).setText("");
        Util.getDefaultSingleThreadExecutor().execute(() -> {
            String ret = ApiUtil.getInstance().getBJTime();
            runOnUiThread(() -> ((TextView) findViewById(R.id.bj_time_ret)).setText(ret));
        });
    }
}
