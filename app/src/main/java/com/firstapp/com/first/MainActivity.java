package com.firstapp.com.first;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.firstapp.com.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void sendMsg(View view){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText et = (EditText)findViewById(R.id.edit_message);
        String msg = et.getText().toString();
        intent.putExtra(EXTRA_MESSAGE,msg);
        startActivity(intent);
    }
}
