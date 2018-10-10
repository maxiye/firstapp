package com.maxiye.first.api;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;

public class IPAddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipaddress);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.ip_input)));
    }

    public void ipaddress(View view) {
        ((TextView) findViewById(R.id.ip_address_ret)).setText("");
        String ip = ((EditText)findViewById(R.id.ip_input)).getText().toString();
        String finalIp = !ip.equals("") ? ip : "127.0.0.1";
        new Thread(() -> {
            String ret = ApiUtil.getInstance().getIPAddress(finalIp);
            runOnUiThread(() -> ((TextView) findViewById(R.id.ip_address_ret)).setText(ret));
        }).start();
    }
}
