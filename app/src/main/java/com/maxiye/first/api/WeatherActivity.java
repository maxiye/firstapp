package com.maxiye.first.api;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.maxiye.first.R;
import com.maxiye.first.part.WeatherRetRvAdapter;
import com.maxiye.first.util.ApiUtil;

import java.util.List;
import java.util.Objects;

public class WeatherActivity extends AppCompatActivity {
    private WeatherRetRvAdapter ma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        RecyclerView rv = findViewById(R.id.weather_ret_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.gif_rv_divider)));
        rv.addItemDecoration(divider);//分隔线
        ma = new WeatherRetRvAdapter();
        rv.setAdapter(ma);
        query(null);
    }
    @SuppressWarnings("unused")
    private void alert(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void query(View view) {
        ma.setData(null);
        ma.notifyDataSetChanged();
        String weaid = ((EditText)findViewById(R.id.weaid)).getText().toString();
        String finalWeaid = !weaid.equals("") ? weaid : "上海";
        new Thread(() -> {
            List<String[]> ret = ApiUtil.getInstance().getWeather(finalWeaid, this.getCacheDir());
            runOnUiThread(() -> {
                ma.setData(ret);
                ma.notifyDataSetChanged();
            });
        }).start();
    }
}
