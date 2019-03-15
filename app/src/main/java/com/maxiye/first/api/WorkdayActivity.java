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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author due
 */
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
        String finalDate = StringUtils.isBlank(date) ? DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now()) : date;
        Util.getDefaultSingleThreadExecutor().execute(() -> {
            String ret = ApiUtil.getInstance().getWorkday(finalDate);
            runOnUiThread(() -> ((TextView) findViewById(R.id.workday_ret)).setText(ret));
        });
    }
}
