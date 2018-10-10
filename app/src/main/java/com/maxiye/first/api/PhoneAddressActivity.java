package com.maxiye.first.api;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;

public class PhoneAddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneaddress);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.phone_input)));
    }

    public void phoneAddress(View view) {
        ((TextView) findViewById(R.id.phone_address_ret)).setText("");
        String phone = ((EditText)findViewById(R.id.phone_input)).getText().toString();
        String finalPhone = !phone.equals("") ? phone : "13641635423";
        new Thread(() -> {
            String ret = ApiUtil.getInstance().getPhoneAddress(finalPhone);
            runOnUiThread(() -> ((TextView) findViewById(R.id.phone_address_ret)).setText(ret));
        }).start();
    }
}
