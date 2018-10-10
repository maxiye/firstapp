package com.maxiye.first.api;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkdayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workday);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.work_day_input)));
        workdayQuery(null);
    }

    public void workdayQuery(View view) {
        ((TextView) findViewById(R.id.workday_ret)).setText("");
        String date = ((EditText)findViewById(R.id.work_day_input)).getText().toString();
        String finalDate = !date.equals("") ? date : new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date());
        new Thread(() -> {
            String ret = ApiUtil.getInstance().getWorkday(finalDate);
            runOnUiThread(() -> ((TextView) findViewById(R.id.workday_ret)).setText(ret));
        }).start();
    }
}
