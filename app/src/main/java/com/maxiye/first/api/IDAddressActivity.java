package com.maxiye.first.api;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;

public class IDAddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idaddress);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.id_input)));
    }

    public void idAddress(View view) {
        ((TextView) findViewById(R.id.id_address_ret)).setText("");
        String id = ((EditText)findViewById(R.id.id_input)).getText().toString();
        String finalId = !id.equals("") ? id : "420701197002278239";
        new Thread(() -> {
            String ret = ApiUtil.getInstance().getIDAddress(finalId);
            runOnUiThread(() -> ((TextView) findViewById(R.id.id_address_ret)).setText(ret));
        }).start();
    }
}
