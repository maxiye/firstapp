package com.firstapp.com.first;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        Intent intent = getIntent();
        String msg = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        TextView tv = new TextView(this);
        tv.setTextSize(40);
        tv.setText(msg);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
        layout.addView(tv);
    }
}
