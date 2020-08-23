package com.maxiye.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Switch;
import android.widget.TextView;

import com.maxiye.first.util.StringUtil;
import com.maxiye.first.util.Util;


public class CommonActivity extends AppCompatActivity {
    public static final String TYPE = "type";
    public static final String TYPE_PROXY = "proxy_setting";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String type = bundle.getString(TYPE, TYPE_PROXY);
            if (TYPE_PROXY.equals(type)) {
                new ProxySetting().init();
            }
        }
    }

    /**
     * 代理设置
     */
    class ProxySetting {
        /**
         * 初始化代理设置页面
         */
        void init() {
            setContentView(R.layout.setting_proxy);
            String proxyAddress =
                    Settings.Global.getString(getContentResolver(), Settings.Global.HTTP_PROXY);
            SharedPreferences sp = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            TextView host = findViewById(R.id.proxy_host_view);
            TextView port = findViewById(R.id.proxy_port_view);
            Switch proxyOnOff = findViewById(R.id.proxy_on_off);
            proxyOnOff.setChecked(StringUtil.notBlank(proxyAddress));
            host.setText(sp.getString(SettingActivity.PROXY_HOST, getText(R.string.not_set).toString()));
            port.setText(sp.getString(SettingActivity.PROXY_PORT, getText(R.string.not_set).toString()));
            proxyOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // 无权限执行
                /*if (isChecked) {
                    String proxy = host.getText().toString() + ":" + port.getText().toString();
                    Settings.Global.putString(getContentResolver(), Settings.Global.HTTP_PROXY, proxy);
                } else {
                    Settings.Global.putString(getContentResolver(), Settings.Global.HTTP_PROXY, "");
                }
                try (InputStream is = Runtime.getRuntime().exec("settings list global").getInputStream()) {
                    byte[] bytes = new byte[is.available()];
                    int len = is.read(bytes);
                    if (len >0) {
                        String ret = new String(bytes);
                        AlertDialog dialog = new AlertDialog.Builder(CommonActivity.this)
                                .setMessage(ret)
                                .setTitle("settings")
                                .create();
                        dialog.show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            });
            findViewById(R.id.proxy_host_cfg).setOnClickListener(view -> editProxyHost());
            findViewById(R.id.proxy_port_cfg).setOnClickListener(view -> editProxyPort());
        }

        /**
         * 编辑代理主机弹窗
         */
        private void editProxyHost() {
            SharedPreferences sp = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            Util.showEditDialog(
                    CommonActivity.this,
                    getString(R.string.host),
                    sp.getString(SettingActivity.PROXY_HOST, ""),
                    InputType.TYPE_CLASS_TEXT,
                    "127.0.0.1",
                    input -> {
                        sp.edit().putString(SettingActivity.PROXY_HOST, input)
                                .apply();
                        TextView textView = findViewById(R.id.proxy_host_view);
                        textView.setText(input);
                    });
        }

        /**
         * 编辑代理端口弹窗
         */
        private void editProxyPort() {
            SharedPreferences sp = getSharedPreferences(SettingActivity.SETTING, Context.MODE_PRIVATE);
            Util.showEditDialog(
                    CommonActivity.this,
                    getString(R.string.port),
                    sp.getString(SettingActivity.PROXY_PORT, ""),
                    InputType.TYPE_CLASS_TEXT,
                    "8888",
                    input -> {
                        sp.edit().putString(SettingActivity.PROXY_PORT, input).apply();
                        TextView textView = findViewById(R.id.proxy_port_view);
                        textView.setText(input);
                    });
        }
    }
}
