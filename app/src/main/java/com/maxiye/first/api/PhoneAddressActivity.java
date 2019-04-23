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
        String finalPhone = StringUtils.notBlank(phone) ? phone : "13641635423";
        Util.getDefaultSingleThreadExecutor().execute(() -> {
            String ret = ApiUtil.getInstance().getPhoneAddress(finalPhone);
            runOnUiThread(() -> ((TextView) findViewById(R.id.phone_address_ret)).setText(ret));
        });
    }
}
