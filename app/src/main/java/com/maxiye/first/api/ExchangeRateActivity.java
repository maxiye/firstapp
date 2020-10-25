package com.maxiye.first.api;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maxiye.first.R;
import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.StringUtil;
import com.maxiye.first.util.Util;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author due
 */
@SuppressWarnings("unused")
public class ExchangeRateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.scur)));
        exchange(findViewById(R.id.exchRate));
    }

    @SuppressWarnings("unused")
    private void alert(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void exchange(View view) {
        ((TextView) findViewById(R.id.curRet)).setText("");
        String scur = ((EditText)findViewById(R.id.scur)).getText().toString();
        String tcur = ((EditText)findViewById(R.id.tcur)).getText().toString();
        String finalScur = StringUtil.notBlank(scur) ? scur : "USD";
        String finalTcur = StringUtil.notBlank(tcur) ? tcur : "CNY";
        Util.getDefaultSingleThreadExecutor().execute(() -> {
            String ret = ApiUtil.getInstance().getExchangeRate(finalScur, finalTcur);
            runOnUiThread(() -> ((TextView) findViewById(R.id.curRet)).setText(ret));
        });
    }
}
