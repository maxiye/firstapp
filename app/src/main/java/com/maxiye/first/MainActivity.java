package com.maxiye.first;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements BlankFragment.OnFrgActionListener {
    public final static String EXTRA_MESSAGE = "com.firstapp.com.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText et = (EditText)findViewById(R.id.edit_message);
        et.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keycode, KeyEvent ke){
                if(keycode==KeyEvent.KEYCODE_ENTER){//修改回车键功能
                    sendMsgNow(v);
                }
                return false;
            }
        });
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            BlankFragment frg = new BlankFragment();
            frg.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frg).commit();
        }
    }
    //ListView项目点击事件，实现OnFrgActionListener接口
    public void onItemClick(View view) {
        TextView tv = (TextView)view;
        String str = tv.getText().toString().split("：")[1];
        ClipboardManager clm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clm.setPrimaryClip(ClipData.newPlainText(null,str));
        Toast.makeText(this,R.string.clip_toast,Toast.LENGTH_SHORT).show();
    }
    public void sendMsg(View view){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText et = (EditText)findViewById(R.id.edit_message);
        String msg = et.getText().toString();
        intent.putExtra(EXTRA_MESSAGE,msg);
        startActivity(intent);
    }
    //查询应用
    public void sendMsgNow(View view){
        BlankFragment frg = new BlankFragment();
        EditText et = (EditText)findViewById(R.id.edit_message);
        String msg = et.getText().toString();
        Bundle args = new Bundle();
        args.putString(BlankFragment.ARG_1,msg);
        frg.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,frg).addToBackStack(null).commit();
    }
}
