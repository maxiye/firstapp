package com.maxiye.first.api;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.maxiye.first.R;
import com.maxiye.first.part.WeatherRetRvAdapter;
import com.maxiye.first.util.ApiUtil;
import com.maxiye.first.util.StringUtil;
import com.maxiye.first.util.Util;

import java.util.List;
import java.util.Objects;

/**
 * @author due
 */
public class WeatherActivity extends AppCompatActivity {
    private WeatherRetRvAdapter ma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        findViewById(android.R.id.content).setOnLongClickListener((v) -> ApiUtil.showPopupmenu(this, findViewById(R.id.weaid)));
        RecyclerView rv = findViewById(R.id.weather_ret_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        divider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.gif_rv_divider)));
        // 分隔线
        rv.addItemDecoration(divider);
        ma = new WeatherRetRvAdapter();
        rv.setAdapter(ma);
        query(null);
    }
    @SuppressWarnings("unused")
    private void alert(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void query(View view) {
        ma.setData(null);
        ma.notifyDataSetChanged();
        String weaid = ((EditText)findViewById(R.id.weaid)).getText().toString();
        String finalWeaid = StringUtil.notBlank(weaid) ? weaid : "上海";
        Util.getDefaultSingleThreadExecutor().execute(() -> {
            List<String[]> ret = ApiUtil.getInstance().getWeather(finalWeaid, this.getCacheDir());
            runOnUiThread(() -> {
                ma.setData(ret);
                ma.notifyDataSetChanged();
            });
        });
    }
}
