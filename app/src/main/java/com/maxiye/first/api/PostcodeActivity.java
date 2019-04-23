package com.maxiye.first.api;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.StringUtils;
import com.maxiye.first.util.Util;

/**
 * @author due
 */
public class PostcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postcode);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.area_nm_input)));
    }

    public void postcode(View view) {
        ((TextView) findViewById(R.id.postcode_ret)).setText("");
        String area = ((EditText)findViewById(R.id.area_nm_input)).getText().toString();
        String finalArea = StringUtils.notBlank(area) ? area : "广东省广东市";
        Util.getDefaultSingleThreadExecutor().execute(() -> {
            String ret = ApiUtil.getInstance().getPostcode(finalArea);
            runOnUiThread(() -> ((TextView) findViewById(R.id.postcode_ret)).setText(ret));
        });
    }
}
